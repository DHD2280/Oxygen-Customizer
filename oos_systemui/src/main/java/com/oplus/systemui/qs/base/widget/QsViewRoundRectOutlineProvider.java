package com.oplus.systemui.qs.base.widget;

import android.graphics.Outline;
import android.graphics.Path;
import android.view.View;

import com.oplus.systemui.qs.base.util.OplusSmoothParams;

import java.util.function.Function;

public final class QsViewRoundRectOutlineProvider extends QsViewBaseOutlineProvider {
    public Path path;

    public QsViewRoundRectOutlineProvider(View view, Function function) {
        super(view, function);
        this.path = new Path();
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewOutlineProvider
    public OplusSmoothParams getSmoothRound(View view) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    // com.oplus.systemui.qs.base.widget.QsViewOutlineProvider.QsViewBaseOutlineProvider, android.view.ViewOutlineProvider
    public void getOutline(View view, Outline outline) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewOutlineProvider
    public void refreshSmoothRound() {
        throw new UnsupportedOperationException("Stub!");
    }
}
