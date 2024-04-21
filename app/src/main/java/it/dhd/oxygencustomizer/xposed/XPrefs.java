package it.dhd.oxygencustomizer.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedRemotePreferences;

public class XPrefs {

    @SuppressLint("StaticFieldLeak")
    public static ExtendedRemotePreferences Xprefs;
    private static String packageName;

    private static final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> loadEverything(packageName, key);
    public static void init(Context context) {
        packageName = context.getPackageName();

        Xprefs = (ExtendedRemotePreferences) new ExtendedRemotePreferences(context, BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + "_preferences", true);

        Xprefs.registerOnSharedPreferenceChangeListener(listener);
    }


    public static void loadEverything(String packageName, String... key) {
        if (key.length > 0 && (key[0] == null || Constants.Preferences.General.PREF_UPDATE_EXCLUSIONS.stream().anyMatch(exclusion -> key[0].startsWith(exclusion))))
            return;

        setPackagePrefs(packageName);

        for (XposedMods thisMod : XPLauncher.runningMods) {
            thisMod.updatePrefs(key);
        }
    }

    public static void setPackagePrefs(String packageName) {
        if (Constants.Packages.SYSTEM_UI.equals(packageName) && !XPLauncher.isChildProcess) {
            //Overlays.setAll(false);
        }
    }
}
