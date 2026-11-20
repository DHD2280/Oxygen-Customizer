package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CHARGING_ICON_STYLE;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.ui.preferences.dialogadapter.ListPreferenceAdapter;

public class NowBarBatteryFragment extends ControlledPreferenceFragmentCompat {

    private static final int[] CHARGING_ICONS = {
            R.drawable.ic_charging_bold,
            R.drawable.ic_charging_asus,
            R.drawable.ic_charging_buddy,
            R.drawable.ic_charging_evplug,
            R.drawable.ic_charging_idc,
            R.drawable.ic_charging_ios,
            R.drawable.ic_charging_koplak,
            R.drawable.ic_charging_miui,
            R.drawable.ic_charging_mmk,
            R.drawable.ic_charging_moto,
            R.drawable.ic_charging_nokia,
            R.drawable.ic_charging_plug,
            R.drawable.ic_charging_powercable,
            R.drawable.ic_charging_powercord,
            R.drawable.ic_charging_powerstation,
            R.drawable.ic_charging_realme,
            R.drawable.ic_charging_soak,
            R.drawable.ic_charging_stres,
            R.drawable.ic_charging_strip,
            R.drawable.ic_charging_usbcable,
            R.drawable.ic_charging_xiaomi,
    };

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_now_bar_battery);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_now_bar_battery_prefs;
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

        ListWithPopUpPreference chargingIcon = findPreference(NOW_BAR_BATTERY_CHARGING_ICON_STYLE);
        if (chargingIcon == null) return;
        List<String> values = new ArrayList<>();
        for (int i = 0; i < chargingIcon.getEntries().length; i++) {
            values.add(String.valueOf(i));
        }
        chargingIcon.setEntryValues(values.toArray(new CharSequence[0]));
        chargingIcon.setDrawables(CHARGING_ICONS);
        chargingIcon.createDefaultAdapter();
        chargingIcon.setAdapterType(ListPreferenceAdapter.TYPE_BATTERY_ICONS);
    }
}
