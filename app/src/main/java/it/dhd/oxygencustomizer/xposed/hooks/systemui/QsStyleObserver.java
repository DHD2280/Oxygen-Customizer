package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findMethodExactIfExists;

import android.content.Context;
import android.os.Build;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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
        ReflectedClass.of(OplusSeparateNotificationAndQSState)
                .afterConstruction()
                .run(param -> {
                    OplusQsStyle = param.thisObject;
                });

    }

    public static boolean isSeparateStyle() {
        if (OplusQsStyle == null) return false;
        if (findMethodExactIfExists(OplusQsStyle.getClass(), "getEnableSeparateNotificationAndQS") != null) {
            Object stateFlow = callMethod(OplusQsStyle, "getEnableSeparateNotificationAndQS");
            return Boolean.TRUE.equals(callMethod(stateFlow, "getValue"));
        }
        return (boolean) callMethod(OplusQsStyle, "getSeparateNotificationAndQSStateBySettings");
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
