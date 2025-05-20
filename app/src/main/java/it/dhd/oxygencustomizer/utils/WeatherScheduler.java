package it.dhd.oxygencustomizer.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Configuration;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.weather.WeatherConfig;
import it.dhd.oxygencustomizer.weather.WeatherWork;

public class WeatherScheduler {
    private static final String UPDATE_WORK_NAME = BuildConfig.APPLICATION_ID + ".WeatherSchedule";

    public static void scheduleUpdates(Context context) {
        Log.d("WeatherScheduler", "Updating update schedule...");

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, new Configuration.Builder().build());
        }

        WorkManager workManager = WorkManager.getInstance(context);

        boolean weatherEnabled = WeatherConfig.isEnabled(context);

        Log.d("WeatherScheduler", "Weather enabled: " + weatherEnabled);

        if (weatherEnabled) {
            Log.d("WeatherScheduler", "Scheduling updates");
            PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(WeatherWork.class, WeatherConfig.getUpdateInterval(context), TimeUnit.HOURS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS);

            workManager.enqueueUniquePeriodicWork(
                    UPDATE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    builder.build());
        } else {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME);
        }
    }

    public static void unscheduleUpdates(Context context) {

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, new Configuration.Builder().build());
        }

        WorkManager workManager = WorkManager.getInstance(context);

        workManager.cancelUniqueWork(UPDATE_WORK_NAME);
    }

    public static void scheduleUpdateNow(Context context) {
        Log.d("WeatherScheduler", "Check update now");

        scheduleUpdateNow(context, false);
    }

    public static void scheduleUpdateNow(Context context, boolean force) {
        Log.d("WeatherScheduler", "Check update now - force: " + force);

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, new Configuration.Builder().build());
        }
        Data inputData = new Data.Builder()
                .putBoolean("force_update", force)
                .build();
        WorkManager workManager = WorkManager.getInstance(context);

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(WeatherWork.class);
        workManager.enqueue(builder.setInputData(inputData).build());
    }

}
