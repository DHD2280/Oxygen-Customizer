package it.dhd.oxygencustomizer.ui.fragments.mods.statusbar;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CUSTOM_NOTIFICATION_APPS;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
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

public class StatusbarNotificationChoice extends AppFragmentBase {

    private Map<String, Integer> mEnabledApps;

    @Override
    public String getTitle() {
        return getString(R.string.notif_per_app);
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
        return false;
    }

    public boolean hasMainSwitch() {
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

        Set<String> enabledApps = mPreferences.getStringSet(CUSTOM_NOTIFICATION_APPS, new ArraySet<>());
        Log.d("StatusbarNotificationChoice", "onViewCreated: " + enabledApps.toString());
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

        new LoadAppsTask(getAppContext(), mEnabledApps, () -> {
            binding.searchView.setEnabled(false);
            binding.progress.setVisibility(View.VISIBLE);
        }, appList -> {
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(getAppContext()));
            binding.recyclerView.setAdapter(new AppAdapter(appList,
                    this::onMenuChange));
            binding.recyclerView.setHasFixedSize(true);
            binding.searchView.setEnabled(true);
            binding.progress.setVisibility(View.GONE);
            ((AppAdapter) binding.recyclerView.getAdapter()).showSystem(showSystem);
            binding.searchView.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    ((AppAdapter) binding.recyclerView.getAdapter()).filter(s.toString());
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        },
                new CharSequence[]{
                        getString(R.string.default_value),
                        getString(R.string.notif_always_expand),
                        getString(R.string.notif_always_collapse)
                },
                new CharSequence[]{
                        "0",
                        "1",
                        "2"
                }).execute();

    }

    private void onMenuChange(AppModel model, CharSequence value) {
        Log.d("StatusbarNotificationChoice", "onMenuChange: " + model.toString() + " value: " + value.toString());
        mEnabledApps.remove(model.getPackageName());
        mEnabledApps.put(model.getPackageName(), Integer.valueOf((String) value));
        savePrefs();
    }

    private void savePrefs() {
        Set<String> enabledApps = new HashSet<>();
        for (Map.Entry<String, Integer> entry : mEnabledApps.entrySet()) {
            enabledApps.add(entry.getKey() + "|" + entry.getValue());
        }
        Log.d("StatusbarNotificationChoice", "savePrefs: " + enabledApps.toString() + " size: " + enabledApps.size() + "");
        mPreferences.edit().putStringSet(CUSTOM_NOTIFICATION_APPS, enabledApps).apply();
    }

}
