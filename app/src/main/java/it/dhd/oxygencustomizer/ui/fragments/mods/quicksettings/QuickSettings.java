package it.dhd.oxygencustomizer.ui.fragments.mods.quicksettings;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.BLUR_RADIUS_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_BLUR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_SWITCH;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class QuickSettings extends ControlledPreferenceFragmentCompat {

    private FrameLayout pullDownIndicator;

    @Override
    public String getTitle() {
        return getString(R.string.quick_settings_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.quick_settings_mods;
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

    @SuppressLint("RtlHardcoded")
    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        try {
            int displayWidth = requireActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width();
            pullDownIndicator.setVisibility(PreferenceHelper.isVisible("quick_pulldown_length") ? View.VISIBLE : View.GONE);
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) pullDownIndicator.getLayoutParams();
            lp.width = Math.round(mPreferences.getSliderInt( "quick_pulldown_length", 25) * displayWidth / 100f);
            lp.gravity = Gravity.TOP | (Integer.parseInt(mPreferences.getString("quick_pulldown_side", "1")) == 1 ? Gravity.RIGHT : Gravity.LEFT);
            pullDownIndicator.setLayoutParams(lp);
        } catch (Exception ignored) {}
        if (key != null && (key.equals(QS_TRANSPARENCY_SWITCH) ||
                key.equals(QSPANEL_BLUR_SWITCH) ||
                key.equals(BLUR_RADIUS_VALUE) ||
                key.equals("qs_show_my_device"))) {
            AppUtils.restartAllScope(new String[]{SYSTEM_UI});
        }
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

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{SYSTEM_UI};
    }

}
