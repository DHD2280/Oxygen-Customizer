package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.memc.MemcActivitiesFragment;
import it.dhd.oxygencustomizer.ui.fragments.mods.misc.memc.MemcApplicationsFragment;


public class MemcCollectionAdapter extends FragmentStateAdapter {

    public MemcCollectionAdapter(Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;
        if (position == 0) {
            fragment = new MemcApplicationsFragment();
        } else {
            fragment = new MemcActivitiesFragment();
        }
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public String getTitle(int position) {
        if (position == 0) {
            return getAppContext().getString(R.string.applications);
        } else {
            return getAppContext().getString(R.string.activities);
        }
    }

}
