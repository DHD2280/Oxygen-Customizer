package it.dhd.oxygencustomizer.ui.models;

import android.graphics.drawable.Drawable;

public class AppModel {

    private String appName;
    private String packageName;
    private Drawable appIcon;
    private boolean isEnabled;
    private int darkModeValue;

    public AppModel(String appName, String packageName, Drawable appIcon, boolean enabled, int darkModeValue) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isEnabled = enabled;
        this.darkModeValue = darkModeValue;
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

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setDarkModeValue(int value) {
        darkModeValue = value;
    }


}
