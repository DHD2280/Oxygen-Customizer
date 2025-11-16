package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.hookAllConstructors;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class OpUtils extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    public static Class<?> QsColorUtil = null;
    private static Class<?> OpUtils = null;
    private static Class<?> QSFragmentHelper = null;
    private Context mSysuiAppContext = null;
    private static Object LunarHelper = null;
    private static Object mAffordanceSqlHelper = null;
    public static Class<?> COUISeekBar = null;
    public static Class<?> COUISeekBarListener = null;

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

    public static boolean isMediaIconNeedUseLightColor(Context context) {
        if (QsColorUtil == null) return false;
        try {
            String method = Build.VERSION.SDK_INT >= 35
                    ? "isIconNeedUseLightColor"
                    : "isMediaIconNeedUseLightColor";

            Object[] params = Build.VERSION.SDK_INT >= 36
                    ? new Object[]{ context, false }
                    : new Object[]{ context };

            return (boolean) callStaticMethod(QsColorUtil, method, params);
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean isNeedSeparateDarkThemeColor(Context context) {
        boolean isNightMode = (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        if (QsColorUtil == null) return isNightMode;
        try {
            Object[] params = Build.VERSION.SDK_INT >= 36
                    ? new Object[]{ context, false }
                    : new Object[]{ context };
            return (boolean) callStaticMethod(QsColorUtil, "isNeedSeparateDarkThemeColor", params);
        } catch (Throwable t) {
            return isNightMode;
        }
    }

    public static int getIconLightColor() {
        if (QsColorUtil == null) return Color.WHITE;
        try {
            return getStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_LIGHT_COLOR");
        } catch (Throwable t) {
            return Color.WHITE;
        }
    }

    public static int getTileActiveColor(Context context) {
        if (QSFragmentHelper == null) return getPrimaryColor(context);
        try {
            Object QSFragmentHelperObj = callStaticMethod(QSFragmentHelper, "getInstance");
            return (int) callMethod(QSFragmentHelperObj, "getActiveColorWithDarkMode", context);
        } catch (Throwable t) {
            return getPrimaryColor(context);
        }
    }

    public static String getLunarDate() {
        if (LunarHelper == null) return "Err";
        try {
            return (String) callMethod(LunarHelper, "getDateToString", System.currentTimeMillis());
        } catch (Throwable t) {
            return "Err";
        }
    }

    public static boolean isLeftAffordanceHidden(Context context, boolean placeHolder) {
        boolean isLeftHidden;
        try {
            Object affordance = callMethod(mAffordanceSqlHelper, "getInstance");
            String left = (String) callMethod(affordance, "queryStartSelection", context);
            isLeftHidden = !TextUtils.isEmpty(left) && left.equals("none");
        } catch (Throwable ignored) {
            return placeHolder;
        }
        return isLeftHidden;
    }

    @Override
    public void updatePrefs(String... Key) {}

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        OpUtils = findClassIfExists("com.oplusos.systemui.util.OpUtils", lpparam.classLoader);

        QsColorUtil = findClassInArray(lpparam,
                "com.oplus.systemui.qs.base.util.QsColorUtil" /* OOS15 */,
                "com.oplus.systemui.qs.util.QsColorUtil" /* OOS13-14 */);

        QSFragmentHelper = findClassInArray(lpparam,
                "com.oplus.systemui.qs.helper.QSFragmentHelper",
                "com.oplusos.systemui.qs.helper.QSFragmentHelper");

        Class<?> LunarHelperClass = findClassInArray(lpparam,
                "com.oplus.systemui.keyguard.clock.LunarHelper", // OOS14
                "com.oplusos.systemui.keyguard.clock.LunarHelper" /* OOS13 */);

        if (LunarHelperClass != null) {
            if (Build.VERSION.SDK_INT >= 35) {
                Object LunarHelperCompanion = getStaticObjectField(LunarHelperClass, "Companion");
                ReflectedClass StatusBarHelper = ReflectedClass.of("com.oplus.systemui.statusbar.util.StatusBarHelper", lpparam.classLoader);
                StatusBarHelper
                        .afterConstruction()
                        .run(param -> {
                            mSysuiAppContext = (Context) param.args[0];
                            LunarHelper = callMethod(LunarHelperCompanion, "getInstance", mSysuiAppContext);
                        });
            } else {
                hookAllConstructors(LunarHelperClass, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (LunarHelper == null) LunarHelper = param.thisObject;
                    }
                });
            }
        }

        try {
            ReflectedClass AffordanceSqlHelper = ReflectedClass.of("com.oplus.systemui.keyguard.domain.quickaffordance.AffordanceSqlHelper");
            mAffordanceSqlHelper = getStaticObjectField(AffordanceSqlHelper.getClazz(), "Companion");
        } catch (Throwable ignored) {}

        try {
            COUISeekBar = findClass("com.coui.appcompat.seekbar.COUISeekBar", lpparam.classLoader);
        } catch (Throwable ignored) {}

        try {
            COUISeekBarListener = findClass(
                    "com.coui.appcompat.seekbar.COUISeekBar$OnSeekBarChangeListener",
                    lpparam.classLoader
            );
        } catch (Throwable ignored) {}

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
