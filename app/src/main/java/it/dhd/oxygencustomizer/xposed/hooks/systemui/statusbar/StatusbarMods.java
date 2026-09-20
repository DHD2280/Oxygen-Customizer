package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setFloatField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_BOOT_COMPLETED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_OPEN_QUICK_SETTINGS;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_BRIGHTNESS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_DT_SLEEP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_PADDING_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_PADDING_SIDE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_PADDING_TOP;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.QsStyleObserver.isSeparateStyle;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
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
import it.dhd.oxygencustomizer.xposed.utils.DrawableSize;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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
                switch (intent.getAction()) {
                    case ACTIONS_BOOT_COMPLETED:
                        updateStatusbarHeight();
                        break;
                    case ACTIONS_OPEN_QUICK_SETTINGS:
                        openQuickSettings();
                        break;
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
    private static String QSExpandMethodName;
    private float mTopPad;
    private Object mActivityStarter;
    private Class<?> NotificationIconAreaController;
    private Class<?> ScalingDrawableWrapper = null;
    private Object mNotificationIconAreaController = null;
    private Object mNotificationIconContainer = null;
    private boolean mNewIconStyle;
    private float mNewIconScale = 1f;
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
        doubleTapToSleepStatusbarEnabled = Xprefs.getBoolean(STATUSBAR_DT_SLEEP, false);

        // Brightness Control
        mBrightnessControl = Xprefs.getBoolean(STATUSBAR_BRIGHTNESS, false);

        // Padding
        mTopPad = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                Xprefs.getSliderFloat(STATUSBAR_PADDING_TOP, 0f),
                mContext.getResources().getDisplayMetrics());
        statusBarPadding = Xprefs.getBoolean(STATUSBAR_PADDING_ENABLED, false);

        // Notifications
        mNewIconStyle = Xprefs.getBoolean("statusbar_notification_app_icon", false);
        mNewIconScale = Xprefs.getSliderFloat("statusbar_notification_app_icon_scale", 1f);

        List<Float> paddings = Xprefs.getSliderValues("statusbarPaddings", 0);
        if (paddings.size() > 1) {
            SBPaddingStart = paddings.get(0);
            SBPaddingEnd = 100f - paddings.get(1);
        }

        if (Key.length > 0) {
            switch (Key[0]) {
                case STATUSBAR_PADDING_SIDE,
                     STATUSBAR_PADDING_TOP -> updateStatusbarHeight();
                case STATUSBAR_PADDING_ENABLED -> updateResources();
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
            intentFilter.addAction(ACTIONS_OPEN_QUICK_SETTINGS);
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

        ReflectedClass NotificationPanelViewControllerClass = ReflectedClass.of("com.android.systemui.shade.NotificationPanelViewController",
                "com.android.systemui.statusbar.phone.NotificationPanelViewController");
        ReflectedClass PhoneStatusBarView = ReflectedClass.of("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader);
        ReflectedClass PhoneStatusBarViewControllerClass = ReflectedClass.of("com.android.systemui.statusbar.phone.PhoneStatusBarViewController", lpparam.classLoader);
        ReflectedClass QSSecurityFooterUtilsClass = ReflectedClass.of("com.android.systemui.qs.QSSecurityFooterUtils",
                "com.android.systemui.qs.QSSecurityFooter");
        ReflectedClass QuickStatusBarHeaderClass = ReflectedClass.of("com.oplus.systemui.qs.OplusQuickStatusBarHeader",
                "com.android.systemui.qs.QuickStatusBarHeader");

        QSSecurityFooterUtilsClass
                .afterConstruction()
                .run(param -> {
                    mActivityStarter = getObjectField(param.thisObject, "mActivityStarter");
                });

        final ClickListener clickListener = new ClickListener();

        //marking clock instances for recognition and setting click actions on some icons
        QuickStatusBarHeaderClass
                .after("onFinishInflate")
                .run(param -> {
                    try {
                        //Clickable icons
                        Object mBatteryRemainingIcon = getObjectField(param.thisObject, "mBatteryView");

                        callMethod(mBatteryRemainingIcon, "setOnClickListener", clickListener);
                        callMethod(mBatteryRemainingIcon, "setOnLongClickListener", clickListener);
                    } catch (Throwable e) {
                        log(e);
                    }
                });

        try { //13 QPR3
            hookTouchHandler(PhoneStatusBarViewControllerClass.getClazz());
        } catch (Throwable ignored) {
        }

        PhoneStatusBarView
                .afterConstruction()
                .run(param -> PSBV = param.thisObject);

        PhoneStatusBarView
                .after("updateStatusBarHeight")
                .run(param -> {
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
                });

        NotificationPanelViewControllerClass
                .afterConstruction()
                .run(param -> {
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
                });

        NotificationPanelViewControllerClass
                .after("createTouchHandler")
                .run(param -> {
                    hookTouchHandler(param.getResult().getClass());
                });


        ReflectedClass OplusQSFooterImpl = ReflectedClass.of("com.oplus.systemui.qs.OplusQSFooterImpl",
                "com.oplusos.systemui.qs.OplusQSFooterImpl");

        LongClickListener onLongClick = new LongClickListener();
        OplusQSFooterImpl
                .after("onFinishInflate")
                .run(param -> {
                    View mSettingsButton = (View) getObjectField(param.thisObject, "mSettingsButton");
                    try {
                        callMethod(mSettingsButton, "setOnLongClickListener", onLongClick);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                });


        QSExpandMethodName = Arrays.stream(NotificationPanelViewControllerClass.getClazz().getMethods())
                .anyMatch(m -> m.getName().equals("expandToQs"))
                ? "expandToQs" //A14
                : "expandWithQs"; //A13


        ReflectedClass CollapsedStatusBarFragmentClass = ReflectedClass.ofIfPossible("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment");

        CollapsedStatusBarFragmentClass
                .afterConstruction()
                .run(param -> mCollapsedStatusBarFragment = param.thisObject);

        CollapsedStatusBarFragmentClass
                .after("onViewCreated")
                .run(param -> mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar"));


        ReflectedClass CentralSurfacesImpl = ReflectedClass.of("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);

        ReflectedClass OplusBrightnessControllerExImpl = ReflectedClass.of("com.oplus.systemui.qs.impl.OplusBrightnessControllerExImpl",
                "com.oplus.systemui.qs.OplusBrightnessControllerExImpl");

        OplusBrightnessControllerExImpl
                .afterConstruction()
                .run(param -> {
                    OplusBrightnessControllerExt = param.thisObject;
                });

        OplusBrightnessControllerExImpl
                .after("setBrightnessMin")
                .run(param -> {
                    mMinimumBacklight = (int) param.args[0];
                });

        OplusBrightnessControllerExImpl
                .after("setBrightnessMax")
                .run(param -> {
                    mMaximumBacklight = (int) param.args[0];
                });

        CentralSurfacesImpl
                .afterConstruction()
                .run(param -> {
                    mDisplayMetrics = (DisplayMetrics) getObjectField(param.thisObject, "mDisplayMetrics");
                    mDisplayManager = (DisplayManager) mContext.getSystemService(Context.DISPLAY_SERVICE);
                });

        if (Build.VERSION.SDK_INT >= 35) {
            ReflectedClass NotificationStackScrollLayoutExtImpl = ReflectedClass.of("com.oplus.systemui.statusbar.notification.stack.NotificationStackScrollLayoutExtImpl");
            NotificationStackScrollLayoutExtImpl
                    .after("initView")
                    .run(param -> {
                        mQuickQsOffsetHeight = getIntField(param.thisObject, "mQuickQsOffsetHeight");
                    });
        } else {
            try {
                mQuickQsOffsetHeight = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("notification_quick_qs_offset_height", "dimen", listenPackage));
            } catch (Throwable t) {
                log("notification_quick_qs_offset_height not found");
            }
        }

        final GestureDetector mGestureDetector = new GestureDetector(mContext, getPullDownLPListener());

        PhoneStatusBarViewControllerClass
                .before("onTouch")
                .run(param -> {
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
                });

        // Notifications
        ReflectedClass NotificationIconAreaControllerClz = ReflectedClass.ofIfPossible("com.android.systemui.statusbar.phone.NotificationIconAreaController");
        NotificationIconAreaControllerClz
                .afterConstruction()
                .run(param -> mNotificationIconAreaController = param.thisObject);

        ReflectedClass NotificationIconContainer = ReflectedClass.of("com.android.systemui.statusbar.phone.NotificationIconContainer", lpparam.classLoader);
        NotificationIconContainer
                .afterConstruction()
                .run(param -> mNotificationIconContainer = param.thisObject);

        try {
            ScalingDrawableWrapper = findClass("com.android.systemui.statusbar.ScalingDrawableWrapper", lpparam.classLoader);
        } catch (Throwable ignored) {
        }
        ReflectedClass StatusBarIconView = ReflectedClass.of("com.android.systemui.statusbar.StatusBarIconView", lpparam.classLoader);
        try {
            StatusBarIconView
                    .before("getIcon")
                    .run(param -> {
                        if (!mNewIconStyle) return;
                        View v = (View) param.thisObject;
                        Context sysuiContext = v.getContext();
                        Drawable icon = null;
                        Object statusBarIcon;
                        if (param.args.length >= 2)
                            statusBarIcon = param.args[2];
                        else
                            statusBarIcon = param.args[0];
                        String pkgName = (String) getObjectField(statusBarIcon, "pkg");
                        if (pkgName.contains("com.android") || pkgName.contains("systemui"))
                            return;
                        try {
                            if (!pkgName.contains("systemui")) {
                                icon = sysuiContext.getPackageManager().getApplicationIcon(pkgName);
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
                            Log.d("StatusbarMods", "dimen " + dimen + " scaleFactor " + scaleFactor + " mNewIconScale " + mNewIconScale);
                            icon = DrawableSize.downscaleToSize(sysuiContext.getResources(), icon, dimen, dimen);
                            if (Build.VERSION.SDK_INT >= 35) {
                                setFloatField(param.thisObject, "mScaleToFitNewIconSize", mNewIconScale);
                            } else {
                                setFloatField(param.thisObject, "mIconAppearAmount", mNewIconScale);
                            }
                            if (scaleFactor == 1f) { // No need to scale icon
                                param.setResult(icon);
                            } else { // Scale Factor != 1f so return a scaled icon
                                param.setResult(ScalingDrawableWrapper.getConstructor(Drawable.class, float.class).newInstance(icon, scaleFactor));
                            }
                        }
                    });
        } catch (Throwable t) {
            log(t);
        }
    }

    private void openOxygenCustomizer() {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0 /* dismissShade */);
    }

    private void showBatteryPage() {
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", new Intent(Intent.ACTION_POWER_USAGE_SUMMARY), 0);
    }
    //endregion

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

    private GestureDetector.OnGestureListener getPullDownLPListener() {
        return new LongpressListener(true) {
            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (Build.VERSION.SDK_INT >= 35) {
                    if (isSeparateStyle()) return false;
                }
                if (STATUSBAR_MODE_SHADE == (int) getObjectField(NotificationPanelViewController, "mBarState")
                        && isValidFling(e1, e2, velocityY, .15f, 0.01f)) {
                    openQuickSettings();
                    return true;
                }
                return false;
            }
        };
    }

    private void openQuickSettings() {
        callMethod(NotificationPanelViewController, QSExpandMethodName);
    }

    private GestureDetector.OnGestureListener getPullUpListener() {
        return new LongpressListener(false) {
            @Override
            public boolean onFling(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                if (isValidFling(e1, e2, velocityY, -.15f, -.06f)) {
                    if (Build.VERSION.SDK_INT >= 35) {
                        callMethod(NotificationPanelViewController, "collapse", true, 1f, "collapse");
                    } else {
                        callMethod(NotificationPanelViewController, "collapse", true, 1f);
                    }
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
