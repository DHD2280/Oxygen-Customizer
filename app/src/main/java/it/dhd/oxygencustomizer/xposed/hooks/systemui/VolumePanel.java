package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class VolumePanel extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private int mTimeOut;
    private int mDesiredTimeout;
    private boolean mDisableVolumeWarning;
    private boolean customizeVolumeProgress, customizeVolumeBg;
    private boolean volumeProgressPrimary;
    private int volumeProgressColor, volumeBgColor;
    private Object OVDI;
    private boolean sliderCustomizable = false;

    public VolumePanel(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mDesiredTimeout = Xprefs.getSliderInt("volume_dialog_timeout", 3);
        mTimeOut = mDesiredTimeout * 1000;
        mDisableVolumeWarning = Xprefs.getBoolean("volume_disable_volume_warning", false);
        customizeVolumeProgress = Xprefs.getBoolean("volume_panel_seekbar_color_enabled", false);
        customizeVolumeBg = Xprefs.getBoolean("volume_panel_seekbar_bg_color_enabled", false);
        volumeProgressPrimary = Xprefs.getBoolean("volume_panel_seekbar_link_primary", false);
        volumeProgressColor = Xprefs.getInt("volume_panel_seekbar_color", 0);
        volumeBgColor = Xprefs.getInt("volume_panel_seekbar_bg_color", Color.GRAY);

        if (Key.length > 0) {
            if (Key[0].equals("volume_panel_seekbar_color_enabled") ||
                    Key[0].equals("volume_panel_seekbar_link_primary") ||
                    Key[0].equals("volume_panel_seekbar_color") ||
                    Key[0].equals("volume_panel_seekbar_bg_color_enabled") ||
                    Key[0].equals("volume_panel_seekbar_bg_color")) {
                updateVolumePanel();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        try {
            Class<?> OplusVolumeSeekBar = findClass("com.oplus.systemui.volume.OplusVolumeSeekBar", lpparam.classLoader);
            sliderCustomizable = true;
        } catch (Throwable ignored) {
            sliderCustomizable = false;
        }

        Class<?> OplusVolumeDialogImpl;
        try {
            OplusVolumeDialogImpl = findClass("com.oplus.systemui.volume.OplusVolumeDialogImpl", lpparam.classLoader);
        } catch (Throwable t) {
            OplusVolumeDialogImpl = findClass("com.oplusos.systemui.volume.VolumeDialogImplEx", lpparam.classLoader); // OOS 13
        }
        hookAllMethods(OplusVolumeDialogImpl, "computeTimeoutH", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                if (mDesiredTimeout == 3) return;

                if (getBooleanField(param.thisObject, "mHovering")) {
                    param.setResult(callMethod(getObjectField(param.thisObject, "mAccessibilityMgr"), "getRecommendedTimeoutMillis", 16000, 4));
                }
                synchronized (getObjectField(param.thisObject, "mSafetyWarningLock")) {
                    if (getBooleanField(param.thisObject, "mExpanded")) {
                        param.setResult(callMethod(getObjectField(param.thisObject, "mAccessibilityMgr"), "getRecommendedTimeoutMillis", 5000, 4));
                    } else {
                        param.setResult(mTimeOut);
                    }
                }
            }
        });

        Class<?> VolumeDialogImpl = findClass("com.android.systemui.volume.VolumeDialogImpl", lpparam.classLoader);
        hookAllMethods(VolumeDialogImpl, "showSafetyWarningH", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (mDisableVolumeWarning) {
                    try {
                        callMethod(SystemUtils.AudioManager(), "disableSafeMediaVolume");
                    } catch (Throwable ignored) {
                    }
                    param.setResult(null);
                }
            }
        });
        hookAllMethods(VolumeDialogImpl, "onShowSafetyWarning", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (mDisableVolumeWarning) {
                    try {
                        callMethod(SystemUtils.AudioManager(), "disableSafeMediaVolume");
                    } catch (Throwable ignored) {
                    }
                    param.setResult(null);
                }
            }
        });

        try {
            hookAllMethods(OplusVolumeDialogImpl, "showSafetyWarningH", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (mDisableVolumeWarning) {
                        try {
                            callMethod(SystemUtils.AudioManager(), "disableSafeMediaVolume");
                        } catch (Throwable ignored) {
                        }
                        param.setResult(null);
                    }
                }
            });

        } catch (Throwable t) {
            log("Error: " + t.getMessage());
        }

        try {
            Class<?> OplusQsVolumeController = findClass("com.oplus.systemui.qs.slider.OplusQsVolumeController", lpparam.classLoader);
            hookAllConstructors(OplusQsVolumeController, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        Object volumeCallback = getObjectField(param.thisObject, "volumeCallback");
                        hookAllMethods(volumeCallback.getClass(), "onShowSafetyWarning", new XC_MethodHook() {
                            @Override
                            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                if (mDisableVolumeWarning) {
                                    param.setResult(null);
                                }
                            }
                        });
                    } catch (Throwable t) {
                        log("OplusQsVolumeController, no volumeCallback " + t.getMessage());
                    }
                }
            });
        } catch (Throwable t) {
            log("Error OplusQsVolumeController: " + t.getMessage());
        }

        hookAllConstructors(OplusVolumeDialogImpl, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                OVDI = param.thisObject;
            }
        });

        hookAllMethods(OplusVolumeDialogImpl, "initRow", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!sliderCustomizable) return;

                Object VolumeRow = param.args[0];
                Object slider = getObjectField(VolumeRow, "slider");
                if (customizeVolumeProgress) {
                    if (volumeProgressPrimary)
                        callMethod(slider, "setProgressColor", ColorStateList.valueOf(getPrimaryColor(mContext)));
                    else
                        callMethod(slider, "setProgressColor", ColorStateList.valueOf(volumeProgressColor));
                }
                if (customizeVolumeBg) {
                    callMethod(slider, "setSeekBarBackgroundColor", ColorStateList.valueOf(volumeBgColor));
                }

            }
        });

    }

    private void updateVolumePanel() {
        if (OVDI == null) return;

        if (!sliderCustomizable) return;

        List<Object> mRows = (List<Object>) getObjectField(OVDI, "mRows");
        for (Object VolumeRow : mRows) {
            Object slider = getObjectField(VolumeRow, "slider");
            if (customizeVolumeProgress) {
                if (volumeProgressPrimary)
                    callMethod(slider, "setProgressColor", ColorStateList.valueOf(getPrimaryColor(mContext)));
                else
                    callMethod(slider, "setProgressColor", ColorStateList.valueOf(volumeProgressColor));
            }
            if (customizeVolumeBg) {
                callMethod(slider, "setSeekBarBackgroundColor", ColorStateList.valueOf(volumeBgColor));
            }
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
