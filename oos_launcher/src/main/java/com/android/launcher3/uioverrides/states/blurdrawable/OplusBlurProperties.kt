package com.android.launcher3.uioverrides.states.blurdrawable

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.android.launcher3.widget.CustomLauncherAppWidgetHostView


class OplusBlurProperties {

    fun recycle() {
        throw UnsupportedOperationException("Stub!")
    }

    fun setBlurBgToView(shortcutAndWidgetContainer: View) {
        throw UnsupportedOperationException("Stub!")
    }

    fun setBlurCornerRadius(radius: Float, isFolder: Boolean) : OplusBlurProperties {
        throw UnsupportedOperationException("Stub!")
    }

    fun setBlurParams(
        blurRadius: Int,
        colorMode: Int,
        blendColor: Int,
        mixColor: Int
    ): OplusBlurProperties {
        throw UnsupportedOperationException("Stub!")
    }

    class BlendColorParams(
        val blendMode: Int,
        val blendColor: Int,
        val mixColor: Int
    ) {
        fun copy(blendMode: Int, blendColor: Int, mixColor: Int): BlendColorParams {
            return BlendColorParams(blendMode, blendColor, mixColor)
        }

        override fun equals(other: Any?): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        override fun toString(): String {
            throw UnsupportedOperationException("Stub!")
        }

        companion object {

        }

        override fun hashCode(): Int {
            throw UnsupportedOperationException("Stub!")
        }
    }


    companion object {

        @JvmStatic
        fun getBlurColorParams(context: Context): BlendColorParams? {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun getSysMaterialBlurSwitchEnable(): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun isMaterialBlurJumpSupported(): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun isSupportNewBlur(context: Context?, isLocalBlur: Boolean): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun pauseBlurService(context: Context?, z: Boolean) {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun prepareBlur(
            view: View?,
            context: Context,
            setBackground: Boolean,
            needFadeIn: Boolean,
            type: Int
        ): OplusBlurProperties? {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun reduceBlurFrequency(context: Context?, z: Boolean) {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun saveStaticWallpaperBlurBitmap(context: Context?) {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun setBitmapToWidgetBackground(
            context: Context,
            view: CustomLauncherAppWidgetHostView,
            bitmap: Bitmap?
        ) {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun setBlurEnable(context: Context?, z: Boolean) {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun setWidgetBackground(context: Context, view: View?, z: Boolean) {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun getBlurColorParams(
            context: Context,
            isBright: Boolean,
            isExtremeDark: Boolean
        ): BlendColorParams? {
            throw UnsupportedOperationException("Stub!")
        }


    }

}