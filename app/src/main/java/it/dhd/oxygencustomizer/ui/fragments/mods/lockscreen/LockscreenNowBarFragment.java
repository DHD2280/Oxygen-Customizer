package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CHARGING_ICON_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.ui.preferences.dialogadapter.ListPreferenceAdapter;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.WeatherScheduler;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;

public class LockscreenNowBarFragment extends ControlledPreferenceFragmentCompat {

    private OmniJawsClient mWeatherClient;

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_now_bar_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_now_bar_prefs;
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

        mWeatherClient = new OmniJawsClient(getContext());
        mWeatherClient.queryWeather();

        int[] chargingIcons = new int[]{
                R.drawable.ic_charging_bold, // Bold
                R.drawable.ic_charging_asus, // Asus
                R.drawable.ic_charging_buddy, // Buddy
                R.drawable.ic_charging_evplug, // EV Plug
                R.drawable.ic_charging_idc, // IDC
                R.drawable.ic_charging_ios, // IOS
                R.drawable.ic_charging_koplak, // Koplak
                R.drawable.ic_charging_miui, // MIUI
                R.drawable.ic_charging_mmk, // MMK
                R.drawable.ic_charging_moto, // Moto
                R.drawable.ic_charging_nokia, // Nokia
                R.drawable.ic_charging_plug, // Plug
                R.drawable.ic_charging_powercable, // Power Cable
                R.drawable.ic_charging_powercord, // Power Cord
                R.drawable.ic_charging_powerstation, // Power Station
                R.drawable.ic_charging_realme, // Realme
                R.drawable.ic_charging_soak, // Soak
                R.drawable.ic_charging_stres, // Stres
                R.drawable.ic_charging_strip, // Strip
                R.drawable.ic_charging_usbcable, // USB Cable
                R.drawable.ic_charging_xiaomi, // Xiaomi
        };

        ListWithPopUpPreference mChargingIcon = findPreference(NOW_BAR_BATTERY_CHARGING_ICON_STYLE);
        if (mChargingIcon != null) {
            List<String> mValues = new ArrayList<>();
            for(int i = 0; i<mChargingIcon.getEntries().length; i++) {
                mValues.add(String.valueOf(i));
            }
            mChargingIcon.setEntryValues(mValues.toArray(new CharSequence[0]));
            mChargingIcon.setDrawables(chargingIcons);
            mChargingIcon.createDefaultAdapter();
            mChargingIcon.setAdapterType(ListPreferenceAdapter.TYPE_BATTERY_ICONS);
        }
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        if (key.equals(NOW_BAR_WEATHER)) {
            boolean enabled = mPreferences.getBoolean(NOW_BAR_WEATHER, false);
            if (enabled) {
                if (mWeatherClient.getWeatherInfo() != null) {
                    // Weather enabled but updater more than 1h ago
                    if (System.currentTimeMillis() - mWeatherClient.getWeatherInfo().timeStamp > 3600000) {
                        WeatherScheduler.scheduleUpdateNow(getContext());
                    }
                } else {
                    // Force refresh
                    WeatherScheduler.scheduleUpdates(getContext());
                    WeatherScheduler.scheduleUpdateNow(getContext());
                }
            }
            } else if (key.equals(NOW_BAR_ENABLED)) {
                boolean barEnabled = mPreferences.getBoolean(NOW_BAR_WEATHER, false);
                boolean weatherEnabled = mPreferences.getBoolean(NOW_BAR_ENABLED, false);
                if (barEnabled && weatherEnabled) {
                    if (mWeatherClient.getWeatherInfo() != null) {
                        // Weather enabled but updater more than 1h ago
                        if (System.currentTimeMillis() - mWeatherClient.getWeatherInfo().timeStamp > 3600000) {
                            WeatherScheduler.scheduleUpdateNow(getContext());
                        }
                    } else {
                        // Force refresh
                        WeatherScheduler.scheduleUpdates(getContext());
                        WeatherScheduler.scheduleUpdateNow(getContext());
                    }
                }
            }
    }

}
