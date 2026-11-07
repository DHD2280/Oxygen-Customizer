package it.dhd.oxygencustomizer.xposed;

import android.content.Context;
import android.util.Log;


import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;

public abstract class XposedMods {

    protected Context mContext;
    protected boolean mDebug = false;

    public XposedMods(Context context) {
        mContext = context;
    }

    public abstract void onPreferenceUpdated(String... Key);

    public abstract void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable;

    protected void initResources() {}

    public abstract boolean listensTo(String packageName);

    public void log(String message) {
        if (!mDebug) return;
        Logger.log("[ Oxygen Customizer - " + getClass().getSimpleName() + " ] " + message);
    }

    public void log(Throwable throwable) {
        Logger.log("[ Oxygen Customizer - " + getClass().getSimpleName() + " ] ERROR:" + " \n " + Log.getStackTraceString(throwable));
        Logger.log(throwable);
    }

}
