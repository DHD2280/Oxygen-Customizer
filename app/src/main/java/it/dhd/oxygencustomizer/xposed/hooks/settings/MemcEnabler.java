package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.OPLUS_MEMC_FEATURES;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class MemcEnabler extends XposedMods {

    private final static String listenPackage = SETTINGS;
    private final static String TAG = "MemcEnabler--> ";

    private boolean mForceEnableMemc = false;

    public MemcEnabler(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mForceEnableMemc = Xprefs.getBoolean("force_memc_enabled", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> SysFeatureUtils = findClass("com.oplus.settings.utils.SysFeatureUtils", lpparam.classLoader);
        hookAllMethods(SysFeatureUtils, "hasOplusFeature", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                String requestedFeature = (String) param.args[0];
                if (OPLUS_MEMC_FEATURES.contains(requestedFeature) && mForceEnableMemc) {
                    param.setResult(true);
                }
            }
        });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
