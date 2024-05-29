package it.dhd.oxygencustomizer.ui.fragments.uistyle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentRecyclerBinding;
import it.dhd.oxygencustomizer.ui.adapters.NavbarAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;
import it.dhd.oxygencustomizer.ui.dialogs.LoadingDialog;

public class NavbarStyle extends BaseFragment {

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
        binding.recyclerViewFragment.setAdapter(initNavbarItems());
        binding.recyclerViewFragment.setHasFixedSize(true);

        return view;
    }

    private NavbarAdapter initNavbarItems() {
        ArrayList<String> navbar_list = new ArrayList<>() {
            {
                add("Android");
                add("Asus");
                add("Dora");
                add("Moto");
                add("Nexus");
                add("Old");
                add("One UI");
                add("Sammy");
                add("Tecno");
            }
        };

        return new NavbarAdapter(requireContext(), navbar_list, loadingDialog);
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
        return getString(R.string.theme_customization_navbar_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }
}
