package it.dhd.oxygencustomizer.xmorph.designer

import android.content.Context
import it.dhd.oxygencustomizer.utils.OCPreferences
import it.dhd.oxygencustomizer.xmorph.data.mapper.ClockFaceJsonMapper
import it.dhd.oxygencustomizer.xmorph.data.mapper.CompositionJsonMapper
import it.dhd.oxygencustomizer.xmorph.data.source.FaceLibrary
import it.dhd.oxygencustomizer.xmorph.domain.editor.CustomFaces
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceComposer
import it.dhd.oxygencustomizer.xmorph.domain.editor.GroupMember
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ComposedFaces
import it.dhd.oxygencustomizer.xmorph.domain.model.Composition
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionDraft
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceKind
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.UngroupedItem
import java.io.File

class DesignerRepository(context: Context) {

    private val context = context.applicationContext

    fun presets(): List<Pair<Int, ClockFace>> =
        FaceLibrary.presets(context, FaceKind.CLOCK).mapIndexed { index, face ->
            FaceLibrary.styleOf(
                FaceKind.CLOCK,
                face,
                index
            ) to (face(FaceLibrary.styleOf(FaceKind.CLOCK, face, index)) ?: face)
        }

    var clockStyle: Int
        get() = OCPreferences.getInt(
            KEY_STYLE,
            presets().firstOrNull()?.first ?: FaceKind.CLOCK.styleBase
        )
        set(value) = OCPreferences.putInt(KEY_STYLE, value)

    var composition: Composition
        get() = CompositionJsonMapper.fromJson(OCPreferences.getString(KEY_COMPOSITION, null))
        set(value) = OCPreferences.putString(KEY_COMPOSITION, CompositionJsonMapper.toJson(value))

    fun defaultFace(style: Int): ClockFace? =
        read(KEY_BASE_PREFIX + style) ?: FaceLibrary.faceOf(context, style)

    fun face(style: Int): ClockFace? =
        (read(KEY_FACE_PREFIX + style) ?: defaultFace(style))?.let(DesignerFonts::sanitize)

    fun saveFace(style: Int, face: ClockFace) {
        if (face == defaultFace(style)) resetFace(style)
        else OCPreferences.putString(KEY_FACE_PREFIX + style, ClockFaceJsonMapper.toJson(face))
    }

    fun resetFace(style: Int) = OCPreferences.clear(KEY_FACE_PREFIX + style)

    private fun deleteFace(style: Int) =
        OCPreferences.clear(KEY_FACE_PREFIX + style, KEY_BASE_PREFIX + style)

    private fun read(key: String): ClockFace? =
        OCPreferences.getString(key, null)?.let(ClockFaceJsonMapper::fromJson)

    fun draft(): CompositionDraft? {
        val style = clockStyle
        val clock = face(style) ?: presets().firstOrNull()?.second ?: return null
        val current = composition
        val widgets =
            current.widgetStyles.mapNotNull { widget -> face(widget)?.let { widget to it } }.toMap()
        val resolved = current.retain(widgets.keys)
        val parts = ComposedFaces(clock, widgets, resolved)
        val defaults = ComposedFaces(
            clock = defaultFace(style) ?: clock,
            widgets = widgets.mapValues { (widget, face) -> defaultFace(widget) ?: face },
            composition = resolved
        )
        return CompositionDraft(
            style,
            parts,
            FaceComposer.compose(parts),
            FaceComposer.compose(defaults)
        )
    }

    fun save(draft: CompositionDraft, face: ClockFace) {
        val split = FaceComposer.decompose(face, draft.parts)
        if (split.composition.clockItem != null && split.clock != draft.parts.clock) saveFace(
            draft.clockStyle,
            split.clock
        )
        split.widgets.forEach { (style, widget) ->
            if (widget != draft.parts.widgets[style]) saveFace(
                style,
                widget
            )
        }
        val kept = split.composition.widgetStyles
        draft.parts.composition.widgetStyles.filterNot { it in kept }.forEach { style ->
            val widget = draft.parts.widgets[style] ?: return@forEach
            if (widget.category == CustomFaces.ELEMENT_CATEGORY) deleteFace(style)
        }
        apply(draft.clockStyle, split.composition)
    }

    fun applyPreset(style: Int) {
        apply(style, composition.withClock())
    }

