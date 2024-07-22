package it.dhd.oxygencustomizer.utils.overlay.compiler;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.helper.Logger.writeLog;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;

import it.dhd.oxygencustomizer.utils.FileUtil;
import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.RootUtil;
import it.dhd.oxygencustomizer.utils.SystemUtil;
import it.dhd.oxygencustomizer.utils.helper.BinaryInstaller;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class RoundnessCompiler {

    private static final String TAG = RoundnessCompiler.class.getSimpleName();
    private static final String[] mPackages = {FRAMEWORK, SYSTEM_UI};
    private static final String[] mOverlayName = {"CRN1"};//, "CR1"};
    private static boolean mForce = false;

    public static boolean buildOverlay(String[] resources, boolean force) throws IOException {
        mForce = force;

        preExecute();

        for (int i = 0; i < mOverlayName.length; i++) {
            // Create AndroidManifest.xml
            if (OverlayCompiler.createManifest(mOverlayName[i], mPackages[i], ModuleConstants.DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i])) {
                Log.e(TAG, "Failed to create Manifest for " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Write resources
            if (writeResources(ModuleConstants.DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i], resources[i])) {
                Log.e(TAG, "Failed to write resource for " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // Build APK using AAPT
            if (OverlayCompiler.runAapt(ModuleConstants.DATA_DIR + "/Overlays/" + mPackages[i] + "/" + mOverlayName[i], mPackages[i])) {
                Log.e(TAG, "Failed to build " + mOverlayName[i] + "! Exiting...");
                postExecute(true);
                return true;
            }

            // ZipAlign the APK
            if (OverlayCompiler.zipAlign(ModuleConstants.UNSIGNED_UNALIGNED_DIR + "/" + mOverlayName[i] + "-unsigned-unaligned.apk")) {
                Log.e(TAG, "Failed to align " + mOverlayName[i] + "-unsigned-unaligned.apk! Exiting...");
                postExecute(true);
                return true;
            }

            // Sign the APK
            if (OverlayCompiler.apkSigner(ModuleConstants.UNSIGNED_DIR + "/" + mOverlayName[i] + "-unsigned.apk")) {
                Log.e(TAG, "Failed to sign " + mOverlayName[i] + "-unsigned.apk! Exiting...");
                postExecute(true);
                return true;
            }
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/Overlays").exec();

        // Extract overlay from assets
        for (int i = 0; i < mOverlayName.length; i++)
            FileUtil.copyAssets("Overlays/" + mPackages[i] + "/" + mOverlayName[i]);

        // Create temp directory
        Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR + "; mkdir -p " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + ModuleConstants.SIGNED_DIR).exec();
        if (!mForce) {
            Shell.cmd("mkdir -p " + ModuleConstants.BACKUP_DIR).exec();
        }

        if (mForce) {
            // Disable the overlay in case it is already enabled
            String[] overlayNames = new String[mOverlayName.length];
            for (int i = 1; i <= mOverlayName.length; i++) {
                overlayNames[i - 1] = "OxygenCustomizerComponentCRN" + i + ".overlay";
            }
            OverlayUtil.disableOverlays(overlayNames);
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            for (String overlayName : mOverlayName) {
                Shell.cmd("cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk " + ModuleConstants.OVERLAY_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk").exec();
                RootUtil.setPermissions(644, ModuleConstants.OVERLAY_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk");

                if (mForce) {
                    // Move to files dir and install
                    Shell.cmd("cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk").exec();
                    RootUtil.setPermissions(644, ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk");
                    Shell.cmd("pm install -r " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk").exec();
                    Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk").exec();
                }
            }

            if (mForce) {
                // Move to system overlay dir
                SystemUtil.mountRW();
                for (String overlayName : mOverlayName) {
                    Shell.cmd("cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk " + ModuleConstants.SYSTEM_OVERLAY_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk").exec();
                    RootUtil.setPermissions(644, "/system/product/overlay/OxygenCustomizerComponent" + overlayName + ".apk");
                }
                SystemUtil.mountRO();

                // Enable the overlays
                String[] overlayNames = new String[mOverlayName.length];
                for (int i = 1; i <= mOverlayName.length; i++) {
                    overlayNames[i - 1] = "OxygenCustomizerComponentCRN" + i + ".overlay";
                }
                OverlayUtil.enableOverlays(overlayNames);
            } else {
                for (String overlayName : mOverlayName) {
                    Shell.cmd("cp -rf " + ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk " + ModuleConstants.BACKUP_DIR + "/OxygenCustomizerComponent" + overlayName + ".apk").exec();
                }
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + ModuleConstants.TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/Overlays").exec();
    }

    private static boolean writeResources(String source, String resources) {
        Shell.Result result = Shell.cmd("rm -rf " + source + "/res/values/dimens.xml", "printf '" + resources + "' > " + source + "/res/values/dimens.xml;").exec();

        if (result.isSuccess())
            Log.i(TAG + " - WriteResources", "Successfully written resources for UiRoundness");
        else {
            Log.e(TAG + " - WriteResources", "Failed to write resources for UiRoundness" + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - WriteResources", "Failed to write resources for UiRoundness", result.getOut());
        }

        return !result.isSuccess();
    }
}
