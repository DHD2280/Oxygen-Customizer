package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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
            ReflectedClass OplusPhoneStatusBarPolicyExImpl = ReflectedClass.of(
                    "com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl" /* OOS15-14 */,
                    "com.oplusos.systemui.statusbar.phone.PhoneStatusBarPolicyEx" /* OOS13 */);

            // private final void updateBluetoothIcon(int i, int i2, CharSequence charSequence, boolean z) {
            OplusPhoneStatusBarPolicyExImpl
                    .before("updateBluetoothIcon")
                            .run(param -> {
                                boolean enabled = (boolean) param.args[3];

                                if (!enabled || !hideBluetooth) return;

                                Object bluetoothController = getObjectField(param.thisObject, Build.VERSION.SDK_INT >= 34 ? "bluetoothController" : "mBluetooth");
                                boolean connected = (boolean) callMethod(bluetoothController, "isBluetoothConnected");

                                if (!connected)
                                    param.setResult(connected);
                            });
        } catch (Throwable t) {
            log(t);
        }

        try {
            ReflectedClass OplusStatusBarSignalPolicyExImpl = ReflectedClass.of(
                    "com.oplus.systemui.statusbar.pipeline.OplusWifiSignalExImpl" /* OOS15 */,
                    "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarSignalPolicyExImpl" /* OOS14 */,
                    "com.oplusos.systemui.statusbar.phone.StatusBarSignalPolicyEx" /* OOS13 */);

            if (Build.VERSION.SDK_INT >= 35) {
                OplusStatusBarSignalPolicyExImpl
                        .before("bindEx$updateActivityIcon")
                        .run(param -> {
                            try {
                            if (mHideWifiActivity)
                                param.args[1] = 0;
                            } catch (Throwable t) {
                                log(t);
                            }
                        });
            } else {
                OplusStatusBarSignalPolicyExImpl
                        .before("getWifiActivityId")
                        .run(param -> {
                            if (mHideWifiActivity)
                                param.setResult(0);
                        });
            }

        } catch (Throwable t) {
            log(t);
        }

        try {
            ReflectedClass OplusStatusBarMobileViewExImpl = ReflectedClass.of(
                    "com.oplus.systemui.statusbar.pipeline.mobile.ui.view.OplusStatusBarMobileViewBinder" /* OOS15 */,
                    "com.oplus.systemui.statusbar.phone.signal.OplusStatusBarMobileViewExImpl" /* OOS14-13 */);

            if (Build.VERSION.SDK_INT >= 35) {
                OplusStatusBarMobileViewExImpl
                        .before("bindCustEx$updateDataActivity")
                        .run(param -> {
                            if (mHideMobileActivity) {
                                param.args[1] = 0;
                            }
                        });
            } else {
                OplusStatusBarMobileViewExImpl
                        .after("updateState")
                        .run(param -> {
                            if (!mHideMobileActivity) return;
                            ImageView mDataActivity = (ImageView) getObjectField(param.thisObject, "mDataActivity");
                            mDataActivity.setVisibility(View.GONE);
                            ImageView mIn = (ImageView) getObjectField(param.thisObject, "mIn");
                            mIn.setVisibility(View.GONE);
                            ImageView mOut = (ImageView) getObjectField(param.thisObject, "mOut");
                            mOut.setVisibility(View.GONE);
                        });
            }
        } catch (Throwable t) {
            log(t);
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
