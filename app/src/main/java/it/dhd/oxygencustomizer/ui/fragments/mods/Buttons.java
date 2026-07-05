package it.dhd.oxygencustomizer.ui.fragments.mods;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import android.content.Intent;
import android.os.Bundle;

import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.ActivitiesListAdapter;
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
    private ActivitiesListAdapter mActivitiesAdapter;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        // Initialize adapter for app list
        new Thread(() -> {
            mPackageAdapter = new PackageListAdapter(requireActivity());
            mActivitiesAdapter = new ActivitiesListAdapter(requireActivity());
        }).start();

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
                if (val.equals("app:")) {
                    // Standard launch
                    showAppPicker(key, "app");
                    return false;
                } else if (val.equals("activity:")) {
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
                saveAction(prefKey, "app:" + item.packageName);
            } else if (mode.equals("activity")) {
                // Show activities of package
                showActivityPicker(prefKey, item.title, item.packageName);
            }
        });
        builder.show();
    }

    private void showActivityPicker(String prefKey, CharSequence appName, String packageName) {
        mActivitiesAdapter.setPackageName(packageName);
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(appName)
                .setAdapter(mActivitiesAdapter, (acDialog, whichApp) -> {
                    String fullClassName = mActivitiesAdapter.getItem(whichApp).activityName;
                    // Save in format "pkg/class"
                    saveAction(prefKey, "activity:" + packageName + "/" + fullClassName);
                }).show();
    }

    private void saveAction(String key, String value) {
        mPreferences.putString(key, value);
        updateScreen(key);
    }
}
