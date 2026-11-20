package it.dhd.oxygencustomizer.xmorph.designer

import android.util.Base64
import it.dhd.oxygencustomizer.xmorph.data.mapper.ClockFaceJsonMapper
import it.dhd.oxygencustomizer.xmorph.data.mapper.CompositionJsonMapper
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceComposer
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceImageRefs
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ComposedFaces
import it.dhd.oxygencustomizer.xmorph.domain.model.Composition
import it.dhd.oxygencustomizer.xmorph.domain.model.UserFont
import org.json.JSONObject

object DesignerBundle {

    private const val VERSION = 1
    private const val KEY_VERSION = "version"
    private const val KEY_NAME = "name"
    private const val KEY_CLOCK = "clock"
    private const val KEY_WIDGETS = "widgets"
    private const val KEY_COMPOSITION = "composition"
    private const val KEY_IMAGES = "images"
    private const val KEY_FONTS = "fonts"

    data class Design(
        val clock: ClockFace,
        val widgets: Map<Int, ClockFace>,
        val composition: Composition,
        val name: String = ""
    ) {
        fun composed(): ClockFace = FaceComposer.compose(ComposedFaces(clock, widgets, composition))
    }

    fun export(parts: ComposedFaces, name: String, withAssets: Boolean = true): String {
        val faces = listOf(parts.clock) + parts.widgets.values
        val root = JSONObject()
            .put(KEY_VERSION, VERSION)
            .put(KEY_NAME, name)
            .put(KEY_CLOCK, JSONObject(ClockFaceJsonMapper.toJson(parts.clock)))
            .put(
                KEY_WIDGETS,
                JSONObject().apply {
                    parts.widgets.forEach { (style, face) ->
                        put(
                            style.toString(),
                            JSONObject(ClockFaceJsonMapper.toJson(face))
                        )
                    }
                })
            .put(KEY_COMPOSITION, JSONObject(CompositionJsonMapper.toJson(parts.composition)))
        if (withAssets) {
            root.put(
                KEY_IMAGES,
                encode(faces.flatMap { FaceImageRefs.ids(it) }.toSet(), DesignerImages::read)
            )
            root.put(
                KEY_FONTS,
                encode(faces.flatMap { face -> face.layers.mapNotNull { UserFont.idOf(it.font) } }
                    .toSet()) { id -> DesignerFonts.fileOf(id)?.readBytes() })
        }
        return root.toString()
    }

    fun preview(json: String): Design? = try {
        parse(JSONObject(json))
    } catch (_: Exception) {
        null
    }

    fun install(json: String): Design? = try {
        val root = JSONObject(json)
        val design = parse(root) ?: return null
        val images = decode(root.optJSONObject(KEY_IMAGES), DesignerImages::write)
        val fonts = decode(root.optJSONObject(KEY_FONTS), DesignerFonts::write)
        val remap: (ClockFace) -> ClockFace =
            { face -> remapFonts(FaceImageRefs.remap(face, images), fonts) }
        Design(
            remap(design.clock),
            design.widgets.mapValues { remap(it.value) },
            design.composition,
            design.name
        )
    } catch (_: Exception) {
        null
    }

    private fun parse(root: JSONObject): Design? {
        val clock =
            root.optJSONObject(KEY_CLOCK)?.let { ClockFaceJsonMapper.fromJson(it.toString()) }
                ?: return null
        val widgets = root.optJSONObject(KEY_WIDGETS)?.let { json ->
            json.keys().asSequence().mapNotNull { key ->
                val style = key.toIntOrNull() ?: return@mapNotNull null
                val face =
                    json.optJSONObject(key)?.let { ClockFaceJsonMapper.fromJson(it.toString()) }
                        ?: return@mapNotNull null
                style to face
            }.toMap()
        }.orEmpty()
        val composition =
            CompositionJsonMapper.fromJson(root.optJSONObject(KEY_COMPOSITION)?.toString())
        return Design(clock, widgets, composition.retain(widgets.keys), root.optString(KEY_NAME))
    }

    fun withName(json: String, name: String): String = try {
        JSONObject(json).put(KEY_NAME, name).toString()
    } catch (_: Exception) {
        json
    }

    private fun encode(ids: Set<String>, read: (String) -> ByteArray?): JSONObject =
        JSONObject().apply {
            ids.forEach { id ->
                read(id)?.let {
                    put(
                        id,
                        Base64.encodeToString(it, Base64.NO_WRAP)
                    )
                }
            }
        }

    private fun decode(
        json: JSONObject?,
        write: (String, ByteArray) -> String?
    ): Map<String, String> {
        json ?: return emptyMap()
        return json.keys().asSequence().mapNotNull { id ->
            val bytes = Base64.decode(json.optString(id), Base64.NO_WRAP)
            write(id, bytes)?.let { id to it }
        }.toMap()
    }

    private fun remapFonts(face: ClockFace, fonts: Map<String, String>): ClockFace {
        if (fonts.all { (from, to) -> from == to }) return face
        return face.copy(layers = face.layers.map { layer ->
            val id = UserFont.idOf(layer.font) ?: return@map layer
            val target = fonts[id] ?: return@map layer
            layer.copy(
                font = layer.font.replaceFirst(
                    UserFont.SCHEME + id,
                    UserFont.SCHEME + target
                )
            )
        })
    }
}
