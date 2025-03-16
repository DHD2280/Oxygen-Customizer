package it.dhd.oxygencustomizer.utils;

import android.graphics.Color;

public class ColorUtils {

    // Adjust the alpha value of a color
    public static int adjustAlpha(int color, float factor) {
        int alpha = (int) (Color.alpha(color) * factor);
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    // Darken or lighten a color
    public static int adjustBrightness(int color, float factor) {
        int r = (int) (Color.red(color) * factor);
        int g = (int) (Color.green(color) * factor);
        int b = (int) (Color.blue(color) * factor);
        return Color.argb(Color.alpha(color), Math.min(r, 255), Math.min(g, 255), Math.min(b, 255));
    }

    public static int adjustColorForPressed(int baseColor, float factor) {
        float[] hsv = new float[3];
        Color.colorToHSV(baseColor, hsv);
        hsv[2] *= factor;
        return Color.HSVToColor(hsv);
    }

    // Dark Shadow Theme Util
    public static int adjustColor(int color, float percent) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        r = (int) Math.min(255, Math.max(0, r + (r * percent / 100)));
        g = (int) Math.min(255, Math.max(0, g + (g * percent / 100)));
        b = (int) Math.min(255, Math.max(0, b + (b * percent / 100)));

        return Color.rgb(r, g, b);
    }

}
