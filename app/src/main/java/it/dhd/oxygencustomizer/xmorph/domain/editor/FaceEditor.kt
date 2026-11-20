package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.EditScope
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceSleepStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTextEffect
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StyleKey
import kotlin.math.abs

object FaceEditor {

    fun layer(face: ClockFace, id: String): FaceLayer? = face.layers.firstOrNull { it.id == id }

    fun node(face: ClockFace, large: Boolean, id: String): LayerNode? =
        find(if (large) face.large else face.small, id)

    fun read(face: ClockFace, id: String, scope: EditScope, key: StyleKey): Float {
        val layer = layer(face, id) ?: return 0f
        val node = node(face, scope != EditScope.SMALL, id)
        return when (key) {
            StyleKey.WEIGHT -> when (scope) {
                EditScope.AOD -> if (layer.sleepStyle.thin) layer.sleepWeight else node?.weight
                    ?: layer.weight

                else -> node?.weight ?: layer.weight
            }.toFloat()

            StyleKey.WIDTH -> when (scope) {
                EditScope.AOD -> layer.sleepWidth ?: node?.width ?: layer.width
                else -> node?.width ?: layer.width
            }.toFloat()

            StyleKey.HEIGHT -> when (scope) {
                EditScope.AOD -> layer.sleepHeight ?: node?.height ?: layer.height
                else -> node?.height ?: layer.height
            }.toFloat()

            StyleKey.TRACKING -> if (scope == EditScope.AOD) layer.tracking else node?.tracking
                ?: layer.tracking

            StyleKey.LINE_HEIGHT -> if (scope == EditScope.AOD) layer.lineHeight else node?.lineHeight
                ?: layer.lineHeight

            StyleKey.SIZE -> (node?.size ?: 0f) * if (scope == EditScope.AOD) layer.sleepScale
                ?: 1f else 1f

            StyleKey.ROTATION -> if (scope == EditScope.AOD) layer.sleepRotation ?: node?.rotation
            ?: 0f else node?.rotation ?: 0f

            StyleKey.TILT_X -> node?.tiltX ?: 0f
            StyleKey.TILT_Y -> node?.tiltY ?: 0f
            StyleKey.OPACITY -> if (scope == EditScope.AOD) layer.sleepOpacity ?: node?.alpha
            ?: 1f else node?.alpha ?: 1f

            StyleKey.EFFECT_TINT -> layer.effectTint ?: (readEffect(face, id, scope)
                ?: layer.effect).defaultTint

            StyleKey.OUTLINE_WIDTH -> layer.outlineWidth
            StyleKey.STRETCH -> if (scope == EditScope.AOD) layer.sleepStretch ?: node?.stretch
            ?: 1f else node?.stretch ?: 1f

            StyleKey.STRETCH_X -> if (scope == EditScope.AOD) layer.sleepStretchX ?: node?.stretchX
            ?: 1f else node?.stretchX ?: 1f
        }
    }

    fun write(
        face: ClockFace,
        ids: List<String>,
        scope: EditScope,
        key: StyleKey,
        value: Float
    ): ClockFace =
        ids.fold(face) { current, id -> writeOne(current, id, scope, key, value) }

    fun readEffect(face: ClockFace, id: String, scope: EditScope): FaceTextEffect? {
        val layer = layer(face, id) ?: return null
        return when (scope) {
            EditScope.AOD -> layer.effect
            else -> node(face, scope == EditScope.LARGE, id)?.effect ?: layer.effect
        }
    }

    fun writeEffect(
        face: ClockFace,
        ids: List<String>,
        scope: EditScope,
        effect: FaceTextEffect
    ): ClockFace =
        ids.fold(face) { current, id ->
            when (scope) {
                EditScope.AOD -> updateBoth(
                    updateLayer(current, id) { it.copy(effect = effect) },
                    id
                ) { it.copy(effect = null) }

                else -> updateNode(
                    current,
                    scope == EditScope.LARGE,
                    id
                ) { it.copy(effect = effect) }
            }
        }

    fun readColor(face: ClockFace, id: String, scope: EditScope): String {
        val layer = layer(face, id) ?: return ""
        return when (scope) {
            EditScope.AOD -> layer.sleepColor ?: layer.color
            else -> node(face, scope == EditScope.LARGE, id)?.color ?: layer.color
        }
    }

    fun writeColor(face: ClockFace, ids: List<String>, scope: EditScope, color: String): ClockFace =
        ids.fold(face) { current, id ->
            when (scope) {
                EditScope.AOD -> updateLayer(current, id) { it.copy(sleepColor = color) }
                else -> updateNode(current, scope == EditScope.LARGE, id) { it.copy(color = color) }
            }
        }

