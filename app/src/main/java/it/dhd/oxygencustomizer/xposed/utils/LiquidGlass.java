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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public final class LiquidGlass {

    public static final int DEFAULT_COLOR = 0x2EFFFFFF;
    public static final int ACTIVE_COLOR = 0xE6FFFFFF;

    private LiquidGlass() {
    }

    public static void setColor(View view, float radius, int color) {
        GradientDrawable fill = getFill(view.getBackground());
        if (fill != null && fill.getColor() != null && fill.getColor().getDefaultColor() == color)
            return;
        apply(view, radius, color);
    }

    public static void apply(View view, float radius, int color) {
        GradientDrawable fill = new GradientDrawable();
        fill.setShape(GradientDrawable.RECTANGLE);
        fill.setCornerRadius(radius);
        fill.setColor(color);
        view.setBackgroundTintList(null);
        Drawable rim = new RimDrawable(radius, Math.max(1f, view.getResources().getDisplayMetrics().density * 0.75f));
        Drawable blur = createBackgroundBlur(view, radius);
        Drawable content = blur != null
                ? new LayerDrawable(new Drawable[]{blur, fill, rim})
                : new LayerDrawable(new Drawable[]{fill, rim});
        view.setBackground(new GlassDrawable(content, fill));
        if (blur != null) return;
        view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                v.removeOnAttachStateChangeListener(this);
                GradientDrawable current = getFill(v.getBackground());
                if (current != fill) return;
                int currentColor = current.getColor() != null ? current.getColor().getDefaultColor() : color;
                apply(v, radius, currentColor);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
            }
        });
    }

    private static GradientDrawable getFill(Drawable background) {
        return background instanceof GlassDrawable glassDrawable ? glassDrawable.fill : null;
    }

    private static final class GlassDrawable extends DrawableWrapper {
        private final GradientDrawable fill;

        GlassDrawable(Drawable content, GradientDrawable fill) {
            super(content);
            this.fill = fill;
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
        private static final int[] COLORS = {0x66FFFFFF, 0x0DFFFFFF, 0x00FFFFFF, 0x0DFFFFFF, 0x4DFFFFFF};
        private static final float[] POSITIONS = {0f, 0.18f, 0.5f, 0.82f, 1f};
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final float radius;

        RimDrawable(float radius, float strokeWidth) {
            this.radius = radius;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(strokeWidth);
        }

        @Override
        protected void onBoundsChange(@NonNull Rect bounds) {
            float inset = paint.getStrokeWidth() / 2f;
            rect.set(bounds.left + inset, bounds.top + inset, bounds.right - inset, bounds.bottom - inset);
            paint.setShader(new LinearGradient(0, bounds.top, 0, bounds.bottom, COLORS, POSITIONS, Shader.TileMode.CLAMP));
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
