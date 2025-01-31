package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
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
import android.view.View;
import android.widget.ImageView;

import java.util.Collection;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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

        ReflectedClass CollapsedStatusBarFragmentClass = ReflectedClass.ofIfPossible("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment");

        if (CollapsedStatusBarFragmentClass.getClazz() != null) {
            CollapsedStatusBarFragmentClass
                    .afterConstruction()
                    .run(param -> mCollapsedStatusBarFragment = param.thisObject);

            CollapsedStatusBarFragmentClass
                    .after("onViewCreated")
                    .run(param -> mStatusBar = (View) getObjectField(mCollapsedStatusBarFragment, "mStatusBar"));
            
        }

        //ReflectedClass OplusGutsContent = ReflectedClass.of("com.oplus.systemui.statusbar.notification.row.OpNotificationGuts.OplusGutsContent");
        //ReflectedClass NotificationMenuRowExtImpl = ReflectedClass.of("com.oplus.systemui.statusbar.notification.row.NotificationMenuRowExtImpl");

        ReflectedClass OplusPowerNotificationWarnings = ReflectedClass.of(
                "com.oplus.systemui.statusbar.notification.power.OplusPowerNotificationWarnings", /* OOS 15-14 */
                "com.oplusos.systemui.notification.power.OplusPowerNotificationWarnings" /* OOS 13 */
        );
        OplusPowerNotificationWarnings
                .before("showChargeErrorDialog")
                        .run(param -> {
                            if (removeChargingCompleteNotification && (int) param.args[0] == 7) {
                                param.setResult(null);
                            }
                        });

        OplusPowerNotificationWarnings
                .before("showLowBatteryDialog")
                        .run(param -> {
                            if (removeLowBattery) param.setResult(null);
                        });

        ReflectedClass FlashlightNotification = ReflectedClass.of(
                "com.oplus.systemui.statusbar.notification.flashlight.FlashlightNotification", /* OOS 15-14 */
                "com.oplusos.systemui.flashlight.FlashlightNotification" /* OOS 13 */
        );
        FlashlightNotification
                .before("sendNotification")
                        .run(param -> {
                            if (removeFlashlightNotification) param.setResult(null);
                        });

        ReflectedClass SystemPromptController = ReflectedClass.of(
                "com.oplus.systemui.statusbar.controller.SystemPromptController", /* OOS 15-14 */
                "com.oplusos.systemui.statusbar.policy.SystemPromptController" /* OOS 13 */
        );
        SystemPromptController
                .before("updateDeveloperMode")
                        .run(param -> {
                            if (removeDevMode) param.setResult(null);
                        });

        ReflectedClass NotificationStackScrollLayoutClass = ReflectedClass.of("com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout");
        ReflectedClass NotifCollectionClass = ReflectedClass.ofIfPossible("com.android.systemui.statusbar.notification.collection.NotifCollection");
        ReflectedClass NotificationPanelViewControllerClass = ReflectedClass.of(
                "com.android.systemui.shade.NotificationPanelViewController", /* OOS 15-14 */
                "com.android.systemui.statusbar.phone.NotificationPanelViewController" /* OOS 13 */
        );

        //region default notification state
        NotificationPanelViewControllerClass
                .before("notifyExpandingStarted")
                        .run(param -> {
                            if (notificationDefaultExpansion != DEFAULT) expandAll(notificationDefaultExpansion == EXPAND_ALWAYS);
                        });
        //endregion

        //grab notification container manager
        if (NotifCollectionClass.getClazz() != null) {
            NotifCollectionClass.afterConstruction().run(param -> NotifCollection = param.thisObject);
        }

        //grab notification scroll page
        NotificationStackScrollLayoutClass
                .afterConstruction()
                .run(param -> Scroller = param.thisObject);

        ReflectedClass OplusClearAllButton = ReflectedClass.of(
                "com.oplus.systemui.statusbar.notification.view.OplusClearAllButton", /* OOS 15-14 */
                "com.oplusos.systemui.notification.view.OplusClearAllButton" // OOS 13
        );

        final View.OnLayoutChangeListener listener = (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (v.getVisibility() == View.VISIBLE) updateButton();
        };

        OplusClearAllButton
                .afterConstruction()
                        .run(param -> {
                            mClearAllButton = (ImageView) param.thisObject;
                            if (defaultClearAllIcon == null && mClearAllButton != null) {
                                defaultClearAllIcon = mClearAllButton.getDrawable();
                            }
                            if (defaultClearAllBg == null && mClearAllButton != null) {
                                defaultClearAllBg = mClearAllButton.getBackground();
                            }
                            updateButton();
                            mClearAllButton.addOnLayoutChangeListener(listener);
                        });
    }

    public void expandAll(boolean expand) {
        if (NotifCollection == null) return;

        if (!expand) {
            callMethod(
                    Scroller,
                    "setOwnScrollY",
                    /* position */0,
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
