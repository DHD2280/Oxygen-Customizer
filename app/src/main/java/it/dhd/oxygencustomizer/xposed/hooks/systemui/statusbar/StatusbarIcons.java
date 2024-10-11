package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class StatusbarIcons extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private boolean hideBluetooth, mHideWifiActivity = false, mHideMobileActivity = false;

    public StatusbarIcons(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        hideBluetooth = Xprefs.getBoolean("hide_bluetooth_when_disconnected", false);
        mHideWifiActivity = Xprefs.getBoolean("hide_inout_wifi", false);
        mHideMobileActivity = Xprefs.getBoolean("hide_inout_mobile", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            Class<?> OplusPhoneStatusBarPolicyExImpl;
            try {
                OplusPhoneStatusBarPolicyExImpl = findClass("com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl", lpparam.classLoader);
            } catch (Throwable t) {
                OplusPhoneStatusBarPolicyExImpl = findClass("com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx", lpparam.classLoader);
            }

            // private final void updateBluetoothIcon(int i, int i2, CharSequence charSequence, boolean z) {
            findAndHookMethod(OplusPhoneStatusBarPolicyExImpl, "updateBluetoothIcon",
                    int.class,
                    int.class,
                    CharSequence.class,
                    boolean.class, new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            boolean enabled = (boolean) param.args[3];

                            if (!enabled || !hideBluetooth) return;

                            Object bluetoothController = getObjectField(param.thisObject, Build.VERSION.SDK_INT >= 34 ? "bluetoothController" : "mBluetooth");
                            boolean connected = (boolean) callMethod(bluetoothController, "isBluetoothConnected");

                            if (!connected)
                                param.setResult(connected);
                        }
                    });
        } catch (Throwable t) {
            log("Class Not Found " + t.getMessage());
        }

        try {
            Class<?> OplusStatusBarSignalPolicyExImpl;
            try {
                OplusStatusBarSignalPolicyExImpl = findClass("com.oplus.systemui.statusbar.phone.signal.OplusStatusBarSignalPolicyExImpl", lpparam.classLoader);
            } catch (Throwable t) {
                OplusStatusBarSignalPolicyExImpl = findClass("com.oplusos.systemui.statusbar.phone.StatusBarSignalPolicyEx", lpparam.classLoader);
            }

            hookAllMethods(OplusStatusBarSignalPolicyExImpl, "getWifiActivityId", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (mHideWifiActivity)
                        param.setResult(0);
                }
            });

        } catch (Throwable t) {
            log(" getWifiActivityId " + t.getMessage());
        }

        try {
            Class<?> OplusStatusBarMobileViewExImpl = findClass("com.oplus.systemui.statusbar.phone.signal.OplusStatusBarMobileViewExImpl", lpparam.classLoader);
            hookAllMethods(OplusStatusBarMobileViewExImpl, "updateState", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!mHideMobileActivity) return;
                    ImageView mDataActivity = (ImageView) getObjectField(param.thisObject, "mDataActivity");
                    mDataActivity.setVisibility(View.GONE);
                    ImageView mIn = (ImageView) getObjectField(param.thisObject, "mIn");
                    mIn.setVisibility(View.GONE);
                    ImageView mOut = (ImageView) getObjectField(param.thisObject, "mOut");
                    mOut.setVisibility(View.GONE);
                }
            });
        } catch (Throwable t) {
            log(" - Class Not Found " + t);
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
