package it.dhd.oxygencustomizer.ui.fragments.mods.navbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class CustomGestures extends ControlledPreferenceFragmentCompat {

    FrameLayout leftSwipeGestureIndicator, rightSwipeGestureIndicator;

    @Override
    public String getTitle() {
        return getString(R.string.gesture_nav_extra_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.custom_gestures;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{/*SYSTEM_UI,*/ LAUNCHER};
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        super.onCreatePreferences(savedInstanceState, rootKey);

        rightSwipeGestureIndicator = prepareSwipeGestureView(Gravity.RIGHT);
        leftSwipeGestureIndicator = prepareSwipeGestureView(Gravity.LEFT);
    }

    @Override
    public void updateScreen(String key) {
        super.updateScreen(key);
        try {
            if (getActivity() != null) {
                int displayHeight = getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().height();
                int displayWidth = getActivity().getWindowManager().getCurrentWindowMetrics().getBounds().width();

                float leftSwipeUpPercentage = mPreferences.getSliderFloat("leftSwipeUpPercentage", 25);

                float rightSwipeUpPercentage = mPreferences.getSliderFloat("rightSwipeUpPercentage", 25);

                int edgeWidth = Math.round(displayWidth * leftSwipeUpPercentage / 100f);
                ViewGroup.LayoutParams lp = leftSwipeGestureIndicator.getLayoutParams();
                lp.width = edgeWidth;
                leftSwipeGestureIndicator.setLayoutParams(lp);

                edgeWidth = Math.round(displayWidth * rightSwipeUpPercentage / 100f);
                lp = rightSwipeGestureIndicator.getLayoutParams();
                lp.width = edgeWidth;
                rightSwipeGestureIndicator.setLayoutParams(lp);

                setVisibility(rightSwipeGestureIndicator, PreferenceHelper.isVisible("rightSwipeUpPercentage"), 400);
                setVisibility(leftSwipeGestureIndicator, PreferenceHelper.isVisible("leftSwipeUpPercentage"), 400);
            }
        } catch (Exception ignored) {
        }
    }

    private FrameLayout prepareSwipeGestureView(int gravity) {
        int navigationBarHeight = 0;
        @SuppressLint({"DiscouragedApi", "InternalInsetResource"})
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = requireContext().getResources().getDimensionPixelSize(resourceId);
        }

        FrameLayout result = new FrameLayout(getContext());
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, navigationBarHeight);
        lp.gravity = gravity | Gravity.BOTTOM;
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

        FrameLayout result = new FrameLayout(getContext());
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
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!visible) v.setVisibility(View.GONE);
                v.setAlpha(basicAlpha);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        }).start();
    }

    @Override
    public void onDestroy() {

        ((ViewGroup) rightSwipeGestureIndicator.getParent()).removeView(rightSwipeGestureIndicator);
        ((ViewGroup) leftSwipeGestureIndicator.getParent()).removeView(leftSwipeGestureIndicator);

        super.onDestroy();
    }

}
