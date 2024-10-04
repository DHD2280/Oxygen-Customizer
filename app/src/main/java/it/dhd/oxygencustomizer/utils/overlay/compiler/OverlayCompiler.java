package it.dhd.oxygencustomizer.utils.overlay.compiler;

import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT;
import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT2;
import static it.dhd.oxygencustomizer.utils.Dynamic.ZIPALIGN;
import static it.dhd.oxygencustomizer.utils.Dynamic.isAtleastA14;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.FRAMEWORK_DIR;
import static it.dhd.oxygencustomizer.utils.apksigner.CryptoUtils.readCertificate;
import static it.dhd.oxygencustomizer.utils.apksigner.CryptoUtils.readPrivateKey;
import static it.dhd.oxygencustomizer.utils.helper.Logger.writeLog;

import android.util.Log;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.apksigner.SignAPK;

public class OverlayCompiler {

    private static final String TAG = OverlayCompiler.class.getSimpleName();
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String aapt2 = AAPT2.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();
    private static PrivateKey key = null;
    private static X509Certificate cert = null;

    public static boolean createManifest(String overlayName, String targetPackage, String sourceDir) {
        List<String> module = new ArrayList<>();
        module.add("printf '" +
                CompilerUtil.createManifestContent(overlayName, targetPackage) +
                "' > " + sourceDir + "/AndroidManifest.xml;");

        Shell.Result result = Shell.cmd(String.join("\\n", module)).exec();

        if (result.isSuccess())
            Log.i(TAG + " - Manifest", "Successfully created manifest for " + overlayName);
        else {
            Log.e(TAG + " - Manifest", "Failed to create manifest for " + overlayName + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - Manifest", "Failed to create manifest for " + overlayName, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean runAapt(String source, String targetPackage) {
        String name = CompilerUtil.getOverlayName(source) +
                (source.contains("SpecialOverlays") ?
                        ".zip" :
                        "-unsigned-unaligned.apk");
        StringBuilder aaptCommand = buildAAPT2Command(source, name);

        String[] splitLocations = AppUtils.getSplitLocations(targetPackage);
        for (String targetApk : splitLocations) {
            aaptCommand.append(" -I ").append(targetApk);
        }

        String command = String.valueOf(aaptCommand);
        Shell.Result result = Shell.cmd(command).exec();

        if (!result.isSuccess() && OverlayCompiler.listContains(result.getOut(), "colorSurfaceHeader")) {
            Shell.cmd("find " + source + "/res -type f -name \"*.xml\" -exec sed -i '/colorSurfaceHeader/d' {} +").exec();
            result = Shell.cmd(command).exec();
        }

        if (result.isSuccess()) Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
        else {
            Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());
        }

        return !result.isSuccess();
    }

    @NonNull
    private static StringBuilder buildAAPT2Command(String source, String name) {
        String outputDir = source.contains("SpecialOverlays") ?
                ModuleConstants.COMPANION_COMPILED_DIR :
                ModuleConstants.UNSIGNED_UNALIGNED_DIR;

        if (!isAtleastA14) {
            return new StringBuilder(aapt + " p -f -M " + source + "/AndroidManifest.xml -S " + source + "/res -F " + outputDir + '/' + name + " -I " + FRAMEWORK_DIR + " --include-meta-data --auto-add-overlay");
        } else {
            return new StringBuilder(getAAPT2Command(source, name, outputDir));
        }
    }

    @NonNull
    private static String getAAPT2Command(String source, String name, String outputDir) {
        String folderCommand = "rm -rf " + source + "/compiled; mkdir " + source + "/compiled; [ -d " + source + "/compiled ] && ";
        String compileCommand = aapt2 + " compile --dir " + source + "/res -o " + source + "/compiled && ";
        String linkCommand = aapt2 + " link -o " + outputDir + '/' + name + " -I " + FRAMEWORK_DIR + " --manifest " + source + "/AndroidManifest.xml " + source + "/compiled/* --auto-add-overlay";

        return folderCommand + compileCommand + linkCommand;
    }

    public static boolean zipAlign(String source) {
        String fileName = CompilerUtil.getOverlayName(source);
        Shell.Result result = Shell.cmd(zipalign + " 4 " + source + ' ' + ModuleConstants.UNSIGNED_DIR + "/" + fileName + "-unsigned.apk").exec();

        if (result.isSuccess())
            Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + fileName);
        else {
            Log.e(TAG + " - ZipAlign", "Failed to zip align " + fileName + "\n" + String.join("\n", result.getOut()));
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + fileName, result.getOut());
        }

        return !result.isSuccess();
    }

    public static boolean apkSigner(String source) {
        String fileName = "null";
        try {
            if (key == null) {
                key = readPrivateKey(Objects.requireNonNull(OxygenCustomizer.getAppContext()).getAssets().open("Keystore/testkey.pk8"));
            }
            if (cert == null) {
                cert = readCertificate(Objects.requireNonNull(OxygenCustomizer.getAppContext()).getAssets().open("Keystore/testkey.x509.pem"));
            }

            fileName = CompilerUtil.getOverlayName(source);
            SignAPK.sign(cert, key, source, ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponent" + fileName + ".apk");

            Log.i(TAG + " - APKSigner", "Successfully signed " + fileName);
        } catch (Exception e) {
            Log.e(TAG, e.toString());
            writeLog(TAG + " - APKSigner", "Failed to sign " + fileName, e);
            return true;
        }
        return false;
    }

    public static boolean listContains(List<String> list, String target) {
        for (String item : list) {
            if (item.toLowerCase().contains(target.toLowerCase())) {
                return true;
            }
        }

        return false;
    }
}
