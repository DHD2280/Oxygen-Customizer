package it.dhd.oxygencustomizer.xmorph.domain.model

data class UngroupedItem(
    val style: Int,
    val large: FacePlacement,
    val small: FacePlacement,
    val aodLarge: FacePlacement?,
    val aodSmall: FacePlacement?,
    val hidden: Set<ItemState>
)
