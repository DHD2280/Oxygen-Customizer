package it.dhd.oxygencustomizer.utils.overlay.compiler;

import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT;
import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT2;
import static it.dhd.oxygencustomizer.utils.Dynamic.ZIPALIGN;
import static it.dhd.oxygencustomizer.utils.Dynamic.isAtleastA14;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.FRAMEWORK_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.UNSIGNED_UNALIGNED_DIR;
import static it.dhd.oxygencustomizer.utils.apksigner.CryptoUtils.readCertificate;
import static it.dhd.oxygencustomizer.utils.apksigner.CryptoUtils.readPrivateKey;
import static it.dhd.oxygencustomizer.utils.helper.Logger.writeLog;

import android.util.Log;

import androidx.annotation.NonNull;

import com.topjohnwu.superuser.Shell;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Objects;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.apksigner.SignAPK;

public class OnboardingCompiler {

    private static final String TAG = OnboardingCompiler.class.getSimpleName();
    private static final String aapt = AAPT.getAbsolutePath();
    private static final String aapt2 = AAPT2.getAbsolutePath();
    private static final String zipalign = ZIPALIGN.getAbsolutePath();

    public static boolean createManifest(String name, String target, String source) {
        boolean hasErroredOut = false;
        int attempt = 3;

        while (attempt-- != 0) {
            if (OverlayCompiler.createManifest(name, target, source)) {
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            } else {
                hasErroredOut = true;
                break;
            }
        }

        return !hasErroredOut;
    }

    public static boolean runAapt(String source, String name) {
        Shell.Result result = null;
        int attempt = 3;
        String command;

        if (!isAtleastA14) {
            command = aapt + " p -f -M " + source + "/AndroidManifest.xml -I " + FRAMEWORK_DIR + " -S " + source + "/res -F " + UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk --include-meta-data --auto-add-overlay";
        } else {
            command = getAAPT2Command(source, name);
        }

        while (attempt-- != 0) {
            result = Shell.cmd(command).exec();

            if (!result.isSuccess() && OverlayCompiler.listContains(result.getOut(), "colorSurfaceHeader")) {
                Shell.cmd("find " + source + "/res -type f -name \"*.xml\" -exec sed -i '/colorSurfaceHeader/d' {} +").exec();
                result = Shell.cmd(command).exec();
            }

            if (result.isSuccess()) {
                Log.i(TAG + " - AAPT", "Successfully built APK for " + name);
                break;
            } else {
                Log.e(TAG + " - AAPT", "Failed to build APK for " + name + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        }

        if (!result.isSuccess())
            writeLog(TAG + " - AAPT", "Failed to build APK for " + name, result.getOut());

        return !result.isSuccess();
    }

    @NonNull
    private static String getAAPT2Command(String source, String name) {
        String folderCommand = "rm -rf " + source + "/compiled; mkdir " + source + "/compiled; [ -d " + source + "/compiled ] && ";
        String compileCommand = aapt2 + " compile --dir " + source + "/res -o " + source + "/compiled && ";
        String linkCommand = aapt2 + " link -o " + UNSIGNED_UNALIGNED_DIR + '/' + name + "-unsigned-unaligned.apk -I " + FRAMEWORK_DIR + " --manifest " + source + "/AndroidManifest.xml " + source + "/compiled/* --auto-add-overlay";

        return folderCommand + compileCommand + linkCommand;
    }

    public static boolean zipAlign(String source, String name) {
        Shell.Result result = null;
        int attempt = 3;

        while (attempt-- != 0) {
            result = Shell.cmd(zipalign + " -p -f 4 " + source + ' ' + ModuleConstants.UNSIGNED_DIR + '/' + name).exec();

            if (result.isSuccess()) {
                Log.i(TAG + " - ZipAlign", "Successfully zip aligned " + name.replace("-unsigned.apk", ""));
                break;
            } else {
                Log.e(TAG + " - ZipAlign", "Failed to zip align " + name.replace("-unsigned.apk", "") + '\n' + String.join("\n", result.getOut()));
                try {
                    Thread.sleep(1000);
                } catch (Exception ignored) {
                }
            }
        }

        if (!result.isSuccess())
            writeLog(TAG + " - ZipAlign", "Failed to zip align " + name.replace("-unsigned.apk", ""), result.getOut());

        return !result.isSuccess();
    }

    public static boolean apkSigner(String source, String name) {
        try {
            PrivateKey key = readPrivateKey(Objects.requireNonNull(OxygenCustomizer.getAppContext()).getAssets().open("Keystore/testkey.pk8"));
            X509Certificate cert = readCertificate(OxygenCustomizer.getAppContext().getAssets().open("Keystore/testkey.x509.pem"));

            SignAPK.sign(cert, key, source, ModuleConstants.SIGNED_DIR + "/OxygenCustomizerComponent" + name);

            Log.i(TAG + " - APKSigner", "Successfully signed " + name.replace(".apk", ""));
        } catch (Exception e) {
            Log.e(TAG + " - APKSigner", "Failed to sign " + name.replace(".apk", "") + '\n' + e);
            writeLog(TAG + " - APKSigner", "Failed to sign " + name, e);
            return true;
        }
        return false;
    }

}
