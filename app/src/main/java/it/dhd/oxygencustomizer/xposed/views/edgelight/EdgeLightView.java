package it.dhd.oxygencustomizer.xposed.views.edgelight;

/*
 * Copyright (C) 2022 FlamingoOS Project
 * Copyright (C) 2023-2025 the risingOS Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License
 */

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

import java.lang.ref.WeakReference;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.utils.ThemeUtils;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeAnimator;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeBlinkAnimator;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeSideLinesAnimator;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeDottedLineAnimator;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeEchoAnimator;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeLineAnimator;
import it.dhd.oxygencustomizer.xposed.views.edgelight.animators.EdgeSnakeAnimator;

@SuppressLint("ViewConstructor")
public class EdgeLightView extends View {

    @SuppressLint("StaticFieldLeak")
    public static WeakReference<EdgeLightView> instance;
    private final Context mContext;
    public static final String TAG = "EdgeLightView";

    // Settings
    private float mEdgeLightWidth = 20f;
    private int mEdgeLightStyle = 0;
    private int mCustomColor = Color.RED;
    private ColorMode mColorMode = ColorMode.ACCENT;
    private boolean mDrawBlur = false;
    private int mBlurMode = 0;
    private int mBlurType = 0;
    private float mCornerRadius = 20f;
    private int mPreviousNotificationColor = -2, mNotificationColor = -2;
    private EdgeAnimator mEdgeAnimator;

    // Styles
    private final int EDGE_STYLE_LINE = 0;
    private final int EDGE_STYLE_BLINK = 1;
    private final int EDGE_STYLE_SIDE_LINES = 2;
    private final int EDGE_STYLE_SNAKE = 3;
    private final int EDGE_STYLE_ECHO = 4;
    private final int EDGE_STYLE_DOTTED_LINE = 5;

    // Vals
    private boolean mPulsing = false;

    public final int PULSE_REASON_NOTIFICATION = 1;

    private final WallpaperManager wallpaperManager;

    // Durations
    private long mAnimationDuration = 6300;
    private long animationDuration = 6300;
    private long pulsingDuration = 2100;
    private int repeatCount = 1;

    private Object mDozeParameters = null;

    public enum ColorMode {
        ACCENT,
        NOTIFICATION,
        WALLPAPER,
        GRADIENT,
        CUSTOM
    }

    private boolean mSettingsInterface = true;

    public void setSettingsInterface(boolean settingsInterface) {
        this.mSettingsInterface = settingsInterface;
    }

    public boolean isSettingsInterface() {
        return mSettingsInterface;
    }

    public static EdgeLightView getInstance(Context context) {
        if (instance != null) return instance.get();
        return new EdgeLightView(context, false);
    }

    public static EdgeLightView getInstance() {
        return instance.get();
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public EdgeLightView(Context context, boolean settingsInterface) {
        super(context);
        mContext = context;
        mSettingsInterface = settingsInterface;
        if (!mSettingsInterface) instance = new WeakReference<>(this);
        this.wallpaperManager = (WallpaperManager) mContext.getSystemService(Context.WALLPAPER_SERVICE);
        loadEdgeAnimator();
    }

    public void setOptions(
            int edgeLightStyle,
            float edgeLightWidth,
            ColorMode colorMode,
            int customColor,
            boolean drawBlur,
            int blurMode,
            int blurType) {
        this.mEdgeLightWidth = edgeLightWidth;
        this.mDrawBlur = drawBlur;
        this.mBlurType = blurType;
        this.mBlurMode = blurMode;
        setEdgeLightStyle(edgeLightStyle);
        this.mCustomColor = customColor;
        setStrokeWidth(mEdgeLightWidth);
        setColorMode(colorMode);
    }

    public void setColorMode(ColorMode colorMode) {
        logD("setColorMode: " + colorMode);
        mColorMode = colorMode;
        switch (colorMode) {
            case ACCENT:
                setColor(mSettingsInterface ? ThemeUtils.getPrimaryColor(mContext) : OpUtils.getPrimaryColor(mContext));
                break;
            case NOTIFICATION:
                setColor(mNotificationColor);
                break;
            case CUSTOM:
                setColor(mCustomColor);
                break;
            case GRADIENT:
                setColor(-1);
                break;
            case WALLPAPER:
                WallpaperColors  wpColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_LOCK);
                if (wpColors == null) setColor(mSettingsInterface ? ThemeUtils.getPrimaryColor(mContext) : OpUtils.getPrimaryColor(mContext));
                else setColor(wpColors.getPrimaryColor().toArgb());
                break;
        }
    }

