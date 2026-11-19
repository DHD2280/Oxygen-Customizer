package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.HashSet;
import java.util.Set;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;

public class StatusbarIcons extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private boolean hideBluetooth, mHideWifiActivity = false, mHideMobileActivity = false;
    private boolean mIosSignal = false;
    private final Set<Integer> mWifiActivityIds = new HashSet<>();
    private final Set<Integer> mMobileActivityIds = new HashSet<>();
    private static final String[] WIFI_ACTIVITY = {"wifi_in", "wifi_out", "wifi_inout", "wifi_none"};
    private static final String[] MOBILE_ACTIVITY = {"default", "in", "out", "inout"};

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
        if (Build.VERSION.SDK_INT >= 37) hookActivityDrawables();

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

    @SuppressLint("DiscouragedApi")
    private void hookActivityDrawables() {
        Resources res = mContext.getResources();
        for (String name : WIFI_ACTIVITY) {
            addDrawableId(res, mWifiActivityIds, "stat_signal_activity_" + name);
            addDrawableId(res, mWifiActivityIds, "stat_signal_activity_" + name + "_os17");
        }
        for (String state : MOBILE_ACTIVITY) {
            for (String style : new String[]{"activity_", "activity_soft_", "stacked_activity_", "soft_stacked_activity_"}) {
                addDrawableId(res, mMobileActivityIds, "stat_signal_" + style + state + "_public");
                addDrawableId(res, mMobileActivityIds, "stat_signal_" + style + state + "_public_os17");
            }
        }
        if (mWifiActivityIds.isEmpty() && mMobileActivityIds.isEmpty()) return;
        ReflectedClass.ReflectionConsumer hideActivity = param -> {
            int id = (int) param.args[0];
            if ((mHideWifiActivity && mWifiActivityIds.contains(id))
                    || (mHideMobileActivity && mMobileActivityIds.contains(id)))
                param.setResult(new ColorDrawable(Color.TRANSPARENT));
        };
        ReflectedClass.of(Resources.class)
                .before(ReflectionHelpers.findMethodExact(Resources.class, "getDrawable", int.class, Resources.Theme.class))
                .run(hideActivity);
        ReflectedClass.of(Resources.class)
                .before(ReflectionHelpers.findMethodExact(Resources.class, "getDrawableForDensity", int.class, int.class, Resources.Theme.class))
                .run(hideActivity);
    }

    @SuppressLint("DiscouragedApi")
    private void addDrawableId(Resources res, Set<Integer> ids, String name) {
        int id = res.getIdentifier(name, "drawable", SYSTEM_UI);
        if (id != 0) ids.add(id);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
