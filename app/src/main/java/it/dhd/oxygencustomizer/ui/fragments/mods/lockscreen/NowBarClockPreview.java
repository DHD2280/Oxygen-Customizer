package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.utils.Constants.NOW_BAR_CLOCK_FONT_FILE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_CLOCK_FORMAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_CLOCK_POSITION;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;

import java.io.File;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.NowBarMusicClockPreviewBinding;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.interfaces.PreferenceListener;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;

public class NowBarClockPreview extends BaseFragment {

    NowBarMusicClockPreviewBinding binding;

    private PreferenceListener mListener;
    private int mDatePosition = 0;
    private String mDateFormat = "";
    private boolean mCustomFont = false;

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_now_bar_music_extended_player_clock_options);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = NowBarMusicClockPreviewBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mListener = this::updatePrefs;
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.getId(), new NowBarClockPrefs(mListener))
                .commit();

        TimeUtils.setCurrentTimeTextClockRed(binding.clockTick, binding.clock, Color.parseColor("#FFF50514"));
        updatePrefs();
    }

    private void updatePrefs() {
        mDatePosition = Integer.parseInt(OCPreferences.getString(NOW_BAR_MUSIC_CLOCK_POSITION, "0"));
        mDateFormat = OCPreferences.getString(NOW_BAR_MUSIC_CLOCK_FORMAT, "");
        mCustomFont = OCPreferences.getBoolean(NOW_BAR_CLOCK_CUSTOM_FONT, false);
        setupClock();
    }

    private void setupClock() {
        binding.topDate.setVisibility(mDatePosition == 0 ? View.VISIBLE : View.GONE);
        binding.bottomDate.setVisibility(mDatePosition == 1 ? View.VISIBLE : View.GONE);
        binding.topDate.setFormat12Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        binding.topDate.setFormat24Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        binding.bottomDate.setFormat12Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        binding.bottomDate.setFormat24Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);

        Typeface typeface = null;
        if (mCustomFont && new File(NOW_BAR_CLOCK_FONT_FILE).exists()) {
            typeface = Typeface.createFromFile(new File(NOW_BAR_CLOCK_FONT_FILE));
        } else {
            typeface = ResourcesCompat.getFont(requireContext(), R.font.slateforoneplus);
        }
        binding.topDate.setTypeface(typeface);
        binding.bottomDate.setTypeface(typeface);
        binding.clock.setTypeface(typeface);
    }

    public static class NowBarClockPrefs extends ControlledPreferenceFragmentCompat {

        private PreferenceListener listener;

        ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Intent data = result.getData();
                        String path = getRealPath(data);

                        if (path != null && moveToOCHiddenDir(path, NOW_BAR_CLOCK_FONT_FILE)) {
                            mPreferences.edit().putBoolean(NOW_BAR_CLOCK_CUSTOM_FONT, false).apply();
                            mPreferences.edit().putBoolean(NOW_BAR_CLOCK_CUSTOM_FONT, true).apply();
                            Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                            listener.onPreferenceChanged();
                        } else {
                            Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            Preference mCustomFontPiker = findPreference("font_picker");
            if (mCustomFontPiker != null) {
                mCustomFontPiker.setOnPreferenceClickListener(preference -> {
                    pick();
                    return true;
                });
            }
        }

        private void pick() {
            if (!AppUtils.hasStoragePermission()) {
                AppUtils.requestStoragePermission(requireContext());
            } else {
                launchFilePicker(startActivityIntent, "font/*");
            }
        }

        public NowBarClockPrefs(PreferenceListener listener) {
            this.listener = listener;
        }

        @Override
        public String getTitle() {
            return getString(R.string.lockscreen_now_bar_music_extended_player_clock_options);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.now_bar_music_clock_prefs;
        }

        @Override
        public boolean hasMenu() {
            return false;
        }

        @Override
        public String[] getScopes() {
            return new String[]{};
        }

        @Override
        public void updateScreen(String key) {
            super.updateScreen(key);

            if (key != null) listener.onPreferenceChanged();

        }

    }

}
