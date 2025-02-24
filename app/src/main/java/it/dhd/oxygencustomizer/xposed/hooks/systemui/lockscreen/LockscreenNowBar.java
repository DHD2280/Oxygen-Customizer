package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_LEFT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.VisualizerView;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarController;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarHolder;
import it.dhd.oxygencustomizer.xposed.views.pulse.PulseControllerImpl;

public class LockscreenNowBar extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;

    private Object mAffordanceSqlHelper = null;
    private ViewGroup mKeyguardBottomArea = null;
    private int mStatusBarState = -1;
    private boolean mKeyguardShowing = false;
    private final LinearLayout mNowBarLayout = new LinearLayout(mContext);

    private final ControllersProvider.OnDozingChanged mDozingChanged = isDozing -> NowBarController.getInstance(mContext).setDozing(isDozing);

    private boolean mNowBarEnabled = true;
    private boolean mHideLeftAfforfance = false, mHideRightAffordance = false;
    private int mNowBarBottomMargin = 0;
    private boolean mNowBarWeather = false;

    private int mAffordanceWidth = 0;

    public LockscreenNowBar(Context context) {
        super(context);
        int resId = mContext.getResources().getIdentifier(
                "keyguard_affordance_width", "dimen", SYSTEM_UI);
        if (resId != 0) {
            mAffordanceWidth = mContext.getResources().getDimensionPixelSize(resId);
            mAffordanceWidth = (int) (mAffordanceWidth * 0.9f);
        } else {
            mAffordanceWidth = dp2px(mContext, 72);
        }
    }

    @Override
    public void updatePrefs(String... Key) {

        mNowBarEnabled = Xprefs.getBoolean(NOW_BAR_ENABLED, false);
        mHideLeftAfforfance = Xprefs.getBoolean(LOCKSCREEN_REMOVE_LEFT_AFFORDANCE, false);
        mHideRightAffordance = Xprefs.getBoolean(LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE, false);
        mNowBarBottomMargin = Xprefs.getSliderInt(NOW_BAR_BOTTOM_MARGIN, 12);
        mNowBarWeather = Xprefs.getBoolean(NOW_BAR_WEATHER, false);

        if (Key.length > 0 &&
                (Key[0].equals(NOW_BAR_ENABLED) ||
                        Key[0].equals(LOCKSCREEN_REMOVE_LEFT_AFFORDANCE) ||
                        Key[0].equals(LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE) ||
                        Key[0].equals(NOW_BAR_BOTTOM_MARGIN) ||
                        Key[0].equals(NOW_BAR_WEATHER))) {
            updateNowBar();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        ReflectedClass KeyguardStatusViewController = ReflectedClass.of("com.android.keyguard.KeyguardStatusViewController");
        KeyguardStatusViewController
                .after("setAlpha")
                .run(param -> {
                    mNowBarLayout.setAlpha((float) param.args[0]);
                });

        ReflectedClass NotificationPanelViewController = ReflectedClass.of("com.android.systemui.shade.NotificationPanelViewController");
        NotificationPanelViewController
                .after("onFinishInflate")
                .run(param -> {
                    mKeyguardBottomArea = (ViewGroup) getObjectField(param.thisObject, "mView");
                    placeNowBar();
                });

        ReflectedClass QSImpl = ReflectedClass.of("com.android.systemui.qs.QSImpl");
        QSImpl
                .after("setQsExpansion")
                .run(param -> {
                    boolean isFullyCollapsed = (boolean) callMethod(param.thisObject, "isFullyCollapsed");
                    NowBarController.getInstance(mContext).setFullyCollapsed(isFullyCollapsed);
                });

        QSImpl
                .after("onStateChanged")
                .run(param -> {
                    mStatusBarState = (int) param.args[0];
                    NowBarController.getInstance(mContext).setStatusBarState(mStatusBarState);
                });

        ReflectedClass KeyguardUpdateMonitor = ReflectedClass.of("com.android.keyguard.KeyguardUpdateMonitor");
        KeyguardUpdateMonitor
                .after("setKeyguardShowing")
                        .run(param -> {
                            mKeyguardShowing = (boolean) param.args[1];
                            NowBarController.getInstance(mContext).setKeyguardShowing(mKeyguardShowing);
                        });

        ReflectedClass MediaHierarchyManager = ReflectedClass.of("com.android.systemui.media.controls.ui.controller.MediaHierarchyManager");
        MediaHierarchyManager
                .before("getAllowMediaPlayerOnLockScreen")
                .run(param -> {
                    if (mNowBarEnabled) param.setResult(false);
                });
        MediaHierarchyManager
                .before("setAllowMediaPlayerOnLockScreen")
                .run(param -> param.args[0] = !mNowBarEnabled);

        ReflectedClass AffordanceSqlHelper = ReflectedClass.of("com.oplus.systemui.keyguard.domain.quickaffordance.AffordanceSqlHelper");
        mAffordanceSqlHelper = getStaticObjectField(AffordanceSqlHelper.getClazz(), "Companion");

        ControllersProvider.registerDozingCallback(mDozingChanged);

    }

    private void placeNowBar() {
        if (mKeyguardBottomArea == null) return;
        NowBarHolder mNowBarHolder = NowBarHolder.getInstance(mContext);
        try {
            ((ViewGroup) mNowBarHolder.getParent()).removeView(mNowBarHolder);
        } catch (Throwable ignored) {
        }
        mNowBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mNowBarLayout.setGravity(Gravity.BOTTOM);
        mNowBarLayout.addView(mNowBarHolder);
        mKeyguardBottomArea.addView(mNowBarLayout, mKeyguardBottomArea.getChildCount() - 1);
        updateNowBar();
    }

    private void updateNowBar() {
        if (BuildConfig.DEBUG) {
            XposedBridge.log("LockscreenNowBar, updateNowBar" + "\n" +
                    "mNowBarEnabled: " + mNowBarEnabled + "\n" +
                    "mHideLeftAfforfance: " + mHideLeftAfforfance + "\n" +
                    "mHideRightAffordance: " + mHideRightAffordance + "\n" +
                    "mNowBarBottomMargin: " + mNowBarBottomMargin + "\n" +
                    "mNowBarWeather: " + mNowBarWeather);
        }
        boolean isLeftHidden = false;
        try {
            Object affordance = callMethod(mAffordanceSqlHelper, "getInstance");
            String left = (String) callMethod(affordance, "queryStartSelection", mContext);
            isLeftHidden = !TextUtils.isEmpty(left) && left.equals("none");
        } catch (Throwable ignored) {
            isLeftHidden = mHideLeftAfforfance;
        }
        NowBarController mNowBarController = NowBarController.getInstance(mContext);
        mNowBarController.setNowBarEnabled(mNowBarEnabled);
        mNowBarController.setNowBarWeatherEnabled(mNowBarWeather);
        mNowBarController.updateMargins(
                isLeftHidden && mHideRightAffordance ? 18 : mAffordanceWidth,
                isLeftHidden && mHideRightAffordance ? 18 : mAffordanceWidth,
                dp2px(mContext, mNowBarBottomMargin)
        );
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(listenPackage);
    }
}
