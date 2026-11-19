package it.dhd.oxygencustomizer.xposed.views.nowbar;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.View;

import androidx.core.graphics.ColorUtils;

import it.dhd.oxygencustomizer.xposed.utils.LiquidGlass;

public final class NowBarBackground {

    private static final float RADIUS = 100f;
    private static final float RIM_LIGHTEN = 0.5f;
    private static final float MIN_RIM_SATURATION = 0.2f;
    private static final float MIN_RIM_LIGHTNESS = 0.1f;

    private static boolean sGlass = false;
    private static int sTint = 40;
    private static boolean sTintRim = false;

    private NowBarBackground() {
    }

    public static boolean update(boolean glass, int tint, boolean tintRim) {
        if (sGlass == glass && sTint == tint && sTintRim == tintRim) return false;
        sGlass = glass;
        sTint = tint;
        sTintRim = tintRim;
        return true;
    }

    public static void apply(View view, int... colors) {
        applyWithRim(view, rimColor(colors[colors.length / 2]), colors);
    }

    public static void applyWithRim(View view, int rim, int... colors) {
        if (sGlass) {
            int[] tinted = new int[colors.length];
            for (int i = 0; i < colors.length; i++) {
                tinted[i] = ColorUtils.setAlphaComponent(colors[i], Math.round(Color.alpha(colors[i]) * sTint / 100f));
            }
            LiquidGlass.setColors(view, RADIUS, tinted);
            LiquidGlass.setRimColor(view, sTintRim ? rim : Color.TRANSPARENT);
            return;
        }
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(RADIUS);
        if (colors.length == 1) {
            background.setColor(colors[0]);
        } else {
            background.setColors(colors);
            background.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        }
        view.setBackground(background);
    }

    private static int rimColor(int color) {
        float[] hsl = new float[3];
        ColorUtils.colorToHSL(color, hsl);
        if (hsl[1] < MIN_RIM_SATURATION || hsl[2] < MIN_RIM_LIGHTNESS) return Color.TRANSPARENT;
        return ColorUtils.blendARGB(color | 0xFF000000, Color.WHITE, RIM_LIGHTEN);
    }
}
