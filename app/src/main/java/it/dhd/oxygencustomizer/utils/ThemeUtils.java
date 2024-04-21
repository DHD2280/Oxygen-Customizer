package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;

public class ThemeUtils {

    /**
     * Get the primary color from the theme
     * @return @ColorInt The primary color
     */
    public static @ColorInt int getPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getOnPrimaryColor(Context context) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getBackgroundColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getAppContext().getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getOnBackgroundColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getAppContext().getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getColorSurfaceHighest() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getAppContext().getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHighest, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getColorSurfaceHigh() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getAppContext().getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainerHigh, typedValue, true);
        return typedValue.data;
    }

    public static @ColorInt int getColorSurfaceContainer() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getAppContext().getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.colorSurfaceContainer, typedValue, true);
        return typedValue.data;
    }

    public static int getColorResCompat(Context context, @AttrRes int id) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = context.getTheme();
        theme.resolveAttribute(id, typedValue, false);
        TypedArray arr = context.obtainStyledAttributes(typedValue.data, new int[]{id});
        @ColorInt int color = arr.getColor(0, -1);
        arr.recycle();
        return color;
    }

}
