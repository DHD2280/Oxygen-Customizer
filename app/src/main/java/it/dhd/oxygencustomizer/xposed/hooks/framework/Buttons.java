package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.widget.Toast.LENGTH_LONG;
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
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
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
    private final String TAG = "Oxygen Customizer - Buttons ";
    Handler mHandler;
    private long wakeTime = 0;
    //    private boolean isVolumeLongPress = false;
    private boolean isVolDown = false;
    private boolean disablePowerOnLockscreen = false;
    private boolean broadcastRegistered = false;
    private int volumeToTorchTimeout = 5000;
    private boolean settingsUpdated = false;
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

    public class PlusKeyActionHandler {
        public static void execute(Context context, String actionValue) {
            if (context == null || TextUtils.isEmpty(actionValue) || actionValue.equals("none")) return;

            try {
                if (actionValue.contains("/")) {
                    // Activity Format: "package.name/com.package.ActivityName"
                    String[] parts = actionValue.split("/");
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setComponent(new ComponentName(parts[0], parts[1]));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    switch (actionValue) {
                        case "browser":
                            Intent browser = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_APP_BROWSER);
                            browser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(browser);
                            break;
                        case "torch": SystemUtils.toggleFlash(); break;
                        case "ringer": SystemUtils.toggleRingerMode(); break;
                        case "dnd": SystemUtils.toggleDnd(); break;
                        case "camera": SystemUtils.launchCamera(context); break;
                        case "recorder": SystemUtils.launchAudioRecorder(context); break;
                        case "screenshot":
                            context.sendBroadcast(new Intent("it.dhd.oxygencustomizer.ACTIONS_SCREENSHOT_OC"));
                            break;
                        default:
                            Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(actionValue);
                            if (launchIntent != null) {
                                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                context.startActivity(launchIntent);
                            }
                            break;
                    }
                }
            } catch (Throwable t) {
                XposedBridge.log("PlusKey ERROR executing " + actionValue + ": " + t.getMessage());
            }
        }
    }
    public static class PlusKeyButtonHandlerClass {

        private static final int KEYCODE_PLUSKEY_SHORT_PRESS = 781;
        private static final int KEYCODE_PLUSKEY_LONG_PRESS = 782;
        private static int pressCount = 0;

        // Using WeakReference to avoid memory leaks
        private static WeakReference<Context> contextRef;
        public static boolean singlePressEnabled = false;
        public static boolean doublePressEnabled = false;
        public static boolean triplePressEnabled = false;
        public static boolean longPressEnabled = false;
        public static long timeout = 250;

        public static boolean isAnyShortPressEnabled(){
            return singlePressEnabled || doublePressEnabled || triplePressEnabled;
        }
        public static boolean isLongPressEnabled(){
            return longPressEnabled;
        }

        private static final Runnable actionRunnable = () -> {
            int count = pressCount;
            //XposedBridge.log("PlusKey LOG: actionRunnable START. Current pressCount read as: " + count);

            // Reset count immediately for next sequence
            pressCount = 0;
            //XposedBridge.log("PlusKey LOG: actionRunnable. pressCount reset to 0");

            Context context = (contextRef != null) ? contextRef.get() : null;
            if (context == null) {
                XposedBridge.log("PlusKey LOG: actionRunnable ABORT. Context from WeakReference is NULL");
                return;
            }

            boolean shouldExecute = false;
            if (count == 1 && singlePressEnabled) shouldExecute = true;
            else if (count == 2 && doublePressEnabled) shouldExecute = true;
            else if (count >= 3 && triplePressEnabled) shouldExecute = true;

            //XposedBridge.log("PlusKey LOG: actionRunnable evaluation. count=" + count +
            //        ", single=" + singlePressEnabled + ", double=" + doublePressEnabled +
            //        ", triple=" + triplePressEnabled + " -> shouldExecute=" + shouldExecute);

            if (shouldExecute) {
                executeAction(context, count);
            } else {
                XposedBridge.log("PlusKey LOG: actionRunnable. No enabled action matched count=" + count);
            }
        };

        /**
         * Call this when the short press key (781) ACTION_UP is detected.
         */
        public static void ShortPressDetected(Context context, Handler handler) {
            //XposedBridge.log("PlusKey LOG: ShortPressDetected ENTRY. Current pressCount before increment: " + pressCount);

            if (!singlePressEnabled && !doublePressEnabled && !triplePressEnabled) {
                //XposedBridge.log("PlusKey LOG: ShortPressDetected EXIT. No short press actions are enabled in settings.");
                return;
            }

            contextRef = new WeakReference<>(context);
            pressCount++;
            //XposedBridge.log("PlusKey LOG: ShortPressDetected. pressCount incremented to: " + pressCount);

            if (handler != null) {
                //XposedBridge.log("PlusKey LOG: ShortPressDetected. Calling handler.removeCallbacks(actionRunnable)");
                handler.removeCallbacks(actionRunnable);

                // Check if we should execute immediately
                boolean hasMultiPressActions = doublePressEnabled || triplePressEnabled;
                boolean isOnlySingleEnabled = singlePressEnabled && !hasMultiPressActions;

                //XposedBridge.log("PlusKey LOG: ShortPressDetected. isOnlySingleEnabled=" + isOnlySingleEnabled + ", hasMultiPressActions=" + hasMultiPressActions + " (pressCount=" + pressCount + ")");

                if (isOnlySingleEnabled || pressCount >= 3 || (pressCount == 2 && !triplePressEnabled)) {
                    //XposedBridge.log("PlusKey LOG: ShortPressDetected. TRIGGERING IMMEDIATE EXECUTION (runnable.run())");
                    actionRunnable.run();
                } else {
                    //XposedBridge.log("PlusKey LOG: ShortPressDetected. SCHEDULING DELAYED EXECUTION (postDelayed) with timeout: " + timeout);
                    handler.postDelayed(actionRunnable, timeout);
                }
            } else {
                XposedBridge.log("PlusKey LOG: ShortPressDetected ERROR. Handler provided is NULL");
            }
        }

        /**
         * Call this when the long press key (782) is detected.
         */
        public static void LongPressDetected(Context context) {
            //XposedBridge.log("PlusKey LOG: LongPressDetected ENTRY. Settings longPressEnabled=" + longPressEnabled);
            if (context == null) {
            //    XposedBridge.log("PlusKey LOG: LongPressDetected ABORT. Context is NULL");
                return;
            }
            if (!longPressEnabled) {
            //    XposedBridge.log("PlusKey LOG: LongPressDetected EXIT. longPressEnabled is FALSE");
                return;
            }
            //XposedBridge.log("PlusKey LOG: LongPressDetected. Triggering executeAction for long press (count=0)");
            executeAction(context, 0);
        }

        private static void executeAction(Context context, int count) {
            try {
                String key = switch (count) {
                    case 0 -> "plusKey_long_press_button_action_value";
                    case 1 -> "plusKey_single_press_button_action_value";
                    case 2 -> "plusKey_double_press_button_action_value";
                    case 3 -> "plusKey_triple_press_button_action_value";
                    default -> "";
                };

                String actionValue = it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs.getString(key, "none");
                PlusKeyActionHandler.execute(context, actionValue);
            } catch (Throwable t) {
                XposedBridge.log("PlusKey executeAction ERROR: " + t.getMessage());
            }
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

        String singleAction = Xprefs.getString("plusKey_single_press_button_action_value", "none");
        PlusKeyButtonHandlerClass.singlePressEnabled = !TextUtils.isEmpty(singleAction) && !singleAction.equals("none");

        String doubleAction = Xprefs.getString("plusKey_double_press_button_action_value", "none");
        PlusKeyButtonHandlerClass.doublePressEnabled = !TextUtils.isEmpty(doubleAction) && !doubleAction.equals("none");

        String tripleAction = Xprefs.getString("plusKey_triple_press_button_action_value", "none");
        PlusKeyButtonHandlerClass.triplePressEnabled = !TextUtils.isEmpty(tripleAction) && !tripleAction.equals("none");

        String longAction = Xprefs.getString("plusKey_long_press_button_action_value", "none");
        PlusKeyButtonHandlerClass.longPressEnabled = !TextUtils.isEmpty(longAction) && !longAction.equals("none");

        PlusKeyButtonHandlerClass.timeout = Xprefs.getSliderInt("plusKey_press_button_action_timeout", 250);


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


            if(lpparam.packageName.equals("android")){
            hookMethod(overrideInterceptKeyBeforeQueueing, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                    try {
                        KeyEvent event = (KeyEvent) param.args[0];
                        int keyCode = event.getKeyCode();

                        Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                        Handler handler = (Handler) XposedHelpers.getObjectField(getObjectField(param.thisObject, "mBase"), "mHandler");

                        if (keyCode == PlusKeyButtonHandlerClass.KEYCODE_PLUSKEY_SHORT_PRESS) {
                            // Check if any multi-press action is enabled in settings
                            if (PlusKeyButtonHandlerClass.isAnyShortPressEnabled()) {
                                if (event.getAction() == KeyEvent.ACTION_UP) {
                                    PlusKeyButtonHandlerClass.ShortPressDetected(context, handler);
                                }
                                param.setResult(0); // Consume the event

                            }
                        } else if (keyCode == PlusKeyButtonHandlerClass.KEYCODE_PLUSKEY_LONG_PRESS) {
                            if (PlusKeyButtonHandlerClass.isLongPressEnabled()) {
                                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                                    PlusKeyButtonHandlerClass.LongPressDetected(context);
                                }
                                param.setResult(0); // Consume the event
                            }
                        }
                    } catch (Throwable t) {
                        log(" ERROR IN PlusKey hook: " + t.getMessage());
                    }
                }});
            }

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


    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(Constants.Packages.FRAMEWORK);
    }
}
