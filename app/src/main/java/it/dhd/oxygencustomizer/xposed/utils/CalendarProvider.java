package it.dhd.oxygencustomizer.xposed.utils;

import android.Manifest;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import it.dhd.oxygencustomizer.BuildConfig;

public class CalendarProvider extends ContentProvider {

    private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".calendarprovider";
    public static final Uri CONTENT_URI_EVENTS = Uri.parse("content://" + AUTHORITY + "/events");
    public static final Uri CONTENT_URI_INSTANCES = Uri.parse("content://" + AUTHORITY + "/instances");

    private static final int MATCH_EVENTS = 1;
    private static final int MATCH_INSTANCES = 2;
    private static final UriMatcher MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        MATCHER.addURI(AUTHORITY, "events", MATCH_EVENTS);
        MATCHER.addURI(AUTHORITY, "instances/#/#", MATCH_INSTANCES);
    }

    public static Uri instancesUri(long begin, long end) {
        Uri.Builder builder = CONTENT_URI_INSTANCES.buildUpon();
        ContentUris.appendId(builder, begin);
        ContentUris.appendId(builder, end);
        return builder.build();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (getContext().checkSelfPermission(Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            throw new SecurityException("Permission READ_CALENDAR not granted");
        }
        ContentResolver resolver = getContext().getContentResolver();
        Uri target = switch (MATCHER.match(uri)) {
            case MATCH_EVENTS -> CalendarContract.Events.CONTENT_URI;
            case MATCH_INSTANCES -> {
                Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
                ContentUris.appendId(builder, Long.parseLong(uri.getPathSegments().get(1)));
                ContentUris.appendId(builder, Long.parseLong(uri.getPathSegments().get(2)));
                yield builder.build();
            }
            default -> null;
        };
        if (target == null) return null;

        return resolver.query(
                target,
                projection,
                selection,
                selectionArgs,
                sortOrder
        );
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".events";
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
