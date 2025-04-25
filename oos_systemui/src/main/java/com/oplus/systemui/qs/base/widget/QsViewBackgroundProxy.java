package com.oplus.systemui.qs.base.widget;

import com.android.systemui.plugins.qs.QSTile;

import java.util.concurrent.atomic.AtomicReference;

public abstract class QsViewBackgroundProxy implements QsViewBackground {

    public final AtomicReference backgroundReference;
    public QsViewOutlineProvider mBgOutlineProvider;
    public final QsViewInfoProvider qsView;


    public QsViewBackgroundProxy(QsViewInfoProvider qsViewInfoProvider) {
        this.qsView = qsViewInfoProvider;
        this.backgroundReference = new AtomicReference();
    }

    public final QsViewOutlineProvider ensureBgOutlineProvider() {
        throw new UnsupportedOperationException("Stub!");
    }

    public abstract QsViewBackground getTargetQsViewBackground();

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    public void onBackgroundAttach() {
        switchBackgroundTo(getTargetQsViewBackground());
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    public void refreshViewBackground() {
        switchBackgroundTo(getTargetQsViewBackground()).refreshViewBackground();
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    public void handleStateChanged(QSTile.State state) {
        switchBackgroundTo(getTargetQsViewBackground()).handleStateChanged(state);
    }

    public synchronized QsViewBackground switchBackgroundTo(QsViewBackground qsViewBackground) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    public void onBackgroundDetach() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewBackground
    public void onClick() {
        throw new UnsupportedOperationException("Stub!");
    }


}
