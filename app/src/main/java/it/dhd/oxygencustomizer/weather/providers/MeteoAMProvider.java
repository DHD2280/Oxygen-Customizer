package it.dhd.oxygencustomizer.weather.providers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import it.dhd.oxygencustomizer.weather.AbstractWeatherProvider;
import it.dhd.oxygencustomizer.weather.WeatherInfo;

public class MeteoAMProvider extends AbstractWeatherProvider {
    private static final String TAG = "MeteoAMProvider";

    private static final String URL_WEATHER = "https://api.meteoam.it/deda-meteograms/api/GetMeteogram/preset1/%s,%s";
    private static final int DAY_FORECASTS = 5;
    private static final int HOUR_FORECASTS = 10;
    private static final float KMH_TO_MPH = 0.621371f;

    private static final String PARAM_TEMP_C = "2t";
    private static final String PARAM_TEMP_F = "2tf";
    private static final String PARAM_HUMIDITY = "r";
    private static final String PARAM_PRESSURE = "pmsl";
    private static final String PARAM_RAIN_CHANCE = "tpp";
    private static final String PARAM_WIND_DIRECTION = "wdir";
    private static final String PARAM_WIND_KMH = "wkmh";
    private static final String PARAM_ICON = "icon";

    public MeteoAMProvider(Context context) {
        super(context);
    }

    @Override
    public WeatherInfo getCustomWeather(String lat, String lon, boolean metric) {
        return handleWeatherRequest(Float.parseFloat(lat), Float.parseFloat(lon), metric);
    }

    @Override
    public WeatherInfo getLocationWeather(Location location, boolean metric) {
        return handleWeatherRequest(location.getLatitude(), location.getLongitude(), metric);
    }

    private WeatherInfo handleWeatherRequest(double lat, double lon, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, lat, lon);
        String url = String.format(Locale.US, URL_WEATHER, coordinate(lat), coordinate(lon));
        String response = retrieve(url, null);
        if (response == null) return null;
        log(TAG, "URL = " + url + " returning a response of " + response);

