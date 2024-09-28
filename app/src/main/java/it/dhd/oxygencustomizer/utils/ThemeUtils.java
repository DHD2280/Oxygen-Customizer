package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.TypedValue;

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

    public static @ColorInt int getBackgroundColor() {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getAppContext().getTheme();
        theme.resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true);
        return typedValue.data;
    }

    public static int getAttrColor(Context context, int colorAttr) {
        TypedArray obtainStyledAttributes = context.getTheme().obtainStyledAttributes(new int[]{colorAttr});
        int color = obtainStyledAttributes.getColor(0, 0);
        obtainStyledAttributes.recycle();
        return color;

    }
}
