package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_BG_SHAPE;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_BG_SOLID;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.SELECTED_SETTINGS_ICONS_SET;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.disableOverlay;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.ButtonsAdapter;
import it.dhd.oxygencustomizer.ui.adapters.SectionTitleAdapter;
import it.dhd.oxygencustomizer.ui.adapters.SettingsIconOptions;
import it.dhd.oxygencustomizer.ui.adapters.SettingsIconsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.SettingsIconModel;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;
import it.dhd.oxygencustomizer.utils.overlay.manager.SettingsIconsResourceManager;

public class SettingsIcons extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;
    private ArrayList<SettingsIconModel> settingsIcons = new ArrayList<>();
    private SettingsIconsAdapter iconsAdapter;
    private SettingsIconOptions settingsIconOptions;
    private ButtonsAdapter buttonsAdapter;

    private int mIconPack = 0;
    private boolean mSolidBg = false;
    private int mBgColor = 0;
    private int mBgShape = 0;
    private int mIconColor = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        mSolidBg = Prefs.getBoolean(SELECTED_SETTINGS_ICONS_BG_SOLID, false);
        mIconPack = Prefs.getInt(SELECTED_SETTINGS_ICONS_SET, -1);
        mBgColor = Prefs.getInt(SELECTED_SETTINGS_ICONS_BG_COLOR, 0);
        mBgShape = Prefs.getInt(SELECTED_SETTINGS_ICONS_BG_SHAPE, 0);
        mIconColor = Prefs.getInt(SELECTED_SETTINGS_ICONS_COLOR, 0);

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initSettingsIconsItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> initSettingsIconsItems() {
        settingsIcons.clear();

        // PUI v1
        settingsIcons.add(
                new SettingsIconModel(
                        getString(R.string.settings_icons_pui),
                        "",
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wifi_pui_v1),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wallpaper_pui_v1),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_battery_pui_v1),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_about_pui_v1),
                        true,
                        false,
                        false,
                        false,
                        false));

        // PUI v2
        settingsIcons.add(
                new SettingsIconModel(
                        getString(R.string.settings_icons_pui_v2),
                        "",
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wifi_pui_v2),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wallpaper_pui_v2),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_battery_pui_v2),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_about_pui_v2),
                        true,
                        false,
                        false,
                        false,
                        false));

        // HOS
        settingsIcons.add(
                new SettingsIconModel(
                        getString(R.string.settings_icons_hos),
                        "",
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wifi_hos),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wallpaper_hos),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_battery_hos),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_about_hos),
                        true,
                        true,
                        true,
                        true,
                        true));

        // PUI v3
        settingsIcons.add(
                new SettingsIconModel(
                        getString(R.string.settings_icons_pui_v3),
                        "",
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wifi_pui_v3),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_wallpaper_pui_v3),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_battery_pui_v3),
                        ContextCompat.getDrawable(requireContext(), R.drawable.settings_about_pui_v3),
                        true,
                        false,
                        false,
                        false,
                        false));

        iconsAdapter = new SettingsIconsAdapter(requireContext(), settingsIcons, onSettingsIconClick);
        settingsIconOptions = new SettingsIconOptions(requireContext(), onSettingsIconChange);
        buttonsAdapter = new ButtonsAdapter(onButtonsClick);

        buttonsAdapter.setButtons(true, OverlayUtil.isOverlayEnabled("OxygenCustomizerComponentSIP1.overlay"));

        return new ConcatAdapter(
                settingsIconOptions,
                new SectionTitleAdapter(getString(R.string.theme_customization_settings_icons_title)),
                iconsAdapter,
                buttonsAdapter);
    }

    private final SettingsIconOptions.OnSettingsIconChange onSettingsIconChange = new SettingsIconOptions.OnSettingsIconChange() {
        @Override
        public void onBgColorChanged(int color) {
            mBgColor = color;
        }

        @Override
        public void onBgShapeChanged(int shape) {
            mBgShape = shape;
        }

        @Override
        public void onSolidBgChanged(boolean solidBg) {
            mSolidBg = solidBg;
        }

        @Override
        public void onIconColorChanged(int color) {
            mIconColor = color;
        }
    };

    private final SettingsIconsAdapter.OnSettingsIconClick onSettingsIconClick = new SettingsIconsAdapter.OnSettingsIconClick() {
        @Override
        public void onSettingsIconClick(int position) {
            // Enable or disable the pack
            SettingsIconModel settingsIcon = settingsIcons.get(position);
            mIconPack = position+1;
            settingsIconOptions.setOptions(
                    settingsIcon.hasBgColor(),
                    settingsIcon.hasBgShape(),
                    settingsIcon.hasBgSolid(),
                    settingsIcon.hasIconColor()
            );
        }
    };

    private final ButtonsAdapter.OnButtonClick onButtonsClick = new ButtonsAdapter.OnButtonClick() {
        @Override
        public void onApplyClick() {
            // Apply the changes
            if (!AppUtils.hasStoragePermission()) {
                AppUtils.requestStoragePermission(requireContext());
            } else {
                // Show loading dialog
                loadingDialog.show(OxygenCustomizer.getAppContext().getResources().getString(R.string.loading_dialog_wait));

                new Thread(() -> {
                    AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                    try {
                        hasErroredOut.set(SettingsIconsResourceManager.buildOverlay(mIconPack, mBgColor, mBgShape, mSolidBg, mIconColor, true));
                    } catch (IOException e) {
                        hasErroredOut.set(true);
                        Log.e("SettingsIcons", e.toString());
                    }

                    if (!hasErroredOut.get()) {
                        Prefs.putInt(SELECTED_SETTINGS_ICONS_SET, mIconPack-1);
                        iconsAdapter.notifyItemChanged(mIconPack-1);
                        buttonsAdapter.setButtons(true, true);
                    }

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        // Hide loading dialog
                        loadingDialog.hide();

                        if (!hasErroredOut.get()) {
                            Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        }
                    }, 3000);
                }).start();
            }
        }

        @Override
        public void onDisableClick() {
            // Disable the pack
            loadingDialog.show(OxygenCustomizer.getAppContext().getResources().getString(R.string.loading_dialog_wait));

            new Thread(() -> {
                AtomicBoolean hasErroredOut = new AtomicBoolean(false);

                disableOverlay("OxygenCustomizerComponentSIP1.overlay");
                disableOverlay("OxygenCustomizerComponentSIP2.overlay");

                if (!hasErroredOut.get()) {
                    Prefs.putInt(SELECTED_SETTINGS_ICONS_SET, -1);
                    iconsAdapter.setSelectedPack(-1);
                    buttonsAdapter.setButtons(true, false);
                    mIconPack = -1;
                }

                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    if (!hasErroredOut.get()) {
                        Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_applied), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(OxygenCustomizer.getAppContext(), OxygenCustomizer.getAppContext().getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                    }
                }, 1000);
            }).start();
        }
    };

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public String getTitle() {
        return getString(R.string.theme_customization_settings_icons_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }


}
