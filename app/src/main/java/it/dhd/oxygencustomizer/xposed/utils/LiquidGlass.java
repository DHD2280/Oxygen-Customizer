package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;

import android.content.res.ColorStateList;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class LiquidGlass {

    public static final int DEFAULT_COLOR = 0x2EFFFFFF;
    public static final int ACTIVE_COLOR = 0xE6FFFFFF;
    public static final float FILL_ALPHA = Color.alpha(DEFAULT_COLOR) / 255f;
    private static final int[] RIM_COLORS = {0x66FFFFFF, 0x0DFFFFFF, 0x00FFFFFF, 0x0DFFFFFF, 0x4DFFFFFF};
    private static final float[] RIM_POSITIONS = {0f, 0.18f, 0.5f, 0.82f, 1f};
    private static final int[] TINTED_RIM_ALPHAS = {0xE6, 0x66, 0x26, 0x66, 0xCC};

    private LiquidGlass() {
    }

    public static void drawRim(View view, Canvas canvas) {
        if (view.getBackground() instanceof GlassDrawable glassDrawable)
            glassDrawable.rim.draw(canvas);
    }

    public static void setRimColor(View view, int color) {
        if (!(view.getBackground() instanceof GlassDrawable glassDrawable)) return;
        glassDrawable.rim.setTint(color);
        view.invalidate();
    }

    public static void setColor(View view, float radius, int color) {
        GradientDrawable fill = getFill(view.getBackground());
        if (fill != null && fill.getColor() != null && fill.getColor().getDefaultColor() == color)
            return;
        apply(view, radius, color);
    }

    public static void setColors(View view, float radius, int[] colors) {
        GradientDrawable fill = getFill(view.getBackground());
        if (fill != null && fill.getCornerRadius() == radius) {
            fillColors(fill, colors);
            return;
        }
        apply(view, radius, colors);
    }

    public static void apply(View view, float radius, int color) {
        apply(view, radius, new int[]{color});
    }

    public static void apply(View view, float radius, int[] colors) {
        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(radius);
        fillColors(fill, colors);
        view.setBackgroundTintList(null);
        RimDrawable rim = new RimDrawable(radius, Math.max(1f, view.getResources().getDisplayMetrics().density * 0.75f));
        int alpha = 255;
        if (view.getBackground() instanceof GlassDrawable previous) {
            rim.setTint(previous.rim.tint);
            alpha = previous.alpha;
        }
        Drawable blur = createBackgroundBlur(view, radius);
        Drawable content = blur != null
                ? new LayerDrawable(new Drawable[]{blur, fill, rim})
                : new LayerDrawable(new Drawable[]{fill, rim});
        GlassDrawable glass = new GlassDrawable(content, fill, rim);
        if (alpha != 255) glass.setAlpha(alpha);
        view.setBackground(glass);
        if (blur != null) return;
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.removeOnAttachStateChangeListener(this);
                GradientDrawable current = getFill(v.getBackground());
                if (current != fill) return;
                apply(v, radius, currentColors(current, colors));
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });
    }

    public static Shader rimShader(float top, float bottom) {
        return new LinearGradient(0, top, 0, bottom, RIM_COLORS, RIM_POSITIONS, Shader.TileMode.CLAMP);
    }

    private static Shader rimShader(float top, float bottom, int tint) {
        if (tint == Color.TRANSPARENT) return rimShader(top, bottom);
        int[] colors = new int[TINTED_RIM_ALPHAS.length];
        for (int i = 0; i < colors.length; i++) {
            colors[i] = Color.argb(TINTED_RIM_ALPHAS[i], Color.red(tint), Color.green(tint), Color.blue(tint));
        }
        return new LinearGradient(0, top, 0, bottom, colors, RIM_POSITIONS, Shader.TileMode.CLAMP);
    }

    public static void setGlassAlpha(View view, float alpha) {
        int value = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255);
        if (view.getBackground() instanceof GlassDrawable glass && glass.getAlpha() != value)
            glass.setAlpha(value);
        if (view instanceof ViewGroup group) {
            for (int i = 0; i < group.getChildCount(); i++)
                setGlassAlpha(group.getChildAt(i), alpha);
        }
    }

    private static void fillColors(GradientDrawable fill, int[] colors) {
        if (colors.length == 1) {
            fill.setColor(colors[0]);
            return;
        }
        fill.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        fill.setColors(colors);
    }

    private static int[] currentColors(GradientDrawable fill, int[] fallback) {
        if (fill.getColors() != null) return fill.getColors();
        if (fill.getColor() != null) return new int[]{fill.getColor().getDefaultColor()};
        return fallback;
    }

    private static GradientDrawable getFill(Drawable background) {
        return background instanceof GlassDrawable glassDrawable ? glassDrawable.fill : null;
    }

    private static final class GlassDrawable extends DrawableWrapper {
        private final GradientDrawable fill;
        private final RimDrawable rim;
        private int alpha = 255;

        GlassDrawable(Drawable content, GradientDrawable fill, RimDrawable rim) {
            super(content);
            this.fill = fill;
            this.rim = rim;
        }

        @Override
        public void setAlpha(int alpha) {
            this.alpha = alpha;
            super.setAlpha(alpha);
        }

        @Override
        public int getAlpha() {
            return alpha;
        }

        @NonNull
        @Override
        public Drawable mutate() {
            return this;
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public void setTintList(@Nullable ColorStateList tint) {
        }

        @Override
        public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
        }

        @Override
        public void setTintBlendMode(@Nullable BlendMode blendMode) {
        }
    }

    private static final class RimDrawable extends Drawable {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final float radius;
        private int tint = Color.TRANSPARENT;

        RimDrawable(float radius, float strokeWidth) {
            this.radius = radius;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
        }

        @Override
        public void setTint(int tint) {
            if (this.tint == tint) return;
            this.tint = tint;
            Rect bounds = getBounds();
            if (!bounds.isEmpty()) paint.setShader(rimShader(bounds.top, bounds.bottom, tint));
            invalidateSelf();
        }

        @Override
        protected void onBoundsChange(@NonNull Rect bounds) {
            float inset = paint.getStrokeWidth() / 2f;
            rect.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
            paint.setShader(rimShader(bounds.top, bounds.bottom, tint));
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            if (rect.isEmpty()) return;
            float r = Math.min(radius, Math.min(rect.width(), rect.height()) / 2f);
            canvas.drawRoundRect(rect, r, r, paint);
        }

        @Override
        public void setAlpha(int alpha) {
            paint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }

    private static Drawable createBackgroundBlur(View view, float radius) {
        try {
            Object viewRoot = callMethod(view, "getViewRootImpl");
            if (viewRoot == null) return null;
            Drawable blur = (Drawable) callMethod(viewRoot, "createBackgroundBlurDrawable");
            callMethod(blur, "setBlurRadius", dp2px(view.getContext(), 25));
            callMethod(blur, "setCornerRadius", radius);
            callMethod(blur, "setColor", Color.TRANSPARENT);
            return blur;
        } catch (Throwable t) {
            return null;
        }
    }
}
