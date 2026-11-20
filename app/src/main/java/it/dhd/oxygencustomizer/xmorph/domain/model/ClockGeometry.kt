package it.dhd.oxygencustomizer.xmorph.domain.model

data class ClockGeometry(
    val large: ClockFrame,
    val small: ClockFrame,
    val aspect: Float
) {
    fun frame(largeProgress: Float): ClockFrame {
        val t = largeProgress.coerceIn(0f, 1f)
        return ClockFrame(
            left = small.left + (large.left - small.left) * t,
            top = small.top + (large.top - small.top) * t,
            width = small.width + (large.width - small.width) * t
        )
    }
}
