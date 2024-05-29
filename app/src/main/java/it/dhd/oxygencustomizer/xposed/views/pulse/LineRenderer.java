package it.dhd.oxygencustomizer.xposed.views.pulse;

/*
 *
 * Copyright 2024, Luigi Conte DHD22800
 * *
 * Old school FFT renderer adapted from
 * @link https://github.com/felixpalmer/android-visualizer
 *
 */

import static de.robv.android.xposed.XposedBridge.log;
import static it.dhd.oxygencustomizer.xposed.views.pulse.PulseControllerImpl.DEBUG;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Handler;

import androidx.core.graphics.ColorUtils;

import it.dhd.oxygencustomizer.xposed.views.PulseView;

public class LineRenderer extends Renderer {
    //private static final int DEF_PAINT_ALPHA = (byte) 188;

    @SuppressLint("StaticFieldLeak")
    private static LineRenderer instance = null;
    private Paint mPaint;
    private Paint mFlashPaint;
    private boolean mShowFlash = true;
    private boolean mLeftInLandscape = false;
    private float[] mPoints;
    private Bitmap mCanvasBitmap;
    private Canvas mCanvas;
    private Matrix mMatrix;
    private int mWidth;
    private int mHeight;
    private float amplitude = 0;
    private boolean drawAmplitude = false;
    private int mColor = Color.WHITE;
    private int mFlashColor = Color.RED;
    private int mWaveOpacity = 200;

    public LineRenderer(Context context, Handler handler, PulseView view,
                        PulseControllerImpl controller, ColorController colorController) {
        super(context, handler, view, colorController);
        instance = this;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mWaveOpacity));

        mFlashPaint = new Paint();
        mFlashPaint.setAntiAlias(true);
        mFlashPaint.setColor(ColorUtils.setAlphaComponent(mFlashColor, mWaveOpacity));
        mMatrix = new Matrix();

        mPaint.setAntiAlias(true);
        onSizeChanged(0, 0, 0, 0);
    }

    public static boolean hasInstance() {
        return (instance != null);
    }

    public static LineRenderer getInstance() {
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
    public void onWaveFormUpdate(byte[] bytes) {
        if (mPoints == null || mPoints.length < bytes.length * 4) {
            mPoints = new float[bytes.length * 4];
        }
        // Calculate points for line
        for (int i = 0; i < bytes.length - 1; i++) {
            mPoints[i * 4] = mWidth * i / (bytes.length - 1);
            mPoints[i * 4 + 1] =  mHeight / 2
                    + ((byte) (bytes[i] + 128)) * (mHeight / 3) / 128;
            mPoints[i * 4 + 2] = mWidth * (i + 1) / (bytes.length - 1);
            mPoints[i * 4 + 3] = mHeight / 2
                    + ((byte) (bytes[i + 1] + 128)) * (mHeight / 3) / 128;
        }

        // Calc amplitude for this waveform
        float accumulator = 0;
        for (int i = 0; i < bytes.length - 1; i++) {
            accumulator += Math.abs(bytes[i]);
        }

        float amp = accumulator/(128 * bytes.length);

        if (mCanvas != null && mPoints != null) {
            if (amp > amplitude) {
                amplitude = amp;
                drawAmplitude = true;
                mCanvas.drawLines(mPoints, mFlashPaint);
            } else {
                amplitude *= 0.99F;
                drawAmplitude = false;
                mCanvas.drawLines(mPoints, mPaint);
            }
        }
        postInvalidate();
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mView.getWidth() > 0 && mView.getHeight() > 0) {
            mWidth = mView.getWidth();
            mHeight = mView.getHeight();
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
        mColor = color;
        mPaint.setColor(ColorUtils.setAlphaComponent(mColor, mWaveOpacity));
    }

    @Override
    public void draw(Canvas canvas) {
        if (DEBUG) log("Pulse LineRenderer draw " + drawAmplitude + " " + amplitude);
        if (mPoints == null) {
            return;
        }
        canvas.drawLines(mPoints, mPaint);
        if (drawAmplitude && mShowFlash) {
            canvas.drawLines(mPoints, mFlashPaint);
        }
    }


    public void updateSettings(boolean showFlash,
                               float stroke,
                               int waveOpacity) {
        log("Pulse LineRenderer updateSettings " + showFlash + " " + stroke + " " + waveOpacity);
        mShowFlash = showFlash;
        mPaint.setStrokeWidth(stroke);
        mFlashPaint.setStrokeWidth(stroke);
        mWaveOpacity = waveOpacity;
    }

}
