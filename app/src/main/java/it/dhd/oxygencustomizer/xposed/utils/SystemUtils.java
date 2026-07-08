package it.dhd.oxygencustomizer.xposed.utils;

import static android.content.res.Configuration.UI_MODE_NIGHT_YES;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_OPEN_QUICK_SETTINGS;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_TOGGLE_ONE_HANDED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_TOGGLE_PANEL;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_INTENT_FLASHLIGHT_TIP;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_INTENT_KILL_APP;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_INTENT_RINGER_TIP;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.FLASHLIGHT_TIP_STATE;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.RINGER_MODE_NORMAL;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.RINGER_MODE_SILENT;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.RINGER_MODE_VIBRATE;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.RINGER_TIP_MODE;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.usage.NetworkStatsManager;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.UserManager;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.os.VibratorManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.xposed.XPLauncher;

public class SystemUtils {
    private static final int THREAD_PRIORITY_BACKGROUND = 10;

    @SuppressLint("StaticFieldLeak")
    static SystemUtils instance;
    Context mContext;
    PackageManager mPackageManager;
    CameraManager mCameraManager;
    VibratorManager mVibrationManager;
    AudioManager mAudioManager;
    NotificationManager mNotificationManager;
    BatteryManager mBatteryManager;
    PowerManager mPowerManager;
    ConnectivityManager mConnectivityManager;
    TelephonyManager mTelephonyManager;
    AlarmManager mAlarmManager;
    DownloadManager mDownloadManager = null;
    NetworkStatsManager mNetworkStatsManager = null;
    boolean mHasVibrator = false;
    int maxFlashLevel = -1;
    static boolean isTorchOn = false;

    //ArrayList<ChangeListener> mFlashlightLevelListeners = new ArrayList<>();
    //ArrayList<ChangeListener> mVolumeChangeListeners = new ArrayList<>();
    private WifiManager mWifiManager;
    private BluetoothManager mBluetoothManager;
    private WindowManager mWindowManager;
    private UserManager mUserManager;

