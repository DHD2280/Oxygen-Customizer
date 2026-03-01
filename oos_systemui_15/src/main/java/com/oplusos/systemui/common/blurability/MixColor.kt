package com.oplusos.systemui.common.blurability

class MixColor(val mode: Int, val topLayerColor: Int, val bottomLayerColor: Int) {
    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj !is MixColor) {
            return false
        }
        val mixColor = obj
        return this.mode == mixColor.mode && this.topLayerColor == mixColor.topLayerColor && this.bottomLayerColor == mixColor.bottomLayerColor
    }

    override fun hashCode(): Int {
        return (((Integer.hashCode(this.mode) * 31) + Integer.hashCode(this.topLayerColor)) * 31) + Integer.hashCode(
            this.bottomLayerColor
        )
    }

    override fun toString(): String {
        return "MixColor(mode=" + this.mode + ", topLayerColor=" + this.topLayerColor + ", bottomLayerColor=" + this.bottomLayerColor + ')'
    }
}
