package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.UserFont

object FaceFontRefs {

    fun ids(face: ClockFace): Set<String> =
        face.layers.mapNotNullTo(LinkedHashSet()) { UserFont.idOf(it.font) }

    fun remap(face: ClockFace, ids: Map<String, String>): ClockFace {
        if (ids.all { (from, to) -> from == to }) return face
        return face.copy(layers = face.layers.map { layer ->
            val id = UserFont.idOf(layer.font) ?: return@map layer
            val target = ids[id] ?: return@map layer
            layer.copy(font = UserFont.SCHEME + target + layer.font.removePrefix(UserFont.SCHEME + id))
        })
    }
}
