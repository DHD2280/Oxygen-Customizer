package it.dhd.oxygencustomizer.xposed.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import java.util.UUID;

public class ScreenshotUtils {

    public static final String SCREENSHOT_FULL = "systemQuickTileScreenshotIn";
    public static final String SCREEHSHOT_PARTIAL = "systemQuickTileArea";
    public static final String SCREENSHOT_SCROLL = "systemQuickTileLongshot";

    public enum ScreenshotType {
        FULL, PARTIAL, SCROLL
    }

    public static void takeScreenshot(Context context) {
        takeScreenshot(ScreenshotType.FULL, context);
    }

    public static void takeScreenshot(Context context, long delay) {
        takeScreenshot(ScreenshotType.FULL, context, delay);
    }

    public static void takeScreenshot(ScreenshotType type, Context context) {
        takeScreenshot(type, context, 750L);
    }

    public static void takeScreenshot(ScreenshotType type, Context context, long delay) {
        String source = switch (type) {
            case PARTIAL -> SCREEHSHOT_PARTIAL;
            case SCROLL -> SCREENSHOT_SCROLL;
            default -> SCREENSHOT_FULL;
        };
        new Handler(Looper.getMainLooper()).postDelayed(
                new ScreenShotRunnable(source, context, SystemClock.uptimeMillis(), UUID.randomUUID().toString().replace("-", "")), delay);
    }

}
