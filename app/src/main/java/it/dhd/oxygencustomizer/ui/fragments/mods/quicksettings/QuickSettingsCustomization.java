package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import android.os.Bundle;
import android.util.Log;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class QuickSettingsCustomization extends ControlledPreferenceFragmentCompat {


    @Override
    public String getTitle() {
        return getString(R.string.quick_settings_tiles_customization_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.quick_settings_tiles_customizations_prefs;
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        OplusJumpPreference highlightTile = findPreference("highlight_tile");
        OplusJumpPreference baseTile = findPreference("base_tile");

        String oplusRom = PreferenceHelper.getOsVersion();
        Log.d("QuickSettingsCustomization", "Oplus ROM: " + oplusRom);

        boolean isOOS16 = oplusRom != null && oplusRom.contains("16.0.10");

        if (highlightTile != null) {
            highlightTile.setVisible(!isOOS16);
        }

        if (baseTile != null) {
            baseTile.setVisible(!isOOS16);
        }

    }

}
