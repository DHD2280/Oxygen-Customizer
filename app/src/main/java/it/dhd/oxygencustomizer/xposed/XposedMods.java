package it.dhd.oxygencustomizer.xposed;

import android.content.Context;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public abstract class XposedMods {

    protected Context mContext;
    protected boolean mDebug = false;

    public XposedMods(Context context) {
        mContext = context;
    }

    public abstract void updatePrefs(String... Key);

    public abstract void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;

    public abstract boolean listensTo(String packageName);

    public void log(String message) {
        if (!mDebug) return;
        XposedBridge.log("[ Oxygen Customizer - " + getClass().getSimpleName() + " ] " + message);
    }

    public void log(Throwable throwable) {
        XposedBridge.log("[ Oxygen Customizer - " + getClass().getSimpleName() + " ] ERROR:" + throwable.getMessage());
    }

}
