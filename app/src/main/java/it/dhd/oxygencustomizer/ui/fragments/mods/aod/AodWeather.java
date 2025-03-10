package it.dhd.oxygencustomizer.ui.fragments.mods.aod;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.utils.Constants.AOD_WEATHER_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_FONT_SWITH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.WeatherPreferenceFragment;
import it.dhd.oxygencustomizer.utils.AppUtils;

public class AodWeather extends WeatherPreferenceFragment {

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToOCHiddenDir(path, AOD_WEATHER_CUSTOM_FONT)) {
                        mPreferences.edit().putBoolean(AOD_WEATHER_CUSTOM_FONT_SWITH, false).apply();
                        mPreferences.edit().putBoolean(AOD_WEATHER_CUSTOM_FONT_SWITH, true).apply();
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
        return getString(R.string.aod_weather);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.aod_weather_prefs;
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
    public String getMainSwitchKey() {
        return AOD_WEATHER_SWITCH;
    }
}
