package it.dhd.oxygencustomizer.xposed.views.pulse;

/*
 * Modified from crDroid
 * https://github.com/crdroidandroid/android_frameworks_base/blob/9ce156fa20d3f7222b8ec63c281b44d9899abc48/packages/SystemUI/src/com/android/systemui/pulse
 *
 * Copyright 2011, Felix Palmer
 * Copyright (C) 2014 The TeamEos Project
 * Copyright (C) 2016-2022 crDroid Android Project
 *
 * AOSP Navigation implementation by
 * @author: Randall Rushing <randall.rushing@gmail.com>
 *
 * Licensed under the MIT license:
 * http://creativecommons.org/licenses/MIT/
 *
 * Old school FFT renderer adapted from
 * @link https://github.com/felixpalmer/android-visualizer
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.util.TypedValue;

import it.dhd.oxygencustomizer.xposed.views.PulseView;

public class FadingBlockRenderer extends Renderer {
    //private static final int DEF_PAINT_ALPHA = (byte) 188;

    @SuppressLint("StaticFieldLeak")
    private static FadingBlockRenderer instance = null;
    private static final int DBFUZZ = 2;
    private byte[] mFFTBytes;
    private static final int GRAVITY_BOTTOM = 0;
    private static final int GRAVITY_TOP = 1;
    private static final int GRAVITY_CENTER = 2;
    private Paint mPaint;
    private Paint mFadePaint;
    private boolean mVertical;
    private boolean mLeftInLandscape = false;
    private FFTAverage[] mFFTAverage;
    private float[] mFFTPoints;
    private byte rfk, ifk;
    private int dbValue;
    private float magnitude;
    private int mDivisions;
    private int mDbFuzzFactor;
    private int mPathEffect1;
    private int mPathEffect2;
    private Bitmap mCanvasBitmap;
    private Canvas mCanvas;
    private Matrix mMatrix;
    private int mWidth;
    private int mHeight;
    private boolean mSmoothingEnabled;
    private int mGravity = GRAVITY_BOTTOM;
    private boolean mCenterMirrored = false;
    private boolean mVerticalMirror = false;

    public FadingBlockRenderer(Context context, Handler handler, PulseView view,
                               PulseControllerImpl controller, ColorController colorController) {
        super(context, handler, view, colorController);
        instance = this;
        mPaint = new Paint();
        mFadePaint = new Paint();
        mFadePaint.setColor(Color.argb(200, 255, 255, 255));
        mFadePaint.setXfermode(new PorterDuffXfermode(Mode.MULTIPLY));
        mMatrix = new Matrix();
        final Resources res = mContext.getResources();

        int emptyBlock = 1;
        int customDimen = 14;
        int numDivision = 16;
        int fudgeFactor = 5;
        int filledBlock = 4;

        mPathEffect1 = getLimitedDimenValue(filledBlock, 4, 8, res);
        mPathEffect2 = getLimitedDimenValue(emptyBlock, 0, 4, res);
        mPaint.setPathEffect(null);
        mPaint.setPathEffect(new android.graphics.DashPathEffect(new float[] {
                mPathEffect1,
                mPathEffect2
        }, 0));
        mPaint.setStrokeWidth(getLimitedDimenValue(customDimen, 1, 30, res));
        mDivisions = validateDivision(numDivision);
        mDbFuzzFactor = Math.max(2, Math.min(6, fudgeFactor));

        mSmoothingEnabled = false;
        mPaint.setAntiAlias(true);
        onSizeChanged(0, 0, 0, 0);
    }

    public static boolean hasInstance() {
        return (instance != null);
    }

    public static FadingBlockRenderer getInstance() {
        return instance;
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
    public void onFFTUpdate(byte[] bytes) {
        if (bytes == null || mDivisions <= 0) return;
        int fudgeFactor = mKeyguardShowing ? mDbFuzzFactor * 4 : mDbFuzzFactor;
        mFFTBytes = bytes;
        if (mFFTPoints == null || mFFTPoints.length < mFFTBytes.length * 4) {
            mFFTPoints = new float[mFFTBytes.length * 4];
        }
        int divisionLength = mFFTBytes.length / mDivisions;
        if (divisionLength < 2) return;
        if (mSmoothingEnabled) {
            if (mFFTAverage == null || mFFTAverage.length != divisionLength) {
                setupFFTAverage(divisionLength);
            }
        } else {
            mFFTAverage = null;
        }
        int i = 0;
        for (; i < (mCenterMirrored ? (divisionLength / 2) : divisionLength); i++) {
            if (mVertical) {
                mFFTPoints[i * 4 + 1] = i * 4 * mDivisions;
                mFFTPoints[i * 4 + 3] = i * 4 * mDivisions;
            } else {
                mFFTPoints[i * 4] = i * 4 * mDivisions;
                mFFTPoints[i * 4 + 2] = i * 4 * mDivisions;
            }
            if ((mDivisions * i + 1) < mFFTBytes.length) {
                byte rfk = mFFTBytes[mDivisions * i];
                byte ifk = mFFTBytes[mDivisions * i + 1];
                float magnitude = (rfk * rfk + ifk * ifk);
                int dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
                if (mSmoothingEnabled) {
                    dbValue = mFFTAverage[i].average(dbValue);
                }
                if (mVertical) {
                    int correctionFactor = 1;
                    float startPoint = mWidth;
                    if (mGravity == GRAVITY_BOTTOM) {
                        startPoint = (float) mWidth;
                    } else if (mGravity == GRAVITY_TOP) {
                        startPoint = 0f;
                        correctionFactor*=-1;
                    } else if (mGravity == GRAVITY_CENTER) {
                        startPoint = (float) mWidth / 2f;
                    }
                    mFFTPoints[i * 4] = mLeftInLandscape ? 0 : startPoint;
                    mFFTPoints[i * 4 + 2] = mLeftInLandscape ? (dbValue * fudgeFactor + DBFUZZ)
                            : (startPoint - correctionFactor * (dbValue * fudgeFactor + DBFUZZ));
                } else {
                    int correctionFactor = 1;
                    float startPoint = mHeight;
                    if (mGravity == GRAVITY_BOTTOM) {
                        startPoint = (float) mHeight;
                    } else if (mGravity == GRAVITY_TOP) {
                        startPoint = 0f;
                        correctionFactor*=-1;
                    } else if (mGravity == GRAVITY_CENTER) {
                        startPoint = (float) mHeight / 2f;
                    }
                    mFFTPoints[i * 4 + 1] = startPoint;
                    mFFTPoints[i * 4 + 3] = startPoint - correctionFactor * (dbValue * fudgeFactor + DBFUZZ);
                }
            }
            if (mCenterMirrored) {
                for (; i < divisionLength; i++) {
                    int j = divisionLength - (i + 1);
                    if (mVertical) {
                        mFFTPoints[i * 4 + 1] = i * 4 * mDivisions;
                        mFFTPoints[i * 4 + 3] = i * 4 * mDivisions;
                    } else {
                        mFFTPoints[i * 4] = i * 4 * mDivisions;
                        mFFTPoints[i * 4 + 2] = i * 4 * mDivisions;
                    }
                    byte rfk = bytes[mDivisions * i];
                    byte ifk = bytes[mDivisions * i + 1];
                    float magnitude = (rfk * rfk + ifk * ifk);
                    int dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
                    if (mSmoothingEnabled) {
                        dbValue = mFFTAverage[i].average(dbValue);
                    }
                    if (mVertical) {
                        int correctionFactor = 1;
                        float startPoint = mWidth;
                        if (mGravity == GRAVITY_BOTTOM) {
                            startPoint = (float) mWidth;
                        } else if (mGravity == GRAVITY_TOP) {
                            startPoint = 0f;
                            correctionFactor*=-1;
                        } else if (mGravity == GRAVITY_CENTER) {
                            startPoint = (float) mWidth / 2f;
                        }
                        mFFTPoints[i * 4] = mLeftInLandscape ? 0 : startPoint;
                        mFFTPoints[i * 4 + 2] = mLeftInLandscape ? (dbValue * fudgeFactor + DBFUZZ)
                                : (startPoint - correctionFactor * (dbValue * fudgeFactor + DBFUZZ));
                    } else {
                        int correctionFactor = 1;
                        float startPoint = mHeight;
                        if (mGravity == GRAVITY_BOTTOM) {
                            startPoint = (float) mHeight;
                        } else if (mGravity == GRAVITY_TOP) {
                            startPoint = 0f;
                            correctionFactor*=-1;
                        } else if (mGravity == GRAVITY_CENTER) {
                            startPoint = (float) mHeight / 2f;
                        }
                        mFFTPoints[i * 4 + 1] = startPoint;
                        mFFTPoints[i * 4 + 3] = startPoint - correctionFactor * (dbValue * fudgeFactor + DBFUZZ);
                    }
                }
            }
        }
        if (mCanvas != null && mFFTPoints != null) {
            mCanvas.drawLines(mFFTPoints, mPaint);
            mCanvas.drawPaint(mFadePaint);
        }
        postInvalidate();
    }

    private void setupFFTAverage(int size) {
        mFFTAverage = new FFTAverage[size];
        for (int i = 0; i < size; i++) {
            mFFTAverage[i] = new FFTAverage();
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mView.getWidth() > 0 && mView.getHeight() > 0) {
            mWidth = mView.getWidth();
            mHeight = mView.getHeight();
            mVertical = false;
            mCanvasBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
            mCanvas = new Canvas(mCanvasBitmap);
        }
    }

    @Override
    public void setLeftInLandscape(boolean leftInLandscape) {
        if (mLeftInLandscape != leftInLandscape) {
            mLeftInLandscape = leftInLandscape;
            onSizeChanged(0, 0, 0, 0);
        }
    }

    @Override
    public void destroy() {
        mColorController.stopLavaLamp();
        mCanvasBitmap = null;
    }

    @Override
    public void onVisualizerLinkChanged(boolean linked) {
        if (!linked) {
            mColorController.stopLavaLamp();
        }
    }

    @Override
    public void onUpdateColor(int color) {
        mPaint.setColor(color);
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(mCanvasBitmap, mMatrix, null);
        if (mVerticalMirror) {
            if (mVertical) {
                canvas.scale(-1, 1, mWidth / 2f, mHeight / 2f);
            } else {
                canvas.scale(1, -1, mWidth / 2f, mHeight / 2f);
            }
            canvas.drawBitmap(mCanvasBitmap, mMatrix, null);
        }
    }

    /*private int applyPaintAlphaToColor(int color) {
        int opaqueColor = Color.rgb(Color.red(color),
                Color.green(color), Color.blue(color));
        return (DEF_PAINT_ALPHA << 24) | (opaqueColor & 0x00ffffff);
    }*/


    public void updateSettings(int emptyBlock,
                               int customDimen,
                               int numDivision,
                               int fudgeFactor,
                               int filledBlock,
                               boolean centerMirrored,
                               boolean verticalMirror,
                               int gravity) {
        final Resources res = mContext.getResources();
        mPathEffect1 = getLimitedDimenValue(filledBlock, 4, 8, res);
        mPathEffect2 = getLimitedDimenValue(emptyBlock, 0, 4, res);
        mPaint.setPathEffect(null);
        mPaint.setPathEffect(new android.graphics.DashPathEffect(new float[] {
                mPathEffect1,
                mPathEffect2
        }, 0));
        mPaint.setStrokeWidth(getLimitedDimenValue(customDimen, 1, 30, res));
        mDivisions = validateDivision(numDivision);
        mDbFuzzFactor = Math.max(2, Math.min(6, fudgeFactor));
        mCenterMirrored = centerMirrored;
        mVerticalMirror = verticalMirror;
        mGravity = gravity;
    }

    public void updateSmoothingEnabled(boolean enabled) {
        mSmoothingEnabled = enabled;
    }


    private static int getLimitedDimenValue(int val, int min, int max, Resources res) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                Math.max(min, Math.min(max, val)), res.getDisplayMetrics());
    }

    private static int validateDivision(int val) {
        // if a bad value was passed from settings (not divisible by 2)
        // reset to default value of 16. Validate range.
        if (val % 2 != 0) {
            val = 16;
        }
        return Math.max(2, Math.min(44, val));
    }
}
