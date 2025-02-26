package it.dhd.oxygencustomizer.xposed.views.nowbar;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.session.PlaybackState;
import android.os.BatteryManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class NowBarHolder extends LinearLayout {

    @SuppressLint("StaticFieldLeak")
    public static NowBarHolder instance = null;

    private final Context mContext;
    private Context appContext;

    private ViewPager mViewPager;
    private NowBarController mController;
    private boolean mWeatherEnabled = false;

    private boolean isChargingStatusHandled = false;

    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_BATTERY_CHANGED:
                    if (isCharging(intent) && isPluggedIn(intent) && !isChargingStatusHandled) {
                        mViewPager.setCurrentItem(1);
                        isChargingStatusHandled = true;
                    }
                    break;
                case Intent.ACTION_POWER_DISCONNECTED:
                    mViewPager.setCurrentItem(mWeatherEnabled ? 2 : 0);
                    isChargingStatusHandled = false;
                    break;
            }
        }
    };

    public NowBarHolder(Context context) {
        super(context);
        instance = this;
        mContext = context;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        init();
    }

    public static NowBarHolder getInstance(Context context) {
        if (instance == null) {
            return new NowBarHolder(context);
        }
        return instance;
    }

    private void init() {
        XposedBridge.log("NowBarHolder init");
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "now_bar_holder",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        mController = NowBarController.getInstance(mContext);
        LinearLayout mPagerContainer = (LinearLayout) ViewHelper.findViewWithTag(view, "nowBarViewPagerContainer");
        mViewPager = new ViewPager(mContext);
        mViewPager.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));
        mViewPager.setAdapter(new NowBarAdapter(mContext, mWeatherEnabled));
        mViewPager.setPageTransformer(false, new PageTransitionTransformer());
        mPagerContainer.addView(mViewPager);
        addView(view);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED);
        mController.setNowBarHolder(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED);
        mController.setNowBarHolder(this);
        AudioDataProvider.registerInfoCallback(this::showMusicNowBarIfNeeded);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(batteryReceiver);
    }

    private static class NowBarAdapter extends PagerAdapter {

        private final Context context;
        private final boolean weatherEnabled;

        public NowBarAdapter(Context context, boolean weatherEnabled) {
            this.context = context;
            this.weatherEnabled = weatherEnabled;
        }

        @Override
        public int getCount() {
            return weatherEnabled ? 3 : 2;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull View container, int position) {
            View view = switch (position) {
                case 1 -> new NowBarBattery(context);
                case 2 -> new NowBarWeather(context);
                default -> NowBarMusic.getInstance(context);
            };
            ((ViewPager) container).addView(view);
            return view;
        }

        @Override
        public void destroyItem(@NonNull View container, int position, @NonNull Object object) {
            ((ViewPager) container).removeView((View) object);
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }
    }

    private static class PageTransitionTransformer implements ViewPager.PageTransformer {

        private static final float SCALE_FACTOR = 0.9f;
        private static final float TRANSLATION_Y_FACTOR = 40f;
        private static final float ALPHA_FACTOR = 0.7f;

        @Override
        public void transformPage(@NonNull View page, float position) {
            if (position < -1 || position > 1) {
                page.setAlpha(0f);
            } else if (position <= 0) {
                page.setScaleX(1f);
                page.setScaleY(1f);
                page.setTranslationY(0f);
                page.setAlpha(1f);
            } else if (position <= 1) {
                float scale = SCALE_FACTOR + (1 - SCALE_FACTOR) * (1 - position);
                float translationY = position * TRANSLATION_Y_FACTOR;
                page.setScaleX(scale);
                page.setScaleY(scale);
                page.setTranslationY(translationY);
                page.setAlpha(ALPHA_FACTOR + (1 - ALPHA_FACTOR) * (1 - position));
            }
        }
    }

    private boolean isCharging(Intent intent) {
        int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
    }

    private boolean isPluggedIn(Intent intent) {
        int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        return chargePlug == BatteryManager.BATTERY_PLUGGED_AC
                || chargePlug == BatteryManager.BATTERY_PLUGGED_USB
                || chargePlug == BatteryManager.BATTERY_PLUGGED_WIRELESS;
    }

    private void showMusicNowBarIfNeeded(int state) {
        if (state != PlaybackState.STATE_PLAYING) return;
        mViewPager.setCurrentItem(0);
    }

    private void refreshViewPager() {
        mViewPager.setAdapter(new NowBarAdapter(mContext, mWeatherEnabled));
        if (mWeatherEnabled) mViewPager.setCurrentItem(2);
    }

    public void setWeatherEnabled(boolean enabled) {
        mWeatherEnabled = enabled;
        refreshViewPager();
    }

    public void updateMargins(int left, int right, int bottom) {
        ViewHelper.setMarginsNoConvert(this, mContext, left, 0, right, bottom);
    }

}

