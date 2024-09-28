package it.dhd.oxygencustomizer.xposed.utils;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.view.View;
import android.view.animation.PathInterpolator;
import android.view.animation.ScaleAnimation;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import kotlin.jvm.internal.Intrinsics;

public final class PressFeedbackHelper {

    @NotNull
    public static final PressFeedbackHelper INSTANCE = new PressFeedbackHelper();
    @NotNull
    private static final PathInterpolator PRESS_FEEDBACK_INTERPOLATOR = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);

    private PressFeedbackHelper() {
    }

    @NotNull
    public ScaleAnimation generatePressAnimation(@Nullable View view) {
        if (view == null) {
            throw new IllegalArgumentException("The given view is empty. Please provide a valid view.".toString());
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.92f, 1.0f, 0.92f, view.getWidth() / 2.0f, view.getHeight() / 2.0f);
        scaleAnimation.setDuration(200L);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setInterpolator(PRESS_FEEDBACK_INTERPOLATOR);
        return scaleAnimation;
    }

    @NotNull
    public AnimatorSet generatePressAnimationForProperty(@Nullable View view) {
        if (view == null) {
            throw new IllegalArgumentException("The given view is empty. Please provide a valid view.".toString());
        }
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 0.92f);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 0.92f);
        animatorSet.setDuration(200L);
        animatorSet.playTogether(ofFloat, ofFloat2);
        animatorSet.setInterpolator(PRESS_FEEDBACK_INTERPOLATOR);
        return animatorSet;
    }

    @NotNull
    public ValueAnimator generatePressAnimationRecord() {
        ValueAnimator pressAnimationRecord = ValueAnimator.ofFloat(1.0f, 0.92f);
        pressAnimationRecord.setDuration(200L);
        pressAnimationRecord.setInterpolator(PRESS_FEEDBACK_INTERPOLATOR);
        Intrinsics.checkNotNullExpressionValue(pressAnimationRecord, "pressAnimationRecord");
        return pressAnimationRecord;
    }

    @NotNull
    public ScaleAnimation generateResumeAnimation(@Nullable View view, float f) {
        if (view == null) {
            throw new IllegalArgumentException("The given view is empty. Please provide a valid view.".toString());
        }
        ScaleAnimation scaleAnimation = new ScaleAnimation(f, 1.0f, f, 1.0f, view.getWidth() / 2.0f, view.getHeight() / 2.0f);
        scaleAnimation.setDuration(340L);
        scaleAnimation.setFillAfter(true);
        scaleAnimation.setInterpolator(PRESS_FEEDBACK_INTERPOLATOR);
        return scaleAnimation;
    }

    @NotNull
    public AnimatorSet generateResumeAnimationForProperty(@Nullable View view, float f) {
        if (view == null) {
            throw new IllegalArgumentException("The given view is empty. Please provide a valid view.".toString());
        }
        AnimatorSet animatorSet = new AnimatorSet();
        ObjectAnimator ofFloat = ObjectAnimator.ofFloat(view, "scaleX", f, 1.0f);
        ObjectAnimator ofFloat2 = ObjectAnimator.ofFloat(view, "scaleY", f, 1.0f);
        animatorSet.setDuration(340L);
        animatorSet.playTogether(ofFloat, ofFloat2);
        animatorSet.setInterpolator(PRESS_FEEDBACK_INTERPOLATOR);
        return animatorSet;
    }
}
