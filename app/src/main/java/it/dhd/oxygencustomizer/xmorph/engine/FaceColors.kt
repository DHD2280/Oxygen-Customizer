package it.dhd.oxygencustomizer.xmorph.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.core.graphics.toColorInt
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceGradient

object FaceColors {

    const val TEXT_INVERSE = "text2"
    const val ACCENT_PRIMARY = "accent1"
    const val ACCENT_SECONDARY = "accent2"
    const val ACCENT_TERTIARY = "accent3"

    fun resolve(context: Context, color: String): Int =
        FaceGradient.parse(color)?.let { resolve(context, it.stops.first()) }
            ?: tonal(context, color) ?: when (color) {
                TEXT_INVERSE -> Color.BLACK
                ACCENT_PRIMARY -> context.getColor(android.R.color.system_accent1_100)
                ACCENT_SECONDARY -> context.getColor(android.R.color.system_accent2_100)
                ACCENT_TERTIARY -> context.getColor(android.R.color.system_accent3_100)

                else -> if (color.startsWith("#")) parseHex(color) else Color.WHITE
            }

    private fun parseHex(color: String): Int = try {
        color.toColorInt()
    } catch (_: IllegalArgumentException) {
        Color.WHITE
    }

    private val tonalIds = HashMap<String, Int>()

    @SuppressLint("DiscouragedApi")
    private fun tonal(context: Context, color: String): Int? {
        val match = TONAL.matchEntire(color) ?: return null
        val (palette, index, tone) = match.destructured
        val name = "system_$palette${index}_$tone"
        val id = synchronized(tonalIds) {
            tonalIds.getOrPut(name) { Resources.getSystem().getIdentifier(name, COLOR, ANDROID) }
        }
        if (id == 0) return null
        return try {
            context.getColor(id)
        } catch (_: Resources.NotFoundException) {
            null
        }
    }

    private const val COLOR = "color"
    private const val ANDROID = "android"
    private val TONAL = Regex("(accent|neutral)([123])_(0|10|50|[1-9]00|1000)")
}
