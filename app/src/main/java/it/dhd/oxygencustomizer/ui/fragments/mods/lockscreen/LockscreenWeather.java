package it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen;

import static it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity.DATA_LOCATION_LAT;
import static it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity.DATA_LOCATION_LON;
import static it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity.DATA_LOCATION_NAME;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_ICON_PACK;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_PROVIDER;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_UNITS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_UPDATE_INTERVAL;
import static it.dhd.oxygencustomizer.weather.AbstractWeatherProvider.PART_COORDINATES;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.CompoundButton;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.customprefs.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.weather.Config;
import it.dhd.oxygencustomizer.weather.WeatherUpdateService;
import it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient;

public class LockscreenWeather
        extends ControlledPreferenceFragmentCompat
        implements OmniJawsClient.OmniJawsObserver,
        Preference.OnPreferenceChangeListener {

    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "it.dhd.oxygencustomizer.google";
    private SharedPreferences mPrefs;
    private ListPreference mProvider;
    private SwitchPreferenceCompat mCustomLocation;
    private ListPreference mUnits;
    private SwitchPreferenceCompat mEnable;
    private boolean mTriggerPermissionCheck;
    private ListPreference mUpdateInterval;
    private ListWithPopUpPreference mWeatherIconPack;
    private Preference mUpdateStatus;
    private Handler mHandler = new Handler();
    protected boolean mShowIconPack = true;
    private EditTextPreference mOwmKey;
    private OmniJawsClient mWeatherClient;
    private Preference mCustomLocationActivity;
    private static final String PREF_KEY_CUSTOM_LOCATION = "weather_custom_location";
    private static final String WEATHER_ICON_PACK = "weather_icon_pack";
    private static final String PREF_KEY_UPDATE_STATUS = "update_status";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

    @Override
    public String getTitle() {
        return getString(R.string.lockscreen_weather);
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
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.SYSTEM_UI};
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mWeatherClient = new OmniJawsClient(getContext(), false);

        doLoadPreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeatherClient.addObserver(this);
        //doLoadPreferences();
        // values can be changed from outside
        if (mTriggerPermissionCheck) {
            checkLocationPermissions(true);
            mTriggerPermissionCheck = false;
        }
        queryAndUpdateWeather();
    }

    private boolean hasPermissions() {
        return requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && requireContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                && requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    ActivityResultLauncher<Intent> mCustomLocationLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent intent = result.getData();
                    if (intent.hasExtra(DATA_LOCATION_NAME)) {
                        String locationName = intent.getStringExtra(DATA_LOCATION_NAME);
                        double lat = intent.getDoubleExtra(DATA_LOCATION_LAT, 0f);
                        double lon = intent.getDoubleExtra(DATA_LOCATION_LON, 0f);
                        String locationId = String.format(Locale.US, PART_COORDINATES, lat, lon);
                        Config.setLocationId(getContext(), locationId);
                        Config.setLocationName(getContext(), locationName);
                        forceRefreshWeatherSettings();
                    }
                }
            });

    private void checkLocationPermissions(boolean force) {
        Log.d("LockscreenWeather", "checking location permissions");
        if (requireContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || requireContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                requireContext().checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
            });
        } else {
            if (force) {
                forceRefreshWeatherSettings();
            }
            queryAndUpdateWeather();
        }
    }

    private boolean doCheckLocationEnabled() {
        LocationManager lm = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        return lm.isLocationEnabled();
    }

    private void checkLocationEnabled() {
        if (!doCheckLocationEnabled()) {
            showDialog();
        } else {
            checkLocationPermissions(false);
        }
    }

    private void checkLocationEnabledInitial() {
        if (!doCheckLocationEnabled()) {
            showDialog();
        } else {
            checkLocationPermissions(true);
        }
    }

    private void showDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        final Dialog dialog;

        // Build and show the dialog
        builder.setTitle(R.string.weather_retrieve_location_dialog_title);
        builder.setMessage(R.string.weather_retrieve_location_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.weather_retrieve_location_dialog_enable_button,
                (dialog1, whichButton) -> {
                    mTriggerPermissionCheck = true;
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                });
        builder.setNegativeButton(android.R.string.cancel, null);
        dialog = builder.create();
        dialog.show();
    }

    private void showPermissionDialog() {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        builder.setTitle(R.string.weather_permission_dialog_title);
        builder.setMessage(R.string.weather_permission_dialog_message);
        builder.setCancelable(false);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
            intent.setData(uri);
            startActivity(intent);
        });
        builder.show();
    }

    private void disableService() {
        // stop any pending
        WeatherUpdateService.cancelAllUpdate(getContext());
    }

    private void enableService() {
        WeatherUpdateService.scheduleUpdatePeriodic(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        mWeatherClient.removeObserver(this);
    }

    public void doLoadPreferences() {

        if (mPrefs == null) {
            if (PreferenceHelper.instance != null) {
                mPrefs = PreferenceHelper.instance.mPreferences;
            } else {
                mPrefs = getContext().createDeviceProtectedStorageContext().getSharedPreferences(BuildConfig.APPLICATION_ID + "_preferences", Context.MODE_PRIVATE);
            }
        }

        final PreferenceScreen prefScreen = getPreferenceScreen();
        mEnable = findPreference(LOCKSCREEN_WEATHER_SWITCH);
        if (mEnable != null) {
            mEnable.setOnPreferenceChangeListener(this);
        }

        mCustomLocation = findPreference(LOCKSCREEN_WEATHER_CUSTOM_LOCATION);

        mProvider = findPreference(LOCKSCREEN_WEATHER_PROVIDER);
        if (mProvider != null) {
            mProvider.setOnPreferenceChangeListener(this);
        }

        mUnits = findPreference(LOCKSCREEN_WEATHER_UNITS);
        if (mUnits != null) {
            mUnits.setOnPreferenceChangeListener(this);
        }

        mUpdateInterval = findPreference(LOCKSCREEN_WEATHER_UPDATE_INTERVAL);
        if (mUpdateInterval != null) {
            mUpdateInterval.setOnPreferenceChangeListener(this);
        }

        if (mPrefs.getBoolean(LOCKSCREEN_WEATHER_SWITCH, false)
                && !mPrefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_LOCATION, false)) {
            checkLocationEnabled();
        }

        mWeatherIconPack = findPreference(LOCKSCREEN_WEATHER_ICON_PACK);

        if (mShowIconPack) {
            String settingHeaderPackage = Config.getIconPack(getContext());
            List<String> entries = new ArrayList<>();
            List<String> values = new ArrayList<>();
            List<Drawable> drawables = new ArrayList<>();
            getAvailableWeatherIconPacks(entries, values, drawables);
            mWeatherIconPack.setEntries(entries.toArray(new String[0]));
            mWeatherIconPack.setEntryValues(values.toArray(new String[0]));
            mWeatherIconPack.createDefaultAdapter(drawables.toArray(new Drawable[0]),
                    (position) -> mWeatherIconPack.setSummary(entries.get(position)));
            int valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
            if (valueIndex == -1) {
                // no longer found
                settingHeaderPackage = DEFAULT_WEATHER_ICON_PACKAGE;
                //Config.setIconPack(getContext(), settingHeaderPackage);
                valueIndex = mWeatherIconPack.findIndexOfValue(settingHeaderPackage);
            }
            mWeatherIconPack.setValueIndex(valueIndex >= 0 ? valueIndex : 0);
            mWeatherIconPack.setSummary(mWeatherIconPack.getEntry());
            mWeatherIconPack.setOnPreferenceChangeListener(this);
        } else {
            if (mWeatherIconPack != null) prefScreen.removePreference(mWeatherIconPack);
        }
        mUpdateStatus = findPreference(PREF_KEY_UPDATE_STATUS);
        if (mUpdateStatus != null) {
            mUpdateStatus.setOnPreferenceClickListener(preference -> {
                forceRefreshWeatherSettings();
                return true;
            });
        }

        mOwmKey = findPreference(Config.PREF_KEY_OWM_KEY);
        if (mOwmKey != null) {
            mOwmKey.setOnPreferenceChangeListener(this);
        }

        mCustomLocation = findPreference(LOCKSCREEN_WEATHER_CUSTOM_LOCATION);
        if (mCustomLocation != null) {
            mCustomLocation.setOnPreferenceClickListener(preference -> {
                forceRefreshWeatherSettings();
                return true;
            });
        }

        mCustomLocationActivity = findPreference(PREF_KEY_CUSTOM_LOCATION);
        if (mCustomLocationActivity != null) {
            mCustomLocationActivity.setOnPreferenceClickListener(preference -> {
                mCustomLocationLauncher.launch(new Intent(getContext(), LocationBrowseActivity.class));
                return true;
            });
            mCustomLocationActivity.setSummary(Config.getLocationName(getContext()));
        }
    }

    private void getAvailableWeatherIconPacks(List<String> entries, List<String> values, List<Drawable> drawables) {
        Intent i = new Intent();
        PackageManager packageManager = getContext().getPackageManager();
        i.setAction("org.omnirom.WeatherIconPack");
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                values.add(0, r.activityInfo.name);
                drawables.add(0, ResourcesCompat.getDrawable(getResources(), getResources().getIdentifier("google_30", "drawable", BuildConfig.APPLICATION_ID), getContext().getTheme()));
            } else {
                values.add(r.activityInfo.name);
                String[] name = r.activityInfo.name.split("\\.");
                Log.d("LockscreenWeather", "icon: " + name[name.length-1].toLowerCase() + "_30");
                drawables.add(ResourcesCompat.getDrawable(getResources(), getResources().getIdentifier(name[name.length-1].toLowerCase() + "_30", "drawable", BuildConfig.APPLICATION_ID), getContext().getTheme()));
            }
            String label = r.activityInfo.loadLabel(packageManager).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            if (packageName.equals(DEFAULT_WEATHER_ICON_PACKAGE)) {
                entries.add(0, label);
            } else {
                entries.add(label);
            }
        }
        i = new Intent(Intent.ACTION_MAIN);
        i.addCategory(CHRONUS_ICON_PACK_INTENT);
        for (ResolveInfo r : packageManager.queryIntentActivities(i, 0)) {
            String packageName = r.activityInfo.packageName;
            values.add(packageName + ".weather");
            String label = r.activityInfo.loadLabel(packageManager).toString();
            if (label == null) {
                label = r.activityInfo.packageName;
            }
            entries.add(label);
        }
    }

    @Override
    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    @Override
    public void weatherError(int errorReason) {
        String errorString = null;
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            errorString = getResources().getString(R.string.omnijaws_service_disabled);
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_LOCATION) {
            errorString = getResources().getString(R.string.omnijaws_service_error_location);
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_NETWORK) {
            errorString = getResources().getString(R.string.omnijaws_service_error_network);
        } else {
            errorString = getResources().getString(R.string.omnijaws_service_error_long);
        }
        if (errorString != null) {
            final String s = errorString;
            getActivity().runOnUiThread(() -> {
                if (mUpdateStatus != null) {
                    mUpdateStatus.setSummary(s);
                }
            });
        }
    }

    private void queryAndUpdateWeather() {
        mWeatherClient.queryWeather();
        if (mWeatherClient.getWeatherInfo() != null) {
            getActivity().runOnUiThread(() -> {
                if (mUpdateStatus != null) {
                    mUpdateStatus.setSummary(mWeatherClient.getWeatherInfo().getLastUpdateTime());
                }
            });
        }
    }

    ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts
                            .RequestMultiplePermissions(), result -> {
                        Boolean fineLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_FINE_LOCATION, false);
                        Boolean coarseLocationGranted = result.getOrDefault(
                                Manifest.permission.ACCESS_COARSE_LOCATION, false);
                        if ((fineLocationGranted != null && fineLocationGranted) ||
                                (coarseLocationGranted != null && coarseLocationGranted)) {
                            forceRefreshWeatherSettings();
                        }
                    }
            );

    private void forceRefreshWeatherSettings() {
        WeatherUpdateService.scheduleUpdateNow(getContext());
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        if (preference == mEnable) {
            Config.setEnabled(getContext(), (Boolean) newValue);
            if ((Boolean) newValue) {
                if (!hasPermissions()) {
                    showPermissionDialog();
                }
                enableService();
                if (!mCustomLocation.isChecked()) {
                    checkLocationEnabledInitial();
                } else {
                    forceRefreshWeatherSettings();
                }
            } else {
                disableService();
            }
            return true;
        } else if (preference == mProvider) {
            forceRefreshWeatherSettings();
            return true;
        } else if (preference == mUnits) {
            forceRefreshWeatherSettings();
            return true;
        } else if (preference == mUpdateInterval) {
            forceRefreshWeatherSettings();
            return true;
        } else if (preference == mWeatherIconPack) {
            forceRefreshWeatherSettings();
            return true;
        } else if (preference == mOwmKey) {
            forceRefreshWeatherSettings();
            return true;
        }
        return false;
    }

}
