package it.dhd.oxygencustomizer.provider;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.BuildConfig;

public class StepsProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".stepsprovider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/steps");
    public static final String COLUMN_STEPS = "steps";
    public static final int NO_SENSOR = -1;
    public static final int NO_PERMISSION = -2;

    private static final String PREFS = "steps_counter";
    private static final String KEY_DAY = "day";
    private static final String KEY_BASE = "base";
    private static final String KEY_OFFSET = "offset";
    private static final String KEY_LAST = "last";
    private static final long SENSOR_TIMEOUT_MS = 2000;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String caller = getCallingPackage();
        if (!SYSTEM_UI.equals(caller) && !BuildConfig.APPLICATION_ID.equals(caller)) return null;
        MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_STEPS});
        cursor.addRow(new Object[]{todaySteps()});
        return cursor;
    }

    private synchronized long todaySteps() {
        Context context = getContext();
        if (context == null) return NO_SENSOR;
        if (context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            return NO_PERMISSION;
        }
        SharedPreferences prefs = context.createDeviceProtectedStorageContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        float raw = readCounter(context);
        if (raw == NO_SENSOR) return NO_SENSOR;
        String today = new SimpleDateFormat("yyyyMMdd", Locale.ROOT).format(new Date());
        long counter = raw < 0 ? prefs.getLong(KEY_LAST, 0) : (long) raw;
        long base = prefs.getLong(KEY_BASE, counter);
        long offset = prefs.getLong(KEY_OFFSET, 0);
        long last = prefs.getLong(KEY_LAST, counter);
        if (!today.equals(prefs.getString(KEY_DAY, null))) {
            base = counter;
            offset = 0;
        } else if (counter < last) {
            offset += Math.max(0, last - base);
            base = 0;
        }
        prefs.edit()
                .putString(KEY_DAY, today)
                .putLong(KEY_BASE, base)
                .putLong(KEY_OFFSET, offset)
                .putLong(KEY_LAST, counter)
                .apply();
        return Math.max(0, offset + counter - base);
    }

    private static float readCounter(Context context) {
        SensorManager manager = context.getSystemService(SensorManager.class);
        Sensor sensor = manager == null ? null : manager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) return NO_SENSOR;
        HandlerThread thread = new HandlerThread("StepsProvider");
        thread.start();
        CountDownLatch latch = new CountDownLatch(1);
        float[] value = {Float.NaN};
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                value[0] = event.values[0];
                latch.countDown();
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        try {
            manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL, new Handler(thread.getLooper()));
            latch.await(SENSOR_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException ignored) {
            Thread.currentThread().interrupt();
        } finally {
            manager.unregisterListener(listener);
            thread.quitSafely();
        }
        return Float.isNaN(value[0]) ? -3 : value[0];
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.item/vnd." + AUTHORITY + ".steps";
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
