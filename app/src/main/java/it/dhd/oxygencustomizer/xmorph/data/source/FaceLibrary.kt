package it.dhd.oxygencustomizer.xmorph.data.source

import android.content.Context
import it.dhd.oxygencustomizer.xmorph.data.mapper.ClockFaceJsonMapper
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceKind
import java.util.concurrent.ConcurrentHashMap

object FaceLibrary {

    private const val JSON = ".json"
    private val cache = ConcurrentHashMap<FaceKind, List<ClockFace>>()

    fun presets(context: Context, kind: FaceKind): List<ClockFace> = when (kind) {
        FaceKind.CLOCK -> ClockFaces.presets(context)
        else -> cache.getOrPut(kind) { load(context, kind) }
    }

    fun styleOf(kind: FaceKind, face: ClockFace, index: Int): Int =
        kind.styleBase + (face.id.takeIf { it >= 0 } ?: index)

    fun faceOf(context: Context, style: Int): ClockFace? {
        val kind = FaceKind.of(style) ?: return null
        if (kind.isMine(style)) return null
        return presets(context, kind).withIndex()
            .firstOrNull { (index, face) -> styleOf(kind, face, index) == style }?.value
    }

    private fun load(context: Context, kind: FaceKind): List<ClockFace> {
        val assets = context.assets
        return assets.list(kind.assetDir).orEmpty()
            .filter { it.endsWith(JSON) }
            .sorted()
            .mapNotNull { file ->
                runCatching {
                    ClockFaceJsonMapper.fromJson(
                        assets.open("${kind.assetDir}/$file").bufferedReader()
                            .use { it.readText() })
                }.getOrNull()
            }
    }
}
