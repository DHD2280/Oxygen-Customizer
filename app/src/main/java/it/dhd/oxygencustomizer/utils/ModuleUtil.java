package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.utils.Constants.OPLUS_FEATURE_XML;
import static it.dhd.oxygencustomizer.utils.Constants.OPLUS_MEMC_FEATURES;
import static it.dhd.oxygencustomizer.utils.Constants.OPLUS_POCKET_STUDIO_FEATURE;
import static it.dhd.oxygencustomizer.utils.Dynamic.skippedInstallation;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MODULE_VERSION_CODE;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MODULE_VERSION_NAME;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MY_PRODUCT_EXTENSION_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MY_PRODUCT_PERMISSIONS_DIR;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getStringFromOverlay;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.CompressionLevel;
import net.lingala.zip4j.model.enums.CompressionMethod;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.utils.helper.BackupRestore;
import it.dhd.oxygencustomizer.utils.helper.BinaryInstaller;
import it.dhd.oxygencustomizer.utils.overlay.FabricatedUtil;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class ModuleUtil {

    private static final String TAG = ModuleUtil.class.getSimpleName();

    public static void handleModule() {
        if (moduleExists()) {
            // Clean temporary directory
            Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();

            // Backup necessary files
            BackupRestore.backupFiles();
        }
        installModule();
    }

    static void installModule() {
        Log.d(TAG, "Magisk module does not exist, creating...");

        // Clean temporary directory
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR).exec();

        Shell.cmd(
                "printf 'id=OxygenCustomizer\n" +
                        "name=Oxygen Customizer\n" +
                        "version=" + MODULE_VERSION_NAME + "\n" +
                        "versionCode=" + MODULE_VERSION_CODE + "\n" +
                        "author=@DHD2280\n" +
                        "description=Systemless module for Oxygen Customizer.\n" +
                        "' > " + ModuleConstants.TEMP_MODULE_DIR + "/module.prop"
        ).exec();

        Shell.cmd(
                "printf 'MODDIR=${0%%/*}\n\nmount --bind $MODDIR/my_product/etc/extension/com.oplus.oplus-feature.xml /my_product/etc/extension/com.oplus.oplus-feature.xml\nmount --bind $MODDIR/my_product/etc/permissions/oplus.product.display_features.xml /my_product/etc/permissions/oplus.product.display_features.xml' > " + ModuleConstants.TEMP_MODULE_DIR + "/post-fs-data.sh"
        ).exec();

        Log.d(TAG, "skipped installation: " + skippedInstallation);
        if (!skippedInstallation) {
            Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\nuntil [ -d /storage/emulated/0/Android ]; do\n  sleep 1\ndone\nsleep 3\n\n" + "sleep 6\n\ntheme=$(cmd overlay list | grep \".x..OxygenCustomizerComponentTH\")\nnum=$(echo $theme | cut -d \"H\" -f 2 | cut -d \".\" -f 1)\nif [ \"${#num}\" -gt 0 ]\nthen\n cmd overlay enable OxygenCustomizerComponentTH$num.overlay\n cmd overlay set-priority OxygenCustomizerComponentTH$num.overlay highest\nfi\n\n' > " + ModuleConstants.TEMP_MODULE_DIR + "/service.sh").exec();
        } else {
            Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\n' > " + ModuleConstants.TEMP_MODULE_DIR + "/service.sh").exec();
        }
        Shell.cmd("touch " + ModuleConstants.TEMP_MODULE_DIR + "/system.prop").exec();
        Shell.cmd("touch " + ModuleConstants.TEMP_MODULE_DIR + "/auto_mount").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/system/product/overlay").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/my_product").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/my_product/etc").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/my_product/etc/extension").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/my_product/etc/permissions").exec();
        Shell.cmd("cp -a /my_product/etc/extension/com.oplus.oplus-feature.xml " + MY_PRODUCT_EXTENSION_DIR + "/").exec();
        Shell.cmd("cp -a /my_product/etc/extension/com.oplus.oplus-feature.xml " + MY_PRODUCT_EXTENSION_DIR + "/com.oplus.oplus-feature.xml.bak").exec();
        Shell.cmd("cp -a /my_product/etc/permissions/oplus.product.display_features.xml " + MY_PRODUCT_PERMISSIONS_DIR + "/").exec();
        Shell.cmd("cp -a /my_product/etc/permissions/oplus.product.display_features.xml " + MY_PRODUCT_PERMISSIONS_DIR + "/oplus.product.display_features.xml.bak").exec();

        createMETAINF();

        writePostExec();
        BinaryInstaller.symLinkBinaries();

        Log.i(TAG, "Magisk module successfully created.");
    }

    private static void createMETAINF() {
        Log.d(TAG, "Creating META-INF directory");
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google/android").exec();
        Shell.cmd("printf '" + ModuleConstants.MAGISK_UPDATE_BINARY + "' > " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google/android/update-binary").exec();
        Shell.cmd("printf '#MAGISK' > " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google/android/updater-script").exec();
        Log.d(TAG, "META-INF directory created");
    }

    private static void writePostExec() {
        StringBuilder post_exec = new StringBuilder();

        SharedPreferences prefs = OxygenCustomizer.getAppContext().getSharedPreferences(BuildConfig.APPLICATION_ID, Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();
        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().startsWith("fabricated")) {
                String name = item.getKey().replace("fabricated", "");
                List<String> commands = FabricatedUtil.buildCommands(Prefs.getString("FOCMDtarget" + name), Prefs.getString("FOCMDname" + name), Prefs.getString("FOCMDtype" + name), Prefs.getString("FOCMDresourceName" + name), Prefs.getString("FOCMDval" + name));
                post_exec.append(commands.get(0)).append('\n').append(commands.get(1)).append('\n');

            }
        }

        Shell.cmd("printf '" + post_exec + "' > " + ModuleConstants.TEMP_MODULE_DIR + "/post-exec.sh").exec();
    }

    private static boolean shouldUseDefaultColors() {
        return OverlayUtil.isOverlayDisabled("OxygenCustomizerComponentAMAC.overlay") && OverlayUtil.isOverlayDisabled("OxygenCustomizerComponentAMGC.overlay") && OverlayUtil.isOverlayDisabled("OxygenCustomizerComponentME.overlay");
    }

    public static boolean moduleExists() {
        return RootUtil.folderExists(ModuleConstants.OVERLAY_DIR);
    }

    public static String createModule(String sourceFolder, String destinationFilePath) throws Exception {
        File input = new File(sourceFolder);
        File output = new File(destinationFilePath);

        ZipParameters parameters = new ZipParameters();
        parameters.setIncludeRootFolder(false);
        parameters.setOverrideExistingFilesInZip(true);
        parameters.setCompressionMethod(CompressionMethod.DEFLATE);
        parameters.setCompressionLevel(CompressionLevel.NORMAL);

        try (ZipFile zipFile = new ZipFile(output)) {
            zipFile.addFolder(input, parameters);

            return zipFile.getFile().getAbsolutePath();
        }
    }

    public static boolean flashModule(String modulePath) throws Exception {
        Shell.Result result = null;

        if (RootUtil.isMagiskInstalled()) {
            result = Shell.cmd("magisk --install-module " + modulePath).exec();
        } else if (RootUtil.isKSUInstalled()) {
            result = Shell.cmd("/data/adb/ksud module install " + modulePath).exec();
        } else if (RootUtil.isApatchInstalled()) {
            result = Shell.cmd("apd module install " + modulePath).exec();
        }

        if (result == null) {
            throw new Exception("No supported root found");
        } else if (result.isSuccess()) {
            Log.i(TAG, "Successfully flashed module");
        } else {
            Log.e(TAG, "Failed to flash module");
            throw new Exception(String.join("\n", result.getOut()));
        }

        return !result.isSuccess();
    }

    public static boolean checkModuleVersion(Context context) {
        if (RootUtil.deviceProperlyRooted() && OverlayUtil.overlayExists()) {
            String version = getStringFromOverlay(context, "OxygenCustomizerComponentOCV.overlay", "oxygen_customizer_module_version");
            return version != null && version.equals(MODULE_VERSION_NAME);
        }
        return false;
    }

    public static void enablePocketStudio(boolean enable) {
        if (!moduleExists()) return;

        List<String> fileRead = Shell.cmd("cat /my_product/etc/extension/com.oplus.oplus-feature.xml").exec().getOut();
        Log.d(TAG, "com.oplus.oplus-feature: " + fileRead);

        Pattern pattern = Pattern.compile("oplus\\.software\\.pocketstudio\\.support");

        boolean featureAlreadyAdded = fileRead.stream().anyMatch(item -> pattern.matcher(item).find());

        if (enable && !featureAlreadyAdded) {
            fileRead.add(fileRead.size()-2, String.format(OPLUS_FEATURE_XML, OPLUS_POCKET_STUDIO_FEATURE));
        } else if (!enable && featureAlreadyAdded) {
            fileRead.removeIf(element -> element.contains(OPLUS_POCKET_STUDIO_FEATURE));
        }

        String oplusFeatures = String.join("\n", fileRead).replace("\"", "\\\"") ;

        Shell.cmd("printf \"" + oplusFeatures + "\" > /data/adb/modules/OxygenCustomizer/my_product/etc/extension/com.oplus.oplus-feature.xml").exec();
    }

    public static void enableMemcFeature(boolean enable) {
        if (!moduleExists()) return;

        enableMemcOplusFeature(enable);
        enableMemcOplusDisplayFeature(enable);
    }

    private static void enableMemcOplusFeature(boolean enable) {
        List<String> fileRead = Shell.cmd("cat /my_product/etc/extension/com.oplus.oplus-feature.xml").exec().getOut();
        Log.d(TAG, "com.oplus.oplus-feature: " + fileRead);

        Pattern pattern = Pattern.compile("name=\"(.*?)\"");
        if (!enable) {
            fileRead.removeIf(item -> {
                Matcher matcher = pattern.matcher(item);
                if (matcher.find()) {
                    String extractedText = matcher.group(1);
                    return OPLUS_MEMC_FEATURES.contains(extractedText);
                }
                return false;
            });
        } else {
            for (String memcFeature : OPLUS_MEMC_FEATURES) {
                boolean alreadyPresent = fileRead.stream().anyMatch(item -> {
                    Matcher matcher = pattern.matcher(item);
                    if (matcher.find()) {
                        String extractedText = matcher.group(1);
                        return extractedText.equals(memcFeature);
                    }
                    return false;
                });

                if (!alreadyPresent) {
                    fileRead.add(fileRead.size() - 2, String.format(OPLUS_FEATURE_XML, memcFeature));
                }
            }
        }

        String oplusFeatures = String.join("\n", fileRead).replace("\"", "\\\"") ;

        Shell.cmd("printf \"" + oplusFeatures + "\" > /data/adb/modules/OxygenCustomizer/my_product/etc/extension/com.oplus.oplus-feature.xml").exec();
    }

    private static void enableMemcOplusDisplayFeature(boolean enable) {
        List<String> fileRead = Shell.cmd("cat /my_product/etc/permissions/oplus.product.display_features.xml").exec().getOut();
        Log.d(TAG, "oplus.product.display_features: " + fileRead);

        Pattern pattern = Pattern.compile("name=\"(.*?)\"");

        if (!enable) {
            fileRead.removeIf(item -> {
                Matcher matcher = pattern.matcher(item);
                if (matcher.find()) {
                    String extractedText = matcher.group(1);
                    return OPLUS_MEMC_FEATURES.contains(extractedText);
                }
                return false;
            });
        } else {
            for (String memcFeature : OPLUS_MEMC_FEATURES) {
                boolean alreadyPresent = fileRead.stream().anyMatch(item -> {
                    Matcher matcher = pattern.matcher(item);
                    if (matcher.find()) {
                        String extractedText = matcher.group(1);
                        return extractedText.equals(memcFeature);
                    }
                    return false;
                });

                if (!alreadyPresent) {
                    fileRead.add(fileRead.size() - 2, String.format(OPLUS_FEATURE_XML, memcFeature));
                }
            }
        }

        String oplusFeatures = String.join("\n", fileRead).replace("\"", "\\\"") ;

        Shell.cmd("printf \"" + oplusFeatures + "\" > /data/adb/modules/OxygenCustomizer/my_product/etc/permissions/oplus.product.display_features.xml").exec();
    }

}
