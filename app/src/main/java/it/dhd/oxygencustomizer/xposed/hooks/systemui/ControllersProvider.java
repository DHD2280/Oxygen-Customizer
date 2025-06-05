package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class ControllersProvider extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    public static Class<?> LaunchableLinearLayout = null;
    public static Class<?> LaunchableImageView = null;
    public static Object PersonalityManagerEx = null;
    public static Object PersonalityManager = null;
    public static Class<?> PersonalityManagerClass = null;
    public static Class<?> Expandable = null;

    @SuppressLint("StaticFieldLeak")
    private static ControllersProvider instance = null;

    private final ArrayList<OnMobileDataChanged> mMobileDataChangedListeners = new ArrayList<>();
    private final ArrayList<OnWifiChanged> mWifiChangedListeners = new ArrayList<>();
    private final ArrayList<OnBluetoothChanged> mBluetoothChangedListeners = new ArrayList<>();
    private final ArrayList<OnTorchModeChanged> mTorchModeChangedListeners = new ArrayList<>();
    private final ArrayList<OnHotspotChanged> mHotspotChangedListeners = new ArrayList<>();
    private final ArrayList<OnDozingChanged> mDozingChangedListeners = new ArrayList<>();
    private final ArrayList<OnKeyguardShowing> mKeyguardShowingListeners = new ArrayList<>();
    private final ArrayList<OnStatusBarStateChanged> mStatusBarStateListeners = new ArrayList<>();

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
    private Object mOplusQsMediaTile = null;
    private Class<?> SystemUIDialog = null;
    private Object mActivityStarterImpl = null;
    private Object mSystemPropertiesHelper = null;
    private Object mBatteryInfo = null;

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

    public static void registerDozingCallback(ControllersProvider.OnDozingChanged callback) {
        instance.mDozingChangedListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterDozingCallback(ControllersProvider.OnDozingChanged callback) {
        instance.mDozingChangedListeners.remove(callback);
    }

    public static void registerKeyguardShowingCallback(OnKeyguardShowing callback) {
        instance.mKeyguardShowingListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterKeyguardShowingCallback(ControllersProvider.OnKeyguardShowing callback) {
        instance.mKeyguardShowingListeners.remove(callback);
    }

    public static void registerStatusBarStateChangedCallback(OnStatusBarStateChanged callback) {
        instance.mStatusBarStateListeners.add(callback);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterStatusBarStateChangedCallback(OnStatusBarStateChanged callback) {
        instance.mStatusBarStateListeners.remove(callback);
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

    public static Object getOplusQsMediaTile() {
        return instance.mOplusQsMediaTile;
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
    public void updatePrefs(String... Key) {}

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        boolean oos13 = Build.VERSION.SDK_INT == 33;

        try {
            getActivityStarter();
        } catch (Throwable t) {
            log(t);
        }

        try {
            getSystemPropertiesHelper();
        } catch (Throwable t) {
            log(t);
        }

        try {
            getBatteryInfo();
        } catch (Throwable t) {
            log(t);
        }

        try {
            // LaunchableLinearLayout
            // This is the container of our custom views
            LaunchableLinearLayout = ReflectedClass.of("com.android.systemui.animation.view.LaunchableLinearLayout").getClazz();
        } catch (Throwable t) {
            log("LaunchableLinearLayout not found: " + t.getMessage());
        }

        try {
            // LaunchableImageView
            // This is an ImageView that can launch dialogs with a GhostView
            LaunchableImageView = ReflectedClass.of("com.android.systemui.animation.view.LaunchableImageView").getClazz();
        } catch (Throwable t) {
            log("LaunchableImageView not found: " + t.getMessage());
        }

        // Dozing Callback
        ReflectedClass CentralSurfacesImpl = ReflectedClass.of("com.android.systemui.statusbar.phone.CentralSurfacesImpl");
        CentralSurfacesImpl
                .after("updateDozingState")
                .run(param -> {
                    boolean dozing = getBooleanField(param.thisObject, "mDozing");
                    updateDozingState(dozing);
                });

        // Keyguard Showing Callback
        ReflectedClass KeyguardUpdateMonitor = ReflectedClass.of(
                "com.android.keyguard.KeyguardUpdateMonitor");
        KeyguardUpdateMonitor
                .after("setKeyguardShowing")
                .run(param -> {
                    boolean keyguardShowing = (boolean) param.args[0];
                    onKeyguardShowing(keyguardShowing);
                });
        ReflectedClass NotificationShadeWindowControllerImpl = ReflectedClass.ofIfPossible("com.android.systemui.shade.NotificationShadeWindowControllerImpl");
        if (NotificationShadeWindowControllerImpl.getClazz() != null) {
            NotificationShadeWindowControllerImpl
                    .after("setKeyguardShowing")
                    .run(param -> {
                        boolean keyguardShowing = (boolean) param.args[0];
                        onKeyguardShowing(keyguardShowing);
                    });
        }
        ReflectedClass StatusBarStateListener = ReflectedClass.ofIfPossible(
                "com.android.systemui.shade.NotificationPanelViewController$StatusBarStateListener");
        if (StatusBarStateListener.getClazz() != null) {
            StatusBarStateListener
                    .after("onStateChanged")
                    .run(param -> {
                        int state = (int) param.args[0];
                        onStateChanged(state);
                    });
        }

        try {
            ReflectedClass OplusPersonalityManager = ReflectedClass.of("com.oplus.systemui.qs.personality.PersonalityManager");
            OplusPersonalityManager
                    .afterConstruction()
                    .run(param -> PersonalityManager = param.thisObject);
        } catch (Throwable t) {
            log("PersonalityManager not found: " + t.getMessage());
        }

        try {
            ReflectedClass PersonalityManagerExImpl = ReflectedClass.of("com.android.systemui.qs.personality.PersonalityManagerEx");
            PersonalityManagerExImpl
                    .afterConstruction()
                    .run(param -> PersonalityManagerEx = param.thisObject);
        } catch (Throwable t) {
            log("PersonalityManagerExImpl not found: " + t.getMessage());
        }

        try {
            PersonalityManagerClass = ReflectedClass.of("com.oplusos.systemui.qs.personality.PersonalityManager$Companion").getClazz();
        } catch (Throwable t) {
            log("PersonalityManager not found: " + t.getMessage());
        }

        try {
            SystemUIDialog = ReflectedClass.of("com.android.systemui.statusbar.phone.SystemUIDialog").getClazz();
        } catch (Throwable t) {
            log("SystemUIDialog not found: " + t.getMessage());
        }

        try {
            Expandable = ReflectedClass.of("com.android.systemui.animation.Expandable").getClazz();
        } catch (Throwable ignored) {
        }

        // Network Callbacks
        ReflectedClass CallbackHandler = ReflectedClass.of("com.android.systemui.statusbar.connectivity.CallbackHandler");

        // Mobile Data
        CallbackHandler.after("setMobileDataIndicators").run(param -> onSetMobileDataIndicators(param.args[0]));

        CallbackHandler.after("setIsAirplaneMode").run(param -> onSetIsAirplaneMode(param.args[0]));

        CallbackHandler.after("setNoSims").run(param -> onSetNoSims((boolean) param.args[0], (boolean) param.args[1]));

        // WiFi
        CallbackHandler.after("setWifiIndicators").run(param -> onWifiChanged(param.args[0]));

        // CellularTile, OplusCellularTile extends CellularTile
        // Get some controllers from CellularTile
        try {
            ReflectedClass CellularTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.CellularTile" /* OOS14-15 */,
                    "com.android.systemui.qs.tiles.CellularTile" /* OOS 13 */);
            CellularTile.afterConstruction().run(param -> {
                mCellularTile = param.thisObject;
                mNetworkController = getObjectField(param.thisObject, "mController");
                mDataController = getObjectField(param.thisObject, "mDataController");
            });
        } catch (Throwable t) {
            log(t);
        }

        // Bluetooth Controller
        try {
            ReflectedClass BluetoothControllerImpl = ReflectedClass.of("com.android.systemui.statusbar.policy.BluetoothControllerImpl");
            BluetoothControllerImpl
                    .afterConstruction()
                    .run(param -> mBluetoothController = param.thisObject);
        } catch (Throwable t) {
            log("BluetoothControllerImpl not found " + t.getMessage());
        }

        // Bluetooth Tile - for Bluetooth Dialog
        try {
            ReflectedClass OplusBluetoothTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.OplusBluetoothTile" /* OOS14-15 */,
                    "com.oplusos.systemui.qs.tiles.OplusBluetoothTile" /* OOS 13 */);
            OplusBluetoothTile.afterConstruction().run(param -> mOplusBluetoothTile = param.thisObject);
        } catch (Throwable t) {
            log(t);
        }

        // Stole a Bluetooth Callback from OplusPhoneStatusBarPolicyExImpl
        ReflectedClass OplusPhoneStatusBarPolicyExImpl = ReflectedClass.of(
                "com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl", /* OOS 15-14 */
                "com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx" // OOS 13
        );
        if (OplusPhoneStatusBarPolicyExImpl.getClazz() != null) {
            OplusPhoneStatusBarPolicyExImpl
                    .after("updateBluetooth")
                    .run(param -> {
                                Object bluetoothController;
                                try {
                                    bluetoothController = getObjectField(param.thisObject, "bluetoothController");
                                } catch (Throwable t) {
                                    bluetoothController = getObjectField(param.thisObject, "mBluetooth");
                                }
                                boolean enabled;
                                try {
                                    enabled = (boolean) callMethod(bluetoothController, "isBluetoothEnabled");
                                } catch (Throwable ignored) {
                                    // No method 15.0.1
                                    enabled = getBooleanField(bluetoothController, "mEnabled");
                                }
                                onBluetoothChanged(enabled);
                            }
                    );
        } else {
            log("OplusPhoneStatusBarPolicyExImpl not found");
        }

        // WiFi Tile - for WiFi Dialog
        if (!oos13) {
            ReflectedClass OplusWifiTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.OplusWifiTile");
            OplusWifiTile
                    .afterConstruction()
                    .run(param -> mOplusWifiTile = param.thisObject);

            OplusWifiTile
                    .after("createSignalCallback")
                    .run(param -> mSignalCallback = param.getResult());
        } else {
            ReflectedClass WifiTile = ReflectedClass.of("com.android.systemui.qs.tiles.WifiTile");
            WifiTile
                    .afterConstruction()
                    .run(param -> mSignalCallback = getObjectField(param.thisObject, "mSignalCallback"));
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

        // QS Media Tile, for Media Dialog OOS15
        try {
            ReflectedClass OplusQsMediaTile = ReflectedClass.of("com.oplus.systemui.qs.media.OplusQsMediaPanelView");
            OplusQsMediaTile.afterConstruction().run(param -> mOplusQsMediaTile = param.thisObject);
        } catch (Throwable t) {
            log("OplusQsMediaTile not found: " + t.getMessage());
        }
        // QS Media Tile Controller, for Dialog
        try {
            ReflectedClass OplusQsMediaTileController = ReflectedClass.of(
                    "com.oplus.systemui.qs.media.OplusQsMediaPanelViewController" /* OOS15 */,
                    "com.oplus.systemui.qs.media.OplusQsMediaTileController" /* OOS14 */,
                    "com.oplus.systemui.qs.media.OplusQsMediaPanelViewController");
            if (Build.VERSION.SDK_INT < 35) {
                OplusQsMediaTileController
                        .after("bindMediaCarouselController")
                        .run(param -> {
                            if (param.args.length > 1) mQsDialogLaunchAnimator = param.args[1];
                            if (param.args[0].getClass().getSimpleName().contains("OplusQsMediaCarouselController")) {
                                Object oplusQsMediaCarouselController = param.args[0];
                                if (oplusQsMediaCarouselController != null) {
                                    mMediaOutputDialogFactory = callMethod(oplusQsMediaCarouselController, "getMediaOutputDialogFactory");
                                }
                            }
                        });
            }
            if (Build.VERSION.SDK_INT >= 35) {
                OplusQsMediaTileController
                        .afterConstruction()
                        .run(param -> {
                            mQsDialogLaunchAnimator = getObjectField(param.thisObject, "dialogLaunchAnimator");
                            mQsMediaDialogController = getObjectField(param.thisObject, "qsMediaDialogController");
                        });
            } else {
                OplusQsMediaTileController
                        .after("setQsMediaDialogController")
                        .run(param -> mQsMediaDialogController = param.args[0]);
            }
        } catch (Throwable t) {
            log("OplusQsMediaPanelViewController not found: " + t.getMessage());
        }

        // Home Controls Tile - for ContrlsActivity
        try {
            ReflectedClass OplusDeviceControlsTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.OplusDeviceControlsTile" /* OOS14-15 */,
                    "com.oplusos.systemui.qs.tiles.OplusDeviceControlsTile" /* OOS 13 */);
            OplusDeviceControlsTile.afterConstruction().run(param -> mDeviceControlsTile = param.thisObject);
        } catch (Throwable t) {
            log("OplusDeviceControlsTile not found: " + t.getMessage());
        }

        // Calculator Tile - for opening calculator
        try {
            ReflectedClass CalculatorTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.CalculatorTile" /* OOS14-15 */,
                    "com.oplusos.systemui.qs.tiles.CalculatorTile" /* OOS 13 */);
            CalculatorTile.afterConstruction().run(param -> mCalculatorTile = param.thisObject);
        } catch (Throwable t) {
            log("CalculatorTile not found");
        }

        // Camera Launcher, so we can launch camera directly
        try {
            ReflectedClass CameraGestureHelper = ReflectedClass.of("com.android.systemui.camera.CameraGestureHelper");
            CameraGestureHelper.afterConstruction().run(param -> mCameraGestureHelper = param.thisObject);
        } catch (Throwable t) {
            log("CameraGestureHelper not found " + t.getMessage());
        }

        // Wallet Tile - for opening wallet
        try {
            ReflectedClass QuickAccessWalletTile = ReflectedClass.of("com.android.systemui.qs.tiles.QuickAccessWalletTile");
            QuickAccessWalletTile.afterConstruction().run(param -> mWalletTile = param.thisObject);
        } catch (Throwable t) {
            log("QuickAccessWalletTile not found");
        }

        // Three State Ringer Mode Tile - for settings Ringer Mode & DND
        try {
            ReflectedClass ThreeStageRingerModeTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.ThreeStageRingerModeTile");
            ThreeStageRingerModeTile.afterConstruction().run(param -> mThreeStateRingerTile = param.thisObject);
        } catch (Throwable t) {
            log("ThreeStateRingerTile error: " + t.getMessage());
        }

        // Hostpost Tile - for settings Hotspot
        try {
            ReflectedClass HotspotTile = ReflectedClass.of("com.android.systemui.qs.tiles.HotspotTile");
            HotspotTile
                    .afterConstruction()
                    .run(param ->
                            mHotspotController = getObjectField(param.thisObject, "mHotspotController"));
        } catch (Throwable t) {
            log("OplusHotspotTile error: " + t.getMessage());
        }

        try {
            ReflectedClass OplusHotspotTile = ReflectedClass.of("com.oplus.systemui.qs.tiles.OplusHotspotTile" /* OOS14-15 */,
                    "com.oplusos.systemui.qs.tiles.OplusHotspotTile" /* OOS 13 */);
            OplusHotspotTile.afterConstruction().run(param -> mHotspotTile = param.thisObject);
        } catch (Throwable t) {
            log("OplusHotspotTile error: " + t.getMessage());
        }

        // Get an Hotspot Callback
        try {
            ReflectedClass HotspotControllerImpl = ReflectedClass.of("com.android.systemui.statusbar.policy.HotspotControllerImpl");
            HotspotControllerImpl
                    .after("fireHotspotChangedCallback")
                    .run(param -> {
                        boolean enabled = (boolean) callMethod(param.thisObject, "isHotspotEnabled");
                        int devices = getIntField(param.thisObject, "mNumConnectedDevices");
                        onHotspotChanged(enabled, devices);
                    });
        } catch (Throwable t) {
            log("HotspotCallback error: " + t.getMessage());
        }

    }

    private void getActivityStarter() {
        ReflectedClass SectionHeaderNodeControllerImpl = ReflectedClass.of("com.android.systemui.statusbar.notification.collection.render.SectionHeaderNodeControllerImpl");
        SectionHeaderNodeControllerImpl
                .afterConstruction()
                .run(param -> mActivityStarterImpl = getObjectField(param.thisObject, "activityStarter"));
    }

    private void getSystemPropertiesHelper() {
        ReflectedClass BouncerMessageInteractor = ReflectedClass.ofIfPossible("com.android.systemui.bouncer.domain.interactor.BouncerMessageInteractor");
        if (BouncerMessageInteractor.getClazz() != null) {
            BouncerMessageInteractor
                    .afterConstruction()
                    .run(param -> mSystemPropertiesHelper = getObjectField(param.thisObject, "systemPropertiesHelper"));
        }
    }

    private void getBatteryInfo() {
        ReflectedClass KeyguardIndicationController = ReflectedClass.of("com.android.systemui.statusbar.KeyguardIndicationController");
        KeyguardIndicationController
                .afterConstruction()
                .run(param -> {
                    mBatteryInfo = getObjectField(param.thisObject, "mBatteryInfo");
                });
    }

    public static Object getActivityStarterExternal() {
        return instance.mActivityStarterImpl;
    }

    public static Object getSystemPropertiesHelperExternal() {
        return instance.mSystemPropertiesHelper;
    }

    public static boolean isOOS1501() {
        Object sysPropHelper = instance.mSystemPropertiesHelper;
        if (sysPropHelper == null) return false;
        String getprop = (String) callMethod(sysPropHelper, "get", "ro.build.version.oplusrom");
        return !TextUtils.isEmpty(getprop) && getprop.contains("15.0.1");
    }

    public static Object getBatteryInfoExternal() {
        return instance.mBatteryInfo;
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

    private void updateDozingState(boolean dozing) {
        for (ControllersProvider.OnDozingChanged callback : mDozingChangedListeners) {
            try {
                callback.onDozingChanged(dozing);
            } catch (Throwable ignored) {
            }
        }
    }

    private void onKeyguardShowing(boolean keyguardShowing) {
        for (ControllersProvider.OnKeyguardShowing callback : mKeyguardShowingListeners) {
            try {
                callback.onKeyguardShowing(keyguardShowing);
            } catch (Throwable t) {
                XposedBridge.log("ControllersProvider - error " + Log.getStackTraceString(t));
            }
        }
    }

    private void onStateChanged(int state) {
        for (ControllersProvider.OnStatusBarStateChanged callback : mStatusBarStateListeners) {
            try {
                callback.onStatusBarStateChanged(state);
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

    /**
     * Callback for Doze
     */
    public interface OnDozingChanged {
        void onDozingChanged(boolean dozing);
    }

    /**
     * Callback for keyguard showing
     */
    public interface OnKeyguardShowing {
        void onKeyguardShowing(boolean showing);
    }

    /**
     * Callback for status bar state changed
     */
    public interface OnStatusBarStateChanged {
        void onStatusBarStateChanged(int state);
    }

}
