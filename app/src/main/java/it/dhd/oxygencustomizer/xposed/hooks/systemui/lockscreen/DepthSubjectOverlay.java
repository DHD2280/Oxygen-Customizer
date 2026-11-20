package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import it.dhd.oxygencustomizer.xmorph.view.DepthSubject;

public final class DepthSubjectOverlay implements DepthSubject {

    public static final DepthSubjectOverlay INSTANCE = new DepthSubjectOverlay();

    private static final Rect EMPTY = new Rect();
    private static WeakReference<View> sSource = new WeakReference<>(null);
    private static final Set<View> sTargets = Collections.newSetFromMap(new WeakHashMap<>());
    private static final int[] sSourceLocation = new int[2];
    private static final int[] sTargetLocation = new int[2];
    private static int sLastSignature = 0;

    private static final ViewTreeObserver.OnPreDrawListener sPreDraw = () -> {
        if (sTargets.isEmpty()) return true;
        int signature = signature();
        if (signature != sLastSignature) {
            sLastSignature = signature;
            invalidateTargets();
        }
        return true;
    };

    private DepthSubjectOverlay() {
    }

    public static void setSource(View source) {
        View previous = sSource.get();
        if (previous == source) return;
        if (previous != null) {
            previous.setClipBounds(null);
            previous.getViewTreeObserver().removeOnPreDrawListener(sPreDraw);
        }
        sSource = new WeakReference<>(source);
        if (source != null) source.getViewTreeObserver().addOnPreDrawListener(sPreDraw);
        updateClip();
    }

    @Override
    public boolean getDrawsWhenAllBehind() {
        return false;
    }

    @Override
    public void attach(@NonNull View target) {
        sTargets.add(target);
        updateClip();
    }

    @Override
    public void detach(@NonNull View target) {
        sTargets.remove(target);
        updateClip();
    }

    @Override
    public void draw(@NonNull Canvas canvas, @NonNull View target) {
        View source = sSource.get();
        if (source == null || source.getVisibility() != View.VISIBLE || !source.isAttachedToWindow())
            return;
        Drawable background = source.getBackground();
        if (background == null || source.getAlpha() <= 0f) return;
        View parent = (View) source.getParent();
        if (parent == null) return;
        parent.getLocationOnScreen(sSourceLocation);
        target.getLocationOnScreen(sTargetLocation);
        int width = source.getWidth();
        int height = source.getHeight();
        int save = canvas.save();
        canvas.translate(sSourceLocation[0] + source.getLeft() - sTargetLocation[0], sSourceLocation[1] + source.getTop() - sTargetLocation[1]);
        canvas.concat(source.getMatrix());
        if (source.getAlpha() < 1f)
            canvas.saveLayerAlpha(0, 0, width, height, Math.round(source.getAlpha() * 255));
        background.setBounds(0, 0, width, height);
        background.draw(canvas);
        canvas.restoreToCount(save);
    }

    private static void updateClip() {
        View source = sSource.get();
        if (source == null) return;
        source.setClipBounds(sTargets.isEmpty() ? null : EMPTY);
        invalidateTargets();
    }

    private static void invalidateTargets() {
        for (View target : new ArrayList<>(sTargets)) {
            target.invalidate();
        }
    }

    private static int signature() {
        View source = sSource.get();
        if (source == null) return 0;
        Drawable background = source.getBackground();
        int result = source.getVisibility();
        result = 31 * result + Float.floatToIntBits(source.getAlpha());
        result = 31 * result + Float.floatToIntBits(source.getScaleX());
        result = 31 * result + Float.floatToIntBits(source.getScaleY());
        result = 31 * result + System.identityHashCode(background);
        if (background != null) {
            result = 31 * result + background.getAlpha();
            if (background instanceof LayerDrawable layers) {
                for (int i = 0; i < layers.getNumberOfLayers(); i++) {
                    result = 31 * result + layers.getDrawable(i).getAlpha();
                }
            }
        }
        return result;
    }
}
