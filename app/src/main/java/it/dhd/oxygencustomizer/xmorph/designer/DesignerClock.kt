package it.dhd.oxygencustomizer.xmorph.designer

import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.PathInterpolator
import android.widget.FrameLayout
import it.dhd.oxygencustomizer.xmorph.data.mapper.ClockFaceJsonMapper
import it.dhd.oxygencustomizer.xmorph.view.DepthSubject
import it.dhd.oxygencustomizer.xmorph.view.FaceClockView
import java.util.WeakHashMap

object DesignerClock {

    const val KEY_APPLIED = DesignerRepository.KEY_APPLIED

    private const val MORPH_MS = 650L
    private const val HOST_HIDE_MS = 1000L
    private val MORPH_CURVE = PathInterpolator(0.2f, 0f, 0f, 1f)
    private val HOST_HIDE_CURVE = PathInterpolator(0.4f, 0f, 0.2f, 1f)
    private const val SIDE_PADDING = 0.06f

    @JvmStatic
    fun create(context: Context, json: String?, hosts: FaceClockView.HostProvider?): View? {
        DesignerFonts.install()
        val face = json?.takeIf { it.isNotBlank() }?.let(ClockFaceJsonMapper::fromJson)
            ?.let(DesignerFonts::sanitize) ?: return null
        val clock = FaceClockView(context, face)
        clock.hostProvider = hosts
        val container = object : FrameLayout(context) {
            override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
                val pad = (MeasureSpec.getSize(widthMeasureSpec) * SIDE_PADDING).toInt()
                if (paddingLeft != pad) setPadding(pad, 0, pad, 0)
                super.onMeasure(widthMeasureSpec, heightMeasureSpec)
            }
        }
        container.clipChildren = false
        container.clipToPadding = false
        container.addView(
            clock,
            FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
        container.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return container
    }

    @JvmStatic
    fun setDepthSubject(view: View?, subject: DepthSubject?) {
        clocksOf(view).forEach { it.depthSubject = subject }
    }

    @JvmStatic
    fun release(view: View?) {
        clocksOf(view).forEach { it.hostProvider = null }
    }

    @JvmStatic
    fun setAod(view: View?, aod: Boolean) {
        clocksOf(view).forEach { clock ->
            val state = states.getOrPut(clock) { MorphState() }
            val target = if (aod) 1f else 0f
            state.aod = animate(state.aod, target, state.aod.animator) {
                clock.setAodProgress(
                    it,
                    state.hosts.value
                )
            }
            val hostMs = if (aod) HOST_HIDE_MS else MORPH_MS
            val hostCurve = if (aod) HOST_HIDE_CURVE else MORPH_CURVE
            state.hosts = animate(
                state.hosts,
                target,
                state.hosts.animator,
                hostMs,
                hostCurve
            ) { clock.setAodProgress(state.aod.value, it) }
        }
    }

    @JvmStatic
    fun setLarge(view: View?, large: Boolean) {
        clocksOf(view).forEach { clock ->
            val state = states.getOrPut(clock) { MorphState() }
            state.large = animate(
                state.large,
                if (large) 1f else 0f,
                state.large.animator
            ) { clock.setLargeProgress(it) }
        }
    }

    private fun animate(
        track: Track,
        target: Float,
        running: ValueAnimator?,
        durationMs: Long = MORPH_MS,
        curve: PathInterpolator = MORPH_CURVE,
        apply: (Float) -> Unit
    ): Track {
        if (track.value == target && running?.isRunning != true) return track
        running?.cancel()
        val animator = ValueAnimator.ofFloat(track.value, target).apply {
            duration = durationMs
            interpolator = curve
            addUpdateListener {
                val value = it.animatedValue as Float
                track.value = value
                apply(value)
            }
        }
        track.animator = animator
        animator.start()
        return track
    }

    private fun clocksOf(view: View?): List<FaceClockView> = when (view) {
        is FaceClockView -> listOf(view)
        is ViewGroup -> (0 until view.childCount).flatMap { clocksOf(view.getChildAt(it)) }
        else -> emptyList()
    }

    private class Track(var value: Float, var animator: ValueAnimator? = null)

    private class MorphState(
        var aod: Track = Track(0f),
        var hosts: Track = Track(0f),
        var large: Track = Track(1f)
    )

    private val states = WeakHashMap<FaceClockView, MorphState>()
}
