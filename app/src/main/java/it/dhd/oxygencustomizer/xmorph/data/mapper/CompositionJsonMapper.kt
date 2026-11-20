package it.dhd.oxygencustomizer.xmorph.data.mapper

import it.dhd.oxygencustomizer.xmorph.domain.model.Composition
import it.dhd.oxygencustomizer.xmorph.domain.model.CompositionItem
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FacePlacement
import it.dhd.oxygencustomizer.xmorph.domain.model.ItemState
import org.json.JSONArray
import org.json.JSONObject

object CompositionJsonMapper {

    fun toJson(composition: Composition): String = JSONObject()
        .put("nextId", composition.nextId)
        .put("items", JSONArray().apply {
            composition.items.forEach { item ->
                put(
                    JSONObject()
                        .put("id", item.id)
                        .put("clock", item.clock)
                        .put("style", item.style)
                        .apply {
                            item.large?.let { put("large", it.toJson()) }
                            item.small?.let { put("small", it.toJson()) }
                            item.aodLarge?.let { put("aodLarge", it.toJson()) }
                            item.aodSmall?.let { put("aodSmall", it.toJson()) }
                            if (item.hidden.isNotEmpty()) put(
                                "hidden",
                                JSONArray().apply { item.hidden.forEach { put(it.name) } })
                            if (item.largeScale != 1f) put("largeScale", item.largeScale.toDouble())
                            if (item.smallScale != 1f) put("smallScale", item.smallScale.toDouble())
                            if (item.aboveSubject) put("aboveSubject", true)
                        }
                )
            }
        })
        .toString()

    fun fromJson(json: String?): Composition {
        if (json.isNullOrEmpty()) return Composition()
        return try {
            val root = JSONObject(json)
            val array = root.optJSONArray("items") ?: return migrate(root)
            val items = List(array.length()) { index ->
                val item = array.getJSONObject(index)
                CompositionItem(
                    id = item.optInt("id", index),
                    clock = item.optBoolean("clock"),
                    style = item.optInt("style", -1),
                    large = item.optJSONObject("large")?.let(::placementOf),
                    small = item.optJSONObject("small")?.let(::placementOf),
                    aodLarge = item.optJSONObject("aodLarge")?.let(::placementOf),
                    aodSmall = item.optJSONObject("aodSmall")?.let(::placementOf),
                    largeScale = item.optDouble("largeScale", 1.0).toFloat(),
                    smallScale = item.optDouble("smallScale", 1.0).toFloat(),
                    aboveSubject = item.optBoolean("aboveSubject"),
                    hidden = item.optJSONArray("hidden")?.let { list ->
                        List(list.length()) { list.optString(it) }.mapNotNullTo(HashSet()) { name -> ItemState.entries.firstOrNull { it.name == name } }
                    }.orEmpty()
                )
            }
            Composition(items, root.optInt("nextId", (items.maxOfOrNull { it.id } ?: 0) + 1))
        } catch (_: Exception) {
            Composition()
        }
    }

    private fun migrate(root: JSONObject): Composition {
        val slots = root.optJSONArray("slots") ?: return Composition()
        var composition = Composition(items = emptyList(), nextId = 0)
        var count = 0
        for (index in 0 until slots.length()) {
            val slot = slots.getJSONObject(index)
            if (slot.optBoolean("clock")) composition = composition.withClock()
            val widgets = slot.optJSONArray("widgets") ?: continue
            for (position in 0 until widgets.length()) {
                composition = composition.add(
                    widgets.getInt(position),
                    FacePlacement(CENTER_X, LARGE_TOP + LARGE_STEP * count, FaceAlign.CENTER),
                    FacePlacement(EDGE_X, SMALL_TOP + SMALL_STEP * count, FaceAlign.START)
                )
                count++
            }
        }
        return composition
    }

    private fun FacePlacement.toJson(): JSONObject = JSONObject()
        .put("x", x.toDouble())
        .put("y", y.toDouble())
        .put("anchor", anchor.name)
        .apply { if (auto) put("auto", true) }

    private fun placementOf(json: JSONObject): FacePlacement = FacePlacement(
        x = json.optDouble("x", 0.0).toFloat(),
        y = json.optDouble("y", 0.0).toFloat(),
        anchor = FaceAlign.entries.firstOrNull { it.name == json.optString("anchor") }
            ?: FaceAlign.START,
        auto = json.optBoolean("auto")
    )

    private const val CENTER_X = 0.5f
    private const val EDGE_X = 0.0225f
    private const val LARGE_TOP = 0.62f
    private const val LARGE_STEP = 0.2f
    private const val SMALL_TOP = 0.3f
    private const val SMALL_STEP = 0.14f
}
