package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.os.Bundle;
import android.util.Log;

import com.oplus.epona.Epona;
import com.oplus.epona.Request;
import com.oplus.epona.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import de.robv.android.xposed.XposedBridge;

public class ThermalServiceOC {

    private static final String COMPONENT_NAME = "android.os.IThermalService";
    private static final String ACTION_NAME = "getCurrentTemperatures";
    private static final String RESULT = "result";
    private static final String TAG = "ThermalServiceOC";
    private static final List<RequestFactory> factories = Arrays.asList(
            () -> new Request.Builder()
                    .setComponentName(COMPONENT_NAME)
                    .setActionName(ACTION_NAME)
                    .build(),
            () -> new Request(new Bundle(), COMPONENT_NAME, ACTION_NAME),
            () -> new Request(COMPONENT_NAME, ACTION_NAME, new Bundle(), null)
    );

    private ThermalServiceOC() {
    }

    interface RequestFactory {
        Request create() throws Throwable;
    }

    public static Object[] getCurrentTemperatures() {
        Request request = null;
        for (RequestFactory factory : factories) {
            try {
                request = factory.create();
                break; // Everything is fine
            } catch (Throwable tr) {
                logE("getCurrentTemperatures error", tr);
            }
        }
        if (request == null) {
            logE("getCurrentTemperatures: No request factory available!", new Throwable("No request factory available!"));
            return new Object[0];
        }
        Response execute = Epona.newCall(request).execute();
        int i2 = 0;
        try {
            Map<String, Float> map = (Map) execute.getBundle().getSerializable("result");
            TemperatureNative[] temperatureNativeArr = new TemperatureNative[map.size()];
            for (Map.Entry<String, Float> entry : map.entrySet()) {
                temperatureNativeArr[i2] = new TemperatureNative((String) entry.getKey(), ((Float) entry.getValue()).floatValue());
                i2++;
            }
            return temperatureNativeArr;
        } catch (Throwable t) {
            logE("getPowerSaveState: " + execute.getMessage(), t);
        }

        return new TemperatureNative[0];
    }

    private static void logE(String message, Throwable throwable) {
        try {
            XposedBridge.log(TAG + " " + message + ": " + Log.getStackTraceString(throwable));
        } catch (Throwable ignored) {
        }
        Log.e(TAG, message, throwable);
    }

}
