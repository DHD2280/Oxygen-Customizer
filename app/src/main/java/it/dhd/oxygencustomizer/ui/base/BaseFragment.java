package it.dhd.oxygencustomizer.ui.base;

import static it.dhd.oxygencustomizer.ui.base.BaseActivity.setHeader;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.Fragment;

import it.dhd.oxygencustomizer.ui.activity.MainActivity;
import it.dhd.oxygencustomizer.utils.LocaleHelper;

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

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(LocaleHelper.setLocale(context));

        if (getActivity() != null) {
            var window = requireActivity().getWindow();
            WindowCompat.setDecorFitsSystemWindows(window, false);
        }
    }

}
