package it.dhd.oxygencustomizer.ui.fragments.mods.qsheader;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class QsHeader extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.qs_header_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_header_prefs;
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
