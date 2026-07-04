package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Context;
import android.os.Build;

import de.robv.android.xposed.XC_MethodHook;
import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class QsStyleObserver extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    public boolean isSeparateStyle = false;

    private static Object OplusQsStyle = null;

    public QsStyleObserver(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return;

        Class<?> OplusSeparateNotificationAndQSState = findClass("com.oplus.systemui.separate.OplusSeparateNotificationAndQSState", PRParam.getClassLoader());
        hookAllConstructors(OplusSeparateNotificationAndQSState, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                OplusQsStyle = param.thisObject;
            }
        });

    }

    public static boolean isSeparateStyle() {
        if (OplusQsStyle == null) return false;
        return (boolean) callMethod(OplusQsStyle, "getSeparateNotificationAndQSStateBySettings");
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
