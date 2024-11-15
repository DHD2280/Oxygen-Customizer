package it.dhd.oxygencustomizer.ui.fragments.mods.qsheader;

import static it.dhd.oxygencustomizer.utils.Constants.HEADER_CLOCK_FONT_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.HEADER_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.HEADER_CLOCK_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_VALUE;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.annotation.SuppressLint;
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
import it.dhd.oxygencustomizer.ui.adapters.ClockPreviewAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.models.ClockModel;
import it.dhd.oxygencustomizer.ui.preferences.OplusRecyclerPreference;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;

public class QsHeaderClock extends ControlledPreferenceFragmentCompat {

    private int type = 0;

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);
                    String destination;
                    if (type == 0)
                        destination = HEADER_CLOCK_FONT_DIR;
                    else
                        destination = HEADER_CLOCK_USER_IMAGE;

                    if (path != null && moveToOCHiddenDir(path, destination)) {
                        mPreferences.edit().putBoolean(
                                type == 0 ? QS_HEADER_CLOCK_CUSTOM_FONT : QS_HEADER_CLOCK_CUSTOM_ENABLED
                                , false).apply();
                        mPreferences.edit().putBoolean(
                                type == 0 ? QS_HEADER_CLOCK_CUSTOM_FONT : QS_HEADER_CLOCK_CUSTOM_ENABLED
                                , true).apply();
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public String getTitle() {
        return getString(R.string.qs_header_clock);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_header_clock_prefs;
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

        Preference mClockFont = findPreference("qs_header_clock_font_custom");
        if (mClockFont != null) {
            mClockFont.setOnPreferenceClickListener(preference -> {
                pick("font");
                return true;
            });
        }

        Preference mClockImage = findPreference("qs_header_clock_custom_user_image_picker");
        if (mClockImage != null) {
            mClockImage.setOnPreferenceClickListener(preference -> {
                pick("image");
                return true;
            });
        }

        OplusRecyclerPreference mQsClockStyle = findPreference("qs_header_clock_custom");
        if (mQsClockStyle != null) {
            mQsClockStyle.setAdapter(initHeaderClockStyles());
            mQsClockStyle.setPreference(QS_HEADER_CLOCK_CUSTOM_VALUE, 0);
        }


    }

    private void pick(String what) {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext());
        } else {
            if (what.equals("font")) {
                launchFilePicker(startActivityIntent, "font/*");
                type = 0;
            } else if (what.equals("image")) {
                launchFilePicker(startActivityIntent, "image/*");
                type = 1;
            }
        }
    }

    @SuppressLint("DiscouragedApi")
    private ClockPreviewAdapter initHeaderClockStyles() {
        ArrayList<ClockModel> header_clock = new ArrayList<>();

        int maxIndex = 0;
        while (requireContext()
                .getResources()
                .getIdentifier(
                        HEADER_CLOCK_LAYOUT + maxIndex,
                        "layout",
                        BuildConfig.APPLICATION_ID
                ) != 0) {
            maxIndex++;
        }

        for (int i = 0; i < maxIndex; i++) {
            header_clock.add(new ClockModel(
                    i == 0 ?
                            getString(R.string.clock_none) :
                            String.format(getString(R.string.clock_style_name), i),
                    requireContext()
                            .getResources()
                            .getIdentifier(
                                    HEADER_CLOCK_LAYOUT + i,
                                    "layout",
                                    BuildConfig.APPLICATION_ID
                            )
            ));
        }

        return new ClockPreviewAdapter(requireContext(), header_clock, QS_HEADER_CLOCK_CUSTOM_ENABLED, QS_HEADER_CLOCK_CUSTOM_VALUE);
    }

}
