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
    private boolean mKeyguardShowing = true; // MUST initialize as true
    private boolean isFullyCollapsed = true; // MUST initialize as true
    private boolean mDozing = false;

    private boolean mEnabled = true;
    private boolean mWeatherEnabled = false;
    private boolean mNotificationEnabled = false;
    private int mLeftMargin, mRightMargin, mBottomMargin;

    private NowBarController(Context context) {
        this.mContext = context;
        instance = this;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static NowBarController getInstance(Context context) {
        if (instance != null) return instance;
        return new NowBarController(context);
    }

    public static NowBarController getInstance() {
        return instance;
    }

    public void setNowBarHolder(NowBarHolder view) {
        logD("setNowBarHolder");
        mView = view;
        mView.updateMargins(mLeftMargin, mRightMargin, mBottomMargin);
        mView.setWeatherEnabled(mWeatherEnabled);
        mView.setNotificationEnabled(mNotificationEnabled);
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
//        logD("setNowBarEnabled: " + enabled);
        this.mEnabled = enabled;
        updateVisibility();
    }

    public void setFullyCollapsed(boolean fullyCollapsed) {
//        logD("setFullyCollapsed: " + fullyCollapsed);
        if (isFullyCollapsed == fullyCollapsed) return;
        isFullyCollapsed = fullyCollapsed;
        updateVisibility();
    }

    public void setStatusBarState(int state) {
//        logD("setStatusBarState: " + state);
        if (mStatusBarState == state) return;
        mStatusBarState = state;
        updateVisibility();
    }

    public void setKeyguardShowing(boolean keyguardShowing) {
//        logD("setKeyguardShowing: " + keyguardShowing);
        if (mKeyguardShowing == keyguardShowing) return;
        mKeyguardShowing = keyguardShowing;
        updateVisibility();
    }

    public void setDozing(boolean dozing) {
//        logD("setDozing: " + dozing);
        if (mDozing == dozing) return;
        mDozing = dozing;
        updateVisibility();
    }

    private void updateVisibility() {
        logD("updateVisibility");
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

    public void setNowBarNotificationEnabled(boolean notificationEnabled) {
        mNotificationEnabled = notificationEnabled;
        if (mView != null) mView.setNotificationEnabled(notificationEnabled);
    }

    public void updateMargins(int left, int right, int bottom) {
        mLeftMargin = left;
        mRightMargin = right;
        mBottomMargin = bottom;
        if (mView != null) mView.updateMargins(left, right, bottom);
    }

    public void updateMusic(
            boolean extendedPlayer, int backgroundMode,
            int playerMode, boolean showClock,
            int datePosition, String dateFormat, float textScaling, boolean customFont, int topMargin) {
        if (mView != null) {
            mView.updateMusic(
                    extendedPlayer, backgroundMode, playerMode, showClock,
                    datePosition, dateFormat, textScaling, customFont, topMargin
                    );
        }
    }

    public void updateNotification(boolean ignoreSecurity,
                                   boolean customColor, boolean useAppIcons,
                                   int backgroundColor, int textColor1, int textColor2, int iconTintColor) {
        if (mView != null) {
            mView.updateNotification(ignoreSecurity,
                    customColor, useAppIcons,
                    backgroundColor, textColor1, textColor2, iconTintColor);
        }
    }

    public void updateBattery(int chargingIconStyle,
                              boolean customColors, int color1, int color2, int color3, int color4,
                              boolean indicateFast, int fastColor, boolean indicatePowerSave, int powerSaveColor,
                              int textColor) {
        if (mView != null) {
            mView.updateBattery(
                    chargingIconStyle,
                    customColors, color1, color2, color3, color4,
                    indicateFast, fastColor, indicatePowerSave, powerSaveColor,
                    textColor
            );
        }
    }

    public void updateWeather(boolean customColors, int textColor, int backgroundColor) {
        if (mView != null) {
            mView.updateWeather(customColors, textColor, backgroundColor);
        }
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
