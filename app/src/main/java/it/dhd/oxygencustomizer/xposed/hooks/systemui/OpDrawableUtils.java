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
    public Class<?> QsViewBackgroundProxyCompanion = null;
    private Object OplusMediaPanelView = null;
    public static Object QsMediaTileBackgroundProxy = null;
    public static Object QsMediaTileBackground = null;
    private static Object BlurTypeMotionInstance = null;
    // OplusQsDialogBlurBackgroundEx oplusQsDialogBlurBackgroundEx = (OplusQsDialogBlurBackgroundEx) DependencyEx.get(OplusQsDialogBlurBackgroundEx.class);

    // Interfaces
    private final ArrayList<OnBackgroundUpdated> mBackgroundUpdatedListeners = new ArrayList<>();


    public OpDrawableUtils(Context context) {
        super(context);
        instance = this;
    }

    public static void registerBackgroundUpdatedListener(OnBackgroundUpdated listener) {
        instance.mBackgroundUpdatedListeners.add(listener);
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

//        try {
//            QsViewBackgroundProxy = ReflectedClass.of("com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy").getClazz();
            ReflectedClass OplusQsMediaPanelView = ReflectedClass.of("com.oplus.systemui.qs.media.OplusQsMediaPanelView");
            OplusQsMediaPanelView
                    .afterConstruction()
                    .run(param -> {
                        OplusMediaPanelView = param.thisObject;
                    });




//            OplusQsMediaPanelView
//                    .after("onAttachedToWindow")
//                    .run(param -> {
//                        callMethod(QsMediaTileBackground, "onBackgroundAttach");
//                    });
//
//
//        } catch (Throwable ignored) {}


//        ReflectedClass QsViewBackgroundProxy = ReflectedClass.of("com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy");
//
//        ReflectedClass QsViewBackgroundProxyCompanionReflected = ReflectedClass.of("com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy$Companion");
//        QsViewBackgroundProxyCompanion = QsViewBackgroundProxyCompanionReflected.getClazz();
//        QsViewBackgroundProxyCompanionReflected
//                .after("getMediaPanelBackgroundProxy")
//                .run(param -> QsMediaTileBackgroundProxy = param.getResult());
//
//        ReflectedClass QsOnePlusStaticBackgroundProxy = ReflectedClass.of("com.oplus.systemui.qs.base.widget.QsOnePlusStaticBackgroundProxy");
//        ReflectedClass QsStaticBackgroundProxy = ReflectedClass.of("com.oplus.systemui.qs.base.widget.QsStaticBackgroundProxy");
//
//        ReflectedClass.ReflectionConsumer panelHook = param -> {
//            if (param.thisObject != QsMediaTileBackgroundProxy) return;
//            new Handler().postDelayed(() -> {
//                Object panelInfo = getObjectField(param.thisObject, "panelInfo");
//                View view = (View) callMethod(panelInfo, "getBackgroundView");
//                XposedBridge.log("getTargetQsViewBackground background null?" + (view.getBackground() == null));
//                onMediaTileBackgroundUpdated(view);
//            }, 250L);
//        };
//
//        QsViewBackgroundProxy
//                .after("switchBackgroundTo")
//                        .run(panelHook);
//
//        QsOnePlusStaticBackgroundProxy
//                .after("getTargetQsViewBackground")
//                .run(panelHook);
//
//        QsStaticBackgroundProxy
//                .after("getTargetQsViewBackground")
//                .run(panelHook);

    }

    public static Drawable getMediaTileBackground() {
        Object panelInfo = getObjectField(QsMediaTileBackgroundProxy, "panelInfo");
        View view = (View) callMethod(panelInfo, "getBackgroundView");
        return view.getBackground();
    }

    private Drawable getOOS15Background() {
        try {
            Object backgroundProxy = getObjectField(OplusMediaPanelView, "backgroundProxy");
            Object panelInfo = getObjectField(backgroundProxy, "panelInfo");
            Drawable bg = (Drawable) callMethod(panelInfo, "getBackgroundDrawable");
            return bg;
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        try {
            return (Drawable) callMethod(OplusMediaPanelView, "getGlobalThemeBackgroundDrawable");
        } catch (Throwable t) {
            XposedBridge.log(t);
        }
        return null;
    }

    public static void applyAutoBlurTint(Context context, Drawable drawable, int color) {

        try {
            Object viewBlurProxy = callMethod(drawable, "getViewBlurProxy");
            Object blurConfig = callMethod(viewBlurProxy, "getBlurConfig");

            Object colorConfig = MixColor.getConstructor(int.class, int.class, int.class)
                    .newInstance(isNeedSeparateDarkThemeColor(context) || SystemUtils.isDarkMode() ? 4 : 1, color, color);

            Object blurMixMulti = BlurMixMulti.getConstructor(MixColor, MixColor)
                    .newInstance(colorConfig, colorConfig);
            callMethod(blurMixMulti, "setUseGlobalBlurConfig", true);
            callMethod(blurConfig, "setPlatformMixConfig", blurMixMulti);
            Object viewBlurProxyNew = callMethod(drawable, "getViewBlurProxy");
            callMethod(viewBlurProxyNew, "applyBlurConfig");
        } catch (Throwable t) {
            XposedBridge.log("applyAutoBlurTint error: \n" + Log.getStackTraceString(t));
        }
    }

//    public static void applyAutoBlurTint(Context context, Drawable drawable, Object colorMix) {
//    }

    public static void applyAutoBlurTint(Drawable drawable, int color) {
        try {
            Object viewBlurProxy = callMethod(drawable, "getViewBlurProxy");
            Object blurConfig = callMethod(viewBlurProxy, "getBlurConfig");

            callMethod(blurConfig, "setMotionBlurMixConfig", newInstance(BlurMixSingle, newInstance(MixColor, SystemUtils.isDarkMode() ? 4 : 5, color, Color.parseColor("#1a525252"))));
            Object viewBlurProxyNew = callMethod(drawable, "getViewBlurProxy");
            callMethod(viewBlurProxyNew, "applyBlurConfig");
        } catch (Throwable t) {
            XposedBridge.log("applyAutoBlurTint error: \n" + Log.getStackTraceString(t));
        }
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

    // Interface methods
    private void onMediaTileBackgroundUpdated(View view) {
        for (OnBackgroundUpdated listener : mBackgroundUpdatedListeners) {
            try {
                listener.onMediaTileBackgroundUpdated(view, view.getBackground());
            } catch (Throwable t) {
                log(t);
            }
        }
    }

    /**
     * Background updated interface
     */
    public interface OnBackgroundUpdated {
        /**
         * Called when the media tile background is updated
         * @param drawable The new background drawable
         */
        void onMediaTileBackgroundUpdated(View view, Drawable drawable);

        /**
         * Called when the highlight tile background is updated
         * @param drawable The new background drawable
         */
        void onHighlightTileBackgroundUpdated(Drawable drawable);

        /**
         * Called when the tile background is updated
         * @param drawable The new background drawable
         */
        void onTileBackgroundUpdated(Drawable drawable);
    }

    /**
     * Background Outline updated interface
     */
    public interface OnOutlineUpdated {
        /**
         * Called when the media tile outline is updated
         * @param drawable The new outline drawable
         */
        void onMediaTileOutlineUpdated(Drawable drawable);

        /**
         * Called when the highlight tile outline is updated
         * @param drawable The new outline drawable
         */
        void onHighlightTileOutlineUpdated(Drawable drawable);

        /**
         * Called when the tile outline is updated
         * @param drawable The new outline drawable
         */
        void onTileOutlineUpdated(Drawable drawable);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
