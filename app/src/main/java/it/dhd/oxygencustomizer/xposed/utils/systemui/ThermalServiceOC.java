package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.os.Bundle;
import android.util.Log;

import com.oplus.epona.Epona;
import com.oplus.epona.Request;
import com.oplus.epona.Response;

import java.util.Map;

public class ThermalServiceOC {
    private static final String COMPONENT_NAME = "android.os.IThermalService";
    private static final String RESULT = "result";
    private static final String TAG = "ThermalServiceNative";

    private ThermalServiceOC() {
    }

    public static Object[] getCurrentTemperatures() {
            Request request;
            try {
                request = new Request.Builder().setComponentName(COMPONENT_NAME).setActionName("getCurrentTemperatures").build();
            } catch (Throwable ignored) {
                // Method build not available - 15.0.1
                request = new Request(new Bundle(), COMPONENT_NAME, "getCurrentTemperatures");
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
                Log.e(TAG, "getPowerSaveState: " + execute.getMessage());
            }

            return new TemperatureNative[0];
    }
}
