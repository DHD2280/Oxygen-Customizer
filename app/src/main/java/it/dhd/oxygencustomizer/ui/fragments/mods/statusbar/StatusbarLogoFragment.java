package it.dhd.oxygencustomizer.ui.fragments.mods.statusbar;

import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_CROP_KEY;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_FILE_URI;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_APPLY_TINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.SETTINGS_OTA_CARD_DIR;
import static it.dhd.oxygencustomizer.utils.Constants.STATUSBAR_LOGO_FILE;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageOptions;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.OCPreferences;

public class StatusbarLogoFragment extends ControlledPreferenceFragmentCompat {

    private ListWithPopUpPreference mLogoStyle;

    @Override
    public String getTitle() {
        return getString(R.string.statusbar_logo);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.statusbar_logo;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getSupportFragmentManager()
                .setFragmentResultListener(DATA_CROP_KEY, this, (requestKey, result) -> {
                    String resultString = result.getString(DATA_FILE_URI);
                    String path = getRealPath(Uri.parse(resultString));
                    if (path != null && moveToOCHiddenDir(path, STATUSBAR_LOGO_FILE)) {
                        mPreferences.putBoolean(STATUSBAR_LOGO_SWITCH, false);
                        mPreferences.putBoolean(STATUSBAR_LOGO_SWITCH, true);
                        setupAdapter();
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mLogoStyle = findPreference(STATUSBAR_LOGO_STYLE);
        setupAdapter();
    }

    private void setupAdapter() {
        mLogoStyle.createDefaultAdapter(getStatusbarLogoDrawables());
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        if (key != null && (key.equals(STATUSBAR_LOGO_STYLE))) {
            if (mPreferences.getString(STATUSBAR_LOGO_STYLE, "0").equals("-1")) {
                pickImage();
            }
        }
    }

    public void pickImage() {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext());
        } else {
            Bundle bundle = new Bundle();
            CropImageOptions options = new CropImageOptions();
            options.aspectRatioX = 1;
            options.aspectRatioY = 1;
            options.fixAspectRatio = true;
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
            FragmentCropImage fragmentCropImage = new FragmentCropImage();
            fragmentCropImage.setArguments(bundle);
            BottomSheetDialog bottomSheetDialog = mLogoStyle.getBottomSheet();
            if (bottomSheetDialog != null && bottomSheetDialog.isShowing()) {
                bottomSheetDialog.dismiss();
            }
            replaceFragment(fragmentCropImage);
        }
    }

    private Drawable[] getStatusbarLogoDrawables() {
        int logoColor = requireContext().getColor(R.color.textColorPrimary);

        List<Integer> predefinedLogos = Arrays.asList(
                R.drawable.ic_android_logo,
                R.drawable.ic_adidas,
                R.drawable.ic_alien,
                R.drawable.ic_apple_logo,
                R.drawable.ic_avengers,
                R.drawable.ic_batman,
                R.drawable.ic_batman_tdk,
                R.drawable.ic_beats,
                R.drawable.ic_biohazard,
                R.drawable.ic_blackberry,
                R.drawable.ic_cannabis,
                R.drawable.ic_emoticon_cool,
                R.drawable.ic_emoticon_devil,
                R.drawable.ic_fire,
                R.drawable.ic_heart,
                R.drawable.ic_nike,
                R.drawable.ic_pac_man,
                R.drawable.ic_puma,
                R.drawable.ic_rog,
                R.drawable.ic_spiderman,
                R.drawable.ic_superman,
                R.drawable.ic_windows,
                R.drawable.ic_xbox,
                R.drawable.ic_ghost,
                R.drawable.ic_ninja,
                R.drawable.ic_robot,
                R.drawable.ic_ironman,
                R.drawable.ic_captain_america,
                R.drawable.ic_flash,
                R.drawable.ic_tux_logo,
                R.drawable.ic_ubuntu_logo,
                R.drawable.ic_mint_logo,
                R.drawable.ic_amogus
        );

        List<Drawable> logoDrawables = predefinedLogos.stream()
                .map(logo -> AppCompatResources.getDrawable(requireContext(), logo))
                .peek(logo -> logo.setTint(logoColor))
                .collect(Collectors.toList());

        Drawable customDrawable;
        try {
            Bitmap customBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(new File(STATUSBAR_LOGO_FILE)));
            RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(requireContext().getResources(), customBitmap);
            roundedDrawable.setCornerRadius(customBitmap.getHeight());
            customDrawable = roundedDrawable;
            if (OCPreferences.getBoolean(STATUSBAR_LOGO_APPLY_TINT)) {
                roundedDrawable.setTint(logoColor);
            }
        } catch (Exception e) {
            customDrawable = AppCompatResources.getDrawable(requireContext(), R.drawable.ic_upload_file);
            customDrawable.setTint(logoColor);
        }
        logoDrawables.add(0, customDrawable);

        return logoDrawables.toArray(new Drawable[0]);
    }

}
