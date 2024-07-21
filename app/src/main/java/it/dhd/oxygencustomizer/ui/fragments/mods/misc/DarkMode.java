package it.dhd.oxygencustomizer.ui.fragments.mods.misc;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.AppAdapter;
import it.dhd.oxygencustomizer.ui.base.AppFragmentBase;
import it.dhd.oxygencustomizer.ui.models.AppModel;
import it.dhd.oxygencustomizer.utils.Constants;

public class DarkMode extends AppFragmentBase {

    private Map<String, Integer> mEnabledApps;

    @Override
    public String getTitle() {
        return getString(R.string.dark_mode);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public String getFunctionTitle() {
        return getString(R.string.custom_dark_mode_title);
    }

    @Override
    public String getFunctionSummary() {
        return getString(R.string.custom_dark_mode_summary);
    }

    @Override
    public boolean hasQuickLaunch() {
        return true;
    }

    @Override
    public String getQuickLaunchIntent() {
        return "com.android.settings.DISPLAY_SETTINGS";
    }

    @Override
    public OnShowSystemChange getShowSystemChange() {
        return showSystem -> ((AppAdapter) binding.recyclerView.getAdapter()).showSystem(showSystem);
    }

    @Override
    public boolean hasRestartScopes() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SETTINGS};
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Set<String> enabledApps = mPreferences.getStringSet("custom_dark_mode", new ArraySet<>());
        mEnabledApps = new ArrayMap<>();
        for (String item : enabledApps) {
            if (item.contains("|")) {
                List<String> arr = new ArrayList<>(Arrays.asList(item.split("\\|")));
                if (arr.size() < 2 || arr.get(1).isBlank()) {
                    arr.set(1, "0");
                }
                mEnabledApps.put(arr.get(0), Integer.parseInt(arr.get(1)));
            } else {
                mEnabledApps.put(item, 0);
            }
        }

        binding.appFunctionSwitch.setSwitchChangeListener((buttonView, isChecked) -> mPreferences.edit().putBoolean("custom_dark_mode_switch", isChecked).apply());
        binding.appFunctionSwitch.setSwitchChecked(mPreferences.getBoolean("custom_dark_mode_switch", false));

        new LoadAppsTask(getAppContext(), mEnabledApps, true, () -> {
            binding.searchViewLayout.setEnabled(false);
            binding.progress.setVisibility(View.VISIBLE);
        }, appList -> {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));
            binding.recyclerView.setAdapter(new AppAdapter(appList,
                    this::onSwitchChange,
                    this::onSliderChange));
            binding.recyclerView.setHasFixedSize(true);
            binding.searchViewLayout.setEnabled(true);
            binding.progress.setVisibility(View.GONE);
            ((AppAdapter) binding.recyclerView.getAdapter()).showSystem(showSystem);
            binding.searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((AppAdapter) binding.recyclerView.getAdapter()).filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {}
            });
        }).execute();

    }

    private void onSwitchChange(AppModel model, boolean isChecked) {
        model.setEnabled(isChecked);
        if (mEnabledApps.containsKey(model.getPackageName())) {
            if (!isChecked) mEnabledApps.remove(model.getPackageName());
        } else {
            mEnabledApps.put(model.getPackageName(), model.getDarkModeValue());
        }
        savePrefs();
    }

    private void onSliderChange(AppModel model, int progress) {
        if (mEnabledApps.containsKey(model.getPackageName())) {
            mEnabledApps.put(model.getPackageName(), progress);
        }
        savePrefs();
    }

    private void savePrefs() {
        Set<String> enabledApps = new HashSet<>();
        for (Map.Entry<String, Integer> entry : mEnabledApps.entrySet()) {
            enabledApps.add(entry.getKey() + "|" + entry.getValue());
        }
        mPreferences.edit().putStringSet("custom_dark_mode", enabledApps).apply();

        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", it.dhd.oxygencustomizer.xposed.hooks.framework.DarkMode.class.getSimpleName());

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);
    }

}
