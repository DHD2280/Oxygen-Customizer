package it.dhd.oxygencustomizer.xposed.views;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;

public class DeviceWidgetView extends FrameLayout {

    private final Context mContext;
    private Context appContext;

    private TextView mBatteryLevelView;
    private ProgressBar mBatteryProgress;
    private int mBatteryPercentage = 1;
    private ImageView mVolumeLevelArcProgress;
    private ImageView mRamUsageArcProgress;

    private AudioManager mAudioManager;
    private ActivityManager mActivityManager;

    public DeviceWidgetView(Context context) {
        super(context);
        mContext = context;
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                        int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                        int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                        mBatteryPercentage = (level * 100) / scale;
                        initBatteryStatus();
                    }
                }
            };
            context.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
        }
        try {
            BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    initSoundManager();
                }
            };
            context.registerReceiver(mVolumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        } catch (Exception ignored) {
        }

        inflateView();
    }

    private void inflateView() {
        inflate(appContext, R.layout.device_widget, this);
        setupViews();
        initSoundManager();
    }

    private void setupViews() {
        mBatteryLevelView = findViewById(R.id.battery_percentage);
        mBatteryProgress = findViewById(R.id.battery_progressbar);
        mVolumeLevelArcProgress = findViewById(R.id.volume_progress);
        mRamUsageArcProgress = findViewById(R.id.ram_usage_info);

        mBatteryProgress.setProgressTintList(ColorStateList.valueOf(getPrimaryColor(mContext)));

        ((TextView) findViewById(R.id.device_name)).setText(Build.MODEL);
    }

    private void initBatteryStatus() {

        if (mBatteryProgress != null) {
            post(() -> {
                mBatteryProgress.setProgress(mBatteryPercentage);
                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(getPrimaryColor(mContext)));
            });
        }
        if (mBatteryLevelView != null) {
            post(() -> mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage)));
        }

        initRamUsage();
    }

    private void initSoundManager() {

        int volLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolLevel = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volPercent = (int) (((float) volLevel / maxVolLevel) * 100);

        if (mVolumeLevelArcProgress != null) {
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    volPercent,
                    appContext.getResources().getString(R.string.percentage_text, volPercent),
                    32,
                    ContextCompat.getDrawable(appContext, R.drawable.ic_volume_up),
                    36,
                    getPrimaryColor(mContext)
            );
            post(() -> mVolumeLevelArcProgress.setImageBitmap(widgetBitmap));
        }
    }

    private void initRamUsage() {

        if (mActivityManager == null) return;

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        long usedMemory = memoryInfo.totalMem - memoryInfo.availMem;
        if (memoryInfo.totalMem == 0) return;
        int usedMemoryPercentage = (int) ((usedMemory * 100) / memoryInfo.totalMem);

        if (mRamUsageArcProgress != null) {
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    usedMemoryPercentage,
                    appContext.getResources().getString(R.string.percentage_text, usedMemoryPercentage),
                    32,
                    "RAM",
                    20,
                    getPrimaryColor(mContext)
            );
            post(() -> mRamUsageArcProgress.setImageBitmap(widgetBitmap));
        }
    }

}
