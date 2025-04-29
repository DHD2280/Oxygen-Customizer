package it.dhd.oxygencustomizer.ui.fragments;


import static it.dhd.oxygencustomizer.ui.activity.MainActivity.prefsList;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ClockPickerFragment;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.models.SearchPreferenceItem;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchConfiguration;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreference;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreferenceResult;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

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
        boolean isModuleInstalled = ModuleUtil.moduleExists();
        boolean isOverlayInstalled = OverlayUtil.overlayExists();

        return (isModuleInstalled && isOverlayInstalled);
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

        for (SearchPreferenceItem mItem : prefsList) {
            config.index(mItem.getXml()).addBreadcrumb(this.getResources().getString(mItem.getTitle()));
        }

        config.setBreadcrumbsEnabled(true);
        config.setHistoryEnabled(true);
        config.setFuzzySearchEnabled(false);
    }

    public void onSearchResultClicked(SearchPreferenceResult result) {
        if (result.getResourceFile() == R.xml.mods) {
            if (searchPreference != null) searchPreference.setVisible(false);
            SearchPreferenceResult.highlight(new Mods(), result.getKey());
        } else {
            for (SearchPreferenceItem mItem : prefsList) {
                if (mItem.getXml() == result.getResourceFile()) {
                    replaceFragment(mItem.getFragment());
                    if (mItem.getFragment() instanceof ClockPickerFragment clockPicker) {
                        ControlledPreferenceFragmentCompat fragment = clockPicker.getPreferenceFragment();
                        clockPicker.scrollToPreference();
                        SearchPreferenceResult.highlight(fragment, result.getKey());
                    } else {
                        SearchPreferenceResult.highlight((PreferenceFragmentCompat) mItem.getFragment(), result.getKey());
                    }
                    break;
                }
            }
        }
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
        public void updateScreen(String key) {
            super.updateScreen(key);

            if (key == null) {
                final boolean mAdaptivePlaybackEnabled = mPreferences.getBoolean("sound_adaptive_playback_main_switch", false);
                final int mAdaptivePlaybackTimeout = mPreferences.getInt("adaptive_playback_timeout", ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS);
                int jumpString = switch (mAdaptivePlaybackTimeout) {
                    case ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS -> R.string.adaptive_playback_timeout_30_secs;
                    case ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN -> R.string.adaptive_playback_timeout_1_min;
                    case ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN -> R.string.adaptive_playback_timeout_2_min;
                    case ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN -> R.string.adaptive_playback_timeout_5_min;
                    case ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN -> R.string.adaptive_playback_timeout_10_min;
                    default -> R.string.adaptive_playback_timeout_none;
                };
                ((OplusJumpPreference)findPreference("adaptive_playback")).setJumpText(
                        mAdaptivePlaybackEnabled ?
                                jumpString :
                                R.string.general_off);
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

        @Override
        public void updateScreen(String key) {
            super.updateScreen(key);

            OplusJumpPreference mAodClock = findPreference("aod_clock");
            OplusJumpPreference mAodWeather = findPreference("aod_weather");
            if (mAodClock != null) {
                mAodClock.setJumpText(mPreferences.getBoolean(AOD_CLOCK_SWITCH, false) ?
                        mPreferences.getInt(AOD_CLOCK_STYLE, 0) == 0 ? getString(R.string.clock_none) : String.format(getString(R.string.clock_style_name), mPreferences.getInt(AOD_CLOCK_STYLE, 0)) :
                        getString(R.string.general_off));
            }
            if (mAodWeather != null) {
                mAodWeather.setJumpText(mPreferences.getBoolean(AOD_WEATHER_SWITCH, false) ?
                        getString(R.string.general_on) :
                        getString(R.string.general_off));
            }

        }

    }

}
