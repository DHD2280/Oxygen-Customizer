package it.dhd.oxygencustomizer.xposed.views;


import static android.net.wifi.WifiManager.UNKNOWN_SSID;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableImageView;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableLinearLayout;
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

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
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
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

@SuppressLint("ViewConstructor")
public class LockscreenWidgetsView extends LinearLayout implements OmniJawsClient.OmniJawsObserver {

    @SuppressLint("StaticFieldLeak")
    public static LockscreenWidgetsView instance = null;

    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    private final Context mContext;
    private Context appContext;

    // Two Linear Layouts, one for main widgets and one for secondary widgets
    private LinearLayout mDeviceWidgetContainer;
    private LinearLayout mMainWidgetsContainer;
    private LinearLayout mSecondaryWidgetsContainer;
    private DeviceWidgetView mDeviceWidgetView;

    private ImageView mediaButton, torchButton, weatherButton, hotspotButton;
    private ExtendedFAB mediaButtonFab, torchButtonFab, weatherButtonFab, hotspotButtonFab;
    private ExtendedFAB wifiButtonFab, dataButtonFab, ringerButtonFab, btButtonFab;
    private ImageView wifiButton, dataButton, ringerButton, btButton;
    private int mDarkColor;
    private int mDarkColorActive;
    private int mLightColor;
    private int mLightColorActive;

    // Custom Widgets Colors
    private boolean mCustomColors = false;
    private int mBigInactiveColor, mBigActiveColor, mSmallInactiveColor, mSmallActiveColor;
    private int mBigIconInactiveColor, mBigIconActiveColor, mSmallIconInactiveColor, mSmallIconActiveColor;

    // Widgets Dimens
    private int mFabWidth, mFabHeight, mFabMarginStart, mFabMarginEnd, mFabPadding;
    private int mWidgetCircleSize, mWidgetMarginHorizontal, mWidgetIconPadding;
    private float mWidgetsScale = 1f;

    private String mMainLockscreenWidgetsList;
    private String mSecondaryLockscreenWidgetsList;
    private ExtendedFAB[] mMainWidgetViews;
    private ImageView[] mSecondaryWidgetViews;
    private List<String> mMainWidgetsList = new ArrayList<>();
    private List<String> mSecondaryWidgetsList = new ArrayList<>();

    private final AudioManager mAudioManager;
    private MediaController mController;
    private MediaMetadata mMediaMetadata;
    private String mLastTrackTitle = null;

    private boolean lockscreenWidgetsEnabled = false;
    private boolean deviceWidgetsEnabled = false;

    private boolean mIsLongPress = false;

    private final CameraManager mCameraManager;
    private String mCameraId;
    private boolean isFlashOn = false;

    private final Runnable mMediaUpdater;
    private final Handler mHandler;

    private ActivityLauncherUtils mActivityLauncherUtils;

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

