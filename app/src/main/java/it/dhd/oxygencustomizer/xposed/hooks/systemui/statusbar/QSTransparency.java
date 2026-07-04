package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.BLUR_RADIUS_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_BLUR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_MAX_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_VAL;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.coerceIn;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bosphere.fadingedgelayout.FadingEdgeLayout;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class QSTransparency extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private final float keyguard_alpha = 0.85f;
    boolean qsTransparencyActive = false;
    private float alpha = 40;
    private boolean blurEnabled = false;
    private int blurRadius = 60;
    private Object mScrimControllerExImp = null;
    private float maxBlurRadius = 1f;
    private boolean mCustomColorEnabled = false;
    private int mCustomColor = Color.GRAY;
    private List<FrameLayout> mBackgroundLayouts = new ArrayList<>();
    private List<ImageView> mQsHeaderImageViews = new ArrayList<>();

    public QSTransparency(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        if (Xprefs == null) return;

        qsTransparencyActive = Xprefs.getBoolean(QS_TRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getSliderInt(QS_TRANSPARENCY_VAL, 40) / 100.0);

        blurEnabled = Xprefs.getBoolean(QSPANEL_BLUR_SWITCH, false);
        blurRadius = Xprefs.getSliderInt(BLUR_RADIUS_VALUE, 60);
        maxBlurRadius = Xprefs.getInt(QSPANEL_MAX_BLUR_AMOUNT, 100) / 100f;

    }

    private final ControllersProvider.ExpandedQsFractionChangeListener mExpandedQsFractionChangeListener = fraction -> {
        float alpha = coerceIn(fraction / 0.86f, 0.0f, 1.0f);
        setAlpha(alpha);
    };

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        setQsTransparency(PRParam);
        setBlurRadius();

        ReflectedClass OplusQSRootView = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.OplusQSRootView");
        if (OplusQSRootView.getClazz() != null) {
            OplusQSRootView
                    .after("onFinishInflate")
                    .run(param -> {
                        FrameLayout mOplusQsSplitView = (FrameLayout) param.thisObject;

                        FrameLayout mQsHeaderSplitLayout = new FadingEdgeLayout(mContext);
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        mQsHeaderSplitLayout.setLayoutParams(layoutParams);
                        mQsHeaderSplitLayout.setVisibility(View.GONE);

                        ImageView mQsHeaderSplitImageView = new ImageView(mContext);
                        mQsHeaderSplitImageView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        mQsHeaderSplitLayout.addView(mQsHeaderSplitImageView);
                        mQsHeaderSplitImageView.setBackgroundColor(Color.BLACK);

//                        mOplusQsSplitView.addView(mQsHeaderSplitLayout, 0);

//                        mBackgroundLayouts.add(mQsHeaderSplitLayout);
//                        mQsHeaderImageViews.add(mQsHeaderSplitImageView);

                    });

            ControllersProvider.registerExpandedQsFractionChangeCallback(mExpandedQsFractionChangeListener);
        }

    }

    private void setAlpha(float alpha) {
        if (mQsHeaderImageViews.isEmpty()) return;
        for (ImageView iv : mQsHeaderImageViews) {
            iv.setAlpha(alpha);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void setQsTransparency(XposedModuleInterface.PackageReadyParam PRParam) {
        final Class<?> ScrimControllerClass = findClass(SYSTEM_UI + ".statusbar.phone.ScrimController", PRParam.getClassLoader());

        hookAllMethods(ScrimControllerClass, "updateScrimColor", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!qsTransparencyActive) return;
                if (Build.VERSION.SDK_INT >= 35) return;

                int alphaIndex = param.args[2] instanceof Float ? 2 : 1;
                String scrimState = getObjectField(param.thisObject, "mState").toString();

                if (scrimState.contains("BOUNCER")) {
                    param.args[alphaIndex] = (Float) param.args[alphaIndex] * keyguard_alpha;
                } else {
                    String scrimName = "unknown_scrim";

                    if (findField(ScrimControllerClass, "mScrimInFront").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "scrim_in_front";
                    } else if (findField(ScrimControllerClass, "mScrimBehind").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "scrim_behind";
                    } else if (findField(ScrimControllerClass, "mNotificationsScrim").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "scrim_notifications";
                    }

                    if (scrimName.equals("scrim_notifications") || scrimName.equals("scrim_behind")) {
                        param.args[alphaIndex] = (Float) param.args[alphaIndex] * alpha;
                    }
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 35) {
            hookQs();
        }

    }

    private void hookQs() {
        ReflectedClass ScrimControllerExImp = ReflectedClass.of("com.oplus.systemui.statusbar.phone.ScrimControllerExImp");
        ScrimControllerExImp
                .afterConstruction()
                .run(param -> {
                    mScrimControllerExImp = param.thisObject;
                });

//        ScrimControllerExImp
//                .after("updateBehindMixConfig")
//                .run(param -> {
//                    ViewBlurProxy viewBlurProxy;
//                    Object scrimBehind = callMethod(callMethod(param.thisObject, "getScrimController"), "getScrimBehind");
//                    if (scrimBehind == null) return;
//                    Object ext = callMethod(scrimBehind, "getExt");
//                    if (ext == null) return;
//                    viewBlurProxy = (ViewBlurProxy) callMethod(ext, "getViewBlurProxy");
//                    if (viewBlurProxy == null) return;
//                    Object currentConfig = callMethod(param.thisObject, "getPanelPlatformMixConfig");
//                    if (currentConfig != null) {
//                        // Se è un BlurMixMulti
//                        if (currentConfig.getClass().getSimpleName().equals("BlurMixMulti")) {
//                            // Ottieni i MixColor
//                            Object foregroundMixColor = callMethod(currentConfig, "getForegroundMixColor");
//                            Object backgroundMixColor = callMethod(currentConfig, "getBackgroundMixColor");
//
//                            // Modifica i colori
//                            if (foregroundMixColor != null) {
//                                callMethod(foregroundMixColor, "setTopLayerColor", 0xFFFF5733);
//                                callMethod(foregroundMixColor, "setBottomLayerColor", 0x40FF5733);
//                            }
//
//                            if (backgroundMixColor != null) {
//                                callMethod(backgroundMixColor, "setTopLayerColor", 0xFF3366FF);
//                                callMethod(backgroundMixColor, "setBottomLayerColor", 0x403366FF);
//                            }
//                        }
//                        // Se è un BlurMixSingle
//                        else if (currentConfig.getClass().getSimpleName().equals("BlurMixSingle")) {
//                            Object mixColor = callMethod(currentConfig, "getMixColor");
//                            if (mixColor != null) {
//                                callMethod(mixColor, "setTopLayerColor", 0xFFFF5733);
//                                callMethod(mixColor, "setBottomLayerColor", 0x40FF5733);
//                            }
//                        } else if (currentConfig.getClass().getSimpleName().equals("BlurMixConfig")) {
//
//                        }
//                    }
//                    callMethod(currentConfig, "setBlurColor", Color.BLACK);
//                    viewBlurProxy.getBlurConfig().setPlatformMixConfig((BlurMixConfig) currentConfig);
//                    viewBlurProxy.applyBlurConfig();
//
//                });

        ReflectedClass ScrimViewExImp = ReflectedClass.of("com.oplus.systemui.scrim.ScrimViewExImp");
        ScrimViewExImp
                .before("setBlurAmount")
                .run(param -> {
                    // float f2, String str
                    if (mScrimControllerExImp == null) return;
                    float blurAmount = (float) param.args[0];
                    boolean isBehind = false;
                    try {
                        isBehind = (boolean) callMethod(param.thisObject, "isBehind");
                    } catch (Throwable ignored) {
                        isBehind = getBooleanField(param.thisObject, "isBehind");
                    }
                    boolean isQsVisible = false;
                    try {
                        isQsVisible = (boolean) callMethod(mScrimControllerExImp, "isQsVisible");
                    } catch (Throwable ignored) {
                        isQsVisible = getBooleanField(mScrimControllerExImp, "isQsVisible");
                    }
                    if (isBehind && isQsVisible) {
                        param.args[0] = constrain(blurAmount, 0.0f, maxBlurRadius);
                    }
                });
    }

    private float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    private void setBlurRadius() {
        hookAllMethods(Resources.class, "getDimensionPixelSize", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (!blurEnabled) return;
                if (Build.VERSION.SDK_INT >= 35) return;

                try {
                    @SuppressLint("DiscouragedApi") int resId = mContext.getResources()
                            .getIdentifier("max_window_blur_radius", "dimen", mContext.getPackageName());
                    if (param.args[0].equals(resId)) {
                        param.setResult(blurRadius);
                    }
                } catch (Throwable throwable) {
                    log(throwable);
                }
            }
        });
    }
}
