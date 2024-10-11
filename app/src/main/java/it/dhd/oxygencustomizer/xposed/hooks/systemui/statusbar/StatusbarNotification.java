package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_ALL_BUTTON_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CUSTOMIZE_CLEAR_BUTTON;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import java.util.Collection;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class StatusbarNotification extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    // Notification Expander
    private static final int DEFAULT = 0;
    private static final int EXPAND_ALWAYS = 1;
    /**
     * @noinspection unused
     */
    private static final int COLLAPSE_ALWAYS = 2;
    private static int notificationDefaultExpansion = DEFAULT;
    private static Drawable defaultClearAllIcon = null, defaultClearAllBg = null;
    private final String TAG = this.getClass().getSimpleName() + ": ";
    private Object mCollapsedStatusBarFragment = null;
    private View mStatusBar;
    private boolean removeChargingCompleteNotification, removeDevMode, removeFlashlightNotification, removeLowBattery;
    private Object Scroller;
    private Object NotifCollection = null;
    private ImageView mClearAllButton = null;
    // Close All Notification Button
    private boolean customizeClearButton = false, linkBackgroundAccent = true, linkIconAccent = false;
    private int clearButtonBgColor = Color.GRAY, clearButtonIconColor = Color.WHITE;

    public StatusbarNotification(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        removeChargingCompleteNotification = Xprefs.getBoolean("remove_charging_complete_notification", false);
        removeDevMode = Xprefs.getBoolean("remove_dev_mode", false);
        removeFlashlightNotification = Xprefs.getBoolean("remove_flashlight_notification", false);
        removeLowBattery = Xprefs.getBoolean("remove_low_battery_notification", false);
        notificationDefaultExpansion = Integer.parseInt(Xprefs.getString("notificationDefaultExpansion", "0"));
        customizeClearButton = Xprefs.getBoolean(CUSTOMIZE_CLEAR_BUTTON, false);
        linkBackgroundAccent = Xprefs.getBoolean(CLEAR_BUTTON_BG_LINK_ACCENT, true);
        linkIconAccent = Xprefs.getBoolean(CLEAR_BUTTON_ICON_LINK_ACCENT, false);
        clearButtonBgColor = Xprefs.getInt(CLEAR_BUTTON_BG_COLOR, Color.GRAY);
        clearButtonIconColor = Xprefs.getInt(CLEAR_BUTTON_ICON_COLOR, Color.WHITE);

        if (Key.length > 0) {
            for (String k : CLEAR_ALL_BUTTON_PREFS)
                if (k.equals(Key[0])) {
                    updateButton();
                }
        }
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
            SystemPromptController = findClass("com.oplusos.systemui.statusbar.policy.SystemPromptController", lpparam.classLoader); // OOS 13
        }
        findAndHookMethod(SystemPromptController, "updateDeveloperMode", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (removeDevMode) param.setResult(null);
            }
        });

        Class<?> NotificationStackScrollLayoutClass = findClass("com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout", lpparam.classLoader);
        Class<?> NotifCollectionClass = findClassIfExists("com.android.systemui.statusbar.notification.collection.NotifCollection", lpparam.classLoader);
        Class<?> NotificationPanelViewControllerClass;
        try {
            NotificationPanelViewControllerClass = findClass("com.android.systemui.shade.NotificationPanelViewController", lpparam.classLoader);
        } catch (Throwable e) {
            NotificationPanelViewControllerClass = findClass("com.android.systemui.statusbar.phone.NotificationPanelViewController", lpparam.classLoader);
        }

        //region default notification state
        hookAllMethods(NotificationPanelViewControllerClass, "notifyExpandingStarted", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                if (notificationDefaultExpansion != DEFAULT)
                    expandAll(notificationDefaultExpansion == EXPAND_ALWAYS);
            }
        });
        //endregion

        //grab notification container manager
        if (NotifCollectionClass != null) {
            hookAllConstructors(NotifCollectionClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    NotifCollection = param.thisObject;
                }
            });
        }

        //grab notification scroll page
        hookAllConstructors(NotificationStackScrollLayoutClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Scroller = param.thisObject;
            }
        });

        Class<?> OplusClearAllButton;
        try {
            OplusClearAllButton = findClass("com.oplus.systemui.statusbar.notification.view.OplusClearAllButton", lpparam.classLoader); // OOS 14
        } catch (Throwable t) {
            OplusClearAllButton = findClass("com.oplusos.systemui.notification.view.OplusClearAllButton", lpparam.classLoader); // OOS 13
        }

        final View.OnLayoutChangeListener listener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (v.getVisibility() == View.VISIBLE) updateButton();
        };

        hookAllConstructors(OplusClearAllButton, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mClearAllButton = (ImageView) param.thisObject;
                if (defaultClearAllIcon == null && mClearAllButton != null) {
                    defaultClearAllIcon = mClearAllButton.getDrawable();
                }
                if (defaultClearAllBg == null && mClearAllButton != null) {
                    defaultClearAllBg = mClearAllButton.getBackground();
                }
                updateButton();
                mClearAllButton.addOnLayoutChangeListener(listener);
            }
        });
    }

    public void expandAll(boolean expand) {
        if (NotifCollection == null) return;

        if (!expand) {
            callMethod(
                    Scroller,
                    "setOwnScrollY",
                    /* pisition */0,
                    /* animate */ true);
        }

        Collection<Object> entries;
        //noinspection unchecked
        entries = (Collection<Object>) getObjectField(NotifCollection, "mReadOnlyNotificationSet");
        for (Object entry : entries.toArray()) {
            Object row = getObjectField(entry, "row");
            if (row != null) {
                setRowExpansion(row, expand);
            }
        }

    }

    private void setRowExpansion(Object row, boolean expand) {
        callMethod(row, "setUserExpanded", expand, true);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }


    private void updateButton() {
        if (mClearAllButton == null) return;
        if (customizeClearButton) {
            Drawable customBg = defaultClearAllBg;
            if (linkBackgroundAccent) {
                customBg.setTint(getPrimaryColor(mContext));
            } else {
                customBg.setTint(clearButtonBgColor);
            }
            Drawable icon = defaultClearAllIcon;
            if (linkIconAccent)
                icon.setTint(getPrimaryColor(mContext));
            else
                icon.setTint(clearButtonIconColor);
            mClearAllButton.setBackground(customBg);
            mClearAllButton.setImageDrawable(icon);
        } else {
            if (defaultClearAllIcon != null) {
                mClearAllButton.setImageDrawable(defaultClearAllIcon);
            }
            if (defaultClearAllBg != null) {
                mClearAllButton.setBackground(defaultClearAllBg);
            }
        }
    }

}
