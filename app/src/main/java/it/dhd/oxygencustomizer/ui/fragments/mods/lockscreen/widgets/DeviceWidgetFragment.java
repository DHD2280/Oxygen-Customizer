package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentDeviceWidgetOptionsBinding;
import it.dhd.oxygencustomizer.ui.adapters.DeviceWidgetsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.interfaces.PreferenceListener;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.ThemeUtils;
import it.dhd.oxygencustomizer.xposed.utils.WidgetFactory;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.DeviceWidgetView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;

public class DeviceWidgetFragment extends BaseFragment {

    private DeviceWidgetPreferences mPreferenceFragment;
    private FragmentDeviceWidgetOptionsBinding binding;
    private DeviceWidgetView mDeviceWidgetView;
    private BottomSheetBehavior<FrameLayout> mAddWidgetsSheet, mWidgetSettingsSheet;
    private WidgetSettingsFragment mWidgetSettingsFragment;
    private int mBottomSheetHeight;

    private final DeviceWidgetsAdapter.WidgetSettingsListener mSettingsListener = new DeviceWidgetsAdapter.WidgetSettingsListener() {
        @Override
        public void onWidgetSettingsClick(BaseDeviceWidget widget) {
            mWidgetSettingsFragment.setWidget(widget);
            mWidgetSettingsSheet.setMaxHeight(mBottomSheetHeight);
            mWidgetSettingsSheet.setPeekHeight(mBottomSheetHeight);
            mWidgetSettingsSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
    };

    private final PreferenceListener mListener = this::updatePrefs;

    private void updatePrefs() {
        if (mDeviceWidgetView == null) return;

        int style = Integer.parseInt(OCPreferences.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE, "0"));
        boolean customColor = OCPreferences.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false);
        int textColor = OCPreferences.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
                ResourcesCompat.getColor(getResources(), R.color.text_color_primary, requireContext().getTheme()));
        int circularProgressColor = OCPreferences.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR,
                ThemeUtils.getPrimaryColor(requireContext()));
        int linearProgressColor = OCPreferences.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR,
                ThemeUtils.getPrimaryColor(requireContext()));

        mDeviceWidgetView.setDeviceWidgetStyle(style);
        mDeviceWidgetView.setCustomColor(customColor, linearProgressColor, circularProgressColor);
        mDeviceWidgetView.setTextCustomColor(textColor);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                                         @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceWidgetOptionsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mDeviceWidgetView = new DeviceWidgetView(requireContext(), true);
        updatePrefs();
        mDeviceWidgetView.setWidgets(WidgetFactory.fromJson(OCPreferences.getString(LOCKSCREEN_WIDGETS_CUSTOM_DEVICE, "[]"), requireContext(), true));
        mDeviceWidgetView.setWidgetSettingsListener(mSettingsListener);
        binding.deviceWidgetContainer.addView(mDeviceWidgetView);

        mPreferenceFragment = new DeviceWidgetPreferences();
        mPreferenceFragment.setPreferenceListener(mListener);
        getChildFragmentManager().beginTransaction()
                .replace(binding.fragmentContainer.getId(), mPreferenceFragment)
                .commit();

        mDeviceWidgetView.setAddWidgetListener(mDeviceWidgetViewListener);

        setupBottomSheet();
        calculateBottomSheetHeight();
    }

    private void setupBottomSheet() {
        mAddWidgetsSheet = BottomSheetBehavior.from(binding.bottomSheet);
        mAddWidgetsSheet.setDraggable(false);
        mAddWidgetsSheet.setHideable(true);
        mAddWidgetsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        DeviceWidgetCustomWidgetsFragment fragment = new DeviceWidgetCustomWidgetsFragment(mDeviceWidgetView, mSettingsListener);
        fragment.setOnButtonsClick(mButtonsListener);
        getChildFragmentManager().beginTransaction()
                .replace(binding.bottomSheet.getId(), fragment)
                .commit();

        mWidgetSettingsFragment = new WidgetSettingsFragment();
        mWidgetSettingsFragment.setApplyButtonListener(mApplyListener);
        getChildFragmentManager().beginTransaction()
                .replace(binding.settingsBottomSheet.getId(), mWidgetSettingsFragment)
                .commit();

        mWidgetSettingsSheet = BottomSheetBehavior.from(binding.settingsBottomSheet);
        mWidgetSettingsSheet.setDraggable(false);
        mWidgetSettingsSheet.setHideable(true);
        mWidgetSettingsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);

        requireActivity().getOnBackPressedDispatcher().addCallback(mBackPressedCallback);

    }

    private final OnBackPressedCallback mBackPressedCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackPressed() {
            if (mWidgetSettingsSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mWidgetSettingsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                if (mAddWidgetsSheet.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    setEnabled(false);
                }
                return;
            }
            if (mAddWidgetsSheet.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                mAddWidgetsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                setEnabled(false);
            }
        }
    };

    private final DeviceWidgetView.DeviceWidgetListener mDeviceWidgetViewListener = new DeviceWidgetView.DeviceWidgetListener() {
        @Override
        public void onAddWidgetClicked() {
            mAddWidgetsSheet.setMaxHeight(mBottomSheetHeight);
            mAddWidgetsSheet.setPeekHeight(mBottomSheetHeight);
            mAddWidgetsSheet.setState(BottomSheetBehavior.STATE_EXPANDED);
            mBackPressedCallback.setEnabled(true);
        }

        @Override
        public void onWidgetRemoved(BaseDeviceWidget widget) {
            WidgetFactory.saveWidgets(mDeviceWidgetView.getWidgetsList());
        }
    };

    private final DeviceWidgetCustomWidgetsFragment.OnButtonsClick mButtonsListener = new DeviceWidgetCustomWidgetsFragment.OnButtonsClick() {
        @Override
        public void onApplyClick() {
            WidgetFactory.saveWidgets(mDeviceWidgetView.getWidgetsList());
            mAddWidgetsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            mBackPressedCallback.setEnabled(false);
        }

        @Override
        public void onCloseClick() {
            mAddWidgetsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
            mBackPressedCallback.setEnabled(false);
        }
    };

    private final WidgetSettingsFragment.OnApplyButtonListener mApplyListener = new WidgetSettingsFragment.OnApplyButtonListener() {
        @Override
        public void onApplyClick() {
            mWidgetSettingsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        calculateBottomSheetHeight();
    }

    private void calculateBottomSheetHeight() {
        binding.deviceWidgetContainer.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        binding.deviceWidgetContainer.getViewTreeObserver()
                                .removeOnGlobalLayoutListener(this);
                        int[] currentLocation = new int[2];
                        binding.deviceWidgetContainer.getLocationOnScreen(currentLocation);

                        mBottomSheetHeight = binding.coordinator.getHeight() - currentLocation[1];
                        ViewGroup.LayoutParams params = binding.bottomSheet.getLayoutParams();
                        params.height = mBottomSheetHeight;
                        binding.bottomSheet.setLayoutParams(params);
                        ViewGroup.LayoutParams layoutParams = binding.bottomSheet.getLayoutParams();
                        layoutParams.height = mBottomSheetHeight;
                        binding.bottomSheet.setLayoutParams(layoutParams);
                        binding.bottomSheet.requestLayout();

                        ViewGroup.LayoutParams layoutParams2 = binding.settingsBottomSheet.getLayoutParams();
                        layoutParams2.height = mBottomSheetHeight;
                        binding.settingsBottomSheet.setLayoutParams(layoutParams2);
                        binding.settingsBottomSheet.requestLayout();

                        mAddWidgetsSheet.setPeekHeight(mBottomSheetHeight);
                        mAddWidgetsSheet.setMaxHeight(mBottomSheetHeight);
                        mAddWidgetsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                        mWidgetSettingsSheet.setPeekHeight(mBottomSheetHeight);
                        mWidgetSettingsSheet.setMaxHeight(mBottomSheetHeight);
                        mWidgetSettingsSheet.setState(BottomSheetBehavior.STATE_HIDDEN);
                    }
                });
    }

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_device_widget_settings);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}
