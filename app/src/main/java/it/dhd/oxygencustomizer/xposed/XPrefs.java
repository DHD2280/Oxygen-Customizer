package it.dhd.oxygencustomizer.xposed;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;

import com.crossbowffs.remotepreferences.RemotePreferenceAccessException;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedRemotePreferences;

public class XPrefs {

    @SuppressLint("StaticFieldLeak")
    public static ExtendedRemotePreferences Xprefs;
    private static String packageName;

    private static final int MAX_RELOAD_ATTEMPTS = 3;
    private static final long RELOAD_RETRY_MS = 1000L;
    private static final Handler handler = new Handler(Looper.getMainLooper());

    private static final SharedPreferences.OnSharedPreferenceChangeListener listener = (sharedPreferences, key) -> reload(key, 1);

    private static void reload(String key, int attempt) {
        try {
            loadEverything(packageName, key);
        } catch (RemotePreferenceAccessException e) {
            if (attempt < MAX_RELOAD_ATTEMPTS) {
                handler.postDelayed(() -> reload(key, attempt + 1), RELOAD_RETRY_MS);
            }
        }
    }
    public static void init(Context context) {
        packageName = context.getPackageName();

        Xprefs = (ExtendedRemotePreferences) new ExtendedRemotePreferences(context, BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + "_preferences", true);

        Xprefs.registerOnSharedPreferenceChangeListener(listener);
    }


    public static void loadEverything(String packageName, String... key) {
        if (key.length > 0 && (key[0] == null || Constants.Preferences.General.PREF_UPDATE_EXCLUSIONS.stream().anyMatch(exclusion -> key[0].startsWith(exclusion))))
            return;

        boolean moreLogging = Xprefs.getBoolean(Constants.Preferences.General.PREF_MORE_LOGGING, false);

        for (XposedMods thisMod : XPLauncher.runningMods) {
            thisMod.mDebug = BuildConfig.VERSION_NAME.contains("nightly") || moreLogging;
            thisMod.onPreferenceUpdated(key);
        }
    }
}
