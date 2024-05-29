package it.dhd.oxygencustomizer.utils;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Configuration;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.BuildConfig;

public class UpdateScheduler {
    private static final String UPDATE_WORK_NAME = BuildConfig.APPLICATION_ID + ".UpdateSchedule";

    public static void scheduleUpdates(Context context)
    {
        if(BuildConfig.DEBUG)
            Log.d("Update Scheduler", "Updating update schedule...");

        if(!WorkManager.isInitialized())
        {
            WorkManager.initialize(context, new Configuration.Builder().build());
        }

        WorkManager workManager = WorkManager.getInstance(context);

        SharedPreferences prefs = getDefaultSharedPreferences(context.createDeviceProtectedStorageContext());

        boolean autoUpdate = prefs.getBoolean("autoUpdate", true);

        if(autoUpdate)
        {
            PeriodicWorkRequest.Builder builder = new PeriodicWorkRequest.Builder(UpdateWorker.class, 12, TimeUnit.HOURS)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.HOURS);

            workManager.enqueueUniquePeriodicWork(
                    UPDATE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    builder.build());
        }
        else
        {
            workManager.cancelUniqueWork(UPDATE_WORK_NAME);
        }
    }

    public static void scheduleUpdateNow(Context context)
    {
        if(BuildConfig.DEBUG)
            Log.d("Update Scheduler", "Check update now");

        if(!WorkManager.isInitialized())
        {
            WorkManager.initialize(context, new Configuration.Builder().build());
        }

        WorkManager workManager = WorkManager.getInstance(context);

        OneTimeWorkRequest.Builder builder = new OneTimeWorkRequest.Builder(UpdateWorker.class);

        workManager.enqueue(builder.build());
    }
}
