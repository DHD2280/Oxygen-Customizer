package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.view.animation.DecelerateInterpolator;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeSideLinesAnimator extends EdgeAnimator {

    private float currentHeight = 0f;
    private int mCurrentRainbow = 0;
    private int mCurrentPaintColor = Color.BLACK;

    public EdgeSideLinesAnimator(EdgeLightView edgeLightView) {
        super(edgeLightView);
    }

    @Override
    public Shader getShader() {
        int height = mEdgeLightView.getHeight();
        float centerY = height / 2f;

        float halfHeight = height * 0.5f * currentHeight;

        log("getShader - height = " + height +
                ", centerY = " + centerY +
                ", halfHeight = " + halfHeight);

        return new LinearGradient(
                0, centerY - halfHeight, 0, centerY + halfHeight,
                new int[]{Color.TRANSPARENT, mCurrentPaintColor, Color.TRANSPARENT},
                new float[]{0.0f, 0.5f, 1.0f},
                Shader.TileMode.CLAMP
        );
    }

    private int getOplusPaintColor() {
        if (rainbowMode) {
            int color = rainbowColors.get(mCurrentRainbow);
            mCurrentRainbow = (mCurrentRainbow + 1) % rainbowColors.size();
            return color;
        }
        return mEdgePaint.getColor();
    }

    @Override
    public void draw(Canvas canvas) {
        int width = mEdgeLightView.getWidth();
        int height = mEdgeLightView.getHeight();
        float barWidth = mEdgePaint.getStrokeWidth() * 2;
        float centerY = height / 2f;

        log("draw, width = " + width +
                ", height = " + height +
                ", barWidth = " + barWidth +
                ", centerY = " + centerY +
                ", currentHeight = " + currentHeight);

        float halfHeight = height * 0.5f * currentHeight;

        canvas.drawLine(mEdgePaint.getStrokeWidth() / 2,
                centerY - halfHeight,
                mEdgePaint.getStrokeWidth() / 2,
                centerY + halfHeight, mDrawBlur && mBlurMode == 1 ? mEdgeBlurPaint : mEdgePaint);
        if (mDrawBlur && mBlurMode == 0)
            canvas.drawRect(0, centerY - halfHeight, barWidth, centerY + halfHeight, mEdgeBlurPaint);


        canvas.drawLine(width - mEdgePaint.getStrokeWidth() / 2,
                centerY - halfHeight,
                width - mEdgePaint.getStrokeWidth() / 2,
                centerY + halfHeight, mDrawBlur && mBlurMode == 1 ? mEdgeBlurPaint : mEdgePaint);

        if (mDrawBlur && mBlurMode == 0)
            canvas.drawRect(width - barWidth, centerY - halfHeight, width, centerY + halfHeight, mEdgeBlurPaint);
    }

    @Override
    public void startAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mCurrentPaintColor = getOplusPaintColor();
        mEdgePaint.setShader(getShader());
        mAnimator = ValueAnimator.ofFloat(0, 1);
        mAnimator.setDuration(mFinalAnimDuration);
        mAnimator.addListener(mAnimationListener);
        mAnimator.setInterpolator(new DecelerateInterpolator());
        mAnimator.addUpdateListener(animation -> {
            currentHeight = (float) animation.getAnimatedValue();
            mEdgePaint.setShader(getShader());
            mEdgeBlurPaint.setShader(getShader());
            log("animationUpdateListener - currentHeight = " + currentHeight);
            postInvalidate();
        });
        mAnimator.start();
        mAnimating = true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {

    }
}
