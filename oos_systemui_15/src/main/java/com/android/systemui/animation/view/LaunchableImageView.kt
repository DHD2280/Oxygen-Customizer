package com.android.systemui.animation.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import com.android.systemui.animation.LaunchableView
import com.android.systemui.animation.LaunchableViewDelegate

/** An [ImageView] that also implements [LaunchableView]. */
@SuppressLint("AppCompatCustomView")
open class LaunchableImageView : ImageView, LaunchableView {
    private val delegate =
        LaunchableViewDelegate(
            this,
            superSetVisibility = { super.setVisibility(it) },
        )

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
    ) : super(context, attrs, defStyleAttr)

    constructor(
        context: Context?,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        defStyleRes: Int,
    ) : super(context, attrs, defStyleAttr, defStyleRes)

    override fun setShouldBlockVisibilityChanges(block: Boolean) {
        delegate.setShouldBlockVisibilityChanges(block)
    }

    override fun setVisibility(visibility: Int) {
        delegate.setVisibility(visibility)
    }
}