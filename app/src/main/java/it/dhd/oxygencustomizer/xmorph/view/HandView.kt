package it.dhd.oxygencustomizer.xmorph.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class HandView(context: Context) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    var angle = 0f
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var length = 0f
        set(value) {
            if (field == value) return
            field = value
            invalidate()
        }

    var thickness = 0f
        set(value) {
            if (field == value) return
            field = value
            paint.strokeWidth = value
            invalidate()
        }

    var color = 0
        set(value) {
            if (field == value) return
            field = value
            paint.color = value
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val cx = width / 2f
        val cy = height / 2f
        canvas.save()
        canvas.rotate(angle, cx, cy)
        canvas.drawLine(cx, cy, cx, cy - length, paint)
        canvas.restore()
    }
}
