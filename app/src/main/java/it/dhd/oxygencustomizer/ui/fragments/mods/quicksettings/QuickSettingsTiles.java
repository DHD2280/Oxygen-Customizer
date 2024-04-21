package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;

public class QuickSettingsTiles extends ControlledPreferenceFragmentCompat {


    @Override
    public String getTitle() {
        return getString(R.string.quick_settings_tiles_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.quick_settings_tiles_prefs;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.SYSTEM_UI};
    }
}
