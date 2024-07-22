package it.dhd.oxygencustomizer.ui.fragments;


import static it.dhd.oxygencustomizer.ui.activity.MainActivity.backButtonDisabled;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.prefsList;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.utils.AppUtils.doesClassExist;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.customprefs.preferencesearch.SearchConfiguration;
import it.dhd.oxygencustomizer.customprefs.preferencesearch.SearchPreference;
import it.dhd.oxygencustomizer.customprefs.preferencesearch.SearchPreferenceResult;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.DarkMode;
import it.dhd.oxygencustomizer.ui.fragments.mods.sound.FluidSettings;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.Prefs;

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

        public Sound() {

        }

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

        public VolumePanelCustomizations() {

        }

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

        public Misc() {

        }

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
        }

    }

    public static class Screenshot extends ControlledPreferenceFragmentCompat {

        public Screenshot() {

        }

        @Override
        public String getTitle() {
            return getString(R.string.screenshot);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.screenshots_prefs;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SCREENSHOT};
        }

        @Override
        public void updateScreen(String key) {
            super.updateScreen(key);
            Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

            broadcast.putExtra("packageName", FRAMEWORK);
            broadcast.putExtra("class", it.dhd.oxygencustomizer.xposed.hooks.framework.PhoneWindowManager.class.getSimpleName());

            broadcast.setPackage(FRAMEWORK);

            if (getContext() != null)
                getContext().sendBroadcast(broadcast);

        }
    }

    public static class PackageManager extends ControlledPreferenceFragmentCompat {

        public PackageManager() {

        }

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
