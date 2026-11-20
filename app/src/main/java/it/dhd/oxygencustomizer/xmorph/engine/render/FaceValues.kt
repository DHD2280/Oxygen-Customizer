package it.dhd.oxygencustomizer.xmorph.engine.render

import it.dhd.oxygencustomizer.xmorph.domain.model.CalendarEvent
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceData
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.HourlyPoint
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

internal object FaceValues {

    private const val EMPTY = "--"
    private const val KILO = 1024.0
    private val UNITS = arrayOf("B", "KB", "MB", "GB", "TB")

    fun progress(layer: FaceLayer, data: FaceData): Float? = when (layer.format) {
        GAUGE_BATTERY -> data.batteryLevel?.let { it / 100f }
        GAUGE_STEPS -> data.steps?.let { it / STEP_GOAL }
        GAUGE_WEATHER -> data.weather?.let { weather ->
            val span = (weather.high - weather.low).toFloat()
            if (span <= 0f) 0.5f else ((weather.temperature - weather.low) / span).coerceIn(0f, 1f)
        }

        GAUGE_HUMIDITY -> data.weather?.let { it.humidity / 100f }
        GAUGE_UV -> data.weather?.uvIndex?.let { (it / UV_MAX).coerceIn(0f, 1f) }
        else -> null
    }

    fun text(layer: FaceLayer, data: FaceData, phrases: DataPhrases): String? {
        when (layer.type) {
            FaceLayerType.ALARM_TIME -> return data.nextAlarm?.let { alarm(it, data.use24Hour) }
                ?: phrases.noAlarm

            FaceLayerType.ALARM_ICON -> return FaceGlyphs.ALARM
            FaceLayerType.CHARGE_STATUS -> return charge(data, phrases)
            FaceLayerType.CHARGE_ICON -> return FaceGlyphs.BOLT
            FaceLayerType.SUN_TIME -> return nextSun(data)?.let { clock(it.first, data.use24Hour) }
                ?: EMPTY

            FaceLayerType.SUN_LABEL -> return nextSun(data)?.let { if (it.second) phrases.sunrise else phrases.sunset }
                ?: phrases.sunrise

            FaceLayerType.SUN_ICON -> return FaceGlyphs.SUN
            FaceLayerType.UV_INDEX -> return data.weather?.uvIndex?.let { phrases.uv.format(it.roundToInt()) }
                ?: EMPTY

            FaceLayerType.HOURLY_TIME -> return hour(layer, data)?.let {
                hourLabel(
                    it.time,
                    data.use24Hour
                )
            } ?: EMPTY

            FaceLayerType.HOURLY_ICON -> return hour(
                layer,
                data
            )?.let { FaceGlyphs.weather(it.conditionCode, it.isDay) } ?: FaceGlyphs.weather(
                -1,
                true
            )

            FaceLayerType.HOURLY_TEMP -> return hour(layer, data)?.let { "${it.temperature}°" }
                ?: EMPTY

            FaceLayerType.MEDIA_TITLE -> return data.mediaTitle ?: phrases.noMedia
            FaceLayerType.MEDIA_ARTIST -> return data.mediaArtist.orEmpty()
            FaceLayerType.MEDIA_ICON -> return if (data.mediaPlaying) FaceGlyphs.EQUALIZER else FaceGlyphs.MUSIC
            FaceLayerType.NOTIFICATION_COUNT -> return (data.notificationCount
                ?: 0).let { count -> template(layer, count.toString()) ?: count.toString() }

            FaceLayerType.NOTIFICATION_ICON -> return FaceGlyphs.BELL
            FaceLayerType.EVENT_TITLE -> return nextEvent(data)?.title?.ifBlank { null }
                ?: phrases.noEvents

            FaceLayerType.EVENT_TIME -> return nextEvent(data)?.let {
                eventTime(
                    it,
                    data.use24Hour,
                    phrases
                )
            }.orEmpty()

            FaceLayerType.EVENT_ICON -> return FaceGlyphs.EVENT
            else -> Unit
        }
        val weather = data.weather
        val value = when (layer.type) {
            FaceLayerType.WEATHER_TEMP -> weather?.let { "${it.temperature}°" }
            FaceLayerType.WEATHER_ICON -> weather?.let {
                FaceGlyphs.weather(
                    it.conditionCode,
                    it.isDay
                )
            } ?: FaceGlyphs.weather(-1, true)

            FaceLayerType.WEATHER_CONDITION -> weather?.condition
            FaceLayerType.WEATHER_HIGH_LOW -> weather?.let {
                template(
                    layer,
                    "${it.high}°",
                    "${it.low}°"
                ) ?: "${it.high}° / ${it.low}°"
            }

            FaceLayerType.WEATHER_HUMIDITY -> weather?.let { "${it.humidity}%" }
            FaceLayerType.WEATHER_WIND -> weather?.let { "${it.wind} ${it.windUnit}" }
            FaceLayerType.WEATHER_LOCATION -> weather?.location
            FaceLayerType.BATTERY_LEVEL -> data.batteryLevel?.let { "$it%" }
            FaceLayerType.BATTERY_ICON -> FaceGlyphs.battery(
                data.batteryLevel ?: 100,
                data.charging
            )

            FaceLayerType.STEPS -> data.steps?.let {
                NumberFormat.getIntegerInstance(Locale.getDefault()).format(it)
            }

            FaceLayerType.DATA_USAGE -> data.dataUsageBytes?.let(::bytes)
            FaceLayerType.TILE -> layer.tile?.let { FaceGlyphs.tile(it, data.tileState(it)) }
            else -> return null
        } ?: EMPTY
        if (layer.isIcon || layer.type == FaceLayerType.WEATHER_HIGH_LOW) return value
        return template(layer, value) ?: value
    }

