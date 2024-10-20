package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.START;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CENTERED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_MARGIN_TOP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_HUMIDITY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_IMAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SHOW_CONDITION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SHOW_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_TEXT_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_WIND;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.views.CurrentWeatherView;

public class AodWeather extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private final int weatherStartPadding = 20;
    private boolean weatherEnabled = false, weatherShowLocation = true, weatherShowCondition = true;
    private boolean weatherShowHumidity = false, weatherShowWind = false;
    private boolean weatherCustomColor = false;
    private int weatherColor = Color.WHITE;
    private int weatherTextSize = 16, weatherImageSize = 18;
    private boolean mCustomMargins = false;
    private int mLeftMargin = 0, mTopMargin = 0;
    private LinearLayout mWeatherContainer = null;
    private boolean mWeatherCentered = false;

    private ViewGroup mAodRootLayout = null;

    public AodWeather(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        // Weather
        weatherEnabled = Xprefs.getBoolean(AOD_WEATHER_SWITCH, false);
        weatherTextSize = Xprefs.getSliderInt(AOD_WEATHER_TEXT_SIZE, 16);
        weatherImageSize = Xprefs.getSliderInt(AOD_WEATHER_IMAGE_SIZE, 18);
        weatherShowLocation = Xprefs.getBoolean(AOD_WEATHER_SHOW_LOCATION, true);
        weatherShowCondition = Xprefs.getBoolean(AOD_WEATHER_SHOW_CONDITION, true);
        weatherShowHumidity = Xprefs.getBoolean(AOD_WEATHER_HUMIDITY, false);
        weatherShowWind = Xprefs.getBoolean(AOD_WEATHER_WIND, false);
        weatherCustomColor = Xprefs.getBoolean(AOD_WEATHER_CUSTOM_COLOR_SWITCH, false);
        weatherColor = Xprefs.getInt(AOD_WEATHER_CUSTOM_COLOR, Color.WHITE);
        mCustomMargins = Xprefs.getBoolean(AOD_WEATHER_CUSTOM_MARGINS, false);
        mLeftMargin = Xprefs.getSliderInt(AOD_WEATHER_CUSTOM_MARGIN_LEFT, 0);
        mTopMargin = Xprefs.getSliderInt(AOD_WEATHER_CUSTOM_MARGIN_TOP, 0);
        mWeatherCentered = Xprefs.getBoolean(AOD_WEATHER_CENTERED, false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> AodClockLayout;

        mWeatherContainer = new LinearLayout(mContext);
        mWeatherContainer.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));

        try {
            AodClockLayout = findClass("com.oplus.systemui.aod.aodclock.off.AodClockLayout", lpparam.classLoader);
        } catch (Throwable t) {
            AodClockLayout = findClass("com.oplusos.systemui.aod.aodclock.off.AodClockLayout", lpparam.classLoader); //OOS 13
        }


        hookAllMethods(AodClockLayout, "initForAodApk", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //if (!mAodClockEnabled) return;
                FrameLayout mAodViewFromApk = (FrameLayout) getObjectField(param.thisObject, "mAodViewFromApk");
                for (int i = 0; i < mAodViewFromApk.getChildCount(); i++) {
                    if (mAodViewFromApk.getChildAt(i) instanceof ViewGroup v) {
                        mAodRootLayout = v;
                    }
                }
                placeWeatherView();
            }
        });
    }

    private void updateMargins() {
        if (mWeatherContainer == null) return;
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mWeatherContainer.getLayoutParams();
        if (mCustomMargins) {
            params.setMargins(dp2px(mContext, mLeftMargin), dp2px(mContext, mTopMargin), dp2px(mContext, mLeftMargin), 0);
        } else {
            params.setMargins(dp2px(mContext, weatherStartPadding), 0, dp2px(mContext, weatherStartPadding), 0);
        }
        mWeatherContainer.setLayoutParams(params);
    }

    private void placeWeatherView() {
        try {
            if (mWeatherContainer == null) {
                mWeatherContainer = new LinearLayout(mContext);
                mWeatherContainer.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT));
            }
            CurrentWeatherView currentWeatherView = CurrentWeatherView.getInstance(mContext, AOD_WEATHER);
            try {
                ((ViewGroup) currentWeatherView.getParent()).removeView(currentWeatherView);
            } catch (Throwable ignored) {
            }
            try {
                ((ViewGroup) mWeatherContainer.getParent()).removeView(mWeatherContainer);
            } catch (Throwable ignored) {
            }
            mWeatherContainer.addView(currentWeatherView);
            mAodRootLayout.addView(mWeatherContainer, mAodRootLayout.getChildCount());
            setWeatherCentered();
            refreshWeatherView(currentWeatherView);
            updateMargins();
        } catch (Throwable tt) {
            log("AOD WEATHER ERROR " + tt.getMessage());
        }
    }

    private void refreshWeatherView(CurrentWeatherView currentWeatherView) {
        if (currentWeatherView == null) return;
        currentWeatherView.updateSizes(weatherTextSize, weatherImageSize, AOD_WEATHER);
        currentWeatherView.updateColors(weatherCustomColor ? weatherColor : Color.WHITE, AOD_WEATHER);
        currentWeatherView.updateWeatherSettings(weatherShowLocation, weatherShowCondition, weatherShowHumidity, weatherShowWind, AOD_WEATHER);
        currentWeatherView.setVisibility(weatherEnabled ? View.VISIBLE : View.GONE);
        updateMargins();
    }

    private void updateWeatherView() {
        refreshWeatherView(CurrentWeatherView.getInstance(AOD_WEATHER));
    }

    private void setWeatherCentered() {
        CurrentWeatherView currentWeatherView = CurrentWeatherView.getInstance(AOD_WEATHER);
        mWeatherContainer.setGravity(mWeatherCentered ? CENTER_HORIZONTAL : START);
        if (currentWeatherView != null)
            currentWeatherView.setGravity(mWeatherCentered ? CENTER_HORIZONTAL : START);
        if (currentWeatherView != null) currentWeatherView.requestLayout();
        ViewGroup weatherContainer = (ViewGroup) mWeatherContainer.getChildAt(0);
        for (int i = 0; i < weatherContainer.getChildCount(); i++) {
            View child = weatherContainer.getChildAt(i);
            if (child instanceof LinearLayout linearLayoutChild) {
                linearLayoutChild.setGravity(mWeatherCentered ? Gravity.CENTER_HORIZONTAL : (Gravity.START | Gravity.CENTER_VERTICAL));
            }
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
