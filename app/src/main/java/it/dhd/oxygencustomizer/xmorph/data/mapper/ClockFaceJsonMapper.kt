package it.dhd.oxygencustomizer.xmorph.data.mapper

import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceBadge
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceChangeStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceImageFilter
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceSleepStyle
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTextEffect
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTile
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode
import org.json.JSONArray
import org.json.JSONObject

object ClockFaceJsonMapper {

    fun fromJson(json: String): ClockFace? = try {
        val root = JSONObject(json)
        val layers = root.getJSONArray("layers")
        val large = nodeOf(root.getJSONObject("large"))
        ClockFace(
            name = root.optString("name"),
            layers = List(layers.length()) { layerOf(layers.getJSONObject(it)) },
            large = large,
            small = root.optJSONObject("small")?.let { nodeOf(it) } ?: large,
            category = root.optString("category"),
            span = root.optInt("span", 1),
            id = root.optInt("id", -1),
            aodLarge = root.optJSONObject("aodLarge")?.let { nodeOf(it) },
            aodSmall = root.optJSONObject("aodSmall")?.let { nodeOf(it) },
            aboveSubject = root.optJSONArray("aboveSubject")
                ?.let { list -> List(list.length()) { list.optString(it) }.toSet() }.orEmpty()
        )
    } catch (_: Exception) {
        null
    }

    fun toJson(face: ClockFace): String = JSONObject()
        .put("name", face.name)
        .apply { if (face.category.isNotEmpty()) put("category", face.category) }
        .apply { if (face.span != 1) put("span", face.span) }
        .apply { if (face.id >= 0) put("id", face.id) }
        .apply {
            face.aodLarge?.let { put("aodLarge", it.toJson()) }
            face.aodSmall?.let { put("aodSmall", it.toJson()) }
            if (face.aboveSubject.isNotEmpty()) put(
                "aboveSubject",
                JSONArray().apply { face.aboveSubject.sorted().forEach { put(it) } })
        }
        .put("layers", JSONArray().apply { face.layers.forEach { put(it.toJson()) } })
        .put("large", face.large.toJson())
        .put("small", face.small.toJson())
        .toString(2)

    private fun layerOf(json: JSONObject): FaceLayer {
        val type = enumOf(json.optString("type"), FaceLayerType.TEXT)
        return FaceLayer(
            id = json.optString("id").ifEmpty { type.name.lowercase() },
            type = type,
            format = json.optString("format"),
            font = json.optString("font"),
            weight = json.optInt("weight", 560),
            sleepWeight = json.optInt("sleepWeight", 250),
            width = json.optInt("width", 25),
            height = json.optInt("height", 788),
            sleepWidth = json.optIntOrNull("sleepWidth"),
            sleepHeight = json.optIntOrNull("sleepHeight"),
            tracking = json.optFloat("tracking", 0f),
            color = json.optString("color"),
            change = enumOf(json.optString("change"), FaceChangeStyle.SLIDE_UP),
            sleepStyle = enumOf(json.optString("sleepStyle"), FaceSleepStyle.THIN),
            lineHeight = json.optFloat("lineHeight", 1f),
            outline = json.optBoolean("outline"),
            outlineWidth = json.optFloat("outlineWidth", 1.5f),
            effect = enumOf(json.optString("effect"), FaceTextEffect.SOLID),
            opacity = json.optFloat("opacity", 1f),
            effectTint = if (json.has("effectTint")) json.optFloat("effectTint", 0f) else null,
            caps = json.optBoolean("caps"),
            hourZero = json.optBoolean("hourZero", true),
            hideIdle = json.optBoolean("hideIdle"),
            maxEms = if (json.has("maxEms")) json.optDouble("maxEms").toFloat()
                .takeIf { it > 0f } else null,
            stroke = json.optString("stroke").ifEmpty { null },
            strokeWidth = json.optFloat("strokeWidth", 0.06f),
            shadow = json.optFloat("shadow", 0f),
            handLength = json.optFloat("handLength", 0.42f),
            badge = json.optString("badge")
                .let { name -> FaceBadge.entries.firstOrNull { it.name == name } }
                ?: FaceBadge.NONE,
            badgeColor = json.optString("badgeColor").ifEmpty { null },
            badgePadding = json.optFloat("badgePadding", 0.22f),
            roman = json.optBoolean("roman"),
            tile = json.optString("tile")
                .let { name -> FaceTile.entries.firstOrNull { it.name == name } },
            sleepColor = json.optString("sleepColor").ifEmpty { null },
            aspect = json.optFloat("aspect", 1f).takeIf { it.isFinite() && it > 0f } ?: 1f,
            hostHeight = json.optFloat("hostHeight", 1f).takeIf { it.isFinite() && it > 0f } ?: 1f,
            imageFilter = json.optString("imageFilter")
                .let { name -> FaceImageFilter.entries.firstOrNull { it.name == name } }
                ?: FaceImageFilter.NONE,
            imageBlur = json.optInt("imageBlur", 30).coerceIn(10, 100),
            imageTint = json.optInt("imageTint", 40).coerceIn(10, 90),
            radius = json.optFloat("radius", 0.5f),
            sleepScale = if (json.has("sleepScale")) json.optFloat("sleepScale", 1f) else null,
            sleepRotation = if (json.has("sleepRotation")) json.optFloat(
                "sleepRotation",
                0f
            ) else null,
            sleepOpacity = if (json.has("sleepOpacity")) json.optFloat(
                "sleepOpacity",
                1f
            ) else null,
            sleepMono = json.optBoolean("sleepMono", false),
            sleepStretch = if (json.has("sleepStretch")) json.optFloat(
                "sleepStretch",
                1f
            ) else null,
            sleepStretchX = if (json.has("sleepStretchX")) json.optFloat(
                "sleepStretchX",
                1f
            ) else null,
            activeColor = json.optString("activeColor").ifEmpty { null },
            activeTint = if (json.has("activeTint")) json.optFloat("activeTint", 0f) else null
        )
    }

