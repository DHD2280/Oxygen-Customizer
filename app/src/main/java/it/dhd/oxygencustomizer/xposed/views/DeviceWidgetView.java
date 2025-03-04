package it.dhd.oxygencustomizer.xposed.views;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.os.BatteryManager;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class DeviceWidgetView extends FrameLayout {

    private final Context mContext;
    private Context appContext;

    public int DEVICE_WIDGET_CLASSIC = 0;
    public int DEVICE_WIDGET_CIRCULAR = 1;

    private int mDeviceWidgetStyle = DEVICE_WIDGET_CLASSIC;
    private float mWidgetScaling = 1f;
    private TextView mBatteryLevelView;
    private ProgressBar mBatteryProgress;
    private int mBatteryPercentage = 1;
    private LinearLayout mClassicRow, mArcRow;
    private LinearLayout mVolumeLevelContainer, mRamUsageContainer, mVolumeLevelContainerArc, mRamUsageContainerArc;
    private ProgressImageView mBatteryPercentArc, mBatteryTempArc, mVolumeLevelArcProgress, mRamUsageArcProgress;

    private boolean mCustomColor = false;
    private int mProgressColor = 0;
    private int mLinearProgressColor = 0;
    private int mTextColor = 0;

    private boolean batteryRegistered = false;
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
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

        ThemeEnabler.registerThemeChangedListener(this::reloadColors);

        inflateView();
    }

    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "device_widget",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        addView(view);
        setupViews();
    }

    private void setupViews() {

        mClassicRow = (LinearLayout) ViewHelper.findViewWithTag(this, "device_widget_classic");
        mArcRow = (LinearLayout) ViewHelper.findViewWithTag(this, "device_widget_arc");

        // First row
        mBatteryLevelView = (TextView) ViewHelper.findViewWithTag(this, "battery_percentage");
        mBatteryProgress = (ProgressBar) ViewHelper.findViewWithTag(this, "battery_progressbar");
        mVolumeLevelContainer = (LinearLayout) ViewHelper.findViewWithTag(this, "volume_progress");
        mRamUsageContainer = (LinearLayout) ViewHelper.findViewWithTag(this, "ram_usage_info");
        // Volume progress
        if (mVolumeLevelArcProgress == null) {
            mVolumeLevelArcProgress = new ProgressImageView(mContext);
            mVolumeLevelArcProgress.setProgressType(ProgressImageView.ProgressType.VOLUME);
        }
        mVolumeLevelContainer.addView(mVolumeLevelArcProgress);
        // Ram usage progress
        if (mRamUsageArcProgress == null) {
            mRamUsageArcProgress = new ProgressImageView(mContext);
            mRamUsageArcProgress.setProgressType(ProgressImageView.ProgressType.MEMORY);
        }
        mRamUsageContainer.addView(mRamUsageArcProgress);

        mBatteryProgress.setProgressTintList(ColorStateList.valueOf(
                mCustomColor ?
                        mLinearProgressColor == 0 ?
                                getPrimaryColor(mContext) :
                                mLinearProgressColor :
                        getPrimaryColor(mContext)
        ));

        ((TextView) ViewHelper.findViewWithTag(this, "device_name")).setText(Build.MODEL);

        // Second Row
        mVolumeLevelContainerArc = (LinearLayout) ViewHelper.findViewWithTag(this, "volume_progress_2");
        mRamUsageContainerArc = (LinearLayout) ViewHelper.findViewWithTag(this, "memory_progress");

        LinearLayout batteryArc = (LinearLayout) ViewHelper.findViewWithTag(this, "battery_progress_arc");
        LinearLayout batteryTemp = (LinearLayout) ViewHelper.findViewWithTag(this, "temperature_progress");

        if (mBatteryPercentArc == null) {
            mBatteryPercentArc = new ProgressImageView(mContext);
            mBatteryPercentArc.setProgressType(ProgressImageView.ProgressType.BATTERY);
        }
        batteryArc.addView(mBatteryPercentArc);

        if (mBatteryTempArc == null) {
            mBatteryTempArc = new ProgressImageView(mContext);
            mBatteryTempArc.setProgressType(ProgressImageView.ProgressType.TEMPERATURE);
        }
        batteryTemp.addView(mBatteryTempArc);
    }

    private void setupRows() {
        LinearLayout volumeContainer = mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC ?
                mVolumeLevelContainer : mVolumeLevelContainerArc;
        LinearLayout ramContainer = mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC ?
                mRamUsageContainer : mRamUsageContainerArc;

        try {
            ((ViewGroup) mVolumeLevelArcProgress.getParent()).removeView(mVolumeLevelArcProgress);
            ((ViewGroup) mRamUsageArcProgress.getParent()).removeView(mRamUsageArcProgress);
        } catch (Throwable ignored) {}

        volumeContainer.addView(mVolumeLevelArcProgress);
        ramContainer.addView(mRamUsageArcProgress);

        mClassicRow.setVisibility(mDeviceWidgetStyle == DEVICE_WIDGET_CLASSIC ? View.VISIBLE : View.GONE);
        mArcRow.setVisibility(mDeviceWidgetStyle == DEVICE_WIDGET_CIRCULAR ? View.VISIBLE : View.GONE);
    }

    public void setDeviceWidgetStyle(int newStyle) {
        mDeviceWidgetStyle = newStyle;
        setupRows();
    }

    public void setDeviceWidgetScaling(float newScaling) {
        mWidgetScaling = newScaling;
        updateScaling();
    }

    private void updateScaling() {
        int artContainerSize = dp2px(mContext, 60);
        int batteryProgressContainerSize = dp2px(mContext, 120);

        List<View> views = List.of(mVolumeLevelArcProgress, mRamUsageArcProgress, mBatteryPercentArc, mBatteryTempArc);
        for (View v : views) {
            ViewGroup parent = (ViewGroup) v.getParent();
            int width = (int) (artContainerSize * mWidgetScaling);
            int height = (int) (artContainerSize * mWidgetScaling);
            ViewGroup.LayoutParams params = parent.getLayoutParams();
            params.width = width;
            params.height = height;
            parent.setLayoutParams(params);
            parent.requestLayout();
        }

        ViewGroup batteryProgressParent = (ViewGroup) mBatteryProgress.getParent();
        ViewGroup.LayoutParams params = batteryProgressParent.getLayoutParams();
        params.width = (int) (batteryProgressContainerSize * mWidgetScaling);
        batteryProgressParent.setLayoutParams(params);
        batteryProgressParent.requestLayout();
    }

    private void initBatteryStatus() {
        if (getVisibility() == View.GONE) return;
        if (mBatteryProgress != null) {
            post(() -> {
                mBatteryProgress.setProgress(mBatteryPercentage);
                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(
                        mCustomColor ?
                                mLinearProgressColor == 0 ?
                                        getPrimaryColor(mContext) :
                                        mLinearProgressColor :
                                getPrimaryColor(mContext)
                ));
            });
        }
        if (mBatteryLevelView != null) {
            post(() -> mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage)));
        }
    }

    public void setCustomColor(boolean customColor, int linearColor, int circularColor) {
        mCustomColor = customColor;
        mProgressColor = circularColor;
        mLinearProgressColor = linearColor;
        mVolumeLevelArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mRamUsageArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryPercentArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryTempArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
    }

    public void setTextCustomColor(int color) {
        mTextColor = color;
        mVolumeLevelArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mRamUsageArcProgress.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryPercentArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        mBatteryTempArc.setColors(mCustomColor ? mProgressColor : getPrimaryColor(mContext), mTextColor);
        post(() -> ViewHelper.findViewWithTagAndChangeColor(this, "text1", mTextColor));
    }

    public void setDeviceName(String devName) {
        String deviceName;
        if (!TextUtils.isEmpty(devName)) {
            deviceName = devName;
        } else {
            deviceName = Build.MODEL;
        }

        post(() -> ((TextView) ViewHelper.findViewWithTag(this, "device_name")).setText(deviceName));

    }

    private void reloadColors() {
        initBatteryStatus();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            if (!batteryRegistered)
                mContext.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        try {
            if (batteryRegistered) mContext.unregisterReceiver(mBatteryReceiver);
        } catch (Exception ignored) {
        }
    }

}
