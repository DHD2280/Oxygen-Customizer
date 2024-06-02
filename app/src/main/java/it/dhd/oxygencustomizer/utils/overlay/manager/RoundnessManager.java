package it.dhd.oxygencustomizer.utils.overlay.manager;

import java.io.IOException;

import it.dhd.oxygencustomizer.utils.overlay.compiler.RoundnessCompiler;

public class RoundnessManager {

    public static boolean buildOverlay(int cornerRadius, boolean force) throws IOException {

        String framework_resources = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n<resources>\n" +
                "    <dimen name=\"harmful_app_name_padding_top\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_right\">" + Math.max((cornerRadius - 2), 0) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_left\">" + Math.max((cornerRadius - 4), 0) + "dip</dimen>\n" +
                "    <dimen name=\"harmful_app_name_padding_bottom\">" + Math.max((cornerRadius - 6), 0) + "dip</dimen>\n" +
                "</resources>\n";

        String sysui_resources = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n" +
                "    <dimen name=\"global_actions_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "    <dimen name=\"notification_corner_radius\">" + cornerRadius + "dip</dimen>\n" +
                "</resources>";

        return RoundnessCompiler.buildOverlay(new String[]{framework_resources, sysui_resources}, force);
    }
}
