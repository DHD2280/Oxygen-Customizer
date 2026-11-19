package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets;

import static android.net.wifi.WifiManager.UNKNOWN_SSID;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getBluetoothController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getCellularTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getControlsTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getDataController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getHotspotController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getHotspotTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getNetworkController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getOplusBluetoothTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getOplusWifiTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getQsMediaDialog;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getRingerTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getWalletTile;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.BT_ACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.BT_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.BT_LABEL_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.CALCULATOR_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.CALCULATOR_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.CAMERA_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.CAMERA_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.DATA_ACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.DATA_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.DATA_LABEL_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOME_CONTROLS;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOME_CONTROLS_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOTSPOT_ACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOTSPOT_ENABLED;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOTSPOT_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOTSPOT_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.MEDIA_PLAY_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.RINGER_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.RINGER_LABEL_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.RINGER_NORMAL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.RINGER_SILENT;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.RINGER_VIBRATE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.TORCH_LABEL_ACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.TORCH_LABEL_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.TORCH_RES_ACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.TORCH_RES_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WIFI_ACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WIFI_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WIFI_LABEL_INACTIVE;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.getDrawable;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.getString;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger.log;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.LiquidGlass;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public final class WidgetsController implements OmniJawsClient.OmniJawsObserver {

    public static final int STYLE_GLASS = 3;

    @SuppressLint("StaticFieldLeak")
    private static WidgetsController instance;

    private final Context mContext;
    private final Context mResContext;
    private final boolean mPreview;
    private Context appContext;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final AudioManager mAudioManager;
    private final CameraManager mCameraManager;
    private String mCameraId;
    private boolean isFlashOn = false;
    private ActivityLauncherUtils mActivityLauncherUtils;

    private final OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;
    private boolean mWeatherObserving = false;

    private MediaController mController;
    private MediaMetadata mMediaMetadata;
    private String mLastTrackTitle = null;
    private boolean mIsLongPress = false;
    private static final long MEDIA_REFRESH_DELAY = 500L;

    private int mDarkColor, mDarkColorActive, mLightColor, mLightColorActive;
    private int mWidgetsStyle = 0;
    private boolean mCustomColors = false;
    private int mBigInactiveColor, mBigActiveColor, mSmallInactiveColor, mSmallActiveColor;
    private int mBigIconInactiveColor, mBigIconActiveColor, mSmallIconInactiveColor, mSmallIconActiveColor;

    final int mFabWidth, mFabHeight, mFabMarginStart, mFabMarginEnd, mFabPadding;
    final int mWidgetCircleSize, mWidgetMarginHorizontal, mWidgetIconPadding;

    private final List<Binding> mBindings = new ArrayList<>();
    private final List<WidgetRowView> mRows = new ArrayList<>();

    static final class Binding {
        final String type;
        final ImageView iv;
        final ExtendedFAB efab;
        final WidgetRowView row;

        Binding(String type, ImageView iv, ExtendedFAB efab, WidgetRowView row) {
            this.type = type;
            this.iv = iv;
            this.efab = efab;
            this.row = row;
        }

        View view() {
            return iv != null ? iv : efab;
        }

        boolean keepsIconColors() {
            return "weather".equals(type) || isApp(type);
        }
    }

    private final MediaController.Callback mMediaCallback = new MediaController.Callback() {
        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            updateMediaController();
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            super.onMetadataChanged(metadata);
            mMediaMetadata = metadata;
            updateMediaController();
        }
    };

    private final BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                onVisible();
            }
        }
    };

    private final BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRingerButtonState();
        }
    };

    private final ControllersProvider.OnMobileDataChanged mMobileDataCallback = new ControllersProvider.OnMobileDataChanged() {
        @Override
        public void setMobileDataIndicators(Object MobileDataIndicators) {
            Object qsIcon = getObjectField(MobileDataIndicators, "qsIcon");
            if (qsIcon == null) {
                updateMobileDataState(false);
                return;
            }
            updateMobileDataState(isMobileDataEnabled());
        }

        @Override
        public void setNoSims(boolean show, boolean simDetected) {
            updateMobileDataState(simDetected && isMobileDataEnabled());
        }

        @Override
        public void setIsAirplaneMode(Object IconState) {
            updateMobileDataState(!getBooleanField(IconState, "visible") && isMobileDataEnabled());
        }
    };

    private WidgetsController(Context context, Object activityStarter, boolean preview) {
        mContext = context;
        mPreview = preview;
        mResContext = preview ? systemUiContext(context) : context;
        mAudioManager = preview ? null : SystemUtils.AudioManager();
        mCameraManager = preview ? null : SystemUtils.CameraManager();
        loadColors();
        mActivityLauncherUtils = preview ? null : new ActivityLauncherUtils(mContext, activityStarter);
        mWeatherClient = new OmniJawsClient(context);
        if (!preview) {
            try {
                mCameraId = SystemUtils.flashCameraId();
                if (mCameraId == null) mCameraId = mCameraManager.getCameraIdList()[0];
            } catch (Throwable e) {
                log("WidgetsController error: " + e.getMessage());
            }
            isFlashOn = SystemUtils.isFlashOn();
        }

        mFabWidth = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width);
        mFabHeight = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height);
        mFabMarginStart = modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_start);
        mFabMarginEnd = modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_end);
        mFabPadding = modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding);
        int circleSize = modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size);
        mWidgetCircleSize = Build.VERSION.SDK_INT >= 35 ? (int) (circleSize * 0.85f) : circleSize;
        mWidgetMarginHorizontal = modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal);
        mWidgetIconPadding = modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding);

        if (preview) return;
        mContext.registerReceiver(mRingerModeReceiver, new IntentFilter("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION"));
        mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON), Context.RECEIVER_EXPORTED);
        ControllersProvider.registerMobileDataCallback(mMobileDataCallback);
        ControllersProvider.registerWifiCallback(this::onWifiChanged);
        ControllersProvider.registerBluetoothCallback(enabled -> updateBtState());
        ControllersProvider.registerTorchModeCallback(this::onTorchChanged);
        ControllersProvider.registerHotspotCallback((enabled, numDevices) -> updateHotspotButtonState(numDevices));
        ThemeEnabler.registerThemeChangedListener(() -> {
            loadColors();
            refreshRows();
        });
    }

    public static WidgetsController getInstance(Context context, Object activityStarter) {
        if (instance == null) instance = new WidgetsController(context, activityStarter, false);
        return instance;
    }

    public static WidgetsController createPreview(Context context) {
        return new WidgetsController(context, null, true);
    }

    private static Context systemUiContext(Context context) {
        try {
            return context.createPackageContext(SYSTEM_UI, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable t) {
            return context;
        }
    }

    public static WidgetsController getInstance() {
        return instance;
    }

    public void setActivityStarter(Object activityStarter) {
        if (mPreview) return;
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, activityStarter);
    }

    public void setStyle(int widgetsStyle, boolean customColorsEnabled,
                         int bigInactive, int bigActive, int smallInactive, int smallActive,
                         int bigIconInactive, int bigIconActive, int smallIconInactive, int smallIconActive) {
        mWidgetsStyle = widgetsStyle;
        mCustomColors = customColorsEnabled;
        mBigInactiveColor = bigInactive;
        mBigActiveColor = bigActive;
        mSmallInactiveColor = smallInactive;
        mSmallActiveColor = smallActive;
        mBigIconInactiveColor = bigIconInactive;
        mBigIconActiveColor = bigIconActive;
        mSmallIconInactiveColor = smallIconInactive;
        mSmallIconActiveColor = smallIconActive;
        refreshRows();
    }

    int getStyle() {
        return mWidgetsStyle;
    }

    boolean isGlass() {
        return mWidgetsStyle == STYLE_GLASS;
    }

    Context getContext() {
        return mContext;
    }

    void addRow(WidgetRowView row) {
        if (!mRows.contains(row)) mRows.add(row);
    }

    void removeRow(WidgetRowView row) {
        mRows.remove(row);
        unbind(row);
    }

    private void refreshRows() {
        for (WidgetRowView row : new ArrayList<>(mRows)) {
            row.rebuild();
        }
    }

    void bind(WidgetRowView row, String type, ImageView iv, ExtendedFAB efab) {
        Binding binding = new Binding(type, iv, efab, row);
        mBindings.add(binding);
        setUpWidget(binding);
        if (mPreview) {
            View view = binding.view();
            view.setOnClickListener(null);
            view.setOnLongClickListener(null);
            view.setOnTouchListener(null);
            view.setClickable(false);
            view.setLongClickable(false);
        }
        setButtonActiveState(binding, false);
        if ("weather".equals(type)) enableWeatherUpdates();
    }

    void unbind(WidgetRowView row) {
        mBindings.removeIf(b -> b.row == row);
        if (!isWidgetEnabled("weather")) disableWeatherUpdates();
    }

    private boolean isWidgetEnabled(String type) {
        for (Binding b : mBindings) {
            if (b.type.equals(type)) return true;
        }
        return false;
    }

    private void forEach(String type, Consumer<Binding> action) {
        for (Binding b : new ArrayList<>(mBindings)) {
            if (b.type.equals(type)) action.accept(b);
        }
    }

    private static boolean isApp(String type) {
        return type.contains("customapp") && type.contains(":");
    }

    private void loadColors() {
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Exception ignored) {
        }
        mDarkColor = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_background_color_dark, appContext.getTheme());
        mLightColor = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_background_color_light, appContext.getTheme());
        mDarkColorActive = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_active_color_dark, appContext.getTheme());
        mLightColorActive = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_active_color_light, appContext.getTheme());
    }

    private void onWifiChanged(Object mWifiTracker) {
        Object qsIcon = getObjectField(mWifiTracker, "qsIcon");
        if (qsIcon == null) {
            updateWiFiButtonState(false);
            return;
        }
        updateWiFiButtonState(isWifiEnabled());
    }

    private void onTorchChanged(boolean enabled) {
        isFlashOn = enabled;
        updateTorchButtonState();
    }

    public void onVisible() {
        if (isWidgetEnabled("weather")) enableWeatherUpdates();
        if (mPreview) return;
        isFlashOn = SystemUtils.isFlashOn();
        updateTorchButtonState();
        updateBtState();
        updateRingerButtonState();
        updateWiFiButtonState(isWifiEnabled());
        updateMobileDataState(isMobileDataEnabled());
        updateHotspotButtonState(0);
        updateMediaController();
    }

    private void enableWeatherUpdates() {
        if (!mWeatherObserving) {
            mWeatherClient.addObserver(this);
            mWeatherObserving = true;
        }
        queryAndUpdateWeather();
    }

    private void disableWeatherUpdates() {
        if (!mWeatherObserving) return;
        mWeatherClient.removeObserver(this);
        mWeatherObserving = false;
    }

    @Override
    public void weatherError(int errorReason) {
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            mWeatherInfo = null;
        }
    }

    @Override
    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    @Override
    public void updateSettings() {
        queryAndUpdateWeather();
    }

    private void queryAndUpdateWeather() {
        try {
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo == null) return;
            String formattedCondition = formatCondition(mWeatherInfo.condition);
            Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
            String text = mWeatherInfo.temp + mWeatherInfo.tempUnits + " • " + formattedCondition;
            forEach("weather", b -> {
                if (b.efab != null) {
                    b.efab.setIcon(d);
                    b.efab.setText(text);
                    b.efab.setIconTint(null);
                }
                if (b.iv != null) {
                    b.iv.setImageDrawable(d);
                    b.iv.setImageTintList(null);
                }
            });
        } catch (Exception ignored) {
        }
    }

    private String formatCondition(String condition) {
        String lower = condition.toLowerCase();
        if (lower.contains("clouds") || lower.contains("overcast"))
            return modRes.getString(R.string.weather_condition_clouds);
        if (lower.contains("rain")) return modRes.getString(R.string.weather_condition_rain);
        if (lower.contains("clear")) return modRes.getString(R.string.weather_condition_clear);
        if (lower.contains("storm")) return modRes.getString(R.string.weather_condition_storm);
        if (lower.contains("snow")) return modRes.getString(R.string.weather_condition_snow);
        if (lower.contains("wind")) return modRes.getString(R.string.weather_condition_wind);
        if (lower.contains("mist")) return modRes.getString(R.string.weather_condition_mist);
        return condition;
    }

    private void setUpWidget(Binding b) {
        String type = b.type;
        if (isApp(type)) {
            String[] split = type.split(":");
            if (split.length != 2) return;
            String packageName = split[1];
            Drawable appIcon = AppUtils.getAppIcon(mContext, packageName);
            if (b.iv != null) {
                b.iv.setTag("app");
                b.iv.setImageDrawable(appIcon);
            }
            if (b.efab != null) {
                b.efab.setTag("app");
                b.efab.setIcon(DrawableConverter.scaleDrawable(mContext, appIcon, 0.3f));
            }
            setUpWidgetResources(b, v -> {
                mActivityLauncherUtils.launchApp(packageName, false);
                vibrate(v, 1);
            }, null, AppUtils.getAppName(mContext, packageName));
            return;
        }
        switch (type) {
            case "wifi" -> {
                setLongClick(b, this::showWifiDialog);
                setUpWidgetResources(b, v -> toggleWiFi(v), getDrawable(mResContext, WIFI_INACTIVE, SYSTEM_UI), getString(mResContext, WIFI_LABEL_INACTIVE, SYSTEM_UI));
            }
            case "data" -> {
                setLongClick(b, this::showInternetDialog);
                setUpWidgetResources(b, v -> toggleMobileData(v), getDrawable(mResContext, DATA_INACTIVE, SYSTEM_UI), getString(mResContext, DATA_LABEL_INACTIVE, SYSTEM_UI));
            }
            case "ringer" -> {
                setLongClick(b, v -> mActivityLauncherUtils.launchAudioSettings(false));
                setUpWidgetResources(b, v -> toggleRingerMode(v), getDrawable(mResContext, RINGER_INACTIVE, SYSTEM_UI), getString(mResContext, RINGER_LABEL_INACTIVE, SYSTEM_UI));
            }
            case "bt" -> {
                setLongClick(b, this::showBluetoothDialog);
                setUpWidgetResources(b, v -> toggleBluetoothState(v), getDrawable(mResContext, BT_INACTIVE, SYSTEM_UI), getString(mResContext, BT_LABEL_INACTIVE, SYSTEM_UI));
            }
            case "torch" ->
                    setUpWidgetResources(b, v -> toggleFlashlight(v), getDrawable(mResContext, TORCH_RES_INACTIVE, SYSTEM_UI), getString(mResContext, TORCH_LABEL_INACTIVE, SYSTEM_UI));
            case "timer" -> setUpWidgetResources(b, v -> {
                mActivityLauncherUtils.launchTimer(false);
                vibrate(v, 1);
            }, getDrawable(mResContext, "ic_alarm", SYSTEM_UI), modRes.getString(R.string.clock_timer));
            case "camera" -> setUpWidgetResources(b, v -> {
                mActivityLauncherUtils.launchCamera(false);
                vibrate(v, 1);
            }, getDrawable(mResContext, CAMERA_ICON, SYSTEM_UI), getString(mResContext, CAMERA_LABEL, SYSTEM_UI));
            case "calculator" -> setUpWidgetResources(b, v -> {
                mActivityLauncherUtils.launchCalculator();
                vibrate(v, 1);
            }, getDrawable(mResContext, CALCULATOR_ICON, SYSTEM_UI), getString(mResContext, CALCULATOR_LABEL, SYSTEM_UI));
            case "homecontrols" ->
                    setUpWidgetResources(b, this::launchHomeControls, getDrawable(mResContext, HOME_CONTROLS, SYSTEM_UI), getString(mResContext, HOME_CONTROLS_LABEL, SYSTEM_UI));
            case "wallet" ->
                    setUpWidgetResources(b, this::launchWallet, getDrawable(mResContext, WALLET_ICON, SYSTEM_UI), getString(mResContext, WALLET_LABEL, SYSTEM_UI));
            case "media" -> {
                setUpWidgetResources(b, v -> toggleMediaPlaybackState(v),
                        ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, mContext.getTheme()),
                        getString(mResContext, MEDIA_PLAY_LABEL, SYSTEM_UI));
                attachSwipeGesture(b.view());
            }
            case "weather" -> {
                setLongClick(b, v -> mActivityLauncherUtils.launchWeatherSettings(false));
                setUpWidgetResources(b, v -> mActivityLauncherUtils.launchWeatherActivity(false),
                        ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.google_30, appContext.getTheme()),
                        appContext.getString(R.string.weather_settings));
            }
            case "hotspot" -> {
                setLongClick(b, this::showHotspotDialog);
                setUpWidgetResources(b, v -> toggleHotspot(v), getDrawable(mResContext, HOTSPOT_INACTIVE, SYSTEM_UI), getString(mResContext, HOTSPOT_LABEL, SYSTEM_UI));
            }
            default -> {
            }
        }
    }

    private void setLongClick(Binding b, Consumer<View> action) {
        b.view().setOnLongClickListener(v -> {
            action.accept(v);
            return true;
        });
    }

    private void setUpWidgetResources(Binding b, View.OnClickListener cl, Drawable icon, String text) {
        if (b.efab != null) {
            b.efab.setOnClickListener(cl);
            if (icon != null) b.efab.setIcon(icon);
            b.efab.setText(text);
            float scale = b.row.getScale();
            b.efab.setIconSize((int) ((mWidgetCircleSize * scale) - (mWidgetIconPadding * 2 * scale)));
            b.efab.setTextSize(14f * scale);
        }
        if (b.iv != null) {
            b.iv.setOnClickListener(cl);
            if (icon != null) b.iv.setImageDrawable(icon);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void attachSwipeGesture(View view) {
        GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    dispatchMediaKeyWithWakeLockToMediaSession(diffX > 0 ? KeyEvent.KEYCODE_MEDIA_PREVIOUS : KeyEvent.KEYCODE_MEDIA_NEXT);
                    vibrate(view, 1);
                    updateMediaController();
                    return true;
                }
                return false;
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                super.onLongPress(e);
                mIsLongPress = true;
                showMediaDialog(view);
                mHandler.postDelayed(() -> mIsLongPress = false, 2500);
            }
        });
        view.setOnTouchListener((v, event) -> {
            boolean isClick = gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_UP && !isClick && !mIsLongPress) {
                v.performClick();
            }
            return true;
        });
    }

    private void setButtonActiveState(Binding b, boolean active) {
        ImageView iv = b.iv;
        ExtendedFAB efab = b.efab;
        boolean keepIconColors = b.keepsIconColors();
        int bgTint;
        int tintColor;

        if (mWidgetsStyle == STYLE_GLASS) {
            if (mCustomColors) {
                bgTint = iv != null ? (active ? mSmallActiveColor : mSmallInactiveColor) : (active ? mBigActiveColor : mBigInactiveColor);
                tintColor = iv != null ? (active ? mSmallIconActiveColor : mSmallIconInactiveColor) : (active ? mBigIconActiveColor : mBigIconInactiveColor);
            } else {
                bgTint = active ? LiquidGlass.ACTIVE_COLOR : LiquidGlass.DEFAULT_COLOR;
                tintColor = active ? Color.BLACK : Color.WHITE;
            }
            float radius = dp2px(mContext, 18) * b.row.getScale();
            if (iv != null) {
                LiquidGlass.setColor(iv, radius, bgTint);
                iv.setImageTintList(!"weather".equals(b.type) ? ColorStateList.valueOf(tintColor) : null);
            }
            if (efab != null) {
                LiquidGlass.setColor(efab, radius, bgTint);
                efab.setIconTint(keepIconColors ? null : ColorStateList.valueOf(tintColor));
                efab.setTextColor(tintColor);
            }
            return;
        }

        if (!mCustomColors) {
            if (active) {
                bgTint = mWidgetsStyle == 2 ? applyAlpha(0.3f, mDarkColorActive) : isNightMode() ? mDarkColorActive : mLightColorActive;
                tintColor = mWidgetsStyle == 2 ? mDarkColorActive : isNightMode() ? mDarkColor : mLightColor;
            } else {
                bgTint = mWidgetsStyle == 2 ? applyAlpha(0.3f, Color.WHITE) : isNightMode() ? mDarkColor : mLightColor;
                tintColor = mWidgetsStyle == 2 ? Color.WHITE : isNightMode() ? mLightColor : mDarkColor;
            }
            if (iv != null) {
                iv.setBackgroundTintList(ColorStateList.valueOf(bgTint));
                iv.setImageTintList(!"weather".equals(b.type) ? ColorStateList.valueOf(tintColor) : null);
            }
            if (efab != null) {
                efab.setBackgroundTintList(ColorStateList.valueOf(bgTint));
                efab.setIconTint(keepIconColors ? null : ColorStateList.valueOf(tintColor));
                efab.setTextColor(tintColor);
            }
        } else {
            if (iv != null) {
                iv.setBackgroundTintList(ColorStateList.valueOf(active ? mSmallActiveColor : mSmallInactiveColor));
                iv.setImageTintList(!"weather".equals(b.type) ? ColorStateList.valueOf(active ? mSmallIconActiveColor : mSmallIconInactiveColor) : null);
            }
            if (efab != null) {
                efab.setBackgroundTintList(ColorStateList.valueOf(active ? mBigActiveColor : mBigInactiveColor));
                efab.setIconTint(!"weather".equals(b.type) ? ColorStateList.valueOf(active ? mBigIconActiveColor : mBigIconInactiveColor) : null);
                efab.setTextColor(active ? mBigIconActiveColor : mBigIconInactiveColor);
            }
        }
    }

    private boolean isNightMode() {
        Configuration config = mContext.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private boolean isMediaControllerAvailable() {
        MediaController mediaController = getActiveLocalMediaController();
        return mediaController != null && !TextUtils.isEmpty(mediaController.getPackageName());
    }

    private MediaController getActiveLocalMediaController() {
        MediaSessionManager mediaSessionManager = mContext.getSystemService(MediaSessionManager.class);
        MediaController localController = null;
        List<String> remoteMediaSessionLists = new ArrayList<>();
        for (MediaController controller : mediaSessionManager.getActiveSessions(null)) {
            MediaController.PlaybackInfo pi = controller.getPlaybackInfo();
            if (pi == null) continue;
            PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState == null) continue;
            if (playbackState.getState() != PlaybackState.STATE_PLAYING) continue;
            if (pi.getPlaybackType() == MediaController.PlaybackInfo.PLAYBACK_TYPE_REMOTE) {
                if (localController != null && TextUtils.equals(localController.getPackageName(), controller.getPackageName())) {
                    localController = null;
                }
                if (!remoteMediaSessionLists.contains(controller.getPackageName())) {
                    remoteMediaSessionLists.add(controller.getPackageName());
                }
                continue;
            }
            if (pi.getPlaybackType() == MediaController.PlaybackInfo.PLAYBACK_TYPE_LOCAL) {
                if (localController == null && !remoteMediaSessionLists.contains(controller.getPackageName())) {
                    localController = controller;
                }
            }
        }
        return localController;
    }

    private void updateMediaController() {
        if (!isWidgetEnabled("media")) return;
        MediaController localController = getActiveLocalMediaController();
        if (localController != null && !Objects.equals(mController, localController)) {
            if (mController != null) {
                mController.unregisterCallback(mMediaCallback);
                mController = null;
            }
            mController = localController;
            mController.registerCallback(mMediaCallback);
        }
        mMediaMetadata = isMediaControllerAvailable() ? mController.getMetadata() : null;
        updateMediaPlaybackState();
        mHandler.postDelayed(this::updateMediaPlaybackState, 250);
    }

    private void toggleMediaPlaybackState(View v) {
        if (isMediaPlaying()) {
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PAUSE);
            updateMediaController();
        } else {
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY);
            mHandler.postDelayed(this::updateMediaController, MEDIA_REFRESH_DELAY);
            mHandler.postDelayed(this::updateMediaController, MEDIA_REFRESH_DELAY * 4);
        }
        vibrate(v, 1);
    }

    private void showMediaDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) return;
        updateMediaController();
        Object[] mediaQsHelper = getQsMediaDialog();
        if (mediaQsHelper[0] == null || mediaQsHelper[1] == null) return;
        callMethod(mediaQsHelper[1], "showPrompt", mContext, anchorOf(view), mediaQsHelper[0]);
        vibrate(view, 0);
    }

    private void dispatchMediaKeyWithWakeLockToMediaSession(int keycode) {
        KeyEvent mediaEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        mAudioManager.dispatchMediaKeyEvent(mediaEvent);
        mediaEvent = KeyEvent.changeAction(mediaEvent, KeyEvent.ACTION_UP);
        mAudioManager.dispatchMediaKeyEvent(mediaEvent);
    }

    private void updateMediaPlaybackState() {
        boolean isPlaying = isMediaPlaying();
        Drawable icon = ResourcesCompat.getDrawable(modRes, isPlaying ? R.drawable.ic_pause : R.drawable.ic_play, mContext.getTheme());
        String trackTitle = mMediaMetadata != null ? mMediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE) : "";
        if (!TextUtils.isEmpty(trackTitle) && !Objects.equals(mLastTrackTitle, trackTitle)) {
            mLastTrackTitle = trackTitle;
        }
        boolean canShowTrackTitle = isPlaying || !TextUtils.isEmpty(mLastTrackTitle);
        forEach("media", b -> {
            if (b.iv != null) b.iv.setImageDrawable(icon);
            if (b.efab != null) {
                b.efab.setIcon(icon);
                b.efab.setText(canShowTrackTitle ? mLastTrackTitle : "Play");
            }
            setButtonActiveState(b, isPlaying);
        });
    }

    private boolean isMediaPlaying() {
        return isMediaControllerAvailable() && PlaybackState.STATE_PLAYING == getMediaControllerPlaybackState(mController);
    }

    private int getMediaControllerPlaybackState(MediaController controller) {
        if (controller != null) {
            PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState != null) return playbackState.getState();
        }
        return PlaybackState.STATE_NONE;
    }

    private void toggleFlashlight(View v) {
        try {
            boolean target = !SystemUtils.isFlashOn();
            if (SystemUtils.CameraManager() != null) {
                SystemUtils.toggleFlash();
            } else {
                mCameraManager.setTorchMode(mCameraId, target);
            }
            isFlashOn = target;
            updateTorchButtonState();
            vibrate(v, 1);
        } catch (Exception e) {
            log("WidgetsController toggleFlashlight error: " + e.getMessage());
        }
    }

    private void launchHomeControls(View view) {
        Object controlsTile = getControlsTile();
        if (controlsTile == null) return;
        View anchor = anchorOf(view);
        mHandler.post(() -> callMethod(controlsTile, "handleClick", (Build.VERSION.SDK_INT >= 35) ? null : anchor));
        vibrate(view, 1);
    }

    private void launchWallet(View view) {
        Object walletTile = getWalletTile();
        if (walletTile != null) {
            View anchor = anchorOf(view);
            mHandler.post(() -> callMethod(walletTile, "handleClick", (Build.VERSION.SDK_INT >= 35) ? null : anchor));
        } else {
            mActivityLauncherUtils.launchWallet();
        }
        vibrate(view, 1);
    }

    private void toggleWiFi(View v) {
        Object networkController = getNetworkController();
        boolean enabled = SystemUtils.WifiManager().isWifiEnabled();
        if (networkController != null) {
            callMethod(networkController, "setWifiEnabled", !enabled);
        } else {
            SystemUtils.WifiManager().setWifiEnabled(!enabled);
        }
        updateWiFiButtonState(!enabled);
        mHandler.postDelayed(() -> updateWiFiButtonState(isWifiEnabled()), 350L);
        vibrate(v, 1);
    }

    private void toggleMobileData(View v) {
        if (getDataController() == null) return;
        callMethod(getDataController(), "setMobileDataEnabled", !isMobileDataEnabled());
        updateMobileDataState(!isMobileDataEnabled());
        mHandler.postDelayed(() -> updateMobileDataState(isMobileDataEnabled()), 250L);
        vibrate(v, 1);
    }

    private void toggleHotspot(View v) {
        Object hotspotController = getHotspotController();
        if (hotspotController != null) {
            callMethod(hotspotController, "setHotspotEnabled", !isHotspotEnabled());
        }
        updateHotspotButtonState(0);
        mHandler.postDelayed(() -> updateHotspotButtonState(0), 350L);
        vibrate(v, 1);
    }

    private void toggleRingerMode(View v) {
        boolean toggled = false;
        try {
            Object ringerTile = getRingerTile();
            if (ringerTile != null) {
                callMethod(ringerTile, "setRingMode");
                toggled = true;
            }
        } catch (Throwable t) {
            log("WidgetsController ringer tile toggle failed: " + t);
        }
        if (!toggled && mAudioManager != null) {
            try {
                int newMode = switch (mAudioManager.getRingerMode()) {
                    case AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE;
                    case AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT;
                    default -> AudioManager.RINGER_MODE_NORMAL;
                };
                mAudioManager.setRingerMode(newMode);
            } catch (Throwable t) {
                log("WidgetsController ringer toggle failed: " + t);
            }
        }
        try {
            updateRingerButtonState();
        } catch (Throwable t) {
            log("WidgetsController ringer state failed: " + t);
        }
        vibrate(v, 1);
    }

    private void toggleBluetoothState(View v) {
        Object bluetoothController = getBluetoothController();
        if (bluetoothController == null) return;
        callMethod(bluetoothController, "setBluetoothEnabled", !isBluetoothEnabled());
        updateBtState();
        mHandler.postDelayed(this::updateBtState, 350L);
        vibrate(v, 1);
    }

    private View anchorOf(View view) {
        return view instanceof ExtendedFAB ? (View) view.getParent() : view;
    }

    private void showWifiDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchWifiSettings();
            vibrate(view, 0);
            return;
        }
        View anchor = anchorOf(view);
        mHandler.post(() -> callMethod(getOplusWifiTile(), "handleSecondaryClick", (Build.VERSION.SDK_INT >= 35) ? null : anchor));
        vibrate(view, 0);
    }

    private void showInternetDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchInternetSettings();
            vibrate(view, 0);
            return;
        }
        if (getCellularTile() == null) return;
        View anchor = anchorOf(view);
        mHandler.post(() -> callMethod(getCellularTile(), "handleSecondaryClick", (Build.VERSION.SDK_INT >= 35) ? null : anchor));
        vibrate(view, 0);
    }

    private void showBluetoothDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchBluetoothSettings();
            vibrate(view, 0);
            return;
        }
        View anchor = anchorOf(view);
        mHandler.post(() -> callMethod(getOplusBluetoothTile(), "handleSecondaryClick", (Build.VERSION.SDK_INT >= 35) ? null : anchor));
        vibrate(view, 0);
    }

    private void showHotspotDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchHotspotSettings();
            vibrate(view, 0);
            return;
        }
        if (getHotspotTile() == null) return;
        View anchor = anchorOf(view);
        mHandler.post(() -> callMethod(getHotspotTile(), "handleSecondaryClick", (Build.VERSION.SDK_INT >= 35) ? null : anchor));
        vibrate(view, 0);
    }

    private void updateTileButtonState(String type, boolean active, Drawable icon, String text) {
        mHandler.post(() -> forEach(type, b -> {
            if (b.iv != null) b.iv.setImageDrawable(icon);
            if (b.efab != null) {
                b.efab.setIcon(icon);
                b.efab.setText(text);
            }
            setButtonActiveState(b, active);
        }));
    }

    @SuppressLint("DiscouragedApi")
    private void updateTileButtonState(String type, boolean active, String activeResource, String inactiveResource, String activeString, String inactiveString) {
        mHandler.post(() -> {
            @SuppressLint("UseCompatLoadingForDrawables")
            Drawable d = mResContext.getDrawable(mResContext.getResources().getIdentifier(active ? activeResource : inactiveResource, "drawable", SYSTEM_UI));
            forEach(type, b -> {
                if (b.iv != null) b.iv.setImageDrawable(d);
                if (b.efab != null) {
                    b.efab.setIcon(d);
                    b.efab.setText(active ? activeString : inactiveString);
                }
                setButtonActiveState(b, active);
            });
        });
    }

    public void updateTorchButtonState() {
        if (!isWidgetEnabled("torch")) return;
        updateTileButtonState("torch", isFlashOn, TORCH_RES_ACTIVE, TORCH_RES_INACTIVE,
                getString(mResContext, TORCH_LABEL_ACTIVE, SYSTEM_UI), getString(mResContext, TORCH_LABEL_INACTIVE, SYSTEM_UI));
    }

    @SuppressWarnings("deprecation")
    private void updateWiFiButtonState(boolean enabled) {
        if (!isWidgetEnabled("wifi")) return;
        String inactive = getString(mResContext, WIFI_LABEL_INACTIVE, SYSTEM_UI);
        String theSsid = SystemUtils.WifiManager().getConnectionInfo().getSSID();
        if (theSsid.equals(UNKNOWN_SSID)) {
            theSsid = inactive;
        } else if (theSsid.startsWith("\"") && theSsid.endsWith("\"")) {
            theSsid = theSsid.substring(1, theSsid.length() - 1);
        }
        updateTileButtonState("wifi", isWifiEnabled(), WIFI_ACTIVE, WIFI_INACTIVE, theSsid, inactive);
    }

    private void updateRingerButtonState() {
        if (!isWidgetEnabled("ringer") || mAudioManager == null) return;
        boolean soundActive = mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
        updateTileButtonState("ringer", soundActive, getRingerDrawable(), getRingerText());
    }

    private void updateMobileDataState(boolean enabled) {
        if (!isWidgetEnabled("data")) return;
        Object networkController = getNetworkController();
        String networkName = networkController == null ? "" : (String) callMethod(networkController, "getMobileDataNetworkName");
        boolean hasNetwork = networkController != null && !TextUtils.isEmpty(networkName);
        String inactive = getString(mResContext, DATA_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState("data", enabled, DATA_ACTIVE, DATA_INACTIVE, hasNetwork && enabled ? networkName : inactive, inactive);
    }

    private void updateBtState() {
        if (!isWidgetEnabled("bt")) return;
        Object bluetoothController = getBluetoothController();
        String deviceName = isBluetoothEnabled() ? (String) callMethod(bluetoothController, "getConnectedDeviceName") : "";
        boolean isConnected = !TextUtils.isEmpty(deviceName);
        String inactiveString = getString(mResContext, BT_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState("bt", isBluetoothEnabled(), BT_ACTIVE, BT_INACTIVE, isConnected ? deviceName : inactiveString, inactiveString);
    }

    private void updateHotspotButtonState(int numDevices) {
        if (!isWidgetEnabled("hotspot")) return;
        String inactiveString = getString(mResContext, HOTSPOT_LABEL, SYSTEM_UI);
        String activeString = inactiveString;
        if (isHotspotEnabled()) {
            String hotspotSSID = getHotspotSSID();
            if (!TextUtils.isEmpty(hotspotSSID)) {
                activeString = numDevices > 0 ? hotspotSSID + " (" + numDevices + ")" : hotspotSSID;
            }
        }
        updateTileButtonState("hotspot", isHotspotEnabled(), HOTSPOT_ACTIVE, HOTSPOT_INACTIVE, activeString, inactiveString);
    }

    private boolean isBluetoothEnabled() {
        Object bluetoothController = getBluetoothController();
        try {
            return getBooleanField(bluetoothController, "mEnabled");
        } catch (Throwable ignored) {
            if (SystemUtils.BluetoothManager().getAdapter() == null) return false;
            return SystemUtils.BluetoothManager().getAdapter().isEnabled();
        }
    }

    private boolean isMobileDataEnabled() {
        Object dataController = getDataController();
        try {
            return (boolean) callMethod(dataController, "isMobileDataEnabled");
        } catch (Throwable ignored) {
            try {
                return ((TelephonyManager) callMethod(dataController, "getTelephonyManager")).isDataEnabled();
            } catch (Throwable ignoredToo) {
            }
            try {
                Method method = SystemUtils.ConnectivityManager().getClass().getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true);
                return (Boolean) method.invoke(SystemUtils.ConnectivityManager());
            } catch (Exception e) {
                return false;
            }
        }
    }

    private boolean isWifiEnabled() {
        return SystemUtils.WifiManager().isWifiEnabled();
    }

    private boolean isHotspotEnabled() {
        Object hotspotController = getHotspotController();
        if (hotspotController != null) {
            return (boolean) callMethod(hotspotController, "isHotspotEnabled");
        }
        try {
            Method method = SystemUtils.WifiManager().getClass().getDeclaredMethod("getWifiApState");
            method.setAccessible(true);
            int actualState = (Integer) method.invoke(SystemUtils.WifiManager(), (Object[]) null);
            return actualState == HOTSPOT_ENABLED;
        } catch (Throwable t) {
            log("WidgetsController isHotspotEnabled error: " + t.getMessage());
        }
        return false;
    }

    @SuppressWarnings("deprecation")
    private String getHotspotSSID() {
        try {
            for (Method m : SystemUtils.WifiManager().getClass().getDeclaredMethods()) {
                if (m.getName().equals("getWifiApConfiguration")) {
                    WifiConfiguration config = (WifiConfiguration) m.invoke(SystemUtils.WifiManager());
                    return config.SSID;
                }
            }
        } catch (Throwable t) {
            log("WidgetsController getHotspotSSID error: " + t.getMessage());
        }
        return "";
    }

    private Drawable getRingerDrawable() {
        String resName = switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE;
            case AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT;
            default -> RINGER_NORMAL;
        };
        return getDrawable(mResContext, resName, SYSTEM_UI);
    }

    private String getRingerText() {
        String resName = switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_VIBRATE -> "state_button_vibration";
            case AudioManager.RINGER_MODE_SILENT -> "state_button_silence";
            default -> "volume_footer_ring";
        };
        return getString(mResContext, resName, SYSTEM_UI);
    }

    private void vibrate(View view, int type) {
        if (view == null) return;
        view.performHapticFeedback(type == 0 ? HapticFeedbackConstants.LONG_PRESS : HapticFeedbackConstants.CONTEXT_CLICK);
    }

    @ColorInt
    public static int applyAlpha(float alpha, int inputColor) {
        alpha *= Color.alpha(inputColor);
        return Color.argb((int) alpha, Color.red(inputColor), Color.green(inputColor), Color.blue(inputColor));
    }
}
