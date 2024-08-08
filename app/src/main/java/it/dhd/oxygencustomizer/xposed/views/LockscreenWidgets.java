package it.dhd.oxygencustomizer.xposed.views;


import static android.net.wifi.WifiManager.UNKNOWN_SSID;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getBluetoothController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getCalculatorTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getCellularTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getControlsTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getDataController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getNetworkController;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getOplusBluetoothTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getOplusWifiTile;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getQsMediaDialog;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.LaunchableImageView;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.LaunchableLinearLayout;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

@SuppressLint("ViewConstructor")
public class LockscreenWidgets extends LinearLayout implements OmniJawsClient.OmniJawsObserver {

    public static LockscreenWidgets instance = null;

    public static final String BT_ACTIVE = "status_bar_qs_bluetooth_active";
    public static final String BT_INACTIVE = "status_bar_qs_bluetooth_inactive";
    public static final String DATA_ACTIVE = "status_bar_qs_data_active";
    public static final String DATA_INACTIVE = "status_bar_qs_data_inactive";
    public static final String RINGER_ACTIVE = "status_bar_qs_mute_inactive";
    public static final String RINGER_NORMAL = "status_bar_qs_mute_inactive";
    public static final String RINGER_VIBRATE = "status_bar_qs_icon_volume_ringer_vibrate";
    public static final String RINGER_SILENT = "status_bar_qs_mute_active";
    public static final String RINGER_INACTIVE = "status_bar_qs_mute_active";
    public static final String TORCH_RES_ACTIVE = "status_bar_qs_flashlight_active";
    public static final String TORCH_RES_INACTIVE = "status_bar_qs_flashlight_inactive";
    public static final String WIFI_ACTIVE = "status_bar_qs_wifi_active";
    public static final String WIFI_INACTIVE = "status_bar_qs_wifi_inactive";
    public static final String HOME_CONTROLS = "controls_icon";
    public static final String CALCULATOR_ICON = "status_bar_qs_calculator_inactive";

    public static final String GENERAL_INACTIVE = "switch_bar_off";
    public static final String GENERAL_ACTIVE = "switch_bar_on";

    public static final String BT_LABEL_INACTIVE = "quick_settings_bluetooth_label";
    public static final String DATA_LABEL_INACTIVE = "mobile_data_settings_title";
    public static final String DATA_LABEL_ACTIVE = "mobile_data_connection_active";
    public static final String RINGER_LABEL_ACTIVE = "";
    public static final String RINGER_LABEL_INACTIVE = "state_button_silence";
    public static final String TORCH_LABEL_ACTIVE = "notification_flashlight_hasopen";
    public static final String TORCH_LABEL_INACTIVE = "notification_flashlight_hasclose";
    public static final String WIFI_LABEL_INACTIVE = "quick_settings_wifi_label";
    public static final String HOME_CONTROLS_LABEL = "quick_controls_title";
    public static final String MEDIA_PLAY_LABEL = "controls_media_button_play";
    public static final String CALCULATOR_LABEL = "state_button_calculator";

    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    private Context mContext;

    // Two Linear Layouts, one for main widgets and one for secondary widgets
    private final LinearLayout mDeviceWidgetContainer;
    private final LinearLayout mMainWidgetsContainer;
    private final LinearLayout mSecondaryWidgetsContainer;
    private DeviceWidgetView mDeviceWidgetView;

    private ImageView mediaButton, torchButton, weatherButton;
    private ExtendedFAB mediaButtonFab, torchButtonFab, weatherButtonFab;
    private ExtendedFAB wifiButtonFab, dataButtonFab, ringerButtonFab, btButtonFab;
    private ImageView wifiButton, dataButton, ringerButton, btButton;
    private final int mDarkColor;
    private final int mDarkColorActive;
    private final int mLightColor;
    private final int mLightColorActive;

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

    private boolean isBluetoothOn = false;

    private boolean mIsInflated = false;
    private boolean mIsLongPress = false;

    private CameraManager mCameraManager;
    private String mCameraId;
    private boolean isFlashOn = false;

    private int mAudioMode;
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

