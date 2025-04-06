package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.widgets;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.OplusPreferenceFragment;
import androidx.preference.Preference;

import com.google.gson.Gson;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.dhd.oneplusui.preference.OplusEditTextPreference;
import it.dhd.oneplusui.preference.OplusListPreference;
import it.dhd.oneplusui.preference.OplusMenuPreference;
import it.dhd.oneplusui.preference.OplusPreferenceCategory;
import it.dhd.oneplusui.preference.OplusSwitchPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.interfaces.PreferenceListener;
import it.dhd.oxygencustomizer.utils.json.SettingItem;

public class WidgetSettingsPreferenceFragment extends OplusPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private HashMap<String, Object> settingsMap = new HashMap<>();
    private List<SettingItem> mSettings;
    private PreferenceListener mPreferenceChangeListener;

    public WidgetSettingsPreferenceFragment(List<SettingItem> settingsList) {
        mSettings = settingsList;
    }

    public void setPreferenceChangeListener(PreferenceListener listener) {
        mPreferenceChangeListener = listener;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.empty_pref, rootKey);
        OplusPreferenceCategory category = findPreference("category");

        for (SettingItem setting : mSettings) {
            String key = setting.getKey();
            Object value = setting.getValue();
            settingsMap.put(key, value);

            String title = getStringResource(requireContext(), setting.getTitleResName());

            switch (setting.getType()) {
                case BOOLEAN:
                    OplusSwitchPreference switchPref = new OplusSwitchPreference(getContext());
                    switchPref.setPersistent(false);
                    switchPref.setKey(key);
                    switchPref.setTitle(title);
                    switchPref.setChecked((boolean) value);
                    switchPref.setOnPreferenceChangeListener(this);
                    category.addPreference(switchPref);
                    break;

                case STRING:
                    OplusEditTextPreference editTextPref = new OplusEditTextPreference(getContext());
                    editTextPref.setPersistent(false);
                    editTextPref.setKey(key);
                    editTextPref.setTitle(title);
                    editTextPref.setText((String) value);
                    editTextPref.setSummary(String.valueOf(value));
                    editTextPref.setOnPreferenceChangeListener(this);
                    category.addPreference(editTextPref);
                    break;

                case INT:
                    OplusEditTextPreference intPref = new OplusEditTextPreference(getContext());
                    intPref.setKey(key);
                    intPref.setPersistent(false);
                    intPref.setTitle(title);
                    intPref.setSummary(String.valueOf(value));
                    intPref.setText(String.valueOf(value));
                    intPref.setOnPreferenceChangeListener(this);
                    category.addPreference(intPref);
                    break;

                case LIST:
                    OplusListPreference listPref = new OplusListPreference(getContext());
                    listPref.setPersistent(false);
                    listPref.setKey(key);
                    listPref.setTitle(title);
                    listPref.setEntries(setting.getEntries().toArray(new String[0]));
                    listPref.setEntryValues(setting.getValues().toArray(new String[0]));
                    listPref.setValue(String.valueOf(value));
                    listPref.setSummary(setting.getEntries().toArray(new String[0])[listPref.findIndexOfValue(String.valueOf(value))]);
                    listPref.setOnPreferenceChangeListener(this);
                    category.addPreference(listPref);
                    break;

                case MENU:
                    OplusMenuPreference menuPref = new OplusMenuPreference(requireContext());
                    menuPref.setPersistent(false);
                    menuPref.setKey(key);
                    menuPref.setTitle(title);
                    menuPref.setEntries(setting.getEntries().toArray(new String[0]));
                    menuPref.setEntryValues(setting.getValues().toArray(new String[0]));
                    menuPref.setValue(String.valueOf(value));
                    menuPref.setSummary(setting.getEntries().toArray(new String[0])[menuPref.findIndexOfValue(String.valueOf(value))]);
                    menuPref.setOnPreferenceChangeListener(this);
                    category.addPreference(menuPref);
            }
        }
    }



    private String getStringResource(Context context, String resName) {
        int resId = context.getResources().getIdentifier(resName, "string", context.getPackageName());
        return (resId != 0) ? context.getString(resId) : resName;
    }

    public String getUpdatedSettingsJson() {
        Gson gson = new Gson();
        return gson.toJson(settingsMap);
    }

    public Map<String, Object> getUpdatedSettings() {
        return settingsMap;
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        settingsMap.put(preference.getKey(), newValue);
        if (mPreferenceChangeListener != null) mPreferenceChangeListener.onPreferenceChanged();
        if (preference instanceof OplusListPreference pref) { // OplusMenuPreference is a subclass of OplusListPreference
            preference.setSummary(pref.getEntries()[pref.findIndexOfValue((String) newValue)]);
        } else if (preference instanceof OplusEditTextPreference) {
            preference.setSummary((String) newValue);
        }
        return true;
    }
}
