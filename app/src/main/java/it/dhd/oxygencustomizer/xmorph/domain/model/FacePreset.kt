package it.dhd.oxygencustomizer.xmorph.domain.model

data class FacePreset(
    val style: Int,
    val face: ClockFace,
    val isModified: Boolean = false,
    val layout: Composition? = null
)
