package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType

object FaceImageRefs {

    fun ids(face: ClockFace): Set<String> = face.layers
        .filter { it.type == FaceLayerType.IMAGE && it.format.isNotEmpty() }
        .mapTo(LinkedHashSet()) { it.format }

    fun remap(face: ClockFace, ids: Map<String, String>): ClockFace {
        if (ids.all { (from, to) -> from == to }) return face
        return face.copy(layers = face.layers.map { layer ->
            val target = ids[layer.format]
            if (layer.type == FaceLayerType.IMAGE && target != null) layer.copy(format = target) else layer
        })
    }
}
