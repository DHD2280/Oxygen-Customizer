package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Buttons.BUTTONS_VOLUME_MUSIC;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.DISABLE_POWER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;
import android.os.VibrationAttributes;
import android.os.VibrationEffect;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ScreenshotUtils;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class Buttons extends XposedMods {

    private static boolean holdVolumeToSkip = false;
    private static boolean holdVolumeToTorch = false;
    private static boolean volumeToTorchHasTimeout = false;
    private static Object PWMExImpl = null;
    private static boolean volumeToTorchProximity = false;
    private static SensorManager sensorManager;
    private static Sensor proximitySensor;
    private static SensorEventListener proximitySensorListener;
    private static boolean shouldTorch = true;
    private static Object PWM;
    Handler mHandler;
    private long wakeTime = 0;
    //    private boolean isVolumeLongPress = false;
    private boolean isVolDown = false;
    private boolean disablePowerOnLockscreen = false;
    private boolean broadcastRegistered = false;
    private int volumeToTorchTimeout = 5000;
    private boolean settingsUpdated = false;
    private String actionValueSingle, actionValueDouble, actionValueTriple, actionValueLong = "none";
    private boolean singlePressEnabled, doublePressEnabled, triplePressEnabled, longPressEnabled = false;
    private final int KEYCODE_PLUSKEY_SHORT_PRESS = 781;
    private final int KEYCODE_PLUSKEY_LONG_PRESS = 782;
    private int pressCount = 0;
    private long plusKeyTimeout = 250;

    final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action == null) return;
                String className = intent.getStringExtra("class");
                if (action.equals(Constants.ACTION_SETTINGS_CHANGED)) {
                    if (!TextUtils.isEmpty(className) && className.equals(Buttons.class.getSimpleName())) {
                        log("Buttons: Intent received - will update preferences");
                        settingsUpdated = false;
                        updatePrefs();
                    }
                }
            } catch (Throwable t) {
                log("Oxygen Customizer - Buttons: " + t.getMessage());
            }
        }
    };

    private boolean isAnyShortPressEnabled() {
        return singlePressEnabled || doublePressEnabled || triplePressEnabled;
    }

    private boolean isLongPressEnabled() {
        return longPressEnabled;
    }

    private final Runnable actionRunnable = () -> {
        int count = pressCount;
        log("PlusKey LOG: actionRunnable START. Current pressCount read as: " + count);

        // Reset count immediately for next sequence
        pressCount = 0;
        log("PlusKey LOG: actionRunnable. pressCount reset to 0");

        if (mContext == null) {
            XposedBridge.log("PlusKey LOG: actionRunnable ABORT. Context from WeakReference is NULL");
            return;
        }

        boolean shouldExecute = false;
        if (count == 1 && singlePressEnabled) shouldExecute = true;
        else if (count == 2 && doublePressEnabled) shouldExecute = true;
        else if (count >= 3 && triplePressEnabled) shouldExecute = true;

        log("PlusKey LOG: actionRunnable evaluation. count=" + count +
                ", single=" + singlePressEnabled + ", double=" + doublePressEnabled +
                ", triple=" + triplePressEnabled + " -> shouldExecute=" + shouldExecute);

        if (shouldExecute) {
            executeAction(count);
        } else {
            XposedBridge.log("PlusKey LOG: actionRunnable. No enabled action matched count=" + count);
        }
    };

    /**
     * Call this when the short press key (781) ACTION_UP is detected.
     */
    private void ShortPressDetected() {
        log("PlusKey LOG: ShortPressDetected ENTRY. Current pressCount before increment: " + pressCount);

        if (!singlePressEnabled && !doublePressEnabled && !triplePressEnabled) {
            log("PlusKey LOG: ShortPressDetected EXIT. No short press actions are enabled in settings.");
            return;
        }

        pressCount++;
        log("PlusKey LOG: ShortPressDetected. pressCount incremented to: " + pressCount);

        log("PlusKey LOG: ShortPressDetected. Calling handler.removeCallbacks(actionRunnable)");
        if (mHandler.hasCallbacks(actionRunnable)) mHandler.removeCallbacks(actionRunnable);

        // Check if we should execute immediately
        boolean hasMultiPressActions = doublePressEnabled || triplePressEnabled;
        boolean isOnlySingleEnabled = singlePressEnabled && !hasMultiPressActions;

        log("PlusKey LOG: ShortPressDetected. isOnlySingleEnabled=" + isOnlySingleEnabled + ", hasMultiPressActions=" + hasMultiPressActions + " (pressCount=" + pressCount + ")");

        if (isOnlySingleEnabled || pressCount >= 3 || (pressCount == 2 && !triplePressEnabled)) {
            log("PlusKey LOG: ShortPressDetected. TRIGGERING IMMEDIATE EXECUTION (runnable.run())");
            actionRunnable.run();
        } else {
            log("PlusKey LOG: ShortPressDetected. SCHEDULING DELAYED EXECUTION (postDelayed) with timeout: " + plusKeyTimeout);
            mHandler.postDelayed(actionRunnable, plusKeyTimeout);
        }
    }

    private void executeAction(int count) {
        try {
            String key = switch (count) {
                case 0 -> actionValueLong;
                case 1 -> actionValueSingle;
                case 2 -> actionValueDouble;
                case 3 -> actionValueTriple;
                default -> "";
            };

            execute(key);
        } catch (Throwable t) {
            log(t);
        }
    }

    public Buttons(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

        if (settingsUpdated) return;

        holdVolumeToSkip = Xprefs.getBoolean(BUTTONS_VOLUME_MUSIC, false);
        disablePowerOnLockscreen = Xprefs.getBoolean(DISABLE_POWER, false);
        holdVolumeToTorch = Xprefs.getBoolean("volbtn_torch", false);
        volumeToTorchHasTimeout = Xprefs.getBoolean("volbtn_torch_enable_timeout", false);
        volumeToTorchTimeout = Xprefs.getSliderInt("volbtn_torch_timeout", 5) * 1000;
        volumeToTorchProximity = Xprefs.getBoolean("volbtn_torch_use_proximity", false);

        actionValueSingle = Xprefs.getString("plusKey_single_press_button_action_value", "none");
        singlePressEnabled = !TextUtils.isEmpty(actionValueSingle) && !actionValueSingle.equals("none");

        actionValueDouble = Xprefs.getString("plusKey_double_press_button_action_value", "none");
        doublePressEnabled = !TextUtils.isEmpty(actionValueDouble) && !actionValueDouble.equals("none");

        actionValueTriple = Xprefs.getString("plusKey_triple_press_button_action_value", "none");
        triplePressEnabled = !TextUtils.isEmpty(actionValueTriple) && !actionValueTriple.equals("none");

        actionValueLong = Xprefs.getString("plusKey_long_press_button_action_value", "none");
        longPressEnabled = !TextUtils.isEmpty(actionValueLong) && !actionValueLong.equals("none");

        plusKeyTimeout = Xprefs.getSliderInt("plusKey_press_button_action_timeout", 250);

        settingsUpdated = true;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!broadcastRegistered) {
            broadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Constants.ACTION_SETTINGS_CHANGED);
            mContext.registerReceiver(broadcastReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }

        Class<?> PhoneWindowManagerClass;
        Class<?> PhoneWindowManagerExtImpl;
        Method overrideInterceptKeyBeforeQueueing; // PhoneWindowManagerExtImpl
        Method overrideShowGlobalActionsInternal;  // PhoneWindowManagerExtImpl

        try {
            PhoneWindowManagerClass = findClass("com.android.server.policy.PhoneWindowManager", lpparam.classLoader);
            PhoneWindowManagerExtImpl = findClass("com.android.server.policy.PhoneWindowManagerExtImpl", lpparam.classLoader);

            overrideInterceptKeyBeforeQueueing = findMethodExact(PhoneWindowManagerExtImpl, "overrideInterceptKeyBeforeQueueing", KeyEvent.class, int.class);
            overrideShowGlobalActionsInternal = findMethodExact(PhoneWindowManagerExtImpl, "overrideShowGlobalActionsInternal");

            Runnable mVolumeLongPress = () -> {
                try {
                    Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
                    KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, (isVolDown) ? KeyEvent.KEYCODE_MEDIA_PREVIOUS : KeyEvent.KEYCODE_MEDIA_NEXT, 0);
                    keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                    KeyEvent mediaEvent = new KeyEvent(KeyEvent.ACTION_DOWN, (isVolDown) ? KeyEvent.KEYCODE_MEDIA_PREVIOUS : KeyEvent.KEYCODE_MEDIA_NEXT);
                    SystemUtils.AudioManager().dispatchMediaKeyEvent(mediaEvent);

                    mediaEvent = KeyEvent.changeAction(mediaEvent, KeyEvent.ACTION_UP);
                    keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
                    SystemUtils.AudioManager().dispatchMediaKeyEvent(mediaEvent);

                    SystemUtils.vibrate(VibrationEffect.EFFECT_TICK, VibrationAttributes.USAGE_COMMUNICATION_REQUEST);
                } catch (Throwable t) {
                    log(" ERROR IN mVolumeLongPress\n" + t);
                }
            };


            Runnable mToggleFlash = () -> {
                try {
                    if (SystemUtils.isFlashOn()) {
                        SystemUtils.shutdownFlash();

                        SystemUtils.vibrate(VibrationEffect.EFFECT_TICK, VibrationAttributes.USAGE_ACCESSIBILITY);

                    }
                } catch (Throwable ignored) {
                }
            };

            Runnable mVolumeLongPressTorch = () -> {
                try {
                    if (volumeToTorchProximity) {
                        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
                        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
                        if (proximitySensor != null) {
                            sensorManager.registerListener(proximitySensorListener, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
                        }
                        if (proximitySensor == null) {
                            // nothing to do
                            shouldTorch = true;
                        } else {
                            proximitySensorListener = new SensorEventListener() {
                                @Override
                                public void onSensorChanged(SensorEvent event) {
                                    float distance = event.values[0];
                                    shouldTorch = !(distance < proximitySensor.getMaximumRange());
                                }

                                @Override
                                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                                }
                            };
                        }
                        if (proximitySensor != null) {
                            sensorManager.unregisterListener(proximitySensorListener);
                        }
                    } else {
                        shouldTorch = true;
                    }
                    if (volumeToTorchProximity && !shouldTorch) {
                        return;
                    }
                    SystemUtils.toggleFlash();
                    SystemUtils.vibrate(VibrationEffect.EFFECT_TICK, VibrationAttributes.USAGE_ACCESSIBILITY);
                    if (mHandler.hasCallbacks(mToggleFlash)) mHandler.removeCallbacks(mToggleFlash);
                    if (volumeToTorchHasTimeout && SystemUtils.isFlashOn()) {
                        mHandler.postDelayed(mToggleFlash, volumeToTorchTimeout);
                    }
                } catch (Throwable t) {
                    log(" ERROR IN mVolumeLongPressTorch\n" + t);
                }
            };

            try {
                hookAllConstructors(PhoneWindowManagerClass, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        PWM = param.thisObject;
                    }
                });
            } catch (Throwable t) {
                log(t);
            }

            hookAllMethods(PhoneWindowManagerExtImpl, "overrideInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        PWMExImpl = param.thisObject;
                    } catch (Throwable ignored) {
                        PWMExImpl = null;
                    }
                }
            });

            hookMethod(overrideInterceptKeyBeforeQueueing, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!holdVolumeToSkip && !holdVolumeToTorch) return;

                    try {
                        Object mBase = getObjectField(param.thisObject, "mBase");
                        if (mHandler == null)
                            mHandler = (Handler) getObjectField(mBase, "mHandler");

                        KeyEvent e = (KeyEvent) param.args[0];
                        int Keycode = e.getKeyCode();

                        switch (e.getAction()) {
                            case KeyEvent.ACTION_UP -> {
                                if (mHandler.hasCallbacks(mVolumeLongPress) || mHandler.hasCallbacks(mVolumeLongPressTorch)) {
                                    SystemUtils.AudioManager().adjustStreamVolume(AudioManager.STREAM_MUSIC, Keycode == KeyEvent.KEYCODE_VOLUME_DOWN ? AudioManager.ADJUST_LOWER : AudioManager.ADJUST_RAISE, 0);
                                    if (mHandler.hasCallbacks(mVolumeLongPress))
                                        mHandler.removeCallbacks(mVolumeLongPress);
                                    if (mHandler.hasCallbacks(mVolumeLongPressTorch))
                                        mHandler.removeCallbacks(mVolumeLongPressTorch);
                                    if (mHandler.hasCallbacks(mToggleFlash))
                                        mHandler.removeCallbacks(mToggleFlash);
                                }
                            }
                            case KeyEvent.ACTION_DOWN -> {
                                if (!SystemUtils.PowerManager().isInteractive() &&
                                        (Keycode == KeyEvent.KEYCODE_VOLUME_DOWN ||
                                                Keycode == KeyEvent.KEYCODE_VOLUME_UP)) {
                                    if (SystemUtils.AudioManager().isMusicActive() && holdVolumeToSkip) {
                                        isVolDown = (Keycode == KeyEvent.KEYCODE_VOLUME_DOWN);
                                        mHandler.postDelayed(mVolumeLongPress, ViewConfiguration.getLongPressTimeout());
                                        param.setResult(0);
                                    } else {
                                        int audioMode = SystemUtils.AudioManager().getMode();
                                        if (audioMode == AudioManager.MODE_IN_CALL ||
                                                audioMode == AudioManager.MODE_IN_COMMUNICATION ||
                                                audioMode == AudioManager.MODE_RINGTONE) return;
                                        if (holdVolumeToTorch) {
                                            mHandler.postDelayed(mVolumeLongPressTorch, ViewConfiguration.getLongPressTimeout());
                                            param.setResult(0);
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Throwable t) {
                        log(" ERROR IN interceptKeyBeforeQueueing\n" + t);
                    }
                }
            });


            hookMethod(overrideInterceptKeyBeforeQueueing, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!isAnyShortPressEnabled() && !isLongPressEnabled()) return;
                    try {
                        Object mBase = getObjectField(param.thisObject, "mBase");
                        if (mHandler == null)
                            mHandler = (Handler) getObjectField(mBase, "mHandler");
                        KeyEvent event = (KeyEvent) param.args[0];
                        int keyCode = event.getKeyCode();

                        if (keyCode == KEYCODE_PLUSKEY_SHORT_PRESS) {
                            // Check if any multi-press action is enabled in settings
                            if (isAnyShortPressEnabled()) {
                                if (event.getAction() == KeyEvent.ACTION_UP) {
                                    ShortPressDetected();
                                }
                                param.setResult(0); // Consume the event

                            }
                        } else if (keyCode == KEYCODE_PLUSKEY_LONG_PRESS) {
                            if (isLongPressEnabled()) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    executeAction(0);
                                }
                                param.setResult(0); // Consume the event
                            }
                        }
                    } catch (Throwable t) {
                        log(" ERROR IN PlusKey hook: " + t.getMessage());
                    }
                }
            });

            hookAllMethods(PhoneWindowManagerClass, "startedWakingUp", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    if (!holdVolumeToTorch) return;
                    int r = (int) param.args[param.args.length - 1];

                    if (r == 1) {
                        wakeTime = SystemClock.uptimeMillis();
                    }
                }
            });


            hookMethod(overrideShowGlobalActionsInternal, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!disablePowerOnLockscreen) return;
                    Object mBase = getObjectField(param.thisObject, "mBase");
                    if (mBase == null) return;
                    int mCurrentUserId = getIntField(param.thisObject, "mCurrentUserId");
                    if (disablePowerOnLockscreen &&
                            (boolean) callMethod(mBase, "keyguardOn") &&
                            (boolean) callMethod(mBase, "isKeyguardSecure", mCurrentUserId)) {
                        param.setResult(null);
                    }
                }
            });

        } catch (Throwable t) {
            log(t);
        }
    }

    private void execute(String actionValue) {
        if (mContext == null || TextUtils.isEmpty(actionValue) || actionValue.equals("none"))
            return;

        try {
            if (actionValue.contains(":")) {
                if (actionValue.contains("app:")) {
                    new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal()).launchApp(actionValue.replace("app:", ""));
                } else if (actionValue.contains("/")) {
                    // Activity Format: "package.name/com.package.ActivityName"
                    String[] parts = actionValue.replace("activity:", "").split("/");
                    new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal()).launchActivity(parts[0], parts[1], false);
                }
            } else {
                switch (actionValue) {
                    case "browser":
                        new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal()).launchBrowser(false);
                        break;
                    case "torch":
                        SystemUtils.toggleFlash();
                        break;
                    case "ringer":
                        SystemUtils.toggleRingerMode();
                        break;
                    case "dnd":
                        SystemUtils.toggleDnd();
                        break;
                    case "camera":
                        new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal()).launchCamera(false);
                        break;
                    case "recorder":
                        new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal()).launchAudioRecorder(false);
                        break;
                    case "screenshot":
                        SystemUtils.takeScreenshot(ScreenshotUtils.ScreenshotType.FULL);
                        break;
                    case "screenshot_area":
                        SystemUtils.takeScreenshot(ScreenshotUtils.ScreenshotType.PARTIAL);
                        break;
                    case "screenshot_scroll":
                        SystemUtils.takeScreenshot(ScreenshotUtils.ScreenshotType.SCROLL);
                        break;
                    case "quick_settings":
                        SystemUtils.openQs();
                        break;
                    case "kill_app":
                        SystemUtils.killForeground();
                        break;
                    case "notification_panel":
                        SystemUtils.toggleNotifications();
                        break;
                    case "toggle_one_handed":
                        SystemUtils.toggleOneHanded();
                        break;
                    case "screen_off":
                        SystemUtils.goToSleep();
                        break;
                    case "circle_to_search":
                        SystemUtils.runCircleToSearch();
                        break;
                    default:
                        break;
                }
            }
        } catch (Throwable t) {
            XposedBridge.log("PlusKey ERROR executing " + actionValue + ": " + t.getMessage());
        }
    }


    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(Constants.Packages.FRAMEWORK);
    }
}
