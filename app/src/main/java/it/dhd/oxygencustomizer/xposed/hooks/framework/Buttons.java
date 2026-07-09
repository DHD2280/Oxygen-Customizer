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
import android.content.ComponentName;
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
    private String actionValueSingle, actionValueDouble, actionValueTriple, actionValueLong, actionValueSingleScreenOff, actionValueDoubleScreenOff, actionValueTripleScreenOff, actionValueLongScreenOff = "none";
    private boolean singlePressEnabled, doublePressEnabled, triplePressEnabled, longPressEnabled, singlePressEnabledScreenOff, doublePressEnabledScreenOff, triplePressEnabledScreenOff, longPressEnabledScreenOff = false;
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
        return SystemUtils.isScreenOff() ? (singlePressEnabledScreenOff || doublePressEnabledScreenOff || triplePressEnabledScreenOff) : (singlePressEnabled || doublePressEnabled || triplePressEnabled);
    }

    private boolean isLongPressEnabled() {
        return SystemUtils.isScreenOff() ? longPressEnabledScreenOff : longPressEnabled;
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

        boolean screenOff = SystemUtils.isScreenOff();

        boolean shouldExecute = false;
        if (count == 1 && screenOff ? singlePressEnabledScreenOff : singlePressEnabled) shouldExecute = true;
        else if (count == 2 && screenOff ? doublePressEnabledScreenOff : doublePressEnabled) shouldExecute = true;
        else if (count >= 3 && screenOff ? triplePressEnabledScreenOff : triplePressEnabled) shouldExecute = true;

        log("PlusKey LOG: actionRunnable evaluation. count=" + count +
                ", single=" + (screenOff ? singlePressEnabledScreenOff : singlePressEnabled) + ", double=" + (screenOff ? doublePressEnabledScreenOff : doublePressEnabled) +
                ", triple=" + (screenOff ? triplePressEnabledScreenOff : triplePressEnabled) + " -> shouldExecute=" + shouldExecute);

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

        if (!isAnyShortPressEnabled()) {
            log("PlusKey LOG: ShortPressDetected EXIT. No short press actions are enabled in settings.");
            return;
        }

        boolean screenOff = SystemUtils.isScreenOff();

        pressCount++;
        log("PlusKey LOG: ShortPressDetected. pressCount incremented to: " + pressCount);

        log("PlusKey LOG: ShortPressDetected. Calling handler.removeCallbacks(actionRunnable)");
        if (mHandler.hasCallbacks(actionRunnable)) mHandler.removeCallbacks(actionRunnable);

        // Check if we should execute immediately
        boolean hasMultiPressActions = screenOff ? (doublePressEnabledScreenOff || triplePressEnabledScreenOff) : (doublePressEnabled || triplePressEnabled);
        boolean isOnlySingleEnabled = (screenOff ? singlePressEnabledScreenOff : singlePressEnabled) && !hasMultiPressActions;

        log("PlusKey LOG: ShortPressDetected. isOnlySingleEnabled=" + isOnlySingleEnabled + ", hasMultiPressActions=" + hasMultiPressActions + " (pressCount=" + pressCount + ")");

        if (isOnlySingleEnabled || pressCount >= 3 || (pressCount == 2 && !(screenOff ? triplePressEnabledScreenOff : triplePressEnabled))) {
            log("PlusKey LOG: ShortPressDetected. TRIGGERING IMMEDIATE EXECUTION (runnable.run())");
            actionRunnable.run();
        } else {
            log("PlusKey LOG: ShortPressDetected. SCHEDULING DELAYED EXECUTION (postDelayed) with timeout: " + plusKeyTimeout);
            mHandler.postDelayed(actionRunnable, plusKeyTimeout);
        }
    }

    private void executeAction(int count) {
        try {
            boolean screenOff = SystemUtils.isScreenOff();
            String key = switch (count) {
                case 0 -> (!screenOff || actionValueLongScreenOff.equals("none")) ? actionValueLong : actionValueLongScreenOff;
                case 1 -> (!screenOff || actionValueSingleScreenOff.equals("none")) ? actionValueSingle : actionValueSingleScreenOff;
                case 2 -> (!screenOff || actionValueDoubleScreenOff.equals("none")) ? actionValueDouble : actionValueDoubleScreenOff;
                case 3 -> (!screenOff || actionValueTripleScreenOff.equals("none")) ? actionValueTriple : actionValueTripleScreenOff;

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

        actionValueSingleScreenOff = Xprefs.getString("plusKey_single_press_button_action_value_screenoff", "none");
        singlePressEnabledScreenOff = (!TextUtils.isEmpty(actionValueSingleScreenOff) && !actionValueSingleScreenOff.equals("none")) || singlePressEnabled;

        actionValueDoubleScreenOff = Xprefs.getString("plusKey_double_press_button_action_value_screenoff", "none");
        doublePressEnabledScreenOff = (!TextUtils.isEmpty(actionValueDoubleScreenOff) && !actionValueDoubleScreenOff.equals("none")) || doublePressEnabled;

        actionValueTripleScreenOff = Xprefs.getString("plusKey_triple_press_button_action_value_screenoff", "none");
        triplePressEnabledScreenOff = (!TextUtils.isEmpty(actionValueTripleScreenOff) && !actionValueTripleScreenOff.equals("none")) || triplePressEnabled;

        actionValueLongScreenOff = Xprefs.getString("plusKey_long_press_button_action_value_screenoff", "none");
        longPressEnabledScreenOff = (!TextUtils.isEmpty(actionValueLongScreenOff) && !actionValueLongScreenOff.equals("none")) || longPressEnabled;


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
                                // Vibrate on execution, so we can add special feedbacks
                                // SystemUtils.vibrate(VibrationEffect.EFFECT_TICK, VibrationAttributes.USAGE_COMMUNICATION_REQUEST);
                                param.setResult(0); // Consume the event

                            }
                        } else if (keyCode == KEYCODE_PLUSKEY_LONG_PRESS) {
                            if (isLongPressEnabled()) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    executeAction(0);
                                }
                                // Vibrate later
                                // SystemUtils.vibrate(VibrationEffect.EFFECT_TICK, VibrationAttributes.USAGE_COMMUNICATION_REQUEST);
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

        // How many short vibrations in response to the command
        int tickles = 1;

        try {
            if (actionValue.contains(":")) {
                if (actionValue.contains("app:")) {
                    launchApp(actionValue.replace("app:", ""), "");
                } else if (actionValue.contains("/")) {
                    // Activity Format: "package.name/com.package.ActivityName"
                    String[] parts = actionValue.replace("activity:", "").split("/");
                    launchApp(parts[0], parts[1]);
                }
            } else {
                switch (actionValue) {
                    case "browser":
                        launchBrowser();
                        break;
                    case "torch":
                        boolean result = !SystemUtils.toggleFlash();
                        tickles+= result ? 1 : 0;
                        SystemUtils.sendFlashIntent(result);
                        break;
                    case "ringer":
                        tickles += 2-SystemUtils.toggleRingerMode();
                        break;
                    case "dnd":
                        tickles += SystemUtils.toggleDnd() ? 1 : 0;
                        break;
                    case "camera":
                        launchCamera();
                        break;
                    case "recorder":
                        launchAudioRecorder();
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
            vibrateTickles(tickles);
        } catch (Throwable t) {
            XposedBridge.log("PlusKey ERROR executing " + actionValue + ": " + t.getMessage());
        }
    }
    private void vibrateTickles(int totalCount) {
        if (totalCount > 0) {
            playTickleSequence(1, totalCount);
        }
    }

    private void playTickleSequence(int currentStep, int totalCount) {
        if (currentStep > totalCount) return;

        int effect = switch (currentStep) {
            case 1 -> VibrationEffect.EFFECT_TICK;
            case 2 -> VibrationEffect.EFFECT_CLICK;
            default -> VibrationEffect.EFFECT_HEAVY_CLICK;
        };

        SystemUtils.vibrate(effect, VibrationAttributes.USAGE_COMMUNICATION_REQUEST);

        if (currentStep < totalCount && mHandler != null) {
            mHandler.postDelayed(() -> playTickleSequence(currentStep + 1, totalCount), 125);
        }
    }

    private void launchBrowser() {
        Intent browser = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER);
        browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(browser);
    }

    private void launchCamera() {
        Intent intent = new Intent(android.provider.MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

    private void launchApp(String appName, String activity) {
        Intent launchIntent;
        if (activity.isEmpty()) {
            launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(appName);
        } else {
            launchIntent = new Intent(Intent.ACTION_MAIN);
            launchIntent.setComponent(new ComponentName(appName, activity));
        }
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        mContext.startActivity(launchIntent);
    }

    private void launchAudioRecorder() {
        Intent intent = new Intent(android.provider.MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            mContext.startActivity(intent);
        } catch (Exception e) {
            Intent fallback = mContext.getPackageManager().getLaunchIntentForPackage("com.oneplus.recorder");
            if (fallback != null) mContext.startActivity(fallback);
        }
        mContext.startActivity(intent);
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(Constants.Packages.FRAMEWORK);
    }
}
