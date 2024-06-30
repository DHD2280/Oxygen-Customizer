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

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.IconsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.IconModel;

public class WifiIcons extends BaseFragment {

    private FragmentRecyclerBinding binding;
    private LoadingDialog loadingDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        View view = binding.getRoot();

        // Loading dialog while enabling or disabling pack
        loadingDialog = new LoadingDialog(requireContext());

        // RecyclerView
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewFragment.setAdapter(initWifiIconsItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private IconsAdapter initWifiIconsItems() {
        ArrayList<IconModel> wifiIcons = new ArrayList<>();
        List<String> packs = getOverlayForComponent("WIFI");
        for (int i = 0; i< packs.size(); i++) {
            String pkgName = packs.get(i).split("]")[1].replaceAll(" ", "");
            wifiIcons.add(
                    new IconModel(
                            getStringFromOverlay(getContext(), pkgName, "theme_name"),
                            pkgName,
                            getDrawableFromOverlay(getContext(), pkgName, "stat_signal_wifi_signal_1"),
                            getDrawableFromOverlay(getContext(), pkgName, "stat_signal_wifi_signal_2"),
                            getDrawableFromOverlay(getContext(), pkgName, "stat_signal_wifi_signal_3"),
                            getDrawableFromOverlay(getContext(), pkgName, "stat_signal_wifi_signal_4"),
                            packs.get(i).contains("[x]")));
        }
        wifiIcons.sort(Comparator.comparing(IconModel::getName));
        return new IconsAdapter(requireContext(), wifiIcons, loadingDialog, "WIFI", "COMMONWIFI", true);
    }

    @Override
    public void onDestroy() {
        if (loadingDialog != null) {
            loadingDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    public String getTitle() {
        return getString(R.string.theme_customization_wifi_icon_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}