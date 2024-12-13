package it.dhd.oxygencustomizer.utils;

import android.content.Context;

import java.util.List;

import it.dhd.oneplusui.preference.OplusSliderPreference;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedSharedPreferences;

public class OCPreferences {

    private static final ExtendedSharedPreferences prefs = ExtendedSharedPreferences.from(OxygenCustomizer.get()
            .createDeviceProtectedStorageContext()
            .getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE));
    private static final ExtendedSharedPreferences.Editor editor = prefs.edit();

    public static ExtendedSharedPreferences getPrefs() {
        return prefs;
    }

    // Basic put methods
    public static void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).apply();
    }

    public static void putInt(String key, int value) {
        editor.putInt(key, value).apply();
    }

    public static void putFloat(String key, float value) {
        editor.putFloat(key, value).apply();
    }

    public static void putLong(String key, long value) {
        editor.putLong(key, value).apply();
    }

    public static void putString(String key, String value) {
        editor.putString(key, value).apply();
    }

    // Basic get methods
    public static boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    public static int getInt(String key) {
        return prefs.getInt(key, 0);
    }

    public static int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    public static long getLong(String key) {
        return prefs.getLong(key, 0);
    }

    public static long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    public static float getFloat(String key) {
        return prefs.getFloat(key, 0f);
    }

    public static float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    public static String getString(String key) {
        return prefs.getString(key, null);
    }

    public static String getString(String key, String defValue) {
        return prefs.getString(key, defValue);
    }

    // Custom slider preference methods
    public static int getSliderInt(String key, int defaultVal) {
        return OplusSliderPreference.getSingleIntValue(prefs, key, defaultVal);
    }

    public static List<Float> getSliderValues(String key, float defaultValue) {
        return OplusSliderPreference.getValues(prefs, key, defaultValue);
    }

    public static float getSliderFloat(String key, float defaultVal) {
        return OplusSliderPreference.getSingleFloatValue(prefs, key, defaultVal);
    }

    // Clear methods
    public static void clear(String... keys) {
        for (String key : keys) {
            editor.remove(key).apply();
        }
    }

    public static void clearAll() {
        editor.clear().apply();
    }
}

