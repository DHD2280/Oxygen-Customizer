package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.utils.CalendarProvider;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;

@SuppressLint("ViewConstructor")
public class CalendarWidget extends BaseDeviceWidget {

    private ImageView mCalendarImage;
    private TextView mEventTitle, mEventTime, mEventLocation;
    private ImageView mCoundDownImage;
    private ContentObserver mCalendarObserver;
    private boolean mTickRegistered = false;
    private static final long LOOKAHEAD_MS = 365L * 24 * 60 * 60 * 1000;

    private final int CALENDAR_NEXT_EVENT = 0;
    private final int CALENDAR_TODAY_EVENT = 1;
    private final int CALENDAR_CUSTOM_COUNTDOWN = 2;

    // Settings
    private String mDateFormat = "dd MMM yyyy";
    private int calendarMode = CALENDAR_NEXT_EVENT;

    private final BroadcastReceiver mEventTick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            getNextCalendarEvent();
        }
    };

    public boolean hasPermission(String packageName, String permission) {
        try {
            AppOpsManager appOps = (AppOpsManager) mContext.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo appInfo = mContext.getPackageManager().getApplicationInfo(packageName, 0);
            int mode = appOps.unsafeCheckOpNoThrow(permission, appInfo.uid, packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean hasCalendarPermission() {
        Context context = appContext != null ? appContext : mContext;
        return context.getPackageManager().checkPermission(Manifest.permission.READ_CALENDAR, BuildConfig.APPLICATION_ID) == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("Range")
    public void getNextCalendarEvent() {
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat(mDateFormat, Locale.getDefault());
        mEventTime.setText(sdf.format(new Date(now)));
        mEventLocation.setText("");

        if (!hasCalendarPermission()) {
            mEventTitle.setText(appContext.getString(R.string.calendar_permission_required));
            return;
        }

        Cursor cursor;
        try {
            cursor = appContext.getContentResolver().query(
                    CalendarProvider.instancesUri(now, now + LOOKAHEAD_MS),
                    new String[]{
                            CalendarContract.Instances.EVENT_ID,
                            CalendarContract.Instances.TITLE,
                            CalendarContract.Instances.BEGIN,
                            CalendarContract.Instances.END,
                            CalendarContract.Instances.CALENDAR_DISPLAY_NAME
                    },
                    CalendarContract.Instances.END + " > ? AND " + CalendarContract.Instances.VISIBLE + " = 1",
                    new String[]{String.valueOf(now)},
                    CalendarContract.Instances.BEGIN + " ASC"
            );
        } catch (Throwable t) {
            Log.e("CalendarWidget", "Calendar query failed", t);
            mEventTitle.setText(appContext.getString(R.string.calendar_no_events));
            return;
        }

        try {
            if (cursor == null || !cursor.moveToFirst()) {
                mEventTitle.setText(appContext.getString(R.string.calendar_no_events));
                return;
            }
            String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Instances.TITLE));
            long startTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.BEGIN));
            long eventEndTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Instances.END));
            String calendarName = cursor.getString(cursor.getColumnIndex(CalendarContract.Instances.CALENDAR_DISPLAY_NAME));
            if (!mSettingsInterface) {
                try {
                    scheduleWidgetUpdate(eventEndTime);
                } catch (Throwable t) {
                    Logger.log("CalendarWidget - error scheduling update");
                }
            }
            mEventTitle.setText(title == null ? "" : title.trim());
            mEventTime.setText(sdf.format(new Date(startTime)));
            mEventLocation.setText(calendarName == null ? "" : calendarName);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void scheduleWidgetUpdate(long eventEndTime) {
        AlarmManager alarmManager = SystemUtils.AlarmManager();
        if (alarmManager == null) return;
        alarmManager.set(AlarmManager.RTC,
                eventEndTime,
                "",
                this::getNextCalendarEvent,
                null);
    }

    public CalendarWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        inflateView();
    }

    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "device_widget_weather_widget",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        mCalendarImage = (ImageView) ViewHelper.findViewWithTag(view, "condition_image");
        mCalendarImage.setVisibility(View.GONE);
        mEventTime = (TextView) ViewHelper.findViewWithTag(view, "current_location");
        mEventTitle = (TextView) ViewHelper.findViewWithTag(view, "current_condition");
        mEventLocation = (TextView) ViewHelper.findViewWithTag(view, "high_low");

        addView(view);
        getNextCalendarEvent();
    }

    @Override
    public String getWidgetReference() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.calendar_widget);
    }

    @Override
    public boolean hasBigMode() {
        return true;
    }

    @Override
    public boolean hasSmallMode() {
        return false;
    }

    @Override
    public View getBigPreview() {
        return this;
    }

    @Override
    public View getSmallPreview() {
        return null;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        return Collections.emptyList();
    }

    @Override
    public void applySettings(Map<String, Object> settings) {}

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        mEventTitle.setTextColor(textColor);
        mEventTime.setTextColor(textColor);
        mEventLocation.setTextColor(textColor);
    }

    @Override
    public void onSetDeviceName(String deviceName) {}

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mSettingsInterface) {
            registerCalendarObserver(appContext);
        }
        getNextCalendarEvent();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!mSettingsInterface) {
            unregisterCalendarObserver(appContext);
        }
    }

    private void registerCalendarObserver(Context context) {
        if (mCalendarObserver == null && hasCalendarPermission()) {
            mCalendarObserver = new ContentObserver(new Handler(Looper.getMainLooper())) {
                @Override
                public void onChange(boolean selfChange) {
                    getNextCalendarEvent();
                }
            };
            try {
                context.getContentResolver().registerContentObserver(CalendarContract.CONTENT_URI, true, mCalendarObserver);
            } catch (Throwable t) {
                mCalendarObserver = null;
            }
        }
        if (!mTickRegistered) {
            IntentFilter filter = new IntentFilter(BuildConfig.APPLICATION_ID + ".UPDATE_WIDGET_AFTER_EVENT");
            mContext.registerReceiver(mEventTick, filter, Context.RECEIVER_EXPORTED);
            mTickRegistered = true;
        }
    }

    private void unregisterCalendarObserver(Context context) {
        if (mCalendarObserver != null) {
            context.getContentResolver().unregisterContentObserver(mCalendarObserver);
            mCalendarObserver = null;
        }
        if (mTickRegistered) {
            try {
                mContext.unregisterReceiver(mEventTick);
            } catch (Throwable ignored) {
            }
            mTickRegistered = false;
        }
    }

    public class WidgetUpdateWorker extends Worker {

        public WidgetUpdateWorker(@NonNull Context context, @NonNull WorkerParameters params) {
            super(context, params);
        }

        @NonNull
        @Override
        public Result doWork() {
            Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".UPDATE_WIDGET_AFTER_EVENT");
            getContext().sendBroadcast(intent);
            return Result.success();
        }
    }

}
