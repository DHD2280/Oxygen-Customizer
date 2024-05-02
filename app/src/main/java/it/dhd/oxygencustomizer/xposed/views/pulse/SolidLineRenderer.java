package it.dhd.oxygencustomizer.xposed.views.pulse;

/*
 * Modified from crDroid
 * https://github.com/crdroidandroid/android_frameworks_base/blob/9ce156fa20d3f7222b8ec63c281b44d9899abc48/packages/SystemUI/src/com/android/systemui/pulse
 * Copyright (C) 2016-2022 crDroid Android Project
 * Copyright (C) 2015 The CyanogenMod Project
 *
 * @author: Randall Rushing <randall.rushing@gmail.com>
 *
 * Contributions from The CyanogenMod Project
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
 * limitations under the License.
 *
 */

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;

import androidx.core.graphics.ColorUtils;

import it.dhd.oxygencustomizer.xposed.views.PulseView;

public class SolidLineRenderer extends Renderer {

    private static final int GRAVITY_BOTTOM = 0;
    private static final int GRAVITY_TOP = 1;
    private static final int GRAVITY_CENTER = 2;
    @SuppressLint("StaticFieldLeak")
    private static SolidLineRenderer instance = null;
    private Paint mPaint;
    private int mUnitsOpacity;
    private int mColor = Color.WHITE;
    private ValueAnimator[] mValueAnimators;
    private FFTAverage[] mFFTAverage;
    private float[] mFFTPoints;

    private byte rfk, ifk;
    private int dbValue;
    private float magnitude;
    private int mDbFuzzFactor;
    private boolean mVertical;
    private boolean mLeftInLandscape = false;
    private int mWidth, mHeight, mUnits, mGravity;

    private boolean mSmoothingEnabled;
    private boolean mRounded;
    private boolean mCenterMirrored;
    private boolean mVerticalMirror;


    public SolidLineRenderer(Context context, Handler handler, PulseView view,
                             PulseControllerImpl controller, ColorController colorController) {
        super(context, handler, view, colorController);
        instance = this;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);

        mUnits = 32;
        mDbFuzzFactor = 5;
        mSmoothingEnabled = false;
        mRounded = false;

        mFFTPoints = new float[mUnits * 4];
        onSizeChanged(0, 0, 0, 0);

        mFFTAverage = null;

        mUnitsOpacity= 200;

        mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mUnitsOpacity));
        loadValueAnimators();
    }

    public static boolean hasInstance() {
        return (instance != null);
    }

    public static SolidLineRenderer getInstance() {
        return instance;
    }

    @Override
    public void setLeftInLandscape(boolean leftInLandscape) {
        if (mLeftInLandscape != leftInLandscape) {
            mLeftInLandscape = leftInLandscape;
            onSizeChanged(0, 0, 0, 0);
        }
    }

    private void loadValueAnimators() {
        if (mValueAnimators != null) {
            stopAnimation(mValueAnimators.length);
        }
        mValueAnimators = new ValueAnimator[mUnits];
        final boolean isVertical = mVertical;
        for (int i = 0; i < mUnits; i++) {
            final int j;
            if (isVertical) {
                j = i * 4;
            } else {
                j = i * 4 + 1;
            }
            mValueAnimators[i] = new ValueAnimator();
            mValueAnimators[i].setDuration(128);
            mValueAnimators[i].addUpdateListener(animation -> {
                mFFTPoints[j] = (float) animation.getAnimatedValue();
                postInvalidate();
            });
        }
    }

    private void stopAnimation(int index) {
        if (mValueAnimators == null) return;
        for (int i = 0; i < index; i++) {
            // prevent onAnimationUpdate existing listeners (by stopping them) to call
            // a wrong mFFTPoints index after mUnits gets updated by the user
            mValueAnimators[i].removeAllUpdateListeners();
            mValueAnimators[i].cancel();
        }
    }

    private void setPortraitPoints() {
        float units = (float) mUnits;
        float barUnit = mWidth / units;
        float barWidth = barUnit * 8f / 9f;
        float startPoint = mHeight;
        if (mGravity == GRAVITY_BOTTOM) {
            startPoint = (float) mHeight;
        } else if (mGravity == GRAVITY_TOP) {
            startPoint = 0f;
        } else if (mGravity == GRAVITY_CENTER) {
            startPoint = (float) mHeight / 2f;
        }
        barUnit = barWidth + (barUnit - barWidth) * units / (units - 1);
        mPaint.setStrokeWidth(barWidth);
        mPaint.setStrokeCap(mRounded ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        for (int i = 0; i < mUnits; i++) {
            mFFTPoints[i * 4] = mFFTPoints[i * 4 + 2] = i * barUnit + (barWidth / 2);
            mFFTPoints[i * 4 + 1] = startPoint;
            mFFTPoints[i * 4 + 3] = startPoint;
        }
    }

    private void setVerticalPoints() {
        float units = (float) mUnits;
        float barUnit = mHeight / units;
        float barHeight = barUnit * 8f / 9f;
        float startPoint = mWidth;
        if (mGravity == GRAVITY_BOTTOM) {
            startPoint = (float) mWidth;
        } else if (mGravity == GRAVITY_TOP) {
            startPoint = 0f;
        } else if (mGravity == GRAVITY_CENTER) {
            startPoint = (float) mWidth / 2f;
        }
        barUnit = barHeight + (barUnit - barHeight) * units / (units - 1);
        mPaint.setStrokeWidth(barHeight);
        mPaint.setStrokeCap(mRounded ? Paint.Cap.ROUND : Paint.Cap.BUTT);
        for (int i = 0; i < mUnits; i++) {
            mFFTPoints[i * 4 + 1] = mFFTPoints[i * 4 + 3] = i * barUnit + (barHeight / 2);
            mFFTPoints[i * 4] = mLeftInLandscape ? 0 : startPoint;
            mFFTPoints[i * 4 + 2] = mLeftInLandscape ? 0 : startPoint;
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mView.getWidth() > 0 && mView.getHeight() > 0) {
            mWidth = mView.getWidth();
            mHeight = mView.getHeight();
            mVertical = false;
            loadValueAnimators();
            if (mVertical) {
                setVerticalPoints();
            } else {
                setPortraitPoints();
            }
        }
    }

    @Override
    public void onStreamAnalyzed(boolean isValid) {
        mIsValidStream = isValid;
        if (isValid) {
            onSizeChanged(0, 0, 0, 0);
            mColorController.startLavaLamp();
        }
    }

    @Override
    public void onFFTUpdate(byte[] fft) {
        int fudgeFactor = mKeyguardShowing ? mDbFuzzFactor * 4 : mDbFuzzFactor;
        for (int i = 0; i < mUnits; i++) {
            if (mValueAnimators[i] == null) continue;
            mValueAnimators[i].cancel();
            rfk = fft[i * 2 + 2];
            ifk = fft[i * 2 + 3];
            magnitude = rfk * rfk + ifk * ifk;
            dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
            if (mSmoothingEnabled) {
                if (mFFTAverage == null) {
                    setupFFTAverage();
                }
                dbValue = mFFTAverage[i].average(dbValue);
            }
            if (mVertical) {
                if (mLeftInLandscape || mGravity == GRAVITY_TOP) {
                    mValueAnimators[i].setFloatValues(mFFTPoints[i * 4],
                            dbValue * fudgeFactor);
                } else if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                    mValueAnimators[i].setFloatValues(mFFTPoints[i * 4],
                            mFFTPoints[2] - (dbValue * fudgeFactor));
                }
            } else {
                if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                    mValueAnimators[i].setFloatValues(mFFTPoints[i * 4 + 1],
                            mFFTPoints[3] - (dbValue * fudgeFactor));
                } else if (mGravity == GRAVITY_TOP) {
                    mValueAnimators[i].setFloatValues(mFFTPoints[i * 4 + 1],
                            mFFTPoints[3] + (dbValue * fudgeFactor));
                }
            }
            mValueAnimators[i].start();
        }
        if (mCenterMirrored) {
            for (int i = 0; i < mUnits; i++) {
                int j = mUnits - (i + 1);
                if (mValueAnimators[i] == null) continue;
                mValueAnimators[i].cancel();
                byte rfk = fft[j * 2 + 2];
                byte ifk = fft[j * 2 + 3];
                float magnitude = rfk * rfk + ifk * ifk;
                int dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
                if (mSmoothingEnabled) {
                    if (mFFTAverage == null) {
                        setupFFTAverage();
                    }
                    dbValue = mFFTAverage[i].average(dbValue);
                }
                if (mVertical) {
                    if (mLeftInLandscape || mGravity == GRAVITY_TOP) {
                        mValueAnimators[i].setFloatValues(mFFTPoints[i * 4],
                                dbValue * fudgeFactor);
                    } else if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                        mValueAnimators[i].setFloatValues(mFFTPoints[i * 4],
                                mFFTPoints[2] - (dbValue * fudgeFactor));
                    }
                } else {
                    if (mGravity == GRAVITY_BOTTOM || mGravity == GRAVITY_CENTER) {
                        mValueAnimators[i].setFloatValues(mFFTPoints[i * 4 + 1],
                                mFFTPoints[3] - (dbValue * fudgeFactor));
                    } else if (mGravity == GRAVITY_TOP) {
                        mValueAnimators[i].setFloatValues(mFFTPoints[i * 4 + 1],
                                mFFTPoints[3] + (dbValue * fudgeFactor));
                    }
                }
                mValueAnimators[i].start();
            }
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLines(mFFTPoints, mPaint);
        if (mVerticalMirror) {
            if (mVertical) {
                canvas.scale(-1, 1, mWidth / 2f, mHeight / 2f);
            } else {
                canvas.scale(1, -1, mWidth / 2f, mHeight / 2f);
            }
            canvas.drawLines(mFFTPoints, mPaint);
        }
    }

    @Override
    public void destroy() {
        mColorController.stopLavaLamp();
    }

    @Override
    public void onVisualizerLinkChanged(boolean linked) {
        if (!linked) {
            mColorController.stopLavaLamp();
        }
    }

    @Override
    public void onUpdateColor(int color) {
        mColor = color;
        mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mUnitsOpacity));
    }

    public void updateSettings(int dbFuzzFactor,
                               boolean smoothingEnabled, boolean rounded, int units, int unitsOpacity,
                               boolean centerMirrored, boolean verticalMirror, int gravity) {

        mDbFuzzFactor = dbFuzzFactor;
        mSmoothingEnabled = smoothingEnabled;
        mRounded = rounded;
        mCenterMirrored = centerMirrored;
        mVerticalMirror = verticalMirror;
        mGravity = gravity;

        if (units != mUnits) {
            stopAnimation(mUnits);
            mUnits = units;
            mFFTPoints = new float[mUnits * 4];
            if (mSmoothingEnabled) {
                setupFFTAverage();
            }
            onSizeChanged(0, 0, 0, 0);
        }

        if (mSmoothingEnabled) {
            if (mFFTAverage == null) {
                setupFFTAverage();
            }
        } else {
            mFFTAverage = null;
        }

        mUnitsOpacity= unitsOpacity;

        mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mUnitsOpacity));

    }

    private void setupFFTAverage() {
        mFFTAverage = new FFTAverage[mUnits];
        for (int i = 0; i < mUnits; i++) {
            mFFTAverage[i] = new FFTAverage();
        }
    }
}
