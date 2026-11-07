package it.dhd.oxygencustomizer.xposed;

import android.content.res.Resources;

public class ResourceManager {

    private static String MODULE_PATH = null;

    /**
     * Resources of the module apk, loaded in the hooked process.
     */
    public static Resources modRes;

    public static void setModulePath(String modulePath) {
        MODULE_PATH = modulePath;
    }

    public static String getModulePath() {
        return MODULE_PATH;
    }

}
