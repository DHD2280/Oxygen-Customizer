package com.oplus.systemui.qs.base.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import com.android.systemui.plugins.qs.QSIconView;
import com.oplus.systemui.qs.base.util.QsLottieUtils;

public interface QsTileViewInfoProvider extends QsViewInfoProvider {

    boolean animationsEnabled();

    int getActiveColorWithDarkMode(Context context);

    @Override
    default Drawable getBackgroundDrawable() {
        return QsViewInfoProvider.super.getBackgroundDrawable();
    }

    @Override View getBackgroundView();

    @Override QsViewOutlineProvider getBgOutlineProvider();

    @Override Context getContext();

    QSIconView getIcon();

    FrameLayout getIconWithBackground();

    @Override // com.oplus.systemui.qs.base.widget.QsViewInfoProvider
    default String getLogTag() {
        return QsViewInfoProvider.super.getLogTag();
    }

    QsLottieUtils getQsLottieController();

    default boolean getTileCanTips() {
        return false;
    }

    int getTileIconState();

    default boolean getTileType() {
        return false;
    }

    boolean isClicked();

    default boolean isHighLightForCustomizer() {
        return false;
    }

    boolean isSeparateMode();

    default void updateBackgroundMask(Drawable drawable) {
        getBackgroundView().setForeground(drawable);
    }

    default Drawable getBackgroundMaskDrawable() {
        return getBackgroundView().getForeground();
    }

    default QsViewOutlineProvider getHighLightBgOutlineProvider() {
        return getBgOutlineProvider();
    }
}
