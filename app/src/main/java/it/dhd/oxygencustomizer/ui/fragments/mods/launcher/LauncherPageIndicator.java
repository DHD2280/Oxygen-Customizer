package it.dhd.oxygencustomizer.ui.fragments.mods.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;

import android.os.Bundle;

import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.ActivitiesListAdapter;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class LauncherPageIndicator extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.custom_page_indicator_tap_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.launcher_pageindicator_pref;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{LAUNCHER};
    }

    private PackageListAdapter mPackageAdapter;
    private ActivitiesListAdapter mActivitiesAdapter;

    private final String mPrefKey = "launcher_page_indicator_tap_launch";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        // Initialize adapter for app list
        new Thread(() -> {
            mPackageAdapter = new PackageListAdapter(requireActivity());
            mActivitiesAdapter = new ActivitiesListAdapter(requireActivity());
        }).start();

        Preference pref = findPreference(mPrefKey);
        if (pref != null) {
            pref.setOnPreferenceChangeListener((preference, newValue) -> {
                String val = (String) newValue;
                if (val.equals("app:")) {
                    // Standard launch
                    showAppPicker("app");
                    return false;
                } else if (val.equals("activity:")) {
                    // Specific activity
                    showAppPicker("activity");
                    return false;
                }
                return true;
            });
        }
    }


    private void showAppPicker(String mode) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setTitle(R.string.select_app);
        builder.setAdapter(mPackageAdapter, (dialog, which) -> {
            PackageListAdapter.PackageItem item = mPackageAdapter.getItem(which);

            if (mode.equals("app")) {
                // Save package name
                saveAction("app:" + item.packageName);
            } else if (mode.equals("activity")) {
                // Show activities of package
                showActivityPicker(item.title, item.packageName);
            }
        });
        builder.show();
    }

    private void showActivityPicker(CharSequence appName, String packageName) {
        mActivitiesAdapter.setPackageName(packageName);
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(appName)
                .setAdapter(mActivitiesAdapter, (acDialog, whichApp) -> {
                    String fullClassName = mActivitiesAdapter.getItem(whichApp).activityName;
                    // Save in format "pkg/class"
                    saveAction("activity:" + packageName + "/" + fullClassName);
                }).show();
    }

    private void saveAction(String value) {
        mPreferences.putString(mPrefKey, value);
    }

}