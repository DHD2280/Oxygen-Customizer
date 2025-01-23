package it.dhd.oxygencustomizer.xposed.views;

import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.CLOCK_UI_STATE_AOD;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.CLOCK_UI_STATE_LS;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.CLOCK_UI_STATE_SHADE;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.findViewWithTag;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMarginsNoConvert;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;

public class LockscreenView extends FrameLayout {

    private Context mContext;
    private LinearLayout mViewsContainer;
    private LinearLayout mClockContainer;
    private LinearLayout mWeatherContainer;
    private LinearLayout mWidgetsContainer;
    private View mClockView;
    private CurrentWeatherView mWeatherView;
    private LockscreenWidgetsView mWidgetsView;
    private AnimatorSet currentAnimationSet;

    private boolean mLockscreenClockEnabled, mLockscreenWeatherEnabled, mLockscreenWidgetsEnabled;

    private int mAodUiDeBurninY = -1;
    private int mStockClockHeight = -1;
    private int keyguardLockHeight = -1;

    @SuppressLint("StaticFieldLeak")
    public static LockscreenView instance = null;

    public LockscreenView(@NonNull Context context) {
        super(context);

        instance = this;
        mContext = context;

        createViews();
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
        post(() -> mClockContainer.setVisibility(enabled ? VISIBLE : GONE));
    }

    public void setClockView(View clockView) {
        try {
            ((ViewGroup) clockView.getParent()).removeView(clockView);
        } catch (Throwable ignored) {}
        mClockView = clockView;
        mClockContainer.removeAllViews();
        mClockContainer.addView(mClockView);
    }

    public void updateClockMargins(int top, int bottom) {
        setMarginsNoConvert(mClockContainer, mContext, 0, top, 0, dp2px(mContext, bottom));
    }

    public int getClockHeight() {
        return mClockContainer.getHeight();
    }

    public void setAodStuff(int aodUiDeBurninY, int stockClockHeight) {
        mAodUiDeBurninY = aodUiDeBurninY;
        mStockClockHeight = stockClockHeight;
    }

    public void setKeyguardLockHeight(int height) {
        keyguardLockHeight = height;
    }

    public void setLockscreenWeatherEnabled(boolean enabled) {
        mLockscreenWeatherEnabled = enabled;
        post(() -> mWeatherContainer.setVisibility(enabled ? VISIBLE : GONE));
    }

    public void updateWeatherMargins(int left, int top, int right, int bottom) {
        setMargins(mWeatherContainer, mContext,
                left, top, right, bottom);
    }

    public void setWeatherCentered(boolean centered) {
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
        post(() -> mWidgetsContainer.setVisibility(enabled ? VISIBLE : GONE));
    }

    public void onUiStateChanged(int uiState) {

        if (mLockscreenWeatherEnabled) {
            post(() -> mWeatherContainer.setVisibility(uiState == CLOCK_UI_STATE_AOD ? GONE : VISIBLE));
        }
        if (mLockscreenWidgetsEnabled) {
            post(() -> mWidgetsContainer.setVisibility(uiState == CLOCK_UI_STATE_AOD ? GONE : VISIBLE));
        }

        try {
            switch (uiState) {
                case CLOCK_UI_STATE_AOD -> animateViewToAod();
                case CLOCK_UI_STATE_LS, CLOCK_UI_STATE_SHADE -> animateViewBack();
            }
        } catch (Throwable t) {
            XposedBridge.log("LockscreenView.onUiStateChanged: " + Log.getStackTraceString(t));
        }
    }

    public void animateViewToAod() {
        stopCurrentAnimation();
        int clockHeight = mClockContainer.getHeight();
        int measuredHeight = mClockContainer.getMeasuredHeight();

        int originalHeight = mClockContainer.getHeight();
        float scaledHeight = originalHeight * 0.8f;

        float desiredBottomY = (mAodUiDeBurninY + mStockClockHeight);
        float deltaY = desiredBottomY - (mStockClockHeight + originalHeight);

        XposedBridge.log("animateViewToAod: keyguardLockHeight: " + keyguardLockHeight + " clockHeight: " + clockHeight + " measuredHeight: " + measuredHeight + " mAodUiDeBurninY: " + mAodUiDeBurninY + " desiredBottomY: " + desiredBottomY + " deltaY: " + deltaY);

        // Translate View
        ObjectAnimator translationX = ObjectAnimator.ofFloat(this, "translationX", -200f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", this.getTranslationY() + deltaY);

        // Scale View
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 0.8f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 0.8f);

        currentAnimationSet = new AnimatorSet();
        List<Animator> animations = new ArrayList<>();
        if (!isClockCentered()) {
            animations.add(translationX);
        }
        animations.add(translationY);
        animations.add(scaleX);
        animations.add(scaleY);
        currentAnimationSet.playTogether(animations);
        currentAnimationSet.setDuration(500);
        currentAnimationSet.start();
    }

    public void animateViewBack() {
        stopCurrentAnimation();

        // Reset state
        ObjectAnimator translationX = ObjectAnimator.ofFloat(this, "translationX", 0f);
        ObjectAnimator translationY = ObjectAnimator.ofFloat(this, "translationY", 0f);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(this, "scaleX", 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(this, "scaleY", 1f);

        currentAnimationSet = new AnimatorSet();
        currentAnimationSet.playTogether(translationX, translationY, scaleX, scaleY);
        currentAnimationSet.setDuration(500);
        currentAnimationSet.start();
    }

    private boolean isClockCentered() {
        return Settings.System.getInt(mContext.getContentResolver(), "keyguard_style_aod_clock_gravity", 0) == 0;
    }

    private void stopCurrentAnimation() {
        if (currentAnimationSet != null && currentAnimationSet.isRunning()) {
            currentAnimationSet.cancel();
        }
    }

    public int getFullHeight() {
        int clockHeight = mClockContainer.getHeight();
        int weatherHeight = mWeatherContainer.getHeight();
        int widgetsHeight = mWidgetsContainer.getHeight();

        int fullHeight = clockHeight;
        if (mLockscreenWeatherEnabled) fullHeight += weatherHeight;
        if (mLockscreenWidgetsEnabled) fullHeight += widgetsHeight;

        return fullHeight;
    }

}
