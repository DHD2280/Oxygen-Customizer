package it.dhd.oxygencustomizer.ui.activity;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.customprefs.preferencesearch.SearchPreferenceResult;
import it.dhd.oxygencustomizer.customprefs.preferencesearch.SearchPreferenceResultListener;
import it.dhd.oxygencustomizer.databinding.ActivityMainBinding;
import it.dhd.oxygencustomizer.ui.events.ColorDismissedEvent;
import it.dhd.oxygencustomizer.ui.events.ColorSelectedEvent;
import it.dhd.oxygencustomizer.ui.fragments.Hooks;
import it.dhd.oxygencustomizer.ui.fragments.Mods;
import it.dhd.oxygencustomizer.ui.fragments.Settings;
import it.dhd.oxygencustomizer.ui.fragments.mods.Buttons;
import it.dhd.oxygencustomizer.ui.fragments.mods.Launcher;
import it.dhd.oxygencustomizer.ui.fragments.mods.lockscreen.Lockscreen;
import it.dhd.oxygencustomizer.ui.fragments.mods.Statusbar;
import it.dhd.oxygencustomizer.ui.fragments.mods.navbar.Gesture;
import it.dhd.oxygencustomizer.ui.fragments.mods.qsheader.QsHeaderClock;
import it.dhd.oxygencustomizer.ui.fragments.mods.qsheader.QsHeaderImage;
import it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.QuickSettings;
import it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.QuickSettingsCustomization;
import it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings.QuickSettingsTiles;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedSharedPreferences;

public class MainActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback, ColorPickerDialogListener, SearchPreferenceResultListener {

    private Integer selectedFragment = null;
    private NavHostFragment navHostFragment;
    private ActivityMainBinding binding;
    private BottomNavigationView navigationView;
    private static FragmentManager fragmentManager;
    private static final String TITLE_TAG = "mainActivityTitle";
    private static ActionBar actionBar;
    private ColorPickerDialog.Builder colorPickerDialog;
    public static final List<Object[]> prefsList = new ArrayList<>();
    private Mods modsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fragmentManager = getSupportFragmentManager();

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (savedInstanceState == null) {
            replaceFragment(new Mods());
        } else {
            setHeader(this, savedInstanceState.getCharSequence(TITLE_TAG));
        }

        prefsList.add(new Object[]{R.xml.mods, R.string.mods_title, new Mods()});
        prefsList.add(new Object[]{R.xml.statusbar, R.string.statusbar_title, new Statusbar()});
        prefsList.add(new Object[]{R.xml.statusbar_clock, R.string.status_bar_clock_title, new Statusbar.Clock()});
        prefsList.add(new Object[]{R.xml.statusbar_notifications, R.string.statusbar_notifications, new Statusbar.Notifications()});
        prefsList.add(new Object[]{R.xml.battery_bar_settings, R.string.statusbar_batterybar_title, new Statusbar.BatteryBar()});
        prefsList.add(new Object[]{R.xml.statusbar_battery_icon, R.string.statusbar_battery_icon_options, new Statusbar.BatteryIcon()});
        prefsList.add(new Object[]{R.xml.quick_settings_mods, R.string.quick_settings_title, new QuickSettings()});
        prefsList.add(new Object[]{R.xml.quick_settings_tiles_prefs, R.string.quick_settings_tiles_title, new QuickSettingsTiles()});
        prefsList.add(new Object[]{R.xml.quick_settings_tiles_customizations_prefs, R.string.quick_settings_tiles_customization_title, new QuickSettingsCustomization()});
        prefsList.add(new Object[]{R.xml.qs_header_image_prefs, R.string.qs_header_image_title, new QsHeaderImage()});
        prefsList.add(new Object[]{R.xml.qs_header_clock_prefs, R.string.qs_header_clock, new QsHeaderClock()});
        prefsList.add(new Object[]{R.xml.gesture_prefs, R.string.gesture_navigation_title, new Gesture()});
        prefsList.add(new Object[]{R.xml.buttons_prefs, R.string.buttons_title, new Buttons()});
        if (AppUtils.isAppInstalled(this, Constants.Packages.LAUNCHER))
            prefsList.add(new Object[]{R.xml.launcher_mods, R.string.launcher_title, new Launcher()});
        prefsList.add(new Object[]{R.xml.lockscreen_prefs, R.string.lockscreen_title, new Lockscreen()});
        prefsList.add(new Object[]{R.xml.lockscreen_clock, R.string.lockscreen_clock, new Lockscreen.LockscreenClock()});
        prefsList.add(new Object[]{R.xml.sound_mods, R.string.sound, new Mods.Sound()});
        prefsList.add(new Object[]{R.xml.package_manager_prefs, R.string.package_manager, new Mods.PackageManager()});
        prefsList.add(new Object[]{R.xml.misc_prefs, R.string.misc, new Mods.Misc()});

