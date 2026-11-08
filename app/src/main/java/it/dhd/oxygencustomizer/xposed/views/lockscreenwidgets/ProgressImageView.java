/*
 * Copyright (C) 2023-2024 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.view.View;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

@SuppressLint({"AppCompatCustomView", "ViewConstructor"})
public class ProgressImageView extends ImageView {

    private final Context mContext;
    private Context appContext;
    private ProgressType progressType;
    private int progressPercent = -1;
    private int batteryLevel = -1;
    private int batteryTemperature = -1;
    private ScheduledExecutorService scheduler;
    private ScheduledFuture<?> updateTask;
    private boolean receiverRegistered = false;
    private String typeface = null;
    private int mProgressColor = Color.RED, mTextColor = Color.WHITE;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
                if (!receiverRegistered) {
                    receiverRegistered = true;
                }
                batteryLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                batteryLevel = Math.max(0, Math.min(batteryLevel, 100));
                batteryTemperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
                updateProgress();
            }
        }
    };

    public enum ProgressType {
        BATTERY(R.drawable.ic_battery),
        MEMORY(R.drawable.ic_memory),
        TEMPERATURE(R.drawable.ic_temperature),
        VOLUME(R.drawable.ic_volume_eq),
        UNKNOWN(-1);

        public final int iconRes;

        ProgressType(int iconRes) {
            this.iconRes = iconRes;
        }
    }

    public ProgressImageView(Context context) {
        super(context);
        mContext = context;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        progressType = ProgressType.UNKNOWN;
        mProgressColor = getPrimaryColor(mContext);
    }

    public void setProgressType(ProgressType progressType) {
        this.progressType = progressType;
        updateProgress();
    }

    /**
     * Set colors of progress and text
     * @param progressColor progress color
     * @param textColor text color
     */
    public void setColors(int progressColor, int textColor) {
        mProgressColor = progressColor;
        mTextColor = textColor;
        updateImageView();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!receiverRegistered) {
            if (progressType == ProgressType.BATTERY || progressType == ProgressType.TEMPERATURE) {
                IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                mContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED);
            }
        }
        startProgressUpdates();
        updateVisibility();
        updateProgress();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (receiverRegistered) {
            mContext.unregisterReceiver(batteryReceiver);
            receiverRegistered = false;
        }
        stopProgressUpdates();
    }

    private void startProgressUpdates() {
        if (progressType == ProgressType.MEMORY || progressType == ProgressType.VOLUME) {
            scheduler = Executors.newSingleThreadScheduledExecutor();
            updateTask = scheduler.scheduleWithFixedDelay(this::updateProgress, 0, 1, TimeUnit.SECONDS);
        }
    }

    private void stopProgressUpdates() {
        if (updateTask != null) {
            updateTask.cancel(true);
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void updateProgress() {
        int newProgressPercent = switch (progressType) {
            case BATTERY -> batteryLevel;
            case MEMORY -> getMemoryLevel();
            case TEMPERATURE -> batteryTemperature;
            case VOLUME -> getVolumeLevel();
            default -> -1;
        };
        if (newProgressPercent != progressPercent) {
            progressPercent = newProgressPercent;
            updateImageView();
        }
    }

    private void updateImageView() {
        if (progressType == ProgressType.UNKNOWN) return;
        String degree = "\u2103";
        String progressText;
        if (progressType == ProgressType.TEMPERATURE) {
            if (progressPercent != -1) {
                progressText = progressPercent + degree;
            } else {
                progressText = "N/A";
            }
        } else {
            if (progressPercent == -1) {
                progressText = "...";
            } else {
                progressText = progressPercent + "%";
            }
        }
        Drawable icon =
                ResourcesCompat.getDrawable(appContext.getResources(), progressType.iconRes, appContext.getTheme());
        Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                mContext,
                progressPercent == -1 ? 0 : progressPercent,
                progressText,
                40,
                icon,
                36,
                typeface,
                mProgressColor,
                mTextColor
        );
        post(() -> setImageBitmap(widgetBitmap));
    }

    private int getMemoryLevel() {
        ActivityManager activityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        long usedMemory = memoryInfo.totalMem - memoryInfo.availMem;
        int usedMemoryPercentage = (int) ((usedMemory * 100) / memoryInfo.totalMem);
        return Math.max(0, Math.min(usedMemoryPercentage, 100));
    }

    private int getVolumeLevel() {
        AudioManager audioManager;
        if (Objects.equals(mContext.getPackageName(), BuildConfig.APPLICATION_ID)) {
            audioManager = mContext.getSystemService(AudioManager.class);
        } else {
            audioManager = SystemUtils.AudioManager();
        }
        int maxVolume = audioManager != null ? audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 1;
        int currentVolume = audioManager != null ? audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : 0;
        return Math.max(0, Math.min((currentVolume * 100) / maxVolume, 100));
    }

    private void updateVisibility() {
        boolean enabled = progressType != ProgressType.UNKNOWN;
        setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
}
