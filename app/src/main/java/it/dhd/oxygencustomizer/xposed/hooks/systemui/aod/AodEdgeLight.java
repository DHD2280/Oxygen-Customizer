package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_ALWAYS_TRIGGER_ON_PULSE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_BLUR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_BLUR_TYPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_COLOR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_DRAW_BLUR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_RETICK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_RETICK_DURATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_WIDTH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.widget.FrameLayout;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;
import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightControllerImpl;

public class AodEdgeLight extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private boolean mEdgeLightEnabled = false;
    private float mEdgeLightWidth = 20f;
    private int mEdgeLightStyle = 0;
    private EdgeLightView.ColorMode mEdgeLightColorMode = EdgeLightView.ColorMode.ACCENT;
    private boolean mAlwaysTriggerOnPulse = false;
    private boolean mRetick = false;
    private long mRetickDuration = 30 * 1000L;
    private int mEdgeLightCustomColor = Color.RED;
    private boolean mEdgeDrawBlur = false;
    private int mEdgeBlurMode = 0, mEdgeBlurType = 0;
    private long mTotalDuration = 0;
    private int mScreenCornerRadius = 20;

    private final Handler mRetickerHandler = new Handler(Looper.getMainLooper());

    private Object mDozeParameters = null;
    private Object mAodSensorManager = null;
    private Object mAodTriggerSensor = null;
    private boolean mIsOc = false;

    private final Runnable mTriggerShow = new Runnable() {
        @Override
        public void run() {
            XposedBridge.log("AodEdgeLight: mTriggerShow");
            if (!mRetick) return;
            if (mAodSensorManager == null) return;
            try {
                mIsOc = true;
                setAdditionalInstanceField(mAodSensorManager, "mIsOC", true);
                setBooleanField(mAodTriggerSensor, "mIsMoving", true);
                callMethod(mAodTriggerSensor, "notifyChanged");
            } catch (Throwable t) {
                log(t);
            }
            mRetickerHandler.postDelayed(mTriggerShow, mRetickDuration);
        }
    };

    public AodEdgeLight(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mEdgeLightEnabled = Xprefs.getBoolean(EDGE_LIGHT_ENABLED, false);
        mEdgeLightStyle = Integer.parseInt(Xprefs.getString(EDGE_LIGHT_STYLE, "0"));
        mEdgeLightWidth = Xprefs.getSliderFloat(EDGE_LIGHT_WIDTH, 20f);
        mEdgeLightColorMode = getColorMode(Integer.parseInt(Xprefs.getString(EDGE_LIGHT_COLOR_MODE, "0")));
        mAlwaysTriggerOnPulse = Xprefs.getBoolean(EDGE_LIGHT_ALWAYS_TRIGGER_ON_PULSE, false);
        mRetick = Xprefs.getBoolean(EDGE_LIGHT_RETICK, true);
        mRetickDuration = getRetickerDuration(Integer.parseInt(Xprefs.getString(EDGE_LIGHT_RETICK_DURATION, "0")));
        mEdgeLightCustomColor = Xprefs.getInt(EDGE_LIGHT_CUSTOM_COLOR, Color.RED);
        mEdgeDrawBlur = Xprefs.getBoolean(EDGE_LIGHT_DRAW_BLUR, false);
        mEdgeBlurType = Integer.parseInt(Xprefs.getString(EDGE_LIGHT_BLUR_TYPE, "0"));
        mEdgeBlurMode = Integer.parseInt(Xprefs.getString(EDGE_LIGHT_BLUR_MODE, "0"));

        refreshEdgeLight();
    }

    private long getRetickerDuration(int retick) {
        return switch (retick) {
            case 1 -> 60 * 1000L;
            case 2 -> 2 * 60 * 1000L;
            case 3 -> 5 * 60 * 1000L;
            case 4 -> 10 * 60 * 1000L;
            default -> 30 * 1000L;
        };
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        ReflectedClass QuickSettingsControllerImpl = ReflectedClass.of("com.android.systemui.shade.QuickSettingsControllerImpl");
        QuickSettingsControllerImpl
                .after("loadDimens")
                .run(param -> {
                    mScreenCornerRadius = getIntField(param.thisObject, "mScreenCornerRadius");
                    EdgeLightControllerImpl.getInstance(mContext).setScreenRadius(mScreenCornerRadius);
                    XposedBridge.log("AodEdgeLight: mScreenCornerRadius = " + mScreenCornerRadius);
                });

        ReflectedClass DozeParameters = ReflectedClass.of("com.android.systemui.statusbar.phone.DozeParameters");
        DozeParameters
                .afterConstruction()
                .run(param -> {
                    XposedBridge.log("AodEdgeLight: DozeParameters");
                    mDozeParameters = param.thisObject;
                    EdgeLightControllerImpl.getInstance(mContext).setDozeParameters(mDozeParameters);
                });

        ReflectedClass OplusAodCurvedDisplayView = ReflectedClass.of("com.oplus.systemui.aod.surface.OplusAodCurvedDisplayView");
        OplusAodCurvedDisplayView
                .after("initAnimatorData")
                        .run(param -> {
                            XposedBridge.log("AodEdgeLight: initAnimatorData");
                            int maskDuration = getIntField(param.thisObject, "mNotificationMaskMoveDuration");
                            int fadeInDuration = getIntField(param.thisObject, "AOD_NOTIFICATION_FADE_DURATION");
                            int fadeOutDuration = getIntField(param.thisObject, "mNotificationFadeOutDuration");
                            int repeatCount = getIntField(param.thisObject, "mAnimRepeatTotalCount");
                            if (repeatCount == 0) {
                                repeatCount = 1;
                            }

                            long totalDuration = maskDuration + fadeInDuration + fadeOutDuration;
                            totalDuration *= repeatCount;

                            XposedBridge.log("AodEdgeLight: totalDuration = " + totalDuration);
                            mTotalDuration = totalDuration;
                            EdgeLightControllerImpl.getInstance(mContext).setAnimationDuration(mTotalDuration);
                        });
        OplusAodCurvedDisplayView
                .before("onDraw")
                .run(param -> {
                    boolean isSettingInterface = getBooleanField(param.thisObject, "isSettingInterface");
                    Object mIncomingNotiPaint = getObjectField(param.thisObject, "mIncomingNotiPaint");
                    if (!isSettingInterface && mIncomingNotiPaint != null) {
                        callMethod(mIncomingNotiPaint, "draw", param.args[0]);
                    }

                    param.setResult(null);
                });

        ReflectedClass CentralSurfacesImpl = ReflectedClass.of("com.android.systemui.statusbar.phone.CentralSurfacesImpl");
        CentralSurfacesImpl
                .after("updateDozingState")
                        .run(param -> {
                            boolean dozing = getBooleanField(param.thisObject, "mDozing");
                            if (!dozing) {
                                XposedBridge.log("AodEdgeLight: dozing = false, removing reticker callbacks");
                                mRetickerHandler.removeCallbacks(mTriggerShow);
                            }
                            if (EdgeLightControllerImpl.hasInstance()) {
                                EdgeLightControllerImpl.getInstance().setDozing(dozing);
                            }
                        });

        ReflectedClass AodRecord = ReflectedClass.of(
                "com.oplus.systemui.aod.AodRecord",
                "com.oplusos.systemui.aod.AodRecord");

        AodRecord
                .after("addCurvedDisplayView")
                .run(param -> {
                    EdgeLightControllerImpl edgeController = EdgeLightControllerImpl.getInstance(mContext);
                    FrameLayout mAodBlackLayout = (FrameLayout) getObjectField(param.thisObject, "mAodBlackLayout");
                    edgeController.setBlockLayout(mAodBlackLayout);
                    edgeController.setCurved(true);
                    XposedBridge.log("AodEdgeLight: addCurvedDisplayView - removing reticker callbacks");
                    mRetickerHandler.removeCallbacks(mTriggerShow);
                });

        AodRecord
                .after("removeCurvedDisplayView")
                .run(param -> {
                    XposedBridge.log("AodEdgeLight: removing view");
                    EdgeLightControllerImpl edgeController = EdgeLightControllerImpl.getInstance(mContext);
                    edgeController.setCurved(false);
                    XposedBridge.log("AodEdgeLight: removeCurvedDisplayView - removing reticker callbacks & trigger again in " + mRetickDuration);
                    mRetickerHandler.removeCallbacks(mTriggerShow);
                    mRetickerHandler.postDelayed(mTriggerShow, mRetickDuration);
                });

        ReflectedClass OpIncomingNotificationPaint = ReflectedClass.of("com.oplus.systemui.aod.surface.OpIncomingNotificationPaint");
        OpIncomingNotificationPaint
                .after("updateNotification")
                .run(param -> {
                    int mNotificationColor = getIntField(param.thisObject, "mMainColor");
                    XposedBridge.log("AodEdgeLight: updateNotification - mNotificationColor = " + mNotificationColor);
                    EdgeLightControllerImpl.getInstance(mContext).setNotificationColor(mNotificationColor);
                });

        ReflectedClass AodManager = ReflectedClass.of("com.oplus.systemui.aod.common.AodManager");
        AodManager
                .before("notNeedWakeAod")
                .run(param -> {
                    XposedBridge.log("AodEdgeLight: notNeedWakeAod - mIsOc = " + mIsOc);
                    if (mIsOc) {
                        param.setResult(false);
                    }
                });

        ReflectedClass AodRootLayout = ReflectedClass.of(
                "com.oplus.systemui.aod.aodclock.off.AodRootLayout",
                "com.oplusos.systemui.aod.aodclock.off.AodRootLayout");
        AodRootLayout
                .afterConstruction()
                        .run(param -> {
                            FrameLayout mAodRootLayout = (FrameLayout) param.thisObject;
                            EdgeLightControllerImpl edgeLightController = EdgeLightControllerImpl.getInstance(mContext);
                            edgeLightController.setAodRootLayout(mAodRootLayout);
                        });

        ReflectedClass AodTriggerSensor = ReflectedClass.of("com.oplus.systemui.aod.sensor.AodTriggerSensor");
        AodTriggerSensor
                .afterConstruction()
                .run(param -> {
                    mAodTriggerSensor = param.thisObject;
                });

        ReflectedClass AodSensorManager = ReflectedClass.of("com.oplus.systemui.aod.sensor.AodSensorManager");
        AodSensorManager
                .afterConstruction()
                        .run(param -> {
                            setAdditionalInstanceField(param.thisObject, "mIsOC", false);
                            mAodSensorManager = param.thisObject;
                        });
        AodSensorManager
                .after("triggerShow")
                .run(param -> {
                    XposedBridge.log("AodEdgeLight triggerShow");
                    boolean isOc = false;
                    try {
                        isOc = (boolean) getAdditionalInstanceField(mAodSensorManager, "mIsOC");
                        if (isOc) {
                            setAdditionalInstanceField(mAodSensorManager, "mIsOC", false);
                            mIsOc = false;
                        }
                    } catch (Throwable t) {
                        log(t);
                    }
                    XposedBridge.log("AodEdgeLight triggerShow isOc = " + isOc);
                    if (EdgeLightControllerImpl.hasInstance()) {
                        EdgeLightControllerImpl.getInstance(mContext).triggerShow(isOc);
                    }
                });

    }

    private EdgeLightView.ColorMode getColorMode(int colorMode) {
        for (EdgeLightView.ColorMode mode : EdgeLightView.ColorMode.values()) {
            if (mode.ordinal() == colorMode) {
                return mode;
            }
        }
        return EdgeLightView.ColorMode.ACCENT;
    }

    private void refreshEdgeLight() {
        EdgeLightControllerImpl.getInstance(mContext).setOptions(mEdgeLightEnabled, mEdgeLightStyle, mEdgeLightWidth, mEdgeLightColorMode, mAlwaysTriggerOnPulse, mRetick, mEdgeLightCustomColor, mEdgeDrawBlur, mEdgeBlurMode, mEdgeBlurType);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
