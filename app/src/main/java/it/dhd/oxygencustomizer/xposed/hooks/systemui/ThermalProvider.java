package it.dhd.oxygencustomizer.xposed.hooks.systemui;


import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.util.Arrays;

import de.robv.android.xposed.XposedBridge;
import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.systemui.ThermalServiceOC;

public class ThermalProvider extends XposedMods {
    public static final int CPU = 0;
    public static final int GPU = 1;
    public static final int BATTERY = 2;
    public static final int SKIN = 3;
    private static final String listenPackage = SYSTEM_UI;
    static Class<?> ThermalServiceNative = null;
    private static String container = "";
    private static boolean TemperatureUnitF = false;

    public ThermalProvider(Context context) {
        super(context);
    }

    public static float getTemperatureMaxFloat(int type) {
        Object[] temperatures = getTemperatures();

        container = "";

        switch (type) {
            case CPU:
                container = "CPU";
                break;
            case GPU:
                container = "GPU";
                break;
            case BATTERY:
                container = "battery";
                break;
            case SKIN:
                container = "skin";
                break;
        }

        final float[] maxValue = {-999};

        Arrays
                .stream(temperatures)
                .filter(temperature -> {
                    String mName = (String) getObjectField(temperature, "mName");
                    return mName.toLowerCase().contains(container.toLowerCase());
                })
                .forEach(temperature -> maxValue[0] = Math.max(maxValue[0], getFloatField(temperature, "mValue")));

        if (TemperatureUnitF) {
            maxValue[0] = toFahrenheit(maxValue[0]);
        }

        return maxValue[0];
    }

    public static int getTemperatureMaxInt(int type) {
        return Math.round(getTemperatureMaxFloat(type));
    }

    public static float getTemperatureAvgFloat(int type) {
        Object[] temperatures = getTemperatures();

        if (temperatures.length > 0) {
            final float[] totalValue = {0};
            Arrays.stream(temperatures).forEach(temperature -> totalValue[0] += getFloatField(temperature, "mValue"));

            float ret = totalValue[0] / temperatures.length;

            if (TemperatureUnitF) {
                ret = toFahrenheit(ret);
            }

            return ret;
        }
        return -999;
    }

    public static int getTemperatureAvgInt(int type) {
        return Math.round(getTemperatureAvgFloat(type));
    }

    private static Object[] getTemperatures() {
        if (Build.VERSION.SDK_INT >= 35) {
            try {
            return ThermalServiceOC.getCurrentTemperatures();
            } catch (Throwable t) {
                try {
                    XposedBridge.log("ThermalProvider - error getting temperature from ThermalServiceOC " + Log.getStackTraceString(t));
                } catch (Throwable ignored) {} // executed outside xposed
                Log.e("ThermalProvider", "getTemperatures error", t);
                return new Object[0];
            }
        }
        try {

            Object[] temps = (Object[]) callStaticMethod(ThermalServiceNative, "getCurrentTemperatures");
            if (BuildConfig.DEBUG) {
                for (Object temp : temps) {
                    if (temp != null) {
                        float value = getFloatField(temp, "mValue");
                        String name = (String) getObjectField(temp, "mName");
                        XposedBridge.log("Temperature: " + name + " = " + value);
                    }
                }
            }
            return temps;
        } catch (Throwable t) {
            Log.e("ThermalProvider", "getTemperatures error", t);
            return new Object[0];
        }
    }

    private static float toFahrenheit(float celsius) {
        return (celsius * 1.8f) + 32f;
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        TemperatureUnitF = Xprefs.getBoolean("TemperatureUnitF", false);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        try {
            ThermalServiceNative = findClass("com.oplus.compat.os.ThermalServiceNative", PRParam.getClassLoader());
        } catch (Throwable t) {
            log("Error in ThermalProvider: " + t);
        }
    }
}
