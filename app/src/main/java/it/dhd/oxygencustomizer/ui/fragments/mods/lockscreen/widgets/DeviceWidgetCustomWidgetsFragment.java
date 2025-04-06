package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OplusRecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentDeviceWidgetPreviewsBinding;
import it.dhd.oxygencustomizer.ui.adapters.DeviceWidgetsAdapter;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.DeviceWidgetView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BatteryWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BluetoothWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.CalendarWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.SoundWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WeatherWidget;

public class DeviceWidgetCustomWidgetsFragment extends Fragment {

    public interface OnButtonsClick {
        void onApplyClick();
        void onCloseClick();
    }

    private CalendarWidget mCalendarWidget;
    private final DeviceWidgetsAdapter.WidgetSettingsListener mSettingsListener;
    private OnButtonsClick onButtonsClick;
    private FragmentDeviceWidgetPreviewsBinding binding;
    private final DeviceWidgetView mDeviceWidgetView;

    ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.READ_CALENDAR, false);
                        if ((fineLocationGranted != null && fineLocationGranted)) {
                            mCalendarWidget.setOnClickListener(null);
                            mCalendarWidget.getNextCalendarEvent();
                        }
                    }
            );

    private void requestCalendarPermission(ActivityResultLauncher<String[]> locationPermissionRequest) {
        if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CALENDAR)) {
            showApplicationPermissionDialog();
        } else {
            locationPermissionRequest.launch(new String[]{
                    Manifest.permission.READ_CALENDAR
            });
        }
    }

    private void showApplicationPermissionDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.weather_permission_dialog_title);
        builder.setMessage(R.string.weather_permission_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.show();
    }

    public DeviceWidgetCustomWidgetsFragment(DeviceWidgetView deviceWidgetView, DeviceWidgetsAdapter.WidgetSettingsListener listener) {
        mDeviceWidgetView = deviceWidgetView;
        mSettingsListener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceWidgetPreviewsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        List<BaseDeviceWidget> widgets = new ArrayList<>();
        widgets.add(new BatteryWidget(requireContext(), true));
        widgets.add(new BluetoothWidget(requireContext(), true));
        mCalendarWidget = new CalendarWidget(requireContext(), true);
        if (!AppUtils.hasPermission(Manifest.permission.READ_CALENDAR)) {
            mCalendarWidget.setOnClickListener(v -> requestCalendarPermission(requestPermissionLauncher));
        }
        widgets.add(mCalendarWidget);
        widgets.add(new WeatherWidget(requireContext(), true));
        widgets.add(new SoundWidget(requireContext(), true));
        binding.selectDialogListview.addItemDecoration(new OplusRecyclerView.OplusRecyclerViewItemDecoration(requireContext()));
        binding.selectDialogListview.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.selectDialogListview.setAdapter(new DeviceWidgetsAdapter(widgets, mDeviceWidgetView, mSettingsListener));
        binding.selectDialogListview.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        binding.applyButton.setOnClickListener(v-> {
            if (onButtonsClick != null) onButtonsClick.onApplyClick();
        });
        binding.closeButton.setOnClickListener(v-> {
            if (onButtonsClick != null) onButtonsClick.onCloseClick();
        });
    }

    public void setOnButtonsClick(OnButtonsClick onButtonsClick) {
        this.onButtonsClick = onButtonsClick;
    }

}
