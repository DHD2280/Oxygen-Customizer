package it.dhd.oxygencustomizer.xmorph.domain.model

data class PresetFileEntry(
    val style: Int,
    val kind: FaceKind,
    val face: ClockFace,
    val preview: ClockFace = face
)
