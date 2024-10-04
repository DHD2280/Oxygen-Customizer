package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.checkOverlayEnabledAndEnable;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.disableOverlay;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getDrawableFromOverlay;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getOverlayForComponent;
import static it.dhd.oxygencustomizer.utils.overlay.OverlayUtil.getStringFromOverlay;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import it.dhd.oxygencustomizer.OxygenCustomizer;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.IconsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.IconModel;
import it.dhd.oxygencustomizer.utils.Prefs;
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class IconPacks extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;
    private List<String> packs;
    private ArrayList<IconModel> iconPacks;

    @Override
    public String getTitle() {
        return getString(R.string.theme_customization_icon_pack_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        packs = getOverlayForComponent("IPSUI");
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initIconPackItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private IconsAdapter initIconPackItems() {
        iconPacks = new ArrayList<>();
        for (int i = 0; i< packs.size(); i++) {
            String pkgName = packs.get(i).split("]")[1].replaceAll(" ", "");
            iconPacks.add(
                    new IconModel(
                            getStringFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "theme_name"),
                            pkgName,
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_signal_lte_single_3"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_wifi_signal_3"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_sys_data_bluetooth"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_sys_airplane_mode"),
                            packs.get(i).contains("[x]")));
        }

        iconPacks.sort(Comparator.comparing(IconModel::getName));

        return new IconsAdapter(requireContext(), iconPacks, loadingDialog, "IPSUI", onButtonClick, true);
    }

    private final IconsAdapter.OnButtonClick onButtonClick = new IconsAdapter.OnButtonClick() {
        @Override
        public void onEnableClick(int position, IconModel item) {
            disableAllIcons(position);
            OverlayUtil.enableOverlayExclusiveInCategory(item.getPackageName());
            checkOverlayEnabledAndEnable("SGIC");
            checkOverlayEnabledAndEnable("WIFI");
        }

        @Override
        public void onDisableClick(int position, IconModel item) {
            Prefs.putBoolean(iconPacks.get(position).getPackageName(), false);
            OverlayUtil.disableOverlay(iconPacks.get(position).getPackageName());
            iconPacks.get(position).setEnabled(false);
        }
    };

    private void disableAllIcons(int position) {
        for (int i=0; i<iconPacks.size(); i++) {
            Prefs.putBoolean(iconPacks.get(i).getPackageName(), i == position);
            iconPacks.get(i).setEnabled(i == position);
            disableOverlay(iconPacks.get(i).getPackageName());
        }
    }

}
