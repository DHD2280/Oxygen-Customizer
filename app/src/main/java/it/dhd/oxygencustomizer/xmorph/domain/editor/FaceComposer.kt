package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.ComposedFaces
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionItem
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemState
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode

object FaceComposer {

    private val ITEM = Regex("^i(\\d+)\\.")

    const val CLOCK_GROUP = "clock"

    fun groupOf(layerId: String): String = itemOf(layerId)?.toString() ?: CLOCK_GROUP

    fun prefix(id: Int): String = "i$id."

    fun itemOf(layerId: String): Int? = ITEM.find(layerId)?.groupValues?.get(1)?.toInt()

    fun isClockLayer(layerId: String): Boolean = itemOf(layerId) == null

    fun compose(parts: ComposedFaces): ClockFace {
        val clock = parts.clock
        val items = parts.composition.items
        if (items.size == 1 && items[0].clock && items[0].isDefault) return clock.copy(aboveSubject = emptySet())
        val entries = items.mapNotNull { item ->
            val face =
                if (item.clock) clock else parts.widgets[item.style] ?: return@mapNotNull null
            item to face
        }

        fun rename(item: CompositionItem, node: FaceNode): FaceNode =
            if (item.clock) node else ids(node) { prefix(item.id) + it }

        fun tree(
            state: ItemState,
            pick: (ClockFace) -> FaceNode,
            place: (CompositionItem, FaceNode) -> FacePlacement
        ): FaceNode = StackNode(
            children = entries.map { (item, face) ->
                val node = FaceEditor.scaleNode(pick(face), item.scale(state))
                val placement = place(item, node)
                placed(
                    rename(item, node),
                    if (item.visible(state)) placement else placement.copy(hidden = true)
                )
            }
        )

        fun smallSpot(item: CompositionItem, own: FacePlacement?, node: FaceNode): FacePlacement {
            if (item.clock) return own ?: node.placement
            return own?.takeUnless { it == node.placement } ?: node.placement.copy(auto = true)
        }

        val aod =
            items.any { it.hasAod } || entries.any { (_, face) -> face.aodLarge != null || face.aodSmall != null }
        return clock.copy(
            layers = entries.flatMap { (item, face) ->
                if (item.clock) face.layers else face.layers.map {
                    it.copy(
                        id = prefix(item.id) + it.id
                    )
                }
            },
            large = tree(ItemState.LARGE, { it.large }) { item, node ->
                item.large ?: node.placement
            },
            small = tree(ItemState.SMALL, { it.small }) { item, node ->
                smallSpot(
                    item,
                    item.small,
                    node
                )
            },
            aodLarge = if (aod) tree(
                ItemState.AOD_LARGE,
                { it.aodLarge ?: it.large }) { item, node ->
                item.aodLarge ?: item.large ?: node.placement
            } else null,
            aodSmall = if (aod) tree(
                ItemState.AOD_SMALL,
                { it.aodSmall ?: it.small }) { item, node ->
                smallSpot(
                    item,
                    item.aodSmall ?: item.small,
                    node
                )
            } else null,
            aboveSubject = entries.filter { (item, _) -> item.aboveSubject }
                .mapTo(HashSet()) { (item, _) -> if (item.clock) CLOCK_GROUP else item.id.toString() }
        )
    }

