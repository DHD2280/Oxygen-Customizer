package com.oplus.systemui.qs.base.widget;

import android.content.Context;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewOutlineProvider;

import com.oplus.systemui.qs.base.util.OplusSmoothParams;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

public abstract class QsViewOutlineProvider extends ViewOutlineProvider {

    public final View hostView;

    public abstract void refreshSmoothRound();

    public abstract void invalidateOutline();

    public abstract void attachAttribute(BiConsumer biConsumer);

    public abstract OplusSmoothParams getSmoothRound(View view);

    public QsViewOutlineProvider(View view) {
        this.hostView = view;
    }

    public final View getHostView() {
        return this.hostView;
    }

    public abstract class QsViewBaseOutlineProvider extends QsViewOutlineProvider implements View.OnAttachStateChangeListener {

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

        @Override // com.oplus.systemui.qs.base.widget.QsViewOutlineProvider.QsViewBaseOutlineProvider, android.view.ViewOutlineProvider
        public void getOutline(View view, Outline outline) {
            throw new UnsupportedOperationException("Stub!");
        }

        @Override // com.oplus.systemui.p127qs.base.widget.QsViewOutlineProvider
        public void refreshSmoothRound() {
            throw new UnsupportedOperationException("Stub!");
        }
    }

    public static final Companion Companion = new Companion();

    public static class Companion {

        public QsViewOutlineProvider getQsViewRoundRectOutlineProvider(View view, Function<View, ?> function) {
            throw new UnsupportedOperationException("Stub!");
        }

        public QsViewOutlineProvider getQsViewRoundRectOutlineProvider(View view, boolean roundBackground, Function<View, ?> function) {
            throw new UnsupportedOperationException("Stub!");
        }

        public static /* synthetic */ QsViewOutlineProvider getQsViewRoundRectOutlineProvider$default(Companion companion, View view, boolean z, Function function, int i2, Object obj) {
            throw new UnsupportedOperationException("Stub!");
        }

        public static /* synthetic */ QsViewOutlineProvider getQsViewRoundRectOutlineProvider$default(Companion companion, View view, boolean z, Function function, int i2) {
            throw new UnsupportedOperationException("Stub!");
        }

        public static /* synthetic */ QsViewOutlineProvider getQsViewRoundRectOutlineProvider$default(Companion companion, View view, boolean z, boolean z2, Function function, int i2, Object obj) {
            throw new UnsupportedOperationException("Stub!");
        }

        public QsViewOutlineProvider getQsViewPathOutlineProvider(View view, Function0<?> f0, Function1<?, ?> f1, Function<View, ?> f) {
            throw new UnsupportedOperationException("Stub!");
        }
    }


}
