package it.dhd.oxygencustomizer.xposed.utils;


import android.content.SharedPreferences;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

import it.dhd.oxygencustomizer.ui.preferences.OplusSliderPreference;

public class ExtendedSharedPreferences implements SharedPreferences {
    private final SharedPreferences prefs;

    public static ExtendedSharedPreferences from(SharedPreferences prefs) {
        return new ExtendedSharedPreferences(prefs);
    }

    private ExtendedSharedPreferences(SharedPreferences prefs) {
        this.prefs = prefs;
    }

    public int getSliderInt(String key, int defaultVal) {
        return OplusSliderPreference.getSingleIntValue(this, key, defaultVal);
    }

    public List<Float> getSliderValues(String key, float defaultValue) {
        return OplusSliderPreference.getValues(this, key, defaultValue);
    }

    public float getSliderFloat(String key, float defaultVal) {
        return OplusSliderPreference.getSingleFloatValue(this, key, defaultVal);
    }

    @Override
    public Map<String, ?> getAll() {
        return prefs.getAll();
    }

    @Nullable
    @Override
    public String getString(String key, @Nullable String defValue) {
        return prefs.getString(key, defValue);
    }

    @Nullable
    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defValues) {
        return prefs.getStringSet(key, defValues);
    }

    @Override
    public int getInt(String key, int defValue) {
        return prefs.getInt(key, defValue);
    }

    @Override
    public long getLong(String key, long defValue) {
        return prefs.getLong(key, defValue);
    }

    @Override
    public float getFloat(String key, float defValue) {
        return prefs.getFloat(key, defValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defValue) {
        return prefs.getBoolean(key, defValue);
    }

    @Override
    public boolean contains(String key) {
        return prefs.contains(key);
    }

    @Override
    public Editor edit() {
        return prefs.edit();
    }

    public void putInt(String key, int value) {
        edit().putInt(key, value).apply();
    }

    @Override
    public void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void unregisterOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener) {
        prefs.unregisterOnSharedPreferenceChangeListener(listener);
    }

    public void putString(String setting, String value) {
        edit().putString(setting, value).apply();
    }

    public void putBoolean(String key, boolean enabled) {
        edit().putBoolean(key, enabled).apply();
    }
}
