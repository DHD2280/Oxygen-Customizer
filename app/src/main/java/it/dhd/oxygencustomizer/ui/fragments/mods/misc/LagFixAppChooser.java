package it.dhd.oxygencustomizer.ui.fragments.mods.misc;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArraySet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Set;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.AppAdapter;
import it.dhd.oxygencustomizer.ui.base.AppFragmentBase;
import it.dhd.oxygencustomizer.ui.models.AppModel;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.hooks.framework.OplusStartingWindowManager;

public class LagFixAppChooser extends AppFragmentBase {

    private Set<String> mEnabledApps;

    @Override
    public String getTitle() {
        return getString(R.string.fix_lag_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    public boolean hasMainSwitch() {
        return false;
    }

    @Override
    public String getFunctionTitle() {
        return "";
    }

    @Override
    public String getFunctionSummary() {
        return "";
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
        return false;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEnabledApps = mPreferences.getStringSet("lag_fix_apps", new ArraySet<>());
        binding.appFunctionSwitch.setVisibility(View.GONE);

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
        mPreferences.edit().putStringSet("lag_fix_apps", mEnabledApps).apply();


        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);
        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", OplusStartingWindowManager.class.getSimpleName());
        broadcast.setPackage(FRAMEWORK);
        if (getContext() != null)
            getContext().sendBroadcast(broadcast);
    }

}
