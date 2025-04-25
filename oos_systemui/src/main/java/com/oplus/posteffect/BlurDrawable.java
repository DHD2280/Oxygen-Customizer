package com.oplus.posteffect;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class BlurDrawable extends Drawable implements IBlurable {

    @Override
    public void draw(@NonNull Canvas canvas) {

    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public BlurParam getBlurParams() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void setBlurParams(BlurParam blurParam) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setBlurParams(BlurParam blurParam, ForegroundBlurParam foregroundBlurParam) {
        throw new UnsupportedOperationException("Stub!");
    }

    private final void setForeColorParams(ForegroundBlurParam foregroundBlurParam) {
        throw new UnsupportedOperationException("Stub!");
    }



}
