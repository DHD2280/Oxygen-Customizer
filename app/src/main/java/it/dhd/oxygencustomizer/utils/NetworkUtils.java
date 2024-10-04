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
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NetworkUtils {

    private static final boolean DEBUG = false;
    private static final String TAG = "NetworkUtils";

    public static void asynchronousGetRequest(String url, String[] header, DownloadCallback callback) {

        if (DEBUG) Log.d(TAG, "download: " + url);

        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .build();

        Request apiHeaderRequest = null;
        boolean hasHeader = header != null && header.length == 2;
        
        if (hasHeader) {
            apiHeaderRequest = new Request.Builder()
                    .url(url)
                    .header(header[0], header[1])
                    .build();
        }
        
        Request apiRequest = new Request.Builder()
                .url(url)
                .build();

        client.newCall(hasHeader ? apiHeaderRequest : apiRequest).enqueue(new Callback() {
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
                if (!response.isSuccessful()) {
                    if (callback != null) {
                        callback.onDownloadComplete("");
                    }
                }
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
