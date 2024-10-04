package it.dhd.oxygencustomizer.utils.overlay;

import static it.dhd.oxygencustomizer.utils.PreferenceHelper.getModulePrefs;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.utils.ModuleConstants;
import it.dhd.oxygencustomizer.utils.Prefs;

public class OverlayUtil {

    public static List<String> getOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '....OxygenCustomizerComponent' | sed -E 's/^....//'").exec().getOut();
    }

    public static List<String> getEnabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '.x..OxygenCustomizerComponent' | sed -E 's/^.x..//'").exec().getOut();
    }

    public static List<String> getDisabledOverlayList() {
        return Shell.cmd("cmd overlay list |  grep -E '. ..OxygenCustomizerComponent' | sed -E 's/^. ..//'").exec().getOut();
    }

    public static List<String> getOverlayForComponent(String componentName) {
        return Shell.cmd("cmd overlay list | grep '....OxygenCustomizerComponent" + componentName + "'").exec().getOut();
    }

    public static boolean isOverlayEnabled(String pkgName) {
        return Shell.cmd("[[ $(cmd overlay list | grep -o '\\[x\\] " + pkgName + "') ]] && echo 1 || echo 0").exec().getOut().get(0).equals("1");
    }

    public static boolean isOverlayDisabled(String pkgName) {
        return !isOverlayEnabled(pkgName);
    }

    public static void checkOverlayEnabledAndEnable(String componentName) {
        List<String> component = Shell.cmd("cmd overlay list | grep \".x..OxygenCustomizerComponent" + componentName + "\"").exec().getOut();
        if (!component.isEmpty()) {
            String num = component.get(0).split("OxygenCustomizerComponent" + componentName)[1].split("\\.overlay")[0];
            enableOverlay("OxygenCustomizerComponent" + componentName + num + ".overlay");
            Log.d("OverlayUtil", "checkOverlayEnabledAndEnable: Enabled " + componentName + num);
        }
    }

    static boolean isOverlayInstalled(List<String> enabledOverlays, String pkgName) {
        for (String line : enabledOverlays) {
            if (line.equals(pkgName)) return true;
        }
        return false;
    }

    public static void enableOverlay(String pkgName) {
        Prefs.putBoolean(pkgName, true);
        if (getModulePrefs() != null) {
            getModulePrefs().edit().putBoolean(pkgName, true).apply();
        }
        Shell.cmd("cmd overlay enable --user current " + pkgName, "cmd overlay set-priority " + pkgName + " highest").submit();
    }

    public static void enableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, true);
            command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void enableOverlayExclusiveInCategory(String pkgName) {
        Prefs.putBoolean(pkgName, true);
        Shell.cmd("cmd overlay enable-exclusive --user current --category " + pkgName, "cmd overlay set-priority " + pkgName + " highest").submit();
    }

    public static void enableOverlaysExclusiveInCategory(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, true);
            command.append("cmd overlay enable-exclusive --user current --category ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void disableOverlay(String pkgName) {
        Prefs.putBoolean(pkgName, false);
        if (getModulePrefs() != null) {
            getModulePrefs().edit().putBoolean(pkgName, false).apply();
        }
        Shell.cmd("cmd overlay disable --user current " + pkgName).submit();
    }

    public static void disableOverlays(String... pkgNames) {
        StringBuilder command = new StringBuilder();

        for (String pkgName : pkgNames) {
            Prefs.putBoolean(pkgName, false);
            command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static void changeOverlayState(Object... args) {
        if (args.length % 2 != 0) {
            throw new IllegalArgumentException("Number of arguments must be even.");
        }

        StringBuilder command = new StringBuilder();

        for (int i = 0; i < args.length; i += 2) {
            String pkgName = (String) args[i];
            boolean state = (boolean) args[i + 1];

            Prefs.putBoolean(pkgName, state);

            if (state) {
                command.append("cmd overlay enable --user current ").append(pkgName).append("; cmd overlay set-priority ").append(pkgName).append(" highest; ");
            } else {
                command.append("cmd overlay disable --user current ").append(pkgName).append("; ");
            }
        }

        Shell.cmd(command.toString().trim()).submit();
    }

    public static boolean overlayExists() {
        return Shell.cmd("[ -f /system/product/overlay/OxygenCustomizerComponentOCV.apk ] && echo \"found\" || echo \"not found\"").exec().getOut().get(0).equals("found");
    }

    public static boolean overlayExist(String overlayName) {
        return Shell.cmd("[ -f /system/product/overlay/OxygenCustomizerComponent" + overlayName + ".apk ] && echo \"found\" || echo \"not found\"").exec().getOut().get(0).equals("found");
    }

    @SuppressWarnings("unused")
    public static boolean matchOverlayAgainstAssets() {
        try {
            String[] packages = OxygenCustomizer.getAppContext().getAssets().list("Overlays");
            int numberOfOverlaysInAssets = 0;

            assert packages != null;
            for (String overlay : packages) {
                numberOfOverlaysInAssets += Objects.requireNonNull(OxygenCustomizer.getAppContext().getAssets().list("Overlays/" + overlay)).length;
            }

            int numberOfOverlaysInstalled = Integer.parseInt(Shell.cmd("find /" + ModuleConstants.OVERLAY_DIR + "/ -maxdepth 1 -type f -print| wc -l").exec().getOut().get(0));
            return numberOfOverlaysInAssets <= numberOfOverlaysInstalled;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Drawable getDrawableFromOverlay(Context context, String pkg, String drawableName) {
        try {
            PackageManager pm = context.getPackageManager();
            Resources res = pm.getResourcesForApplication(pkg);
            int resId = res.getIdentifier(drawableName, "drawable", pkg);
            if (resId != 0X0)
                return res.getDrawable(resId);
            else
                return null;
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("OverlayUtil", "getDrawableFromOverlay: Package Not Found " + e.getMessage());
            return null;
        }
    }

    public static String getStringFromOverlay(Context context, String pkg, String stringName) {
        try {
            PackageManager pm = context.getPackageManager();
            Resources res = pm.getResourcesForApplication(pkg);
            int resId = res.getIdentifier(stringName, "string", pkg);
            if (resId != 0X0)
                return res.getString(resId);
            else
                return null;
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e("OverlayUtil", "getStringFromOverlay: Package Not Found" + e.getMessage());
            return null;
        }
    }


}
