package it.dhd.oxygencustomizer.utils.overlay.compiler;

import static it.dhd.oxygencustomizer.utils.ModuleConstants.BACKUP_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.OVERLAY_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.SIGNED_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.SYSTEM_OVERLAY_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.TEMP_CACHE_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.TEMP_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.TEMP_OVERLAY_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.UNSIGNED_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.UNSIGNED_UNALIGNED_DIR;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.io.IOException;

import it.dhd.oxygencustomizer.utils.FileUtil;
import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.RootUtil;
import it.dhd.oxygencustomizer.utils.SystemUtil;
import it.dhd.oxygencustomizer.utils.helper.BinaryInstaller;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class OnDemandCompiler {

    private static final String TAG = OnDemandCompiler.class.getSimpleName();
    private static String mOverlayName = null;
    private static String mPackage = null;
    private static int mStyle = 0;
    private static boolean mForce = false;

    public static boolean buildOverlay(String overlay_name, int style, String targetPackage, boolean force) throws IOException {
        mOverlayName = overlay_name;
        mPackage = targetPackage;
        mStyle = style;
        mForce = force;

        preExecute();
        moveOverlaysToCache();

        // Create AndroidManifest.xml
        if (OverlayCompiler.createManifest(overlay_name, targetPackage, TEMP_CACHE_DIR + "/" + targetPackage + "/" + overlay_name)) {
            Log.e(TAG, "Failed to create Manifest for " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // Build APK using AAPT
        if (OverlayCompiler.runAapt(TEMP_CACHE_DIR + "/" + targetPackage + "/" + overlay_name, targetPackage)) {
            Log.e(TAG, "Failed to build " + overlay_name + "! Exiting...");
            postExecute(true);
            return true;
        }

        // ZipAlign the APK
        if (OverlayCompiler.zipAlign(UNSIGNED_UNALIGNED_DIR + "/" + overlay_name + "-unsigned-unaligned.apk")) {
            Log.e(TAG, "Failed to align " + overlay_name + "-unsigned-unaligned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        // Sign the APK
        if (OverlayCompiler.apkSigner(UNSIGNED_DIR + "/" + overlay_name + "-unsigned.apk")) {
            Log.e(TAG, "Failed to sign " + overlay_name + "-unsigned.apk! Exiting...");
            postExecute(true);
            return true;
        }

        postExecute(false);
        return false;
    }

    private static void preExecute() throws IOException {
        // Create symbolic link
        BinaryInstaller.symLinkBinaries();

        // Clean data directory
        Shell.cmd("rm -rf " + TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/CompileOnDemand").exec();

        // Extract overlay from assets
        FileUtil.copyAssets("CompileOnDemand/" + mPackage + "/" + mOverlayName + mStyle);

        // Create temp directory
        Shell.cmd("rm -rf " + TEMP_DIR + "; mkdir -p " + TEMP_DIR).exec();
        Shell.cmd("mkdir -p " + TEMP_OVERLAY_DIR).exec();
        Shell.cmd("mkdir -p " + TEMP_CACHE_DIR).exec();
        Shell.cmd("mkdir -p " + UNSIGNED_UNALIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + UNSIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + SIGNED_DIR).exec();
        Shell.cmd("mkdir -p " + TEMP_CACHE_DIR + "/" + mPackage + "/").exec();
        if (!mForce) {
            Shell.cmd("mkdir -p " + BACKUP_DIR).exec();
        }

        if (mForce) {
            // Disable the overlay in case it is already enabled
            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + mOverlayName + ".overlay");
        }
    }

    private static void postExecute(boolean hasErroredOut) {
        // Move all generated overlays to module
        if (!hasErroredOut) {
            Shell.cmd("cp -rf " + SIGNED_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk").exec();
            RootUtil.setPermissions(644, OVERLAY_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk");

            // Move to files dir and install
            if (mForce) {
                Shell.cmd("cp -rf " + SIGNED_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk").exec();
                RootUtil.setPermissions(644, ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk");
                Shell.cmd("pm install -r " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk").exec();
                Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk").exec();

                // Move to system overlay dir
                SystemUtil.mountRW();
                Shell.cmd("cp -rf " + SIGNED_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk " + SYSTEM_OVERLAY_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk").exec();
                RootUtil.setPermissions(644, "/system/product/overlay/OxygenCustomizerComponent" + mOverlayName + ".apk");
                SystemUtil.mountRO();

                // Enable the overlay
                OverlayUtil.enableOverlay("OxygenCustomizerComponent" + mOverlayName + ".overlay");
            } else {
                Shell.cmd("cp -rf " + SIGNED_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk " + BACKUP_DIR + "/OxygenCustomizerComponent" + mOverlayName + ".apk").exec();
            }
        }

        // Clean temp directory
        Shell.cmd("rm -rf " + TEMP_DIR).exec();
        Shell.cmd("rm -rf " + ModuleConstants.DATA_DIR + "/CompileOnDemand").exec();
    }

    private static void moveOverlaysToCache() {
        Shell.cmd("mv -f \"" + ModuleConstants.DATA_DIR + "/CompileOnDemand/" + mPackage + "/" + mOverlayName + mStyle + "\" \"" + TEMP_CACHE_DIR + "/" + mPackage + "/" + mOverlayName + "\"").exec().isSuccess();
    }
}
