package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static de.robv.android.xposed.XposedHelpers.newInstance;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.isNeedSeparateDarkThemeColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class OpDrawableUtils extends XposedMods {

    @SuppressLint("StaticFieldLeak")
    private static OpDrawableUtils instance = null;

    private static final String listenPackage = SYSTEM_UI;
    public static Class<?> MixColor = null;
    public static Class<?> BlurConfig = null;
    public static Class<?> OplusQsSmoothRoundUtil = null;
    public static Class<?> BlurMixConfig = null;
    public static Class<?> BlurMixSingle = null;
    public static Class<?> BlurMixMulti = null;
    public static Class<?> ViewBlurProxy = null;
    public static Class<?> AutoBlurDrawable = null;
    private static Object BlurTypeMotionInstance = null;


    public OpDrawableUtils(Context context) {
        super(context);
        instance = this;
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        if (Build.VERSION.SDK_INT < 35) return; // Only OOS15

        try {
            MixColor = ReflectedClass.of("com.oplusos.systemui.common.blurability.MixColor").getClazz();
        } catch (Throwable ignored) {}

        try {
            BlurConfig = ReflectedClass.of("com.oplusos.systemui.common.blurability.BlurConfig").getClazz();
        } catch (Throwable ignored) {}

        try {
            BlurMixConfig = ReflectedClass.of("com.oplusos.systemui.common.blurability.BlurMixConfig").getClazz();
        } catch (Throwable ignored) {}

        try {
            BlurMixMulti = ReflectedClass.of("com.oplusos.systemui.common.blurability.BlurMixConfig$BlurMixMulti").getClazz();
        } catch (Throwable ignored) {}

        try {
            BlurMixSingle = ReflectedClass.of("com.oplusos.systemui.common.blurability.BlurMixConfig$BlurMixSingle").getClazz();
        } catch (Throwable ignored) {}

        try {
            OplusQsSmoothRoundUtil = ReflectedClass.of("com.oplus.systemui.p127qs.base.util.OplusQsSmoothRoundUtil").getClazz();
        } catch (Throwable ignored) {}

        try {
            ViewBlurProxy = ReflectedClass.of("com.oplusos.systemui.common.blurability.ViewBlurProxy").getClazz();
            ReflectedClass BlurTypeMotion = ReflectedClass.of("com.oplusos.systemui.common.blurability.ViewBlurProxy$BlurType$BlurTypeMotion");
            BlurTypeMotionInstance = getStaticObjectField(BlurTypeMotion.getClazz(), "INSTANCE");
        } catch (Throwable ignored) {}

        try {
            AutoBlurDrawable = ReflectedClass.of("com.oplusos.systemui.common.blurability.drawable.AutoBlurDrawable").getClazz();
        } catch (Throwable ignored) {}

    }

    public static Drawable getNewAutoBlurDrawable(View view, Drawable fallbackDrawable, int blurRadius, int backgroundColor, int foregroundColor, float cornerRadius) {
        try {
            Object blurConfig = newInstance(BlurConfig, (int) 0, (int) 0, (float) 0.0f, (float) 0.0f, (float) 0.0f, (float) 0.0f, (Float) null, false, false, false, (Object) null, (Object) null, (Object) null, (int) 8191, (Object) null);
            callMethod(blurConfig, "setBlurRadius", blurRadius); //800);
            callMethod(blurConfig, "setCornerRadius", cornerRadius);
            callMethod(blurConfig, "setMotionBlurMixConfig", newInstance(BlurMixSingle, newInstance(MixColor, SystemUtils.isDarkMode() ? 4 : 5, backgroundColor, foregroundColor)));
            Object viewBlurProxy = newInstance(ViewBlurProxy, view, blurConfig, null);
            callMethod(viewBlurProxy, "setBlurType", BlurTypeMotionInstance);
            return (Drawable) newInstance(AutoBlurDrawable, viewBlurProxy, fallbackDrawable);
        } catch (Throwable t) {
            XposedBridge.log("getNewAutoBlurDrawable error: \n" + Log.getStackTraceString(t));
            return fallbackDrawable;
        }
    }

    public static Drawable getNewAutoBlurDrawable(Context context, View view, Drawable fallbackDrawable) {
        return getNewAutoBlurDrawable(view, fallbackDrawable, 800, Color.parseColor("#1a525252"), Color.parseColor("#40262626"), (float) dp2px(context, 24));
    }

    public static Drawable getNewAutoBlurDrawable(View view, Drawable fallbackDrawable, float cornerRadius) {
        return getNewAutoBlurDrawable(view, fallbackDrawable, 800, Color.parseColor("#1a525252"), Color.parseColor("#40262626"), cornerRadius);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
