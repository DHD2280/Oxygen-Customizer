package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.view.animation.LinearInterpolator;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeBlinkAnimator extends EdgeAnimator {


    public EdgeBlinkAnimator(EdgeLightView edgeLightView) {
        super(edgeLightView);
    }

    @Override
    public void draw(Canvas canvas) {
        log("draw, mEdgePaint.getStrokeWidth() = " + mEdgePaint.getStrokeWidth() +
                ", mEdgeLightView.getWidth() = " + mEdgeLightView.getWidth() +
                ", mEdgeLightView.getHeight() = " + mEdgeLightView.getHeight() +
                ", mScreenRadius = " + mScreenRadius);
        RectF rect = new RectF(
                mEdgePaint.getStrokeWidth(),
                mEdgePaint.getStrokeWidth(),
                mEdgeLightView.getWidth() - mEdgePaint.getStrokeWidth(),
                mEdgeLightView.getHeight() - mEdgePaint.getStrokeWidth()
        );
        canvas.drawRoundRect(rect, mScreenRadius, mScreenRadius, mDrawBlur && mBlurMode == 1 ? mEdgeBlurPaint : mEdgePaint);
        if (mDrawBlur && mBlurMode == 0) canvas.drawRoundRect(rect, mScreenRadius, mScreenRadius, mEdgeBlurPaint);
    }

    @Override
    public void startAnimation() {
        log("startPulseAnimation, mPulsingDuration = " + mPulsingDuration);
        if (mAnimating) return;
        mAnimator = ValueAnimator.ofFloat(0.3f, 1f);
        mAnimator.setDuration(mPulsingDuration);
        mAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mAnimator.addListener(mAnimationListener);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animator -> {
            float alpha = (float) animator.getAnimatedValue();
            mEdgePaint.setAlpha((int) (alpha * 255));
            mEdgeBlurPaint.setAlpha((int) (alpha * 255));
            postInvalidate();
        });
        mAnimator.start();
        mAnimating = true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {

    }

}
