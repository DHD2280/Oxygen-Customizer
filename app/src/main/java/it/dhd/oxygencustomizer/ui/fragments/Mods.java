package it.dhd.oxygencustomizer.ui.fragments;


import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContextLocale;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.prefsList;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_CROP_KEY;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_FILE_URI;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN;
import static it.dhd.oxygencustomizer.ui.fragments.mods.sound.AdaptivePlaybackSoundSettings.ADAPTIVE_PLAYBACK_TIMEOUT_NONE;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.SETTINGS_OTA_CARD_DIR;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;
import static it.dhd.oxygencustomizer.utils.ModuleConstants.XPOSED_ONLY_MODE;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oneplusui.preference.OplusSwitchPreference;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ClockPickerFragment;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.DarkMode;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.LagFixAppChooser;
import it.dhd.oxygencustomizer.ui.fragments.mods.sound.FluidSettings;
import it.dhd.oxygencustomizer.ui.models.SearchPreferenceItem;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchConfiguration;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreference;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreferenceResult;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
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

    public static class Misc extends ControlledPreferenceFragmentCompat {

        private LoadingDialog mLoadingDialog;

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
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            requireActivity().getSupportFragmentManager()
                    .setFragmentResultListener(DATA_CROP_KEY, this, (requestKey, result) -> {
                        String resultString = result.getString(DATA_FILE_URI);
                        String path = getRealPath(Uri.parse(resultString));
                        if (path != null && moveToOCHiddenDir(path, SETTINGS_OTA_CARD_DIR)) {
                            Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                        }
                    });

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            mLoadingDialog = new LoadingDialog(requireContext());

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

            OplusJumpPreference mOtaCardPicker = findPreference("ota_card_picker");
            mOtaCardPicker.setOnPreferenceClickListener(preference -> {
                pickImage();
                return true;
            });

        }


        public void pickImage() {
            if (!AppUtils.hasStoragePermission()) {
                AppUtils.requestStoragePermission(requireContext());
            } else {
                Bundle bundle = new Bundle();
                CropImageOptions options = new CropImageOptions();
                options.aspectRatioX = 82;
                options.aspectRatioY = 31;
                options.fixAspectRatio = true;
                bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
                FragmentCropImage fragmentCropImage = new FragmentCropImage();
                fragmentCropImage.setArguments(bundle);
                replaceFragment(fragmentCropImage);
            }
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
                case "enable_pocket_studio":
                    enablePocketStudio();
                    break;
            }
        }

        private void enablePocketStudio() {

            mLoadingDialog.show(getAppContextLocale().getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                // wait 500 millis
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ModuleUtil.enablePocketStudio(mPreferences.getBoolean("enable_pocket_studio", false));
                    // hide dialog
                    ((Activity) requireContext()).runOnUiThread(() -> {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            mLoadingDialog.hide();
                            Toast.makeText(OxygenCustomizer.getAppContext(), getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        }, 1000);
                    });

                }, 500);
            };

            Thread thread = new Thread(runnable);
            thread.start();
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
