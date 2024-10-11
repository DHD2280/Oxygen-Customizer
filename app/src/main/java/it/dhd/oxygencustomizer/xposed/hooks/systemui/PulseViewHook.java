package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_AMBIENT;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_CENTER_MIRRORED;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_COLOR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_COLOR_USER;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_CUSTOM_DIMEN;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_CUSTOM_DIV;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_EMPTY_BLOCK_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_FILLED_BLOCK_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_FUDGE_FACTOR;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_GRAVITY;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_LAVA_SPEED;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_LINE_SHOW_FLASH;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_LINE_WAVE_OPACITY;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_LINE_WAVE_STROKE;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_LOCKSCREEN;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_NAVBAR;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_RENDER_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_SMOOTHING;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_SOLID_FUDGE_FACTOR;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_SOLID_UNITS_COUNT;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_SOLID_UNITS_OPACITY;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_SOLID_UNITS_ROUNDED;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.PULSE_VERTICAL_MIRROR;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.views.VisualizerView;
import it.dhd.oxygencustomizer.xposed.views.pulse.ColorController;
import it.dhd.oxygencustomizer.xposed.views.pulse.FadingBlockRenderer;
import it.dhd.oxygencustomizer.xposed.views.pulse.LineRenderer;
import it.dhd.oxygencustomizer.xposed.views.pulse.PulseControllerImpl;
import it.dhd.oxygencustomizer.xposed.views.pulse.SolidLineRenderer;

