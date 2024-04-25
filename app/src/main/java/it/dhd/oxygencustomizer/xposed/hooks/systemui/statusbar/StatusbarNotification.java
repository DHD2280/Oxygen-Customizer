package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class StatusbarNotification extends XposedMods {

    private final String TAG = this.getClass().getSimpleName() + ": ";
    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private Object mCollapsedStatusBarFragment = null;
    private View mStatusBar;
    private boolean removeChargingCompleteNotification, removeDevMode, removeFlashlightNotification, removeLowBattery;

    public StatusbarNotification(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        removeChargingCompleteNotification = Xprefs.getBoolean("remove_charging_complete_notification", false);
        removeDevMode = Xprefs.getBoolean("remove_dev_mode", false);
        removeFlashlightNotification = Xprefs.getBoolean("remove_flashlight_notification", false);
        removeLowBattery = Xprefs.getBoolean("remove_low_battery_notification", false);

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;



        Class<?> CollapsedStatusBarFragmentClass = findClassIfExists("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.classLoader);

        hookAllConstructors(CollapsedStatusBarFragmentClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mCollapsedStatusBarFragment = param.thisObject;
            }
        });

        findAndHookMethod(CollapsedStatusBarFragmentClass,
                "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mStatusBar = (View) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");
                    }
                });

        //Class<?> OplusGutsContent = findClass("com.oplus.systemui.statusbar.notification.row.OpNotificationGuts.OplusGutsContent", lpparam.classLoader);
        //Class<?> NotificationMenuRowExtImpl = findClass("com.oplus.systemui.statusbar.notification.row.NotificationMenuRowExtImpl", lpparam.classLoader);

        Class<?> OplusPowerNotificationWarnings;
        try {
            OplusPowerNotificationWarnings = findClass("com.oplus.systemui.statusbar.notification.power.OplusPowerNotificationWarnings", lpparam.classLoader);
        } catch (Throwable t) {
            OplusPowerNotificationWarnings = findClass("com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings", lpparam.classLoader); // OOS 13
        }
        findAndHookMethod(OplusPowerNotificationWarnings, "showChargeErrorDialog",
                int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (removeChargingCompleteNotification && (int) param.args[0] == 7) {
                            param.setResult(null);
                        }
                    }
                });

        findAndHookMethod(OplusPowerNotificationWarnings, "showLowBatteryDialog",
                Context.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (removeLowBattery) param.setResult(null);
                    }
                });

        Class<?> FlashlightNotification;
        try {
            FlashlightNotification = findClass("com.oplus.systemui.statusbar.notification.flashlight.FlashlightNotification", lpparam.classLoader);
        } catch (Throwable t) {
            FlashlightNotification = findClass("com.oplusos.systemui.flashlight.FlashlightNotification", lpparam.classLoader); // OOS 13
        }
        findAndHookMethod(FlashlightNotification, "sendNotification",
                boolean.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (removeFlashlightNotification) param.setResult(null);
                    }
                });

        Class<?> SystemPromptController;
        try {
            SystemPromptController = findClass("com.oplus.systemui.statusbar.controller.SystemPromptController", lpparam.classLoader);
        } catch (Throwable t) {
            SystemPromptController = findClass("com.oplusos.systemui.controller.SystemPromptController", lpparam.classLoader); // OOS 13
        }
        findAndHookMethod(SystemPromptController, "updateDeveloperMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (removeDevMode) param.setResult(null);
            }
        });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }


}
