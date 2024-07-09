package it.dhd.oxygencustomizer.weather;

/*
 *  Copyright (C) 2015 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_ICON_PACK;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_OWM_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_PROVIDER;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_UNITS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_UPDATE_INTERVAL;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.weather.providers.METNorwayProvider;
import it.dhd.oxygencustomizer.weather.providers.OpenWeatherMapProvider;

public class Config {
    public static final String SharedXPref = BuildConfig.APPLICATION_ID + "_preferences";
    public static final String PREF_KEY_PROVIDER = "provider";
    public static final String PREF_KEY_UNITS = "units";
    public static final String PREF_KEY_LOCATION_ID = "location_id";
    public static final String PREF_KEY_LOCATION_NAME = "location_name";
    public static final String PREF_KEY_WEATHER_DATA = "weather_data";
    public static final String PREF_KEY_LAST_UPDATE = "last_update";
    public static final String PREF_KEY_ENABLE = "enable";
    public static final String PREF_KEY_UPDATE_INTERVAL = "update_interval";
    public static final String PREF_KEY_ICON_PACK = "icon_pack";
    public static final String PREF_KEY_UPDATE_ERROR = "update_error";
    public static final String PREF_KEY_OWM_KEY = "owm_key";
    public static final String PREF_KEY_HISTORY = "history";
    public static final String PREF_KEY_HISTORY_SIZE = "history_size";

    private static SharedPreferences getPrefs(Context context)
    {
        try {
            if (Xprefs != null)
                return Xprefs;
            return getDefaultSharedPreferences(context.createDeviceProtectedStorageContext());
        } catch (Throwable t) {
            return getDefaultSharedPreferences(context.createDeviceProtectedStorageContext());
        }
    }

    public static AbstractWeatherProvider getProvider(Context context) {
        String provider = getPrefs(context).getString(LOCKSCREEN_WEATHER_PROVIDER, "0");

        return switch (provider) {
            case "1" -> new METNorwayProvider(context);
            default -> new OpenWeatherMapProvider(context);
        };
    }

    public static String getProviderId(Context context) {
        String provider = getPrefs(context).getString(LOCKSCREEN_WEATHER_PROVIDER, "0");

        return switch (provider) {
            case "1" -> "MET Norway";
            default -> "OpenWeatherMap";
        };
    }

    public static boolean isMetric(Context context) {

        return getPrefs(context).getString(LOCKSCREEN_WEATHER_UNITS, "0").equals("0");
    }

    public static boolean isCustomLocation(Context context) {
        return getPrefs(context).getBoolean(LOCKSCREEN_WEATHER_CUSTOM_LOCATION, false);
    }

    public static String getLocationId(Context context) {

        return getPrefs(context).getString(PREF_KEY_LOCATION_ID, null);
    }

    public static void setLocationId(Context context, String id) {

        getPrefs(context).edit().putString(PREF_KEY_LOCATION_ID, id).apply();
    }

    public static String getLocationName(Context context) {

        return getPrefs(context).getString(PREF_KEY_LOCATION_NAME, null);
    }

    public static void setLocationName(Context context, String name) {
        getPrefs(context).edit().putString(PREF_KEY_LOCATION_NAME, name).apply();
    }

    public static WeatherInfo getWeatherData(Context context) {
        String str = null;
        try {
            str = getPrefs(context).getString(PREF_KEY_WEATHER_DATA, null);
        } catch (Throwable ignored) {
        }

        if (str != null) {
            return WeatherInfo.fromSerializedString(context, str);
        }
        return null;
    }

    public static void setWeatherData(WeatherInfo data, Context context) {
        getPrefs(context).edit().putString(PREF_KEY_WEATHER_DATA, data.toSerializedString()).apply();
        getPrefs(context).edit().putLong(PREF_KEY_LAST_UPDATE, System.currentTimeMillis()).apply();
    }

    public static void clearLastUpdateTime(Context context) {
        getPrefs(context).edit().putLong(PREF_KEY_LAST_UPDATE, 0).apply();
    }

    public static boolean isEnabled(Context context) {
        return getPrefs(context).getBoolean(LOCKSCREEN_WEATHER_SWITCH, false);
    }

    public static void setEnabled(Context context, boolean value) {
        getPrefs(context).edit().putBoolean(LOCKSCREEN_WEATHER_SWITCH, value).apply();
    }

    public static int getUpdateInterval(Context context) {

        int updateValue = 2;
        try {
            updateValue = Integer.parseInt(getPrefs(context).getString(LOCKSCREEN_WEATHER_UPDATE_INTERVAL, "2"));
        } catch (Throwable ignored) {
        }

        return updateValue;
    }

    public static String getIconPack(Context context) {

        return getPrefs(context).getString(LOCKSCREEN_WEATHER_ICON_PACK, null);
    }

    public static void setUpdateError(Context context, boolean value) {
        getPrefs(context).edit().putBoolean(PREF_KEY_UPDATE_ERROR, value).apply();
    }

    public static boolean isSetupDone(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static String getOwmKey(Context context) {

        return getPrefs(context).getString(LOCKSCREEN_WEATHER_OWM_KEY, null);
    }
}
