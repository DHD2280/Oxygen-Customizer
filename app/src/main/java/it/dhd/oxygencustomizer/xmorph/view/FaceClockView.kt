package it.dhd.oxygencustomizer.xmorph.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.format.DateFormat
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import it.dhd.oxygencustomizer.xmorph.designer.DesignerImages
import it.dhd.oxygencustomizer.xmorph.domain.editor.FaceComposer
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ColumnNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceData
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceNode
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceTextEffect
import it.dhd.oxygencustomizer.xmorph.domain.model.LayerNode
import it.dhd.oxygencustomizer.xmorph.domain.model.RowNode
import it.dhd.oxygencustomizer.xmorph.domain.model.StackNode
import it.dhd.oxygencustomizer.xmorph.engine.FaceColors
import it.dhd.oxygencustomizer.xmorph.engine.FaceFont
import it.dhd.oxygencustomizer.xmorph.engine.render.Box
import it.dhd.oxygencustomizer.xmorph.engine.render.DataPhrases
import it.dhd.oxygencustomizer.xmorph.engine.render.LayerMetrics
import it.dhd.oxygencustomizer.xmorph.engine.render.Leaf
import it.dhd.oxygencustomizer.xmorph.engine.render.Placement
import it.dhd.oxygencustomizer.xmorph.engine.render.TextTokens
import it.dhd.oxygencustomizer.xmorph.engine.render.WordPhrases
import it.dhd.oxygencustomizer.xmorph.engine.render.withRomanNumerals
import it.dhd.oxygencustomizer.xposed.utils.GlassTextEffect
import it.dhd.oxygencustomizer.xposed.utils.LiquidGlass
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

class FaceClockView(context: Context, face: ClockFace) : FrameLayout(context) {

    var face: ClockFace = face
        set(value) {
            if (field == value) return
            val structure =
                field.layers.map { it.id to it.type } != value.layers.map { it.id to it.type }
            val subjectChanged = field.aboveSubject != value.aboveSubject
            field = value
            if (structure) createViews() else updateHosts()
            if (subjectChanged && !structure) updateSubjectView()
            refresh()
        }

    interface HostProvider {
        fun create(layer: FaceLayer): View?
        fun update(layer: FaceLayer, view: View)
        fun release(view: View)
        fun aod(view: View, progress: Float)
    }

    var hostProvider: HostProvider? = null
        set(value) {
            if (field === value) return
            releaseHosts()
            field = value
            createViews()
            refresh()
        }

    var depthSubject: DepthSubject? = null
        set(value) {
            if (field === value) return
            subjectView?.let { field?.detach(it) }
            field = value
            subjectView?.let { removeView(it) }
            subjectView = null
            updateSubjectView()
        }

    private var subjectView: SubjectLayer? = null
    private var drawOrder: IntArray = IntArray(0)

    var aodColor: Int = Color.WHITE

    var largeProgress = 1f
        private set
    var aodProgress = 0f
        private set

    private var hostAodProgress = 0f