    private fun nodeOf(json: JSONObject): FaceNode {
        val placement = FacePlacement(
            x = json.optFloat("x", 0f),
            y = json.optFloat("y", 0f),
            anchor = enumOf(json.optString("anchor"), FaceAlign.START),
            hidden = json.optBoolean("hidden"),
            auto = json.optBoolean("auto")
        )
        json.optJSONObject("row")?.let { row ->
            return RowNode(
                children = childrenOf(row),
                gap = row.optFloat("gap", 0f),
                align = enumOf(row.optString("align"), FaceAlign.END),
                placement = placement
            )
        }
        json.optJSONObject("column")?.let { column ->
            return ColumnNode(
                children = childrenOf(column),
                gap = column.optFloat("gap", 0f),
                align = enumOf(column.optString("align"), FaceAlign.CENTER),
                placement = placement
            )
        }
        json.optJSONObject("stack")?.let { stack ->
            return StackNode(
                children = childrenOf(stack),
                width = if (stack.has("width")) stack.optFloat("width", 0f) else null,
                height = if (stack.has("height")) stack.optFloat("height", 0f) else null,
                placement = placement
            )
        }
        return LayerNode(
            id = json.getString("layer"),
            size = json.optFloat("size", 0.1f),
            alpha = json.optFloat("alpha", 1f),
            rotation = json.optFloat("rotation", 0f),
            tiltX = json.optFloat("tiltX", 0f),
            tiltY = json.optFloat("tiltY", 0f),
            width = json.optIntOrNull("width"),
            height = json.optIntOrNull("height"),
            tracking = if (json.has("tracking")) json.optFloat("tracking", 0f) else null,
            weight = json.optIntOrNull("weight"),
            color = json.optString("color").ifEmpty { null },
            lineHeight = if (json.has("lineHeight")) json.optFloat("lineHeight", 1f) else null,
            stretch = if (json.has("stretch")) json.optFloat("stretch", 1f) else null,
            stretchX = if (json.has("stretchX")) json.optFloat("stretchX", 1f) else null,
            effect = json.optString("effect").ifEmpty { null }
                ?.let { enumOf(it, FaceTextEffect.SOLID) },
            align = json.optString("align").ifEmpty { null }?.let { enumOf(it, FaceAlign.START) },
            placement = placement
        )
    }

    private fun childrenOf(json: JSONObject): List<FaceNode> {
        val children = json.optJSONArray("children") ?: return emptyList()
        return List(children.length()) { nodeOf(children.getJSONObject(it)) }
    }

    private fun JSONObject.optFloat(name: String, fallback: Float): Float =
        optDouble(name, fallback.toDouble()).toFloat()

    private fun JSONObject.optIntOrNull(name: String): Int? =
        if (has(name)) optInt(name) else null

    private inline fun <reified T : Enum<T>> enumOf(value: String, fallback: T): T =
        enumValues<T>().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: fallback

