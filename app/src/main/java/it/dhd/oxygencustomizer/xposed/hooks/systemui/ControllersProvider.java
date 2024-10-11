package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class ControllersProvider extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    public static Class<?> LaunchableLinearLayout = null;
    public static Class<?> LaunchableImageView = null;
    public static Object PersonalityManagerEx = null;
    public static Class<?> PersonalityManagerClass = null;

    @SuppressLint("StaticFieldLeak")
    private static ControllersProvider instance = null;
    private final ArrayList<OnMobileDataChanged> mMobileDataChangedListeners = new ArrayList<>();
    private final ArrayList<OnWifiChanged> mWifiChangedListeners = new ArrayList<>();
    private final ArrayList<OnBluetoothChanged> mBluetoothChangedListeners = new ArrayList<>();
    private final ArrayList<OnTorchModeChanged> mTorchModeChangedListeners = new ArrayList<>();
    private final ArrayList<OnHotspotChanged> mHotspotChangedListeners = new ArrayList<>();
    private Object mBluetoothController = null;
    private Object mDataController = null;
    private Object mNetworkController = null;
    private Object mSignalCallback = null;
    private Object mHotspotController = null;
    private Object mOplusBluetoothTile = null;
    private Object mOplusWifiTile = null;
    private Object mCellularTile = null;
    private Object mDeviceControlsTile = null;
    private Object mCalculatorTile = null;
    private Object mWalletTile = null;
    private Object mThreeStateRingerTile = null;
    private Object mHotspotTile = null;
    private Object mQsDialogLaunchAnimator = null;
    private Object mQsMediaDialogController = null;
    private Object mMediaOutputDialogFactory = null;
    private Object mCameraGestureHelper = null;
    private Class<?> SystemUIDialog = null;

    public ControllersProvider(Context context) {
        super(context);
        instance = this;
    }

    public static void registerMobileDataCallback(ControllersProvider.OnMobileDataChanged callback) {
        instance.mMobileDataChangedListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterMobileDataCallback(ControllersProvider.OnMobileDataChanged callback) {
        instance.mMobileDataChangedListeners.remove(callback);
    }

    public static void registerWifiCallback(ControllersProvider.OnWifiChanged callback) {
        instance.mWifiChangedListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterWifiCallback(ControllersProvider.OnWifiChanged callback) {
        instance.mWifiChangedListeners.remove(callback);
    }

    public static void registerBluetoothCallback(ControllersProvider.OnBluetoothChanged callback) {
        instance.mBluetoothChangedListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterBluetoothCallback(ControllersProvider.OnBluetoothChanged callback) {
        instance.mBluetoothChangedListeners.remove(callback);
    }

    public static void registerTorchModeCallback(ControllersProvider.OnTorchModeChanged callback) {
        instance.mTorchModeChangedListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterTorchModeCallback(ControllersProvider.OnTorchModeChanged callback) {
        instance.mTorchModeChangedListeners.remove(callback);
    }

    public static void registerHotspotCallback(ControllersProvider.OnHotspotChanged callback) {
        instance.mHotspotChangedListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterHotspotCallback(ControllersProvider.OnHotspotChanged callback) {
        instance.mHotspotChangedListeners.remove(callback);
    }

    public static Object getBluetoothController() {
        return instance.mBluetoothController;
    }

    public static Object getDataController() {
        return instance.mDataController;
    }

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

    public static Object getMediaOutputDialogFactory() {
        return instance.mMediaOutputDialogFactory;
    }

    public static Object getControlsTile() {
        return instance.mDeviceControlsTile;
    }

    public static Object getCalculatorTile() {
        return instance.mCalculatorTile;
    }

    public static Object getCameraGestureHelper() {
        return instance.mCameraGestureHelper;
    }

    public static Object getWalletTile() {
        return instance.mWalletTile;
    }

    public static Object getRingerTile() {
        return instance.mThreeStateRingerTile;
    }

    public static Object getHotspotTile() {
        return instance.mHotspotTile;
    }

    public static Object getHotspotController() {
        return instance.mHotspotController;
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        boolean oos13 = Build.VERSION.SDK_INT == 33;

        try {
            // LaunchableLinearLayout
            // This is the container of our custom views
            LaunchableLinearLayout = findClass("com.android.systemui.animation.view.LaunchableLinearLayout", lpparam.classLoader);
        } catch (Throwable t) {
            log("LaunchableLinearLayout not found: " + t.getMessage());
        }

        try {
            // LaunchableImageView
            // This is an ImageView that can launch dialogs with a GhostView
            LaunchableImageView = findClass("com.android.systemui.animation.view.LaunchableImageView", lpparam.classLoader);
        } catch (Throwable t) {
            log("LaunchableImageView not found: " + t.getMessage());
        }

        try {
            Class<?> PersonalityManagerExImpl = findClass("com.android.systemui.qs.personality.PersonalityManagerEx", lpparam.classLoader);
            hookAllConstructors(PersonalityManagerExImpl, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    PersonalityManagerEx = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("PersonalityManagerExImpl not found: " + t.getMessage());
        }

        try {
            PersonalityManagerClass = findClass("com.oplusos.systemui.qs.personality.PersonalityManager$Companion", lpparam.classLoader);
        } catch (Throwable t) {
            log("PersonalityManager not found: " + t.getMessage());
        }

        try {
            SystemUIDialog = findClass("com.android.systemui.statusbar.phone.SystemUIDialog", lpparam.classLoader);
        } catch (Throwable t) {
            log("SystemUIDialog not found: " + t.getMessage());
        }

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
            log("CellularTile not found");
        }

        // Bluetooth Controller
        try {
            Class<?> BluetoothControllerImpl = findClass("com.android.systemui.statusbar.policy.BluetoothControllerImpl", lpparam.classLoader);
            hookAllConstructors(BluetoothControllerImpl, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mBluetoothController = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("BluetoothControllerImpl not found " + t.getMessage());
        }

        // Bluetooth Tile - for Bluetooth Dialog
        try {
            Class<?> OplusBluetoothTile;
            try {
                OplusBluetoothTile = findClass("com.oplus.systemui.qs.tiles.OplusBluetoothTile", lpparam.classLoader);
            } catch (Throwable t) {
                OplusBluetoothTile = findClass("com.oplusos.systemui.qs.tiles.OplusBluetoothTile", lpparam.classLoader); // OOS 13
            }
            hookAllConstructors(OplusBluetoothTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mOplusBluetoothTile = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("OplusBluetoothTile not found " + t.getMessage());
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
                            Object bluetoothController;
                            try {
                                bluetoothController = getObjectField(param.thisObject, "bluetoothController");
                            } catch (Throwable t) {
                                bluetoothController = getObjectField(param.thisObject, "mBluetooth");
                            }
                            boolean enabled = (boolean) callMethod(bluetoothController, "isBluetoothEnabled");
                            boolean connected = (boolean) callMethod(bluetoothController, "isBluetoothConnected");
                            onBluetoothChanged(enabled);
                        }
                    }
            );
        } else {
            log("OplusPhoneStatusBarPolicyExImpl not found");
        }

        // WiFi Tile - for WiFi Dialog
        if (!oos13) {
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
        } else {
            Class<?> WifiTile = findClass("com.android.systemui.qs.tiles.WifiTile", lpparam.classLoader);
            hookAllConstructors(WifiTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mSignalCallback = getObjectField(param.thisObject, "mSignalCallback");
                }
            });
        }


        // Stole FlashLight Callback
        try {
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
        } catch (Throwable t) {
            log("FlashlightControllerImpl not found " + t.getMessage());
        }

        // QS Media Tile Controller, for Dialog
        try {
            Class<?> OplusQsMediaPanelViewController = findClass("com.oplus.systemui.qs.media.OplusQsMediaPanelViewController", lpparam.classLoader);
            hookAllMethods(OplusQsMediaPanelViewController, "bindMediaCarouselController", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (param.args.length > 1) mQsDialogLaunchAnimator = param.args[1];
                    if (param.args[0].getClass().getSimpleName().contains("OplusQsMediaCarouselController")) {
                        Object oplusQsMediaCarouselController = param.args[0];
                        if (oplusQsMediaCarouselController != null) {
                            mMediaOutputDialogFactory = callMethod(oplusQsMediaCarouselController, "getMediaOutputDialogFactory");
                        }
                    }
                }
            });
            hookAllMethods(OplusQsMediaPanelViewController, "setQsMediaDialogController", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mQsMediaDialogController = param.args[0];
                }
            });
        } catch (Throwable t) {
            log("OplusQsMediaPanelViewController not found: " + t.getMessage());
        }

        // Home Controls Tile - for ControlsActivity
        Class<?> OplusDeviceControlsTile;
        try {
            OplusDeviceControlsTile = findClass("com.oplus.systemui.qs.tiles.OplusDeviceControlsTile", lpparam.classLoader);
        } catch (Throwable t) {
            OplusDeviceControlsTile = findClass("com.android.systemui.qs.tiles.DeviceControlsTile", lpparam.classLoader); // OOS 13
        }
        hookAllConstructors(OplusDeviceControlsTile, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mDeviceControlsTile = param.thisObject;
            }
        });

        // Calculator Tile - for opening calculator
        try {
            Class<?> CalculatorTile;
            try {
                CalculatorTile = findClass("com.oplus.systemui.qs.tiles.CalculatorTile", lpparam.classLoader);
            } catch (Throwable t) {
                CalculatorTile = findClass("com.oplusos.systemui.qs.tiles.CalculatorTile", lpparam.classLoader); // OOS 13
            }
            hookAllConstructors(CalculatorTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mCalculatorTile = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("CalculatorTile not found");
        }

        // Camera Launcher, so we can launch camera directly
        try {
            Class<?> CameraGestureHelper = findClass("com.android.systemui.camera.CameraGestureHelper", lpparam.classLoader);
            hookAllConstructors(CameraGestureHelper, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mCameraGestureHelper = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("CameraGestureHelper not found " + t.getMessage());
        }

        // Wallet Tile - for opening wallet
        try {
            Class<?> QuickAccessWalletTile = findClass("com.android.systemui.qs.tiles.QuickAccessWalletTile", lpparam.classLoader);
            hookAllConstructors(QuickAccessWalletTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mWalletTile = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("QuickAccessWalletTile not found");
        }

        // Three State Ringer Mode Tile - for settings Ringer Mode & DND
        try {
            Class<?> ThreeStageRingerModeTile = findClass("com.oplus.systemui.qs.tiles.ThreeStageRingerModeTile", lpparam.classLoader);
            hookAllConstructors(ThreeStageRingerModeTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mThreeStateRingerTile = param.thisObject;
                }
            });
        } catch (Throwable t) {
            log("ThreeStateRingerTile error: " + t.getMessage());
        }

        // Hostpost Tile - for settings Hotspot
        try {
            Class<?> HotspotTile = findClass("com.android.systemui.qs.tiles.HotspotTile", lpparam.classLoader);
            hookAllConstructors(HotspotTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mHotspotController = getObjectField(param.thisObject, "mHotspotController");
                }
            });
        } catch (Throwable t) {
            log("OplusHotspotTile error: " + t.getMessage());
        }

        try {
            Class<?> OplusHotspotTile;
            try {
                OplusHotspotTile = findClass("com.oplus.systemui.qs.tiles.OplusHotspotTile", lpparam.classLoader);
            } catch (Throwable t) {
                OplusHotspotTile = findClass("com.oplusos.systemui.qs.tiles.OplusHotspotTile", lpparam.classLoader); // OOS 13
            }
            hookAllConstructors(OplusHotspotTile, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mHotspotTile = param.thisObject;
                }
            });

        } catch (Throwable t) {
            log("OplusHotspotTile error: " + t.getMessage());
        }

        // Get an Hotspot Callback
        try {
            Class<?> HotspotControllerImpl = findClass("com.android.systemui.statusbar.policy.HotspotControllerImpl", lpparam.classLoader);
            hookAllMethods(HotspotControllerImpl, "fireHotspotChangedCallback", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    boolean enabled = (boolean) callMethod(param.thisObject, "isHotspotEnabled");
                    int devices = getIntField(param.thisObject, "mNumConnectedDevices");
                    onHotspotChanged(enabled, devices);
                }
            });
        } catch (Throwable t) {
            log("HotspotCallback error: " + t.getMessage());
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void onSetMobileDataIndicators(Object MobileDataIndicators) {
        for (ControllersProvider.OnMobileDataChanged callback : mMobileDataChangedListeners) {
            try {
                callback.setMobileDataIndicators(MobileDataIndicators);
            } catch (Throwable ignored) {
            }
        }
    }

    private void onSetIsAirplaneMode(Object MobileDataIndicators) {
        for (ControllersProvider.OnMobileDataChanged callback : mMobileDataChangedListeners) {
            try {
                callback.setIsAirplaneMode(MobileDataIndicators);
            } catch (Throwable ignored) {
            }
        }
    }

    private void onSetNoSims(boolean show, boolean simDetected) {
        for (ControllersProvider.OnMobileDataChanged callback : mMobileDataChangedListeners) {
            try {
                callback.setNoSims(show, simDetected);
            } catch (Throwable ignored) {
            }
        }
    }

    private void onWifiChanged(Object mWifiIndicators) {
        for (ControllersProvider.OnWifiChanged callback : mWifiChangedListeners) {
            try {
                callback.onWifiChanged(mWifiIndicators);
            } catch (Throwable ignored) {
            }
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

    private void onHotspotChanged(boolean enabled, int connectedDevices) {
        for (ControllersProvider.OnHotspotChanged callback : mHotspotChangedListeners) {
            try {
                callback.onHotspotChanged(enabled, connectedDevices);
            } catch (Throwable ignored) {
            }
        }
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

    /**
     * Callback for Hotspot
     */
    public interface OnHotspotChanged {
        void onHotspotChanged(boolean enabled, int connectedDevices);
    }

}
