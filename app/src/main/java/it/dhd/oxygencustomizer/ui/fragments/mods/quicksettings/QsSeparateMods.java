package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_WIDTH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class QsSeparateMods extends ControlledPreferenceFragmentCompat {

    private FrameLayout pullDownIndicator;

    @Override
    public String getTitle() {
        return getString(R.string.qs_separate_mods);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.qs_separate_mods;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SYSTEM_UI};
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);
        createPullDownIndicator();
    }

    @Override
    public void onDestroy() {
        ((ViewGroup) pullDownIndicator.getParent()).removeView(pullDownIndicator);
        super.onDestroy();
    }

    @Override
    public void onPause() {
        super.onPause();
        pullDownIndicator.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (pullDownIndicator != null) {
            pullDownIndicator.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        try {
            int displayWidth = requireActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width();
            pullDownIndicator.setVisibility(PreferenceHelper.isVisible(SEPARATE_QS_WIDTH) ? View.VISIBLE : View.GONE);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) pullDownIndicator.getLayoutParams();
            lp.width = Math.round(OCPreferences.getSliderInt( SEPARATE_QS_WIDTH, 50) * displayWidth / 100f);
            lp.gravity = Gravity.TOP | Gravity.END;
            pullDownIndicator.setLayoutParams(lp);
        } catch (Exception ignored) {}
    }

    private void createPullDownIndicator() {
        pullDownIndicator = new FrameLayout(requireContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, 25);
        lp.gravity = Gravity.TOP;

        pullDownIndicator.setLayoutParams(lp);
        pullDownIndicator.setBackgroundColor(requireContext().getColor(android.R.color.system_accent1_200));
        pullDownIndicator.setAlpha(.7f);
        pullDownIndicator.setVisibility(View.VISIBLE);

        ((ViewGroup) requireActivity().getWindow().getDecorView().getRootView()).addView(pullDownIndicator);
    }

}