    private fun storeElement(face: ClockFace): Int {
        val style = OCPreferences.getInt(KEY_NEXT_ELEMENT, FaceKind.WIDGET.mineBase)
        OCPreferences.putInt(KEY_NEXT_ELEMENT, style + 1)
        OCPreferences.putString(KEY_BASE_PREFIX + style, ClockFaceJsonMapper.toJson(face))
        return style
    }

    private fun deletePrivate(style: Int) {
        val stored = face(style) ?: return
        if (stored.category == CustomFaces.ELEMENT_CATEGORY || stored.category == CustomFaces.GROUP_CATEGORY) deleteFace(
            style
        )
    }

    fun group(members: List<Int>, face: ClockFace): Int {
        val style = storeElement(face)
        val current = composition
        val memberStyles = members.mapNotNull { id -> current.item(id)?.style }
        val id = current.nextId
        apply(clockStyle, current.group(members, style))
        memberStyles.forEach(::deletePrivate)
        return id
    }

    fun ungroup(id: Int, members: List<GroupMember>) {
        val current = composition
        val style = current.item(id)?.style ?: return
        val items = members.map { member ->
            UngroupedItem(
                storeElement(member.face),
                member.large,
                member.small,
                member.aodLarge,
                member.aodSmall,
                member.hidden
            )
        }
        apply(clockStyle, current.ungroup(id, items))
        deletePrivate(style)
    }

    fun importDesign(design: DesignerBundle.Design) {
        val style = OCPreferences.getInt(KEY_NEXT_CLOCK, FaceKind.CLOCK.mineBase)
        OCPreferences.putInt(KEY_NEXT_CLOCK, style + 1)
        OCPreferences.putString(KEY_BASE_PREFIX + style, ClockFaceJsonMapper.toJson(design.clock))
        val styles = design.widgets.mapValues { (_, face) -> storeElement(face) }
        val items = design.composition.items.mapNotNull { item ->
            if (item.clock) item else styles[item.style]?.let { item.copy(style = it) }
        }
        apply(style, design.composition.copy(items = items))
    }

    private val designsDir: File
        get() = File(context.filesDir, DESIGNS_DIR)

    fun designs(): List<File> =
        designsDir.listFiles()?.filter { it.isFile && it.name.endsWith(DESIGN_EXTENSION) }
            ?.sortedByDescending { it.lastModified() }.orEmpty()

    fun saveDesign(name: String, json: String): File? {
        if (!designsDir.isDirectory && !designsDir.mkdirs()) return null
        designs().firstOrNull { it.readText() == json }?.let { return it }
        val base = name.trim().replace(Regex("[^A-Za-z0-9 _-]"), "_").ifEmpty { "design" }
        var target = File(designsDir, base + DESIGN_EXTENSION)
        var index = 1
        while (target.exists()) target =
            File(designsDir, "$base ($index)$DESIGN_EXTENSION").also { index++ }
        return try {
            target.writeText(json)
            target
        } catch (_: Exception) {
            null
        }
    }

    fun deleteDesign(file: File) {
        if (file.parentFile == designsDir) file.delete()
    }

    fun resetPreset(style: Int) = resetFace(style)

    fun addElement(face: ClockFace, large: FacePlacement?, small: FacePlacement?): Int {
        val style = storeElement(face)
        val updated = composition
        val id = updated.nextId
        apply(clockStyle, updated.add(style, large, small))
        return id
    }

    private fun apply(style: Int, target: Composition) {
        val clock = face(style) ?: return
        val widgets =
            target.widgetStyles.mapNotNull { widget -> face(widget)?.let { widget to it } }.toMap()
        val resolved = target.retain(widgets.keys)
        clockStyle = style
        composition = resolved
        OCPreferences.putString(
            KEY_APPLIED,
            ClockFaceJsonMapper.toJson(
                FaceComposer.compose(
                    ComposedFaces(
                        clock,
                        widgets,
                        resolved
                    )
                )
            )
        )
    }

    companion object {
        const val KEY_STYLE = "xmorph_clock_style"
        const val KEY_COMPOSITION = "xmorph_composition"
        const val KEY_APPLIED = "xmorph_applied_face"
        private const val KEY_FACE_PREFIX = "xmorph_face_"
        private const val KEY_BASE_PREFIX = "xmorph_base_"
        private const val KEY_NEXT_ELEMENT = "xmorph_next_element"
        private const val KEY_NEXT_CLOCK = "xmorph_next_clock"
        private const val DESIGNS_DIR = "designs"
        const val DESIGN_EXTENSION = ".ocdesign"
    }
}