    fun updateLayers(
        face: ClockFace,
        ids: List<String>,
        transform: (FaceLayer) -> FaceLayer
    ): ClockFace =
        ids.fold(face) { current, id -> updateLayer(current, id, transform) }

    fun setNodeAlpha(face: ClockFace, large: Boolean, id: String, alpha: Float): ClockFace =
        updateNode(face, large, id) { it.copy(alpha = alpha) }

    fun move(face: ClockFace, id: String, scope: EditScope, dx: Float, dy: Float): ClockFace {
        val shift: (LayerNode) -> LayerNode = {
            it.copy(placement = it.placement.copy(x = it.placement.x + dx, y = it.placement.y + dy))
        }
        val large = scope != EditScope.SMALL
        val mapped: (FaceNode) -> FaceNode =
            { node -> if (node is LayerNode && node.id == id) shift(node) else node }
        val moved = updateNode(face, large, id, shift)
        return if (large) moved.copy(aodLarge = moved.aodLarge?.let {
            mapTree(
                it,
                mapped
            )
        }) else moved.copy(aodSmall = moved.aodSmall?.let { mapTree(it, mapped) })
    }

    fun groupSize(face: ClockFace, belongs: (String) -> Boolean, large: Boolean): Float =
        groupRoot(if (large) face.large else face.small, belongs)?.let(::largestLayer) ?: 0f

    fun scaleGroup(
        face: ClockFace,
        belongs: (String) -> Boolean,
        large: Boolean,
        factor: Float
    ): ClockFace {
        if (factor <= 0f || factor == 1f) return face
        val transform: (FaceNode) -> FaceNode = { scaled(it, factor, true) }
        return if (large) {
            face.copy(
                large = replaceGroup(face.large, belongs, transform),
                aodLarge = face.aodLarge?.let { replaceGroup(it, belongs, transform) })
        } else {
            face.copy(
                small = replaceGroup(face.small, belongs, transform),
                aodSmall = face.aodSmall?.let { replaceGroup(it, belongs, transform) })
        }
    }

    fun moveGroup(
        face: ClockFace,
        belongs: (String) -> Boolean,
        large: Boolean,
        dx: Float,
        dy: Float
    ): ClockFace {
        val transform: (FaceNode) -> FaceNode = {
            withPlacement(
                it,
                it.placement.copy(x = it.placement.x + dx, y = it.placement.y + dy)
            )
        }
        return if (large) {
            face.copy(
                large = replaceGroup(face.large, belongs, transform),
                aodLarge = face.aodLarge?.let { replaceGroup(it, belongs, transform) })
        } else {
            face.copy(
                small = replaceGroup(face.small, belongs, transform),
                aodSmall = face.aodSmall?.let { replaceGroup(it, belongs, transform) })
        }
    }

    fun moveMember(
        face: ClockFace,
        belongs: (String) -> Boolean,
        large: Boolean,
        dx: Float,
        dy: Float
    ): ClockFace {
        val transform: (FaceNode) -> FaceNode = {
            withPlacement(
                it,
                it.placement.copy(x = it.placement.x + dx, y = it.placement.y + dy)
            )
        }
        return if (large) {
            face.copy(
                large = replaceNested(face.large, belongs, transform),
                aodLarge = face.aodLarge?.let { replaceNested(it, belongs, transform) })
        } else {
            face.copy(
                small = replaceNested(face.small, belongs, transform),
                aodSmall = face.aodSmall?.let { replaceNested(it, belongs, transform) })
        }
    }

    fun groupPlacement(
        face: ClockFace,
        belongs: (String) -> Boolean,
        large: Boolean
    ): FacePlacement? =
        groupRoot(if (large) face.large else face.small, belongs)?.placement

    fun groupAlign(face: ClockFace, belongs: (String) -> Boolean, large: Boolean): FaceAlign? =
        groupRoot(if (large) face.large else face.small, belongs)?.placement?.anchor

    fun alignGroup(
        face: ClockFace,
        belongs: (String) -> Boolean,
        large: Boolean,
        align: FaceAlign
    ): ClockFace {
        val x = when (align) {
            FaceAlign.START -> GROUP_EDGE
            FaceAlign.CENTER -> 0.5f
            FaceAlign.END -> 1f - GROUP_EDGE
        }
        val transform: (FaceNode) -> FaceNode =
            { withPlacement(it, it.placement.copy(x = x, anchor = align)) }
        return if (large) {
            face.copy(
                large = replaceGroup(face.large, belongs, transform),
                aodLarge = face.aodLarge?.let { replaceGroup(it, belongs, transform) })
        } else {
            face.copy(
                small = replaceGroup(face.small, belongs, transform),
                aodSmall = face.aodSmall?.let { replaceGroup(it, belongs, transform) })
        }
    }

