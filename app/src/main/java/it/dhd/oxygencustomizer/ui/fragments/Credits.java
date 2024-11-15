package it.dhd.oxygencustomizer.ui.fragments;

import static it.dhd.oxygencustomizer.ui.adapters.CreditsAdapter.VIEW_TYPE_ITEM;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OplusRecyclerView;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.CreditsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.models.CreditsModel;
import it.dhd.oxygencustomizer.utils.ContributorParser;
import it.dhd.oxygencustomizer.utils.TranslatorParser;

public class Credits extends BaseFragment {

    private FragmentRecyclerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
        binding.recyclerViewFragment.addItemDecoration(new OplusRecyclerView.OplusRecyclerViewItemDecoration(requireContext()));
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<CreditsModel> credits = new ArrayList<>();
        credits.add(new CreditsModel("Special Thanks"));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Siavash", """
                For helping with Xposed
                And his amazing work with PixelXpert
                github/Siavash79""", "https://github.com/siavash79", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "DrDisagree", """
                For his amazing work with Iconify
                github/Mahmud0808""", "https://github.com/Mahmud0808", ResourcesCompat.getDrawable(getResources(), R.drawable.drdisagree, requireContext().getTheme())));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "crDroid", """
        Pulse Controller, check source for more info
        github/crdroidandroid""", "https://github.com/crdroidandroid", R.drawable.ic_crdroid));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "OmniROM", """
        For Weather
        github/OmniROM""", "https://github.com/OmniROM", R.drawable.ic_omnirom));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Project Matrixx", """
        For some Illustrations
        github/ProjectMatrixx""", "https://github.com/ProjectMatrixx", R.drawable.ic_matrixx_logo));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Superior Extended", """
        For some customizations
        github/SuperiorExtended""", "https://github.com/SuperiorExtended/", R.drawable.ic_superior));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "ℙ\uD835\uDD52\uD835\uDD5F\uD835\uDD43","PUI Theme",
                "https://t.me/PUINewsroom",
                ResourcesCompat.getDrawable(getResources(), R.drawable.panl, requireContext().getTheme())));

        credits.add(new CreditsModel("Contributors"));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "tugaia56", "OOS Themer", "https://t.me/OnePlus_Mods_Theme", R.drawable.tugaia));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "thecubed", """
        Recognized Developer
        For finding a fix to lag issue from recents""", "https://github.com/thecubed", R.drawable.ic_default_person));
        try {
            credits.addAll(new ContributorParser().parseContributors());
        } catch (JSONException e) {
            Log.e("Credits", "Error parsing contributors", e);
        }

        credits.add(new CreditsModel("Testers"));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Max", "", "", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Siri00", "", "", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Pasqui1978", "", "", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "ZioProne", "", "https://github.com/Pronegate", ResourcesCompat.getDrawable(getResources(), R.drawable.zioprone, requireContext().getTheme())));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "ℤ\uD835\uDD56\uD835\uDD5F\uD835\uDD60 \uD835\uDD4F", "OOS 13 Tester", "", R.drawable.ic_default_person));

        credits.add(new CreditsModel("Translators"));
        try {
            credits.addAll(new TranslatorParser().parseContributors());
        } catch (Exception e) {
            Log.e("Credits", "Error parsing translators", e);
        }

        CreditsAdapter adapter = new CreditsAdapter(credits);
        binding.recyclerViewFragment.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewFragment.setAdapter(adapter);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public String getTitle() {
        return getString(R.string.credits_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}
