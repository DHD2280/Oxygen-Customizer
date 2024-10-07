package it.dhd.oxygencustomizer.ui.models;

import android.graphics.drawable.Drawable;

public class MemcAppModel {

    private String appName;
    private String packageName;
    private String activityName;
    private Drawable appIcon;
    private boolean isEnabled;
    private int refreshRate;
    private String memcConfig;
    private boolean isActivity;

    public MemcAppModel(
            String appName,
            String packageName,
            String activityName,
            Drawable appIcon,
            boolean isActivity,
            int refreshRate,
            String memcConfig) {
        this.appName = appName;
        this.packageName = packageName;
        this.activityName = activityName;
        this.appIcon = appIcon;
        this.isActivity = isActivity;
        this.refreshRate = refreshRate;
        this.memcConfig = memcConfig;
    }

    public MemcAppModel(
            String appName,
            String packageName,
            Drawable appIcon) {
        this.appName = appName;
        this.packageName = packageName;
        this.appIcon = appIcon;
        this.isActivity = false;
        this.refreshRate = 120;
        this.memcConfig = "267-3-1";
    }

    public String getAppName() {
        return appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getActivityName() {
        return activityName;
    }

    public Drawable getAppIcon() {
        return appIcon;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public int getRefreshRate() {
        return refreshRate;
    }

    public String getMemcConfig() {
        return memcConfig;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public void setRefreshRate(int value) {
        refreshRate = value;
    }

    public void setMemcConfig(String value) {
        memcConfig = value;
    }

    public boolean isActivity() {
        return this.isActivity;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

}
