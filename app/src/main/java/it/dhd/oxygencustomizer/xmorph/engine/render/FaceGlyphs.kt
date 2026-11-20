package it.dhd.oxygencustomizer.xmorph.engine.render

import it.dhd.oxygencustomizer.xmorph.domain.model.FaceData
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTile

internal object FaceGlyphs {

    private const val CLEAR_DAY = ""
    private const val CLEAR_NIGHT = ""
    private const val PARTLY_DAY = ""
    private const val PARTLY_NIGHT = ""
    private const val CLOUD = ""
    private const val FOG = ""
    private const val RAIN = ""
    private const val RAIN_LIGHT = ""
    private const val RAIN_HEAVY = ""
    private const val SNOW = ""
    private const val SLEET = ""
    private const val STORM = ""
    private const val HAIL = ""
    private const val WIND = ""
    private const val TORNADO = ""
    private const val HURRICANE = ""
    private const val UNKNOWN = ""

    private val BATTERY = arrayOf("", "", "", "", "", "", "", "")
    private val CHARGING = arrayOf("", "", "", "", "", "", "", "")

    fun weather(code: Int, isDay: Boolean): String = when (code) {
        0 -> TORNADO
        1, 2 -> HURRICANE
        3, 4, 37, 38, 39, 45, 47 -> STORM
        5, 6, 7, 18 -> SLEET
        8, 9 -> RAIN_LIGHT
        10, 11, 12, 40 -> RAIN
        35 -> HAIL
        13, 14, 15, 16, 41, 42, 43, 46 -> SNOW
        17 -> HAIL
        19, 20, 21, 22 -> FOG
        23, 24 -> WIND
        25 -> CLOUD
        26 -> CLOUD
        27, 29 -> PARTLY_NIGHT
        28, 30, 44 -> if (isDay) PARTLY_DAY else PARTLY_NIGHT
        31, 33 -> CLEAR_NIGHT
        32, 34, 36 -> if (isDay) CLEAR_DAY else CLEAR_NIGHT
        else -> UNKNOWN
    }

    fun rain(code: Int): String = if (code in 8..12) RAIN_HEAVY else RAIN

    const val ALARM = "\uE855"
    const val BOLT = "\uEA0B"
    const val SUN = "\uE1C6"
    const val UV = "\uE81A"
    const val MUSIC = "\uE405"
    const val EQUALIZER = "\uE1B8"
    const val BELL = "\uE7F7"
    const val EVENT = "\uE878"
    const val PLAY = "\uE037"
    const val PAUSE = "\uE034"
    const val SKIP_PREVIOUS = "\uE045"
    const val SKIP_NEXT = "\uE044"

    fun battery(level: Int, charging: Boolean): String {
        val index = ((level.coerceIn(0, 100) * (BATTERY.size - 1)) + 50) / 100
        return if (charging) CHARGING[index] else BATTERY[index]
    }

    fun tile(tile: FaceTile, state: Int): String = when (tile) {
        FaceTile.WIFI -> if (state == FaceData.TILE_OFF) "" else ""
        FaceTile.BLUETOOTH -> if (state == FaceData.TILE_OFF) "" else ""
        FaceTile.FLASHLIGHT -> if (state == FaceData.TILE_OFF) "" else ""
        FaceTile.HOTSPOT -> if (state == FaceData.TILE_OFF) "" else ""
        FaceTile.MOBILE_DATA -> if (state == FaceData.TILE_OFF) "" else ""
        FaceTile.RINGER -> when (state) {
            FaceData.TILE_ALT -> ""
            FaceData.TILE_OFF -> ""
            else -> ""
        }

        FaceTile.DND -> if (state == FaceData.TILE_OFF) "" else ""
        FaceTile.MEDIA -> if (state == FaceData.TILE_ON) PAUSE else PLAY
        FaceTile.MEDIA_PREVIOUS -> SKIP_PREVIOUS
        FaceTile.MEDIA_NEXT -> SKIP_NEXT
    }
}
