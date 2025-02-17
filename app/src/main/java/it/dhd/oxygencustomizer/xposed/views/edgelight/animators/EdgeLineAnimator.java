package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.RectF;
import android.view.animation.LinearInterpolator;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class EdgeLineAnimator extends EdgeAnimator {

    public float animatedPathLength;
    public Path edgeLightPath = new Path();
    public Path drawnPath = new Path();
    public PathMeasure pathMeasure;

    public EdgeLineAnimator(EdgeLightView edgeLightView) {
        super(edgeLightView);
    }

    @Override
    public void setStrokeWidth(float width) {
        super.setStrokeWidth(width);
        mEdgeBlurPaint.setStrokeWidth(width);
        log("setStrokeWidth");
        initPath();
    }

    @Override
    public void setScreenRadius(float radius) {
        super.setScreenRadius(radius);
        log("setScreenRadius");
        initPath();
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

        pathMeasure.getSegment(0, animatedPathLength, drawnPath, true);

        canvas.drawPath(drawnPath, mDrawBlur && mBlurMode == 1 ? mEdgeBlurPaint : mEdgePaint);
        if (mDrawBlur && mBlurMode == 0) canvas.drawPath(drawnPath, mEdgeBlurPaint);
    }

    @Override
    public void startAnimation() {
        log("startAnimation");
        if (mAnimating) return;

        if (pathMeasure == null) {
            log("startAnimation: Path is not initialized, calling initPath()");
            initPath();
        }

        if (pathMeasure == null || pathMeasure.getLength() == 0) {
            log("startAnimation: Path still not valid, cannot start animation!");
            return;
        }

        float pathLength = pathMeasure.getLength();
        log("startAnimation, pathLength= " + pathLength + ", mAnimationDuration= " + mFinalAnimDuration);

        mAnimator = ValueAnimator.ofFloat(0, pathLength);
        mAnimator.setDuration(mFinalAnimDuration);
        mAnimator.addListener(mAnimationListener);
        mAnimator.setInterpolator(new LinearInterpolator());
        mAnimator.addUpdateListener(animation -> {
            animatedPathLength = (float) animation.getAnimatedValue();
            postInvalidate();
        });
        mAnimator.start();
        mAnimating = true;
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        log("onSizeChanged");
        initPath();
    }

    private void initPath() {
        log("initPath, mEdgeLightView.getWidth() = " + mEdgeLightView.getWidth() +
                ", mEdgeLightView.getHeight() = " + mEdgeLightView.getHeight() +
                ", mEdgePaint.getStrokeWidth() = " + mEdgePaint.getStrokeWidth() +
                ", mScreenRadius = " + mScreenRadius);
        edgeLightPath.reset();
        RectF rect = new RectF(
                mEdgePaint.getStrokeWidth() / 2,
                mEdgePaint.getStrokeWidth() / 2,
                mEdgeLightView.getWidth() - mEdgePaint.getStrokeWidth() / 2,
                mEdgeLightView.getHeight() - mEdgePaint.getStrokeWidth() / 2
        );
        edgeLightPath.addRoundRect(rect, mScreenRadius, mScreenRadius, Path.Direction.CW);

        pathMeasure = new PathMeasure(edgeLightPath, false);
    }


}
