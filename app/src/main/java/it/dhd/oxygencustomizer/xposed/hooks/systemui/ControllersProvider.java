package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class ControllersProvider extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private static final String TAG = "Oxygen Customizer - ControllersProvider: ";

    @SuppressLint("StaticFieldLeak")
    private static ControllersProvider instance = null;

    private Object mBluetoothController = null;
    private Object mDataController = null;
    private Object mNetworkController = null;
    private Object mSignalCallback = null;

    private Object mOplusBluetoothTile = null;
    private Object mOplusWifiTile = null;
    private Object mCellularTile = null;
    private Object mDeviceControlsTile = null;

    private Object mQsDialogLaunchAnimator = null;
    private Object mQsMediaDialogController = null;

    private final ArrayList<OnMobileDataChanged> mMobileDataChangedListeners = new ArrayList<>();
    private final ArrayList<OnWifiChanged> mWifiChangedListeners = new ArrayList<>();
    private final ArrayList<OnBluetoothChanged> mBluetoothChangedListeners = new ArrayList<>();
    private final ArrayList<OnTorchModeChanged> mTorchModeChangedListeners = new ArrayList<>();

    public ControllersProvider(Context context) {
        super(context);
        instance = this;
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Network Callbacks
        Class<?> CallbackHandler = findClass("com.android.systemui.statusbar.connectivity.CallbackHandler", lpparam.classLoader);

        // Mobile Data
        hookAllMethods(CallbackHandler, "setMobileDataIndicators", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                onSetMobileDataIndicators(param.args[0]);
            }
        });

        hookAllMethods(CallbackHandler, "setIsAirplaneMode", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //mAirplane = (boolean) param.args[0];
                onSetIsAirplaneMode(param.args[0]);
            }
        });

        hookAllMethods(CallbackHandler, "setNoSims", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                onSetNoSims((boolean) param.args[0], (boolean) param.args[1]);
            }
        });

        // WiFi
        hookAllMethods(CallbackHandler, "setWifiIndicators", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                onWifiChanged(param.args[0]);
            }
        });

        // CellularTile, OplusCellularTile extends CellularTile
        // Get some controllers from CellularTile
        Class<?> CellularTile;
        try {
            CellularTile = findClass("com.oplus.systemui.qs.tiles.CellularTile", lpparam.classLoader);
        } catch (Throwable t) {
            CellularTile = findClass("com.android.systemui.qs.tiles.CellularTile", lpparam.classLoader); // OOS 13
        }
        if (CellularTile != null) {
            hookAllConstructors(CellularTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mCellularTile = param.thisObject;
                    mNetworkController = getObjectField(param.thisObject, "mController");
                    mDataController = getObjectField(param.thisObject, "mDataController");
                }
            });
        } else {
            log(TAG + "CellularTile not found");
        }

        // Bluetooth Controller
        Class<?> BluetoothControllerImpl = findClass("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader);
        hookAllConstructors(BluetoothControllerImpl, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mBluetoothController = param.thisObject;
            }
        });

        // Bluetooth Tile - for Bluetooth Dialog
        try {
            Class<?> OplusBluetoothTile = findClass("com.oplus.systemui.qs.tiles.OplusBluetoothTile", lpparam.classLoader);
            hookAllConstructors(OplusBluetoothTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mOplusBluetoothTile = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log(TAG + "OplusBluetoothTile not found");
        }

        // Stole a Bluetooth Callback from OplusPhoneStatusBarPolicyExImpl
        Class<?> OplusPhoneStatusBarPolicyExImpl;
        try {
            OplusPhoneStatusBarPolicyExImpl = findClass("com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl", lpparam.classLoader);
        } catch (Throwable t) {
            OplusPhoneStatusBarPolicyExImpl = findClass("com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx", lpparam.classLoader); // OOS 13
        }
        if (OplusPhoneStatusBarPolicyExImpl != null) {
            findAndHookMethod(
                    OplusPhoneStatusBarPolicyExImpl,
                    "updateBluetooth",
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Object bluetoothController = getObjectField(param.thisObject, "bluetoothController");
                            boolean enabled = (boolean) callMethod(bluetoothController, "isBluetoothEnabled");
                            boolean connected = (boolean) callMethod(bluetoothController, "isBluetoothConnected");
                            onBluetoothChanged(enabled);
                        }
                    }
            );
        } else {
            log(TAG + "OplusPhoneStatusBarPolicyExImpl not found");
        }

        // WiFi Tile - for WiFi Dialog
        Class<?> OplusWifiTile = findClass("com.oplus.systemui.qs.tiles.OplusWifiTile", lpparam.classLoader);
        hookAllConstructors(OplusWifiTile, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mOplusWifiTile = param.thisObject;
            }
        });

        hookAllMethods(OplusWifiTile, "createSignalCallback", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mSignalCallback = param.getResult();
            }
        });

        // Stole FlashLight Callback
        Class<?> FlashlightControllerImpl = findClass("com.android.systemui.statusbar.policy.FlashlightControllerImpl", lpparam.classLoader);
        hookAllConstructors(FlashlightControllerImpl, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mTorchCallback = getObjectField(param.thisObject, "mTorchCallback");
                findAndHookMethod(
                        mTorchCallback.getClass(),
                        "onTorchModeChanged",
                        String.class,
                        boolean.class,
                        new XC_MethodHook() {
                            @Override
                            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                                onTorchModeChanged((boolean) param.args[1]);
                            }
                        });
            }
        });

        // QS Media Tile Controller, for Dialog
        Class<?> OplusQsMediaPanelViewController = findClass("com.oplus.systemui.qs.media.OplusQsMediaPanelViewController", lpparam.classLoader);
        hookAllMethods(OplusQsMediaPanelViewController, "bindMediaCarouselController", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mQsDialogLaunchAnimator = param.args[1];
            }
        });
        hookAllMethods(OplusQsMediaPanelViewController, "setQsMediaDialogController", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mQsMediaDialogController = param.args[0];
            }
        });

        // Home Controls Tile - for ControlsActivity
        Class<?> OplusDeviceControlsTile;
        try {
            OplusDeviceControlsTile = findClass("com.oplus.systemui.qs.tiles.OplusDeviceControlsTile", lpparam.classLoader);
        } catch (Throwable t) {
            OplusDeviceControlsTile = findClass("com.android.systemui.qs.tiles.DeviceControlsTile/", lpparam.classLoader); // OOS 13
        }
        hookAllConstructors(OplusDeviceControlsTile, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mDeviceControlsTile = param.thisObject;
            }
        });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    /**
     * Callbacks for Mobile Data
     */
    public interface OnMobileDataChanged {
        void setMobileDataIndicators(Object MobileDataIndicators);
        void setNoSims(boolean show, boolean simDetected);
        void setIsAirplaneMode(Object IconState);
    }

    /**
     * Callback for WiFi
     */
    public interface OnWifiChanged {
        void onWifiChanged(Object mWifiIndicators);
    }

    /**
     * Callback for Bluetooth
     */
    public interface OnBluetoothChanged {
        void onBluetoothChanged(boolean enabled);
    }

    /**
     * Callback for FlashLight
     */
    public interface OnTorchModeChanged {
        void onTorchModeChanged(boolean enabled);
    }

    public static void registerMobileDataCallback(ControllersProvider.OnMobileDataChanged callback)
    {
        instance.mMobileDataChangedListeners.add(callback);
    }

    /** @noinspection unused*/
    public static void unRegisterMobileDataCallback(ControllersProvider.OnMobileDataChanged callback)
    {
        instance.mMobileDataChangedListeners.remove(callback);
    }

    public static void registerWifiCallback(ControllersProvider.OnWifiChanged callback)
    {
        instance.mWifiChangedListeners.add(callback);
    }

    /** @noinspection unused*/
    public static void unRegisterWifiCallback(ControllersProvider.OnWifiChanged callback)
    {
        instance.mWifiChangedListeners.remove(callback);
    }

    public static void registerBluetoothCallback(ControllersProvider.OnBluetoothChanged callback)
    {
        instance.mBluetoothChangedListeners.add(callback);
    }

    /** @noinspection unused*/
    public static void unRegisterBluetoothCallback(ControllersProvider.OnBluetoothChanged callback)
    {
        instance.mBluetoothChangedListeners.remove(callback);
    }

    public static void registerTorchModeCallback(ControllersProvider.OnTorchModeChanged callback)
    {
        instance.mTorchModeChangedListeners.add(callback);
    }

    /** @noinspection unused*/
    public static void unRegisterTorchModeCallback(ControllersProvider.OnTorchModeChanged callback)
    {
        instance.mTorchModeChangedListeners.remove(callback);
    }

    private void onSetMobileDataIndicators(Object MobileDataIndicators) {
        for(ControllersProvider.OnMobileDataChanged callback : mMobileDataChangedListeners)
        {
            try
            {
                callback.setMobileDataIndicators(MobileDataIndicators);
            }
            catch (Throwable ignored){}
        }
    }

    private void onSetIsAirplaneMode(Object MobileDataIndicators) {
        for(ControllersProvider.OnMobileDataChanged callback : mMobileDataChangedListeners)
        {
            try
            {
                callback.setIsAirplaneMode(MobileDataIndicators);
            }
            catch (Throwable ignored){}
        }
    }

    private void onSetNoSims(boolean show, boolean simDetected) {
        for(ControllersProvider.OnMobileDataChanged callback : mMobileDataChangedListeners)
        {
            try
            {
                callback.setNoSims(show, simDetected);
            }
            catch (Throwable ignored){}
        }
    }

    private void onWifiChanged(Object mWifiIndicators) {
        for(ControllersProvider.OnWifiChanged callback : mWifiChangedListeners)
        {
            try
            {
                callback.onWifiChanged(mWifiIndicators);
            }
            catch (Throwable ignored){}
        }
    }

    private void onBluetoothChanged(boolean enabled) {
        for (ControllersProvider.OnBluetoothChanged callback : mBluetoothChangedListeners) {
            try {
                callback.onBluetoothChanged(enabled);
            } catch (Throwable ignored) {
            }
        }
    }

    private void onTorchModeChanged(boolean enabled) {
        for (ControllersProvider.OnTorchModeChanged callback : mTorchModeChangedListeners) {
            try {
                callback.onTorchModeChanged(enabled);
            } catch (Throwable ignored) {
            }
        }
    }

    public static Object getBluetoothController() {
        return instance.mBluetoothController;
    }

    public static Object getDataController() {
        return instance.mDataController;
    }

//    public Object getMediaOutputDialogFactory() {
//        return instance.mMediaOutputDialogFactory;
//    }

    public static Object getNetworkController() {
        return instance.mNetworkController;
    }

    public static Object getOplusBluetoothTile() {
        return instance.mOplusBluetoothTile;
    }

    public static Object getOplusWifiTile() {
        return instance.mOplusWifiTile;
    }

    public static Object getSignalCallback() {
        return instance.mSignalCallback;
    }

    public static Object getCellularTile() {
        return instance.mCellularTile;
    }

    public static Object[] getQsMediaDialog() {
        return new Object[]{instance.mQsDialogLaunchAnimator, instance.mQsMediaDialogController};
    }

    public static Object getControlsTile() {
        return instance.mDeviceControlsTile;
    }

}
