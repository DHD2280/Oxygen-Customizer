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
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_EXTRAS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_LIST;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_CUSTOM_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_ICON_PACK;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_OWM_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_PROVIDER;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_UNITS;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_UPDATE_INTERVAL;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_YANDEX_KEY;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.weather.providers.METNorwayProvider;
import it.dhd.oxygencustomizer.weather.providers.OpenMeteoProvider;
import it.dhd.oxygencustomizer.weather.providers.OpenWeatherMapProvider;
import it.dhd.oxygencustomizer.weather.providers.YandexProvider;

public class WeatherConfig {
    public static final String PREF_KEY_LOCATION_LAT = "location_lat";
    public static final String PREF_KEY_LOCATION_LON = "location_lon";
    public static final String PREF_KEY_LOCATION_NAME = "location_name";
    public static final String PREF_KEY_WEATHER_DATA = "weather_data";
    public static final String PREF_KEY_LAST_UPDATE = "last_update";
    public static final String PREF_KEY_UPDATE_ERROR = "update_error";
    public static final String WEATHER_PREFS = BuildConfig.APPLICATION_ID + "_weatherprefs";

    private static SharedPreferences getPrefs(Context context) {
        Context deviceProtectedContext = context.createDeviceProtectedStorageContext();
        return getDefaultSharedPreferences(deviceProtectedContext);
    }

    private static SharedPreferences getWeatherPrefs(Context context) {
        Context deviceProtectedContext = context.createDeviceProtectedStorageContext();
        return deviceProtectedContext.getSharedPreferences(WEATHER_PREFS, Context.MODE_PRIVATE);
    }

    public static AbstractWeatherProvider getProvider(Context context) {
        String provider = getPrefs(context).getString(WEATHER_PROVIDER, "2");

        return switch (provider) {
            case "1" -> new METNorwayProvider(context);
            case "2" -> new OpenMeteoProvider(context);
            case "3" -> new YandexProvider(context);
            default -> new OpenWeatherMapProvider(context);
        };
    }

    public static String getProviderId(Context context) {
        String provider = getPrefs(context).getString(WEATHER_PROVIDER, "2");

        return switch (provider) {
            case "1" -> "MET Norway";
            case "2" -> "OpenMeteo";
            case "3" -> "Yandex";
            case "4" -> "Meteo AM";
            default -> "OpenWeatherMap";
        };
    }

    public static boolean isMetric(Context context) {

        return getPrefs(context).getString(WEATHER_UNITS, "0").equals("0");
    }

    public static boolean isCustomLocation(Context context) {
        return getPrefs(context).getBoolean(WEATHER_CUSTOM_LOCATION, false);
    }

    public static String getLocationLat(Context context) {
            return getWeatherPrefs(context).getString(PREF_KEY_LOCATION_LAT, null);
    }

    public static String getLocationLon(Context context) {
            return getWeatherPrefs(context).getString(PREF_KEY_LOCATION_LON, null);
    }

    public static void setLocationId(Context context, String lat, String lon) {
        getWeatherPrefs(context).edit().putString(PREF_KEY_LOCATION_LAT, lat).apply();
        getWeatherPrefs(context).edit().putString(PREF_KEY_LOCATION_LON, lon).apply();
    }

    public static String getLocationName(Context context) {

        return getWeatherPrefs(context).getString(PREF_KEY_LOCATION_NAME, null);
    }

    public static void setLocationName(Context context, String name) {
        getWeatherPrefs(context).edit().putString(PREF_KEY_LOCATION_NAME, name).apply();
    }

    public static WeatherInfo getWeatherData(Context context) {
        String str = null;
        try {
            str = getWeatherPrefs(context).getString(PREF_KEY_WEATHER_DATA, null);
        } catch (Throwable ignored) {
        }

        if (str != null) {
            return WeatherInfo.fromSerializedString(context, str);
        }
        return null;
    }

    public static void setWeatherData(WeatherInfo data, Context context) {
        getWeatherPrefs(context).edit().putString(PREF_KEY_WEATHER_DATA, data.toSerializedString()).apply();
        getWeatherPrefs(context).edit().putLong(PREF_KEY_LAST_UPDATE, System.currentTimeMillis()).apply();
    }

    public static void clearLastUpdateTime(Context context) {
        getWeatherPrefs(context).edit().putLong(PREF_KEY_LAST_UPDATE, 0).apply();
    }

    public static boolean isEnabled(Context context) {
        boolean lsWeather = getPrefs(context).getBoolean(LOCKSCREEN_WEATHER_SWITCH, false);
        boolean aodWeather = getPrefs(context).getBoolean(AOD_WEATHER_SWITCH, false);
        String bigWidgets = getPrefs(context).getString(LOCKSCREEN_WIDGETS, "");
        String miniWidgets = getPrefs(context).getString(LOCKSCREEN_WIDGETS_EXTRAS, "");
        String qsWidgets = getPrefs(context).getString(QS_WIDGETS_LIST, "media");

        boolean weatherWidget = bigWidgets.contains("weather") || miniWidgets.contains("weather");
        boolean qsWeather = qsWidgets.contains("weather");

        return lsWeather || aodWeather || weatherWidget || qsWeather;
    }

    public static void setEnabled(Context context, boolean value, String key) {
        getPrefs(context).edit().putBoolean(key, value).apply();
    }

    public static int getUpdateInterval(Context context) {

        int updateValue = 2;
        try {
            updateValue = Integer.parseInt(getPrefs(context).getString(WEATHER_UPDATE_INTERVAL, "2"));
        } catch (Throwable ignored) {
        }

        return updateValue;
    }

    public static String getIconPack(Context context) {
        return getPrefs(context).getString(WEATHER_ICON_PACK, null);
    }

    public static void setUpdateError(Context context, boolean value) {
        getWeatherPrefs(context).edit().putBoolean(PREF_KEY_UPDATE_ERROR, value).apply();
    }

    public static boolean isSetupDone(Context context) {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static String getOwmKey(Context context) {
        return getPrefs(context).getString(WEATHER_OWM_KEY, null);
    }

    public static String getYandexKey(Context context) {
        return getPrefs(context).getString(WEATHER_YANDEX_KEY, null);
    }

}
