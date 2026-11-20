package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class NowBarMusicFragment extends ControlledPreferenceFragmentCompat {

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_now_bar_music);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_now_bar_music_prefs;
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
