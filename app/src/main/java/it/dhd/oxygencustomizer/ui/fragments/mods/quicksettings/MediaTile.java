package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import androidx.fragment.app.Fragment;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class MediaTile extends ControlledPreferenceFragmentCompat {


    @Override
    public String getTitle() {
        return getString(R.string.media_tile);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_media_tile_prefs;
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
