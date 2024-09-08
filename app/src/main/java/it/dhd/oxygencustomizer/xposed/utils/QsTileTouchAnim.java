package it.dhd.oxygencustomizer.xposed.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;

public class QsTileTouchAnim {

    private static final String TAG = "QSTileTouchAnim";
    private ScaleAnimation mDownAnimation;
    private ValueAnimator mDownAnimationRecorder;
    private AnimatorSet mDownAnimatorSet;
    private ScaleAnimation mUpAnimation;
    private AnimatorSet mUpAnimatorSet;
    private boolean mIsNeedToDelayCancelScaleAnim = false;
    private float mAnimationUpValue = 1.0f;
    private boolean mIsPropertyAni = false;

    public void initTouchAnim(final View view, boolean z) {
        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View view2, MotionEvent motionEvent) {
                QsTileTouchAnim.this.doAction(motionEvent.getAction(), view);
                return false;
            }
        });
        this.mIsPropertyAni = z;
    }


    public void doAction(int motionAction, View view) {
        if (motionAction == MotionEvent.ACTION_DOWN) {
            doActionDown(view);
        } else if (motionAction == MotionEvent.ACTION_UP || motionAction == MotionEvent.ACTION_CANCEL) {
            doActionUpOrCancel(view);
        }
    }

    private void doActionDown(View view) {
        cancelViewRecorder(true);
        initViewRecorder(view);
        animateDown(view, this.mDownAnimationRecorder);
    }

    private void doActionUpOrCancel(View view) {
        cancelViewRecorder(false);
        animateNormal(view, this.mAnimationUpValue);
    }

    private void animateDown(View view, final ValueAnimator valueAnimator) {
        if (this.mIsPropertyAni) {
            view.clearAnimation();
            AnimatorSet animatorSet = this.mDownAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.mDownAnimatorSet = null;
            }
            AnimatorSet generatePressAnimationForProperty = PressFeedbackHelper.INSTANCE.generatePressAnimationForProperty(view);
            this.mDownAnimatorSet = generatePressAnimationForProperty;
            generatePressAnimationForProperty.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animator) {
                    valueAnimator.start();
                }
            });
            this.mDownAnimatorSet.start();
            return;
        }
        view.clearAnimation();
        ScaleAnimation scaleAnimation = this.mDownAnimation;
        if (scaleAnimation != null) {
            scaleAnimation.cancel();
            this.mDownAnimation = null;
        }
        ScaleAnimation generatePressAnimation = PressFeedbackHelper.INSTANCE.generatePressAnimation(view);
        this.mDownAnimation = generatePressAnimation;
        generatePressAnimation.setAnimationListener(new AnimationListenerAdapter() {
            @Override
            public void onAnimationStart(Animation animation) {
                valueAnimator.start();
            }
        });
        view.startAnimation(this.mDownAnimation);
    }

    private void cancelViewRecorder(Boolean bool) {
        ValueAnimator valueAnimator;
        boolean z = false;
        this.mIsNeedToDelayCancelScaleAnim = false;
        ValueAnimator valueAnimator2 = this.mDownAnimationRecorder;
        if (valueAnimator2 == null || !valueAnimator2.isRunning()) {
            return;
        }
        if (this.mDownAnimationRecorder != null) {
            if (!bool && ((float) this.mDownAnimationRecorder.getCurrentPlayTime()) < ((float) this.mDownAnimationRecorder.getDuration()) * 0.4f) {
                z = true;
            }
            this.mIsNeedToDelayCancelScaleAnim = z;
        }
        if (this.mIsNeedToDelayCancelScaleAnim || (valueAnimator = this.mDownAnimationRecorder) == null) {
            return;
        }
        valueAnimator.cancel();
    }

    private void initViewRecorder(final View view) {
        if (this.mDownAnimationRecorder != null) {
            this.mDownAnimationRecorder = null;
        }
        ValueAnimator generatePressAnimationRecord = PressFeedbackHelper.INSTANCE.generatePressAnimationRecord();
        this.mDownAnimationRecorder = generatePressAnimationRecord;
        generatePressAnimationRecord.addUpdateListener(valueAnimator -> {
            QsTileTouchAnim.this.mAnimationUpValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
            if (!QsTileTouchAnim.this.mIsNeedToDelayCancelScaleAnim || ((float) valueAnimator.getCurrentPlayTime()) <= ((float) valueAnimator.getDuration()) * 0.4f) {
                return;
            }
            QsTileTouchAnim.this.mIsNeedToDelayCancelScaleAnim = false;
            valueAnimator.cancel();
            QsTileTouchAnim qSTileTouchAnim = QsTileTouchAnim.this;
            qSTileTouchAnim.animateNormal(view, Float.valueOf(qSTileTouchAnim.mAnimationUpValue));
        });
    }

    void animateNormal(View view, Float f) {
        if (this.mIsPropertyAni) {
            if (this.mIsNeedToDelayCancelScaleAnim) {
                return;
            }
            view.clearAnimation();
            AnimatorSet animatorSet = this.mUpAnimatorSet;
            if (animatorSet != null) {
                animatorSet.cancel();
                this.mUpAnimatorSet = null;
            }
            AnimatorSet generateResumeAnimationForProperty = PressFeedbackHelper.INSTANCE.generateResumeAnimationForProperty(view, f.floatValue());
            this.mUpAnimatorSet = generateResumeAnimationForProperty;
            generateResumeAnimationForProperty.start();
        } else if (this.mIsNeedToDelayCancelScaleAnim) {
        } else {
            view.clearAnimation();
            ScaleAnimation scaleAnimation = this.mUpAnimation;
            if (scaleAnimation != null) {
                scaleAnimation.cancel();
                this.mUpAnimation = null;
            }
            ScaleAnimation generateResumeAnimation = PressFeedbackHelper.INSTANCE.generateResumeAnimation(view, f.floatValue());
            this.mUpAnimation = generateResumeAnimation;
            view.startAnimation(generateResumeAnimation);
        }
    }
}
