package it.dhd.oxygencustomizer.xmorph.engine

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.graphics.fonts.Font
import android.graphics.fonts.FontFamily
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import it.dhd.oxygencustomizer.R
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode
import it.dhd.oxygencustomizer.xmorph.domain.model.UserFont
import it.dhd.oxygencustomizer.xmorph.engine.render.FontAxes
import it.dhd.oxygencustomizer.xmorph.engine.render.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.Executors

@SuppressLint("StaticFieldLeak")
object FaceFont {

    private const val WEIGHT_STEP = 10
    private const val WIDTH_STEP = 5
    private const val HEIGHT_STEP = 8
    private const val OPTICAL_SIZE = 144
    private const val SYSTEM_FALLBACK = "sans-serif"
    private const val AXES_SEPARATOR = "?"
    private const val AXIS_JOIN = "&"
    private const val AXIS_ASSIGN = "="
    private const val AXIS_TAG_LENGTH = 4
    private const val MAX_PREWARM = 400
    private const val WEIGHT_AXIS = "wght"
    private const val WIDTH_AXIS = "wdth"
    private const val RETRY_MS = 5_000L

    @Volatile
    private var fontContext: Context? = null

    private val flexFont: Font? by lazy {
        val context = fontContext ?: return@lazy null
        try {
            Font.Builder(context.resources, R.font.morph_roboto_flex).build()
        } catch (_: Exception) {
            null
        }
    }

    private val cache = HashMap<String, Typeface>()
    private val bundledBases = HashMap<String, Font>()
    private val weightAxes = HashMap<String, Boolean>()
    private val userBases = HashMap<String, Font>()
    private val userAxes = HashMap<String, Set<String>>()
    private val userMisses = HashMap<String, Long>()

    @Volatile
    private var userFonts: ((String) -> ParcelFileDescriptor?)? = null
    private val warmer = Executors.newSingleThreadExecutor()

    @Volatile
    var isWarm = false
        private set

    fun init(context: Context) {
        if (fontContext == null) fontContext = context
    }

    fun setUserFonts(loader: (String) -> ParcelFileDescriptor?) {
        userFonts = loader
    }

    fun warmUp() {
        warmer.execute {
            flexFont
            isWarm = true
            BundledFonts.ids.keys.forEach { hasWeightAxis(BundledFonts.SCHEME + it) }
        }
    }

    fun hasWidthAxis(font: String): Boolean {
        if (font.isEmpty()) return true
        UserFont.idOf(font)?.let { return WIDTH_AXIS in userAxesOf(it) }
        if (!font.startsWith(BundledFonts.SCHEME)) return false
        return font.removePrefix(BundledFonts.SCHEME)
            .substringBefore(AXES_SEPARATOR) in BundledFonts.widthAxis
    }

    fun hasHeightAxis(font: String): Boolean = font.isEmpty()

    fun supportsWeight(font: String): Boolean =
        !font.startsWith(BundledFonts.SCHEME) && !font.startsWith(UserFont.SCHEME) || hasWeightAxis(
            font
        )

    fun hasWeightAxis(font: String): Boolean {
        if (font.isEmpty()) return true
        UserFont.idOf(font)?.let { return WEIGHT_AXIS in userAxesOf(it) }
        if (!font.startsWith(BundledFonts.SCHEME)) return false
        val name = font.removePrefix(BundledFonts.SCHEME).substringBefore(AXES_SEPARATOR)
        synchronized(weightAxes) { weightAxes[name] }?.let { return it }
        val context = fontContext ?: return false
        val id = BundledFonts.ids[name] ?: return false
        val found = try {
            context.resources.openRawResource(id).use { FontAxes.hasWeight(it) }
        } catch (_: Exception) {
            false
        }
        return synchronized(weightAxes) { weightAxes.getOrPut(name) { found } }
    }

    fun typeface(font: String, weight: Int, width: Int, height: Int): Typeface {
        val wght = quantize(weight, WEIGHT_STEP)
        val wdth = quantize(width, WIDTH_STEP)
        val ytfi = quantize(height, HEIGHT_STEP)
        val key = "$font|$wght|$wdth|$ytfi"

        synchronized(cache) { cache[key] }?.let { return it }
        val typeface = when {
            font.isEmpty() -> flexTypeface(wght, wdth, ytfi)
            font.startsWith(UserFont.SCHEME) -> userTypeface(font, wght, wdth)
                ?: return flexTypeface(wght, wdth, ytfi)

            font.startsWith(BundledFonts.SCHEME) -> bundledTypeface(font, wght, wdth)
            else -> fileTypeface(font, wght)
        }
        return synchronized(cache) { cache.getOrPut(key) { typeface } }
    }

    fun prewarm(face: ClockFace) {
        val nodes = HashMap<String, MutableList<LayerNode>>()
        fun collect(node: FaceNode) {
            when (node) {
                is LayerNode -> nodes.getOrPut(node.id) { mutableListOf() } += node
                is RowNode -> node.children.forEach(::collect)
                is ColumnNode -> node.children.forEach(::collect)
                is StackNode -> node.children.forEach(::collect)
            }
        }
        listOfNotNull(face.large, face.small, face.aodLarge, face.aodSmall).forEach(::collect)
        val requests = face.layers.flatMap { layer ->
            val placed = nodes[layer.id].orEmpty()
            val weights = buildList {
                add(layer.weight)
                if (layer.sleepStyle.thin) add(layer.sleepWeight)
                placed.mapNotNullTo(this) { it.weight }
            }
            val widths =
                listOfNotNull(layer.width, layer.sleepWidth) + placed.mapNotNull { it.width }
            val heights =
                listOfNotNull(layer.height, layer.sleepHeight) + placed.mapNotNull { it.height }
            val weightSteps = steps(weights, WEIGHT_STEP)
            val widthSteps = steps(widths, WIDTH_STEP)
            val heightSteps = steps(heights, HEIGHT_STEP)
            weightSteps.flatMap { weight ->
                widthSteps.flatMap { width ->
                    heightSteps.map { height ->
                        Request(
                            layer.font,
                            weight,
                            width,
                            height
                        )
                    }
                }
            }.take(MAX_PREWARM)
        }.distinct()
        warmer.execute {
            requests.forEach { typeface(it.font, it.weight, it.width, it.height) }
        }
    }

