package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpDrawableUtils.getNewAutoBlurDrawable;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.RenderEffect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.session.PlaybackState;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.views.ExtendedViewPager;

@SuppressLint("ViewConstructor")
public class NowBarHolder extends LinearLayout {

    @SuppressLint("StaticFieldLeak")
    public static NowBarHolder instance = null;

    private final Context mContext;
    private Context appContext;

    // Our pages
    private NowBarMusic mNowBarMusic;
    private NowBarBattery mNowBarBattery;
    private NowBarWeather mNowBarWeather;
    private NowBarNotification mNowBarNotification;

    // Backgrounds
    private FrameLayout mBlurredBackground;
    private ImageView mBlurredAlbumArt;
    private int mBackgroundMode = 0;

    private LinearLayout mPagerContainer;

    // Pager
    private ExtendedViewPager mViewPager;
    private PagerAdapter mPagerAdapter;
    private final List<View> mPages = new ArrayList<>();

    // Controller
    private NowBarController mController;

    // Vars
    private boolean mWeatherEnabled = false;
    private boolean mNotificationEnabled = true;
    public NowBarMusic.MusicExpansionListener parentExpandedListener;
    private boolean mExpanded = false;
    private int mLeftMargin, mRightMargin, mBottomMargin;

    private boolean isChargingStatusHandled = false;

    private void updateBackground(boolean expanded) {
        if (mBackgroundMode == 0) {
            mBlurredAlbumArt.setVisibility(View.GONE);
            mBlurredBackground.setVisibility(expanded ? View.VISIBLE : View.GONE);
        } else {
            mBlurredBackground.setVisibility(View.GONE);
            mBlurredAlbumArt.setVisibility(expanded ? View.VISIBLE : View.GONE);
            mBlurredAlbumArt.setRenderEffect(expanded ?
                    RenderEffect.createBlurEffect(
                            50, 50, Shader.TileMode.CLAMP) :
                    null);
        }
    }

    private final NowBarMusic.MusicExpansionListener musicExpansionListener = expanded -> {
        mExpanded = expanded;
        mViewPager.setExpanded(expanded);

        parentExpandedListener.onExpandedStateChanged(expanded);
        updatePagerParams(expanded);
        updateBackground(expanded);
    };

    private final NowBarMusic.OnAlbumArtChanged albumArtListener = albumArt -> {
        if (mBlurredAlbumArt != null) {
            mBlurredAlbumArt.setImageDrawable(albumArt);
        }
    };

    private final NowBarNotification.OnUsefulNotificationListener notificationListener = () -> {
        if (AudioDataProvider.isMediaPlaying()) return;
        if (mExpanded || !mNotificationEnabled) return;
        if (mWeatherEnabled) mViewPager.setCurrentItem(3);
        else mViewPager.setCurrentItem(2);
    };

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

    public NowBarHolder(Context context, NowBarMusic.MusicExpansionListener listener) {
        super(context);
        instance = this;
        mContext = context;
        parentExpandedListener = listener;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {
        }
        init();
    }

    public static NowBarHolder getInstance(Context context, NowBarMusic.MusicExpansionListener listener) {
        if (instance == null) {
            return new NowBarHolder(context, listener);
        }
        return instance;
    }

