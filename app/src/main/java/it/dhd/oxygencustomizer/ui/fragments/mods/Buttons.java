package it.dhd.oxygencustomizer.ui.fragments.mods;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Intent;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;

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

    @Override
    public void updateScreen(String key) {
        Intent broadcast = new Intent(Constants.ACTION_SETTINGS_CHANGED);

        broadcast.putExtra("packageName", SYSTEM_UI);

        broadcast.setPackage(SYSTEM_UI);

        if (getContext() != null)
            getContext().sendBroadcast(broadcast);

    }
}