    fun groupIn(root: FaceNode?, belongs: (String) -> Boolean): Boolean =
        root != null && groupRoot(root, belongs)?.placement?.hidden == false

    fun groupChanged(face: ClockFace, default: ClockFace, belongs: (String) -> Boolean): Boolean {
        if (face.layers.filter { belongs(it.id) } != default.layers.filter { belongs(it.id) }) return true
        fun shape(root: FaceNode): FaceNode? =
            groupRoot(root, belongs)?.let { withPlacement(it, FacePlacement()) }
        return shape(face.large) != shape(default.large) || shape(face.small) != shape(default.small)
    }

    fun resetGroup(face: ClockFace, default: ClockFace, belongs: (String) -> Boolean): ClockFace {
        val defaults = default.layers.filter { belongs(it.id) }.associateBy { it.id }
        fun tree(current: FaceNode, original: FaceNode): FaceNode {
            val source = groupRoot(original, belongs) ?: return current
            return replaceGroup(current, belongs) { node -> withPlacement(source, node.placement) }
        }
        return face.copy(
            layers = face.layers.filter { !belongs(it.id) || it.id in defaults }
                .map { defaults[it.id] ?: it },
            large = tree(face.large, default.large),
            small = tree(face.small, default.small)
        )
    }

    private fun groupRoot(root: FaceNode, belongs: (String) -> Boolean): FaceNode? {
        (root as? StackNode)?.children?.firstOrNull { owns(it, belongs) }?.let { return it }
        return root.takeIf { owns(it, belongs) }
    }

    private fun replaceGroup(
        root: FaceNode,
        belongs: (String) -> Boolean,
        transform: (FaceNode) -> FaceNode
    ): FaceNode {
        if (root is StackNode && root.children.any { owns(it, belongs) }) {
            return root.copy(children = root.children.map { child ->
                if (owns(
                        child,
                        belongs
                    )
                ) transform(child) else child
            })
        }
        return if (owns(root, belongs)) transform(root) else root
    }

    private fun replaceNested(
        root: FaceNode,
        belongs: (String) -> Boolean,
        transform: (FaceNode) -> FaceNode
    ): FaceNode {
        if (owns(root, belongs)) return transform(root)
        if (root !is StackNode || layerIds(root).none(belongs)) return root
        return root.copy(children = root.children.map { replaceNested(it, belongs, transform) })
    }

    private fun owns(node: FaceNode, belongs: (String) -> Boolean): Boolean =
        layerIds(node).let { it.isNotEmpty() && it.all(belongs) }

    private fun layerIds(node: FaceNode): List<String> = when (node) {
        is LayerNode -> listOf(node.id)
        is RowNode -> node.children.flatMap(::layerIds)
        is ColumnNode -> node.children.flatMap(::layerIds)
        is StackNode -> node.children.flatMap(::layerIds)
    }

    private fun largestLayer(node: FaceNode): Float = when (node) {
        is LayerNode -> node.size
        is RowNode -> node.children.maxOfOrNull(::largestLayer) ?: 0f
        is ColumnNode -> node.children.maxOfOrNull(::largestLayer) ?: 0f
        is StackNode -> node.children.maxOfOrNull(::largestLayer) ?: 0f
    }

    fun scaleNode(node: FaceNode, factor: Float): FaceNode =
        if (factor == 1f || factor <= 0f) node else scaled(node, factor, true)

    private fun scaled(node: FaceNode, factor: Float, root: Boolean): FaceNode {
        val placement = if (root) node.placement else node.placement.copy(
            x = node.placement.x * factor,
            y = node.placement.y * factor
        )
        return when (node) {
            is LayerNode -> node.copy(size = node.size * factor, placement = placement)
            is RowNode -> node.copy(
                children = node.children.map { scaled(it, factor, false) },
                gap = node.gap * factor,
                placement = placement
            )

            is ColumnNode -> node.copy(
                children = node.children.map { scaled(it, factor, false) },
                gap = node.gap * factor,
                placement = placement
            )

            is StackNode -> node.copy(
                children = node.children.map { scaled(it, factor, false) },
                width = node.width?.let { it * factor },
                height = node.height?.let { it * factor },
                placement = placement
            )
        }
    }

    fun canAlign(face: ClockFace, large: Boolean, id: String): Boolean =
        when (val parent = parentOf(if (large) face.large else face.small, id)) {
            is ColumnNode, is RowNode -> true
            is StackNode -> parent.width != null
            else -> false
        }

