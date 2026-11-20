package it.dhd.oxygencustomizer.xmorph.view

import android.graphics.Canvas
import android.view.View

interface DepthSubject {
    val drawsWhenAllBehind: Boolean
    fun attach(target: View)
    fun detach(target: View)
    fun draw(canvas: Canvas, target: View)
}
