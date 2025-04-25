package com.oplus.systemui.qs.base.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.oplusos.systemui.common.blurability.drawable.AutoBlurDrawable;

public interface QsViewInfoProvider {
    View getBackgroundView();

    QsViewOutlineProvider getBgOutlineProvider();

    Context getContext();

    boolean isGaussBlurDisabled();

    default String getLogTag() {
        throw new UnsupportedOperationException("Stub!");
    }

    default void updateBackground(Drawable drawable) {
        throw new UnsupportedOperationException("Stub!");
    }

    default Drawable getBackgroundDrawable() {
        throw new UnsupportedOperationException("Stub!");
    }

    default void updateBgOutlineProvider(QsViewOutlineProvider qsViewOutlineProvider) {
        throw new UnsupportedOperationException("Stub!");
    }
}
