package it.dhd.oxygencustomizer.xposed.views;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.START;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.CLOCK_UI_STATE_AOD;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMarginsNoConvert;

import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;

import androidx.annotation.NonNull;

public class LockscreenView extends FrameLayout {

    private Context mContext;
    private LinearLayout mViewsContainer;
    private LinearLayout mClockContainer;
    private LinearLayout mWeatherContainer;
    private LinearLayout mWidgetsContainer;
    private View mClockView;
    private CurrentWeatherView mWeatherView;
    private LockscreenWidgetsView mWidgetsView;

    private boolean mLockscreenClockEnabled, mLockscreenWeatherEnabled, mLockscreenWidgetsEnabled;

    @SuppressLint("StaticFieldLeak")
    public static LockscreenView instance = null;

    public LockscreenView(@NonNull Context context) {
        super(context);

        instance = this;
        mContext = context;
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        createViews();
        setVisibility(View.GONE);
    }

    private void createViews() {

        mViewsContainer = new LinearLayout(mContext);
        mViewsContainer.setOrientation(LinearLayout.VERTICAL);
        mViewsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mClockContainer = new LinearLayout(mContext);
        mClockContainer.setOrientation(LinearLayout.VERTICAL);
        mClockContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        mWeatherContainer = new LinearLayout(mContext);
        mWeatherContainer.setOrientation(LinearLayout.VERTICAL);
        mWeatherContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mWeatherView = new CurrentWeatherView(mContext, LOCKSCREEN_WEATHER);
        mWeatherContainer.addView(mWeatherView);

        mWidgetsContainer = new LinearLayout(mContext);
        mWidgetsContainer.setOrientation(LinearLayout.VERTICAL);
        mWidgetsContainer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mWidgetsView = LockscreenWidgetsView.getInstance(mContext, null);
        mWidgetsContainer.addView(mWidgetsView);

        mViewsContainer.addView(mClockContainer);
        mViewsContainer.addView(mWeatherContainer);
        mViewsContainer.addView(mWidgetsContainer);

        addView(mViewsContainer);
    }

    public static LockscreenView getInstance() {
        if (instance != null) return instance;
        return null;
    }

    public static LockscreenView getInstance(Context context) {
        if (instance != null) return instance;
        return new LockscreenView(context);
    }

    public void setLockscreeClockEnabled(boolean enabled) {
        mLockscreenClockEnabled = enabled;
        updateVisibility();
    }

    public void setClockView(View clockView) {
        try {
            ((ViewGroup) clockView.getParent()).removeView(clockView);
        } catch (Throwable ignored) {}
        mClockView = clockView;
        mClockContainer.removeAllViews();
        mClockContainer.addView(mClockView);
    }

    public void updateClock(long time) {
        if (mClockContainer == null || mClockView == null) return;

        processAllTextClocks(mClockContainer);
    }

    private void processAllTextClocks(ViewGroup parent) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);

            if (child instanceof TextClock textClock) {
                CharSequence format12 = textClock.getFormat12Hour();
                CharSequence format24 = textClock.getFormat24Hour();
                textClock.setFormat12Hour(null);
                textClock.setFormat24Hour(null);
                textClock.setFormat12Hour(format12);
                textClock.setFormat24Hour(format24);
            } else if (child instanceof ViewGroup) {
                processAllTextClocks((ViewGroup) child);
            }
        }
    }

    public void updateClockMargins(int top) {
        setMarginsNoConvert(mClockContainer, mContext, 0, top, 0, 0);
    }

    public int getClockHeight() {
        return mClockContainer.getHeight();
    }

    public void setLockscreenWeatherEnabled(boolean enabled) {
        mLockscreenWeatherEnabled = enabled;
        updateVisibility();
    }

    public void updateWeatherMargins(int left, int top, int right, int bottom) {
        setMargins(mWeatherContainer, mContext,
                left, top, right, bottom);
    }

    public void setWeatherCentered(boolean centered) {
        mWeatherContainer.setGravity(centered ? CENTER_HORIZONTAL : START);
        ViewGroup weatherContainer = (ViewGroup) mWeatherContainer.getChildAt(0);
        for (int i = 0; i < weatherContainer.getChildCount(); i++) {
            View child = weatherContainer.getChildAt(i);
            if (child instanceof LinearLayout linearLayoutChild) {
                linearLayoutChild.setGravity(centered ? Gravity.CENTER_HORIZONTAL : (Gravity.START | Gravity.CENTER_VERTICAL));
            }
        }
    }

    public void setLockscreenWidgetsEnabled(boolean enabled) {
        mLockscreenWidgetsEnabled = enabled;
        updateVisibility();
    }

    public void updateWidgetsMargin(int topMargin) {
        post(() -> setMargins(mWidgetsContainer, mContext, 0, topMargin, 0, 0));
    }

    public void onUiStateChanged(int uiState) {
        if (mLockscreenWeatherEnabled) {
            post(() -> mWeatherContainer.setVisibility(uiState == CLOCK_UI_STATE_AOD ? GONE : VISIBLE));
        }
        if (mLockscreenWidgetsEnabled) {
            post(() -> mWidgetsContainer.setVisibility(uiState == CLOCK_UI_STATE_AOD ? GONE : VISIBLE));
        }

    }

    public int getFullHeight() {
        int clockHeight = mClockContainer.getHeight();
        int weatherHeight = mWeatherContainer.getHeight();
        int widgetsHeight = mWidgetsContainer.getHeight();

        int fullHeight = 0;
        if (mLockscreenClockEnabled) fullHeight += clockHeight;
        if (mLockscreenWeatherEnabled) fullHeight += weatherHeight;
        if (mLockscreenWidgetsEnabled) fullHeight += widgetsHeight;

        return fullHeight;
    }

    private void updateVisibility() {
        post(() -> {
            mClockContainer.setVisibility(mLockscreenClockEnabled ? View.VISIBLE : View.GONE);
            mWeatherContainer.setVisibility(mLockscreenWeatherEnabled ? View.VISIBLE : View.GONE);
            mWidgetsContainer.setVisibility(mLockscreenWidgetsEnabled ? View.VISIBLE : View.GONE);

            if (!mLockscreenClockEnabled && !mLockscreenWeatherEnabled && !mLockscreenWidgetsEnabled) {
                setVisibility(View.GONE);
            } else {
                setVisibility(View.VISIBLE);
            }
        });
    }

}
