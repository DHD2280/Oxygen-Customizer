package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_NOW_BAR_EXPANDED_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_LEFT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.isLeftAffordanceHidden;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarController;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarHolder;
import it.dhd.oxygencustomizer.xposed.views.nowbar.NowBarMusic;

public class LockscreenNowBar extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;

    private ViewGroup mKeyguardBottomArea = null;
    private final FrameLayout mNowBarLayout = new FrameLayout(mContext);

    private final ControllersProvider.OnDozingChanged mDozingChanged = isDozing -> NowBarController.getInstance().setDozing(isDozing);

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

        ReflectedClass NotificationPanelViewController = ReflectedClass.of("com.android.systemui.shade.NotificationPanelViewController");
        NotificationPanelViewController
                .after("onFinishInflate")
                .run(param -> {
                    mKeyguardBottomArea = (ViewGroup) getObjectField(param.thisObject, "mView");
                    placeNowBar();
                });

        ReflectedClass KeyguardStatusViewController = ReflectedClass.of("com.android.keyguard.KeyguardStatusViewController");
        KeyguardStatusViewController
                .after("setAlpha")
                .run(param -> {
                    mNowBarLayout.setAlpha((float) param.args[0]);
                });

        Class<?> QSImpl = findClassInArray(
                lpparam,
            "com.android.systemui.qs.QSImpl", //OOS15
            "com.android.systemui.qs.QSFragment" //OOS14
        );
        hookAllMethods(QSImpl, "setQsExpansion", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                boolean isFullyCollapsed = (boolean) callMethod(param.thisObject, "isFullyCollapsed");
                if (NowBarController.hasInstance()) {
                    NowBarController.getInstance().setFullyCollapsed(isFullyCollapsed);
                }
            }
        });

        Class<?> KeyguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
        hookAllMethods(KeyguardUpdateMonitor, "setKeyguardShowing", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (NowBarController.hasInstance()) {
                    NowBarController.getInstance().setKeyguardShowing((boolean) param.args[0]);
                }
            }
        });

        ReflectedClass MediaHierarchyManager = ReflectedClass.of("com.android.systemui.media.controls.ui.controller.MediaHierarchyManager", lpparam.classLoader);
        MediaHierarchyManager
                .before("getAllowMediaPlayerOnLockScreen")
                .run(param -> {
                    if (mNowBarEnabled) param.setResult(false);
                });
        MediaHierarchyManager
                .before("setAllowMediaPlayerOnLockScreen")
                .run(param -> param.args[0] = !mNowBarEnabled);

        ControllersProvider.registerDozingCallback(mDozingChanged);

    }

    private final NowBarMusic.MusicExpansionListener musicExpansionListener = expanded -> {
        if (!expanded) {
            updateBarMargins();
        } else {
            NowBarController mNowBarController = NowBarController.getInstance();
            mNowBarController.updateMargins(
                    dp2px(mContext, 12),
                    dp2px(mContext, 12),
                    dp2px(mContext, mNowBarBottomMargin)
            );
        }
        Intent intent = new Intent(ACTIONS_NOW_BAR_EXPANDED_CHANGED);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.putExtra("isExpanded", expanded);
        mContext.sendBroadcast(intent);
    };

    private void placeNowBar() {
        if (mKeyguardBottomArea == null) return;
        NowBarHolder mNowBarHolder = NowBarHolder.getInstance(mContext, musicExpansionListener);
        try {
            ((ViewGroup) mNowBarHolder.getParent()).removeView(mNowBarHolder);
        } catch (Throwable ignored) {
        }
        mNowBarLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
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
        NowBarController mNowBarController = NowBarController.getInstance();
        mNowBarController.setNowBarEnabled(mNowBarEnabled);
        mNowBarController.setNowBarWeatherEnabled(mNowBarWeather);
        updateBarMargins();
    }

    private void updateBarMargins() {
        boolean isLeftHidden = isLeftAffordanceHidden(mContext, mHideLeftAfforfance);
        NowBarController mNowBarController = NowBarController.getInstance();
        mNowBarController.updateMargins(
                isLeftHidden && mHideRightAffordance ? dp2px(mContext, 12) : mAffordanceWidth,
                isLeftHidden && mHideRightAffordance ? dp2px(mContext, 12) : mAffordanceWidth,
                dp2px(mContext, mNowBarBottomMargin)
        );
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(listenPackage);
    }
}
