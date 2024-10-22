package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.SplashActivity.SKIP_INSTALLATION;

import android.os.Environment;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.OxygenCustomizer;

public class ModuleConstants {

    public final static String MODULE_VERSION_NAME = "0.0.44";
    public final static int MODULE_VERSION_CODE = 1;

    // Storage location
    public static final String DOCUMENTS_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath();
    public static final String DOWNLOADS_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath();
    public static final String LOG_DIR = DOCUMENTS_DIR + "/OxygenCustomizer";
    public static final String MODULE_DIR = "/data/adb/modules/OxygenCustomizer";
    public static final String SYSTEM_OVERLAY_DIR = "/system/product/overlay";
    public static final String DATA_DIR = OxygenCustomizer.getAppContext().getFilesDir().getAbsolutePath();
    public static final String OVERLAY_DIR = MODULE_DIR + "/system/product/overlay";
    public static final String BIN_DIR = OxygenCustomizer.getAppContext().getDataDir() + "/bin";
    public static final String BACKUP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.oc_backup";
    public static final String TEMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/.oc";
    public static final String TEMP_MODULE_DIR = TEMP_DIR + "/OxygenCustomizer";
    public static final String TEMP_MODULE_OVERLAY_DIR = TEMP_MODULE_DIR + "/system/product/overlay";
    public static final String TEMP_OVERLAY_DIR = TEMP_DIR + "/overlays";
    public static final String TEMP_CACHE_DIR = TEMP_OVERLAY_DIR + "/cache";
    public static final String UNSIGNED_UNALIGNED_DIR = TEMP_OVERLAY_DIR + "/unsigned_unaligned";
    public static final String UNSIGNED_DIR = TEMP_OVERLAY_DIR + "/unsigned";
    public static final String SIGNED_DIR = TEMP_OVERLAY_DIR + "/signed";
    public static final String COMPANION_TEMP_DIR = TEMP_DIR + "/companion";
    public static final String COMPANION_COMPILED_DIR = COMPANION_TEMP_DIR + "/compiled";
    public static final String COMPANION_MODULE_DIR = TEMP_DIR + "/module/OxygenCustomizerCompanion";
    public static final String COMPANION_RES_DIR = COMPANION_MODULE_DIR + "/substratumXML/SystemUI/res";
    public static final String COMPANION_DRAWABLE_DIR = COMPANION_RES_DIR + "/drawable";
    public static final String COMPANION_LAYOUT_DIR = COMPANION_RES_DIR + "/layout";
    public static final String MY_PRODUCT_DIR = TEMP_MODULE_DIR + "/my_product";
    public static final String MY_PRODUCT_ETC_DIR = MY_PRODUCT_DIR + "/etc";
    public static final String MY_PRODUCT_EXTENSION_DIR = MY_PRODUCT_ETC_DIR + "/extension";
    public static final String MY_PRODUCT_PERMISSIONS_DIR = MY_PRODUCT_DIR + "/permissions";

    public static final String XPOSED_ONLY_MODE = "OCXposedOnlyMode";
    public static boolean isXposedOnlyMode = Prefs.getBoolean(XPOSED_ONLY_MODE, true) &&
            !SKIP_INSTALLATION;

    // Module script
    public static final String MAGISK_UPDATE_BINARY = """
            #!/sbin/sh
                        
            #################
            # Initialization
            #################
                        
            umask 022
                        
            # echo before loading util_functions
            ui_print() { echo "$1"; }
                        
            require_new_magisk() {
              ui_print "*******************************"
              ui_print " Please install Magisk v20.4+! "
              ui_print "*******************************"
              exit 1
            }
                        
            #########################
            # Load util_functions.sh
            #########################
                        
            OUTFD=$2
            ZIPFILE=$3
                        
            mount /data 2>/dev/null
                        
            [ -f /data/adb/magisk/util_functions.sh ] || require_new_magisk
            . /data/adb/magisk/util_functions.sh
            [ $MAGISK_VER_CODE -lt 20400 ] && require_new_magisk
                        
            install_module
            exit 0
            """;
    // Fragment variables
    public static final int TRANSITION_DELAY = 120;
    public static final int FRAGMENT_BACK_BUTTON_DELAY = 50;
    public static final int SWITCH_ANIMATION_DELAY = 300;

    // Overlay metadata
    public static final String METADATA_OVERLAY_PARENT = "OVERLAY_PARENT";
    public static final String METADATA_OVERLAY_TARGET = "OVERLAY_TARGET";
    public static final String METADATA_THEME_VERSION = "THEME_VERSION";
    public static final String METADATA_THEME_CATEGORY = "THEME_CATEGORY";

    // Overlay categories
    public static final String OVERLAY_CATEGORY_PREFIX = BuildConfig.APPLICATION_ID.replace(".debug", "") + ".category.";

    // File resources
    public static final String FRAMEWORK_DIR = "/system/framework/framework-res.apk";

}
