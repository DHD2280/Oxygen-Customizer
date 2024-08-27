package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.START;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_BACKGROUND;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CENTERED;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_HUMIDITY;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_IMAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_CONDITION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_TEXT_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_WIND;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_STOCK_CLOCK_RED_ONE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_EXTRAS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SCALE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_INACTIVE;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
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

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Calendar;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.views.CurrentWeatherView;
import it.dhd.oxygencustomizer.xposed.views.LockscreenWidgetsView;

public class LockscreenClock extends XposedMods {

    private static final String TAG = "Oxygen Customizer - " + LockscreenClock.class.getSimpleName() + ": ";
    private final static String listenPackage = Constants.Packages.SYSTEM_UI;
    public static final String OC_LOCKSCREEN_CLOCK_TAG = "oxygencustomizer_lockscreen_clock";

    private final String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_clock_font.ttf";

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
    private boolean useCustomName;
    private String customName;
    private boolean useCustomUserImage;
    private boolean useCustomImage;

    // Stock Clock
    private int mStockClockRed, mStockClockRedColor;
    private static Object mStockClock;
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
    private ImageView mVolumeLevelArcProgress;
    private ImageView mRamUsageArcProgress;
    private ImageView mBatteryArcProgress;
    private static long lastUpdated = System.currentTimeMillis();
    private static final long thresholdTime = 500; // milliseconds
    private int accent1, accent2, accent3, text1, text2;
    private boolean customColor;
    Class<?> LottieAn = null;

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
    private final BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initSoundManager();
        }
    };

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
        useCustomName = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_USER, false);
        customName = Xprefs.getString(LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE, getUserName());
        useCustomUserImage = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE, false);
        useCustomImage = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_IMAGE, false);

        if (Key.length > 0) {
            for (String LCPrefs : LOCKSCREEN_CLOCK_PREFS) {
                if (Key[0].equals(LCPrefs)) {
                    new Handler(Looper.getMainLooper()).post(this::updateClockView);
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

        LottieAn = findClass("com.airbnb.lottie.LottieAnimationView", lpparam.classLoader);

        initResources(mContext);

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

        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        try {
            context.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
        }
        try {
            context.registerReceiver(mVolumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        } catch (Exception ignored) {
        }
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

        if (clockScale != 1.0f) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, clockScale);
        }

        switch (lockscreenClockStyle) {
            case 5 -> {
                mBatteryStatusView = clockView.findViewById(R.id.battery_status);
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage);
                mVolumeLevelView = clockView.findViewById(R.id.volume_level);
                mBatteryProgress = clockView.findViewById(R.id.battery_progressbar);
                mVolumeProgress = clockView.findViewById(R.id.volume_progressbar);
            }
            case 7 -> {
                TextView usernameView = clockView.findViewById(R.id.summary);
                usernameView.setText(useCustomName ? customName : getUserName());
                ImageView imageView = clockView.findViewById(R.id.user_profile_image);
                imageView.setImageDrawable(useCustomUserImage ? getCustomUserImage() : getUserImage());
            }
            case 19 -> {
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage);
                mBatteryProgress = clockView.findViewById(R.id.battery_progressbar);
                mVolumeLevelArcProgress = clockView.findViewById(R.id.volume_progress);
                mRamUsageArcProgress = clockView.findViewById(R.id.ram_usage_info);

                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(customColor ? accent1 : getPrimaryColor(mContext)));

                ((TextView) clockView.findViewById(R.id.device_name)).setText(Build.MODEL);
            }
            case 25 -> {
                ImageView imageView = clockView.findViewById(R.id.custom_image);
                if (useCustomImage) {
                    imageView.setImageDrawable(getCustomImage());
                }
            }
            case 27 -> {
                TextView hourView = (TextView) findViewWithTag(clockView, "textHour");
                TextView minuteView = (TextView) findViewWithTag(clockView, "textMinute");
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");

                TimeUtils.setCurrentTimeTextClock(mContext, tickIndicator, hourView, minuteView);
            }
            case 99 -> {
                // TextViews
                mBatteryLevelView = clockView.findViewById(R.id.battery_percentage);

                mVolumeLevelArcProgress = clockView.findViewById(R.id.volume_progress);
                mRamUsageArcProgress = clockView.findViewById(R.id.ram_usage_info);
                mBatteryArcProgress = clockView.findViewById(R.id.battery_progress);
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
            mVolumeProgress.setProgress(volPercent);
        }
        if (mVolumeLevelView != null) {
            mVolumeLevelView.setText(appContext.getResources().getString(R.string.percentage_text, volPercent));
        }

        if (mVolumeLevelArcProgress != null) {
            Bitmap widgetBitmap = ArcProgressWidget.generateBitmap(
                    mContext,
                    volPercent,
                    appContext.getResources().getString(R.string.percentage_text, volPercent),
                    32,
                    ContextCompat.getDrawable(appContext, R.drawable.ic_volume_up),
                    36,
                    customColor ? accent1 : getPrimaryColor(mContext)
            );
            mVolumeLevelArcProgress.setImageBitmap(widgetBitmap);
        }
    }

    private void initRamUsage() {
        if (mActivityManager == null) return;

        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        mActivityManager.getMemoryInfo(memoryInfo);
        long usedMemory = memoryInfo.totalMem - memoryInfo.availMem;
        if (memoryInfo.totalMem == 0) return;
        int usedMemoryPercentage = (int) ((usedMemory * 100) / memoryInfo.totalMem);

        if (mRamUsageArcProgress != null) {
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
            log(TAG + throwable);
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }
    }

    private Drawable getCustomUserImage() {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_user_image.png"));

            Drawable drawable = ImageDecoder.decodeDrawable(source);

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                ((AnimatedImageDrawable) drawable).start();
            }

            return drawable;
        } catch (Throwable ignored) {
            return ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.default_avatar, appContext.getTheme());
        }
    }

    private Drawable getCustomImage() {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_custom_image.png"));

            Drawable drawable = ImageDecoder.decodeDrawable(source);

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                ((AnimatedImageDrawable) drawable).start();
            }

            return drawable;
        } catch (Throwable ignored) {
            return ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.relax, appContext.getTheme());
        }
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
