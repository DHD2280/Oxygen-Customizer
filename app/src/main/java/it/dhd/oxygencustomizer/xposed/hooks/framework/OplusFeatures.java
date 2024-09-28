package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class OplusFeatures extends XposedMods {

    public static final String listenPackage = FRAMEWORK;

    public OplusFeatures(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            Class<?> OplusFeatureCache = findClass("com.oplus.content.OplusFeatureCache", lpparam.classLoader);
            try {
                hookAllMethods(OplusFeatureCache, "query", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] instanceof String) {
                            String key = (String) param.args[0];
                            log("OplusFeatureCache.query: " + key);
                            if (key.contains("pocketstudio")) {
                                log("OplusFeatureCache.query: " + key);
                                param.setResult(true);
                            }
                        }
                    }
                });
            } catch (Throwable t) {
                log(t);
            }
        } catch (Throwable t) {
            log(t);
        }

        try {
            Class<?> FlexibleWindowManager = findClass("com.oplus.flexiblewindow.FlexibleWindowManager", lpparam.classLoader);
            try {
                hookAllConstructors(FlexibleWindowManager, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            setBooleanField(param.thisObject, "mHasPocketStudioFeature", true);
                        } catch (Throwable t) {
                            log(t);
                        }
                    }
                });
            } catch (Throwable t) {
                log(t);
            }
        } catch (Throwable t) {
            log(t);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
