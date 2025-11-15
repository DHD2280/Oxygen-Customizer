package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_SHOWCASE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_HIDE_EDIT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_HIDE_MENU;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_CUSTOM_WIDTH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_LAYOUT_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_ROWS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_WIDGETS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_WIDTH;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.oplus.systemui.plugins.qs.DeviceProfile;
import com.oplus.systemui.plugins.qs.tile.OplusLargeTileContainerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ReflectionTools;
import it.dhd.oxygencustomizer.xposed.utils.SeparateQsWidgetsFactory;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;
import it.dhd.oxygencustomizer.xposed.views.controls.customapp.QsCustomAppWidgetView;
import it.dhd.oxygencustomizer.xposed.views.controls.photo.QsPhotoShowcaseContainerView;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsMiniWeatherView;

public class SeparateQsCustomization extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private Object mDeviceProfile;
    private OplusLargeTileContainerView mOplusLargeTileContainerView;
    private boolean mHideEdit = false, mHideMenu = false;
    private View mEditButton, mMenuButton;
    private boolean mCustomLayout = false;
    private int mRows = 3;
    // Stock Layout
    private String mSavedCells = "";
    private boolean mMediaEnabled = true;
    private int[] mMediaCell = {0, 1, 2, 2};
    private boolean mH1Enabled = true;
    private int[] mH1Cell = {0, 0, 2, 1};
    private boolean mH2Enabled = true;
    private int[] mH2Cell = {2, 0, 2, 1};
    private boolean mBrightnessEnabled = true;
    private int[] mBrightnessCell = {2, 1, 1, 2};
    private boolean mVolumeEnabled = true;
    private int[] mVolumeCell = {3, 1, 1, 2};
    private int mPhotoRadius;
    private boolean mPhotoShowcase = false;
    private boolean mCustomQsArea = false;
    private float mCustomQsWidth = 0.5f;

    // Custom Views
    private String mCustomWidgets = "";
    Map<View, int[]> mOldWidgets = new HashMap<>();
    Map<View, int[]> mWidgets = new HashMap<>();


    private final BroadcastReceiver mDeviceProfileReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int cellSize = ((DeviceProfile)mDeviceProfile).getCellCalculator().getCellSize();
            int marginHorizontal = ((DeviceProfile)mDeviceProfile).getCellCalculator().getCellMarginHorizontal();
            int marginVertical = ((DeviceProfile)mDeviceProfile).getCellCalculator().getCellMarginVertical();
            Intent i = new Intent();
            i.setAction(BuildConfig.APPLICATION_ID + ".DEVICE_PROFILE_POST");
            i.putExtra("cellSize", cellSize);
            i.putExtra("marginHorizontal", marginHorizontal);
            i.putExtra("marginVertical", marginVertical);
            i.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mContext.sendBroadcast(i);
        }
    };

    public SeparateQsCustomization(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

        mPhotoRadius = Xprefs.getSliderInt(QS_PHOTO_RADIUS, 22);
        mPhotoShowcase = Xprefs.getString(QS_PHOTO_SHOWCASE, "0").equals("1");
        mRows = Xprefs.getInt(SEPARATE_QS_ROWS, 3);
        mCustomLayout = Build.VERSION.SDK_INT == 35 ? Xprefs.getBoolean(SEPARATE_QS_LAYOUT_SWITCH, false) : false;
        mHideMenu = Xprefs.getBoolean(SEPARATE_HIDE_MENU, false);
        mHideEdit = Xprefs.getBoolean(SEPARATE_HIDE_EDIT, false);
        mCustomQsArea = Xprefs.getBoolean(SEPARATE_QS_CUSTOM_WIDTH, false);
        mCustomQsWidth = Xprefs.getSliderInt(SEPARATE_QS_WIDTH, 50) / 100f;
        String customWidgets = Xprefs.getString(SEPARATE_QS_WIDGETS, "");
        String savedCells = Xprefs.getString(SEPARATE_QS_LAYOUT, "");

        if (!TextUtils.equals(savedCells, mSavedCells)) {
            mSavedCells = savedCells;
            parseCell();
        }
        if (!TextUtils.equals(customWidgets, mCustomWidgets)) {
            mCustomWidgets = customWidgets;
            parseCustomWidgets();
        }

        if (Key.length > 0) {
            if (Key[0].equals(QS_PHOTO_RADIUS)) {
                if (mCustomWidgets.contains("photo")) {
                    for (View widget : mWidgets.keySet()) {
                        if (widget instanceof QsPhotoShowcaseContainerView photoShowcaseContainerView) {
                            photoShowcaseContainerView.setRadius(mPhotoRadius);
                        }
                    }
                }
            }
            if (Key[0].equals(QS_PHOTO_SHOWCASE)) {
                if (mCustomWidgets.contains("photo")) {
                    for (View widget : mWidgets.keySet()) {
                        if (widget instanceof QsPhotoShowcaseContainerView photoShowcaseContainerView) {
                            photoShowcaseContainerView.setPhotoMode(mPhotoShowcase);
                        }
                    }
                }
            }
            if (Key[0].equals(SEPARATE_QS_LAYOUT_SWITCH) ||
                    Key[0].equals(SEPARATE_QS_LAYOUT) ||
                    Key[0].equals(SEPARATE_QS_WIDGETS) ||
                    Key[0].equals(SEPARATE_QS_ROWS)) {
                updateLayout();
            } else if (Key[0].equals(SEPARATE_HIDE_MENU) ||
                        Key[0].equals(SEPARATE_HIDE_EDIT)) { {
                            setupButtons();
            }}
        }
    }

    private void parseCell() {
        try {
            if (!TextUtils.isEmpty(mSavedCells)) {
                JSONObject json = new JSONObject(mSavedCells);
                mMediaEnabled = json.getBoolean("mediaEnabled");
                JSONArray mediaCell = json.getJSONArray("mediaCell");
                mH1Enabled = json.getBoolean("h1Enabled");
                JSONArray h1Cell = json.getJSONArray("h1Cell");
                mH2Enabled = json.getBoolean("h2Enabled");
                JSONArray h2Cell = json.getJSONArray("h2Cell");
                mBrightnessEnabled = json.getBoolean("brightnessEnabled");
                JSONArray brightnessCell = json.getJSONArray("brightnessCell");
                mVolumeEnabled = json.getBoolean("volumeEnabled");
                JSONArray volumeCell = json.getJSONArray("volumeCell");
                mMediaCell = new int[]{
                        mediaCell.getInt(0), mediaCell.getInt(1),
                        mediaCell.getInt(2), mediaCell.getInt(3)
                };
                mH1Cell = new int[]{
                        h1Cell.getInt(0), h1Cell.getInt(1),
                        h1Cell.getInt(2), h1Cell.getInt(3)
                };
                mH2Cell = new int[]{
                        h2Cell.getInt(0), h2Cell.getInt(1),
                        h2Cell.getInt(2), h2Cell.getInt(3)
                };
                mBrightnessCell = new int[]{
                        brightnessCell.getInt(0), brightnessCell.getInt(1),
                        brightnessCell.getInt(2), brightnessCell.getInt(3)
                };
                mVolumeCell = new int[]{
                        volumeCell.getInt(0), volumeCell.getInt(1),
                        volumeCell.getInt(2), volumeCell.getInt(3)
                };
            }
        } catch (Throwable t) {
            XposedBridge.log("Oxygen Customizer - QsTileCustomization: Error parsing layout_cells\n" + Log.getStackTraceString(t));
        }
    }

    private void parseCustomWidgets() {
        try {
            if (!TextUtils.isEmpty(mCustomWidgets)) {
                mOldWidgets.clear();
                mOldWidgets.putAll(mWidgets);

                mWidgets.clear();
                JSONObject json = new JSONObject(mCustomWidgets);
                JSONArray widgets = json.getJSONArray("widgets");
                for (int i = 0; i < widgets.length(); i++) {
                    JSONObject widget = widgets.getJSONObject(i);
                    String name = widget.getString("viewId");
                    int x = widget.getInt("x");
                    int y = widget.getInt("y");
                    int spanX = widget.getInt("width");
                    int spanY = widget.getInt("height");
                    mWidgets.put(SeparateQsWidgetsFactory.getWidget(
                            name, mContext, false
                    ), new int[]{x, y, spanX, spanY});
                }
                mOldWidgets.clear();
                mOldWidgets.putAll(mWidgets);
            }
        } catch (Throwable t) {
            XposedBridge.log("Oxygen Customizer - SeparateQsCustomization: Error parsing custom_widgets\n" + Log.getStackTraceString(t));
        }
    }
    
    private void updateCellsHook(Object obj) throws Throwable {
        if (!mCustomLayout) return;
        try {
            setIntField(obj, "mRows", mRows);
        } catch (Throwable ignored) {}
        ArrayList mViewCells = (ArrayList) getObjectField(obj, "mViewCells");
        ViewGroup mMediaPanelContainer = (ViewGroup) getObjectField(obj, "mMediaPanelContainer");
        ViewGroup mBrightnessTileContainer = (ViewGroup) getObjectField(obj, "mBrightnessTileContainer");
        ViewGroup mVolumeTileContainer = (ViewGroup) getObjectField(obj, "mVolumeTileContainer");
        ViewGroup mFirstTileContainer = (ViewGroup) getObjectField(obj, "mFirstTileContainer");
        ViewGroup mSecondTileContainer = (ViewGroup) getObjectField(obj, "mSecondTileContainer");
        ReflectedClass ViewCellInfoClz = ReflectedClass.of(mViewCells.get(0).getClass());
        for (int i = 0; i < mViewCells.size(); i++) {
            OplusLargeTileContainerView.ViewCellInfo cell = (OplusLargeTileContainerView.ViewCellInfo) mViewCells.get(i);
            View v = (View) getObjectField(cell, "v");
            if (v == mMediaPanelContainer) {
                setIntField(cell, "x", mMediaCell[0]);
                setIntField(cell, "y", mMediaCell[1]);
                setIntField(cell, "spanX", mMediaCell[2]);
                setIntField(cell, "spanY", mMediaCell[3]);
                v.setVisibility(mMediaEnabled ? View.VISIBLE : View.GONE);
            } else if (v == mFirstTileContainer) {
                if (mH1Cell[3] > 1) {
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    v.setLayoutParams(params);
                }
                setIntField(cell, "x", mH1Cell[0]);
                setIntField(cell, "y", mH1Cell[1]);
                setIntField(cell, "spanX", mH1Cell[2]);
                setIntField(cell, "spanY", mH1Cell[3]);
                v.setVisibility(mH1Enabled ? View.VISIBLE : View.GONE);
            } else if (v == mSecondTileContainer) {
                if (mH2Cell[3] > 1) {
                    ViewGroup.LayoutParams params = v.getLayoutParams();
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    v.setLayoutParams(params);
                }
                setIntField(cell, "x", mH2Cell[0]);
                setIntField(cell, "y", mH2Cell[1]);
                setIntField(cell, "spanX", mH2Cell[2]);
                setIntField(cell, "spanY", mH2Cell[3]);
                v.setVisibility(mH2Enabled ? View.VISIBLE : View.GONE);
            } else if (v == mBrightnessTileContainer) {
                setIntField(cell, "x", mBrightnessCell[0]);
                setIntField(cell, "y", mBrightnessCell[1]);
                setIntField(cell, "spanX", mBrightnessCell[2]);
                setIntField(cell, "spanY", mBrightnessCell[3]);
                v.setVisibility(mBrightnessEnabled ? View.VISIBLE : View.GONE);
            } else if (v == mVolumeTileContainer) {
                setIntField(cell, "x", mVolumeCell[0]);
                setIntField(cell, "y", mVolumeCell[1]);
                setIntField(cell, "spanX", mVolumeCell[2]);
                setIntField(cell, "spanY", mVolumeCell[3]);
                v.setVisibility(mVolumeEnabled ? View.VISIBLE : View.GONE);
            }
        }
        Set<View> validViews = mWidgets.keySet();

        Iterator it = mViewCells.iterator();
        while (it.hasNext()) {
            Object cell = it.next();
            View v = (View) getObjectField(cell, "v");
            if (!validViews.contains(v)
                    && v != mMediaPanelContainer
                    && v != mFirstTileContainer
                    && v != mSecondTileContainer
                    && v != mBrightnessTileContainer
                    && v != mVolumeTileContainer) {

                try {
                    ViewGroup parent = (ViewGroup) v.getParent();
                    if (parent != null) parent.removeView(v);
                } catch (Throwable ignored) {}

                it.remove();
            }
        }

        for (Map.Entry<View, int[]> entry : mWidgets.entrySet()) {
            View widget = entry.getKey();
            int[] specs = entry.getValue();

            boolean exists = false;
            for (Object cell : mViewCells) {
                if (getObjectField(cell, "v") == widget) {
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                try {
                    ViewGroup parent = (ViewGroup) widget.getParent();
                    if (parent != null) parent.removeView(widget);
                } catch (Throwable ignored) {}
                if (widget instanceof QsMiniWeatherView miniWeather) {
                    miniWeather.onSizeChanged(specs[2]);
                } else if (widget instanceof QsCustomAppWidgetView customApp) {
                    customApp.onSizeChanged(specs[2]);
                } else if (widget instanceof QsPhotoShowcaseContainerView photoShowcaseContainerView) {
                    photoShowcaseContainerView.setRadius(mPhotoRadius);
                    photoShowcaseContainerView.setPhotoMode(mPhotoShowcase);
                }
                if (widget instanceof BaseQsStaticView staticView) {
                    staticView.reloadBackground();
                }
                mOplusLargeTileContainerView.addView(widget);
                Class<?> clazz = ViewCellInfoClz.getClazz();
                Constructor<?> constructor = null;

                for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                    if (c.getParameterCount() == 6 || c.getParameterCount() == 5) {
                        constructor = c;
                        break;
                    }
                }

                if (constructor != null) {
                    constructor.setAccessible(true);
                    Object viewCellInfo;
                    if (constructor.getParameterCount() == 6) {
                        viewCellInfo = constructor.newInstance(
                                mOplusLargeTileContainerView,  // outer class instance
                                widget,                        // View
                                specs[0], specs[1], specs[2], specs[3]  // int, int, int, int
                        );
                    } else {
                        viewCellInfo = constructor.newInstance(
                                widget,                        // View
                                specs[0], specs[1], specs[2], specs[3]  // int, int, int, int
                        );
                    }
                    mViewCells.add(viewCellInfo);
                } else {
                    log("Constructor ViewCellInfo not found!");
                }
            }
        }
        setObjectField(obj, "mViewCells", mViewCells);
        mOplusLargeTileContainerView.requestLayout();
        mOplusLargeTileContainerView.postInvalidate();
        XposedBridge.log("Oxygen Customizer - SeparateQsCustomization - updateCellsHook: mViewCells size: " + mViewCells.size());
    }
    
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return;

        mContext.registerReceiver(mDeviceProfileReceiver, new IntentFilter(BuildConfig.APPLICATION_ID + ".DEVICE_PROFILE_GET"), RECEIVER_EXPORTED);

        ReflectedClass OplusQSQuickEntranceContainerViewController = ReflectedClass.of(
                "com.oplus.systemui.plugins.qs.quickentrance.OplusQSQuickEntranceComponent", //OOS16
                "com.oplus.systemui.plugins.qs.quickentrance.OplusQSQuickEntranceContainerViewController");
        OplusQSQuickEntranceContainerViewController
                .after("onInit")
                .run(param -> {
                    ImageView settingsButton = (ImageView) getObjectField(param.thisObject, "settingsButton");
                    settingsButton.setOnLongClickListener(v -> {
                        openOxygenCustomizer();
                        return true;
                    });
                    if (Build.VERSION.SDK_INT >= 36) {
                        try {
                            mEditButton = (View) getObjectField(param.thisObject, "editBtn");
                            mMenuButton = (View) getObjectField(param.thisObject, "moreBtn");
                            setupButtons();
                        } catch (Throwable ignored) {}
                    }
                });

        ReflectedClass OplusQSBottomViewController = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.bottom.OplusQSBottomViewController");
        OplusQSBottomViewController
                .after("init")
                .run(param -> {
                    mEditButton = (View) getObjectField(param.thisObject, "editBtn");
                    mMenuButton = (View) getObjectField(param.thisObject, "moreBtn");
                    setupButtons();
                });

        ReflectedClass DeviceProfile = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.DeviceProfile");
        DeviceProfile
                .afterConstruction()
                .run(param -> mDeviceProfile = param.thisObject);

        ReflectedClass OplusLargeTileContainerViewClz = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.tile.OplusLargeTileContainerView");

        OplusLargeTileContainerViewClz
                .before("onMeasure")
                .run(param -> {
                    if (!mCustomLayout) return;
                    setIntField(param.thisObject, "mRows", mRows);
                });

        OplusLargeTileContainerViewClz
                .after("onFinishInflate")
                .run(param -> {
                    mOplusLargeTileContainerView = (OplusLargeTileContainerView) param.thisObject;
                    updateCellsHook(param.thisObject);
                    mOplusLargeTileContainerView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
                        @Override
                        public void onViewAttachedToWindow(@NonNull View v) {
                            try {
                                updateCellsHook(v);
                            } catch (Throwable e) {
                                log(e);
                            }
                        }

                        @Override
                        public void onViewDetachedFromWindow(@NonNull View v) {}
                    });
                });

        OplusLargeTileContainerViewClz
                .before("updateViewState")
                .run(param -> {
                    // float f2, float f3, int i2
                    if (!mCustomQsArea) return;
                    float f2 = (float) param.args[0];
                    float f3 = (float) param.args[1];
                    int i2 = (int) param.args[2];
                    if (i2 == 1) {
                        for (Map.Entry<View, int[]> entry : mWidgets.entrySet()) {
                            View widget = entry.getKey();
                            widget.setTransitionAlpha(f2);
                            widget.setScaleX(f3);
                            widget.setScaleY(f3);
                        }
                    }
                });

        OplusLargeTileContainerViewClz
                .before("setCusTranslationY")
                .run(param -> {
                   // int i2, int i3, float f2
                    if (!mCustomQsArea) return;
                    View v = (View) param.thisObject;
                    int i2 = (int) param.args[0];
                    int i3 = (int) param.args[1];
                    float f2 = (float) param.args[2];
                    if (i3 == 1) {
                        for (Map.Entry<View, int[]> entry : mWidgets.entrySet()) {
                            View widget = entry.getKey();
                            widget.setTranslationY(f2 - v.getTranslationY());
                        }
                    }
                });

        // OOS 15.0.1
        // OplusLargeTileContainerViewController for our views
        ReflectedClass OplusLargeTileContainerViewController = ReflectedClass.ofIfPossible("com.oplus.systemui.plugins.qs.tile.OplusLargeTileContainerViewController");
        if (OplusLargeTileContainerViewController.getClazz() != null) {
            OplusLargeTileContainerViewController
                    .before("setCusTranslationY")
                    .run(param -> {
                        // float f2, int i2, int i3
                        if (!mCustomQsArea) return;
                        View oplusLargeTileContainerView = (View) getObjectField(param.thisObject, "mView");
                        int i3 = (int) param.args[2];
                        float f2 = (float) param.args[0];
                        if (i3 == 0) return;
                        for (Map.Entry<View, int[]> entry : mWidgets.entrySet()) {
                            View widget = entry.getKey();
                            widget.setTranslationY(f2 - oplusLargeTileContainerView.getTranslationY());
                        }
                    });
            OplusLargeTileContainerViewController
                    .before("updateState")
                    .run(param -> {
                        // float f2, float f3, int i2
                        if (!mCustomQsArea) return;
                        float f2 = (float) param.args[0];
                        float f3 = (float) param.args[1];
                        int i2 = (int) param.args[2];
                        if (i2 == 1) {
                            for (Map.Entry<View, int[]> entry : mWidgets.entrySet()) {
                                View widget = entry.getKey();
                                widget.setTransitionAlpha(f2);
                                widget.setScaleX(f3);
                                widget.setScaleY(f3);
                            }
                        }
                    });
        }

        ReflectedClass OplusPanelViewPagerController = ReflectedClass.of("com.oplus.systemui.separate.OplusPanelViewPagerController");
        OplusPanelViewPagerController
                .before("isRightArea")
                .run(param -> {
                    if (!mCustomQsArea) return;
                    Object centralSurfaces = getObjectField(param.thisObject, "centralSurfaces");
                    float f2 = (float) param.args[0];
                    param.setResult(f2 >= (centralSurfaces != null ? (float) callMethod(centralSurfaces, "getDisplayWidth") : 1080.0f) * (1f - mCustomQsWidth));
                });

    }
    
    private void updateLayout() {
        if (mOplusLargeTileContainerView == null) return;
        try {
            updateCellsHook(mOplusLargeTileContainerView);
        } catch (Throwable t) {
            XposedBridge.log("Oxygen Customizer - SeparateQsCustomization: Error updating layout\n" + Log.getStackTraceString(t));
        }
    }

    private void openOxygenCustomizer() {
        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
        Object mActivityStarter = ControllersProvider.getActivityStarterExternal();
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0 /* dismissShade */);
    }

    private void setupButtons() {
        if (mEditButton != null) {
            mEditButton.setVisibility(mHideEdit ? View.GONE : View.VISIBLE);
        }
        if (mMenuButton != null) {
            mMenuButton.setVisibility(mHideMenu ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
