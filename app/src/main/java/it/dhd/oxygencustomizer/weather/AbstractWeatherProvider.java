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

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.NetworkUtils;

public abstract class AbstractWeatherProvider {
    private static final String TAG = "AbstractWeatherProvider";
    private static final boolean DEBUG = false;
    protected Context mContext;
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final String URL_PLACES =
            "https://secure.geonames.org/searchJSON?name_startsWith=%s&lang=%s&username=omnijaws&maxRows=20";
    private static final String URL_LOCALITY =
            "https://secure.geonames.org/extendedFindNearbyJSON?lat=%f&lng=%f&lang=%s&username=omnijaws";
    public static final String PART_COORDINATES =
            "lat=%f&lon=%f";
    private static String response = "";

    public AbstractWeatherProvider(Context context) {
        mContext = context;
    }

    protected String retrieve(String url, String[] header) {
        response = "";
        CountDownLatch latch = new CountDownLatch(1);

        NetworkUtils.asynchronousGetRequest(url, header, result -> {
            if (!TextUtils.isEmpty(result)) {
                Log.d(TAG, "Download success " + result);
                response = result;
            } else {
                response = "";
                Log.e(TAG, "Download " + url + " failed");
            }
            latch.countDown();
        });

        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                Log.d(TAG, "Timeout while waiting for network response");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupt status
            Log.e(TAG, "retrieve interrupted", e);
        }

        return response;
    }

    public abstract WeatherInfo getCustomWeather(String lat, String lon, boolean metric);

    public abstract WeatherInfo getLocationWeather(Location location, boolean metric);

    public abstract boolean shouldRetry();

    protected void log(String tag, String msg) {
        if (DEBUG) Log.d("WeatherService:" + tag, msg);
    }

    private String getCoordinatesLocalityWithGoogle(String coordinate) {
        double latitude = Double.valueOf(coordinate.substring(coordinate.indexOf("=") + 1, coordinate.indexOf("&")));
        double longitude = Double.valueOf(coordinate.substring(coordinate.lastIndexOf("=") + 1));

        Geocoder geocoder = new Geocoder(mContext.getApplicationContext(), Locale.getDefault());
        try {
            List<Address> listAddresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (listAddresses != null && !listAddresses.isEmpty()) {
                Address a = listAddresses.get(0);
                return TextUtils.isEmpty(a.getLocality()) ? a.getAdminArea() : a.getLocality();
            }
        } catch (IOException e) {
            log(TAG, e.getMessage());
        }
        return null;
    }

    protected String getCoordinatesLocality(String coordinate) {
        String cityGoogle = getCoordinatesLocalityWithGoogle(coordinate);
        if (!TextUtils.isEmpty(cityGoogle)) {
            return cityGoogle;
        }
        double latitude = Double.valueOf(coordinate.substring(coordinate.indexOf("=") + 1, coordinate.indexOf("&")));
        double longitude = Double.valueOf(coordinate.substring(coordinate.lastIndexOf("=") + 1));

        String lang = Locale.getDefault().getLanguage().replaceFirst("_", "-");
        String url = String.format(URL_LOCALITY, latitude, longitude, lang);
        String response = retrieve(url, null);
        if (response == null) {
            return null;
        }
        log(TAG, "URL = " + url + " returning a response of " + response);

        try {
            JSONObject jsonResults = new JSONObject(response);
            if (jsonResults.has("address")) {
                JSONObject address = jsonResults.getJSONObject("address");
                String city = address.getString("placename");
                String area = address.getString("adminName2");
                if (!TextUtils.isEmpty(city)) {
                    return city;
                }
                if (!TextUtils.isEmpty(area)) {
                    return area;
                }
            } else if (jsonResults.has("geonames")) {
                JSONArray jsonResultsArray = jsonResults.getJSONArray("geonames");
                int count = jsonResultsArray.length();

                for (int i = count - 1; i >= 0; i--) {
                    JSONObject geoname = jsonResultsArray.getJSONObject(i);
                    String fcode = geoname.getString("fcode");
                    String name = geoname.getString("name");
                    if (TextUtils.isEmpty(name)) {
                        continue;
                    }
                    if (fcode.equals("ADM3")) {
                        return name;
                    }
                    if (fcode.equals("ADM2")) {
                        return name;
                    }
                    if (fcode.equals("ADM1")) {
                        return name;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Received malformed location data (coordinate=" + coordinate + ")", e);
        }
        return null;
    }

    protected String getWeatherDataLocality(String coordinates) {
        String city;
        if (WeatherConfig.isCustomLocation(mContext)) {
            city = WeatherConfig.getLocationName(mContext);
            if (TextUtils.isEmpty(city)) {
                city = getCoordinatesLocality(coordinates);
            }
        } else {
            city = getCoordinatesLocality(coordinates);
        }
        if (TextUtils.isEmpty(city)) {
            city = mContext.getResources().getString(R.string.omnijaws_city_unknown);
        }
        log(TAG, "getWeatherDataLocality = " + city);
        return city;
    }

    protected String getDay(int i) {
        Calendar calendar = Calendar.getInstance();
        if (i > 0) {
            calendar.add(Calendar.DATE, i);
        }
        return dayFormat.format(calendar.getTime());
    }
}