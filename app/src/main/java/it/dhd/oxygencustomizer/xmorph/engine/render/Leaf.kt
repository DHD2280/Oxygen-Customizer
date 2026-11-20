package it.dhd.oxygencustomizer.xmorph.engine.render

internal data class Leaf(
    val metrics: LayerMetrics,
    val width: Float,
    val height: Float,
    val size: Float,
    val descent: Float = 0f
)
