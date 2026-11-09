package it.dhd.oxygencustomizer.utils;

import android.content.Context;
import android.util.Log;

import androidx.work.BackoffPolicy;
import androidx.work.Configuration;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;
import androidx.work.WorkQuery;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.weather.WeatherConfig;
import it.dhd.oxygencustomizer.weather.WeatherWork;

public class WeatherScheduler {
    private static final String UPDATE_WORK_NAME = BuildConfig.APPLICATION_ID + ".WeatherSchedule";
    private static final String UPDATE_NOW_WORK_NAME = BuildConfig.APPLICATION_ID + ".WeatherUpdateNow";
    private static final String UPDATE_NOW_TAG = BuildConfig.APPLICATION_ID + ".WeatherUpdateNowTag";

    public static void scheduleUpdates(Context context) {
        Log.d("WeatherScheduler", "Updating update schedule...");

        if (!WorkManager.isInitialized()) {
            WorkManager.initialize(context, new Configuration.Builder().build());
        }

        WorkManager workManager = WorkManager.getInstance(context);
        cancelStaleWork(workManager);

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
        cancelStaleWork(workManager);

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(WeatherWork.class)
                .setInputData(inputData)
                .addTag(UPDATE_NOW_TAG)
                .build();
        workManager.enqueueUniqueWork(
                UPDATE_NOW_WORK_NAME,
                force ? ExistingWorkPolicy.REPLACE : ExistingWorkPolicy.KEEP,
                request);
    }

    private static void cancelStaleWork(WorkManager workManager) {
        WorkQuery query = WorkQuery.Builder
                .fromTags(List.of(WeatherWork.class.getName()))
                .addStates(List.of(WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED))
                .build();
        ListenableFuture<List<WorkInfo>> future = workManager.getWorkInfos(query);
        future.addListener(() -> {
            try {
                for (WorkInfo info : future.get()) {
                    if (info.getPeriodicityInfo() == null && !info.getTags().contains(UPDATE_NOW_TAG)) {
                        workManager.cancelWorkById(info.getId());
                    }
                }
            } catch (ExecutionException | InterruptedException e) {
                Log.e("WeatherScheduler", "cancelStaleWork", e);
            }
        }, Runnable::run);
    }

}
