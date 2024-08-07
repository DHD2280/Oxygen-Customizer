package it.dhd.oxygencustomizer.weather;

/*
 * Copyright (C) 2012 The AOKP Project
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherInfo {
    private static final DecimalFormat sNoDigitsFormat = new DecimalFormat("0");

    private Context mContext;

    private String id;
    private String city;
    private String condition;
    private String pinWheel;
    private int conditionCode;
    public float temperature;
    private float humidity;
    private float wind;
    private int windDirection;
    private long timestamp;
    private ArrayList<DayForecast> forecasts;
    private boolean metric;

    private WeatherInfo(Context context, String id,
                        String city, String condition, int conditionCode, float temp,
                        float humidity, float wind, int windDir,
                        boolean metric, ArrayList<DayForecast> forecasts, long timestamp,
                        String pinWheel) {
        this.mContext = context.getApplicationContext();
        this.id = id;
        this.city = city;
        this.condition = condition;
        this.conditionCode = conditionCode;
        this.humidity = humidity;
        this.wind = wind;
        this.windDirection = windDir;
        this.timestamp = timestamp;
        this.temperature = temp;
        this.forecasts = forecasts;
        this.metric = metric;
        this.pinWheel = pinWheel;
    }

    public WeatherInfo(Context context, String id,
                       String city, String condition, int conditionCode, float temp,
                       float humidity, float wind, int windDir,
                       boolean metric, ArrayList<DayForecast> forecasts, long timestamp) {
        this(context, id, city, condition, conditionCode, temp, humidity, wind, windDir,
                metric, forecasts, timestamp, "");
        this.pinWheel = getFormattedWindDirection(windDir);
    }

    public static class WeatherLocation {
        public String id;
        public String city;
        public String postal;
        public String countryId;
        public String country;
    }

    public static class DayForecast {
        public final float low, high;
        public final int conditionCode;
        public final String condition;
        public boolean metric;
        public String date;

        public DayForecast(float low, float high, String condition, int conditionCode, String date, boolean metric) {
            this.low = low;
            this.high = high;
            this.condition = condition;
            this.conditionCode = conditionCode;
            this.metric = metric;
            this.date = date;
        }

        public float getLow() {
            return low;
        }

        public float getHigh() {
            return high;
        }

        public String getCondition(Context context) {
            return WeatherInfo.getCondition(context, conditionCode, condition);
        }

        public int getConditionCode() {
            return conditionCode;
        }
    }

    public static final String[] WIND_DIRECTION = new String[]{
            "N",
            "NNE",
            "NE",
            "ENE",
            "E",
            "ESE",
            "SE",
            "SSE",
            "S",
            "SSW",
            "SW",
            "WSW",
            "W",
            "WNW",
            "NW",
            "NNW"
    };

    public String getId() {
        return id;
    }

    public String getCity() {
        return city;
    }

    public String getCondition() {
        return getCondition(mContext, conditionCode, condition);
    }

    public int getConditionCode() {
        return conditionCode;
    }

    private static String getCondition(Context context, int conditionCode, String condition) {
        final Resources res = context.getResources();
        final int resId = res.getIdentifier("weather_" + conditionCode, "string", context.getPackageName());
        if (resId != 0) {
            return res.getString(resId);
        }
        return condition;
    }

    public Long getTimestamp() {
        return new Long(timestamp);
    }

    public Date getFormattedTimestamp() {
        return new Date(timestamp);
    }

    private static String getFormattedValue(float value, String unit) {
        if (Float.isNaN(value)) {
            return "-";
        }
        String formatted = sNoDigitsFormat.format(value);
        if (formatted.equals("-0")) {
            formatted = "0";
        }
        return formatted + unit;
    }

    public String getFormattedHumidity() {
        return getFormattedValue(humidity, "%");
    }

    public float getWindSpeed() {
        if (wind < 0) {
            return 0;
        }
        return wind;
    }

    private String getFormattedWindSpeed() {
        if (wind < 0) {
            return "0";
        }
        return getFormattedValue(wind, metric?"km/h":"m/h");
    }

    public int getWindDirection() {
        return windDirection;
    }

    private String getFormattedWindDirection(int direction) {
        int value = (int) ((direction/22.5)+0.5);
        String pw = WIND_DIRECTION[(value % 16)];
        return pw;
    }

    public String getPinWheel() {
        return pinWheel;
    }

    public ArrayList<DayForecast> getForecasts() {
        return forecasts;
    }

    public float getTemperature() {
        return temperature;
    }

    private String getTemperatureUnit() {
        return "\u00b0" + (metric ? "C" : "F");
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("WeatherInfo for ");
        builder.append(city);
        builder.append(" (");
        builder.append(id);
        builder.append(") @ ");
        builder.append(getFormattedTimestamp());
        builder.append(": ");
        builder.append(getCondition());
        builder.append("(");
        builder.append(conditionCode);
        builder.append("), temperature ");
        builder.append(getFormattedValue(getTemperature(), getTemperatureUnit()));
        builder.append(", humidity ");
        builder.append(getFormattedHumidity());
        builder.append(", wind ");
        builder.append(getFormattedWindSpeed());
        builder.append(" at ");
        builder.append(getWindDirection());
        if (!forecasts.isEmpty()) {
            builder.append(", forecasts:");
        }
        for (int i = 0; i < forecasts.size(); i++) {
            DayForecast d = forecasts.get(i);
            if (i != 0) {
                builder.append(";");
            }
            builder.append(" day ").append(i + 1).append(":");
            builder.append(d.date);
            builder.append(" high ").append(getFormattedValue(d.getHigh(), getTemperatureUnit()));
            builder.append(", low ").append(getFormattedValue(d.getLow(), getTemperatureUnit()));
            builder.append(", ").append(d.condition);
            builder.append("(").append(d.conditionCode).append(")");
        }
        return builder.toString();
    }

    public String toSerializedString() {
        StringBuilder builder = new StringBuilder();
        builder.append(id).append('|');
        builder.append(city).append('|');
        builder.append(condition).append('|');
        builder.append(conditionCode).append('|');
        builder.append(temperature).append('|');
        builder.append(humidity).append('|');
        builder.append(wind).append('|');
        builder.append(windDirection).append('|');
        builder.append(metric).append('|');
        builder.append(timestamp).append('|');
        builder.append(pinWheel);
        if (!forecasts.isEmpty()) {
            serializeForecasts(builder);
        }
        return builder.toString();
    }

    private void serializeForecasts(StringBuilder builder) {
        builder.append('|');
        builder.append(forecasts.size());
        if (forecasts.isEmpty()) {
            return;
        }
        for (DayForecast d : forecasts) {
            builder.append(';');
            builder.append(d.high).append(';');
            builder.append(d.low).append(';');
            builder.append(d.condition).append(';');
            builder.append(d.conditionCode).append(';');
            builder.append(d.date);
        }
    }

    public static WeatherInfo fromSerializedString(Context context, String input) {
        if (input == null) {
            return null;
        }

        String[] parts = input.split("\\|");
        if (parts == null || parts.length != 11) {
            return null;
        }

        int conditionCode, windDirection;
        long timestamp;
        float temperature, humidity, wind;
        boolean metric;
        String pinWheel;
        String[] forecastParts = parts[10].split(";");
        int forecastItems;
        ArrayList<DayForecast> forecasts = new ArrayList<DayForecast>();

        // Parse the core data
        try {
            conditionCode = Integer.parseInt(parts[3]);
            temperature = Float.parseFloat(parts[4]);
            humidity = Float.parseFloat(parts[5]);
            wind = Float.parseFloat(parts[6]);
            windDirection = Integer.parseInt(parts[7]);
            metric = Boolean.parseBoolean(parts[8]);
            timestamp = Long.parseLong(parts[9]);
            pinWheel = parts[10];
//            forecastItems = forecastParts == null ? 0 : Integer.parseInt(forecastParts[0]);
        } catch (NumberFormatException e) {
            android.util.Log.e("Weather", "Error parsing weather data", e);
            return null;
        }

//        if (forecastItems == 0 || forecastParts.length != 5 * forecastItems + 1) {
//            return null;
//        }

        // Parse the forecast data
//        try {
//            for (int item = 0; item < forecastItems; item ++) {
//                int offset = item * 5 + 1;
//                DayForecast day = new DayForecast(
//                        /* low */ Float.parseFloat(forecastParts[offset + 1]),
//                        /* high */ Float.parseFloat(forecastParts[offset]),
//                        /* condition */ forecastParts[offset + 2],
//                        /* conditionCode */ Integer.parseInt(forecastParts[offset + 3]),
//                        forecastParts[offset + 4],
//                        metric);
//                if (!Float.isNaN(day.low) && !Float.isNaN(day.high) /*&& day.conditionCode >= 0*/) {
//                    forecasts.add(day);
//                }
//            }
//        } catch (NumberFormatException ignored) {
//        }
//
//        if (forecasts.isEmpty()) {
//            return null;
//        }

        return new WeatherInfo(context,
                /* id */ parts[0], /* city */ parts[1], /* condition */ parts[2],
                conditionCode, temperature,
                humidity, wind, windDirection, metric,
                /* forecasts */ forecasts, timestamp, pinWheel);
    }
}
