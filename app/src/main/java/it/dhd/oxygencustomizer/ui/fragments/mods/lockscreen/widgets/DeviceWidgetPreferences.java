package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.interfaces.PreferenceListener;

public class DeviceWidgetPreferences extends ControlledPreferenceFragmentCompat {

    private PreferenceListener mListener;

    public void setPreferenceListener(PreferenceListener listener) {
        mListener = listener;
    }

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_display_widgets_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_device_widget_prefs;
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return new String[0];
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key != null && mListener != null) mListener.onPreferenceChanged();

    }

}
