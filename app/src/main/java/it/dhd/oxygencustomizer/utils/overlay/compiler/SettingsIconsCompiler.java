package it.dhd.oxygencustomizer.utils.overlay.compiler;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.utils.FileUtil.copyAssets;
import static it.dhd.oxygencustomizer.utils.RootUtil.setPermissions;
import static it.dhd.oxygencustomizer.utils.SystemUtil.mountRO;
import static it.dhd.oxygencustomizer.utils.SystemUtil.mountRW;
import static it.dhd.oxygencustomizer.utils.helper.BinaryInstaller.symLinkBinaries;
import static it.dhd.oxygencustomizer.utils.helper.Logger.writeLog;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.disableOverlay;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.enableOverlays;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.ModuleConstants;

public class SettingsIconsCompiler {

    private final static String TAG = SettingsIconsCompiler.class.getSimpleName();

    private static int mIconSet;
    private static boolean mForce;

    private static final List<String> mPackages = new ArrayList<>() {{
        add(SETTINGS);
        add(BuildConfig.APPLICATION_ID.replaceAll(".debug", ""));
    }};

    public static boolean buildOverlay(int iconSet, String resources, boolean force) throws IOException {
        mIconSet = iconSet;
        mForce = force;

        preExecute();
        moveOverlaysToCache();

        for (int i = 0; i < mPackages.size(); i++) {
            String overlayName = "SIP" + (i + 1);

            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(
                    overlayName,
                    mPackages.get(i),
                    ModuleConstants.TEMP_CACHE_DIR + "/" + mPackages.get(i) + "/" + overlayName
            )) {
                Log.e(TAG, "Failed to create Manifest for " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Write resources
            if (!resources.isEmpty() && writeResources(
                    ModuleConstants.TEMP_CACHE_DIR + "/" + mPackages.get(i) + "/" + overlayName,
                    resources
            )) {
                Log.e(TAG, "Failed to write resource for " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(
                    ModuleConstants.TEMP_CACHE_DIR + "/" + mPackages.get(i) + "/" + overlayName,
                    mPackages.get(i)
            )) {
                Log.e(TAG, "Failed to build " + overlayName + "! Exiting...");
                postExecute(true);
                return true;
            }

            // ZipAlign the APK
            if (OverlayCompiler.zipAlign(ModuleConstants.UNSIGNED_UNALIGNED_DIR + "/" + overlayName + "-unsigned-unaligned.apk")) {
                Log.e(TAG, "Failed to align " + overlayName + "-unsigned-unaligned.apk! Exiting...");
                postExecute(true);
                return true;
            }

            // Sign the APK
            if (OverlayCompiler.apkSigner(ModuleConstants.UNSIGNED_DIR + "/" + overlayName + "-unsigned.apk")) {
                Log.e(TAG, "Failed to sign " + overlayName + "-unsigned.apk! Exiting...");
                postExecute(true);
                return true;
            }
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Create symbolic link
        symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/CompileOnDemand").exec();

        // Extract overlay from assets
        for (String packageName : mPackages) {
            copyAssets("CompileOnDemand/" + packageName + "/ICS" + mIconSet);
        }

        // Create temp directory
        Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR + "; mkdir -p " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.SIGNED_DIR).exec();

        for (String packageName : mPackages) {
            Shell.cmd("mkdir -p " + ModuleConstants.TEMP_CACHE_DIR + "/" + packageName + "/").exec();
        }

        if (!mForce) {
            Shell.cmd("mkdir -p " + ModuleConstants.BACKUP_DIR).exec();
        } else {
            disableOverlay("OxygenCustomizerComponentSIP1.overlay");
            disableOverlay("OxygenCustomizerComponentSIP2.overlay");
        }
    }

    private static void moveOverlaysToCache() {
        for (int i = 0; i < mPackages.size(); i++) {
            Shell.cmd(
                    "mv -f \"" + ModuleConstants.DATA_DIR + "/CompileOnDemand/" + mPackages.get(i) + "/" + "ICS" + mIconSet + "\" \"" + ModuleConstants.TEMP_CACHE_DIR + "/" + mPackages.get(i) + "/" + "SIP" + (i + 1) + "\""
            ).exec();
        }
    }

    private static boolean writeResources(String source, String resources) {
        Shell.Result result = Shell.cmd(
                "rm -rf " + source + "/res/values/OxygenCustomizer.xml",
                "printf '" + resources + "' > " + source + "/res/values/OxygenCustomizer.xml;"
        ).exec();

        if (result.isSuccess()) {
            Log.i(TAG + " - WriteResources", "Successfully written resources for SettingsIcons");
        } else {
            Log.e(TAG + " - WriteResources", "Failed to write resources for SettingsIcons\n" +
                    String.join("\n", result.getOut()));
            writeLog(TAG + " - WriteResources", "Failed to write resources for SettingsIcons", result.getOut());
        }

        return !result.isSuccess();
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (int i = 1; i <= mPackages.size(); i++) {
                Shell.cmd(
                        "cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk " + ModuleConstants.OVERLAY_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk"
                ).exec();
                setPermissions(644, ModuleConstants.OVERLAY_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk");
                if (mForce) {
                    // Move to files dir and install
                    Shell.cmd(
                            "cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk"
                    ).exec();
                    setPermissions(644, ModuleConstants.DATA_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk");
                    Shell.cmd(
                            "pm install -r " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk"
                    ).exec();
                    Shell.cmd(
                            "rm -rf " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk"
                    ).exec();
                }
            }

            if (mForce) {
                // Move to system overlay dir
                mountRW();
                for (int i = 1; i <= mPackages.size(); i++) {
                    Shell.cmd(
                            "cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk " + ModuleConstants.SYSTEM_OVERLAY_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk"
                    ).exec();
                    setPermissions(644, "/system/product/overlay/OxygenCustomizerComponentSIP" + i + ".apk");
                }
                mountRO();

                // Enable the overlays
                String[] overlayNames = new String[mPackages.size()];

                for (int i = 1; i <= mPackages.size(); i++) {
                    overlayNames[i - 1] = "OxygenCustomizerComponentSIP" + i + ".overlay";
                }

                enableOverlays(overlayNames);
            } else {
                for (int i = 1; i <= mPackages.size(); i++) {
                    Shell.cmd(
                            "cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk " + ModuleConstants.BACKUP_DIR + "/OxygenCustomizerComponentSIP" + i + ".apk"
                    ).exec();
                }
            }
        }

        // Clean temp directory
        //Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();
        //Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/CompileOnDemand").exec();
    }

}
