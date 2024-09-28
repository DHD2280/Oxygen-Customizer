package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_CROP_KEY;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_FILE_URI;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_QS_PHOTO_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_PHOTO_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_LIST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsWidgetsPrefs.QS_WIDGETS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.QS_PHOTO_DIR;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentQsWidgetsBinding;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter.PackageItem;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dslv.DragSortController;
import it.dhd.oxygencustomizer.ui.dslv.DragSortListView;
import it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage;
import it.dhd.oxygencustomizer.ui.fragments.mods.WeatherSettings;
import it.dhd.oxygencustomizer.ui.widgets.SliderWidget;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.WeatherScheduler;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.weather.WeatherConfig;

public class QuickSettingsWidgets extends BaseFragment {

    private OmniJawsClient mWeatherClient;

    private FragmentQsWidgetsBinding binding;
    private WidgetListAdapter mWidgetAdapter;
    private DragSortListView mWidgetListView;
    private PackageListAdapter mPackageAdapter;
    private PackageManager mPackageManager;
    private List<String> mWidgetsList;
    private String mWidgets = "";
    private boolean mWidgetsEnabled = false;

    private final List<String> mAvailableWidgets = new ArrayList<>() {{
        add("photo");
        add("weather");
        add("w:weather");
        add("w:wifi");
        add("w:data");
        add("w:calculator");
        add("w:torch");
        add("w:ringer");
        add("w:bt");
        add("w:homecontrols");
        add("w:wallet");
        add("ca1:");
        add("ca2:");
        add("ca3:");
        add("ca4:");
    }};

