package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;


import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;

public class ActivityLauncherUtils {

    private final static String PERSONALIZATIONS_ACTIVITY = "com.android.settings.Settings$personalizationSettingsLayoutActivity";

    private final Context mContext;
    private final Object mActivityStarter;
    private final PackageManager mPackageManager;

    public ActivityLauncherUtils(Context context, Object activityStarter) {
        this.mContext = context;
        this.mActivityStarter = activityStarter;
        mPackageManager = mContext.getPackageManager();
    }

    public String getInstalledMusicApp() {
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_APP_MUSIC);
        final  List<ResolveInfo> musicApps = mPackageManager.queryIntentActivities(intent, 0);
        ResolveInfo musicApp = musicApps.isEmpty() ? null : musicApps.get(0);
        return musicApp != null ? musicApp.activityInfo.packageName : "";
    }

    private void launchAppIfAvailable(Intent launchIntent, @StringRes int appTypeResId) {
        final List<ResolveInfo> apps = mPackageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (!apps.isEmpty()) {
            callMethod(mActivityStarter, "startActivity", launchIntent, false);
        } else {
            if (appTypeResId != 0) showNoDefaultAppFoundToast(appTypeResId);
        }
    }

    public void launchCamera() {
        final Intent launchIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        launchAppIfAvailable(launchIntent, R.string.camera);
    }

    public void launchTimer() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SHOW_ALARMS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(intent, R.string.clock_timer);
    }

    public void launchCalculator() {
        Intent launchIntent = new Intent();
        if (AppUtils.isAppInstalled(mContext, "com.oneplus.calculator")) {
            launchIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.oneplus.calculator");
        } else {
            launchIntent.setAction(Intent.ACTION_MAIN);
            launchIntent.addCategory(Intent.CATEGORY_APP_CALCULATOR);
        }
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(launchIntent, R.string.calculator);
    }

    public void launchSettingsComponent(String className) {
        if (mActivityStarter == null) return;
        Intent intent = className.equals(PERSONALIZATIONS_ACTIVITY) ? new Intent(Intent.ACTION_MAIN) : new Intent();
        intent.setComponent(new ComponentName("com.android.settings", className));
        callMethod(mActivityStarter, "startActivity", intent, true);
    }

    public void launchAudioSettings() {
        final Intent launchIntent = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
        launchAppIfAvailable(launchIntent, 0);
    }

    public void startSettingsActivity() {
        if (mActivityStarter == null) return;
        callMethod(mActivityStarter, "startActivity", new Intent(android.provider.Settings.ACTION_SETTINGS), true);
    }

    private void showNoDefaultAppFoundToast(@StringRes int appTypeResId) {
        Toast.makeText(mContext, modRes.getString(appTypeResId) + " not found", Toast.LENGTH_SHORT).show();
    }
}
