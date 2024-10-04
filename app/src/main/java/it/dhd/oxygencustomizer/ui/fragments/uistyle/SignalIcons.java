package it.dhd.oxygencustomizer.ui.fragments.uistyle;

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
import it.dhd.oxygencustomizer.utils.overlay.OverlayUtil;

public class SignalIcons extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;
    private ArrayList<IconModel> signalIcons;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initSignalIconsItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private IconsAdapter initSignalIconsItems() {
        signalIcons = new ArrayList<>();
        List<String> pack = getOverlayForComponent("SGIC");
        for (int i = 0; i< pack.size(); i++) {
            String pkgName = pack.get(i).split("]")[1].replaceAll(" ", "");
            signalIcons.add(
                    new IconModel(
                            getStringFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "theme_name"),
                            pkgName,
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_signal_lte_single_0"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_signal_lte_single_1"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_signal_lte_single_2"),
                            getDrawableFromOverlay(OxygenCustomizer.getAppContext(), pkgName, "stat_signal_signal_lte_single_3"),
                            pack.get(i).contains("[x]")));
        }
        signalIcons.sort(Comparator.comparing(IconModel::getName));
        return new IconsAdapter(requireContext(), signalIcons, loadingDialog, "SGIC", onButtonClick, true);
    }

    private final IconsAdapter.OnButtonClick onButtonClick = new IconsAdapter.OnButtonClick() {
        @Override
        public void onEnableClick(int position, IconModel item) {
            for (int i = 0; i <= signalIcons.size()-1; i++) {
                signalIcons.get(i).setEnabled(i == position);
                OverlayUtil.disableOverlay(signalIcons.get(i).getPackageName());
            }
            OverlayUtil.enableOverlay("OxygenCustomizerComponentCOMMONSGIC.overlay");
            OverlayUtil.enableOverlay(item.getPackageName());
        }

        @Override
        public void onDisableClick(int position, IconModel item) {
            item.setEnabled(false);
            OverlayUtil.disableOverlay("OxygenCustomizerComponentCOMMONSGIC.overlay");
            OverlayUtil.disableOverlay(item.getPackageName());
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
        return getString(R.string.theme_customization_signal_icon_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

}