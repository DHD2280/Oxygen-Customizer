package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.BACKGROUND;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentAppListBinding;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.adapters.DarkShadowColorsAdapter;
import it.dhd.oxygencustomizer.ui.adapters.FooterWidgetAdapter;
import it.dhd.oxygencustomizer.ui.adapters.SectionTitleAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;
import it.dhd.oxygencustomizer.utils.ColorUtils;
import it.dhd.oxygencustomizer.utils.DarkShadowUtils;
import it.dhd.oxygencustomizer.utils.overlay.FabricatedUtil;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class DarkShadowThemeFragment extends BaseFragment {

    String[] overlays = new String[]{"DST", "DSTSUI", "DSTSTG"};
    private List<DarkShadowItem> mDarkShadowColors = new ArrayList<>() {{
        add(ACCENT);
        add(BACKGROUND);
    }};

    private FragmentAppListBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public String getTitle() {
        return getString(R.string.dark_shadow_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAppListBinding.inflate(inflater, container, false);
        binding.recyclerView.addItemDecoration(new OplusRecyclerView.OplusRecyclerViewItemDecoration(requireContext()));

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        binding.progress.setVisibility(View.GONE);
        binding.searchViewLayout.setVisibility(View.GONE);

        binding.appFunctionSwitch.setTitle(getString(R.string.dark_shadow_enable_theme));
        binding.appFunctionSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                enableShadowTheme();
            } else {
                disableShadowTheme();
            }
        });
        binding.appFunctionSwitch.setSwitchChecked(
                OverlayUtil.isOverlayDisabled("OxygenCustomizerComponent" + overlays[0] + Build.VERSION.SDK_INT + ".overlay")
        );

        // RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(initDarkShadowColors());
        binding.recyclerView.setHasFixedSize(true);

        return binding.getRoot();
    }

    private final DarkShadowColorsAdapter.OnUserAction mListener = new DarkShadowColorsAdapter.OnUserAction() {
        @Override
        public void onColorChanged(DarkShadowItem darkShadowItem) {

        }

        @Override
        public void onEnabledClicked(DarkShadowItem darkShadowItem) {
            loadingDialog.show(getString(R.string.loading_dialog_wait));
            Log.w("DarkShadowThemeFragment", "onEnabledClicked: " + darkShadowItem.toString());
            DarkShadowUtils.saveColor(darkShadowItem);
            enableShadowTheme();
            int i = 0;
            for (String resName : darkShadowItem.getResourceNames()) {
                FabricatedUtil
                        .buildAndEnableOverlay(
                                darkShadowItem.getPackages().get(0),
                                darkShadowItem.getOverlayName() + "_" + i,
                                "color",
                                resName,
                                String.format("0x%08X", (0xFFFFFFFF & darkShadowItem.getColor()))
                        );
                i++;
            }
            int j = i;
            if (!darkShadowItem.getAdjustColors().isEmpty()) {
                for (String resName : darkShadowItem.getAdjustColors().keySet()) {
                    FabricatedUtil
                            .buildAndEnableOverlay(
                                    darkShadowItem.getPackages().get(0),
                                    darkShadowItem.getOverlayName() + "_" + j,
                                    "color",
                                    resName,
                                    String.format("0x%08X", (0xFFFFFFFF & ColorUtils.adjustColor(darkShadowItem.getColor(), darkShadowItem.getAdjustColors().get(resName))))
                            );
                    j++;
                }
            }
            loadingDialog.dismiss();
        }

        @Override
        public void onDisabledClicked(DarkShadowItem darkShadowItem) {
            loadingDialog.show(getString(R.string.loading_dialog_wait));
            Log.w("DarkShadowThemeFragment", "onDisabledClicked: " + darkShadowItem.toString());
            int i = 0;
            for (String resName : darkShadowItem.getResourceNames()) {
                FabricatedUtil
                        .disableOverlay(
                                darkShadowItem.getOverlayName() + "_" + i
                        );
                i++;
            }
            int j = i;
            if (!darkShadowItem.getAdjustColors().isEmpty()) {
                for (String resName : darkShadowItem.getAdjustColors().keySet()) {
                    FabricatedUtil
                            .disableOverlay(
                                    darkShadowItem.getOverlayName() + "_" + j
                            );
                    j++;
                }
            }
            loadingDialog.dismiss();
        }
    };

    private void enableShadowTheme() {
        loadingDialog.show(getString(R.string.loading_dialog_wait));
        for (String overlay : overlays) {
            OverlayUtil.enableOverlay("OxygenCustomizerComponent" + overlay + Build.VERSION.SDK_INT + ".overlay");
        }
        loadingDialog.dismiss();
    }

    private void disableShadowTheme() {
        loadingDialog.show(getString(R.string.loading_dialog_wait));
        for (String overlay : overlays) {
            OverlayUtil.disableOverlay("OxygenCustomizerComponent" + overlay + Build.VERSION.SDK_INT + ".overlay");
        }
        loadingDialog.dismiss();
    }

    private RecyclerView.Adapter<RecyclerView.ViewHolder> initDarkShadowColors() {

        SectionTitleAdapter titleAdapter = new SectionTitleAdapter(getString(R.string.colors));
        DarkShadowColorsAdapter colors = new DarkShadowColorsAdapter((MainActivity) requireActivity(), mDarkShadowColors, mListener);
        FooterWidgetAdapter footerAdapter = new FooterWidgetAdapter(getString(R.string.dark_shadow_footer), v -> openDarkShadow());

        return new ConcatAdapter(titleAdapter, colors, footerAdapter);
    }

//    private void setupPrefs() {
//        FooterPreference darkShadowFooter = findPreference("dark_shadow_footer");
//        darkShadowFooter.setLearnMoreAction(v -> openDarkShadow());
//
//        OplusPreferenceCategory mColorsCategory = findPreference("colors_category");
//    }

    private void openDarkShadow() {
        requireActivity().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://mythemedarkandmore.altervista.org/")));
    }

}
