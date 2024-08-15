package it.dhd.oxygencustomizer.ui.base;

import static it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity.DATA_LOCATION_LAT;
import static it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity.DATA_LOCATION_LON;
import static it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity.DATA_LOCATION_NAME;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_CUSTOM_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_ICON_PACK;
import static it.dhd.oxygencustomizer.weather.AbstractWeatherProvider.PART_COORDINATES;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceScreen;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.customprefs.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.customprefs.MaterialSwitchPreference;
import it.dhd.oxygencustomizer.ui.activity.LocationBrowseActivity;
import it.dhd.oxygencustomizer.utils.WeatherScheduler;
import it.dhd.oxygencustomizer.weather.Config;
import it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient;

public abstract class WeatherPreferenceFragment extends ControlledPreferenceFragmentCompat
    implements OmniJawsClient.OmniJawsObserver {

    private static final String DEFAULT_WEATHER_ICON_PACKAGE = "it.dhd.oxygencustomizer.google";
    private MaterialSwitchPreference mCustomLocation;
    private boolean mTriggerPermissionCheck;
    private ListWithPopUpPreference mWeatherIconPack;
    private Preference mUpdateStatus;
    private OmniJawsClient mWeatherClient;
    private Preference mCustomLocationActivity;
    private static final String PREF_KEY_UPDATE_STATUS = "update_status";
    private static final String CHRONUS_ICON_PACK_INTENT = "com.dvtonder.chronus.ICON_PACK";

    public abstract String getMainSwitchKey();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        mWeatherClient = new OmniJawsClient(getContext());

        mWeatherIconPack = findPreference(WEATHER_ICON_PACK);

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

        mUpdateStatus = findPreference(PREF_KEY_UPDATE_STATUS);
        if (mUpdateStatus != null) {
            mUpdateStatus.setOnPreferenceClickListener(preference -> {
                forceRefreshWeatherSettings();
                return true;
            });
        }

        mCustomLocation = findPreference(WEATHER_CUSTOM_LOCATION);
        if (mCustomLocation != null) {
            mCustomLocation.setOnPreferenceClickListener(preference -> {
                forceRefreshWeatherSettings();
                return true;
            });
        }

        mCustomLocationActivity = findPreference("weather_custom_location_picker");
        if (mCustomLocationActivity != null) {
            mCustomLocationActivity.setOnPreferenceClickListener(preference -> {
                mCustomLocationLauncher.launch(new Intent(getContext(), LocationBrowseActivity.class));
                return true;
            });
            mCustomLocationActivity.setSummary(Config.getLocationName(getContext()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeatherClient.addObserver(this);
        if (mPreferences.getBoolean(getMainSwitchKey(), false)
                && !mPreferences.getBoolean(WEATHER_CUSTOM_LOCATION, false)) {
            checkLocationEnabled();
        }
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
                        Config.setLocationId(getContext(), String.valueOf(lat), String.valueOf(lon));
                        Config.setLocationName(getContext(), locationName);
                        mCustomLocationActivity.setSummary(locationName);
                        if (mPreferences.getBoolean(getMainSwitchKey(), false)
                                && !mPreferences.getBoolean(WEATHER_CUSTOM_LOCATION, false)) {
                            checkLocationEnabled();
                        }
                        forceRefreshWeatherSettings();
                    }
                }
            });

    private void checkLocationPermissions(boolean force) {
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

    private void enableService() {
        WeatherScheduler.scheduleUpdates(getContext());
    }

    @Override
    public void onPause() {
        super.onPause();
        mWeatherClient.removeObserver(this);
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);

        if (key == null) return;

        String mainKey = getMainSwitchKey();

        if (key.equals(mainKey)) {
            Config.setEnabled(getContext(), mPreferences.getBoolean(mainKey, false), mainKey);
            if (mPreferences.getBoolean(mainKey, false)) {
                if (!hasPermissions()) {
                    showPermissionDialog();
                }
                enableService();
                if (!mPreferences.getBoolean(WEATHER_CUSTOM_LOCATION, false)) {
                    checkLocationEnabledInitial();
                } else {
                    forceRefreshWeatherSettings();
                }
            }
        } else {
            forceRefreshWeatherSettings();
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
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS) {
            errorString = getResources().getString(R.string.omnijaws_service_error_permissions);
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
        WeatherScheduler.scheduleUpdateNow(getContext());
    }

}
