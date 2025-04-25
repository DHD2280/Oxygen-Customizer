package com.oplus.systemui.qs.base.widget;

import android.graphics.drawable.GradientDrawable;

public abstract class BaseQsViewBackground implements QsViewBackground {

    public final QsViewInfoProvider qsView;

    public abstract QsViewOutlineProvider getBgOutlineProvider();

    public BaseQsViewBackground(QsViewInfoProvider qsViewInfoProvider) {
        this.qsView = qsViewInfoProvider;
    }

    public GradientDrawable createOrUpdateGradientDrawable(GradientDrawable gradientDrawable) {
        throw new UnsupportedOperationException("Stub!");
    }



}
