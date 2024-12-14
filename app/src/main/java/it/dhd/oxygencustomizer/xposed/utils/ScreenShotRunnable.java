package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.content.Context;
import android.os.Bundle;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import de.robv.android.xposed.XposedBridge;

public final class ScreenShotRunnable implements Runnable {
    public final Context context;
    public final String relationId;
    public final String screenshotSource;
    public final long startTime;

    public ScreenShotRunnable(String str, Context context, long j, String str2) {
        this.screenshotSource = str;
        this.context = context;
        this.startTime = j;
        this.relationId = str2;
    }

    @Override
    public void run() {
        takeScreenshot(this.screenshotSource);
    }

    public void takeScreenshot(String str) {
        Class<?> OplusLongshotUtils = null;
        try {
            Context otherAppContext = context.createPackageContext("com.oplus.screenshot", Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            ClassLoader classLoader = otherAppContext.getClassLoader();
            OplusLongshotUtils = Class.forName("com.oplus.screenshot.OplusLongshotUtils", false, classLoader);
        } catch (Exception e) {
            XposedBridge.log(e);
        }
        if (OplusLongshotUtils == null) return;
        Method getScreenshotManagerMethod = null;
        Object screenshotManager = null;
        try {
            getScreenshotManagerMethod = OplusLongshotUtils.getMethod("getScreenshotManager", Context.class);
            screenshotManager = getScreenshotManagerMethod.invoke(null, context);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            XposedBridge.log(e);
        }

        if (screenshotManager != null) {
            Bundle bundle = new Bundle();
            bundle.putString("screenshot_source", str);
            bundle.putString("screenshot_relation_id", this.relationId);
            bundle.putLong("screenshot_start_time", this.startTime);
            bundle.putBoolean("statusbar_visible", false);
            bundle.putBoolean("navigationbar_visible", false);
            bundle.putBoolean("global_action_visible", false);
            bundle.putBoolean("screenshot_orientation", this.context.getResources().getConfiguration().orientation == 2);
            callMethod(screenshotManager, "takeScreenshot", bundle);
        }
    }
}
