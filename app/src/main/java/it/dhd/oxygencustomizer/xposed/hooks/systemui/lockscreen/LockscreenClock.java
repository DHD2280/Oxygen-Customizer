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
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.findViewWithTag;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.loadLottieAnimationView;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;

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
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Calendar;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.LockscreenView;
import it.dhd.oxygencustomizer.xposed.views.ProgressImageView;

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
    private ProgressImageView mVolumeLevelArcProgress;
    private ProgressImageView mRamUsageArcProgress;
    private int accent1, accent2, accent3, text1, text2;
    private boolean customColor;
    private LockscreenView mLockscreenView;
    private View mStockClockView = null;
    public final static int CLOCK_UI_STATE_SHADE = 1;
    public final static int CLOCK_UI_STATE_LS = 2;
    public final static int CLOCK_UI_STATE_AOD = 3;
    private int mAodUiDeBurninY = -1;
    private int mStockClockHeight = -1;
    private int keyguardLockHeight = -1;

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
            initSoundManager();
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
                    createCustomClockView();
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
                    .before("getKeyguardStyleClockHeight")
                    .run(param -> {
                        if (mLockscreenView == null || !customLockscreenClock) return;
                        Object lockIconViewController = getObjectField(param.thisObject, "lockIconViewController");
                        Object obj = callMethod(lockIconViewController, "get");
                        float height = (float) callMethod(obj, "getBottom");
                        keyguardLockHeight = (int) height;
                        mLockscreenView.setKeyguardLockHeight(keyguardLockHeight);
                        int clockHeight = mLockscreenView.getFullHeight();
                        mLockscreenView.updateClockMargins(keyguardLockHeight + dp2px(mContext, topMargin));
                        param.setResult((int) (height + clockHeight + dp2px(mContext, bottomMargin)));
                    });

            ReflectedClass OplusKeyguardStyleBaseClock = ReflectedClass.of("com.oplus.keyguard.OplusKeyguardStyleBaseClock");
            ReflectedClass OplusKeyguardStyleWrapper = ReflectedClass.ofIfPossible("com.oplus.keyguard.comm.OplusKeyguardStyleWrapper");
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
             * 1 used in setKeyguardStyleClockScale, getKeyguardStyleClockHeight - This maybe is custom image
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
            ReflectedClass.ReflectionConsumer lockscreenClockHook = param -> {
                int viewType = (int) param.args[0];
                if (viewType == 0) {
                    if (customLockscreenClock) {
                        param.setResult(mLockscreenView);
                    }
                } else if (viewType == 1) {
                    //TODO: Custom Image for some clock styles
                }
            };

            OplusKeyguardStyleBaseClock
                    .before("getView")
                    .run(lockscreenClockHook);

            OplusKeyguardStyleBaseClock
                    .after("setTime")
                    .run(param -> {
                        XposedBridge.log("LockscreenClock setTime");
                        long time = (long) param.args[0];
                        if (customLockscreenClock) {
                            mLockscreenView.updateClock(time);
                        }
                    });

            if (OplusKeyguardStyleWrapper.getClazz() != null) { // RUI 6.0
                OplusKeyguardStyleWrapper
                        .before("getView")
                        .run(lockscreenClockHook);
            }

            OplusKeyguardStyleBaseClock
                    .before("loadPlugin")
                    .run(param -> {
                        boolean isPluginLoaded = (boolean) callMethod(param.thisObject, "isPluginLoaded");
                        if (!isPluginLoaded) {
                            try {
                                if (mLockscreenView.getParent() != null) {
                                    ((ViewGroup) mLockscreenView.getParent()).removeView(mLockscreenView);
                                }
                                createCustomClockView();
                            } catch (Throwable ignored) {
                            }
                            mAodUiDeBurninY = Settings.System.getInt(mContext.getContentResolver(), "oplus_keyguardstyle_aod_clock_margin_top", 378);
                            mStockClockHeight = Settings.System.getInt(mContext.getContentResolver(), "oplus_keyguardstyle_aod_clock_height", 0);
                            mLockscreenView.setAodStuff(mAodUiDeBurninY, mStockClockHeight);
                        }
                    });

            ReflectedClass OplusKeyguardStyleClock = ReflectedClass.of("com.oplus.keyguard.OplusKeyguardStyleClock");
            OplusKeyguardStyleClock
                    .after("onUiStateChanged")
                    .run(param -> moveClockView((int) param.args[0]));

            OplusKeyguardStyleClock
                    .after("setAodUiDeBurnin")
                    .run(param -> {
                        int x = (int) param.args[0];
                        int y = (int) param.args[1];
                        mAodUiDeBurninY = y;
                        mStockClockHeight = Settings.System.getInt(mContext.getContentResolver(), "oplus_keyguardstyle_aod_clock_height", 0);
                        if (mLockscreenView != null) {
                            mLockscreenView.setAodStuff(mAodUiDeBurninY, mStockClockHeight);
                        }
                        moveClockView(CLOCK_UI_STATE_AOD);
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


        ReflectedClass SingleClockView = ReflectedClass.of(
                "com.oplus.systemui.shared.clocks.SingleClockView", //OOS 14
                "com.oplusos.systemui.keyguard.clock.SingleClockView" // OOS 13
        );
        SingleClockView
                .after("updateStandardTime")
                .run(param -> {
                    mStockClock = param.thisObject;

                    if (customLockscreenClock || mStockClockRed == 0) return;

                    try {
                        TextView mTimeHour = (TextView) getObjectField(param.thisObject, "mTimeHour");
                        String mHour = (String) getObjectField(param.thisObject, "mHour");
                        setClockRed(mTimeHour, mHour);
                    } catch (Throwable ignored) {
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
        if (Build.VERSION.SDK_INT < 35) return; // No need for lower
        mLockscreenView = LockscreenView.getInstance(mContext);
        View clockView = getClockView();
        clockView.setTag(OC_LOCKSCREEN_CLOCK_TAG);
        modifyClockView(clockView);
        mLockscreenView.setClockView(clockView);
        mLockscreenView.updateClockMargins(keyguardLockHeight + dp2px(mContext, topMargin));
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
            initSoundManager();
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

        mLockscreenView.setClockScale(clockScale);
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

        if (mVolumeLevelArcProgress != null) {
            mVolumeLevelArcProgress.setColors(customColor ? accent1 : systemAccent, text1);
        }
        if (mRamUsageArcProgress != null) {
            mRamUsageArcProgress.setColors(customColor ? accent1 : systemAccent, text1);
        }

        mBatteryLevelView = null;
        mBatteryProgress = null;
        mBatteryStatusView = null;
        mVolumeLevelView = null;
        mVolumeProgress = null;

        try {
            ((ViewGroup) mVolumeLevelArcProgress.getParent()).removeView(mVolumeLevelArcProgress);
        } catch (Throwable ignored) {}
        try {
            ((ViewGroup) mRamUsageArcProgress.getParent()).removeView(mRamUsageArcProgress);
        } catch (Throwable ignored) {}

        switch (lockscreenClockStyle) {
            case 2 -> {
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");
                tickIndicator.setTextColor(Color.TRANSPARENT);
                TextView hourView = (TextView) findViewWithTag(clockView, "hours");
                hourView.setVisibility(View.VISIBLE);
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
                LinearLayout volumeProgress = (LinearLayout) findViewWithTag(clockView, "volume_progress");
                if (mVolumeLevelArcProgress == null) {
                    mVolumeLevelArcProgress = new ProgressImageView(mContext);
                    mVolumeLevelArcProgress.setColors(customColor ? accent1 : getPrimaryColor(mContext), text1);
                    mVolumeLevelArcProgress.setProgressType(ProgressImageView.ProgressType.VOLUME);
                }
                volumeProgress.setBackground(null);
                volumeProgress.addView(mVolumeLevelArcProgress);
                LinearLayout ramProgress = (LinearLayout) findViewWithTag(clockView, "ram_usage_info");
                if (mRamUsageArcProgress == null) {
                    mRamUsageArcProgress = new ProgressImageView(mContext);
                    mRamUsageArcProgress.setColors(customColor ? accent1 : getPrimaryColor(mContext), text1);
                    mRamUsageArcProgress.setProgressType(ProgressImageView.ProgressType.MEMORY);
                }
                ramProgress.setBackground(null);
                ramProgress.addView(mRamUsageArcProgress);

                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(customColor ? accent1 : getPrimaryColor(mContext)));
            }
            case 27 -> {
                TextView hourView = (TextView) findViewWithTag(clockView, "textHour");
                TextView minuteView = (TextView) findViewWithTag(clockView, "textMinute");
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");

                TimeUtils.setCurrentTimeTextClock(mContext, tickIndicator, hourView, minuteView);
            }
        }
    }

    private void moveClockView(int uiMode) {
        if (!customLockscreenClock) return;

        if (mLockscreenView == null) return;
        try {
            mLockscreenView.onUiStateChanged(uiMode);
        } catch (Throwable t) {
            XposedBridge.log("Error moving clock view: " + Log.getStackTraceString(t));
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
        if (mBatteryLevelView != null) {
            mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage));
        }
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
