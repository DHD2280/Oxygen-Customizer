package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_BOOT_COMPLETED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_USER_UNLOCKED;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN_AOD;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_DESIGNER_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_KEEP_LARGE_CLOCK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_STOCK_CLOCK_RED_ONE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.findViewWithTag;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.loadLottieAnimationView;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setIntField;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.CircleFramedDrawable;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;
import it.dhd.oxygencustomizer.xposed.views.LockscreenView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.ProgressImageView;

//import it.dhd.oxygencustomizer.xmorph.designer.DesignerClock;
//import it.dhd.oxygencustomizer.xmorph.designer.LockscreenHosts;
//import it.dhd.oxygencustomizer.xposed.utils.KeyguardWallpaper;
//import it.dhd.oxygencustomizer.xposed.utils.GlassTextEffect;

public class LockscreenClock extends XposedMods {

    private static final String OC_LOCKSCREEN_CLOCK_LAYOUT = "lockscreen_clock_layout";
    private static final String OC_LOCKSCREEN_CLOCK_TAG = "oxygencustomizer_lockscreen_clock";
    private final static String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final long thresholdTime = 500; // milliseconds
    private static Object mStockClock;
    private final Map<View, Boolean> mStockClockViews = new WeakHashMap<>();
    private WeakReference<View> mStockClockRoot;
    private boolean mStockMirrorHooked = false;
    private static final String OC_HIDDEN_STOCK_CLOCK = "OC_HIDDEN_STOCK_CLOCK";
    private static final int OVERFLOW_PARENTS = 3;
    private final Map<View, ViewTreeObserver> mGuardedObservers = new WeakHashMap<>();
    private static long lastUpdated = System.currentTimeMillis();
    private final String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_clock_font.ttf";
    Class<?> LottieAn = null;
    private ViewGroup mClockViewContainer = null;
    private ViewGroup mStatusViewContainer = null;
    private RelativeLayout mClockView = null;
    private View mMediaHostContainer = null;
    // Lockscreen Clock Prefs
    private boolean customLockscreenClock = false;
    private boolean designerMode = false;
    private boolean keepLargeClock = false;
    private Object mStyleClockController = null;
    private int mNotificationHeight = -1, mKeyguardBottomPadding = 0;
    private int lockscreenClockStyle = 1;
    private View mDesignerClockView = null;
    private boolean mAodBurnInActive = false;
    private int mAodBurnInBase = Integer.MIN_VALUE;
    private int topMargin, bottomMargin, bottomAodMargin;
    private float mLockHeight = 0;
    private float clockScale;
    private int lineHeight;
    private boolean customFontEnabled = false;
    private String customName, customDeviceName;
    private boolean useCustomUserImage;
    private boolean useCustomImage;
    private String mCustomDateFormat = "";
    // Stock Clock
    private int mStockClockRed, mStockClockRedColor;
    private UserManager mUserManager;
    private AudioManager mAudioManager;
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
    public final static int CLOCK_UI_STATE_SHADE = 1;
    public final static int CLOCK_UI_STATE_LS = 2;
    public final static int CLOCK_UI_STATE_AOD = 3;
    private Object mKeyguardStyleClockControllerImpl = null;
    private ViewGroup mStyleClockContainer = null;
    private final ExecutorService mImageExecutor = Executors.newSingleThreadExecutor();
    private int mStockClockBottom = 0;
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

    private boolean mBroadcastRegistered = false;

