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

public class EdgeLightControllerImpl {

    public static final boolean DEBUG = true;
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
    private int mEdgeLightCustomColor = Color.RED;
    private int mScreenCornerRadius = 20;
    private boolean mEdgeDrawBlur = false;
    private int mEdgeBlurMode = 0, mEdgeBlurType = 0;

    private boolean mDozing = false;
    private boolean mCurved = false;

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
            if (mEdgeLightEnabled)
                updateEdgeVisibility();
        }
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
        mEdgeLightView.setDurations(animationDuration, pulsingDuration, totalDuration);
    }

    public void setAodRootLayout(FrameLayout aodRootLayout) {
        mAodRootLayout = aodRootLayout;
        updateEdgeVisibility();
    }

    public void setBlockLayout(FrameLayout aodBlockLayout) {
        mAodBlockLayout = aodBlockLayout;
        updateEdgeVisibility();
    }

    public void setCurved(boolean curved) {
        this.mCurved = curved;
        updateEdgeVisibility();
    }

    private void updateEdgeVisibility() {
        logD("updateEdgeVisibility");
        boolean allowCurved = mCurved;
        boolean allowAlwaysPulse = mAlwaysTriggerOnPulse && !mCurved;

        detachEdge();
        if (allowAlwaysPulse) {
            attachEdgeTo(mAodRootLayout);
        } else if (allowCurved) {
            attachEdgeTo(mAodBlockLayout);
        }
    }

    public void triggerShow() {
        logD("triggerShow");
        mCurved = false;
        mDozing = true;
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
            mEdgeLightView.setPulsing(mEdgeLightEnabled, mCurved ? mEdgeLightView.PULSE_REASON_NOTIFICATION : 0);
        }
    }

    private void detachEdge() {
        try {
            ((ViewGroup) mEdgeLightView.getParent()).removeView(mEdgeLightView);
        } catch (Throwable ignored) {}
    }

    private void logD(String msg) {
        if (!DEBUG) return;
        XposedBridge.log(TAG + "\n" +
                "mEdgeLightEnabled: " + mEdgeLightEnabled + " " +
                "mDozing: " + mDozing + " " +
                "mCurved: " + mCurved + " " +
                "mAlwaysTriggerOnPulse: " + mAlwaysTriggerOnPulse + "\n" +
                msg);
    }

}
