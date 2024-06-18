package it.dhd.oxygencustomizer.weather;

/*
 *  Copyright (C) 2017 The OmniROM Project
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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

// we dont need an explicit wakelock JobScheduler takes care of that
public class WeatherUpdateService extends JobService {
    private static final String TAG = "WeatherUpdateService";
    private static final boolean DEBUG = false;
    private static final String ACTION_BROADCAST = "it.dhd.oxygencustomizer.WEATHER_UPDATE";
    private static final String ACTION_ERROR = "it.dhd.oxygencustomizer.WEATHER_ERROR";

    private static final String EXTRA_ERROR = "error";

    private static final int EXTRA_ERROR_LOCATION = 1;
    private static final int EXTRA_ERROR_DISABLED = 2;

    private static final float LOCATION_ACCURACY_THRESHOLD_METERS = 50000;
    private static final long OUTDATED_LOCATION_THRESHOLD_MILLIS = 10L * 60L * 1000L; // 10 minutes
    private static final int RETRY_DELAY_MS = 5000;
    private static final int RETRY_MAX_NUM = 5;

    public static final int PERIODIC_UPDATE_JOB_ID = 0;
    public static final int ONCE_UPDATE_JOB_ID = 1;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private static final SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US);

    private static final Criteria sLocationCriteria;
    static {
        sLocationCriteria = new Criteria();
        sLocationCriteria.setPowerRequirement(Criteria.POWER_LOW);
        sLocationCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        sLocationCriteria.setCostAllowed(false);
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (DEBUG) Log.d(TAG, "onStopJob " + params.getJobId());
        return true;
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        if (DEBUG) Log.d(TAG, "onStartJob " + params.getJobId());
        updateWeatherFromAlarm(params);
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (DEBUG) Log.d(TAG, "onCreate");
        mHandlerThread = new HandlerThread("WeatherService Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

    }

    private void updateWeatherFromAlarm(JobParameters params) {
        Config.setUpdateError(this, false);

        try {
            if (!Config.isEnabled(WeatherUpdateService.this)) {
                Log.w(TAG, "Service started, but not enabled ... stopping");
                Intent errorIntent = new Intent(ACTION_ERROR);
                errorIntent.putExtra(EXTRA_ERROR, EXTRA_ERROR_DISABLED);
                sendBroadcast(errorIntent);
                return;
            }

            Config.clearLastUpdateTime(WeatherUpdateService.this);

            if (DEBUG) Log.d(TAG, "call updateWeather from updateWeatherFromAlarm");
            updateWeather();
        } finally {
            jobFinished(params, false);
        }
    }

    private boolean doCheckLocationEnabled() {
        return Settings.Secure.getInt(getContentResolver(), Settings.Secure.LOCATION_MODE, -1) != Settings.Secure.LOCATION_MODE_OFF;
    }

    @SuppressLint("MissingPermission")
    private Location getCurrentLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!doCheckLocationEnabled()) {
            Log.w(TAG, "locations disabled");
            return null;
        }
        Location location = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
        if (DEBUG) Log.d(TAG, "Current location is " + location);

        if (location != null && location.getAccuracy() > LOCATION_ACCURACY_THRESHOLD_METERS) {
            Log.w(TAG, "Ignoring inaccurate location");
            location = null;
        }

        // If lastKnownLocation is not present (because none of the apps in the
        // device has requested the current location to the system yet) or outdated,
        // then try to get the current location use the provider that best matches the criteria.
        boolean needsUpdate = location == null;
        if (location != null) {
            long delta = System.currentTimeMillis() - location.getTime();
            needsUpdate = delta > OUTDATED_LOCATION_THRESHOLD_MILLIS;
            if (needsUpdate) {
                Log.w(TAG, "Ignoring too old location from " + dayFormat.format(location.getTime()));
                location = null;
            }
        }
        if (needsUpdate) {
            String locationProvider = lm.getBestProvider(sLocationCriteria, true);
            if (TextUtils.isEmpty(locationProvider)) {
                Log.e(TAG, "No available location providers matching criteria.");
            } else {
                if (DEBUG) Log.d(TAG, "Getting current location with provider " + locationProvider);
                lm.getCurrentLocation(locationProvider, null, getApplication().getMainExecutor(), location1 -> {
                    if (location1 != null) {
                        if (DEBUG) Log.d(TAG, "Got valid location now update");
                        WeatherUpdateService.scheduleUpdateNow(WeatherUpdateService.this);
                    } else {
                        Log.w(TAG, "Failed to retrieve location");
                        Intent errorIntent = new Intent(ACTION_ERROR);
                        errorIntent.putExtra(EXTRA_ERROR, EXTRA_ERROR_LOCATION);
                        sendBroadcast(errorIntent);
                        Config.setUpdateError(WeatherUpdateService.this, true);
                    }
                });
            }
        }

        return location;
    }

    public static void scheduleUpdatePeriodic(Context context) {
        cancelUpdatePeriodic(context);

        if (DEBUG) Log.d(TAG, "scheduleUpdatePeriodic");
        final long interval = TimeUnit.HOURS.toMillis(Config.getUpdateInterval(context));
        final long due = System.currentTimeMillis() + interval;

        if (DEBUG) Log.d(TAG, "Scheduling next update at " + new Date(due));

        ComponentName component = new ComponentName(context, WeatherUpdateService.class);
        JobInfo job = new JobInfo.Builder(PERIODIC_UPDATE_JOB_ID, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(interval)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(job);
    }

    public static void scheduleUpdateOnce(Context context, long timeoutMillis, int jobId) {
        if (DEBUG) Log.d(TAG, "scheduleUpdateOnce");

        ComponentName component = new ComponentName(context, WeatherUpdateService.class);
        JobInfo job = new JobInfo.Builder(jobId, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(timeoutMillis)
                .setOverrideDeadline(timeoutMillis)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(job);
    }

    public static void scheduleUpdateNow(Context context) {
        if (DEBUG) Log.d(TAG, "scheduleUpdateNow");

        ComponentName component = new ComponentName(context, WeatherUpdateService.class);
        JobInfo job = new JobInfo.Builder(ONCE_UPDATE_JOB_ID, component)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setMinimumLatency(1)
                .setOverrideDeadline(1)
                .build();
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.schedule(job);
    }

    private static void cancelUpdate(Context context) {
        if (DEBUG) Log.d(TAG, "cancelUpdate");
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancel(PERIODIC_UPDATE_JOB_ID);
    }

    public static void cancelUpdatePeriodic(Context context) {
        cancelUpdate(context);
    }

    public static void cancelAllUpdate(Context context) {
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        jobScheduler.cancelAll();
    }

    private void updateWeather() {
        mHandler.post(() -> {
            WeatherInfo w = null;
            try {
                AbstractWeatherProvider provider = Config.getProvider(WeatherUpdateService.this);
                int i = 0;
                // retry max 3 times
                while (i < RETRY_MAX_NUM) {
                    if (!Config.isCustomLocation(WeatherUpdateService.this)) {
                        if (checkPermissions()) {
                            Location location = getCurrentLocation();
                            if (location != null) {
                                w = provider.getLocationWeather(location, Config.isMetric(WeatherUpdateService.this));
                            } else {
                                Log.w(TAG, "no location yet");
                                // we are outa here
                                break;
                            }
                        } else {
                            Log.w(TAG, "no location permissions");
                            // we are outa here
                            break;
                        }
                    } else if (Config.getLocationId(WeatherUpdateService.this) != null) {
                        w = provider.getCustomWeather(Config.getLocationId(WeatherUpdateService.this), Config.isMetric(WeatherUpdateService.this));
                    } else {
                        Log.w(TAG, "no valid custom location");
                        // we are outa here
                        break;
                    }
                    if (w != null) {
                        Config.setWeatherData(w, WeatherUpdateService.this);
                        WeatherContentProvider.updateCachedWeatherInfo(WeatherUpdateService.this);
                        Log.d(TAG, "Weather updated updateCachedWeatherInfo");
                        //WeatherAppWidgetProvider.updateAllWidgets(WeatherUpdateService.this);
                        // we are outa here
                        break;
                    } else {
                        if (!provider.shouldRetry()) {
                            // some other error
                            break;
                        } else {
                            Log.w(TAG, "retry count =" + i);
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
                    // error
                    if (DEBUG) Log.d(TAG, "error updating weather");
                    Config.setUpdateError(WeatherUpdateService.this, true);
                }
                // send broadcast that something has changed
                Intent updateIntent = new Intent(ACTION_BROADCAST);
                sendBroadcast(updateIntent);
            }
        });
    }

    private boolean checkPermissions() {
        return checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
}
