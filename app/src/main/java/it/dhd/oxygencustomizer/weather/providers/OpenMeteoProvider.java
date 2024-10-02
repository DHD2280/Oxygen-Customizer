package it.dhd.oxygencustomizer.weather.providers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.weather.AbstractWeatherProvider;
import it.dhd.oxygencustomizer.weather.WeatherInfo;

public class OpenMeteoProvider extends AbstractWeatherProvider {
    private static final String TAG = "OpenMeteoProvider";

    private static final int FORECAST_DAYS = 5;
    private static final String URL_WEATHER =
            "https://api.open-meteo.com/v1/forecast?";
    private static final String PART_COORDINATES =
            "latitude=%f&longitude=%f";
    private static final String PART_PARAMETERS =
            "%s&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m,is_day&hourly=weather_code,temperature_2m&forecast_hours=24&daily=weather_code,temperature_2m_max,temperature_2m_min&temperature_unit=%s&windspeed_unit=%s&timezone=%s&past_days=1&models=best_match,gfs_seamless";


    public OpenMeteoProvider(Context context) {
        super(context);
    }

    public WeatherInfo getCustomWeather(String lat, String lon, boolean metric) {
        String id = String.format(Locale.US, PART_COORDINATES, Float.valueOf(lat), Float.valueOf(lon));
        return handleWeatherRequest(id, metric);
    }

