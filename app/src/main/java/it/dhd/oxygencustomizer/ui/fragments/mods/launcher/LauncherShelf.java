package it.dhd.oxygencustomizer.ui.fragments.mods.launcher;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.SelectorWithWidgetPreference;
import it.dhd.oxygencustomizer.utils.Constants;

public class LauncherShelf extends ControlledPreferenceFragmentCompat implements SelectorWithWidgetPreference.OnClickListener {

    private static final String KEY_DISCOVER_STOCK = "launcher_stock";
    private static final String KEY_DISABLE_DISCOVER = "disable_discover";
    private static final String KEY_REPLACE_DISCOVER = "adaptive_playback_timeout_1_min";

    public static final int SHELF_STOCK = 2;
    public static final int SHELF_DISABLE_DISCOVER = 0;
    public static final int SHELF_REPLACE_DISCOVER = 1;

    private boolean mLauncherShelfCustomBehaviorEnabled;
    private int mShelfBehavior;

    private PreferenceCategory mPreferenceCategory;
    private SelectorWithWidgetPreference mStock;
    private SelectorWithWidgetPreference mDisableDiscover;
    private SelectorWithWidgetPreference mReplaceDiscover;

    @Override
    public String getTitle() {
        return getString(R.string.custom_swipe_right_behavior_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.launcher_shelf_feature;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mPreferenceCategory = findPreference("behavior_category");
        mStock = makeRadioPreference(KEY_DISCOVER_STOCK,
                R.string.enable_discover);
        mDisableDiscover = makeRadioPreference(KEY_DISABLE_DISCOVER,
                R.string.disable_discover);
        mReplaceDiscover = makeRadioPreference(KEY_REPLACE_DISCOVER, R.string.replace_discover_with_shelf);
    }

    private SelectorWithWidgetPreference makeRadioPreference(String key, int titleId) {
        SelectorWithWidgetPreference pref = new SelectorWithWidgetPreference(mPreferenceCategory.getContext());
        pref.setKey(key);
        pref.setTitle(titleId);
        pref.setOnClickListener(this);
        mPreferenceCategory.addPreference(pref);
        return pref;
    }

    /**
     * Called when a preference has been clicked.
     *
     * @param emiter The clicked preference
     */
    @Override
    public void onRadioButtonClicked(SelectorWithWidgetPreference emiter) {
        int shelfBehavior = keyToSetting(emiter.getKey());
        if (shelfBehavior != mPreferences.getInt("laucher_shelf_custom", SHELF_DISABLE_DISCOVER)) {
            mPreferences.putInt("laucher_shelf_custom", shelfBehavior);
            mShelfBehavior = shelfBehavior;
        }
    }

    private static int keyToSetting(String key) {
        return switch (key) {
            case KEY_DISCOVER_STOCK -> SHELF_STOCK;
            case KEY_REPLACE_DISCOVER -> SHELF_REPLACE_DISCOVER;
            default -> SHELF_DISABLE_DISCOVER;
        };
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        mLauncherShelfCustomBehaviorEnabled = mPreferences.getBoolean("launcher_custom_shelf_switch", false);
        mShelfBehavior = mPreferences.getInt("laucher_shelf_custom", SHELF_DISABLE_DISCOVER);
        final boolean isStockShelf = mLauncherShelfCustomBehaviorEnabled
                && mShelfBehavior == SHELF_STOCK;
        final boolean isDiscoverDisabled = mLauncherShelfCustomBehaviorEnabled
                && mShelfBehavior == SHELF_DISABLE_DISCOVER;
        final boolean isDiscoverReplaced = mLauncherShelfCustomBehaviorEnabled
                && mShelfBehavior == SHELF_REPLACE_DISCOVER;

        if (mStock != null && mStock.isChecked() != isStockShelf) {
            mStock.setChecked(isStockShelf);
        }
        if (mDisableDiscover != null && mDisableDiscover.isChecked() != isDiscoverDisabled) {
            mDisableDiscover.setChecked(isDiscoverDisabled);
        }
        if (mReplaceDiscover != null && mReplaceDiscover.isChecked() != isDiscoverReplaced) {
            mReplaceDiscover.setChecked(isDiscoverReplaced);
        }

        if (mLauncherShelfCustomBehaviorEnabled) {
            mPreferenceCategory.setEnabled(true);
            mStock.setEnabled(true);
            mDisableDiscover.setEnabled(true);
            mReplaceDiscover.setEnabled(true);
        } else {
            mPreferenceCategory.setEnabled(false);
            mStock.setEnabled(false);
            mDisableDiscover.setEnabled(false);
            mReplaceDiscover.setEnabled(false);
        }
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.LAUNCHER};
    }

}
