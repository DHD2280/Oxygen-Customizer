package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

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

    private final int CALENDAR_NEXT_EVENT = 0;
    private final int CALENDAR_TODAY_EVENT = 1;
    private final int CALENDAR_CUSTOM_COUNTDOWN = 2;

    // Settings
    private String mDateFormat = "dd MMM yyyy";
    private int calendarMode = CALENDAR_NEXT_EVENT;

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
        Uri uri = CalendarProvider.CONTENT_URI;

        String selection = CalendarContract.Events.DTSTART + " >= ?";
        String[] selectionArgs = new String[]{String.valueOf(System.currentTimeMillis())};

        String sortOrder = CalendarContract.Events.DTSTART + " ASC LIMIT 1";

        Cursor cursor = cr.query(uri, new String[]{
                CalendarContract.Events.TITLE,
                CalendarContract.Events.DTSTART,
                CalendarContract.Events.CALENDAR_DISPLAY_NAME
        }, selection, selectionArgs, sortOrder);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(0);
            long startTime = cursor.getLong(1);
            String calendarName = cursor.getString(2);

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

}