    public WeatherInfo getLocationWeather(Location location, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, location.getLatitude(), location.getLongitude());
        return handleWeatherRequest(coordinates, metric);
    }

    private WeatherInfo handleWeatherRequest(String selection, boolean metric) {
        String tempUnit = metric ? "celsius" : "fahrenheit";
        String speedUnit = metric ? "kmh" : "mph";
        String timeZone = java.util.TimeZone.getDefault().getID();
        String conditionUrl = String.format(Locale.US,URL_WEATHER + PART_PARAMETERS, selection, tempUnit, speedUnit, timeZone);
        Log.w(TAG, "Condition URL = " + conditionUrl);
        String conditionResponse = retrieve(conditionUrl, null);
        if (conditionResponse == null) {
            return null;
        }
        log(TAG, "Condition URL = " + conditionUrl + " returning a response of " + conditionResponse);

        try {
            JSONObject weather = new JSONObject(conditionResponse).getJSONObject("current");

            String city = getWeatherDataLocality(selection);

            int weathercode = weather.getInt("weather_code");
            boolean isDay = weather.getInt("is_day") == 1;

            WeatherInfo w = new WeatherInfo(mContext,
                    /* id */ selection,
                    /* cityId */ city,
                    /* condition */ getWeatherDescription(weathercode),
                    /* conditionCode */ mapConditionIconToCode(weathercode, isDay),
                    /* temperature */ (float) weather.getDouble("temperature_2m"),
                    // Api: Humidity included in current
                    /* humidity */ (float) weather.getDouble("relative_humidity_2m"),
                    /* wind */ (float) weather.getDouble("wind_speed_10m"),
                    /* windDir */ weather.getInt("wind_direction_10m"),
                    metric,
                    parseHourlyForecasts(new JSONObject(conditionResponse).getJSONObject("hourly"), metric),
                    parseForecasts(new JSONObject(conditionResponse).getJSONObject("daily"), metric),
                    System.currentTimeMillis());

            log(TAG, "Weather updated: " + w);
            return w;
        } catch (JSONException e) {
            Log.e(TAG, "Received malformed weather data (coordinates = " + selection + ")" + " response:\n" + conditionResponse, e);
        }


        return null;
    }

    private static String getWeatherDescription(int code) {
        /*
         * 0 	Clear sky
         * 1, 2, 3 	Mainly clear, partly cloudy, and overcast
         * 45, 48 	Fog and depositing rime fog
         * 51, 53, 55 	Drizzle: Light, moderate, and dense intensity
         * 56, 57 	Freezing Drizzle: Light and dense intensity
         * 61, 63, 65 	Rain: Slight, moderate and heavy intensity
         * 66, 67 	Freezing Rain: Light and heavy intensity
         * 71, 73, 75 	Snow fall: Slight, moderate, and heavy intensity
         * 77 	Snow grains
         * 80, 81, 82 	Rain showers: Slight, moderate, and violent
         * 85, 86 	Snow showers slight and heavy
         * 95 * 	Thunderstorm: Slight or moderate
         * 96, 99 * 	Thunderstorm with slight and heavy hail
         */
        return switch (code) {
            case 0 -> "Clear sky";
            case 1 -> "Mainly clear";
            case 2 -> "Partly clouds";
            case 3 -> "Clouds";
            case 45 -> "Fog";
            case 48 -> "Depositing rime fog";
            case 51 -> "Light intensity drizzle rain";
            case 53 -> "Moderate intensity drizzle rain";
            case 55 -> "Dense intensity drizzle rain";
            case 56 -> "Light intensity freezing drizzle rain";
            case 57 -> "Dense intensity freezing drizzle rain";
            case 61 -> "Slight intensity rain";
            case 63 -> "Moderate intensity rain";
            case 65 -> "Heavy intensity rain";
            case 66 -> "Light intensity freezing rain";
            case 67 -> "Heavy intensity freezing rain";
            case 71 -> "Slight intensity snowfall";
            case 73 -> "Moderate intensity snowfall";
            case 75 -> "Heavy intensity snowfall";
            case 77 -> "Snow grains";
            case 80 -> "Slight intensity rain showers";
            case 81 -> "Moderate intensity rain showers";
            case 82 -> "Violent intensity rain showers";
            case 85 -> "Slight intensity snow showers";
            case 86 -> "Heavy intensity snow showers";
            case 95 -> "Slight or moderate thunderstorm";
            case 96 -> "Thunderstorm with slight hail";
            case 99 -> "Thunderstorm with heavy hail";
            default -> "Unknown";
        };
    }

    private ArrayList<WeatherInfo.DayForecast> parseForecasts(JSONObject forecasts, boolean metric) throws JSONException {
        ArrayList<WeatherInfo.DayForecast> result = new ArrayList<>(5);

        JSONArray timeJson = forecasts.getJSONArray("time");
        JSONArray temperatureMinJson = forecasts.getJSONArray("temperature_2m_min_best_match");
        JSONArray temperatureMaxJson = forecasts.getJSONArray("temperature_2m_max_best_match");
        JSONArray weatherCodeJson = forecasts.getJSONArray("weather_code_best_match");
        JSONArray altWeatherCodeJson = forecasts.getJSONArray("weather_code_gfs_seamless");
        String currentDay = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Calendar.getInstance().getTime());

        int startIndex = 1;
        if (currentDay.equals(timeJson.getString(0)))
            startIndex = 0;
        else if (currentDay.equals(timeJson.getString(2)))
            startIndex = 2;

        for (int i = startIndex; i < timeJson.length() && result.size() < 5; i++) {
            WeatherInfo.DayForecast item;
            int weatherCode = weatherCodeJson.getInt(i);
            if(weatherCode == 45 || weatherCode == 48)
                weatherCode = altWeatherCodeJson.getInt(i);

            try {
                item = new WeatherInfo.DayForecast(
                        /* low */ (float) temperatureMinJson.getDouble(i),
                        /* high */ (float) temperatureMaxJson.getDouble(i),
                        /* condition */ getWeatherDescription(weatherCode),
                        /* conditionCode */ mapConditionIconToCode(weatherCode, true),
                        timeJson.getString(i),
                        metric);
            } catch (JSONException e) {
                Log.w(TAG, "Invalid forecast for day " + i + " creating dummy", e);
                item = new WeatherInfo.DayForecast(
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
                WeatherInfo.DayForecast item = new WeatherInfo.DayForecast(
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

    private ArrayList<WeatherInfo.HourForecast> parseHourlyForecasts(JSONObject forecasts, boolean metric) throws JSONException {
        ArrayList<WeatherInfo.HourForecast> result = new ArrayList<>();

        JSONArray timeJson = forecasts.getJSONArray("time");
        JSONArray temperature = forecasts.getJSONArray("temperature_2m_best_match");
        JSONArray weatherCodeJson = forecasts.getJSONArray("weather_code_best_match");
        JSONArray altWeatherCodeJson = forecasts.getJSONArray("weather_code_gfs_seamless");
        String currentDay = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US).format(Calendar.getInstance().getTime());

        int startIndex = 1;
        if (currentDay.equals(timeJson.getString(0)))
            startIndex = 0;
        else if (currentDay.equals(timeJson.getString(2)))
            startIndex = 2;

        for (int i = startIndex; i < timeJson.length() && result.size() < 10; i++) {
            WeatherInfo.HourForecast item;
            int weatherCode = weatherCodeJson.getInt(i);
            if(weatherCode == 45 || weatherCode == 48)
                weatherCode = altWeatherCodeJson.getInt(i);

            try {
                item = new WeatherInfo.HourForecast(
                        /* temp */ (float) temperature.getDouble(i),
                        /* condition */ getWeatherDescription(weatherCode),
                        /* conditionCode */ mapConditionIconToCode(weatherCode, true),
                        timeJson.getString(i),
                        metric);
            } catch (JSONException e) {
                Log.w(TAG, "Invalid forecast for day " + i + " creating dummy", e);
                item = new WeatherInfo.HourForecast(
                        /* temp */ 0,
                        /* condition */ "",
                        /* conditionCode */ -1,
                        "NaN",
                        metric);
            }
            result.add(item);
        }
        // clients assume there are 5  entries - so fill with dummy if needed
        if (result.size() < 10) {
            for (int i = result.size(); i < 10; i++) {
                Log.w(TAG, "Missing forecast for hour " + i + " creating dummy");
                WeatherInfo.HourForecast item = new WeatherInfo.HourForecast(
                        /* temp */ 0,
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

    private int mapConditionIconToCode(int code, boolean isDay) {

        return switch (code) {
            case 0 -> // Clear sky
                    isDay ? 32 : 31;
            case 1 -> // Mainly clear
                    isDay ? 34 : 33;
            case 2 -> // Partly cloudy
                    isDay ? 30 : 29;
            case 3 -> // Overcast
                    26; // Fog
            case 45, 48 -> // Depositing rime fog
                    20;
            case 51 -> // Light intensity drizzle
                    9;
            case 53 -> // Moderate intensity drizzle
                    9;
            case 55 -> // Dense intensity drizzle
                    12;
            case 56 -> // Light intensity freezing drizzle
                    8;
            case 57 -> // Dense intensity freezing drizzle
                    8;
            case 61 -> // Slight intensity rain
                    9;
            case 63 -> // Moderate intensity rain
                    11;
            case 65 -> // Heavy intensity rain
                    12;
            case 66 -> // Light intensity freezing rain
                    10;
            case 67 -> // Heavy intensity freezing rain
                    10;
            case 71 -> // Slight intensity snowfall
                    14;
            case 73 -> // Moderate intensity snowfall
                    16;
            case 75 -> // Heavy intensity snowfall
                    43;
            case 77 -> // Snow grains
                    16;
            case 80 -> // Slight intensity rain showers
                    11;
            case 81 -> // Moderate intensity rain showers
                    40;
            case 82 -> // Violent intensity rain showers
                    40;
            case 85 -> // Slight intensity snow showers
                    14;
            case 86 -> // Heavy intensity snow showers
                    43;
            case 95 -> // Slight or moderate thunderstorm
                    4; // Thunderstorm with slight hail
            case 96, 99 -> // Thunderstorm with heavy hail
                    38;
            default -> // Unknown
                    -1;
        };

    }

    public boolean shouldRetry() {
        return false;
    }
}