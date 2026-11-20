package it.dhd.oxygencustomizer.xmorph.domain.model

data class FaceLayer(
    val id: String,
    val type: FaceLayerType,
    val format: String = "",
    val font: String = "",
    val weight: Int = 560,
    val sleepWeight: Int = 250,
    val width: Int = 25,
    val height: Int = 788,
    val sleepWidth: Int? = null,
    val sleepHeight: Int? = null,
    val tracking: Float = 0f,
    val color: String = "",
    val change: FaceChangeStyle = FaceChangeStyle.SLIDE_UP,
    val sleepStyle: FaceSleepStyle = FaceSleepStyle.THIN,
    val lineHeight: Float = 1f,
    val outline: Boolean = false,
    val outlineWidth: Float = 1.5f,
    val effect: FaceTextEffect = FaceTextEffect.SOLID,
    val opacity: Float = 1f,
    val effectTint: Float? = null,
    val caps: Boolean = false,
    val hourZero: Boolean = true,
    val hideIdle: Boolean = false,
    val maxEms: Float? = null,
    val stroke: String? = null,
    val strokeWidth: Float = 0.06f,
    val shadow: Float = 0f,
    val handLength: Float = 0.42f,
    val badge: FaceBadge = FaceBadge.NONE,
    val badgeColor: String? = null,
    val badgePadding: Float = 0.22f,
    val roman: Boolean = false,
    val tile: FaceTile? = null,
    val sleepColor: String? = null,
    val aspect: Float = 1f,
    val hostHeight: Float = 1f,
    val radius: Float = 0.5f,
    val sleepScale: Float? = null,
    val sleepRotation: Float? = null,
    val sleepOpacity: Float? = null,
    val sleepMono: Boolean = false,
    val sleepStretch: Float? = null,
    val sleepStretchX: Float? = null,
    val activeColor: String? = null,
    val activeTint: Float? = null
) {
    val isTime: Boolean
        get() = type in TIME_TYPES

    val isIcon: Boolean
        get() = type == FaceLayerType.WEATHER_ICON || type == FaceLayerType.BATTERY_ICON || type == FaceLayerType.TILE ||
                type == FaceLayerType.ALARM_ICON || type == FaceLayerType.CHARGE_ICON || type == FaceLayerType.SUN_ICON ||
                type == FaceLayerType.HOURLY_ICON || type == FaceLayerType.MEDIA_ICON || type == FaceLayerType.NOTIFICATION_ICON ||
                type == FaceLayerType.EVENT_ICON

    val isHand: Boolean
        get() = type == FaceLayerType.HAND_HOUR || type == FaceLayerType.HAND_MINUTE

    val isImage: Boolean
        get() = type == FaceLayerType.IMAGE || type == FaceLayerType.MEDIA_ART

    val isShape: Boolean
        get() = isHand || type == FaceLayerType.CARD || type == FaceLayerType.GAUGE || isImage || type == FaceLayerType.SPACER || isHost

    val isHost: Boolean
        get() = type == FaceLayerType.WEATHER_CARD || type == FaceLayerType.DEVICE_WIDGET || type == FaceLayerType.DEVICE_ITEM || type == FaceLayerType.WIDGET_BIG || type == FaceLayerType.WIDGET_SMALL
}

private val TIME_TYPES = setOf(
    FaceLayerType.HOURS,
    FaceLayerType.MINUTES,
    FaceLayerType.COLON,
    FaceLayerType.TIME,
    FaceLayerType.HOUR_TENS,
    FaceLayerType.HOUR_ONES,
    FaceLayerType.MINUTE_TENS,
    FaceLayerType.MINUTE_ONES
)
