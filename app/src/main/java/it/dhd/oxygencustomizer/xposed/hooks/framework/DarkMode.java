package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findMethodExact;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setObjectField;

import android.content.Context;
import android.util.ArrayMap;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.FrameworkMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class DarkMode extends FrameworkMods {

    private static final int[] mapping = {4, 3, 2, 1, 0};
    private boolean enableCustomDarkMode = false;
    private Set<String> darkModeApps = new HashSet<>();

    public DarkMode(Context context) {
        super(context);
    }

    @Override
    protected void loadSettings() {
        enableCustomDarkMode = Xprefs.getBoolean("custom_dark_mode_switch", false);
        darkModeApps = Xprefs.getStringSet("custom_dark_mode", new HashSet<>());
    }

    @Override
    protected void onFrameworkLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        Class<?> OplusDarkModeServiceManager;
        Method updateList;
        try {
            OplusDarkModeServiceManager = findClass("com.android.server.OplusDarkModeServiceManager", PRParam.getClassLoader());
            final Class<?> OplusDarkModeData = findClass("com.oplus.darkmode.OplusDarkModeData", PRParam.getClassLoader());
            updateList = findMethodExact(OplusDarkModeServiceManager, "updateList", int.class);

            ReflectedClass.of(updateList.getDeclaringClass())
                    .after(updateList)
                    .run(param -> {
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
                    });
        } catch (Throwable t) {
            log(t);
        }
    }
}
