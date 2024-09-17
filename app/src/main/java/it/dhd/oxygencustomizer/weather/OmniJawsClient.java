package it.dhd.oxygencustomizer.weather;

/*
 * Copyright (C) 2021 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;

public class OmniJawsClient {
    private static final String TAG = "OmniJawsClient";
    private static final boolean DEBUG = BuildConfig.DEBUG;
    public static final String SERVICE_PACKAGE = BuildConfig.APPLICATION_ID;
    public static final Uri WEATHER_URI
            = Uri.parse("content://it.dhd.oxygencustomizer.weatherprovider/weather");
    public static final Uri SETTINGS_URI
            = Uri.parse("content://it.dhd.oxygencustomizer.weatherprovider/settings");
    public static final Uri CONTROL_URI
            = Uri.parse("content://it.dhd.oxygencustomizer.weatherprovider/control");

    private static final String ICON_PACKAGE_DEFAULT = BuildConfig.APPLICATION_ID;
    private static final String ICON_PREFIX_DEFAULT = "google";
    private static final String ICON_PREFIX_OUTLINE = "outline";
    private static final String EXTRA_ERROR = "error";
    public static final int EXTRA_ERROR_NETWORK = 0; // No Network
    public static final int EXTRA_ERROR_LOCATION = 1; // No Location Found
    public static final int EXTRA_ERROR_DISABLED = 2; // Disabled
    public static final int EXTRA_ERROR_NO_PERMISSIONS = 3; // No Permissions

    public static final String[] WEATHER_PROJECTION = new String[]{
            "city",
            "wind_speed",
            "wind_direction",
            "condition_code",
            "temperature",
            "humidity",
            "condition",
            "forecast_low",
            "forecast_high",
            "forecast_condition",
            "forecast_condition_code",
            "time_stamp",
            "forecast_date",
            "pin_wheel",
            "forecast_hour",
            "forecast_hour_temp",
            "forecast_hour_condition",
            "forecast_hour_condition_code"
    };

    public static final String[] SETTINGS_PROJECTION = new String[] {
            "enabled",
            "units",
            "provider",
            "setup",
            "icon_pack"
    };

    private static final String WEATHER_UPDATE = SERVICE_PACKAGE + ".WEATHER_UPDATE";
    private static final String WEATHER_ERROR = SERVICE_PACKAGE + ".WEATHER_ERROR";

    private static final DecimalFormat sNoDigitsFormat = new DecimalFormat("0");

    public static class WeatherInfo {
        public String city;
        public String windSpeed;
        public String windDirection;
        public int conditionCode;
        public String temp;
        public String humidity;
        public String condition;
        public Long timeStamp = 0L;
        public List<DayForecast> dayForecasts;
        public List<HourForecast> hourlyForecasts;
        public String tempUnits;
        public String windUnits;
        public String provider;
        public String pinWheel;
        public String iconPack;

        public String toString() {
            return city + ":" + new Date(timeStamp) + ": " + windSpeed + ":" + windDirection + ":" +conditionCode + ":" + temp + ":" + humidity + ":" + condition + ":" + tempUnits + ":" + windUnits + ": " + hourlyForecasts + ": " + dayForecasts + ": " + iconPack;
        }

        public String getLastUpdateTime() {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            return sdf.format(new Date(timeStamp));
        }
    }

    public static class DayForecast {
        public String low;
        public String high;
        public int conditionCode;
        public String condition;
        public String date;

        @NonNull
        public String toString() {
            return "[" + low + ":" + high + ":" +conditionCode + ":" + condition + ":" + date + "]";
        }
    }

    public static class HourForecast {
        public String condition;
        public String time;
        public int conditionCode;
        public String temperature;

        @NonNull
        public String toString() {
            return "[" + temperature + ":" +conditionCode + ":" + condition + ":" + time + "]";
        }
    }

    public interface OmniJawsObserver {
        void weatherUpdated();
        void weatherError(int errorReason);
        default void updateSettings() {};
    }

    private class WeatherUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();
            for (OmniJawsObserver observer : mObserver) {
                if (action.equals(WEATHER_UPDATE)) {
                    observer.weatherUpdated();
                }
                if (action.equals(WEATHER_ERROR)) {
                    int errorReason = intent.getIntExtra(EXTRA_ERROR, 0);
                    observer.weatherError(errorReason);
                }
            }
        }
    }

    private Context mContext;
    private WeatherInfo mCachedInfo;
    private Resources mRes;
    private String mPackageName;
    private String mIconPrefix;
    private String mSettingIconPackage;
    private boolean mMetric;
    private List<OmniJawsObserver> mObserver;
    private WeatherUpdateReceiver mReceiver;

    public OmniJawsClient(Context context) {
        mContext = context;
        mObserver = new ArrayList<>();
    }

    public Intent getSettingsIntent() {
        final Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".ui.activity.MainActivity"));
        launchIntent.putExtra("openWeatherSettings", true);
        return launchIntent;
    }

    public WeatherInfo getWeatherInfo() {
        return mCachedInfo;
    }

    private static String getFormattedValue(float value) {
        if (Float.isNaN(value)) {
            return "-";
        }
        String formatted = sNoDigitsFormat.format(value);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted;
    }

    public void queryWeather() {
        try {
            mCachedInfo = null;
            Cursor c = mContext.getContentResolver().query(WEATHER_URI, WEATHER_PROJECTION,
                    null, null, null);
            if (c != null) {
                try {
                    int count = c.getCount();
                    if (count > 0) {
                        mCachedInfo = new WeatherInfo();
                        List<DayForecast> forecastList = new ArrayList<DayForecast>();
                        List<HourForecast> hourlyForecastList = new ArrayList<HourForecast>();
                        int i = 0;
                        for (i = 0; i < count; i++) {
                            c.moveToPosition(i);
                            boolean isDailyForecast = c.getString(12) != null && !TextUtils.isEmpty(c.getString(12));
                            boolean isHourlyForecast = c.getString(14) != null && !TextUtils.isEmpty(c.getString(14));
                            if (i == 0) {
                                mCachedInfo.city = c.getString(0);
                                mCachedInfo.windSpeed = getFormattedValue(c.getFloat(1));
                                mCachedInfo.windDirection = String.valueOf(c.getInt(2)) + "\u00b0";
                                mCachedInfo.conditionCode = c.getInt(3);
                                mCachedInfo.temp = getFormattedValue(c.getFloat(4));
                                mCachedInfo.humidity = c.getString(5);
                                mCachedInfo.condition = c.getString(6);
                                mCachedInfo.timeStamp = Long.valueOf(c.getString(11));
                                mCachedInfo.pinWheel = c.getString(13);
                            } else if (isDailyForecast) {
                                DayForecast day = new DayForecast();
                                day.low = getFormattedValue(c.getFloat(7));
                                day.high = getFormattedValue(c.getFloat(8));
                                day.condition = c.getString(9);
                                day.conditionCode = c.getInt(10);
                                day.date = c.getString(12);
                                forecastList.add(day);
                            } else if (isHourlyForecast) {
                                HourForecast hour = new HourForecast();
                                hour.time = c.getString(14);
                                hour.temperature = getFormattedValue(c.getFloat(15));
                                hour.condition = c.getString(16);
                                hour.conditionCode = c.getInt(17);
                                hourlyForecastList.add(hour);
                            }
                        }
                        mCachedInfo.dayForecasts = forecastList;
                        mCachedInfo.hourlyForecasts = hourlyForecastList;
                    }
                } finally {
                    c.close();
                }
            }
            c = mContext.getContentResolver().query(SETTINGS_URI, SETTINGS_PROJECTION,
                    null, null, null);
            if (c != null) {
                try {
                    int count = c.getCount();
                    if (count == 1) {
                        c.moveToPosition(0);
                        mMetric = c.getInt(1) == 0;
                        if (mCachedInfo != null) {
                            mCachedInfo.tempUnits = getTemperatureUnit();
                            mCachedInfo.windUnits = getWindUnit();
                            mCachedInfo.provider = c.getString(2);
                            mCachedInfo.iconPack = c.getString(4);
                        }
                    }
                } finally {
                    c.close();
                }
            }

            if (DEBUG) Log.d(TAG, "queryWeather " + mCachedInfo);
            updateSettings();
        } catch (Exception e) {
            Log.e(TAG, "queryWeather", e);
        }
    }

    private void loadDefaultIconsPackage() {
        mPackageName = ICON_PACKAGE_DEFAULT;
        mIconPrefix = ICON_PREFIX_DEFAULT;
        mSettingIconPackage = mPackageName + "." + mIconPrefix;
        if (DEBUG) Log.d(TAG, "Load default icon pack " + mSettingIconPackage + " " + mPackageName + " " + mIconPrefix);
        try {
            PackageManager packageManager = mContext.getPackageManager();
            mRes = packageManager.getResourcesForApplication(mPackageName);
        } catch (Exception e) {
            mRes = null;
        }
        if (mRes == null) {
            Log.w(TAG, "No default package found");
        }
    }

    private Drawable getDefaultConditionImage() {
        String packageName = ICON_PACKAGE_DEFAULT;
        String iconPrefix = ICON_PREFIX_DEFAULT;

        try {
            PackageManager packageManager = mContext.getPackageManager();
            Resources res = packageManager.getResourcesForApplication(packageName);
            if (res != null) {
                int resId = res.getIdentifier(iconPrefix + "_na", "drawable", packageName);
                Drawable d = ResourcesCompat.getDrawable(mRes, resId, mContext.getTheme());
                if (d != null) {
                    return d;
                }
            } else {
                int resId = modRes.getIdentifier(iconPrefix + "_na", "drawable", packageName);
                Drawable d = ResourcesCompat.getDrawable(modRes, resId, mContext.getTheme());
                if (d != null) {
                    return d;
                }
            }
        } catch (Exception ignored) {
        }
        // absolute absolute fallback
        Log.w(TAG, "No default package found");
        return new ColorDrawable(Color.RED);
    }

    private void loadCustomIconPackage() {
        if (DEBUG) Log.d(TAG, "Load custom icon pack " + mSettingIconPackage);
        int idx = mSettingIconPackage.lastIndexOf(".");
        mPackageName = mSettingIconPackage.substring(0, idx);
        mIconPrefix = mSettingIconPackage.substring(idx + 1);
        if (DEBUG) Log.d(TAG, "Load custom icon pack " + mPackageName + " " + mIconPrefix);
        try {
            PackageManager packageManager = mContext.getPackageManager();
            mRes = packageManager.getResourcesForApplication(mPackageName);
        } catch (Exception e) {
            mRes = null;
        }
        if (mRes == null) {
            Log.w(TAG, "Icon pack loading failed - loading default");
            loadDefaultIconsPackage();
        }
    }

    public Drawable getWeatherConditionImage(int conditionCode) {
        try {
            int resId = mRes.getIdentifier(mIconPrefix + "_" + conditionCode, "drawable", mPackageName);
            Drawable d = ResourcesCompat.getDrawable(mRes, resId, mContext.getTheme());
            if (d != null) {
                return d;
            }
            Log.w(TAG, "Failed to get condition image for " + conditionCode + " use default");
            resId = mRes.getIdentifier(mIconPrefix + "_na", "drawable", mPackageName);
            d = ResourcesCompat.getDrawable(mRes, resId, mContext.getTheme());
            if (d != null) {
                return d;
            }
        } catch(Exception e) {
            Log.e(TAG, "getWeatherConditionImage", e);
        }
        Log.w(TAG, "Failed to get condition image for " + conditionCode);
        return getDefaultConditionImage();
    }

    public boolean isOmniJawsEnabled() {
        return WeatherConfig.isEnabled(mContext);
    }

    private String getTemperatureUnit() {
        return "\u00b0" + (mMetric ? "C" : "F");
    }

    private String getWindUnit() {
        return mMetric ? "km/h":"mph";
    }

    private void updateSettings() {
        final String iconPack = mCachedInfo != null ? mCachedInfo.iconPack : null;
        if (TextUtils.isEmpty(iconPack)) {
            loadDefaultIconsPackage();
        } else if (mSettingIconPackage == null || !iconPack.equals(mSettingIconPackage)) {
            mSettingIconPackage = iconPack;
            loadCustomIconPackage();
        }
    }

    public void addObserver(OmniJawsObserver observer) {
        if (mObserver.isEmpty()) {
            if (mReceiver != null) {
                try {
                    mContext.unregisterReceiver(mReceiver);
                } catch (Exception ignored) {
                }
            }
            mReceiver = new WeatherUpdateReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(WEATHER_UPDATE);
            filter.addAction(WEATHER_ERROR);
            if (DEBUG) Log.d(TAG, "registerReceiver");
            mContext.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
        }
        mObserver.add(observer);
    }

    public void removeObserver(OmniJawsObserver observer) {
        mObserver.remove(observer);
        if (mObserver.isEmpty() && mReceiver != null) {
            try {
                if (DEBUG) Log.d(TAG, "unregisterReceiver");
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception ignored) {
            }
            mReceiver = null;
        }
    }

}
