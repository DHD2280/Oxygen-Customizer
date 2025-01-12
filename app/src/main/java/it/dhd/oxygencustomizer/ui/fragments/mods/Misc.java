package it.dhd.oxygencustomizer.ui.fragments.mods;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContextLocale;
import static it.dhd.oxygencustomizer.ui.activity.MainActivity.replaceFragment;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_CROP_KEY;
import static it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage.DATA_FILE_URI;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;
import static it.dhd.oxygencustomizer.utils.Constants.SETTINGS_OTA_CARD_DIR;
import static it.dhd.oxygencustomizer.utils.FileUtil.getRealPath;
import static it.dhd.oxygencustomizer.utils.FileUtil.moveToOCHiddenDir;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import android.os.Build;

import com.canhub.cropper.CropImage;
import com.canhub.cropper.CropImageOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.topjohnwu.superuser.Shell;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oneplusui.preference.OplusSwitchPreference;
import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.fragments.FragmentCropImage;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.ModuleUtil;
import it.dhd.oxygencustomizer.xposed.hooks.framework.OplusStartingWindowManager;

public class Misc extends ControlledPreferenceFragmentCompat {

    private LoadingDialog mLoadingDialog;

    @Override
    public String getTitle() {
        return getString(R.string.misc);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.misc_prefs;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().getSupportFragmentManager()
                .setFragmentResultListener(DATA_CROP_KEY, this, (requestKey, result) -> {
                    String resultString = result.getString(DATA_FILE_URI);
                    String path = getRealPath(Uri.parse(resultString));
                    if (path != null && moveToOCHiddenDir(path, SETTINGS_OTA_CARD_DIR)) {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mLoadingDialog = new LoadingDialog(requireContext());

        OplusJumpPreference mOtaCardPicker = findPreference("ota_card_picker");
        mOtaCardPicker.setOnPreferenceClickListener(preference -> {
            pickImage();
            return true;
        });

    }

    public void pickImage() {
        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(requireContext());
        } else {
            Bundle bundle = new Bundle();
            CropImageOptions options = new CropImageOptions();
            options.aspectRatioX = Build.VERSION.SDK_INT >= 35 ? 164 : 82;
            options.aspectRatioY = Build.VERSION.SDK_INT >= 35 ? 117 : 31;
            options.fixAspectRatio = true;
            bundle.putParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS, options);
            FragmentCropImage fragmentCropImage = new FragmentCropImage();
            fragmentCropImage.setArguments(bundle);
            replaceFragment(fragmentCropImage);
        }
    }

    private void checkOplusVersion() {
        String osVersion = Shell.cmd("getprop ro.build.display.id").exec().getOut().get(0);
        if (!TextUtils.isEmpty(osVersion)) {
            String[] split = osVersion.split("\\.");
            String version = split[split.length - 1].substring(0, split[split.length - 1].indexOf("("));
            Log.d("Misc OC", "Oplus version: " + version);
            if (Integer.parseInt(version) >= 610) {
                Log.d("Misc OC", "Oplus version is greater than 610");
                showConfirmDialog();
            } else {
                Log.d("Misc OC", "Oplus version is less than 610");
                sendIntent();
            }
        }
    }

    private void showConfirmDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.warning)
                .setMessage(R.string.fix_lag_dialog_message)
                .setNegativeButton(R.string.btn_cancel, (dialog, which) -> {
                    mPreferences.putBoolean("fix_lag_switch", false);
                    sendIntent();
                    ((OplusSwitchPreference)findPreference("fix_lag_switch")).setChecked(false);
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.fix_lag_apply_anyway, (dialog, which) -> {
                    mPreferences.putBoolean("fix_lag_switch", true);
                    sendIntent();
                })
                .show();
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        switch (key) {
            case "fix_lag_switch":
                if (mPreferences.getBoolean("fix_lag_switch", false)) {
                    checkOplusVersion();
                } else {
                    sendIntent();
                }
                break;
            case "fix_lag_force_all_apps":
                sendIntent();
                break;
            case "enable_pocket_studio":
                enablePocketStudio();
                break;
        }
    }

    private void enablePocketStudio() {

        mLoadingDialog.show(getAppContextLocale().getResources().getString(R.string.loading_dialog_wait));

        Runnable runnable = () -> {
            // wait 500 millis
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                ModuleUtil.enablePocketStudio(mPreferences.getBoolean("enable_pocket_studio", false));
                // hide dialog
                ((Activity) requireContext()).runOnUiThread(() -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        mLoadingDialog.hide();
                        Toast.makeText(OxygenCustomizer.getAppContext(), getAppContextLocale().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    }, 1000);
                });

            }, 500);
        };

        Thread thread = new Thread(runnable);
        thread.start();
    }

    private void sendIntent() {
        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);
        broadcast.putExtra("packageName", FRAMEWORK);
        broadcast.putExtra("class", OplusStartingWindowManager.class.getSimpleName());
        broadcast.setPackage(FRAMEWORK);
        broadcast.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        requireContext().sendBroadcast(broadcast);
    }

}
