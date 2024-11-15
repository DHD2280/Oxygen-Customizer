package it.dhd.oxygencustomizer.ui.base;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;
import static it.dhd.oxygencustomizer.ui.base.BaseActivity.setHeader;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.core.view.WindowCompat;
import androidx.lifecycle.Lifecycle;
import androidx.preference.OplusPreferenceFragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.LocaleHelper;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedSharedPreferences;

public abstract class ControlledPreferenceFragmentCompat extends OplusPreferenceFragment {
    public ExtendedSharedPreferences mPreferences;
    private final SharedPreferences.OnSharedPreferenceChangeListener changeListener = (sharedPreferences, key) -> updateScreen(key);

    public abstract String getTitle();

    public abstract boolean backButtonEnabled();

    public abstract int getLayoutResource();

    public abstract boolean hasMenu();
    public abstract String[] getScopes();

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        getPreferenceManager().setStorageDeviceProtected();
        try {
            setPreferencesFromResource(getLayoutResource(), rootKey);
        } catch (Exception e) {
            Log.e("ControlledPreferenceFragmentCompat", "Error loading preferences", e);
            setPreferencesFromResource(R.xml.mods, rootKey);
        }

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (hasMenu()) {
            MenuHost menuHost = requireActivity();
            // Add menu items without using the Fragment Menu APIs
            // Note how we can tie the MenuProvider to the viewLifecycleOwner
            // and an optional Lifecycle.State (here, RESUMED) to indicate when
            // the menu should be visible
            menuHost.addMenuProvider(new MenuProvider() {
                @Override
                public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                    // Add menu items here
                    menu.add(0, 1, 0, R.string.restart_scopes)
                            .setIcon(R.drawable.ic_restart)
                            .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                            .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
                }

                @Override
                public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                    // Handle the menu selection
                    if (menuItem.getItemId() == 1) {
                        AppUtils.restartScopes(requireActivity(), getScopes());
                        return true;
                    }
                    return true;
                }
            }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        }
        final RecyclerView rv = getListView();
        rv.setPadding(0, 0, 0, dp2px(requireContext(), 16));
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.setLocale(context));

        if (getActivity() != null) {
            var window = requireActivity().getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
    }

    @NonNull
    @Override
    public RecyclerView.Adapter<?> onCreateAdapter(@NonNull PreferenceScreen preferenceScreen) {
        mPreferences = ExtendedSharedPreferences.from(getDefaultSharedPreferences(requireContext().createDeviceProtectedStorageContext()));
        mPreferences.registerOnSharedPreferenceChangeListener(changeListener);
        updateScreen(null);
        return super.onCreateAdapter(preferenceScreen);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            setHeader(getContext(), getTitle());
            ((MainActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(backButtonEnabled());
        }
    }

    @Override
    public void onDestroy() {
        if (mPreferences != null) {
            mPreferences.unregisterOnSharedPreferenceChangeListener(changeListener);
        }
        super.onDestroy();
    }

    public void updateScreen(String key) {
        PreferenceHelper.setupAllPreferences(this.getPreferenceScreen());
    }

}
