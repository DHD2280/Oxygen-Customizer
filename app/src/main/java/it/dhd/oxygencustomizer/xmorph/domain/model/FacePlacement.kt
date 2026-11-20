package it.dhd.oxygencustomizer.xmorph.domain.model

data class FacePlacement(
    val x: Float = 0f,
    val y: Float = 0f,
    val anchor: FaceAlign = FaceAlign.START,
    val hidden: Boolean = false,
    val auto: Boolean = false
)