    fun selfAlign(face: ClockFace, large: Boolean, id: String): FaceAlign? {
        val root = if (large) face.large else face.small
        find(root, id)?.align?.let { return it }
        return when (val parent = parentOf(root, id)) {
            is ColumnNode -> parent.align
            is RowNode -> parent.align
            is StackNode -> find(root, id)?.placement?.anchor
            else -> null
        }
    }

    fun contentAlign(face: ClockFace, large: Boolean, cardId: String): FaceAlign? {
        val cell =
            parentOf(if (large) face.large else face.small, cardId) as? StackNode ?: return null
        val content =
            cell.children.firstOrNull { !(it is LayerNode && it.id == cardId) } ?: return null
        return (content as? LayerNode)?.align ?: content.placement.anchor
    }

    fun setContentAlign(
        face: ClockFace,
        scope: EditScope,
        cardId: String,
        align: FaceAlign
    ): ClockFace {
        fun apply(root: FaceNode): FaceNode = mapTree(root) { node ->
            if (node !is StackNode || node.width == null || node.children.none { it is LayerNode && it.id == cardId }) return@mapTree node
            node.copy(children = node.children.map { child ->
                if (child is LayerNode && child.id == cardId) return@map child
                val nudge = abs(child.placement.x)
                val x = when (align) {
                    FaceAlign.START -> nudge
                    FaceAlign.CENTER -> 0f
                    FaceAlign.END -> -nudge
                }
                when (child) {
                    is LayerNode -> child.copy(
                        align = align,
                        placement = child.placement.copy(x = x, anchor = align)
                    )

                    is ColumnNode -> child.copy(
                        align = align,
                        placement = child.placement.copy(x = x, anchor = align)
                    )

                    is RowNode -> child.copy(
                        placement = child.placement.copy(
                            x = x,
                            anchor = align
                        )
                    )

                    is StackNode -> child.copy(
                        placement = child.placement.copy(
                            x = x,
                            anchor = align
                        )
                    )
                }
            })
        }
        return when (scope) {
            EditScope.AOD -> face.copy(large = apply(face.large), small = apply(face.small))
            EditScope.LARGE -> face.copy(large = apply(face.large))
            EditScope.SMALL -> face.copy(small = apply(face.small))
        }
    }

