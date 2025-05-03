package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.*;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMarginsNoConvert;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.peek.PeekDisplayHolder;
import it.dhd.oxygencustomizer.xposed.views.peek.PeekDisplayView;
import it.dhd.oxygencustomizer.xposed.views.peek.PeekDisplayViewController;

public class LockscreenPeekDisplay extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;

    private PeekDisplayHolder mPeekDisplayView;
    private FrameLayout mPeekContainer;

    private Object notificationController;
    private ViewGroup mNotificationPanelViewController;
    private View mNotificationStackScroller;

    private int mStatusBarState = -1;
    private boolean mKeyguardShowing = false;
    private int mTopHeight;
    private int mTopMargin = 0;
    private boolean mMarginSet = false;

    // Preferences
    private boolean mPeekEnabled = false;
    private int mPeekLocation = 0;

    // Style
    private int mPeekStyle = 0;
    private int mPeekIconBgColor;
    private int mPeekNotificationIconSize;
    private int mPeekNotificationIconCornerRadius;
    private int mPeekNotificationIconMarginEnd;
    private int mPeekCardTitleColor;
    private int mPeekCardSummaryColor;
    private int mPeekCardBgColor;
    private int mPeekCardButtonsColor;
    private float[] mPeekCardRadius = new float[8];
    private boolean mPeekAppIcons = false;
    private boolean mPeekIgnoreSecurity = false;

    public LockscreenPeekDisplay(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

        mPeekEnabled = Xprefs.getBoolean(LOCKSCREEN_PEEK_NOTIFICATIONS_ENABLED, false);
        mPeekLocation = Integer.parseInt(Xprefs.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_LOCATION, "0"));
        mPeekStyle = Integer.parseInt(Xprefs.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_STYLE, "0"));
        mPeekIconBgColor = Xprefs.getInt(LOCKSCREEN_PEEK_ICON_BG_COLOR, PeekDisplayView.getSurfaceColor(mContext));
        mPeekNotificationIconSize = modRes.getDimensionPixelSize(R.dimen.peek_display_notification_icon_size);
        mPeekNotificationIconCornerRadius = modRes.getDimensionPixelSize(R.dimen.peek_notification_icon_corner_radius);
        mPeekNotificationIconMarginEnd = modRes.getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end);
        mPeekCardTitleColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_TITLE_COLOR, PeekDisplayView.getPrimaryColor(mContext));
        mPeekCardSummaryColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_SUMMARY_COLOR, PeekDisplayView.getSecondaryColor(mContext));
        mPeekCardBgColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_BG_COLOR, PeekDisplayView.getSurfaceColor(mContext));
        mPeekAppIcons = Xprefs.getBoolean(LOCKSCREEN_PEEK_USE_APP_ICON, false);
        mTopMargin = Xprefs.getSliderInt(LOCKSCREEN_PEEK_TOP_MARGIN, 0);
        mPeekIgnoreSecurity = Xprefs.getBoolean(LOCKSCREEN_PEEK_IGNORE_SECURITY, false);

        mPeekCardRadius = new float[]{
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TSX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TSX, 26f)),
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TDX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TDX, 26f)),
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BDX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BDX, 26f)),
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BSX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BSX, 26f))
        };

        mPeekCardButtonsColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_BUTTONS_COLOR, PeekDisplayView.getPrimaryColor(mContext));

        if (Key.length > 0) {
            for (String peekPref : LOCKSCREEN_PEEK_PREFS) {
                if (peekPref.equals(Key[0])) {
                    updatePeekOptions();
                    break;
                }
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        ControllersProvider.registerStatusBarStateChangedCallback(mStatusBarState -> {
            XposedBridge.log("LockscreenPeekDisplay, StatusBarStateChangedCallback " + mStatusBarState);
            this.mStatusBarState = mStatusBarState;
            updateVisibility();
        });
        Class<?> KeyguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
        hookAllMethods(KeyguardUpdateMonitor, "setKeyguardShowing", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mKeyguardShowing = (boolean) param.args[0];
                updateVisibility();
            }
        });

        ReflectedClass KeyguardStatusViewController = ReflectedClass.of("com.android.keyguard.KeyguardStatusViewController");
        KeyguardStatusViewController
                .after("setAlpha")
                .run(param -> {
                    if (mPeekContainer == null) return;
                    mPeekContainer.setAlpha((float) param.args[0]);
                });

        ReflectedClass NotificationPanelViewController = ReflectedClass.of("com.android.systemui.shade.NotificationPanelViewController");
        NotificationPanelViewController
                .after("onFinishInflate")
                .run(param -> {
                    if (notificationController != null && notificationController != param.thisObject) {
                        XposedBridge.log("LockscreenPeekDisplay, NotificationPanelViewController onFinishInflate, notificationController != param.thisObject");
                        return;
                    }
                    XposedBridge.log("NotificationPanelViewController, NotificationPanelViewController onFinishInflate");
                    mNotificationPanelViewController = (ViewGroup) getObjectField(param.thisObject, "mView");
                    Object mNotificationStackScrollLayoutController = getObjectField(param.thisObject, "mNotificationStackScrollLayoutController");

                    mNotificationStackScroller = (View) callMethod(mNotificationStackScrollLayoutController, "getView");
                    Object mClockPositionResult = getObjectField(param.thisObject, "mClockPositionResult");
                    mTopHeight = getIntField(mClockPositionResult, "stackScrollerPadding");
                    int stackScrollerPaddingExpanded = getIntField(mClockPositionResult, "stackScrollerPaddingExpanded");
                    XposedBridge.log("LockscreenPeekDisplay, NotificationPanelViewController onFinishInflate, mTopHeight: " + mTopHeight + ", stackScrollerPaddingExpanded: " + stackScrollerPaddingExpanded);
                    placePeek();
                });

        ReflectedClass NotificationKeyguardHelper = ReflectedClass.of("com.oplus.systemui.statusbar.notification.helper.NotificationKeyguardHelper");
        NotificationKeyguardHelper
                .before("shouldShowOnKeyguard")
                .run(param -> {
                    if (mPeekEnabled) param.setResult(false);
                });

