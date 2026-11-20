package it.dhd.oxygencustomizer.xmorph.domain.model

data class CompositionDraft(
    val clockStyle: Int,
    val parts: ComposedFaces,
    val face: ClockFace,
    val default: ClockFace
)