    private void init() {
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
        mBlurredBackground = (FrameLayout) ViewHelper.findViewWithTag(view, "nowBarBlurredBackground");
        mBlurredAlbumArt = (ImageView) ViewHelper.findViewWithTag(view, "nowBarBlurredAlbumArt");
        mPagerContainer = (LinearLayout) ViewHelper.findViewWithTag(view, "nowBarViewPagerContainer");
        mViewPager = new ExtendedViewPager(mContext);
        mViewPager.setLayoutParams(new LinearLayout.LayoutParams(
                MATCH_PARENT,
                MATCH_PARENT
        ));
        try {
            mNowBarMusic = NowBarMusic.getInstance(mContext, musicExpansionListener, albumArtListener);
        } catch (Throwable t) {
            XposedBridge.log(this.getClass().getSimpleName() + " - " + Log.getStackTraceString(t));
        }
        try {
            mNowBarBattery = new NowBarBattery(mContext);
        } catch (Throwable t) {
            XposedBridge.log(this.getClass().getSimpleName() + " - " + Log.getStackTraceString(t));
        }
        try {
            mNowBarWeather = new NowBarWeather(mContext);
        } catch (Throwable t) {
            XposedBridge.log(this.getClass().getSimpleName() + " - " + Log.getStackTraceString(t));
        }
        try {
            mNowBarNotification = new NowBarNotification(mContext, notificationListener);
        } catch (Throwable t) {
            XposedBridge.log(this.getClass().getSimpleName() + " - " + Log.getStackTraceString(t));
        }
        mPages.addAll(List.of(mNowBarMusic, mNowBarBattery));
        setupPager();
        mViewPager.setPageTransformer(false, new PageTransitionTransformer());
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                // ensure that media player is match parent height if expanded
                boolean expanded = position == 0 && mNowBarMusic != null && mNowBarMusic.isExpanded();
                mViewPager.setExpanded(expanded); // if there is a page change, ensure that it can swipe
                updatePagerParams(expanded);
            }
        });
        mPagerContainer.addView(mViewPager);
        addView(view);
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        mContext.registerReceiver(batteryReceiver, filter, Context.RECEIVER_EXPORTED);
        mController.setNowBarHolder(this);
    }

    private void setupPager() {
        mPagerAdapter = new PagerAdapter() {
            @Override
            public int getCount() {
                return mPages.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                View view = mPages.get(position);
                container.addView(view);
                return view;
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView((View) object);
            }
        };
    }

    private void updatePagerParams(boolean expanded) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) mPagerContainer.getLayoutParams();
        params.height = expanded ? MATCH_PARENT : dp2px(mContext, 72);
        params.setMargins(expanded ? dp2px(mContext, 12) : mLeftMargin, 0, expanded ? dp2px(mContext, 12) : mRightMargin, mBottomMargin);
        params.gravity = Gravity.BOTTOM;
        mPagerContainer.setLayoutParams(params);
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
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#6F161616"));
        if (Build.VERSION.SDK_INT >= 35) {
            Drawable blurred = getNewAutoBlurDrawable(mBlurredBackground, background, 0);
            mBlurredBackground.setBackground(blurred);
        } else {
            mBlurredBackground.setBackground(background);
        }
        mBlurredBackground.setVisibility(View.GONE);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(batteryReceiver);
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
        mViewPager.setAdapter(null);
        mPages.clear();
        mPagerAdapter.notifyDataSetChanged();
        mPages.addAll(List.of(mNowBarMusic, mNowBarBattery));
        if (mWeatherEnabled) mPages.add(mNowBarWeather);
        if (mNotificationEnabled) mPages.add(mNowBarNotification);
        mPagerAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mPagerAdapter);
        if (mWeatherEnabled) mViewPager.setCurrentItem(2);
        else mViewPager.setCurrentItem(0);
    }

    public void setWeatherEnabled(boolean enabled) {
        mWeatherEnabled = enabled;
        refreshViewPager();
    }

    public void setNotificationEnabled(boolean enabled) {
        mNotificationEnabled = enabled;
        mNowBarNotification.setNotificationEnabled(enabled);
        refreshViewPager();
    }

    public void updateMargins(int left, int right, int bottom) {
        mLeftMargin = left;
        mRightMargin = right;
        mBottomMargin = bottom;
        updatePagerParams(false);
    }

    public void updateBattery(int chargingIconStyle,
                              boolean customColors, int color1, int color2, int color3, int color4,
                              boolean indicateFast, int fastColor, boolean indicatePowerSave, int powerSaveColor,
                              int textColor) {
        if (mNowBarBattery != null) {
            mNowBarBattery.setBatteryBarOptions(
                    chargingIconStyle,
                    customColors, color1, color2, color3, color4,
                    indicateFast, fastColor, indicatePowerSave, powerSaveColor,
                    textColor
            );
        }
    }

    public void updateWeather(boolean customColors, int textColor, int backgroundColor) {
        if (mNowBarWeather != null) {
            mNowBarWeather.setOptions(customColors, textColor, backgroundColor);
        }
    }

    public void updateMusic(
            boolean extendedPlayer, int backgroundMode,
            int playerMode, boolean showClock,
            int datePosition, String dateFormat, float textScaling, boolean customFont, int topMargin) {
        if (mNowBarMusic != null) {
            mNowBarMusic.setExtendedPlayerEnabled(extendedPlayer);
            mNowBarMusic.setExtendedPlayerOptions(
                    playerMode, showClock,
                    datePosition, dateFormat, textScaling, customFont, topMargin
                    );
        }
        mBackgroundMode = backgroundMode;
    }

    public void updateNotification(boolean ignoreSecurity,
                                   boolean customColor, boolean useAppIcons,
                                   int backgroundColor, int textColor1, int textColor2, int iconTintColor) {
        if (mNowBarNotification != null) {
            mNowBarNotification.setIgnoreSecurity(ignoreSecurity);
            mNowBarNotification.setNotificationsOptions(
                    customColor, useAppIcons,
                    backgroundColor, textColor1, textColor2, iconTintColor
            );
        }
    }

}

