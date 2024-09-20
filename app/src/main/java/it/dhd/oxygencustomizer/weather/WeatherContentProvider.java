package it.dhd.oxygencustomizer.weather;

/*
 * Copyright (C) 2013 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;

import it.dhd.oxygencustomizer.utils.WeatherScheduler;

public class WeatherContentProvider extends ContentProvider {
    private static final String TAG = "WeatherService:WeatherContentProvider";
    private static final boolean DEBUG = false;

    static WeatherInfo sCachedWeatherInfo;

    private static final int URI_TYPE_WEATHER = 1;
    private static final int URI_TYPE_SETTINGS = 2;
    private static final int URI_TYPE_CONTROL = 3;

    private static final String COLUMN_CURRENT_CITY_ID = "city_id";
    private static final String COLUMN_CURRENT_CITY = "city";
    private static final String COLUMN_CURRENT_CONDITION = "condition";
    private static final String COLUMN_CURRENT_TEMPERATURE = "temperature";
    private static final String COLUMN_CURRENT_HUMIDITY = "humidity";
    private static final String COLUMN_CURRENT_WIND_SPEED = "wind_speed";
    private static final String COLUMN_CURRENT_WIND_DIRECTION = "wind_direction";
    private static final String COLUMN_CURRENT_TIME_STAMP = "time_stamp";
    private static final String COLUMN_CURRENT_CONDITION_CODE = "condition_code";
    private static final String COLUMN_CURRENT_PIN_WHEEL = "pin_wheel";

    private static final String COLUMN_FORECAST_LOW = "forecast_low";
    private static final String COLUMN_FORECAST_HIGH = "forecast_high";
    private static final String COLUMN_FORECAST_CONDITION = "forecast_condition";
    private static final String COLUMN_FORECAST_CONDITION_CODE = "forecast_condition_code";
    private static final String COLUMN_FORECAST_DATE = "forecast_date";

    private static final String COLUMN_FORECAST_HOUR = "forecast_hour";
    private static final String COLUMN_FORECAST_HOUR_TEMP = "forecast_hour_temp";
    private static final String COLUMN_FORECAST_HOUR_CONDITION = "forecast_hour_condition";
    private static final String COLUMN_FORECAST_HOUR_CONDITION_CODE = "forecast_hour_condition_code";

    private static final String COLUMN_ENABLED = "enabled";
    private static final String COLUMN_PROVIDER = "provider";
    private static final String COLUMN_INTERVAL = "interval";
    private static final String COLUMN_UNITS = "units";
    private static final String COLUMN_LOCATION = "location";
    private static final String COLUMN_SETUP = "setup";
    private static final String COLUMN_ICON_PACK = "icon_pack";

    public static final String COLUMN_FORCE_REFRESH = "update";

    private static final String[] PROJECTION_DEFAULT_WEATHER = new String[] {
            COLUMN_CURRENT_CITY_ID,
            COLUMN_CURRENT_CITY,
            COLUMN_CURRENT_CONDITION,
            COLUMN_CURRENT_TEMPERATURE,
            COLUMN_CURRENT_HUMIDITY,
            COLUMN_CURRENT_WIND_SPEED,
            COLUMN_CURRENT_WIND_DIRECTION,
            COLUMN_CURRENT_TIME_STAMP,
            COLUMN_CURRENT_PIN_WHEEL,
            COLUMN_CURRENT_CONDITION_CODE,
            COLUMN_FORECAST_LOW,
            COLUMN_FORECAST_HIGH,
            COLUMN_FORECAST_CONDITION,
            COLUMN_FORECAST_CONDITION_CODE,
            COLUMN_FORECAST_DATE,
            COLUMN_FORECAST_HOUR,
            COLUMN_FORECAST_HOUR_TEMP,
            COLUMN_FORECAST_HOUR_CONDITION,
            COLUMN_FORECAST_HOUR_CONDITION_CODE
    };

    private static final String[] PROJECTION_DEFAULT_SETTINGS = new String[] {
            COLUMN_ENABLED,
            COLUMN_PROVIDER,
            COLUMN_INTERVAL,
            COLUMN_UNITS,
            COLUMN_LOCATION,
            COLUMN_SETUP,
            COLUMN_ICON_PACK
    };

    public static final String AUTHORITY = "it.dhd.oxygencustomizer.weatherprovider";

    private static final UriMatcher sUriMatcher;
    static {
        sUriMatcher = new UriMatcher(URI_TYPE_WEATHER);
        sUriMatcher.addURI(AUTHORITY, "weather", URI_TYPE_WEATHER);
        sUriMatcher.addURI(AUTHORITY, "settings", URI_TYPE_SETTINGS);
        sUriMatcher.addURI(AUTHORITY, "control", URI_TYPE_CONTROL);
    }

    private Context mContext;

    @Override
    public boolean onCreate() {
        mContext = getContext();
        sCachedWeatherInfo = WeatherConfig.getWeatherData(mContext);
        return true;
    }

    @Override
    public Cursor query(
            @NonNull Uri uri,
            String[] projection,
            String selection,
            String[] selectionArgs,
            String sortOrder) {

        final int projectionType = sUriMatcher.match(uri);
        final MatrixCursor result = new MatrixCursor(resolveProjection(projection, projectionType));


        if (projectionType == URI_TYPE_SETTINGS) {
            result.newRow()
                    .add(COLUMN_ENABLED, WeatherConfig.isEnabled(mContext) ? 1 : 0)
                    .add(COLUMN_PROVIDER, WeatherConfig.getProviderId(mContext))
                    .add(COLUMN_INTERVAL, WeatherConfig.getUpdateInterval(mContext))
                    .add(COLUMN_UNITS, WeatherConfig.isMetric(mContext) ? 0 : 1)
                    .add(COLUMN_LOCATION, WeatherConfig.isCustomLocation(mContext) ? WeatherConfig.getLocationName(mContext) : "")
                    .add(COLUMN_SETUP, !WeatherConfig.isSetupDone(mContext) && sCachedWeatherInfo == null ? 0 : 1)
                    .add(COLUMN_ICON_PACK, WeatherConfig.getIconPack(mContext) != null ? WeatherConfig.getIconPack(mContext) : "");


            return result;
        } else if (projectionType == URI_TYPE_WEATHER) {
            WeatherInfo weather = sCachedWeatherInfo;
            if (weather != null) {
                // current
                result.newRow()
                        .add(COLUMN_CURRENT_CITY, weather.getCity())
                        .add(COLUMN_CURRENT_CITY_ID, weather.getId())
                        .add(COLUMN_CURRENT_CONDITION, weather.getCondition())
                        .add(COLUMN_CURRENT_HUMIDITY, weather.getFormattedHumidity())
                        .add(COLUMN_CURRENT_WIND_SPEED, weather.getWindSpeed())
                        .add(COLUMN_CURRENT_WIND_DIRECTION, weather.getWindDirection())
                        .add(COLUMN_CURRENT_TEMPERATURE, weather.getTemperature())
                        .add(COLUMN_CURRENT_TIME_STAMP, weather.getTimestamp().toString())
                        .add(COLUMN_CURRENT_PIN_WHEEL, weather.getPinWheel())
                        .add(COLUMN_CURRENT_CONDITION_CODE, weather.getConditionCode());

                // forecast
                for (WeatherInfo.DayForecast day : weather.getForecasts()) {
                    result.newRow()
                            .add(COLUMN_FORECAST_CONDITION, day.getCondition(mContext))
                            .add(COLUMN_FORECAST_LOW, day.getLow())
                            .add(COLUMN_FORECAST_HIGH, day.getHigh())
                            .add(COLUMN_FORECAST_CONDITION_CODE, day.getConditionCode())
                            .add(COLUMN_FORECAST_DATE, day.date);
                }

                for (WeatherInfo.HourForecast hour : weather.getHourForecasts()) {
                    result.newRow()
                            .add(COLUMN_FORECAST_HOUR, hour.date)
                            .add(COLUMN_FORECAST_HOUR_TEMP, hour.getTemp())
                            .add(COLUMN_FORECAST_HOUR_CONDITION, hour.getCondition(mContext))
                            .add(COLUMN_FORECAST_HOUR_CONDITION_CODE, hour.getConditionCode());
                }


                return result;
            }
        }
        return null;
    }

    private String[] resolveProjection(String[] projection, int uriType) {
        if (projection != null)
            return projection;
        return switch (uriType) {
            default -> PROJECTION_DEFAULT_WEATHER;
            case URI_TYPE_SETTINGS -> PROJECTION_DEFAULT_SETTINGS;
        };
    }

    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int projectionType = sUriMatcher.match(uri);
        if (projectionType == URI_TYPE_CONTROL) {
            if (values.containsKey(COLUMN_FORCE_REFRESH) && values.getAsBoolean(COLUMN_FORCE_REFRESH)) {
                if (DEBUG) Log.i(TAG, "update: " + uri.toString() + " " + values);
                WeatherScheduler.scheduleUpdateNow(mContext);
            }
        }
        return 0;
    }

    public static void updateCachedWeatherInfo(Context context) {
        if (DEBUG) Log.d(TAG, "updateCachedWeatherInfo()");
        sCachedWeatherInfo = WeatherConfig.getWeatherData(context);
        context.getContentResolver().notifyChange(
                Uri.parse("content://" + WeatherContentProvider.AUTHORITY + "/weather"), null);
    }
}
