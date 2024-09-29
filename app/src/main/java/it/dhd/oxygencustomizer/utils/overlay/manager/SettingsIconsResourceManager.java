package it.dhd.oxygencustomizer.utils.overlay.manager;

import java.io.IOException;

import it.dhd.oxygencustomizer.utils.overlay.compiler.SettingsIconsCompiler;

public class SettingsIconsResourceManager {

    public static boolean buildOverlay(
            int iconSet,
            int backgroundColor,
            int backgroundShape,
            boolean backgroundSolid,
            int iconColor,
            boolean force
    ) throws IOException {
        String resources = "";

        if (iconSet == 1 || iconSet == 2 || iconSet == 4) {
            // PUI Icon Pack
            resources += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<resources>\n" +
                    "    <color name=\"monet_color\">" +
                    getIconColor(iconColor) +
                    "</color>\n";

        } else {
            resources += "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                    "<resources>\n" +
                    "    <color name=\"bg_color\">" +
                    getBackgroundColor(backgroundColor) +
                    "</color>\n";

            resources += "    <color name=\"solid_bg_color\">" +
                    (backgroundSolid ? getBackgroundColor(backgroundColor) : "#00000000") +
                    "</color>\n";

            resources += "    <color name=\"icon_color\">" +
                    getIconColor(iconColor) +
                    "</color>\n";

            switch (backgroundShape) {
                // Circle
                case 0 -> resources += """
                            <dimen name="top_left">18dp</dimen>
                            <dimen name="top_right">18dp</dimen>
                            <dimen name="bottom_left">18dp</dimen>
                            <dimen name="bottom_right">18dp</dimen>
                        """;
                // Squircle
                case 1 -> resources += """
                            <dimen name="top_left">15dp</dimen>
                            <dimen name="top_right">15dp</dimen>
                            <dimen name="bottom_left">15dp</dimen>
                            <dimen name="bottom_right">15dp</dimen>
                        """;
                // Rounded Square
                case 2 -> resources += """
                            <dimen name="top_left">4.0dip</dimen>
                            <dimen name="top_right">4.0dp</dimen>
                            <dimen name="bottom_left">4.0dp</dimen>
                            <dimen name="bottom_right">4.0dp</dimen>
                        """;
                // Teardrop
                case 3 -> resources += """
                            <dimen name="top_left">90.0dp</dimen>
                            <dimen name="top_right">90.0dp</dimen>
                            <dimen name="bottom_left">90.0dp</dimen>
                            <dimen name="bottom_right">24.0dp</dimen>
                        """;
                // Rhombus
                // <corners android:topLeftRadius="2.0dp" android:topRightRadius="14.0dp" android:bottomLeftRadius="14.0dp" android:bottomRightRadius="2.0dp"/>
                case 4 -> resources += """
                            <dimen name="top_left">2.0dp</dimen>
                            <dimen name="top_right">14.0dp</dimen>
                            <dimen name="bottom_left">14.0dp</dimen>
                            <dimen name="bottom_right">2.0dp</dimen>
                        """;
            }
        }

        resources += "</resources>";

        return SettingsIconsCompiler.buildOverlay(iconSet, resources, force);
    }

    private static String getIconColor(int iconColor) {
        return switch (iconColor) {
            case 0 -> "@*android:color/system_accent1_600";
            case 1 -> "#FFFFFF";
            default -> "#000000";
        };
    }

    private static String getBackgroundColor(int backgroundColor) {
        return switch (backgroundColor) {
            case 0 -> "@*android:color/system_accent1_600";
            case 1 -> "#FFFFFF";
            default -> "#000000";
        };
    }

}
