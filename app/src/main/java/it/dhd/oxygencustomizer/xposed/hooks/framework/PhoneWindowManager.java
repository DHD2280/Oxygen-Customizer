package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.Deoptimizer.deoptimizeMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class PhoneWindowManager extends XposedMods {

    private final static String TAG = "Oxygen Customizer - PhoneWindowManager: ";
    private boolean mDisableSecure = false, mDisableScreenshotObserver = false;

    public PhoneWindowManager(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mDisableSecure = Xprefs.getBoolean("disable_secure_screenshot", false);
        mDisableScreenshotObserver = Xprefs.getBoolean("disable_screenshot_observer", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (mDisableSecure) {
            try {
                hookSecureScreenshot(lpparam);
            } catch (Throwable t) {
                log(TAG + "hook Secure Screenshot failed " + t.getMessage());
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            try {
                hookActivityTaskManagerService(lpparam.classLoader);
            } catch (Throwable t) {
                log(TAG + "hook ActivityTaskManagerService failed " + t.getMessage());
            }
        }
    }

    @SuppressLint("PrivateApi")
    private void hookSecureScreenshot(XC_LoadPackage.LoadPackageParam lpparam) throws InvocationTargetException, IllegalAccessException {
        try {
            deoptimizeMethod(
                    lpparam.classLoader.loadClass("com.android.server.wm.WindowStateAnimator"),
                    "createSurfaceLocked");
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            log(t);
        }

        try {
            deoptimizeMethod(
                    lpparam.classLoader.loadClass("com.android.server.wm.WindowManagerService"),
                    "relayoutWindow");
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            log(t);
        }

        for (int i = 0; i < 20; i++) {
            try {
                var clazz = lpparam.classLoader.loadClass("com.android.server.wm.RootWindowContainer$$ExternalSyntheticLambda" + i);
                if (BiConsumer.class.isAssignableFrom(clazz)) {
                    deoptimizeMethod(clazz, "accept");
                }
            } catch (ClassNotFoundException ignored) {
            }
            try {
                var clazz = lpparam.classLoader.loadClass("com.android.server.wm.DisplayContent$$ExternalSyntheticLambda" + i);
                if (BiPredicate.class.isAssignableFrom(clazz)) {
                    deoptimizeMethod(clazz, "test");
                }
            } catch (ClassNotFoundException ignored) {
            }

        }

        try {
            Class<?> WindowState = lpparam.classLoader.loadClass("com.android.server.wm.WindowState");
            Method isSecureLockedMethod = WindowState.getDeclaredMethod("isSecureLocked");
            hookMethod(isSecureLockedMethod, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (mDisableSecure) param.setResult(false);
                }
            });
        } catch (ClassNotFoundException ignored) {
        } catch (Throwable t) {
            log(t);
        }
    }

    private void hookActivityTaskManagerService(ClassLoader classLoader) throws ClassNotFoundException, NoSuchMethodException {
        @SuppressLint("PrivateApi") var activityTaskManagerServiceClazz = classLoader.loadClass("com.android.server.wm.ActivityTaskManagerService");
        var iBinderClazz = classLoader.loadClass("android.os.IBinder");
        @SuppressLint("PrivateApi") var iScreenCaptureObserverClazz = classLoader.loadClass("android.app.IScreenCaptureObserver");
        var method = activityTaskManagerServiceClazz.getDeclaredMethod("registerScreenCaptureObserver", iBinderClazz, iScreenCaptureObserverClazz);
        final XC_MethodHook nullReturner = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (mDisableScreenshotObserver) param.setResult(null);
            }
        };
        hookMethod(method, nullReturner);
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(FRAMEWORK);
    }
}
