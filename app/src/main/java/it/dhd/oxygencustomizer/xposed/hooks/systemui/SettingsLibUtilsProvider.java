package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class SettingsLibUtilsProvider extends XposedMods {
    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static Class<?> UtilsClass = null;
    private static Class<?> CoUIColors = null;

    public SettingsLibUtilsProvider(Context context) {
        super(context);
    }

    public static ColorStateList getColorAttr(int resID, Context context) {
        if (UtilsClass == null) return null;
        try {
            return (ColorStateList) callStaticMethod(UtilsClass, "getColorAttr", context, resID);
        } catch (Throwable ignored) {
            return (ColorStateList) callStaticMethod(UtilsClass, "getColorAttr", resID, context);
        }
    }

    public static int getColorStateListDefaultColor(Context context, int resID) {
        if (UtilsClass == null) return 0;

        return (int) callStaticMethod(UtilsClass, "getColorStateListDefaultColor", context, resID);
    }

    public static int getColorErrorDefaultColor(Context context) {
        if (CoUIColors == null) return 0;

        return (int) callStaticMethod(CoUIColors, "getColorErrorDefaultColor", context);
    }

    public static int getColorAttrDefaultColor(Context context, int resID) {
        if (UtilsClass == null) return 0;

        Object[][] argsList = {
                {resID, 0, context},                  // OOS15.0.1
                {context, resID, 0},                  // OOS13+
                {context, resID}                      // fallback
        };

        for (Object[] args : argsList) {
            try {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", args);
            } catch (Throwable ignored) {
                // try next
            }
        }

        return 0;
    }

    public static int getColorAttrDefaultColor(int resID, Context context, int defValue) {
        if (UtilsClass == null) {
            return defValue;
        }

        Object[][] argsList = {
                {context, resID, defValue},     // OOS15.0.1
                {context, resID},               // OOS13+
                {resID, context}                // fallback
        };

        for (Object[] args : argsList) {
            try {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", args);
            } catch (Throwable ignored) {
                // try next
            }
        }

        return 0;
    }

    public static int getColorAttrDefaultColor(int resID, Context context) {
        if (UtilsClass == null) return 0;

        try {
            return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", resID, context);
        } catch (Throwable throwable) {
            try {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID);
            } catch (Throwable throwable1) {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID, 0);
            }
        }
    }

    public static int getThemeAttr(Context context, int attr) {
        return getThemeAttr(context, attr, 0);
    }

    public static int getThemeAttr(Context context, int attr, int defaultValue) {
        if (UtilsClass == null) return 0;
        TypedArray ta = context.obtainStyledAttributes(new int[]{attr});
        int theme = ta.getResourceId(0, defaultValue);
        ta.recycle();
        return theme;
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        UtilsClass = findClass("com.android.settingslib.Utils", PRParam.getClassLoader());
        CoUIColors = findClass("com.coui.appcompat.contextutil.COUIContextUtil", PRParam.getClassLoader());
    }
}