    public static void killSelf() {
        BootLoopProtector.resetCounter(android.os.Process.myProcessName());

        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public static void restartSystemUI() {
        BootLoopProtector.resetCounter("com.android.systemui");

        XPLauncher.enqueueProxyCommand(proxy -> {
            try {
                proxy.runCommand("killall com.android.systemui");
            } catch (Throwable ignored) {
            }
        });
    }

    public static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable ignored) {
        }
    }

    @Nullable
    @Contract(pure = true)
    public static AudioManager AudioManager() {
        return instance == null
                ? null
                : instance.getAudioManager();
    }

    @Nullable
    @Contract(pure = true)
    public static NotificationManager NotificationManager() {
        return instance == null
                ? null
                : instance.getNotificationManager();
    }

    @Nullable
    @Contract(pure = true)
    public static WindowManager WindowManager() {
        return instance == null
                ? null
                : instance.getWindowManager();
    }

    @Nullable
    @Contract(pure = true)
    public static PackageManager PackageManager() {
        return instance == null
                ? null
                : instance.getPackageManager();
    }

    @Nullable
    @Contract(pure = true)
    public static CameraManager CameraManager() {
        return instance == null
                ? null
                : instance.getCameraManager();
    }

    @Nullable
    @Contract(pure = true)
    public static AlarmManager AlarmManager() {
        return instance == null
                ? null
                : instance.getAlarmManager();
    }

    @Nullable
    @Contract(pure = true)
    public static BatteryManager BatteryManager() {
        return instance == null
                ? null
                : instance.getBatteryManager();
    }

    @Nullable
    @Contract(pure = true)
    public static WifiManager WifiManager() {
        return instance == null
                ? null
                : instance.getWifiManager();
    }

    @Nullable
    @Contract(pure = true)
    public static BluetoothManager BluetoothManager() {
        return instance == null
                ? null
                : instance.getBluetoothManager();
    }

    @Nullable
    @Contract(pure = true)
    public static UserManager UserManager() {
        return instance == null
                ? null
                : instance.getUserManager();
    }

    @Nullable
    @Contract(pure = true)
    public static ConnectivityManager ConnectivityManager() {
        return instance == null
                ? null
                : instance.getConnectivityManager();
    }

    private CameraManager getCameraManager() {
        if (mCameraManager == null) {
            try {
                HandlerThread thread = new HandlerThread("", THREAD_PRIORITY_BACKGROUND);
                thread.start();
                Handler mHandler = new Handler(thread.getLooper());
                mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

                mCameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                    @Override
                    public void onTorchModeChanged(@NonNull String cameraId, boolean enabled) {
                        super.onTorchModeChanged(cameraId, enabled);
                        isTorchOn = enabled;
                    }
                }, mHandler);
            } catch (Throwable t) {
                mCameraManager = null;
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mCameraManager;
    }

    private PackageManager getPackageManager() {
        if (mPackageManager == null) {
            try {
                mPackageManager = mContext.getPackageManager();
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mPackageManager;
    }

    public SystemUtils(Context context) {
        mContext = context;
        instance = this;
    }

    public static void threadSleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Throwable ignored) {
        }
    }

    private PowerManager getPowerManager() {
        if (mPowerManager == null) {
            try {
                mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log("OxygenCustomizer Error getting power manager");
                    log(t);
                }
            }
        }
        return mPowerManager;
    }

    @Nullable
    @Contract(pure = true)
    public static PowerManager PowerManager() {
        return instance == null
                ? null
                : instance.getPowerManager();
    }

    public static boolean isDarkMode() {
        return instance != null
                && instance.getIsDark();
    }

    private boolean getIsDark() {
        return (mContext.getResources().getConfiguration().uiMode & UI_MODE_NIGHT_YES) == UI_MODE_NIGHT_YES;
    }

    public static void sleep() {
        if (instance != null) {
            try {
                callMethod(PowerManager(), "goToSleep", SystemClock.uptimeMillis());
            } catch (Throwable ignored) {
            }
        }
    }

    static boolean darkSwitching = false;

    public static void doubleToggleDarkMode() {
        XPLauncher.enqueueProxyCommand(proxy -> {
            boolean isDark = isDarkMode();
            new Thread(() -> {
                try {
                    while (darkSwitching) {
                        Thread.currentThread().wait(100);
                    }
                    darkSwitching = true;

                    proxy.runCommand("cmd uimode night " + (isDark ? "no" : "yes"));
                    threadSleep(1000);
                    proxy.runCommand("cmd uimode night " + (isDark ? "yes" : "no"));

                    threadSleep(500);
                    darkSwitching = false;
                } catch (Exception ignored) {
                }
            }).start();
        });
    }

    private AudioManager getAudioManager() {
        if (mAudioManager == null) {
            try {
                mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mAudioManager;
    }

    private NotificationManager getNotificationManager() {
        if (mNotificationManager == null) {
            try {
                mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mNotificationManager;
    }

    private WindowManager getWindowManager() {
        if (mWindowManager == null) {
            try {
                mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mWindowManager;
    }

    private AlarmManager getAlarmManager() {
        if (mAlarmManager == null) {
            try {
                mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log("OxygenCustomizer Error getting alarm manager");
                    log(t);
                }
            }
        }
        return mAlarmManager;
    }

    private BatteryManager getBatteryManager() {
        if (mBatteryManager == null) {
            try {
                mBatteryManager = (BatteryManager) mContext.getSystemService(Context.BATTERY_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mBatteryManager;
    }

    private WifiManager getWifiManager() {
        if (mWifiManager == null) {
            try {
                mWifiManager = mContext.getSystemService(WifiManager.class);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log("Oxygen Customizer Error getting wifi manager");
                    log(t);
                }
            }
        }
        return mWifiManager;
    }

    private BluetoothManager getBluetoothManager() {
        if (mBluetoothManager == null) {
            try {
                mBluetoothManager = mContext.getSystemService(BluetoothManager.class);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log("Oxygen Customizer Error getting bluetooth manager");
                    log(t);
                }
            }
        }
        return mBluetoothManager;
    }

    private ConnectivityManager getConnectivityManager() {
        if (mConnectivityManager == null) {
            try {
                mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log("Oxygen Customizer Error getting connection manager");
                    log(t);
                }
            }
        }
        return mConnectivityManager;
    }

    private UserManager getUserManager() {
        if (mUserManager == null) {
            try {
                mUserManager = (UserManager) mContext.getSystemService(Context.USER_SERVICE);
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log(t);
                }
            }
        }
        return mUserManager;
    }

    private VibratorManager getVibrationManager() {
        if (mVibrationManager == null) {
            try {
                mVibrationManager = (VibratorManager) mContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
                mHasVibrator = mVibrationManager.getDefaultVibrator().hasVibrator();
            } catch (Throwable t) {
                if (BuildConfig.DEBUG) {
                    log("Oxygen Customizer Error getting vibrator");
                    log(t);
                }
            }
        }
        return mVibrationManager;
    }

    private boolean hasVibrator() {
        return getVibrationManager() != null && mHasVibrator;
    }

    public static void vibrate(int effect, @Nullable Integer vibrationUsage) {
        vibrate(VibrationEffect.createPredefined(effect), vibrationUsage);
    }

    @SuppressLint("MissingPermission")
    public static void vibrate(VibrationEffect effect, @Nullable Integer vibrationUsage) {
        if (instance == null || !instance.hasVibrator()) return;
        try {
            if (vibrationUsage != null) {
                instance.getVibrationManager().getDefaultVibrator().vibrate(effect, VibrationAttributes.createForUsage(vibrationUsage));
            } else {
                instance.getVibrationManager().getDefaultVibrator().vibrate(effect);
            }
        } catch (Exception ignored) {
        }
    }

    public static boolean isFlashOn() {
        return isTorchOn;
    }

    /**
     * Toggles flash
     * @return boolean flash state
     */
    public static boolean toggleFlash() {
        if (instance != null) {
            instance.toggleFlashInternal();
            return isFlashOn();
        }
        return false;
    }

    public static void shutdownFlash() {
        if (instance != null)
            instance.shutdownFlashInternal();
    }

    private void shutdownFlashInternal() {
        setFlashInternal(false);
    }

    private void toggleFlashInternal() {
        setFlashInternal(!isTorchOn);
    }


    private boolean supportsFlashLevelsInternal() {
        if (getCameraManager() == null) {
            return false;
        }

        try {
            String flashID = getFlashID(mCameraManager);
            if (flashID.equals("")) {
                return false;
            }
            if (maxFlashLevel == -1) {
                @SuppressWarnings("unchecked")
                CameraCharacteristics.Key<Integer> FLASH_INFO_STRENGTH_MAXIMUM_LEVEL = (CameraCharacteristics.Key<Integer>) getStaticObjectField(CameraCharacteristics.class, "FLASH_INFO_STRENGTH_MAXIMUM_LEVEL");
                maxFlashLevel = mCameraManager.getCameraCharacteristics(flashID).get(FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
            }
            return maxFlashLevel > 1;
        } catch (Throwable ignored) {
            return false;
        }
    }

    private void setFlashInternal(boolean enabled) {
        if (getCameraManager() == null)
            return;

        try {
            String flashID = getFlashID(mCameraManager);
            if (flashID.equals("")) {
                return;
            }
            if (enabled
                    && Xprefs.getBoolean("leveledFlashTile", false)
                    && Xprefs.getBoolean("isFlashLevelGlobal", false)
                    && supportsFlashLevelsInternal()) {
                float currentPct = Xprefs.getFloat("flashPCT", 0.5f);
                setFlashInternal(true, currentPct);
                return;
            }

            mCameraManager.setTorchMode(flashID, enabled);
        } catch (Throwable t) {
            if (BuildConfig.DEBUG) {
                log("Oxygen Customizer Error in setting flashlight");
                log(t);
            }
        }
    }

    private void setFlashInternal(boolean enabled, float pct) {
        if (getCameraManager() == null) {
            return;
        }

        try {
            String flashID = getFlashID(mCameraManager);
            if (flashID.equals("")) {
                return;
            }
            if (maxFlashLevel == -1) {
                @SuppressWarnings("unchecked")
                CameraCharacteristics.Key<Integer> FLASH_INFO_STRENGTH_MAXIMUM_LEVEL = (CameraCharacteristics.Key<Integer>) getStaticObjectField(CameraCharacteristics.class, "FLASH_INFO_STRENGTH_MAXIMUM_LEVEL");
                maxFlashLevel = mCameraManager.getCameraCharacteristics(flashID).get(FLASH_INFO_STRENGTH_MAXIMUM_LEVEL);
            }
            if (enabled) {
                if (maxFlashLevel > 1) //good news. we can set levels
                {
                    callMethod(mCameraManager, "turnOnTorchWithStrengthLevel", flashID, Math.max(Math.round(pct * maxFlashLevel), 1));
                } else //flash doesn't support levels: go normal
                {
                    setFlashInternal(true);
                }
            } else {
                mCameraManager.setTorchMode(flashID, false);
            }
        } catch (Throwable t) {
            if (BuildConfig.DEBUG) {
                log("Oxygen Customizer Error in setting flashlight");
                log(t);
            }
        }
    }


    private String getFlashID(@NonNull CameraManager cameraManager) throws CameraAccessException {
        String[] ids = cameraManager.getCameraIdList();
        for (String id : ids) {
            if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK) {
                if (cameraManager.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    return id;
                }
            }
        }
        return "";
    }

    /**
     * Toggles ringer mode between Normal, Vibrate, Silent
     * Issues the capsule special notifications
     * @return current mode integer (coincidentally, also the number of tickles in Buttons.java)
     */
    public static int toggleRingerMode() {
        AudioManager am = AudioManager();

        if (am != null) {
            int mode = am.getRingerMode();
            // Cycle ringer, vibrate and silent
            if (mode == AudioManager.RINGER_MODE_VIBRATE) {
                // Vibrate -> Silent

                // Avoid this or we get Vibrate + DND
                // NotificationManager nm = NotificationManager();
                //am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                // if(nm != null)
                //     nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);

                try {
                    // Call hidden API or we get Vibrate + DND
                    de.robv.android.xposed.XposedHelpers.callMethod(
                            am,
                            "setRingerModeInternal",
                            AudioManager.RINGER_MODE_SILENT
                    );
                } catch (Throwable t) {
                    // Fallback
                    am.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    XposedBridge.log("OxygenCustomizer Error: Impossibile usare setRingerModeInternal - " + t.getMessage());
                }

                sendRingerTipIntent(RINGER_MODE_SILENT);
                return RINGER_MODE_SILENT;
            } else if (mode == AudioManager.RINGER_MODE_NORMAL) {
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                sendRingerTipIntent(RINGER_MODE_VIBRATE);
                return RINGER_MODE_VIBRATE;
            } else {
                am.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                sendRingerTipIntent(RINGER_MODE_NORMAL);
                return RINGER_MODE_NORMAL;
            }

        }
        return 0;
    }

    public static void sendRingerTipIntent(int ringerMode) {
        Intent ringer = new Intent(ACTION_INTENT_RINGER_TIP);
        ringer.setPackage(SYSTEM_UI);
        ringer.putExtra(RINGER_TIP_MODE, ringerMode);
        ringer.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        instance.mContext.sendBroadcast(ringer);
    }

    public static void sendFlashIntent(boolean onOff){
        Intent flashIntent = new Intent(ACTION_INTENT_FLASHLIGHT_TIP);
        flashIntent.setPackage(SYSTEM_UI);
        flashIntent.putExtra(FLASHLIGHT_TIP_STATE, onOff ? 1 : 2);
        flashIntent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        instance.mContext.sendBroadcast(flashIntent);
    }

    /**
     * Toggles Dnd
     * @return Dnd state on/off
     */
    public static boolean toggleDnd() {
        if (instance == null) return false;
        NotificationManager nm = (NotificationManager) instance.mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        if (nm != null && nm.isNotificationPolicyAccessGranted()) {
            int filter = nm.getCurrentInterruptionFilter();
            if (filter == NotificationManager.INTERRUPTION_FILTER_ALL) {
                // Enable DND
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                return true;
            } else {
                // Disable DND
                nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL);
                return false;
            }
        }
        return false;
    }

    public static void toggleNotifications() {
        Intent intent = new Intent(ACTIONS_TOGGLE_PANEL);
        intent.setPackage(LAUNCHER);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        instance.mContext.sendBroadcast(intent);
    }

    public static void toggleOneHanded() {
        Intent intent = new Intent(ACTIONS_TOGGLE_ONE_HANDED);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setPackage(LAUNCHER);
        instance.mContext.sendBroadcast(intent);
    }

    public static void runCircleToSearch() {
        new Handler(Looper.getMainLooper()).postDelayed(AppUtils::circleToSearch, 150L);
    }

    public static void takeScreenshot(ScreenshotUtils.ScreenshotType type) {
        ScreenshotUtils.takeScreenshot(type, instance.mContext, 250L);
    }

    public static void openQs() {
        Intent intent = new Intent(ACTIONS_OPEN_QUICK_SETTINGS);
        intent.setPackage(SYSTEM_UI);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        instance.mContext.sendBroadcast(intent);
    }

    public static void killForeground() {
        Intent intent = new Intent(ACTION_INTENT_KILL_APP);
        intent.setPackage(SYSTEM_UI);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        instance.mContext.sendBroadcast(intent);
    }

    public static void goToSleep() {
        callMethod(SystemUtils.PowerManager(), "goToSleep", SystemClock.uptimeMillis());
    }

}