    fun setSelfAlign(face: ClockFace, scope: EditScope, id: String, align: FaceAlign): ClockFace =
        when (scope) {
            EditScope.AOD -> updateBoth(face, id) { it.copy(align = align) }
            else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(align = align) }
        }

    fun viewAlign(face: ClockFace, large: Boolean): FaceAlign =
        (if (large) face.large else face.small).placement.anchor

    fun setViewAlign(face: ClockFace, scope: EditScope, align: FaceAlign): ClockFace {
        val x = when (align) {
            FaceAlign.START -> 0f
            FaceAlign.CENTER -> 0.5f
            FaceAlign.END -> 1f
        }

        fun apply(root: FaceNode): FaceNode =
            withPlacement(root, root.placement.copy(x = x, anchor = align))
        return when (scope) {
            EditScope.AOD -> face.copy(large = apply(face.large), small = apply(face.small))
            EditScope.LARGE -> face.copy(large = apply(face.large))
            EditScope.SMALL -> face.copy(small = apply(face.small))
        }
    }

    private fun writeOne(
        face: ClockFace,
        id: String,
        scope: EditScope,
        key: StyleKey,
        value: Float
    ): ClockFace {
        val int = value.toInt()
        return when (key) {
            StyleKey.WEIGHT -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) {
                    it.copy(
                        sleepWeight = int,
                        sleepStyle = it.sleepStyle.withThin()
                    )
                }

                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(weight = int) }
            }

            StyleKey.WIDTH -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) { it.copy(sleepWidth = int) }
                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(width = int) }
            }

            StyleKey.HEIGHT -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) { it.copy(sleepHeight = int) }
                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(height = int) }
            }

            StyleKey.TRACKING -> when (scope) {
                EditScope.AOD -> updateBoth(
                    updateLayer(face, id) { it.copy(tracking = value) },
                    id
                ) { it.copy(tracking = null) }

                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(tracking = value) }
            }

            StyleKey.LINE_HEIGHT -> when (scope) {
                EditScope.AOD -> updateBoth(
                    updateLayer(face, id) { it.copy(lineHeight = value) },
                    id
                ) { it.copy(lineHeight = null) }

                else -> updateNode(
                    face,
                    scope == EditScope.LARGE,
                    id
                ) { it.copy(lineHeight = value) }
            }

            StyleKey.SIZE -> when (scope) {
                EditScope.AOD -> {
                    val current = node(face, true, id)?.size ?: return face
                    val factor = if (current > 0f) (value / current).coerceIn(
                        MIN_SLEEP_SCALE,
                        MAX_SLEEP_SCALE
                    ) else 1f
                    updateLayer(face, id) { it.copy(sleepScale = factor) }
                }

                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(size = value) }
            }

            StyleKey.ROTATION -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) { it.copy(sleepRotation = value) }
                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(rotation = value) }
            }

            StyleKey.TILT_X -> updateNode(
                face,
                scope != EditScope.SMALL,
                id
            ) { it.copy(tiltX = value) }

            StyleKey.TILT_Y -> updateNode(
                face,
                scope != EditScope.SMALL,
                id
            ) { it.copy(tiltY = value) }

            StyleKey.OPACITY -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) { it.copy(sleepOpacity = value) }
                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(alpha = value) }
            }

            StyleKey.EFFECT_TINT -> updateLayer(face, id) { it.copy(effectTint = value) }
            StyleKey.OUTLINE_WIDTH -> updateLayer(face, id) { it.copy(outlineWidth = value) }
            StyleKey.STRETCH -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) { it.copy(sleepStretch = value) }
                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(stretch = value) }
            }

            StyleKey.STRETCH_X -> when (scope) {
                EditScope.AOD -> updateLayer(face, id) { it.copy(sleepStretchX = value) }
                else -> updateNode(face, scope == EditScope.LARGE, id) { it.copy(stretchX = value) }
            }
        }
    }

    private fun FaceSleepStyle.withThin(): FaceSleepStyle = when (this) {
        FaceSleepStyle.NONE, FaceSleepStyle.HIDE -> FaceSleepStyle.THIN
        FaceSleepStyle.OUTLINE -> FaceSleepStyle.THIN_OUTLINE
        else -> this
    }

    private fun updateLayer(
        face: ClockFace,
        id: String,
        transform: (FaceLayer) -> FaceLayer
    ): ClockFace =
        face.copy(layers = face.layers.map { if (it.id == id) transform(it) else it })

    private fun updateNode(
        face: ClockFace,
        large: Boolean,
        id: String,
        transform: (LayerNode) -> LayerNode
    ): ClockFace {
        val mapped: (FaceNode) -> FaceNode =
            { node -> if (node is LayerNode && node.id == id) transform(node) else node }
        return if (large) face.copy(
            large = mapTree(
                face.large,
                mapped
            )
        ) else face.copy(small = mapTree(face.small, mapped))
    }

    private fun updateBoth(
        face: ClockFace,
        id: String,
        transform: (LayerNode) -> LayerNode
    ): ClockFace =
        updateNode(updateNode(face, true, id, transform), false, id, transform)

    private fun mapTree(node: FaceNode, transform: (FaceNode) -> FaceNode): FaceNode {
        val mapped = when (node) {
            is RowNode -> node.copy(children = node.children.map { mapTree(it, transform) })
            is ColumnNode -> node.copy(children = node.children.map { mapTree(it, transform) })
            is StackNode -> node.copy(children = node.children.map { mapTree(it, transform) })
            is LayerNode -> node
        }
        return transform(mapped)
    }

    private fun withPlacement(node: FaceNode, placement: FacePlacement): FaceNode = when (node) {
        is RowNode -> node.copy(placement = placement)
        is ColumnNode -> node.copy(placement = placement)
        is StackNode -> node.copy(placement = placement)
        is LayerNode -> node.copy(placement = placement)
    }

    private fun find(node: FaceNode, id: String): LayerNode? = when (node) {
        is LayerNode -> node.takeIf { it.id == id }
        is RowNode -> node.children.firstNotNullOfOrNull { find(it, id) }
        is ColumnNode -> node.children.firstNotNullOfOrNull { find(it, id) }
        is StackNode -> node.children.firstNotNullOfOrNull { find(it, id) }
    }

    private fun contains(node: FaceNode, id: String): Boolean = find(node, id) != null

    private fun parentOf(node: FaceNode, id: String): FaceNode? {
        val children = when (node) {
            is RowNode -> node.children
            is ColumnNode -> node.children
            is StackNode -> node.children
            is LayerNode -> return null
        }
        if (children.any { it is LayerNode && it.id == id }) return node
        return children.firstNotNullOfOrNull { parentOf(it, id) }
    }
}

private const val MIN_SLEEP_SCALE = 0.2f
private const val MAX_SLEEP_SCALE = 3f
private const val GROUP_EDGE = 0.0225f
