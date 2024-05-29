package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.utils.Dynamic.skippedInstallation;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MODULE_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MODULE_VERSION_CODE;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.MODULE_VERSION_NAME;
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
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
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
            //BackupRestore.backupFiles();
        }
        installModule();
    }

    static void installModule() {
        Log.d(TAG, "Magisk module does not exist, creating...");

        // Clean temporary directory
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR).exec();
        Shell.cmd("printf 'id=OxygenCustomizer\nname=Oxygen Customizer\nversion=" + MODULE_VERSION_NAME + "\nversionCode=" + MODULE_VERSION_CODE + "\nauthor=@DHD2280\ndescription=Systemless module for Oxygen Customizer. " + OxygenCustomizer.getAppContext().getResources().getString(R.string.xposeddescription) + ".\n' > " + ModuleConstants.TEMP_MODULE_DIR + "/module.prop").exec();
        Shell.cmd("printf 'MODDIR=${0%%/*}\n\n' > " + ModuleConstants.TEMP_MODULE_DIR + "/post-fs-data.sh").exec();
        if (!skippedInstallation) {
            Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\nuntil [ -d /storage/emulated/0/Android ]; do\n  sleep 1\ndone\nsleep 3\n\n" + "sleep 6\n\nqspbd=$(cmd overlay list |  grep -E \"^.x..OxygenCustomizerComponentQSPBD.overlay\" | sed -E \"s/^.x..//\")\ndm=$(cmd overlay list |  grep -E \"^.x..OxygenCustomizerComponentDM.overlay\" | sed -E \"s/^.x..//\")\nif ([ ! -z \"$qspbd\" ] && [ -z \"$dm\" ])\nthen\n cmd overlay disable --user current OxygenCustomizerComponentQSPBD.overlay\n cmd overlay enable --user current OxygenCustomizerComponentQSPBD.overlay\n cmd overlay set-priority OxygenCustomizerComponentQSPBD.overlay highest\nfi\n\nqspba=$(cmd overlay list |  grep -E \"^.x..OxygenCustomizerComponentQSPBA.overlay\" | sed -E \"s/^.x..//\")\ndm=$(cmd overlay list |  grep -E \"^.x..OxygenCustomizerComponentDM.overlay\" | sed -E \"s/^.x..//\")\nif ([ ! -z \"$qspba\" ] && [ -z \"$dm\" ])\nthen\n cmd overlay disable --user current OxygenCustomizerComponentQSPBA.overlay\n cmd overlay enable --user current OxygenCustomizerComponentQSPBA.overlay\n cmd overlay set-priority OxygenCustomizerComponentQSPBA.overlay highest\nfi\n\n' > " + ModuleConstants.TEMP_MODULE_DIR + "/service.sh").exec();
        } else {
            Shell.cmd("printf 'MODDIR=${0%%/*}\n\nwhile [ \"$(getprop sys.boot_completed | tr -d \"\\r\")\" != \"1\" ]\ndo\n sleep 1\ndone\nsleep 5\n\nsh $MODDIR/post-exec.sh\n\n' > " + ModuleConstants.TEMP_MODULE_DIR + "/service.sh").exec();
        }
        Shell.cmd("touch " + ModuleConstants.TEMP_MODULE_DIR + "/system.prop").exec();
        Shell.cmd("touch " + ModuleConstants.TEMP_MODULE_DIR + "/auto_mount").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/system").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/system/product").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/system/product/overlay").exec();
        createMETAINF();

        writePostExec();
        BinaryInstaller.symLinkBinaries();

        Log.i(TAG, "Magisk module successfully created.");
    }

    private static void createMETAINF() {
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google").exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google/android").exec();
        Shell.cmd("printf '" + ModuleConstants.MAGISK_UPDATE_BINARY + "' > " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google/android/update-binary").exec();
        Shell.cmd("printf '#MAGISK' > " + ModuleConstants.TEMP_MODULE_DIR + "/META-INF/com/google/android/updater-script").exec();
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
        if (ModuleUtil.moduleExists() && RootUtil.deviceProperlyRooted() && OverlayUtil.overlayExists()) {
            String version = getStringFromOverlay(context, "OxygenCustomizerComponentOCV.overlay", "oxygen_customizer_module_version");
            return version != null && version.equals(MODULE_VERSION_NAME);
        }
        return false;
    }

}
