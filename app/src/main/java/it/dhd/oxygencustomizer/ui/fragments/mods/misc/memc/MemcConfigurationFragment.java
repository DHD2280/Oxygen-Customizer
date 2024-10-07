package it.dhd.oxygencustomizer.ui.fragments.mods.misc.memc;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayoutMediator;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.FragmentMemcBinding;
import it.dhd.oxygencustomizer.ui.adapters.MemcCollectionAdapter;
import it.dhd.oxygencustomizer.ui.base.BaseFragment;

public class MemcConfigurationFragment extends BaseFragment {

    private FragmentMemcBinding binding;
    private MemcCollectionAdapter mMemcCollectionAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMemcBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        MenuHost menuHost = requireActivity();
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                // Add menu items here
                menu.add(0, 1, 0, "Info")
                        .setIcon(R.drawable.settingslib_ic_info_outline_24)
                        .setIconTintList(ColorStateList.valueOf(ContextCompat.getColor(requireContext(), R.color.textColorPrimary)))
                        .setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                // Handle the menu selection
                if (menuItem.getItemId() == 1) {
                    MemcInfoFragment memcInfoFragment = new MemcInfoFragment();
                    memcInfoFragment.show(getChildFragmentManager(), "memc_info");
                    return true;
                }
                return true;
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        mMemcCollectionAdapter = new MemcCollectionAdapter(this);
        binding.pager.setAdapter(mMemcCollectionAdapter);
        new TabLayoutMediator(binding.tabLayout, binding.pager,
                (tab, position) -> tab.setText(mMemcCollectionAdapter.getTitle(position))
        ).attach();
        binding.addButton.setOnClickListener(v -> addItem());
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setFabText(position);
            }
        });
    }

    private void addItem() {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof MemcApplicationsFragment memcApplicationsFragment) {
            memcApplicationsFragment.addItem();
        } else if (currentFragment instanceof MemcActivitiesFragment memcActivitiesFragment) {
            memcActivitiesFragment.addItem();
        }
    }

    private Fragment getCurrentFragment() {
        int currentItem = binding.pager.getCurrentItem();
        String fragmentTag = "f" + currentItem;
        return getChildFragmentManager().findFragmentByTag(fragmentTag);
    }

    @Override
    public String getTitle() {
        return getString(R.string.custom_memc_configuration_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    private void setFabText(int position) {
        if (position == 0) {
            binding.addButton.setText(R.string.add_app);
        } else {
            binding.addButton.setText(R.string.add_activity);
        }
    }

}
