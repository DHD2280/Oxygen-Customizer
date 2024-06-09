package it.dhd.oxygencustomizer.ui.fragments;

import static it.dhd.oxygencustomizer.utils.AppUtils.restartDevice;
import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_ANDROID_THEMES;
import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_NAVBAR;
import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_NOTIFICATIONS;
import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_SIGNAL_ICONS;
import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_WIFI_ICONS;

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
    Preference mUiStyle, mNotifications, mNavBar, mSignalIcons, mWifiIcons;

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

        mUiStyle = findPreference("android.theme.customization.style");
        mNotifications = findPreference("android.theme.customization.notifications");
        mNavBar = findPreference("android.theme.customization.navbar");
        mSignalIcons = findPreference("android.theme.customization.signal_icon");
        mWifiIcons = findPreference("android.theme.customization.wifi_icon");

    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        mRebootPreference.setVisible(!ModuleUtil.checkModuleVersion(getContext()));
        mUiStyle.setVisible(TOTAL_ANDROID_THEMES > 0);
        mNotifications.setVisible(TOTAL_NOTIFICATIONS > 0);
        mNavBar.setVisible(TOTAL_NAVBAR > 0);
        mSignalIcons.setVisible(TOTAL_SIGNAL_ICONS > 0);
        mWifiIcons.setVisible(TOTAL_WIFI_ICONS > 0);
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
