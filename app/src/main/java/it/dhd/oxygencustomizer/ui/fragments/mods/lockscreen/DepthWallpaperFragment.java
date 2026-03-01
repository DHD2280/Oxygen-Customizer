package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static android.app.Activity.RESULT_OK;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_DEPTH_BACKGROUND_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_DEPTH_SUBJECT_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.PLUGIN_URL;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.getLockScreenBitmapCachePath;
import static it.dhd.oxygencustomizer.utils.Constants.getLockScreenSubjectCachePath;
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

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.BitmapSubjectSegmenter;

public class DepthWallpaperFragment extends ControlledPreferenceFragmentCompat {

    private final int PICK_DEPTH_BACKGROUND = 1;
    private final int PICK_DEPTH_SUBJECT = 2;
    private int mPick = -1;
    private OplusJumpPreference mAiStatus;

    @Override
    public String getTitle() {
        return getString(R.string.depth_wallpaper_category);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.depth_wallpaper_prefs;
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        Preference mDepthBackground = findPreference("DWBackground");
        if (mDepthBackground != null) {
            mDepthBackground.setOnPreferenceClickListener(preference -> {
                mPick = PICK_DEPTH_BACKGROUND;
                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    launchFilePicker(pickImageIntent, "image/*");
                }
                return true;
            });
        }

        Preference mDepthSubject = findPreference("DWSubject");
        if (mDepthSubject != null) {
            mDepthSubject.setOnPreferenceClickListener(preference -> {
                mPick = PICK_DEPTH_SUBJECT;
                if (!AppUtils.hasStoragePermission()) {
                    AppUtils.requestStoragePermission(requireContext());
                } else {
                    launchFilePicker(pickImageIntent, "image/*");
                }
                return true;
            });
        }
    }

    ActivityResultLauncher<Intent> pickImageIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    String path = getRealPath(data);

                    String dest = switch (mPick) {
                        case PICK_DEPTH_BACKGROUND -> getLockScreenBitmapCachePath();
                        case PICK_DEPTH_SUBJECT -> getLockScreenSubjectCachePath();
                        default -> "";
                    };

                    if (path != null && moveToOCHiddenDir(path, dest)) {
                        switch (mPick) {
                            case PICK_DEPTH_BACKGROUND:
                                sendIntent(ACTION_DEPTH_BACKGROUND_CHANGED);
                                break;
                            case PICK_DEPTH_SUBJECT:
                                sendIntent(ACTION_DEPTH_SUBJECT_CHANGED);
                                break;
                        }
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), requireContext().getResources().getString(R.string.toast_rename_file), Toast.LENGTH_SHORT).show();
                    }
                }
            });

    private void sendIntent(String action) {
        Intent intent = new Intent(action);
        intent.putExtra("packageName", SYSTEM_UI);
        requireContext().sendBroadcast(intent);
    }

    private void checkAiStatus() {
        mAiStatus = findPreference("DWAIStatus");
        mAiStatus.setOnPreferenceClickListener(null);
        if (mPreferences.getString("DWMode", "0").equals("0")) {
            new BitmapSubjectSegmenter(getActivity()).checkModelAvailability(moduleAvailabilityResponse ->
                    mAiStatus
                            .setSummary(
                                    moduleAvailabilityResponse.areModulesAvailable()
                                            ? R.string.depth_wallpaper_model_ready
                                            : R.string.depth_wallpaper_model_not_available));
            mAiStatus.setJumpEnabled(false);
        } else if (mPreferences.getString("DWMode", "0").equals("2")) {
            mAiStatus.setJumpEnabled(true);
            if (AppUtils.isAppInstalled(requireContext(), "it.dhd.oxygencustomizer.aiplugin")) {
                mAiStatus.setSummary(R.string.depth_wallpaper_plugin_installed);
                mAiStatus.setOnPreferenceClickListener(preference -> {
                    Intent intent = requireContext().getPackageManager().getLaunchIntentForPackage("it.dhd.oxygencustomizer.aiplugin");
                    startActivity(intent);
                    return true;
                });
            } else {
                mAiStatus.setSummary(R.string.depth_wallpaper_plugin_not_installed);
                mAiStatus.setOnPreferenceClickListener(preference -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(PLUGIN_URL));
                    startActivity(intent);
                    return true;
                });
            }
        }
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) {
            checkAiStatus();
            return;
        }

        switch (key) {
            case "DWMode":
                checkAiStatus();
                break;
        }
    }

}
