package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_FINGERPRINT_FILE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_CARRIER_REPLACEMENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_CAPSULE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_CARRIER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_STATUSBAR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_NOTIFICATIONS_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_ENABLED;
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

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oneplusui.preference.OplusSwitchPreference;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.DateFormatDialog;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.ui.preferences.dialogadapter.ListPreferenceAdapter;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.OCPreferences;

public class Lockscreen extends ControlledPreferenceFragmentCompat {

    private DateFormatDialog mDateFormatDialog;

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

        mDateFormatDialog = new DateFormatDialog(requireActivity());

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
            fpIconsEntries.add(String.format(getString(R.string.lockscreen_fp_style), i));
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
                        (text) -> {
                            mPreferences.edit().putString(LOCKSCREEN_CARRIER_REPLACEMENT, text.toString()).apply();
                            setJumps();
                        });
                return true;
            });
        }

    }

    private void setJumps() {
        OplusJumpPreference mLockscreenClock = findPreference("lockscreen_clock_main");
        OplusJumpPreference mLockscreenWeather = findPreference("lockscreen_weather");
        OplusJumpPreference mLockscreenWidgets = findPreference("lockscreen_widgets");
        OplusJumpPreference mLockscreenNowBar = findPreference("lockscreen_nowbar_jump");
        OplusJumpPreference mLockscreenPeek = findPreference("lockscreen_peeknotifications_jump");
        OplusJumpPreference mLsCarrierText = findPreference("ls_carrier_replacement");

        if (mLockscreenClock != null) {
            mLockscreenClock.setJumpText(OCPreferences.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false) ?
                    OCPreferences.getInt(LOCKSCREEN_CLOCK_STYLE, 0) == 0 ? getString(R.string.clock_none) : String.format(getString(R.string.clock_style_name), mPreferences.getInt(LOCKSCREEN_CLOCK_STYLE, 0)) :
                    getString(R.string.general_off));
        }

        if (mLockscreenWeather != null) {
            mLockscreenWeather.setJumpText(OCPreferences.getBoolean(LOCKSCREEN_WEATHER_SWITCH, false) ? getString(R.string.general_on) : getString(R.string.general_off));
        }

        if (mLockscreenWidgets != null) {
            mLockscreenWidgets.setJumpText(OCPreferences.getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false) ? getString(R.string.general_on) : getString(R.string.general_off));
        }

        if (mLockscreenNowBar != null) {
            mLockscreenNowBar.setJumpText(OCPreferences.getBoolean(NOW_BAR_ENABLED, false) ? getString(R.string.general_on) : getString(R.string.general_off));
        }

        if (mLockscreenPeek != null) {
            mLockscreenPeek.setJumpText(OCPreferences.getBoolean(LOCKSCREEN_PEEK_NOTIFICATIONS_ENABLED, false) ? getString(R.string.general_on) : getString(R.string.general_off));
        }

        if (mLsCarrierText != null) {
            mLsCarrierText.setJumpText(OCPreferences.getString(LOCKSCREEN_CARRIER_REPLACEMENT, "").isEmpty() ?
                    getString(R.string.general_off) : getString(R.string.general_on));
        }
    }

    ActivityResultLauncher<Intent> pickImageIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToOCHiddenDir(path, LOCKSCREEN_FINGERPRINT_FILE)) {
                        mPreferences.edit().putString(LOCKSCREEN_FINGERPRINT_STYLE, "-1").apply();
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        setJumps();

        if (key == null) return;
        switch (key) {
            case "DWallpaperEnabled":
                try {
                    boolean DepthEffectEnabled = mPreferences.getBoolean("DWallpaperEnabled", false);

                    if (DepthEffectEnabled) {
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle(R.string.depth_effect_alert_title)
                                .setMessage(getString(R.string.depth_effect_alert_body))
                                .setPositiveButton(R.string.depth_effect_ok_btn, (dialog, which) -> dialog.dismiss())
                                .setCancelable(false)
                                .show();
                    }
                } catch (Exception ignored) {
                }
                break;
        }
    }
}
