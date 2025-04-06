package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentDeviceWidgetSettingsBinding;
import it.dhd.oxygencustomizer.ui.interfaces.PreferenceListener;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;

public class WidgetSettingsFragment extends Fragment {

    public interface OnApplyButtonListener {
        void onApplyClick();
    }

    private FragmentDeviceWidgetSettingsBinding binding;
    private BaseDeviceWidget mWidget;
    private WidgetSettingsPreferenceFragment mSettingsPreference;
    private OnApplyButtonListener mButtonListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeviceWidgetSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.applyButton.setOnClickListener(v -> {
            mWidget.applySettings(mSettingsPreference.getUpdatedSettings());
            if (mWidget == null || mButtonListener == null) return;
            mButtonListener.onApplyClick();
        });
    }

    public void setWidget(BaseDeviceWidget widget) {
        mWidget = widget;
        mSettingsPreference = new WidgetSettingsPreferenceFragment(mWidget.getCustomSettings());
        mSettingsPreference.setPreferenceChangeListener(mSettingsChangedListener);
        binding.toolbarPreference.setTitle(String.format(getString(R.string.widget_string), mWidget.getWidgetName()));
        getChildFragmentManager().beginTransaction().replace(binding.frameLayout.getId(), mSettingsPreference).commit();
    }

    private final PreferenceListener mSettingsChangedListener = new PreferenceListener() {
        @Override
        public void onPreferenceChanged() {
            mWidget.applySettings(mSettingsPreference.getUpdatedSettings());
        }
    };

    public void setApplyButtonListener(OnApplyButtonListener listener) {
        mButtonListener = listener;
    }

}
