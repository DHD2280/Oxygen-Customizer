package it.dhd.oxygencustomizer.ui.fragments.mods.sound;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArraySet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.HashSet;
import java.util.Set;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.AppAdapter;
import it.dhd.oxygencustomizer.ui.base.AppFragmentBase;
import it.dhd.oxygencustomizer.ui.models.AppModel;

public class FluidSettings extends AppFragmentBase {

    private Set<String> mEnabledApps;

    @Override
    public String getTitle() {
        return getString(R.string.fluid_music);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public String getFunctionTitle() {
        return getString(R.string.fluid_music);
    }

    @Override
    public String getFunctionSummary() {
        return getString(R.string.fluid_music_custom_list_summary);
    }

    @Override
    public boolean hasQuickLaunch() {
        return false;
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
        return new String[]{SYSTEM_UI};
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEnabledApps = mPreferences.getStringSet("fluid_music_apps", new ArraySet<>());

        binding.appFunctionSwitch.setSwitchChangeListener((buttonView, isChecked) -> mPreferences.edit().putBoolean("fluid_music_custom_switch", isChecked).apply());
        binding.appFunctionSwitch.setSwitchChecked(mPreferences.getBoolean("fluid_music_custom_switch", false));

        new LoadAppsTask(getAppContext(), mEnabledApps, false, () -> {
            binding.searchViewLayout.setEnabled(false);
            binding.progress.setVisibility(View.VISIBLE);
        }, appList -> {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));
            binding.recyclerView.setAdapter(new AppAdapter(appList,
                    this::onSwitchChange));
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
        if (mEnabledApps.contains(model.getPackageName())) {
            if (!isChecked) mEnabledApps.remove(model.getPackageName());
        } else {
            mEnabledApps.add(model.getPackageName());
        }
        savePrefs();
    }

    private void savePrefs() {
        Set<String> enabledApps = new HashSet<>();
        mPreferences.edit().putStringSet("fluid_music_apps", mEnabledApps).apply();
    }
}
