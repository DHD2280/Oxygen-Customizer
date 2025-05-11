package com.oplus.systemui.plugins.qs;

public class DeviceProfile {

    public CellCalculator getCellCalculator() {
        throw new UnsupportedOperationException("Stub!");
    }

    public interface CellCalculator {

        int getCellSize();

        int getCellMarginHorizontal();

        int getCellMarginVertical();


    }

}
