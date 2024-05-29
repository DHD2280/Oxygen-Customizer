package it.dhd.oxygencustomizer.ui.fragments;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class UserInterface extends ControlledPreferenceFragmentCompat {

    @Override
    public String getTitle() {
        return getString(R.string.app_name);
    }

    @Override
    public boolean backButtonEnabled() {
        return false;
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
