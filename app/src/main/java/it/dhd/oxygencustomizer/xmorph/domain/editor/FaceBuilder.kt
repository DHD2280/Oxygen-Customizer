package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.OrderPosition
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode

object FaceBuilder {

    fun groupLayers(face: ClockFace, key: String): List<FaceLayer> =
        face.layers.filter { ItemGroups.belongs(key, it.id) }

    fun remove(face: ClockFace, id: String): ClockFace {
        if (groupLayers(face, ItemGroups.keyOf(id)).size < 2) return face
        return face.copy(
            layers = face.layers.filterNot { it.id == id },
            large = prune(face.large, id) ?: StackNode(
                children = emptyList(),
                placement = face.large.placement
            ),
            small = prune(face.small, id) ?: StackNode(
                children = emptyList(),
                placement = face.small.placement
            ),
            aodLarge = face.aodLarge?.let { prune(it, id) },
            aodSmall = face.aodSmall?.let { prune(it, id) }
        )
    }

    fun position(face: ClockFace, id: String): OrderPosition? {
        val group = groupLayers(face, ItemGroups.keyOf(id))
        if (group.size < 2) return null
        return OrderPosition(group.indexOfFirst { it.id == id }, group.size)
    }

    fun move(face: ClockFace, id: String, steps: Int): ClockFace {
        val key = ItemGroups.keyOf(id)
        val group = groupLayers(face, key)
        val from = group.indexOfFirst { it.id == id }
        if (from < 0 || group.size < 2) return face
        val to = (from + steps).coerceIn(0, group.lastIndex)
        if (to == from) return face
        val reordered = group.toMutableList().apply { add(to, removeAt(from)) }
        val slots = face.layers.indices.filter { ItemGroups.belongs(key, face.layers[it].id) }
        val layers = face.layers.toMutableList()
        slots.forEachIndexed { index, slot -> layers[slot] = reordered[index] }
        return face.copy(layers = layers)
    }

    private fun prune(node: FaceNode, id: String): FaceNode? = when (node) {
        is LayerNode -> node.takeUnless { it.id == id }
        is RowNode -> node.children.mapNotNull { prune(it, id) }.takeIf { it.isNotEmpty() }
            ?.let { node.copy(children = it) }

        is ColumnNode -> node.children.mapNotNull { prune(it, id) }.takeIf { it.isNotEmpty() }
            ?.let { node.copy(children = it) }

        is StackNode -> node.children.mapNotNull { prune(it, id) }.takeIf { it.isNotEmpty() }
            ?.let { node.copy(children = it) }
    }
}
