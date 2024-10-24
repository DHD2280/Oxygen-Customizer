package it.dhd.oxygencustomizer.ui.activity;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static it.dhd.oxygencustomizer.ui.fragments.UpdateFragment.UPDATES_CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;
import com.topjohnwu.superuser.Shell;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ActivityMainBinding;
import it.dhd.oxygencustomizer.ui.base.BaseActivity;
import it.dhd.oxygencustomizer.ui.events.ColorDismissedEvent;
import it.dhd.oxygencustomizer.ui.events.ColorSelectedEvent;
import it.dhd.oxygencustomizer.ui.fragments.Hooks;
import it.dhd.oxygencustomizer.ui.fragments.Mods;
import it.dhd.oxygencustomizer.ui.fragments.Settings;
import it.dhd.oxygencustomizer.ui.fragments.UpdateFragment;
import it.dhd.oxygencustomizer.ui.fragments.UserInterface;
import it.dhd.oxygencustomizer.ui.fragments.mods.Buttons;
import it.dhd.oxygencustomizer.ui.fragments.mods.Launcher;
import it.dhd.oxygencustomizer.ui.fragments.mods.Statusbar;
import it.dhd.oxygencustomizer.ui.fragments.mods.WeatherSettings;
import it.dhd.oxygencustomizer.ui.fragments.mods.aod.AodClock;
import it.dhd.oxygencustomizer.ui.fragments.mods.aod.AodWeather;
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.Lockscreen;
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.LockscreenClockFragment;
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.LockscreenWeather;
import it.dhd.oxygencustomizer.ui.fragments.mods.navbar.Gesture;
import it.dhd.oxygencustomizer.ui.fragments.mods.qsheader.QsHeaderClock;
import it.dhd.oxygencustomizer.ui.fragments.mods.qsheader.QsHeaderImage;
import it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.QuickSettings;
import it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.QuickSettingsCustomization;
import it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.QuickSettingsTiles;
import it.dhd.oxygencustomizer.ui.models.SearchPreferenceItem;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreferenceResult;
import it.dhd.oxygencustomizer.ui.preferences.preferencesearch.SearchPreferenceResultListener;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedSharedPreferences;

public class MainActivity extends BaseActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, ColorPickerDialogListener, SearchPreferenceResultListener {

    private Integer selectedFragment = null;
    private ActivityMainBinding binding;
    private static FragmentManager fragmentManager;
    private static final String TITLE_TAG = "mainActivityTitle";
    private static ActionBar actionBar;
    private ColorPickerDialog.Builder colorPickerDialog;
    public static final List<SearchPreferenceItem> prefsList = new ArrayList<>();
    private Mods modsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        createChannels();

        if (savedInstanceState == null) {
            replaceFragment(!OverlayUtil.overlayExists() ?
                    new Mods() :
                    new UserInterface());
        } else {
            setHeader(this, savedInstanceState.getCharSequence(TITLE_TAG));
        }

        if (getIntent() != null) {
            if (getIntent().getBooleanExtra("updateTapped", false)) {
                Intent intent = getIntent();
                Bundle bundle = new Bundle();
                bundle.putBoolean("updateTapped", intent.getBooleanExtra("updateTapped", false));
                bundle.putString("filePath", intent.getStringExtra("filePath"));
                UpdateFragment updateFragment = new UpdateFragment();
                updateFragment.setArguments(bundle);
                replaceFragment(updateFragment);
            } else if (getIntent().getBooleanExtra("newUpdate", false)) {
                replaceFragment(new UpdateFragment());
            } else if (getIntent().getBooleanExtra("openWeatherSettings", false)) {
                replaceFragment(new Mods());
                replaceFragment(new WeatherSettings());
            }
        }