public class PulseViewHook extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static Class<?> CentralSurfacesImpl = null;
    private FrameLayout mNotificationShadeView = null;
    private FrameLayout mKeyguardBottomArea = null;
    private boolean mNavBarPulse, mLockScreenPulse, mAmbientPulse, mPulseEnabled, mPulseSmoothing;
    private int mPulseStyle, mPulseColorMode, mPulseColor;
    private int mPulseLavaSpeed, mPulseCustomDimen, mPulseDiv, mPulseFilledBlock, mPulseEmptyBlock, mPulseFudgeFactor;
    private boolean mPulseSolidRounded;
    private int mPulseSolidOpacity, mPulseSolidCount, mPulseSolidFudgeFactor;
    private View mStartButton = null, mEndButton = null;
    private FrameLayout mAodRootLayout = null;
    private FrameLayout mNavigationBar = null;
    private boolean mCenterMirrored = false, mVerticalMirror = false;
    private int mGravity = 0;
    private boolean mShowFlash = true;
    private int mWaveOpacity = 200;
    private float mWaveStroke = 5f;
    private boolean DWallpaperEnabled = false;


    public PulseViewHook(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        // Pulse State
        mNavBarPulse = Xprefs.getBoolean(PULSE_NAVBAR, false);
        mLockScreenPulse = Xprefs.getBoolean(PULSE_LOCKSCREEN, false);
        mAmbientPulse = Xprefs.getBoolean(PULSE_AMBIENT, false);

        // Render Mode
        mPulseStyle = Integer.parseInt(Xprefs.getString(PULSE_RENDER_STYLE, "0"));

        // Pulse Smoothing
        mPulseSmoothing = Xprefs.getBoolean(PULSE_SMOOTHING, false);

        // Pulse Color
        mPulseColorMode = Integer.parseInt(Xprefs.getString(PULSE_COLOR_MODE, "2"));
        mPulseColor = Xprefs.getInt(PULSE_COLOR_USER, 0x92FFFFFF);

        // Pulse Style
        mPulseLavaSpeed = Xprefs.getSliderInt(PULSE_LAVA_SPEED, ColorController.LAVA_LAMP_SPEED_DEF);

        // Fading Block
        mPulseEmptyBlock = Xprefs.getSliderInt(PULSE_EMPTY_BLOCK_SIZE, 1);
        mPulseCustomDimen = Xprefs.getSliderInt(PULSE_CUSTOM_DIMEN, 14);
        mPulseDiv = Xprefs.getSliderInt(PULSE_CUSTOM_DIV, 16);
        mPulseFilledBlock = Xprefs.getSliderInt(PULSE_FILLED_BLOCK_SIZE, 4);
        mPulseFudgeFactor = Xprefs.getSliderInt(PULSE_FUDGE_FACTOR, 4);

        // Solid Lines
        mPulseSolidRounded = Xprefs.getBoolean(PULSE_SOLID_UNITS_ROUNDED, false);
        mPulseSolidOpacity = Xprefs.getSliderInt(PULSE_SOLID_UNITS_OPACITY, 200);
        mPulseSolidCount = Xprefs.getSliderInt(PULSE_SOLID_UNITS_COUNT, 32);
        mPulseSolidFudgeFactor = Xprefs.getSliderInt(PULSE_SOLID_FUDGE_FACTOR, 4);

        // Pulse Mirror and Gravity
        mCenterMirrored = Xprefs.getBoolean(PULSE_CENTER_MIRRORED, false);
        mVerticalMirror = Xprefs.getBoolean(PULSE_VERTICAL_MIRROR, false);
        mGravity = Integer.parseInt(Xprefs.getString(PULSE_GRAVITY, "0"));

        // Line Renderer
        mShowFlash = Xprefs.getBoolean(PULSE_LINE_SHOW_FLASH, true);
        mWaveStroke = Xprefs.getSliderFloat(PULSE_LINE_WAVE_STROKE, 5f);
        mWaveOpacity = Xprefs.getSliderInt(PULSE_LINE_WAVE_OPACITY, 200);

        mPulseEnabled = mNavBarPulse || mLockScreenPulse || mAmbientPulse;

        DWallpaperEnabled = Xprefs.getBoolean("DWallpaperEnabled", false);

        if (Key.length > 0) {
            for (String PulsePref : PULSE_PREFS) {
                if (Key[0].equals(PulsePref)) {
                    if (PulseControllerImpl.hasInstance()) {
                        refreshPulse(PulseControllerImpl.getInstance());
                    }
                }
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!lpparam.packageName.equals(listenPackage)) return;

        AudioDataProvider.registerInfoCallback(this::onPrimaryMetadataOrStateChanged);

        CentralSurfacesImpl = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);

        Class<?> NavigationBarView = findClass("com.android.systemui.navigationbar.NavigationBarView", lpparam.classLoader);

        Class<?> NotificationShadeWindowView;
        try {
            NotificationShadeWindowView = findClass("com.android.systemui.shade.NotificationShadeWindowView", lpparam.classLoader);
        } catch (Throwable t) {
            NotificationShadeWindowView = findClass("com.android.systemui.statusbar.phone.NotificationShadeWindowView", lpparam.classLoader);
        }
        hookAllConstructors(NotificationShadeWindowView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mNotificationShadeView = (FrameLayout) param.thisObject;
                placePulseView();
            }
        });

        hookAllMethods(CentralSurfacesImpl, "getNotificationShadeWindowView", new XC_MethodHook() {
            protected void afterHookedMethod(XC_MethodHook.MethodHookParam methodHookParam) throws Throwable {
                mNavigationBar = (FrameLayout) callMethod(methodHookParam.thisObject, "getNavigationBarView", new Object[0]);
                if (PulseControllerImpl.hasInstance()) {
                    PulseControllerImpl.getInstance().setNavbar(mNavigationBar);
                }
            }
        });

        Class<?> AodRootLayout;
        try {
            AodRootLayout = findClass("com.oplus.systemui.aod.aodclock.off.AodRootLayout", lpparam.classLoader);
        } catch (Throwable t) {
            AodRootLayout = findClass("com.oplusos.systemui.aod.aodclock.off.AodRootLayout", lpparam.classLoader);
        }
        hookAllConstructors(AodRootLayout, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mAodRootLayout = (FrameLayout) param.thisObject;
                if (PulseControllerImpl.hasInstance()) {
                    PulseControllerImpl.getInstance().setAodRootLayout(mAodRootLayout);
                }
            }
        });

        Class<?> KeyguardBottomAreaView = findClass("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader);
        hookAllMethods(KeyguardBottomAreaView, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mKeyguardBottomArea = (FrameLayout) param.thisObject;
                try {
                    mStartButton = mKeyguardBottomArea.findViewById(mContext.getResources().getIdentifier("start_button", "id", Constants.Packages.SYSTEM_UI));
                    mEndButton = mKeyguardBottomArea.findViewById(mContext.getResources().getIdentifier("end_button", "id", Constants.Packages.SYSTEM_UI));
                } catch (Throwable t) {
                    log("Failed to find start/end button");
                }
                updateLockscreenIcons();
            }
        });

        hookAllMethods(CentralSurfacesImpl, "keyguardGoingAway", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (PulseControllerImpl.hasInstance()) {
                    ///log("keyguardGoingAway");
                    PulseControllerImpl.getInstance().notifyKeyguardGoingAway();
                }
            }
        });

        // Stole Dozing State
        hookAllMethods(CentralSurfacesImpl, "updateDozingState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //log("updateDozingState, dozing: " + getBooleanField(param.thisObject, "mDozing"));
                if (PulseControllerImpl.hasInstance()) {
                    PulseControllerImpl.getInstance().setDozing(getBooleanField(param.thisObject, "mDozing"));
                }
            }
        });

        // Stole Keyguard is showing
        Class<?> KayguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
        hookAllMethods(KayguardUpdateMonitor, "setKeyguardShowing", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (PulseControllerImpl.hasInstance()) {
                    //log("Keyguard is showing: " + param.args[0]);
                    PulseControllerImpl.getInstance().setKeyguardShowing((boolean) param.args[0]);
                    VisualizerView.getInstance().bringToFront();
                    VisualizerView.getInstance().requestLayout();
                    updateLockscreenIcons();
                }
            }
        });

        // Stole Screen Pinning
        try {
            hookAllMethods(NavigationBarView, "setInScreenPinning", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (PulseControllerImpl.hasInstance()) {
                        //log("Screen pinning: " + (boolean)param.args[0]);
                        PulseControllerImpl.getInstance().setScreenPinning((boolean) param.args[0]);
                    }
                }
            });
        } catch (Throwable ignored) {
        }

    }

    private void updateLockscreenIcons() {
        if (!mPulseEnabled || !mLockScreenPulse) return;

        if (mStartButton != null) {
            mStartButton.bringToFront();
            mStartButton.requestLayout();
        }
        if (mEndButton != null) {
            mEndButton.bringToFront();
            mEndButton.requestLayout();
        }
        mKeyguardBottomArea.bringToFront();
        mKeyguardBottomArea.requestLayout();
    }

    private void onPrimaryMetadataOrStateChanged(int state) {
        if (mPulseEnabled && PulseControllerImpl.hasInstance()) {
            PulseControllerImpl.getInstance().onPrimaryMetadataOrStateChanged(state);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void placePulseView() {
        log("Placing PulseView");
        VisualizerView visualizerView;
        if (VisualizerView.hasInstance()) {
            visualizerView = VisualizerView.getInstance();
        } else {
            visualizerView = new VisualizerView(mContext);
        }
        try {
            ((ViewGroup) visualizerView.getParent()).removeView(visualizerView);
        } catch (Throwable ignored) {
        }
        mNotificationShadeView.addView(visualizerView);
        refreshPulse(PulseControllerImpl.getInstance(mContext));
    }

    private void refreshPulse(PulseControllerImpl pulseController) {
        if (pulseController == null) return;
        pulseController.setPulseEnabled(mPulseEnabled);
        pulseController.setLockscreenPulseEnabled(mLockScreenPulse);
        pulseController.setNavbarPulseEnabled(mNavBarPulse);
        pulseController.setAmbientPulseEnabled(mAmbientPulse);
        pulseController.setPulseRenderStyle(mPulseStyle);
        pulseController.setAodRootLayout(mAodRootLayout);
        pulseController.setNavbar(mNavigationBar);
        if (SolidLineRenderer.hasInstance()) {
            refreshPulseSolidLineRenderer(SolidLineRenderer.getInstance());
        }
        if (FadingBlockRenderer.hasInstance()) {
            refreshPulseFadingBlockRenderer(FadingBlockRenderer.getInstance());
        }
        if (LineRenderer.hasInstance()) {
            refreshPulseLineRenderer(LineRenderer.getInstance());
        }

        if (ColorController.hasInstance()) {
            refreshPulseColorController(ColorController.getInstance());
        }
    }

    private void refreshPulseSolidLineRenderer(SolidLineRenderer solidLineRenderer) {
        if (solidLineRenderer == null) return;

        solidLineRenderer.updateSettings(mPulseSolidFudgeFactor, mPulseSmoothing, mPulseSolidRounded, mPulseSolidCount, mPulseSolidOpacity,
                mCenterMirrored, mVerticalMirror, mGravity);
    }

    private void refreshPulseFadingBlockRenderer(FadingBlockRenderer fadingBlockRenderer) {
        if (fadingBlockRenderer == null) return;

        fadingBlockRenderer.updateSettings(mPulseEmptyBlock, mPulseCustomDimen, mPulseDiv, mPulseFudgeFactor, mPulseFilledBlock,
                mCenterMirrored, mVerticalMirror, mGravity);
        fadingBlockRenderer.updateSmoothingEnabled(mPulseSmoothing);
    }

    private void refreshPulseLineRenderer(LineRenderer lineRenderer) {
        if (lineRenderer == null) return;

        lineRenderer.updateSettings(mShowFlash, mWaveStroke, mWaveOpacity);
    }

    private void refreshPulseColorController(ColorController colorController) {
        if (colorController == null) return;
        colorController.setColorType(mPulseColorMode);
        colorController.setCustomColor(mPulseColor);
        colorController.setLavaLampSpeed(mPulseLavaSpeed);
    }
}
