package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import static it.dhd.oxygencustomizer.utils.Dynamic.TOTAL_ANDROID_THEMES;
import static it.dhd.oxygencustomizer.utils.PreferenceHelper.getModulePrefs;
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
import it.dhd.oxygencustomizer.ui.adapters.ThemeAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;
import it.dhd.oxygencustomizer.ui.models.ThemeModel;

public class UiStyle extends BaseFragment {

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
        binding.recyclerViewFragment.setAdapter(initThemesItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private ThemeAdapter initThemesItems() {
        ArrayList<ThemeModel> mThemeNames = new ArrayList<>();
        List<String> pack = getOverlayForComponent("TH");
        for (int i = 0; i< pack.size(); i++) {
            String themeName = pack.get(i).split("]")[1].replaceAll(" ", "");
            mThemeNames.add(new ThemeModel(themeName,
                    getStringFromOverlay(
                            requireContext(),
                            themeName,
                            "android_theme_name"),
                    pack.get(i).contains("[x]")
                    ));
        }
        if (getModulePrefs() != null) {
            getModulePrefs().edit().putInt("UiStylesThemes", TOTAL_ANDROID_THEMES).apply();
        }
        mThemeNames.sort(Comparator.comparing(ThemeModel::getThemeName));
        return new ThemeAdapter(requireContext(), mThemeNames, loadingDialog);
    }

    @Override
    public String getTitle() {
        return getString(R.string.theme_customization_ui_style_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

}