//        ReflectedClass KeyguardStyleClockControllerImpl = ReflectedClass.of("com.oplus.systemui.keyguard.clockstyle.KeyguardStyleClockControllerImpl");
//        KeyguardStyleClockControllerImpl
//                .after("getKeyguardStyleClockHeight")
//                .run(param -> {
//                    XposedBridge.log("getKeyguardStyleClockHeight " + (int) param.getResult());
//                    int height = (int) param.getResult();
//                    if (mPeekContainer == null) return;
//                    ViewHelper.setMarginsNoConvert(mPeekContainer, mContext, 0, (int) height, 0, 0);
//                });

        ReflectedClass NotificationPanelViewControllerExImp = ReflectedClass.of("com.oplus.systemui.shade.NotificationPanelViewControllerExImp");
        NotificationPanelViewControllerExImp
                .before("setNotificationsConstraints")
                .run(param -> {
                    int top = (int) param.args[3];
                    if (mPeekContainer == null) return;
                    ViewHelper.setMarginsNoConvert(mPeekContainer, mContext, 0, top + dp2px(mContext, mTopMargin), 0, 0);
                    XposedBridge.log("setNotificationsConstraints " + top);
                });
        //

        ReflectedClass KeyguardStatusBarView = ReflectedClass.of("com.android.systemui.statusbar.phone.KeyguardStatusBarView");
        KeyguardStatusBarView
                .after("setNotificationPanelController")
                .run(param -> {
                    log("KeyguardStatusBarView setNotificationPanelController");
                    notificationController = param.args[0];
                });

        ReflectedClass NotificationStackScrollLayoutExtImpl = ReflectedClass.of("com.oplus.systemui.statusbar.notification.stack.NotificationStackScrollLayoutExtImpl");
        NotificationStackScrollLayoutExtImpl
                .after("getEndTopPosition")
                .run(param -> {
                    if (!mKeyguardShowing || mStatusBarState != 1) return;
                    if (mPeekContainer == null) return;
                    float position = (float) param.getResult();
                    ViewHelper.setMarginsNoConvert(mPeekContainer, mContext, 0, (int) position + dp2px(mContext, mTopMargin), 0, 0);
                    mMarginSet = true;
                });

        ReflectedClass NotificationListener = ReflectedClass.of("com.android.systemui.statusbar.NotificationListener");

        NotificationListener
                .afterConstruction()
                .run(param -> {
                    PeekDisplayViewController.getInstance().setNotificationListener(param.thisObject);
                });

