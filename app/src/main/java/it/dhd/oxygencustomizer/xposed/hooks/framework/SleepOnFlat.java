package it.dhd.oxygencustomizer.xposed.hooks.framework;


import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getLongField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.hardware.SensorEvent;
import android.os.SystemClock;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

/**
 * Put the device to sleep when it is placed face up on a flat surface
 * From Siavash79/PixelXpert
 */
public class SleepOnFlat extends XposedMods {

    public static final String listenPackage = FRAMEWORK;
    public static final float FACE_UP_Z_THRESHOLD = 9f;
    public static final int DEFAULT_DISPLAY_GROUP = 0;
    public static final int GO_TO_SLEEP_REASON_TIMEOUT = 2;
    static final int WAKE_LOCK_STAY_AWAKE = 1 << 5;
    long mFirstStableMillis = 0;
    boolean mIsMoving = true;
    private Object mPowerManagerServiceInstance;
    long mLastSleepOrderMillis = 0;
    public long minimumSleepTime = 0;
    private static boolean SleepOnFlatScreen = false;
    public static long FlatStandbyTimeMillis = 5000;
    private static boolean SleepOnFlatRespectWakeLock = true;

    public SleepOnFlat(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        SleepOnFlatScreen = Xprefs.getBoolean("SleepOnFlatScreen", false);
        FlatStandbyTimeMillis = Xprefs.getSliderInt("FlatStandbyTime", 5) * 1000L;
        SleepOnFlatRespectWakeLock = Xprefs.getBoolean("SleepOnFlatRespectWakeLock", true);
        resetTime("pref update");
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        ReflectedClass FaceDownDetectorClass = ReflectedClass.of("com.android.server.power.FaceDownDetector", lpParam.classLoader);
        ReflectedClass PowerManagerServiceClass = ReflectedClass.of("com.android.server.power.PowerManagerService", lpParam.classLoader);

        List<Set<XC_MethodHook.Unhook>> unHooks = new ArrayList<>();
        unHooks.add(PowerManagerServiceClass
                .after("updatePowerStateLocked")
                .run(param -> {
                    mPowerManagerServiceInstance = param.thisObject;
                    unHooks.get(0).forEach(XC_MethodHook.Unhook::unhook);
                    unHooks.clear();
                }));

        FaceDownDetectorClass
                .after("onSensorChanged")
                .run(param -> {
                    if(!SleepOnFlatScreen || getBooleanField(param.thisObject, "mFaceDown")) return; //device is already facing down or feature not enabled

                    SensorEvent event = (SensorEvent) param.args[0];
                    long currentTimeNanos = event.timestamp;
                    long currentTimeMillis = currentTimeNanos / 1_000_000L;

                    if(currentTimeMillis < minimumSleepTime) return;

                    Object mPowerGroups = getObjectField(mPowerManagerServiceInstance, "mPowerGroups");
                    Object defaultPowerGroup = callMethod(mPowerGroups, "get", DEFAULT_DISPLAY_GROUP);

                    if(!(event.values[2] > FACE_UP_Z_THRESHOLD))
                    {
                        resetTime("direction");
                        return; //not facing up
                    }

                    long lastUserActivityMillis = (long) callMethod(defaultPowerGroup, "getLastUserActivityTimeLocked");

                    if(SystemClock.uptimeMillis() - lastUserActivityMillis < FlatStandbyTimeMillis) {
                        resetTime("user activity");
                        return; //user is touching the screen
                    }

                    Duration mTimeThreshold = (Duration) getObjectField(param.thisObject, "mTimeThreshold");

                    boolean moving = currentTimeNanos - getLongField(param.thisObject, "mPrevAccelerationTime") <= mTimeThreshold.toNanos();

                    if(mIsMoving && !moving) {
                        mIsMoving = false;
                        mFirstStableMillis = currentTimeMillis;
                    }
                    else if(!mIsMoving && moving) {
                        mIsMoving = true;
                    }

                    if(mIsMoving || currentTimeMillis < minimumSleepTime)
                    {
                        resetTime("movement");
                        return; //not stationary enough
                    }

                    if(SleepOnFlatRespectWakeLock)
                    {
                        callMethod(mPowerManagerServiceInstance, "updatePowerStateLocked");
                        int wakeLockSummary = getIntField(mPowerManagerServiceInstance, "mWakeLockSummary");
                        if((wakeLockSummary & WAKE_LOCK_STAY_AWAKE) != 0) {
                            resetTime("wake lock");
                            return; //wake lock detected
                        }
                    }

                    if((currentTimeMillis - mLastSleepOrderMillis) > 5000) { //avoid multiple sleep orders
                        mLastSleepOrderMillis = currentTimeMillis;
                        callMethod(mPowerManagerServiceInstance, "goToSleepInternal", getObjectField(mPowerManagerServiceInstance, "DEFAULT_DISPLAY_GROUP_IDS"), SystemClock.uptimeMillis(), GO_TO_SLEEP_REASON_TIMEOUT, 0 /* flag */);
                    }
                });
    }

    private void resetTime(String reason) {
        log("resetting time for " + reason);
        minimumSleepTime = SystemClock.elapsedRealtime() + FlatStandbyTimeMillis;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

}