    fun isIdle(layer: FaceLayer, data: FaceData): Boolean = when (layer.type) {
        FaceLayerType.ALARM_TIME, FaceLayerType.ALARM_ICON -> data.nextAlarm == null
        FaceLayerType.CHARGE_STATUS, FaceLayerType.CHARGE_ICON -> !isCharging(data)
        FaceLayerType.SUN_TIME, FaceLayerType.SUN_LABEL, FaceLayerType.SUN_ICON -> nextSun(data) == null
        FaceLayerType.UV_INDEX -> data.weather?.uvIndex == null
        FaceLayerType.HOURLY_TIME, FaceLayerType.HOURLY_ICON, FaceLayerType.HOURLY_TEMP -> hour(
            layer,
            data
        ) == null

        FaceLayerType.MEDIA_TITLE, FaceLayerType.MEDIA_ARTIST, FaceLayerType.MEDIA_ICON, FaceLayerType.MEDIA_ART -> data.mediaTitle == null
        FaceLayerType.NOTIFICATION_COUNT, FaceLayerType.NOTIFICATION_ICON -> (data.notificationCount
            ?: 0) == 0

        FaceLayerType.EVENT_TITLE, FaceLayerType.EVENT_TIME, FaceLayerType.EVENT_ICON -> nextEvent(
            data
        ) == null

        FaceLayerType.TILE -> layer.tile?.takeIf { it.media != null }
            ?.let { data.tileState(it) == FaceData.TILE_OFF } == true

        else -> false
    }

    private fun isCharging(data: FaceData): Boolean =
        data.charging || data.chargeRemainingMs != null

    private fun charge(data: FaceData, phrases: DataPhrases): String {
        if (!isCharging(data)) return phrases.notCharging
        if (data.batteryFull) return phrases.charged
        val remaining = data.chargeRemainingMs?.takeIf { it > 0 } ?: return phrases.charging
        val minutes = ((remaining + MINUTE_MS - 1) / MINUTE_MS).toInt()
        val duration = if (minutes >= MINUTES_PER_HOUR) {
            phrases.hoursMinutes.format(minutes / MINUTES_PER_HOUR, minutes % MINUTES_PER_HOUR)
        } else {
            phrases.minutes.format(minutes)
        }
        return phrases.chargingLeft.format(duration)
    }

    fun hourSlot(layer: FaceLayer): Int =
        layer.format.trim().toIntOrNull()?.coerceIn(1, MAX_SLOT) ?: 1

    private fun nextEvent(data: FaceData): CalendarEvent? {
        val now = System.currentTimeMillis()
        return data.events.firstOrNull { it.end > now }
    }

