package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.view.View;

public class WidgetTicker {

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final View mView;
    private final long mInterval;
    private final Runnable mTask;
    private boolean mRunning = false;

    private final Runnable mTick = new Runnable() {
        @Override
        public void run() {
            if (!mRunning) return;
            if (mView.isShown() && isInteractive()) mTask.run();
            mHandler.postDelayed(this, mInterval);
        }
    };

    public WidgetTicker(View view, long intervalMs, Runnable task) {
        mView = view;
        mInterval = intervalMs;
        mTask = task;
    }

    private boolean isInteractive() {
        PowerManager power = mView.getContext().getSystemService(PowerManager.class);
        return power == null || power.isInteractive();
    }

    public void start() {
        stop();
        mRunning = true;
        mHandler.post(mTick);
    }

    public void stop() {
        mRunning = false;
        mHandler.removeCallbacks(mTick);
    }
}
