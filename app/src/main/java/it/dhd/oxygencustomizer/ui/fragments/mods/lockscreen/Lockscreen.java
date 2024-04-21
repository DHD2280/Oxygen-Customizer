package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_FONT_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_FINGERPRINT_FILE;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_CUSTOM_FINGERPRINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.customprefs.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.customprefs.RecyclerPreference;
import it.dhd.oxygencustomizer.customprefs.dialogadapter.ListPreferenceAdapter;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.adapters.ClockPreviewAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.models.ClockModel;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.CarouselLayoutManager;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.weather.Config;
import it.dhd.oxygencustomizer.weather.WeatherUpdateService;
import it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.Preference.OnPreferenceChangeListener;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

public class Lockscreen extends ControlledPreferenceFragmentCompat {
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
        return new String[]{Constants.Packages.SYSTEM_UI};
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
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
                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    launchFilePicker(startActivityIntent, "image/*");
                }
                return true;
            });
        }
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
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
            return new String[]{Constants.Packages.SYSTEM_UI};
        }

        private int type = 0;


        ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        String path = getRealPath(data);
                        String destination = "";
                        if (type == 0)
                            destination = LOCKSCREEN_USER_IMAGE;
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

            RecyclerPreference mLockscreenClockStyles = findPreference("lockscreen_clock_custom");
            if (mLockscreenClockStyles != null) {
                mLockscreenClockStyles.setLayoutManager(new CarouselLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false));
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
