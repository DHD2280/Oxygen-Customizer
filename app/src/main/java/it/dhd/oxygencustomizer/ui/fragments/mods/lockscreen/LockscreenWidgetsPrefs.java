package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_1_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_2_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_3_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_4_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_EXTRAS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.MAIN_WIDGET_1_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.MAIN_WIDGET_2_KEY;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.Preference;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.PackageListAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.WeatherScheduler;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.weather.WeatherConfig;

public class LockscreenWidgetsPrefs extends ControlledPreferenceFragmentCompat{

    private OmniJawsClient mWeatherClient;
    private PackageListAdapter mPackageAdapter;

    private Map<Preference, String> widgetKeysMap = new HashMap<>();
    private Map<Preference, String> initialWidgetKeysMap = new HashMap<>();

    private ListPreference mMainWidget1;
    private ListPreference mMainWidget2;
    private ListPreference mExtraWidget1;
    private ListPreference mExtraWidget2;
    private ListPreference mExtraWidget3;
    private ListPreference mExtraWidget4;
    private ListPreference mDeviceInfoWidgetPref;

    private List<ListPreference> mWidgetPreferences;

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

        new Thread(() -> {
            mPackageAdapter = new PackageListAdapter(requireActivity());
        }).start();

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

    private void pickApp(String preferenceKey) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        builder.setAdapter(mPackageAdapter, (dialog, which) -> {
            PackageListAdapter.PackageItem info = mPackageAdapter.getItem(which);
            mPreferences.putString(preferenceKey, "customapp:" + info.packageName);
            savePrefs();
        });
        builder.setCancelable(false);
        builder.setTitle(R.string.qs_widget_custom_app);
        builder.show();
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        for (Preference widgetPref : mWidgetPreferences) {
            if (widgetPref != null && key.equals(widgetPref.getKey())) {
                String value = mPreferences.getString(key, "none");
                if (value.equals("customapp")) {
                    pickApp(key);
                }
            }
        }

        savePrefs();
    }

    private void savePrefs() {
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
