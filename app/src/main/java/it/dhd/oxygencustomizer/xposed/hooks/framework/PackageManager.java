package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;

import android.content.Context;
import android.os.Binder;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

/**
 * Credits Siavash79/PixelXpert
 */
public class PackageManager extends XposedMods {

    private static final String listenPackage = FRAMEWORK;

    private static final int PERMISSION_GRANTED = 0;

    public PackageManager(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        try {
            ReflectedClass ActivityManagerServiceClass = ReflectedClass.of("com.android.server.am.ActivityManagerService", lpparam.classLoader);

            //Granting oplus launcher permission to force stop apps
            ActivityManagerServiceClass
                    .before("checkCallingPermission")
                    .run(param -> {
                        try {
                            if ("android.permission.FORCE_STOP_PACKAGES".equals(param.args[0])) {
                                if (LAUNCHER.equals(
                                        callMethod(
                                                getObjectField(param.thisObject, "mInternal"),
                                                "getPackageNameByPid",
                                                Binder.getCallingPid()))) {
                                    param.setResult(PERMISSION_GRANTED);
                                }
                            }
                        } catch (Throwable ignored) {
                        }
                    });

        } catch (Throwable t) {
            log(t);
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