        if (!prefsList.isEmpty()) prefsList.clear();
        prefsList.add(new SearchPreferenceItem(R.xml.mods, R.string.mods_title, new Mods()));
        prefsList.add(new SearchPreferenceItem(R.xml.statusbar, R.string.statusbar_title, new Statusbar()));
        prefsList.add(new SearchPreferenceItem(R.xml.statusbar_clock, R.string.status_bar_clock_title, new Statusbar.Clock()));
        prefsList.add(new SearchPreferenceItem(R.xml.statusbar_notifications, R.string.statusbar_notifications, new Statusbar.Notifications()));
        prefsList.add(new SearchPreferenceItem(R.xml.battery_bar_settings, R.string.statusbar_batterybar_title, new Statusbar.BatteryBar()));
        prefsList.add(new SearchPreferenceItem(R.xml.statusbar_battery_icon, R.string.statusbar_battery_icon_options, new Statusbar.BatteryIcon()));
        prefsList.add(new SearchPreferenceItem(R.xml.statusbar_icons, R.string.statusbar_icons, new Statusbar.Icons()));
        prefsList.add(new SearchPreferenceItem(R.xml.quick_settings_mods, R.string.quick_settings_title, new QuickSettings()));
        prefsList.add(new SearchPreferenceItem(R.xml.quick_settings_tiles_prefs, R.string.quick_settings_tiles_title, new QuickSettingsTiles()));
        prefsList.add(new SearchPreferenceItem(R.xml.quick_settings_tiles_customizations_prefs, R.string.quick_settings_tiles_customization_title, new QuickSettingsCustomization()));
        prefsList.add(new SearchPreferenceItem(R.xml.qs_header_image_prefs, R.string.qs_header_image_title, new QsHeaderImage()));
        prefsList.add(new SearchPreferenceItem(R.xml.qs_header_clock_prefs, R.string.qs_header_clock, new QsHeaderClock()));
        prefsList.add(new SearchPreferenceItem(R.xml.gesture_prefs, R.string.gesture_navigation_title, new Gesture()));
        prefsList.add(new SearchPreferenceItem(R.xml.buttons_prefs, R.string.buttons_title, new Buttons()));
        if (AppUtils.isAppInstalled(this, Constants.Packages.LAUNCHER))
            prefsList.add(new SearchPreferenceItem(R.xml.launcher_mods, R.string.launcher_title, new Launcher()));
        prefsList.add(new SearchPreferenceItem(R.xml.lockscreen_prefs, R.string.lockscreen_title, new Lockscreen()));
        prefsList.add(new SearchPreferenceItem(R.xml.lockscreen_clock, R.string.lockscreen_clock, new LockscreenClockFragment()));
        prefsList.add(new SearchPreferenceItem(R.xml.lockscreen_weather_prefs, R.string.lockscreen_weather, new LockscreenWeather()));
        prefsList.add(new SearchPreferenceItem(R.xml.aod_clock_prefs, R.string.aod_clock, new AodClock()));
        prefsList.add(new SearchPreferenceItem(R.xml.aod_weather_prefs, R.string.aod_weather, new AodWeather()));
        prefsList.add(new SearchPreferenceItem(R.xml.sound_mods, R.string.sound, new Mods.Sound()));
        prefsList.add(new SearchPreferenceItem(R.xml.package_manager_prefs, R.string.package_manager, new Mods.PackageManager()));
        prefsList.add(new SearchPreferenceItem(R.xml.misc_prefs, R.string.misc, new Mods.Misc()));

        PreferenceHelper.init(ExtendedSharedPreferences.from(getDefaultSharedPreferences(createDeviceProtectedStorageContext())));


        // Setup navigation
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        setupBottomNavigationView();

        colorPickerDialog = ColorPickerDialog.newBuilder();

