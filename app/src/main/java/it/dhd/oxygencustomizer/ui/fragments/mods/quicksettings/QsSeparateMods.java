package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class QsSeparateMods extends ControlledPreferenceFragmentCompat {

    @Override
    public String getTitle() {
        return getString(R.string.qs_separate_mods);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_separate_mods;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SYSTEM_UI};
    }
}
