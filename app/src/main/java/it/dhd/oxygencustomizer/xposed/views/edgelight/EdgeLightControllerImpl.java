package it.dhd.oxygencustomizer.xposed.views.edgelight;

import static de.robv.android.xposed.XposedHelpers.callMethod;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeAnimator;

public class EdgeLightControllerImpl {

    public static final boolean DEBUG = BuildConfig.DEBUG;
    private static final String TAG = EdgeLightControllerImpl.class.getSimpleName();

    private final Context mContext;
    @SuppressLint("StaticFieldLeak")
    public static EdgeLightControllerImpl instance = null;
    @SuppressLint("StaticFieldLeak")
    private static EdgeLightView mEdgeLightView;

    private FrameLayout mAodRootLayout;
    private FrameLayout mAodBlockLayout;
    private Object mDozeParameters = null;

    private int animationDuration, pulsingDuration;
    private long totalDuration = 0;

    // Prefs
    private boolean mEdgeLightEnabled = false;
    private float mEdgeLightWidth = 20f;
    private int mEdgeLightStyle = 0;
    private EdgeLightView.ColorMode mEdgeLightColorMode = EdgeLightView.ColorMode.ACCENT;
    private boolean mAlwaysTriggerOnPulse = false;
    private boolean mRetick = false;
    private int mEdgeLightCustomColor = Color.RED;
    private int mScreenCornerRadius = 20;
    private boolean mEdgeDrawBlur = false;
    private int mEdgeBlurMode = 0, mEdgeBlurType = 0;

    private boolean mDozing = false;
    private boolean mCurved = false;
    private boolean mIsFromOc = false;

    public static EdgeLightControllerImpl getInstance(Context context) {
        if (instance != null) return instance;
        return new EdgeLightControllerImpl(context);
    }

    public static EdgeLightControllerImpl getInstance() {
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public EdgeLightView getEdgeLightView() {
        return mEdgeLightView;
    }

    public EdgeLightControllerImpl(Context context) {
        instance = this;
        mContext = context;
        mEdgeLightView = EdgeLightView.getInstance(mContext);
    }

    public void setOptions(
            boolean edgeLightEnabled,
            int edgeLightStyle,
            float edgeLightWidth,
            EdgeLightView.ColorMode colorMode,
            boolean alwaysTriggerOnPulse,
            boolean retick,
            int customColor,
            boolean drawBlur,
            int blurMode,
            int blurType) {
        logD("setOptions");
        mEdgeLightEnabled = edgeLightEnabled;
        mEdgeLightStyle = edgeLightStyle;
        mEdgeLightWidth = edgeLightWidth;
        mEdgeLightColorMode = colorMode;
        mAlwaysTriggerOnPulse = alwaysTriggerOnPulse;
        mRetick = retick;
        mEdgeLightCustomColor = customColor;
        mEdgeDrawBlur = drawBlur;
        mEdgeBlurMode = blurMode;
        mEdgeBlurType = blurType;
        mEdgeLightView.setOptions(mEdgeLightStyle, mEdgeLightWidth, mEdgeLightColorMode, mEdgeLightCustomColor, mEdgeDrawBlur, mEdgeBlurMode, mEdgeBlurType);
    }

    public void setNotificationColor(int color) {
        logD("setNotificationColor: " + color);
        mEdgeLightView.setNotificationColor(color);
    }

    public void setScreenRadius(int radius) {
        mScreenCornerRadius = radius;
        mEdgeLightView.setScreenRadius(mScreenCornerRadius);
    }

    public void setDozing(boolean dozing) {
        if (mDozing != dozing) {
            mDozing = dozing;
        }
        logD("setDozing: " + dozing);
    }

    public void setDozeParameters(Object dozeParameters) {
        mDozeParameters = dozeParameters;
        try {
            animationDuration = (int) callMethod(mDozeParameters, "getPulseVisibleDuration");
            pulsingDuration = ((int) callMethod(mDozeParameters, "getPulseVisibleDuration") / 3);
            mEdgeLightView.setDurations(animationDuration, pulsingDuration, totalDuration);
        } catch (Throwable t) {
            logD("setDozeParameters: error: " + Log.getStackTraceString(t));
        }
    }

    public void setAnimationDuration(long dur) {
        this.totalDuration = dur;
        mEdgeLightView.setDurations(animationDuration, (int) (totalDuration/3), totalDuration);
    }

    public void setAodRootLayout(FrameLayout aodRootLayout) {
        mAodRootLayout = aodRootLayout;
    }

    public void setBlockLayout(FrameLayout aodBlockLayout) {
        mAodBlockLayout = aodBlockLayout;
    }

    public void setCurved(boolean curved) {
        this.mCurved = curved;
        updateEdgeVisibility();
    }

    private void updateEdgeVisibility() {
        boolean allowCurved = mCurved;
        boolean allowAlwaysPulse = (mAlwaysTriggerOnPulse || mRetick) && !mCurved;
        logD("updateEdgeVisibility, allowCurved: " + allowCurved + " allowAlwaysPulse: " + allowAlwaysPulse);

        detachEdge();
        if (allowAlwaysPulse) {
            attachEdgeTo(mAodRootLayout);
        } else if (allowCurved) {
            attachEdgeTo(mAodBlockLayout);
        }
    }

    public void triggerShow(boolean isFromOc) {
        logD("triggerShow");
        mCurved = false;
        mDozing = true;
        mIsFromOc = isFromOc;
        updateEdgeVisibility();
    }

    private void attachEdgeTo(FrameLayout parent) {
        logD("attachEdgeTo() " + parent);
        if (parent == null) return;
        View v = parent.findViewWithTag(EdgeLightView.TAG);
        if (v == null) {
            parent.addView(mEdgeLightView);
            mEdgeLightView.bringToFront();
            mEdgeLightView.requestLayout();
            mEdgeLightView.setPulsing(mEdgeLightEnabled, mCurved || mIsFromOc ? mEdgeLightView.PULSE_REASON_NOTIFICATION : 0);
        }
        mIsFromOc = false;
    }

    private void detachEdge() {
        logD("detachEdge()");
        try {
            ((ViewGroup) mEdgeLightView.getParent()).removeView(mEdgeLightView);
            EdgeAnimator animator = mEdgeLightView.getCurrentEdgeAnimator();
            if (animator != null) {
                animator.stopAnimation();
            }
        } catch (Throwable ignored) {}
    }

    private void logD(String msg) {
        if (!DEBUG) return;
        XposedBridge.log(TAG + "\n" + msg + "\n" +
                "mEdgeLightEnabled: " + mEdgeLightEnabled + " " +
                "mDozing: " + mDozing + " " +
                "mCurved: " + mCurved + " " +
                "mIsFromOc: " + mIsFromOc + " " +
                "mRetick: " + mRetick + " " +
                "mAlwaysTriggerOnPulse: " + mAlwaysTriggerOnPulse);
    }

}
