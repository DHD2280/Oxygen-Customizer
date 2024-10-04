package it.dhd.oxygencustomizer.ui.fragments;

import static it.dhd.oxygencustomizer.ui.adapters.CreditsAdapter.VIEW_TYPE_ITEM;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.CreditsAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.models.CreditsModel;

public class Credits extends BaseFragment {

    private FragmentRecyclerBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecyclerBinding.inflate(inflater, container, false);
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
                                     
        credits.add(new CreditsModel("Testers"));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Max", "", "", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Siri00", "", "", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Pasqui1978", "", "", R.drawable.ic_default_person));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "ZioProne", "", "https://github.com/Pronegate", ResourcesCompat.getDrawable(getResources(), R.drawable.zioprone, requireContext().getTheme())));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "ℤ\uD835\uDD56\uD835\uDD5F\uD835\uDD60 \uD835\uDD4F", "OOS 13 Tester", "", R.drawable.ic_default_person));

        credits.add(new CreditsModel("Translators"));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Osean22", "Russian", "https://t.me/Osean22", R.drawable.flag_ru));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "ANTI SEMPAI", "Russian", "https://crowdin.com/profile/senpai4ek", R.drawable.flag_ru));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Kirrillak", "Russian", "https://crowdin.com/profile/Kirrillak", R.drawable.flag_ru));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Дилшод Исматов", "Russian", "https://crowdin.com/profile/dilshod199714", R.drawable.flag_ru));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Andfi", "Russian", "https://crowdin.com/profile/andfi", R.drawable.flag_ru));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Pasqui1978", "Italian", "https://crowdin.com/profile/pasqui1978", R.drawable.flag_it));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "tugaia56", "Italian", "https://crowdin.com/profile/tugaia56", R.drawable.flag_it));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "𝗦𝗵𝗟𝗲𝗿𝗣", "Turkish", "https://crowdin.com/profile/mikropsoft", R.drawable.flag_tr));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "\uD835\uDDE6\uD835\uDDF5\uD835\uDDDF\uD835\uDDF2\uD835\uDDFF\uD835\uDDE3", "Turkish", "https://crowdin.com/profile/mikropsoft", R.drawable.flag_tr));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "凌天", "Chinese Simplified", "https://crowdin.com/profile/lingtian", R.drawable.flag_cn));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "芜蚌湖埠", "Chinese Simplified", "https://crowdin.com/profile/11451420", R.drawable.flag_cn));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "-拂暁-", "Chinese Simplified", "https://crowdin.com/profile/Neko-Madoka", R.drawable.flag_cn));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Lorie Eckerson", "Chinese Simplified", "https://crowdin.com/profile/lorieeckersonbq2284", R.drawable.flag_cn));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "haosiang0331", "Chinese Traditional", "https://crowdin.com/profile/haosiang0331", R.drawable.flag_cn));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Re*Index.", "Japanese", "https://crowdin.com/profile/ot_inc", R.drawable.flag_jp));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Serge Croise ", "French", "https://crowdin.com/profile/serge.croise", R.drawable.flag_fr));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Serge Croise ", "Spanish", "https://crowdin.com/profile/serge.croise", R.drawable.flag_es));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "AlejandroMoc", "Spanish", "https://crowdin.com/profile/AlejandroMoc", R.drawable.flag_es));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Jean Rivera", "Spanish", "https://crowdin.com/profile/jeanrivera ", R.drawable.flag_es));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "MKAdam", "Hungarian", "https://crowdin.com/profile/If.you.know.better.than.me.do.it.If.not.shut.up", R.drawable.flag_hu));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Czak", "Polish", "https://crowdin.com/profile/Czak", R.drawable.flag_pl));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Wiktor Gajewicz", "Polish", "https://crowdin.com/profile/wgajuraj", R.drawable.flag_pl));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Ahmed Hamada", "Arabic", "https://crowdin.com/profile/a7medhamada76", R.drawable.flag_sa));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Muhammad Bahaa", "Arabic", "https://crowdin.com/profile/muhammadbahaa2001", R.drawable.flag_sa));
        credits.add(new CreditsModel(VIEW_TYPE_ITEM, "Marvin Grasberger", "German", "https://crowdin.com/profile/marvingrasberger14", R.drawable.flag_de));

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
