package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeEchoAnimator extends EdgeAnimator {

    private final float[] lineHeights = new float[3];
    private float animProgress = 0f;

    public EdgeEchoAnimator(EdgeLightView edgeLightView) {
        super(edgeLightView);
        lineHeights[0] = edgeLightView.getHeight() * 0.8f;
        lineHeights[1] = edgeLightView.getHeight() * 0.5f;
        lineHeights[2] = edgeLightView.getHeight() * 0.3f;
    }

    @Override
    public void draw(Canvas canvas) {
        float strokeWidth = mEdgePaint.getStrokeWidth();
        float padding = strokeWidth / 2;
        float startXLeft = 0 + strokeWidth;
        float startXRight = mEdgeLightView.getWidth() - strokeWidth;

        for (int i = 0; i < 3; i++) {
            float alpha = calculateAlpha(i, animProgress);
            mEdgePaint.setAlpha((int) (alpha * 255));

            float offsetX = i * (strokeWidth + padding);

            float leftX = startXLeft + offsetX;
            canvas.drawLine(leftX, (mEdgeLightView.getHeight() - lineHeights[i]) / 2,
                    leftX, (mEdgeLightView.getHeight() + lineHeights[i]) / 2, mEdgePaint);

            float rightX = startXRight - offsetX;
            canvas.drawLine(rightX, (mEdgeLightView.getHeight() - lineHeights[i]) / 2,
                    rightX, (mEdgeLightView.getHeight() + lineHeights[i]) / 2, mEdgePaint);
        }
    }

    private float calculateAlpha(int index, float progress) {
        float phase = progress * 3 - index;
        return Math.max(0, Math.min(1, phase));
    }

    @Override
    public void startAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mFinalAnimDuration);
        mAnimator.addListener(mAnimationListener);
        mAnimator.addUpdateListener(animation -> {
            animProgress = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        mAnimator.start();
        mAnimating = true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        lineHeights[0] = h * 0.8f;
        lineHeights[1] = h * 0.5f;
        lineHeights[2] = h * 0.3f;
    }

}
