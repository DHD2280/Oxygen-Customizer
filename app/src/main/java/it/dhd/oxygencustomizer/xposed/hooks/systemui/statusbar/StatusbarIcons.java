package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class StatusbarIcons extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private boolean hideBluetooth, mHideWifiActivity = false, mHideMobileActivity = false;
    private boolean mIosSignal = false;

    public StatusbarIcons(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        hideBluetooth = Xprefs.getBoolean("hide_bluetooth_when_disconnected", false);
        mHideWifiActivity = Xprefs.getBoolean("hide_inout_wifi", false);
        mHideMobileActivity = Xprefs.getBoolean("hide_inout_mobile", false);
        mIosSignal = Xprefs.getBoolean("OxygenCustomizerComponentSGIC40.overlay", false);

    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
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
//                OplusStatusBarSignalPolicyExImpl
//                        .after("bindEx")
//                        .run(param -> {
//                            XposedBridge.log("StatusbarIcons: after bindEx");
//                            if (!(param.args[0] instanceof ViewGroup viewGroup)) return;
//                            int id = viewGroup.getContext().getResources()
//                                    .getIdentifier("wifi_inout", "id", viewGroup.getContext().getPackageName());
//                            if (id != 0) {
//                                View wifiInOut = viewGroup.findViewById(id);
//                                if (wifiInOut != null) {
//                                    wifiInOut.setVisibility(View.GONE);
//                                    XposedBridge.log("StatusbarIcons: wifi_inout view found and set gone");
//                                }
//                            }
//                            XposedBridge.log("StatusbarIcons: wifi_inout id NULL");
//                        }, true);
//
//                ReflectedClass WifiViewBinder = ReflectedClass.ofIfPossible("com.android.systemui.statusbar.pipeline.wifi.ui.binder.WifiViewBinder");
//                if (WifiViewBinder.getClazz() != null) {
//                    WifiViewBinder
//                            .after("bind")
//                            .run(param -> {
//                                XposedBridge.log("StatusbarIcons: after WifiViewBinder bind");
//                                if (!(param.args[0] instanceof ViewGroup viewGroup)) return;
//                                int resId = viewGroup.getResources().getIdentifier(
//                                        "wifi_in", "id", viewGroup.getContext().getPackageName()
//                                );
//                                int resId2 = viewGroup.getResources().getIdentifier(
//                                        "wifi_out", "id", viewGroup.getContext().getPackageName()
//                                );
//                                ImageView imageView2 = (ImageView) viewGroup.findViewById(resId);
//                                ImageView imageView3 = (ImageView) viewGroup.findViewById(resId2);
//                                if (mHideWifiActivity) {
//                                    imageView2.setVisibility(View.GONE);
//                                    imageView3.setVisibility(View.GONE);
//                                }
//                                XposedBridge.log("StatusbarIcons: wifi_in/out view found and set gone");
//                            }, true);
//                }
//
//                ReflectedClass DataActivityModelKt = ReflectedClass.ofIfPossible("com.android.systemui.statusbar.pipeline.shared.data.model.DataActivityModelKt");
//                if (DataActivityModelKt.getClazz() != null) {
//                    DataActivityModelKt
//                            .before("toWifiDataActivityModel")
//                            .run(param -> {
////                                if (mHideWifiActivity) param.args[0] = 0;
//                            });
//                }
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
                        .after("bindCustEx$updateSignalIcon")
                                .run(param -> {
                                    if (!mIosSignal) return;
                                    try {
                                        ImageView mMobileSignal = (ImageView) param.args[0];
                                        ViewGroup.LayoutParams originalParams = mMobileSignal.getLayoutParams();
                                        if (originalParams instanceof ViewGroup.MarginLayoutParams) {
                                            ViewGroup.MarginLayoutParams marginParams = (ViewGroup.MarginLayoutParams) originalParams;
                                            marginParams.width = dp2px(32, mContext);
                                            marginParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                                            mMobileSignal.setLayoutParams(marginParams);
                                        } else {
                                            // fallback
                                            mMobileSignal.setLayoutParams(new ViewGroup.MarginLayoutParams(
                                                    dp2px(32, mContext),
                                                    ViewGroup.LayoutParams.MATCH_PARENT
                                            ));
                                        }
                                        mMobileSignal.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    } catch (Throwable t) {
                                        log(t);
                                    }
                                });
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
