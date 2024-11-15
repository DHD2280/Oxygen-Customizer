package it.dhd.oxygencustomizer.ui.fragments.mods.qsheader;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_ENABLED;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;

public class QsHeader extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.qs_header_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_header_prefs;
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
        super.updateScreen(key);

        ((OplusJumpPreference)findPreference("qs_header_image_main")).setJumpText(
                mPreferences.getBoolean(QS_HEADER_IMAGE_ENABLED, false) ?
                        getString(R.string.general_on) :
                        getString(R.string.general_off)
        );

        ((OplusJumpPreference)findPreference("qs_header_clock_main")).setJumpText(
                mPreferences.getBoolean(QS_HEADER_CLOCK_CUSTOM_ENABLED, false) ?
                        mPreferences.getInt(QS_HEADER_CLOCK_CUSTOM_VALUE, 0) == 0 ?
                                getString(R.string.clock_none) :
                                String.format(getString(R.string.clock_style_name), mPreferences.getInt(QS_HEADER_CLOCK_CUSTOM_VALUE, 0)) :
                        getString(R.string.general_off)
        );

    }

}
