package it.dhd.oxygencustomizer.utils.helper;

import com.topjohnwu.superuser.Shell;

import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.RootUtil;

public class BackupRestore {

    public static void backupFiles() {
        // Create backup directory
        Shell.cmd("rm -rf " + ModuleConstants.BACKUP_DIR, "mkdir -p " + ModuleConstants.BACKUP_DIR).exec();
        
        backupFile(ModuleConstants.OVERLAY_DIR + "/OxygenCustomizerComponentCR1.apk");
    }

    public static void restoreFiles() {
        restoreFile("OxygenCustomizerComponentCR1.apk", ModuleConstants.TEMP_MODULE_OVERLAY_DIR);

        // Remove backup directory
        Shell.cmd("rm -rf " + ModuleConstants.BACKUP_DIR).exec();
    }

    private static boolean backupExists(String fileName) {
        return RootUtil.fileExists(ModuleConstants.BACKUP_DIR + "/" + fileName);
    }

    private static void backupFile(String source) {
        if (RootUtil.fileExists(source))
            Shell.cmd("cp -rf " + source + " " + ModuleConstants.BACKUP_DIR + "/").exec();
    }

    private static void restoreFile(String fileName, String dest) {
        if (backupExists(fileName)) {
            Shell.cmd("rm -rf " + dest + "/" + fileName).exec();
            Shell.cmd("cp -rf " + ModuleConstants.BACKUP_DIR + "/" + fileName + " " + dest + "/").exec();
        }
    }
    
}
