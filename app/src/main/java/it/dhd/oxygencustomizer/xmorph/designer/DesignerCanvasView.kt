package it.dhd.oxygencustomizer.xmorph.designer

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Outline
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewOutlineProvider
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import it.dhd.oxygencustomizer.R
import it.dhd.oxygencustomizer.utils.OCPreferences
import it.dhd.oxygencustomizer.xmorph.domain.editor.ItemGroups
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFace
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockFrame
import it.dhd.oxygencustomizer.xmorph.domain.model.ClockGeometry
import it.dhd.oxygencustomizer.xmorph.domain.model.EditScope
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceAlign
import it.dhd.oxygencustomizer.xmorph.view.FaceClockView
import it.dhd.oxygencustomizer.xposed.utils.GlassTextEffect
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

class DesignerCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    interface Listener {
        fun onSelect(id: String?)
        fun onTransformStart(id: String): Pair<Float, Float>
        fun onMove(id: String, dx: Float, dy: Float)
        fun onTransform(id: String, size: Float, rotation: Float)
        fun onResizeStart(id: String)
        fun onResize(id: String, scaleX: Float, scaleY: Float)
        fun onGestureEnd()
    }

    var listener: Listener? = null

    private val density = resources.displayMetrics.density
    private val wallpaperView =
        ImageView(context).apply { scaleType = ImageView.ScaleType.CENTER_CROP }
    private val dimView = View(context).apply { setBackgroundColor(Color.BLACK); alpha = 0f }
    private var clock: FaceClockView? = null
    private var wallpaper: Bitmap? = null
    private var depthSubject: DesignerDepthSubject? = null
    private var scope = EditScope.LARGE
    private var selectedId: String? = null

    private val screenAspect: Float =
        resources.displayMetrics.let { it.widthPixels.toFloat() / it.heightPixels.toFloat() }
    private val geometry = ClockGeometry(
        large = ClockFrame(0.06f, 0.1f, 0.88f),
        small = ClockFrame(0.06f, 0.07f, 0.88f),
        aspect = screenAspect
    )

    private val selectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = 0xE6FFFFFF.toInt()
    }
    private val outlinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = 0x59FFFFFF
    }
    private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f * density
        strokeCap = Paint.Cap.ROUND
        color = Color.WHITE
    }
    private val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1f * density
        color = 0x40FFFFFF
    }

    var editable = true
        set(value) {
            field = value
            invalidate()
        }

    var topMarginDp = 0f
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    private val lockView = ImageView(context).apply {
        setImageResource(R.drawable.ic_lock)
        imageTintList = ColorStateList.valueOf(LOCK_TINT)
    }

    private val previewScale: Float
        get() = width / resources.displayMetrics.widthPixels.toFloat().coerceAtLeast(1f)

    init {
        setWillNotDraw(false)
        clipChildren = false
        clipToOutline = true
        outlineProvider = object : ViewOutlineProvider() {
            override fun getOutline(view: View, outline: Outline) {
                outline.setRoundRect(0, 0, view.width, view.height, CARD_RADIUS_DP * density)
            }
        }
        setBackgroundColor(Color.BLACK)
        addView(wallpaperView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(dimView, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(lockView, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val maxWidth = MeasureSpec.getSize(widthMeasureSpec)
        val maxHeight = MeasureSpec.getSize(heightMeasureSpec)
        var height = maxHeight
        var width = (height * screenAspect).roundToInt()
        if (width > maxWidth) {
            width = maxWidth
            height = (width / screenAspect).roundToInt()
        }
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        layoutLock()
        layoutClock()
    }

    private fun layoutLock() {
        if (width == 0 || height == 0) return
        val size = (LOCK_SIZE_DP * density * previewScale).roundToInt().coerceAtLeast(1)
        lockView.measure(
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY)
        )
        val bottom =
            (height * geometry.large.top - LOCK_GAP_DP * density * previewScale).roundToInt()
        val left = (width - size) / 2
        lockView.layout(left, bottom - size, left + size, bottom)
    }

    private var large = 1f
    private var aod = 0f
    private var morph: ValueAnimator? = null

    private fun largeProgress(): Float = large

    private fun layoutClock() {
        val view = clock ?: return
        if (width == 0 || height == 0) return
        val frame = geometry.frame(largeProgress())
        val clockWidth = (width * frame.width).roundToInt().coerceAtLeast(1)
        view.measure(
            MeasureSpec.makeMeasureSpec(clockWidth, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
        )
        val x = (width * frame.left).roundToInt()
        val y = (height * frame.top + topMarginDp * density * previewScale).roundToInt()
        view.layout(x, y, x + clockWidth, y + view.measuredHeight)
    }

    override fun onDetachedFromWindow() {
        clock?.hostProvider = null
        super.onDetachedFromWindow()
    }

    fun setWallpaper(bitmap: Bitmap?) {
        wallpaper = bitmap
        wallpaperView.setImageBitmap(bitmap)
        GlassTextEffect.setFrame(this, bitmap)
        requestLayout()
    }

    private var depthOpacity = DEPTH_OPAQUE

    fun setDepthSubject(bitmap: Bitmap?) {
        depthSubject = bitmap?.let {
            DesignerDepthSubject(this, it).apply {
                dim = aod
                opacity = depthOpacity
            }
        }
        clock?.depthSubject = depthSubject
    }

    fun setDepthOpacity(value: Int) {
        depthOpacity = value
        depthSubject?.opacity = value
        clock?.invalidateSubject()
    }

    fun setFace(face: ClockFace) {
        val view = clock
        if (view == null) {
            val created = FaceClockView(context, face)
            created.hostProvider = LockscreenHosts(context, OCPreferences.getPrefs(), true)
            created.depthSubject = depthSubject
            clock = created
            addView(created, 2, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT))
            applyScope()
        } else if (view.face != face) {
            view.editFace(face)
        }
        post { invalidate() }
    }

    fun setScope(value: EditScope) {
        if (scope == value) return
        scope = value
        morph?.cancel()
        large = targetLarge()
        aod = targetAod()
        applyScope()
    }

    fun morphTo(value: EditScope, from: EditScope) {
        scope = value
        morph?.cancel()
        val startLarge = if (from == EditScope.SMALL) 0f else 1f
        val startAod = if (from == EditScope.AOD) 1f else 0f
        val endLarge = targetLarge()
        val endAod = targetAod()
        morph = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = MORPH_MS
            interpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
            addUpdateListener {
                val t = it.animatedValue as Float
                large = startLarge + (endLarge - startLarge) * t
                aod = startAod + (endAod - startAod) * t
                applyScope()
            }
            start()
        }
    }

    private fun targetLarge(): Float = if (scope == EditScope.SMALL) 0f else 1f

    private fun targetAod(): Float = if (scope == EditScope.AOD) 1f else 0f

    private fun applyScope() {
        val view = clock ?: return
        view.setLargeProgress(large)
        view.setAodProgress(aod)
        dimView.alpha = aod
        depthSubject?.dim = aod
        view.invalidateSubject()
        layoutClock()
        invalidate()
    }

    fun setSelected(id: String?) {
        if (selectedId == id) return
        selectedId = id
        invalidate()
    }

    fun alignOffset(id: String, align: FaceAlign): Float {
        val view = clock ?: return 0f
        val width = view.width.toFloat().takeIf { it > 0f } ?: return 0f
        val rect = rectOf(rects(includeHidden = false), id) ?: return 0f
        val left = rect.left - view.left
        val target = when (align) {
            FaceAlign.START -> 0f
            FaceAlign.CENTER -> (width - rect.width()) / 2f
            FaceAlign.END -> width - rect.width()
        }
        return (target - left) / width
    }

    fun visibleRects(): Map<String, RectF> = rects(false)

    fun clockWidth(): Float = clock?.width?.toFloat() ?: 0f

    private fun rects(includeHidden: Boolean = true): Map<String, RectF> {
        val view = clock ?: return emptyMap()
        val dx = view.left.toFloat()
        val dy = view.top.toFloat()
        val shown =
            view.layerRects(includeHidden).mapValues { RectF(it.value).apply { offset(dx, dy) } }
        if (!includeHidden) return shown
        return shown + view.missingLayerRects()
            .mapValues { RectF(it.value).apply { offset(dx, dy) } }
    }

    private fun rectOf(rects: Map<String, RectF>, id: String?): RectF? {
        id ?: return null
        if (!ItemGroups.isGroup(id)) return rects[id]
        val members = rects.filterKeys { ItemGroups.belongs(id, it) }.values
        if (members.isEmpty()) return null
        return RectF(
            members.minOf { it.left },
            members.minOf { it.top },
            members.maxOf { it.right },
            members.maxOf { it.bottom })
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val half = cardPaint.strokeWidth / 2f
        canvas.drawRoundRect(
            half,
            half,
            width - half,
            height - half,
            CARD_RADIUS_DP * density,
            CARD_RADIUS_DP * density,
            cardPaint
        )
        if (!editable) return
        val rects = rects()
        if (rects.isEmpty()) return
        val pad = FRAME_PAD_DP * density
        val radius = FRAME_RADIUS_DP * density
        val selectedKey = selectedId
        rects.keys.map { ItemGroups.keyOf(it) }.distinct().filter { it != selectedKey }
            .forEach { key ->
                rectOf(rects, key)?.let {
                    canvas.drawRoundRect(
                        it.left - pad,
                        it.top - pad,
                        it.right + pad,
                        it.bottom + pad,
                        radius,
                        radius,
                        outlinePaint
                    )
                }
            }
        rectOf(rects, selectedKey)?.let { rect ->
            val l = rect.left - pad
            val t = rect.top - pad
            val r = rect.right + pad
            val b = rect.bottom + pad
            canvas.drawRoundRect(l, t, r, b, radius, radius, selectionPaint)
            canvas.drawArc(r - 2 * radius, b - 2 * radius, r, b, 0f, 90f, false, handlePaint)
        }
        postInvalidateOnAnimation()
    }

    private val slop = ViewConfiguration.get(context).scaledTouchSlop
    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false
    private var target: String? = null
    private var transforming = false
    private var baseSize = 0f
    private var baseRotation = 0f
    private var startSpan = 0f
    private var startAngle = 0f
    private var handleMode = false
    private var emptyTap = false
    private var handleCenterX = 0f
    private var handleCenterY = 0f
    private var handleStartX = 0f
    private var handleStartY = 0f

    @SuppressLint("ClickableViewAccessibility")
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean = true

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val view = clock ?: return false
        val clockWidth = view.width.toFloat().takeIf { it > 0f } ?: return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                parent?.requestDisallowInterceptTouchEvent(true)
                downX = event.x
                downY = event.y
                lastX = event.x
                lastY = event.y
                dragging = false
                transforming = false
                handleMode = false
                emptyTap = false
                val rects = rects()
                val selected = rectOf(rects, selectedId)
                if (!editable) return false
                val pad = FRAME_PAD_DP * density
                val onHandle = selected != null && hypot(
                    event.x - (selected.right + pad),
                    event.y - (selected.bottom + pad)
                ) < HANDLE_TOUCH_DP * density
                if (!onHandle && hitsAt(rects, event.x, event.y).isEmpty()) {
                    target = null
                    if (selectedId == null) return false
                    parent?.requestDisallowInterceptTouchEvent(false)
                    emptyTap = true
                    return true
                }
                if (selected != null && hypot(
                        event.x - (selected.right + pad),
                        event.y - (selected.bottom + pad)
                    ) < HANDLE_TOUCH_DP * density
                ) {
                    val id = selectedId!!
                    target = id
                    handleMode = true
                    listener?.onResizeStart(id)
                    handleCenterX = selected.left
                    handleCenterY = selected.top
                    handleStartX =
                        (event.x - handleCenterX).coerceAtLeast(MIN_HANDLE_SPAN_DP * density)
                    handleStartY =
                        (event.y - handleCenterY).coerceAtLeast(MIN_HANDLE_SPAN_DP * density)
                } else {
                    target = hitsAt(rects, event.x, event.y).firstOrNull()?.let { hit ->
                        val key = ItemGroups.keyOf(hit)
                        if (selectedId != null && (selectedId == hit || (ItemGroups.isGroup(
                                selectedId!!
                            ) && selectedId == key) || ItemGroups.belongs(key, selectedId!!))
                        ) selectedId else key
                    }
                }
                return true
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                val id = target ?: selectedId ?: return true
                if (event.pointerCount >= 2) {
                    transforming = true
                    dragging = false
                    target = id
                    if (id != selectedId) listener?.onSelect(id)
                    val (size, rotation) = listener?.onTransformStart(id) ?: (0f to 0f)
                    baseSize = size
                    baseRotation = rotation
                    startSpan = span(event)
                    startAngle = angle(event)
                }
                return true
            }

            MotionEvent.ACTION_MOVE -> {
                val id = target ?: return true
                if (handleMode) {
                    val scaleX = (event.x - handleCenterX).coerceAtLeast(1f) / handleStartX
                    val scaleY = (event.y - handleCenterY).coerceAtLeast(1f) / handleStartY
                    listener?.onResize(id, scaleX, scaleY)
                    return true
                }
                if (transforming && event.pointerCount >= 2) {
                    val zoom = span(event) / startSpan.coerceAtLeast(1f)
                    var rotation = baseRotation + (angle(event) - startAngle)
                    if (abs(rotation) < ROTATION_SNAP) rotation = 0f
                    listener?.onTransform(id, baseSize * zoom, rotation)
                    return true
                }
                if (transforming) return true
                if (!dragging && hypot(event.x - downX, event.y - downY) > slop) {
                    dragging = true
                    if (id != selectedId) listener?.onSelect(id)
                    lastX = event.x
                    lastY = event.y
                }
                if (dragging) {
                    listener?.onMove(
                        id,
                        (event.x - lastX) / clockWidth,
                        (event.y - lastY) / clockWidth
                    )
                    lastX = event.x
                    lastY = event.y
                }
                return true
            }

            MotionEvent.ACTION_UP -> {
                if (emptyTap) {
                    emptyTap = false
                    if (hypot(event.x - downX, event.y - downY) <= slop) listener?.onSelect(null)
                    return true
                }
                if (!dragging && !transforming && !handleMode) {
                    listener?.onSelect(cycleTarget(rects(), event.x, event.y))
                }
                listener?.onGestureEnd()
                dragging = false
                transforming = false
                handleMode = false
                target = null
                return true
            }

            MotionEvent.ACTION_CANCEL -> {
                listener?.onGestureEnd()
                emptyTap = false
                dragging = false
                transforming = false
                handleMode = false
                target = null
                return true
            }
        }
        return true
    }

    private fun span(event: MotionEvent): Float =
        hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))

    private fun angle(event: MotionEvent): Float =
        Math.toDegrees(
            atan2(
                (event.getY(1) - event.getY(0)).toDouble(),
                (event.getX(1) - event.getX(0)).toDouble()
            )
        ).toFloat()

    private fun hitsAt(rects: Map<String, RectF>, x: Float, y: Float): List<String> {
        val slop = HIT_SLOP_DP * density
        return rects.entries
            .filter { (_, r) -> x >= r.left - slop && x <= r.right + slop && y >= r.top - slop && y <= r.bottom + slop }
            .sortedBy { (_, r) -> r.width() * r.height() }
            .map { it.key }
    }

    private fun cycleTarget(rects: Map<String, RectF>, x: Float, y: Float): String? {
        val hits = hitsAt(rects, x, y)
        val first = hits.firstOrNull() ?: return null
        val key = ItemGroups.keyOf(first)
        val members = hits.filter { ItemGroups.belongs(key, it) }
        val selected = selectedId
        return when {
            selected == null -> key
            ItemGroups.isGroup(selected) -> if (selected == key) members.first() else key
            !ItemGroups.belongs(key, selected) -> key
            else -> members.getOrNull(members.indexOf(selected) + 1) ?: key
        }
    }

    companion object {
        private const val DEPTH_OPAQUE = 255
        private const val HIT_SLOP_DP = 8f
        private const val LOCK_SIZE_DP = 24f
        private const val LOCK_GAP_DP = 10f
        private const val LOCK_TINT = 0x99FFFFFF.toInt()
        private const val HANDLE_TOUCH_DP = 24f
        private const val MIN_HANDLE_SPAN_DP = 12f
        private const val ROTATION_SNAP = 4f
        private const val MORPH_MS = 650L
        private const val CARD_RADIUS_DP = 36f
        private const val FRAME_PAD_DP = 6f
        private const val FRAME_RADIUS_DP = 14f
    }
}
