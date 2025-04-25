package com.oplusos.systemui.common.blurability

import kotlin.jvm.internal.Intrinsics


abstract class BlurMixConfig {

    var mirrorScale: Float

    constructor() {
        this.mirrorScale = 1.0f
    }

    class BlurMixSingle(val mixColor: MixColor) : BlurMixConfig() {
        override fun equals(obj: Any?): Boolean {
            if (this === obj) {
                return true
            }
            return (obj is BlurMixSingle) && Intrinsics.areEqual(this.mixColor, obj.mixColor)
        }

        override fun hashCode(): Int {
            return this.mixColor.hashCode()
        }

        override fun toString(): String {
            return "BlurMixSingle(mixColor=" + this.mixColor + ')'
        }
    }

}