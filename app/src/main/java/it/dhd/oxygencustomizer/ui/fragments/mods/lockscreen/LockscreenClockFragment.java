package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ClockPickerFragment;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class LockscreenClockFragment extends ClockPickerFragment {

    private final LockscreenClockPrefs mFragment = new LockscreenClockPrefs();

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_clock);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public String getCategoryTitle() {
        return getString(R.string.lockscreen_clock_switch);
    }

    @Override
    public String getSwitchPreferenceKey() {
        return LOCKSCREEN_CLOCK_SWITCH;
    }

    @Override
    public String getSwitchTitle() {
        return getString(R.string.lockscreen_clock_switch);
    }

    @Override
    public String getPreferenceKey() {
        return LOCKSCREEN_CLOCK_STYLE;
    }

    @Override
    public String getLayoutName() {
        return LOCKSCREEN_CLOCK_LAYOUT;
    }

    @Override
    public boolean shouldLoadWallpaper() {
        return true;
    }

    @Override
    public ControlledPreferenceFragmentCompat getPreferenceFragment() {
        return mFragment;
    }

}
