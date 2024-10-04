package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getCalculatorTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getCameraGestureHelper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.provider.MediaStore;
import android.provider.Settings;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
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

    public void launchAppIfAvailable(Intent launchIntent, @StringRes int appTypeResId, boolean fromQs) {
        final List<ResolveInfo> apps = mPackageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_DEFAULT_ONLY);
        if (mActivityStarter == null) {
            log("ActivityStarter is null");
            return;
        }
        if (!apps.isEmpty()) {
            callMethod(mActivityStarter, fromQs ? "postStartActivityDismissingKeyguard" : "startActivity", launchIntent, fromQs ? 0 : false);
        } else {
            if (appTypeResId != 0) showNoDefaultAppFoundToast(appTypeResId);
        }
    }

    /**
     * Launches an app using the ActivityStarter
     * dismissing the shade.
     * Used for launching apps from Quick Settings.
     * @param launchIntent The intent to launch the app
     */
    public void launchApp(Intent launchIntent) {
        if (mActivityStarter == null) {
            log("ActivityStarter is null");
            return;
        }
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", launchIntent, 0 /* dismissShade */);
    }

    /**
     * Launches the weather settings page {@link it.dhd.oxygencustomizer.ui.fragments.mods.WeatherSettings}
     * @param fromQs Whether the intent is launched from Quick Settings
     */
    public void launchWeatherSettings(boolean fromQs) {
        final Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".ui.activity.MainActivity"));
        launchIntent.putExtra("openWeatherSettings", true);
        if (mActivityStarter == null) {
            log("ActivityStarter is null");
            return;
        }
        callMethod(mActivityStarter, fromQs ? "postStartActivityDismissingKeyguard" : "startActivity", launchIntent, fromQs ? 0 : false);
    }

    /**
     * Launches the weather activity {@link it.dhd.oxygencustomizer.ui.activity.WeatherActivity}
     * @param fromQs Whether the intent is launched from Quick Settings
     */
    public void launchWeatherActivity(boolean fromQs) {
        final Intent launchIntent = new Intent();
        launchIntent.setComponent(new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".ui.activity.WeatherActivity"));
        if (mActivityStarter == null) {
            log("ActivityStarter is null");
            return;
        }
        callMethod(mActivityStarter, fromQs ? "postStartActivityDismissingKeyguard" : "startActivity", launchIntent, fromQs ? 0 : false);
    }

    public void launchCamera(boolean fromQs) {
        Object mCameraGestureHelper = getCameraGestureHelper();

        if (mCameraGestureHelper != null) {
            callMethod(mCameraGestureHelper, "launchCamera", 3);
        } else {
            final Intent launchIntent = new Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA_SECURE);
            launchAppIfAvailable(launchIntent, R.string.camera, fromQs);
        }
    }

    public void launchTimer(boolean fromQs) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SHOW_ALARMS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(intent, R.string.clock_timer, fromQs);
    }

    public void launchCalculator() {
        // If the calculator tile is available
        // we can use it to open the calculator
        Object calculatorTile = getCalculatorTile();
        if (calculatorTile != null) {
            callMethod(calculatorTile, "openCalculator");
            return;
        }

        // Otherwise we try to launch the calculator app
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
        launchAppIfAvailable(launchIntent, R.string.calculator, false);
    }

    public void launchWallet() {
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage("com.google.android.apps.walletnfcrel");
        if (launchIntent != null) launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(launchIntent, R.string.wallet, false);
    }

    public void launchSettingsComponent(String className) {
        if (mActivityStarter == null) return;
        Intent intent = className.equals(PERSONALIZATIONS_ACTIVITY) ? new Intent(Intent.ACTION_MAIN) : new Intent();
        intent.setComponent(new ComponentName("com.android.settings", className));
        callMethod(mActivityStarter, "startActivity", intent, true);
    }

    public void launchAudioSettings(boolean fromQs) {
        final Intent launchIntent = new Intent(android.provider.Settings.ACTION_SOUND_SETTINGS);
        launchAppIfAvailable(launchIntent, 0, fromQs);
    }

    public void startSettingsActivity() {
        if (mActivityStarter == null) return;
        callMethod(mActivityStarter, "startActivity", new Intent(android.provider.Settings.ACTION_SETTINGS), true);
    }

    public void launchWifiSettings() {
        final Intent launchIntent = new Intent(Settings.ACTION_WIFI_SETTINGS);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(launchIntent, 0, false);
    }

    public void launchInternetSettings() {
        final Intent launchIntent = new Intent(Settings.ACTION_NETWORK_OPERATOR_SETTINGS);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(launchIntent, 0, false);
    }

    public void launchBluetoothSettings() {
        final Intent launchIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        launchAppIfAvailable(launchIntent, 0, false);
    }

    public void launchHotspotSettings() {
        launchSettingsComponent("com.android.settings.TetherSettings");
    }

    private void showNoDefaultAppFoundToast(@StringRes int appTypeResId) {
        Toast.makeText(mContext, modRes.getString(appTypeResId) + " not found", Toast.LENGTH_SHORT).show();
    }
}