    private fun eventTime(event: CalendarEvent, use24Hour: Boolean, phrases: DataPhrases): String {
        val now = System.currentTimeMillis()
        val today = Calendar.getInstance()
        val start = Calendar.getInstance().apply { timeInMillis = event.begin }
        val dayOffset = start.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR) +
                (start.get(Calendar.YEAR) - today.get(Calendar.YEAR)) * DAYS_PER_YEAR
        return when {
            event.begin <= now -> if (event.allDay) phrases.allDay else phrases.happeningNow
            event.allDay && dayOffset == 0 -> phrases.allDay
            event.allDay && dayOffset == 1 -> phrases.tomorrow.format(phrases.allDay)
            event.allDay -> SimpleDateFormat(
                ALARM_DAY,
                Locale.getDefault()
            ).format(Date(event.begin))

            event.begin - now < HOUR_MS -> phrases.inMinutes.format(((event.begin - now + MINUTE_MS - 1) / MINUTE_MS).toInt())
            dayOffset == 0 -> clock(event.begin, use24Hour)
            dayOffset == 1 -> phrases.tomorrow.format(clock(event.begin, use24Hour))
            else -> SimpleDateFormat(
                ALARM_DAY,
                Locale.getDefault()
            ).format(Date(event.begin)) + " " + clock(event.begin, use24Hour)
        }
    }

    private fun nextSun(data: FaceData): Pair<Long, Boolean>? {
        val weather = data.weather ?: return null
        val now = System.currentTimeMillis()
        return (weather.sunrises.map { it to true } + weather.sunsets.map { it to false })
            .filter { it.first > now }
            .minByOrNull { it.first }
    }

    private fun hour(layer: FaceLayer, data: FaceData): HourlyPoint? {
        val points = data.weather?.hourly?.takeIf { it.isNotEmpty() } ?: return null
        val target = System.currentTimeMillis() / HOUR_MS * HOUR_MS + hourSlot(layer) * HOUR_MS
        return points.firstOrNull { it.time >= target }
    }

    private fun clock(time: Long, use24Hour: Boolean): String =
        SimpleDateFormat(if (use24Hour) ALARM_24 else ALARM_12, Locale.getDefault()).format(
            Date(
                time
            )
        )

    private fun hourLabel(time: Long, use24Hour: Boolean): String =
        SimpleDateFormat(
            if (use24Hour) HOUR_24 else HOUR_12,
            Locale.getDefault()
        ).format(Date(time))

    private fun alarm(time: Long, use24Hour: Boolean): String {
        val locale = Locale.getDefault()
        val clock =
            SimpleDateFormat(if (use24Hour) ALARM_24 else ALARM_12, locale).format(Date(time))
        if (time - System.currentTimeMillis() < DAY_MS) return clock
        return SimpleDateFormat(ALARM_DAY, locale).format(Date(time)) + " " + clock
    }

    private fun template(layer: FaceLayer, vararg values: String): String? =
        layer.format.takeIf { it.contains('%') }
            ?.let { runCatching { it.format(*values) }.getOrNull() }

    private fun bytes(value: Long): String {
        var size = value.toDouble()
        var unit = 0
        while (size >= KILO && unit < UNITS.lastIndex) {
            size /= KILO
            unit++
        }
        return if (unit == 0 || size >= 100) "${size.toInt()} ${UNITS[unit]}" else "%.1f %s".format(
            size,
            UNITS[unit]
        )
    }
}

private const val GAUGE_BATTERY = "battery"
private const val GAUGE_STEPS = "steps"
private const val GAUGE_WEATHER = "weather"
private const val GAUGE_HUMIDITY = "humidity"
private const val STEP_GOAL = 10_000f
private const val MINUTE_MS = 60_000L
private const val MINUTES_PER_HOUR = 60
private const val DAY_MS = 86_400_000L
private const val ALARM_24 = "HH:mm"
private const val ALARM_12 = "h:mm a"
private const val ALARM_DAY = "EEE"
private const val HOUR_24 = "HH:mm"
private const val HOUR_12 = "h a"
private const val HOUR_MS = 3_600_000L
private const val MAX_SLOT = 12
private const val GAUGE_UV = "uv"
private const val UV_MAX = 11f
private const val DAYS_PER_YEAR = 365
