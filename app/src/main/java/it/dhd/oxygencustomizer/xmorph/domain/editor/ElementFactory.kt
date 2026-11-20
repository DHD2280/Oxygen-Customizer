package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ElementKind
import it.dhd.oxygencustomizer.xmorph.domain.model.ElementSpec
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceChangeStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceImage
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceSleepStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTile
import it.dhd.oxygencustomizer.xmorph.domain.model.GaugeSource

object ElementFactory {

    fun create(kind: ElementKind, text: String): List<ElementSpec> = when (kind) {
        ElementKind.TIME -> listOf(ElementSpec(digits(FaceLayerType.TIME), TIME_SIZE))
        ElementKind.HOURS -> listOf(ElementSpec(digits(FaceLayerType.HOURS), DIGITS_SIZE))
        ElementKind.MINUTES -> listOf(ElementSpec(digits(FaceLayerType.MINUTES), DIGITS_SIZE))
        ElementKind.HOUR_TENS -> listOf(ElementSpec(digits(FaceLayerType.HOUR_TENS), DIGITS_SIZE))
        ElementKind.HOUR_ONES -> listOf(ElementSpec(digits(FaceLayerType.HOUR_ONES), DIGITS_SIZE))
        ElementKind.MINUTE_TENS -> listOf(
            ElementSpec(
                digits(FaceLayerType.MINUTE_TENS),
                DIGITS_SIZE
            )
        )

        ElementKind.MINUTE_ONES -> listOf(
            ElementSpec(
                digits(FaceLayerType.MINUTE_ONES),
                DIGITS_SIZE
            )
        )

        ElementKind.DATE -> listOf(ElementSpec(label(FaceLayerType.DATE, DATE_FORMAT), LABEL_SIZE))
        ElementKind.HANDS -> listOf(
            ElementSpec(hand(FaceLayerType.HAND_HOUR, HOUR_LENGTH), HANDS_SIZE),
            ElementSpec(hand(FaceLayerType.HAND_MINUTE, MINUTE_LENGTH), HANDS_SIZE)
        )

        ElementKind.TEXT -> listOf(ElementSpec(label(FaceLayerType.TEXT, text), LABEL_SIZE))
        ElementKind.ICON -> listOf(
            ElementSpec(
                icon(FaceLayerType.TEXT).copy(
                    format = DEFAULT_ICON,
                    font = FILLED_SYMBOLS
                ), ICON_SIZE
            )
        )

        ElementKind.IMAGE -> emptyList()
        ElementKind.CARD -> listOf(
            ElementSpec(
                FaceLayer(
                    id = "",
                    type = FaceLayerType.CARD,
                    color = CARD_COLOR,
                    aspect = CARD_ASPECT,
                    radius = CARD_RADIUS,
                    change = FaceChangeStyle.NONE,
                    sleepStyle = FaceSleepStyle.HIDE
                ),
                CARD_SIZE,
                front = false
            )
        )

        ElementKind.RING -> listOf(ElementSpec(gauge(1f), RING_SIZE))
        ElementKind.BAR -> listOf(ElementSpec(gauge(BAR_ASPECT), BAR_SIZE))
        ElementKind.TEMPERATURE -> listOf(
            ElementSpec(
                label(FaceLayerType.WEATHER_TEMP),
                LABEL_SIZE
            )
        )

        ElementKind.WEATHER_ICON -> listOf(ElementSpec(icon(FaceLayerType.WEATHER_ICON), ICON_SIZE))
        ElementKind.CONDITION -> listOf(
            ElementSpec(
                label(FaceLayerType.WEATHER_CONDITION),
                LABEL_SIZE
            )
        )

        ElementKind.HIGH_LOW -> listOf(
            ElementSpec(
                label(FaceLayerType.WEATHER_HIGH_LOW),
                LABEL_SIZE
            )
        )

        ElementKind.HUMIDITY -> listOf(
            ElementSpec(
                label(FaceLayerType.WEATHER_HUMIDITY),
                LABEL_SIZE
            )
        )

        ElementKind.BATTERY -> listOf(ElementSpec(label(FaceLayerType.BATTERY_LEVEL), LABEL_SIZE))
        ElementKind.BATTERY_ICON -> listOf(ElementSpec(icon(FaceLayerType.BATTERY_ICON), ICON_SIZE))
        ElementKind.STEPS -> listOf(ElementSpec(label(FaceLayerType.STEPS), LABEL_SIZE))
        ElementKind.DATA_USAGE -> listOf(ElementSpec(label(FaceLayerType.DATA_USAGE), LABEL_SIZE))
        ElementKind.TILE -> listOf(
            ElementSpec(
                icon(FaceLayerType.TILE).copy(tile = FaceTile.WIFI),
                TILE_SIZE
            )
        )

        ElementKind.ALARM -> listOf(ElementSpec(label(FaceLayerType.ALARM_TIME), LABEL_SIZE))
        ElementKind.ALARM_ICON -> listOf(ElementSpec(icon(FaceLayerType.ALARM_ICON), ICON_SIZE))
        ElementKind.CHARGING -> listOf(ElementSpec(label(FaceLayerType.CHARGE_STATUS), LABEL_SIZE))
        ElementKind.CHARGE_ICON -> listOf(ElementSpec(icon(FaceLayerType.CHARGE_ICON), ICON_SIZE))
        ElementKind.SUN_TIME -> listOf(ElementSpec(label(FaceLayerType.SUN_TIME), LABEL_SIZE))
        ElementKind.SUN_ICON -> listOf(ElementSpec(icon(FaceLayerType.SUN_ICON), ICON_SIZE))
        ElementKind.UV -> listOf(ElementSpec(label(FaceLayerType.UV_INDEX), LABEL_SIZE))
        ElementKind.HOURLY_TEMP -> listOf(
            ElementSpec(
                label(FaceLayerType.HOURLY_TEMP, HOURLY_SLOT),
                LABEL_SIZE
            )
        )

        ElementKind.HOURLY_ICON -> listOf(
            ElementSpec(
                icon(FaceLayerType.HOURLY_ICON).copy(format = HOURLY_SLOT),
                ICON_SIZE
            )
        )

        ElementKind.HOURLY_TIME -> listOf(
            ElementSpec(
                label(FaceLayerType.HOURLY_TIME, HOURLY_SLOT),
                LABEL_SIZE
            )
        )

        ElementKind.MEDIA_TITLE -> listOf(ElementSpec(label(FaceLayerType.MEDIA_TITLE), LABEL_SIZE))
        ElementKind.MEDIA_ARTIST -> listOf(
            ElementSpec(
                label(FaceLayerType.MEDIA_ARTIST),
                LABEL_SIZE
            )
        )

        ElementKind.MEDIA_ICON -> listOf(ElementSpec(icon(FaceLayerType.MEDIA_ICON), ICON_SIZE))
        ElementKind.MEDIA_ART -> listOf(
            ElementSpec(
                FaceLayer(
                    id = "",
                    type = FaceLayerType.MEDIA_ART,
                    color = WHITE,
                    aspect = 1f,
                    radius = ART_RADIUS,
                    change = FaceChangeStyle.NONE,
                    sleepStyle = FaceSleepStyle.NONE,
                    sleepOpacity = 0f
                ),
                ART_SIZE
            )
        )

        ElementKind.MEDIA_CONTROL -> listOf(
            ElementSpec(
                icon(FaceLayerType.TILE).copy(tile = FaceTile.MEDIA),
                TILE_SIZE
            )
        )

        ElementKind.NOTIFICATIONS -> listOf(
            ElementSpec(
                label(FaceLayerType.NOTIFICATION_COUNT),
                LABEL_SIZE
            )
        )

        ElementKind.NOTIFICATION_ICON -> listOf(
            ElementSpec(
                icon(FaceLayerType.NOTIFICATION_ICON),
                ICON_SIZE
            )
        )

        ElementKind.EVENT_TITLE -> listOf(ElementSpec(label(FaceLayerType.EVENT_TITLE), LABEL_SIZE))
        ElementKind.EVENT_TIME -> listOf(ElementSpec(label(FaceLayerType.EVENT_TIME), LABEL_SIZE))
        ElementKind.EVENT_ICON -> listOf(ElementSpec(icon(FaceLayerType.EVENT_ICON), ICON_SIZE))
        ElementKind.SPACER -> listOf(
            ElementSpec(
                FaceLayer(
                    id = "",
                    type = FaceLayerType.SPACER,
                    aspect = SPACER_ASPECT,
                    change = FaceChangeStyle.NONE
                ), SPACER_SIZE
            )
        )

        ElementKind.WEATHER_CARD -> listOf(
            ElementSpec(
                host(FaceLayerType.WEATHER_CARD, ""),
                HOST_SIZE
            )
        )

        ElementKind.DEVICE_WIDGET -> listOf(
            ElementSpec(
                host(FaceLayerType.DEVICE_WIDGET, ""),
                HOST_SIZE
            )
        )

        ElementKind.DEVICE_ITEM -> listOf(
            ElementSpec(
                host(FaceLayerType.DEVICE_ITEM, text),
                HOST_SIZE
            )
        )

        ElementKind.WIDGET_BIG -> listOf(
            ElementSpec(
                host(FaceLayerType.WIDGET_BIG, text),
                HOST_SIZE
            )
        )

        ElementKind.WIDGET_SMALL -> listOf(
            ElementSpec(
                host(FaceLayerType.WIDGET_SMALL, text),
                HOST_SIZE
            )
        )
    }

