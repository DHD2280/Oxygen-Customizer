package it.dhd.oxygencustomizer.xposed.views.pulse;

/*
 * Modified from crDroid
 * https://github.com/crdroidandroid/android_frameworks_base/blob/9ce156fa20d3f7222b8ec63c281b44d9899abc48/packages/SystemUI/src/com/android/systemui/pulse
 * Copyright (C) 2016-2022 crDroid Android Project
 *
 * @author: Randall Rushing <randall.rushing@gmail.com>
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
 * Control state of visualizer link, stream validation, and the flow
 * of data to listener
 *
 */

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.audiofx.Visualizer;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.concurrent.Executor;

public class VisualizerStreamHandler {
    public interface Listener {
        public void onStreamAnalyzed(boolean isValid);

        public void onFFTUpdate(byte[] bytes);

        public void onWaveFormUpdate(byte[] bytes);
    }

    protected static final String TAG = VisualizerStreamHandler.class.getSimpleName();
    protected static final boolean ENABLE_WAVEFORM = true;

    protected static final int MSG_STREAM_VALID = 55;
    protected static final int MSG_STREAM_INVALID = 56;
    // we have 6 seconds to get three consecutive valid frames
    protected static final int VALIDATION_TIME_MILLIS = 6000;
    protected static final int VALID_BYTES_THRESHOLD = 3;

    protected Visualizer mVisualizer;

    // manage stream validation
    protected int mConsecutiveFrames;
    protected boolean mIsValidated;
    protected boolean mIsAnalyzed;
    protected boolean mIsPrepared;
    protected boolean mIsPaused;

    protected Context mContext;
    protected PulseControllerImpl mController;
    protected Listener mListener;

    private final Executor mUiBgExecutor;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message m) {
            switch (m.what) {
                case MSG_STREAM_VALID -> {
                    mIsAnalyzed = true;
                    mIsValidated = true;
                    mIsPrepared = false;
                    mListener.onStreamAnalyzed(true);
                }
                case MSG_STREAM_INVALID -> {
                    mIsAnalyzed = true;
                    mIsValidated = false;
                    mIsPrepared = false;
                    mListener.onStreamAnalyzed(false);
                }
            }
        }
    };

    public VisualizerStreamHandler(Context context, PulseControllerImpl controller,
                                   VisualizerStreamHandler.Listener listener, Executor backgroundExecutor) {
        Log.d(TAG, "VisualizerStreamHandler()");
        mContext = context;
        mController = controller;
        mListener = listener;
        mUiBgExecutor = backgroundExecutor;
    }

    /**
     * Links the visualizer to a player
     */
    public final void link() {
        Log.d(TAG, "VisualizerStreamHandler link()");
        mUiBgExecutor.execute(() -> {
            pause();
            resetAnalyzer();

            if (mVisualizer == null) {
                try {
                    mVisualizer = new Visualizer(0);
                } catch (Exception e) {
                    return;
                }
                mVisualizer.setEnabled(false);
                mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);

                Visualizer.OnDataCaptureListener captureListener = new Visualizer.OnDataCaptureListener() {
                    @Override
                    public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes,
                                                      int samplingRate) {
                        if (ENABLE_WAVEFORM) {
                            analyze(bytes);
                            if (isValidStream() && !mIsPaused) {
                                mListener.onWaveFormUpdate(bytes);
                            }
                        }
                    }

                    @Override
                    public void onFftDataCapture(Visualizer visualizer, byte[] bytes,
                                                 int samplingRate) {
                        analyze(bytes);
                        if (isValidStream() && !mIsPaused) {
                            mListener.onFFTUpdate(bytes);
                        }
                    }
                };

                mVisualizer.setDataCaptureListener(captureListener,
                        (int) (Visualizer.getMaxCaptureRate() * 0.75), ENABLE_WAVEFORM, true);

            }
            mVisualizer.setEnabled(true);
        });
    }

    public final void unlink() {
        if (mVisualizer != null) {
            pause();
            mVisualizer.setEnabled(false);
            mVisualizer.release();
            mVisualizer = null;
            resetAnalyzer();
        }
    }

    public boolean isValidStream() {
        return mIsAnalyzed && mIsValidated;
    }

    public void resetAnalyzer() {
        mIsAnalyzed = false;
        mIsValidated = false;
        mIsPrepared = false;
        mConsecutiveFrames = 0;
    }

    public void pause() {
        mIsPaused = true;
    }

    public void resume() {
        mIsPaused = false;
    }

    private void analyze(byte[] data) {
        Log.d(TAG, "analyze: ");
        if (mIsAnalyzed) {
            return;
        }

        Log.d(TAG, "analyze: mIsAnalyzed = false");

        if (!mIsPrepared) {
            Log.d(TAG, "analyze: !mIsPrepared send MSG_STREAM_INVALID");
            mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STREAM_INVALID),
                    VALIDATION_TIME_MILLIS);
            mIsPrepared = true;
        }

        if (isDataEmpty(data)) {
            mConsecutiveFrames = 0;
        } else {
            mConsecutiveFrames++;
        }

        Log.d(TAG, "analyze: mConsecutiveFrames = " + mConsecutiveFrames + " VALID_BYTES_THRESHOLD = " + VALID_BYTES_THRESHOLD);

        if (mConsecutiveFrames == VALID_BYTES_THRESHOLD) {
            mIsPaused = true;
            mHandler.removeMessages(MSG_STREAM_INVALID);
            mHandler.sendEmptyMessage(MSG_STREAM_VALID);
        }
    }

    private boolean isDataEmpty(byte[] data) {
        for (byte datum : data) {
            if (datum != 0) {
                return false;
            }
        }
        return true;
    }
}