    private void loadEdgeAnimator() {
        logD("loadEdgeAnimator");
        if (mEdgeAnimator != null) {
            mEdgeAnimator = null;
        }
        mEdgeAnimator = getEdgeAnimator();
        mEdgeAnimator.setBlurOptions(mDrawBlur, mBlurType, mBlurMode);
        mEdgeAnimator.setScreenRadius(mCornerRadius);
        mEdgeAnimator.setDurations((int) animationDuration, (int) pulsingDuration, mAnimationDuration);
    }

    private EdgeAnimator getEdgeAnimator() {
        return switch (mEdgeLightStyle) {
            case EDGE_STYLE_BLINK -> new EdgeBlinkAnimator(this);
            case EDGE_STYLE_SIDE_LINES -> new EdgeSideLinesAnimator(this);
            case EDGE_STYLE_SNAKE -> new EdgeSnakeAnimator(this);
            case EDGE_STYLE_ECHO -> new EdgeEchoAnimator(this);
            case EDGE_STYLE_DOTTED_LINE -> new EdgeDottedLineAnimator(this);
            default -> new EdgeLineAnimator(this);
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mEdgeAnimator == null) return;
        mEdgeAnimator.onSizeChanged(w, h, oldw, oldh);
    }

    public void startAnimation() {
        logD("startAnimation");
        if (mEdgeAnimator == null) return;
        mEdgeAnimator.startAnimation();
    }

    public void stopAnimation() {
        logD("stopAnimation");
        if (mEdgeAnimator == null) return;
        mEdgeAnimator.stopAnimation();
    }

    public void setEdgeLightStyle(int style) {
        logD("setEdgeLightStyle: " + style);
        mEdgeLightStyle = style;
        loadEdgeAnimator();
        setStrokeWidth(mEdgeLightWidth);
        invalidate();
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        if (!mEdgeAnimator.isAnimating()) return;
        mEdgeAnimator.draw(canvas);
    }

    public void show() {
        logD("show " + "\n" +
                "mPulsing: " + mPulsing + "\n" +
                "animating: " + mEdgeAnimator.isAnimating() + "\n" +
                "mAnimationDuration: " + mAnimationDuration + "\n" +
                "animationDuration: " + animationDuration + "\n" +
                "pulsingDuration: " + pulsingDuration + "\n" +
                "mEdgeLightStyle: " + mEdgeLightStyle + "\n" +
                "mColorMode: " + mColorMode + "\n" +
                "mCustomColor: " + mCustomColor + "\n" +
                "mNotificationColor: " + mNotificationColor);
        if (mEdgeAnimator.isAnimating()) return;
        if (getVisibility() == GONE) setVisibility(VISIBLE);
        startAnimation();
    }

    public void hide() {
        logD("hide");
        if (getVisibility() == VISIBLE) setVisibility(GONE);
        if (!mEdgeAnimator.isAnimating()) return;
        stopAnimation();
    }

    public void setColor(int color) {
        logD("setColor: " + color);
        mEdgeAnimator.setPaintColor(color, color == -1);
        invalidate();
    }

    public void setRepeatCount(int repeatCount) {
        this.repeatCount = repeatCount;
    }

    public void setScreenRadius(float radius) {
        mCornerRadius = radius;
    }

    public void setDurations(int anim, int pulsing, long duration) {
        this.animationDuration = anim;
        this.pulsingDuration = pulsing;
        if (duration == 0) duration = animationDuration;
        this.mAnimationDuration = duration;
        if (mEdgeAnimator != null) {
            mEdgeAnimator.setDurations((int) animationDuration, (int) pulsingDuration, mAnimationDuration);
        }
    }

    public void setStrokeWidth(float width) {
        this.mEdgeLightWidth = width;
        mEdgeAnimator.setStrokeWidth(mEdgeLightWidth);
    }

    public void setPulsing(boolean pulsing, int reason) {
        logD("setPulsing: " + pulsing + " reason: " + reason);
        try {
            if (pulsing) {
                this.mPulsing = true;
                // Use accent color if color mode is set to notification color
                // and pulse is not because of notification.
                if (mColorMode == ColorMode.NOTIFICATION && reason != PULSE_REASON_NOTIFICATION) {
                    setColor(mSettingsInterface ? ThemeUtils.getPrimaryColor(mContext) : OpUtils.getPrimaryColor(mContext));
                }
                logD("setPulsing: show()");
                show();
            } else {
                this.mPulsing = false;
                hide();
            }
        } catch (Throwable t) {
            logD("setPulsing: error: " + Log.getStackTraceString(t));
        }
    }

    private void logD(String msg) {
        if (mSettingsInterface) {
            Log.w("EdgeLightView", msg);
        } else {
            XposedBridge.log("EdgeLightView: " + msg);
        }

    }

    public void setNotificationColor(int newColor) {
        mPreviousNotificationColor = mNotificationColor;
        mNotificationColor = newColor;
        setColorMode(mColorMode);
        invalidate();
    }

}