package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getCamerGestureHelper;

import android.content.Context;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.StringRes;


import java.util.List;

import it.dhd.oxygencustomizer.R;

public class ActivityLauncherUtils {

    private final static String PERSONALIZATIONS_ACTIVITY = "com.android.settings.Settings$personalizationSettingsLayoutActivity";

    private final Context mContext;
    private final Object mActivityStarter;
    private final PackageManager mPackageManager;

    private final String[] mCalculatorApps = new String[]{
            "com.oneplus.calculator", "com.coloros.calculator"
    };

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
        Object mCameraGestureHelper = getCamerGestureHelper();

        if (mCameraGestureHelper != null) {
            callMethod(mCameraGestureHelper, "launchCamera", 3);
        } else {
            final Intent launchIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            launchAppIfAvailable(launchIntent, R.string.camera);
        }
    }

    public void launchTimer() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SHOW_ALARMS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(intent, R.string.clock_timer);
    }

    public void launchCalculator() {
        Intent launchIntent = null;
        for (String packageName : mCalculatorApps) {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
            if (intent != null) {
                launchIntent = intent;
                break;
            }
        }

        if (launchIntent == null) {
            launchIntent = new Intent();
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