    fun image(image: FaceImage): List<ElementSpec> = listOf(
        ElementSpec(
            FaceLayer(
                id = "",
                type = FaceLayerType.IMAGE,
                format = image.id,
                aspect = image.aspect,
                radius = IMAGE_RADIUS,
                change = FaceChangeStyle.NONE,
                sleepStyle = FaceSleepStyle.NONE,
                sleepOpacity = 0f
            ),
            IMAGE_SIZE
        )
    )

    private fun digits(type: FaceLayerType): FaceLayer =
        FaceLayer(id = "", type = type, color = WHITE)

    private fun label(type: FaceLayerType, format: String = ""): FaceLayer = FaceLayer(
        id = "",
        type = type,
        format = format,
        font = LABEL_FONT,
        weight = LABEL_WEIGHT,
        width = LABEL_WIDTH,
        height = LABEL_HEIGHT,
        color = WHITE,
        change = FaceChangeStyle.FADE,
        sleepStyle = FaceSleepStyle.THIN
    )

    private fun icon(type: FaceLayerType): FaceLayer = FaceLayer(
        id = "",
        type = type,
        weight = LABEL_WEIGHT,
        color = WHITE,
        change = FaceChangeStyle.FADE,
        sleepStyle = FaceSleepStyle.THIN
    )

