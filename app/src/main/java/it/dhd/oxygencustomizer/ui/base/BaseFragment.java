package it.dhd.oxygencustomizer.ui.base;

import static it.dhd.oxygencustomizer.ui.base.BaseActivity.setHeader;

import androidx.fragment.app.Fragment;

import it.dhd.oxygencustomizer.ui.activity.MainActivity;

public abstract class BaseFragment extends Fragment {

    public abstract String getTitle();

    public abstract boolean backButtonEnabled();

    @Override
    public void onResume() {
        super.onResume();
        if (getContext() != null) {
            setHeader(getContext(), getTitle());
            ((MainActivity) getContext()).getSupportActionBar().setDisplayHomeAsUpEnabled(backButtonEnabled());
        }
    }

}
