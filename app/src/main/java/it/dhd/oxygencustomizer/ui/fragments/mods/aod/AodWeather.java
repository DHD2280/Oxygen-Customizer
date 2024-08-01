package it.dhd.oxygencustomizer.ui.fragments.mods.aod;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.WeatherPreferenceFragment;

public class AodWeather extends WeatherPreferenceFragment {
    @Override
    public String getTitle() {
        return getString(R.string.aod_weather);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.aod_weather_prefs;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SYSTEM_UI};
    }

    @Override
    public String getMainSwitchKey() {
        return AOD_WEATHER_SWITCH;
    }
}
