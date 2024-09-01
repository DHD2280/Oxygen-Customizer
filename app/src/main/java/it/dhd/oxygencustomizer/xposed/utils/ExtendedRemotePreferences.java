package it.dhd.oxygencustomizer.xposed.utils;

import android.content.Context;

import com.crossbowffs.remotepreferences.RemotePreferences;

import java.util.List;

import it.dhd.oxygencustomizer.ui.preferences.SliderPreference;

public class ExtendedRemotePreferences extends RemotePreferences {
    public ExtendedRemotePreferences(Context context, String authority, String prefFileName) {
        super(context, authority, prefFileName);
    }

    public ExtendedRemotePreferences(Context context, String authority, String prefFileName, boolean strictMode) {
        super(context, authority, prefFileName, strictMode);
    }

    public int getSliderInt(String key, int defaultVal) {
        return SliderPreference.getSingleIntValue(this, key, defaultVal);
    }

    public float getSliderFloat(String key, float defaultVal) {
        return SliderPreference.getSingleFloatValue(this, key, defaultVal);
    }

    public List<Float> getSliderValues(String key, float defaultValue) {
        return SliderPreference.getValues(this, key, defaultValue);
    }
}
