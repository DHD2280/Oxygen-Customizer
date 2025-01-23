package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.AOD_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_DEVICE_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_DATE_FORMAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.findViewWithTag;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.loadLottieAnimationView;

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
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.lang.reflect.Method;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class AodClock extends XposedMods {

    public static final String OC_AOD_CLOCK_TAG = "oxygencustomizer_aod_clock";
    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    Class<?> LottieAn = null;
    private ViewGroup mRootLayout = null;
    private Context appContext;
    private boolean mAodClockEnabled = false;
    private int accent1, accent2, accent3, text1, text2;
    private boolean mCustomColor, mCustomFont, mCustomImage, mCustomUserImage;
    private String mCustomUserName;
    private String mCustomDeviceName;
    private float mClockScale;
    private String mCustomDateFormat;
    private int mLineHeight;
    private int mAodClockStyle = 0;
    private int mBatteryStatus = 1;
    private int mBatteryPercentage = 1;
    private UserManager mUserManager;
    private AudioManager mAudioManager;
    private ActivityManager mActivityManager;
    private TextView mBatteryStatusView;
    private TextView mBatteryLevelView;
    private TextView mVolumeLevelView;
    private ProgressBar mBatteryProgress;
    private ProgressBar mVolumeProgress;
    private ImageView mVolumeLevelArcProgress;
    private final BroadcastReceiver mVolumeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            initSoundManager();
        }
    };
    private ImageView mRamUsageArcProgress;
    private ImageView mBatteryArcProgress;
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
    public AodClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mAodClockEnabled = Xprefs.getBoolean(AOD_CLOCK_SWITCH, false);
        mAodClockStyle = Xprefs.getInt(AOD_CLOCK_STYLE, 0);
        accent1 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_ACCENT1, getPrimaryColor(mContext));
        accent2 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_ACCENT2, getPrimaryColor(mContext));
        accent3 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_ACCENT3, getPrimaryColor(mContext));
        text1 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_TEXT1, Color.WHITE);
        text2 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_TEXT2, Color.WHITE);
        mCustomFont = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_FONT, false);
        mCustomColor = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_COLOR_SWITCH, false);
        mCustomImage = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_IMAGE, false);
        mCustomDeviceName = Xprefs.getString(AOD_CLOCK_CUSTOM_DEVICE_VALUE, "");
        mCustomUserName = Xprefs.getString(AOD_CLOCK_CUSTOM_USER_VALUE, "");
        mCustomUserImage = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_USER_IMAGE, false);
        mClockScale = Xprefs.getSliderFloat(AOD_CLOCK_TEXT_SCALING, 1.0f);
        mLineHeight = Xprefs.getSliderInt(AOD_CLOCK_LINE_HEIGHT, 0);
        mCustomDateFormat = Xprefs.getString(AOD_CLOCK_DATE_FORMAT, "");

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        initResources(mContext);

        LottieAn = findClass("com.airbnb.lottie.LottieAnimationView", lpparam.classLoader);

        ReflectedClass AodClockLayout = ReflectedClass.of(
                "com.oplus.systemui.aod.aodclock.off.AodClockLayout", /* OOS 15, 14 */
                "com.oplusos.systemui.aod.aodclock.off.AodClockLayout" /* OOS 13 */
        );


        AodClockLayout
                .after("initForAodApk")
                        .run(param -> {
                            if (!mAodClockEnabled) return;
                            if (!canShowAodClock()) return;
                            FrameLayout mAodViewFromApk = (FrameLayout) getObjectField(param.thisObject, "mAodViewFromApk");
                            for (int i = 0; i < mAodViewFromApk.getChildCount(); i++) {
                                log(" mAodViewFromApk " + mAodViewFromApk.getChildAt(i).getClass().getCanonicalName());
                                if (mAodViewFromApk.getChildAt(i) instanceof ViewGroup v) {
                                    for (int j = 0; j < v.getChildCount(); j++) {
                                        mRootLayout = v;
                                        if (v.getChildAt(j) instanceof ViewGroup v2) {
                                            for (int k = 0; k < v2.getChildCount(); k++) {
                                                v.getChildAt(k).setVisibility(View.GONE);
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                            try {
                                updateClockView();
                            } catch (Throwable t) {
                                log(t);
                            }
                        });


        AodClockLayout
                .after("performTimeUpdate")
                        .run(param -> {
                            if (!mAodClockEnabled) return;
                            if (!canShowAodClock()) return;
                            try {
                                updateClockView();
                            } catch (Throwable t) {
                                log(t);
                            }
                        });

    }

    private boolean canShowAodClock() {
        if (Build.VERSION.SDK_INT < 35) return true;

        int fullScreenAod = Settings.Secure.getInt(mContext.getContentResolver(), "full_screen_aod", 0);
        return fullScreenAod == 0;
    }

    private void updateClockView() {

        if (mRootLayout == null) return;

        log(" updateClockView " + mRootLayout.getChildCount());

        View clockView = getClockView();

        boolean isClockAdded = mRootLayout.findViewWithTag(OC_AOD_CLOCK_TAG) != null;

        // Remove existing clock view
        if (isClockAdded) {
            mRootLayout.removeView(mRootLayout.findViewWithTag(OC_AOD_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(OC_AOD_CLOCK_TAG);

            if (clockView.getParent() != null) {
                ((ViewGroup) clockView.getParent()).removeView(clockView);
            }

            mRootLayout.addView(clockView, 0);
            modifyClockView(clockView);
            initSoundManager();
            initBatteryStatus();

        }
    }

    private void modifyClockView(View clockView) {
        String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/aod_clock_font.ttf";
        int systemAccent = getPrimaryColor(mContext);

        Typeface typeface = null;
        if (mCustomFont && (new File(customFont).exists())) {
            typeface = Typeface.createFromFile(new File(customFont));
        }

        ViewHelper.setMargins(clockView, mContext, 0, 0, 0, 0);
        log(" customColor: " + mCustomColor);

        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", mCustomColor ? accent1 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", mCustomColor ? accent2 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", mCustomColor ? accent3 : systemAccent);
        if (mCustomColor) {
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", text1);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", text2);
        }

        if (typeface != null) {
            ViewHelper.applyFontRecursively((ViewGroup) clockView, typeface);
        }

        ViewHelper.applyTextMarginRecursively((ViewGroup) clockView, mLineHeight);

        if (mClockScale != 1.0f) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, mClockScale);
        }
        clockView.setVisibility(View.VISIBLE);

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
            deviceName.setText(mCustomDeviceName.isEmpty() ? Build.MODEL : mCustomDeviceName);
        }

        TextView username = (TextView) findViewWithTag(clockView, "username");
        if (username != null) {
            username.setText(mCustomUserName.isEmpty() ? getUserName() : mCustomUserName);
        }

        ImageView profilePicture = (ImageView) findViewWithTag(clockView, "profile_picture");
        if (mCustomUserImage && profilePicture != null) {
            profilePicture.post(() -> profilePicture.setImageDrawable(getCustomUserImage()));
        }

        ImageView customImage = (ImageView) findViewWithTag(clockView, "custom_image");
        if (mCustomImage && customImage != null) {
            customImage.post(() -> customImage.setImageDrawable(getCustomImage()));
        }

        mBatteryArcProgress = null;
        mBatteryStatusView = null;
        mBatteryLevelView = null;
        mBatteryProgress = null;
        mVolumeLevelView = null;
        mVolumeProgress = null;
        mVolumeLevelArcProgress = null;
        mRamUsageArcProgress = null;

        switch (mAodClockStyle) {
            case 2 -> {
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");
                tickIndicator.setVisibility(View.GONE);
                TextView hourView = (TextView) findViewWithTag(clockView, "hours");
                TimeUtils.setCurrentTimeTextClockRed(tickIndicator, hourView, mCustomColor ? accent1 : getPrimaryColor(mContext));
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
                        imageView.setImageDrawable(mCustomUserImage ? getCustomUserImage() : getUserImage()));
            }
            case 19 -> {
                mBatteryLevelView = (TextView) findViewWithTag(clockView, "battery_percentage");
                mBatteryProgress = (ProgressBar) findViewWithTag(clockView, "battery_progressbar");
                mVolumeLevelArcProgress = (ImageView) findViewWithTag(clockView, "volume_progress");
                mRamUsageArcProgress = (ImageView) findViewWithTag(clockView, "ram_usage_info");

                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(mCustomColor ? accent1 : getPrimaryColor(mContext)));
            }
            case 25 -> {
                ImageView imageView = (ImageView) findViewWithTag(clockView, "custom_image");
                if (mCustomImage) {
                    imageView.setImageDrawable(getCustomImage());
                }
            }
            case 27 -> {
                TextView hourView = (TextView) findViewWithTag(clockView, "textHour");
                TextView minuteView = (TextView) findViewWithTag(clockView, "textMinute");
                TextClock tickIndicator = (TextClock) findViewWithTag(clockView, "tickIndicator");

                TimeUtils.setCurrentTimeTextClock(mContext, tickIndicator, hourView, minuteView);
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private View getClockView() {
        if (appContext == null) return null;
        LayoutInflater inflater = LayoutInflater.from(appContext);

        int resId = appContext
                .getResources()
                .getIdentifier(
                        AOD_CLOCK_LAYOUT + mAodClockStyle,
                        "layout",
                        BuildConfig.APPLICATION_ID
                );
        if (resId == 0) {
            resId = appContext
                    .getResources()
                    .getIdentifier(
                            LOCKSCREEN_CLOCK_LAYOUT + mAodClockStyle,
                            "layout",
                            BuildConfig.APPLICATION_ID
                    );
        }

        View v = inflater.inflate(resId, null);

        loadLottieAnimationView(
                appContext,
                LottieAn,
                v,
                mAodClockStyle
        );

        return v;

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
            if (mAodClockStyle == 19) {
                mBatteryProgress.setProgressTintList(ColorStateList.valueOf(mCustomColor ? accent1 : getPrimaryColor(mContext)));
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
                    mCustomColor ? accent1 : getPrimaryColor(mContext)
            );
            mBatteryArcProgress.setImageBitmap(widgetBitmap);
        }
        if (mBatteryLevelView != null) {
            mBatteryLevelView.setText(appContext.getResources().getString(R.string.percentage_text, mBatteryPercentage));
        }

        initRamUsage();
    }

    private void initSoundManager() {
        if (!mAodClockEnabled || (Build.VERSION.SDK_INT >= 35 && !canShowAodClock())) return;
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
                    mCustomColor ? accent1 : getPrimaryColor(mContext)
            );
            mVolumeLevelArcProgress.setImageBitmap(widgetBitmap);
        }
    }

    private void initRamUsage() {
        if (!mAodClockEnabled || (Build.VERSION.SDK_INT >= 35 && !canShowAodClock())) return;
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
                    mCustomColor ? accent1 : getPrimaryColor(mContext)
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
        return getImageFromFile("aod_user_image.png", R.drawable.default_avatar);
    }

    private Drawable getCustomImage() {
        return getImageFromFile("aod_custom_image.png", R.drawable.relax);
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
            context.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        } catch (Exception ignored) {
        }
        try {
            context.registerReceiver(mVolumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));
        } catch (Exception ignored) {
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
