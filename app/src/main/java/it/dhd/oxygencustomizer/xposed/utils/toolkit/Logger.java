package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import android.util.Log;

import io.github.libxposed.api.XposedInterface;

public class Logger {
    public static String TAG = "OxygenCustomizer Lsposed Module";
    private static XposedInterface xposedInterface;

    public static void setXposedInterface(XposedInterface xposedInterface) {
        Logger.xposedInterface = xposedInterface;
    }

    /**
     * Logs to logcat. tagged with {@link #TAG}
     */
    public static synchronized void log(String text) {
        try {
            xposedInterface.log(XposedInterface.PRIORITY_DEFAULT, TAG, text);
        }
        //XposedInterface and its log system won't activate before module loaded. A fallback can be useful
        catch (Throwable ignored) {
            Log.w(TAG, text);
        }
    }

    /**
     * Logs to logcat. tagged with {@link #TAG}
     */
    public static synchronized void log(String text, Throwable t) {
        try {
            xposedInterface.log(XposedInterface.PRIORITY_DEFAULT, TAG, text, t);
        } catch (Throwable ignored) {
            Log.e(TAG, text, t);
        }
    }

    /**
     * Logs to logcat. tagged with {@link #TAG}
     */
    public static synchronized void log(Throwable t) {
        log("", t);
    }
}
