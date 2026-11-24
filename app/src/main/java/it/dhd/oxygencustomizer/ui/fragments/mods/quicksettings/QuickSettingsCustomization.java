package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;

import it.dhd.oneplusui.preference.OplusJumpPreference;
import it.dhd.oneplusui.preference.OplusPreferenceCategory;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class QuickSettingsCustomization extends ControlledPreferenceFragmentCompat {


    @Override
    public String getTitle() {
        return getString(R.string.quick_settings_tiles_customization_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.quick_settings_tiles_customizations_prefs;
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        OplusJumpPreference highlightTile = findPreference("highlight_tile");
        OplusJumpPreference baseTile = findPreference("base_tile");
        boolean shouldHide = PreferenceHelper.is16010() || Build.VERSION.SDK_INT >= 37;
        if (highlightTile != null) {
            highlightTile.setVisible(!shouldHide);
        }
        if (baseTile != null) {
            baseTile.setVisible(!shouldHide);
        }

        OplusPreferenceCategory tileBorder = findPreference("tile_border_17");
        if (tileBorder != null) {
            tileBorder.setVisible(Build.VERSION.SDK_INT >= 37);
            // A selected pack owns the edge, so the manual controls would do nothing.
            boolean packApplied = !TextUtils.isEmpty(OCPreferences.getString(
                    Constants.Preferences.QsTilesCustomization.QS_STYLE_PACK, ""));
            Preference borderSwitch = findPreference(
                    Constants.Preferences.QsTilesCustomization.QS_TILE_BORDER_SWITCH);
            if (borderSwitch != null) {
                borderSwitch.setEnabled(!packApplied);
                borderSwitch.setSummary(packApplied ? getString(R.string.qs_tile_border_pack_active) : null);
            }
        }

    }

}
