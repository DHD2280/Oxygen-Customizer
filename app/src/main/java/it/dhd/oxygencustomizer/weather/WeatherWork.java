package it.dhd.oxygencustomizer.weather;

import static it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient.EXTRA_ERROR_DISABLED;
import static it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient.EXTRA_ERROR_LOCATION;
import static it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient.EXTRA_ERROR_NETWORK;
import static it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class WeatherWork extends ListenableWorker {
    final Context mContext;
    private static final String TAG = "WeatherWork";
    private static final boolean DEBUG = false;
    private static final String ACTION_BROADCAST = "it.dhd.oxygencustomizer.WEATHER_UPDATE";
    private static final String ACTION_ERROR = "it.dhd.oxygencustomizer.WEATHER_ERROR";

    private static final String EXTRA_ERROR = "error";

    private static final float LOCATION_ACCURACY_THRESHOLD_METERS = 10000;
    private static final long OUTDATED_LOCATION_THRESHOLD_MILLIS = 10L * 60L * 1000L; // 10 minutes
    private static final int RETRY_DELAY_MS = 5000;
    private static final int RETRY_MAX_NUM = 5;

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private static final Criteria sLocationCriteria;
    static {
        sLocationCriteria = new Criteria();
        sLocationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        sLocationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        sLocationCriteria.setCostAllowed(false);
    }

    public WeatherWork(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mContext = appContext;
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        if (DEBUG) Log.d(TAG, "startWork");

        return CallbackToFutureAdapter.getFuture(completer -> {
            if (!Config.isEnabled(mContext)) {
                handleError(completer, EXTRA_ERROR_DISABLED, "Service started, but not enabled ... stopping");
                return completer;
            }

            if (!checkPermissions()) {
                handleError(completer, EXTRA_ERROR_NO_PERMISSIONS, "Location permissions are not granted");
                return completer;
            }

            if (!doCheckLocationEnabled()) {
                handleError(completer, EXTRA_ERROR_NETWORK, "Location services are disabled");
                return completer;
            }

            executor.execute(() -> {
                Location location = getCurrentLocation();
                if (location != null) {
                    Log.d(TAG, "Location retrieved");
                    updateWeather(location, completer);
                } else if (Config.isCustomLocation(mContext)) {
                    Log.d(TAG, "Using custom location configuration");
                    updateWeather(null, completer);
                } else {
                    handleError(completer, EXTRA_ERROR_LOCATION, "Failed to retrieve location");
                }
            });

            return completer;
        });
    }

    private void handleError(CallbackToFutureAdapter.Completer<Result> completer, int errorExtra, String logMessage) {
        Log.w(TAG, logMessage);
        Intent errorIntent = new Intent(ACTION_ERROR);
        errorIntent.putExtra(EXTRA_ERROR, errorExtra);
        mContext.sendBroadcast(errorIntent);
        completer.set(Result.retry());
    }

    private boolean doCheckLocationEnabled() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        boolean networkEnabled = false;

        try {
            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            Log.d(TAG, "doCheckLocationEnabled: " + ex.getMessage());
        }

        try {
            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            Log.d(TAG, "doCheckLocationEnabled: " + ex.getMessage());
        }

        if (DEBUG) Log.d(TAG, "gpsEnabled: " + gpsEnabled + " networkEnabled: " + networkEnabled);

        return gpsEnabled || networkEnabled;
    }

    @SuppressLint("MissingPermission")
    private Location getCurrentLocation() {
        LocationManager lm = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

        if (!doCheckLocationEnabled()) {
            Log.w(TAG, "locations disabled");
            return null;
        }

        AtomicReference<Location> location = new AtomicReference<>(lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER));
        Log.d(TAG, "Current location is " + location);

        if (location.get() != null && location.get().getAccuracy() > LOCATION_ACCURACY_THRESHOLD_METERS) {
            Log.w(TAG, "Ignoring inaccurate location");
            location.set(null);
        }

        boolean needsUpdate = location.get() == null;
        if (location.get() != null) {
            long delta = System.currentTimeMillis() - location.get().getTime();
            needsUpdate = delta > OUTDATED_LOCATION_THRESHOLD_MILLIS;
            Log.d(TAG, "Location is " + delta + "ms old");
            if (needsUpdate) {
                Log.w(TAG, "Ignoring too old location from " + dayFormat.format(location.get().getTime()));
                location.set(null);
            }
        }

        if (needsUpdate) {
            Log.d(TAG, "Requesting current location");
            String locationProvider = lm.getBestProvider(sLocationCriteria, true);
            if (TextUtils.isEmpty(locationProvider)) {
                Log.e(TAG, "No available location providers matching criteria.");
            } else {
                Log.d(TAG, "Getting current location with provider " + locationProvider);
                lm.getCurrentLocation(locationProvider, null, mContext.getMainExecutor(), location1 -> {
                    if (location1 != null) {
                        Log.d(TAG, "Got valid location now update");
                        location.set(location1);
                    } else {
                        Log.w(TAG, "Failed to retrieve location");
                    }
                });
            }
        }

        return location.get();
    }

    private void updateWeather(Location location, CallbackToFutureAdapter.Completer<Result> completer) {
        WeatherInfo w = null;
        try {
            AbstractWeatherProvider provider = Config.getProvider(mContext);
            boolean isMetric = Config.isMetric(mContext);
            int i = 0;
            while (i < RETRY_MAX_NUM) {
                if (location != null && !Config.isCustomLocation(mContext)) {
                    w = provider.getLocationWeather(location, isMetric);
                } else if (Config.getLocationId(mContext) != null) {
                    w = provider.getCustomWeather(Config.getLocationId(mContext), isMetric);
                } else {
                    Log.w(TAG, "No valid custom location and location is null");
                    break;
                }

                if (w != null) {
                    Config.setWeatherData(w, mContext);
                    WeatherContentProvider.updateCachedWeatherInfo(mContext);
                    Log.d(TAG, "Weather updated updateCachedWeatherInfo");
                    completer.set(Result.success());
                    return;
                } else {
                    if (!provider.shouldRetry()) {
                        break;
                    } else {
                        Log.w(TAG, "retry count = " + i);
                        try {
                            Thread.sleep(RETRY_DELAY_MS);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                i++;
            }
        } finally {
            if (w == null) {
                Log.d(TAG, "error updating weather");
                Config.setUpdateError(mContext, true);
                completer.set(Result.retry());
            }
            Intent updateIntent = new Intent(ACTION_BROADCAST);
            mContext.sendBroadcast(updateIntent);
        }
    }

    private boolean checkPermissions() {
        return mContext.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                mContext.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

}

