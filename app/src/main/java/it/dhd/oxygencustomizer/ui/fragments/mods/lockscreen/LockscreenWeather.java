package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.WeatherPreferenceFragment;
import it.dhd.oxygencustomizer.utils.Constants;

public class LockscreenWeather
        extends WeatherPreferenceFragment {

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_weather);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_weather_prefs;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.SYSTEM_UI};
    }

    @Override
    public String getMainSwitchKey() {
        return LOCKSCREEN_WEATHER_SWITCH;
    }

}
