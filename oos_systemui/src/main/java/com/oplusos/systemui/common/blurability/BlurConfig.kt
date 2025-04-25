package com.oplusos.systemui.common.blurability

import java.lang.Boolean
import java.lang.Float


class BlurConfig(
    var blurRadius: Int,
    var blurColor: Int,
    var leftTopCornerRadius: Float,
    var leftBottomCornerRadius: Float,
    var rightTopCornerRadius: Float,
    var rightBottomCornerRadius: Float,
    var radiusWeight: Float?,
    var enableStaticBlurCorner: Boolean,
    var enableMotionAmountWithAlphaToUser: Boolean,
    val isFullScreen: Boolean,
    var platformMixConfig: BlurMixConfig,
    blurMixSingle: BlurMixConfig.BlurMixSingle?,
    windowBlurConfig: WindowBlurConfig?
) {
    var cornerRadius = 0f
    var motionBlurMixConfig: BlurMixConfig.BlurMixSingle? = null
    var windowBlurConfig: WindowBlurConfig? = null

    fun copy(
        i2: Int,
        i3: Int,
        f2: Float,
        f3: Float,
        f4: Float,
        f5: Float,
        f6: Float?,
        z: Boolean,
        z2: Boolean,
        z3: Boolean,
        blurMixConfig: BlurMixConfig,
        blurMixSingle: BlurMixConfig.BlurMixSingle?,
        windowBlurConfig: WindowBlurConfig?
    ): BlurConfig {
        throw UnsupportedOperationException("Stub!")
    }


    override fun toString(): String {
        throw UnsupportedOperationException("Stub!")
    }

    init {
        this.motionBlurMixConfig = blurMixSingle
        this.windowBlurConfig = windowBlurConfig
    }

}