        if (!AppUtils.hasStoragePermission()) {
            AppUtils.requestStoragePermission(this);
        }
        File modDir = new File(Constants.XPOSED_RESOURCE_TEMP_DIR);
        if (!modDir.exists()) {
            Shell.cmd("mkdir -p " + Constants.XPOSED_RESOURCE_TEMP_DIR).exec();
        }

    }



    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigationView() {
        if (!OverlayUtil.overlayExists()) {
            binding.bottomNavigationView.getMenu().clear();
            binding.bottomNavigationView.inflateMenu(R.menu.bottom_nav_menu_xposed_only);
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            String tag = getTopFragment();

            if (Objects.equals(tag, UserInterface.class.getSimpleName())) {
                selectedFragment = R.id.ui;
                binding.bottomNavigationView.getMenu().getItem(0).setChecked(true);
            } else if (Objects.equals(tag, Mods.class.getSimpleName())) {
                selectedFragment = R.id.mods;
                binding.bottomNavigationView.getMenu().getItem(!OverlayUtil.overlayExists() ? 0 : 1).setChecked(true);
            } else if (Objects.equals(tag, UpdateFragment.class.getSimpleName())) {
                selectedFragment = R.id.updates;
                binding.bottomNavigationView.getMenu().getItem(!OverlayUtil.overlayExists() ? 1 : 2).setChecked(true);
            } else if (Objects.equals(tag, Hooks.class.getSimpleName())) {
                selectedFragment = R.id.hooks;
                binding.bottomNavigationView.getMenu().getItem(!OverlayUtil.overlayExists() ? 2 : 3).setChecked(true);
            } else if (Objects.equals(tag, Settings.class.getSimpleName())) {
                selectedFragment = R.id.settings;
                binding.bottomNavigationView.getMenu().getItem(!OverlayUtil.overlayExists() ? 3 : 4).setChecked(true);
            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            String tag = getTopFragment();

            switch (item.getItemId()) {
                case R.id.ui -> {
                    if (!Objects.equals(tag, UserInterface.class.getSimpleName())) {
                        selectedFragment = R.id.ui;
                        replaceFragment(new UserInterface());
                    }
                    return true;
                }
                case R.id.mods -> {
                    if (!Objects.equals(tag, Mods.class.getSimpleName())) {
                        selectedFragment = R.id.mods;
                        replaceFragment(new Mods());
                    }
                    return true;
                }
                case R.id.updates -> {
                    if (!Objects.equals(tag, UpdateFragment.class.getSimpleName())) {
                        selectedFragment = R.id.updates;
                        replaceFragment(new UpdateFragment());
                    }
                    return true;
                }
                case R.id.hooks -> {
                    if (!Objects.equals(tag, Hooks.class.getSimpleName())) {
                        selectedFragment = R.id.hooks;
                        replaceFragment(new Hooks());
                    }
                    return true;
                }
                case R.id.settings -> {
                    if (!Objects.equals(tag, Settings.class.getSimpleName())) {
                        selectedFragment = R.id.settings;
                        replaceFragment(new Settings());
                    }
                    return true;
                }
                default -> {
                    return true;
                }
            }
        });
    }

    private String getTopFragment() {
        String[] fragment = {null};

        int last = getSupportFragmentManager().getFragments().size() - 1;

        if (last >= 0) {
            Fragment topFragment = getSupportFragmentManager().getFragments().get(last);

            if (topFragment instanceof UserInterface)
                fragment[0] = UserInterface.class.getSimpleName();
            else if (topFragment instanceof Mods)
                fragment[0] = Mods.class.getSimpleName();
            else if (topFragment instanceof UpdateFragment)
                fragment[0] = UpdateFragment.class.getSimpleName();
            else if (topFragment instanceof Hooks)
                fragment[0] = Hooks.class.getSimpleName();
            else if (topFragment instanceof Settings)
                fragment[0] = Settings.class.getSimpleName();
        }

        return fragment[0];
    }

    public static void replaceFragment(Fragment fragment) {
        String tag = fragment.getClass().getSimpleName();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out, R.anim.fragment_fade_in, R.anim.fragment_fade_out);
        fragmentTransaction.replace(R.id.frame_layout, fragment, tag);
        if (Objects.equals(tag, UserInterface.class.getSimpleName())) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (Objects.equals(tag, Mods.class.getSimpleName()) && !OverlayUtil.overlayExists()) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (Objects.equals(tag, Mods.class.getSimpleName()) ||
                Objects.equals(tag, Hooks.class.getSimpleName()) ||
                Objects.equals(tag, Settings.class.getSimpleName())) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
            fragmentTransaction.addToBackStack(tag);
        } else {
            fragmentTransaction.addToBackStack(tag);
        }

        fragmentTransaction.commit();
    }

    @SuppressLint("Deprecated")
    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(getClassLoader(), Objects.requireNonNull(pref.getFragment()));
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);
        // Replace the existing Fragment with the new Fragment
        replaceFragment(fragment);
        return true;
    }

    public static void backButtonEnabled() {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    public static void backButtonDisabled() {
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setDisplayShowHomeEnabled(false);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            getOnBackPressedDispatcher().onBackPressed();
            return true;
        }
        return false;
    }

    public void showColorPickerDialog(int dialogId, int defaultColor, boolean showPresets, boolean showAlphaSlider, boolean showColorShades) {
        colorPickerDialog
                .setColor(defaultColor)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setAllowCustom(false)
                .setAllowPresets(showPresets)
                .setDialogId(dialogId)
                .setShowAlphaSlider(showAlphaSlider)
                .setShowColorShades(showColorShades);
        colorPickerDialog.show(this);
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        EventBus.getDefault().post(new ColorSelectedEvent(dialogId, color));
    }

    @Override
    public void onDialogDismissed(int dialogId) {
        EventBus.getDefault().post(new ColorDismissedEvent(dialogId));
    }

    @Override
    public void onSearchResultClicked(@NonNull SearchPreferenceResult result) {
        modsFragment = new Mods();
        new Handler(getMainLooper()).post(() -> modsFragment.onSearchResultClicked(result));
    }

    private void createChannels() {
        CharSequence name = getString(R.string.update_channel_name);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        NotificationChannel channel = new NotificationChannel(UPDATES_CHANNEL_ID, name, importance);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);
    }

}
