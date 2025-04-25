package com.oplus.systemui.qs.base.widget

import android.content.Context
import android.view.View
import com.oplusos.systemui.common.blurability.BlurConfig
import com.oplusos.systemui.common.blurability.BlurMixConfig
import com.oplusos.systemui.common.blurability.MixColor
import com.oplusos.systemui.common.blurability.drawable.AutoBlurDrawable
import com.oplusos.systemui.common.blurability.drawable.ViewBlurProxy
import kotlin.jvm.internal.Intrinsics


class QsViewBackgroundKt {

    companion object {

        @JvmStatic
        fun updateBlurConfig(
            blurConfig: BlurConfig,
            context: Context,
            mixColor: MixColor?,
            i2: Int,
            obj: Any?
        ): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun createMixColorDrawableForQsView(
            view: View,
            mixColor: MixColor?,
            i2: Int,
            obj: Any?
        ): AutoBlurDrawable {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun createMixColorDrawableForQsView(BackgroundView: View, mixColor: MixColor?): AutoBlurDrawable {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun updateBlurConfig(blurConfig: BlurConfig, context: Context, mixColor: MixColor?): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun isMixColorSupport(qsViewInfoProvider: QsViewInfoProvider): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun isMixColorSupport(context: Context?, z: Boolean): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

        @JvmStatic
        fun isGlobalThemeApplied(context: Context): Boolean {
            throw UnsupportedOperationException("Stub!")
        }

    }

}