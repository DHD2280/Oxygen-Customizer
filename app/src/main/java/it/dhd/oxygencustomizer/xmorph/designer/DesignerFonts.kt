package it.dhd.oxygencustomizer.xmorph.designer

import android.content.Context
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import it.dhd.oxygencustomizer.utils.Constants
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.UserFont
import it.dhd.oxygencustomizer.xmorph.engine.FaceFont
import java.io.File
import java.util.Locale

object DesignerFonts {

    val directory: File
        get() = File(Constants.XPOSED_RESOURCE_TEMP_DIR + "/lockscreen_clock/fonts")

    private val EXTENSIONS = setOf("ttf", "otf")

    @JvmStatic
    fun install() {
        FaceFont.setUserFonts { id ->
            fileOf(id)?.let { ParcelFileDescriptor.open(it, ParcelFileDescriptor.MODE_READ_ONLY) }
        }
    }

    fun list(): List<File> =
        directory.listFiles()
            ?.filter { it.isFile && it.extension.lowercase(Locale.ROOT) in EXTENSIONS }
            ?.sortedBy { it.name.lowercase(Locale.ROOT) }.orEmpty()

    fun familyOf(file: File): String = UserFont.SCHEME + file.name

    fun fileOf(id: String): File? = File(directory, id).takeIf { it.isFile }

    fun exists(font: String): Boolean = UserFont.idOf(font)?.let { fileOf(it) != null } ?: true

    fun import(context: Context, uri: Uri): File? {
        val resolver = context.contentResolver
        val display = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: "font.ttf"
        val extension = display.substringAfterLast('.', "ttf").lowercase(Locale.ROOT)
            .takeIf { it in EXTENSIONS } ?: "ttf"
        val base = display.substringBeforeLast('.').replace(Regex("[^A-Za-z0-9_-]"), "_")
            .ifEmpty { "font" }
        if (!directory.isDirectory && !directory.mkdirs()) return null
        var target = File(directory, "$base.$extension")
        var index = 1
        while (target.exists()) target = File(directory, "${base}_${index++}.$extension")
        return try {
            resolver.openInputStream(uri)
                ?.use { input -> target.outputStream().use { input.copyTo(it) } } ?: return null
            target
        } catch (_: Exception) {
            target.delete()
            null
        }
    }

    fun write(id: String, bytes: ByteArray): String? {
        if (!directory.isDirectory && !directory.mkdirs()) return null
        val existing = fileOf(id)
        if (existing != null && existing.readBytes().contentEquals(bytes)) return id
        val extension =
            id.substringAfterLast('.', "ttf").lowercase(Locale.ROOT).takeIf { it in EXTENSIONS }
                ?: "ttf"
        val base =
            id.substringBeforeLast('.').replace(Regex("[^A-Za-z0-9_-]"), "_").ifEmpty { "font" }
        var target = File(directory, "$base.$extension")
        var index = 1
        while (target.exists()) target = File(directory, "${base}_${index++}.$extension")
        return try {
            target.writeBytes(bytes)
            target.name
        } catch (_: Exception) {
            null
        }
    }

    @JvmStatic
    fun sanitize(face: ClockFace): ClockFace {
        if (face.layers.all { exists(it.font) }) return face
        return face.copy(layers = face.layers.map { if (exists(it.font)) it else it.copy(font = "") })
    }
}