    public LockscreenWidgets(Context context, Object activityStarter) {
        super(context);

        instance = this;

        mContext = context;
        mAudioManager = SystemUtils.AudioManager();
        mCameraManager = SystemUtils.CameraManager();
        mDarkColor = ResourcesCompat.getColor(modRes, R.color.lockscreen_widget_background_color_dark, mContext.getTheme());
        mLightColor = ResourcesCompat.getColor(modRes, R.color.lockscreen_widget_background_color_light, mContext.getTheme());
        mDarkColorActive = ResourcesCompat.getColor(modRes, R.color.lockscreen_widget_active_color_dark, mContext.getTheme());
        mLightColorActive = ResourcesCompat.getColor(modRes, R.color.lockscreen_widget_active_color_light, mContext.getTheme());

        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, activityStarter);

        mHandler = new Handler(Looper.getMainLooper());

        if (mWeatherClient == null) {
            mWeatherClient = new OmniJawsClient(context, true);
        }
        try {
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (Throwable e) {
            log("LockscreenWidgets error: " + e.getMessage());
        }

        LinearLayout container = new LinearLayout(context);
        container.setOrientation(VERTICAL);
        container.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        // Device Widget Container
        mDeviceWidgetContainer = createDeviceWidgetContainer(context);
        container.addView(mDeviceWidgetContainer);

        // Add main widgets container
        mMainWidgetsContainer = createMainWidgetsContainer(context);
        container.addView(mMainWidgetsContainer);

        // Add secondary widgets container
        mSecondaryWidgetsContainer = createSecondaryWidgetsContainer(context);
        container.addView(mSecondaryWidgetsContainer);

        addView(container);

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

        addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                onVisible();
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {}
        });
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
        log("LockscreenWidgets onWifiChanged");
        Object qsIcon = getObjectField(mWifiTracker, "qsIcon");
        log("LockscreenWidgets onWifiChanged qsIcon " + (qsIcon != null));
        if (qsIcon == null) {
            updateWiFiButtonState(false);
            return;
        }
        boolean enabled = getBooleanField(mWifiTracker, "enabled");
        updateWiFiButtonState(isWifiEnabled());
    }

    private void onBluetoothChanged(boolean enabled) {
        log("LockscreenWidgets onBluetoothChanged " + enabled);
        isBluetoothOn = enabled;
        updateBtState();
    }

    private void onTorchChanged(boolean enabled) {
        log("LockscreenWidgets onTorchChanged " + enabled);
        isFlashOn = enabled;
        updateTorchButtonState();
    }

    private LinearLayout createDeviceWidgetContainer(Context context) {
        LinearLayout deviceWidget = new LinearLayout(context);
        deviceWidget.setOrientation(HORIZONTAL);
        deviceWidget.setGravity(Gravity.CENTER);
        deviceWidget.setLayoutParams(new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        mDeviceWidgetView = new DeviceWidgetView(context);

        deviceWidget.addView(mDeviceWidgetView);

        return deviceWidget;
    }

    private LinearLayout createMainWidgetsContainer(Context context) {
        LinearLayout mainWidgetsContainer;
        try {
            mainWidgetsContainer = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);

        } catch (NoSuchMethodException | IllegalAccessException | IllegalStateException |
                 InvocationTargetException | InstantiationException e) {
            log("LockscreenWidgets createMainWidgetsContainer LaunchableLinearLayout not found: " + e.getMessage());
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
                modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width),
                modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height));
        params.setMargins(
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_start),
                0,
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_main_margin_end),
                0);
        fab.setLayoutParams(params);
        fab.setPadding(
                modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding),
                modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding),
                modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding),
                modRes.getDimensionPixelSize(R.dimen.kg_main_widgets_icon_padding));
        fab.setGravity(Gravity.CENTER);
        return fab;
    }

    private LinearLayout createSecondaryWidgetsContainer(Context context) {
        LinearLayout secondaryWidgetsContainer;
        try {
            secondaryWidgetsContainer = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);

        } catch (NoSuchMethodException | IllegalAccessException | IllegalStateException |
                 InvocationTargetException | InstantiationException e) {
            log("LockscreenWidgets createMainWidgetsContainer LaunchableLinearLayout not found: " + e.getMessage());
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

        } catch (NoSuchMethodException | IllegalAccessException | IllegalStateException |
                 InvocationTargetException | InstantiationException e) {
            log("LockscreenWidgets createImageView LaunchableImageView not found: " + e.getMessage());
            imageView = new ImageView(context);
        }

        imageView.setId(View.generateViewId());
        LayoutParams params = new LayoutParams(
                modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size),
                modRes.getDimensionPixelSize(R.dimen.kg_widget_circle_size));
        params.setMargins(
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal),
                0,
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_margin_horizontal),
                0);
        imageView.setLayoutParams(params);
        imageView.setPadding(
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding),
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding),
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding),
                modRes.getDimensionPixelSize(R.dimen.kg_widgets_icon_padding));
        imageView.setFocusable(true);
        imageView.setClickable(true);

        return imageView;
    }

    public void enableWeatherUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this);
            queryAndUpdateWeather();
        }
    }

    public void disableWeatherUpdates() {
        if (mWeatherClient != null) {
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

    private void queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null || !mWeatherClient.isOmniJawsEnabled()) {
                return;
            }
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo != null) {
                // OpenWeatherMap
                String formattedCondition = mWeatherInfo.condition;
                if (formattedCondition.toLowerCase().contains("clouds")) {
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

                // MET Norway
                if (formattedCondition.toLowerCase().contains("_")) {
                    final String[] words = formattedCondition.split("_");
                    final StringBuilder formattedConditionBuilder = new StringBuilder();
                    for (String word : words) {
                        final String capitalizedWord = word.substring(0, 1).toUpperCase() + word.substring(1);
                        formattedConditionBuilder.append(capitalizedWord).append(" ");
                    }
                    formattedCondition = formattedConditionBuilder.toString().trim();
                }

                final Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
                if (weatherButtonFab != null) {
                    weatherButtonFab.setIcon(d);
                    weatherButtonFab.setText(mWeatherInfo.temp + mWeatherInfo.tempUnits + " \u2022 " + formattedCondition);
                    weatherButtonFab.setIconTint(null);
                }
                if (weatherButton != null) {
                    weatherButton.setImageDrawable(d);
                    weatherButton.setImageTintList(null);
                }
            }
        } catch(Exception e) {
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
        log("LockscreenWidgets onVisible");
        updateTorchButtonState();
        updateRingerButtonState();
        updateWiFiButtonState(isWifiEnabled());
        updateMobileDataState(isMobileDataEnabled());
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        log("LockscreenWidgets onAttachedToWindow");
        onVisible();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        log("LockscreenWidgets onDetachedFromWindow");
        if (isWidgetEnabled("weather")) {
            disableWeatherUpdates();
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        log("LockscreenWidgets onFinishInflate");
        mIsInflated = true;
        updateWidgetViews();
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
        log("LockscreenWidgets updateWidgetViews lockscreenWidgetsEnabled " + lockscreenWidgetsEnabled);

        if (mMainWidgetViews != null && mMainWidgetsList != null) {
            for (int i = 0; i < mMainWidgetViews.length; i++) {
                if (mMainWidgetViews[i] != null) {
                    mMainWidgetViews[i].setVisibility(i < mMainWidgetsList.size() ? View.VISIBLE : View.GONE);
                }
            }
            for (int i = 0; i < Math.min(mMainWidgetsList.size(), mMainWidgetViews.length); i++) {
                String widgetType = mMainWidgetsList.get(i);
                if (widgetType != null && i < mMainWidgetViews.length && mMainWidgetViews[i] != null) {
                    log("LockscreenWidgets updateWidgetViews mMainWidgetsList " + widgetType);
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
                if (widgetType != null && i < mSecondaryWidgetViews.length && mSecondaryWidgetViews[i] != null) {
                    log("LockscreenWidgets updateWidgetViews mSecondaryWidgetsList " + widgetType);
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
        if (params instanceof LayoutParams) {
            LayoutParams layoutParams = (LayoutParams) params;
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
                    wifiButton.setOnLongClickListener(v -> { showWifiDialog(v); return true; });
                }
                if (efab != null) {
                    wifiButtonFab = efab;
                    wifiButtonFab.setOnLongClickListener(v -> { showWifiDialog(v); return true; });
                }
                setUpWidgetResources(iv, efab, v -> toggleWiFi(), WIFI_INACTIVE, "wifi_Connected");
                break;
            case "data":
                if (iv != null) {
                    dataButton = iv;
                    dataButton.setOnLongClickListener(v -> { showInternetDialog(v); return true; });
                }
                if (efab != null) {
                    dataButtonFab = efab;
                    dataButtonFab.setOnLongClickListener(v -> { showInternetDialog(v); return true; });
                }
                setUpWidgetResources(iv, efab, v -> toggleMobileData(), DATA_INACTIVE, DATA_LABEL_INACTIVE);
                break;
            case "ringer":
                if (iv != null) {
                    ringerButton = iv;
                    ringerButton.setOnLongClickListener(v -> { mActivityLauncherUtils.launchAudioSettings(); return true; });
                }
                if (efab != null) {
                    ringerButtonFab = efab;
                    ringerButtonFab.setOnLongClickListener(v -> { mActivityLauncherUtils.launchAudioSettings(); return true; });
                }
                setUpWidgetResources(iv, efab, v -> toggleRingerMode(), RINGER_INACTIVE, RINGER_LABEL_INACTIVE);
                break;
            case "bt":
                if (iv != null) {
                    btButton = iv;
                    btButton.setOnLongClickListener(v -> { showBluetoothDialog(v); return true; });
                }
                if (efab != null) {
                    btButtonFab = efab;
                    btButtonFab.setOnLongClickListener(v -> { showBluetoothDialog(v); return true; });
                }
                setUpWidgetResources(iv, efab, v -> toggleBluetoothState(), BT_INACTIVE, BT_LABEL_INACTIVE);
                break;
            case "torch":
                if (iv != null) {
                    torchButton = iv;
                }
                if (efab != null) {
                    torchButtonFab = efab;
                }
                setUpWidgetResources(iv, efab, v -> toggleFlashlight(), TORCH_RES_INACTIVE, TORCH_LABEL_INACTIVE);
                break;
            case "timer":
                setUpWidgetResources(iv, efab, v -> {
                    mActivityLauncherUtils.launchTimer();
                    vibrate(1);
                }, getDrawable("ic_alarm", SYSTEM_UI), modRes.getString(R.string.clock_timer));
                break;
            case "calculator":
                setUpWidgetResources(iv, efab, v -> openCalculator(), getDrawable(CALCULATOR_ICON, SYSTEM_UI), getString(CALCULATOR_LABEL, SYSTEM_UI));
                break;
            case "homecontrols":
                setUpWidgetResources(iv, efab, this::launchHomeControls, HOME_CONTROLS, HOME_CONTROLS_LABEL);
                break;
            case "media":
                if (iv != null) {
                    mediaButton = iv;
                    mediaButton.setOnLongClickListener(v -> { showMediaDialog(v); return true; });
                }
                if (efab != null) {
                    mediaButtonFab = efab;
                }
                setUpWidgetResources(iv, efab, v -> toggleMediaPlaybackState(),
                        ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, mContext.getTheme()),
                        getString(MEDIA_PLAY_LABEL, SYSTEM_UI));
                break;
            case "weather":
                if (iv != null) {
                    weatherButton = iv;
                }
                if (efab != null) {
                    weatherButtonFab = efab;
                }
                //setUpWidgetResources(iv, efab, v -> mActivityLauncherUtils.launchWeatherApp(), "ic_alarm", R.string.weather_data_unavailable);
                enableWeatherUpdates();
                break;
            default:
                break;
        }
    }


    private void setUpWidgetResources(ImageView iv, ExtendedFAB efab,
                                      OnClickListener cl, String drawableRes, String stringRes){
        Drawable d = getDrawable(drawableRes, SYSTEM_UI);
        if (efab != null) {
            efab.setOnClickListener(cl);
            efab.setIcon(d);
            String text = mContext.getResources().getString(mContext.getResources().getIdentifier(stringRes, "string", SYSTEM_UI));
            efab.setText(text);
            if (mediaButtonFab == efab) {
                attachSwipeGesture(efab);
            }
        }
        if (iv != null) {
            iv.setOnClickListener(cl);
            iv.setImageDrawable(d);
        }
    }

    private void setUpWidgetResources(ImageView iv, ExtendedFAB efab,
                                      OnClickListener cl, Drawable icon, String text){
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
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void attachSwipeGesture(ExtendedFAB efab) {
        final GestureDetector gestureDetector = new GestureDetector(mContext, new GestureDetector.SimpleOnGestureListener() {
            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                    } else {
                        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT);
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
                showMediaDialog(efab);
                mHandler.postDelayed(() -> mIsLongPress = false, 2500);
            }
        });
        efab.setOnTouchListener((v, event) -> {
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
    }

    private void showMediaDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) return; // OOS 13
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
            if (!TextUtils.isEmpty(trackTitle) && mLastTrackTitle != trackTitle) {
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
            log("LockscreenWidgets toggleFlashlight error: " + e.getMessage());
        }
    }

    private void launchHomeControls(View view) {
        log("LockscreenWidgets launchHomeControls");
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

    private void openCalculator() {
        Object calculatorTile = getCalculatorTile();
        if (calculatorTile == null) mActivityLauncherUtils.launchCalculator();
        else post(() -> callMethod(calculatorTile, "openCalculator"));
        vibrate(1);
    }

    private void toggleWiFi() {
        Object networkController = getNetworkController();
        if (networkController == null) {
            log("LockscreenWidgets toggleWiFi networkController is null");
            return;
        }
        boolean enabled = SystemUtils.WifiManager().isWifiEnabled();
        callMethod(networkController, "setWifiEnabled", !enabled);
        //SystemUtils.WifiManager().setWifiEnabled(!SystemUtils.WifiManager().isWifiEnabled());
        updateWiFiButtonState(!enabled);
        mHandler.postDelayed(() -> updateWiFiButtonState(isWifiEnabled()), 350L);
        vibrate(1);
    }

    private boolean isMobileDataEnabled() {
        Object dataController = getDataController();
        log("LockscreenWidgets isMobileDataEnabled (dataController == null) " + (dataController == null));
        if (dataController != null) {
            return (boolean) callMethod(dataController, "isMobileDataEnabled");
        } else {
            try {
                Class cmClass = Class.forName(SystemUtils.ConnectivityManager().getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true); // Make the method callable
                // get the setting for "mobile data"
                return (Boolean)method.invoke(SystemUtils.ConnectivityManager());
            } catch (Exception e) {
                return false;
            }
        }
    }

    private boolean isWifiEnabled() {
        boolean enabled = SystemUtils.WifiManager().isWifiEnabled();
        log("LockscreenWidgets isWifiEnabled " + enabled);
        return enabled;
    }

    private void toggleMobileData() {
        if (getDataController() == null) return;
        callMethod(getDataController(), "setMobileDataEnabled", !isMobileDataEnabled());
        updateMobileDataState(!isMobileDataEnabled());
        mHandler.postDelayed(() -> updateMobileDataState(isMobileDataEnabled()), 250L);
        vibrate(1);
    }


    private void showWifiDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) return; // OOS 13
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
        if (Build.VERSION.SDK_INT == 33) return; // OOS 13
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

    /**
     * Toggles the ringer modes
     * Normal -> Vibrate -> Silent -> Normal
     */
    private void toggleRingerMode() {
        if (mAudioManager != null) {
            int mode = mAudioManager.getRingerMode();
            switch (mode) {
                case AudioManager.RINGER_MODE_NORMAL:
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
                    break;
                case AudioManager.RINGER_MODE_SILENT:
                    mAudioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                    break;
            }
            updateRingerButtonState();
            vibrate(1);
        }
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
        log("LockscreenWidgets updateTorchButtonState " + isFlashOn);
        String activeString = getString(TORCH_LABEL_ACTIVE, SYSTEM_UI);
        String inactiveString = getString(TORCH_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(torchButton, torchButtonFab, isFlashOn,
                TORCH_RES_ACTIVE, TORCH_RES_INACTIVE, activeString, inactiveString);
    }

    private final BroadcastReceiver mRingerModeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateRingerButtonState();
        }
    };

    private void updateWiFiButtonState(boolean enabled) {
        log("LockscreenWidgets updateWiFiButtonState " + enabled + " | " + isWidgetEnabled("wifi"));
        if (!isWidgetEnabled("wifi")) return;
        if (wifiButton == null && wifiButtonFab == null) return;
        String theSsid = SystemUtils.WifiManager().getConnectionInfo().getSSID();
        if (theSsid.equals(UNKNOWN_SSID)) {
            theSsid = getString(WIFI_LABEL_INACTIVE, SYSTEM_UI);
        } else {
            if (theSsid.startsWith("\"") && theSsid.endsWith("\"")) {
                theSsid = theSsid.substring(1, theSsid.length() - 1);
            }
        }
        updateTileButtonState(wifiButton, wifiButtonFab, isWifiEnabled(),
                WIFI_ACTIVE, WIFI_INACTIVE, theSsid, getString(WIFI_LABEL_INACTIVE, SYSTEM_UI));
    }

    private void updateRingerButtonState() {
        log("LockscreenWidgets updateRingerButtonState " + (isWidgetEnabled("ringer")) + " | " + (ringerButton == null) + " | " + (ringerButtonFab == null));
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
        String inactive = getString(DATA_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(dataButton, dataButtonFab, enabled,
                DATA_ACTIVE, DATA_INACTIVE, hasNetwork && enabled ? networkName : inactive, inactive);
    }

    private void toggleBluetoothState() {
        Object bluetoothController = getBluetoothController();
        if (bluetoothController == null) return;
        callMethod(bluetoothController, "setBluetoothEnabled", !isBluetoothEnabled());
        updateBtState();
        mHandler.postDelayed(this::updateBtState, 350L);
        vibrate(1);
    }

    private void showBluetoothDialog(View view) {
        View finalView;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        post(() -> callMethod(getOplusBluetoothTile(), "handleSecondaryClick", finalView));
        vibrate(0);
    }

    private void updateBtState() {
        if (!isWidgetEnabled("bt")) return;
        log("LockscreenWidgets updateBtState " + isBluetoothOn);
        if (btButton == null && btButtonFab == null) return;
        Object bluetoothController = getBluetoothController();
        String deviceName = isBluetoothEnabled() ? (String) callMethod(bluetoothController, "getConnectedDeviceName") : "";
        boolean isConnected = !TextUtils.isEmpty(deviceName);
        String inactiveString = getString(BT_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(btButton, btButtonFab, isBluetoothOn,
                BT_ACTIVE, BT_INACTIVE, isConnected ? deviceName : inactiveString, inactiveString);
    }

    private boolean isBluetoothEnabled() {
        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
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

    public static LockscreenWidgets getInstance(Context context, Object activityStarter) {
        if (instance != null) return instance;
        return new LockscreenWidgets(context, activityStarter);
    }

    public static LockscreenWidgets getInstance() {
        if (instance != null) return instance;
        return null;
    }

    /**
     * Set the options for the lockscreen widgets
     * @param lsWidgets true if lockscreen widgets are enabled
     * @param deviceWidget true if device widget is enabled
     * @param mainWidgets comma separated list of main widgets
     * @param secondaryWidgets comma separated list of secondary widgets
     */
    public void setOptions(boolean lsWidgets, boolean deviceWidget,
                           String mainWidgets, String secondaryWidgets) {
        log("LockscreenWidgets setOptions " + lsWidgets +
                " | " + deviceWidget + " | " + mainWidgets + " | " + secondaryWidgets);
        instance.lockscreenWidgetsEnabled = lsWidgets;
        instance.deviceWidgetsEnabled = deviceWidget;
        instance.mMainLockscreenWidgetsList = mainWidgets;
        instance.mMainWidgetsList = Arrays.asList(instance.mMainLockscreenWidgetsList.split(","));
        instance.mSecondaryLockscreenWidgetsList = secondaryWidgets;
        instance.mSecondaryWidgetsList = Arrays.asList(instance.mSecondaryLockscreenWidgetsList.split(","));
        instance.updateWidgetViews();
    }

    public void setDeviceWidgetOptions(boolean customColor, int linearColor, int progressColor, int textColor, String devName) {
        if (instance.mDeviceWidgetView == null) return;
        instance.mDeviceWidgetView.setCustomColor(customColor, linearColor, progressColor);
        instance.mDeviceWidgetView.setTextCustomColor(textColor);
        instance.mDeviceWidgetView.setDeviceName(devName);
    }

    public void setActivityStarter(Object activityStarter) {
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, activityStarter);
    }

    private Drawable getDrawable(String drawableRes, String pkg) {
        try {
            return ContextCompat.getDrawable(
                    mContext,
                    mContext.getResources().getIdentifier(drawableRes, "drawable", pkg));
        } catch (Throwable t) {
            log("LockscreenWidgets getDrawable " + drawableRes + " from " + pkg + " error " + t);
            return null;
        }
    }

    private String getString(String stringRes, String pkg) {
        try {
            return mContext.getResources().getString(
                    mContext.getResources().getIdentifier(stringRes, "string", pkg));
        } catch (Throwable t) {
            log("LockscreenWidgets getString " + stringRes + " from " + pkg + " error " + t);
            return "";
        }
    }

    private Drawable getRingerDrawable() {
        String resName = switch (mAudioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL -> RINGER_NORMAL;
            case AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE;
            case AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT;
            default ->
                    throw new IllegalStateException("Unexpected value: " + mAudioManager.getRingerMode());
        };

        return getDrawable(resName, SYSTEM_UI);
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

        return getString(resName, SYSTEM_UI);

    }

    /**
     * Vibrate the device
     * @param type 0 = click, 1 = tick
     */
    private void vibrate(int type) {
        if (type == 0) {
            this.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        } else if (type == 1) {
            this.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
        }
    }

}
