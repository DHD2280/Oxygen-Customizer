package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
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
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.isOOS1501;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.text.StaticLayout;
import android.widget.FrameLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightControllerImpl;
import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

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
                if (mAodTriggerSensor != null) setBooleanField(mAodTriggerSensor, "mIsMoving", true);
                try {
                    callMethod(mAodTriggerSensor, "notifyChanged");
                } catch (Throwable ignored) {}
                try {
                    callMethod(mAodSensorManager, "notifyChanged", true, false);
                } catch (Throwable ignored) {}
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
        mRetick = Xprefs.getBoolean(EDGE_LIGHT_RETICK, false);
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

        ReflectedClass QuickSettingsControllerImpl = ReflectedClass.ofIfPossible("com.android.systemui.shade.QuickSettingsControllerImpl");
        if (QuickSettingsControllerImpl.getClazz() != null) {
            QuickSettingsControllerImpl
                    .after("loadDimens")
                    .run(param -> {
                        mScreenCornerRadius = getIntField(param.thisObject, "mScreenCornerRadius");
                        EdgeLightControllerImpl.getInstance(mContext).setScreenRadius(mScreenCornerRadius);
                        refreshEdgeLight();
                    });
        } else {
            ReflectedClass QuickStepContract = ReflectedClass.of("com.android.systemui.shared.system.QuickStepContract");
            Object radius = callStaticMethod(QuickStepContract.getClazz(), "getWindowCornerRadius", mContext);
            if (radius instanceof Float rad) {
                mScreenCornerRadius = rad.intValue();
            } else if (radius instanceof Integer) {
                mScreenCornerRadius = (int) radius;
            } else {
                mScreenCornerRadius = 0;
            }
            EdgeLightControllerImpl.getInstance(mContext).setScreenRadius(mScreenCornerRadius);
        }

        ReflectedClass NotificationPanelViewController = ReflectedClass.ofIfPossible("com.android.systemui.shade.NotificationPanelViewController");
        if (NotificationPanelViewController.getClazz() != null) {
            NotificationPanelViewController
                    .after("loadDimens")
                    .run(param -> {
                        mScreenCornerRadius = getIntField(getObjectField(param.thisObject, "mQsController"), "mScreenCornerRadius");
                        EdgeLightControllerImpl.getInstance(mContext).setScreenRadius(mScreenCornerRadius);
                    });
        }

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
                .afterConstruction()
                .run(param -> {
                    if (!isOOS1501()) return;
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
                    if (!mEdgeLightEnabled) return;
                    boolean isSettingInterface = getBooleanField(param.thisObject, "isSettingInterface");
                    Object mIncomingNotiPaint = getObjectField(param.thisObject, "mIncomingNotiPaint");
                    if (!isSettingInterface && mIncomingNotiPaint != null) {
                        try {
                            callMethod(mIncomingNotiPaint, "draw", param.args[0]);
                        } catch (Throwable ignored) {
                            // Method removed in OOS15.0.1
                            drawIncomingPaint(param, mIncomingNotiPaint);
                        }
                    }
                    param.setResult(null);
                });
        OplusAodCurvedDisplayView
                .after("updateReceiveNotification")
                .run(param -> {
                    Object mIncomingNotiPaint = getObjectField(param.thisObject, "mIncomingNotiPaint");
                    int mMainColor = getIntField(mIncomingNotiPaint, "mMainColor");
                    EdgeLightControllerImpl.getInstance(mContext).setNotificationColor(mMainColor);
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
        ReflectedClass AodData = ReflectedClass.of("com.oplus.systemui.aod.aodclock.constant.AodData");

        AodRecord
                .after("onCreate")
                .run(param -> {
                    EdgeLightControllerImpl edgeController = EdgeLightControllerImpl.getInstance(mContext);
                    FrameLayout mAodBlackLayout = (FrameLayout) getObjectField(param.thisObject, "mAodBlackLayout");
                    edgeController.setBlockLayout(mAodBlackLayout);
                });

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

        AodData
                .before("setShowingCurvedDisplay")
                .run(param -> {
                    boolean showing = (boolean) param.args[0];
                    EdgeLightControllerImpl edgeController = EdgeLightControllerImpl.getInstance(mContext);
                    edgeController.setCurved(showing);
                    mRetickerHandler.removeCallbacks(mTriggerShow);
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

        ReflectedClass AodTriggerSensor = ReflectedClass.ofIfPossible("com.oplus.systemui.aod.sensor.AodTriggerSensor");
        if (AodTriggerSensor.getClazz() != null) {
            AodTriggerSensor
                    .afterConstruction()
                    .run(param -> {
                        mAodTriggerSensor = param.thisObject;
                    });
        }

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

    private void drawIncomingPaint(XC_MethodHook.MethodHookParam param, Object mIncomingNotiPaint) {
        Canvas canvas = (Canvas) param.args[0];
        // Drawable
        Drawable mDrawable = (Drawable) getObjectField(mIncomingNotiPaint, "mDrawable");
        // Header
        StaticLayout mHeaderLayout = (StaticLayout) getObjectField(mIncomingNotiPaint, "mHeaderLayout");
        Point mHeaderPoint = (Point) getObjectField(mIncomingNotiPaint, "mHeaderPoint");
        // Title
        StaticLayout mTitleLayout = (StaticLayout) getObjectField(mIncomingNotiPaint, "mTitleLayout");
        Point mTitlePoint = (Point) getObjectField(mIncomingNotiPaint, "mTitlePoint");
        // Message
        StaticLayout mMessageLayout = (StaticLayout) getObjectField(mIncomingNotiPaint, "mMessageLayout");
        Point mMessagePoint = (Point) getObjectField(mIncomingNotiPaint, "mMessagePoint");

        // OpIncomingNotificationPaint
        int mLayoutHeight = getIntField(mIncomingNotiPaint, "mLayoutHeight");
        int height = (canvas.getHeight() - mLayoutHeight) / 2;
        Paint mPaint = (Paint) getObjectField(mIncomingNotiPaint, "mPaint");
        int mMainColor = getIntField(mIncomingNotiPaint, "mMainColor");
        if (mDrawable != null) {
            canvas.save();
            canvas.translate(0.0f, height);
            mDrawable.draw(canvas);
            canvas.restore();
        }
        if (mHeaderLayout != null) {
            canvas.save();
            canvas.translate(mHeaderPoint.x, mHeaderPoint.y + height);
            mPaint.setColor(mMainColor);
            mHeaderLayout.draw(canvas);
            canvas.restore();
        }
        mPaint.setColor(-1);
        if (mTitleLayout != null) {
            canvas.save();
            canvas.translate(mTitlePoint.x, mTitlePoint.y + height);
            mTitleLayout.draw(canvas);
            canvas.restore();
        }
        if (mMessageLayout != null) {
            canvas.save();
            canvas.translate(mMessagePoint.x, height + mMessagePoint.y);
            mMessageLayout.draw(canvas);
            canvas.restore();
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
