package com.android.systemui.animation

import android.content.ComponentName
import android.view.View

/** A piece of UI that can be expanded into a Dialog or an Activity. */
interface Expandable {

    companion object {
        /**
         * Create an [Expandable] that will animate [view] when expanded.
         *
         * Note: The background of [view] should be a (rounded) rectangle so that it can be properly
         * animated.
         */
        @JvmStatic
        fun fromView(view: View): Expandable {
            throw UnsupportedOperationException("Stub!")
        }
    }
}