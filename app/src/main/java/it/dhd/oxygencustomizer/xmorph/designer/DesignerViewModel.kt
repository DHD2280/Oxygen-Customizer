package it.dhd.oxygencustomizer.xmorph.designer

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import it.dhd.oxygencustomizer.xmorph.domain.editor.CustomFaces
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceBuilder
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceComposer
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceEditor
import it.dhd.oxygencustomizer.xmorph.domain.editor.ItemGrouping
import it.dhd.oxygencustomizer.xmorph.domain.editor.ItemGroups
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ComposedFaces
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionDraft
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionItem
import it.dhd.oxygencustomizer.xmorph.domain.model.EditScope
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceSleepStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTextEffect
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemState
import it.dhd.oxygencustomizer.xmorph.domain.model.OrderPosition
import it.dhd.oxygencustomizer.xmorph.domain.model.StyleKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

class DesignerViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DesignerRepository(application)


    fun setFont(family: String) = updateLayers("font") { it.copy(font = family) }

    private val state = MutableStateFlow(EditorUiState())
    private val undoStack = ArrayDeque<EditorSnapshot>()
    private val redoStack = ArrayDeque<EditorSnapshot>()
    private var lastEditKey: Any? = null
    private var lastEditAt = 0L

    val uiState: StateFlow<EditorUiState> = state.asStateFlow()

    fun open(focus: String?) {
        undoStack.clear()
        redoStack.clear()
        lastEditKey = null
        viewModelScope.launch {
            val draft = repository.draft() ?: return@launch
            val selected = focus?.takeIf { id ->
                draft.face.layers.any {
                    it.id == id || ItemGroups.belongs(
                        id,
                        it.id
                    )
                }
            }
                ?.let { id -> if (ItemGroups.isGroup(id)) elementOf(draft, id) ?: id else id }
            state.value = EditorUiState(
                face = draft.face,
                style = draft.clockStyle,
                default = draft.default,
                selectedId = selected,
                draft = draft
            )
        }
    }

    fun select(id: String?) {
        val target = id?.takeIf(ItemGroups::isGroup)
            ?.let { key -> state.value.draft?.let { elementOf(it, key) } } ?: id
        state.update { it.copy(selectedId = target, editingOn = false) }
    }

    fun standaloneKey(layerId: String): String? {
        val draft = state.value.draft ?: return null
        val key = ItemGroups.keyOf(layerId)
        return key.takeIf { elementOf(draft, it) == layerId }
    }

    private fun elementOf(draft: CompositionDraft, key: String): String? {
        val id = ItemGroups.itemId(key) ?: return null
        val item = draft.parts.composition.item(id) ?: return null
        val face = draft.parts.widgets[item.style] ?: return null
        if (face.category != CustomFaces.ELEMENT_CATEGORY || face.layers.size != 1) return null
        return FaceComposer.prefix(id) + face.layers.first().id
    }

    fun setEditingOn(on: Boolean) = state.update { it.copy(editingOn = on) }

    private fun onTarget(current: EditorUiState): Boolean =
        current.editingOn && current.scope != EditScope.AOD && current.face?.let { face ->
            current.primary?.let {
                FaceEditor.layer(
                    face,
                    it
                )
            }
        }?.tile != null

    fun setScope(scope: EditScope) = state.update {
        if (scope == EditScope.AOD) it.copy(scope = scope) else it.copy(
            scope = scope,
            aodLarge = scope == EditScope.LARGE
        )
    }

    private fun isGeometry(key: StyleKey): Boolean =
        key == StyleKey.SIZE || key == StyleKey.ROTATION

    private fun aodView(face: ClockFace): ClockFace =
        face.copy(large = face.aodLarge ?: face.large, small = face.aodSmall ?: face.small)

    private fun geometry(
        face: ClockFace,
        current: EditorUiState,
        block: (ClockFace, EditorUiState) -> ClockFace
    ): ClockFace {
        if (current.scope != EditScope.AOD) {
            val frozen = if (face.aodLarge == null && face.aodSmall == null) face.copy(
                aodLarge = face.large,
                aodSmall = face.small
            ) else face
            return block(frozen, current)
        }
        val view = aodView(face)
        val edited = block(
            view,
            current.copy(
                scope = EditScope.LARGE,
                aodLarge = true,
                default = current.default?.let(::aodView)
            )
        )
        if (edited == view) return face
        return face.copy(layers = edited.layers, aodLarge = edited.large, aodSmall = edited.small)
    }

    private fun <T> reading(
        current: EditorUiState,
        face: ClockFace,
        block: (ClockFace, EditorUiState) -> T
    ): T =
        if (current.scope == EditScope.AOD) block(
            aodView(face),
            current.copy(
                scope = EditScope.LARGE,
                aodLarge = true,
                default = current.default?.let(::aodView)
            )
        )
        else block(face, current)

    fun value(key: StyleKey): Float {
        val current = state.value
        val face = current.face ?: return 0f
        val id = current.primary ?: return 0f
        if (isGeometry(key)) return reading(current, face) { f, c ->
            FaceEditor.read(
                f,
                id,
                c.scope,
                key
            )
        }
        if (key == StyleKey.EFFECT_TINT && onTarget(current)) {
            FaceEditor.layer(face, id)?.let { layer ->
                return layer.activeTint ?: FaceEditor.read(
                    face,
                    id,
                    current.scope,
                    key
                )
            }
        }
        return FaceEditor.read(face, id, current.scope, key)
    }

    fun axes(id: String): Pair<Float, Float> {
        val current = state.value
        val face = current.face ?: return 0f to 0f
        return FaceEditor.read(face, id, current.scope, StyleKey.WIDTH) to FaceEditor.read(
            face,
            id,
            current.scope,
            StyleKey.HEIGHT
        )
    }

    fun color(): String {
        val current = state.value
        val face = current.face ?: return ""
        val id = current.primary ?: return ""
        if (onTarget(current)) FaceEditor.layer(face, id)?.activeColor?.let { return it }
        return FaceEditor.readColor(face, id, current.scope)
    }

    fun setValue(key: StyleKey, value: Float) = edit(key) { face, current ->
        if (key == StyleKey.EFFECT_TINT && onTarget(current)) return@edit FaceEditor.updateLayers(
            face,
            current.targets
        ) { it.copy(activeTint = value) }
        if (isGeometry(key)) return@edit geometry(face, current) { f, c ->
            FaceEditor.write(
                f,
                c.targets,
                c.scope,
                key,
                value
            )
        }
        FaceEditor.write(face, current.targets, current.scope, key, value)
    }

    fun effect(): FaceTextEffect? {
        val current = state.value
        val face = current.face ?: return null
        val id = current.primary ?: return null
        return FaceEditor.readEffect(face, id, current.scope)
    }

    fun setDimensions(width: Float, height: Float) = edit("dimensions") { face, current ->
        val id = current.primary ?: return@edit face
        if (current.scope == EditScope.AOD || !width.isFinite() || !height.isFinite() || width <= 0f || height <= 0f) return@edit face
        val aspect = (width / height).takeIf { it.isFinite() }?.coerceIn(MIN_ASPECT, MAX_ASPECT)
            ?: return@edit face
        val sized = FaceEditor.write(face, current.targets, current.scope, StyleKey.SIZE, width)
        FaceEditor.updateLayers(sized, listOf(id)) { it.copy(aspect = aspect) }
    }

    fun setEffect(effect: FaceTextEffect) = edit("effect") { face, current ->
        FaceEditor.writeEffect(face, current.targets, current.scope, effect)
    }

    fun borderColor(): String {
        val current = state.value
        val face = current.face ?: return ""
        val id = current.primary ?: return ""
        return FaceEditor.layer(face, id)?.stroke.orEmpty()
    }

    fun setBorder(color: String) = updateLayers("stroke") { it.copy(stroke = color) }

    fun setColor(color: String) = edit("color") { face, current ->
        if (onTarget(current)) return@edit FaceEditor.updateLayers(face, current.targets) {
            it.copy(
                activeColor = color
            )
        }
        FaceEditor.writeColor(face, current.targets, current.scope, color)
    }

    fun groupScale(key: String): Float {
        val current = state.value
        val face = current.face ?: return 1f
        return reading(current, face) { f, c -> groupScaleOf(f, c, key) }
    }

    private fun groupScaleOf(face: ClockFace, current: EditorUiState, key: String): Float {
        val large = current.scope != EditScope.SMALL
        val belongs: (String) -> Boolean = { ItemGroups.belongs(key, it) }
        val base =
            current.default?.let { FaceEditor.groupSize(it, belongs, large) }?.takeIf { it > 0f }
                ?: return 1f
        return FaceEditor.groupSize(face, belongs, large) / base
    }

    fun setGroupScale(key: String, target: Float) = edit("groupScale:$key") { face, current ->
        geometry(face, current) { f, c -> scaleGroupTo(f, c, key, target) }
    }

    fun resetGroup(key: String) = edit("groupReset:$key") { face, current ->
        val default = current.default ?: return@edit face
        FaceEditor.resetGroup(face, default, { ItemGroups.belongs(key, it) })
    }

    fun groupChanged(key: String): Boolean {
        val current = state.value
        val face = current.face ?: return false
        val default = current.default ?: return false
        if (isElementItem(key)) return false
        return FaceEditor.groupChanged(face, default) { ItemGroups.belongs(key, it) }
    }

    private fun isElementItem(key: String): Boolean {
        val draft = state.value.draft ?: return false
        val id = ItemGroups.itemId(key) ?: return false
        val style = draft.parts.composition.item(id)?.style ?: return false
        return draft.parts.widgets[style]?.category == CustomFaces.ELEMENT_CATEGORY
    }

    fun groupVisible(key: String, scope: EditScope): Boolean {
        val face = state.value.face ?: return true
        val belongs: (String) -> Boolean = { ItemGroups.belongs(key, it) }
        return when (scope) {
            EditScope.LARGE -> FaceEditor.groupIn(face.large, belongs)
            EditScope.SMALL -> FaceEditor.groupIn(face.small, belongs)
            EditScope.AOD -> FaceEditor.groupIn(
                face.aodLarge ?: face.large,
                belongs
            ) || FaceEditor.groupIn(face.aodSmall ?: face.small, belongs)
        }
    }

    fun setGroupVisible(key: String, scope: EditScope, visible: Boolean) {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        val split = FaceComposer.decompose(face, draft.parts)
        val states = when (scope) {
            EditScope.LARGE -> setOf(ItemState.LARGE)
            EditScope.SMALL -> setOf(ItemState.SMALL)
            EditScope.AOD -> setOf(ItemState.AOD_LARGE, ItemState.AOD_SMALL)
        }
        val id = ItemGroups.itemId(key)
        val items = split.composition.items.map { item ->
            val matches = if (id == null) item.clock else item.id == id
            if (!matches) return@map item
            val hidden = if (visible) item.hidden - states else item.hidden + states
            if (hidden.size == ItemState.entries.size) item else item.copy(hidden = hidden)
        }
        commitComposition(
            face,
            draft,
            split.copy(composition = split.composition.copy(items = items))
        )
    }

    fun groupAboveSubject(key: String): Boolean =
        state.value.draft?.parts?.composition?.items?.firstOrNull {
            matchesGroup(
                it,
                key
            )
        }?.aboveSubject ?: false

    fun setGroupAboveSubject(key: String, above: Boolean) {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        val split = FaceComposer.decompose(face, draft.parts)
        val items = split.composition.items.map {
            if (matchesGroup(
                    it,
                    key
                )
            ) it.copy(aboveSubject = above) else it
        }
        commitComposition(
            face,
            draft,
            split.copy(composition = split.composition.copy(items = items))
        )
    }

    fun groupPosition(key: String): OrderPosition? {
        val items = state.value.draft?.parts?.composition?.items ?: return null
        if (items.size < 2) return null
        return OrderPosition(items.indexOfFirst { matchesGroup(it, key) }, items.size)
    }

    fun removeGroup(key: String) {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        val split = FaceComposer.decompose(face, draft.parts)
        val target = split.composition.items.firstOrNull { matchesGroup(it, key) } ?: return
        commitComposition(
            face,
            draft,
            split.copy(composition = split.composition.remove(target.id))
        )
        state.update { it.copy(selectedId = null, editingOn = false) }
    }

    fun isGroupItem(key: String): Boolean {
        val draft = state.value.draft ?: return false
        val id = ItemGroups.itemId(key) ?: return false
        val style = draft.parts.composition.item(id)?.style ?: return false
        return draft.parts.widgets[style]?.category == CustomFaces.GROUP_CATEGORY
    }

    fun groupedMember(layerId: String): Boolean =
        ItemGrouping.memberOf(layerId) != null && isGroupItem(ItemGroups.keyOf(layerId))

    fun startGrouping(key: String) {
        val id = ItemGroups.itemId(key) ?: return
        state.update { it.copy(grouping = setOf(id), selectedId = null, editingOn = false) }
    }

    fun toggleGrouping(key: String) {
        val id = ItemGroups.itemId(key) ?: return
        state.update { current -> current.copy(grouping = current.grouping?.let { if (id in it) it - id else it + id }) }
    }

    fun cancelGrouping() {
        state.update { it.copy(grouping = null) }
    }

    fun beginGrouping() {
        state.update { it.copy(grouping = emptySet(), selectedId = null, editingOn = false) }
    }

    fun commitGrouping(name: String) {
        val current = state.value
        val selected = current.grouping ?: return
        val face = current.face ?: return
        val draft = current.draft ?: return
        state.update { it.copy(grouping = null) }
        if (selected.size < 2) return
        repository.save(draft, face)
        val fresh = repository.draft() ?: return
        val composition = fresh.parts.composition
        val members = composition.items.filter { !it.clock && it.id in selected }.map { it.id }
        if (members.size < 2) return
        val groups =
            members.filter { id -> composition.item(id)?.style?.let { fresh.parts.widgets[it]?.category } == CustomFaces.GROUP_CATEGORY }
                .toSet()
        val grouped = ItemGrouping.group(fresh.face, members, groups, name) ?: return
        val id = repository.group(members, grouped)
        open(ItemGroups.keyOfItem(id, false))
    }

    fun ungroup(key: String) {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        val id = ItemGroups.itemId(key) ?: return
        repository.save(draft, face)
        val fresh = repository.draft() ?: return
        val item = fresh.parts.composition.item(id) ?: return
        val group =
            fresh.parts.widgets[item.style]?.takeIf { it.category == CustomFaces.GROUP_CATEGORY }
                ?: return
        repository.ungroup(id, ItemGrouping.ungroup(group, item))
        open(null)
    }

    fun distribute(key: String, offsets: Map<Int, Pair<Float, Float>>) {
        val group = ItemGroups.itemId(key) ?: return
        if (offsets.isEmpty()) return
        edit("distribute:$key") { face, current ->
            geometry(face, current) { f, c ->
                val large = if (c.scope == EditScope.AOD) c.aodLarge else c.scope == EditScope.LARGE
                offsets.entries.fold(f) { acc, (member, delta) ->
                    val prefix = FaceComposer.prefix(group) + FaceComposer.prefix(member)
                    FaceEditor.moveMember(
                        acc,
                        { it.startsWith(prefix) },
                        large,
                        delta.first,
                        delta.second
                    )
                }
            }
        }
    }

    private fun alignKey(id: String): String? = when {
        state.value.scope == EditScope.AOD -> null
        ItemGroups.isGroup(id) && isGroupItem(id) -> null
        ItemGroups.isGroup(id) -> id
        else -> standaloneKey(id)
    }

    fun alignOf(id: String): FaceAlign? {
        val key = alignKey(id) ?: return null
        val face = state.value.face ?: return null
        return FaceEditor.groupAlign(
            face,
            { ItemGroups.belongs(key, it) },
            state.value.scope != EditScope.SMALL
        )
    }

    fun alignTarget(id: String, align: FaceAlign) {
        val key = alignKey(id) ?: return
        edit("alignGroup:$key") { face, current ->
            FaceEditor.alignGroup(
                face,
                { ItemGroups.belongs(key, it) },
                current.scope != EditScope.SMALL,
                align
            )
        }
    }

    fun canRemoveTarget(id: String): Boolean =
        state.value.scope != EditScope.AOD && (ItemGroups.isGroup(id) || canRemove(id) || standaloneKey(
            id
        ) != null)

    fun removeTarget(id: String) {
        when {
            ItemGroups.isGroup(id) -> removeGroup(id)
            canRemove(id) -> removeElement(id)
            else -> standaloneKey(id)?.let(::removeGroup)
        }
    }

    fun moveGroup(key: String, steps: Int) {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        val split = FaceComposer.decompose(face, draft.parts)
        val target = split.composition.items.firstOrNull { matchesGroup(it, key) } ?: return
        commitComposition(
            face,
            draft,
            split.copy(composition = split.composition.move(target.id, steps))
        )
    }

    fun groupLayers(key: String): List<FaceLayer> =
        state.value.face?.let { FaceBuilder.groupLayers(it, key) }.orEmpty()

    fun elementVisible(id: String): Boolean {
        val current = state.value
        val face = current.face ?: return true
        return when (current.scope) {
            EditScope.LARGE -> (FaceEditor.node(face, true, id)?.alpha ?: 1f) > 0f
            EditScope.SMALL -> (FaceEditor.node(face, false, id)?.alpha ?: 1f) > 0f
            EditScope.AOD -> FaceEditor.layer(face, id)
                ?.let { (it.sleepOpacity ?: 1f) > 0f && it.sleepStyle != FaceSleepStyle.HIDE }
                ?: true
        }
    }

    fun setElementVisible(id: String, visible: Boolean) = edit(Any()) { face, current ->
        when (current.scope) {
            EditScope.LARGE -> FaceEditor.setNodeAlpha(face, true, id, if (visible) 1f else 0f)
            EditScope.SMALL -> FaceEditor.setNodeAlpha(face, false, id, if (visible) 1f else 0f)
            EditScope.AOD -> FaceEditor.updateLayers(face, listOf(id)) { layer ->
                val style =
                    if (visible && layer.sleepStyle == FaceSleepStyle.HIDE) FaceSleepStyle.THIN else layer.sleepStyle
                layer.copy(sleepOpacity = if (visible) null else 0f, sleepStyle = style)
            }
        }
    }

    fun canRemove(id: String): Boolean = groupLayers(ItemGroups.keyOf(id)).size > 1

    fun removeElement(id: String) {
        if (!canRemove(id)) return
        edit(Any()) { face, _ -> FaceBuilder.remove(face, id) }
        state.update { it.copy(selectedId = ItemGroups.keyOf(id), editingOn = false) }
    }

    fun elementPosition(id: String): OrderPosition? =
        state.value.face?.let { FaceBuilder.position(it, id) }

    fun moveElement(id: String, steps: Int) =
        edit(Any()) { face, _ -> FaceBuilder.move(face, id, steps) }

    private fun matchesGroup(item: CompositionItem, key: String): Boolean {
        val id = ItemGroups.itemId(key)
        return if (id == null) item.clock else item.id == id
    }

    private fun commitComposition(face: ClockFace, draft: CompositionDraft, parts: ComposedFaces) {
        val updated = FaceComposer.compose(parts)
        if (updated == face && parts.composition == draft.parts.composition) return
        undoStack.addLast(EditorSnapshot(face, draft))
        if (undoStack.size > HISTORY_LIMIT) undoStack.removeFirst()
        redoStack.clear()
        lastEditKey = null
        state.update {
            it.copy(
                face = updated,
                draft = draft.copy(parts = parts),
                canUndo = true,
                canRedo = false
            )
        }
    }

    fun groupName(key: String): String {
        val draft = state.value.draft ?: return ""
        val id = ItemGroups.itemId(key) ?: return draft.parts.clock.name
        val style = draft.parts.composition.item(id)?.style ?: return ""
        return draft.parts.widgets[style]?.name.orEmpty()
    }

    private fun scaleGroupTo(
        face: ClockFace,
        current: EditorUiState,
        key: String,
        target: Float
    ): ClockFace {
        val large = current.scope != EditScope.SMALL
        val belongs: (String) -> Boolean = { ItemGroups.belongs(key, it) }
        val base =
            current.default?.let { FaceEditor.groupSize(it, belongs, large) }?.takeIf { it > 0f }
                ?: return face
        val size = FaceEditor.groupSize(face, belongs, large).takeIf { it > 0f } ?: return face
        return FaceEditor.scaleGroup(
            face,
            belongs,
            large,
            base * target.coerceIn(MIN_GROUP_SCALE, MAX_GROUP_SCALE) / size
        )
    }

    fun setHideIdle(ids: List<String>, hide: Boolean) = edit("hideIdle") { face, _ ->
        FaceEditor.updateLayers(face, ids) { it.copy(hideIdle = hide) }
    }

    fun updateLayer(id: String, key: Any, transform: (FaceLayer) -> FaceLayer) =
        edit(key) { face, _ ->
            FaceEditor.updateLayers(face, listOf(id), transform)
        }

    fun updateLayers(key: Any, transform: (FaceLayer) -> FaceLayer) = edit(key) { face, current ->
        FaceEditor.updateLayers(face, current.targets, transform)
    }

    fun setAlign(align: FaceAlign) = edit("align") { face, current ->
        val id = current.primary ?: return@edit face
        val layer = FaceEditor.layer(face, id)
        if (layer?.type == FaceLayerType.CARD) FaceEditor.setContentAlign(
            face,
            current.scope,
            id,
            align
        )
        else FaceEditor.setSelfAlign(face, current.scope, id, align)
    }

    fun move(id: String, dx: Float, dy: Float) = edit("move:$id") { face, current ->
        geometry(face, current) { f, c -> shift(f, c, id, dx, dy) }
    }

    private fun shift(
        face: ClockFace,
        current: EditorUiState,
        id: String,
        dx: Float,
        dy: Float
    ): ClockFace {
        val large =
            if (current.scope == EditScope.AOD) current.aodLarge else current.scope == EditScope.LARGE
        return if (ItemGroups.isGroup(id)) {
            FaceEditor.moveGroup(face, { ItemGroups.belongs(id, it) }, large, dx, dy)
        } else {
            FaceEditor.move(face, id, if (large) EditScope.LARGE else EditScope.SMALL, dx, dy)
        }
    }

    fun placementOf(current: EditorUiState, id: String): FacePlacement? =
        current.face?.let { face ->
            reading(current, face) { f, c -> placementIn(f, c.scope, id) }
        }

    fun placementIn(face: ClockFace, scope: EditScope, id: String): FacePlacement? {
        val large = scope != EditScope.SMALL
        return if (ItemGroups.isGroup(id)) {
            FaceEditor.groupPlacement(face, { ItemGroups.belongs(id, it) }, large)
        } else {
            FaceEditor.node(face, large, id)?.placement
        }
    }

    fun reportBounds(bounds: TargetBounds?) {
        if (state.value.bounds != bounds) state.update { it.copy(bounds = bounds) }
    }

    fun setPositionX(id: String, x: Float) {
        val placement = placementOf(state.value, id) ?: return
        move(id, x - placement.x, 0f)
    }

    fun setPositionY(id: String, y: Float) {
        val placement = placementOf(state.value, id) ?: return
        move(id, 0f, y - placement.y)
    }

    fun resize(id: String, width: Float, height: Float) = edit("resize:$id") { face, current ->
        val scoped = FaceEditor.write(
            face,
            listOf(id),
            current.scope,
            StyleKey.WIDTH,
            width.coerceIn(25f, 151f)
        )
        FaceEditor.write(
            scoped,
            listOf(id),
            current.scope,
            StyleKey.HEIGHT,
            height.coerceIn(560f, 788f)
        )
    }

    fun transformBase(id: String): Pair<Float, Float> {
        if (ItemGroups.isGroup(id)) return groupScale(id) to 0f
        val current = state.value
        val face = current.face ?: return 0f to 0f
        return reading(current, face) { f, c ->
            FaceEditor.read(
                f,
                id,
                c.scope,
                StyleKey.SIZE
            ) to FaceEditor.read(f, id, c.scope, StyleKey.ROTATION)
        }
    }

    fun transform(id: String, size: Float, rotation: Float, dx: Float, dy: Float) =
        edit("transform:$id") { face, current ->
            geometry(face, current) { f, c -> transformIn(f, c, id, size, rotation, dx, dy) }
        }

    private fun transformIn(
        face: ClockFace,
        current: EditorUiState,
        id: String,
        size: Float,
        rotation: Float,
        dx: Float,
        dy: Float
    ): ClockFace {
        if (ItemGroups.isGroup(id)) {
            val scaled = scaleGroupTo(face, current, id, size)
            return if (dx == 0f && dy == 0f) scaled else shift(scaled, current, id, dx, dy)
        }
        val sized = FaceEditor.write(
            face,
            listOf(id),
            current.scope,
            StyleKey.SIZE,
            size.coerceIn(MIN_SIZE, MAX_SIZE)
        )
        val turned = FaceEditor.write(
            sized,
            listOf(id),
            current.scope,
            StyleKey.ROTATION,
            rotation.coerceIn(-MAX_ROTATION, MAX_ROTATION)
        )
        return if (dx == 0f && dy == 0f) turned else shift(turned, current, id, dx, dy)
    }

    private var resizeBase: ClockFace? = null

    fun beginResize() {
        resizeBase = state.value.face
    }

    fun stretchTo(id: String, scaleX: Float, scaleY: Float) {
        val base = resizeBase ?: return
        edit("stretch:$id") { _, current ->
            geometry(base, current) { f, c ->
                stretchIn(
                    f,
                    c,
                    id,
                    scaleX,
                    scaleY
                )
            }
        }
    }

    private fun stretchIn(
        face: ClockFace,
        current: EditorUiState,
        id: String,
        scaleX: Float,
        scaleY: Float
    ): ClockFace {
        val sy = scaleY.coerceIn(MIN_STRETCH, MAX_STRETCH)
        val ratio = (scaleX.coerceIn(MIN_STRETCH, MAX_STRETCH) / sy)
        if (ItemGroups.isGroup(id)) {
            val large = current.scope != EditScope.SMALL
            val scaled = FaceEditor.scaleGroup(face, { ItemGroups.belongs(id, it) }, large, sy)
            return FaceBuilder.groupLayers(face, id)
                .fold(scaled) { f, layer -> stretchLayer(face, f, current, layer, 1f, ratio) }
        }
        val layer = FaceEditor.layer(face, id) ?: return face
        return stretchLayer(face, face, current, layer, sy, ratio)
    }

    private fun stretchLayer(
        base: ClockFace,
        face: ClockFace,
        current: EditorUiState,
        layer: FaceLayer,
        sy: Float,
        ratio: Float
    ): ClockFace {
        if (layer.type == FaceLayerType.DEVICE_ITEM) {
            return FaceEditor.updateLayers(face, listOf(layer.id)) {
                it.copy(
                    aspect = (layer.aspect * sy * ratio).coerceIn(
                        MIN_HOST_STRETCH,
                        MAX_HOST_STRETCH
                    ),
                    hostHeight = (layer.hostHeight * sy).coerceIn(
                        MIN_HOST_STRETCH,
                        MAX_HOST_STRETCH
                    )
                )
            }
        }
        if (layer.isShape) {
            val sized = if (sy == 1f) face else FaceEditor.write(
                face,
                listOf(layer.id),
                current.scope,
                StyleKey.SIZE,
                (FaceEditor.read(
                    base,
                    layer.id,
                    current.scope,
                    StyleKey.SIZE
                ) * sy * ratio).coerceIn(MIN_SIZE, MAX_SIZE)
            )
            return FaceEditor.updateLayers(
                sized,
                listOf(layer.id)
            ) { it.copy(aspect = (layer.aspect * ratio).coerceIn(MIN_ASPECT, MAX_ASPECT)) }
        }
        val sized = if (sy == 1f) face else FaceEditor.write(
            face,
            listOf(layer.id),
            current.scope,
            StyleKey.SIZE,
            (FaceEditor.read(base, layer.id, current.scope, StyleKey.SIZE) * sy).coerceIn(
                MIN_SIZE,
                MAX_SIZE
            )
        )
        val stretchX = FaceEditor.read(base, layer.id, current.scope, StyleKey.STRETCH_X) * ratio
        return FaceEditor.write(
            sized,
            listOf(layer.id),
            current.scope,
            StyleKey.STRETCH_X,
            stretchX.coerceIn(MIN_STRETCH, MAX_STRETCH)
        )
    }

    fun endGesture() {
        lastEditKey = null
        resizeBase = null
    }

    fun presets(): List<Pair<Int, ClockFace>> = repository.presets()

    fun applyPreset(style: Int) {
        val current = state.value
        val face = current.face
        val draft = current.draft
        if (face != null && draft != null) repository.save(draft, face)
        repository.resetPreset(style)
        repository.applyPreset(style)
        open(null)
    }

    fun exportJson(name: String, withAssets: Boolean = true): String? {
        val current = state.value
        val face = current.face ?: return null
        val draft = current.draft ?: return null
        repository.save(draft, face)
        val fresh = repository.draft() ?: return null
        return DesignerBundle.export(fresh.parts, name, withAssets)
    }

    fun importJson(json: String, fallbackName: String? = null): Boolean {
        val design = DesignerBundle.install(json) ?: return false
        repository.importDesign(design)
        if (fallbackName != null) {
            val name = design.name.ifBlank { fallbackName }
            repository.saveDesign(
                name,
                if (design.name.isBlank()) DesignerBundle.withName(json, name) else json
            )
        }
        open(null)
        return true
    }

    fun designs(): List<File> = repository.designs()

    fun saveDesign(name: String): Boolean {
        val json = exportJson(name) ?: return false
        return repository.saveDesign(name, json) != null
    }

    fun deleteDesign(file: File) = repository.deleteDesign(file)

    fun addElement(element: ClockFace, large: FacePlacement?, small: FacePlacement?) {
        val current = state.value
        val face = current.face
        val draft = current.draft
        if (face != null && draft != null) repository.save(draft, face)
        val id = repository.addElement(element, large, small)
        open(ItemGroups.keyOfItem(id, false))
    }

    fun commit() {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        repository.save(draft, face)
    }

    fun undo() {
        val current = state.value
        val face = current.face ?: return
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(EditorSnapshot(face, current.draft))
        lastEditKey = null
        state.update {
            it.copy(
                face = previous.face,
                draft = previous.draft,
                canUndo = undoStack.isNotEmpty(),
                canRedo = true
            )
        }
    }

    fun redo() {
        val current = state.value
        val face = current.face ?: return
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(EditorSnapshot(face, current.draft))
        lastEditKey = null
        state.update {
            it.copy(
                face = next.face,
                draft = next.draft,
                canUndo = true,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun save(then: () -> Unit) {
        val current = state.value
        val face = current.face ?: return
        val draft = current.draft ?: return
        if (current.saving) return
        state.update { it.copy(saving = true) }
        viewModelScope.launch {
            repository.save(draft, face)
            state.update { it.copy(saving = false) }
            then()
        }
    }

    private fun edit(key: Any, transform: (ClockFace, EditorUiState) -> ClockFace) {
        val current = state.value
        val face = current.face ?: return
        val updated = transform(face, current)
        if (updated == face) return
        val now = SystemClock.uptimeMillis()
        if (key != lastEditKey || now - lastEditAt > COALESCE_MS) {
            undoStack.addLast(EditorSnapshot(face, current.draft))
            if (undoStack.size > HISTORY_LIMIT) undoStack.removeFirst()
            redoStack.clear()
        }
        lastEditKey = key
        lastEditAt = now
        state.update { it.copy(face = updated, canUndo = undoStack.isNotEmpty(), canRedo = false) }
    }
}

private const val COALESCE_MS = 600L
private const val MIN_ASPECT = 0.05f
private const val MIN_HOST_STRETCH = 0.4f
private const val MAX_HOST_STRETCH = 4f
private const val MAX_ASPECT = 120f
private const val STOP_TIMEOUT = 5_000L
private const val HISTORY_LIMIT = 60
private const val MIN_SIZE = 0.02f
private const val MAX_SIZE = 1.5f
private const val MAX_ROTATION = 180f
private const val MIN_GROUP_SCALE = 0.3f
private const val MAX_GROUP_SCALE = 3f
private const val MIN_STRETCH = 0.25f
private const val MAX_STRETCH = 4f
