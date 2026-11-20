package it.dhd.oxygencustomizer.xmorph.domain.model

data class ComposedFaces(
    val clock: ClockFace,
    val widgets: Map<Int, ClockFace>,
    val composition: Composition
)
