package it.dhd.oxygencustomizer.xmorph.data.source

import android.content.Context
import it.dhd.oxygencustomizer.xmorph.data.mapper.ClockFaceJsonMapper
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace

object ClockFaces {

    const val STYLE_BASE = 2000
    private const val PRESET_DIR = "xmorph/clock_faces"
    private const val JSON = ".json"

    @Volatile
    private var presets: List<ClockFace>? = null

    fun presets(context: Context): List<ClockFace> = presets ?: synchronized(this) {
        presets ?: load(context).also { presets = it }
    }

    fun styleOf(index: Int): Int = STYLE_BASE + index

    fun faceOf(context: Context, style: Int): ClockFace? =
        presets(context).getOrNull(style - STYLE_BASE)

    private fun load(context: Context): List<ClockFace> {
        val assets = context.assets
        val faces = sortedMapOf<String, ClockFace>()

        assets.list(PRESET_DIR).orEmpty()
            .filter { it.endsWith(JSON) }
            .forEach { file ->
                readFace { assets.open("$PRESET_DIR/$file").bufferedReader().use { it.readText() } }
                    ?.let { faces[file] = it }
            }

        return faces.values.toList()
    }

    private fun readFace(read: () -> String): ClockFace? = try {
        ClockFaceJsonMapper.fromJson(read())
    } catch (_: Exception) {
        null
    }
}
