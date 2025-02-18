package it.dhd.oxygencustomizer.xposed.views.edgelight.animators;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public abstract class EdgeAnimator {

    protected EdgeLightView mEdgeLightView;

    // Screen Radius placeholder
    public float mScreenRadius = 20f;

    // Animator
    public ValueAnimator mAnimator;
    public boolean mAnimating;
    // Paint
    protected Paint mEdgePaint;
    protected Paint mEdgeBlurPaint;
    // Durations
    protected long mAnimationDuration = 6300;
    protected long mPulsingDuration = 2100;
    protected long mFinalAnimDuration;

    // Paint Stroke Width placeholder
    public float mEdgeLightWidth = 20f;

    protected boolean mDrawBlur = false;
    protected int mBlurType = 0;
    protected int mBlurMode = 0;

    protected boolean rainbowMode = false;
    protected final List<Integer> rainbowColors = Arrays.asList(
            Color.RED,
            Color.parseColor("#FF7F00"),
            Color.YELLOW,
            Color.GREEN,
            Color.BLUE,
            Color.parseColor("#4B0082"),
            Color.parseColor("#8A2BE2")
    );

    private final Runnable hideRunnable = () -> mEdgeLightView.hide();

    protected final Animator.AnimatorListener mAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(@NonNull Animator animation) {}

        @Override
        public void onAnimationEnd(@NonNull Animator animation) {
            log("onAnimationEnd");
            if (mEdgeLightView.isSettingsInterface()) return;
            stopAnimation();
            mEdgeLightView.postDelayed(hideRunnable, 500L);
        }

        @Override
        public void onAnimationCancel(@NonNull Animator animation) {
            log("onAnimationCancel");
            mEdgeLightView.removeCallbacks(hideRunnable);
        }

        @Override
        public void onAnimationRepeat(@NonNull Animator animation) {}
    };

    public EdgeAnimator(EdgeLightView edgeLightView) {
        mEdgeLightView = edgeLightView;
        mEdgePaint = new Paint();
        mEdgePaint.setStyle(Paint.Style.STROKE);
        mEdgePaint.setStrokeWidth(mEdgeLightWidth);
        mEdgePaint.setAlpha(255);
        mEdgePaint.setAntiAlias(true);
        mEdgePaint.setStrokeCap(Paint.Cap.ROUND);
        mEdgePaint.setStrokeJoin(Paint.Join.ROUND);
        mEdgePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        mEdgePaint.setShader(getShader());
        mEdgePaint.setMaskFilter(null);
        mEdgeBlurPaint = new Paint(mEdgePaint);
    }

    public void setBlurOptions(boolean drawBlur, int blurType, int blurMode) {
        mDrawBlur = drawBlur;
        mBlurType = blurType;
        mBlurMode = blurMode;
        setBlurType(blurType);
    }

    public abstract void draw(Canvas canvas);

    public abstract void startAnimation();

    public void stopAnimation() {
        log("stopAnimation");
        if (mAnimator != null) mAnimator.cancel();
        mAnimator = null;
        mAnimating = false;
        mEdgePaint.setAlpha(255);
        mEdgeBlurPaint.setAlpha(255);
        postInvalidate();
    }

    public abstract void onSizeChanged(int w, int h, int oldw, int oldh);

    public Shader getShader() {
        if (!rainbowMode) return null;
        return new LinearGradient(
                0f, 0f, (float) mEdgeLightView.getWidth(), (float) mEdgeLightView.getHeight(),
                rainbowColors.stream().mapToInt(Integer::intValue).toArray(),
                null,
                Shader.TileMode.CLAMP
        );
    }

    public void setScreenRadius(float radius) {
        mScreenRadius = radius;
    }

    public void setDurations(int anim, int pulsing, long duration) {
        if (mFinalAnimDuration == duration) return;
        this.mAnimationDuration = anim;
        this.mPulsingDuration = pulsing;
        if (duration == 0) duration = mAnimationDuration;
        this.mFinalAnimDuration = duration;
        if (mAnimating) {
            stopAnimation();
            startAnimation();
        }
    }

    public void setPaintColor(int color, boolean isRainbow) {
        log("setPaintColor: " + color + ", isRainbow: " + isRainbow);
        rainbowMode = isRainbow;
        mEdgePaint.setColor(color);
        mEdgeBlurPaint.setColor(color);
        refreshShader();
    }

    public void setBlurType(int type) {
        BlurMaskFilter.Blur blurMaskFilter = switch (type) {
            case 1 -> BlurMaskFilter.Blur.SOLID;
            case 2 -> BlurMaskFilter.Blur.INNER;
            case 3 -> BlurMaskFilter.Blur.OUTER;
            default -> BlurMaskFilter.Blur.NORMAL;
        };
        mEdgeBlurPaint.setMaskFilter(new BlurMaskFilter(50, blurMaskFilter));
    }

    private void refreshShader() {
        mEdgePaint.setShader(getShader());
        mEdgeBlurPaint.setShader(getShader());
    }

    public void setStrokeWidth(float width) {
        this.mEdgeLightWidth = width;
        mEdgePaint.setStrokeWidth(mEdgeLightWidth);
        mEdgeBlurPaint.setStrokeWidth(mEdgeLightWidth);
    }

    protected final void postInvalidate() {
        mEdgeLightView.postInvalidate();
    }

    protected final void log(String message) {
        mEdgeLightView.logD(this.getClass().getSimpleName() + " - " + message);
    }

    public boolean isAnimating() {
        return this.mAnimating;
    }

}