        try {
            Meteogram meteogram = new Meteogram(new JSONObject(response));
            int now = meteogram.currentIndex();
            if (now < 0) return null;

            float temperature = (float) meteogram.number(metric ? PARAM_TEMP_C : PARAM_TEMP_F, now);
            float wind = (float) meteogram.number(PARAM_WIND_KMH, now);
            if (!metric && !Float.isNaN(wind)) wind *= KMH_TO_MPH;
            String icon = meteogram.string(PARAM_ICON, now);

            WeatherInfo w = new WeatherInfo(mContext,
                    /* id */ coordinates,
                    /* cityId */ getWeatherDataLocality(coordinates),
                    /* condition */ getWeatherDescription(icon),
                    /* conditionCode */ mapConditionIconToCode(icon),
                    /* temperature */ temperature,
                    /* humidity */ (float) meteogram.number(PARAM_HUMIDITY, now),
                    /* wind */ Float.isNaN(wind) ? 0 : wind,
                    /* windDir */ (int) Math.round(nonNaN(meteogram.number(PARAM_WIND_DIRECTION, now))),
                    metric,
                    parseHourlyForecasts(meteogram, now, metric),
                    parseForecasts(meteogram, metric),
                    System.currentTimeMillis());
            double rain = meteogram.number(PARAM_RAIN_CHANCE, now);
            w.setExtras(
                    Float.NaN,
                    Float.NaN,
                    (float) meteogram.number(PARAM_PRESSURE, now),
                    Double.isNaN(rain) ? -1 : (int) Math.round(rain),
                    0,
                    0);

            log(TAG, "Weather updated: " + w);
            return w;
        } catch (JSONException e) {
            Log.e(TAG, "Received malformed weather data (coordinates = " + coordinates + ")", e);
        }
        return null;
    }

    private static String coordinate(double value) {
        return String.format(Locale.US, "%.4f", value);
    }

    private static double nonNaN(double value) {
        return Double.isNaN(value) ? 0 : value;
    }

    private ArrayList<WeatherInfo.HourForecast> parseHourlyForecasts(Meteogram meteogram, int start, boolean metric) {
        ArrayList<WeatherInfo.HourForecast> result = new ArrayList<>();
        SimpleDateFormat local = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.US);
        for (int i = start; i < meteogram.size() && result.size() < HOUR_FORECASTS; i++) {
            long time = meteogram.time(i);
            double temp = meteogram.number(metric ? PARAM_TEMP_C : PARAM_TEMP_F, i);
            if (time <= 0 || Double.isNaN(temp)) continue;
            String icon = meteogram.string(PARAM_ICON, i);
            result.add(new WeatherInfo.HourForecast((float) temp, getWeatherDescription(icon), mapConditionIconToCode(icon), local.format(new Date(time)), metric));
        }
        while (result.size() < HOUR_FORECASTS) {
            result.add(new WeatherInfo.HourForecast(0, "", -1, "NaN", metric));
        }
        return result;
    }

    private ArrayList<WeatherInfo.DayForecast> parseForecasts(Meteogram meteogram, boolean metric) throws JSONException {
        ArrayList<WeatherInfo.DayForecast> result = new ArrayList<>(DAY_FORECASTS);
        JSONArray stats = meteogram.stats();
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (stats != null) {
            for (int i = 0; i < stats.length() && result.size() < DAY_FORECASTS; i++) {
                JSONObject day = stats.getJSONObject(i);
                String date = day.optString("localDate");
                if (date.length() < 10) continue;
                date = date.substring(0, 10);
                if (date.compareTo(today) < 0) continue;
                String icon = day.optString("icon");
                result.add(new WeatherInfo.DayForecast(
                        (float) day.optDouble(metric ? "minCelsius" : "minFahrenheit", 0),
                        (float) day.optDouble(metric ? "maxCelsius" : "maxFahrenheit", 0),
                        getWeatherDescription(icon),
                        mapConditionIconToCode(icon),
                        date,
                        metric));
            }
        }
        while (result.size() < DAY_FORECASTS) {
            result.add(new WeatherInfo.DayForecast(0, 0, "", -1, "NaN", metric));
        }
        return result;
    }

    private static String getWeatherDescription(String icon) {
        return switch (icon) {
            case "01", "31" -> "Clear sky";
            case "02" -> "Mainly clear";
            case "03", "33" -> "Haze";
            case "04", "34" -> "Partly clouds";
            case "05", "35" -> "Mostly clouds";
            case "06", "36" -> "Clouds";
            case "07" -> "Overcast";
            case "08" -> "Rain";
            case "09" -> "Heavy rain";
            case "10" -> "Thunderstorm";
            case "11" -> "Rain and snow";
            case "12" -> "Sleet";
            case "13", "14", "32" -> "Fog";
            case "15" -> "Heavy rain showers";
            case "16" -> "Snow";
            case "17" -> "Storm";
            case "18" -> "Wind";
            case "19" -> "Sand storm";
            default -> "Unknown";
        };
    }

    private static int mapConditionIconToCode(String icon) {
        return switch (icon) {
            case "01" -> 32;
            case "02" -> 34;
            case "03", "33" -> 21;
            case "04" -> 30;
            case "05" -> 28;
            case "06", "07", "36" -> 26;
            case "08" -> 11;
            case "09" -> 12;
            case "10" -> 4;
            case "11" -> 5;
            case "12" -> 18;
            case "13", "14", "32" -> 20;
            case "15" -> 40;
            case "16" -> 16;
            case "17" -> 3;
            case "18" -> 24;
            case "19" -> 19;
            case "31" -> 31;
            case "34" -> 29;
            case "35" -> 27;
            default -> -1;
        };
    }

    @Override
    public boolean shouldRetry() {
        return false;
    }

    private static final class Meteogram {
        private final JSONArray mTimes;
        private final JSONArray mParams;
        private final JSONObject mData;
        private final JSONObject mExtra;
        private final SimpleDateFormat mUtc = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

        Meteogram(JSONObject root) throws JSONException {
            mTimes = root.getJSONArray("timeseries");
            mParams = root.getJSONArray("paramlist");
            JSONObject datasets = root.getJSONObject("datasets");
            mData = datasets.getJSONObject(datasets.keys().next());
            mExtra = root.optJSONObject("extrainfo");
            mUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        int size() {
            return mTimes.length();
        }

        long time(int index) {
            try {
                Date date = mUtc.parse(mTimes.optString(index));
                return date == null ? 0 : date.getTime();
            } catch (ParseException e) {
                return 0;
            }
        }

        int currentIndex() {
            long now = System.currentTimeMillis();
            int current = -1;
            for (int i = 0; i < size(); i++) {
                long time = time(i);
                if (time <= 0) continue;
                if (time <= now) current = i;
                else return current < 0 ? i : current;
            }
            return current;
        }

        private JSONObject series(String param) {
            for (int i = 0; i < mParams.length(); i++) {
                if (param.equals(mParams.optString(i)))
                    return mData.optJSONObject(String.valueOf(i));
            }
            return null;
        }

        double number(String param, int index) {
            JSONObject series = series(param);
            if (series == null || !series.has(String.valueOf(index)) || series.isNull(String.valueOf(index)))
                return Double.NaN;
            return series.optDouble(String.valueOf(index), Double.NaN);
        }

        String string(String param, int index) {
            JSONObject series = series(param);
            return series == null ? "" : series.optString(String.valueOf(index), "");
        }

        JSONArray stats() {
            return mExtra == null ? null : mExtra.optJSONArray("stats");
        }
    }
}
