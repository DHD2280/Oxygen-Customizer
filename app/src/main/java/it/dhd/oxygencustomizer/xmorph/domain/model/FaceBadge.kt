package it.dhd.oxygencustomizer.xmorph.domain.model

enum class FaceBadge {
    NONE, PILL, CIRCLE, PILL_FILLED, CIRCLE_FILLED;

    val filled: Boolean
        get() = this == PILL_FILLED || this == CIRCLE_FILLED
}
