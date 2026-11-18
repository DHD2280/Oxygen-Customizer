package it.dhd.oxygencustomizer.provider;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.app.AppOpsManager;
import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.dhd.oxygencustomizer.BuildConfig;

public class UsageProvider extends ContentProvider {

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".usageprovider";
    public static final Uri SCREEN_TIME_URI = Uri.parse("content://" + AUTHORITY + "/screen_time");
    public static final Uri DATA_TODAY_URI = Uri.parse("content://" + AUTHORITY + "/data_today");
    public static final Uri DATA_MONTH_URI = Uri.parse("content://" + AUTHORITY + "/data_month");
    public static final String COLUMN_VALUE = "value";
    public static final long NO_ACCESS = -2;

    @Override
    public boolean onCreate() {
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        String caller = getCallingPackage();
        if (!SYSTEM_UI.equals(caller) && !BuildConfig.APPLICATION_ID.equals(caller)) return null;
        Context context = getContext();
        if (context == null) return null;
        long value;
        if (!hasUsageAccess(context)) {
            value = NO_ACCESS;
        } else if (SCREEN_TIME_URI.getPath().equals(uri.getPath())) {
            value = screenTime(context);
        } else if (DATA_MONTH_URI.getPath().equals(uri.getPath())) {
            value = mobileData(context, startOfMonth());
        } else {
            value = mobileData(context, startOfDay());
        }
        MatrixCursor cursor = new MatrixCursor(new String[]{COLUMN_VALUE});
        cursor.addRow(new Object[]{value});
        return cursor;
    }

    public static boolean hasUsageAccess(Context context) {
        AppOpsManager appOps = context.getSystemService(AppOpsManager.class);
        if (appOps == null) return false;
        int mode = appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), BuildConfig.APPLICATION_ID);
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private static long startOfDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private static long startOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startOfDay());
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar.getTimeInMillis();
    }

    private static long screenTime(Context context) {
        UsageStatsManager manager = context.getSystemService(UsageStatsManager.class);
        if (manager == null) return 0;
        Set<String> excluded = launchers(context);
        excluded.add(SYSTEM_UI);
        long start = startOfDay();
        long now = System.currentTimeMillis();
        UsageEvents events = manager.queryEvents(start, now);
        Map<String, Long> resumed = new HashMap<>();
        long total = 0;
        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            String pkg = event.getPackageName();
            if (pkg == null || excluded.contains(pkg)) continue;
            String key = pkg + "/" + event.getClassName();
            if (event.getEventType() == UsageEvents.Event.ACTIVITY_RESUMED) {
                resumed.put(key, event.getTimeStamp());
            } else if (event.getEventType() == UsageEvents.Event.ACTIVITY_PAUSED || event.getEventType() == UsageEvents.Event.ACTIVITY_STOPPED) {
                Long since = resumed.remove(key);
                if (since != null) total += event.getTimeStamp() - since;
            }
        }
        for (Long since : resumed.values()) total += now - since;
        return Math.max(0, total);
    }

    private static Set<String> launchers(Context context) {
        Set<String> packages = new HashSet<>();
        Intent home = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> infos = context.getPackageManager().queryIntentActivities(home, 0);
        for (ResolveInfo info : infos) {
            if (info.activityInfo != null) packages.add(info.activityInfo.packageName);
        }
        return packages;
    }

    private static long mobileData(Context context, long start) {
        NetworkStatsManager manager = context.getSystemService(NetworkStatsManager.class);
        if (manager == null) return 0;
        try {
            NetworkStats.Bucket bucket = manager.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, null, start, System.currentTimeMillis());
            return bucket == null ? 0 : bucket.getRxBytes() + bucket.getTxBytes();
        } catch (Throwable t) {
            return 0;
        }
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.item/vnd." + AUTHORITY;
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
