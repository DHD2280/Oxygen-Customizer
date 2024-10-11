package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.ArrayMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class DarkMode extends XposedMods {

    private static final String listenPackage = FRAMEWORK;
    private static final int[] mapping = {4, 3, 2, 1, 0};
    private boolean enableCustomDarkMode = false;
    private Set<String> darkModeApps = new HashSet<>();
    private boolean settingsUpdated = false;
    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action == null) return;
                String className = intent.getStringExtra("class");
                if (action.equals(Constants.ACTION_SETTINGS_CHANGED)) {
                    if (!TextUtils.isEmpty(className) && className.equals(DarkMode.class.getSimpleName())) {
                        log("DarkMode: Intent received - will update preferences");
                        settingsUpdated = false;
                        updatePrefs();
                    }
                }
            } catch (Throwable t) {
                log("Oxygen Customizer - DarkMode: " + t.getMessage());
            }
        }
    };
    private boolean broadcastRegistered = false;

    public DarkMode(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (settingsUpdated) return;

        enableCustomDarkMode = Xprefs.getBoolean("custom_dark_mode_switch", false);
        darkModeApps = Xprefs.getStringSet("custom_dark_mode", new HashSet<>());

        settingsUpdated = true;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!broadcastRegistered) {
            broadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_SETTINGS_CHANGED);
            mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }

        Class<?> OplusDarkModeServiceManager;
        Method updateList;
        try {
            OplusDarkModeServiceManager = findClass("com.android.server.OplusDarkModeServiceManager", lpparam.classLoader);
            final Class<?> OplusDarkModeData = findClass("com.oplus.darkmode.OplusDarkModeData", lpparam.classLoader);
            updateList = findMethodExact(OplusDarkModeServiceManager, "updateList", int.class);

            hookMethod(updateList, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!enableCustomDarkMode) return;
                    log("DarkMode: Hooked updateList");

                    Map<String, Integer> supportListMap = new ArrayMap<>();
                    for (String item : darkModeApps) {
                        if (item.contains("|")) {
                            List<String> arr = new ArrayList<>(Arrays.asList(item.split("\\|")));
                            if (arr.size() < 2 || arr.get(1).isBlank()) {
                                arr.set(1, "0");
                            }
                            supportListMap.put(arr.get(0), Integer.parseInt(arr.get(1)));
                        } else {
                            supportListMap.put(item, 0);
                        }
                    }

                    Map<String, Object> dataMap = new ArrayMap<>();
                    for (Map.Entry<String, Integer> entry : supportListMap.entrySet()) {
                        final Object darkModeObject = OplusDarkModeData.getConstructor().newInstance();
                        if (entry.getValue() == 0) {
                            dataMap.put(entry.getKey(), darkModeObject);
                        } else {
                            setObjectField(darkModeObject, "mCurType", mapping[entry.getValue()]);
                            dataMap.put(entry.getKey(), darkModeObject);
                        }
                    }

                    log("DarkMode: Hooked updateList (" + dataMap.size() + ")- setting mRusAppMap");

                    setObjectField(param.thisObject, "mRusAppMap", dataMap);
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
