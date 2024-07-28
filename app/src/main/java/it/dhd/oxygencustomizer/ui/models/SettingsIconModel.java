package it.dhd.oxygencustomizer.ui.models;

import android.graphics.drawable.Drawable;

public class SettingsIconModel extends IconModel{

    private boolean hasBgColor, hasBgShape, hasBgSolid, hasIconColor;

    public SettingsIconModel(String name, int desc, int icon1, int icon2, int icon3, int icon4) {
        super(name, desc, icon1, icon2, icon3, icon4);
    }

    public SettingsIconModel(String packName, String pkgName, Drawable icon1, Drawable icon2, Drawable icon3, Drawable icon4, boolean isEnabled) {
        super(packName, pkgName, icon1, icon2, icon3, icon4, isEnabled);
    }

    public SettingsIconModel(String name, int desc, int icon1, int icon2, int icon3, int icon4, boolean hasBgColor, boolean hasBgShape, boolean hasBgSolid, boolean hasIconColor) {
        super(name, desc, icon1, icon2, icon3, icon4);
        this.hasBgColor = hasBgColor;
        this.hasBgShape = hasBgShape;
        this.hasBgSolid = hasBgSolid;
        this.hasIconColor = hasIconColor;
    }

    public SettingsIconModel(String packName, String pkgName, Drawable icon1, Drawable icon2, Drawable icon3, Drawable icon4, boolean isEnabled, boolean hasBgColor, boolean hasBgShape, boolean hasBgSolid, boolean hasIconColor) {
        super(packName, pkgName, icon1, icon2, icon3, icon4, isEnabled);
        this.hasBgColor = hasBgColor;
        this.hasBgShape = hasBgShape;
        this.hasBgSolid = hasBgSolid;
        this.hasIconColor = hasIconColor;
    }

    public boolean hasBgColor() {
        return hasBgColor;
    }

    public boolean hasBgShape() {
        return hasBgShape;
    }

    public boolean hasBgSolid() {
        return hasBgSolid;
    }

    public boolean hasIconColor() {
        return hasIconColor;
    }
}
