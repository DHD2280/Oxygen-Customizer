package com.oplusos.systemui.common.util

import android.content.Context
import com.oplusos.systemui.common.blurability.BlurMixConfig
import com.oplusos.systemui.common.blurability.MixColor

public abstract class NotifiAndQsPlatformBlurEx {

    companion object {
        @JvmStatic
        fun panelElementPlatformMixConfig(context: Context, mixColor: MixColor) : BlurMixConfig {
            throw UnsupportedOperationException("Stub!")
        }
    }





}