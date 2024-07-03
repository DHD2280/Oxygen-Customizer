package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class DarkModeSettings extends XposedMods {

    private static final String listenPackage = SETTINGS;

    private boolean enableCustomDarkMode = false;
    private Set<String> darkModeApps = new HashSet<>();

    public DarkModeSettings(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        enableCustomDarkMode = Xprefs.getBoolean("custom_dark_mode_switch", false);
        darkModeApps = Xprefs.getStringSet("custom_dark_mode", new HashSet<>());
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> DarkModeFileUtils = findClassIfExists("com.oplus.settings.feature.display.darkmode.utils.DarkModeFileUtils", lpparam.classLoader);

        final Class<?> AppEntity = findClassIfExists("com.oplus.settings.feature.display.darkmode.utils.DarkModeFileUtils$AppEntity", lpparam.classLoader);

        if (DarkModeFileUtils != null) {
            hookAllMethods(DarkModeFileUtils, "parseManagedList", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!enableCustomDarkMode) return;

                    Map<String, Object> mEnabledApps = new ArrayMap<>();
                    for (String item : darkModeApps) {
                        final Object mAppEntity = AppEntity.getConstructor().newInstance();
                        if (item.contains("|")) {
                            List<String> arr = new ArrayList<>(Arrays.asList(item.split("\\|")));
                            if (arr.size() < 2 || arr.get(1).isBlank()) {
                                arr.set(1, "0");
                            }
                            setObjectField(mAppEntity, "curType", Integer.parseInt(arr.get(1)));
                            mEnabledApps.put(arr.get(0), mAppEntity);
                        } else {
                            mEnabledApps.put(item, mAppEntity);
                        }
                    }
                    setObjectField(param.thisObject, "mAppsMap", mEnabledApps);
                    param.setResult(null);
                }
            });
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
