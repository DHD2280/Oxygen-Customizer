package it.dhd.oxygencustomizer.xmorph.designer

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.view.View
import it.dhd.oxygencustomizer.xmorph.view.DepthSubject
import kotlin.math.max
import kotlin.math.roundToInt

class DesignerDepthSubject(private val frame: View, private val bitmap: Bitmap) : DepthSubject {

    var dim = 0f
        set(value) {
            field = value
            paint.colorFilter = if (value > 0f) PorterDuffColorFilter(
                Color.argb(
                    (value * OPAQUE).roundToInt(),
                    0,
                    0,
                    0
                ), PorterDuff.Mode.SRC_ATOP
            ) else null
        }

    var opacity = OPAQUE.toInt()
        set(value) {
            field = value
            paint.alpha = value.coerceIn(0, OPAQUE.toInt())
        }

    private val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
    private val matrix = Matrix()
    private val frameLocation = IntArray(2)
    private val targetLocation = IntArray(2)

    override val drawsWhenAllBehind: Boolean = true

    override fun attach(target: View) = Unit

    override fun detach(target: View) = Unit

    override fun draw(canvas: Canvas, target: View) {
        if (frame.width == 0 || frame.height == 0 || bitmap.isRecycled) return
        frame.getLocationInWindow(frameLocation)
        target.getLocationInWindow(targetLocation)
        val scale =
            max(frame.width / bitmap.width.toFloat(), frame.height / bitmap.height.toFloat())
        matrix.setScale(scale, scale)
        matrix.postTranslate(
            (frame.width - bitmap.width * scale) / 2f + frameLocation[0] - targetLocation[0],
            (frame.height - bitmap.height * scale) / 2f + frameLocation[1] - targetLocation[1]
        )
        canvas.drawBitmap(bitmap, matrix, paint)
    }

    private companion object {
        const val OPAQUE = 255f
    }
}
