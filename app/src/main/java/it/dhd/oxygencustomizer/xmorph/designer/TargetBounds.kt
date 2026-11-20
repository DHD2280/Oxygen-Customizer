package it.dhd.oxygencustomizer.xmorph.designer

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemBounds

data class TargetBounds(
    val id: String,
    val face: ClockFace,
    val bounds: ItemBounds,
    val maxBottom: Float
)
