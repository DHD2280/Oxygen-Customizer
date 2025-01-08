package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_DEVICE_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_DATE_FORMAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_STOCK_CLOCK_RED_ONE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.findViewWithTag;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.loadLottieAnimationView;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.ReflectionTools;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class LockscreenClock extends XposedMods {

    public static final String OC_LOCKSCREEN_CLOCK_TAG = "oxygencustomizer_lockscreen_clock";
    private final static String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final long thresholdTime = 500; // milliseconds
    private static Object mStockClock;
    private static long lastUpdated = System.currentTimeMillis();
    private final String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_clock_font.ttf";
    Class<?> LottieAn = null;
    private ViewGroup mClockViewContainer = null;
    private ViewGroup mStatusViewContainer = null;
    private RelativeLayout mClockView = null;
    private View mMediaHostContainer = null;
    // Lockscreen Clock Prefs
    private boolean customLockscreenClock = false;
    private int lockscreenClockStyle = 1;
    private int topMargin, bottomMargin;
    private float clockScale;
    private int lineHeight;
    private boolean customFontEnabled;
    private String customName, customDeviceName;
    private boolean useCustomUserImage;
    private boolean useCustomImage;
    private String mCustomDateFormat = "";
    // Stock Clock
    private int mStockClockRed, mStockClockRedColor;
    private UserManager mUserManager;
    private AudioManager mAudioManager;
    private ActivityManager mActivityManager;
    private Context appContext;
    private TextView mBatteryStatusView;
    private TextView mBatteryLevelView;
    private TextView mVolumeLevelView;
    private ProgressBar mBatteryProgress;
    private ProgressBar mVolumeProgress;
    private int mBatteryStatus = 1;
    private int mBatteryPercentage = 1;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private final ScheduledExecutorService mDebouncer = Executors.newScheduledThreadPool(1);
    private ScheduledFuture<?> mScheduledFuture;
    private ImageView mVolumeLevelArcProgress;
    private ImageView mRamUsageArcProgress;
    private ImageView mBatteryArcProgress;
    private int accent1, accent2, accent3, text1, text2;
    private boolean customColor;

    private AnimatorSet currentAnimationSet;
    @SuppressLint("StaticFieldLeak")
    public static FrameLayout mCustomClockView = null;
    private View mStockClockView = null;
    public final static int CLOCK_UI_STATE_SHADE = 1;
    public final static int CLOCK_UI_STATE_LS = 2;
    public final static int CLOCK_UI_STATE_AOD = 3;

    private boolean mBatteryReceiverRegistered = false;
    private final BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BATTERY_CHANGED)) {
                mBatteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 1);
                int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 100);
                mBatteryPercentage = (level * 100) / scale;
                initBatteryStatus();
            }
        }
    };

    private boolean mVolumeReceiverRegistered = false;
    private final BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
