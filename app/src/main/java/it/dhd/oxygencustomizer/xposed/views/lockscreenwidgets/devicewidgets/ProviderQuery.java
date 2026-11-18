package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.LongConsumer;

public final class ProviderQuery {

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private ProviderQuery() {
    }

    public static void queryLong(Context context, Uri uri, long fallback, LongConsumer callback) {
        EXECUTOR.execute(() -> {
            long value = fallback;
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) value = cursor.getLong(0);
            } catch (Throwable ignored) {
            }
            long result = value;
            MAIN.post(() -> callback.accept(result));
        });
    }
}
