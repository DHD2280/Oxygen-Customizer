package it.dhd.oxygencustomizer.weather.providers;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.weather.AbstractWeatherProvider;
import it.dhd.oxygencustomizer.weather.WeatherInfo;
import it.dhd.oxygencustomizer.weather.WeatherInfo.DayForecast;

public class METNorwayProvider extends AbstractWeatherProvider {
    private static final String TAG = "METNorwayProvider";

    private static final String URL_WEATHER =
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?";

    private static final SimpleDateFormat gmt0Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
    private static final SimpleDateFormat userTimeZoneFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    public METNorwayProvider(Context context) {
        super(context);
        initTimeZoneFormat();
    }

    public WeatherInfo getLocationWeather(Location location, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, location.getLatitude(), location.getLongitude());
        return getAllWeather(coordinates, metric);
    }

    public WeatherInfo getCustomWeather(String lat, String lon, boolean metric) {
        String coordinates = String.format(Locale.US, PART_COORDINATES, Float.valueOf(lat), Float.valueOf(lon));
        return getAllWeather(coordinates, metric);
    }

    private WeatherInfo getAllWeather(String coordinates, boolean metric) {
        String url = URL_WEATHER + coordinates;
        String response = retrieve(url, new String[]{"User-Agent", "OxygenCustomizer/" + BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE});
        if (response == null) {
            return null;
        }
        log(TAG, "URL = " + url + " returning a response of " + response);
        Log.w(TAG, "Response: " + response);

        try {
            JSONArray timeseries = new JSONObject(response).getJSONObject("properties").getJSONArray("timeseries");
            JSONObject weather = timeseries.getJSONObject(0).getJSONObject("data").getJSONObject("instant").getJSONObject("details");

            String symbolCode = timeseries.getJSONObject(0).getJSONObject("data").getJSONObject("next_1_hours").getJSONObject("summary").getString("symbol_code");
            String conditionDescription = getWeatherCondition(symbolCode);
            int weatherCode = arrayWeatherIconToCode[getPriorityCondition(symbolCode)];

            // Check Available Night Icon
            if(symbolCode.contains("_night") && (weatherCode == 30 || weatherCode == 32 || weatherCode == 34)) {
                weatherCode -= 1;
            }

            String city = getWeatherDataLocality(coordinates);
            ArrayList<WeatherInfo.HourForecast> hourlyForecasts = new ArrayList<>();

            WeatherInfo w = new WeatherInfo(mContext,
                    /* id */ coordinates,
                    /* cityId */ city,
                    /* condition */ conditionDescription,
                    /* conditionCode */ weatherCode,
                    /* temperature */ convertTemperature(weather.getDouble("air_temperature"), metric),
                    /* humidity */ (float) weather.getDouble("relative_humidity"),
                    /* wind */ convertWindSpeed(weather.getDouble("wind_speed"), metric),
                    /* windDir */ (int) weather.getDouble("wind_from_direction"),
                    metric,
                    parseHourlyForecasts(timeseries, metric),
                    parseForecasts(timeseries, metric),
                    System.currentTimeMillis());

            log(TAG, "Weather updated: " + w);
            return w;
        } catch (JSONException e) {
            Log.w(TAG, "Received malformed weather data (coordinates = " + coordinates + ")", e);
        }

        return null;
    }

    private ArrayList<DayForecast> parseForecasts(JSONArray timeseries, boolean metric) throws JSONException {
        ArrayList<DayForecast> result = new ArrayList<>(5);
        int count = timeseries.length();

        if (count == 0) {
            throw new JSONException("Empty forecasts array");
        }

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);
        String yesterday = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());

        int whileIndex = 0;

        while (convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")).contains(yesterday)) {
            whileIndex++;
        }

        boolean endDay = (whileIndex == 0) && isEndDay(convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")));

        for (int i = 0; i < 5; i++) {
            DayForecast item;
            try {
                // temp = temperature
                double temp_max = Double.MIN_VALUE;
                double temp_min = Double.MAX_VALUE;
                String day = getDay(i);
                int symbolCode = 0;
                int scSixToTwelve = 0; // symbolCode next_6_hours at 06:00
                int scTwelveToEighteen = 0; // symbolCode next_6_hours at 12:00
                int scSixToEighteen = 0; // symbolCode next_12_hours at 06:00
                boolean hasFastCondition = false; // If true, there is no need to calculate "symbolCode" and "conditionDescription".
                String conditionDescription = "";
                String cdSixToEighteen = ""; // conditionDescription at 06:00 or 12:00

                while (convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")).contains(day)) {
                    double tempI = timeseries.getJSONObject(whileIndex).getJSONObject("data").getJSONObject("instant").getJSONObject("details").getDouble("air_temperature");

                    if (tempI > temp_max) {
                        temp_max = tempI;
                    }
                    if (tempI < temp_min) {
                        temp_min = tempI;
                    }

                    boolean hasOneHour = timeseries.getJSONObject(whileIndex).getJSONObject("data").has("next_1_hours");
                    boolean hasSixHours = timeseries.getJSONObject(whileIndex).getJSONObject("data").has("next_6_hours");
                    boolean hasTwelveHours = timeseries.getJSONObject(whileIndex).getJSONObject("data").has("next_12_hours");

                    hasFastCondition = scSixToEighteen != 0 || (scSixToTwelve != 0 && scTwelveToEighteen != 0);

                    if (!hasFastCondition && ((i == 0 && endDay) || isMorningOrAfternoon(convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")), hasOneHour))) {
                        String stepHours = hasOneHour ? "next_1_hours" : "next_6_hours";

                        String stepTextSymbolCode = timeseries.getJSONObject(whileIndex).getJSONObject("data").getJSONObject(stepHours).getJSONObject("summary").getString("symbol_code");
                        int stepSymbolCode = getPriorityCondition(stepTextSymbolCode);

                        if (stepSymbolCode > symbolCode) {
                            symbolCode = stepSymbolCode;
                            conditionDescription = stepTextSymbolCode;
                        }

                        if(hasSixHours || hasTwelveHours) {
                            if (convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")).contains("T06")) {
                                String textSymbolCode = timeseries.getJSONObject(whileIndex).getJSONObject("data").getJSONObject(hasTwelveHours ? "next_12_hours" : "next_6_hours").getJSONObject("summary").getString("symbol_code");
                                if (hasTwelveHours) {
                                    scSixToEighteen = getPriorityCondition(textSymbolCode);
                                    cdSixToEighteen = timeseries.getJSONObject(whileIndex).getJSONObject("data").getJSONObject("next_12_hours").getJSONObject("summary").getString("symbol_code");
                                } else {
                                    scSixToTwelve = getPriorityCondition(textSymbolCode);
                                    cdSixToEighteen = textSymbolCode;
                                }
                            } else if (scSixToTwelve != 0 && convertTimeZone(timeseries.getJSONObject(whileIndex).getString("time")).contains("T12")) {
                                String textSymbolCode = timeseries.getJSONObject(whileIndex).getJSONObject("data").getJSONObject("next_6_hours").getJSONObject("summary").getString("symbol_code");
                                scTwelveToEighteen = getPriorityCondition(textSymbolCode);

                                if (scSixToTwelve < scTwelveToEighteen) {
                                    cdSixToEighteen = textSymbolCode;
                                }
                            }
                        }
                    }
                    whileIndex++;
                }

                if(hasFastCondition) {
                    symbolCode = (scSixToEighteen != 0) ? scSixToEighteen : Math.max(scSixToTwelve, scTwelveToEighteen);
                    conditionDescription = cdSixToEighteen;
                }

                String formattedConditionDescription = getWeatherCondition(conditionDescription);

                item = new DayForecast(
                        /* low */ convertTemperature(temp_min, metric),
                        /* high */ convertTemperature(temp_max, metric),
                        /* condition */ formattedConditionDescription,
                        /* conditionCode */ arrayWeatherIconToCode[symbolCode],
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

    private ArrayList<WeatherInfo.HourForecast> parseHourlyForecasts(JSONArray timeseries, boolean metric) throws JSONException {
        ArrayList<WeatherInfo.HourForecast> result = new ArrayList<>(10);

        int count = timeseries.length();
        if (count == 0) {
            throw new JSONException("Empty forecasts array");
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        for (int i = 0; i < count; i++) {
            JSONObject item = timeseries.getJSONObject(i);
            String timeString = item.getString("time");
            LocalDateTime time = LocalDateTime.parse(timeString, formatter);

            JSONObject data = item.getJSONObject("data");
            JSONObject next1Hours = data.optJSONObject("next_1_hours");

            if (next1Hours != null) {
                if (time.isAfter(now)) {
                    JSONObject instant = data.getJSONObject("instant").getJSONObject("details");
                    double temperature = instant.getDouble("air_temperature");
                    String symbolCode = next1Hours.getJSONObject("summary").getString("symbol_code");
                    String formattedConditionDescription = getWeatherCondition(symbolCode);

                    WeatherInfo.HourForecast hour = new WeatherInfo.HourForecast(
                            /* temp */ convertTemperature(temperature, metric),
                            /* condition */ formattedConditionDescription,
                            /* conditionCode */ arrayWeatherIconToCode[getPriorityCondition(symbolCode)],
                            /* date */ timeString,
                            metric);

                    result.add(hour);

                    if (result.size() >= 10) {
                        break;
                    }
                }
            }
        }
        return result;

    }

    private static final HashMap<String, String> WEATHER_CONDITION_MAPPING = new HashMap<>();
    static {
        WEATHER_CONDITION_MAPPING.put("clearsky", "Clear Sky");
        WEATHER_CONDITION_MAPPING.put("fair", "Mostly Clear");
        WEATHER_CONDITION_MAPPING.put("partlycloudy", "Mostly Cloudy");
        WEATHER_CONDITION_MAPPING.put("cloudy", "Cloudy");
        WEATHER_CONDITION_MAPPING.put("rainshowers", "Showers");
        WEATHER_CONDITION_MAPPING.put("rainshowersandthunder", "Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("sleetshowers", "Sleet Showers");
        WEATHER_CONDITION_MAPPING.put("snowshowers", "Snow Showers");
        WEATHER_CONDITION_MAPPING.put("rain", "Rainfall");
        WEATHER_CONDITION_MAPPING.put("heavyrain", "Heavy Rainfall");
        WEATHER_CONDITION_MAPPING.put("heavyrainandthunder", "Heavy Rainfall and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("sleet", "Sleet");
        WEATHER_CONDITION_MAPPING.put("snow", "Snowfall");
        WEATHER_CONDITION_MAPPING.put("snowandthunder", "Snowfall and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("fog", "Foggy");
        WEATHER_CONDITION_MAPPING.put("sleetshowersandthunder", "Sleet Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("snowshowersandthunder", "Snow Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("rainandthunder", "Rainfall and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("sleetandthunder", "Sleet and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightrainshowersandthunder", "Light Rain Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("heavyrainshowersandthunder", "Heavy Rain Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightssleetshowersandthunder", "Light Sleet Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("heavysleetshowersandthunder", "Heavy Sleet Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightssnowshowersandthunder", "Light Snow Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("heavysnowshowersandthunder", "Heavy Snow Showers and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightrainandthunder", "Light Rain and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightsleetandthunder", "Light Sleet and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("heavysleetandthunder", "Heavy Sleet and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightsnowandthunder", "Light Snow and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("heavysnowandthunder", "Heavy Snow and Thunderstorms");
        WEATHER_CONDITION_MAPPING.put("lightrainshowers", "Light Rain Showers");
        WEATHER_CONDITION_MAPPING.put("heavyrainshowers", "Heavy Rain Showers");
        WEATHER_CONDITION_MAPPING.put("lightsleetshowers", "Light Sleet Showers");
        WEATHER_CONDITION_MAPPING.put("heavysleetshowers", "Heavy Sleet Showers");
        WEATHER_CONDITION_MAPPING.put("lightsnowshowers", "Light Snow Showers");
        WEATHER_CONDITION_MAPPING.put("heavysnowshowers", "Heavy Snow Showers");
        WEATHER_CONDITION_MAPPING.put("lightrain", "Light Rain");
        WEATHER_CONDITION_MAPPING.put("lightsleet", "Light Sleet");
        WEATHER_CONDITION_MAPPING.put("heavysleet", "Heavy Sleet");
        WEATHER_CONDITION_MAPPING.put("lightsnow", "Light Snow");
        WEATHER_CONDITION_MAPPING.put("heavysnow", "Heavy Snow");
    }

    private static final HashMap<String, Integer> SYMBOL_CODE_MAPPING = new HashMap<>();
    static {
        SYMBOL_CODE_MAPPING.put("clearsky", 1);
        SYMBOL_CODE_MAPPING.put("fair", 2);
        SYMBOL_CODE_MAPPING.put("partlycloudy", 3);
        SYMBOL_CODE_MAPPING.put("cloudy", 4);
        SYMBOL_CODE_MAPPING.put("rainshowers", 5);
        SYMBOL_CODE_MAPPING.put("rainshowersandthunder", 6);
        SYMBOL_CODE_MAPPING.put("sleetshowers", 7);
        SYMBOL_CODE_MAPPING.put("snowshowers", 8);
        SYMBOL_CODE_MAPPING.put("rain", 9);
        SYMBOL_CODE_MAPPING.put("heavyrain", 10);
        SYMBOL_CODE_MAPPING.put("heavyrainandthunder", 11);
        SYMBOL_CODE_MAPPING.put("sleet", 12);
        SYMBOL_CODE_MAPPING.put("snow", 13);
        SYMBOL_CODE_MAPPING.put("snowandthunder", 14);
        SYMBOL_CODE_MAPPING.put("fog", 15);
        SYMBOL_CODE_MAPPING.put("sleetshowersandthunder", 20);
        SYMBOL_CODE_MAPPING.put("snowshowersandthunder", 21);
        SYMBOL_CODE_MAPPING.put("rainandthunder", 22);
        SYMBOL_CODE_MAPPING.put("sleetandthunder", 23);
        SYMBOL_CODE_MAPPING.put("lightrainshowersandthunder", 24);
        SYMBOL_CODE_MAPPING.put("heavyrainshowersandthunder", 25);
        SYMBOL_CODE_MAPPING.put("lightssleetshowersandthunder", 26);
        SYMBOL_CODE_MAPPING.put("heavysleetshowersandthunder", 27);
        SYMBOL_CODE_MAPPING.put("lightssnowshowersandthunder", 28);
        SYMBOL_CODE_MAPPING.put("heavysnowshowersandthunder", 29);
        SYMBOL_CODE_MAPPING.put("lightrainandthunder", 30);
        SYMBOL_CODE_MAPPING.put("lightsleetandthunder", 31);
        SYMBOL_CODE_MAPPING.put("heavysleetandthunder", 32);
        SYMBOL_CODE_MAPPING.put("lightsnowandthunder", 33);
        SYMBOL_CODE_MAPPING.put("heavysnowandthunder", 34);
        SYMBOL_CODE_MAPPING.put("lightrainshowers", 40);
        SYMBOL_CODE_MAPPING.put("heavyrainshowers", 41);
        SYMBOL_CODE_MAPPING.put("lightsleetshowers", 42);
        SYMBOL_CODE_MAPPING.put("heavysleetshowers", 43);
        SYMBOL_CODE_MAPPING.put("lightsnowshowers", 44);
        SYMBOL_CODE_MAPPING.put("heavysnowshowers", 45);
        SYMBOL_CODE_MAPPING.put("lightrain", 46);
        SYMBOL_CODE_MAPPING.put("lightsleet", 47);
        SYMBOL_CODE_MAPPING.put("heavysleet", 48);
        SYMBOL_CODE_MAPPING.put("lightsnow", 49);
        SYMBOL_CODE_MAPPING.put("heavysnow", 50);
    }

    /* Thanks Chronus(app) */
    private static final int[] arrayWeatherIconToCode = {-1, /*1*/ 32, /*2*/ 34, /*3*/ 30, /*4*/ 26, /*5*/ 40, /*6*/ 39, /*7*/ 6, /*8*/ 14, /*9*/ 11, /*10*/ 12, /*11*/ 4, /*12*/ 18, /*13*/ 16, /*14*/ 15, /*15*/ 20, /*16*/ -1, /*17*/ -1, /*18*/ -1, /*19*/ -1, /*20*/ 42, /*21*/ 42, /*22*/ 4, /*23*/ 6, /*24*/ 39, /*25*/ 39, /*26*/ 42, /*27*/ 42, /*28*/ 42, /*29*/ 42, /*30*/ 4, /*31*/ 6, /*32*/ 6, /*33*/ 15, /*34*/ 15, /*35*/ -1, /*36*/ -1, /*37*/ -1, /*38*/ -1, /*39*/ -1, /*40*/ 40, /*41*/ 40, /*42*/ 6, /*43*/ 6, /*44*/ 14, /*45*/ 14, /*46*/ 9, /*47*/ 18, /*48*/ 18, /*49*/ 16, /*50*/ 16};

    private int getPriorityCondition(String condition) {
        int endIndex = condition.indexOf("_");
        if(endIndex != -1) {
            condition = condition.substring(0, endIndex);
        }
        return SYMBOL_CODE_MAPPING.getOrDefault(condition, 0);
    }

    private String getWeatherCondition(String condition) {
        int endIndex = condition.indexOf("_");
        if (endIndex != -1) {
            condition = condition.substring(0, endIndex);
        }
        return WEATHER_CONDITION_MAPPING.getOrDefault(condition, condition);
    }

    private void initTimeZoneFormat() {
        gmt0Format.setTimeZone(TimeZone.getTimeZone("GMT"));
        userTimeZoneFormat.setTimeZone(TimeZone.getDefault());
    }

    private String convertTimeZone(String tmp) {
        try {
            return userTimeZoneFormat.format(gmt0Format.parse(tmp));
        } catch (ParseException e) {
            return tmp;
        }
    }

    private Boolean isMorningOrAfternoon(String time, boolean hasOneHour) {
        int endI = hasOneHour ? 17 : 13;
        for (int i = 6; i <= endI; i++) {
            if(time.contains((i < 10) ? "T0":"T" + i)) {
                return true;
            }
        }
        return false;
    }

    private boolean isEndDay(String time) {
        for (int i = 18; i <= 23; i++) {
            if(time.contains("T" + i)) {
                return true;
            }
        }
        return false;
    }

    private static float convertTemperature(double value, boolean metric) {
        if (!metric) {
            value = (value * 1.8) + 32;
        }
        return (float) value;
    }

    private static float convertWindSpeed(double valueMs, boolean metric) {
        return (float) (valueMs * (metric ? 3.6 : 2.2369362920544));
    }

    public boolean shouldRetry() {
        return false;
    }
}
