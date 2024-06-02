package it.dhd.oxygencustomizer.ui.fragments;

import static it.dhd.oxygencustomizer.utils.AppUtils.restartDevice;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.preference.Preference;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.utils.ModuleUtil;

public class UserInterface extends ControlledPreferenceFragmentCompat {

    Preference mRebootPreference;

    @Override
    public String getTitle() {
        return getString(R.string.app_name);
    }

    @Override
    public boolean backButtonEnabled() {
        return false;
    }

    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mRebootPreference = findPreference("reboot_pref");

        if (mRebootPreference != null) {
            mRebootPreference.setOnPreferenceClickListener(preference -> {
                LoadingDialog mReboot = new LoadingDialog(getContext());

                mReboot.show(getString(R.string.rebooting_desc), false);

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    mReboot.dismiss();
                    restartDevice();
                }, 5000);
                return true;
            });
        }

    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        mRebootPreference.setVisible(!ModuleUtil.checkModuleVersion(getContext()));
    }

    @Override
    public int getLayoutResource() {
        return R.xml.ui_mods;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return null;
    }
}
