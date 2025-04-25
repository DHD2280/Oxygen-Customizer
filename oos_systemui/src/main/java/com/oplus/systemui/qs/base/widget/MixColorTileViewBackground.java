package com.oplus.systemui.qs.base.widget;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.Pair;

import com.oplusos.systemui.common.blurability.drawable.AutoBlurDrawable;

public abstract class MixColorTileViewBackground extends BaseTileViewBackground {

    public final QsTileViewInfoProvider qsTileView;

    public MixColorTileViewBackground(QsTileViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.qsTileView = qsTileViewInfoProvider;
    }

    public final Context getViewContext() {
        return this.qsTileView.getContext();
    }

    private final ValueAnimator initIconBgAnimator(GradientDrawable gradientDrawable, int i2, int i3, long j2) {
        throw new UnsupportedOperationException("Stub!");
    }

    private final ValueAnimator initDarkThemeIconBgAnimator(long j2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void cancelIconBgAnimator() {
        throw new UnsupportedOperationException("Stub!");
    }

    private final AutoBlurDrawable ensureAutoBlurDrawable() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final GradientDrawable getMaskDrawable() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void setMaskDrawable(GradientDrawable gradientDrawable) {
        throw new UnsupportedOperationException("Stub!");
    }

    public GradientDrawable ensureMaskDrawable() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void updateMixColorBackgroundWithAnimation(Context context, int i2, int i3) {
        throw new UnsupportedOperationException("Stub!");
    }

    private final Pair<Integer, Integer> obtainColorForBgState(boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final Pair<Integer, Integer> calculateColorWithAnimation(float f2, Pair pair) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void updateBackground(int i2, boolean z, boolean z2) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void toActiveBackground(boolean z) {
        throw new UnsupportedOperationException("Stub!");

    }

    public final void toInactiveBackground(boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void toInactiveDarkThemeBackground(boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }
}
