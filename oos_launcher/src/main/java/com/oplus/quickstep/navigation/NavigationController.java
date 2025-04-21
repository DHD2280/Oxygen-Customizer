package com.oplus.quickstep.navigation;

import com.android.launcher3.util.DisplayController;

public final class NavigationController {

    @FunctionalInterface
    public interface ModeChangeListener {
        void onModeChange(DisplayController.NavigationMode navigationMode);
    }


}
