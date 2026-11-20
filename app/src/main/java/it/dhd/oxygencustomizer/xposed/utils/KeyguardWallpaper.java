package it.dhd.oxygencustomizer.xposed.utils;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.CopyOnWriteArrayList;

public final class KeyguardWallpaper {

    private static final Handler sHandler = new Handler(Looper.getMainLooper());
    private static final CopyOnWriteArrayList<Runnable> sListeners = new CopyOnWriteArrayList<>();
    private static volatile Bitmap sBitmap;

    private KeyguardWallpaper() {
    }

    public static Bitmap get() {
        Bitmap bitmap = sBitmap;
        return bitmap != null && !bitmap.isRecycled() ? bitmap : null;
    }

    public static void set(Bitmap bitmap) {
        if (bitmap == null || bitmap.isRecycled() || bitmap == sBitmap) return;
        Bitmap copy = bitmap.copy(bitmap.getConfig() == Bitmap.Config.HARDWARE ? Bitmap.Config.ARGB_8888 : bitmap.getConfig(), false);
        if (copy == null) return;
        sBitmap = copy;
        sHandler.post(() -> {
            for (Runnable listener : sListeners) listener.run();
        });
    }

    public static void addListener(Runnable listener) {
        sListeners.addIfAbsent(listener);
    }

    public static void removeListener(Runnable listener) {
        sListeners.remove(listener);
    }
}
