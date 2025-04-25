package com.oplus.posteffect;

import android.graphics.Rect;

public final class BlurParam {

    public int blendColorA;
    public int blendColorB;
    public int blendMode;
    public final Rect blurArea;
    public int blurRadius;
    public int blurType;
    public int fgBlendColorA;
    public int fgBlendColorB;
    public int fgBlendMode;
    public int rotation;
    public float zoomScale;

    public BlurParam(int i2, int i3, int i4, int i5, int i6, int i7, int i8, int i9, Rect rect, float f2, Wrapping wrapping, int i10) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getBlendMode() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlendMode(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getBlendColorA() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlendColorA(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getBlendColorB() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlendColorB(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getFgBlendMode() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setFgBlendMode(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getFgBlendColorA() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setFgBlendColorA(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getFgBlendColorB() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setFgBlendColorB(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getBlurRadius() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlurRadius(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final int getBlurType() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlurType(int i2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public enum Wrapping {
        REPEAT(0),
        STRETCH(1),
        MIRRORED_REPEAT(2),
        BLACK(3);

        private final int value;

        Wrapping(int i2) {
            this.value = i2;
        }

        public final int getValue() {
            return this.value;
        }
    }


}
