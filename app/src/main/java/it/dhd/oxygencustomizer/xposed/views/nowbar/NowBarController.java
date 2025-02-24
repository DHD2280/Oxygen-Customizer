package it.dhd.oxygencustomizer.xposed.views.nowbar;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;

public class NowBarController {

    private final boolean DEBUG = BuildConfig.DEBUG;

    @SuppressLint("StaticFieldLeak")
    private static NowBarController instance;

    private final Context mContext;
    @SuppressLint("StaticFieldLeak")
    private static NowBarHolder mView = null;
    private int mStatusBarState = -1;
    private boolean mKeyguardShowing = false;
    private boolean isFullyCollapsed = false;
    private boolean mDozing = false;

    private boolean mEnabled = true;
    private boolean mWeatherEnabled = false;
    private int mLeftMargin, mRightMargin, mBottomMargin;

    private NowBarController(Context context) {
        this.mContext = context;
    }

    public static synchronized NowBarController getInstance(Context context) {
        if (instance == null) {
            instance = new NowBarController(context);
        }
        return instance;
    }

    public void setNowBarHolder(NowBarHolder view) {
        logD("setNowBarHolder");
        mView = view;
        mView.updateMargins(mLeftMargin, mRightMargin, mBottomMargin);
        mView.setWeatherEnabled(mWeatherEnabled);
        updateVisibility();
    }

    public void show() {
        logD("show - mView null? " + (mView == null) + " - mEnabled: " + mEnabled);
        if (mView == null || !mEnabled) return;
        mView.setVisibility(View.VISIBLE);
    }

    public void hide() {
        if (mView != null) {
            mView.setVisibility(View.GONE);
        }
    }

    public void setNowBarEnabled(boolean enabled) {
        logD("setNowBarEnabled: " + enabled);
        this.mEnabled = enabled;
        updateVisibility();
    }

    public void setFullyCollapsed(boolean fullyCollapsed) {
        logD("setFullyCollapsed: " + fullyCollapsed);
        isFullyCollapsed = fullyCollapsed;
        updateVisibility();
    }

    public void setStatusBarState(int state) {
        logD("setStatusBarState: " + state);
        mStatusBarState = state;
        updateVisibility();
    }

    public void setKeyguardShowing(boolean keyguardShowing) {
        logD("setKeyguardShowing: " + keyguardShowing);
        mKeyguardShowing = keyguardShowing;
        updateVisibility();
    }

    public void setDozing(boolean dozing) {
        logD("setDozing: " + dozing);
        mDozing = dozing;
        updateVisibility();
    }

    private void updateVisibility() {
        if (mView == null) return;
        if (!mEnabled) {
            mView.setVisibility(View.GONE);
            return;
        }
        if (isFullyCollapsed && (mStatusBarState == 1 || mKeyguardShowing) && !mDozing) {
            mView.setVisibility(View.VISIBLE);
        } else {
            mView.setVisibility(View.GONE);
        }
    }

    public void setNowBarWeatherEnabled(boolean weatherEnabled) {
        mWeatherEnabled = weatherEnabled;
        if (mView != null) mView.setWeatherEnabled(weatherEnabled);
    }

    public void updateMargins(int left, int right, int bottom) {
        mLeftMargin = left;
        mRightMargin = right;
        mBottomMargin = bottom;
        if (mView != null) mView.updateMargins(left, right, bottom);
    }

    private void logD(String message) {
        if (!DEBUG) return;
        XposedBridge.log("NowBarController" + "\n" + message + "\n" +
                "mEnabled: " + mEnabled + "\n" +
                "mWeatherEnabled: " + mWeatherEnabled + "\n" +
                "mLeftMargin: " + mLeftMargin + "\n" +
                "mRightMargin: " + mRightMargin + "\n" +
                "mBottomMargin: " + mBottomMargin + "\n" +
                "mStatusBarState: " + mStatusBarState + "\n" +
                "isFullyCollapsed: " + isFullyCollapsed + "\n" +
                "mDozing: " + mDozing + "\n" +
                "mKeyguardShowing: " + mKeyguardShowing);
    }

}
