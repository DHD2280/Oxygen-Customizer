package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.content.Context;
import android.graphics.Color;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class OpUtils extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;

    private static Class<?> OpUtils = null;
    private static Class<?> UtilsClass = null;
    private static Class<?> OplusChargingStrategy = null;

    public OpUtils(Context context) {
        super(context);
    }

    public static int getPrimaryColor(Context mContext) {
        if (mContext == null) return Color.WHITE;
        int colorIfNull = ResourcesCompat.getColor(mContext.getResources(), android.R.color.system_accent1_600, mContext.getTheme());
        if (OpUtils == null) return colorIfNull;
        int color;
        try {
            color = (int) callStaticMethod(OpUtils, "getThemeAccentColor", mContext, true);
        } catch (Throwable t) {
            color = colorIfNull;
        }
        return color;
    }

    public static int getChargingColor(int defaultColor) {
        if (OplusChargingStrategy == null) return defaultColor;
        try {
            return (int) callMethod(OplusChargingStrategy, "getTechnologyChargingColor", defaultColor);
        } catch (Throwable t) {
            return defaultColor;
        }
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        try {
            OpUtils = findClass("com.oplusos.systemui.util.OpUtils", lpparam.classLoader);
        } catch (Throwable t) {
            OpUtils = null;
        }

        try {
            OplusChargingStrategy = findClass("com.oplus.systemui.statusbar.pipeline.battery.ui.strategy.OplusChargingColorStrategy", lpparam.classLoader);
        } catch (Throwable t) {
            OplusChargingStrategy = null;
        }


    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
