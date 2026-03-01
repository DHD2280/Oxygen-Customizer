package com.oplus.systemui.qs.base.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.oplusos.systemui.common.blurability.MixColor;

public interface QsStaticViewInfoProvider extends QsViewInfoProvider {
    @Override
    default Drawable getBackgroundDrawable() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    View getBackgroundView();

    @Override
    QsViewOutlineProvider getBgOutlineProvider();

    @Override
    Context getContext();

    Drawable getGlobalThemeBackgroundDrawable();

    @Override
    default String getLogTag() {
        throw new UnsupportedOperationException("Stub!");
    }

    MixColor getMixColor();

    int getNormalBackGroundColor();

    boolean isApplyGlobalTheme();
}
