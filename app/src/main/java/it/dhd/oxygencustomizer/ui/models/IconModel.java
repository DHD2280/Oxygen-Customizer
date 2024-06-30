package it.dhd.oxygencustomizer.ui.models;

import android.graphics.drawable.Drawable;

public class IconModel {

    private String name;
    private String packageName;
    private int desc = 0, icon1 = 0, icon2 = 0, icon3 = 0, icon4 = 0;
    private Drawable drawableIcon1, drawableIcon2, drawableIcon3, drawableIcon4;
    private boolean isEnabled;

    public IconModel(String name, int desc, int icon1, int icon2, int icon3, int icon4) {
        this.name = name;
        this.desc = desc;
        this.icon1 = icon1;
        this.icon2 = icon2;
        this.icon3 = icon3;
        this.icon4 = icon4;
    }

    public IconModel(String packName, String pkgName, Drawable icon1, Drawable icon2, Drawable icon3, Drawable icon4, boolean isEnabled) {
        this.name = packName;
        this.packageName = pkgName;
        this.drawableIcon1 = icon1;
        this.drawableIcon2 = icon2;
        this.drawableIcon3 = icon3;
        this.drawableIcon4= icon4;
        this.isEnabled = isEnabled;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getDesc() {
        return desc;
    }

    public void setDesc(int desc) {
        this.desc = desc;
    }

    public int getIcon1() {
        return icon1;
    }

    public void setIcon1(int icon1) {
        this.icon1 = icon1;
    }

    public int getIcon2() {
        return icon2;
    }

    public void setIcon2(int icon2) {
        this.icon2 = icon2;
    }

    public int getIcon3() {
        return icon3;
    }

    public void setIcon3(int icon3) {
        this.icon3 = icon3;
    }

    public int getIcon4() {
        return icon4;
    }

    public void setIcon4(int icon4) {
        this.icon4 = icon4;
    }

    public Drawable getDrawableIcon1() {
        return drawableIcon1;
    }

    public void setDrawableIcon1(Drawable drawableIcon1) {
        this.drawableIcon1 = drawableIcon1;
    }

    public Drawable getDrawableIcon2() {
        return drawableIcon2;
    }

    public void setDrawableIcon2(Drawable drawableIcon2) {
        this.drawableIcon2 = drawableIcon2;
    }

    public Drawable getDrawableIcon3() {
        return drawableIcon3;
    }

    public void setDrawableIcon3(Drawable drawableIcon3) {
        this.drawableIcon3 = drawableIcon3;
    }

    public Drawable getDrawableIcon4() {
        return drawableIcon4;
    }

    public void setDrawableIcon4(Drawable drawableIcon4) {
        this.drawableIcon4 = drawableIcon4;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }
}
