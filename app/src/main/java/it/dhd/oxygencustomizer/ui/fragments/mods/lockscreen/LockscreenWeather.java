package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_FONT_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_WEATHER_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.WeatherPreferenceFragment;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;

public class LockscreenWeather
        extends WeatherPreferenceFragment {

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToOCHiddenDir(path, LOCKSCREEN_WEATHER_CUSTOM_FONT)) {
                        mPreferences.edit().putBoolean(LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH, false).apply();
                        mPreferences.edit().putBoolean(LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH, true).apply();
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void pick() {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext());
        } else {
            launchFilePicker(startActivityIntent, "font/*");
        }
    }

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

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_weather);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_weather_prefs;
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
    public String getMainSwitchKey() {
        return LOCKSCREEN_WEATHER_SWITCH;
    }

}
