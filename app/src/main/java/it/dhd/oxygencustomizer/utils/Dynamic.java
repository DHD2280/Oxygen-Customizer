package it.dhd.oxygencustomizer.utils;


import static it.dhd.oxygencustomizer.utils.ModuleConstants.BIN_DIR;

import android.os.Build;

import com.topjohnwu.superuser.Shell;

import java.io.File;
import java.util.List;

import it.dhd.oxygencustomizer.OxygenCustomizer;

public class Dynamic {

    // Grab number of overlays dynamically for each variant
    public static final int TOTAL_ANDROID_THEMES = (Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentTH'").exec().getOut()).size();
    public static final List<String> LIST_ANDROID_THEMES = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentTH'").exec().getOut();
    public static final int TOTAL_ICON_PACKS = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentIPSUI'").exec().getOut().size();
    public static final List<String> LIST_ICON_PACKS = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentIPSUI'").exec().getOut();
    public static final int TOTAL_NOTIFICATIONS = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentNFN'").exec().getOut().size();
    public static final int TOTAL_NAVBAR = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentNB'").exec().getOut().size();
    public static final int TOTAL_SIGNAL_ICONS = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentSGIC'").exec().getOut().size();
    public static final int TOTAL_WIFI_ICONS = Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponentWIFI'").exec().getOut().size();
    // Overlay compiler tools
    public static final String NATIVE_LIBRARY_DIR = OxygenCustomizer.getAppContext().getApplicationInfo().nativeLibraryDir;
    public static final File AAPTLIB = new File(NATIVE_LIBRARY_DIR, "libaapt.so");
    public static final File AAPT2LIB = new File(NATIVE_LIBRARY_DIR, "libaapt2.so");
    public static final File AAPT = new File(BIN_DIR, "aapt");
    public static final File AAPT2 = new File(BIN_DIR, "aapt2");
    public static final File ZIPALIGNLIB = new File(NATIVE_LIBRARY_DIR, "libzipalign.so");
    public static final File ZIPALIGN = new File(BIN_DIR, "zipalign");

    // Onboarding overlay installation
    public static boolean skippedInstallation = false;

    // Device information
    public static final boolean isAtleastA14 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
}