    private fun FaceLayer.toJson(): JSONObject = JSONObject()
        .put("id", id)
        .put("type", type.name)
        .put("format", format)
        .put("font", font)
        .put("weight", weight)
        .put("sleepWeight", sleepWeight)
        .put("width", width)
        .put("height", height)
        .apply {
            sleepWidth?.let { put("sleepWidth", it) }
            sleepHeight?.let { put("sleepHeight", it) }
        }
        .put("tracking", tracking.toDouble())
        .put("color", color)
        .put("change", change.name)
        .put("sleepStyle", sleepStyle.name)
        .put("lineHeight", lineHeight.toDouble())
        .put("outline", outline)
        .put("outlineWidth", outlineWidth.toDouble())
        .put("effect", effect.name)
        .put("opacity", opacity.toDouble())
        .apply {
            effectTint?.let { put("effectTint", it.toDouble()) }
            if (caps) put("caps", true)
            if (!hourZero) put("hourZero", false)
            if (hideIdle) put("hideIdle", true)
            maxEms?.let { put("maxEms", it.toDouble()) }
            stroke?.let { put("stroke", it) }
            if (stroke != null || type == FaceLayerType.GAUGE) put(
                "strokeWidth",
                strokeWidth.toDouble()
            )
            if (shadow > 0f) put("shadow", shadow.toDouble())
            if (isHand) put("handLength", handLength.toDouble())
            if (roman) put("roman", true)
            tile?.let { put("tile", it.name) }
            if (badge != FaceBadge.NONE) {
                put("badge", badge.name)
                badgeColor?.let { put("badgeColor", it) }
                put("badgePadding", badgePadding.toDouble())
            }
        }
        .apply {
            sleepColor?.let { put("sleepColor", it) }
            sleepScale?.let { put("sleepScale", it.toDouble()) }
            sleepRotation?.let { put("sleepRotation", it.toDouble()) }
            sleepOpacity?.let { put("sleepOpacity", it.toDouble()) }
            if (sleepMono) put("sleepMono", true)
            sleepStretch?.let { put("sleepStretch", it.toDouble()) }
            sleepStretchX?.let { put("sleepStretchX", it.toDouble()) }
            activeColor?.let { put("activeColor", it) }
            activeTint?.let { put("activeTint", it.toDouble()) }
            if (type == FaceLayerType.CARD || type == FaceLayerType.GAUGE || type == FaceLayerType.IMAGE || type == FaceLayerType.MEDIA_ART || type == FaceLayerType.SPACER) {
                put("aspect", (aspect.takeIf { it.isFinite() && it > 0f } ?: 1f).toDouble())
                put("radius", radius.toDouble())
            }
            if (type == FaceLayerType.IMAGE && imageFilter != FaceImageFilter.NONE) {
                put("imageFilter", imageFilter.name)
                put("imageBlur", imageBlur)
                put("imageTint", imageTint)
            }
            if (type == FaceLayerType.DEVICE_ITEM) {
                put("aspect", (aspect.takeIf { it.isFinite() && it > 0f } ?: 1f).toDouble())
                put("hostHeight", (hostHeight.takeIf { it.isFinite() && it > 0f } ?: 1f).toDouble())
            }
        }

    private fun FaceNode.toJson(): JSONObject {
        val json = JSONObject()
            .put("x", placement.x.toDouble())
            .put("y", placement.y.toDouble())
            .put("anchor", placement.anchor.name)
        if (placement.hidden) json.put("hidden", true)
        if (placement.auto) json.put("auto", true)

        fun children(nodes: List<FaceNode>) =
            JSONArray().apply { nodes.forEach { put(it.toJson()) } }

        when (this) {
            is LayerNode -> json
                .put("layer", id)
                .put("size", size.toDouble())
                .put("alpha", alpha.toDouble())
                .put("rotation", rotation.toDouble())
                .apply {
                    if (tiltX != 0f) put("tiltX", tiltX.toDouble())
                    if (tiltY != 0f) put("tiltY", tiltY.toDouble())
                    width?.let { put("width", it) }
                    height?.let { put("height", it) }
                    tracking?.let { put("tracking", it.toDouble()) }
                    weight?.let { put("weight", it) }
                    color?.let { put("color", it) }
                    lineHeight?.let { put("lineHeight", it.toDouble()) }
                    stretch?.let { put("stretch", it.toDouble()) }
                    stretchX?.let { put("stretchX", it.toDouble()) }
                    effect?.let { put("effect", it.name) }
                    align?.let { put("align", it.name) }
                }

            is RowNode -> json.put(
                "row",
                JSONObject().put("gap", gap.toDouble()).put("align", align.name)
                    .put("children", children(this.children))
            )

            is ColumnNode -> json.put(
                "column",
                JSONObject().put("gap", gap.toDouble()).put("align", align.name)
                    .put("children", children(this.children))
            )

            is StackNode -> json.put(
                "stack",
                JSONObject().put("children", children(this.children)).apply {
                    width?.let { put("width", it.toDouble()) }
                    height?.let { put("height", it.toDouble()) }
                }
            )
        }
        return json
    }
}
