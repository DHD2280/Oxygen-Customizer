package it.dhd.oxygencustomizer.xmorph.domain.model

import kotlin.math.roundToInt

data class FaceGradient(val angle: Float, val stops: List<String>) {

    fun encode(): String =
        PREFIX + angle.roundToInt() + SEPARATOR + stops.joinToString(STOP_SEPARATOR)

    companion object {
        const val PREFIX = "gradient:"
        private const val SEPARATOR = ":"
        private const val STOP_SEPARATOR = ","

        fun parse(value: String?): FaceGradient? {
            if (value == null || !value.startsWith(PREFIX)) return null
            val parts = value.removePrefix(PREFIX).split(SEPARATOR, limit = 2)
            val angle = parts.firstOrNull()?.toFloatOrNull() ?: return null
            val stops = parts.getOrNull(1)?.split(STOP_SEPARATOR)?.map { it.trim() }
                ?.filter { it.isNotEmpty() }.orEmpty()
            return if (stops.size < 2) null else FaceGradient(angle, stops)
        }
    }
}
