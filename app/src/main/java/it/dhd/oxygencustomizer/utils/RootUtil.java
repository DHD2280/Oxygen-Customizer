package it.dhd.oxygencustomizer.utils;

import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RootUtil {

    public static boolean isDeviceRooted() {
        return Boolean.TRUE.equals(Shell.isAppGrantedRoot());
    }

    public static boolean isMagiskInstalled() {
        return Shell.cmd("magisk -v").exec().isSuccess();
    }

    public static boolean isKSUInstalled() {
        return Shell.cmd("/data/adb/ksud -h").exec().isSuccess();
    }

    public static boolean isApatchInstalled() {
        return Shell.cmd("apd --help").exec().isSuccess();
    }

    public static boolean moduleExists(String moduleId) {
        return folderExists("/data/adb/modules/" + moduleId);
    }

    public static void setPermissions(final int permission, final String filename) {
        Shell.cmd("chmod " + permission + ' ' + filename).exec();
    }

    public static void setPermissionsRecursively(final int permission, final String foldername) {
        Shell.cmd("chmod -R " + permission + ' ' + foldername).exec();

        String perm = String.valueOf(permission);

        if (!Shell.cmd("stat -c '%a' " + foldername).exec().getOut().contains(perm) || !Shell.cmd("fl=$(find '" + foldername + "' -type f -mindepth 1 -print -quit); stat -c '%a' $fl").exec().getOut().contains(perm))
            Shell.cmd("for file in " + foldername + "*; do chmod " + permission + " \"$file\"; done").exec();
    }

    public static boolean fileExists(String dir) {
        List<String> lines = Shell.cmd("test -f " + dir + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1")) return true;
        }
        return false;
    }

    public static boolean folderExists(String dir) {
        List<String> lines = Shell.cmd("test -d " + dir + " && echo '1'").exec().getOut();
        for (String line : lines) {
            if (line.contains("1")) return true;
        }
        return false;
    }

    public static boolean deviceProperlyRooted() {
        return isDeviceRooted() && (isMagiskInstalled() || isKSUInstalled() || isApatchInstalled());
    }

    public static boolean requireMetamodule() {
        return isKSUInstalled() && getKsuVersion() >= 3 && !isMetaModuleInstalled();
    }

    public static int getKsuVersion() {
        String ksuVersion = Shell.cmd("ksud -V").exec().getOut().stream()
                .reduce("", (acc, s) -> acc + s).trim();

        Pattern pattern = Pattern.compile("\\b(\\d+)(?=\\.)");
        Matcher matcher = pattern.matcher(ksuVersion);

        if (matcher.find()) {
            String majorVersion = matcher.group(1);
            try {
                return Integer.parseInt(majorVersion);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    public static boolean isMetaModuleInstalled() {
        String command = "for d in /data/adb/modules/*; do prop=\"$d/module.prop\"; [ -f \"$prop\" ] && grep -qiE \"^metamodule[[:space:]]*=[[:space:]]*(1|true)$\" \"$prop\" && echo true && exit 0; done; echo false";
        Shell.Result result = Shell.cmd(command).exec();

        List<String> output = result.getOut();
        return !output.isEmpty() && "true".equals(output.get(0));
    }

}
