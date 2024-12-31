package it.dhd.oxygencustomizer.ui.fragments.mods.aod;

import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;

import android.os.Build;
import android.view.View;

import androidx.annotation.StringRes;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ClockPickerFragment;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class AodClock extends ClockPickerFragment {

    private final AodClockPrefs mFragment = new AodClockPrefs();

    @Override
    public String getCategoryTitle() {
        return getString(R.string.aod_clock);
    }

    @Override
    public String getSwitchPreferenceKey() {
        return AOD_CLOCK_SWITCH;
    }

    @Override
    public String getSwitchTitle() {
        return getString(R.string.aod_clock_switch);
    }

    @Override
    public String getPreferenceKey() {
        return AOD_CLOCK_STYLE;
    }

    @Override
    public String getLayoutName() {
        return LOCKSCREEN_CLOCK_LAYOUT;
    }

    @Override
    public String getTitle() {
        return getString(R.string.aod_clock);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public boolean getIntroEnabled() { return (Build.VERSION.SDK_INT >= 35); }

    @Override
    public @StringRes int getIntroTitle() { return R.string.aod_clock_intro_oos15; }

    @Override
    public ControlledPreferenceFragmentCompat getPreferenceFragment() {
        return mFragment;
    }

    @Override
    public PREVIEW_TYPE getPreviewType() {
        return PREVIEW_TYPE.AOD;
    }

}
