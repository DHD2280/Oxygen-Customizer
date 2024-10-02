package it.dhd.oxygencustomizer.weather.providers;

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

import android.content.Context;
import android.content.res.Resources;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.AbstractWeatherProvider;
import it.dhd.oxygencustomizer.weather.WeatherConfig;
import it.dhd.oxygencustomizer.weather.WeatherInfo;
import it.dhd.oxygencustomizer.weather.WeatherInfo.DayForecast;

public class OpenWeatherMapProvider extends AbstractWeatherProvider {
    private static final String TAG = "OpenWeatherMapProvider";

    private static final int FORECAST_DAYS = 5;
    private static final String URL_WEATHER =
            "https://api.openweathermap.org/data/2.5/weather?%s&mode=json&units=%s&lang=%s&cnt=" + FORECAST_DAYS + "&appid=%s";

    private List<String> mKeys = new ArrayList<>();
    private boolean mHasAPIKey;
    private int mRequestNumber;

    public OpenWeatherMapProvider(Context context) {
        super(context);
        loadKeys();
        mHasAPIKey = getAPIKey() != null;
    }

    public WeatherInfo getCustomWeather(String lat, String lon, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, Float.valueOf(lat), Float.valueOf(lon));
        return handleWeatherRequest(coordinates, metric);
    }

    public WeatherInfo getLocationWeather(Location location, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, location.getLatitude(), location.getLongitude());
        return handleWeatherRequest(coordinates, metric);
    }

    private WeatherInfo handleWeatherRequest(String selection, boolean metric) {
        if (!mHasAPIKey) {
            return null;
        }
        mRequestNumber++;
        String units = metric ? "metric" : "imperial";
        String locale = getLanguageCode();
        String conditionUrl = String.format(Locale.US, URL_WEATHER, selection, units, locale, getAPIKey());
        String conditionResponse = retrieve(conditionUrl, null);
        if (conditionResponse == null) {
            return null;
        }
        log(TAG, "Condition URL = " + conditionUrl + " returning a response of " + conditionResponse);

        try {
            JSONObject conditions = new JSONObject(conditionResponse);
            JSONObject conditionData = conditions.getJSONObject("main");
            JSONObject weather = conditions.getJSONArray("weather").getJSONObject(0);
            ArrayList<DayForecast> forecasts = new ArrayList<>();
            ArrayList<WeatherInfo.HourForecast> hourlyForecasts = new ArrayList<>();
            if (conditions.has("daily")) {
                forecasts =
                        parseForecasts(conditions.getJSONArray("daily"), metric);
            }
            JSONObject wind = conditions.getJSONObject("wind");
            float windSpeed = (float) wind.getDouble("speed");
            if (metric) {
                // speeds are in m/s so convert to our common metric unit km/h
                windSpeed *= 3.6f;
            }

            String city = getWeatherDataLocality(selection);

            WeatherInfo w = new WeatherInfo(mContext, selection, city,
                    /* condition */ weather.getString("main"),
                    /* conditionCode */ mapConditionIconToCode(
                    weather.getString("icon"), weather.getInt("id")),
                    /* temperature */ sanitizeTemperature(conditionData.getDouble("temp"), metric),
                    /* humidity */ (float) conditionData.getDouble("humidity"),
                    /* wind */ windSpeed,
                    /* windDir */ wind.has("deg") ? wind.getInt("deg") : 0,
                    metric,
                    hourlyForecasts,
                    forecasts,
                    System.currentTimeMillis());

            log(TAG, "Weather updated: " + w);
            return w;
        } catch (JSONException e) {
            Log.w(TAG, "Received malformed weather data (selection = " + selection
                    + ", lang = " + locale + ")", e);
        }

        return null;
    }

    private ArrayList<DayForecast> parseForecasts(JSONArray forecasts, boolean metric) throws JSONException {
        ArrayList<DayForecast> result = new ArrayList<DayForecast>();
        int count = forecasts.length();

        if (count == 0) {
            throw new JSONException("Empty forecasts array");
        }
        for (int i = 0; i < count; i++) {
            String day = getDay(i);
            DayForecast item = null;
            try {
                JSONObject forecast = forecasts.getJSONObject(i);
                JSONObject conditionData = forecast.getJSONObject("temp");
                JSONObject data = forecast.getJSONArray("weather").getJSONObject(0);
                item = new DayForecast(
                        /* low */ sanitizeTemperature(conditionData.getDouble("min"), metric),
                        /* high */ sanitizeTemperature(conditionData.getDouble("max"), metric),
                        /* condition */ data.getString("main"),
                        /* conditionCode */ mapConditionIconToCode(
                        data.getString("icon"), data.getInt("id")),
                        day,
                        metric);
            } catch (JSONException e) {
                Log.w(TAG, "Invalid forecast for day " + i + " creating dummy", e);
                item = new DayForecast(
                        /* low */ 0,
                        /* high */ 0,
                        /* condition */ "",
                        /* conditionCode */ -1,
                        "NaN",
                        metric);
            }
            result.add(item);
        }
        // clients assume there are 5  entries - so fill with dummy if needed
        if (result.size() < 5) {
            for (int i = result.size(); i < 5; i++) {
                Log.w(TAG, "Missing forecast for day " + i + " creating dummy");
                DayForecast item = new DayForecast(
                        /* low */ 0,
                        /* high */ 0,
                        /* condition */ "",
                        /* conditionCode */ -1,
                        "NaN",
                        metric);
                result.add(item);
            }
        }
        return result;
    }

    // OpenWeatherMap sometimes returns temperatures in Kelvin even if we ask it
    // for deg C or deg F. Detect this and convert accordingly.
    private static float sanitizeTemperature(double value, boolean metric) {
        // threshold chosen to work for both C and F. 170 deg F is hotter
        // than the hottest place on earth.
        if (value > 170) {
            // K -> deg C
            value -= 273.15;
            if (!metric) {
                // deg C -> deg F
                value = (value * 1.8) + 32;
            }
        }
        return (float) value;
    }

    private static final HashMap<String, String> LANGUAGE_CODE_MAPPING = new HashMap<String, String>();

    static {
        LANGUAGE_CODE_MAPPING.put("bg-", "bg");
        LANGUAGE_CODE_MAPPING.put("de-", "de");
        LANGUAGE_CODE_MAPPING.put("es-", "sp");
        LANGUAGE_CODE_MAPPING.put("fi-", "fi");
        LANGUAGE_CODE_MAPPING.put("fr-", "fr");
        LANGUAGE_CODE_MAPPING.put("it-", "it");
        LANGUAGE_CODE_MAPPING.put("nl-", "nl");
        LANGUAGE_CODE_MAPPING.put("pl-", "pl");
        LANGUAGE_CODE_MAPPING.put("pt-", "pt");
        LANGUAGE_CODE_MAPPING.put("ro-", "ro");
        LANGUAGE_CODE_MAPPING.put("ru-", "ru");
        LANGUAGE_CODE_MAPPING.put("se-", "se");
        LANGUAGE_CODE_MAPPING.put("tr-", "tr");
        LANGUAGE_CODE_MAPPING.put("uk-", "ua");
        LANGUAGE_CODE_MAPPING.put("zh-CN", "zh_cn");
        LANGUAGE_CODE_MAPPING.put("zh-TW", "zh_tw");
    }

    private String getLanguageCode() {
        Locale locale = mContext.getResources().getConfiguration().locale;
        String selector = locale.getLanguage() + "-" + locale.getCountry();

        for (Map.Entry<String, String> entry : LANGUAGE_CODE_MAPPING.entrySet()) {
            if (selector.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }

        return "en";
    }

    private int mapConditionIconToCode(String icon, int conditionId) {

        // First, use condition ID for specific cases
        return switch (conditionId) {
            // Thunderstorms
            // thunderstorm with heavy rain
            // thunderstorm with heavy drizzle
            case 202, 232, 211 ->   // thunderstorm
                    4;
            case 212 ->   // heavy thunderstorm
                    3;   // ragged thunderstorm
            // thunderstorm with drizzle
            case 221, 231, 201 ->   // thunderstorm with rain
                    38;   // thunderstorm with light drizzle
            // thunderstorm with light rain
            case 230, 200, 210 ->   // light thunderstorm
                    37;

            // Drizzle
            // light intensity drizzle
            // drizzle
            // heavy intensity drizzle
            // light intensity drizzle rain
            // drizzle rain
            // heavy intensity drizzle rain
            // shower rain and drizzle
            // heavy shower rain and drizzle
            case 300, 301, 302, 310, 311, 312, 313, 314, 321 ->    // shower drizzle
                    9;

            // Rain
            // light rain
            // moderate rain
            // light intensity shower rain
            // shower rain
            case 500, 501, 520, 521, 531 ->    // ragged shower rain
                    11;    // heavy intensity rain
            // very heavy rain
            // extreme rain
            case 502, 503, 504, 522 ->    // heavy intensity shower rain
                    12;
            case 511 ->    // freezing rain
                    10;

            // Snow
            case 600, 620 -> 14; // light snow
            case 601, 621 -> 16; // snow
            case 602, 622 -> 41; // heavy snow
            case 611, 612 -> 18; // sleet
            case 615, 616 -> 5;  // rain and snow

            // Atmosphere
            case 741 ->    // fog
                    20;    // smoke
            case 711, 762 ->    // volcanic ash
                    22;    // mist
            case 701, 721 ->    // haze
                    21;    // sand/dust whirls
            // sand
            case 731, 751, 761 ->    // dust
                    19;
            case 771 ->    // squalls
                    23;
            case 781 ->    // tornado
                    0;

            // clouds
            case 800 ->     // clear sky
                    icon.endsWith("n") ? 31 : 32; // day or night
            case 801 ->     // few clouds
                    icon.endsWith("n") ? 33 : 34; // day or night
            case 802 ->     // scattered clouds
                    icon.endsWith("n") ? 27 : 28; // day or night
            // broken clouds
            case 803, 804 ->     // overcast clouds
                    icon.endsWith("n") ? 29 : 30; // day or night

            // Extreme
            case 900 -> 0;  // tornado
            case 901 -> 1;  // tropical storm
            case 902 -> 2;  // hurricane
            case 903 -> 25; // cold
            case 904 -> 36; // hot
            case 905 -> 24; // windy
            case 906 -> 17;
            default -> // hail
                    -1;
        };

    }

    private void loadKeys() {
        try {
            String key = mContext.getResources().getString(R.string.owm_api_key_1);
            if (!TextUtils.isEmpty(key)) {
                mKeys.add(key);
            }
        } catch (Resources.NotFoundException ignored) {
        }
        try {
            String key = mContext.getResources().getString(R.string.owm_api_key_2);
            if (!TextUtils.isEmpty(key)) {
                mKeys.add(key);
            }
        } catch (Resources.NotFoundException ignored) {
        }
        try {
            String key = mContext.getResources().getString(R.string.owm_api_key);
            if (!TextUtils.isEmpty(key)) {
                mKeys.add(key);
            }
        } catch (Resources.NotFoundException ignored) {
        }
        log(TAG, "use API keys = " + mKeys);
    }

    private String getAPIKey() {
        String customKey = WeatherConfig.getOwmKey(mContext);
        if (!TextUtils.isEmpty(customKey)) {
            return customKey;
        }
        if (!mKeys.isEmpty()) {
            int key = mRequestNumber % mKeys.size();
            log(TAG, "use API key = " + key);
            return mKeys.get(key);
        }
        try {
            return mContext.getResources().getString(R.string.owm_api_key);
        } catch (Resources.NotFoundException ignored) {
        }
        return null;
    }

    public boolean shouldRetry() {
        return false;
    }
}
