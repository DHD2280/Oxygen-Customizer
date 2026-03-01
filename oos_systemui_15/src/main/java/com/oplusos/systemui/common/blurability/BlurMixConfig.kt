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

    class BlurMixMulti(val foregroundMixColor: MixColor, val backgroundMixColor: MixColor) :
        BlurMixConfig() {
        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
        override fun equals(obj: Any?): Boolean {
            if (this === obj) {
                return true
            }
            if (obj !is BlurMixMulti) {
                return false
            }
            val blurMixMulti = obj
            return Intrinsics.areEqual(
                this.foregroundMixColor,
                blurMixMulti.foregroundMixColor
            ) && Intrinsics.areEqual(this.backgroundMixColor, blurMixMulti.backgroundMixColor)
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
        override fun hashCode(): Int {
            return (this.foregroundMixColor.hashCode() * 31) + this.backgroundMixColor.hashCode()
        }

        /* JADX DEBUG: Don't trust debug lines info. Lines numbers was adjusted: min line is 0 */
        override fun toString(): String {
            return "BlurMixMulti(foregroundMixColor=" + this.foregroundMixColor + ", backgroundMixColor=" + this.backgroundMixColor + ')'
        }
    }


}