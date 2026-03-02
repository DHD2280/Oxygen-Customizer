package it.dhd.oxygencustomizer.ui.models;

import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

public class AppModel {

    private final String appName;
    private final String packageName;
    private final Drawable appIcon;
    private boolean isEnabled;
    private int darkModeValue;
    private final boolean isSystem;
    private CharSequence[] mEntries, mEntryValues;
    private String mSelectedValue = "";

    public AppModel(String appName, String packageName, Drawable appIcon, boolean isSystemApp, boolean enabled, int darkModeValue) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isEnabled = enabled;
        this.darkModeValue = darkModeValue;
        this.isSystem = isSystemApp;
    }

    public AppModel(String appName, String packageName, Drawable appIcon, boolean isSystemApp, boolean enabled) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isEnabled = enabled;
        this.isSystem = isSystemApp;
    }

    public AppModel(String appName, String packageName, Drawable appIcon, boolean isSystemApp,
                    CharSequence[] entries, CharSequence[] entryValues, String selectedValue) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.mEntries = entries;
        this.mEntryValues = entryValues;
        this.isSystem = isSystemApp;
        this.mSelectedValue = selectedValue;
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public int getDarkModeValue() {
        return darkModeValue;
    }

    public String getSelectedValue() {
        return mSelectedValue;
    }

    public void setSelectedValue(String selectedValue) {
        mSelectedValue = selectedValue;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    public void setDarkModeValue(int value) {
        darkModeValue = value;
    }

    public boolean isSystemApp() {
        return isSystem;
    }

    @Override
    @NonNull
    public String toString() {
        return "AppModel{" +
                "appName='" + appName + '\'' +
                ", packageName='" + packageName + '\'' +
                ", appIcon=" + appIcon +
                ", isEnabled=" + isEnabled +
                ", darkModeValue=" + darkModeValue +
                ", mSelectedValue='" + mSelectedValue +
                ", isSystem=" + isSystem +
                '}';
    }

}
