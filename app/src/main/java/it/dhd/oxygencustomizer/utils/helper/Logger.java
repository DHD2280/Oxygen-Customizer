package it.dhd.oxygencustomizer.utils.helper;

import static it.dhd.oxygencustomizer.utils.ModuleConstants.DOCUMENTS_DIR;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.LOG_DIR;

import android.os.Build;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.BuildConfig;

public class Logger {

    private static final String TAG = Logger.class.getSimpleName();

    public static void writeLog(String tag, String header, List<String> details) {
        StringBuilder log = getDeviceInfo();
        log.append("error: ").append(header).append('\n');
        log.append('\n');
        log.append(tag).append(":\n");

        for (String line : details) {
            log.append('\t').append(line).append('\n');
        }

        writeLogToFile(log);
    }

    public static void writeLog(String tag, String header, String details) {
        StringBuilder log = getDeviceInfo();
        log.append("error: ").append(header).append('\n');
        log.append('\n');
        log.append(tag).append(":\n");
        log.append(details).append('\n');

        writeLogToFile(log);
    }

    public static void writeLog(String tag, String header, Exception exception) {
        StringBuilder log = getDeviceInfo();
        log.append("error: ").append(header).append('\n');
        log.append('\n');
        log.append(tag).append(":\n");

        StringWriter writer = new StringWriter();
        exception.printStackTrace(new PrintWriter(writer));
        String str = writer.toString();

        log.append(str).append('\n');

        writeLogToFile(log);
    }

    private static StringBuilder getDeviceInfo() {
        StringBuilder info = new StringBuilder("Oxygen Customizer bug report ");
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.getDefault());
        info.append(sdf.format(new Date())).append('\n');
        info.append("version: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\n");
        info.append("build.brand: ").append(Build.BRAND).append('\n');
        info.append("build.device: ").append(Build.DEVICE).append('\n');
        info.append("build.display: ").append(Build.DISPLAY).append('\n');
        info.append("build.fingerprint: ").append(Build.FINGERPRINT).append('\n');
        info.append("build.hardware: ").append(Build.HARDWARE).append('\n');
        info.append("build.id: ").append(Build.ID).append('\n');
        info.append("build.manufacturer: ").append(Build.MANUFACTURER).append('\n');
        info.append("build.model: ").append(Build.MODEL).append('\n');
        info.append("build.product: ").append(Build.PRODUCT).append('\n');
        info.append("build.type: ").append(Build.TYPE).append('\n');
        info.append("version.codename: ").append(Build.VERSION.CODENAME).append('\n');
        info.append("version.release: ").append(Build.VERSION.RELEASE).append('\n');
        info.append("version.sdk_int: ").append(Build.VERSION.SDK_INT).append('\n');
        info.append("oxygen_customizer.version_name: ").append(BuildConfig.VERSION_NAME).append('\n');
        info.append("oxygen_customizer.version_code: ").append(BuildConfig.VERSION_CODE).append('\n');
        info.append('\n');

        return info;
    }

    private static void writeLogToFile(StringBuilder log) {
        Log.d(TAG, "Writing logs to " + LOG_DIR + ".");
        try {
            Files.createDirectories(Paths.get(LOG_DIR));

            SimpleDateFormat dF = new SimpleDateFormat("dd-MM-yy_HH_mm_ss", Locale.getDefault());
            String fileName = "oxygen_customizer_logcat_" + dF.format(new Date()) + ".txt";

            File ocDir = new File(DOCUMENTS_DIR, "OxygenCustomizer");
            File file = new File(ocDir, fileName);

            Log.d(TAG, "Writing logs to " + file.getAbsolutePath() + ".");

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(log.toString());
            bw.close();
        } catch (Exception e) {
            Log.e(TAG, "Failed to write logs.\n" + e);
        }
    }
}
