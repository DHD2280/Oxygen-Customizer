package it.dhd.oxygencustomizer.ui.fragments.mods.aod;

import static it.dhd.oxygencustomizer.utils.Constants.AOD_CLOCK_FONT_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.AOD_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.AOD_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import java.util.ArrayList;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.preferences.RecyclerPreference;
import it.dhd.oxygencustomizer.ui.adapters.ClockPreviewAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.models.ClockModel;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;

public class AodClock extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.aod_clock);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.aod_clock_prefs;
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
                        destination = AOD_USER_IMAGE;
                    else if (type == 2)
                        destination = AOD_CUSTOM_IMAGE;
                    else
                        destination = AOD_CLOCK_FONT_DIR;

                    if (path != null && moveToOCHiddenDir(path, destination)) {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        RecyclerPreference mAodClockStyles = findPreference("aod_clock_custom");
        if (mAodClockStyles != null) {
            mAodClockStyles.setAdapter(initLockscreenClockStyles());
            mAodClockStyles.setPreference(AOD_CLOCK_STYLE, 0);
        }

        Preference mAodUserImage = findPreference("aod_clock_custom_user_image_picker");
        if (mAodUserImage != null) {
            mAodUserImage.setOnPreferenceClickListener(preference -> {
                pick("image");
                type = 0;
                return true;
            });
        }

        Preference mAodCustomFont = findPreference("aod_clock_font_custom");
        if (mAodCustomFont != null) {
            mAodCustomFont.setOnPreferenceClickListener(preference -> {
                pick("font");
                type = 1;
                return true;
            });
        }

        Preference mAodCustomImage = findPreference("aod_clock_custom_image_picker");
        if (mAodCustomImage != null) {
            mAodCustomImage.setOnPreferenceClickListener(preference -> {
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
        ArrayList<ClockModel> aod_clock = new ArrayList<>();

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
            aod_clock.add(new ClockModel(
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

        return new ClockPreviewAdapter(requireContext(), aod_clock, AOD_CLOCK_SWITCH, AOD_CLOCK_STYLE);
    }


}
