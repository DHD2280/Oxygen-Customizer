package it.dhd.oxygencustomizer.ui.fragments.mods.sound;

import android.os.Bundle;

import androidx.preference.PreferenceCategory;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.SelectorWithWidgetPreference;

public class AdaptivePlaybackSoundSettings extends ControlledPreferenceFragmentCompat implements SelectorWithWidgetPreference.OnClickListener {

    private static final String KEY_NO_TIMEOUT = "adaptive_playback_timeout_none";
    private static final String KEY_30_SECS = "adaptive_playback_timeout_30_secs";
    private static final String KEY_1_MIN = "adaptive_playback_timeout_1_min";
    private static final String KEY_2_MIN = "adaptive_playback_timeout_2_min";
    private static final String KEY_5_MIN = "adaptive_playback_timeout_5_min";
    private static final String KEY_10_MIN = "adaptive_playback_timeout_10_min";

    public static final int ADAPTIVE_PLAYBACK_TIMEOUT_NONE = 0;
    public static final int ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS = 30000;
    public static final int ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN = 60000;
    public static final int ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN = 120000;
    public static final int ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN = 300000;
    public static final int ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN = 600000;

    private boolean mAdaptivePlaybackEnabled;
    private int mAdaptivePlaybackTimeout;

    private PreferenceCategory mPreferenceCategory;
    private SelectorWithWidgetPreference mTimeoutNonePref;
    private SelectorWithWidgetPreference mTimeout30SecPref;
    private SelectorWithWidgetPreference mTimeout1MinPref;
    private SelectorWithWidgetPreference mTimeout2MinPref;
    private SelectorWithWidgetPreference mTimeout5MinPref;
    private SelectorWithWidgetPreference mTimeout10MinPref;

    @Override
    public String getTitle() {
        return getString(R.string.adaptive_playback_screen_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.adaptive_playback_sound_mods;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mPreferenceCategory = findPreference("sound_adaptive_playback_category");
        mTimeoutNonePref = makeRadioPreference(KEY_NO_TIMEOUT,
                R.string.adaptive_playback_timeout_none);
        mTimeout30SecPref = makeRadioPreference(KEY_30_SECS,
                R.string.adaptive_playback_timeout_30_secs);
        mTimeout1MinPref = makeRadioPreference(KEY_1_MIN, R.string.adaptive_playback_timeout_1_min);
        mTimeout2MinPref = makeRadioPreference(KEY_2_MIN, R.string.adaptive_playback_timeout_2_min);
        mTimeout5MinPref = makeRadioPreference(KEY_5_MIN, R.string.adaptive_playback_timeout_5_min);
        mTimeout10MinPref = makeRadioPreference(KEY_10_MIN,
                R.string.adaptive_playback_timeout_10_min);
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
        int adaptivePlaybackTimeout = keyToSetting(emiter.getKey());
        if (adaptivePlaybackTimeout != mPreferences.getInt("adaptive_playback_timeout", ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS)) {
            mPreferences.putInt("adaptive_playback_timeout", adaptivePlaybackTimeout);
            mAdaptivePlaybackTimeout = adaptivePlaybackTimeout;
        }
    }

    private static int keyToSetting(String key) {
        return switch (key) {
            case KEY_NO_TIMEOUT -> ADAPTIVE_PLAYBACK_TIMEOUT_NONE;
            case KEY_1_MIN -> ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN;
            case KEY_2_MIN -> ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN;
            case KEY_5_MIN -> ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN;
            case KEY_10_MIN -> ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN;
            default -> ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
        };
    }

    @Override
    public void updateScreen(String key) {
        mAdaptivePlaybackEnabled = mPreferences.getBoolean("sound_adaptive_playback_main_switch", false);
        mAdaptivePlaybackTimeout = mPreferences.getInt("adaptive_playback_timeout", ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS);
        final boolean isTimeoutNone = mAdaptivePlaybackEnabled
                && mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_NONE;
        final boolean isTimeout30Sec = mAdaptivePlaybackEnabled
                && mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_30_SECS;
        final boolean isTimeout1Min = mAdaptivePlaybackEnabled
                && mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_1_MIN;
        final boolean isTimeout2Min = mAdaptivePlaybackEnabled
                && mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_2_MIN;
        final boolean isTimeout5Min = mAdaptivePlaybackEnabled
                && mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_5_MIN;
        final boolean isTimeout10Min = mAdaptivePlaybackEnabled
                && mAdaptivePlaybackTimeout == ADAPTIVE_PLAYBACK_TIMEOUT_10_MIN;
        if (mTimeoutNonePref != null && mTimeoutNonePref.isChecked() != isTimeoutNone) {
            mTimeoutNonePref.setChecked(isTimeoutNone);
        }
        if (mTimeout30SecPref != null && mTimeout30SecPref.isChecked() != isTimeout30Sec) {
            mTimeout30SecPref.setChecked(isTimeout30Sec);
        }
        if (mTimeout1MinPref != null && mTimeout1MinPref.isChecked() != isTimeout1Min) {
            mTimeout1MinPref.setChecked(isTimeout1Min);
        }
        if (mTimeout2MinPref != null && mTimeout2MinPref.isChecked() != isTimeout2Min) {
            mTimeout2MinPref.setChecked(isTimeout2Min);
        }
        if (mTimeout5MinPref != null && mTimeout5MinPref.isChecked() != isTimeout5Min) {
            mTimeout5MinPref.setChecked(isTimeout5Min);
        }
        if (mTimeout10MinPref != null && mTimeout10MinPref.isChecked() != isTimeout10Min) {
            mTimeout10MinPref.setChecked(isTimeout10Min);
        }

        if (mAdaptivePlaybackEnabled) {
            mPreferenceCategory.setEnabled(true);
            mTimeoutNonePref.setEnabled(true);
            mTimeout30SecPref.setEnabled(true);
            mTimeout1MinPref.setEnabled(true);
            mTimeout2MinPref.setEnabled(true);
            mTimeout5MinPref.setEnabled(true);
            mTimeout10MinPref.setEnabled(true);
        } else {
            mPreferenceCategory.setEnabled(false);
            mTimeoutNonePref.setEnabled(false);
            mTimeout30SecPref.setEnabled(false);
            mTimeout1MinPref.setEnabled(false);
            mTimeout2MinPref.setEnabled(false);
            mTimeout5MinPref.setEnabled(false);
            mTimeout10MinPref.setEnabled(false);
        }
    }

    @Override
    public boolean hasMenu() {
        return false;
    }

    @Override
    public String[] getScopes() {
        return null;
    }

}