//        ReflectedClass AodRecord = ReflectedClass.of("com.oplus.systemui.aod.AodRecord");
//        AodRecord
//                .before("onNearStateChange")
//                .run(param -> {
//                    PeekDisplayViewController mPeekController = PeekDisplayViewController.getInstance();
//                    int state = (int) param.args[0];
//                    Log.d("LockscreenPeekDisplay", "onNearStateChange: state " + state);
//                    if (state == 4) {
//                        mPeekController.registerAodCallback();
//                    } else {
//                        mPeekController.unRegisterAodCallback();
//                    }
//                });


    }

    private void placePeek() {
        XposedBridge.log("LockscreenPeekDisplay, placePeek mNotificationPanelViewController null?: " + (mNotificationPanelViewController == null));
        mPeekDisplayView = new PeekDisplayHolder(mContext, "TOP");
        mPeekContainer = new FrameLayout(mContext);
        mPeekContainer.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mPeekContainer.addView(mPeekDisplayView);
        if (mNotificationPanelViewController == null) return;
        try {
            ((ViewGroup) mPeekContainer.getParent()).removeView(mPeekContainer);
        } catch (Throwable ignored) {
        }
        updatePeekOptions();
        mNotificationPanelViewController.addView(mPeekContainer, mNotificationPanelViewController.getChildCount() - 1);
        setMarginsNoConvert(mPeekContainer, mContext, 0, mTopHeight + dp2px(mContext, mTopMargin), 0, 0);
    }

    private void updateVisibility() {
        if (mPeekDisplayView == null) return;
        XposedBridge.log("LockscreenPeekDisplay, updateVisibility - mPeekEnabled=" + mPeekEnabled + ", mStatusBarState=" + mStatusBarState + ", mKeyguardShowing=" + mKeyguardShowing);
        if (!mPeekEnabled) {
            mPeekContainer.setVisibility(ViewGroup.GONE);
            return;
        }
        if (mPeekContainer == null) return;
        if (mStatusBarState == 1 || mKeyguardShowing) {
            mNotificationStackScroller.setVisibility(View.GONE);
            mPeekContainer.setVisibility(View.VISIBLE);
        } else {
            mPeekContainer.setVisibility(View.GONE);
        }
    }

    private void updatePeekOptions() {

        PeekDisplayViewController mPeekController = PeekDisplayViewController.getInstance();
        PeekDisplayView mPeekView = mPeekController.getPeekView();
        if (mPeekView == null) return;
        mPeekView.updatePeekDisplayView(mPeekLocation);
        mPeekView.setIgnoreSecurity(mPeekIgnoreSecurity);
        mPeekView.updatePeekStyle(
                mPeekStyle,
                mPeekIconBgColor,
                mPeekNotificationIconSize,
                mPeekNotificationIconCornerRadius,
                mPeekNotificationIconMarginEnd,
                mPeekCardTitleColor,
                mPeekCardSummaryColor,
                mPeekCardBgColor,
                mPeekCardRadius,
                mPeekCardButtonsColor,
                mPeekAppIcons
        );

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

}
