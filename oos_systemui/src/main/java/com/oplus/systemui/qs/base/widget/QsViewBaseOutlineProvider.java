package com.oplus.systemui.qs.base.widget;

import android.content.Context;
import android.graphics.Outline;
import android.view.View;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class QsViewBaseOutlineProvider extends QsViewOutlineProvider implements View.OnAttachStateChangeListener {

    public List attributeReferences;
    public final Runnable invalidateOutlineRunnable;
    public final View.OnLayoutChangeListener layoutChangeListener;
    public final Function outlineShapeProvider;
    public final AtomicReference outlineShapeReference;

    public QsViewBaseOutlineProvider(final View view, Function function) {
        super(view);
        throw new UnsupportedOperationException("Stub!");
    }

    private final Context getContext() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final AtomicReference<Object> getOutlineShapeReference() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewOutlineProvider
    public void invalidateOutline() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final List ensureAttributeReferences() {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // com.oplus.systemui.p127qs.base.widget.QsViewOutlineProvider
    public synchronized void attachAttribute(BiConsumer biConsumer) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewAttachedToWindow(View view) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // android.view.View.OnAttachStateChangeListener
    public void onViewDetachedFromWindow(View view) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override // android.view.ViewOutlineProvider
    public void getOutline(View view, Outline outline) {
        throw new UnsupportedOperationException("Stub!");
    }
}