    final BroadcastReceiver mBootReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                updateClockView();
            }
        }
    };

    public LockscreenClock(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        if (Xprefs == null) return;

        designerMode = Build.VERSION.SDK_INT >= 37 && Xprefs.getBoolean(LOCKSCREEN_DESIGNER_MODE, false);
        boolean keptLarge = keepLargeClock;
        keepLargeClock = Build.VERSION.SDK_INT >= 37 && Xprefs.getBoolean(LOCKSCREEN_KEEP_LARGE_CLOCK, false);
        if (keptLarge != keepLargeClock && mStyleClockController != null && mNotificationHeight >= 0) {
            Object controller = mStyleClockController;
            int notificationHeight = mNotificationHeight;
            int bottomPadding = mKeyguardBottomPadding;
            new Handler(Looper.getMainLooper()).post(() -> {
                try {
                    callMethod(controller, "onClockExpectedHeightChange", notificationHeight, bottomPadding);
                } catch (Throwable t) {
                    log("Keep large clock refresh failed: " + t.getMessage());
                }
            });
        }
        customLockscreenClock = designerMode || Xprefs.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false);
        for (View view : mStockClockViews.keySet()) view.post(() -> enforceStockClockView(view));
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
        bottomAodMargin = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_BOTTOM_MARGIN_AOD, 40);
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
                if (Key[0].equals(LOCKSCREEN_CLOCK_SWITCH) || Key[0].equals(LOCKSCREEN_DESIGNER_MODE)) {
                    reloadPlugin();
                }
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
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        if (!mBroadcastRegistered) {
            mBroadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTIONS_BOOT_COMPLETED);
            intentFilter.addAction(ACTIONS_USER_UNLOCKED);
            mContext.registerReceiver(mBootReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }

        LottieAn = ReflectedClass.of("com.airbnb.lottie.LottieAnimationView").getClazz();

        initResources(mContext);

        createCustomClockView();

        if (Build.VERSION.SDK_INT >= 36) {
            ReflectedClass ThemePlugin = ReflectedClass.ofIfPossible("com.oplus.keyguard.plugin.ThemePlugin");
            ThemePlugin
                    .after("render")
                    .run(param -> {
                        View v = (View) callMethod(param.thisObject, "getClockScrimLayer");
                        v.setVisibility(View.GONE);
                    });
        }
        if (Build.VERSION.SDK_INT >= 35) {

            ReflectedClass KeyguardStyleClockControllerImpl = ReflectedClass.of("com.oplus.systemui.keyguard.clockstyle.KeyguardStyleClockControllerImpl");
            KeyguardStyleClockControllerImpl
                    .before("getKeyguardStyleClockHeight")
                    .run(param -> {
                        Object lockIconViewController = getObjectField(param.thisObject, "lockIconViewController");
                        Object obj = callMethod(lockIconViewController, "get");
                        mLockHeight = (float) callMethod(obj, "getBottom");
                        if (mLockscreenView == null) return;
                        int clockHeight = mLockscreenView.getFullHeight(customLockscreenClock);
                        int finalResult = (int) (mLockHeight + clockHeight);
                        if (customLockscreenClock) {
                            finalResult += dp2px(mContext, topMargin);
                            finalResult += dp2px(mContext, bottomMargin);
                            applyLockscreenViewTop(0);
                        } else {
                            int mStockClockHeight = Settings.System.getInt(mContext.getContentResolver(), "oplus_keyguardstyle_aod_clock_height", 0);
                            applyLockscreenViewTop(mStockClockHeight);
                        }
                        if (!customLockscreenClock) {
                            return;
                        }
                        param.setResult(finalResult);
                    });
            if (Build.VERSION.SDK_INT >= 37) {
                KeyguardStyleClockControllerImpl
                        .before("onClockExpectedHeightChange")
                        .run(param -> {
                            mStyleClockController = param.thisObject;
                            mNotificationHeight = (int) param.args[0];
                            mKeyguardBottomPadding = (int) param.args[1];
                            if (keepLargeClock) param.args[0] = 0;
                        });
            }
            KeyguardStyleClockControllerImpl
                    .after("getKeyguardStyleClockHeight")
                    .run(param -> {
                        if (customLockscreenClock || mStyleClockContainer == null || mLockscreenView == null)
                            return;
                        int stockClockBottom = (int) mLockHeight + (int) param.getResult();
                        if (stockClockBottom == mStockClockBottom) return;
                        mStockClockBottom = stockClockBottom;
                        applyLockscreenViewTop(0);
                    });

            ReflectedClass NotificationPanelViewController = ReflectedClass.ofIfPossible("com.android.systemui.shade.NotificationPanelViewController");
            NotificationPanelViewController
                    .before("getKeyguardNotificationStaticPadding")
                    .run(param -> {
                        int clockHeight = mLockscreenView.getFullHeight(customLockscreenClock);
                        int finalResult = (int) (mLockHeight + clockHeight);
                        if (customLockscreenClock) {
                            finalResult += dp2px(mContext, topMargin);
                            finalResult += dp2px(mContext, bottomMargin);
                            applyLockscreenViewTop(0);
                        } else {
                            int mStockClockHeight = Settings.System.getInt(mContext.getContentResolver(), "oplus_keyguardstyle_aod_clock_height", 0);
                            applyLockscreenViewTop(mStockClockHeight);
                            finalResult += mStyleClockContainer != null ? mStockClockBottom - (int) mLockHeight : mStockClockHeight;
                        }
                        if (mLockscreenView.isClockEnabled() ||
                                mLockscreenView.isWeatherEnabled() ||
                                mLockscreenView.isWidgetsEnabled()) param.setResult(finalResult);
                    });

            ReflectedClass OplusKeyguardStyleBaseClock = ReflectedClass.ofIfPossible("com.oplus.keyguard.OplusKeyguardStyleBaseClock");
            ReflectedClass OplusKeyguardStyleWrapper = ReflectedClass.ofIfPossible("com.oplus.keyguard.comm.OplusKeyguardStyleWrapper");
            ReflectedClass ClockPlugin = ReflectedClass.ofIfPossible("com.oplus.keyguard.plugin.ClockPlugin");

            ReflectedClass.ReflectionConsumer lockscreenClockPostHook = param -> {
                int viewType = (int) param.args[0];
                if (viewType != 1) return;
                attachLockscreenView((View) param.getResult());
            };

            Class<?> customStyleClockClass = ReflectedClass.ofIfPossible("com.oplus.systemui.keyguard.view.CustomOplusKeyguardStyleClock").getClazz();
            if (customStyleClockClass != null) {
                if (Build.VERSION.SDK_INT >= 37) {
                    ReflectedClass OplusLauncherFlag = ReflectedClass.ofIfPossible("com.oplus.systemui.keyguard.launcher.flag.OplusLauncherFlag");
                    OplusLauncherFlag
                            .before("isOneShotEnabled")
                            .run(param -> {
                                if (customLockscreenClock) param.setResult(false);
                            });

                    ReflectedClass AbstractSubPlugin = ReflectedClass.ofIfPossible("com.oplus.keyguard.plugin.AbstractSubPlugin");
                    AbstractSubPlugin
                            .after("getView")
                            .run(param -> {
                                if (!param.thisObject.getClass().getSimpleName().equals("ClockPlugin"))
                                    return;
                                if (!(param.getResult() instanceof View view)) return;
                                if (Integer.valueOf(1).equals(param.args[0])) {
                                    mStockClockRoot = new WeakReference<>(view);
                                    hookStockClockMirror(view.getClass());
                                    return;
                                }
                                if (Integer.valueOf(0).equals(param.args[0]))
                                    trackStockClockView(view);
                            });
                }

                ReflectedClass KeyguardPlugin = ReflectedClass.ofIfPossible("com.oplus.keyguard.plugin.KeyguardPlugin");
                KeyguardPlugin
                        .after("setUp")
                        .run(param -> {
                            if (param.args.length != 2) return;
                            Object container = getObjectField(param.thisObject, "pluginContainer");
                            if (!customStyleClockClass.isInstance(container)) return;
                            mStyleClockContainer = (ViewGroup) container;
                            attachLockscreenView(mStyleClockContainer);
                            applyLockscreenViewTop(0);
                        });

                ReflectedClass.of(customStyleClockClass)
                        .after("setAlpha")
                        .run(param -> {
                            if (mStyleClockContainer == param.thisObject) return;
                            ViewGroup container = (ViewGroup) param.thisObject;
                            if (container.getChildCount() == 0) return;
                            mStyleClockContainer = container;
                            attachLockscreenView(mStyleClockContainer);
                            applyLockscreenViewTop(0);
                        });

                if (Build.VERSION.SDK_INT >= 37) {
                    ReflectedClass.ofIfPossible("com.oplus.systemui.aod.controller.BaseAodClockLayoutController")
                            .after("updateNextPosition")
                            .run(param -> {
                                if (!mAodBurnInActive || !customLockscreenClock || mLockscreenView == null)
                                    return;
                                int position = (int) param.args[0];
                                if (mAodBurnInBase == Integer.MIN_VALUE) mAodBurnInBase = position;
                                mLockscreenView.setBurnInOffset(position - mAodBurnInBase);
                            });
                }

                ReflectedClass KeyguardStyleServiceCallback = ReflectedClass.ofIfPossible("com.oplus.systemui.keyguard.clockstyle.KeyguardStyleClockControllerImpl$keyguardStyleServiceCallback$1");
                KeyguardStyleServiceCallback
                        .after("onUiStateChanged")
                        .run(param -> {
                            if (mLockscreenView == null) return;
                            if (!(param.args[0] instanceof Enum<?> uiState)) return;
                            mLockscreenView.onUiStateChanged(uiState.ordinal());
                            updateAodBurnIn(uiState.ordinal());
                            DesignerClock.setAod(mDesignerClockView, uiState.ordinal() == 3 || uiState.ordinal() == 4);
                            GlassTextEffect.setAod(uiState.ordinal() == 3 || uiState.ordinal() == 4);
                        });

                ReflectedClass KeyguardClockSizeChangeWrapper = ReflectedClass.ofIfPossible("com.oplus.systemui.keyguard.clockstyle.KeyguardClockSizeChangeWrapper");
                KeyguardClockSizeChangeWrapper
                        .before("setClockSize")
                        .run(param -> {
                            if (!keepLargeClock || !(param.args[0] instanceof Enum<?> clockSize))
                                return;
                            if (!"SMALL".equals(clockSize.name())) return;
                            try {
                                param.args[0] = Enum.valueOf(clockSize.getDeclaringClass().asSubclass(Enum.class), "LARGE");
                            } catch (Throwable t) {
                                log("Keep large clock failed: " + t.getMessage());
                            }
                        });
                KeyguardClockSizeChangeWrapper
                        .after("setClockSize")
                        .run(param -> {
                            if (!(param.args[0] instanceof Enum<?> clockSize)) return;
                            if (mDesignerClockView == null) return;
                            DesignerClock.setLarge(mDesignerClockView, !"SMALL".equals(clockSize.name()));
                        });

                KeyguardPlugin
                        .before("setWallpaperBitmap")
                        .run(param -> {
                            if (param.args[0] instanceof Bitmap bitmap)
                                KeyguardWallpaper.set(bitmap);
                        });
            }
            OplusKeyguardStyleBaseClock
                    .after("getView")
                    .run(lockscreenClockPostHook);

            ReflectedClass.ReflectionConsumer timeHooker = param -> {
                long time = (long) param.args[0];
                if (customLockscreenClock) {
                    mLockscreenView.updateClock(time);
                }
            };
            OplusKeyguardStyleBaseClock
                    .after("setTime")
                    .run(timeHooker);
            ClockPlugin
                    .after("setTime")
                    .run(timeHooker);

            if (OplusKeyguardStyleWrapper.getClazz() != null) { // RUI 6.0
                OplusKeyguardStyleWrapper
                        .after("getView")
                        .run(lockscreenClockPostHook);
            }

            OplusKeyguardStyleBaseClock
                    .before("loadPlugin")
                    .run(param -> {
                        boolean isPluginLoaded = (boolean) callMethod(param.thisObject, "isPluginLoaded");
                        if (mLockscreenView != null) {
                            try {
                                ((ViewGroup) mLockscreenView.getParent()).removeView(mLockscreenView);
                            } catch (Throwable ignored) {
                            }
                        }
                        if (!isPluginLoaded) {
                            try {
                                createCustomClockView();
                            } catch (Throwable t) {
                                log(t);
                            }
                        }
                    });

            ReflectedClass AodClockLayout = ReflectedClass.of("com.oplus.systemui.aod.aodclock.off.AodClockLayout");
            AodClockLayout
                    .after("initAodViewInfo")
                    .run(param -> {
                        if (!customLockscreenClock) return;
                        try { // first OOS15 versions
                            int marginTop = dp2px(mContext, topMargin);
                            int marginBottom = dp2px(mContext, bottomAodMargin);
                            setIntField(param.thisObject, "mAodWorkShopClockHeight", (mLockscreenView.getClockHeight() + marginTop + marginBottom));
                        } catch (Throwable ignored) {
                        }
                    });
            ReflectedClass AodData = ReflectedClass.of("com.oplus.systemui.aod.aodclock.constant.AodData");
            AodData
                    .before("setWorkShowAODViewInfo")
                    .run(param -> {
                        if (!customLockscreenClock) return;
                        try {
                            int marginTop = dp2px(mContext, topMargin);
                            int marginBottom = dp2px(mContext, bottomAodMargin);
                            param.args[1] = (mLockscreenView.getClockHeight() + marginTop + marginBottom);
                        } catch (Throwable t) {
                            log(t);
                        }
                    });
            AodData
                    .before("getWorkShopAodClockHeight")
                    .run(param -> {
                        if (!customLockscreenClock) return;
                        if (Build.VERSION.SDK_INT >= 37) {
                            if (mLockscreenView == null || mStyleClockContainer == null) return;
                            int clockHeight = mLockscreenView.getClockHeight();
                            if (clockHeight <= 0) return;
                            int marginTop = Settings.System.getInt(mContext.getContentResolver(), "oplus_keyguardstyle_aod_clock_margin_top", 0);
                            param.setResult(Math.max(0, (int) mLockHeight + clockHeight - marginTop));
                            return;
                        }
                        try {
                            int marginTop = dp2px(mContext, topMargin);
                            int marginBottom = dp2px(mContext, bottomAodMargin);
                            param.setResult((mLockscreenView.getClockHeight() + marginTop + marginBottom));
                        } catch (Throwable t) {
                            log(t);
                        }
                    });

            ReflectedClass OplusKeyguardStyleClock = ReflectedClass.ofIfPossible("com.oplus.keyguard.OplusKeyguardStyleClock");
            OplusKeyguardStyleClock
                    .after("onUiStateChanged")
                    .run(param -> {
                        if (mLockscreenView == null) return;
                        mLockscreenView.onUiStateChanged((int) param.args[0]);
                    });

        } else {
            Class<?> KeyguardStatusViewClass = findClass("com.android.keyguard.KeyguardStatusView", PRParam.getClassLoader());
            ReflectedClass.of(KeyguardStatusViewClass)
                    .after("onFinishInflate")
                    .run(param -> {
                    ViewGroup statusViewContainer = (ViewGroup) getObjectField(param.thisObject, "mStatusViewContainer");
                    mStatusViewContainer = (ViewGroup) param.thisObject;
                    mClockViewContainer = statusViewContainer;

                    // Hide stock clock
                    GridLayout KeyguardStatusView = (GridLayout) param.thisObject;

                    mClockView = KeyguardStatusView.findViewById(mContext.getResources().getIdentifier("keyguard_clock_container", "id", mContext.getPackageName()));

                    mMediaHostContainer = (View) getObjectField(param.thisObject, "mMediaHostContainer");

                    registerClockUpdater();
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
            RedTextClock = findClass("com.oplus.systemui.shared.clocks.RedTextClock", PRParam.getClassLoader());
        } catch (Throwable t) {
            RedTextClock = findClass("com.oplusos.systemui.keyguard.clock.RedTextClock", PRParam.getClassLoader()); // OOS 13
        }
        ReflectedClass.of(RedTextClock)
                .after(ReflectionHelpers.findMethodExact(RedTextClock, "onTimeChanged"))
                .run(param -> {
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
        mLockscreenView.setTag(OC_LOCKSCREEN_CLOCK_LAYOUT);
        View clockView = getClockView();
        clockView.setTag(OC_LOCKSCREEN_CLOCK_TAG);
        modifyClockView(clockView);
        mLockscreenView.setClockView(clockView);
        mLockscreenView.setLockscreeClockEnabled(customLockscreenClock);
        mLockscreenView.updateClockMargins(dp2px(mContext, topMargin));
    }

    private void applyLockscreenViewTop(int legacyTop) {
        if (mLockscreenView == null) return;
        int top = legacyTop;
        if (mStyleClockContainer != null) {
            if (Build.VERSION.SDK_INT >= 37) {
                top = customLockscreenClock ? (int) mLockHeight : mStockClockBottom;
            } else {
                top = customLockscreenClock ? 0 : Math.max(0, mStockClockBottom - (int) mLockHeight);
            }
        }
        if (!(mLockscreenView.getLayoutParams() instanceof ViewGroup.MarginLayoutParams lp)) return;
        if (lp.topMargin == top && lp.leftMargin == 0 && lp.rightMargin == 0 && lp.bottomMargin == 0)
            return;
        lp.setMargins(0, top, 0, 0);
        mLockscreenView.setLayoutParams(lp);
    }

    private void updateAodBurnIn(int uiState) {
        if (Build.VERSION.SDK_INT < 37) return;
        boolean aod = uiState >= 3 && uiState <= 5;
        if (aod == mAodBurnInActive) return;
        mAodBurnInActive = aod;
        mAodBurnInBase = Integer.MIN_VALUE;
        if (mLockscreenView != null && customLockscreenClock) mLockscreenView.setAodClock(aod);
    }

    private void guardStockClock(ViewGroup container) {
        ViewTreeObserver observer = container.getViewTreeObserver();
        if (!observer.isAlive() || mGuardedObservers.get(container) == observer) return;
        mGuardedObservers.put(container, observer);
        observer.addOnPreDrawListener(() -> !customLockscreenClock || !hideStockChildren(container));
    }

    private boolean hideStockChildren(ViewGroup container) {
        boolean changed = false;
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof LockscreenView) continue;
            if (ViewHelper.findViewWithTag(child, OC_LOCKSCREEN_CLOCK_LAYOUT) != null) {
                if (child.getVisibility() != View.VISIBLE) {
                    child.setVisibility(View.VISIBLE);
                    changed = true;
                }
                continue;
            }
            if (child.getVisibility() == View.GONE) continue;
            child.setTag("BY_OC");
            child.setVisibility(View.GONE);
            changed = true;
        }
        return changed;
    }

    private void trackStockClockView(View view) {
        if (mStockClockViews.put(view, Boolean.TRUE) == null) {
            ViewTreeObserver.OnPreDrawListener guard = () -> !enforceStockClockView(view);
            view.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                @Override
                public void onViewAttachedToWindow(@NonNull View v) {
                    enforceStockClockView(v);
                    v.getViewTreeObserver().addOnPreDrawListener(guard);
                }

                @Override
                public void onViewDetachedFromWindow(@NonNull View v) {
                    v.getViewTreeObserver().removeOnPreDrawListener(guard);
                }
            });
            if (view.isAttachedToWindow()) view.getViewTreeObserver().addOnPreDrawListener(guard);
        }
        enforceStockClockView(view);
    }

    private boolean enforceStockClockView(View view) {
        if (customLockscreenClock && ViewHelper.findViewWithTag(view, OC_LOCKSCREEN_CLOCK_LAYOUT) == null) {
            if (view.getVisibility() == View.INVISIBLE) return false;
            view.setVisibility(View.INVISIBLE);
            view.setTag(OC_HIDDEN_STOCK_CLOCK);
            redrawStockClockMirror();
            return true;
        }
        if (!OC_HIDDEN_STOCK_CLOCK.equals(view.getTag())) return false;
        view.setTag(null);
        view.setVisibility(View.VISIBLE);
        redrawStockClockMirror();
        return true;
    }

    private void hookStockClockMirror(Class<?> clockRootClass) {
        if (mStockMirrorHooked) return;
        for (Class<?> c = clockRootClass; c != null && c != View.class; c = c.getSuperclass()) {
            try {
                c.getDeclaredMethod("getMirrorViewAlpha");
            } catch (NoSuchMethodException e) {
                continue;
            }
            mStockMirrorHooked = true;
            ReflectedClass.of(c)
                    .before("getMirrorViewAlpha")
                    .run(param -> {
                        if (customLockscreenClock) param.setResult(0f);
                    });
            return;
        }
    }

    private void redrawStockClockMirror() {
        View root = mStockClockRoot != null ? mStockClockRoot.get() : null;
        if (root == null) return;
        try {
            callMethod(root, "requestMirrorRedraw", true);
        } catch (Throwable ignored) {
        }
    }

    private void attachLockscreenView(View view) {
        if (view == null || mLockscreenView == null) return;
        if (Build.VERSION.SDK_INT >= 37 && customLockscreenClock && view instanceof ViewGroup container) {
            guardStockClock(container);
        }
        if (view instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                if (child instanceof LockscreenView || ViewHelper.findViewWithTag(child, OC_LOCKSCREEN_CLOCK_LAYOUT) != null) {
                    continue;
                }
                if (child.getTag() != null && child.getTag().toString().contains("BY_OC")) {
                    child.setVisibility(customLockscreenClock ? View.GONE : View.VISIBLE);
                }
                if (child.getVisibility() == View.VISIBLE) {
                    child.setTag("BY_OC");
                    child.setVisibility(customLockscreenClock ? View.GONE : View.VISIBLE);
                }
            }
        }
        if (customLockscreenClock) {
            ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
            if (layoutParams != null && layoutParams.width != MATCH_PARENT) {
                layoutParams.width = MATCH_PARENT;
                view.setLayoutParams(layoutParams);
                view.requestLayout();
            }
        }
        if (ViewHelper.findViewWithTag(view, OC_LOCKSCREEN_CLOCK_LAYOUT) != null) {
            return;
        }
        try {
            ((ViewGroup) mLockscreenView.getParent()).removeView(mLockscreenView);
        } catch (Throwable ignored) {
        }
        mLockscreenView.setTag(OC_LOCKSCREEN_CLOCK_LAYOUT);
        if (view instanceof ViewGroup viewGroup) {
            viewGroup.addView(mLockscreenView);
        }
        allowOverflow(mLockscreenView);
    }

    private void allowOverflow(View view) {
        if (view instanceof ViewGroup group) {
            group.setClipChildren(false);
            group.setClipToPadding(false);
            for (int i = 0; i < group.getChildCount(); i++) {
                if (group.getChildAt(i) instanceof ViewGroup child) {
                    child.setClipChildren(false);
                    child.setClipToPadding(false);
                }
            }
        }
        ViewParent parent = view.getParent();
        for (int depth = 0; depth < OVERFLOW_PARENTS && parent instanceof ViewGroup group; depth++) {
            group.setClipChildren(false);
            group.setClipToPadding(false);
            parent = group.getParent();
        }
    }

    private void reloadPlugin() {
        if (mStyleClockContainer != null) {
            mStyleClockContainer.post(() -> attachLockscreenView(mStyleClockContainer));
        }
        if (Build.VERSION.SDK_INT < 35) return;
        Logger.log("LockscreenClock reloadPlugin (mKeyguardStyleClockControllerImpl == null) " + (mKeyguardStyleClockControllerImpl == null));
        if (mKeyguardStyleClockControllerImpl == null) return;
        try {
            boolean isPluginLoaded = getBooleanField(mKeyguardStyleClockControllerImpl, "isPluginLoaded");
            callMethod(mKeyguardStyleClockControllerImpl, "loadOrUnloadPlugin");
        } catch (Throwable t) {
            log("Error reloading plugin: " + t.getMessage());
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
        DesignerClock.release(mDesignerClockView);
        mDesignerClockView = null;
        if (designerMode) {
            mDesignerClockView = DesignerClock.create(appContext, Xprefs.getString(DesignerClock.KEY_APPLIED, ""), new LockscreenHosts(mContext, Xprefs, false));
            DesignerClock.setDepthSubject(mDesignerClockView, DepthSubjectOverlay.INSTANCE);
        }
        LockscreenView lockscreenView = LockscreenView.getInstance();
        if (lockscreenView != null) lockscreenView.setDesignerMode(mDesignerClockView != null);
        if (mDesignerClockView != null) return mDesignerClockView;
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

        if (mDesignerClockView == null)
            setMargins(clockView, mContext, 0, topMargin, 0, bottomMargin);

        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", customColor ? accent1 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", customColor ? accent2 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", customColor ? accent3 : systemAccent);
        if (customColor) {
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", text1);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", text2);
        }

        if (Build.VERSION.SDK_INT >= 37) {
            ViewHelper.applyLiquidGlassWithTag(clockView, "glass");
            GlassTextEffect.applyWithTag(clockView, "glass_clock", "glass_digit");
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
            profilePicture.post(() -> {
                int size = profilePicture.getWidth();
                mImageExecutor.execute(() -> {
                    Drawable drawable = getCustomUserImage(size);
                    profilePicture.post(() -> profilePicture.setImageDrawable(drawable));
                });
            });
        }

        View customImage = findViewWithTag(clockView, "custom_image");
        if (useCustomImage && customImage != null) {
            boolean isRound = lockscreenClockStyle == 39;
            customImage.post(() -> {
                int size = customImage.getWidth();
                mImageExecutor.execute(() -> setCustomImage(customImage, getCustomImage(size, isRound)));
            });
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
        } catch (Throwable ignored) {
        }
        try {
            ((ViewGroup) mRamUsageArcProgress.getParent()).removeView(mRamUsageArcProgress);
        } catch (Throwable ignored) {
        }

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
                if (useCustomUserImage) {
                    imageView.post(() -> {
                        int size = imageView.getWidth();
                        mImageExecutor.execute(() -> {
                            Drawable drawable = getCustomUserImage(size);
                            imageView.post(() -> imageView.setImageDrawable(drawable));
                        });
                    });
                } else {
                    imageView.post(() -> imageView.setImageDrawable(getUserImage()));
                }
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

    @WorkerThread
    private Drawable getImageFromFile(String fileName, @DrawableRes int defaultImage) {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/" + fileName));
            Drawable drawable = ImageDecoder.decodeDrawable(source);
            if (drawable instanceof AnimatedImageDrawable animated) {
                animated.setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
            }
            return drawable;
        } catch (Throwable t) {
            log(t);
            return ResourcesCompat.getDrawable(appContext.getResources(), defaultImage, appContext.getTheme());
        }
    }

    @WorkerThread
    private Drawable getCustomUserImage(int size) {
        Drawable customUserImage = getImageFromFile("lockscreen_user_image.png", R.drawable.default_avatar);
        return new CircleFramedDrawable(DrawableConverter.drawableToBitmap(customUserImage), size);
    }

    @WorkerThread
    private Drawable getCustomImage(int size, boolean isRound) {
        Drawable customUserImage = getImageFromFile("lockscreen_custom_image.png", R.drawable.relax);
        CircleFramedDrawable circled = new CircleFramedDrawable(DrawableConverter.drawableToBitmap(customUserImage), size);
        RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), DrawableConverter.drawableToBitmap(customUserImage));
        roundedDrawable.setCornerRadius(32f);
        return isRound ? circled : roundedDrawable;
    }

    private void setCustomImage(View view, Drawable image) {
        if (view instanceof ImageView iv) {
            iv.post(() -> iv.setImageDrawable(image));
        } else {
            view.post(() -> view.setBackground(image));
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