    private fun steps(values: List<Int>, step: Int): List<Int> {
        val low = quantize(values.min(), step)
        val high = quantize(values.max(), step)
        return (low..high step step).toList()
    }

    private fun quantize(value: Int, step: Int): Int = (value + step / 2) / step * step

    private fun flexTypeface(weight: Int, width: Int, height: Int): Typeface {
        val font = flexFont ?: return Typeface.create(Typeface.DEFAULT, weight, false)
        return try {
            val variant = Font.Builder(font)
                .setFontVariationSettings(
                    "'wght' $weight, 'wdth' $width, 'YTFI' $height, 'opsz' $OPTICAL_SIZE"
                )
                .build()
            Typeface.CustomFallbackBuilder(FontFamily.Builder(variant).build())
                .setSystemFallback(SYSTEM_FALLBACK)
                .build()
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
    }

    private fun bundledTypeface(font: String, weight: Int, width: Int): Typeface {
        val spec = font.removePrefix(BundledFonts.SCHEME)
        val name = spec.substringBefore(AXES_SEPARATOR)
        val context = fontContext ?: return Typeface.create(Typeface.DEFAULT, weight, false)
        val id = BundledFonts.ids[name] ?: return Typeface.create(Typeface.DEFAULT, weight, false)
        val extras = spec.substringAfter(AXES_SEPARATOR, "")
            .split(AXIS_JOIN)
            .mapNotNull { pair ->
                val tag = pair.substringBefore(AXIS_ASSIGN).trim()
                val value = pair.substringAfter(AXIS_ASSIGN, "").toFloatOrNull()
                if (tag.length == AXIS_TAG_LENGTH && value != null) "'$tag' $value" else null
            }
        val settings = buildList {
            add("'wght' $weight")
            if (name in BundledFonts.widthAxis) add("'wdth' $width")
            addAll(extras)
        }.joinToString(", ")
        return try {
            val base = synchronized(bundledBases) {
                bundledBases.getOrPut(name) { Font.Builder(context.resources, id).build() }
            }
            val variant = try {
                Font.Builder(base).setFontVariationSettings(settings).build()
            } catch (_: Exception) {
                base
            }
            Typeface.CustomFallbackBuilder(FontFamily.Builder(variant).build())
                .setSystemFallback(SYSTEM_FALLBACK)
                .build()
        } catch (_: Exception) {
            Typeface.create(Typeface.DEFAULT, weight, false)
        }
    }

    private fun userTypeface(font: String, weight: Int, width: Int): Typeface? {
        val id = UserFont.idOf(font) ?: return null
        val base = synchronized(userBases) { userBases[id] } ?: loadUser(id)?.also { loaded ->
            synchronized(userBases) { userBases.getOrPut(id) { loaded } }
        } ?: return null
        val axes = userAxesOf(id)
        val settings = buildList {
            if (WEIGHT_AXIS in axes) add("'$WEIGHT_AXIS' $weight")
            if (WIDTH_AXIS in axes) add("'$WIDTH_AXIS' $width")
        }.joinToString(", ")
        return try {
            val variant = if (settings.isEmpty()) base else Font.Builder(base)
                .setFontVariationSettings(settings).build()
            val typeface = Typeface.CustomFallbackBuilder(FontFamily.Builder(variant).build())
                .setSystemFallback(SYSTEM_FALLBACK)
                .build()
            if (WEIGHT_AXIS in axes) typeface else Typeface.create(typeface, weight, false)
        } catch (_: Exception) {
            null
        }
    }

    private fun loadUser(id: String): Font? = openUser(id)?.let { descriptor ->
        try {
            descriptor.use { Font.Builder(it).build() }
        } catch (_: IOException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private fun userAxesOf(id: String): Set<String> {
        synchronized(userAxes) { userAxes[id] }?.let { return it }
        val descriptor = openUser(id) ?: return emptySet()
        val axes = ParcelFileDescriptor.AutoCloseInputStream(descriptor).use { FontAxes.axes(it) }
        return synchronized(userAxes) { userAxes.getOrPut(id) { axes } }
    }

    private fun openUser(id: String): ParcelFileDescriptor? {
        val now = SystemClock.uptimeMillis()
        synchronized(userMisses) { userMisses[id] }?.let { if (now - it < RETRY_MS) return null }
        val descriptor = try {
            userFonts?.invoke(id)
        } catch (_: Exception) {
            null
        }
        synchronized(userMisses) {
            if (descriptor == null) userMisses[id] = now else userMisses.remove(id)
        }
        return descriptor
    }

    private fun fileTypeface(font: String, weight: Int): Typeface {
        val base = try {
            val file = File(font)
            if (file.isFile) Typeface.createFromFile(file) else Typeface.create(
                font,
                Typeface.NORMAL
            )
        } catch (_: Exception) {
            Typeface.DEFAULT
        }
        return Typeface.create(base, weight, false)
    }
}
