package it.dhd.oxygencustomizer.utils;

import android.graphics.Color;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;

public final class DarkShadowUtils {

    public static final DarkShadowItem ACCENT =
            new DarkShadowItem("ACCENT", "Main Dark Accent", "",
                    new ArrayList<>() {{
                        add("accent_material_dark");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.RED
            );
    public static final DarkShadowItem ACCENT1 =
            new DarkShadowItem("ACCENT1", "Main Light Accent", "",
                    new ArrayList<>() {{
                        add("accent_material_light");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.CYAN
            );
    public static final DarkShadowItem ACCENT2 =
            new DarkShadowItem("ACCENT2", "Main Ripple Accent", "",
                    new ArrayList<>() {{
                        add("ripple_material_dark");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }},
                    Color.YELLOW
            );

//    public static final DarkShadowItem BACKGROUND =
//            new DarkShadowItem("BACKGROUND", "Background", "",
//                    new ArrayList<>() {{
//                        addAll(List.of("background_dark", "background_device_default_dark", "legacy_primary",
//                                "legacy_primary_dark",
//                                "primary_dark_material_dark", "primary_material_dark"));
//                    }},
//                    Map.of(
//                            "background_floating_material_dark", 10,
//                            "background_holo_dark", 25,
//                            "background_leanback_dark", 30,
//                            "button_material_dark", -5,
//                            "button_material_light", -15,
//                            "holo_light_primary_dark", -10,
//                            "holo_primary", -15,
//                            "holo_primary_dark", -5
//                    ),
//                    new ArrayList<>() {{
//                        add("android");
//                    }},
//                    Color.RED
//            );

    public static int getColor(DarkShadowItem darkShadowItem) {
        Log.w("DarkShadowUtils", "getColor: " + darkShadowItem.toString());
        return OCPreferences.getInt("DST" + darkShadowItem.getOverlayName(), darkShadowItem.getColor());
    }

    public static void saveColor(DarkShadowItem darkShadowItem) {
        Log.w("DarkShadowUtils", "saveColor: " + darkShadowItem.toString());
        OCPreferences.putInt("DST" + darkShadowItem.getOverlayName(), darkShadowItem.getColor());
    }

}
