package it.dhd.oxygencustomizer.xmorph.engine.render

import android.text.format.DateFormat
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceData
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import java.util.Calendar
import java.util.Locale

internal object TextTokens {

    private val TOKEN = Regex("""\{([^{}]+)\}""")

    private val DATA = mapOf(
        "temp" to FaceLayerType.WEATHER_TEMP,
        "condition" to FaceLayerType.WEATHER_CONDITION,
        "highlow" to FaceLayerType.WEATHER_HIGH_LOW,
        "humidity" to FaceLayerType.WEATHER_HUMIDITY,
        "wind" to FaceLayerType.WEATHER_WIND,
        "city" to FaceLayerType.WEATHER_LOCATION,
        "battery" to FaceLayerType.BATTERY_LEVEL,
        "steps" to FaceLayerType.STEPS,
        "data" to FaceLayerType.DATA_USAGE,
        "alarm" to FaceLayerType.ALARM_TIME,
        "charging" to FaceLayerType.CHARGE_STATUS,
        "sun" to FaceLayerType.SUN_TIME,
        "uv" to FaceLayerType.UV_INDEX,
        "song" to FaceLayerType.MEDIA_TITLE,
        "artist" to FaceLayerType.MEDIA_ARTIST,
        "notifications" to FaceLayerType.NOTIFICATION_COUNT,
        "event" to FaceLayerType.EVENT_TITLE,
        "event_time" to FaceLayerType.EVENT_TIME
    )

    private fun words(key: String, calendar: Calendar, phrases: WordPhrases): String? {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val locale = Locale.getDefault()
        return when (key) {
            "hour_word" -> WordTime.hour(hour, locale)
            "hour_word_24" -> WordTime.hour24(hour, locale)
            "minute_word" -> WordTime.minute(minute, locale)
            "time_words" -> WordTime.time(hour, minute, locale)
            "time_words_24" -> WordTime.time24(hour, minute, phrases, locale)
            "fuzzy_time" -> WordTime.fuzzy(hour, minute, phrases, locale)
            "fuzzy_lead" -> WordTime.fuzzyLead(hour, minute, phrases, locale)
            "fuzzy_hour" -> WordTime.fuzzyHour(hour, minute, phrases, locale)
            else -> null
        }
    }

    fun expand(
        format: String,
        calendar: Calendar,
        data: FaceData,
        phrases: WordPhrases,
        dataPhrases: DataPhrases
    ): String {
        if ('{' !in format) return format
        return TOKEN.replace(format) { match ->
            val key = match.groupValues[1].trim()
            words(key.lowercase(), calendar, phrases)
                ?: DATA[key.lowercase()]?.let { type ->
                    FaceValues.text(
                        FaceLayer(
                            id = "",
                            type = type
                        ), data, dataPhrases
                    ).orEmpty()
                }
                ?: runCatching {
                    DateFormat.format(key, calendar).toString()
                }.getOrDefault(match.value)
        }
    }
}
