package it.dhd.oxygencustomizer.ui.fragments.mods;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.WeatherPreferenceFragment;

/**
 * Fragment used for Weather Settings
 */
public class WeatherSettings extends WeatherPreferenceFragment {


    @Override
    public String getMainSwitchKey() {
        return "";
    }

    @Override
    public String getTitle() {
        return getString(R.string.weather_settings);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.weather_settings;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return new String[0];
    }
}
