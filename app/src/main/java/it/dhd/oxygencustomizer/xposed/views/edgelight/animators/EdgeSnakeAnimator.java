package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.view.animation.LinearInterpolator;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeSnakeAnimator extends EdgeLineAnimator {

    private float snakeLengthFactor = 0.2f;
    private float snakeStart = 0;
    private float snakeEnd = 0;
    private Path drawnPath = new Path();

    public EdgeSnakeAnimator(EdgeLightView edgeLightView) {
        super(edgeLightView);
    }

    @Override
    public void draw(Canvas canvas) {
        edgeLightPath.reset();
        drawnPath.reset();

        RectF rect = new RectF(
                mEdgePaint.getStrokeWidth() / 2,
                mEdgePaint.getStrokeWidth() / 2,
                mEdgeLightView.getWidth() - mEdgePaint.getStrokeWidth() / 2,
                mEdgeLightView.getHeight() - mEdgePaint.getStrokeWidth() / 2
        );

        edgeLightPath.addRoundRect(rect, mScreenRadius, mScreenRadius, Path.Direction.CW);

        if (pathMeasure == null) {
            pathMeasure = new PathMeasure(edgeLightPath, false);
        }

        pathMeasure.getSegment(snakeStart, snakeEnd, drawnPath, true);
        canvas.drawPath(drawnPath, mDrawBlur && mBlurMode == 1 ? mEdgeBlurPaint : mEdgePaint);
        if (mDrawBlur && mBlurMode == 0) canvas.drawPath(drawnPath, mEdgeBlurPaint);
    }

    @Override
    public void startAnimation() {
        if (mAnimator != null && mAnimator.isRunning()) {
            mAnimator.cancel();
        }

        pathMeasure = new PathMeasure(edgeLightPath, false);
        float pathLength = pathMeasure.getLength();
        float snakeLength = pathLength * snakeLengthFactor;

        mAnimator = ValueAnimator.ofFloat(0, pathLength + snakeLength);
        mAnimator.setDuration(mFinalAnimDuration);
        mAnimator.addListener(mAnimationListener);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            float progress = (float) animation.getAnimatedValue();
            snakeStart = progress - snakeLength;
            snakeEnd = progress;

            if (snakeStart < 0) snakeStart = 0;
            if (snakeEnd > pathLength) snakeEnd = pathLength;

            log("snakeStart: " + snakeStart + " snakeEnd: " + snakeEnd);

            postInvalidate();
        });
        mAnimator.start();
        mAnimating = true;
    }

}
