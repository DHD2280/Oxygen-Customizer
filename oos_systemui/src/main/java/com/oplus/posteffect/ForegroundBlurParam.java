package com.oplus.posteffect;

public final class ForegroundBlurParam {

    public final int blendColorA;
    public final int blendColorB;
    public final int blendMode;


    public ForegroundBlurParam(int i2, int i3, int i4) {
        this.blendMode = i2;
        this.blendColorA = i3;
        this.blendColorB = i4;
    }

    public final int getBlendMode() {
        return this.blendMode;
    }

    public final int getBlendColorA() {
        return this.blendColorA;
    }

    public final int getBlendColorB() {
        return this.blendColorB;
    }


}
