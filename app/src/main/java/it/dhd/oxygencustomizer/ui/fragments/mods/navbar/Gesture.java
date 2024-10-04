package it.dhd.oxygencustomizer.ui.fragments.mods.navbar;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.ui.preferences.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class Gesture extends ControlledPreferenceFragmentCompat {

    FrameLayout leftBackGestureIndicator, rightBackGestureIndicator;
    FrameLayout leftSwipeGestureIndicator, rightSwipeGestureIndicator;
    private ListWithPopUpPreference mOverrideBackLeft, mOverrideBackRight;
    int navigationBarHeight = 0;

    @Override
    public String getTitle() {
        return getString(R.string.gesture_navigation_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.gesture_prefs;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.SYSTEM_UI};
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        int[] backDrawables = new int[]{
                R.drawable.ic_switch_app, // Switch App
                R.drawable.ic_kill, // Kill App
                R.drawable.ic_screenshot, // Screenshot
                R.drawable.ic_quick_settings, // Quick Settings
                R.drawable.ic_power_menu, // Power Menu
                R.drawable.ic_notifications, // Notification Panel
                R.drawable.ic_screen_off, // Volume Panel
        };

        mOverrideBackLeft = findPreference("gesture_override_holdback_left");
        if (mOverrideBackLeft != null)
            mOverrideBackLeft.setDrawables(backDrawables);

        mOverrideBackRight = findPreference("gesture_override_holdback_right");
        if (mOverrideBackRight != null)
            mOverrideBackRight.setDrawables(backDrawables);


        rightBackGestureIndicator = prepareBackGestureView(Gravity.RIGHT);
        leftBackGestureIndicator = prepareBackGestureView(Gravity.LEFT);

        rightSwipeGestureIndicator = prepareSwipeGestureView(Gravity.RIGHT);
        leftSwipeGestureIndicator = prepareSwipeGestureView(Gravity.LEFT);
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        try {

            mOverrideBackLeft.setTitle(
                    mPreferences.getString("gesture_override_holdback_mode", "0").equals("0") ?
                            R.string.gesture_override_back_hold_common : R.string.gesture_override_back_hold_left
            );

            int displayHeight = requireActivity().getWindowManager().getCurrentWindowMetrics().getBounds().height();
            int displayWidth = requireActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width();

            float leftSwipeUpPercentage = mPreferences.getSliderFloat("gesture_left_height_double", 25);

            float rightSwipeUpPercentage = mPreferences.getSliderFloat("gesture_right_height_double", 25);

            int edgeWidth = Math.round(displayWidth * leftSwipeUpPercentage / 100f);
            ViewGroup.LayoutParams lp = leftSwipeGestureIndicator.getLayoutParams();
            lp.width = edgeWidth;
            leftSwipeGestureIndicator.setLayoutParams(lp);

            edgeWidth = Math.round(displayWidth * rightSwipeUpPercentage / 100f);
            lp = rightSwipeGestureIndicator.getLayoutParams();
            lp.width = edgeWidth;
            rightSwipeGestureIndicator.setLayoutParams(lp);

            setVisibility(rightSwipeGestureIndicator, false, 400);
            setVisibility(leftSwipeGestureIndicator, false, 400);

            setVisibility(rightBackGestureIndicator, PreferenceHelper.isVisible("gesture_right_height_double"), 400);
            setVisibility(leftBackGestureIndicator, PreferenceHelper.isVisible("gesture_left_height_double"), 400);

            List<Float> prefs = mPreferences.getSliderValues("gesture_right_height_double", 100f);
            int bottomMargin, topMargin;
            if (prefs.size() == 2) {
                bottomMargin = Math.round(displayHeight * prefs.get(0) / 100f);
                topMargin = Math.round(displayHeight - displayHeight * prefs.get(1) / 100f);
            } else {
                bottomMargin = 0;
                topMargin = Math.round(displayHeight * prefs.get(0) / 100f);
            }
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(50, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            layoutParams.topMargin = topMargin;
            layoutParams.bottomMargin = bottomMargin;
            rightBackGestureIndicator.setLayoutParams(layoutParams);

            prefs = mPreferences.getSliderValues("gesture_left_height_double", 100f);
            if (prefs.size() == 2) {
                bottomMargin = Math.round(displayHeight * prefs.get(0) / 100f);
                topMargin = Math.round(displayHeight - displayHeight * prefs.get(1) / 100f);
            } else {
                bottomMargin = 0;
                topMargin = Math.round(displayHeight * prefs.get(0) / 100f);
            }
            layoutParams = new FrameLayout.LayoutParams(50, ViewGroup.LayoutParams.MATCH_PARENT);
            layoutParams.gravity = Gravity.LEFT | Gravity.BOTTOM;
            layoutParams.topMargin = topMargin;
            layoutParams.bottomMargin = bottomMargin;
            leftBackGestureIndicator.setLayoutParams(layoutParams);

        } catch (Exception ignored) {
        }
    }

    private FrameLayout prepareSwipeGestureView(int gravity) {
        @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = requireContext().getResources().getDimensionPixelSize(resourceId);
        }

        FrameLayout result = new FrameLayout(requireContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, navigationBarHeight);
        lp.gravity = gravity | Gravity.CENTER_VERTICAL;
        lp.bottomMargin = 0;
        result.setLayoutParams(lp);

        result.setBackgroundColor(requireContext().getColor(android.R.color.system_accent1_300));
        result.setAlpha(.7f);
        ((ViewGroup) requireActivity().getWindow().getDecorView().getRootView()).addView(result);
        result.setVisibility(View.GONE);
        return result;
    }

    private FrameLayout prepareBackGestureView(int gravity) {
        int navigationBarHeight = 0;
        @SuppressLint({"InternalInsetResource", "DiscouragedApi"})
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = requireContext().getResources().getDimensionPixelSize(resourceId);
        }

        FrameLayout result = new FrameLayout(requireContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(50, 0);
        lp.gravity = gravity | Gravity.BOTTOM;
        lp.bottomMargin = navigationBarHeight;
        result.setLayoutParams(lp);

        result.setBackgroundColor(requireContext().getColor(android.R.color.system_accent1_300));
        result.setAlpha(.7f);
        ((ViewGroup) requireActivity().getWindow().getDecorView().getRootView()).addView(result);
        result.setVisibility(View.GONE);
        return result;
    }

    @SuppressWarnings("SameParameterValue")
    private void setVisibility(View v, boolean visible, long duration) {
        if ((v.getVisibility() == View.VISIBLE) == visible) return;

        float basicAlpha = v.getAlpha();
        float destAlpha = (visible) ? 1f : 0f;

        if (visible) v.setAlpha(0f);
        v.setVisibility(View.VISIBLE);

        v.animate().setDuration(duration).alpha(destAlpha).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                if (!visible) v.setVisibility(View.GONE);
                v.setAlpha(basicAlpha);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animator) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {
            }
        }).start();
    }

    @Override
    public void onDestroy() {
        ((ViewGroup) rightBackGestureIndicator.getParent()).removeView(rightBackGestureIndicator);
        ((ViewGroup) leftBackGestureIndicator.getParent()).removeView(leftBackGestureIndicator);

        ((ViewGroup) rightSwipeGestureIndicator.getParent()).removeView(rightSwipeGestureIndicator);
        ((ViewGroup) leftSwipeGestureIndicator.getParent()).removeView(leftSwipeGestureIndicator);

        super.onDestroy();
    }
}
