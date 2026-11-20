package it.dhd.oxygencustomizer.xmorph.data.source

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import android.os.FileObserver
import android.os.Handler
import android.os.Looper
import it.dhd.oxygencustomizer.BuildConfig
import it.dhd.oxygencustomizer.xmorph.data.mapper.ClockFaceJsonMapper
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList

@SuppressLint("StaticFieldLeak")
object ClockFaces {

    const val STYLE_BASE = 2000
    private const val PRESET_DIR = "xmorph/clock_faces"
    private const val DEV_DIR = "XMorphClock/faces"
    private const val JSON = ".json"
    private const val RELOAD_DELAY = 150L

    @Volatile
    private var presets: List<ClockFace>? = null

    private val listeners = CopyOnWriteArrayList<() -> Unit>()
    private val mainHandler by lazy { Handler(Looper.getMainLooper()) }
    private var observer: FileObserver? = null

    val devFacesDir: File
        get() = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            DEV_DIR
        )

    @Volatile
    private var assetContext: Context? = null

    fun presets(context: Context): List<ClockFace> = presets ?: synchronized(this) {
        assetContext = context
        presets ?: load(context).also { presets = it }
    }

    fun reloadedFace(style: Int): ClockFace? = assetContext?.let { faceOf(it, style) }

    fun isFaceStyle(style: Int): Boolean = style >= STYLE_BASE

    fun styleOf(index: Int): Int = STYLE_BASE + index

    fun faceOf(context: Context, style: Int): ClockFace? =
        presets(context).getOrNull(style - STYLE_BASE)

    fun addChangeListener(listener: () -> Unit) {
        listeners += listener
        if (BuildConfig.DEBUG) watchDevFaces()
    }

    fun removeChangeListener(listener: () -> Unit) {
        listeners -= listener
    }

    private fun load(context: Context): List<ClockFace> {
        val assets = context.assets
        val faces = sortedMapOf<String, ClockFace>()

        assets.list(PRESET_DIR).orEmpty()
            .filter { it.endsWith(JSON) }
            .forEach { file ->
                readFace { assets.open("$PRESET_DIR/$file").bufferedReader().use { it.readText() } }
                    ?.let { faces[file] = it }
            }

        if (BuildConfig.DEBUG) {
            devFacesDir.listFiles { file -> file.name.endsWith(JSON) }.orEmpty().forEach { file ->
                readFace { file.readText() }?.let { faces[file.name] = it }
            }
        }

        return faces.values.toList()
    }

    private fun readFace(read: () -> String): ClockFace? = try {
        ClockFaceJsonMapper.fromJson(read())
    } catch (_: Exception) {
        null
    }

    @Synchronized
    private fun watchDevFaces() {
        if (observer != null) return
        val dir = devFacesDir
        if (!dir.isDirectory) dir.mkdirs()

        observer = object : FileObserver(
            dir,
            CLOSE_WRITE or MOVED_TO or MOVED_FROM or DELETE or CREATE
        ) {
            override fun onEvent(event: Int, path: String?) {
                if (path?.endsWith(JSON) != true) return
                mainHandler.removeCallbacks(reload)
                mainHandler.postDelayed(reload, RELOAD_DELAY)
            }
        }.also { it.startWatching() }
    }

    private val reload = Runnable {
        presets = null
        listeners.forEach { it() }
    }
}
