package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.OplusBatteryStatus;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class BatteryDataProvider extends XposedMods {

    public static final int CHARGING_FAST = 2;
    public static final int BATTERY_STATUS_DISCHARGING = 3;
    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    @SuppressLint("StaticFieldLeak")
    private static BatteryDataProvider instance = null;
    private final ArrayList<BatteryInfoCallback> mInfoCallbacks = new ArrayList<>();
    List<BatteryStatusCallback> mStatusCallbacks = new ArrayList<>();
    private final ArrayList<OplusBatteryStatusCallback> mOplusBatteryCallbacks = new ArrayList<>();
    private boolean mCharging;
    private int mCurrentLevel = 0;
    private boolean mPowerSave = false;
    private boolean mIsFastCharging = false;


    public BatteryDataProvider(Context context) {
        super(context);
        instance = this;
    }

    public static void registerStatusCallback(BatteryStatusCallback callback) {
        instance.mStatusCallbacks.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterStatusCallback(BatteryStatusCallback callback) {
        instance.mStatusCallbacks.remove(callback);
    }

    public static void registerInfoCallback(BatteryInfoCallback callback) {
        instance.mInfoCallbacks.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterInfoCallback(BatteryInfoCallback callback) {
        instance.mInfoCallbacks.remove(callback);
    }

    public static void registerOplusBatteryCallback(OplusBatteryStatusCallback callback) {
        instance.mOplusBatteryCallbacks.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterOplusBatteryCallback(OplusBatteryStatusCallback callback) {
        instance.mOplusBatteryCallbacks.remove(callback);
    }

    public static boolean isCharging() {
        return instance.mCharging;
    }

    public static int getCurrentLevel() {
        return instance.mCurrentLevel;
    }

    public static boolean isPowerSaving() {
        return instance.mPowerSave;
    }

    public static boolean isFastCharging() {
        return instance.mCharging && instance.mIsFastCharging;
    }

    public static void refreshAllInfoCallbacks() {
        instance.onBatteryInfoChanged();
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ReflectedClass BatteryStatusClass = ReflectedClass.of("com.android.settingslib.fuelgauge.BatteryStatus");
        ReflectedClass BatteryControllerImplClass = ReflectedClass.of("com.android.systemui.statusbar.policy.BatteryControllerImpl");
        ReflectedClass OplusBatteryController = ReflectedClass.of("com.oplusos.systemui.common.battery.OplusBatteryController");

        ReflectedClass.ReflectionConsumer batteryDataRefreshHook = param -> {
            mCurrentLevel = getIntField(param.thisObject, "mLevel");
            mCharging = getBooleanField(param.thisObject, "mPluggedIn")
                    || getBooleanField(param.thisObject, "mCharging")
                    || getBooleanField(param.thisObject, "mWirelessCharging");
            mPowerSave = getBooleanField(param.thisObject, "mPowerSave");

            onBatteryInfoChanged();
        };

        BatteryControllerImplClass
                .after("fireBatteryLevelChanged")
                .run(batteryDataRefreshHook);
        BatteryControllerImplClass
                .after("firePowerSaveChanged")
                .run(batteryDataRefreshHook);

        BatteryStatusClass
                .afterConstruction()
                .run(param -> {
                    mIsFastCharging = callMethod(param.thisObject, "getChargingSpeed", mContext).equals(CHARGING_FAST);
                    if (param.args[0] instanceof Intent) {
                        try {
                            onBatteryStatusChanged((int) getObjectField(param.thisObject, "status"), (Intent) param.args[0]);
                        } catch (Throwable ignored) {
                        }
                    } else if (param.args[0] instanceof Integer) {
                        try {
                            onBatteryStatusChanged((int) param.args[0], null);
                        } catch (Throwable ignored) {
                        }
                    }
                });

        ReflectedClass.ReflectionConsumer additionalBatteryHook = param -> {
            if (!(param.args.length > 0)) return;
            OplusBatteryStatus batteryStatus = new OplusBatteryStatus(param.args[0]);
            try {
                onOplusBatteryChanged(batteryStatus);
            } catch (Throwable t) {
                XposedBridge.log("BatteryDataProvider - additionalBatteryHook: " + Log.getStackTraceString(t));
            }
        };

        OplusBatteryController
                .after("fireAdditionalBatteryStateChanged")
                .run(additionalBatteryHook);

    }

    private void onBatteryStatusChanged(int status, Intent intent) {
        for (BatteryStatusCallback callback : mStatusCallbacks) {
            try {
                callback.onBatteryStatusChanged(status, intent);
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }

    private void onBatteryInfoChanged() {
        for (BatteryInfoCallback callback : mInfoCallbacks) {
            try {
                callback.onBatteryInfoChanged();
            } catch (Throwable ignored) {
            }
        }
    }

    private void onOplusBatteryChanged(OplusBatteryStatus oplusBatteryStatus) {
        for (OplusBatteryStatusCallback callback : mOplusBatteryCallbacks) {
            try {
                callback.onOplusBatteryStatusChanged(oplusBatteryStatus);
            } catch (Throwable ignored) {
            }
        }
    }

    public interface BatteryInfoCallback {
        void onBatteryInfoChanged();
    }


    public interface BatteryStatusCallback {
        void onBatteryStatusChanged(int batteryStatus, Intent batteryStatusIntent);
    }

    public interface OplusBatteryStatusCallback {
        void onOplusBatteryStatusChanged(OplusBatteryStatus oplusBatteryStatus);
    }
}