//            initSoundManager();
        }
    };

    private enum ImageType {
        USER_IMAGE,
        CUSTOM_IMAGE
    }

    public LockscreenClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        customLockscreenClock = Xprefs.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false);
        lockscreenClockStyle = Xprefs.getInt(LOCKSCREEN_CLOCK_STYLE, 0);
        mStockClockRed = Integer.parseInt(Xprefs.getString(LOCKSCREEN_STOCK_CLOCK_RED_ONE, "0"));
        mStockClockRedColor = Xprefs.getInt(LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR, Color.WHITE);
        accent1 = Xprefs.getInt(
                LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT1,
                getPrimaryColor(mContext)
        );
        accent2 = Xprefs.getInt(
                LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT2,
                ContextCompat.getColor(mContext, android.R.color.system_accent2_600)
        );
        accent3 = Xprefs.getInt(
                LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT3,
                ContextCompat.getColor(mContext, android.R.color.system_accent3_600)
        );
        text1 = Xprefs.getInt(
                LOCKSCREEN_CLOCK_COLOR_CODE_TEXT1,
                Color.WHITE
        );
        text2 = Xprefs.getInt(
                LOCKSCREEN_CLOCK_COLOR_CODE_TEXT2,
                Color.BLACK
        );
        customColor = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_COLOR_SWITCH, false);
        topMargin = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_TOP_MARGIN, 100);
        bottomMargin = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_BOTTOM_MARGIN, 40);
        clockScale = Xprefs.getSliderFloat(LOCKSCREEN_CLOCK_TEXT_SCALING, 1.0f);
        lineHeight = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_LINE_HEIGHT, 0);
        customFontEnabled = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_FONT, false);
        customName = Xprefs.getString(LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE, "");
        customDeviceName = Xprefs.getString(LOCKSCREEN_CLOCK_CUSTOM_DEVICE_VALUE, "");
        useCustomUserImage = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE, false);
        useCustomImage = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_IMAGE, false);
        mCustomDateFormat = Xprefs.getString(LOCKSCREEN_CLOCK_DATE_FORMAT, "");

        if (Key.length > 0) {
            for (String LCPrefs : LOCKSCREEN_CLOCK_PREFS) {
                if (Key[0].equals(LCPrefs)) {
                    new Handler(Looper.getMainLooper()).post(this::updateClockView);
                    if (Build.VERSION.SDK_INT >= 35) {
                        createCustomClockView();
                    }
                }
                if (Key[0].equals(LOCKSCREEN_STOCK_CLOCK_RED_ONE) ||
                        Key[0].equals(LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR)) {
                    updateStockClock();
                }
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        LottieAn = ReflectedClass.of("com.airbnb.lottie.LottieAnimationView").getClazz();

        initResources(mContext);

        createCustomClockView();

        if (Build.VERSION.SDK_INT >= 35) {
            ReflectedClass KeyguardStyleClockControllerImpl = ReflectedClass.of("com.oplus.systemui.keyguard.clockstyle.KeyguardStyleClockControllerImpl");
            KeyguardStyleClockControllerImpl
                    .afterConstruction()
                    .run(param -> {
                        Object keyguardStyleClockCallback = getObjectField(param.thisObject, "keyguardStyleClockCallback");
                        ReflectedClass keyguardStyleClockCallbackClass = ReflectedClass.of(keyguardStyleClockCallback.getClass());
                        keyguardStyleClockCallbackClass
                                .after("onCall")
                                .run(param1 -> {
                                    String str = (String) param1.args[0];
                                    Bundle bundle = (Bundle) param1.args[1];

                                    if (bundle == null) {
                                        XposedBridge.log("KeyguardStyleClockControllerImpl.onCall: bundle is null for " + str);
                                        return;
                                    }

                                    HashMap<String, Object> map = new HashMap<>();
                                    for (String key : bundle.keySet()) {
                                        Object value = bundle.get(key);
                                        map.put(key, value);
                                    }
                                    XposedBridge.log("KeyguardStyleClockControllerImpl.onCall: " + map.toString());

                                        });
//                        moveClockView((int) callMethod(param.thisObject, "getUiState"));
                    });

            KeyguardStyleClockControllerImpl
                    .after("setKeyguardStyleClockScale")
                    .run(param -> {
                        float f2, f3, f4;
                        f2 = (float) param.args[0];
                        f3 = (float) param.args[1];
                        f4 = (float) param.args[2];

                        int[] outLocation = new int[2];
                        mCustomClockView.getLocationInWindow(outLocation);
                        XposedBridge.log("setKeyguardStyleClockScale: " + f2 + " " + f3 + " " + f4 + " " + outLocation[0] + " " + outLocation[1]);
                        mCustomClockView.setPivotX(f3 - outLocation[0]);
                        mCustomClockView.setPivotY(f4 - outLocation[1]);
                        mCustomClockView.setScaleX(f2);
                        mCustomClockView.setScaleY(f2);

                    });

            ReflectedClass OplusKeyguardStyleBaseClock = ReflectedClass.of("com.oplus.keyguard.OplusKeyguardStyleBaseClock");

            /*
             * OOS15
             * They added a Plugin for Lockscreen Clock
             * OplusKeyguardStyleBaseClock and OplusKeyguardStyleClock
             * responsible to load everything related to lockscreen
             *
             * we have getView(int i) method that returns views
             * we refers i to the following: (source KeyguardStyleClockControllerImpl)
             *
             * 0 used in setKeyguardStyleClockVisibility, setKeyguardStyleClockAlpha
             * Apparently we can set the Lockscreen Clock here
             *
             * 1 used in setKeyguardStyleClockScale, getKeyguardStyleClockHeight
             * Apparently we can set the Lockscreen Clock here
             * With Weather and Widgets
             * since is called getKeyguardStyleClockHeight
             * putting our clock to the bottom of LockIconView
             * and calculating height for notifications
             * so we need to calculate height with weather and widgets
             *
             * 2 never seen
             * 3 same as 2
             *
             * 4 used in addDeepSceneryImageView, removeDeepSceneryImageView, setKeyguardStyleForegroundScale, refreshDeepSceneryMatrix
             * Should be the image bundled with some styles
             * We can try to set our depth here and see what happens
             */
            OplusKeyguardStyleBaseClock
                    .before("getView")
                    .run(param -> {
                        int viewType = (int) param.args[0];
                        if (viewType == 0 || viewType == 1) {
//                            ReflectionTools.dumpView((View) param.getResult());
                            if (customLockscreenClock) {
                                param.setResult(mCustomClockView);
                            }
                        }
                    });
            OplusKeyguardStyleBaseClock
                    .after("getView")
                    .run(param -> {
//                        ReflectionTools.dumpView((View) param.getResult());
//                        if (customLockscreenClock) return;
//                        if ((int) param.args[0] != 0) return;
//
//                        ViewGroup group = (ViewGroup) param.getResult();
//                        if (group.findViewWithTag("OC_CLOCK_LINEAR") == null) {
//                            LinearLayout linearLayout = new LinearLayout(mContext);
//                            linearLayout.setTag("OC_CLOCK_LINEAR");
//                            linearLayout.setLayoutParams(new ViewGroup.LayoutParams(
//                                    ViewGroup.LayoutParams.MATCH_PARENT,
//                                    ViewGroup.LayoutParams.WRAP_CONTENT
//                            ));
//                            linearLayout.setOrientation(LinearLayout.VERTICAL);
//                            group.addView(linearLayout, group.getChildCount() - 1);
//                        }
                    });

            ReflectedClass OplusKeyguardStyleClock = ReflectedClass.of("com.oplus.keyguard.OplusKeyguardStyleClock");
//            OplusKeyguardStyleClock
//                    .after("onUiStateChanged")
//                    .run(param -> moveClockView((int) param.args[0]));

            OplusKeyguardStyleClock
                    .after("isAodTranslationChanged")
                    .run(param -> {
                        boolean b = (boolean) param.getResult();
                        XposedBridge.log("isAodTranslationChanged: " + b);
                    });

            OplusKeyguardStyleClock
                    .after("setAodUiDeBurnin")
                    .run(param -> {
                        int x = (int) param.args[0];
                        int y = (int) param.args[1];
                        XposedBridge.log("setAodUiDeBurnin: X: " + x + " Y: " + y);
                    });

        } else {
            Class<?> KeyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView", lpparam.classLoader);
            hookAllMethods(KeyguardStatusViewClass, "onFinishInflate", new XC_MethodHook() {
                @SuppressLint("DiscouragedApi")
                @Override
                protected void afterHookedMethod(MethodHookParam param) {

                    ViewGroup statusViewContainer = (ViewGroup) getObjectField(param.thisObject, "mStatusViewContainer");
                    mStatusViewContainer = (ViewGroup) param.thisObject;
                    mClockViewContainer = statusViewContainer;

                    // Hide stock clock
                    GridLayout KeyguardStatusView = (GridLayout) param.thisObject;

                    mClockView = KeyguardStatusView.findViewById(mContext.getResources().getIdentifier("keyguard_clock_container", "id", mContext.getPackageName()));

                    mMediaHostContainer = (View) getObjectField(param.thisObject, "mMediaHostContainer");

                    registerClockUpdater();
                }
            });
        }


        Class<?> SingleClockView;
        try {
            SingleClockView = findClass("com.oplus.systemui.shared.clocks.SingleClockView", lpparam.classLoader);
        } catch (Throwable t) {
            SingleClockView = findClass("com.oplusos.systemui.keyguard.clock.SingleClockView", lpparam.classLoader); // OOS 13
        }
        findAndHookMethod(SingleClockView, "updateStandardTime", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mStockClock = param.thisObject;

                if (customLockscreenClock || mStockClockRed == 0) return;

                try {
                    TextView mTimeHour = (TextView) getObjectField(param.thisObject, "mTimeHour");
                    String mHour = (String) getObjectField(param.thisObject, "mHour");
                    setClockRed(mTimeHour, mHour);
                } catch (Throwable ignored) {
                }
            }
        });

        Class<?> RedTextClock;
        try {
            RedTextClock = findClass("com.oplus.systemui.shared.clocks.RedTextClock", lpparam.classLoader);
        } catch (Throwable t) {
            RedTextClock = findClass("com.oplusos.systemui.keyguard.clock.RedTextClock", lpparam.classLoader); // OOS 13
        }
        findAndHookMethod(RedTextClock, "onTimeChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (customLockscreenClock || mStockClockRed == 0) return;

                boolean mShouldRunTicker = getBooleanField(param.thisObject, "mShouldRunTicker");
                if (!mShouldRunTicker) return;

                try {
                    Calendar mTime = (Calendar) getObjectField(param.thisObject, "mTime");
                    String format = (String) getObjectField(param.thisObject, "format");
                    String mHour = DateFormat.format(format, mTime).toString();
                    TextView mTimeHour = (TextView) param.thisObject;
                    setClockRed(mTimeHour, mHour);
                } catch (Throwable ignored) {
                }
            }
        });

    }

    private void initResources(Context context) {
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        mUserManager = SystemUtils.UserManager();
        mAudioManager = SystemUtils.AudioManager();
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            if (!mBatteryReceiverRegistered) {
                context.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
                mBatteryReceiverRegistered = true;
            }
        } catch (Exception ignored) {
        }
        try {
            if (!mVolumeReceiverRegistered) {
                context.registerReceiver(mVolumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
                mVolumeReceiverRegistered = true;
            }
        } catch (Exception ignored) {
        }
    }

    private void createCustomClockView() {
        if (mCustomClockView == null) {
            mCustomClockView = new FrameLayout(mContext);
//            mCustomClockView.setOrientation(LinearLayout.VERTICAL);
            mCustomClockView.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            ));
        } else {
            try {
                View clock = mCustomClockView.findViewWithTag(OC_LOCKSCREEN_CLOCK_TAG);
                if (clock != null) {
                    mCustomClockView.removeView(clock);
                }
            } catch (Throwable ignored) {}
        }
        View clockView = getClockView();
        clockView.setTag(OC_LOCKSCREEN_CLOCK_TAG);
        modifyClockView(clockView);
        mCustomClockView.addView(clockView, 0);
        setMargins(mCustomClockView, mContext, 0, topMargin, 0, bottomMargin);
    }

    // Broadcast receiver for updating clock
    private void registerClockUpdater() {
        if (mClockViewContainer == null) return;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_LOCALE_CHANGED);

        BroadcastReceiver timeChangedReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null) {
                    new Handler(Looper.getMainLooper()).post(() -> updateClockView());
                }
            }
        };

        mContext.registerReceiver(timeChangedReceiver, filter);

        new Handler(Looper.getMainLooper()).post(this::updateClockView);
    }

    private void updateClockView() {
        if (mClockViewContainer == null) return;

        if (customLockscreenClock) {
            if (mClockView != null && mClockView.getVisibility() != View.INVISIBLE)
                mClockView.setVisibility(View.INVISIBLE);
            if (mMediaHostContainer != null && mMediaHostContainer.getVisibility() != View.INVISIBLE)
                mMediaHostContainer.setVisibility(View.INVISIBLE);
        } else {
            if (mClockView != null && mClockView.getVisibility() != View.VISIBLE)
                mClockView.setVisibility(View.VISIBLE);
            if (mMediaHostContainer != null && mMediaHostContainer.getVisibility() != View.VISIBLE)
                mMediaHostContainer.setVisibility(View.VISIBLE);
        }


        long currentTime = System.currentTimeMillis();
        boolean isClockAdded = mClockViewContainer.findViewWithTag(OC_LOCKSCREEN_CLOCK_TAG) != null;

        if (!customLockscreenClock) {
            if (isClockAdded)
                mClockViewContainer.removeView(mClockViewContainer.findViewWithTag(OC_LOCKSCREEN_CLOCK_TAG));
            return;
        }

        if (isClockAdded && currentTime - lastUpdated < thresholdTime) {
            return;
        } else {
            lastUpdated = currentTime;
        }
        View clockView = getClockView();

        // Remove existing clock view
        if (isClockAdded) {
            mClockViewContainer.removeView(mClockViewContainer.findViewWithTag(OC_LOCKSCREEN_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(OC_LOCKSCREEN_CLOCK_TAG);

            int idx = 0;
            if (clockView.getParent() != null) {
                ((ViewGroup) clockView.getParent()).removeView(clockView);
            }


            mClockViewContainer.addView(clockView, idx);
            modifyClockView(clockView);
//            initSoundManager();
            initBatteryStatus();
        }
    }

    @SuppressLint("DiscouragedApi")
    private View getClockView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);

        View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                LOCKSCREEN_CLOCK_LAYOUT + lockscreenClockStyle,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );

        loadLottieAnimationView(
                appContext,
                LottieAn,
                v,
                lockscreenClockStyle
        );

        return v;

    }

    private void modifyClockView(View clockView) {

        int systemAccent = getPrimaryColor(mContext);

        Typeface typeface = null;
        if (customFontEnabled && (new File(customFont).exists())) {
            typeface = Typeface.createFromFile(new File(customFont));
        }

        setMargins(clockView, mContext, 0, topMargin, 0, bottomMargin);

        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", customColor ? accent1 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", customColor ? accent2 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", customColor ? accent3 : systemAccent);
        if (customColor) {
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", text1);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", text2);
        }

        if (typeface != null) {
            ViewHelper.applyFontRecursively((ViewGroup) clockView, typeface);
        }

        ViewHelper.applyTextMarginRecursively((ViewGroup) clockView, lineHeight);

        if (clockScale != 1.0f) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, clockScale);
        }

        TextClock textClock = (TextClock) findViewWithTag(clockView, "textClockDate");
        if (!TextUtils.isEmpty(mCustomDateFormat) && textClock != null) {
            try {
                textClock.setFormat12Hour(mCustomDateFormat);
                textClock.setFormat24Hour(mCustomDateFormat);
            } catch (Throwable t) {
                log("Error setting date format: " + t.getMessage());
            }
        }

        TextView deviceName = (TextView) findViewWithTag(clockView, "device_name");
        if (deviceName != null) {
            deviceName.setText(customDeviceName.isEmpty() ? Build.MODEL : customDeviceName);
        }

        TextView username = (TextView) findViewWithTag(clockView, "username");
        if (username != null) {
            username.setText(customName.isEmpty() ? getUserName() : customName);
        }

        ImageView profilePicture = (ImageView) findViewWithTag(clockView, "profile_picture");
        if (useCustomUserImage && profilePicture != null) {
            profilePicture.post(() -> profilePicture.setImageDrawable(getCustomUserImage()));
        }

        ImageView customImage = (ImageView) findViewWithTag(clockView, "custom_image");
        if (useCustomImage && customImage != null) {
            customImage.post(() -> customImage.setImageDrawable(getCustomImage()));
        }

        switch (lockscreenClockStyle) {
            case 2 -> {
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");
                tickIndicator.setVisibility(View.GONE);
                TextView hourView = (TextView) findViewWithTag(clockView, "hours");
                TimeUtils.setCurrentTimeTextClockRed(tickIndicator, hourView, customColor ? accent1 : getPrimaryColor(mContext));
            }
            case 5 -> {
                mBatteryStatusView = (TextView) findViewWithTag(clockView, "battery_status");
                mBatteryLevelView = (TextView) findViewWithTag(clockView, "battery_percentage");
                mVolumeLevelView = (TextView) findViewWithTag(clockView, "volume_level");
                mBatteryProgress = (ProgressBar) findViewWithTag(clockView, "battery_progressbar");
                mVolumeProgress = (ProgressBar) findViewWithTag(clockView, "volume_progressbar");
            }
            case 7 -> {
                ImageView imageView = (ImageView) findViewWithTag(clockView, "user_profile_image");
                imageView.post(() ->
                        imageView.setImageDrawable(useCustomUserImage ? getCustomUserImage() : getUserImage()));
            }
            case 19 -> {
                mBatteryLevelView = (TextView) findViewWithTag(clockView, "battery_percentage");
                mBatteryProgress = (ProgressBar) findViewWithTag(clockView, "battery_progressbar");
                mVolumeLevelArcProgress = (ImageView) findViewWithTag(clockView, "volume_progress");
                mRamUsageArcProgress = (ImageView) findViewWithTag(clockView, "ram_usage_info");

                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(customColor ? accent1 : getPrimaryColor(mContext)));
            }
            case 27 -> {
                TextView hourView = (TextView) findViewWithTag(clockView, "textHour");
                TextView minuteView = (TextView) findViewWithTag(clockView, "textMinute");
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");

                TimeUtils.setCurrentTimeTextClock(mContext, tickIndicator, hourView, minuteView);
            }
            default -> {
                mBatteryStatusView = null;
                mBatteryLevelView = null;
                mVolumeLevelView = null;
                mBatteryProgress = null;
                mVolumeProgress = null;
                mVolumeLevelArcProgress = null;
                mBatteryArcProgress = null;
            }
        }
    }

    private void moveClockView(int uiMode) {
        if (mCustomClockView == null || !customLockscreenClock) return;

        switch (uiMode) {
            case CLOCK_UI_STATE_AOD -> animateViewToAod(mCustomClockView);
            case CLOCK_UI_STATE_LS, CLOCK_UI_STATE_SHADE -> animateViewBack(mCustomClockView);
        }
    }

    public void animateViewToAod(View view) {
        stopCurrentAnimation();

        // Translate View
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", -200f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 200f);

        // Scale View
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 0.8f);

        currentAnimationSet = new AnimatorSet();
        currentAnimationSet.playTogether(translationX, translationY, scaleX, scaleY);
        currentAnimationSet.setDuration(500);
        currentAnimationSet.start();
    }

    public void animateViewBack(View view) {
        stopCurrentAnimation();

        // Reset state
        ObjectAnimator translationX = ObjectAnimator.ofFloat(view, "translationX", 0f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(view, "translationY", 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f);

        currentAnimationSet = new AnimatorSet();
        currentAnimationSet.playTogether(translationX, translationY, scaleX, scaleY);
        currentAnimationSet.setDuration(500);
        currentAnimationSet.start();
    }

    private void stopCurrentAnimation() {
        if (currentAnimationSet != null && currentAnimationSet.isRunning()) {
            currentAnimationSet.cancel();
        }
    }

    private void initBatteryStatus() {
        if (mBatteryStatusView != null) {
            if (mBatteryStatus == BatteryManager.BATTERY_STATUS_CHARGING) {
                mBatteryStatusView.setText(ResourceManager.modRes.getString(R.string.battery_charging));
            } else if (mBatteryStatus == BatteryManager.BATTERY_STATUS_DISCHARGING ||
                    mBatteryStatus == BatteryManager.BATTERY_STATUS_NOT_CHARGING) {
                mBatteryStatusView.setText(ResourceManager.modRes.getString(R.string.battery_discharging));
            } else if (mBatteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
                mBatteryStatusView.setText(ResourceManager.modRes.getString(R.string.battery_full));
            } else if (mBatteryStatus == BatteryManager.BATTERY_STATUS_UNKNOWN) {
                mBatteryStatusView.setText(ResourceManager.modRes.getString(R.string.battery_level_percentage));
            }
        }

        if (mBatteryProgress != null) {
            mBatteryProgress.setProgress(mBatteryPercentage);
            if (lockscreenClockStyle == 19) {
                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(customColor ? accent1 : getPrimaryColor(mContext)));
            }
        }
        if (mBatteryArcProgress != null) {
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    mBatteryPercentage,
                    appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage),
                    32,
                    "BATTERY",
                    20,
                    customColor ? accent1 : getPrimaryColor(mContext)
            );
            mBatteryArcProgress.setImageBitmap(widgetBitmap);
        }
        if (mBatteryLevelView != null) {
            mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage));
        }

        initRamUsage();
    }

    private void initSoundManager() {
        int volLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int maxVolLevel = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int volPercent = (int) (((float) volLevel / maxVolLevel) * 100);

        if (mVolumeProgress != null) {
            mVolumeProgress.post(() -> mVolumeProgress.setProgress(volPercent));
        }
        if (mVolumeLevelView != null) {
            mVolumeLevelView.post(() -> mVolumeLevelView.setText(appContext.getResources().getString(R.string.percentage_text, volPercent)));
        }

        if (mVolumeLevelArcProgress != null && mVolumeLevelArcProgress.getVisibility() == View.VISIBLE) {
            updateVolumeLevelDebounced(volPercent);
        }
    }

    public void updateVolumeLevelDebounced(int volPercent) {
        if (mScheduledFuture != null && !mScheduledFuture.isDone()) {
            mScheduledFuture.cancel(false);
        }

        mScheduledFuture = mDebouncer.schedule(() -> mExecutor.execute(() -> updateVolumeLevel(volPercent)),
                300, TimeUnit.MILLISECONDS);
    }

    private void updateVolumeLevel(int volPercent) {
        if (mVolumeLevelArcProgress == null) return;

        Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                mContext,
                volPercent,
                appContext.getResources().getString(R.string.percentage_text, volPercent),
                32,
                ContextCompat.getDrawable(appContext, R.drawable.ic_volume_up),
                36,
                customColor ? accent1 : getPrimaryColor(mContext)
        );
        mVolumeLevelArcProgress.post(() -> mVolumeLevelArcProgress.setImageBitmap(widgetBitmap));
    }

    private void initRamUsage() {
        if (mActivityManager == null) return;

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        long usedMemory = memoryInfo.totalMem - memoryInfo.availMem;
        if (memoryInfo.totalMem == 0) return;
        int usedMemoryPercentage = (int) ((usedMemory * 100) / memoryInfo.totalMem);

        if (mRamUsageArcProgress != null && mRamUsageArcProgress.getVisibility() == View.VISIBLE) {
            mExecutor.submit(() -> {
                Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                        mContext,
                        usedMemoryPercentage,
                        appContext.getResources().getString(R.string.percentage_text, usedMemoryPercentage),
                        32,
                        "RAM",
                        20,
                        customColor ? accent1 : getPrimaryColor(mContext)
                );
                mRamUsageArcProgress.setImageBitmap(widgetBitmap);
            });
        }
    }

    @SuppressLint("MissingPermission")
    private String getUserName() {
        if (mUserManager == null) {
            return "User";
        }

        String username = mUserManager.getUserName();
        return !username.isEmpty() ?
                mUserManager.getUserName() :
                appContext.getResources().getString(R.string.default_user_name);
    }

    @SuppressWarnings("all")
    private Drawable getUserImage() {
        if (mUserManager == null) {
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }

        try {
            Method getUserIconMethod = mUserManager.getClass().getMethod("getUserIcon", int.class);
            int userId = (int) UserHandle.class.getDeclaredMethod("myUserId").invoke(null);
            Bitmap bitmapUserIcon = (Bitmap) getUserIconMethod.invoke(mUserManager, userId);
            return new BitmapDrawable(mContext.getResources(), bitmapUserIcon);
        } catch (Throwable throwable) {
            log(throwable);
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }
    }

    private Drawable getImageFromFile(String fileName, @DrawableRes int defaultImage) {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/" + fileName));

            Drawable drawable = ImageDecoder.decodeDrawable(source);

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                ((AnimatedImageDrawable) drawable).start();
            }

            return drawable;
        } catch (Throwable t) {
            log(t);
            return ResourcesCompat.getDrawable(appContext.getResources(), defaultImage, appContext.getTheme());
        }
    }

    private Drawable getCustomUserImage() {
        return getImageFromFile("lockscreen_user_image.png", R.drawable.default_avatar);
    }

    private Drawable getCustomImage() {
        return getImageFromFile("lockscreen_custom_image.png", R.drawable.relax);
    }

    private void updateStockClock() {
        if (mStockClock != null) {
            callMethod(mStockClock, "updateStandardTime");
        }
    }

    private void setClockRed(TextView tv, String hour) {
        int colorToApply = getPrimaryColor(mContext);
        if (mStockClockRed == 1) {
            colorToApply = tv.getCurrentTextColor();
        } else if (mStockClockRed == 3) colorToApply = mStockClockRedColor;
        StringBuilder sb = new StringBuilder(hour);
        SpannableString spannableString = new SpannableString(sb);
        for (int i = 0; i < 2 && i < sb.length(); i++) {
            if (sb.charAt(i) == '1') {
                spannableString.setSpan(new ForegroundColorSpan(colorToApply), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        tv.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
