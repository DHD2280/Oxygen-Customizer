package it.dhd.oxygencustomizer.xposed.views;

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.START;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock.CLOCK_UI_STATE_AOD;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMarginsNoConvert;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextClock;

import androidx.annotation.NonNull;

import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.LockscreenWidgetsView;

public class LockscreenView extends FrameLayout {

    private static final long AOD_CLOCK_MS = 650;
    private static final float AOD_CLOCK_SCALE = 0.9f;
    private static final float AOD_SAFE_MARGIN_DP = 8f;

    private Context mContext;
    private boolean mAodClock = false;
    private LinearLayout mViewsContainer;
    private LinearLayout mClockContainer;
    private LinearLayout mWeatherContainer;
    private LinearLayout mWidgetsContainer;
    private View mClockView;
    private CurrentWeatherView mWeatherView;
    private LockscreenWidgetsView mWidgetsView;

    public boolean mLockscreenClockEnabled, mLockscreenWeatherEnabled, mLockscreenWidgetsEnabled;

    @SuppressLint("StaticFieldLeak")
    public static LockscreenView instance = null;

    private boolean mTimeReceiverRegistered = false;

    private final BroadcastReceiver mTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            post(LockscreenView.this::refreshClock);
        }
    };

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
        mClockContainer.setClipChildren(false);
        mClockContainer.setClipToPadding(false);
        mViewsContainer.setClipChildren(false);
        mViewsContainer.setClipToPadding(false);
        setClipChildren(false);
        setClipToPadding(false);

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
        refreshClock();
    }

    private void refreshClock() {
        if (mClockContainer == null || mClockView == null) return;

        processAllTextClocks(mClockContainer);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mTimeReceiverRegistered) return;
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_TICK);
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        mContext.registerReceiver(mTimeReceiver, filter, Context.RECEIVER_EXPORTED);
        mTimeReceiverRegistered = true;
        refreshClock();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!mTimeReceiverRegistered) return;
        try {
            mContext.unregisterReceiver(mTimeReceiver);
        } catch (Throwable ignored) {
        }
        mTimeReceiverRegistered = false;
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (isVisible) refreshClock();
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

    public void setAodClock(boolean aod) {
        if (mClockContainer == null) return;
        mAodClock = aod;
        mClockContainer.setPivotX(mClockContainer.getWidth() / 2f);
        mClockContainer.setPivotY(0f);
        float scale = aod ? AOD_CLOCK_SCALE : 1f;
        ViewPropertyAnimator animator = mClockContainer.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(AOD_CLOCK_MS)
                .setInterpolator(new PathInterpolator(0.2f, 0f, 0f, 1f));
        if (!aod) animator.translationY(0f);
        animator.start();
    }

    public void setBurnInOffset(float offset) {
        if (mClockContainer == null || !mAodClock || !mClockContainer.isAttachedToWindow()) return;
        View parent = (View) mClockContainer.getParent();
        int[] location = new int[2];
        parent.getLocationOnScreen(location);
        float height = mClockContainer.getHeight();
        float scaled = height * AOD_CLOCK_SCALE;
        float top = location[1] - parent.getTranslationY() + mClockContainer.getTop();
        float screen = mClockContainer.getRootView().getHeight();
        float safe = safeInset();
        float min = Math.min(0f, safe - top);
        float max = Math.max(0f, screen - safe - (top + scaled));
        mClockContainer.setTranslationY(Math.max(min, Math.min(max, offset)));
    }

    private float safeInset() {
        int id = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        int statusBar = id != 0 ? mContext.getResources().getDimensionPixelSize(id) : 0;
        return statusBar + mContext.getResources().getDisplayMetrics().density * AOD_SAFE_MARGIN_DP;
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
        if (weatherShown()) {
            post(() -> mWeatherContainer.setVisibility(uiState == CLOCK_UI_STATE_AOD ? GONE : VISIBLE));
        }
        if (widgetsShown()) {
            post(() -> mWidgetsContainer.setVisibility(uiState == CLOCK_UI_STATE_AOD ? GONE : VISIBLE));
        }

    }

    public int getFullHeight(boolean includeMargins) {
        int clockHeight = includeMargins ? mClockContainer.getHeight() : getViewHeightWithoutMargins(mClockContainer);
        int weatherHeight = mWeatherContainer.getHeight();
        int widgetsHeight = mWidgetsContainer.getHeight();

        int fullHeight = 0;
        if (mLockscreenClockEnabled) fullHeight += clockHeight;
        if (weatherShown()) fullHeight += weatherHeight;
        if (widgetsShown()) fullHeight += widgetsHeight;

        return fullHeight;
    }

    private int getViewHeightWithoutMargins(View view) {
        if (view == null) return 0;

        int height = view.getHeight();
        ViewGroup.LayoutParams lp = view.getLayoutParams();

        if (lp instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) lp;
            return height - mlp.topMargin - mlp.bottomMargin;
        }

        return height;
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

    public boolean isClockEnabled() {
        return mLockscreenClockEnabled;
    }

    public boolean isWeatherEnabled() {
        return mLockscreenWeatherEnabled;
    }

    public boolean isWidgetsEnabled() {
        return mLockscreenWidgetsEnabled;
    }

}
