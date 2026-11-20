package it.dhd.oxygencustomizer.xmorph.designer

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.view.View

class FontTileView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.LEFT
    }
    private val bounds = Rect()

    var text: String = "21"
        set(value) {
            field = value
            invalidate()
        }

    var typeface: Typeface? = null
        set(value) {
            field = value
            paint.typeface = value
            invalidate()
        }

    var textSizePx: Float
        get() = paint.textSize
        set(value) {
            paint.textSize = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        paint.getTextBounds(text, 0, text.length, bounds)
        if (bounds.isEmpty) return
        val maxWidth = width * 0.8f
        val maxHeight = height * 0.6f
        val scale = minOf(1f, maxWidth / bounds.width(), maxHeight / bounds.height())
        canvas.save()
        canvas.translate(width / 2f, height / 2f)
        canvas.scale(scale, scale)
        canvas.drawText(text, -bounds.exactCenterX(), -bounds.exactCenterY(), paint)
        canvas.restore()
    }
}
