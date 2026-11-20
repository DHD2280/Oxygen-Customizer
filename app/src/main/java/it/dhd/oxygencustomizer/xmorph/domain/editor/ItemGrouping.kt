package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionItem
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemState
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode

object ItemGrouping {

    private val NESTED = Regex("^i\\d+\\.i(\\d+)\\.")

    fun group(composed: ClockFace, members: List<Int>, groups: Set<Int>, name: String): ClockFace? {
        if (members.size < 2) return null
        val large = subset(composed.large, members, groups) ?: return null
        val small = subset(composed.small, members, groups) ?: return null
        return ClockFace(
            aodLarge = composed.aodLarge?.let { subset(it, members, groups, large.placement) },
            aodSmall = composed.aodSmall?.let { subset(it, members, groups, small.placement) },
            name = name,
            layers = composed.layers.filter { FaceComposer.itemOf(it.id) in members }.map { layer ->
                val owner = FaceComposer.itemOf(layer.id)
                if (owner in groups) layer.copy(id = layer.id.removePrefix(FaceComposer.prefix(owner!!))) else layer
            },
            large = large,
            small = small,
            category = CustomFaces.GROUP_CATEGORY
        )
    }

    fun memberOf(layerId: String): Int? = NESTED.find(layerId)?.groupValues?.get(1)?.toInt()

    fun ungroup(group: ClockFace, item: CompositionItem): List<GroupMember> {
        val large = (group.large as? StackNode)?.children.orEmpty()
        val small = (group.small as? StackNode)?.children.orEmpty()
        val aodLarge = (group.aodLarge as? StackNode)?.children.orEmpty()
        val aodSmall = (group.aodSmall as? StackNode)?.children.orEmpty()
        return large.mapNotNull { largeChild ->
            val member = owner(largeChild) ?: return@mapNotNull null
            val smallChild = small.firstOrNull { owner(it) == member } ?: return@mapNotNull null
            val prefix = FaceComposer.prefix(member)
            val layers = group.layers.filter { it.id.startsWith(prefix) }
                .map { it.copy(id = it.id.removePrefix(prefix)) }
            if (layers.isEmpty()) return@mapNotNull null
            val aodLargeChild = aodLarge.firstOrNull { owner(it) == member }
            val aodSmallChild = aodSmall.firstOrNull { owner(it) == member }
            val hidden = buildSet {
                if (largeChild.placement.hidden || ItemState.LARGE in item.hidden) add(ItemState.LARGE)
                if (smallChild.placement.hidden || ItemState.SMALL in item.hidden) add(ItemState.SMALL)
                if (aodLargeChild?.placement?.hidden == true || ItemState.AOD_LARGE in item.hidden) add(
                    ItemState.AOD_LARGE
                )
                if (aodSmallChild?.placement?.hidden == true || ItemState.AOD_SMALL in item.hidden) add(
                    ItemState.AOD_SMALL
                )
            }
            GroupMember(
                source = member,
                face = ClockFace(
                    name = group.name,
                    layers = layers,
                    large = detach(largeChild, prefix, item.largeScale),
                    small = detach(smallChild, prefix, item.smallScale),
                    category = if (layers.size == 1) CustomFaces.ELEMENT_CATEGORY else CustomFaces.OWNED_CATEGORY
                ),
                large = offset(
                    largeChild.placement,
                    item.large ?: group.large.placement,
                    item.largeScale
                ),
                small = offset(
                    smallChild.placement,
                    item.small ?: group.small.placement,
                    item.smallScale
                ),
                aodLarge = aodLargeChild?.let {
                    offset(
                        it.placement,
                        item.aodLarge ?: item.large ?: group.aodLarge?.placement
                        ?: group.large.placement,
                        item.largeScale
                    )
                },
                aodSmall = aodSmallChild?.let {
                    offset(
                        it.placement,
                        item.aodSmall ?: item.small ?: group.aodSmall?.placement
                        ?: group.small.placement,
                        item.smallScale
                    )
                },
                hidden = hidden.takeIf { it.size < ItemState.entries.size }.orEmpty()
            )
        }
    }

    fun syncAod(awake: FaceNode, aod: FaceNode?): FaceNode? {
        val awakeStack = awake as? StackNode ?: return aod
        val aodStack = aod as? StackNode ?: return aod
        return awakeStack.copy(children = awakeStack.children.map { child ->
            val match =
                aodStack.children.firstOrNull { owner(it) == owner(child) } ?: return@map child
            placed(child, match.placement)
        })
    }

    private fun subset(
        root: FaceNode?,
        members: List<Int>,
        groups: Set<Int>,
        origin: FacePlacement? = null
    ): FaceNode? {
        val stack = root as? StackNode ?: return null
        val children = members.map { member ->
            val child = stack.children.firstOrNull { owner(it) == member } ?: return null
            if (member in groups) flatten(child, member) else listOf(child)
        }.flatten()
        val originX = origin?.x ?: children.minOf { it.placement.x }
        val originY = origin?.y ?: children.minOf { it.placement.y }
        return StackNode(
            children = children.map {
                placed(
                    it,
                    it.placement.copy(x = it.placement.x - originX, y = it.placement.y - originY)
                )
            },
            placement = FacePlacement(originX, originY)
        )
    }

    private fun flatten(group: FaceNode, id: Int): List<FaceNode> {
        val stack = group as? StackNode ?: return listOf(group)
        val prefix = FaceComposer.prefix(id)
        val origin = stack.placement
        return stack.children.map { child ->
            val placement = child.placement.copy(
                x = origin.x + child.placement.x,
                y = origin.y + child.placement.y,
                hidden = origin.hidden || child.placement.hidden
            )
            rename(placed(child, placement)) { it.removePrefix(prefix) }
        }
    }

    private fun offset(child: FacePlacement, group: FacePlacement, scale: Float): FacePlacement =
        FacePlacement(
            x = group.x + child.x * scale,
            y = group.y + child.y * scale,
            anchor = child.anchor,
            auto = child.auto || group.auto
        )

    private fun detach(node: FaceNode, prefix: String, scale: Float): FaceNode =
        rename(
            FaceEditor.scaleNode(
                placed(node, FacePlacement()),
                scale
            )
        ) { it.removePrefix(prefix) }

    private fun owner(node: FaceNode): Int? = firstLayer(node)?.let(FaceComposer::itemOf)

    private fun firstLayer(node: FaceNode): String? = when (node) {
        is LayerNode -> node.id
        is RowNode -> node.children.firstNotNullOfOrNull(::firstLayer)
        is ColumnNode -> node.children.firstNotNullOfOrNull(::firstLayer)
        is StackNode -> node.children.firstNotNullOfOrNull(::firstLayer)
    }

    private fun placed(node: FaceNode, placement: FacePlacement): FaceNode = when (node) {
        is LayerNode -> node.copy(placement = placement)
        is RowNode -> node.copy(placement = placement)
        is ColumnNode -> node.copy(placement = placement)
        is StackNode -> node.copy(placement = placement)
    }

    private fun rename(node: FaceNode, map: (String) -> String): FaceNode = when (node) {
        is LayerNode -> node.copy(id = map(node.id))
        is RowNode -> node.copy(children = node.children.map { rename(it, map) })
        is ColumnNode -> node.copy(children = node.children.map { rename(it, map) })
        is StackNode -> node.copy(children = node.children.map { rename(it, map) })
    }
}
