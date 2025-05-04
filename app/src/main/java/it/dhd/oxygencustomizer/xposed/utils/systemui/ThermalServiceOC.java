package it.dhd.oxygencustomizer.xposed.utils.systemui;

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
            Response execute = Epona.newCall(new Request.Builder().setComponentName(COMPONENT_NAME).setActionName("getCurrentTemperatures").build()).execute();
            int i2 = 0;
            if (execute.isSuccessful()) {
                Map<String, Float> map = (Map) execute.getBundle().getSerializable("result");
                TemperatureNative[] temperatureNativeArr = new TemperatureNative[map.size()];
                for (Map.Entry<String, Float> entry : map.entrySet()) {
                    temperatureNativeArr[i2] = new TemperatureNative((String) entry.getKey(), ((Float) entry.getValue()).floatValue());
                    i2++;
                }
                return temperatureNativeArr;
            }
            Log.e(TAG, "getPowerSaveState: " + execute.getMessage());
            return new TemperatureNative[0];
    }
}