    private val views = LinkedHashMap<String, View>()
    private val hosts = HashMap<String, View>()
    private val hostSizes = HashMap<String, Pair<Int, Int>>()
    private val wrapHosts = HashSet<String>()
    private var hostWidth = 0f
    private val texts = HashMap<String, String>()
    private val metrics = HashMap<MetricsKey, LayerMetrics>()
    private var framesWidth = -1f
    private var smallFrames: Map<String, Placement> = emptyMap()
    private var largeFrames: Map<String, Placement> = emptyMap()
    private var aodSmallFrames: Map<String, Placement>? = null
    private var aodLargeFrames: Map<String, Placement>? = null
    private val measurePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = BASE_SIZE }
    private val bounds = Rect()
    private val phrases by lazy { WordPhrases.from(context.resources) }
    private val dataPhrases by lazy { DataPhrases.from(context.resources) }
    private val glassSupported by lazy { GlassTextEffect.isSupported() }
    private var receiverRegistered = false

    private val timeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            post { updateTexts() }
        }
    }

    init {
        FaceFont.init(context)
        clipChildren = false
        clipToPadding = false
        createViews()
        updateTexts()
    }

    fun editFace(value: ClockFace) {
        face = value
    }

    fun setLargeProgress(progress: Float) {
        val value = progress.coerceIn(0f, 1f)
        if (value == largeProgress) return
        largeProgress = value
        requestLayout()
        applyPlacements()
    }

    fun setAodProgress(progress: Float, hostProgress: Float = progress) {
        val value = progress.coerceIn(0f, 1f)
        val hosts = hostProgress.coerceIn(0f, 1f)
        if (value == aodProgress && hosts == hostAodProgress) return
        val morph = value != aodProgress
        aodProgress = value
        hostAodProgress = hosts
        if (morph && (face.aodLarge != null || face.aodSmall != null)) requestLayout()
        applyPlacements()
    }

    private fun refresh() {
        metrics.clear()
        framesWidth = -1f
        texts.clear()
        updateTexts()
        requestLayout()
    }

    private fun createViews() {
        releaseHosts()
        removeAllViews()
        views.clear()
        face.layers.filter(::isSupported).sortedBy { if (it.type == FaceLayerType.CARD) 0 else 1 }
            .forEach { layer ->
                if (layer.isHost) {
                    createHost(layer)
                    return@forEach
                }
                val view =
                    if (layer.isHand) HandView(context) else if (layer.type == FaceLayerType.CARD) View(
                        context
                    ) else if (layer.type == FaceLayerType.IMAGE) ImageView(context).apply {
                        scaleType = ImageView.ScaleType.CENTER_CROP
                        clipToOutline = true
                    } else TextView(context).apply {
                        includeFontPadding = false
                        isSingleLine = true
                        ellipsize = null
                        setTextSize(TypedValue.COMPLEX_UNIT_PX, BASE_SIZE)
                        gravity =
                            if (layer.isTime) Gravity.CENTER_HORIZONTAL or Gravity.TOP else Gravity.START or Gravity.TOP
                    }
                view.pivotX = 0f
                view.pivotY = 0f
                views[layer.id] = view
                addView(view, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            }
        subjectView = null
        updateSubjectView()
    }

    private fun updateSubjectView() {
        val source = depthSubject
        val wanted = source != null && (source.drawsWhenAllBehind || face.aboveSubject.isNotEmpty())
        val current = subjectView
        if (wanted && current == null) {
            val created = SubjectLayer()
            subjectView = created
            addView(created, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        } else if (!wanted && current != null) {
            removeView(current)
            subjectView = null
        }
        updateDrawOrder()
    }

    private fun updateDrawOrder() {
        val subject = subjectView
        isChildrenDrawingOrderEnabled = subject != null
        if (subject == null) {
            drawOrder = IntArray(0)
            invalidate()
            return
        }
        val groups = HashMap<View, String>()
        views.forEach { (id, view) -> groups[view] = FaceComposer.groupOf(id) }
        val behind = ArrayList<Int>()
        val front = ArrayList<Int>()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            if (child === subject) continue
            if (groups[child] in face.aboveSubject) front += index else behind += index
        }
        drawOrder = (behind + indexOfChild(subject) + front).toIntArray()
        invalidate()
    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int =
        if (drawOrder.size == childCount) drawOrder[drawingPosition] else drawingPosition

    fun invalidateSubject() {
        subjectView?.invalidate()
    }

    private inner class SubjectLayer : View(context) {
        private val location = IntArray(2)
        private val lastLocation = IntArray(2)
        private val preDraw = ViewTreeObserver.OnPreDrawListener {
            getLocationInWindow(location)
            if (!location.contentEquals(lastLocation)) {
                location.copyInto(lastLocation)
                invalidate()
            }
            true
        }

        override fun onAttachedToWindow() {
            super.onAttachedToWindow()
            viewTreeObserver.addOnPreDrawListener(preDraw)
            depthSubject?.attach(this)
        }

        override fun onDetachedFromWindow() {
            depthSubject?.detach(this)
            viewTreeObserver.removeOnPreDrawListener(preDraw)
            super.onDetachedFromWindow()
        }

        override fun onDraw(canvas: Canvas) {
            depthSubject?.draw(canvas, this)
        }
    }

    private fun isSupported(layer: FaceLayer): Boolean =
        layer.isTime || layer.isHand || layer.type == FaceLayerType.DATE || layer.type == FaceLayerType.TEXT || layer.type == FaceLayerType.CARD || layer.type == FaceLayerType.IMAGE ||
                (layer.isHost && hostProvider != null)

    private fun createHost(layer: FaceLayer) {
        val hosted = hostProvider?.create(layer) ?: return
        (hosted.parent as? ViewGroup)?.removeView(hosted)
        val wrap = hosted.layoutParams?.width == LayoutParams.WRAP_CONTENT
        val width = if (wrap) LayoutParams.WRAP_CONTENT else LayoutParams.MATCH_PARENT
        if (wrap) wrapHosts += layer.id
        val frame = FrameLayout(context).apply {
            clipChildren = false
            clipToPadding = false
            addView(hosted, LayoutParams(width, LayoutParams.WRAP_CONTENT))
        }
        hosts[layer.id] = hosted
        views[layer.id] = frame
        addView(frame, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    private fun updateHosts() {
        val provider = hostProvider ?: return
        face.layers.forEach { layer ->
            val hosted = hosts[layer.id] ?: return@forEach
            provider.update(layer, hosted)
        }
    }

    private fun releaseHosts() {
        val provider = hostProvider
        hosts.values.forEach { hosted ->
            (hosted.parent as? ViewGroup)?.removeView(hosted)
            provider?.release(hosted)
        }
        hosts.clear()
        hostSizes.clear()
        wrapHosts.clear()
    }

    private fun measureHosts() {
        if (hosts.isEmpty()) return
        val reference = resources.displayMetrics.widthPixels.toFloat().coerceAtLeast(1f)
        val wrapSpec = MeasureSpec.makeMeasureSpec(reference.roundToInt(), MeasureSpec.AT_MOST)
        val fillSpec = MeasureSpec.makeMeasureSpec(reference.roundToInt(), MeasureSpec.EXACTLY)
        val heightSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        var changed = reference != hostWidth
        hostWidth = reference
        hosts.keys.forEach { id ->
            val frame = views[id] ?: return@forEach
            frame.measure(if (id in wrapHosts) wrapSpec else fillSpec, heightSpec)
            val size = frame.measuredWidth to frame.measuredHeight
            if (hostSizes[id] != size) {
                hostSizes[id] = size
                changed = true
            }
        }
        if (changed) {
            metrics.keys.removeAll { it.id in hosts }
            framesWidth = -1f
        }
    }

    private fun String.cased(caps: Boolean): String =
        if (caps) uppercase(Locale.getDefault()) else this

    private fun updateTexts() {
        val calendar = Calendar.getInstance()
        val is24Hour = DateFormat.is24HourFormat(context)
        val hour =
            if (is24Hour) calendar.get(Calendar.HOUR_OF_DAY) else (calendar.get(Calendar.HOUR) + 11) % 12 + 1
        val hours = "%02d".format(hour)
        val bareHours = hour.toString()
        val minutes = "%02d".format(calendar.get(Calendar.MINUTE))
        val hourAngle =
            ((calendar.get(Calendar.HOUR_OF_DAY) % HOURS_ON_DIAL) + calendar.get(Calendar.MINUTE) / MINUTES_PER_HOUR) * DEGREES_PER_HOUR
        val minuteAngle = calendar.get(Calendar.MINUTE) * DEGREES_PER_MINUTE
        face.layers.forEach { layer ->
            val hand = views[layer.id] as? HandView ?: return@forEach
            hand.angle = if (layer.type == FaceLayerType.HAND_HOUR) hourAngle else minuteAngle
        }
        var changed = false
        face.layers.forEach { layer ->
            if (!isSupported(layer)) return@forEach
            val text = when (layer.type) {
                FaceLayerType.HOURS -> if (layer.hourZero) hours else bareHours
                FaceLayerType.MINUTES -> minutes
                FaceLayerType.COLON -> ":"
                FaceLayerType.TIME -> (if (layer.hourZero) hours else bareHours) + ":" + minutes
                FaceLayerType.HOUR_TENS -> if (layer.hourZero || hour >= 10) hours.substring(
                    0,
                    1
                ) else ""

                FaceLayerType.HOUR_ONES -> hours.substring(1, 2)
                FaceLayerType.MINUTE_TENS -> minutes.substring(0, 1)
                FaceLayerType.MINUTE_ONES -> minutes.substring(1, 2)
                FaceLayerType.DATE -> DateFormat.format(
                    layer.format.ifEmpty { DATE_FORMAT },
                    calendar
                )
                    .toString().cased(layer.caps)
                    .let { if (layer.roman) it.withRomanNumerals() else it }

                FaceLayerType.TEXT -> TextTokens.expand(
                    layer.format,
                    calendar,
                    FaceData(),
                    phrases,
                    dataPhrases
                ).cased(layer.caps)

                else -> ""
            }
            val old = texts[layer.id]
            if (old == text) return@forEach
            texts[layer.id] = text
            (views[layer.id] as? TextView)?.text = text
            if (old == null || old.length != text.length || !layer.isTime) {
                metrics.keys.removeAll { it.id == layer.id }
                changed = true
            }
        }
        if (changed) {
            framesWidth = -1f
            requestLayout()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (!receiverRegistered) {
            ContextCompat.registerReceiver(
                context,
                timeReceiver,
                IntentFilter().apply {
                    addAction(Intent.ACTION_TIME_TICK)
                    addAction(Intent.ACTION_TIME_CHANGED)
                    addAction(Intent.ACTION_TIMEZONE_CHANGED)
                },
                ContextCompat.RECEIVER_EXPORTED
            )
            receiverRegistered = true
        }
        updateTexts()
    }

    override fun onDetachedFromWindow() {
        if (receiverRegistered) {
            try {
                context.unregisterReceiver(timeReceiver)
            } catch (_: IllegalArgumentException) {
            }
            receiverRegistered = false
        }
        super.onDetachedFromWindow()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        measureHosts()
        frames(width)
        var bottom = lerp(
            max(bottomOf(smallFrames), boxBottom(face.small, width)),
            max(bottomOf(largeFrames), boxBottom(face.large, width)),
            largeProgress
        )
        if (aodSmallFrames != null || aodLargeFrames != null) {
            bottom = lerp(
                bottom,
                lerp(
                    bottomOf(aodSmallFrames ?: smallFrames),
                    bottomOf(aodLargeFrames ?: largeFrames),
                    largeProgress
                ),
                aodProgress
            )
        }
        views.forEach { (id, view) -> if (id !in hosts) measureChildView(view) }
        setMeasuredDimension(width.toInt(), resolveSize(ceil(bottom).toInt(), heightMeasureSpec))
        subjectView?.measure(
            MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        views.values.forEach { it.layout(0, 0, it.measuredWidth, it.measuredHeight) }
        subjectView?.layout(0, 0, measuredWidth, measuredHeight)
        applyPlacements()
    }

    private fun measureChildView(view: View) {
        val unspecified = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        view.measure(unspecified, unspecified)
    }

    private fun applyPlacements() {
        val width = width.toFloat()
        if (width <= 0f) return
        frames(width)
        face.layers.forEach { layer ->
            val view = views[layer.id] ?: return@forEach
            val placement = placement(layer.id)
            if (placement == null || placement.alpha <= 0f) {
                view.visibility = INVISIBLE
                return@forEach
            }
            view.visibility = VISIBLE
            val awakeColor = stateOf(layer.id, smallFrames, largeFrames)?.color ?: placement.color
            if (layer.isHost) {
                applyHost(view, layer, placement)
                return@forEach
            }
            when (view) {
                is TextView -> applyText(view, layer, placement, awakeColor)
                is HandView -> applyHand(view, layer, placement, awakeColor)
                is ImageView -> applyImage(view, layer, placement)
                else -> applyCard(view, layer, placement, awakeColor)
            }
        }
    }

    private fun applyText(view: TextView, layer: FaceLayer, placement: Placement, awakeColor: Int) {
        val awake = 1f - aodProgress
        val weight = if (layer.sleepStyle.thin) lerp(
            layer.sleepWeight.toFloat(),
            placement.weight,
            awake
        ) else placement.weight
        val axisWidth = layer.sleepWidth?.let { lerp(it.toFloat(), placement.widthAxis, awake) }
            ?: placement.widthAxis
        val axisHeight = layer.sleepHeight?.let { lerp(it.toFloat(), placement.heightAxis, awake) }
            ?: placement.heightAxis
        val typeface = FaceFont.typeface(
            layer.font,
            weight.roundToInt(),
            axisWidth.roundToInt(),
            axisHeight.roundToInt()
        )
        val size = placement.size.coerceAtLeast(1f)
        var relayout = false
        if (view.typeface !== typeface) {
            view.typeface = typeface
            relayout = true
        }
        if (abs(view.textSize - size) > SIZE_EPSILON) {
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, size)
            relayout = true
        }
        if (view.letterSpacing != placement.tracking) {
            view.letterSpacing = placement.tracking
            relayout = true
        }
        val boxWidth = placement.advances.sum() * size
        if (layer.isTime) {
            val fixed = ceil(boxWidth).toInt()
            if (view.width != fixed) {
                view.measure(
                    MeasureSpec.makeMeasureSpec(fixed, MeasureSpec.EXACTLY),
                    MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
                view.layout(0, 0, view.measuredWidth, view.measuredHeight)
                relayout = false
            }
        }
        if (relayout) {
            if (layer.isTime) {
                view.measure(
                    MeasureSpec.makeMeasureSpec(
                        ceil(boxWidth).toInt(),
                        MeasureSpec.EXACTLY
                    ), MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                )
            } else {
                measureChildView(view)
            }
            view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        }

        val glass =
            placement.effect == FaceTextEffect.GLASS && glassSupported && aodProgress < GLASS_AOD_CUTOFF
        val sleepColor = layer.sleepColor?.let { FaceColors.resolve(context, it) } ?: aodColor
        val color = when {
            glass -> if (layer.isTime) GLASS_DIGIT else GLASS_EXTRA
            placement.effect == FaceTextEffect.GLASS -> ColorUtils.blendARGB(
                ColorUtils.setAlphaComponent(
                    awakeColor,
                    GLASS_FALLBACK_ALPHA
                ), sleepColor, aodProgress
            )

            else -> ColorUtils.blendARGB(awakeColor, sleepColor, aodProgress)
        }
        if (view.currentTextColor != color) view.setTextColor(color)
        val outline = layer.sleepStyle.outline && aodProgress >= HALF
        val style = if (outline) Paint.Style.STROKE else Paint.Style.FILL
        if (view.paint.style != style) {
            view.paint.style = style
            view.paint.strokeWidth = size * OUTLINE_RATIO
            view.invalidate()
        }
        if (glass) GlassTextEffect.applyToView(view) else GlassTextEffect.clear(view)

        val stretchX = lerp(layer.sleepStretchX ?: placement.stretchX, placement.stretchX, awake)
        val stretch = lerp(layer.sleepStretch ?: placement.stretch, placement.stretch, awake)
        val sleepScale = layer.sleepScale?.let { lerp(it, 1f, awake) } ?: 1f
        val baseline =
            placement.top + size * placement.stretch * (placement.capRatio * (placement.lineHeight + 1f) / 2f - placement.drop)
        val localBaseline = view.baseline.toFloat()
        val px = boxWidth / 2f
        val py = localBaseline - placement.capRatio * size / 2f
        val sx = stretchX * sleepScale
        val sy = stretch * sleepScale
        view.pivotX = px
        view.pivotY = py
        view.scaleX = sx
        view.scaleY = sy
        view.translationX = placement.left - px * (1f - sx)
        view.translationY = baseline - py - (localBaseline - py) * sy
        view.rotation =
            layer.sleepRotation?.let { lerp(it, placement.rotation, awake) } ?: placement.rotation
        view.rotationX = placement.tiltX
        view.rotationY = placement.tiltY
        view.cameraDistance = CAMERA_DISTANCE * resources.displayMetrics.density
        val awakeAlpha = placement.alpha * layer.opacity
        val alpha = layer.sleepOpacity?.let { lerp(min(it, placement.alpha), awakeAlpha, awake) }
            ?: awakeAlpha
        view.alpha = alpha * if (layer.sleepStyle.hide) awake else 1f
    }

    private fun applyHand(view: HandView, layer: FaceLayer, placement: Placement, awakeColor: Int) {
        val side = placement.size.roundToInt().coerceAtLeast(1)
        if (view.width != side || view.height != side) {
            view.measure(
                MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(side, MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, side, side)
        }
        val awake = 1f - aodProgress
        val weight = if (layer.sleepStyle.thin) lerp(
            layer.sleepWeight.toFloat(),
            placement.weight,
            awake
        ) else placement.weight
        val sleepColor = layer.sleepColor?.let { FaceColors.resolve(context, it) } ?: aodColor
        view.color = ColorUtils.blendARGB(awakeColor, sleepColor, aodProgress)
        view.length = placement.size * layer.handLength
        view.thickness = placement.size * HAND_THICKNESS * weight / MAX_WEIGHT
        view.pivotX = side / 2f
        view.pivotY = side / 2f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationX = placement.left
        view.translationY = placement.top
        view.rotation = placement.rotation
        val awakeAlpha = placement.alpha * layer.opacity
        val alpha = layer.sleepOpacity?.let { lerp(min(it, placement.alpha), awakeAlpha, awake) }
            ?: awakeAlpha
        view.alpha = alpha * if (layer.sleepStyle.hide) awake else 1f
    }

    private fun applyCard(view: View, layer: FaceLayer, placement: Placement, awakeColor: Int) {
        val w = (placement.advances.sum() * placement.size).roundToInt().coerceAtLeast(1)
        val h = (placement.capRatio * placement.size).roundToInt().coerceAtLeast(1)
        if (view.width != w || view.height != h) {
            view.measure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, w, h)
        }
        val radius = min(w, h) * layer.radius.coerceIn(0f, 0.5f)
        val sleepColor = layer.sleepColor?.let { FaceColors.resolve(context, it) } ?: awakeColor
        val color = ColorUtils.blendARGB(awakeColor, sleepColor, aodProgress)
        val glass = placement.effect == FaceTextEffect.GLASS && aodProgress < GLASS_AOD_CUTOFF
        val tag = Triple(glass, color, radius)
        if (view.tag != tag) {
            view.tag = tag
            if (glass) {
                LiquidGlass.apply(
                    view,
                    radius,
                    ColorUtils.setAlphaComponent(
                        color,
                        (Color.alpha(LiquidGlass.DEFAULT_COLOR) * Color.alpha(color) / OPAQUE).roundToInt()
                    )
                )
            } else {
                view.background = GradientDrawable().apply {
                    cornerRadius = radius
                    setColor(color)
                }
            }
        }
        view.pivotX = w / 2f
        view.pivotY = h / 2f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationX = placement.left
        view.translationY = placement.top
        view.rotation = placement.rotation
        view.alpha =
            placement.alpha * layer.opacity * if (layer.sleepStyle.hide) 1f - aodProgress else 1f
    }

    private fun applyImage(view: ImageView, layer: FaceLayer, placement: Placement) {
        val w = (placement.advances.sum() * placement.size).roundToInt().coerceAtLeast(1)
        val h = (placement.capRatio * placement.size).roundToInt().coerceAtLeast(1)
        if (view.width != w || view.height != h) {
            view.measure(
                MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY)
            )
            view.layout(0, 0, w, h)
        }
        val radius = min(w, h) * layer.radius.coerceIn(0f, 0.5f)
        val tag = layer.format to radius
        if (view.tag != tag) {
            view.tag = tag
            view.setImageBitmap(DesignerImages.bitmap(layer.format))
            view.outlineProvider = object : ViewOutlineProvider() {
                override fun getOutline(target: View, outline: Outline) {
                    outline.setRoundRect(0, 0, target.width, target.height, radius)
                }
            }
            view.invalidateOutline()
        }
        view.pivotX = w / 2f
        view.pivotY = h / 2f
        view.scaleX = 1f
        view.scaleY = 1f
        view.translationX = placement.left
        view.translationY = placement.top
        view.rotation = placement.rotation
        view.alpha =
            placement.alpha * layer.opacity * if (layer.sleepStyle.hide) 1f - aodProgress else 1f
    }

    private fun applyHost(view: View, layer: FaceLayer, placement: Placement) {
        hosts[layer.id]?.let { hostProvider?.aod(it, aodProgress) }
        val enter = fade(hostAodProgress)
        val scale = placement.size / hostWidth.coerceAtLeast(1f) * lerp(1f, HOST_WAKE_SCALE, enter)
        view.pivotX = view.measuredWidth / 2f
        view.pivotY = view.measuredHeight / 2f
        view.scaleX = scale
        view.scaleY = scale
        view.translationX = placement.left - view.pivotX * (1f - scale)
        view.translationY = placement.top - view.pivotY * (1f - scale)
        view.rotation = placement.rotation
        val alpha = placement.alpha * layer.opacity * (1f - enter)
        view.alpha = alpha
        view.visibility = if (alpha <= HOST_MIN_ALPHA) INVISIBLE else VISIBLE
    }

    fun layerRects(includeHidden: Boolean = false): Map<String, RectF> {
        val width = width.toFloat()
        if (width <= 0f) return emptyMap()
        frames(width)
        val shown = if (includeHidden) editedStates().first else emptyMap()
        return face.layers.mapNotNull { layer ->
            if (layer.id !in views) return@mapNotNull null
            val placement = placement(layer.id) ?: return@mapNotNull null
            if (placement.alpha <= 0f && (!includeHidden || layer.id !in shown)) return@mapNotNull null
            layer.id to boundsOf(placement)
        }.toMap()
    }

    fun missingLayerRects(): Map<String, RectF> {
        val width = width.toFloat()
        if (width <= 0f) return emptyMap()
        frames(width)
        val (shown, other) = editedStates()
        val rects = layerRects(includeHidden = true)
        val missing = face.layers.filter { it.id in views && it.id !in shown && it.id in other }
        if (missing.isEmpty()) return emptyMap()
        return missing.groupBy { groupOf(it.id) }.flatMap { (group, layers) ->
            val common = rects.keys.filter { groupOf(it) == group && it in other }
            val target = unionOf(common.mapNotNull { rects[it] }) ?: return@flatMap emptyList()
            val source = unionOf(common.mapNotNull { other[it]?.let(::boundsOf) })
                ?: return@flatMap emptyList()
            val scale = if (source.width() > 0f) target.width() / source.width() else 1f
            layers.map { layer ->
                val rect = boundsOf(other.getValue(layer.id))
                layer.id to RectF(
                    target.left + (rect.left - source.left) * scale,
                    target.top + (rect.top - source.top) * scale,
                    target.left + (rect.right - source.left) * scale,
                    target.top + (rect.bottom - source.top) * scale
                )
            }
        }.toMap()
    }

    private fun editedStates(): Pair<Map<String, Placement>, Map<String, Placement>> {
        val showsLarge = largeProgress >= HALF
        val shownLarge = if (aodProgress < HALF) largeFrames else aodLargeFrames ?: largeFrames
        val shownSmall = if (aodProgress < HALF) smallFrames else aodSmallFrames ?: smallFrames
        return if (showsLarge) shownLarge to shownSmall else shownSmall to shownLarge
    }

    private fun boundsOf(placement: Placement): RectF = RectF(
        placement.left,
        placement.top,
        placement.left + placement.advances.sum() * placement.stretchX * placement.size,
        placement.top + placement.size * placement.capRatio * placement.stretch * placement.lineHeight
    )

    private fun unionOf(boxes: List<RectF>): RectF? {
        if (boxes.isEmpty()) return null
        return RectF(
            boxes.minOf { it.left },
            boxes.minOf { it.top },
            boxes.maxOf { it.right },
            boxes.maxOf { it.bottom })
    }

    private fun groupOf(id: String): String = FaceComposer.itemOf(id)?.toString() ?: CLOCK_GROUP

    private fun placement(id: String): Placement? {
        val awake = stateOf(id, smallFrames, largeFrames) ?: return null
        val aodSmall = aodSmallFrames
        val aodLarge = aodLargeFrames
        if ((aodSmall == null && aodLarge == null) || aodProgress <= 0f) return awake
        val asleep = stateOf(id, aodSmall ?: smallFrames, aodLarge ?: largeFrames) ?: awake.hidden()
        return blend(awake, asleep, aodProgress)
    }

    private fun stateOf(
        id: String,
        small: Map<String, Placement>,
        large: Map<String, Placement>
    ): Placement? {
        val inSmall = small[id]
        val inLarge = large[id]
        val from = inSmall ?: inLarge?.hidden() ?: return null
        val to = inLarge ?: from.hidden()
        val blended = blend(from, to, largeProgress)
        val alpha = when {
            inLarge == null || inLarge.hidden -> from.alpha * (1f - fade(largeProgress / ONE_SIDED_FADE))
            inSmall == null || inSmall.hidden -> to.alpha * fade((largeProgress - (1f - ONE_SIDED_FADE)) / ONE_SIDED_FADE)
            else -> blended.alpha
        }
        return if (alpha == blended.alpha) blended else blended.withAlpha(alpha)
    }

    private fun blend(from: Placement, to: Placement, t: Float): Placement = Placement(
        left = lerp(from.left, to.left, t),
        top = lerp(from.top, to.top, t),
        size = lerp(from.size, to.size, t),
        alpha = lerp(from.alpha, to.alpha, t),
        rotation = lerp(from.rotation, to.rotation, t),
        capRatio = lerp(from.capRatio, to.capRatio, t),
        advances = FloatArray(to.advances.size) { i ->
            lerp(
                from.advances.getOrElse(i) { 0f },
                to.advances[i],
                t
            )
        },
        widthAxis = lerp(from.widthAxis, to.widthAxis, t),
        heightAxis = lerp(from.heightAxis, to.heightAxis, t),
        tracking = lerp(from.tracking, to.tracking, t),
        weight = lerp(from.weight, to.weight, t),
        color = ColorUtils.blendARGB(from.color, to.color, t),
        lineHeight = lerp(from.lineHeight, to.lineHeight, t),
        stretch = lerp(from.stretch, to.stretch, t),
        stretchX = lerp(from.stretchX, to.stretchX, t),
        effect = if (t < HALF) from.effect else to.effect,
        hidden = to.hidden,
        tiltX = lerp(from.tiltX, to.tiltX, t),
        tiltY = lerp(from.tiltY, to.tiltY, t),
        drop = lerp(from.drop, to.drop, t)
    )

    private fun Placement.withAlpha(value: Float): Placement = Placement(
        left,
        top,
        size,
        value,
        rotation,
        capRatio,
        advances,
        widthAxis,
        heightAxis,
        tracking,
        weight,
        color,
        lineHeight,
        stretch,
        stretchX,
        effect,
        hidden,
        tiltX,
        tiltY,
        drop
    )

    private fun Placement.hidden(): Placement = Placement(
        left,
        top,
        size,
        0f,
        rotation,
        capRatio,
        advances,
        widthAxis,
        heightAxis,
        tracking,
        weight,
        color,
        lineHeight,
        stretch,
        stretchX,
        effect,
        tiltX = tiltX,
        tiltY = tiltY,
        drop = drop
    )

    private fun fade(value: Float): Float {
        val t = value.coerceIn(0f, 1f)
        return t * t * (3f - 2f * t)
    }

    private fun Placement.bottom(layer: FaceLayer?): Float {
        val descent = if (layer == null || layer.isTime || layer.isShape) DESCENT else TEXT_DESCENT
        return top + size * (capRatio * stretch * lineHeight + descent)
    }

    private fun boxBottom(root: FaceNode, width: Float): Float =
        if (root is StackNode && root.width != null) root.placement.y * width + measure(
            root,
            width
        ).height else 0f

    private fun bottomOf(placements: Map<String, Placement>): Float =
        placements.filter { (id, placement) -> !placement.hidden && id in views }
            .maxOfOrNull { (id, placement) -> placement.bottom(layerById(id)) } ?: 0f

    private fun frames(width: Float) {
        if (width == framesWidth) return
        framesWidth = width
        smallFrames = resolve(face.small, width)
        largeFrames = resolve(face.large, width)
        aodSmallFrames = face.aodSmall?.let { resolve(it, width) }
        aodLargeFrames = face.aodLarge?.let { resolve(it, width) }
    }

    private fun resolve(root: FaceNode, width: Float): Map<String, Placement> {
        val out = HashMap<String, Placement>()
        val box = measure(root, width)
        val anchor = root.placement.x * width
        val left = when (root.placement.anchor) {
            FaceAlign.START -> anchor
            FaceAlign.CENTER -> anchor - box.width / 2f
            FaceAlign.END -> anchor - box.width
        }
        place(root, left, root.placement.y * width, width, out)
        return out
    }

    private fun measure(node: FaceNode, width: Float): Box = when (node) {
        is LayerNode -> leaf(node, width)?.let { Box(it.width, it.height, it.descent) } ?: Box(
            0f,
            0f
        )

        is RowNode -> {
            val boxes = node.children.map { measure(it, width) }
            if (boxes.isEmpty()) {
                Box(0f, 0f)
            } else {
                val base = boxes.maxOf { it.height }
                var x = 0f
                var minLeft = Float.MAX_VALUE
                var maxRight = -Float.MAX_VALUE
                var minTop = Float.MAX_VALUE
                var maxBottom = -Float.MAX_VALUE
                node.children.forEachIndexed { index, child ->
                    val box = boxes[index]
                    val left = x + child.placement.x * width
                    val top = crossOffset(
                        (child as? LayerNode)?.align ?: node.align,
                        base,
                        box.height
                    ) + child.placement.y * width
                    minLeft = min(minLeft, left)
                    maxRight = max(maxRight, left + box.width)
                    minTop = min(minTop, top)
                    maxBottom = max(maxBottom, top + box.height)
                    x += box.width + node.gap * width
                }
                Box(
                    maxRight - minLeft,
                    maxBottom - minTop,
                    boxes.maxOf { it.descent },
                    -minLeft,
                    -minTop
                )
            }
        }

        is ColumnNode -> {
            val boxes = node.children.map { measure(it, width) }
            if (boxes.isEmpty()) {
                Box(0f, 0f)
            } else {
                val base = boxes.maxOf { it.width }
                var y = 0f
                var minLeft = Float.MAX_VALUE
                var maxRight = -Float.MAX_VALUE
                var minTop = Float.MAX_VALUE
                var maxBottom = -Float.MAX_VALUE
                node.children.forEachIndexed { index, child ->
                    val box = boxes[index]
                    val left = crossOffset(
                        (child as? LayerNode)?.align ?: node.align,
                        base,
                        box.width
                    ) + child.placement.x * width
                    val top = y + child.placement.y * width
                    minLeft = min(minLeft, left)
                    maxRight = max(maxRight, left + box.width)
                    minTop = min(minTop, top)
                    maxBottom = max(maxBottom, top + box.height)
                    y += box.height + node.gap * width
                }
                Box(maxRight - minLeft, maxBottom - minTop, boxes.last().descent, -minLeft, -minTop)
            }
        }

        is StackNode -> if (node.width != null) {
            Box(
                node.width * width,
                node.height?.let { it * width } ?: (node.children.maxOfOrNull {
                    measure(
                        it,
                        width
                    ).height
                } ?: 0f))
        } else {
            val tops = stackTops(node, width)
            var right = 0f
            var bottom = 0f
            node.children.forEachIndexed { index, child ->
                val box = measure(child, width)
                right = max(right, stackLeft(child, box, width) + box.width)
                bottom = max(bottom, tops[index] + box.height)
            }
            Box(right, bottom)
        }
    }

    private fun place(
        node: FaceNode,
        left: Float,
        top: Float,
        width: Float,
        out: MutableMap<String, Placement>,
        hidden: Boolean = false
    ) {
        val off = hidden || node.placement.hidden
        when (node) {
            is LayerNode -> {
                val (metrics, _, _, size) = leaf(node, width) ?: return
                val layer = layerById(node.id) ?: return
                out[node.id] = Placement(
                    left = left,
                    top = top,
                    size = size,
                    alpha = if (off) 0f else node.alpha,
                    rotation = node.rotation,
                    capRatio = metrics.capRatio,
                    advances = metrics.advances,
                    widthAxis = (node.width ?: layer.width).toFloat(),
                    heightAxis = (node.height ?: layer.height).toFloat(),
                    tracking = node.tracking ?: layer.tracking,
                    weight = (node.weight ?: layer.weight).toFloat(),
                    color = FaceColors.resolve(context, node.color ?: layer.color),
                    lineHeight = node.lineHeight ?: layer.lineHeight,
                    stretch = if (layer.isShape) 1f else node.stretch ?: 1f,
                    stretchX = if (layer.isShape) 1f else node.stretchX ?: 1f,
                    effect = node.effect ?: layer.effect,
                    hidden = off,
                    tiltX = node.tiltX,
                    tiltY = node.tiltY,
                    drop = metrics.drop
                )
            }

            is RowNode -> {
                val box = measure(node, width)
                val base = node.children.maxOfOrNull { measure(it, width).height } ?: 0f
                var x = left + box.offsetX
                node.children.forEach { child ->
                    val childBox = measure(child, width)
                    val y = top + box.offsetY + crossOffset(
                        (child as? LayerNode)?.align ?: node.align,
                        base,
                        childBox.height
                    )
                    place(
                        child,
                        x + child.placement.x * width,
                        y + child.placement.y * width,
                        width,
                        out,
                        off
                    )
                    x += childBox.width + node.gap * width
                }
            }

            is ColumnNode -> {
                val box = measure(node, width)
                val base = node.children.maxOfOrNull { measure(it, width).width } ?: 0f
                var y = top + box.offsetY
                node.children.forEach { child ->
                    val childBox = measure(child, width)
                    val x = left + box.offsetX + crossOffset(
                        (child as? LayerNode)?.align ?: node.align,
                        base,
                        childBox.width
                    )
                    place(
                        child,
                        x + child.placement.x * width,
                        y + child.placement.y * width,
                        width,
                        out,
                        off
                    )
                    y += childBox.height + node.gap * width
                }
            }

            is StackNode -> if (node.width != null) {
                val boxWidth = node.width * width
                val boxHeight = node.height?.let { it * width }
                node.children.forEach { child ->
                    val box = measure(child, width)
                    val x = when ((child as? LayerNode)?.align ?: child.placement.anchor) {
                        FaceAlign.START -> 0f
                        FaceAlign.CENTER -> (boxWidth - box.width) / 2f
                        FaceAlign.END -> boxWidth - box.width
                    }
                    val y = boxHeight?.let { (it - (box.height - box.descent)) / 2f } ?: 0f
                    place(
                        child,
                        left + x + child.placement.x * width,
                        top + y + child.placement.y * width,
                        width,
                        out,
                        off
                    )
                }
            } else {
                val tops = stackTops(node, width)
                node.children.forEachIndexed { index, child ->
                    val box = measure(child, width)
                    place(
                        child,
                        left + stackLeft(child, box, width),
                        top + tops[index],
                        width,
                        out,
                        off
                    )
                }
            }
        }
    }

    private fun leaf(node: LayerNode, width: Float): Leaf? {
        val layer = layerById(node.id)?.takeIf(::isSupported) ?: return null
        val metrics = metricsOf(
            layer,
            node.width ?: layer.width,
            node.height ?: layer.height,
            node.tracking ?: layer.tracking,
            node.weight ?: layer.weight
        )
        val size = node.size * width
        val lineHeight = node.lineHeight ?: layer.lineHeight
        val descent = if (layer.isTime || layer.isShape) 0f else size * TEXT_DESCENT
        val stretch = if (layer.isShape) 1f else node.stretch ?: 1f
        val stretchX = if (layer.isShape) 1f else node.stretchX ?: 1f
        return Leaf(
            metrics,
            metrics.widthRatio * stretchX * size,
            metrics.capRatio * stretch * size * lineHeight + descent,
            size,
            descent
        )
    }

    private fun stackTops(node: StackNode, width: Float): FloatArray {
        var cursor = 0f
        val gap = AUTO_GAP * width
        return FloatArray(node.children.size) { index ->
            val child = node.children[index]
            val height = measure(child, width).height
            val offset = child.placement.y * width
            val top = if (child.placement.auto) cursor + offset else offset
            if (!child.placement.hidden && height > 0f) {
                cursor = if (child.placement.auto) cursor + height + gap else max(
                    cursor,
                    top + height + gap
                )
            }
            top
        }
    }

    private fun stackLeft(child: FaceNode, box: Box, width: Float): Float {
        val anchor = child.placement.x * width
        return when (child.placement.anchor) {
            FaceAlign.START -> anchor
            FaceAlign.CENTER -> anchor - box.width / 2f
            FaceAlign.END -> anchor - box.width
        }
    }

    private fun crossOffset(align: FaceAlign, container: Float, child: Float): Float =
        when (align) {
            FaceAlign.START -> 0f
            FaceAlign.CENTER -> (container - child) / 2f
            FaceAlign.END -> container - child
        }

    private fun layerById(id: String): FaceLayer? = face.layers.firstOrNull { it.id == id }

    private fun metricsOf(
        layer: FaceLayer,
        widthAxis: Int,
        heightAxis: Int,
        tracking: Float,
        weight: Int
    ): LayerMetrics =
        metrics.getOrPut(MetricsKey(layer.id, widthAxis, heightAxis, tracking, weight)) {
            if (layer.isHost) {
                val reference = hostWidth.coerceAtLeast(1f)
                val (hostW, hostH) = hostSizes[layer.id] ?: (0 to 0)
                return@getOrPut LayerMetrics(
                    hostH / reference,
                    floatArrayOf(hostW / reference),
                    hostW / reference
                )
            }
            if (layer.isShape) return@getOrPut LayerMetrics(
                1f / layer.aspect.coerceAtLeast(
                    MIN_ASPECT
                ), floatArrayOf(1f), 1f
            )
            val text = texts[layer.id].orEmpty()
            val typeface: Typeface = FaceFont.typeface(layer.font, weight, widthAxis, heightAxis)
            measurePaint.typeface = typeface
            measurePaint.letterSpacing = 0f
            measurePaint.getTextBounds("0", 0, 1, bounds)
            val capRatio = if (bounds.isEmpty) 1f else bounds.height() / BASE_SIZE
            val drop =
                if (bounds.isEmpty || abs(bounds.bottom) <= bounds.height() * DROP_TOLERANCE) 0f else bounds.bottom / BASE_SIZE
            val advances = if (layer.isTime) {
                val digit = DIGITS.maxOf { measurePaint.measureText(it.toString()) } / BASE_SIZE
                FloatArray(text.length) { index ->
                    val glyph =
                        if (text[index].isDigit()) digit else measurePaint.measureText(text[index].toString()) / BASE_SIZE
                    if (index < text.lastIndex) glyph + tracking else glyph
                }
            } else {
                measurePaint.letterSpacing = tracking
                floatArrayOf(measurePaint.measureText(text) / BASE_SIZE)
            }
            LayerMetrics(capRatio, advances, advances.sum(), drop)
        }

    private fun lerp(from: Float, to: Float, fraction: Float): Float = from + (to - from) * fraction

    private data class MetricsKey(
        val id: String,
        val width: Int,
        val height: Int,
        val tracking: Float,
        val weight: Int
    )

    companion object {
        const val BASE_SIZE = 200f
        private const val SIZE_EPSILON = 0.5f
        private const val HOST_MIN_ALPHA = 0.01f
        private const val HALF = 0.5f
        private const val ONE_SIDED_FADE = 0.3f
        private const val DESCENT = 0.08f
        private const val AUTO_GAP = 0.025f
        private const val TEXT_DESCENT = 0.24f
        private const val DROP_TOLERANCE = 0.15f
        private const val MIN_ASPECT = 0.05f
        private const val OUTLINE_RATIO = 0.02f
        private const val CAMERA_DISTANCE = 8000f
        private const val GLASS_AOD_CUTOFF = 0.5f
        private const val GLASS_FALLBACK_ALPHA = 0x99
        private const val GLASS_DIGIT = 0xFFFFFFFF.toInt()
        private const val GLASS_EXTRA = 0xFF808080.toInt()
        private const val OPAQUE = 255f
        private const val DIGITS = "0123456789"
        private const val HOURS_ON_DIAL = 12
        private const val MINUTES_PER_HOUR = 60f
        private const val DEGREES_PER_HOUR = 30f
        private const val DEGREES_PER_MINUTE = 6f
        private const val HAND_THICKNESS = 0.14f
        private const val MAX_WEIGHT = 1000f
        private const val DATE_FORMAT = "EEE d MMM"
        private const val CLOCK_GROUP = "clock"
        private const val HOST_WAKE_SCALE = 0.9f
    }
}
