package it.dhd.oxygencustomizer.utils.helper;

import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT;
import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT2;
import static it.dhd.oxygencustomizer.utils.Dynamic.AAPT2LIB;
import static it.dhd.oxygencustomizer.utils.Dynamic.AAPTLIB;
import static it.dhd.oxygencustomizer.utils.Dynamic.NATIVE_LIBRARY_DIR;
import static it.dhd.oxygencustomizer.utils.Dynamic.ZIPALIGN;
import static it.dhd.oxygencustomizer.utils.Dynamic.ZIPALIGNLIB;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.BIN_DIR;

import android.util.Log;

import com.topjohnwu.superuser.Shell;

import it.dhd.oxygencustomizer.utils.FileUtil;
import it.dhd.oxygencustomizer.utils.ModuleConstants;

public class BinaryInstaller {

    private static final String TAG = BinaryInstaller.class.getSimpleName();

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void symLinkBinaries() {
        Shell.cmd("mkdir -p " + BIN_DIR).exec();
        extractTools();

        if (AAPT.exists()) AAPT.delete();
        if (AAPT2.exists()) AAPT2.delete();
        if (ZIPALIGN.exists()) ZIPALIGN.delete();

        Shell.cmd("ln -sf " + AAPTLIB.getAbsolutePath() + ' ' + AAPT.getAbsolutePath()).exec();
        Shell.cmd("ln -sf " + AAPT2LIB.getAbsolutePath() + ' ' + AAPT2.getAbsolutePath()).exec();
        Shell.cmd("ln -sf " + ZIPALIGNLIB.getAbsolutePath() + ' ' + ZIPALIGN.getAbsolutePath()).exec();
    }

    public static void extractTools() {
        Log.d(TAG, "Extracting tools...");
        try {
            FileUtil.copyAssets("Tools");
            Shell.cmd("for fl in " + ModuleConstants.DATA_DIR + "/Tools/*; do cp -f \"$fl\" \"" + NATIVE_LIBRARY_DIR + "\"; chmod 755 \"" + NATIVE_LIBRARY_DIR + "/$(basename $fl)\"; ln -sf \"" + NATIVE_LIBRARY_DIR + "/$(basename $fl)\" \"" + BIN_DIR + "/$(basename $fl)\"; done").exec();
            FileUtil.cleanDir("Tools");
        } catch (Exception e) {
            Log.e(TAG, "Failed to extract tools.\n" + e);
        }
    }
}
