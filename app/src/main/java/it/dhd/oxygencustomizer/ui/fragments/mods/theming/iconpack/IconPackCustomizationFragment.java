package it.dhd.oxygencustomizer.ui.fragments.mods.theming.iconpack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OplusRecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.databinding.FragmentIconPackCustomizationBinding;
import it.dhd.oxygencustomizer.ui.adapters.iconpack.IconPackCustomizationAdapter;
import it.dhd.oxygencustomizer.ui.adapters.iconpack.ItemChangedListener;
import it.dhd.oxygencustomizer.xposed.utils.IconPackUtil;

public class IconPackCustomizationFragment extends Fragment implements IconPackUtil.IconPackQueryListener {

    private FragmentIconPackCustomizationBinding binding;
    private IconPackUtil mIconPackUtil;
    private IconPackCustomizationAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentIconPackCustomizationBinding.inflate(inflater, container, false);

        mIconPackUtil = IconPackUtil.getInstance(requireContext());
        mIconPackUtil.addListener(this);
        mIconPackUtil.queryIconPacks(false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerView.addItemDecoration(new OplusRecyclerView.OplusRecyclerViewItemDecoration(requireContext()));
    }

    @Override
    public void onIconPacksLoaded(IconPackUtil.ResourceMapping mapping, IconPackUtil.IconPackMapping packMapping) {
        List<IconPackUtil.IconPack> iconPacks = new ArrayList<>(packMapping.keySet());
        iconPacks.sort((o1, o2) -> {
            int nameComparison = o1.mName.compareTo(o2.mName);
            if (nameComparison == 0) {
                return o1.mPackageName.compareTo(o2.mPackageName);
            }
            return nameComparison;
        });
        new Handler(Looper.getMainLooper()).post(this::requestFabVisibility);
        mAdapter = new IconPackCustomizationAdapter(mIconPackUtil, mapping, mItemChangedListener);
        binding.recyclerView.post(() -> binding.recyclerView.setAdapter(mAdapter));
    }

    private final ItemChangedListener mItemChangedListener = () -> new Handler(Looper.getMainLooper()).post(this::requestFabVisibility);

    public boolean isFabVisible() {
        return mIconPackUtil.isAnythingEnabled();
    }

    private void requestFabVisibility() {
        if (getParentFragment() instanceof IconPackFragment iconPackFragment) {
            iconPackFragment.requestFabVisibility();
        }
    }

    public void query(String mSearchQuery) {
        if (mAdapter == null) return;
        mAdapter.filter(mSearchQuery);
    }

}