package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.FrameworkMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class OplusStartingWindowManager extends FrameworkMods {

    private boolean mEnableLagFix = false;
    private boolean mForceAllApps = false;
    private Set<String> mLagFixApps = new HashSet<>();

    public OplusStartingWindowManager(Context context) {
        super(context);
    }

    @Override
    protected void loadSettings() {
        mEnableLagFix = Xprefs.getBoolean("fix_lag_switch", false);
        mForceAllApps = Xprefs.getBoolean("fix_lag_force_all_apps", false);
        mLagFixApps = Xprefs.getStringSet("lag_fix_apps", null);

        log("Enabled Lag Fix: " + mEnableLagFix + " Force All Apps: " + mForceAllApps + " Lag Fix Apps: " + mLagFixApps);
    }

    @Override
    protected void onFrameworkLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        if (Build.VERSION.SDK_INT < 34 || Build.VERSION.SDK_INT >= 37) return;

        Class<?> OplusStartingWindowManager;
        Method isLayerMatchToStartingWindow;
        try {
            OplusStartingWindowManager = findClass("com.android.server.wm.OplusStartingWindowManager", PRParam.getClassLoader());
            isLayerMatchToStartingWindow = OplusStartingWindowManager.getDeclaredMethod("isLayerMatchToStartingWindow", String.class);

            ReflectedClass.of(isLayerMatchToStartingWindow.getDeclaringClass())
                    .before(isLayerMatchToStartingWindow)
                    .run(param -> {
                        if (!mEnableLagFix) return;
                        if (mForceAllApps) {
                            param.setResult(true);
                            return;
                        }
                        String layer = (String) param.args[0];
                        if (TextUtils.isEmpty(layer) || mLagFixApps == null) return;
                        for (String app : mLagFixApps) {
                            if (layer.contains(app)) {
                                param.setResult(true);
                                return;
                            }
                        }
                    });
        } catch (Throwable t) {
            log(t);
        }

    }
}
