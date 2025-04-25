package com.oplus.systemui.qs.base.widget;

import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.android.systemui.plugins.qs.QSTile;

public abstract class BaseTileViewBackground extends BaseQsViewBackground {

    public static final Interpolator ICON_COLOR_TRANSITION_INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);

    public final QsTileViewInfoProvider qsTileView;
    public final String logTag;

    public abstract void updateBackground(int i2, boolean z, boolean z2);

    public BaseTileViewBackground(QsTileViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.qsTileView = qsTileViewInfoProvider;
        this.logTag = getClass().getSimpleName();
    }

    @Override
    public void onBackgroundAttach() {
        throw new UnsupportedOperationException("Stub!");
    }


    @Override
    public void refreshViewBackground() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    public void handleStateChanged(QSTile.State state) {
        throw new UnsupportedOperationException("Stub!");
    }


}
