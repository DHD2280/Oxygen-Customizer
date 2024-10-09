package it.dhd.oxygencustomizer.xposed.views;

import static android.net.wifi.WifiManager.UNKNOWN_SSID;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
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
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.QsColorUtil;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getTileActiveColor;
import static it.dhd.oxygencustomizer.xposed.utils.QsTileHelper.getLastShape;
import static it.dhd.oxygencustomizer.xposed.utils.QsTileHelper.getShapeForHighlightTile;
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
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.wifi.WifiConfiguration;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QsWidgets;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.QsTileHelper;
import it.dhd.oxygencustomizer.xposed.utils.QsTileTouchAnim;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

@SuppressLint("ViewConstructor")
public class QsControlsView extends LinearLayout implements OmniJawsClient.OmniJawsObserver {

    @SuppressLint("StaticFieldLeak")
    public static QsControlsView instance = null;

    private static final String TAG = "QsControlsView: ";

    private static final int QS_BG_INACTIVE = Color.parseColor("#CCFFFFFF");
    private static final int QS_BG_INACTIVE_DARK = Color.parseColor("#33FFFFFF");
    private static final int ICON_LIGHT_COLOR = Color.parseColor("#99000000");
    private static final int ICON_DARK_COLOR = Color.parseColor("#B3FFFFFF");

    private final Context mContext;
    private Context appContext;

    private LinearLayout mContainer;

    private final ViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private final List<View> mPages = new ArrayList<>();
    private String mWidgets;
    private final Handler mHandler;

    private int mPhotoRadius = 22;

    private ActivityLauncherUtils mActivityLauncherUtils;

    private final CameraManager mCameraManager;
    private String mCameraId;
    private boolean isFlashOn = false;

    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    // Widgets View
    private ImageView torchButton, hotspotButton;
    private ExtendedFAB torchButtonFab, hotspotButtonFab;
    private ExtendedFAB wifiButtonFab, dataButtonFab, ringerButtonFab, btButtonFab, weatherButtonFab;
    private ImageView wifiButton, dataButton, ringerButton, btButton, weatherButton;

    private Drawable mDefaultBackground = null;

    // Colors
    private boolean mCustomInactive = false;
    private int mCustomInactiveColor;
    private boolean mCustomActive = false;
    private int mCustomActiveColor;
    private int mInactiveColor;
    private int mIconInactiveColor;
    private int mActiveColor;
    private int mIconActiveColor;

    // Radius
    private boolean mCustomFabRadius = false;
    private float[] mFabRadius = new float[8];
    private boolean mCustomTileRadius = false;
    private float[] mTileRadius = new float[8];

    // Our Views
    private QsMediaTile mMediaPlayer; // Use own media player

