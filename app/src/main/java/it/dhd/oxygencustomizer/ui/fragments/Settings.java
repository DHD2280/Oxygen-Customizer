package it.dhd.oxygencustomizer.ui.fragments;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static it.dhd.oxygencustomizer.utils.AppUtils.restartApplication;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.OplusJumpPreference;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.preferences.OplusSwitchPreference;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.PrefManager;
import it.dhd.oxygencustomizer.utils.UpdateScheduler;

public class Settings extends ControlledPreferenceFragmentCompat {

    // Language Pref
    private ListPreference languagePref;

    private Preference ghPref, deleteAllPref, importPref, exportPref, creditsPref, supportGroupPref, translatePref;
    private OplusSwitchPreference appIconThemed;

    // Updater Prefs
    private OplusJumpPreference updatePref;
    private OplusSwitchPreference autoUpdatePref;

    boolean export = true;

    @Override
    public String getTitle() {
        return getString(R.string.settings_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.own_settings;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return new String[0];
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        setPreferencesFromResource(R.xml.own_settings, rootKey);

        appIconThemed = findPreference("themed_icon");
        languagePref = findPreference("appLanguage");
        ghPref = findPreference("GitHubRepo");
        deleteAllPref = findPreference("deleteAllPrefs");
        exportPref = findPreference("export");
        importPref = findPreference("import");
        creditsPref = findPreference("credits");
        updatePref = findPreference("updates");
        autoUpdatePref = findPreference("autoUpdate");
        supportGroupPref = findPreference("SupportGroup");
        translatePref = findPreference("translate");

        if (appIconThemed != null) {
            appIconThemed.setOnPreferenceChangeListener((preference, newValue) -> {
                boolean isThemed = (boolean) newValue;
                new MaterialAlertDialogBuilder(requireActivity(), R.style.MaterialComponents_MaterialAlertDialog)
                        .setTitle(R.string.app_kill_alert_title)
                        .setMessage(R.string.app_kill_alert_body)
                        .setPositiveButton(R.string.app_kill_ok_btn, (dialog, which) -> changeIcon(isThemed))
                        .setCancelable(false)
                        .show();
                return true;
            });
        }

        if (languagePref != null) {
            languagePref.setOnPreferenceChangeListener((preference, newValue) -> {
                restartApplication(requireActivity());
                return true;
            });
        }

        if (ghPref != null) {
            ghPref.setOnPreferenceClickListener(preference -> {
                // Open GitHub
                requireActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/DHD2280/Oxygen-Customizer")));
                return true;
            });
        }

        if (deleteAllPref != null) {
            deleteAllPref.setOnPreferenceClickListener(preference -> {
                // Delete all data
                SharedPreferences prefs = getDefaultSharedPreferences(requireContext().createDeviceProtectedStorageContext());
                PrefManager.clearPrefs(prefs);
                AppUtils.restartAllScope(new String[]{SYSTEM_UI});
                return true;
            });
        }

        if (exportPref != null) {
            exportPref.setOnPreferenceClickListener(preference -> {
                // Export data
                importExportSettings(true);
                return true;
            });
        }

        if (importPref != null) {
            importPref.setOnPreferenceClickListener(preference -> {
                // Export data
                importExportSettings(false);
                return true;
            });
        }

        if (creditsPref != null) {
            creditsPref.setOnPreferenceClickListener(preference -> {
                MainActivity.replaceFragment(new Credits());
                return true;
            });
        }

        if (updatePref != null) {
            updatePref.setJumpText(
                    BuildConfig.VERSION_NAME
            );
            updatePref.setOnPreferenceClickListener(preference -> {
                MainActivity.replaceFragment(new UpdateFragment());
                return true;
            });

            autoUpdatePref.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((boolean) newValue) {
                    UpdateScheduler.scheduleUpdateNow(requireContext());
                }
                return true;
            });
        }

        if (supportGroupPref != null) {
            supportGroupPref.setOnPreferenceClickListener(preference -> {
                // Open Telegram Group
                requireActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/OxygenCustomizer")));
                return true;
            });
        }

        if (translatePref != null) {
            translatePref.setOnPreferenceClickListener(preference -> {
                // Open Crowdin
                requireActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://crowdin.com/project/oxygen-customizer")));
                return true;
            });
        }

    }

    private void changeIcon(boolean isThemed) {
        PackageManager packageManager = getActivity().getPackageManager();

        packageManager.setComponentEnabledSetting(
                new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".SplashActivity"),
                isThemed ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
        );

        // Enable themed app icon component
        packageManager.setComponentEnabledSetting(
                new ComponentName(BuildConfig.APPLICATION_ID, BuildConfig.APPLICATION_ID + ".SplashActivityThemed"),
                isThemed ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP
        );

        getActivity().finish();
    }

    private void importExportSettings(boolean export) {
        Intent fileIntent = new Intent();
        this.export = export;
        fileIntent.setAction(export ? Intent.ACTION_CREATE_DOCUMENT : Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        fileIntent.putExtra(Intent.EXTRA_TITLE, "OxygenCustomizer_Config" + ".bin");
        mImportExportLauncher.launch(fileIntent);
    }

    ActivityResultLauncher<Intent> mImportExportLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data == null) return;

                    SharedPreferences prefs = getDefaultSharedPreferences(requireContext().createDeviceProtectedStorageContext());

                    Log.d("Settings", "importExportSettings: export? " + export + " " + data.getData());

                    if (export) {
                        try {
                            if (PrefManager.exportPrefs(prefs, getContext().getContentResolver().openOutputStream(data.getData()))) {
                                AppUtils.showToast(requireContext(), getString(R.string.export_success));
                            } else {
                                AppUtils.showToast(requireContext(), getString(R.string.export_failed));
                            }

                        } catch (Exception ignored) {
                        }
                    } else {
                        try {
                            if (PrefManager.importPath(prefs, getContext().getContentResolver().openInputStream(data.getData()))) {
                                AppUtils.showToast(requireContext(), getString(R.string.import_success));
                                new Handler(Looper.getMainLooper()).postDelayed(() -> AppUtils.restartAllScope(new String[]{SYSTEM_UI}), 1000);
                            } else {
                                AppUtils.showToast(requireContext(), getString(R.string.import_failed));
                            }
                        } catch (Exception e) {
                            Log.d("Settings", "importExportSettings: " + e.getMessage());
                        }
                    }
                }
            });

}
