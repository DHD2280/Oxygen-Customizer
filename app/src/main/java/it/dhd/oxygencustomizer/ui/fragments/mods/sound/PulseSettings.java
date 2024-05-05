package it.dhd.oxygencustomizer.ui.fragments.mods.sound;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;

public class PulseSettings extends ControlledPreferenceFragmentCompat {

    private PreferenceCategory mFading, mSolid, mLine;

    @Override
    public String getTitle() {
        return getString(R.string.pulse_settings);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.pulse_mods;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mFading = findPreference("pulse_fading_bars_category");
        mSolid = findPreference("pulse_2");
        mLine = findPreference("pulse_line");
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        if (mFading != null) {
            mFading.setEnabled(Integer.parseInt(mPreferences.getString("pulse_render_style", "0")) == 0);
        }
        if (mSolid != null) {
            mSolid.setEnabled(Integer.parseInt(mPreferences.getString("pulse_render_style", "0")) == 1);
        }
        if (mLine != null) {
            mLine.setEnabled(Integer.parseInt(mPreferences.getString("pulse_render_style", "0")) == 2);
        }
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
