package it.dhd.oxygencustomizer.ui.models;

public class ThemeModel {

    private String themeName;
    private String pkgName;
    private boolean isEnabled;

    public ThemeModel(String pkgName, String themeName, boolean isEnabled) {
        this.themeName = themeName;
        this.pkgName = pkgName;
        this.isEnabled = isEnabled;
    }

    public String getThemeName() {
        return themeName;
    }

    public String getPkgName() {
        return pkgName;
    }

    public boolean isEnabled() {
        return isEnabled;
    }
    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

}
