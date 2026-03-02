package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_ALL_BUTTON_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CUSTOMIZE_CLEAR_BUTTON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CUSTOM_NOTIFICATION_APPS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIFICATIONS_SHOW_BUTTONS;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.coerceIn;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.service.notification.StatusBarNotification;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;

import com.android.systemui.statusbar.AlphaOptimizedImageView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class StatusbarNotification extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    // Notification Expander
    private static final int DEFAULT = 0;
    private static final int EXPAND_ALWAYS = 1;
    private static final int EXPAND_PACKAGE = 3;
    /**
     * @noinspection unused
     */
    private static final int COLLAPSE_ALWAYS = 2;
    private Set<String> notificationApps = new HashSet<>();
    private Map<String, Integer> notificationAppMode = new HashMap<>();
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

    // Expand and Collapse Buttons
    private boolean mShowButtons = false;
    private LinearLayout mNotificationButtonsContainer = new LinearLayout(mContext);

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
        mShowButtons = Xprefs.getBoolean(NOTIFICATIONS_SHOW_BUTTONS, false);
        customizeClearButton = Xprefs.getBoolean(CUSTOMIZE_CLEAR_BUTTON, false);
        linkBackgroundAccent = Xprefs.getBoolean(CLEAR_BUTTON_BG_LINK_ACCENT, true);
        linkIconAccent = Xprefs.getBoolean(CLEAR_BUTTON_ICON_LINK_ACCENT, false);
        clearButtonBgColor = Xprefs.getInt(CLEAR_BUTTON_BG_COLOR, Color.GRAY);
        clearButtonIconColor = Xprefs.getInt(CLEAR_BUTTON_ICON_COLOR, Color.WHITE);
        notificationApps = Xprefs.getStringSet(CUSTOM_NOTIFICATION_APPS, new HashSet<>());
        notificationAppMode = new ArrayMap<>();
        for (String item : notificationApps) {
            if (item.contains("|")) {
                List<String> arr = new ArrayList<>(Arrays.asList(item.split("\\|")));
                if (arr.size() < 2 || arr.get(1).isBlank()) {
                    arr.set(1, "0");
                }
                notificationAppMode.put(arr.get(0), Integer.parseInt(arr.get(1)));
            } else {
                notificationAppMode.put(item, 0);
            }
        }

        if (Key.length > 0) {
            for (String k : CLEAR_ALL_BUTTON_PREFS)
                if (k.equals(Key[0])) {
                    updateButton();
                }
            if (Key[0].equals(NOTIFICATIONS_SHOW_BUTTONS)) {
                setupButtons();
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

        OplusPowerNotificationWarnings
                .before("showLowBatteryWarning")
                .run(param -> {
                    if (removeLowBattery) param.setResult(false);
                });

        OplusPowerNotificationWarnings
                .before("createNotification")
                .run(param -> {
                    if (removeLowBattery) param.setResult(null);
                });

        ReflectedClass FlashlightNotification = ReflectedClass.of(
                "com.oplus.systemui.notification.flashlight.FlashlightNotification", /* OOS 15.0.1+ */
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
        ReflectedClass StatusBarFeatureOption = ReflectedClass.ofIfPossible("com.oplusos.systemui.common.feature.StatusBarFeatureOption");
        if (StatusBarFeatureOption.getClazz() != null) {
            StatusBarFeatureOption
                    .before("getSendDeveloperModeNotification")
                    .run(param -> {
                        if (removeDevMode) param.setResult(false);
                    });
        }

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
                    if (notificationDefaultExpansion != DEFAULT)
                        expandAll(notificationDefaultExpansion);
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
                "com.oplus.systemui.notification.clearall.OplusClearAllButton", /* OOS 15.0.1 */
                "com.oplus.systemui.statusbar.notification.view.OplusClearAllButton", /* OOS 15-14 */
                "com.oplusos.systemui.notification.view.OplusClearAllButton" // OOS 13
        );

        addNotificationButtons();

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

        ReflectedClass ClearAllController = ReflectedClass.ofIfPossible("com.oplus.systemui.notification.clearall.ClearAllController");
        if (ClearAllController.getClazz() != null) {
            ClearAllController
                    .before("getPlatformBlurDrawable")
                    .run(param -> {
                        if (customizeClearButton) {
                            Drawable customBg = (Drawable) param.args[0];
                            if (linkBackgroundAccent) {
                                customBg.setTint(getPrimaryColor(mContext));
                            } else {
                                customBg.setTint(clearButtonBgColor);
                            }
                            param.setResult(customBg);
                        }
                    });
            ClearAllController
                    .after("updateClearAllBackground")
                    .run(param -> {
                        XposedBridge.log("StatusbarNotification updateClearAllBackground" );
                        if (customizeClearButton) {
                            ImageView clearAllButton = (ImageView) getObjectField(param.thisObject, "clearAll");
                            Drawable icon = clearAllButton.getDrawable();
                            if (linkIconAccent)
                                icon.setTint(getPrimaryColor(mContext));
                            else
                                icon.setTint(clearButtonIconColor);
                            icon.invalidateSelf();
                            clearAllButton.setImageDrawable(icon);
                            XposedBridge.log(TAG + "updateClearAllBackground: icon color set to " + clearButtonIconColor);
                        }
                    });
        }

    }

    public void expandAll(int expandMode) {
        if (NotifCollection == null) return;


        if (!(expandMode == EXPAND_ALWAYS)) {
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
            StatusBarNotification mSbn = (StatusBarNotification) getObjectField(entry, "mSbn");
            if (row != null) {
                setRowExpansion(row, expandMode == EXPAND_PACKAGE ?
                        checkExpansion(mSbn.getPackageName()) :
                        expandMode == EXPAND_ALWAYS);
            }
        }

    }

    private boolean checkExpansion(String packageName) {
        if (notificationAppMode == null || notificationAppMode.isEmpty()) return false;
        int value = notificationAppMode.getOrDefault(packageName, 0);
        return value == 1;
    }

    private void setRowExpansion(Object row, boolean expand) {
        callMethod(row, "setUserExpanded", expand, true);
    }

    private void addNotificationButtons() {
        ReflectedClass OplusQSSimpleHeader = ReflectedClass.ofIfPossible("com.oplus.systemui.separate.OplusQSSimpleHeader");

        if (OplusQSSimpleHeader.getClazz() == null) return;

        OplusQSSimpleHeader
                .after("onInit")
                .run(param -> {
                    FrameLayout view = (FrameLayout) param.thisObject;

                    LinearLayout clockContainer = view.findViewById(
                            mContext.getResources().getIdentifier(
                                    "button_container_parent",
                                    "id",
                                    mContext.getPackageName()
                            )
                    );
                    mNotificationButtonsContainer.setTag("notification_buttons_container");
                    mNotificationButtonsContainer.setLayoutParams(
                            new LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                            )
                    );
                    mNotificationButtonsContainer.setOrientation(LinearLayout.HORIZONTAL);
                    mNotificationButtonsContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL);
                    try {
                        ((ViewGroup) mNotificationButtonsContainer.getParent()).removeView(mNotificationButtonsContainer);
                    } catch (Throwable ignored) {}
                    clockContainer.addView(mNotificationButtonsContainer, clockContainer.getChildCount() - 1);

                    AlphaOptimizedImageView mExpand, mCollapse;
                    int resIdWidth = mContext.getResources().getIdentifier("simple_qs_header_button_width", "dimen", SYSTEM_UI);
                    int resIdHeight = mContext.getResources().getIdentifier("qs_footer_settings_button_size", "dimen", SYSTEM_UI);

                    mExpand = new AlphaOptimizedImageView(mContext);
                    mCollapse = new AlphaOptimizedImageView(mContext);

                    mExpand.setLayoutParams(new LinearLayout.LayoutParams(
                            (int) mContext.getResources().getDimension(resIdWidth),
                            (int) mContext.getResources().getDimension(resIdHeight)
                    ));
                    mExpand.setPadding(dp2px(mContext, 4), 0, dp2px(mContext, 4), 0);
                    mCollapse.setLayoutParams(new LinearLayout.LayoutParams(
                            (int) mContext.getResources().getDimension(resIdWidth),
                            (int) mContext.getResources().getDimension(resIdHeight)
                    ));

                    mNotificationButtonsContainer.addView(mExpand);
                    mNotificationButtonsContainer.addView(mCollapse);

                    mExpand.setImageDrawable(ResourcesCompat.getDrawable(modRes, R.drawable.ic_expand, mContext.getTheme()));
                    mCollapse.setImageDrawable(ResourcesCompat.getDrawable(modRes, R.drawable.ic_collapse, mContext.getTheme()));

                    mExpand.setOnClickListener(v -> expandAll(EXPAND_ALWAYS));

                    mCollapse.setOnClickListener(v -> expandAll(COLLAPSE_ALWAYS));

                    setupButtons();
                });

        ControllersProvider.registerExpandedFractionChangeCallback(mExpandedFractionChangeListener);

    }

    private final ControllersProvider.ExpandedFractionChangeListener mExpandedFractionChangeListener = fraction -> {
        if (!mShowButtons) return;
        float alpha = coerceIn(fraction / 0.86f, 0.0f, 1.0f);
        mNotificationButtonsContainer.setAlpha(alpha);
    };

    private void setupButtons() {
        mNotificationButtonsContainer.setVisibility(mShowButtons ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }


    private void updateButton() {
        if (mClearAllButton == null) return;
        if (customizeClearButton) {
            if (defaultClearAllBg != null) {
                Drawable customBg = defaultClearAllBg;
                if (linkBackgroundAccent) {
                    customBg.setTint(getPrimaryColor(mContext));
                } else {
                    customBg.setTint(clearButtonBgColor);
                }
                mClearAllButton.setBackground(customBg);
            }
            Drawable icon = defaultClearAllIcon;
            if (linkIconAccent)
                icon.setTint(getPrimaryColor(mContext));
            else
                icon.setTint(clearButtonIconColor);
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
