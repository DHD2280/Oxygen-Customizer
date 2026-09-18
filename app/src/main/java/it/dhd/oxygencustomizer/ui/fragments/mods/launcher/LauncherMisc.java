package it.dhd.oxygencustomizer.ui.fragments.mods.launcher;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;

public class LauncherMisc extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.misc);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.launcher_misc;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.LAUNCHER};
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
    }
}
