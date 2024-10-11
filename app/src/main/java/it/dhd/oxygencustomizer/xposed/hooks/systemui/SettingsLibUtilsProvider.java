package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
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

        return (ColorStateList) callStaticMethod(UtilsClass, "getColorAttr", context, resID);
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

        try {
            return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID, 0);
        } catch (Throwable ignored) { //OOS 13
            return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID);
        }
    }

    public static int getColorAttrDefaultColor(int resID, Context context, int defValue) {
        if (UtilsClass == null) {
            return defValue;
        }
        try {
            return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID, defValue);
        } catch (Throwable throwable) {
            try {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", context, resID);
            } catch (Throwable throwable1) {
                return (int) callStaticMethod(UtilsClass, "getColorAttrDefaultColor", resID, context);
            }
        }
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
    public void updatePrefs(String... Key) {
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        UtilsClass = findClass("com.android.settingslib.Utils", lpparam.classLoader);
        CoUIColors = findClass("com.coui.appcompat.contextutil.COUIContextUtil", lpparam.classLoader);
    }
}
