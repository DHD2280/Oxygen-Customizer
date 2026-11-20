package it.dhd.oxygencustomizer.xmorph.designer

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.LruCache
import it.dhd.oxygencustomizer.utils.Constants
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceImage
import java.io.File

object DesignerImages {

    private const val MAX_SIDE = 1440
    private const val CACHE_BYTES = 12 * 1024 * 1024
    private const val PREFIX = "img_"
    private const val EXTENSION = "png"

    val directory: File
        get() = File(Constants.XPOSED_RESOURCE_TEMP_DIR + "/lockscreen_clock/images")

    private val cache = object : LruCache<String, Bitmap>(CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap): Int = value.byteCount
    }

    fun fileOf(id: String): File? = File(directory, id).takeIf { id.isNotEmpty() && it.isFile }

    fun bitmap(id: String): Bitmap? {
        cache.get(id)?.let { return it }
        val file = fileOf(id) ?: return null
        val bitmap = BitmapFactory.decodeFile(file.path) ?: return null
        cache.put(id, bitmap)
        return bitmap
    }

    fun import(context: Context, uri: Uri): FaceImage? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            ?: return null
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIDE) sample *= 2
        val decoded = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(
                it,
                null,
                BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val scale = MAX_SIDE.toFloat() / maxOf(decoded.width, decoded.height)
        val bitmap = if (scale < 1f) Bitmap.createScaledBitmap(
            decoded,
            (decoded.width * scale).toInt().coerceAtLeast(1),
            (decoded.height * scale).toInt().coerceAtLeast(1),
            true
        ) else decoded
        if (!directory.isDirectory && !directory.mkdirs()) return null
        val target = uniqueFile("$PREFIX${System.currentTimeMillis()}")
        return try {
            target.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
            cache.put(target.name, bitmap)
            FaceImage(target.name, bitmap.width.toFloat() / bitmap.height.toFloat())
        } catch (_: Exception) {
            target.delete()
            null
        }
    }

    fun read(id: String): ByteArray? = fileOf(id)?.readBytes()

    fun write(id: String, bytes: ByteArray): String? {
        if (!directory.isDirectory && !directory.mkdirs()) return null
        val existing = fileOf(id)
        if (existing != null && existing.readBytes().contentEquals(bytes)) return id
        val target = if (existing == null) File(
            directory,
            safeName(id)
        ) else uniqueFile(safeName(id).substringBeforeLast('.'))
        return try {
            target.writeBytes(bytes)
            cache.remove(target.name)
            target.name
        } catch (_: Exception) {
            null
        }
    }

    private fun safeName(id: String): String = id.replace(Regex("[^A-Za-z0-9._-]"), "_")
        .ifEmpty { "$PREFIX${System.currentTimeMillis()}.$EXTENSION" }

    private fun uniqueFile(base: String): File {
        var target = File(directory, "$base.$EXTENSION")
        var index = 1
        while (target.exists()) target = File(directory, "${base}_${index++}.$EXTENSION")
        return target
    }
}
