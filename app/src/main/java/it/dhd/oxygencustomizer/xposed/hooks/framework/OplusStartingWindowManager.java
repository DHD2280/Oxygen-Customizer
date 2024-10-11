package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class OplusStartingWindowManager extends XposedMods {

    private final static String listenPackage = FRAMEWORK;

    private final static String TAG = "Oxygen Customizer - OplusStartingWindowManager: ";
    private final String LOG_FILE = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/OplusStartingWindowManager_fix.log";
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
                    if (!TextUtils.isEmpty(className) && className.equals(OplusStartingWindowManager.class.getSimpleName())) {
                        log("OplusStartingWindowManager: Intent received - will update preferences");
                        settingsUpdated = false;
                        updatePrefs();
                    }
                }
            } catch (Throwable t) {
                log("OplusStartingWindowManager: " + t.getMessage());
            }
        }
    };
    private boolean broadcastRegistered = false;

    public OplusStartingWindowManager(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        if (settingsUpdated) return;

        mEnableLagFix = Xprefs.getBoolean("fix_lag_switch", false);
        mForceAllApps = Xprefs.getBoolean("fix_lag_force_all_apps", false);
        mLagFixApps = Xprefs.getStringSet("lag_fix_apps", null);

        XPLauncher.enqueueProxyCommand((proxy) -> {
            proxy.runCommand("echo " + getFormattedDate() + " - updatePrefs: Enabled Lag Fix: " + mEnableLagFix + " Force All Apps: " + mForceAllApps + " Lag Fix Apps: " + mLagFixApps + " >> " + LOG_FILE);
        });

        settingsUpdated = true;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (Build.VERSION.SDK_INT < 34) return;

        XPLauncher.enqueueProxyCommand((proxy) ->
                proxy.runCommand("echo " + getFormattedDate() + " - OplusStartingWindowManager: hook started >> " + LOG_FILE));

        if (!broadcastRegistered) {
            broadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_SETTINGS_CHANGED);
            mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }


        Class<?> OplusStartingWindowManager;
        Method isLayerMatchToStartingWindow;
        try {
            OplusStartingWindowManager = findClass("com.android.server.wm.OplusStartingWindowManager", lpparam.classLoader);
            isLayerMatchToStartingWindow = OplusStartingWindowManager.getDeclaredMethod("isLayerMatchToStartingWindow", String.class);

            hookMethod(isLayerMatchToStartingWindow, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    XPLauncher.enqueueProxyCommand((proxy) ->
                            proxy.runCommand("echo " + getFormattedDate() + " - isLayerMatchToStartingWindow: " + param.args[0] + " >> " + LOG_FILE));
                    if (mEnableLagFix) {
                        XPLauncher.enqueueProxyCommand((proxy) ->
                                proxy.runCommand("echo " + getFormattedDate() + " - lag fix enabled >> " + LOG_FILE));
                        if (mForceAllApps) {
                            XPLauncher.enqueueProxyCommand((proxy) ->
                                    proxy.runCommand("echo " + getFormattedDate() + " - forced all apps, return ture >> " + LOG_FILE));
                            param.setResult(true);
                        } else {
                            String layer = (String) param.args[0];
                            if (TextUtils.isEmpty(layer)) {
                                XPLauncher.enqueueProxyCommand((proxy) ->
                                        proxy.runCommand("echo " + getFormattedDate() + " - layer is empty, return false >> " + LOG_FILE));
                                return;
                            }
                            if (mLagFixApps != null) {
                                mLagFixApps.forEach((app) -> {
                                    if (layer.contains(app)) {
                                        XPLauncher.enqueueProxyCommand((proxy) ->
                                                proxy.runCommand("echo " + getFormattedDate() + " - lag fix app found '" + app + "', return ture >> " + LOG_FILE));
                                        param.setResult(true);
                                    }
                                });
                            }
                        }
                    } else {
                        XPLauncher.enqueueProxyCommand((proxy) ->
                                proxy.runCommand("echo " + getFormattedDate() + " - lag fix disabled, do nothing >> " + LOG_FILE));
                    }
                }
            });
        } catch (Throwable t) {
            log(" error: " + t.getMessage());
        }


    }

    private String getFormattedDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss:SSS");
        Date now = new Date();
        return sdf.format(now);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
