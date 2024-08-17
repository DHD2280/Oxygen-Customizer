package it.dhd.oxygencustomizer.utils;

/*
 *  Copyright (C) 2018 The OmniROM Project
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

import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;

import it.dhd.oxygencustomizer.BuildConfig;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {
    private static final boolean DEBUG = false;
    private static final String TAG = "NetworkUtils";


    public static void asynchronousGetRequest(String url, DownloadCallback callback) {
        if (DEBUG) Log.d(TAG, "download: " + url);
        OkHttpClient client = new OkHttpClient();


        Request apiRequest = new Request.Builder()
                .url(url)
                .build();

        Request metRequest = new Request.Builder()
                .url(url)
                .header("User-Agent", "OxygenCustomizer/" + BuildConfig.VERSION_NAME + "-" + BuildConfig.VERSION_CODE)
                .build();

        Request finalRequest;
        if (url.contains("api.met.no")) {
            finalRequest = metRequest;
        } else {
            finalRequest = apiRequest;
        }

        client.newCall(finalRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Handle failure
                if (callback != null) {
                    callback.onDownloadComplete("");
                }
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                // Handle success
                String result = response.body() != null ? response.body().string() : "";
                // Process the response data
                if (callback != null) {
                    callback.onDownloadComplete(result);
                }
            }
        });
    }

    public interface DownloadCallback {
        void onDownloadComplete(String result);
    }

}
