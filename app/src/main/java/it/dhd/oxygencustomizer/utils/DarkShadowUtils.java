package it.dhd.oxygencustomizer.utils;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;

public final class DarkShadowUtils {

    public static final DarkShadowItem ACCENT =
            new DarkShadowItem("ACCENT1", "Accent 1", "",
                    new ArrayList<>() {{
                        add("accent_material_dark");
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }}
            );

    public static final DarkShadowItem BACKGROUND =
            new DarkShadowItem("BACKGROUND", "Background", "",
                    new ArrayList<>() {{
                        addAll(List.of("background_dark", "background_device_default_dark", "legacy_primary",
                                "legacy_primary_dark",
                                "primary_dark_material_dark", "primary_material_dark"));
                    }},
                    new ArrayList<>() {{
                        add("android");
                    }}
            );

}
