package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_CROP_KEY;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_FILE_URI;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_FONT_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.launchFilePicker;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageOptions;

import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.OCPreferences;

public class LockscreenClockPrefs extends ControlledPreferenceFragmentCompat {

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
                    if (type == 2)
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getSupportFragmentManager()
                .setFragmentResultListener(DATA_CROP_KEY, this, (requestKey, result) -> {
                    String resultString = result.getString(DATA_FILE_URI);
                    String path = getRealPath(Uri.parse(resultString));
                    OCPreferences.getPrefs().edit().putBoolean(
                            LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE, false
                    ).apply();
                    OCPreferences.getPrefs().edit().putBoolean(
                            LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE, true
                    ).apply();
                    if (path != null && moveToOCHiddenDir(path, LOCKSCREEN_USER_IMAGE)) {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        Preference mLockscreenUserImage = findPreference("lockscreen_clock_custom_user_image_picker");
        if (mLockscreenUserImage != null) {
            mLockscreenUserImage.setOnPreferenceClickListener(preference -> {
                type = 0;
                pick("image");
                return true;
            });
        }

        Preference mLockscreenCustomFont = findPreference("lockscreen_clock_font_custom");
        if (mLockscreenCustomFont != null) {
            mLockscreenCustomFont.setOnPreferenceClickListener(preference -> {
                type = 1;
                pick("font");
                return true;
            });
        }

        Preference mLockscreenCustomImage = findPreference("lockscreen_clock_custom_image_picker");
        if (mLockscreenCustomImage != null) {
            mLockscreenCustomImage.setOnPreferenceClickListener(preference -> {
                type = 2;
                pick("image");
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
                if (type == 0) pickCropper();
                else launchFilePicker(startActivityIntent, "image/*");
        }
    }

    public void pickCropper() {
        Bundle bundle = new Bundle();
        CropImageOptions options = new CropImageOptions();
        options.aspectRatioX = 1;
        options.aspectRatioY = 1;
        options.fixAspectRatio = true;
        bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
        FragmentCropImage fragmentCropImage = new FragmentCropImage();
        fragmentCropImage.setArguments(bundle);
        replaceFragment(fragmentCropImage);
    }

}