    fun decompose(face: ClockFace, parts: ComposedFaces): ComposedFaces {
        val items = parts.composition.items
        if (items.size == 1 && items[0].clock && items[0].isDefault) return parts.copy(clock = face)
        val large = face.large as? StackNode ?: return parts
        val small = face.small as? StackNode ?: return parts
        fun child(root: FaceNode?, item: CompositionItem): FaceNode? =
            (root as? StackNode)?.children?.firstOrNull { node ->
                val first = firstLayer(node) ?: return@firstOrNull false
                if (item.clock) isClockLayer(first) else itemOf(first) == item.id
            }

        val clockItem = items.firstOrNull { it.clock }
        fun unscaled(node: FaceNode?, item: CompositionItem, state: ItemState): FaceNode? =
            node?.let { FaceEditor.scaleNode(it, 1f / item.scale(state)) }

        val clock = clockItem?.let { item ->
            val largeChild = unscaled(child(large, item), item, ItemState.LARGE)
            val smallChild = unscaled(child(small, item), item, ItemState.SMALL)
            if (largeChild == null && smallChild == null) return@let null
            val clockLarge =
                largeChild?.let { placed(it, parts.clock.large.placement) } ?: parts.clock.large
            val clockSmall =
                smallChild?.let { placed(it, parts.clock.small.placement) } ?: parts.clock.small

            fun aodOf(
                root: FaceNode?,
                state: ItemState,
                own: FaceNode?,
                awake: FaceNode
            ): FaceNode? {
                val edited = unscaled(child(root, item), item, state)?.let {
                    placed(
                        it,
                        (own ?: awake).placement
                    )
                } ?: return own
                return if (own == null && edited == awake) null else edited
            }
            face.copy(
                aboveSubject = emptySet(),
                layers = face.layers.filter { isClockLayer(it.id) },
                large = clockLarge,
                small = clockSmall,
                aodLarge = aodOf(
                    face.aodLarge,
                    ItemState.AOD_LARGE,
                    parts.clock.aodLarge,
                    clockLarge
                ),
                aodSmall = aodOf(
                    face.aodSmall,
                    ItemState.AOD_SMALL,
                    parts.clock.aodSmall,
                    clockSmall
                )
            )
        } ?: parts.clock
        val widgets = parts.widgets.toMutableMap()
        val restored = HashSet<Int>()
        items.filterNot { it.clock }.forEach { item ->
            if (item.style in restored) return@forEach
            val original = parts.widgets[item.style] ?: return@forEach
            val prefix = prefix(item.id)
            val largeChild = unscaled(child(large, item), item, ItemState.LARGE)
            val smallChild = unscaled(child(small, item), item, ItemState.SMALL)
            if (largeChild == null && smallChild == null) return@forEach
            val rebuilt = original.copy(
                layers = face.layers.filter { it.id.startsWith(prefix) }
                    .map { it.copy(id = it.id.removePrefix(prefix)) },
                large = largeChild?.let {
                    placed(
                        ids(it) { id -> id.removePrefix(prefix) },
                        original.large.placement
                    )
                } ?: original.large,
                small = smallChild?.let {
                    placed(
                        ids(it) { id -> id.removePrefix(prefix) },
                        original.small.placement
                    )
                } ?: original.small
            )
            widgets[item.style] = rebuilt.copy(
                aodLarge = ItemGrouping.syncAod(rebuilt.large, original.aodLarge),
                aodSmall = ItemGrouping.syncAod(rebuilt.small, original.aodSmall)
            )
            restored += item.style
        }
        val placed = items.map { item ->
            fun shown(root: FaceNode?): FacePlacement? =
                child(root, item)?.placement?.takeUnless { it.hidden }

            fun spot(root: FaceNode?): FacePlacement? =
                child(root, item)?.placement?.copy(hidden = false)

            val hidden = buildSet {
                if (shown(large) == null) add(ItemState.LARGE)
                if (shown(small) == null) add(ItemState.SMALL)
                if (face.aodLarge != null && shown(face.aodLarge) == null) add(ItemState.AOD_LARGE)
                if (face.aodSmall != null && shown(face.aodSmall) == null) add(ItemState.AOD_SMALL)
            }

            fun aodSpot(
                root: FaceNode?,
                own: FacePlacement?,
                awake: FacePlacement?
            ): FacePlacement? {
                val edited = root?.let(::spot) ?: return own
                return if (own == null && edited == awake) null else edited
            }
            item.copy(
                hidden = hidden,
                large = spot(large) ?: item.large,
                small = spot(small) ?: item.small,
                aodLarge = aodSpot(face.aodLarge, item.aodLarge, spot(large) ?: item.large),
                aodSmall = aodSpot(face.aodSmall, item.aodSmall, spot(small) ?: item.small)
            )
        }
        return ComposedFaces(clock, widgets, parts.composition.copy(items = placed))
    }

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

    private fun ids(node: FaceNode, rename: (String) -> String): FaceNode = when (node) {
        is LayerNode -> node.copy(id = rename(node.id))
        is RowNode -> node.copy(children = node.children.map { ids(it, rename) })
        is ColumnNode -> node.copy(children = node.children.map { ids(it, rename) })
        is StackNode -> node.copy(children = node.children.map { ids(it, rename) })
    }
}
