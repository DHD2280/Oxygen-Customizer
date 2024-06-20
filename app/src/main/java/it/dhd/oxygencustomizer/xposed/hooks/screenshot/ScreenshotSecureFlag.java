package it.dhd.oxygencustomizer.xposed.hooks.screenshot;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class ScreenshotSecureFlag extends XposedMods {

    private static final String TAG = "Oxygen Customizer - Screenshot Secure Flag: ";
    private boolean mDisableSecure = false;

    public ScreenshotSecureFlag(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mDisableSecure = Xprefs.getBoolean("disable_secure_screenshot", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        final XC_MethodHook nullReturner = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (mDisableSecure)
                    param.setResult(null);
            }
        };
        try {
            Class<?> ScreenshotContext = findClass("com.oplus.screenshot.screenshot.core.ScreenshotContext", lpparam.classLoader);
            hookMethods(ScreenshotContext, nullReturner, "setScreenshotReject", "setLongshotReject");
        } catch (Throwable t) {
            log(TAG + "Error hooking methods: " + t.getMessage());
        }
        try {
            Class<?> ScreenshotContext = findClass("com.oplus.screenshot.screenshot.core.ScreenshotContentContext", lpparam.classLoader);
            hookMethods(ScreenshotContext, nullReturner, "setScreenshotReject", "setLongshotReject");
        } catch (Throwable t) {
            log(TAG + "Error hooking methods: " + t.getMessage());
        }
    }

    private void hookMethods(Class<?> clazz, XC_MethodHook hooker, String... names) {
        var list = Arrays.asList(names);
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> list.contains(method.getName()))
                .forEach(method -> hookMethod(method, hooker));
    }

    @Override
    public boolean listensTo(String packageName) {
        return false;
    }
}
