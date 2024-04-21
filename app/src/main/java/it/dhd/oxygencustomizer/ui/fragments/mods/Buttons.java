package it.dhd.oxygencustomizer.ui.fragments.mods;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class Buttons extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.buttons_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.buttons_prefs;
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
