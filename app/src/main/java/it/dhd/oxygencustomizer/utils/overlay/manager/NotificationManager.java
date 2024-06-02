package it.dhd.oxygencustomizer.utils.overlay.manager;

import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_NOTIFICATIONS;

import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class NotificationManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlayExclusiveInCategory("OxygenCustomizerComponentNFN" + n + ".overlay");
        if (PreferenceHelper.getModulePrefs() != null) {
            boolean hasOverlays = PreferenceHelper.getModulePrefs().getBoolean("hasNotificationOverlays", false);
            PreferenceHelper.getModulePrefs().edit().putBoolean("hasNotificationOverlays", true).apply();
            if (!hasOverlays) AppUtils.restartScope("systemui");
        }
        if (!OverlayUtil.isOverlayEnabled("OxygenCustomizerComponentCRN1.overlay") || !OverlayUtil.isOverlayEnabled("OxygenCustomizerComponentCRN1.overlay")) {
            OverlayUtil.enableOverlays("OxygenCustomizerComponentCRN1.overlay", "OxygenCustomizerComponentCR1.overlay");
        }
    }

    public static void disableOverlay(int n) {
        OverlayUtil.disableOverlay("OxygenCustomizerComponentNFN" + n + ".overlay");
        if (PreferenceHelper.getModulePrefs() != null) {
            boolean hasOverlays = PreferenceHelper.getModulePrefs().getBoolean("hasNotificationOverlays", false);
            PreferenceHelper.getModulePrefs().edit().putBoolean("hasNotificationOverlays", false).apply();
            if (hasOverlays) AppUtils.restartScope("systemui");
        }
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_NOTIFICATIONS; i++) {
            Prefs.putBoolean("OxygenCustomizerComponentNFN" + i + ".overlay", i == n);
        }
    }
}
