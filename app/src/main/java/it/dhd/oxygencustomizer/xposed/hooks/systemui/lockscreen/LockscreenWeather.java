package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.START;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_WEATHER_INFLATED;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_BACKGROUND;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CENTERED;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_HUMIDITY;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_IMAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_CONDITION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_TEXT_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_WIND;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.views.CurrentWeatherView;

public class LockscreenWeather extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private final int mWeatherStartPadding;
    private ViewGroup mStatusViewContainer = null;
    // Weather
    private LinearLayout mWeatherContainer = null;
    private boolean mWeatherEnabled = false, mWeatherShowLocation = true, mWeatherShowCondition = true;
    private boolean mWeatherShowHumidity = true, mWeatherShowWind = true;
    private boolean mWeatherCustomColor = false;
    private int mWeatherColor = Color.WHITE;
    private int mWeatherTextSize = 16;
    private int mWeatherImageSize = 18;
    private boolean mWeatherCustomMargins = false;
    private int mWeatherLeftMargin = 0, mWeatherTopMargin = 0;
    private int mWeatherBackground = 0;
    private boolean mWeatherCentered = false;

    public LockscreenWeather(Context context) {
        super(context);
        @SuppressLint("DiscouragedApi") int resourceId = mContext.getResources().getIdentifier("red_horizontal_single_clock_margin_start", "dimen", listenPackage);
        if (resourceId > 0) {
            mWeatherStartPadding = mContext.getResources().getDimensionPixelSize(resourceId);
        } else {
            mWeatherStartPadding = dp2px(mContext, 32);
        }
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        // Weather
        mWeatherEnabled = Xprefs.getBoolean(LOCKSCREEN_WEATHER_SWITCH, false);
        mWeatherTextSize = Xprefs.getSliderInt(LOCKSCREEN_WEATHER_TEXT_SIZE, 16);
        mWeatherImageSize = Xprefs.getSliderInt(LOCKSCREEN_WEATHER_IMAGE_SIZE, 18);
        mWeatherShowLocation = Xprefs.getBoolean(LOCKSCREEN_WEATHER_SHOW_LOCATION, true);
        mWeatherShowCondition = Xprefs.getBoolean(LOCKSCREEN_WEATHER_SHOW_CONDITION, true);
        mWeatherShowHumidity = Xprefs.getBoolean(LOCKSCREEN_WEATHER_HUMIDITY, true);
        mWeatherShowWind = Xprefs.getBoolean(LOCKSCREEN_WEATHER_WIND, true);
        mWeatherCustomColor = Xprefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH, false);
        mWeatherColor = Xprefs.getInt(LOCKSCREEN_WEATHER_CUSTOM_COLOR, Color.WHITE);
        mWeatherCentered = Xprefs.getBoolean(LOCKSCREEN_WEATHER_CENTERED, false);
        mWeatherCustomMargins = Xprefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_MARGINS, false);
        mWeatherLeftMargin = Xprefs.getSliderInt(LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT, 0);
        mWeatherTopMargin = Xprefs.getSliderInt(LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP, 0);
        mWeatherBackground = Integer.parseInt(Xprefs.getString(LOCKSCREEN_WEATHER_BACKGROUND, "0"));

        for (String LCWeatherPref : LOCKSCREEN_WEATHER_PREFS) {
            if (Key[0].equals(LCWeatherPref)) updateWeatherView();
        }
        if (Key[0].equals(LOCKSCREEN_WEATHER_CENTERED)) {
            setWeatherCentered();
        }
        for (String LCWeatherMargins : LOCKSCREEN_WEATHER_MARGINS) {
            if (Key[0].equals(LCWeatherMargins))
                updateMargins();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        mWeatherContainer = new LinearLayout(mContext);
        mWeatherContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, WRAP_CONTENT));

        Class<?> KeyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView", lpparam.classLoader);

        hookAllMethods(KeyguardStatusViewClass, "onFinishInflate", new XC_MethodHook() {
            @SuppressLint("DiscouragedApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) {

                mStatusViewContainer = (ViewGroup) getObjectField(param.thisObject, "mStatusViewContainer");

                placeWeatherView();
            }
        });
    }

    private void placeWeatherView() {
        try {
            CurrentWeatherView currentWeatherView = CurrentWeatherView.getInstance(mContext, LOCKSCREEN_WEATHER);
            try {
                ((ViewGroup) currentWeatherView.getParent()).removeView(currentWeatherView);
            } catch (Throwable ignored) {
            }
            try {
                ((ViewGroup) mWeatherContainer.getParent()).removeView(mWeatherContainer);
            } catch (Throwable ignored) {
            }
            mWeatherContainer.addView(currentWeatherView);
            mStatusViewContainer.addView(mWeatherContainer);
            setWeatherCentered();
            refreshWeatherView(currentWeatherView);
            updateMargins();

            // Weather placed, now inflate widgets
            Intent broadcast = new Intent(ACTION_WEATHER_INFLATED);
            broadcast.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            new Thread(() -> mContext.sendBroadcast(broadcast)).start();
        } catch (Throwable ignored) {
        }
    }

    private void updateMargins() {
        if (mWeatherContainer == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mWeatherContainer.getLayoutParams();
        if (mWeatherCustomMargins) {
            params.setMargins(dp2px(mContext, mWeatherLeftMargin), dp2px(mContext, mWeatherTopMargin), dp2px(mContext, mWeatherLeftMargin), 0);
        } else {
            params.setMargins(mWeatherStartPadding, 0, mWeatherStartPadding, 0);
        }
        mWeatherContainer.setLayoutParams(params);
    }

    private void setWeatherCentered() {
        CurrentWeatherView currentWeatherView = CurrentWeatherView.getInstance(LOCKSCREEN_WEATHER);
        mWeatherContainer.setGravity(mWeatherCentered ? CENTER_HORIZONTAL : START);
        if (currentWeatherView != null)
            currentWeatherView.getLayoutParams().width = mWeatherCentered ? WRAP_CONTENT : MATCH_PARENT;
        if (currentWeatherView != null) currentWeatherView.requestLayout();
        ViewGroup weatherContainer = (ViewGroup) mWeatherContainer.getChildAt(0);
        for (int i = 0; i < weatherContainer.getChildCount(); i++) {
            View child = weatherContainer.getChildAt(i);
            if (child instanceof LinearLayout linearLayoutChild) {
                linearLayoutChild.setGravity(mWeatherCentered ? Gravity.CENTER_HORIZONTAL : (Gravity.START | Gravity.CENTER_VERTICAL));
            }
        }
    }

    private void refreshWeatherView(CurrentWeatherView currentWeatherView) {
        if (currentWeatherView == null) return;
        currentWeatherView.updateSizes(mWeatherTextSize, mWeatherImageSize, Constants.LockscreenWeather.LOCKSCREEN_WEATHER);
        currentWeatherView.updateColors(mWeatherCustomColor ? mWeatherColor : Color.WHITE, Constants.LockscreenWeather.LOCKSCREEN_WEATHER);
        currentWeatherView.updateWeatherSettings(mWeatherShowLocation, mWeatherShowCondition, mWeatherShowHumidity, mWeatherShowWind, Constants.LockscreenWeather.LOCKSCREEN_WEATHER);
        currentWeatherView.setVisibility(mWeatherEnabled ? View.VISIBLE : View.GONE);
        currentWeatherView.updateWeatherBg(mWeatherBackground, Constants.LockscreenWeather.LOCKSCREEN_WEATHER);
        updateMargins();
    }

    private void updateWeatherView() {
        refreshWeatherView(CurrentWeatherView.getInstance(LOCKSCREEN_WEATHER));
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