    public QsControlsView(@NonNull Context context) {
        super(context);

        instance = this;

        mContext = context;
        mHandler = new Handler(Looper.getMainLooper());
        mCameraManager = SystemUtils.CameraManager();
        try {
            //noinspection NullPointerException
            mCameraId = mCameraManager.getCameraIdList()[0];
        } catch (Throwable e) {
            log(TAG + "error: " + e.getMessage());
        }
        if (mWeatherClient == null) {
            mWeatherClient = new OmniJawsClient(context);
        }

        setId(generateViewId());
        loadColors();

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));

        instance.mActivityLauncherUtils = new ActivityLauncherUtils(mContext, QsWidgets.mActivityStarter);

        try {
            mContainer = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            mContainer = new LinearLayout(context);
        }
        mContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        mViewPager = new ViewPager(mContext);
        mViewPager.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        mContainer.addView(mViewPager);
        addView(mContainer);

        if (mMediaPlayer == null) {
            mMediaPlayer = new QsMediaTile(mContext);
        }

        collectViews(mPages, mMediaPlayer);
        setupViewPager();

        ControllersProvider.registerMobileDataCallback(mMobileDataCallback);
        ControllersProvider.registerWifiCallback(this::onWifiChanged);
        ControllersProvider.registerBluetoothCallback(this::onBluetoothChanged);
        ControllersProvider.registerTorchModeCallback(this::onTorchChanged);
        ControllersProvider.registerHotspotCallback(this::onHotspotChanged);
        ThemeEnabler.registerThemeChangedListener(() -> {
            loadColors();
            updateWidgetsState();
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
            log(TAG + "queryAndUpdateWeather error: " + e.getMessage());
        }
    }

    private void collectViews(List<View> viewList, View... views) {
        for (View view : views) {
            try {
                ((ViewGroup) view.getParent()).removeView(view);
            } catch (Throwable ignored) {
            }
            if (view != null && !viewList.contains(view)) {
                if (view == mMediaPlayer) {
                    view.setOnLongClickListener(v -> {
                        showMediaDialog(mContainer);
                        return true;
                    });
                }
                if (view instanceof QsPhotoShowcaseContainer) {
                    ((QsPhotoShowcaseContainer) view).setRadius(mPhotoRadius);
                } else if (view instanceof QsWeatherWidget weatherWidget) {
                    weatherWidget.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchWeatherActivity(true);
                        return true;
                    });
                }
                viewList.add(view);
            }
        }
    }

    private void setupViewPager() {
        mPagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mPages.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view = mPages.get(position);
                Log.d("QsControlsView", "instantiateItem: " + position + " " + (view != null));
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        };
        setupSingleViewPager(mViewPager, mPagerAdapter, new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mPages.size(); i++) {
                    if (i == position) {
                        mPages.get(i).setVisibility(VISIBLE);
                    } else {
                        mPages.get(i).setVisibility(GONE);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void setupSingleViewPager(ViewPager viewPager, PagerAdapter pagerAdapter, ViewPager.OnPageChangeListener listener) {
        if (viewPager != null) {
            viewPager.setAdapter(null);
            viewPager.setAdapter(pagerAdapter);
            if (mPages.size() >= 2) viewPager.setCurrentItem(1);
            viewPager.setCurrentItem(0);
            viewPager.addOnPageChangeListener(listener);
        }
    }

    private void loadColors() {
        try {
            appContext = mContext.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (Exception ignored) {
        }
        mActiveColor = getTileActiveColor(mContext);
        try {
            mInactiveColor = (int) callStaticMethod(QsColorUtil, "obtainColorForQsPanelBackground", mContext);
        } catch (Throwable t) {
            mInactiveColor = isNightMode() ? QS_BG_INACTIVE_DARK : QS_BG_INACTIVE;
        }

        try {
            if ((boolean) callStaticMethod(QsColorUtil, "isIconNeedUseLightColor", mContext)) {
                mIconInactiveColor = getStaticIntField(QsColorUtil, "BRIGHTNESS_ICON_BG_LIGHT_COLOR");
            } else {
                mIconInactiveColor = ResourcesCompat.getColor(
                        mContext.getResources(),
                        mContext.getResources().getIdentifier("status_bar_qs_tile_icon_color_inactive", "color", SYSTEM_UI),
                        appContext.getTheme()
                );
            }
        } catch (Throwable t) {
            mIconInactiveColor = isNightMode() ? ICON_LIGHT_COLOR : ICON_DARK_COLOR;
        }
        mIconActiveColor = ResourcesCompat.getColor(
                mContext.getResources(),
                mContext.getResources().getIdentifier("status_bar_qs_tile_icon_color_active", "color", SYSTEM_UI),
                appContext.getTheme()
        );
    }

    private boolean isNightMode() {
        final Configuration config = mContext.getResources().getConfiguration();
        return (config.uiMode & Configuration.UI_MODE_NIGHT_MASK)
                == Configuration.UI_MODE_NIGHT_YES;
    }

    private void showMediaDialog(View view) {
        if (Build.VERSION.SDK_INT == 33) return;
        Object[] mediaQsHelper = getQsMediaDialog();
        if (mediaQsHelper[0] == null || mediaQsHelper[1] == null) return;
        callMethod(mediaQsHelper[1], "showPrompt", mContext, view, mediaQsHelper[0]);
        vibrate(0);
    }

    private void resetButtons() {
        wifiButton = null;
        dataButton = null;
        ringerButton = null;
        btButton = null;
        weatherButton = null;
        wifiButtonFab = null;
        dataButtonFab = null;
        ringerButtonFab = null;
        btButtonFab = null;
        weatherButtonFab = null;
        torchButton = null;
        hotspotButton = null;
        torchButtonFab = null;
        hotspotButtonFab = null;
    }

    private void setupWidgets() {

        mPages.clear();
        mPagerAdapter.notifyDataSetChanged();
        List<View> views = new ArrayList<>();
        List<List<String>> orderedGroups = splitString(mWidgets);
        resetButtons();
        for (List<String> group : orderedGroups) {
            if (group.size() == 1 && !group.get(0).contains(":")) {
                String s = group.get(0);
                switch (s) {
                    case "media" -> views.add(mMediaPlayer);
                    case "weather" -> views.add(new QsWeatherWidget(mContext));
                    case "photo" -> views.add(new QsPhotoShowcaseContainer(mContext));
                }
            } else {
                try {
                    views.add(createGroupView(mContext, group));
                } catch (Throwable t) {
                    log(TAG + "setupWidgets: error creating group view: " + t.getMessage());
                }
            }
        }
        if (mWidgets.contains("w:weather")) {
            enableWeatherUpdates();
        }

        collectViews(mPages, views.toArray(new View[0]));
        reloadBackground();
        mPagerAdapter.notifyDataSetChanged();
        setupSingleViewPager(mViewPager, mPagerAdapter, new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < mPages.size(); i++) {
                    mPages.get(i).setVisibility(i == position ? VISIBLE : GONE);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private LinearLayout createGroupView(Context context, List<String> group) {
        LinearLayout widgetsContainer;
        try {
            widgetsContainer = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            widgetsContainer = new LinearLayout(context);
        }
        widgetsContainer.setOrientation(LinearLayout.VERTICAL);
        widgetsContainer.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        widgetsContainer.setTag("widgetsContainer");

        LinearLayout firstRow;
        try {
            firstRow = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            firstRow = new LinearLayout(context);
        }
        firstRow.setId(View.generateViewId());
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        firstRow.setGravity(Gravity.TOP);
        LinearLayout.LayoutParams firstRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        firstRowParams.setMargins(
                0,
                0,
                0,
                dp2px(context, 2));
        firstRow.setLayoutParams(firstRowParams);

        LinearLayout secondRow;
        try {
            secondRow = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            secondRow = new LinearLayout(context);
        }
        secondRow.setId(View.generateViewId());
        secondRow.setOrientation(LinearLayout.HORIZONTAL);
        secondRow.setGravity(Gravity.BOTTOM);
        LinearLayout.LayoutParams secondRowParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1
        );
        secondRowParams.setMargins(
                0,
                dp2px(context, 2),
                0,
                0);
        secondRow.setLayoutParams(secondRowParams);

        int numItems = group.size();
        if (numItems == 0) {
            return widgetsContainer; // Return empty layout if no items
        }

        if (numItems == 1) {
            // One big FAB
            ExtendedFAB fab = createFAB();
            setUpWidgetWiews(null, fab, group.get(0));
            firstRow.addView(fab);
        } else if (numItems == 2) {
            // Two big FABs, one per row
            ExtendedFAB fab = createFAB();
            ExtendedFAB fab2 = createFAB();
            setUpWidgetWiews(null, fab, group.get(0));
            setUpWidgetWiews(null, fab2, group.get(1));
            firstRow.addView(fab);
            secondRow.addView(fab2);
        } else if (numItems == 3) {
            // Two small ImageViews in first row, one big FAB in second
            Space space1 = createSpace();
            ImageView iv1 = createImageView();
            Space space2 = createSpace();
            ImageView iv2 = createImageView();
            Space space3 = createSpace();
            setUpWidgetWiews(iv1, null, group.get(0));
            setUpWidgetWiews(iv2, null, group.get(1));
            firstRow.addView(space1);
            firstRow.addView(iv1);
            firstRow.addView(space2);
            firstRow.addView(iv2);
            firstRow.addView(space3);
            ExtendedFAB fab = createFAB();
            setUpWidgetWiews(null, fab, group.get(2));
            secondRow.addView(fab);
        } else {
            // Four small ImageViews, two per row
            Space space1 = createSpace();
            ImageView iv1 = createImageView();
            Space space2 = createSpace();
            ImageView iv2 = createImageView();
            Space space3 = createSpace();
            setUpWidgetWiews(iv1, null, group.get(0));
            setUpWidgetWiews(iv2, null, group.get(1));
            firstRow.addView(space1);
            firstRow.addView(iv1);
            firstRow.addView(space2);
            firstRow.addView(iv2);
            firstRow.addView(space3);

            Space space4 = createSpace();
            ImageView iv3 = createImageView();
            Space space5 = createSpace();
            ImageView iv4 = createImageView();
            Space space6 = createSpace();
            setUpWidgetWiews(iv3, null, group.get(2));
            setUpWidgetWiews(iv4, null, group.get(3));
            secondRow.addView(space4);
            secondRow.addView(iv3);
            secondRow.addView(space5);
            secondRow.addView(iv4);
            secondRow.addView(space6);
        }

        for (int i = 0; i < firstRow.getChildCount(); i++) {
            View v = firstRow.getChildAt(i);
            if (v instanceof ImageView) {
                updateWidgetsResources((ImageView) v);
            } else if (v instanceof ExtendedFAB) {
                updateMainWidgetResources((ExtendedFAB) v, false);
            }
        }
        for (int i = 0; i < secondRow.getChildCount(); i++) {
            View v = secondRow.getChildAt(i);
            if (v instanceof ImageView) {
                updateWidgetsResources((ImageView) v);
            } else if (v instanceof ExtendedFAB) {
                updateMainWidgetResources((ExtendedFAB) v, false);
            }
        }

        widgetsContainer.addView(firstRow);
        widgetsContainer.addView(secondRow);

        return widgetsContainer;
    }

    private ExtendedFAB createFAB() {
        ExtendedFAB fab = new ExtendedFAB(mContext);
        int resIdH = mContext.getResources().getIdentifier("qs_footer_hl_tile_height_with_media_with_volume", "dimen", SYSTEM_UI);
        if (resIdH == 0) {
            resIdH = mContext.getResources().getIdentifier("qs_footer_hl_tile_collapse_height", "dimen", SYSTEM_UI);
        }
        int h = mContext.getResources().getDimensionPixelSize(resIdH);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                0,
                h,
                1);
        layoutParams.gravity = Gravity.CENTER;
        fab.setId(View.generateViewId());
        fab.setLayoutParams(layoutParams);
        fab.setPadding(
                dp2px(mContext, 6),
                0,
                dp2px(mContext, 6),
                0);
        fab.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        fab.setSingleLine(true);
        fab.setMarqueeRepeatLimit(-1);
        fab.setFocusable(false);
        fab.setFocusableInTouchMode(false);
        fab.setHorizontallyScrolling(true);
        fab.setClickable(true);
        fab.setTypeface(null, Typeface.BOLD);
        fab.setGravity(Gravity.CENTER_VERTICAL);
        fab.setIconSize(dp2px(mContext, 35));
        fab.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            int resIdH2 = mContext.getResources().getIdentifier("qs_footer_hl_tile_height_with_media_with_volume", "dimen", SYSTEM_UI);
            if (resIdH2 == 0) {
                resIdH2 = mContext.getResources().getIdentifier("qs_footer_hl_tile_collapse_height", "dimen", SYSTEM_UI);
            }
            int h2 = mContext.getResources().getDimensionPixelSize(
                    resIdH2);
            LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(
                    0, h2, 1);
            layoutParams2.gravity = Gravity.CENTER;
            ShapeDrawable fabBackground = new ShapeDrawable();
            if (mCustomFabRadius) {
                fabBackground.setShape(new RoundRectShape(mFabRadius, null, null));
            } else {
                fabBackground.setShape(getShapeForHighlightTile(mContext));
            }
            fab.setBackground(fabBackground);
            fab.setLayoutParams(layoutParams2);
            fab.requestLayout();
        });
        fab.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                fab.setSelected(true);
            }
        });
        QsTileTouchAnim anim = new QsTileTouchAnim();
        anim.initTouchAnim(fab, true);

        return fab;
    }

    private ImageView createImageView() {

        ImageView imageView;
        try {
            imageView = (ImageView) LaunchableImageView.getConstructor(Context.class).newInstance(mContext);
        } catch (Exception e) {
            // LaunchableImageView not found or other error, ensure the creation of our ImageView
            imageView = new ImageView(mContext);
        }

        imageView.setId(View.generateViewId());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                QsTileHelper.getQsTileSize(mContext, QsTileHelper.TYPE_QS_QUICK_TILE_SIZE),
                QsTileHelper.getQsTileSize(mContext, QsTileHelper.TYPE_QS_QUICK_TILE_SIZE)); // Weight 1 for small views
        params.gravity = Gravity.CENTER;
        imageView.setLayoutParams(params);
        imageView.setPadding(
                dp2px(mContext, 10),
                dp2px(mContext, 10),
                dp2px(mContext, 10),
                dp2px(mContext, 10));
        imageView.setFocusable(true);
        imageView.setClickable(true);
        QsTileTouchAnim anim = new QsTileTouchAnim();
        anim.initTouchAnim(imageView, true);

        return imageView;

    }

    private Space createSpace() {
        Space space = new Space(mContext);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1);
        space.setLayoutParams(params);
        return space;
    }

    private void updateWidgetsState() {
        updateWiFiButtonState(isWifiEnabled());
        updateMobileDataState(isMobileDataEnabled());
        updateBtState();
        updateRingerButtonState();
        updateHotspotButtonState(0);
    }

    private void setButtonActiveState(ImageView iv, ExtendedFAB efab, boolean active) {
        int bgTint;
        int tintColor;

        if (active) {
            bgTint = mCustomActive ? mCustomActiveColor : mActiveColor;
            tintColor = mIconActiveColor;
        } else {
            bgTint = mCustomInactive ? mCustomInactiveColor : mInactiveColor;
            tintColor = mIconInactiveColor;
        }
        if (iv != null) {
            iv.setBackgroundTintList(ColorStateList.valueOf(bgTint));
            if (iv.getTag() != null && iv.getTag().equals("app")) {
                iv.setImageTintList(null);
            } else {
                if (iv != weatherButton) {
                    iv.setImageTintList(ColorStateList.valueOf(tintColor));
                } else {
                    iv.setImageTintList(null);
                }
            }
        }
        if (efab != null) {
            efab.setBackgroundTintList(ColorStateList.valueOf(bgTint));
            efab.setBackgroundTintMode(PorterDuff.Mode.SRC_IN);
            if (efab.getTag() != null && efab.getTag().equals("app")) {
                efab.setIconTint(null);
            } else {
                if (efab != weatherButtonFab) {
                    efab.setIconTint(ColorStateList.valueOf(tintColor));
                } else {
                    efab.setIconTint(null);
                }
            }
            efab.setTextColor(tintColor);
        }

    }

    private List<List<String>> splitString(String input) {
        String[] elements = input.split(",");

        List<String> currentGroup = new ArrayList<>();
        List<List<String>> groups = new ArrayList<>();

        for (String element : elements) {
            if (element.contains(":")) {
                currentGroup.add(element);
                if (currentGroup.size() == 4) {
                    groups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                }
            } else {
                if (!currentGroup.isEmpty()) {
                    groups.add(new ArrayList<>(currentGroup));
                    currentGroup.clear();
                }
                groups.add(List.of(element));
            }
        }

        if (!currentGroup.isEmpty()) {
            groups.add(new ArrayList<>(currentGroup));
        }

        return groups;
    }

    private void updateMainWidgetResources(ExtendedFAB efab, boolean active) {
        if (efab == null) return;
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        if (mCustomFabRadius) {
            shapeDrawable.setShape(new RoundRectShape(mFabRadius, null, null));
        } else {
            shapeDrawable.setShape(getShapeForHighlightTile(mContext));
        }
        efab.setBackground(shapeDrawable);
        efab.setElevation(0);
        setButtonActiveState(null, efab, false);
    }

    private void updateWidgetsResources(ImageView iv) {
        if (iv == null) return;
        ShapeDrawable shapeDrawable = new ShapeDrawable();
        if (mCustomTileRadius) {
            shapeDrawable.setShape(new RoundRectShape(mTileRadius, null, null));
        } else {
            shapeDrawable.setShape(getLastShape(mContext));
        }
        iv.setBackground(shapeDrawable);
        setButtonActiveState(iv, null, false);
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

    private void setUpWidgetWiews(ImageView iv, ExtendedFAB efab, String widgetType) {
        if (widgetType.contains(":")) {
            String[] parts = widgetType.split(":");
            if (parts[0].startsWith("ca")) {
                Drawable appIcon = AppUtils.getAppIcon(mContext, parts[1]);
                if (iv != null) {
                    iv.setTag("app");
                    iv.setImageDrawable(appIcon);
                }
                if (efab != null) {
                    efab.setTag("app");
                    efab.setIcon(DrawableConverter.scaleDrawable(mContext,
                            appIcon,
                            0.3f));
                }
                setUpWidgetResources(iv, efab,
                        v -> launchApp(parts[1]), null, AppUtils.getAppName(mContext, parts[1]));
                return;
            }
        }
        switch (widgetType) {
            case "w:weather":
                if (iv != null) {
                    weatherButton = iv;
                    weatherButton.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchWeatherSettings(true);
                        return true;
                    });
                }
                if (efab != null) {
                    weatherButtonFab = efab;
                    weatherButtonFab.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchWeatherSettings(true);
                        return true;
                    });
                }
                // Set a null on click listener to weather button to avoid running previous button action
                setUpWidgetResources(iv, efab, v -> mActivityLauncherUtils.launchWeatherActivity(true), ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.google_30, appContext.getTheme()), appContext.getString(R.string.weather_settings));
                enableWeatherUpdates();
                break;
            case "w:wifi":
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
                setUpWidgetResources(iv, efab, v -> toggleWiFi(), getDrawable(mContext, WIFI_INACTIVE, SYSTEM_UI), getString(mContext, WIFI_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "w:data":
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
                setUpWidgetResources(iv, efab, v -> toggleMobileData(), getDrawable(mContext, DATA_INACTIVE, SYSTEM_UI), getString(mContext, DATA_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "w:ringer":
                if (iv != null) {
                    ringerButton = iv;
                    ringerButton.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchAudioSettings(true);
                        return true;
                    });
                }
                if (efab != null) {
                    ringerButtonFab = efab;
                    ringerButtonFab.setOnLongClickListener(v -> {
                        mActivityLauncherUtils.launchAudioSettings(true);
                        return true;
                    });
                }
                setUpWidgetResources(iv, efab, v -> toggleRingerMode(), getDrawable(mContext, RINGER_INACTIVE, SYSTEM_UI), getString(mContext, RINGER_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "w:bt":
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
                setUpWidgetResources(iv, efab, v -> toggleBluetoothState(), getDrawable(mContext, BT_INACTIVE, SYSTEM_UI), getString(mContext, BT_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "w:torch":
                if (iv != null) {
                    torchButton = iv;
                }
                if (efab != null) {
                    torchButtonFab = efab;
                }
                setUpWidgetResources(iv, efab, v -> toggleFlashlight(), getDrawable(mContext, TORCH_RES_INACTIVE, SYSTEM_UI), getString(mContext, TORCH_LABEL_INACTIVE, SYSTEM_UI));
                break;
            case "w:timer":
                setUpWidgetResources(iv, efab, v -> {
                    mActivityLauncherUtils.launchTimer(true);
                    vibrate(1);
                }, getDrawable(mContext, "ic_alarm", SYSTEM_UI), modRes.getString(R.string.clock_timer));
                break;
            case "w:camera":
                setUpWidgetResources(iv, efab, v -> {
                    mActivityLauncherUtils.launchCamera(true);
                    vibrate(1);
                }, getDrawable(mContext, CAMERA_ICON, SYSTEM_UI), getString(mContext, CAMERA_LABEL, SYSTEM_UI));
                break;
            case "w:calculator":
                setUpWidgetResources(iv, efab, v -> mActivityLauncherUtils.launchCalculator(), getDrawable(mContext, CALCULATOR_ICON, SYSTEM_UI), getString(mContext, CALCULATOR_LABEL, SYSTEM_UI));
                break;
            case "w:homecontrols":
                setUpWidgetResources(iv, efab, this::launchHomeControls, getDrawable(mContext, HOME_CONTROLS, SYSTEM_UI), getString(mContext, HOME_CONTROLS_LABEL, SYSTEM_UI));
                break;
            case "w:wallet":
                setUpWidgetResources(iv, efab, this::launchWallet, getDrawable(mContext, WALLET_ICON, SYSTEM_UI), getString(mContext, WALLET_LABEL, SYSTEM_UI));
                break;
            case "w:hotspot":
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
                        getString(mContext, HOTSPOT_LABEL, SYSTEM_UI));
            default:
                break;
        }
    }

    // Dialogs
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

    @SuppressLint("ClickableViewAccessibility")
    private void setUpWidgetResources(ImageView iv, ExtendedFAB efab,
                                      final OnClickListener cl, Drawable icon, String text) {
        if (efab != null) {
            efab.setOnClickListener(cl);
            if (icon != null) efab.setIcon(icon);
            efab.setText(text);
        }
        if (iv != null) {
            iv.setOnClickListener(cl);
            if (icon != null) iv.setImageDrawable(icon);
        }
    }

    /**
     * Toggles the ringer modes
     * Normal -> Vibrate -> Silent -> Normal
     */
    private void toggleRingerMode() {
        Object mRingerTile = getRingerTile();

        if (mRingerTile != null) {
            callMethod(mRingerTile, "setRingMode");
        } else if (SystemUtils.AudioManager() != null) {
            int mode = SystemUtils.AudioManager().getRingerMode();
            int newMode = switch (mode) {
                case AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE;
                case AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT;
                default -> AudioManager.RINGER_MODE_NORMAL;
            };

            SystemUtils.AudioManager().setRingerMode(newMode);
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

    private void toggleFlashlight() {
        if (torchButton == null && torchButtonFab == null) return;
        try {
            mCameraManager.setTorchMode(mCameraId, !isFlashOn);
            isFlashOn = !isFlashOn;
            updateTorchButtonState();
            vibrate(1);
        } catch (Exception e) {
            log(TAG + "toggleFlashlight error: " + e.getMessage());
        }
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

    private void launchApp(String packageName) {
        Intent launchIntent = mContext.getPackageManager().getLaunchIntentForPackage(packageName);
        if (launchIntent == null) return;
        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        mActivityLauncherUtils.launchApp(launchIntent);
    }

    private boolean isWidgetEnabled(String widget) {
        return mWidgets.contains(widget);
    }

    // Update States
    @SuppressWarnings("deprecation")
    private void updateWiFiButtonState(boolean enabled) {
        if (!isWidgetEnabled("wifi")) return;
        if (wifiButton == null && wifiButtonFab == null) return;
        String theSsid = SystemUtils.WifiManager().getConnectionInfo().getSSID();
        if (theSsid.equals(UNKNOWN_SSID)) {
            theSsid = getString(mContext, WIFI_LABEL_INACTIVE, SYSTEM_UI);
        } else {
            if (theSsid.startsWith("\"") && theSsid.endsWith("\"")) {
                theSsid = theSsid.substring(1, theSsid.length() - 1);
            }
        }
        updateTileButtonState(wifiButton, wifiButtonFab, isWifiEnabled(),
                WIFI_ACTIVE, WIFI_INACTIVE, theSsid, getString(mContext, WIFI_LABEL_INACTIVE, SYSTEM_UI));
    }

    private void updateRingerButtonState() {
        if (!isWidgetEnabled("ringer")) return;
        if (ringerButton == null && ringerButtonFab == null) return;
        if (SystemUtils.AudioManager() != null) {
            boolean soundActive = SystemUtils.AudioManager().getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
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
        String inactive = getString(mContext, DATA_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(dataButton, dataButtonFab, enabled,
                DATA_ACTIVE, DATA_INACTIVE, hasNetwork && enabled ? networkName : inactive, inactive);
    }

    private void updateBtState() {
        if (!isWidgetEnabled("bt")) return;
        if (btButton == null && btButtonFab == null) return;
        Object bluetoothController = getBluetoothController();
        String deviceName = isBluetoothEnabled() ? (String) callMethod(bluetoothController, "getConnectedDeviceName") : "";
        boolean isConnected = !TextUtils.isEmpty(deviceName);
        String inactiveString = getString(mContext, BT_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(btButton, btButtonFab, isBluetoothEnabled(),
                BT_ACTIVE, BT_INACTIVE, isConnected ? deviceName : inactiveString, inactiveString);
    }

    public void updateTorchButtonState() {
        if (!isWidgetEnabled("torch")) return;
        String activeString = getString(mContext, TORCH_LABEL_ACTIVE, SYSTEM_UI);
        String inactiveString = getString(mContext, TORCH_LABEL_INACTIVE, SYSTEM_UI);
        updateTileButtonState(torchButton, torchButtonFab, isFlashOn,
                TORCH_RES_ACTIVE, TORCH_RES_INACTIVE, activeString, inactiveString);
    }

    private void updateHotspotButtonState(int numDevices) {
        if (!isWidgetEnabled("hotspot")) return;
        if (hotspotButton == null && hotspotButtonFab == null) return;
        String inactiveString = getString(mContext, HOTSPOT_LABEL, SYSTEM_UI);
        String activeString = getString(mContext, HOTSPOT_LABEL, SYSTEM_UI);
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
        if (SystemUtils.BluetoothManager().getAdapter() == null) return false;
        return SystemUtils.BluetoothManager().getAdapter().isEnabled();
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
        boolean enabled = SystemUtils.WifiManager().isWifiEnabled();
        return enabled;
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
                log(TAG + "isHotspotEnabled error: " + t.getMessage());
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
            log(TAG + "getHotspotSSID error: " + t.getMessage());
        }
        return "";
    }

    // Ringer Getter
    private Drawable getRingerDrawable() {
        String resName = switch (SystemUtils.AudioManager().getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL -> RINGER_NORMAL;
            case AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE;
            case AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT;
            default ->
                    throw new IllegalStateException("Unexpected value: " + SystemUtils.AudioManager().getRingerMode());
        };

        return getDrawable(mContext, resName, SYSTEM_UI);
    }

    private String getRingerText() {
        String RINGER_NORMAL = "volume_footer_ring";
        String RINGER_VIBRATE = "state_button_vibration";
        String RINGER_SILENT = "state_button_silence";

        String resName = switch (SystemUtils.AudioManager().getRingerMode()) {
            case AudioManager.RINGER_MODE_NORMAL -> RINGER_NORMAL;
            case AudioManager.RINGER_MODE_VIBRATE -> RINGER_VIBRATE;
            case AudioManager.RINGER_MODE_SILENT -> RINGER_SILENT;
            default ->
                    throw new IllegalStateException("Unexpected value: " + SystemUtils.AudioManager().getRingerMode());
        };

        return getString(mContext, resName, SYSTEM_UI);

    }

    public static QsControlsView getInstance(Context context) {
        if (instance != null) return instance;
        return new QsControlsView(context);
    }

    public static QsControlsView getInstance() {
        return instance;
    }

    /**
     * Update the widgets shown in the QS view
     *
     * @param widgets Comma separated list of widgets to show
     */
    public void updateWidgets(String widgets) {
        if (instance == null) return;
        mWidgets = widgets;
        setupWidgets();
        updateWidgetsState();
    }

    /**
     * Update the media player preferences
     *
     * @param showAlbumArt         Show album art as media tile background
     * @param mediaQsArtFilter     Media tile art filter [
     * @param mediaQsTintColor     Media tile tint color
     * @param mediaQsTintAmount    Media tile tint amount [int 25 - 70%]
     * @param mediaQsArtBlurAmount Media tile art blur amount [float 0.1 - 1f]
     */
    public void updateMediaPlayerPrefs(boolean showAlbumArt, int mediaQsArtFilter, int mediaQsTintColor,
                                       int mediaQsTintAmount, float mediaQsArtBlurAmount) {
        if (instance == null) return;
        instance.mMediaPlayer.updatePrefs(showAlbumArt, mediaQsArtFilter, mediaQsTintColor, mediaQsTintAmount, mediaQsArtBlurAmount);
    }

    /**
     * Update the media colors based on chosen prefs for stock media player
     *
     * @param defBg Default background (obtained from OplusQsMediaTileView)
     */
    public void updateDefaultMediaBg(Drawable defBg) {
        if (instance == null) return;
        mDefaultBackground = defBg;
    }

    /**
     * Set custom tile colors
     * this will match qs tile customizations
     *
     * @param customInactive Use custom color for inactive state
     * @param inactiveColor  Inactive color state
     * @param customActive   Use custom color for active state
     * @param activeColor    Active color state
     * @param force          Force update
     */
    public void updateQsTileColors(boolean customInactive, int inactiveColor, boolean customActive, int activeColor, boolean force) {
        if (instance == null) return;
        mCustomInactive = customInactive;
        mCustomInactiveColor = inactiveColor;
        mCustomActive = customActive;
        mCustomActiveColor = activeColor;
        if (mMediaPlayer != null) {
            mMediaPlayer.updateColors(mDefaultBackground, customInactive, inactiveColor);
        }
        if (force) {
            reloadBackground();
            updateWidgetsState();
            setButtonActiveState(weatherButton, weatherButtonFab, false);
        }
    }

    /**
     * Update the radius of the widgets
     *
     * @param customHighlight Use custom highlight radius
     * @param highlightRadius Highlight radius float of 8 int @Px
     * @param customTile      Use custom tile radius
     * @param tileRadius      Tile radius float of 8 int @Px
     * @param force           Force update
     */
    public void updateTileShapes(boolean customHighlight, float[] highlightRadius,
                                 boolean customTile, float[] tileRadius, boolean force) {
        if (instance == null) return;
        mCustomFabRadius = customHighlight;
        mFabRadius = highlightRadius;
        mCustomTileRadius = customTile;
        mTileRadius = tileRadius;
        if (force) updateWidgets(mWidgets);
    }

    /**
     * Reload all backgrounds
     * used after a drawable, color or other resource change
     * to ensure all backgrounds are updated
     */
    private void reloadBackground() {
        if (mDefaultBackground == null || mPages.isEmpty()) return;
        int i = 0;
        for (View v : mPages) {
            if (v.getTag() != null && v.getTag().equals("widgetsContainer")) {
                v.setBackground(null);
            } else {
                Drawable back = mDefaultBackground.getConstantState().newDrawable().mutate();
                if (v instanceof QsWeatherWidget) {
                    if (back instanceof GradientDrawable) {
                        ((GradientDrawable) back).setColors(new int[]{Color.parseColor("#0D47A1"), Color.parseColor("#0D47A1")});
                    } else if (back instanceof ShapeDrawable) {
                        ((ShapeDrawable) back).getPaint().setColor(Color.parseColor("#0D47A1"));
                    }
                } else {
                    int color = mCustomInactive ? mCustomInactiveColor : mInactiveColor;
                    if (back instanceof GradientDrawable) {
                        ((GradientDrawable) back).setColors(new int[]{color, color});
                    } else if (back instanceof ShapeDrawable) {
                        ((ShapeDrawable) back).getPaint().setColor(color);
                    }
                }
                back.invalidateSelf();
                v.setBackground(back);
            }
            i++;
        }
    }

    /**
     * Update the radius of the photo showcase
     *
     * @param radius New radius
     */
    public void updatePhotoRadius(int radius) {
        if (instance == null) return;
        instance.mPhotoRadius = radius;
        if (!instance.mPages.isEmpty()) {
            for (View v : instance.mPages) {
                if (v instanceof QsPhotoShowcaseContainer) {
                    ((QsPhotoShowcaseContainer) v).setRadius(radius);
                }
            }
        }
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
