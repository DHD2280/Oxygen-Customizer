package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.utils.CalendarProvider;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class CalendarWidget extends BaseDeviceWidget {

    private ImageView mCalendarImage;
    private TextView mEventTitle, mEventTime, mEventLocation;
    private ImageView mCoundDownImage;
    private static ContentObserver mCalendarObserver;

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

    @SuppressLint("Range")
    public void getNextCalendarEvent() {

        if (mSettingsInterface && !AppUtils.hasPermission(appContext, Manifest.permission.READ_CALENDAR)) {
            mEventTitle.setText("Tap to set permission");
            return;
        } else {
            if (!hasPermission(BuildConfig.APPLICATION_ID, AppOpsManager.OPSTR_READ_CALENDAR)) {
                return;
            }
        }

        ContentResolver cr = appContext.getContentResolver();

        Cursor cursor = cr.query(
                CalendarProvider.CONTENT_URI_EVENTS,
                new String[]{
                        CalendarContract.Events._ID,
                        CalendarContract.Events.TITLE,
                        CalendarContract.Events.DTSTART,
                        CalendarContract.Events.CALENDAR_DISPLAY_NAME,
                        CalendarContract.Events.DTEND
                },
                CalendarContract.Events.DTSTART + " >= ?",
                new String[]{String.valueOf(System.currentTimeMillis())},
                CalendarContract.Events.DTSTART + " ASC LIMIT 1"
        );

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.TITLE));
            long startTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTSTART));
            String calendarName = cursor.getString(cursor.getColumnIndex(CalendarContract.Events.CALENDAR_DISPLAY_NAME));
            long eventEndTime = cursor.getLong(cursor.getColumnIndex(CalendarContract.Events.DTEND));
            SimpleDateFormat sdf2 = new SimpleDateFormat(mDateFormat, Locale.getDefault());
            String evendEnd = sdf2.format(new Date(startTime));
            Log.i("CalendarWidget", "Event end time: " + evendEnd);
            scheduleWidgetUpdate(eventEndTime);

            SimpleDateFormat sdf = new SimpleDateFormat(mDateFormat, Locale.getDefault());
            String eventTime = sdf.format(new Date(startTime));
            mEventTitle.setText(title.trim());
            mEventTime.setText(eventTime);
            mEventLocation.setText(calendarName);
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat(mDateFormat, Locale.getDefault());
            String eventTime = sdf.format(new Date(System.currentTimeMillis()));
            mEventTime.setText(eventTime);
        }

        if (cursor != null) {
            cursor.close();
        }
    }

    @RequiresPermission(Manifest.permission.SCHEDULE_EXACT_ALARM)
    private void scheduleWidgetUpdate(long eventEndTime) {
        Log.d("CalendarWidget", "Scheduling widget update for event end time: " + eventEndTime + " appContext " + (appContext == null));
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".UPDATE_WIDGET_AFTER_EVENT");
        intent.setAction("UPDATE_WIDGET_AFTER_EVENT");
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                eventEndTime,
                pendingIntent
        );
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
        return "Calendar";
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
        for (int i = 0; i<getChildCount(); i++) {
            View v = getChildAt(i);
            if (v instanceof TextView tv) {
                tv.setTextColor(textColor);
            }
        }
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
        if (mCalendarObserver == null) {
            mCalendarObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {
                    getNextCalendarEvent();
                }
            };

            context.getContentResolver().registerContentObserver(
                    CalendarProvider.CONTENT_URI_EVENTS,
                    true,
                    mCalendarObserver
            );
        }
        IntentFilter filter = new IntentFilter(BuildConfig.APPLICATION_ID + ".UPDATE_WIDGET_AFTER_EVENT");
        context.registerReceiver(mEventTick, filter, Context.RECEIVER_EXPORTED);
    }

    private void unregisterCalendarObserver(Context context) {
        if (mCalendarObserver != null) {
            context.getContentResolver().unregisterContentObserver(mCalendarObserver);
            mCalendarObserver = null;
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
