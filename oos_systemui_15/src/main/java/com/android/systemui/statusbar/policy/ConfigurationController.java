package com.android.systemui.statusbar.policy;

import android.content.res.Configuration;

public interface ConfigurationController extends CallbackController {

    public interface ConfigurationListener {
        default void onConfigChanged(Configuration configuration) {
        }

        default void onDensityOrFontScaleChanged() {
        }

        default void onFontChanged(Configuration configuration) {
        }

        default void onFontWeightChanged() {
        }

        default void onLayoutDirectionChanged(boolean z) {
        }

        default void onLocaleListChanged() {
        }

        default void onMaxBoundsChanged() {
        }

        default void onOrientationChanged(int i2) {
        }

        default void onOverlayChanged() {
        }

        default void onSmallestScreenWidthChanged() {
        }

        default void onThemeChanged() {
        }

        default void onUiModeChanged() {
        }
    }

    String getNightModeName();

    boolean isLayoutRtl();

    void notifyThemeChanged();

    void onConfigurationChanged(Configuration configuration);
}
