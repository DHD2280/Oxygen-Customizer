package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_MEMC_FEATURE_GET;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_MEMC_FEATURE_RECEIVED;
import static it.dhd.oxygencustomizer.utils.Constants.OPLUS_MEMC_FEATURES;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class MemcEnhancer extends XposedMods {

    private static final String listenPackage = FRAMEWORK;
    private static final ArrayList<String> sConfigPackage = new ArrayList<>();
    private static final ArrayList<String> sConfigActivity = new ArrayList<>();
    private static final HashMap<String, String> sSdr2hdrCommandMap = new HashMap<>();
    private static final HashMap<String, String> sMemcCommandMap = new HashMap<>();
    private static final HashMap<String, String> sAppScreenRateMap = new HashMap<>();
    private boolean mBroadcastRegistered = false;
    private boolean mSettingsBroadcastRegistered = false;
    private boolean settingsUpdated = false;
    private boolean mIsFeatureOn = false;
    private boolean mIsPwX7Enable = false;
    private boolean mMemcEnable = false;
    private boolean mSdr2hdrEnable = false;
    private boolean mVideoOsieSupport = false;
    private Object mOplusFeatureMEMC = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, android.content.Intent intent) {
            if (intent.getAction() == null) return;
            if (intent.getAction().equals(ACTIONS_MEMC_FEATURE_GET)) {
                getAndSendBroadcast();
            }
        }
    };
    private boolean enableMemcFeature = false;
    private boolean useCustomMemcConfig = false;
    final BroadcastReceiver mSettingsBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action == null) return;
                String className = intent.getStringExtra("class");
                if (action.equals(Constants.ACTION_SETTINGS_CHANGED)) {
                    if (!TextUtils.isEmpty(className) && className.equals(MemcEnhancer.class.getSimpleName())) {
                        log("Intent received - will update preferences");
                        settingsUpdated = false;
                        updatePrefs();
                    }
                }
            } catch (Throwable t) {
                log("error: " + t.getMessage());
            }
        }
    };

    public MemcEnhancer(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (settingsUpdated) return;

        enableMemcFeature = Xprefs.getBoolean("force_memc_enabled", false);
        useCustomMemcConfig = Xprefs.getBoolean("custom_memc_config", false);

        clearLists();

        Set<String> memcConfig = Xprefs.getStringSet("custom_memc_applications", new HashSet<>());
        for (String config : memcConfig) {
            String pkg = config.split("\\|")[0];
            String confs = config.split("\\|")[1];
            String refreshRate = confs.split("\\$")[0];
            String memcConf = confs.split("\\$")[1];
            sConfigPackage.add(pkg);
            sAppScreenRateMap.put(pkg, refreshRate);
            sSdr2hdrCommandMap.put(pkg, memcConf);
        }

        Set<String> memcActivityConfig = Xprefs.getStringSet("custom_memc_activities", new HashSet<>());
        for (String config : memcActivityConfig) {
            String pkg = config.split("\\|")[0];
            String confs = config.split("\\|")[1];
            sConfigActivity.add(pkg.substring(pkg.indexOf("/") + 1));
            sMemcCommandMap.put(pkg, confs);
        }

        logLists();

        settingsUpdated = true;
    }

    private void clearLists() {
        sConfigPackage.clear();
        sConfigActivity.clear();
        sSdr2hdrCommandMap.clear();
        sMemcCommandMap.clear();
        sAppScreenRateMap.clear();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!mBroadcastRegistered) {
            mBroadcastRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTIONS_MEMC_FEATURE_GET);
            mContext.registerReceiver(mBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        }
        if (!mSettingsBroadcastRegistered) {
            mSettingsBroadcastRegistered = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction(Constants.ACTION_SETTINGS_CHANGED);
            mContext.registerReceiver(mSettingsBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);
        }

        hookFeatureManager(lpparam);
        hookSystemProperties(lpparam);
        hookOplusFeatureMEMC(lpparam);
        hookOplusMemcHelper(lpparam);

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void getAndSendBroadcast() {
        if (mOplusFeatureMEMC != null) {
            mIsFeatureOn = getBooleanField(mOplusFeatureMEMC, "mIsFeatureOn");
            mIsPwX7Enable = getBooleanField(mOplusFeatureMEMC, "mIsPwX7Enable");
            mMemcEnable = getBooleanField(mOplusFeatureMEMC, "mMemcEnable");
            mSdr2hdrEnable = getBooleanField(mOplusFeatureMEMC, "mSdr2hdrEnable");
            mVideoOsieSupport = getBooleanField(mOplusFeatureMEMC, "mVideoOsieSupport");
        }
        Intent intent = new Intent(ACTIONS_MEMC_FEATURE_RECEIVED);
        intent.putExtra("feature_on", mIsFeatureOn);
        intent.putExtra("pw_x7_enable", mIsPwX7Enable);
        intent.putExtra("memc_enable", mMemcEnable);
        intent.putExtra("sdr2hdr_enable", mSdr2hdrEnable);
        intent.putExtra("video_osie_support", mVideoOsieSupport);
        mContext.sendBroadcast(intent);
    }

    private void hookFeatureManager(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> OplusFeatureManager;
        OplusFeatureManager = findClassIfExists("com.oplus.content.OplusFeatureConfigManager", lpparam.classLoader);

        if (OplusFeatureManager == null) {
            log("OplusFeatureManager not found");
            return;
        }

        hookAllMethods(OplusFeatureManager, "hasFeature", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    /*
                        oplus.software.display.pixelworks_enable
                        oplus.software.display.iris_enable
                        oplus.software.display.memc_enable
                        oplus.software.display.game.memc_enable
                     */
                String requestedFeature = (String) param.args[0];
                if (OPLUS_MEMC_FEATURES.contains(requestedFeature) && enableMemcFeature) {
                    log("hasFeature: " + param.args[0] + " called, returning true");
                    param.setResult(true);
                }
            }
        });
    }

    private void hookSystemProperties(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> SystemProperties;
        SystemProperties = findClassIfExists("android.os.SystemProperties", lpparam.classLoader);

        if (SystemProperties == null) {
            log("SystemProperties not found");
            return;
        }

        hookAllMethods(SystemProperties, "getBoolean", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!enableMemcFeature) return;
                if (param.args[0].equals("ro.oplus.display.memc_video_refreshrate") ||
                        param.args[0].equals("vendor.display.show_memc_tomast")) {
                    log("get: " + param.args[0] + " called, returning true");
                    param.setResult(true);
                }
            }
        });
    }

    private void hookOplusFeatureMEMC(XC_LoadPackage.LoadPackageParam lpparam) {
        Class<?> OplusFeatureMEMC;
        OplusFeatureMEMC = findClassIfExists("com.android.server.display.OplusFeatureMEMC", lpparam.classLoader);

        if (OplusFeatureMEMC == null) {
            log("OplusFeatureMEMC not found");
            return;
        }

        hookAllConstructors(OplusFeatureMEMC, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mOplusFeatureMEMC = param.thisObject;
                mIsFeatureOn = getBooleanField(param.thisObject, "mIsFeatureOn");
                mIsPwX7Enable = getBooleanField(param.thisObject, "mIsPwX7Enable");
                mMemcEnable = getBooleanField(param.thisObject, "mMemcEnable");
                mSdr2hdrEnable = getBooleanField(param.thisObject, "mSdr2hdrEnable");
                mVideoOsieSupport = getBooleanField(param.thisObject, "mVideoOsieSupport");
            }
        });

    }

    private void hookOplusMemcHelper(XC_LoadPackage.LoadPackageParam lpparam) {

        Class<?> OplusMemcHelper = findClassIfExists("com.android.server.display.memc.OplusMemcHelper", lpparam.classLoader);

        hookAllMethods(OplusMemcHelper, "getConfigAppList", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!useCustomMemcConfig) return;
                log("getConfigAppList called");
                param.setResult(sConfigPackage);
            }
        });

        hookAllMethods(OplusMemcHelper, "getConfigActivityList", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!useCustomMemcConfig) return;
                log("getConfigActivityList called");
                param.setResult(sConfigActivity);
            }
        });

        hookAllMethods(OplusMemcHelper, "getSdr2hdrCommandMap", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!useCustomMemcConfig) return;
                log("getSdr2hdrCommandMap called");
                param.setResult(sSdr2hdrCommandMap);
            }
        });

        hookAllMethods(OplusMemcHelper, "getMemcCommandMap", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!useCustomMemcConfig) return;
                log("getMemcCommandMap called");
                param.setResult(sMemcCommandMap);
            }
        });

        hookAllMethods(OplusMemcHelper, "getAppScreenRateMap", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!useCustomMemcConfig) return;
                log("getAppScreenRateMap called");
                param.setResult(sAppScreenRateMap);
            }
        });

        /*
        public boolean isInConfigpackageList(String packageName) {
            Iterator<String> it = sConfigPackage.iterator();
            while (it.hasNext()) {
                String name = it.next();
                if (packageName.equals(name)) {
                    return true;
                }
            }
            return false;
        }
         */

        hookAllMethods(OplusMemcHelper, "isInConfigpackageList", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!useCustomMemcConfig) return;
                String packageName = (String) param.args[0];
                log("isInConfigpackageList called with: " + packageName + " returning: " + sConfigPackage.contains(packageName));
                param.setResult(sConfigPackage.contains(packageName));
            }
        });
    }

    private void logLists() {
        log("logLists: " + "\n" +
                "enableMemcFeature: " + enableMemcFeature + "\n" +
                "useCustomMemcConfig: " + useCustomMemcConfig + "\n" +
                "sConfigPackage: " + Arrays.toString(sConfigPackage.toArray()) + "\n" +
                "sConfigActivity: " + Arrays.toString(sConfigActivity.toArray()) + "\n" +
                "sSdr2hdrCommandMap: " + sSdr2hdrCommandMap.toString() + "\n" +
                "sMemcCommandMap: " + sMemcCommandMap.toString() + "\n" +
                "sAppScreenRateMap: " + sAppScreenRateMap.toString());
    }

}
