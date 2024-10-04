package it.dhd.oxygencustomizer.ui.fragments.mods.qsheader;

import static it.dhd.oxygencustomizer.utils.Constants.HEADER_IMAGE_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_VALUE;
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
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.ui.preferences.dialogadapter.ListPreferenceAdapter;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;

public class QsHeaderImage extends ControlledPreferenceFragmentCompat {

    public QsHeaderImage() {

    }

    @Override
    public String getTitle() {
        return getString(R.string.qs_header_image_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_header_image_prefs;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        ListWithPopUpPreference mImage = findPreference("qs_header_image");
        List<String> mValues = new ArrayList<>();
        int maxIndex = 1;
        while (requireContext()
                .getResources()
                .getIdentifier(
                        "qs_header_image_low_" + maxIndex,
                        "drawable",
                        BuildConfig.APPLICATION_ID
                ) != 0) {
            mValues.add("qs_header_image_low_" + maxIndex);
            maxIndex++;
        }
        if (mImage != null) {
            mImage.createDefaultAdapter();
            mImage.setAdapterType(ListPreferenceAdapter.TYPE_QS_IMAGE);
            mImage.setEntries(mValues.toArray(new CharSequence[0]));
            mImage.setEntryValues(mValues.toArray(new CharSequence[0]));
            mImage.setImages(mValues);
        }
        Preference mQsImageFile = findPreference("qs_header_image_file");
        if (mQsImageFile != null) {
            mQsImageFile.setOnPreferenceClickListener(preference -> {
                pickFile();
                return true;
            });
        }
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.SYSTEM_UI};
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    if (path != null && moveToOCHiddenDir(path, HEADER_IMAGE_DIR)) {
                        mPreferences.edit().putInt(QS_HEADER_IMAGE_VALUE, -1).apply();
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        mPreferences.edit().putBoolean(QS_HEADER_IMAGE_ENABLED, false).apply();
                        mPreferences.edit().putBoolean(QS_HEADER_IMAGE_ENABLED, true).apply();
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void pickFile() {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext());
        } else {
            launchFilePicker(startActivityIntent, "image/*");
        }
    }
}
