package it.dhd.oxygencustomizer.xposed.utils.systemui;

public class TemperatureNative {
    private String mName;
    private float mValue;

    public TemperatureNative(String str, float f2) {
        this.mName = str;
        this.mValue = f2;
    }

    public String getmName() {
        return this.mName;
    }

    public void setmName(String str) {
        this.mName = str;
    }

    public float getmValue() {
        return this.mValue;
    }

    public void setmValue(float f2) {
        this.mValue = f2;
    }
}