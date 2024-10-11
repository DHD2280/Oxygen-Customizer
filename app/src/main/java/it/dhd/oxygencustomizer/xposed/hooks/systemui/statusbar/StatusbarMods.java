package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_BOOT_COMPLETED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

/**
 * @noinspection RedundantThrows
 */
public class StatusbarMods extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final int SHADE = 0; //frameworks/base/packages/SystemUI/src/com/android/systemui/statusbar/StatusBarState.java - screen unlocked - pulsing means screen is locked - shade locked means (Q)QS is open on lockscreen
    private static final float PADDING_DEFAULT = -0.5f;
    private static final int PULLDOWN_SIDE_RIGHT = 1;
    @SuppressWarnings("unused")
    private static final int PULLDOWN_SIDE_LEFT = 2;
    private static final int STATUSBAR_MODE_SHADE = 0;
    private static final int STATUSBAR_MODE_KEYGUARD = 1;
    @SuppressWarnings("unused")
    private static final int STATUSBAR_MODE_SHADE_LOCKED = 2;
    private static final float BRIGHTNESS_CONTROL_PADDING = 0.15f;
    private static final int BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT = 750; // ms
    private static final int BRIGHTNESS_CONTROL_LINGER_THRESHOLD = 20;
    private static float SBPaddingStart = 0, SBPaddingEnd = 0;
    private static float statusbarPortion = 0.25f;
    final Handler handler = new Handler(Looper.getMainLooper());
    boolean oneFingerPulldownEnabled = false;
    boolean oneFingerPullupEnabled = false;
    GestureDetector mLockscreenDoubleTapToSleep; //event callback for double tap to sleep detection of statusbar only
    // general use
    private Object PSBV;
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTIONS_BOOT_COMPLETED)) {
                    updateStatusbarHeight();
                }
            }
        }
    };
    private View mStatusBarContents = null;
    private boolean statusBarPadding;
    private Object NotificationPanelViewController;
    private int pullDownSide = PULLDOWN_SIDE_RIGHT;
    private float mMinimumBacklight;
    private float mMaximumBacklight;
    private int mInitialTouchX;
    private int mInitialTouchY;
    private int mLinger;
    private int mQuickQsOffsetHeight;
    private boolean mBrightnessControl;
    private boolean mJustPeeked;
    private Object OplusBrightnessControllerExt = null;
    private DisplayMetrics mDisplayMetrics = null;
    private DisplayManager mDisplayManager = null;
    private Object mCollapsedStatusBarFragment = null;
    private ViewGroup mStatusBar;
    Runnable mLongPressed = this::onLongPressBrightnessChange;
    // End Padding Vars
    private boolean doubleTapToSleepStatusbarEnabled;
    // Padding Vars
    private float mTopPad;
    private boolean mKeyguardShowing = false;
    private Object mActivityStarter;
    private Class<?> NotificationIconAreaController;
    private Class<?> DrawableSize = null, ScalingDrawableWrapper = null;
    private Object mNotificationIconAreaController = null;
    private Object mNotificationIconContainer = null;
    private boolean mNewIconStyle;
    private boolean oos13 = false;
    private boolean mBroadcastRegistered = false;


    public StatusbarMods(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        // Quick Pulldown
        oneFingerPulldownEnabled = Xprefs.getBoolean("quick_pulldown", false);
        oneFingerPullupEnabled = Xprefs.getBoolean("quick_collapse", false);
        pullDownSide = Integer.parseInt(Xprefs.getString("quick_pulldown_side", "1"));
        statusbarPortion = Xprefs.getSliderFloat("quick_pulldown_length", 25f) / 100f;

        // Double Tap to Sleep
        doubleTapToSleepStatusbarEnabled = Xprefs.getBoolean("double_tap_sleep_statusbar", false);

        // Brightness Control
        mBrightnessControl = Xprefs.getBoolean("brightness_control", false);

        // Padding
        mTopPad = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Xprefs.getSliderFloat("statusbar_top_padding", 0f),
                mContext.getResources().getDisplayMetrics());
        statusBarPadding = Xprefs.getBoolean("statusbar_padding_enabled", false);

        // Notifications
        mNewIconStyle = Xprefs.getBoolean("statusbar_notification_app_icon", false);

        List<Float> paddings = Xprefs.getSliderValues("statusbarPaddings", 0);
        if (paddings.size() > 1) {
            SBPaddingStart = paddings.get(0);
            SBPaddingEnd = 100f - paddings.get(1);
        }

        if (Key.length > 0) {
            switch (Key[0]) {
                case "statusbarPaddings",
                     "statusbar_top_padding" -> updateStatusbarHeight();
                case "statusbar_padding_enabled" -> updateResources();
                case "statusbar_notification_app_icon" -> updateNotificationIcons();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        if (!mBroadcastRegistered) {
            mBroadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTIONS_BOOT_COMPLETED);
            mContext.registerReceiver(mReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }

        mLockscreenDoubleTapToSleep = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(@NonNull MotionEvent e) {
                if (mStatusBar != null)
                    mStatusBar.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
                SystemUtils.sleep();
                return true;
            }
        });

        Class<?> NotificationPanelViewControllerClass;
        try {
            NotificationPanelViewControllerClass = findClass("com.android.systemui.shade.NotificationPanelViewController", lpparam.classLoader);
        } catch (Throwable e) {
            oos13 = true;
            NotificationPanelViewControllerClass = findClass("com.android.systemui.statusbar.phone.NotificationPanelViewController", lpparam.classLoader);
        }
        Class<?> PhoneStatusBarView = findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader);
        Class<?> PhoneStatusBarViewControllerClass = findClass("com.android.systemui.statusbar.phone.PhoneStatusBarViewController", lpparam.classLoader);
        Class<?> QSSecurityFooterUtilsClass;
        try {
            QSSecurityFooterUtilsClass = findClass("com.android.systemui.qs.QSSecurityFooterUtils", lpparam.classLoader);
        } catch (Throwable e) {
            oos13 = true;
            QSSecurityFooterUtilsClass = findClass("com.android.systemui.qs.QSSecurityFooter", lpparam.classLoader);
        }
        Class<?> QuickStatusBarHeaderClass;
        try {
            QuickStatusBarHeaderClass = findClass("com.oplus.systemui.qs.OplusQuickStatusBarHeader", lpparam.classLoader);
        } catch (Throwable t) {
            oos13 = true;
            QuickStatusBarHeaderClass = findClass("com.android.systemui.qs.QuickStatusBarHeader", lpparam.classLoader);
        }

        hookAllConstructors(QSSecurityFooterUtilsClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mActivityStarter = getObjectField(param.thisObject, "mActivityStarter");
            }
        });

        final ClickListener clickListener = new ClickListener();

        //marking clock instances for recognition and setting click actions on some icons
        hookAllMethods(QuickStatusBarHeaderClass,
                "onFinishInflate", new XC_MethodHook() {
                    @SuppressLint("DiscouragedApi")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //Getting QS text color for Network traffic
                        try {
                            //Clickable icons
                            Object mBatteryRemainingIcon = getObjectField(param.thisObject, "mBatteryView");

                            callMethod(mBatteryRemainingIcon, "setOnClickListener", clickListener);
                            callMethod(mBatteryRemainingIcon, "setOnLongClickListener", clickListener);
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                });

        try { //13 QPR3
            hookTouchHandler(PhoneStatusBarViewControllerClass);
        } catch (Throwable ignored) {
        }

        hookAllConstructors(PhoneStatusBarView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                PSBV = param.thisObject;
            }
        });

        hookAllMethods(PhoneStatusBarView, "updateStatusBarHeight", new XC_MethodHook() {
            @SuppressLint("DiscouragedApi")
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mStatusBarContents = ((View) param.thisObject).findViewById(mContext.getResources().getIdentifier("status_bar_contents", "id", listenPackage));

                if (!statusBarPadding) return;

                int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;

                int paddingStart = SBPaddingStart == PADDING_DEFAULT
                        ? mContext.getResources().getIdentifier("status_bar_padding_start", "type/dimen", listenPackage)
                        : Math.round(SBPaddingStart * screenWidth / 100f);

                int paddingEnd = SBPaddingEnd == PADDING_DEFAULT
                        ? mContext.getResources().getIdentifier("status_bar_padding_end", "type/dimen", listenPackage)
                        : Math.round(SBPaddingEnd * screenWidth / 100f);
                mStatusBarContents.setPaddingRelative(paddingStart, (int) mTopPad, paddingEnd, 0);
            }
        });

        hookAllConstructors(NotificationPanelViewControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                NotificationPanelViewController = param.thisObject;
                Object mTouchHandler = getObjectField(param.thisObject, "mTouchHandler");
                GestureDetector pullUpDetector = new GestureDetector(mContext, getPullUpListener());
                try {
                    hookTouchHandler(getObjectField(param.thisObject, "mStatusBarViewTouchEventHandler").getClass());
                } catch (Throwable ignored) {
                }
                hookAllMethods(mTouchHandler.getClass(), "onTouchEvent", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (oneFingerPullupEnabled
                                && STATUSBAR_MODE_KEYGUARD != (int) getObjectField(NotificationPanelViewController, "mBarState")) {
                            pullUpDetector.onTouchEvent((MotionEvent) param.args[0]);
                        }
                    }
                });
            }
        });

        hookAllMethods(NotificationPanelViewControllerClass, "createTouchHandler", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                hookTouchHandler(param.getResult().getClass());
            }
        });

        Class<?> OplusQSFooterImpl;
        try {
            OplusQSFooterImpl = findClass("com.oplus.systemui.qs.OplusQSFooterImpl", lpparam.classLoader);
        } catch (Throwable e) {
            oos13 = true;
            OplusQSFooterImpl = findClass("com.oplusos.systemui.qs.OplusQSFooterImpl", lpparam.classLoader); // OOS 13
        }

        LongClickListener onLongClick = new LongClickListener();
        hookAllMethods(OplusQSFooterImpl, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                View mSettingsButton = (View) getObjectField(param.thisObject, "mSettingsButton");
                try {
                    callMethod(mSettingsButton, "setOnLongClickListener", onLongClick);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });

        String QSExpandMethodName = Arrays.stream(NotificationPanelViewControllerClass.getMethods())
                .anyMatch(m -> m.getName().equals("expandToQs"))
                ? "expandToQs" //A14
                : "expandWithQs"; //A13


        Class<?> CollapsedStatusBarFragmentClass = findClassIfExists("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.classLoader);

        hookAllConstructors(CollapsedStatusBarFragmentClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mCollapsedStatusBarFragment = param.thisObject;
            }
        });

        findAndHookMethod(CollapsedStatusBarFragmentClass,
                "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");

                    }
                });


        // Stole Keyguard is showing
        Class<?> KayguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
        hookAllMethods(KayguardUpdateMonitor, "setKeyguardShowing", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mKeyguardShowing = (boolean) param.args[0];
            }
        });

        Class<?> CentralSurfacesImpl = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);

        Class<?> OplusBrightnessControllerExImpl;
        try {
            OplusBrightnessControllerExImpl = findClass("com.oplus.systemui.qs.impl.OplusBrightnessControllerExImpl", lpparam.classLoader);
        } catch (Throwable t) {
            OplusBrightnessControllerExImpl = findClass("com.oplus.systemui.qs.OplusBrightnessControllerExImpl", lpparam.classLoader); // OOS 13
        }

        hookAllConstructors(OplusBrightnessControllerExImpl, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                OplusBrightnessControllerExt = param.thisObject;
            }
        });
        hookAllMethods(OplusBrightnessControllerExImpl, "setBrightnessMin", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mMinimumBacklight = (int) param.args[0];
            }
        });
        hookAllMethods(OplusBrightnessControllerExImpl, "setBrightnessMax", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mMaximumBacklight = (int) param.args[0];
            }
        });

        hookAllConstructors(CentralSurfacesImpl, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mDisplayMetrics = (DisplayMetrics) getObjectField(param.thisObject, "mDisplayMetrics");
                mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
            }
        });

        if (!oos13) {
            Class<?> NotificationStackScrollLayoutExtImpl = findClass("com.oplus.systemui.statusbar.notification.stack.NotificationStackScrollLayoutExtImpl", lpparam.classLoader);
            findAndHookMethod(NotificationStackScrollLayoutExtImpl, "initView", Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mQuickQsOffsetHeight = getIntField(param.thisObject, "mQuickQsOffsetHeight");
                }
            });
        } else {
            try {
                mQuickQsOffsetHeight = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("notification_quick_qs_offset_height", "dimen", listenPackage));
            } catch (Throwable t) {
                log("notification_quick_qs_offset_height not found");
            }
        }

        final GestureDetector mGestureDetector = new GestureDetector(mContext, getPullDownLPListener(QSExpandMethodName));

        hookAllMethods(PhoneStatusBarViewControllerClass, "onTouch", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                MotionEvent event =
                        param.args[0] instanceof MotionEvent
                                ? (MotionEvent) param.args[0]
                                : (MotionEvent) param.args[1];

                if (oneFingerPulldownEnabled) {
                    mGestureDetector.onTouchEvent(event);
                }

                if (!mBrightnessControl) return;

                final int action = event.getAction();
                final int x = (int) event.getRawX();
                final int y = (int) event.getRawY();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (y < mQuickQsOffsetHeight) {
                        mLinger = 0;
                        mInitialTouchX = x;
                        mInitialTouchY = y;
                        mJustPeeked = true;
                        handler.removeCallbacks(mLongPressed);
                        handler.postDelayed(mLongPressed, BRIGHTNESS_CONTROL_LONG_PRESS_TIMEOUT);
                    }
                } else if (action == MotionEvent.ACTION_MOVE) {
                    if (y < mQuickQsOffsetHeight && mJustPeeked) {
                        if (mLinger > BRIGHTNESS_CONTROL_LINGER_THRESHOLD) {
                            //mStatusBar.performHapticFeedback(HapticFeedbackConstants.SEGMENT_TICK);
                            adjustBrightness(x);
                        } else {
                            final int xDiff = Math.abs(x - mInitialTouchX);
                            final int yDiff = Math.abs(y - mInitialTouchY);
                            final int touchSlop = ViewConfiguration.get(mContext).getScaledTouchSlop();
                            if (xDiff > yDiff) {
                                mLinger++;
                            }
                            if (xDiff > touchSlop || yDiff > touchSlop) {
                                handler.removeCallbacks(mLongPressed);
                            }
                        }
                    } else {
                        if (y > mQuickQsOffsetHeight) {
                            mJustPeeked = false;
                        }
                        handler.removeCallbacks(mLongPressed);
                    }
                } else if (action == MotionEvent.ACTION_UP
                        || action == MotionEvent.ACTION_CANCEL) {
                    handler.removeCallbacks(mLongPressed);
                }
                //mGestureDetector.onTouchEvent(event);

            }
        });


        // Notifications
        NotificationIconAreaController = findClass("com.android.systemui.statusbar.phone.NotificationIconAreaController", lpparam.classLoader);
        hookAllConstructors(NotificationIconAreaController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mNotificationIconAreaController = param.thisObject;
            }
        });
        Class<?> NotificationIconContainer = findClass("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.classLoader);
        hookAllConstructors(NotificationIconContainer, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mNotificationIconContainer = param.thisObject;
            }
        });
        try {
            DrawableSize = findClassIfExists("com.android.systemui.util.drawable.DrawableSize", lpparam.classLoader);
        } catch (Throwable ignored) {
        }
        try {
            ScalingDrawableWrapper = findClass("com.android.systemui.statusbar.ScalingDrawableWrapper", lpparam.classLoader);
        } catch (Throwable ignored) {
        }
        Class<?> StatusBarIconView = findClass("com.android.systemui.statusbar.StatusBarIconView", lpparam.classLoader);
        findAndHookMethod(StatusBarIconView,
                "getIcon",
                Context.class,
                Context.class,
                "com.android.internal.statusbar.StatusBarIcon",
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!mNewIconStyle) return;
                        Context sysuiContext = (Context) param.args[0];
                        Context context = (Context) param.args[1];
                        Drawable icon = null;
                        Object statusBarIcon = param.args[2];

                        String pkgName = (String) getObjectField(statusBarIcon, "pkg");
                        if (pkgName.contains("com.android") || pkgName.contains("systemui")) return;
                        try {
                            if (!pkgName.contains("systemui")) {
                                icon = context.getPackageManager().getApplicationIcon(pkgName);
                            }
                        } catch (Throwable e) {
                            return;
                        }
                        int dimen = 0;
                        try {
                            boolean isLowRam = (boolean) callStaticMethod(ActivityManager.class, "isLowRamDeviceStatic");
                            dimen = mContext.getResources().getDimensionPixelSize(
                                    mContext.getResources().getIdentifier(
                                            isLowRam ?
                                                    "notification_small_icon_size" :
                                                    "notification_small_icon_size_low_ram", "dimen", FRAMEWORK));
                        } catch (Throwable ignored) {
                        }
                        TypedValue typedValue = new TypedValue();
                        sysuiContext.getResources().getValue(
                                sysuiContext.getResources().getIdentifier("status_bar_icon_scale_factor", "dimen", listenPackage),
                                typedValue, true);
                        float scaleFactor = typedValue.getFloat();

                        if (icon != null) {
                            if (DrawableSize != null) {
                                icon = (Drawable) callStaticMethod(DrawableSize, "downscaleToSize", sysuiContext.getResources(), icon, dimen, dimen);
                            }
                            if (scaleFactor == 1f) { // No need to scale icon
                                param.setResult(icon);
                            } else { // Scale Factor != 1f so return a scaled icon
                                param.setResult(ScalingDrawableWrapper.getConstructor(Drawable.class, float.class).newInstance(icon, scaleFactor));
                            }
                        }
                    }
                });


    }

    private void openOxygenCustomizer() {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0 /* dismissShade */);
    }

    private void showBatteryPage() {
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 0);
    }
    //endregion

    @SuppressLint("DiscouragedApi")
    private void updatePaddings(Object thisObject) {
        if (mStatusBarContents == null) return;

        int screenWidth = mContext.getResources().getDisplayMetrics().widthPixels;

        int paddingStart = SBPaddingStart == PADDING_DEFAULT
                ? mContext.getResources().getIdentifier("status_bar_padding_start", "dimen", listenPackage)
                : Math.round(SBPaddingStart * screenWidth / 100f);

        int paddingEnd = SBPaddingEnd == PADDING_DEFAULT
                ? mContext.getResources().getIdentifier("status_bar_padding_end", "dimen", listenPackage)
                : Math.round(SBPaddingEnd * screenWidth / 100f);

        mStatusBarContents.setPaddingRelative(
                paddingStart,
                (int) mTopPad,
                paddingEnd,
                mStatusBarContents.getPaddingBottom());

    }

    private void onLongPressBrightnessChange() {
        if (mStatusBar != null)
            mStatusBar.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        adjustBrightness(mInitialTouchX);
        mLinger = BRIGHTNESS_CONTROL_LINGER_THRESHOLD + 1;
    }

    private void adjustBrightness(int x) {
        if (mDisplayMetrics == null) return;
        float raw = ((float) x) / mDisplayMetrics.widthPixels;

        // Add a padding to the brightness control on both sides to
        // make it easier to reach min/max brightness
        float padded = Math.min(1.0f - BRIGHTNESS_CONTROL_PADDING,
                Math.max(BRIGHTNESS_CONTROL_PADDING, raw));
        float value = (padded - BRIGHTNESS_CONTROL_PADDING) /
                (1 - (2.0f * BRIGHTNESS_CONTROL_PADDING));

        if (mStatusBar != null)
            mStatusBar.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK);

        final float val = mMinimumBacklight + value * (mMaximumBacklight - mMinimumBacklight);
        callMethod(mDisplayManager, "setTemporaryBrightness", 0, val);
        callMethod(mDisplayManager, "setTemporaryAutoBrightnessAdjustment", val);
        callMethod(OplusBrightnessControllerExt, "setBrightness", (int) val);
    }

    private GestureDetector.OnGestureListener getPullDownLPListener(String QSExpandMethodName) {
        return new LongpressListener(true) {
            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (STATUSBAR_MODE_SHADE == (int) getObjectField(NotificationPanelViewController, "mBarState")
                        && isValidFling(e1, e2, velocityY, .15f, 0.01f)) {
                    callMethod(NotificationPanelViewController, QSExpandMethodName);
                    return true;
                }
                return false;
            }
        };
    }

    private GestureDetector.OnGestureListener getPullUpListener() {
        return new LongpressListener(false) {
            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (isValidFling(e1, e2, velocityY, -.15f, -.06f)) {
                    callMethod(NotificationPanelViewController, "collapse", true, 1f);
                    return true;
                }
                return false;
            }
        };
    }

    private boolean isValidFling(MotionEvent e1, MotionEvent e2, float velocityY, float speedFactor, float heightFactor) {
        Rect displayBounds = ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE)).getCurrentWindowMetrics().getBounds();

        try {
            return (e2.getY() - e1.getY()) / heightFactor > displayBounds.height() //enough travel in right direction
                    && isTouchInRegion(e1, displayBounds.width()) //start point in hot zone
                    && (velocityY / speedFactor > displayBounds.height()); //enough speed in right direction
        } catch (Throwable ignored) {
            return false;
        }
    }

    private boolean isTouchInRegion(MotionEvent motionEvent, float width) {
        float x = motionEvent.getX();
        float region = width * statusbarPortion;

        return (pullDownSide == PULLDOWN_SIDE_RIGHT)
                ? width - region < x
                : x < region;
    }

    private void hookTouchHandler(Class<?> TouchHanlderClass) {
        XC_MethodHook touchHook = new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!doubleTapToSleepStatusbarEnabled) return;

                //double tap to sleep, statusbar only
                if (!(boolean) getObjectField(NotificationPanelViewController, "mPulsing")
                        && !(boolean) getObjectField(NotificationPanelViewController, "mDozing")
                        && (int) getObjectField(NotificationPanelViewController, "mBarState") == SHADE
                        && (boolean) callMethod(NotificationPanelViewController, "isFullyCollapsed")) {
                    mLockscreenDoubleTapToSleep.onTouchEvent((MotionEvent) param.args[param.args.length - 1]);
                }
            }
        };

        hookAllMethods(TouchHanlderClass, "onTouch", touchHook); //13 QPR2
        hookAllMethods(TouchHanlderClass, "handleTouchEvent", touchHook); //A13 R18
    }

    private void updateStatusbarHeight() {
        try {
            callMethod(PSBV, "updateStatusBarHeight");
        } catch (Throwable ignored) {
        }
    }

    private void updateResources() {
        try {
            callMethod(PSBV, "updateResources");
        } catch (Throwable ignored) {
        }
        try {
            callMethod(PSBV, "updateLayoutForCutout");
        } catch (Throwable ignored) {
        }
        try {
            callMethod(PSBV, "requestLayout");
        } catch (Throwable ignored) {
        }
    }

    private void updateNotificationIcons() {
        try {
            callMethod(mNotificationIconAreaController, "updateStatusBarIcons");
        } catch (Throwable ignored) {
        }
        try {
            callMethod(mNotificationIconContainer, "updateState");
        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private static class LongpressListener implements GestureDetector.OnGestureListener {
        final boolean mDetectLongpress;

        public LongpressListener(boolean detectLongpress) {
            mDetectLongpress = detectLongpress;
        }

        @Override
        public boolean onDown(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(@NonNull MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(@NonNull MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(@NonNull MotionEvent e) {
        }

        @Override
        public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }
    }

    //region icon tap related
    class ClickListener implements View.OnClickListener, View.OnLongClickListener {
        public ClickListener() {
        }

        @Override
        public void onClick(View v) {
            String name = mContext.getResources().getResourceName(v.getId());
            if (name.endsWith("batteryRemainingIcon")) {
                showBatteryPage();
            }
        }

        @Override
        public boolean onLongClick(View v) {
            String name = mContext.getResources().getResourceName(v.getId());

            if (name.endsWith("batteryRemainingIcon")) {
                showBatteryPage();
                return true;
            }
            return false;
        }
    }

    class LongClickListener implements View.OnLongClickListener {
        @Override
        public boolean onLongClick(View v) {
            String name = mContext.getResources().getResourceName(v.getId());

            if (name.endsWith("settings_button")) {
                openOxygenCustomizer();
                return true;
            }
            return false;
        }
    }

}
