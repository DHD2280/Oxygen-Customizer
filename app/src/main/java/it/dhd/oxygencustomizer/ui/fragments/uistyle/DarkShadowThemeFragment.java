package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.ACCENT;
import static it.dhd.oxygencustomizer.utils.DarkShadowUtils.BACKGROUND;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OplusRecyclerView;
import androidx.recyclerview.widget.RecyclerView;

import java.sql.Array;
import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.ui.adapters.DarkShadowColorsAdapter;
import it.dhd.oxygencustomizer.ui.adapters.FooterWidgetAdapter;
import it.dhd.oxygencustomizer.ui.adapters.SectionTitleAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.DarkShadowItem;
import it.dhd.oxygencustomizer.utils.overlay.FabricatedUtil;

public class DarkShadowThemeFragment extends BaseFragment {

    private List<DarkShadowItem> mDarkShadowColors = new ArrayList<>() {{
        add(ACCENT);
        add(BACKGROUND);
    }};

    private FragmentRecyclerBinding binding;
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
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        binding.recyclerViewFragment.addItemDecoration(new OplusRecyclerView.OplusRecyclerViewItemDecoration(requireContext()));

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initDarkShadowColors());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return binding.getRoot();
    }

    private final DarkShadowColorsAdapter.OnUserAction mListener = new DarkShadowColorsAdapter.OnUserAction() {
        @Override
        public void onColorChanged(DarkShadowItem darkShadowItem) {

        }

        @Override
        public void onEnabledClicked(DarkShadowItem darkShadowItem) {
            Log.w("DarkShadowThemeFragment", "onEnabledClicked: " + darkShadowItem.toString());
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
        }

        @Override
        public void onDisabledClicked(DarkShadowItem darkShadowItem) {
            Log.w("DarkShadowThemeFragment", "onDisabledClicked: " + darkShadowItem.toString());
            int i = 0;
            for (String resName : darkShadowItem.getResourceNames()) {
                FabricatedUtil
                        .disableOverlay(
                                darkShadowItem.getOverlayName() + "_" + i
                        );
                i++;
            }
        }
    };

    private RecyclerView.Adapter<RecyclerView.ViewHolder> initDarkShadowColors() {

        SectionTitleAdapter titleAdapter = new SectionTitleAdapter("Colors");
        DarkShadowColorsAdapter colors = new DarkShadowColorsAdapter((MainActivity) requireActivity(), mDarkShadowColors, mListener);
        FooterWidgetAdapter footerAdapter = new FooterWidgetAdapter("Dark Shadow", v -> openDarkShadow());

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