        PreferenceHelper.init(ExtendedSharedPreferences.from(getDefaultSharedPreferences(createDeviceProtectedStorageContext())));

        // Setup navigation
        setSupportActionBar(binding.toolbar);
        actionBar = getSupportActionBar();
        setupNavigation();
        setupBottomNavigationView();

        colorPickerDialog = ColorPickerDialog.newBuilder();
    }

    private void setupNavigation() {
        //navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerView);
        if (navHostFragment == null) return;

        NavController navController = navHostFragment.getNavController();
        AppBarConfiguration appBarConfiguration =
                new AppBarConfiguration.Builder(
                        R.id.mods,
                        R.id.hooks,
                        R.id.settings
                ).build();

        NavigationUI.setupWithNavController(
                binding.toolbar, navController, appBarConfiguration);
        navigationView = binding.bottomNavigationView;
        binding.bottomNavigationView.setOnItemSelectedListener(item -> NavigationUI.onNavDestinationSelected(item, navController));

    }

    @SuppressLint("NonConstantResourceId")
    private void setupBottomNavigationView() {
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            String tag = getTopFragment();

            if (Objects.equals(tag, Mods.class.getSimpleName())) {
                selectedFragment = R.id.mods;
                binding.bottomNavigationView.getMenu().getItem(0).setChecked(true);
                setHeader(this, getString(R.string.app_name));
                backButtonDisabled();
            } else if (Objects.equals(tag, Hooks.class.getSimpleName())) {
                selectedFragment = R.id.hooks;
                binding.bottomNavigationView.getMenu().getItem(1).setChecked(true);
                setHeader(this, getString(R.string.hooked_packages_title));
                backButtonDisabled();
            } else if (Objects.equals(tag, Settings.class.getSimpleName())) {
                selectedFragment = R.id.settings;
                binding.bottomNavigationView.getMenu().getItem(2).setChecked(true);
                setHeader(this, getString(R.string.navbar_settings));
                backButtonDisabled();
            }
        });

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            String tag = getTopFragment();

            switch (item.getItemId()) {
                case R.id.mods -> {
                    if (!Objects.equals(tag, Mods.class.getSimpleName())) {
                        selectedFragment = R.id.mods;
                        replaceFragment(new Mods());
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

            if (topFragment instanceof Mods)
                fragment[0] = Mods.class.getSimpleName();
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
        if (Objects.equals(tag, Mods.class.getSimpleName())) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        } else if (Objects.equals(tag, Hooks.class.getSimpleName()) ||
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

    public void setHeader(Context context, int title) {
        Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        toolbar.setTitle(title);
    }

    public void setHeader(Context context, CharSequence title) {
        Toolbar toolbar = ((AppCompatActivity) context).findViewById(R.id.toolbar);
        ((AppCompatActivity) context).setSupportActionBar(toolbar);
        toolbar.setTitle(title);
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
    protected void onResume() {
        super.onResume();
        if (Objects.equals(getTopFragment(), Mods.class.getSimpleName()) ||
                Objects.equals(getTopFragment(), Hooks.class.getSimpleName()) ||
                Objects.equals(getTopFragment(), Settings.class.getSimpleName())) {
            backButtonDisabled();
        } else {
            backButtonEnabled();
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
        Log.d("ColorPickerDialog", "showColorPickerDialog: " + dialogId);
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
}
