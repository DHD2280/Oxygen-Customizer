package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class AlarmWidget extends BaseDeviceWidget {

    private static final long DAY_MS = 24L * 60 * 60 * 1000;
    private static final long MINUTE_MS = 60_000L;

    private final ImageView mProgress;
    private ImageView mIcon;
    private TextView mTime, mDay, mRemaining;
    private boolean mReceiverRegistered = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            update();
        }
    };

    public AlarmWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mProgress = new ImageView(mContext);
        mProgress.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mProgress.setLayoutParams(new LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        onWidgetModeChanged();
    }

    private void inflateView() {
        View view = LayoutInflater.from(appContext).inflate(
                appContext.getResources().getIdentifier("device_widget_weather_widget", "layout", BuildConfig.APPLICATION_ID),
                null
        );
        mIcon = (ImageView) ViewHelper.findViewWithTag(view, "condition_image");
        mTime = (TextView) ViewHelper.findViewWithTag(view, "current_location");
        mDay = (TextView) ViewHelper.findViewWithTag(view, "current_condition");
        mRemaining = (TextView) ViewHelper.findViewWithTag(view, "high_low");
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    private long nextAlarm() {
        if (mSettingsInterface)
            return System.currentTimeMillis() + 7 * 60 * MINUTE_MS + 30 * MINUTE_MS;
        try {
            AlarmManager manager = mContext.getSystemService(AlarmManager.class);
            AlarmManager.AlarmClockInfo info = manager == null ? null : manager.getNextAlarmClock();
            return info == null ? -1 : info.getTriggerTime();
        } catch (Throwable t) {
            return -1;
        }
    }

    private Drawable icon() {
        Drawable icon = ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_alarm_widget, appContext.getTheme());
        if (icon == null) return null;
        icon = icon.mutate();
        icon.setTint(mTextColor);
        return icon;
    }

    private void update() {
        long trigger = nextAlarm();
        long now = System.currentTimeMillis();
        long remaining = trigger - now;
        String time = trigger > 0 ? DateFormat.getTimeFormat(mContext).format(new Date(trigger)) : "--";

        if (mCurrentMode == WidgetMode.SMALL) {
            int progress = trigger > 0 && remaining < DAY_MS ? (int) ((DAY_MS - Math.max(remaining, 0)) / MINUTE_MS) : 0;
            mProgress.setImageBitmap(ArcProgressWidget.generateBitmap(mContext, progress, (int) (DAY_MS / MINUTE_MS), time,
                    time.length() > 5 ? 30 : 36, icon(), false, 36, null, mProgressColor, mTextColor));
            return;
        }
        if (mTime == null) return;
        mIcon.setImageTintList(null);
        mIcon.setImageDrawable(icon());
        mTime.setTextColor(mTextColor);
        mDay.setTextColor(mTextColor);
        mRemaining.setTextColor(mTextColor);
        if (trigger <= 0) {
            mTime.setText(appContext.getString(R.string.alarm_none));
            mDay.setText("");
            mRemaining.setText("");
            return;
        }
        mTime.setText(time);
        mDay.setText(dayLabel(trigger));
        mRemaining.setText(appContext.getString(R.string.alarm_in, duration(Math.max(remaining, 0))));
    }

    private String dayLabel(long trigger) {
        Calendar today = Calendar.getInstance();
        Calendar alarm = Calendar.getInstance();
        alarm.setTimeInMillis(trigger);
        int days = alarm.get(Calendar.DAY_OF_YEAR) - today.get(Calendar.DAY_OF_YEAR)
                + 365 * (alarm.get(Calendar.YEAR) - today.get(Calendar.YEAR));
        if (days == 0) return appContext.getString(R.string.alarm_today);
        if (days == 1) return appContext.getString(R.string.alarm_tomorrow);
        return DateFormat.format("EEEE", alarm).toString();
    }

    private String duration(long millis) {
        long minutes = (millis + MINUTE_MS - 1) / MINUTE_MS;
        long hours = minutes / 60;
        long days = hours / 24;
        if (days > 0) return String.format(Locale.getDefault(), "%dd %dh", days, hours % 24);
        if (hours > 0) return String.format(Locale.getDefault(), "%dh %dm", hours, minutes % 60);
        return String.format(Locale.getDefault(), "%dm", minutes);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mReceiverRegistered && !mSettingsInterface) {
            IntentFilter filter = new IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED);
            filter.addAction(Intent.ACTION_TIME_TICK);
            filter.addAction(Intent.ACTION_TIME_CHANGED);
            filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
            mContext.registerReceiver(mReceiver, filter, Context.RECEIVER_EXPORTED);
            mReceiverRegistered = true;
        }
        update();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mReceiverRegistered) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Throwable ignored) {
            }
            mReceiverRegistered = false;
        }
    }

    @Override
    public void onWidgetModeChanged() {
        super.onWidgetModeChanged();
        if (mProgress == null) return;
        removeAllViews();
        if (mProgress.getParent() instanceof ViewGroup parent) parent.removeView(mProgress);
        mTime = null;
        if (mCurrentMode == WidgetMode.SMALL) {
            addView(mProgress);
        } else {
            inflateView();
        }
        update();
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.alarm_widget);
    }

    @Override
    public boolean hasBigMode() {
        return true;
    }

    @Override
    public boolean hasSmallMode() {
        return true;
    }

    @Override
    public View getBigPreview() {
        AlarmWidget preview = new AlarmWidget(mContext, true);
        preview.setMode(WidgetMode.BIG);
        return preview;
    }

    @Override
    public View getSmallPreview() {
        AlarmWidget preview = new AlarmWidget(mContext, true);
        preview.setMode(WidgetMode.SMALL);
        return preview;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        return Collections.emptyList();
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        update();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
