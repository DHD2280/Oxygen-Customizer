package it.dhd.oxygencustomizer.xmorph.domain.editor

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemState

data class GroupMember(
    val source: Int,
    val face: ClockFace,
    val large: FacePlacement,
    val small: FacePlacement,
    val aodLarge: FacePlacement? = null,
    val aodSmall: FacePlacement? = null,
    val hidden: Set<ItemState>
)
