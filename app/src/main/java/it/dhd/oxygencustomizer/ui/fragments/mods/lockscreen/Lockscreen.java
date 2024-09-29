package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_DEPTH_BACKGROUND_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_DEPTH_SUBJECT_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_FONT_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_FINGERPRINT_FILE;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_CARRIER_REPLACEMENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_CAPSULE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_CARRIER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_STATUSBAR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.getLockScreenBitmapCachePath;
import static it.dhd.oxygencustomizer.utils.Constants.getLockScreenSubjectCachePath;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.ui.preferences.OplusSwitchPreference;
import it.dhd.oxygencustomizer.ui.preferences.OplusRecyclerPreference;
import it.dhd.oxygencustomizer.ui.preferences.dialogadapter.ListPreferenceAdapter;
import it.dhd.oxygencustomizer.ui.adapters.ClockPreviewAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.DateFormatDialog;
import it.dhd.oxygencustomizer.ui.models.ClockModel;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.BitmapSubjectSegmenter;

public class Lockscreen extends ControlledPreferenceFragmentCompat {

    private DateFormatDialog mDateFormatDialog;

    private final int PICK_FP_ICON = 0;
    private final int PICK_DEPTH_BACKGROUND = 1;
    private final int PICK_DEPTH_SUBJECT = 2;
    private int mPick = -1;

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_prefs;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SYSTEM_UI};
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mDateFormatDialog = new DateFormatDialog(requireContext());

        ListWithPopUpPreference mLockscreenFpIcons = findPreference(LOCKSCREEN_FINGERPRINT_STYLE);
        int maxIndex = 0;
        List<String> fpIconsEntries = new ArrayList<>(), fpIconsValues = new ArrayList<>();
        List<Drawable> fpIconsDrawables = new ArrayList<>();
        while (requireContext()
                .getResources()
                .getIdentifier(
                        "fingerprint_" + maxIndex,
                        "drawable",
                        BuildConfig.APPLICATION_ID
                ) != 0) {
            maxIndex++;
        }

        for (int i = 0; i < maxIndex; i++) {
            fpIconsEntries.add("Fingerprint " + i);
            fpIconsValues.add(String.valueOf(i));
            fpIconsDrawables.add(
                    ResourcesCompat.getDrawable(
                            requireContext().getResources(),
                            requireContext().getResources().getIdentifier(
                                    "fingerprint_" + i,
                                    "drawable",
                                    BuildConfig.APPLICATION_ID
                            ),
                            requireContext().getTheme()
                    ));
        }
        if (mLockscreenFpIcons != null) {
            mLockscreenFpIcons.setEntries(fpIconsEntries.toArray(new CharSequence[0]));
            mLockscreenFpIcons.setEntryValues(fpIconsValues.toArray(new CharSequence[0]));
            mLockscreenFpIcons.createDefaultAdapter(fpIconsDrawables.toArray(new Drawable[0]));
            mLockscreenFpIcons.setAdapterType(ListPreferenceAdapter.TYPE_BATTERY_ICONS);
        }

        Preference mFingerprintPicker = findPreference("lockscreen_fp_icon_picker");
        if (mFingerprintPicker != null) {
            mFingerprintPicker.setOnPreferenceClickListener(preference -> {
                mPick = PICK_FP_ICON;
                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    launchFilePicker(pickImageIntent, "image/*");
                }
                return true;
            });
        }

        Preference mDepthBackground = findPreference("DWBackground");
        if (mDepthBackground != null) {
            mDepthBackground.setOnPreferenceClickListener(preference -> {
                mPick = PICK_DEPTH_BACKGROUND;
                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    launchFilePicker(pickImageIntent, "image/*");
                }
                return true;
            });
        }

        Preference mDepthSubject = findPreference("DWSubject");
        if (mDepthSubject != null) {
            mDepthSubject.setOnPreferenceClickListener(preference -> {
                mPick = PICK_DEPTH_SUBJECT;
                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    launchFilePicker(pickImageIntent, "image/*");
                }
                return true;
            });
        }

        OplusSwitchPreference hideCarrier, hideCapsule, hideStatusbar;
        hideCarrier = findPreference(LOCKSCREEN_HIDE_CARRIER);
        hideCapsule = findPreference(LOCKSCREEN_HIDE_CAPSULE);
        hideStatusbar = findPreference(LOCKSCREEN_HIDE_STATUSBAR);
        OnPreferenceChangeListener listener = (preference, newValue) -> {
            AppUtils.restartScope(SYSTEM_UI);
            return true;
        };
        if (hideCarrier != null) {
            hideCarrier.setOnPreferenceChangeListener(listener);
        }
        if (hideCapsule != null) {
            hideCapsule.setOnPreferenceChangeListener(listener);
        }
        if (hideStatusbar != null) {
            hideStatusbar.setOnPreferenceChangeListener(listener);
        }

        Preference mLsCarrierText = findPreference("ls_carrier_replacement");
        if (mLsCarrierText != null) {
            mLsCarrierText.setOnPreferenceClickListener(preference -> {
                mDateFormatDialog.show(
                        getString(R.string.lockscreen_carrier_replacement),
                        mPreferences.getString(LOCKSCREEN_CARRIER_REPLACEMENT, ""),
                        (text) -> mPreferences.edit().putString(LOCKSCREEN_CARRIER_REPLACEMENT, text.toString()).apply());
                return true;
            });
        }

        new BitmapSubjectSegmenter(getActivity()).checkModelAvailability(moduleAvailabilityResponse ->
                findPreference("DWallpaperEnabled")
                        .setSummary(
                                moduleAvailabilityResponse.areModulesAvailable()
                                        ? R.string.depth_wallpaper_model_ready
                                        : R.string.depth_wallpaper_model_not_available));

    }

    ActivityResultLauncher<Intent> pickImageIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    String dest = switch (mPick) {
                        case PICK_FP_ICON -> LOCKSCREEN_FINGERPRINT_FILE;
                        case PICK_DEPTH_BACKGROUND -> getLockScreenBitmapCachePath();
                        case PICK_DEPTH_SUBJECT -> getLockScreenSubjectCachePath();
                        default -> "";
                    };

                    if (path != null && moveToOCHiddenDir(path, dest)) {
                        switch (mPick) {
                            case PICK_FP_ICON:
                                mPreferences.edit().putString(LOCKSCREEN_FINGERPRINT_STYLE, "-1").apply();
                                break;
                            case PICK_DEPTH_BACKGROUND:
                                sendIntent(ACTION_DEPTH_BACKGROUND_CHANGED);
                                break;
                            case PICK_DEPTH_SUBJECT:
                                sendIntent(ACTION_DEPTH_SUBJECT_CHANGED);
                                break;
                        }
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void sendIntent(String action) {
        Intent intent = new Intent(action);
        intent.putExtra("packageName", SYSTEM_UI);
        requireContext().sendBroadcast(intent);
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        switch (key) {
            case "DWallpaperEnabled":
                try {
                    boolean DepthEffectEnabled = mPreferences.getBoolean("DWallpaperEnabled", false);

                    if (DepthEffectEnabled) {
                        new MaterialAlertDialogBuilder(getContext())
                                .setTitle(R.string.depth_effect_alert_title)
                                .setMessage(getString(R.string.depth_effect_alert_body, getString(R.string.sysui_restart_needed)))
                                .setPositiveButton(R.string.depth_effect_ok_btn, (dialog, which) -> AppUtils.restartScope("systemui"))
                                .setCancelable(false)
                                .show();
                    }
                } catch (Exception ignored) {
                }
                break;
        }
    }

    public static class LockscreenClock extends ControlledPreferenceFragmentCompat {
        @Override
        public String getTitle() {
            return getString(R.string.lockscreen_clock);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.lockscreen_clock;
        }
        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{SYSTEM_UI};
        }
        private int type = 0;

        ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        String path = getRealPath(data);
                        String destination = "";
                        if (type == 0)
                            destination = LOCKSCREEN_USER_IMAGE;
                        else if (type == 2)
                            destination = LOCKSCREEN_CUSTOM_IMAGE;
                        else
                            destination = LOCKSCREEN_CLOCK_FONT_DIR;

                        if (path != null && moveToOCHiddenDir(path, destination)) {
                            if (Objects.equals(destination, LOCKSCREEN_CLOCK_FONT_DIR)) {
                                mPreferences.edit().putBoolean(LOCKSCREEN_CLOCK_CUSTOM_FONT, false).apply();
                                mPreferences.edit().putBoolean(LOCKSCREEN_CLOCK_CUSTOM_FONT, true).apply();
                            }
                            Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            OplusRecyclerPreference mLockscreenClockStyles = findPreference("lockscreen_clock_custom");
            if (mLockscreenClockStyles != null) {
                mLockscreenClockStyles.setAdapter(initLockscreenClockStyles());
                mLockscreenClockStyles.setPreference(LOCKSCREEN_CLOCK_STYLE, 0);
            }

            Preference mLockscreenUserImage = findPreference("lockscreen_clock_custom_user_image_picker");
            if (mLockscreenUserImage != null) {
                mLockscreenUserImage.setOnPreferenceClickListener(preference -> {
                    pick("image");
                    type = 0;
                    return true;
                });
            }

            Preference mLockscreenCustomFont = findPreference("lockscreen_clock_font_custom");
            if (mLockscreenCustomFont != null) {
                mLockscreenCustomFont.setOnPreferenceClickListener(preference -> {
                    pick("font");
                    type = 1;
                    return true;
                });
            }

            Preference mLockscreenCustomImage = findPreference("lockscreen_clock_custom_image_picker");
            if (mLockscreenCustomImage != null) {
                mLockscreenCustomImage.setOnPreferenceClickListener(preference -> {
                    pick("image");
                    type = 2;
                    return true;
                });
            }

        }

        private void pick(String what) {
            if (!AppUtils.hasStoragePermission()) {
                AppUtils.requestStoragePermission(requireContext());
            } else {
                if (what.equals("font"))
                    launchFilePicker(startActivityIntent, "font/*");
                else if (what.equals("image"))
                    launchFilePicker(startActivityIntent, "image/*");
            }
        }

        private ClockPreviewAdapter initLockscreenClockStyles() {
            ArrayList<ClockModel> ls_clock = new ArrayList<>();

            int maxIndex = 0;
            while (requireContext()
                    .getResources()
                    .getIdentifier(
                            "preview_lockscreen_clock_" + maxIndex,
                            "layout",
                            BuildConfig.APPLICATION_ID
                    ) != 0) {
                maxIndex++;
            }

            for (int i = 0; i < maxIndex; i++) {
                ls_clock.add(new ClockModel(
                        i == 0 ?
                                "No Clock" :
                                "Clock Style " + i,
                        requireContext()
                                .getResources()
                                .getIdentifier(
                                        LOCKSCREEN_CLOCK_LAYOUT + i,
                                        "layout",
                                        BuildConfig.APPLICATION_ID
                                )
                ));
            }

            return new ClockPreviewAdapter(requireContext(), ls_clock, LOCKSCREEN_CLOCK_SWITCH, LOCKSCREEN_CLOCK_STYLE);
        }

    }
}
