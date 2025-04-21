package com.android.launcher3.util;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;

import androidx.annotation.NonNull;

import com.oplus.quickstep.navigation.NavigationController;

public class DisplayController implements ComponentCallbacks, SafeCloseable, NavigationController.ModeChangeListener {

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void onLowMemory() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void close() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public void onModeChange(NavigationMode navigationMode) {
        throw new UnsupportedOperationException("STUB");
    }

    public static final class NavigationMode {

    }

    public interface DisplayInfoChangeListener {
        void onDisplayInfoChanged(Context context, Info info, int i8);
    }

    public static class Info {
    }

}
