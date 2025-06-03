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
import it.dhd.oxygencustomizer.xposed.hooks.systemui.QsStyleObserver;
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
    private int mPeekCardTitleColor;
    private int mPeekCardSummaryColor;
    private int mPeekCardBgColor;
    private int mPeekCardButtonsColor;
    private float[] mPeekCardRadius = new float[8];
    private boolean mPeekAppIcons = false;
    private boolean mPeekIgnoreSecurity = false;

    // Icon Style
    private int mPeekIconStyle = 0;
    private int mPeekIconBgColor;
    private int mPeekIconSize, mPeekIconMargin, mPeekIconPadding;

    // Clear all button
    private int mClearAllMode = 1;
    private int mClearAllCount = 4;

    public LockscreenPeekDisplay(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

        mPeekEnabled = Xprefs.getBoolean(LOCKSCREEN_PEEK_NOTIFICATIONS_ENABLED, false);
        mPeekLocation = Integer.parseInt(Xprefs.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_LOCATION, "0"));
        mPeekStyle = Integer.parseInt(Xprefs.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_STYLE, "0"));
        mPeekCardTitleColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_TITLE_COLOR, PeekDisplayView.getPrimaryColor(mContext));
        mPeekCardSummaryColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_SUMMARY_COLOR, PeekDisplayView.getSecondaryColor(mContext));
        mPeekCardBgColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_BG_COLOR, PeekDisplayView.getSurfaceColor(mContext));
        mPeekAppIcons = Xprefs.getBoolean(LOCKSCREEN_PEEK_USE_APP_ICON, false);
        mTopMargin = Xprefs.getSliderInt(LOCKSCREEN_PEEK_TOP_MARGIN, 0);
        mPeekIgnoreSecurity = Xprefs.getBoolean(LOCKSCREEN_PEEK_IGNORE_SECURITY, false);
        // Icon Style
        mPeekIconBgColor = Xprefs.getInt(LOCKSCREEN_PEEK_ICON_BG_COLOR, PeekDisplayView.getSurfaceColor(mContext));
        mPeekIconStyle = Integer.parseInt(Xprefs.getString(LOCKSCREEN_PEEK_ICON_STYLE, "0"));
        mPeekIconSize = Xprefs.getInt(LOCKSCREEN_PEEK_ICON_SIZE, modRes.getDimensionPixelSize(R.dimen.peek_display_notification_icon_size));
        mPeekIconMargin = Xprefs.getInt(LOCKSCREEN_PEEK_ICON_MARGIN, modRes.getDimensionPixelSize(R.dimen.peek_display_notification_icon_margin_end));
        mPeekIconPadding = Xprefs.getInt(LOCKSCREEN_PEEK_ICON_PADDING, 0);

        mPeekCardRadius = new float[]{
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TSX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TSX, 26f)),
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TDX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_TDX, 26f)),
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BDX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BDX, 26f)),
                dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BSX, 26f)), dp2px(mContext, Xprefs.getSliderFloat(LOCKSCREEN_PEEK_CARD_BSX, 26f))
        };

        mPeekCardButtonsColor = Xprefs.getInt(LOCKSCREEN_PEEK_CARD_BUTTONS_COLOR, PeekDisplayView.getPrimaryColor(mContext));

        mClearAllMode = Integer.parseInt(Xprefs.getString(LOCKSCREEN_PEEK_CLEAR_ALL_MODE, "1"));
        mClearAllCount = Xprefs.getInt(LOCKSCREEN_PEEK_CLEAR_ALL_COUNT, 4);

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
            this.mStatusBarState = mStatusBarState;
            updateVisibility();
        });
        ControllersProvider.registerKeyguardShowingCallback(showing -> {
            mKeyguardShowing = showing;
            updateVisibility();
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

        ReflectedClass NotificationLockscreenUserManagerImpl = ReflectedClass.of("com.android.systemui.statusbar.NotificationLockscreenUserManagerImpl");
        NotificationLockscreenUserManagerImpl
                .before("shouldShowLockscreenNotifications")
                .run(param -> {
                    if (mPeekEnabled) {
                        param.setResult(false);
                    }
                });

        ReflectedClass NotificationPanelViewControllerExImp = ReflectedClass.of("com.oplus.systemui.shade.NotificationPanelViewControllerExImp");
        NotificationPanelViewControllerExImp
                .before("setNotificationsConstraints")
                .run(param -> {
                    int top = (int) param.args[3];
                    if (mPeekContainer == null) return;
                    ViewHelper.setMarginsNoConvert(mPeekContainer, mContext, 0, top + dp2px(mContext, mTopMargin), 0, 0);
                });

        ReflectedClass KeyguardStatusBarView = ReflectedClass.of("com.android.systemui.statusbar.phone.KeyguardStatusBarView");
        KeyguardStatusBarView
                .after("setNotificationPanelController")
                .run(param -> {
                    notificationController = param.args[0];
                });


        ReflectedClass NotificationStackScrollLayoutExtImpl = ReflectedClass.of("com.oplus.systemui.statusbar.notification.stack.NotificationStackScrollLayoutExtImpl");
        NotificationStackScrollLayoutExtImpl
                .after("getEndTopPosition")
                .run(param -> {
                    if (!mKeyguardShowing || mStatusBarState != 1) return;
                    if (mPeekContainer == null) return;
                    if (QsStyleObserver.isSeparateStyle()) return;
                    float position = (float) param.getResult();
                    ViewHelper.setMarginsNoConvert(mPeekContainer, mContext, 0, (int) position + dp2px(mContext, mTopMargin), 0, 0);
                });

        ReflectedClass NotificationStackScrollLayout = ReflectedClass.of("com.android.systemui.statusbar.notification.stack.NotificationStackScrollLayout");
        NotificationStackScrollLayout
                .before("updateStackEndHeight")
                .run(param -> {
                    // float f2, float f3, float f4
                    float f2, f3;
                    f2 = (float) param.args[0];
                    f3 = (float) param.args[1];
                    if (!QsStyleObserver.isSeparateStyle()) return;
                    ViewHelper.setMarginsNoConvert(mPeekContainer, mContext, 0, (int)f2 - (int)f3 + dp2px(mContext, mTopMargin), 0, 0);
                });

        ReflectedClass NotificationListener = ReflectedClass.of("com.android.systemui.statusbar.NotificationListener");

        NotificationListener
                .afterConstruction()
                .run(param -> {
                    PeekDisplayViewController.getInstance().setNotificationListener(param.thisObject);
                });

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
                mPeekIconStyle,
                mPeekIconBgColor,
                mPeekIconSize,
                mPeekIconMargin,
                mPeekIconPadding,
                mPeekCardTitleColor,
                mPeekCardSummaryColor,
                mPeekCardBgColor,
                mPeekCardRadius,
                mPeekCardButtonsColor,
                mPeekAppIcons,
                mClearAllMode,
                mClearAllCount
        );

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

}
