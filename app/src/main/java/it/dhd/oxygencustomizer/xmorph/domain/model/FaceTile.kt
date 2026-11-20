package it.dhd.oxygencustomizer.xmorph.domain.model

enum class FaceTile(val media: MediaAction? = null) {
    WIFI,
    BLUETOOTH,
    FLASHLIGHT,
    HOTSPOT,
    MOBILE_DATA,
    RINGER,
    DND,
    MEDIA(MediaAction.TOGGLE),
    MEDIA_PREVIOUS(MediaAction.PREVIOUS),
    MEDIA_NEXT(MediaAction.NEXT)
}
