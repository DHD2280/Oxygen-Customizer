package it.dhd.oxygencustomizer.xmorph.engine

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import androidx.core.graphics.toColorInt
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceGradient

object FaceColors {

    const val TEXT_PRIMARY = "text1"
    const val TEXT_INVERSE = "text2"
    const val ACCENT_PRIMARY = "accent1"
    const val ACCENT_SECONDARY = "accent2"
    const val ACCENT_TERTIARY = "accent3"

    fun isDynamic(color: String): Boolean = color.startsWith(ACCENT) || color.startsWith(NEUTRAL)

    fun resolve(context: Context, color: String): Int =
        FaceGradient.parse(color)?.let { resolve(context, it.stops.first()) }
            ?: tonal(context, color) ?: when (color) {
                TEXT_INVERSE -> Color.BLACK
                ACCENT_PRIMARY -> palette[ACCENT_PRIMARY + SHADE_100]
                    ?: context.getColor(android.R.color.system_accent1_100)

                ACCENT_SECONDARY -> palette[ACCENT_SECONDARY + SHADE_100] ?: context.getColor(
                    android.R.color.system_accent2_100
                )

                ACCENT_TERTIARY -> palette[ACCENT_TERTIARY + SHADE_100]
                    ?: context.getColor(android.R.color.system_accent3_100)

                else -> if (color.startsWith("#")) parseHex(color) else Color.WHITE
            }

    private fun parseHex(color: String): Int = try {
        color.toColorInt()
    } catch (_: IllegalArgumentException) {
        Color.WHITE
    }

    @Volatile
    private var palette: Map<String, Int> = emptyMap()

    @Volatile
    var paletteVersion = 0
        private set

    fun setPalette(value: Map<String, Int>) {
        if (value == palette) return
        palette = value
        paletteVersion++
    }

    private val tonalIds = HashMap<String, Int>()

    @SuppressLint("DiscouragedApi")
    private fun tonal(context: Context, color: String): Int? {
        val match = TONAL.matchEntire(color) ?: return null
        palette[color]?.let { return it }
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

    private const val ACCENT = "accent"
    private const val SHADE_100 = "_100"
    private const val NEUTRAL = "neutral"
    private const val COLOR = "color"
    private const val ANDROID = "android"
    private val TONAL = Regex("(accent|neutral)([123])_(0|10|50|[1-9]00|1000)")
}
