package com.android.launcher3;

import java.util.List;

public class DeviceProfile {

    public interface DeviceProfileListenable {

    }

    public interface OnDeviceProfileChangeListener {
        void onDeviceProfileChanged(DeviceProfile deviceProfile);
    }


}
