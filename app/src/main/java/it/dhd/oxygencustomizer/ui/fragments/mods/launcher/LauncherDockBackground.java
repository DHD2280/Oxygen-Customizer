package it.dhd.oxygencustomizer.ui.fragments.mods.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class LauncherDockBackground extends ControlledPreferenceFragmentCompat {

    @Override
    public String getTitle() {
        return getString(R.string.dock_background);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.launcher_dock_background;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{LAUNCHER};
    }
}
