package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static android.content.Context.RECEIVER_EXPORTED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class OplusStartingWindowManager extends XposedMods {

    private final static String listenPackage = FRAMEWORK;

    private boolean mEnableLagFix = false;
    private boolean mForceAllApps = false;
    private Set<String> mLagFixApps = new HashSet<>();
    private boolean settingsUpdated = false;
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action == null) return;
                String className = intent.getStringExtra("class");
                if (action.equals(Constants.ACTION_SETTINGS_CHANGED)) {
                    if (!TextUtils.isEmpty(className) && className.equals(this.getClass().getSimpleName())) {
                        log("Intent received - will update preferences");
                        settingsUpdated = false;
                        onPreferenceUpdated();
                    }
                }
            } catch (Throwable t) {
                log(t);
            }
        }
    };
    private boolean broadcastRegistered = false;

    public OplusStartingWindowManager(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {

        if (settingsUpdated) return;

        mEnableLagFix = Xprefs.getBoolean("fix_lag_switch", false);
        mForceAllApps = Xprefs.getBoolean("fix_lag_force_all_apps", false);
        mLagFixApps = Xprefs.getStringSet("lag_fix_apps", null);

        log("Enabled Lag Fix: " + mEnableLagFix + " Force All Apps: " + mForceAllApps + " Lag Fix Apps: " + mLagFixApps);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        if (Build.VERSION.SDK_INT < 34) return;

        if (!broadcastRegistered) {
            broadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_SETTINGS_CHANGED);
            mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }


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

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
