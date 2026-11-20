package it.dhd.oxygencustomizer.xmorph.designer

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionDraft

internal data class EditorSnapshot(
    val face: ClockFace,
    val draft: CompositionDraft?
)