    private fun hand(type: FaceLayerType, length: Float): FaceLayer = FaceLayer(
        id = "",
        type = type,
        weight = HAND_WEIGHT,
        color = WHITE,
        handLength = length,
        change = FaceChangeStyle.NONE,
        sleepStyle = FaceSleepStyle.THIN
    )

    private fun host(type: FaceLayerType, format: String): FaceLayer = FaceLayer(
        id = "",
        type = type,
        format = format,
        change = FaceChangeStyle.NONE,
        sleepStyle = FaceSleepStyle.HIDE
    )

    private fun gauge(aspect: Float): FaceLayer = FaceLayer(
        id = "",
        type = FaceLayerType.GAUGE,
        format = GaugeSource.BATTERY.key,
        aspect = aspect,
        color = WHITE,
        change = FaceChangeStyle.NONE,
        sleepStyle = FaceSleepStyle.THIN
    )

    private const val WHITE = "#FFFFFF"
    private const val DEFAULT_ICON = ""
    private const val FILLED_SYMBOLS = "res:symbols?FILL=1"
    private const val CARD_COLOR = "#E61C1C1E"
    private const val LABEL_FONT = "res:google_sans_flex?opsz=144"
    private const val LABEL_WEIGHT = 500
    private const val LABEL_WIDTH = 100
    private const val LABEL_HEIGHT = 700
    private const val HAND_WEIGHT = 700
    private const val DATE_FORMAT = "EEE d MMM"
    private const val TIME_SIZE = 0.16f
    private const val DIGITS_SIZE = 0.16f
    private const val LABEL_SIZE = 0.05f
    private const val ICON_SIZE = 0.06f
    private const val TILE_SIZE = 0.07f
    private const val ART_SIZE = 0.16f
    private const val ART_RADIUS = 0.18f
    private const val HANDS_SIZE = 0.3f
    private const val HOUR_LENGTH = 0.28f
    private const val MINUTE_LENGTH = 0.42f
    private const val CARD_SIZE = 0.4f
    private const val CARD_ASPECT = 2f
    private const val CARD_RADIUS = 0.25f
    private const val RING_SIZE = 0.12f
    private const val BAR_SIZE = 0.3f
    private const val BAR_ASPECT = 6f
    private const val IMAGE_SIZE = 0.25f
    private const val SPACER_SIZE = 0.9f
    private const val SPACER_ASPECT = 15f
    private const val IMAGE_RADIUS = 0.12f
    private const val HOST_SIZE = 1f

    private const val HOURLY_SLOT = "1"
}
