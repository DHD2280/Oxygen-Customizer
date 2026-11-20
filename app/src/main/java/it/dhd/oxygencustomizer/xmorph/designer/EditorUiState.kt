package it.dhd.oxygencustomizer.xmorph.designer

import it.dhd.oxygencustomizer.xmorph.domain.editor.ItemGroups
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionDraft
import it.dhd.oxygencustomizer.xmorph.domain.model.EditScope

data class EditorUiState(
    val face: ClockFace? = null,
    val style: Int? = null,
    val default: ClockFace? = null,
    val selectedId: String? = null,
    val scope: EditScope = EditScope.LARGE,
    val aodLarge: Boolean = true,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val saving: Boolean = false,
    val draft: CompositionDraft? = null,
    val editingOn: Boolean = false,
    val grouping: Set<Int>? = null,
    val bounds: TargetBounds? = null
) {
    val targets: List<String>
        get() = listOfNotNull(primary)

    val primary: String?
        get() = selectedId?.takeUnless(ItemGroups::isGroup)

    val group: String?
        get() = selectedId?.takeIf(ItemGroups::isGroup)
}
