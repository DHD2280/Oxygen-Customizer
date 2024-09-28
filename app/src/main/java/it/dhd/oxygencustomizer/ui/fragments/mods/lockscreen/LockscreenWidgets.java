package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_EXTRAS;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.Preference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.WeatherScheduler;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.weather.WeatherConfig;

public class LockscreenWidgets extends ControlledPreferenceFragmentCompat {

    private OmniJawsClient mWeatherClient;

    private static final String MAIN_WIDGET_1_KEY = "main_custom_widgets1";
    private static final String MAIN_WIDGET_2_KEY = "main_custom_widgets2";
    private static final String EXTRA_WIDGET_1_KEY = "custom_widgets1";
    private static final String EXTRA_WIDGET_2_KEY = "custom_widgets2";
    private static final String EXTRA_WIDGET_3_KEY = "custom_widgets3";
    private static final String EXTRA_WIDGET_4_KEY = "custom_widgets4";

    private Map<Preference, String> widgetKeysMap = new HashMap<>();
    private Map<Preference, String> initialWidgetKeysMap = new HashMap<>();

    private Preference mMainWidget1;
    private Preference mMainWidget2;
    private Preference mExtraWidget1;
    private Preference mExtraWidget2;
    private Preference mExtraWidget3;
    private Preference mExtraWidget4;
    private Preference mDeviceInfoWidgetPref;

    private List<Preference> mWidgetPreferences;

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_widgets_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.lockscreen_widgets;
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
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        mWeatherClient = new OmniJawsClient(getContext());
        mWeatherClient.queryWeather();

        mMainWidget1 = findPreference(MAIN_WIDGET_1_KEY);
        mMainWidget2 = findPreference(MAIN_WIDGET_2_KEY);
        mExtraWidget1 = findPreference(EXTRA_WIDGET_1_KEY);
        mExtraWidget2 = findPreference(EXTRA_WIDGET_2_KEY);
        mExtraWidget3 = findPreference(EXTRA_WIDGET_3_KEY);
        mExtraWidget4 = findPreference(EXTRA_WIDGET_4_KEY);
        mDeviceInfoWidgetPref = findPreference("lockscreen_display_widgets");

        mWidgetPreferences = Arrays.asList(
                mMainWidget1,
                mMainWidget2,
                mExtraWidget1,
                mExtraWidget2,
                mExtraWidget3,
                mExtraWidget4,
                mDeviceInfoWidgetPref);
    }

    private List<String> replaceEmptyWithNone(List<String> inputList) {
        return inputList.stream()
                .map(s -> TextUtils.isEmpty(s) ? "none" : s)
                .collect(Collectors.toList());
    }

    private void saveInitialPreferences() {
        initialWidgetKeysMap.clear();
        for (Preference widgetPref : mWidgetPreferences) {
            String value = widgetKeysMap.get(widgetPref);
            initialWidgetKeysMap.put(widgetPref, value);
        }
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        saveInitialPreferences();

        List<String> mainWidgetsList = Arrays.asList(
                mPreferences.getString(MAIN_WIDGET_1_KEY, "none"),
                mPreferences.getString(MAIN_WIDGET_2_KEY, "none"));
        List<String> extraWidgetsList = Arrays.asList(
                mPreferences.getString(EXTRA_WIDGET_1_KEY, "none"),
                mPreferences.getString(EXTRA_WIDGET_2_KEY, "none"),
                mPreferences.getString(EXTRA_WIDGET_3_KEY, "none"),
                mPreferences.getString(EXTRA_WIDGET_4_KEY, "none"));

        mainWidgetsList = replaceEmptyWithNone(mainWidgetsList);
        extraWidgetsList = replaceEmptyWithNone(extraWidgetsList);

        String mainWidgets = TextUtils.join(",", mainWidgetsList);
        String extraWidgets = TextUtils.join(",", extraWidgetsList);

        boolean wasWeatherEnabled = WeatherConfig.isEnabled(getContext());

        mPreferences.putString(LOCKSCREEN_WIDGETS, mainWidgets);
        mPreferences.putString(LOCKSCREEN_WIDGETS_EXTRAS, extraWidgets);

        boolean weatherEnabled =
                mainWidgets.contains("weather") || extraWidgets.contains("weather");

        if (weatherEnabled && wasWeatherEnabled && mWeatherClient.getWeatherInfo() != null) {
            // Weather enabled but updater more than 1h ago
            if (System.currentTimeMillis() - mWeatherClient.getWeatherInfo().timeStamp > 3600000) {
                WeatherScheduler.scheduleUpdateNow(getContext());
            }
        } else if (weatherEnabled) {
            // Weather not enabled (LS/AOD Weather) so we will update now
            WeatherScheduler.scheduleUpdates(getContext());
            WeatherScheduler.scheduleUpdateNow(getContext());
        }

    }

}
