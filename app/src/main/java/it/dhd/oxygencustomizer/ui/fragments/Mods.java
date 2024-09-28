package it.dhd.oxygencustomizer.ui.fragments;


import static it.dhd.oxygencustomizer.ui.activity.MainActivity.backButtonDisabled;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.prefsList;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.preferences.OplusSwitchPreference;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchConfiguration;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreference;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreferenceResult;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.DarkMode;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.LagFixAppChooser;
import it.dhd.oxygencustomizer.ui.fragments.mods.sound.FluidSettings;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.xposed.hooks.framework.OplusStartingWindowManager;

public class Mods extends ControlledPreferenceFragmentCompat {

    SearchPreference searchPreference;

    @Override
    public String getTitle() {
        return Prefs.getBoolean(XPOSED_ONLY_MODE, true) ?
                getString(R.string.app_name) :
                getString(R.string.mods_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return !Prefs.getBoolean(XPOSED_ONLY_MODE, true);
    }

    @Override
    public int getLayoutResource() {
        return R.xml.mods;
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        searchPreference = findPreference("searchPreference");
        SearchConfiguration config = searchPreference.getSearchConfiguration();
        config.setActivity((AppCompatActivity) requireActivity());
        config.setFragmentContainerViewId(R.id.frame_layout);

        for (Object[] obj : prefsList) {
            config.index((Integer) obj[0]).addBreadcrumb(this.getResources().getString((Integer) obj[1]));
        }

        config.setBreadcrumbsEnabled(true);
        config.setHistoryEnabled(true);
        config.setFuzzySearchEnabled(false);
    }

    public void onSearchResultClicked(SearchPreferenceResult result) {
        Log.d("Mods", "onSearchResultClicked: " + result.getKey() + " " + result.getResourceFile() + " " + result.toString());
        if (result.getResourceFile() == R.xml.mods) {
            if (searchPreference != null) searchPreference.setVisible(false);
            SearchPreferenceResult.highlight(new Mods(), result.getKey());
        } else {
            for (Object[] obj : prefsList) {
                if ((Integer) obj[0] == result.getResourceFile()) {
                    replaceFragment((PreferenceFragmentCompat) obj[2]);
                    SearchPreferenceResult.highlight((PreferenceFragmentCompat) obj[2], result.getKey());
                    break;
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        backButtonDisabled();
    }

    public static class Sound extends ControlledPreferenceFragmentCompat {

        public Sound() {}

        @Override
        public String getTitle() {
            return getString(R.string.sound);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.sound_mods;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            Preference mFluid = findPreference("fluid_settings");
            if (mFluid != null) {
                mFluid.setOnPreferenceClickListener(preference -> {
                    replaceFragment(new FluidSettings());
                    return true;
                });
            }
        }
    }

    public static class VolumePanelCustomizations extends ControlledPreferenceFragmentCompat {

        public VolumePanelCustomizations() {}

        @Override
        public String getTitle() {
            return getString(R.string.volume_panel_custom_title);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.volume_panel_customizations;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }
    }

    public static class Misc extends ControlledPreferenceFragmentCompat {

        public Misc() {}

        @Override
        public String getTitle() {
            return getString(R.string.misc);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.misc_prefs;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            Preference mDarkMode = findPreference("dark_mode_prefs");
            mDarkMode.setOnPreferenceClickListener(preference -> {
                replaceFragment(new DarkMode());
                return true;
            });

            Preference mLagFix = findPreference("fix_lag_app_chooser");
            mLagFix.setOnPreferenceClickListener(preference -> {
                replaceFragment(new LagFixAppChooser());
                return true;
            });

        }

        private void checkOplusVersion() {
            String osVersion = Shell.cmd("getprop ro.build.display.id").exec().getOut().get(0);
            if (!TextUtils.isEmpty(osVersion)) {
                String[] split = osVersion.split("\\.");
                String version = split[split.length - 1].substring(0, split[split.length - 1].indexOf("("));
                Log.d("Misc OC", "Oplus version: " + version);
                if (Integer.parseInt(version) >= 610) {
                    Log.d("Misc OC", "Oplus version is greater than 610");
                    showConfirmDialog();
                } else {
                    Log.d("Misc OC", "Oplus version is less than 610");
                    sendIntent();
                }
            }
        }

        private void showConfirmDialog() {
            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
            builder.setTitle(R.string.warning)
                    .setMessage(R.string.fix_lag_dialog_message)
                    .setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                        mPreferences.putBoolean("fix_lag_switch", false);
                        ((OplusSwitchPreference)findPreference("fix_lag_switch")).setChecked(false);
                        dialog.dismiss();
                    })
                    .setPositiveButton(R.string.fix_lag_apply_anyway, (dialog, which) -> {
                        mPreferences.putBoolean("fix_lag_switch", true);
                        sendIntent();
                    })
                    .show();
        }

        @Override
        public void updateScreen(String key) {
            super.updateScreen(key);

            if (key == null) return;

            switch (key) {
                case "fix_lag_switch":
                    if (mPreferences.getBoolean("fix_lag_switch", false)) {
                        checkOplusVersion();
                    } else {
                        sendIntent();
                    }
                    break;
                case "fix_lag_force_all_apps":
                    sendIntent();
                    break;
            }
        }

        private void sendIntent() {
            Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);
            broadcast.putExtra("packageName", FRAMEWORK);
            broadcast.putExtra("class", OplusStartingWindowManager.class.getSimpleName());
            broadcast.setPackage(FRAMEWORK);
            if (getContext() != null)
                getContext().sendBroadcast(broadcast);
        }

    }

    public static class PackageManager extends ControlledPreferenceFragmentCompat {

        public PackageManager() {}

        @Override
        public String getTitle() {
            return getString(R.string.package_manager);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.package_manager_prefs;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }
    }

    public static class Aod extends ControlledPreferenceFragmentCompat {

        public Aod() {
        }

        @Override
        public String getTitle() {
            return getString(R.string.aod_title);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.aod_prefs;
        }

        @Override
        public boolean hasMenu() {
            return false;
        }

        @Override
        public String[] getScopes() {
            return null;
        }
    }

}