    final BroadcastReceiver mScreenOnReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                onVisible();
            }
        }
    };

    public LockscreenWidgetsView(Context context, Object activityStarter) {
        super(context);

        instance = this;

        mContext = context;
        mAudioManager = SystemUtils.AudioManager();
        mCameraManager = SystemUtils.CameraManager();

        loadColors();

        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, activityStarter);

        mHandler = new Handler(Looper.getMainLooper());

        if (mWeatherClient == null) {
            mWeatherClient = new OmniJawsClient(context);
        }
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (Throwable e) {
            log("LockscreenWidgetsView error: " + e.getMessage());
        }

        setupDimens();
        drawUI();

        IntentFilter ringerFilter = new IntentFilter("android.media.INTERNAL_RINGER_MODE_CHANGED_ACTION");
        mContext.registerReceiver(mRingerModeReceiver, ringerFilter);
        mMediaUpdater = new Runnable() {
            @Override
            public void run() {
                updateMediaController();
                mHandler.postDelayed(this, 1000);
            }
        };
        updateMediaController();

        ControllersProvider.registerMobileDataCallback(mMobileDataCallback);
        ControllersProvider.registerWifiCallback(this::onWifiChanged);
        ControllersProvider.registerBluetoothCallback(this::onBluetoothChanged);
        ControllersProvider.registerTorchModeCallback(this::onTorchChanged);
        ControllersProvider.registerHotspotCallback(this::onHotspotChanged);
        ThemeEnabler.registerThemeChangedListener(() -> {
            loadColors();
            updateWidgetViews();
        });

        // Add a Screen On Receiver so we can update the widgets state when the screen is turned on
        mContext.registerReceiver(mScreenOnReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON), Context.RECEIVER_EXPORTED);

    }

    private void setupDimens() {

        // Fab Dimens
        mFabWidth = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width);
        mFabHeight = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height);
        mFabMarginStart = modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_start);
        mFabMarginEnd = modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_end);
        mFabPadding = modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding);

        // Circle Dimens
        mWidgetCircleSize = modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size);
        mWidgetMarginHorizontal = modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal);
        mWidgetIconPadding = modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding);
    }

    private void drawUI() {
        LinearLayout container = new LinearLayout(mContext);
        container.setOrientation(VERTICAL);
        container.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Device Widget Container
        mDeviceWidgetContainer = createDeviceWidgetContainer(mContext);
        container.addView(mDeviceWidgetContainer);

        // Add main widgets container
        mMainWidgetsContainer = createMainWidgetsContainer(mContext);
        container.addView(mMainWidgetsContainer);

        // Add secondary widgets container
        mSecondaryWidgetsContainer = createSecondaryWidgetsContainer(mContext);
        container.addView(mSecondaryWidgetsContainer);

        addView(container);
    }

    private LinearLayout createDeviceWidgetContainer(Context context) {
        LinearLayout deviceWidget = new LinearLayout(context);
        deviceWidget.setOrientation(HORIZONTAL);
        deviceWidget.setGravity(Gravity.CENTER);
        deviceWidget.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        if (mDeviceWidgetView == null)
            mDeviceWidgetView = new DeviceWidgetView(context);

        try {
            ((ViewGroup)(mDeviceWidgetView.getParent())).removeView(mDeviceWidgetView);
        } catch (Throwable ignored) {}

        deviceWidget.addView(mDeviceWidgetView);

        return deviceWidget;
    }

    private LinearLayout createMainWidgetsContainer(Context context) {
        LinearLayout mainWidgetsContainer;
        try {
            mainWidgetsContainer = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            mainWidgetsContainer = new LinearLayout(context);
        }

        mainWidgetsContainer.setOrientation(HORIZONTAL);
        mainWidgetsContainer.setGravity(Gravity.CENTER);
        mainWidgetsContainer.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Add FABs to the main widgets container
        mMainWidgetViews = new ExtendedFAB[]{
                createFAB(context),
                createFAB(context)
        };

        for (ExtendedFAB mMainWidgetView : mMainWidgetViews) {
            mainWidgetsContainer.addView(mMainWidgetView);
        }

        return mainWidgetsContainer;
    }

    private ExtendedFAB createFAB(Context context) {
        ExtendedFAB fab = new ExtendedFAB(context);
        fab.setId(View.generateViewId());
        LayoutParams params = new LayoutParams(
                (int)(mFabWidth*mWidgetsScale),
                (int)(mFabHeight*mWidgetsScale));
        params.setMargins(
                (int)(mFabMarginStart*mWidgetsScale),
                0,
                (int)(mFabMarginEnd*mWidgetsScale),
                0);
        fab.setLayoutParams(params);
        fab.setPadding(
                (int)(mFabPadding*mWidgetsScale),
                (int)(mFabPadding*mWidgetsScale),
                (int)(mFabPadding*mWidgetsScale),
                (int)(mFabPadding*mWidgetsScale));
        fab.setGravity(Gravity.CENTER);
        return fab;
    }

    private LinearLayout createSecondaryWidgetsContainer(Context context) {
        LinearLayout secondaryWidgetsContainer;
        try {
            secondaryWidgetsContainer = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            secondaryWidgetsContainer = new LinearLayout(context);
        }

        secondaryWidgetsContainer.setOrientation(HORIZONTAL);
        secondaryWidgetsContainer.setGravity(Gravity.CENTER_HORIZONTAL);
        secondaryWidgetsContainer.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        ((MarginLayoutParams) secondaryWidgetsContainer.getLayoutParams()).topMargin =
                modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_vertical);
        ((MarginLayoutParams) secondaryWidgetsContainer.getLayoutParams()).bottomMargin =
                modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_bottom);

        // Add ImageViews to the secondary widgets container
        mSecondaryWidgetViews = new ImageView[]{
                createImageView(context),
                createImageView(context),
                createImageView(context),
                createImageView(context)
        };

        for (ImageView mSecondaryWidgetView : mSecondaryWidgetViews) {
            secondaryWidgetsContainer.addView(mSecondaryWidgetView);
        }

        return secondaryWidgetsContainer;
    }

    private ImageView createImageView(Context context) {
        ImageView imageView;
        try {
            imageView = (ImageView) LaunchableImageView.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            // LaunchableImageView not found or other error, ensure the creation of our ImageView
            imageView = new ImageView(context);
        }

        imageView.setId(View.generateViewId());
        LayoutParams params = new LayoutParams(
                (int)(mWidgetCircleSize*mWidgetsScale),
                (int)(mWidgetCircleSize*mWidgetsScale));
        params.setMargins(
                (int)(mWidgetMarginHorizontal*mWidgetsScale),
                0,
                (int)(mWidgetMarginHorizontal*mWidgetsScale),
                0);
        imageView.setLayoutParams(params);
        imageView.setPadding(
                (int)(mWidgetIconPadding*mWidgetsScale),
                (int)(mWidgetIconPadding*mWidgetsScale),
                (int)(mWidgetIconPadding*mWidgetsScale),
                (int)(mWidgetIconPadding*mWidgetsScale));
        imageView.setFocusable(true);
        imageView.setClickable(true);

        return imageView;
    }

    private void loadColors() {
        try {
            appContext = mContext.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (Exception ignored) {}
        mDarkColor = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_background_color_dark, appContext.getTheme());
        mLightColor = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_background_color_light, appContext.getTheme());
        mDarkColorActive = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_active_color_dark, appContext.getTheme());
        mLightColorActive = ResourcesCompat.getColor(appContext.getResources(), R.color.lockscreen_widget_active_color_light, appContext.getTheme());
    }

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

    private void onWifiChanged(Object mWifiTracker) {
        Object qsIcon = getObjectField(mWifiTracker, "qsIcon");
        if (qsIcon == null) {
            updateWiFiButtonState(false);
            return;
        }
        updateWiFiButtonState(isWifiEnabled());
    }

    private void onBluetoothChanged(boolean enabled) {
        updateBtState();
    }

    private void onTorchChanged(boolean enabled) {
        isFlashOn = enabled;
        updateTorchButtonState();
    }

    private void onHotspotChanged(boolean enabled, int numDevices) {
        updateHotspotButtonState(numDevices);
    }

    public void enableWeatherUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this);
            queryAndUpdateWeather();
        }
    }

    public void disableWeatherUpdates() {
        if (mWeatherClient != null) {
            weatherButton = null;
            weatherButtonFab = null;
            mWeatherClient.removeObserver(this);
        }
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

    @SuppressLint("SetTextI18n")
    private void queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null) {
                return;
            }
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo != null) {
                // OpenWeatherMap
                String formattedCondition = mWeatherInfo.condition;
                if (formattedCondition.toLowerCase().contains("clouds") || formattedCondition.toLowerCase().contains("overcast")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clouds);
                } else if (formattedCondition.toLowerCase().contains("rain")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_rain);
                } else if (formattedCondition.toLowerCase().contains("clear")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clear);
                } else if (formattedCondition.toLowerCase().contains("storm")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_storm);
                } else if (formattedCondition.toLowerCase().contains("snow")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_snow);
                } else if (formattedCondition.toLowerCase().contains("wind")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_wind);
                } else if (formattedCondition.toLowerCase().contains("mist")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_mist);
                }

                final Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
                if (weatherButtonFab != null) {
                    weatherButtonFab.setIcon(d);
                    weatherButtonFab.setText(mWeatherInfo.temp + mWeatherInfo.tempUnits + " • " + formattedCondition);
                    weatherButtonFab.setIconTint(null);
                }
                if (weatherButton != null) {
                    weatherButton.setImageDrawable(d);
                    weatherButton.setImageTintList(null);
                }
            }
        } catch (Exception e) {
            // Do nothing
        }
    }

    private boolean isMediaControllerAvailable() {
        final MediaController mediaController = getActiveLocalMediaController();
        return mediaController != null && !TextUtils.isEmpty(mediaController.getPackageName());
    }

    private MediaController getActiveLocalMediaController() {
        MediaSessionManager mediaSessionManager =
                mContext.getSystemService(MediaSessionManager.class);
        MediaController localController = null;
        final List<String> remoteMediaSessionLists = new ArrayList<>();
        for (MediaController controller : mediaSessionManager.getActiveSessions(null)) {
            final MediaController.PlaybackInfo pi = controller.getPlaybackInfo();
            if (pi == null) {
                continue;
            }
            final PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState == null) {
                continue;
            }
            if (playbackState.getState() != PlaybackState.STATE_PLAYING) {
                continue;
            }
            if (pi.getPlaybackType() == MediaController.PlaybackInfo.PLAYBACK_TYPE_REMOTE) {
                if (localController != null
                        && TextUtils.equals(
                        localController.getPackageName(), controller.getPackageName())) {
                    localController = null;
                }
                if (!remoteMediaSessionLists.contains(controller.getPackageName())) {
                    remoteMediaSessionLists.add(controller.getPackageName());
                }
                continue;
            }
            if (pi.getPlaybackType() == MediaController.PlaybackInfo.PLAYBACK_TYPE_LOCAL) {
                if (localController == null
                        && !remoteMediaSessionLists.contains(controller.getPackageName())) {
                    localController = controller;
                }
            }
        }
        return localController;
    }

    private boolean isWidgetEnabled(String widget) {
        if (mMainWidgetViews == null || mSecondaryWidgetViews == null) {
            return false;
        }
        return mMainWidgetsList.contains(widget) || mSecondaryWidgetsList.contains(widget);
    }

    private void updateMediaController() {
        if (!isWidgetEnabled("media")) return;
        MediaController localController = getActiveLocalMediaController();
        if (localController != null && !sameSessions(mController, localController)) {
            if (mController != null) {
                mController.unregisterCallback(mMediaCallback);
                mController = null;
            }
            mController = localController;
            mController.registerCallback(mMediaCallback);
        }
        mMediaMetadata = isMediaControllerAvailable() ? mController.getMetadata() : null;
        updateMediaState();
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE && isAttachedToWindow()) {
            onVisible();
            updateMediaController();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (!lockscreenWidgetsEnabled) return;
        if (visibility == View.VISIBLE) {
            onVisible();
        }
    }

    private void onVisible() {
        // Update the widgets when the view is visible
        if (isWidgetEnabled("weather")) {
            enableWeatherUpdates();
        }
        updateTorchButtonState();
        updateBtState();
        updateRingerButtonState();
        updateWiFiButtonState(isWifiEnabled());
        updateMobileDataState(isMobileDataEnabled());
        updateHotspotButtonState(0);
        updateMediaController();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (isWidgetEnabled("weather")) {
            enableWeatherUpdates();
        }
        onVisible();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (isWidgetEnabled("weather")) {
            disableWeatherUpdates();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateWidgetViews();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        updateWidgetViews();
        onVisible();
    }

    private void updateContainerVisibility() {
        final boolean isMainWidgetsEmpty = mMainLockscreenWidgetsList == null
                || TextUtils.isEmpty(mMainLockscreenWidgetsList);
        final boolean isSecondaryWidgetsEmpty = mSecondaryLockscreenWidgetsList == null
                || TextUtils.isEmpty(mSecondaryLockscreenWidgetsList);
        final boolean isEmpty = isMainWidgetsEmpty && isSecondaryWidgetsEmpty;

        if (mDeviceWidgetContainer != null) {
            mDeviceWidgetContainer.setVisibility(deviceWidgetsEnabled ? View.VISIBLE : View.GONE);
        }
        if (mMainWidgetsContainer != null) {
            mMainWidgetsContainer.setVisibility(isMainWidgetsEmpty ? View.GONE : View.VISIBLE);
        }
        if (mSecondaryWidgetsContainer != null) {
            mSecondaryWidgetsContainer.setVisibility(isSecondaryWidgetsEmpty ? View.GONE : View.VISIBLE);
        }
        final boolean shouldHideContainer = isEmpty || !lockscreenWidgetsEnabled;
        setVisibility(shouldHideContainer ? View.GONE : View.VISIBLE);
    }

    public void updateWidgetViews() {
        if (mMainWidgetViews != null && mMainWidgetsList != null) {
            for (int i = 0; i < mMainWidgetViews.length; i++) {
                if (mMainWidgetViews[i] != null) {
                    mMainWidgetViews[i].setVisibility(i < mMainWidgetsList.size() ? View.VISIBLE : View.GONE);
                }
            }
            for (int i = 0; i < Math.min(mMainWidgetsList.size(), mMainWidgetViews.length); i++) {
                String widgetType = mMainWidgetsList.get(i);
                if (widgetType != null && mMainWidgetViews[i] != null) {
                    setUpWidgetWiews(null, mMainWidgetViews[i], widgetType);
                    updateMainWidgetResources(mMainWidgetViews[i], false);
                }
            }
        }
        if (mSecondaryWidgetViews != null && mSecondaryWidgetsList != null) {
            for (int i = 0; i < mSecondaryWidgetViews.length; i++) {
                if (mSecondaryWidgetViews[i] != null) {
                    mSecondaryWidgetViews[i].setVisibility(i < mSecondaryWidgetsList.size() ? View.VISIBLE : View.GONE);
                }
            }
            for (int i = 0; i < Math.min(mSecondaryWidgetsList.size(), mSecondaryWidgetViews.length); i++) {
                String widgetType = mSecondaryWidgetsList.get(i);
                if (widgetType != null && mSecondaryWidgetViews[i] != null) {
                    setUpWidgetWiews(mSecondaryWidgetViews[i], null, widgetType);
                    updateWidgetsResources(mSecondaryWidgetViews[i]);
                }
            }
        }
        updateContainerVisibility();
        updateMediaController();
    }

    private void updateMainWidgetResources(ExtendedFAB efab, boolean active) {
        if (efab == null) return;
        efab.setElevation(0);
        setButtonActiveState(null, efab, false);
        ViewGroup.LayoutParams params = efab.getLayoutParams();
        if (params instanceof LayoutParams layoutParams) {
            if (efab.getVisibility() == View.VISIBLE && mMainWidgetsList.size() == 1) {
                layoutParams.width = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width);
                layoutParams.height = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height);
            } else {
                layoutParams.width = 0;
                layoutParams.weight = 1;
            }
            efab.setLayoutParams(layoutParams);
        }
    }

    private void updateWidgetsResources(ImageView iv) {
        if (iv == null) return;
        Drawable d = ResourcesCompat.getDrawable(modRes, R.drawable.lockscreen_widget_background_circle, mContext.getTheme());
        iv.setBackground(d);
        setButtonActiveState(iv, null, false);
    }

    private boolean isNightMode() {
        final Configuration config = mContext.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void setUpWidgetWiews(ImageView iv, ExtendedFAB efab, String type) {
        switch (type) {
            case "none":
                if (iv != null) {
                    iv.setVisibility(View.GONE);
                }
                if (efab != null) {
                    efab.setVisibility(View.GONE);
                }
                break;
            case "wifi":
                if (iv != null) {
                    wifiButton = iv;
                    wifiButton.setOnLongClickListener(v -> {
                        showWifiDialog(v);
                        return true;
                    });
                }
                if (efab != null) {
                    wifiButtonFab = efab;
                    wifiButtonFab.setOnLongClickListener(v -> {
                        showWifiDialog(v);
                        return true;
                    });
                }
                setUpWidgetResources(iv, efab, v -> toggleWiFi(), getDrawable(mContext, WIFI_INACTIVE, SYSTEM_UI), getString(mContext,WIFI_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "data":
                if (iv != null) {
                    dataButton = iv;
                    dataButton.setOnLongClickListener(v -> {
                        showInternetDialog(v);
                        return true;
                    });
                }
                if (efab != null) {
                    dataButtonFab = efab;
                    dataButtonFab.setOnLongClickListener(v -> {
                        showInternetDialog(v);
                        return true;
                    });
                }
                setUpWidgetResources(iv, efab, v -> toggleMobileData(), getDrawable(mContext, DATA_INACTIVE, SYSTEM_UI), getString(mContext,DATA_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "ringer":
                if (iv != null) {
                    ringerButton = iv;
                    ringerButton.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchAudioSettings(false);
                        return true;
                    });
                }
                if (efab != null) {
                    ringerButtonFab = efab;
                    ringerButtonFab.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchAudioSettings(false);
                        return true;
                    });
                }
                setUpWidgetResources(iv, efab, v -> toggleRingerMode(), getDrawable(mContext, RINGER_INACTIVE, SYSTEM_UI), getString(mContext,RINGER_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "bt":
                if (iv != null) {
                    btButton = iv;
                    btButton.setOnLongClickListener(v -> {
                        showBluetoothDialog(v);
                        return true;
                    });
                }
                if (efab != null) {
                    btButtonFab = efab;
                    btButtonFab.setOnLongClickListener(v -> {
                        showBluetoothDialog(v);
                        return true;
                    });
                }
                setUpWidgetResources(iv, efab, v -> toggleBluetoothState(), getDrawable(mContext, BT_INACTIVE, SYSTEM_UI), getString(mContext,BT_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "torch":
                if (iv != null) {
                    torchButton = iv;
                }
                if (efab != null) {
                    torchButtonFab = efab;
                }
                setUpWidgetResources(iv, efab, v -> toggleFlashlight(), getDrawable(mContext, TORCH_RES_INACTIVE, SYSTEM_UI), getString(mContext,TORCH_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "timer":
                setUpWidgetResources(iv, efab, v -> {
                    mActivityLauncherUtils.launchTimer(false);
                    vibrate(1);
                }, getDrawable(mContext, "ic_alarm", SYSTEM_UI), modRes.getString(R.string.clock_timer));
                break;
            case "camera":
                setUpWidgetResources(iv, efab, v -> {
                    mActivityLauncherUtils.launchCamera(false);
                    vibrate(1);
                }, getDrawable(mContext, CAMERA_ICON, SYSTEM_UI), getString(mContext,CAMERA_LABEL, SYSTEM_UI));
                break;
            case "calculator":
                setUpWidgetResources(iv, efab, v -> openCalculator(), getDrawable(mContext, CALCULATOR_ICON, SYSTEM_UI), getString(mContext,CALCULATOR_LABEL, SYSTEM_UI));
                break;
            case "homecontrols":
                setUpWidgetResources(iv, efab, this::launchHomeControls, getDrawable(mContext, HOME_CONTROLS, SYSTEM_UI), getString(mContext,HOME_CONTROLS_LABEL, SYSTEM_UI));
                break;
            case "wallet":
                setUpWidgetResources(iv, efab, this::launchWallet, getDrawable(mContext, WALLET_ICON, SYSTEM_UI), getString(mContext,WALLET_LABEL, SYSTEM_UI));
                break;
            case "media":
                if (iv != null) {
                    mediaButton = iv;
                    mediaButton.setOnLongClickListener(v -> {
                        showMediaDialog(v);
                        return true;
                    });
                }
                if (efab != null) {
                    mediaButtonFab = efab;
                }
                setUpWidgetResources(iv, efab, v -> toggleMediaPlaybackState(),
                        ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, mContext.getTheme()),
                        getString(mContext,MEDIA_PLAY_LABEL, SYSTEM_UI));
                break;
            case "weather":
                if (iv != null) {
                    weatherButton = iv;
                    weatherButton.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchWeatherSettings(false);
                        return true;
                    });
                }
                if (efab != null) {
                    weatherButtonFab = efab;
                    weatherButtonFab.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchWeatherSettings(false);
                        return true;
                    });
                }
                // Set a null on click listener to weather button to avoid running previous button action
                setUpWidgetResources(iv, efab, v -> mActivityLauncherUtils.launchWeatherActivity(false), ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.google_30, appContext.getTheme()), appContext.getString(R.string.weather_settings));
                enableWeatherUpdates();
                break;
            case "hotspot":
                if (iv != null) {
                    hotspotButton = iv;
                    hotspotButton.setOnLongClickListener(v -> {
                        showHotspotDialog(v);
                        return true;
                    });
                }
                if (efab != null) {
                    hotspotButtonFab = efab;
                    hotspotButtonFab.setOnLongClickListener(v -> {
                        showHotspotDialog(v);
                        return true;
                    });
                }
                setUpWidgetResources(iv, efab, v -> toggleHotspot(),
                        getDrawable(mContext, HOTSPOT_INACTIVE, SYSTEM_UI),
                        getString(mContext,HOTSPOT_LABEL, SYSTEM_UI));
            default:
                break;
        }
    }

    private void setUpWidgetResources(ImageView iv, ExtendedFAB efab,
                                      OnClickListener cl, Drawable icon, String text) {
        if (efab != null) {
            efab.setOnClickListener(cl);
            efab.setIcon(icon);
            efab.setText(text);
            if (mediaButtonFab == efab) {
                attachSwipeGesture(efab);
            }
        }
        if (iv != null) {
            iv.setOnClickListener(cl);
            iv.setImageDrawable(icon);
            if (mediaButton == iv) {
                attachSwipeGesture(iv);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void attachSwipeGesture(View view) {
        final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT);
                    } else {
                        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    }
                    vibrate(1);
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

    private void setButtonActiveState(ImageView iv, ExtendedFAB efab, boolean active) {
        int bgTint;
        int tintColor;

        if (!mCustomColors) {
            if (active) {
                bgTint = isNightMode() ? mDarkColorActive : mLightColorActive;
                tintColor = isNightMode() ? mDarkColor : mLightColor;
            } else {
                bgTint = isNightMode() ? mDarkColor : mLightColor;
                tintColor = isNightMode() ? mLightColor : mDarkColor;
            }
            if (iv != null) {
                iv.setBackgroundTintList(ColorStateList.valueOf(bgTint));
                if (iv != weatherButton) {
                    iv.setImageTintList(ColorStateList.valueOf(tintColor));
                } else {
                    iv.setImageTintList(null);
                }
            }
            if (efab != null) {
                efab.setBackgroundTintList(ColorStateList.valueOf(bgTint));
                if (efab != weatherButtonFab) {
                    efab.setIconTint(ColorStateList.valueOf(tintColor));
                } else {
                    efab.setIconTint(null);
                }
                efab.setTextColor(tintColor);
            }
        } else {
            if (iv != null) {
                iv.setBackgroundTintList(ColorStateList.valueOf(active ? mSmallActiveColor : mSmallInactiveColor));
                if (iv != weatherButton) {
                    iv.setImageTintList(ColorStateList.valueOf(active ? mSmallIconActiveColor : mSmallIconInactiveColor));
                } else {
                    iv.setImageTintList(null);
                }
            }
            if (efab != null) {
                efab.setBackgroundTintList(ColorStateList.valueOf(active ? mBigActiveColor : mBigInactiveColor));
                if (efab != weatherButtonFab) {
                    efab.setIconTint(ColorStateList.valueOf(active ? mBigIconActiveColor : mBigIconInactiveColor));
                } else {
                    efab.setIconTint(null);
                }
                efab.setTextColor(active ? mBigIconActiveColor : mBigIconInactiveColor);
            }
        }

    }

    public void updateMediaState() {
        updateMediaPlaybackState();
        mHandler.postDelayed(this::updateMediaPlaybackState, 250);
    }

    private void toggleMediaPlaybackState() {
        if (isMediaPlaying()) {
            mHandler.removeCallbacks(mMediaUpdater);
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PAUSE);
            updateMediaController();
        } else {
            mMediaUpdater.run();
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
        vibrate(1);
    }

    private void showMediaDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) return;
        updateMediaController();
        Object[] mediaQsHelper = getQsMediaDialog();
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        if (mediaQsHelper[0] == null || mediaQsHelper[1] == null) return;
        callMethod(mediaQsHelper[1], "showPrompt", mContext, finalView, mediaQsHelper[0]);
        vibrate(0);
    }

    private void dispatchMediaKeyWithWakeLockToMediaSession(final int keycode) {
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keycode, 0);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        KeyEvent mediaEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        mAudioManager.dispatchMediaKeyEvent(mediaEvent);

        mediaEvent = KeyEvent.changeAction(mediaEvent, KeyEvent.ACTION_UP);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        mAudioManager.dispatchMediaKeyEvent(mediaEvent);

    }

    private void updateMediaPlaybackState() {
        boolean isPlaying = isMediaPlaying();
        Drawable icon = ResourcesCompat.getDrawable(
                modRes,
                isPlaying ? R.drawable.ic_pause : R.drawable.ic_play,
                mContext.getTheme()
        );
        if (mediaButton != null) {
            mediaButton.setImageDrawable(icon);
            setButtonActiveState(mediaButton, null, isPlaying);
        }
        if (mediaButtonFab != null) {
            String trackTitle = mMediaMetadata != null ? mMediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE) : "";
            if (!TextUtils.isEmpty(trackTitle) && !Objects.equals(mLastTrackTitle, trackTitle)) {
                mLastTrackTitle = trackTitle;
            }
            final boolean canShowTrackTitle = isPlaying || !TextUtils.isEmpty(mLastTrackTitle);
            mediaButtonFab.setIcon(icon);
            mediaButtonFab.setText(canShowTrackTitle ? mLastTrackTitle : "Play");
            setButtonActiveState(null, mediaButtonFab, isPlaying);
        }
    }

    private boolean isMediaPlaying() {
        return isMediaControllerAvailable()
                && PlaybackState.STATE_PLAYING == getMediaControllerPlaybackState(mController);
    }

    private void toggleFlashlight() {
        if (torchButton == null && torchButtonFab == null) return;
        try {
            mCameraManager.setTorchMode(mCameraId, !isFlashOn);
            isFlashOn = !isFlashOn;
            updateTorchButtonState();
            vibrate(1);
        } catch (Exception e) {
            log("LockscreenWidgetsView toggleFlashlight error: " + e.getMessage());
        }
    }

    private void launchHomeControls(View view) {
        Object controlsTile = getControlsTile();
        if (controlsTile == null) return;
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        post(() -> callMethod(controlsTile, "handleClick", finalView));
        vibrate(1);
    }

    private void launchWallet(View view) {
        Object WalletTile = getWalletTile();
        if (WalletTile != null) {
            View finalView;
            if (view instanceof ExtendedFAB) {
                finalView = (View) view.getParent();
            } else {
                finalView = view;
            }
            post(() -> callMethod(WalletTile, "handleClick", finalView));
        } else {
            mActivityLauncherUtils.launchWallet();
        }
        vibrate(1);
    }

    private void openCalculator() {
        mActivityLauncherUtils.launchCalculator();
        vibrate(1);
    }

    private void toggleWiFi() {
        Object networkController = getNetworkController();
        boolean enabled = SystemUtils.WifiManager().isWifiEnabled();
        if (networkController != null) {
            callMethod(networkController, "setWifiEnabled", !enabled);
        } else {
            SystemUtils.WifiManager().setWifiEnabled(!enabled);
        }

        updateWiFiButtonState(!enabled);
        mHandler.postDelayed(() -> updateWiFiButtonState(isWifiEnabled()), 350L);
        vibrate(1);
    }

    private void toggleMobileData() {
        if (getDataController() == null) return;
        callMethod(getDataController(), "setMobileDataEnabled", !isMobileDataEnabled());
        updateMobileDataState(!isMobileDataEnabled());
        mHandler.postDelayed(() -> updateMobileDataState(isMobileDataEnabled()), 250L);
        vibrate(1);
    }

    private void toggleHotspot() {
        Object mHostpotController = getHotspotController();
        if (mHostpotController != null) {
            callMethod(mHostpotController, "setHotspotEnabled", !isHotspotEnabled());
        }
        updateHotspotButtonState(0);
        postDelayed(() -> updateHotspotButtonState(0), 350L);
        vibrate(1);
    }

    /**
     * Toggles the ringer modes
     * Normal -> Vibrate -> Silent -> Normal
     */
    private void toggleRingerMode() {
        Object mRingerTile = getRingerTile();

        if (mRingerTile != null) {
            callMethod(mRingerTile, "setRingMode");
        } else if (mAudioManager != null) {
            int mode = mAudioManager.getRingerMode();
            int newMode = switch (mode) {
                case AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE;
                case AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT;
                default -> AudioManager.RINGER_MODE_NORMAL;
            };

            mAudioManager.setRingerMode(newMode);
        }

        updateRingerButtonState();
        vibrate(1);
    }

    private void toggleBluetoothState() {
        Object bluetoothController = getBluetoothController();
        if (bluetoothController == null) return;
        callMethod(bluetoothController, "setBluetoothEnabled", !isBluetoothEnabled());
        updateBtState();
        mHandler.postDelayed(this::updateBtState, 350L);
        vibrate(1);
    }

    private void showWifiDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchWifiSettings();
            vibrate(0);
            return;
        }
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        post(() -> callMethod(getOplusWifiTile(), "handleSecondaryClick", finalView));
        vibrate(0);
    }

    private void showInternetDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchInternetSettings();
            vibrate(0);
            return;
        }
        if (getCellularTile() == null) return;
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        post(() -> callMethod(getCellularTile(), "handleSecondaryClick", finalView));
        vibrate(0);
    }

    private void showBluetoothDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchBluetoothSettings();
            vibrate(0);
            return;
        }
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        post(() -> callMethod(getOplusBluetoothTile(), "handleSecondaryClick", finalView));
        vibrate(0);
    }

    private void showHotspotDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) {
            mActivityLauncherUtils.launchHotspotSettings();
            vibrate(0);
            return;
        }
        if (getHotspotTile() == null) return;
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        post(() -> callMethod(getHotspotTile(), "handleSecondaryClick", finalView));
        vibrate(0);
    }

    private void updateTileButtonState(
            ImageView iv,
            ExtendedFAB efab,
            boolean active,
            Drawable icon,
            String text
    ) {
        post(() -> {
            if (iv != null) {
                iv.setImageDrawable(icon);
                setButtonActiveState(iv, null, active);
            }
            if (efab != null) {
                efab.setIcon(icon);
                efab.setText(text);
                setButtonActiveState(null, efab, active);
            }
        });
    }

    private void updateTileButtonState(
            ImageView iv,
            ExtendedFAB efab,
            boolean active,
            String activeResource,
            String inactiveResource,
            String activeString,
            String inactiveString) {
        post(() -> {

            @SuppressLint("UseCompatLoadingForDrawables") Drawable d = mContext.getDrawable(mContext.getResources().getIdentifier(active ? activeResource : inactiveResource, "drawable", SYSTEM_UI));
            if (iv != null) {
                iv.setImageDrawable(d);
                setButtonActiveState(iv, null, active);
            }
            if (efab != null) {
                efab.setIcon(d);
                efab.setText(active ? activeString : inactiveString);
                setButtonActiveState(null, efab, active);
            }
        });
    }

    public void updateTorchButtonState() {
        if (!isWidgetEnabled("torch")) return;
        String activeString = getString(mContext,TORCH_LABEL_ACTIVE, SYSTEM_UI);
        String inactiveString = getString(mContext,TORCH_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(torchButton, torchButtonFab, isFlashOn,
                TORCH_RES_ACTIVE, TORCH_RES_INACTIVE, activeString, inactiveString);
    }

    private final BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRingerButtonState();
        }
    };

    @SuppressWarnings("deprecation")
    private void updateWiFiButtonState(boolean enabled) {
        if (!isWidgetEnabled("wifi")) return;
        if (wifiButton == null && wifiButtonFab == null) return;
        String theSsid = SystemUtils.WifiManager().getConnectionInfo().getSSID();
        if (theSsid.equals(UNKNOWN_SSID)) {
            theSsid = getString(mContext,WIFI_LABEL_INACTIVE, SYSTEM_UI);
        } else {
            if (theSsid.startsWith("\"") && theSsid.endsWith("\"")) {
                theSsid = theSsid.substring(1, theSsid.length() - 1);
            }
        }
        updateTileButtonState(wifiButton, wifiButtonFab, isWifiEnabled(),
                WIFI_ACTIVE, WIFI_INACTIVE, theSsid, getString(mContext,WIFI_LABEL_INACTIVE, SYSTEM_UI));
    }

    private void updateRingerButtonState() {
        if (!isWidgetEnabled("ringer")) return;
        if (ringerButton == null && ringerButtonFab == null) return;
        if (mAudioManager != null) {
            boolean soundActive = mAudioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
            updateTileButtonState(ringerButton, ringerButtonFab,
                    soundActive,
                    getRingerDrawable(),
                    getRingerText());
        }
    }

    private void updateMobileDataState(boolean enabled) {
        if (!isWidgetEnabled("data")) return;
        if (dataButton == null && dataButtonFab == null) return;
        Object networkController = getNetworkController();
        String networkName =
                networkController == null ? "" : (String) callMethod(networkController, "getMobileDataNetworkName");
        boolean hasNetwork = networkController != null && !TextUtils.isEmpty(networkName);
        String inactive = getString(mContext,DATA_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(dataButton, dataButtonFab, enabled,
                DATA_ACTIVE, DATA_INACTIVE, hasNetwork && enabled ? networkName : inactive, inactive);
    }

    private void updateBtState() {
        if (!isWidgetEnabled("bt")) return;
        if (btButton == null && btButtonFab == null) return;
        Object bluetoothController = getBluetoothController();
        String deviceName = isBluetoothEnabled() ? (String) callMethod(bluetoothController, "getConnectedDeviceName") : "";
        boolean isConnected = !TextUtils.isEmpty(deviceName);
        String inactiveString = getString(mContext,BT_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(btButton, btButtonFab, isBluetoothEnabled(),
                BT_ACTIVE, BT_INACTIVE, isConnected ? deviceName : inactiveString, inactiveString);
    }

    private void updateHotspotButtonState(int numDevices) {
        if (!isWidgetEnabled("hotspot")) return;
        if (hotspotButton == null && hotspotButtonFab == null) return;
        String inactiveString = getString(mContext,HOTSPOT_LABEL, SYSTEM_UI);
        String activeString = getString(mContext,HOTSPOT_LABEL, SYSTEM_UI);
        if (isHotspotEnabled()) {
            String hotspotSSID = getHotspotSSID();
            String devices = "(" + numDevices + ")";
            if (!TextUtils.isEmpty(hotspotSSID)) {
                if (numDevices > 0) activeString = hotspotSSID + " " + devices;
                else activeString = hotspotSSID;
            }
        }
        updateTileButtonState(hotspotButton, hotspotButtonFab, isHotspotEnabled(),
                HOTSPOT_ACTIVE, HOTSPOT_INACTIVE, activeString, inactiveString);
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
        if (dataController != null) {
            return (boolean) callMethod(dataController, "isMobileDataEnabled");
        } else {
            try {
                Class<?> cmClass = Class.forName(SystemUtils.ConnectivityManager().getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true); // Make the method callable
                // get the setting for "mobile data"
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
        } else {
            try {
                Method method = SystemUtils.WifiManager().getClass().getDeclaredMethod("getWifiApState");
                method.setAccessible(true);
                int actualState = (Integer) method.invoke(SystemUtils.WifiManager(), (Object[]) null);
                return actualState == HOTSPOT_ENABLED;
            } catch (Throwable t) {
                log("LockscreenWidgetsView isHotspotEnabled error: " + t.getMessage());
            }
        }
        return false;
    }

    private String getHotspotSSID() {
        try {
            Method[] methods = SystemUtils.WifiManager().getClass().getDeclaredMethods();
            for (Method m : methods) {
                if (m.getName().equals("getWifiApConfiguration")) {
                    WifiConfiguration config = (WifiConfiguration) m.invoke(SystemUtils.WifiManager());
                    return config.SSID;
                }
            }
        } catch (Throwable t) {
            log("LockscreenWidgetsView getHotspotSSID error: " + t.getMessage());
        }
        return "";
    }

    private boolean sameSessions(MediaController a, MediaController b) {
        if (a == b) {
            return true;
        }
        if (a == null) {
            return false;
        }
        return a.equals(b);
    }

    private int getMediaControllerPlaybackState(MediaController controller) {
        if (controller != null) {
            final PlaybackState playbackState = controller.getPlaybackState();
            if (playbackState != null) {
                return playbackState.getState();
            }
        }
        return PlaybackState.STATE_NONE;
    }

    public static LockscreenWidgetsView getInstance(Context context, Object activityStarter) {
        if (instance != null) return instance;
        return new LockscreenWidgetsView(context, activityStarter);
    }

    public static LockscreenWidgetsView getInstance() {
        if (instance != null) return instance;
        return null;
    }

    /**
     * Set the options for the lockscreen widgets
     *
     * @param lsWidgets        true if lockscreen widgets are enabled
     * @param deviceWidget     true if device widget is enabled
     * @param mainWidgets      comma separated list of main widgets
     * @param secondaryWidgets comma separated list of secondary widgets
     */
    public void setOptions(boolean lsWidgets, boolean deviceWidget,
                           String mainWidgets, String secondaryWidgets) {
        instance.lockscreenWidgetsEnabled = lsWidgets;
        instance.deviceWidgetsEnabled = deviceWidget;
        instance.mMainLockscreenWidgetsList = mainWidgets;
        instance.mMainWidgetsList = Arrays.asList(instance.mMainLockscreenWidgetsList.split(","));
        instance.mSecondaryLockscreenWidgetsList = secondaryWidgets;
        instance.mSecondaryWidgetsList = Arrays.asList(instance.mSecondaryLockscreenWidgetsList.split(","));
        instance.updateWidgetViews();
    }

    /**
     * Set the options for the Device Widget
     *
     * @param customColor   true if custom color is enabled
     * @param linearColor   color for linear battery progressbar
     * @param circularColor color for circular progressbar
     * @param textColor     color for text
     * @param devName       device name, keep blank for default Build.MODEL
     */
    public void setDeviceWidgetOptions(boolean customColor, int linearColor, int circularColor, int textColor, String devName) {
        if (instance.mDeviceWidgetView == null) return;
        instance.mDeviceWidgetView.setCustomColor(customColor, linearColor, circularColor);
        instance.mDeviceWidgetView.setTextCustomColor(textColor);
        instance.mDeviceWidgetView.setDeviceName(devName);
    }

    public void setCustomColors(
            boolean customColorsEnabled,
            int bigInactive, int bigActive, int smallInactive, int smallActive,
            int bigIconInactive, int bigIconActive, int smallIconInactive, int smallIconActive) {
        instance.mCustomColors = customColorsEnabled;
        instance.mBigInactiveColor = bigInactive;
        instance.mBigActiveColor = bigActive;
        instance.mSmallInactiveColor = smallInactive;
        instance.mSmallActiveColor = smallActive;
        instance.mBigIconInactiveColor = bigIconInactive;
        instance.mBigIconActiveColor = bigIconActive;
        instance.mSmallIconInactiveColor = smallIconInactive;
        instance.mSmallIconActiveColor = smallIconActive;
        instance.updateWidgetViews();
    }

    public void setScale(float scale) {
        instance.mWidgetsScale = scale;
        instance.removeAllViews();
        instance.drawUI();
        instance.updateWidgetViews();
    }

    public void setActivityStarter(Object activityStarter) {
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, activityStarter);
    }

    private Drawable getRingerDrawable() {
        String resName = switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL -> RINGER_NORMAL;
            case AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE;
            case AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT;
            default ->
                    throw new IllegalStateException("Unexpected value: " + mAudioManager.getRingerMode());
        };

        return getDrawable(mContext, resName, SYSTEM_UI);
    }

    private String getRingerText() {
        String RINGER_NORMAL = "volume_footer_ring";
        String RINGER_VIBRATE = "state_button_vibration";
        String RINGER_SILENT = "state_button_silence";

        String resName = switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL -> RINGER_NORMAL;
            case AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE;
            case AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT;
            default ->
                    throw new IllegalStateException("Unexpected value: " + mAudioManager.getRingerMode());
        };

        return getString(mContext,resName, SYSTEM_UI);

    }

    /**
     * Vibrate the device
     *
     * @param type 0 = Long Press, 1 = Click
     */
    private void vibrate(int type) {
        if (type == 0) {
            this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else if (type == 1) {
            this.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }
    }

}
