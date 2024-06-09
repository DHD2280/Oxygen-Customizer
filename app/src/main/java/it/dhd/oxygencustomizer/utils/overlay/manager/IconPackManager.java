package it.dhd.oxygencustomizer.utils.overlay.manager;

import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_ICON_PACKS;

import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class IconPackManager {

    public static void enableOverlay(int n) {
        disable_others(n);
        OverlayUtil.enableOverlayExclusiveInCategory(

                "OxygenCustomizerComponentIPSUI" + n + ".overlay"
        );
    }

    public static void disableOverlay(int n) {
        Prefs.putBoolean("OxygenCustomizerComponentIPSUI" + n + ".overlay", false);
        OverlayUtil.disableOverlay("OxygenCustomizerComponentIPSUI" + n + ".overlay");
    }

    private static void disable_others(int n) {
        for (int i = 1; i <= TOTAL_ICON_PACKS; i++) {
            Prefs.putBoolean("OxygenCustomizerComponentIPAS" + i + ".overlay", i == n);
            Prefs.putBoolean("OxygenCustomizerComponentIPSUI" + i + ".overlay", i == n);
        }
    }
}
