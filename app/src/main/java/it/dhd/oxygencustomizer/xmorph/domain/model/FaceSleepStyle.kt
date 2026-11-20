package it.dhd.oxygencustomizer.xmorph.domain.model

enum class FaceSleepStyle(val thin: Boolean, val outline: Boolean, val hide: Boolean) {
    NONE(false, false, false),
    THIN(true, false, false),
    OUTLINE(false, true, false),
    THIN_OUTLINE(true, true, false),
    HIDE(false, false, true)
}