    private List<String> mSelectableWidgets = new ArrayList<>();

    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);

    @Override
    public String getTitle() {
        return getString(R.string.quick_settings_widgets);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWeatherClient = new OmniJawsClient(getContext());
        mWeatherClient.queryWeather();

        new Thread(() -> {
            mPackageManager = requireActivity().getPackageManager();
            mPackageAdapter = new PackageListAdapter(requireActivity());
        }).start();

        requireActivity().getSupportFragmentManager()
                .setFragmentResultListener(DATA_CROP_KEY, this, (requestKey, result) -> {
                    String resultString = result.getString(DATA_FILE_URI);
                    String path = getRealPath(Uri.parse(resultString));
                    if (path != null && moveToOCHiddenDir(path, QS_PHOTO_DIR)) {
                        Intent updateImage = new Intent(ACTIONS_QS_PHOTO_CHANGED);
                        requireContext().sendBroadcast(updateImage);
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentQsWidgetsBinding.inflate(inflater, container, false);

        mWidgetListView = binding.widgetsList;

        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getPrefs();

        binding.qsWidgetsSwitch.setSwitchChecked(mWidgetsEnabled);
        binding.qsWidgetsSwitch.setSwitchChangeListener(((buttonView, isChecked) -> {
            mWidgetsEnabled = isChecked;
            mWidgetListView.setEnabled(isChecked);
            binding.addWidgets.setEnabled(isChecked);
            PreferenceHelper.getModulePrefs().edit().putBoolean(QS_WIDGETS_SWITCH, isChecked).apply();
            AppUtils.restartScope("systemui");
        }));

        mWidgetListView = view
                .findViewById(R.id.widgets_list);
        mWidgetAdapter = new WidgetListAdapter(getContext(), mWidgetsList);
        mWidgetListView.setAdapter(mWidgetAdapter);

        final DragSortController dragSortController = new WidgetDragSortController();
        mWidgetListView.setFloatViewManager(dragSortController);
        mWidgetListView
                .setDropListener((from, to) -> {
                    String packageName = mWidgetsList.remove(from);
                    mWidgetsList.add(to, packageName);
                    savePrefs();
                    mWidgetAdapter.notifyDataSetChanged();
                });
        mWidgetListView
                .setRemoveListener(which -> {
                    if (mWidgetsList.get(which).contains("media")) {
                        showTip();
                        mWidgetAdapter.notifyDataSetChanged();
                        return;
                    }
                    mWidgetsList.remove(which);
                    buildList();
                    savePrefs();
                    mWidgetAdapter.notifyDataSetChanged();
                });
        mWidgetListView.setOnTouchListener(dragSortController);
        mWidgetListView.setItemsCanFocus(false);
        mWidgetListView.setEnabled(mWidgetsEnabled);

        binding.addWidgets.setOnClickListener(v -> showAddWidget());
        binding.addWidgets.setEnabled(mWidgetsEnabled);

    }

    private void showTip() {
        Snackbar snackbar = Snackbar
                .make(binding.widgetsList, getString(R.string.qs_media_alert), Snackbar.LENGTH_LONG)
                .setAnchorView(binding.addWidgets);
        snackbar.show();
    }

    private void showAddWidget() {

        List<String> widgetValues = mSelectableWidgets.stream()
                .map(this::getWidgetType)
                .collect(Collectors.toList());
        String[] filteredArray = widgetValues.toArray(new String[0]);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.add_widget);
        builder.setItems(filteredArray, (dialog, which) -> {
            if (mSelectableWidgets.get(which).startsWith("ca")) {
                // Custom App Widget
                pickApp(mSelectableWidgets.get(which));
                return;
            }
            checkWeather(mSelectableWidgets.get(which));
            mWidgetsList.add(mSelectableWidgets.get(which));
            mSelectableWidgets.remove(which);
            mWidgetAdapter.notifyDataSetChanged();
            savePrefs();
        });
        builder.show();

    }

    private void pickApp(String widget) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setAdapter(mPackageAdapter, (dialog, which) -> {
            PackageItem info = mPackageAdapter.getItem(which);
            mWidgetsList.add(widget + info.packageName);
            mSelectableWidgets.remove(widget);
            Log.d("QuickSettingsWidgets", widget + " pickApp: " + info.title);
            mWidgetAdapter.notifyDataSetChanged();
            savePrefs();
        });
        builder.setTitle(R.string.qs_widget_custom_app);
        builder.show();
    }

    private void checkWeather(String widget) {
        if (widget.contains("weather")) {
            boolean wasWeatherEnabled = WeatherConfig.isEnabled(getContext());

            if (wasWeatherEnabled && mWeatherClient.getWeatherInfo() != null) {
                // Weather enabled but updated more than 1h ago
                if (System.currentTimeMillis() - mWeatherClient.getWeatherInfo().timeStamp > 3600000) {
                    WeatherScheduler.scheduleUpdateNow(getContext());
                }
            } else {
                // Weather not enabled so we will update now
                WeatherScheduler.scheduleUpdates(getContext());
                WeatherScheduler.scheduleUpdateNow(getContext());
            }
        }
    }

    private void getPrefs() {

        mWidgetsEnabled = PreferenceHelper.getModulePrefs().getBoolean(QS_WIDGETS_SWITCH, false);
        mWidgetsList = new ArrayList<>();
        mWidgets = PreferenceHelper.getModulePrefs().getString(QS_WIDGETS_LIST, "media");
        String[] widgetsArray = mWidgets.split(",");
        Collections.addAll(mWidgetsList, widgetsArray);

        buildList();
    }

    private void buildList() {
        mSelectableWidgets = mAvailableWidgets.stream()
                .filter(item -> {
                    if (item.startsWith("ca1:") || item.startsWith("ca2:") || item.startsWith("ca3:") || item.startsWith("ca4:")) {
                        return mWidgetsList.stream().noneMatch(widget -> widget.startsWith(item));
                    }
                    return !mWidgetsList.contains(item);
                })
                .collect(Collectors.toList());
    }

    private void savePrefs() {
        mWidgets = "";
        mWidgets = String.join(",", mWidgetsList);
        PreferenceHelper.getModulePrefs().edit().putString(QS_WIDGETS_LIST, mWidgets).apply();
    }

    private static class ViewHolder {
        TextView item;
    }

    public class WidgetListAdapter extends ArrayAdapter<String> {

        public WidgetListAdapter(Context context, List<String> values) {
            super(context, R.layout.widget_item, values);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            ViewHolder holder;
            if (convertView != null) {
                holder = (ViewHolder) convertView.getTag();
            } else {
                convertView = getLayoutInflater().inflate(
                        R.layout.widget_item, parent, false);
                holder = new ViewHolder();
                convertView.setTag(holder);

                holder.item = convertView
                        .findViewById(R.id.widget_item);
            }
            String widget = mWidgetsList.get(position);
            setPopUpMenu(holder.item, widget);
            holder.item.setText(getWidgetType(widget));
            return convertView;
        }

        private void setPopUpMenu(TextView item, String widget) {
            if (widget.equals("photo")) {
                item.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(getContext(), v, Gravity.END);
                    MenuInflater inflater = popup.getMenuInflater();
                    inflater.inflate(R.menu.qs_widget_photo_menu, popup.getMenu());
                    popup.setOnMenuItemClickListener(item1 -> {
                        if (item1.getItemId() == R.id.set_photo_radius) {
                            showRadiusDialog();
                        } else if (item1.getItemId() == R.id.set_photo) {
                            pickImage();
                        }
                        return true;
                    });
                    popup.show();
                });
            } else if (widget.equals("weather")) {
                item.setOnClickListener(v -> {
                    PopupMenu popup = new PopupMenu(getContext(), v, Gravity.END);
                    Menu menu = popup.getMenu();
                    menu.add(R.string.weather_settings);
                    popup.setOnMenuItemClickListener(item1 -> {
                        replaceFragment(new WeatherSettings());
                        return true;
                    });
                    popup.show();
                });
            } else {
                item.setOnClickListener(null);
            }

        }
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

    public void showRadiusDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        SliderWidget sliderWidget = new SliderWidget(requireContext());
        sliderWidget.setTitle(R.string.qs_widget_set_radius);
        sliderWidget.setSliderValue(PreferenceHelper.getModulePrefs().getInt(QS_PHOTO_RADIUS, 22));
        sliderWidget.setSliderValueFrom(0);
        sliderWidget.setSliderValueTo(50);
        builder.setView(sliderWidget);
        builder.setTitle(R.string.qs_widget_set_radius);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            int radius = sliderWidget.getSliderValue();
            PreferenceHelper.getModulePrefs().edit().putInt(QS_PHOTO_RADIUS, radius).apply();
        });
        builder.setNegativeButton(android.R.string.cancel, (dialog, which) -> {
            dialog.dismiss();
        });
        builder.setCancelable(false);
        builder.show();
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
            if (wCat.startsWith("ca")) {
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
        }
        return "";
    }

    private class WidgetDragSortController extends DragSortController {

        public WidgetDragSortController() {
            super(mWidgetListView, R.id.drag_handle,
                    DragSortController.ON_DOWN,
                    DragSortController.FLING_RIGHT_REMOVE);
            setRemoveEnabled(true);
            setSortEnabled(true);
            setBackgroundColor(0x363636);
        }

        @Override
        public void onDragFloatView(View floatView, Point floatPoint,
                                    Point touchPoint) {
            floatView.setLayoutParams(params);
            mWidgetListView.setFloatAlpha(0.8f);
        }

        @Override
        public View onCreateFloatView(int position) {
            View v = mWidgetAdapter.getView(position, null,
                    mWidgetListView);
            v.setLayoutParams(params);
            return v;
        }

        @Override
        public void onDestroyFloatView(View floatView) {
        }
    }


}
