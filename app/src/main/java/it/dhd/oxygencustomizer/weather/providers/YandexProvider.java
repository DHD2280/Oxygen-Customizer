package it.dhd.oxygencustomizer.weather.providers;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.AbstractWeatherProvider;
import it.dhd.oxygencustomizer.weather.WeatherConfig;
import it.dhd.oxygencustomizer.weather.WeatherInfo;

public class YandexProvider extends AbstractWeatherProvider {
    private static final String TAG = "YandexProvider";

    private static final String URL_WEATHER =
            "https://api.weather.yandex.ru/v2/forecast?";
    private static final String PART_PARAMETERS =
            "&limit=6&hours=false&lang=%s";
    private static final String URL_PLACES =
            "http://api.geonames.org/searchJSON?q=%s&lang=%s&username=omnijaws&isNameRequired=true";
    private static final String PART_COORDINATES =
            "lat=%f&lon=%f";

    public YandexProvider(Context context) {
        super(context);
    }

    public WeatherInfo getCustomWeather(String lat, String lon, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, Float.valueOf(lat), Float.valueOf(lon));
        return getAllWeather(coordinates, metric);
    }

    public WeatherInfo getLocationWeather(Location location, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, location.getLatitude(), location.getLongitude());
        return getAllWeather(coordinates, metric);
    }

    private WeatherInfo getAllWeather(String coordinates, boolean metric) {
        String url = String.format(URL_WEATHER + coordinates + PART_PARAMETERS, getLanguage());
        String apiKey = WeatherConfig.getYandexKey(mContext);
        // Check API Key first
        if (TextUtils.isEmpty(apiKey)) {
            Log.e(TAG, "Yandex API key is not set");
            return null;
        }
        String response = retrieve(url, new String[]{"X-Yandex-Weather-Key", WeatherConfig.getYandexKey(mContext)});
        if (response == null) {
            return null;
        }
        log(TAG, "URL = " + url + " returning a response of " + response);

        try {
            JSONObject weather = new JSONObject(response);
            JSONObject current = weather.getJSONObject("fact");

            String city = getWeatherDataLocality(coordinates);
            if (TextUtils.isEmpty(city)) {
                city = mContext.getResources().getString(R.string.omnijaws_city_unknown);
            }

            WeatherInfo w = new WeatherInfo(mContext,
                    /* id */ coordinates,
                    /* cityId */ city,
                    /* condition */ CONDITION_MAPPING.getOrDefault(current.optString("condition"), "error"),
                    /* conditionCode */ ICON_MAPPING.getOrDefault(current.getString("icon"), -1),
                    /* temperature */ convertTemperature(current.getInt("temp"), metric),
                    /* humidity */ (float) current.getInt("humidity"),
                    /* wind */ convertWind(current.getInt("wind_speed"), metric),
                    /* windDir */ convertWindDegree(current.getString("wind_dir")),
                    metric,
                    parseForecasts(weather.getJSONArray("forecasts"), metric),
                    weather.getLong("now") * 1000L);

            log(TAG, "Weather updated: " + w);
            return w;

        } catch (JSONException e) {
            Log.w(TAG, "Received malformed weather data (coordinates = " + coordinates + ")", e);
        }

        return null;
    }

    private ArrayList<WeatherInfo.DayForecast> parseForecasts(JSONArray forecasts, boolean metric) throws JSONException {
        ArrayList<WeatherInfo.DayForecast> result = new ArrayList<>(5);
        int count = forecasts.length();

        if (count == 0) {
            throw new JSONException("Empty forecasts array");
        }

        for (int i = 0; i < count && result.size() < 5; i++) {
            WeatherInfo.DayForecast item;
            try {
                JSONObject forecast = forecasts.getJSONObject(i);

                if (i == 0 && !checkYesterday(forecast.getString("date"))) {
                    // skip if yesterday
                    continue;
                }

                JSONObject forecastParts = forecast.getJSONObject("parts");
                item = new WeatherInfo.DayForecast(
                        /* low */ convertTemperature(getMinMaxTemp(forecastParts, false), metric),
                        /* high */ convertTemperature(getMinMaxTemp(forecastParts, true), metric),
                        /* condition */ forecastParts.getJSONObject("day").getString("condition"),
                        /* conditionCode */ ICON_MAPPING.getOrDefault(forecastParts.getJSONObject("day_short").getString("icon"), -1),
                        forecast.getString("date"),
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

    private static final HashMap<String, Integer> ICON_MAPPING = new HashMap<>();
    static {
        ICON_MAPPING.put("bkn_d", 30);
        ICON_MAPPING.put("bkn_n", 29);
        ICON_MAPPING.put("bkn_ra_d", 11);
        ICON_MAPPING.put("bkn_ra_n", 11);
        ICON_MAPPING.put("bkn_-ra_d", 9);
        ICON_MAPPING.put("bkn_-ra_n", 9);
        ICON_MAPPING.put("bkn_-sn_d", 14);
        ICON_MAPPING.put("bkn_-sn_n", 14);
        ICON_MAPPING.put("ovc_-ra", 9);
        ICON_MAPPING.put("ovc_-sn", 14);
        ICON_MAPPING.put("bkn_+ra_d", 12);
        ICON_MAPPING.put("bkn_+ra_n", 12);
        ICON_MAPPING.put("bkn_+sn_d", 13);
        ICON_MAPPING.put("bkn_+sn_n", 13);
        ICON_MAPPING.put("ovc_+ra", 12);
        ICON_MAPPING.put("ovc_+sn", 13);
        ICON_MAPPING.put("bkn_sn_d", 14);
        ICON_MAPPING.put("bkn_sn_n", 14);
        ICON_MAPPING.put("bl", 15);
        ICON_MAPPING.put("fg_d", 20);
        ICON_MAPPING.put("fg_n", 20);
        ICON_MAPPING.put("ovc", 26);
        ICON_MAPPING.put("ovc_gr", 17);
        ICON_MAPPING.put("ovc_ra", 12);
        ICON_MAPPING.put("ovc_ra_sn", 18);
        ICON_MAPPING.put("ovc_sn", 16);
        ICON_MAPPING.put("ovc_ts_ra", 4);
        ICON_MAPPING.put("skc_d", 32);
        ICON_MAPPING.put("skc_n", 31);
    }

    /*
    clear — Clear.
    partly-cloudy — Partly cloudy.
    cloudy — Cloudy.- overcast — Overcast.
    drizzle — Drizzle.
    light-rain — Light rain.
    rain — Rain.
    moderate-rain — Moderate rain.
    heavy-rain — Heavy rain.
    continuous-heavy-rain — Continuous heavy rain.
    showers — Showers.- wet-snow — Sleet.
    light-snow — Light snow.
    snow — Snow.
    snow-showers — Snowfall.
    hail — Hail.
    thunderstorm — Thunderstorm.
    thunderstorm-with-rain — Rain, thunderstorm.
    thunderstorm-with-hail — Thunderstorm, hail.
     */
    private static final HashMap<String, String> CONDITION_MAPPING = new HashMap<>();
    static {
        CONDITION_MAPPING.put("clear", "Clear sky");
        CONDITION_MAPPING.put("partly-cloudy", "Partly clouds");
        CONDITION_MAPPING.put("cloudy", "Clouds");
        CONDITION_MAPPING.put("overcast", "Clouds");
        CONDITION_MAPPING.put("partly-cloudy-and-light-rain", "partly_clouds_and_light_rain");
        CONDITION_MAPPING.put("partly-cloudy-and-rain", "partly_cloudy_and_rain");
        CONDITION_MAPPING.put("overcast-and-rain", "Light intensity drizzle rain");
        CONDITION_MAPPING.put("overcast-thunderstorms-with-rain", "overcast_thunderstorms_with_rain");
        CONDITION_MAPPING.put("cloudy-and-light-rain", "cloudy_and_light_rain");
        CONDITION_MAPPING.put("overcast-and-light-rain", "overcast_and_light_rain");
        CONDITION_MAPPING.put("cloudy-and-rain", "cloudy_and_rain");
        CONDITION_MAPPING.put("overcast-and-wet-snow", "overcast_and_wet_snow");
        CONDITION_MAPPING.put("partly-cloudy-and-light-snow", "partly_cloudy_and_light_snow");
        CONDITION_MAPPING.put("partly-cloudy-and-snow", "partly_cloudy_and_snow");
        CONDITION_MAPPING.put("overcast-and-snow", "overcast_and_snow");
        CONDITION_MAPPING.put("cloudy-and-light-snow", "cloudy_and_light_snow");
        CONDITION_MAPPING.put("overcast-and-light-snow", "overcast_and_light_snow");
        CONDITION_MAPPING.put("cloudy-and-snow", "snow");
    }

    private static String getLanguage() {
        String currentLang = Locale.getDefault().toString();
        String[] availableLang = {"ru_RU", "ru_UA", "uk_UA", "be_BY", "kk_KZ", "tr_TR", "en_US"};
        return Arrays.asList(availableLang).contains(currentLang) ? currentLang : "en_US";
    }

    // if yesterday returns false
    private static boolean checkYesterday(String valueDate)
    {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
        return !yesterday.equals(valueDate);
    }

    // !needMax = needMin
    private static int getMinMaxTemp(JSONObject dayPart, boolean needMax) throws JSONException {
        String[] typePart = {"night","morning","day","evening"};
        int result  = needMax ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (String s : typePart) {
            int tmp = dayPart.getJSONObject(s).getInt(needMax ? "temp_max" : "temp_min");

            if ((needMax && tmp > result) || (!needMax && tmp < result)) {
                result = tmp;
            }
        }
        return result;
    }

    private static float convertTemperature(int value, boolean metric) {
        return metric ? value : (value * 1.8F) + 32;
    }

    private static float convertWind(int value, boolean metric) {
        return metric ? value * 3.6F : value / 0.44704F;
    }

    private static int convertWindDegree(String value)
    {
        switch (value) {
            case "nw":
                return 315;
            case "n":
                return 360;
            case "ne":
                return 45;
            case "e":
                return 90;
            case "se":
                return 135;
            case "s":
                return 180;
            case "sw":
                return 225;
            case "w":
                return 270;
            default:
                return 0;
        }
    }

    public boolean shouldRetry() {
        return false;
    }
}
