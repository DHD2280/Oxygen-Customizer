package it.dhd.oxygencustomizer.ui.fragments.mods;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import android.content.Intent;
import android.content.pm.LauncherApps;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ShortcutInfo;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;

public class Buttons extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.buttons_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.buttons_prefs;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return null;
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", it.dhd.oxygencustomizer.xposed.hooks.framework.Buttons.class.getSimpleName());

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);

    }

    private PackageListAdapter mPackageAdapter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        // Initialize adapter for app list
        new Thread(() -> mPackageAdapter = new PackageListAdapter(requireActivity())).start();

        setupActionPreference("plusKey_single_press_button_action_value");
        setupActionPreference("plusKey_double_press_button_action_value");
        setupActionPreference("plusKey_triple_press_button_action_value");
        setupActionPreference("plusKey_long_press_button_action_value");
    }


    private void setupActionPreference(String key) {
        Preference pref = findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                String val = (String) newValue;
                if (val.equals("activity_picker")) {
                    // Standard launch
                    showAppPicker(key, "app");
                    return false;
                } else if (val.equals("full_activity_picker")) {
                    // Specific activity
                    showAppPicker(key, "activity");
                    return false;
                }
                return true; // For system actions (torch, dnd, etc.) save normally
            });
        }
    }

    private void showAppPicker(String prefKey, String mode) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setTitle(R.string.select_app);
        builder.setAdapter(mPackageAdapter, (dialog, which) -> {
            PackageListAdapter.PackageItem item = mPackageAdapter.getItem(which);

            if (mode.equals("app")) {
                // Save package name (PlusKeyActionHandler will use getLaunchIntentForPackage)
                saveAction(prefKey, item.packageName);
            } else if (mode.equals("activity")) {
                // Show activities of package
                showActivityPicker(prefKey, item.packageName);
            }
        });
        builder.show();
    }

    private void showActivityPicker(String prefKey, String packageName) {
        try {
            PackageManager pm = requireActivity().getPackageManager();

            PackageInfo info = pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);

            if (info.activities == null || info.activities.length == 0) {
                Toast.makeText(getContext(), "No activities found for this app", Toast.LENGTH_SHORT).show();
                return;
            }

            String[] names = new String[info.activities.length];
            for (int i = 0; i < info.activities.length; i++) {
                // Short name for readability
                String activityName = info.activities[i].name;
                names[i] = activityName.startsWith(packageName) ? activityName.substring(packageName.length()) : activityName;
            }

            new MaterialAlertDialogBuilder(requireActivity())
                    .setTitle(R.string.plusKey_select_activity)
                    .setItems(names, (dialog, which) -> {
                        String fullClassName = info.activities[which].name;
                        // Save in format "pkg/class"
                        saveAction(prefKey, packageName + "/" + fullClassName);
                    }).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error reading activities", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveAction(String key, String value) {
        mPreferences.putString(key, value);
        updateScreen(key);
    }
}
