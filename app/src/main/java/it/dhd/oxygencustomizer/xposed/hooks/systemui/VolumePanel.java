package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_CUSTOMIZE_BG;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_CUSTOMIZE_PROGRESS;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_DISABLE_WARNING;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_PROGRESS_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_PROGRESS_LINK_PRIMARY;
import static it.dhd.oxygencustomizer.utils.Constants.SoundPrefs.VOLUME_PANEL_TIMEOUT;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;

import java.sql.Ref;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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

        mDesiredTimeout = Xprefs.getSliderInt(VOLUME_PANEL_TIMEOUT, 3);
        mTimeOut = mDesiredTimeout * 1000;
        mDisableVolumeWarning = Xprefs.getBoolean(VOLUME_PANEL_DISABLE_WARNING, false);
        customizeVolumeProgress = Xprefs.getBoolean(VOLUME_PANEL_CUSTOMIZE_PROGRESS, false);
        customizeVolumeBg = Xprefs.getBoolean(VOLUME_PANEL_CUSTOMIZE_BG, false);
        volumeProgressPrimary = Xprefs.getBoolean(VOLUME_PANEL_PROGRESS_LINK_PRIMARY, false);
        volumeProgressColor = Xprefs.getInt(VOLUME_PANEL_PROGRESS_COLOR, 0);
        volumeBgColor = Xprefs.getInt(VOLUME_PANEL_BG_COLOR, Color.GRAY);

        if (Key.length > 0) {
            if (Key[0].equals(VOLUME_PANEL_CUSTOMIZE_PROGRESS) ||
                    Key[0].equals(VOLUME_PANEL_PROGRESS_LINK_PRIMARY) ||
                    Key[0].equals(VOLUME_PANEL_PROGRESS_COLOR) ||
                    Key[0].equals(VOLUME_PANEL_CUSTOMIZE_BG) ||
                    Key[0].equals(VOLUME_PANEL_BG_COLOR)) {
                updateVolumePanel();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        ReflectedClass OplusVolumeSeekBar = ReflectedClass.ofIfPossible("com.oplus.systemui.volume.OplusVolumeSeekBar");
        sliderCustomizable = OplusVolumeSeekBar.getClazz() != null;

        ReflectedClass OplusVolumeDialogImpl = ReflectedClass.of(
                "com.oplus.systemui.volume.OplusVolumeDialogImpl",
                "com.oplusos.systemui.volume.VolumeDialogImplEx" // OOS 13
        );
        OplusVolumeDialogImpl
                .before("computeTimeoutH")
                .run(param -> {
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
                });

        ReflectedClass.ReflectionConsumer panelDisabled = param -> {
            if (mDisableVolumeWarning) {
                try {
                    callMethod(SystemUtils.AudioManager(), "disableSafeMediaVolume");
                } catch (Throwable ignored) {
                }
                param.setResult(null);
            }
        };

        ReflectedClass VolumeDialogImpl = ReflectedClass.of("com.android.systemui.volume.VolumeDialogImpl");
        VolumeDialogImpl
                .before("showSafetyWarningH")
                .run(panelDisabled);
        VolumeDialogImpl
                .before("onShowSafetyWarning")
                .run(panelDisabled);
        OplusVolumeDialogImpl
                .before("showSafetyWarningH")
                .run(panelDisabled);
        OplusVolumeDialogImpl
                .before("onShowSafetyWarning")
                .run(panelDisabled);

        try {
            ReflectedClass OplusQsVolumeController = ReflectedClass.of("com.oplus.systemui.qs.slider.OplusQsVolumeController");
            OplusQsVolumeController
                    .afterConstruction()
                            .run(param -> {
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
                            });
        } catch (Throwable t) {
            log("Error OplusQsVolumeController: " + t.getMessage());
        }

        OplusVolumeDialogImpl
                .afterConstruction()
                        .run(param -> OVDI = param.thisObject);

        OplusVolumeDialogImpl
                .after("initRow")
                        .run(param -> {
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
