package com.oplusos.systemui.common.blurability.drawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class AutoBlurDrawable extends Drawable implements Drawable.Callback {

    public int blurColor;
    public final Drawable defaultDrawable;
    public final ViewBlurProxy viewBlurProxy;

    public AutoBlurDrawable(ViewBlurProxy viewBlurProxy, Drawable drawable) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final ViewBlurProxy getViewBlurProxy() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public int getOpacity() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void invalidateDrawable(@NonNull Drawable who) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void scheduleDrawable(@NonNull Drawable who, @NonNull Runnable what, long when) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void unscheduleDrawable(@NonNull Drawable who, @NonNull Runnable what) {
        throw new UnsupportedOperationException("Stub!");
    }


}
