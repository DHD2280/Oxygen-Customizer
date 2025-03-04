package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_NOW_BAR_EXPANDED_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_LEFT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CHARGING_ICON_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CHARGING_ICON_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_4;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CUSTOM_COLORS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_FAST_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_INDICATE_FAST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_INDICATE_POWERSAVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_POWERSAVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_TEXT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXDENDED_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXTENDED_BACKGROUND;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXTENDED_CLOCK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXTENDED_PLAYER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATIONS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATION_IGNORE_SECURITY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER_BACKGROUND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER_CUSTOM_COLORS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER_TEXT_COLOR;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.isLeftAffordanceHidden;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarController;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarHolder;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarMusic;

public class LockscreenNowBar extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;

    private ViewGroup mNotificationPanelView = null;
    private final FrameLayout mNowBarLayout = new FrameLayout(mContext);
    private View mKeyguardTipView = null;

    private final ControllersProvider.OnDozingChanged mDozingChanged = isDozing -> NowBarController.getInstance().setDozing(isDozing);

    private boolean mNowBarEnabled = true;
    private boolean mHideLeftAfforfance = false, mHideRightAffordance = false;
    private int mNowBarBottomMargin = 0;
    private boolean mNowBarWeather = false;
    private boolean mNowBarNotification = false;
    private boolean mExpanded = false;

    // Music
    private boolean mShowExtendedPlayer = true;
    private int mBackgroundMode = 0;
    private int mExtendedPlayerMode = 0;
    private boolean mExtendedPlayerShowClock = false;

    // Battery
    private boolean mNowBarBatteryCustomColors = false;
    private int mNowBarBatteryColor1 = Color.parseColor("#FFFF0000"),
            mNowBarBatteryColor2 = Color.parseColor("#FFFFA500"),
            mNowBarBatteryColor3 = Color.parseColor("#FF48D5C4"),
            mNowBarBatteryColor4 = Color.parseColor("#FF4CAF50");
    private boolean mNowBarCustomChargingIcon = false;
    private int mNowBarBatteryChargingIconStyle = -1;
    private boolean mNowBarBatteryIndicateFastCharging = false, mNowBarBatteryIndicatePowerSave = false;
    private int mNowBarBatteryFastChargingColor, mNowBarBatteryPowerSaveColor;
    private int mNowBarBatteryTextColor = Color.WHITE;

    // Weather
    private boolean mNowBarWeatherCustomColors = false;
    private int mNowBarWeatherBackgroundColor = Color.parseColor("#0D47A1");
    private int mNowBarWeatherTextColor = Color.WHITE;

    // Notification
    private boolean mNowBarNotificationIgnoreSecurity = false;

    private int mAffordanceWidth = 0;

    public LockscreenNowBar(Context context) {
        super(context);
        int resId = mContext.getResources().getIdentifier(
                "keyguard_affordance_width", "dimen", SYSTEM_UI);
        if (resId != 0) {
            mAffordanceWidth = mContext.getResources().getDimensionPixelSize(resId);
            mAffordanceWidth = (int) (mAffordanceWidth * 0.9f);
        } else {
            mAffordanceWidth = dp2px(mContext, 72);
        }
    }

    @Override
    public void updatePrefs(String... Key) {

        mNowBarEnabled = Xprefs.getBoolean(NOW_BAR_ENABLED, false);
        mHideLeftAfforfance = Xprefs.getBoolean(LOCKSCREEN_REMOVE_LEFT_AFFORDANCE, false);
        mHideRightAffordance = Xprefs.getBoolean(LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE, false);
        mNowBarBottomMargin = Xprefs.getSliderInt(NOW_BAR_BOTTOM_MARGIN, 12);
        mNowBarWeather = Xprefs.getBoolean(NOW_BAR_WEATHER, false);
        mNowBarNotification = Xprefs.getBoolean(NOW_BAR_NOTIFICATIONS, false);

        // Music
        mShowExtendedPlayer = Xprefs.getBoolean(NOW_BAR_MUSIC_EXTENDED_PLAYER, true);
        mBackgroundMode = Integer.parseInt(Xprefs.getString(NOW_BAR_MUSIC_EXTENDED_BACKGROUND, "0"));
        mExtendedPlayerMode = Integer.parseInt(Xprefs.getString(NOW_BAR_MUSIC_EXDENDED_MODE, "0"));
        mExtendedPlayerShowClock = Xprefs.getBoolean(NOW_BAR_MUSIC_EXTENDED_CLOCK, false);

        // Battery
        mNowBarBatteryCustomColors = Xprefs.getBoolean(NOW_BAR_BATTERY_CUSTOM_COLORS, false);
        mNowBarBatteryColor1 = Xprefs.getInt(NOW_BAR_BATTERY_COLOR_1, Color.parseColor("#FFFF0000"));
        mNowBarBatteryColor2 = Xprefs.getInt(NOW_BAR_BATTERY_COLOR_2, Color.parseColor("#FFFFA500"));
        mNowBarBatteryColor3 = Xprefs.getInt(NOW_BAR_BATTERY_COLOR_3, Color.parseColor("#FF48D5C4"));
        mNowBarBatteryColor4 = Xprefs.getInt(NOW_BAR_BATTERY_COLOR_4, Color.parseColor("#FF4CAF50"));
        mNowBarCustomChargingIcon = Xprefs.getBoolean(NOW_BAR_BATTERY_CHARGING_ICON_SWITCH, false);
        mNowBarBatteryChargingIconStyle = Integer.parseInt(Xprefs.getString(NOW_BAR_BATTERY_CHARGING_ICON_STYLE, "-1"));
        mNowBarBatteryIndicateFastCharging = Xprefs.getBoolean(NOW_BAR_BATTERY_INDICATE_FAST, false);
        mNowBarBatteryIndicatePowerSave = Xprefs.getBoolean(NOW_BAR_BATTERY_INDICATE_POWERSAVE, false);
        mNowBarBatteryFastChargingColor = Xprefs.getInt(NOW_BAR_BATTERY_FAST_COLOR, Color.parseColor("#FF247CFF"));
        mNowBarBatteryPowerSaveColor = Xprefs.getInt(NOW_BAR_BATTERY_POWERSAVE_COLOR, Color.parseColor("#FFA500"));
        mNowBarBatteryTextColor = Xprefs.getInt(NOW_BAR_BATTERY_TEXT_COLOR, Color.WHITE);

        // Weather
        mNowBarWeatherCustomColors = Xprefs.getBoolean(NOW_BAR_WEATHER_CUSTOM_COLORS, false);
        mNowBarWeatherBackgroundColor = Xprefs.getInt(NOW_BAR_WEATHER_BACKGROUND_COLOR, Color.parseColor("#0D47A1"));
        mNowBarWeatherTextColor = Xprefs.getInt(NOW_BAR_WEATHER_TEXT_COLOR, Color.WHITE);

        // Notification
        mNowBarNotificationIgnoreSecurity = Xprefs.getBoolean(NOW_BAR_NOTIFICATION_IGNORE_SECURITY, false);

        if (Key.length > 0) {
            if (Key[0].equals(NOW_BAR_ENABLED) ||
                    Key[0].equals(LOCKSCREEN_REMOVE_LEFT_AFFORDANCE) ||
                    Key[0].equals(LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE) ||
                    Key[0].equals(NOW_BAR_BOTTOM_MARGIN) ||
                    Key[0].equals(NOW_BAR_WEATHER) ||
                    Key[0].equals(NOW_BAR_NOTIFICATIONS)) {
                updateNowBar();
            }
            if (Key[0].equals(NOW_BAR_MUSIC_EXTENDED_PLAYER) ||
                    Key[0].equals(NOW_BAR_MUSIC_EXTENDED_BACKGROUND) ||
                    Key[0].equals(NOW_BAR_MUSIC_EXDENDED_MODE) ||
                    Key[0].equals(NOW_BAR_MUSIC_EXTENDED_CLOCK)) {
                updateMusic();
            }
            for (String k : NOW_BAR_BATTERY_PREFS) {
                if (Key[0].equals(k)) {
                    updateBattery();
                    break;
                }
            }
            if (Key[0].equals(NOW_BAR_WEATHER_BACKGROUND_COLOR) ||
                    Key[0].equals(NOW_BAR_WEATHER_TEXT_COLOR) ||
                    Key[0].equals(NOW_BAR_WEATHER_CUSTOM_COLORS)) {
                updateWeather();
            }
            if (Key[0].equals(NOW_BAR_NOTIFICATION_IGNORE_SECURITY)) {
                updateNotification();
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> NotificationPanelViewController = findClass("com.android.systemui.shade.NotificationPanelViewController", lpparam.classLoader);
        hookAllMethods(NotificationPanelViewController, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                XposedBridge.log("LockscreenNowBar NotificationPanelViewController onFinishInflate");
                mNotificationPanelView = (ViewGroup) getObjectField(param.thisObject, "mView");
                placeNowBar();
            }
        });

        ReflectedClass KeyguardStatusViewController = ReflectedClass.of("com.android.keyguard.KeyguardStatusViewController");
        KeyguardStatusViewController
                .after("setAlpha")
                .run(param -> {
                    mNowBarLayout.setAlpha((float) param.args[0]);
                });

        Class<?> QSImpl = findClassInArray(
                lpparam,
                "com.android.systemui.qs.QSImpl", //OOS15
                "com.android.systemui.qs.QSFragment" //OOS14
        );
        hookAllMethods(QSImpl, "setQsExpansion", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean isFullyCollapsed = (boolean) callMethod(param.thisObject, "isFullyCollapsed");
                if (NowBarController.hasInstance()) {
                    NowBarController.getInstance().setFullyCollapsed(isFullyCollapsed);
                }
            }
        });

        Class<?> KeyguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
        hookAllMethods(KeyguardUpdateMonitor, "setKeyguardShowing", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (NowBarController.hasInstance()) {
                    NowBarController.getInstance().setKeyguardShowing((boolean) param.args[0]);
                }
            }
        });

        ReflectedClass KeyguardGlideTipView = ReflectedClass.ofIfPossible("com.oplusos.systemui.keyguard.view.KeyguardGlideTipView", lpparam.classLoader);
        if (KeyguardGlideTipView.getClazz() != null) {
            KeyguardGlideTipView
                    .after("onFinishInflate")
                    .run(param -> mKeyguardTipView = (View) param.thisObject);
            KeyguardGlideTipView
                    .before("getIfNeedHide")
                    .run(param -> {
                        if (mNowBarEnabled && mExpanded) {
                            param.setResult(true);
                        }
                    });
        }

        ControllersProvider.registerDozingCallback(mDozingChanged);

    }

    private final NowBarMusic.MusicExpansionListener musicExpansionListener = expanded -> {
        mExpanded = expanded;
        updateTipView();
        Intent intent = new Intent(ACTIONS_NOW_BAR_EXPANDED_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("isExpanded", expanded);
        mContext.sendBroadcast(intent);
    };

    private void updateTipView() {
        try {
            if (mExpanded) mKeyguardTipView.setVisibility(View.GONE);
        } catch (Throwable ignored) {
        }
    }

    private void placeNowBar() {
        XposedBridge.log("LockscreenNowBar, placeNowBar");
        if (mNotificationPanelView == null) return;
        NowBarHolder mNowBarHolder = NowBarHolder.getInstance(mContext, musicExpansionListener);
        try {
            ((ViewGroup) mNowBarHolder.getParent()).removeView(mNowBarHolder);
        } catch (Throwable ignored) {
        }
        mNowBarLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        mNowBarLayout.addView(mNowBarHolder);
        mNotificationPanelView.addView(mNowBarLayout, mNotificationPanelView.getChildCount() - 1);
        updateNowBar();
        updateMusic();
        updateBattery();
        updateWeather();
        updateNotification();
    }

    private void updateNowBar() {
        if (BuildConfig.DEBUG) {
            XposedBridge.log("LockscreenNowBar, updateNowBar" + "\n" +
                    "mNowBarEnabled: " + mNowBarEnabled + "\n" +
                    "mHideLeftAfforfance: " + mHideLeftAfforfance + "\n" +
                    "mHideRightAffordance: " + mHideRightAffordance + "\n" +
                    "mNowBarBottomMargin: " + mNowBarBottomMargin + "\n" +
                    "mNowBarWeather: " + mNowBarWeather);
        }
        NowBarController mNowBarController = NowBarController.getInstance();
        mNowBarController.setNowBarEnabled(mNowBarEnabled);
        mNowBarController.setNowBarWeatherEnabled(mNowBarWeather);
        mNowBarController.setNowBarNotificationEnabled(mNowBarNotification);
        updateBarMargins();
    }

    private void updateBarMargins() {
        boolean isLeftHidden = isLeftAffordanceHidden(mContext, mHideLeftAfforfance);
        NowBarController mNowBarController = NowBarController.getInstance();
        mNowBarController.updateMargins(
                isLeftHidden && mHideRightAffordance ? dp2px(mContext, 12) : mAffordanceWidth,
                isLeftHidden && mHideRightAffordance ? dp2px(mContext, 12) : mAffordanceWidth,
                dp2px(mContext, mNowBarBottomMargin)
        );
    }

    private void updateMusic() {
        NowBarController mNowBarController = NowBarController.getInstance();
        if (mNowBarController == null) return;
        mNowBarController.updateMusic(mShowExtendedPlayer, mBackgroundMode, mExtendedPlayerMode, mExtendedPlayerShowClock);
    }

    private void updateBattery() {
        NowBarController mNowBarController = NowBarController.getInstance();
        if (mNowBarController == null) return;
        mNowBarController.updateBattery(
                mNowBarCustomChargingIcon ? mNowBarBatteryChargingIconStyle : -1,
                mNowBarBatteryCustomColors, mNowBarBatteryColor1, mNowBarBatteryColor2, mNowBarBatteryColor3, mNowBarBatteryColor4,
                mNowBarBatteryIndicateFastCharging, mNowBarBatteryFastChargingColor, mNowBarBatteryIndicatePowerSave, mNowBarBatteryPowerSaveColor,
                mNowBarBatteryTextColor
        );
    }

    private void updateWeather() {
        NowBarController mNowBarController = NowBarController.getInstance();
        if (mNowBarController == null) return;
        mNowBarController.updateWeather(
                mNowBarWeatherCustomColors, mNowBarWeatherTextColor, mNowBarWeatherBackgroundColor
        );
    }

    private void updateNotification() {
        NowBarController mNowBarController = NowBarController.getInstance();
        if (mNowBarController == null) return;
        mNowBarController.updateNotification(mNowBarNotificationIgnoreSecurity);
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(listenPackage);
    }
}
