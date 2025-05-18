package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.separate;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_ROWS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_WIDGETS;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.preference.OplusPreferenceFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.dhd.oneplusui.appcompat.dialog.adapter.SummaryAdapter;
import it.dhd.oneplusui.preference.OplusPreferenceCategory;
import it.dhd.oneplusui.preference.OplusSectionSeekbarPreference;
import it.dhd.oneplusui.preference.OplusSwitchPreference;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentSeparateBinding;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage;
import it.dhd.oxygencustomizer.ui.fragments.mods.WeatherSettings;
import it.dhd.oxygencustomizer.utils.json.SeparateQsWidgetInfo;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.xposed.utils.SeparateQsWidgetsFactory;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.views.controls.customapp.QsCustomAppWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.devicewidget.DeviceWidgetInterface;
import it.dhd.oxygencustomizer.xposed.views.controls.media.MediaBinder;
import it.dhd.oxygencustomizer.xposed.views.controls.photo.QsPhotoShowcaseContainer;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsMiniWeather;
import it.dhd.oxygencustomizer.xposed.views.controls.weather.QsWeatherWidget;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.base.BaseQsWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class SeparateQsPreview extends BaseFragment {

    private FragmentSeparateBinding binding;
    private static int mRows = 3; // default rows
    private static boolean mMediaEnabled = true;
    private static int[] mMediaCell = {0, 1, 2, 2};
    private static boolean mH1Enabled = true;
    private static int[] mH1Cell = {0, 0, 2, 1};
    private static boolean mH2Enabled = true;
    private static int[] mH2Cell = {2, 0, 2, 1};
    private static boolean mBrightnessEnabled = true;
    private static int[] mBrightnessCell = {2, 1, 1, 2};
    private static boolean mVolumeEnabled = true;
    private static int[] mVolumeCell = {3, 1, 1, 2};
    private final Map<View, int[]> mCustomWidgets = new HashMap<>();
    private final List<SeparateQsWidgetInfo> mWidgetsList = new ArrayList<>();
    private QsTilePrefs qsTilePrefs;
    private PackageListAdapter mPackageAdapter;

    private final List<String> mAvailableWidgets = new ArrayList<>() {{
        add("photo");
        add("weather");
        add("w:weather");
        add("w:calculator");
        add("w:homecontrols");
        add("w:wallet");
        add("customapp:");
        /*add("device:batteryWidget");
        add("device:BluetoothWidget");*/
    }};

    private List<String> mSelectableWidgets = new ArrayList();

    final BroadcastReceiver mFeatureReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == null) return;
            if (intent.getAction().equals(BuildConfig.APPLICATION_ID + ".DEVICE_PROFILE_POST")) {
                int cellSize = intent.getIntExtra("cellSize", 0);
                int marginHorizontal = intent.getIntExtra("marginHorizontal", 0);
                int marginVertical = intent.getIntExtra("marginVertical", 0);
                binding.qsQuickEntranceContainer.qsFixTileContainer.setInts(cellSize, marginHorizontal, marginVertical);
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Thread(() -> mPackageAdapter = new PackageListAdapter(requireActivity())).start();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSeparateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MenuHost menuHost = requireActivity();
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                menu.add(0, 1, 0, R.string.add_widget)
                        .setIcon(R.drawable.ic_add)
                        .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                menu.add(0, 2, 0, "Save")
                        .setIcon(R.drawable.ic_save)
                        .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == 1) {
                    showAddWidget();
                } else if (menuItem.getItemId() == 2) {
                    try {
                        savePrefs();
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    return true;
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        setupMediaPanel();
        requireContext().registerReceiver(mFeatureReceiver, new IntentFilter(BuildConfig.APPLICATION_ID + ".DEVICE_PROFILE_POST"), Context.RECEIVER_EXPORTED);
        Intent intent = new Intent(BuildConfig.APPLICATION_ID + ".DEVICE_PROFILE_GET");
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        requireContext().sendBroadcast(intent);

        mRows = OCPreferences.getInt(SEPARATE_QS_ROWS, 3);
        qsTilePrefs = new QsTilePrefs();
        qsTilePrefs.setPreferenceListener(mSectionListener);
        getChildFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), qsTilePrefs).commit();
        try {
            loadPrefs();
            loadCustomWidgets();
            reloadTileView();
        } catch (JSONException e) {
            Log.e("SeparateQsPreview", "Error loading preferences", e);
        }
        getChildFragmentManager().registerFragmentLifecycleCallbacks(new FragmentManager.FragmentLifecycleCallbacks() {
            @Override
            public void onFragmentViewCreated(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull View v, @Nullable Bundle savedInstanceState) {
                if (f == qsTilePrefs) {
                    qsTilePrefs.setCustomWidgets(mCustomWidgets);
                }
            }
        }, false);
        buildList();
    }

    private void buildList() {
        mSelectableWidgets = mAvailableWidgets.stream()
                .filter(widget ->
                        mCustomWidgets.keySet().stream()
                                .map(view -> (String) view.getTag())
                                .noneMatch(widget::equals) || widget.startsWith("customapp:")
                )
                .collect(Collectors.toList());
    }

    private void loadPrefs() throws JSONException {
        String layoutCells = OCPreferences.getString(SEPARATE_QS_LAYOUT, "");
        if (TextUtils.isEmpty(layoutCells)) return;
        JSONObject json = new JSONObject(layoutCells);
        JSONArray mediaCell = json.getJSONArray("mediaCell");
        JSONArray h1Cell = json.getJSONArray("h1Cell");
        JSONArray h2Cell = json.getJSONArray("h2Cell");
        JSONArray brightnessCell = json.getJSONArray("brightnessCell");
        JSONArray volumeCell = json.getJSONArray("volumeCell");
        mMediaEnabled = json.getBoolean("mediaEnabled");
        mH1Enabled = json.getBoolean("h1Enabled");
        mH2Enabled = json.getBoolean("h2Enabled");
        mBrightnessEnabled = json.getBoolean("brightnessEnabled");
        mVolumeEnabled = json.getBoolean("volumeEnabled");
        mMediaCell = new int[]{
                mediaCell.getInt(0), mediaCell.getInt(1),
                mediaCell.getInt(2), mediaCell.getInt(3)
        };
        mH1Cell = new int[]{
                h1Cell.getInt(0), h1Cell.getInt(1),
                h1Cell.getInt(2), 1
        };
        mH2Cell = new int[]{
                h2Cell.getInt(0), h2Cell.getInt(1),
                h2Cell.getInt(2), 1
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

    private void loadCustomWidgets() throws JSONException {
        String customWidgets = OCPreferences.getString(SEPARATE_QS_WIDGETS, "");
        mWidgetsList.clear();
        if (TextUtils.isEmpty(customWidgets)) return;
        JSONObject root = new JSONObject(customWidgets);
        JSONArray widgetsArray = root.getJSONArray("widgets");
        for (int i = 0; i < widgetsArray.length(); i++) {
            JSONObject obj = widgetsArray.getJSONObject(i);
            String viewId = obj.getString("viewId");
            int x = obj.getInt("x");
            int y = obj.getInt("y");
            int width = obj.getInt("width");
            int height = obj.getInt("height");
            mWidgetsList.add(new SeparateQsWidgetInfo(viewId, x, y, width, height));
        }
        mCustomWidgets.clear();
        for (SeparateQsWidgetInfo wItem : mWidgetsList) {
            View widgetToAdd = SeparateQsWidgetsFactory.getWidget(wItem.viewId, requireContext(), true);
            widgetToAdd.setTag(wItem.viewId);
            widgetToAdd.setOnClickListener(this::showMenu);
            if (widgetToAdd instanceof QsCustomAppWidget customApp) {
                customApp.onSizeChanged(wItem.width);
            } else if (widgetToAdd instanceof QsMiniWeather miniWeather) {
                miniWeather.onSizeChanged(wItem.width);
            } if (widgetToAdd instanceof DeviceWidgetInterface deviceWidget) {
                deviceWidget.getWidget().setAdded(true);
                deviceWidget.getWidget().setTag(widgetToAdd.getTag());
                deviceWidget.getWidget().setWidgetClickListener(new BaseDeviceWidget.OnWidgetClick() {
                    @Override
                    public void onWidgetClick(BaseDeviceWidget widget) {}

                    @Override
                    public void onWidgetRemove(BaseDeviceWidget widget) {
                        removeWidget((String) widgetToAdd.getTag(), true);
                    }

                    @Override
                    public void onSettingsClicked(BaseDeviceWidget widget) {

                    }
                });
                deviceWidget.setMode(wItem.width >= 2 ? WidgetMode.BIG : WidgetMode.SMALL);
            }
            mCustomWidgets.put(widgetToAdd,
                    new int[]{wItem.x, wItem.y, wItem.width, wItem.height});
        }
    }

    private void savePrefs() throws JSONException {
        OCPreferences.putInt(SEPARATE_QS_ROWS, mRows);
        // Layout
        JSONObject json = new JSONObject();
        json.put("mediaEnabled", mMediaEnabled);
        json.put("h1Enabled", mH1Enabled);
        json.put("h2Enabled", mH2Enabled);
        json.put("brightnessEnabled", mBrightnessEnabled);
        json.put("volumeEnabled", mVolumeEnabled);
        json.put("mediaCell", new JSONArray(mMediaCell));
        json.put("h1Cell", new JSONArray(mH1Cell));
        json.put("h2Cell", new JSONArray(mH2Cell));
        json.put("brightnessCell", new JSONArray(mBrightnessCell));
        json.put("volumeCell", new JSONArray(mVolumeCell));
        OCPreferences.putString(SEPARATE_QS_LAYOUT, json.toString());
        // Widgets
        JSONArray jsonArray = new JSONArray();
        Map<View, int[]> widgetsMap = binding.qsQuickEntranceContainer.qsFixTileContainer.getCustomWidgetsMap();
        mWidgetsList.clear();
        for (Map.Entry<View, int[]> entry : widgetsMap.entrySet()) {
            View widget = entry.getKey();
            int[] cell = entry.getValue();
            String viewId = widget.getTag() != null ? widget.getTag().toString() : "";
            Log.d("SeparateQsPreview", "Saving widget: " + viewId);
            SeparateQsWidgetInfo wInfo = new SeparateQsWidgetInfo(viewId, cell[0], cell[1], cell[2], cell[3]);
            mWidgetsList.add(wInfo);
        }
        for (SeparateQsWidgetInfo wInfo : mWidgetsList) {
            JSONObject obj = new JSONObject();
            obj.put("viewId", wInfo.viewId);
            obj.put("x", wInfo.x);
            obj.put("y", wInfo.y);
            obj.put("width", wInfo.width);
            obj.put("height", wInfo.height);
            jsonArray.put(obj);
        }
        JSONObject root = new JSONObject();
        root.put("widgets", jsonArray);
        OCPreferences.putString(SEPARATE_QS_WIDGETS, root.toString());
    }

    private void showAddWidget() {

        List<String> widgetValues = mSelectableWidgets.stream()
                .map(this::getWidgetType)
                .collect(Collectors.toList());
        String[] filteredArray = widgetValues.toArray(new String[0]);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.add_widget);
        SummaryAdapter adapter = new SummaryAdapter(requireContext(), filteredArray);
        adapter.setTextCentered(false);
        builder.setAdapter(adapter, (dialog, which) -> {
            String widget = mSelectableWidgets.get(which);
            if (widget.startsWith("customapp")) {
                pickApp(widget);
                return;
            }
            View widgetToAdd = SeparateQsWidgetsFactory.getWidget(widget, requireContext(), true);
            widgetToAdd.setOnClickListener(this::showMenu);
            int[] specs =
                binding.qsQuickEntranceContainer.qsFixTileContainer.addWidget(widget, widgetToAdd, 1 ,1);
            if (Arrays.equals(specs, new int[]{-1, -1, -1, -1})) {
                Toast.makeText(requireContext(), "Not enough space!", Toast.LENGTH_SHORT).show();
                return;
            }
            qsTilePrefs.addCat(widget, getWidgetType(widget), specs);
            mCustomWidgets.put(widgetToAdd, specs);
            buildList();
        });
        builder.show();

    }

    private void showMenu(View view) {
        PopupMenu popup = new PopupMenu(requireContext(), view, Gravity.END);
        if (view instanceof QsPhotoShowcaseContainer) popup.getMenuInflater().inflate(R.menu.qs_widget_photo_menu, popup.getMenu());
        if (view instanceof QsWeatherWidget || view instanceof QsMiniWeather) {
            popup.getMenu().add(0, -2, 0, getString(R.string.weather_settings));
        }
        popup.getMenu().add(0, -1, 0, getString(R.string.remove_widget));

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == -1) {
                removeWidget((String) view.getTag(), true);
            } else if (item.getItemId() == -2) {
                replaceFragment(new WeatherSettings());
            } else if (item.getItemId() == R.id.set_photo_radius) {
                AppUtils.showPhotoShowcaseRadiusDialog(requireContext());
            } else if (item.getItemId() == R.id.set_photo) {
                pickImage();
            }
            return true;
        });
        popup.show();
    }

    public void pickImage() {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext());
        } else {
            Bundle bundle = new Bundle();
            CropImageOptions options = new CropImageOptions();
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;
            options.fixAspectRatio = true;
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
            FragmentCropImage fragmentCropImage = new FragmentCropImage();
            fragmentCropImage.setArguments(bundle);
            replaceFragment(fragmentCropImage);
        }
    }

    private void pickApp(String widget) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setAdapter(mPackageAdapter, (dialog, which) -> {
            PackageListAdapter.PackageItem info = mPackageAdapter.getItem(which);
            View widgetToAdd = SeparateQsWidgetsFactory.getWidget(widget + info.packageName, requireContext(), true);
            widgetToAdd.setOnClickListener(this::showMenu);
            int[] specs =
                    binding.qsQuickEntranceContainer.qsFixTileContainer.addWidget(widget + info.packageName, widgetToAdd, 1 ,1);
            if (Arrays.equals(specs, new int[]{-1, -1, -1, -1})) {
                Toast.makeText(requireContext(), "Not enough space!", Toast.LENGTH_SHORT).show();
                return;
            }
            String pkgName = info.packageName;
            qsTilePrefs.addCat(widget + info.packageName, getWidgetType(widget) + " " + AppUtils.getAppName(requireContext(), pkgName), specs);
            mCustomWidgets.put(widgetToAdd, specs);
        });
        builder.setCancelable(false);
        builder.setTitle(R.string.qs_widget_custom_app);
        builder.show();
    }


    private void removeWidget(String tag, boolean removeCat) {
        Log.d("SeparateQsPreview", "Removing widget: " + tag);
        if (tag == null) return;
        Log.d("SeparateQsPreview", "Removing widget: " + tag);
        for (Map.Entry<View, int[]> entry : mCustomWidgets.entrySet()) {
            View widget = entry.getKey();
            if (widget.getTag() != null && widget.getTag().equals(tag)) {
                Log.d("SeparateQsPreview", "Removing widget - widget found");
                mCustomWidgets.remove(widget);
                binding.qsQuickEntranceContainer.qsFixTileContainer.removeWidget((String) widget.getTag());
                break;
            }
        }
        if (removeCat) qsTilePrefs.removeCat(tag);
        buildList();
    }

    private String getWidgetType(String widget) {
        if (widget.equals("photo")) {
            return getString(R.string.qs_widget_photo);
        } else if (widget.equals("weather")) {
            return getString(R.string.qs_widget_weather);
        } else if (widget.equals("media")) {
            return getString(R.string.qs_widget_media_player);
        } else if (widget.equals("bluetooth")) {
            return getString(R.string.bt);
        } else if (widget.equals("calendar")) {
            return getString(R.string.calendar_widget);
        } else if (widget.equals("battery")) {
            return getString(R.string.battery_widget);
        }  else if (widget.contains(":")) {
            String[] split = widget.split(":");
            String wCat = split[0];
            if (wCat.startsWith("customapp")) {
                String title = getString(R.string.qs_widget_custom_app);
                if (split.length > 1) {
                    String packageName = split[1];
                    return title + "\n" + AppUtils.getAppName(requireActivity(), packageName);
                } else {
                    return title;
                }
            }
            String wType = split[1];
            switch (wType) {
                case "weather":
                    return getString(R.string.qs_widget_mini_weather);
                case "wifi":
                    return getString(R.string.wifi);
                case "data":
                    return getString(R.string.data);
                case "calculator":
                    return getString(R.string.calculator);
                case "torch":
                    return getString(R.string.torch);
                case "ringer":
                    return getString(R.string.ringer);
                case "bt":
                    return getString(R.string.bt);
                case "homecontrols":
                    return getString(R.string.home_controls);
                case "wallet":
                    return getString(R.string.wallet);
            }
        } else if (widget.contains("batteryWidget")) {
            return String.format(getString(R.string.widget_string), getString(R.string.battery_widget));
        } else if (widget.contains("BluetoothWidget")) {
            return String.format(getString(R.string.widget_string), getString(R.string.bt));
        }
        return widget;
    }

    private void reloadTileView() {
        binding.qsQuickEntranceContainer.qsFixTileContainer.setRows(mRows);
        binding.qsQuickEntranceContainer.qsFixTileContainer.setMediaCell(
                mMediaEnabled,
                mMediaCell[0], mMediaCell[1], mMediaCell[2], mMediaCell[3]
        );
        binding.qsQuickEntranceContainer.qsFixTileContainer.setFirstTileCell(
                mH1Enabled,
                mH1Cell[0], mH1Cell[1], mH1Cell[2], mH1Cell[3]
        );
        binding.qsQuickEntranceContainer.qsFixTileContainer.setSecondTileCell(
                mH2Enabled,
                mH2Cell[0], mH2Cell[1], mH2Cell[2], mH2Cell[3]
        );
        binding.qsQuickEntranceContainer.qsFixTileContainer.setBrightnessCell(
                mBrightnessEnabled,
                mBrightnessCell[0], mBrightnessCell[1], mBrightnessCell[2], mBrightnessCell[3]
        );
        binding.qsQuickEntranceContainer.qsFixTileContainer.setVolumeCell(
                mVolumeEnabled,
                mVolumeCell[0], mVolumeCell[1], mVolumeCell[2], mVolumeCell[3]
        );
        for (Map.Entry<View, int[]> entry : mCustomWidgets.entrySet()) {
            Log.d("SeparateQsPreview", "Adding widget: " + entry.getKey().toString());
            View widget = entry.getKey();
            if (widget.getParent() != null) {
                ((ViewGroup) widget.getParent()).removeView(widget);
            }
            int[] cell = entry.getValue();
            widget.setOnClickListener(this::showMenu);
            binding.qsQuickEntranceContainer.qsFixTileContainer.addWidget(widget, cell[0], cell[1], cell[2], cell[3]);
        }
        binding.qsQuickEntranceContainer.qsFixTileContainer.requestLayout();
        binding.qsQuickEntranceContainer.qsFixTileContainer.postInvalidate();
    }

    private void setupMediaPanel() {
        LinearLayout mediaPanel = binding.qsQuickEntranceContainer.qsMediaPanel.getRoot();
        ImageView mAppIcon = (ImageView) ViewHelper.findViewWithTag(mediaPanel, "app_icon");

        LinearLayout mDeviceParent = (LinearLayout) ViewHelper.findViewWithTag(mediaPanel, "media_output_switch_btn_parent");
        ImageView mDeviceIcon = new ImageView(requireContext());
        mDeviceIcon.setLayoutParams(mDeviceParent.getLayoutParams());
        mDeviceIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        mDeviceParent.addView(mDeviceIcon);

        TextView mTitle = (TextView) ViewHelper.findViewWithTag(mediaPanel, "media_panel_title");

        ImageView mPrev = (ImageView) ViewHelper.findViewWithTag(mediaPanel, "media_panel_action_pre");
        ImageView mPlayPause = (ImageView) ViewHelper.findViewWithTag(mediaPanel, "media_panel_action_play_or_pause");
        ImageView mNext = (ImageView) ViewHelper.findViewWithTag(mediaPanel, "media_panel_action_next");

        mAppIcon.setImageDrawable(getDrawable(MediaBinder.APP_ICON, SYSTEM_UI));
        mDeviceIcon.setImageDrawable(getDrawable(MediaBinder.DEVICE_ICON, SYSTEM_UI));
        mTitle.setText(getString(MediaBinder.DEFAULT_LABEL, SYSTEM_UI));
        mPrev.setImageDrawable(getDrawable(MediaBinder.PREV_ICON, SYSTEM_UI));
        mPlayPause.setImageDrawable(getDrawable(MediaBinder.PLAY_ICON, SYSTEM_UI));
        mNext.setImageDrawable(getDrawable(MediaBinder.NEXT_ICON, SYSTEM_UI));
        mDeviceIcon.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        mPrev.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        mPlayPause.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        mNext.setImageTintList(ColorStateList.valueOf(Color.WHITE));
        mediaPanel.setBackgroundResource(R.drawable.qs_tile_background);
    }

    private Drawable getDrawable(String resName, String pkg) {
        Context pkgContext = null;
        try {
            pkgContext = requireContext().createPackageContext(pkg, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {
        }
        Resources pkgRes = pkgContext.getResources();
        int resourceId = pkgRes.getIdentifier(resName, "drawable", pkg);
        if (resourceId != 0x0) {
            return ResourcesCompat.getDrawable(pkgRes, resourceId, pkgContext.getTheme());
        } else {
            return ResourcesCompat.getDrawable(requireContext().getResources(), R.drawable.ic_launcher_foreground, requireContext().getTheme());
        }
    }

    private String getString(String resName, String pkg) {
        Context pkgContext = null;
        try {
            pkgContext = requireContext().createPackageContext(pkg, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {
        }
        Resources pkgRes = pkgContext.getResources();
        int resourceId = pkgRes.getIdentifier(resName, "string", pkg);
        if (resourceId != 0x0) {
            return pkgRes.getString(resourceId);
        } else {
            return requireContext().getString(R.string.app_name);
        }
    }

    private QsTilePrefs.SectionListener mSectionListener = new QsTilePrefs.SectionListener() {
        @Override
        public void onPreferenceChanged(int rows,
                                        boolean mediaEnabled, boolean h1Enabled, boolean h2Enabled, boolean brightnessEnabled, boolean volumeEnabled,
                                        int[] mediaCell, int[] h1Cell, int[] h2Cell, int[] brightnessCell, int[] volumeCell) {
            reloadLayout(rows,
                    mediaEnabled, h1Enabled, h2Enabled, brightnessEnabled, volumeEnabled,
                    mediaCell, h1Cell, h2Cell, brightnessCell, volumeCell);
        }

        @Override
        public void onCustomWidgetsChanged(String widgetTag, int x, int y, int width, int height) {
            Log.d("SeparateQsPreview", "onCustomWidgetsChanged: " + widgetTag + ", " + x + ", " + y + ", " + width + ", " + height);
            reloadCustomWidget(widgetTag, x, y, width, height);
        }
    };

    private void reloadLayout(int rows,
                              boolean mediaEnabled, boolean h1Enabled, boolean h2Enabled, boolean brightnessEnabled, boolean volumeEnabled,
                              int[] mediaCell, int[] h1Cell, int[] h2Cell, int[] brightnessCell, int[] volumeCell) {
        mRows = rows;
        mMediaEnabled = mediaEnabled;
        mH1Enabled = h1Enabled;
        mH2Enabled = h2Enabled;
        mBrightnessEnabled = brightnessEnabled;
        mVolumeEnabled = volumeEnabled;
        mMediaCell = mediaCell;
        mH1Cell = h1Cell;
        mH2Cell = h2Cell;
        mBrightnessCell = brightnessCell;
        mVolumeCell = volumeCell;
        reloadTileView();
        Log.d("SeparateQsPreview", "Reloading layout" + "\n" +
                "Media: " + mediaCell[0] + ", " + mediaCell[1] + ", " + mediaCell[2] + ", " + mediaCell[3] + "\n" +
                "H1: " + h1Cell[0] + ", " + h1Cell[1] + ", " + h1Cell[2] + ", " + h1Cell[3] + "\n" +
                "H2: " + h2Cell[0] + ", " + h2Cell[1] + ", " + h2Cell[2] + ", " + h2Cell[3] + "\n" +
                "Brightness: " + brightnessCell[0] + ", " + brightnessCell[1] + ", " + brightnessCell[2] + ", " + brightnessCell[3] + "\n" +
                "Volume: " + volumeCell[0] + ", " + volumeCell[1] + ", " + volumeCell[2] + ", " + volumeCell[3]);
    }

    private void reloadCustomWidget(String widgetTag, int x, int y, int width, int height) {
        Iterator<Map.Entry<View, int[]>> iterator = mCustomWidgets.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<View, int[]> entry = iterator.next();
            View widget = entry.getKey();
            Log.d("SeparateQsPreview", "reloadCustomWidget: " + widget.getTag() + " searching for ");
            if (widget.getTag() != null && widget.getTag().equals(widgetTag)) {
                iterator.remove();
                binding.qsQuickEntranceContainer.qsFixTileContainer.removeView(widget);
                View v = SeparateQsWidgetsFactory.getWidget(widgetTag, requireContext(), true);
                if (v instanceof QsMiniWeather miniWeather) {
                    miniWeather.onSizeChanged(width);
                } else if (v instanceof QsCustomAppWidget customApp) {
                    customApp.onSizeChanged(width);
                } else if (v instanceof BaseQsWidget baseQsWidget) {
                    baseQsWidget.onSizeChanged(width);
                } else if (v instanceof DeviceWidgetInterface deviceWidget) {
                    deviceWidget.setMode(width >= 2 ? WidgetMode.BIG : WidgetMode.SMALL);
                }
                mCustomWidgets.put(
                        v,
                        new int[]{x, y, width, height}
                );
                Log.d("SeparateQsPreview", "Reloading custom widget: " + mCustomWidgets.toString());
                break;
            }
        }
        reloadTileView();
    }

    @Override
    public String getTitle() {
        return getString(R.string.qs_separate_layout);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    public static class QsTilePrefs extends OplusPreferenceFragment implements Preference.OnPreferenceChangeListener {

        PreferenceCategory mMediaPanelCat, mH1Cat, mH2Cat, mBrightnessCat, mVolumeCat;
        // Rows
        OplusSectionSeekbarPreference mRowsPref;
        // Enabled prefs
        OplusSwitchPreference mMediaVisible, mH1Visible, mH2Visible, mBrightnessVisible, mVolumeVisible;
        // Media
        OplusSectionSeekbarPreference mMediaX, mMediaY, mMediaSpanX, mMediaSpanY;
        // H1
        OplusSectionSeekbarPreference mH1X, mH1Y, mH1SpanX;
        // H2
        OplusSectionSeekbarPreference mH2X, mH2Y, mH2SpanX;
        // Brightness
        OplusSectionSeekbarPreference mBrightnessX, mBrightnessY, mBrightnessSpanX, mBrightnessSpanY;
        // Volume
        OplusSectionSeekbarPreference mVolumeX, mVolumeY, mVolumeSpanX, mVolumeSpanY;

        private final Map<Preference, CellType> prefMap = new HashMap<>();
        private final Map<String, Integer> prefKeyMap = new HashMap<>();

        public void setCustomWidgets(Map<View,int[]> mCustomWidgets) {
            for (Map.Entry<View, int[]> entry : mCustomWidgets.entrySet()) {
                View v = entry.getKey();
                addCat((String) v.getTag(), getWidgetType((String) v.getTag()), entry.getValue());
            }
        }

        public interface SectionListener {
            void onPreferenceChanged(
                    int rows,
                    boolean mediaEnabled, boolean h1Enabled, boolean h2Enabled, boolean brightnessEnabled, boolean volumeEnabled,
                    int[] mediaCell,
                    int[] h1Cell,
                    int[] h2Cell,
                    int[] brightnessCell,
                    int[] volumeCell
            );
            void onCustomWidgetsChanged(
                    String widgetTag,
                    int x,
                    int y,
                    int width,
                    int height
            );
        }

        private SectionListener mListener;

        public void setPreferenceListener(SectionListener listener) {
            mListener = listener;
        }

        public void addCat(String widget, String name, int[] specs) {
            OplusPreferenceCategory mCategory = new OplusPreferenceCategory(getAppContext());
            mCategory.setKey(widget);
            mCategory.setTitle(name);

            getPreferenceScreen().addPreference(mCategory);

            OplusSectionSeekbarPreference mX = createSeekbar("qs_separate_" + widget + "_x", "X", specs[0], 0, 4);
            OplusSectionSeekbarPreference mY = createSeekbar("qs_separate_" + widget + "_y", "Y", specs[1], 0, 4);
            OplusSectionSeekbarPreference mSpanX = createSeekbar("qs_separate_" + widget + "_span_x", getString(R.string.width), specs[2], 1, 4);
            OplusSectionSeekbarPreference mSpanY = createSeekbar("qs_separate_" + widget + "_span_y", getString(R.string.height), specs[3], 1, 4);

            mCategory.addPreference(mX);
            mCategory.addPreference(mY);
            mCategory.addPreference(mSpanX);
            mCategory.addPreference(mSpanY);

            mX.setOnPreferenceChangeListener(mWidgetListener);
            mY.setOnPreferenceChangeListener(mWidgetListener);
            mSpanX.setOnPreferenceChangeListener(mWidgetListener);
            mSpanY.setOnPreferenceChangeListener(mWidgetListener);

        }

        private final Preference.OnPreferenceChangeListener mWidgetListener = (preference, newValue) -> {

            String key = preference.getKey();
            if (key == null || !key.startsWith("qs_separate_")) return true;

            String tag = key.split("_")[2];

            prefKeyMap.put(key, (Integer) newValue);

            int x = getIntPreference("qs_separate_" + tag + "_x", 0);
            int y = getIntPreference("qs_separate_" + tag + "_y", 0);
            int width = getIntPreference("qs_separate_" + tag + "_span_x", 1);
            int height = getIntPreference("qs_separate_" + tag + "_span_y", 1);

            Log.d("SeparateQsPreview", "onPreferenceChange: " + tag + ", " + x + ", " + y + ", " + width + ", " + height + "\n" +
                    "Key: " + key + ", Value: " + newValue + "\n" +
                    "X: " + getIntPreference("qs_separate_" + tag + "_x", 0) + "\n" +
                    "Y: " + getIntPreference("qs_separate_" + tag + "_y", 0) + "\n" +
                    "Width: " + getIntPreference("qs_separate_" + tag + "_span_x", 1) + "\n" +
                    "Height: " + getIntPreference("qs_separate_" + tag + "_span_y", 1));

            if (mListener != null) {
                mListener.onCustomWidgetsChanged(tag, x, y, width, height);
            }

            return true;
        };

        private int getIntPreference(String key, int defaultValue) {
            if (prefKeyMap.containsKey(key)) {
                return prefKeyMap.get(key);
            }
            return defaultValue;
        }

        private OplusSectionSeekbarPreference createSeekbar(String key, String title, int defaultValue, int min, int max) {
            OplusSectionSeekbarPreference pref = new OplusSectionSeekbarPreference(requireContext());
            pref.setKey(key);
            pref.setTitle(title);
            pref.setDefaultValue(defaultValue);
            pref.setMin(min);
            pref.setMax(max);
            pref.setValue(defaultValue);
            pref.setPersistent(false);
            prefKeyMap.put(key, defaultValue);
            return pref;
        }

        public void removeCat(String widget) {
            OplusPreferenceCategory mCategory = findPreference(widget);
            if (mCategory != null) {
                getPreferenceScreen().removePreference(mCategory);
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            setPreferencesFromResource(R.xml.qs_separate_layout, rootKey);
            mRowsPref = findPreference("qs_separate_rows");
            // Media Panel
            mMediaPanelCat = findPreference("qs_separate_media_cat");
            mMediaVisible = findPreference("qs_separate_media_enabled");
            mMediaX = findPreference("qs_separate_media_x");
            mMediaY = findPreference("qs_separate_media_y");
            mMediaSpanX = findPreference("qs_separate_media_span_x");
            mMediaSpanY = findPreference("qs_separate_media_span_y");
            // H1 Tile
            mH1Cat = findPreference("qs_separate_ht_cat");
            mH1Visible = findPreference("qs_separate_h1_enabled");
            mH1X = findPreference("qs_separate_ht1_x");
            mH1Y = findPreference("qs_separate_ht1_y");
            mH1SpanX = findPreference("qs_separate_ht1_span_x");
            // H2 tile
            mH2Cat = findPreference("qs_separate_ht2_cat");
            mH2Visible = findPreference("qs_separate_h2_enabled");
            mH2X = findPreference("qs_separate_ht2_x");
            mH2Y = findPreference("qs_separate_ht2_y");
            mH2SpanX = findPreference("qs_separate_ht2_span_x");
            // Brightness
            mBrightnessCat = findPreference("qs_separate_brightness_cat");
            mBrightnessVisible = findPreference("qs_separate_brightness_enabled");
            mBrightnessX = findPreference("qs_separate_brightness_x");
            mBrightnessY = findPreference("qs_separate_brightness_y");
            mBrightnessSpanX = findPreference("qs_separate_brightness_span_x");
            mBrightnessSpanY = findPreference("qs_separate_brightness_span_y");
            // Volume
            mVolumeCat = findPreference("qs_separate_volume_cat");
            mVolumeVisible = findPreference("qs_separate_volume_enabled");
            mVolumeX = findPreference("qs_separate_volume_x");
            mVolumeY = findPreference("qs_separate_volume_y");
            mVolumeSpanX = findPreference("qs_separate_volume_span_x");
            mVolumeSpanY = findPreference("qs_separate_volume_span_y");

            mRowsPref.setValue(mRows);

            mMediaVisible.setChecked(mMediaEnabled);
            mMediaX.setValue(mMediaCell[0]);
            mMediaY.setValue(mMediaCell[1]);
            mMediaSpanX.setValue(mMediaCell[2]);
            mMediaSpanY.setValue(mMediaCell[3]);

            mH1Visible.setChecked(mH1Enabled);
            mH1X.setValue(mH1Cell[0]);
            mH1Y.setValue(mH1Cell[1]);
            mH1SpanX.setValue(mH1Cell[2]);

            mH2Visible.setChecked(mH2Enabled);
            mH2X.setValue(mH2Cell[0]);
            mH2Y.setValue(mH2Cell[1]);
            mH2SpanX.setValue(mH2Cell[2]);

            mBrightnessVisible.setChecked(mBrightnessEnabled);
            mBrightnessX.setValue(mBrightnessCell[0]);
            mBrightnessY.setValue(mBrightnessCell[1]);
            mBrightnessSpanX.setValue(mBrightnessCell[2]);
            mBrightnessSpanY.setValue(mBrightnessCell[3]);

            mVolumeVisible.setChecked(mVolumeEnabled);
            mVolumeX.setValue(mVolumeCell[0]);
            mVolumeY.setValue(mVolumeCell[1]);
            mVolumeSpanX.setValue(mVolumeCell[2]);
            mVolumeSpanY.setValue(mVolumeCell[3]);

            List<OplusSectionSeekbarPreference> seekbarPreferences = new ArrayList<>();
            seekbarPreferences.addAll(
                    Arrays.asList(mRowsPref, mMediaX, mMediaY, mMediaSpanX, mMediaSpanY,
                            mH1X, mH1Y, mH1SpanX,
                            mH2X, mH2Y, mH2SpanX,
                            mBrightnessX, mBrightnessY, mBrightnessSpanX, mBrightnessSpanY,
                            mVolumeX, mVolumeY, mVolumeSpanX, mVolumeSpanY)
            );
            for (OplusSectionSeekbarPreference seekbarPreference : seekbarPreferences) {
                seekbarPreference.setOnPreferenceChangeListener(this);
            }
            mMediaVisible.setOnPreferenceChangeListener(this);
            mH1Visible.setOnPreferenceChangeListener(this);
            mH2Visible.setOnPreferenceChangeListener(this);
            mBrightnessVisible.setOnPreferenceChangeListener(this);
            mVolumeVisible.setOnPreferenceChangeListener(this);
            mapPrefs();
        }

        private void mapPrefs() {
            prefMap.put(mRowsPref, CellType.ROWS);

            prefMap.put(mMediaVisible, CellType.MEDIA_ENABLED);
            prefMap.put(mH1Visible, CellType.H1_ENABLED);
            prefMap.put(mH2Visible, CellType.H2_ENABLED);
            prefMap.put(mBrightnessVisible, CellType.BRIGHTNESS_ENABLED);
            prefMap.put(mVolumeVisible, CellType.VOLUME_ENABLED);

            prefMap.put(mMediaX, CellType.MEDIA_X);
            prefMap.put(mMediaY, CellType.MEDIA_Y);
            prefMap.put(mMediaSpanX, CellType.MEDIA_SPAN_X);
            prefMap.put(mMediaSpanY, CellType.MEDIA_SPAN_Y);

            prefMap.put(mH1X, CellType.H1_X);
            prefMap.put(mH1Y, CellType.H1_Y);
            prefMap.put(mH1SpanX, CellType.H1_SPAN_X);

            prefMap.put(mH2X, CellType.H2_X);
            prefMap.put(mH2Y, CellType.H2_Y);
            prefMap.put(mH2SpanX, CellType.H2_SPAN_X);

            prefMap.put(mBrightnessX, CellType.BRIGHTNESS_X);
            prefMap.put(mBrightnessY, CellType.BRIGHTNESS_Y);
            prefMap.put(mBrightnessSpanX, CellType.BRIGHTNESS_SPAN_X);
            prefMap.put(mBrightnessSpanY, CellType.BRIGHTNESS_SPAN_Y);

            prefMap.put(mVolumeX, CellType.VOLUME_X);
            prefMap.put(mVolumeY, CellType.VOLUME_Y);
            prefMap.put(mVolumeSpanX, CellType.VOLUME_SPAN_X);
            prefMap.put(mVolumeSpanY, CellType.VOLUME_SPAN_Y);
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            int rows = mRowsPref.getValue();
            boolean mediaEnabled = mMediaEnabled;
            boolean h1Enabled = mH1Enabled;
            boolean h2Enabled = mH2Enabled;
            boolean brightnessEnabled = mBrightnessEnabled;
            boolean volumeEnabled = mVolumeEnabled;
            int[] media = {
                    mMediaX.getValue(), mMediaY.getValue(),
                    mMediaSpanX.getValue(), mMediaSpanY.getValue()
            };
            int[] h1 = {
                    mH1X.getValue(), mH1Y.getValue(),
                    mH1SpanX.getValue(), 1
            };
            int[] h2 = {
                    mH2X.getValue(), mH2Y.getValue(),
                    mH2SpanX.getValue(), 1
            };
            int[] brightness = {
                    mBrightnessX.getValue(), mBrightnessY.getValue(),
                    mBrightnessSpanX.getValue(), mBrightnessSpanY.getValue()
            };
            int[] volume = {
                    mVolumeX.getValue(), mVolumeY.getValue(),
                    mVolumeSpanX.getValue(), mVolumeSpanY.getValue()
            };

            CellType changed = prefMap.get(preference);
            if (changed != null) {
                switch (changed) {
                    case ROWS: rows = (int) newValue; break;

                    case MEDIA_ENABLED: mediaEnabled = (boolean) newValue; break;
                    case H1_ENABLED: h1Enabled = (boolean) newValue; break;
                    case H2_ENABLED: h2Enabled = (boolean) newValue; break;
                    case BRIGHTNESS_ENABLED: brightnessEnabled = (boolean) newValue; break;
                    case VOLUME_ENABLED: volumeEnabled = (boolean) newValue; break;

                    case MEDIA_X: media[0] = (int) newValue; break;
                    case MEDIA_Y: media[1] = (int) newValue; break;
                    case MEDIA_SPAN_X: media[2] = (int) newValue; break;
                    case MEDIA_SPAN_Y: media[3] = (int) newValue; break;

                    case H1_X: h1[0] = (int) newValue; break;
                    case H1_Y: h1[1] = (int) newValue; break;
                    case H1_SPAN_X: h1[2] = (int) newValue; break;

                    case H2_X: h2[0] = (int) newValue; break;
                    case H2_Y: h2[1] = (int) newValue; break;
                    case H2_SPAN_X: h2[2] = (int) newValue; break;

                    case BRIGHTNESS_X: brightness[0] = (int) newValue; break;
                    case BRIGHTNESS_Y: brightness[1] = (int) newValue; break;
                    case BRIGHTNESS_SPAN_X: brightness[2] = (int) newValue; break;
                    case BRIGHTNESS_SPAN_Y: brightness[3] = (int) newValue; break;

                    case VOLUME_X: volume[0] = (int) newValue; break;
                    case VOLUME_Y: volume[1] = (int) newValue; break;
                    case VOLUME_SPAN_X: volume[2] = (int) newValue; break;
                    case VOLUME_SPAN_Y: volume[3] = (int) newValue; break;
                }
            }

            if (mListener != null) {
                mListener.onPreferenceChanged(rows,
                        mediaEnabled, h1Enabled, h2Enabled, brightnessEnabled, volumeEnabled,
                        media, h1, h2, brightness, volume);
            }

            return true;
        }

        private String getWidgetType(String widget) {
            if (widget.equals("photo")) {
                return getString(R.string.qs_widget_photo);
            } else if (widget.equals("weather")) {
                return getString(R.string.qs_widget_weather);
            } else if (widget.equals("media")) {
                return getString(R.string.qs_widget_media_player);
            } else if (widget.contains(":")) {
                String[] split = widget.split(":");
                String wCat = split[0];
                if (wCat.startsWith("customapp")) {
                    String title = getString(R.string.qs_widget_custom_app);
                    if (split.length > 1) {
                        String packageName = split[1];
                        return title + "\n" + AppUtils.getAppName(requireActivity(), packageName);
                    } else {
                        return title;
                    }
                }
                String wType = split[1];
                switch (wType) {
                    case "weather":
                        return getString(R.string.qs_widget_mini_weather);
                    case "wifi":
                        return getString(R.string.wifi);
                    case "data":
                        return getString(R.string.data);
                    case "calculator":
                        return getString(R.string.calculator);
                    case "torch":
                        return getString(R.string.torch);
                    case "ringer":
                        return getString(R.string.ringer);
                    case "bt":
                        return getString(R.string.bt);
                    case "homecontrols":
                        return getString(R.string.home_controls);
                    case "wallet":
                        return getString(R.string.wallet);
                }
            } else if (widget.contains("batteryWidget")) {
                return String.format(getString(R.string.widget_string), getString(R.string.battery_widget));
            } else if (widget.contains("BluetoothWidget")) {
                return String.format(getString(R.string.widget_string), getString(R.string.bt));
            }
            return "";
        }


        public enum CellType {
            ROWS,
            MEDIA_ENABLED, H1_ENABLED, H2_ENABLED, BRIGHTNESS_ENABLED, VOLUME_ENABLED,
            MEDIA_X, MEDIA_Y, MEDIA_SPAN_X, MEDIA_SPAN_Y,
            H1_X, H1_Y, H1_SPAN_X,
            H2_X, H2_Y, H2_SPAN_X,
            BRIGHTNESS_X, BRIGHTNESS_Y, BRIGHTNESS_SPAN_X, BRIGHTNESS_SPAN_Y,
            VOLUME_X, VOLUME_Y, VOLUME_SPAN_X, VOLUME_SPAN_Y
        }

    }
    



}
