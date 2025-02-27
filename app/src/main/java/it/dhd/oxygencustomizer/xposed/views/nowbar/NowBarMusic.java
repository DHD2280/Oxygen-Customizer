package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getColorScheme;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getLastNonNullPackageName;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableLinearLayout;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.COUISeekBar;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.COUISeekBarListener;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsSeekBar;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.CircleFramedDrawable;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class NowBarMusic extends LinearLayout {

    @SuppressLint("StaticFieldLeak")
    public static NowBarMusic instance = null;

    private final Context mContext;
    private Context appContext;

    private final String NO_MEDIA_ID = "oplus_qs_media_panel_title_default";
    private final String NO_MEDIA_STRING;

    // Album Arts
    private ImageView mAlbumArtBig;
    private ImageView albumArt;
    // Actions
    private ImageButton mPrevButton, mPrevButtonBig;
    private ImageButton mPlayPauseButton, mPlayPauseButtonBig;
    private ImageButton mNextButton, mNextButtonBig;

    // Containers
    private LinearLayout mNowBarLayout;
    private LinearLayout mBigMediaPlayer, mLittleMediaPlayer;
    private LinearLayout mSeekBarContainer;

    // Texts
    private TextView trackTitle;
    private TextView mTitle, mAuthor;
    private TextView mCurrentTime, mTotalTime;
    private TextView mAppName;

    // SeekBar
    private AbsSeekBar mTrackSeekBar;

    // Vars
    private boolean mIsSeeking = false;
    private int mSeekingProgress = 0;
    private ImageView mAppIcon;
    private Object mCurrentColorScheme = null;
    private boolean mExpanded = false;
    private final ActivityLauncherUtils mActivityLauncherUtils;
    private final MusicExpansionListener mExpansionListener;

    public interface MusicExpansionListener {
        void onExpandedStateChanged(boolean expanded);
    }

    public NowBarMusic(Context context, MusicExpansionListener listener) {
        super(context);
        instance = this;
        mContext = context;
        mExpansionListener = listener;
        int noMediaString = mContext.getResources().getIdentifier(NO_MEDIA_ID, "string", SYSTEM_UI);
        if (noMediaString != 0x0) {
            NO_MEDIA_STRING = mContext.getResources().getString(noMediaString);
        } else {
            NO_MEDIA_STRING = "No Media"; //no really needed
        }
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException ignored) {}
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        init();
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static NowBarMusic getInstance() {
        return instance;
    }

    public static NowBarMusic getInstance(Context context, MusicExpansionListener listener) {
        if (instance == null) {
            instance = new NowBarMusic(context, listener);
        }
        return instance;
    }

    private void init() {
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        try {
            mNowBarLayout = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(mContext);
        } catch (Exception e) {
            mNowBarLayout = new LinearLayout(mContext);
        }

        mNowBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "now_bar_music",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );

        // Setup album art big as 90% of the screen width
        mAlbumArtBig = (ImageView) ViewHelper.findViewWithTag(v, "album_art_big");
        Rect bounds = SystemUtils.WindowManager().getCurrentWindowMetrics().getBounds();
        mAlbumArtBig.setLayoutParams(new LinearLayout.LayoutParams(
                (int) (bounds.right * 0.9f),
                (int) (bounds.right * 0.9f)
        ));
        mTitle = (TextView) ViewHelper.findViewWithTag(v, "title_big");
        mAuthor = (TextView) ViewHelper.findViewWithTag(v, "album_big");
        // Big Media Player
        mAppIcon = (ImageView) ViewHelper.findViewWithTag(v, "app_icon");
        mAppName = (TextView) ViewHelper.findViewWithTag(v, "app_name");
        mCurrentTime = (TextView) ViewHelper.findViewWithTag(v, "current_position");
        mTotalTime = (TextView) ViewHelper.findViewWithTag(v, "total_duration");
        mSeekBarContainer = (LinearLayout) ViewHelper.findViewWithTag(v, "seek_bar_container");
        try {
            mTrackSeekBar = (AbsSeekBar) COUISeekBar.getConstructor(Context.class).newInstance(mContext);
            // use seekbar from oplus
        } catch (Throwable ignored) {
            mTrackSeekBar = new SeekBar(mContext);
        }
        mTrackSeekBar.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        mTrackSeekBar.setMin(0);
        mTrackSeekBar.setMax(100);
        // create a new proxy for the seekbar listener
        callMethod(mTrackSeekBar, "setOnSeekBarChangeListener", Proxy.newProxyInstance(
                COUISeekBarListener.getClassLoader(),
                new Class[]{COUISeekBarListener},
                new SeekbarListener()
        ));
        mSeekBarContainer.addView(mTrackSeekBar);
        mBigMediaPlayer = (LinearLayout) ViewHelper.findViewWithTag(v, "big_media_player");
        mPrevButtonBig = (ImageButton) ViewHelper.findViewWithTag(v, "previous_big");
        mPlayPauseButtonBig = (ImageButton) ViewHelper.findViewWithTag(v, "big_playpause");
        mNextButtonBig = (ImageButton) ViewHelper.findViewWithTag(v, "next_big");
        // Little Media Player
        mLittleMediaPlayer = (LinearLayout) ViewHelper.findViewWithTag(v, "little_media_player");
        albumArt = (ImageView) ViewHelper.findViewWithTag(v, "albumArt");
        trackTitle = (TextView) ViewHelper.findViewWithTag(v, "trackTitle");
        mPrevButton = (ImageButton) ViewHelper.findViewWithTag(v, "previous_action");
        mPlayPauseButton = (ImageButton) ViewHelper.findViewWithTag(v, "play_pause");
        mNextButton = (ImageButton) ViewHelper.findViewWithTag(v, "next_action");
        mNowBarLayout.addView(v);
        setBarBackground(Color.BLACK);

        mAlbumArtBig.setOnClickListener(v1 -> triggerMediaPlayer()/*mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false)*/);
        albumArt.setOnClickListener(v1 -> triggerMediaPlayer()/*mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false)*/);
        mPrevButton.setOnClickListener(v1 -> AudioDataProvider.instance.prevSong());
        mPlayPauseButton.setOnClickListener(v1 -> AudioDataProvider.instance.toggleMediaPlaybackState());
        mNextButton.setOnClickListener(v1 -> AudioDataProvider.instance.nextSong());
        mPrevButtonBig.setOnClickListener(v1 -> AudioDataProvider.instance.prevSong());
        mPlayPauseButtonBig.setOnClickListener(v1 -> AudioDataProvider.instance.toggleMediaPlaybackState());
        mNextButtonBig.setOnClickListener(v1 -> AudioDataProvider.instance.nextSong());

        setDefaultIcon();
        trackTitle.setText(NO_MEDIA_STRING);
        mTitle.setText(NO_MEDIA_STRING);

        setOnLongClickListener(v1 -> {
            mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false);
            return true;
        });
        addView(mNowBarLayout);

        AudioDataProvider.registerMediaMetadataListener(mMediaMetaDataListener);
    }

    // Our custom Seekbar listener
    static class SeekbarListener implements InvocationHandler {
        /**
         * @noinspection SuspiciousInvocationHandlerImplementation
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            switch (methodName) {
                case "onProgressChanged" -> {
                    AbsSeekBar seekBar = (AbsSeekBar) args[0];
                    int progress = (int) args[1];
                    boolean fromUser = (boolean) args[2];
                    if (fromUser) {
                        instance.mIsSeeking = true;
                        instance.mSeekingProgress = progress;
                        instance.mCurrentTime.setText(instance.formatTime((long) ((instance.mSeekingProgress / 100f) * AudioDataProvider.getTotalDuration())));
                        return true;
                    } else {
                        return false;
                    }
                }
                case "onStartTrackingTouch" -> {
                    instance.mIsSeeking = true;
                    return true;
                }
                case "onStopTrackingTouch" -> {
                    if (instance.mIsSeeking) {
                        instance.mIsSeeking = false;
                        long time = (long) ((instance.mSeekingProgress / 100f) * AudioDataProvider.getTotalDuration());
                        AudioDataProvider.instance.seekTo(time);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    private void triggerMediaPlayer() {
        mExpanded = !mExpanded;
        View[] viewToAnimate = new View[]{mAlbumArtBig, mTitle, mAuthor, mBigMediaPlayer};
        if (mExpanded) {
            mLittleMediaPlayer
                    .animate()
                    .scaleX(0.4f)
                    .scaleY(0.4f)
                    .alpha(0f)
                    .setDuration(200L)
                    .setInterpolator(new AccelerateInterpolator())
                    .withEndAction(() -> {
                        mLittleMediaPlayer.setVisibility(View.GONE);
                        mExpansionListener.onExpandedStateChanged(mExpanded);
                        animateOthers();
                    })
                    .start();
        } else {
            for (View v : viewToAnimate) {
                v
                        .animate()
                        .scaleX(0.4f)
                        .scaleY(0.4f)
                        .alpha(0f)
                        .setDuration(200L)
                        .setInterpolator(new AccelerateInterpolator())
                        .withEndAction(() -> {
                            v.setVisibility(View.GONE);
                            mExpansionListener.onExpandedStateChanged(mExpanded);
                            mLittleMediaPlayer.setVisibility(View.VISIBLE);
                            mLittleMediaPlayer.setScaleX(0.4f);
                            mLittleMediaPlayer.setScaleY(0.4f);
                            mLittleMediaPlayer.setAlpha(0f);
                            mLittleMediaPlayer.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .alpha(1f)
                                    .setDuration(300L)
                                    .setInterpolator(new FastOutLinearInInterpolator())
                                    .start();
                        })
                        .start();
            }
        }

    }

    private void animateOthers() {
        View[] viewToAnimate = new View[]{mAlbumArtBig, mTitle, mAuthor, mBigMediaPlayer};
        for (View v : viewToAnimate) {
            // show
            v.setVisibility(View.VISIBLE);
            v.setScaleX(0.4f);
            v.setScaleY(0.4f);
            v.setAlpha(0f);
            v.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(300L)
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .start();
            mTitle.setSelected(true);
            mAuthor.setSelected(true);
        }
    }

    private void setBarBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(100f);
        mLittleMediaPlayer.setBackground(background);
        mBigMediaPlayer.setBackground(background);
    }

    private void updateMediaPlaybackState() {
        MediaMetadata mediaMetadata = AudioDataProvider.getMediaMetadata();
        boolean isPlaying = AudioDataProvider.isMediaPlaying();

        if (resetMediaIfNeeded()) {
            return;
        }

        mPlayPauseButton.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_pause, null));
        mPlayPauseButtonBig.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_pause, null));

        if (mediaMetadata != null) {
            String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String album = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            trackTitle.setText(title != null && !title.isEmpty() ? title : modRes.getString(R.string.omnijaws_city_unknown));
            mTitle.setText(title != null && !title.isEmpty() ? title : modRes.getString(R.string.omnijaws_city_unknown));
            mAuthor.setText(album != null && !album.isEmpty() ? album : modRes.getString(R.string.omnijaws_city_unknown));

            long currentMillis = AudioDataProvider.instance.getCurrentTime();
            long durationMillis = AudioDataProvider.getTotalDuration();
            if (!mIsSeeking) {
                mCurrentTime.setText(formatTime(currentMillis));
            }
            mTotalTime.setText(formatTime(durationMillis));
            int progress = (int) ((currentMillis * 100) / durationMillis);
            mTrackSeekBar.setProgress(progress);
            mAppIcon.setImageDrawable(getAppIcon(mContext, getLastNonNullPackageName()));
            mAppName.setText(getAppName(mContext, getLastNonNullPackageName()));

            Bitmap bitmap = getArt();
            if (bitmap != null) {
                mAlbumArtBig.clearColorFilter();
                mAlbumArtBig.setImageBitmap(DrawableConverter.getRoundedCornerBitmap(bitmap, 32f));
                int roundSize = (int) modRes.getDimension(R.dimen.nowbar_album_art_size);
                CircleFramedDrawable drawable = new CircleFramedDrawable(bitmap, roundSize);
                albumArt.setImageDrawable(drawable);
                albumArt.clearColorFilter();
            } else {
                setDefaultIcon();
            }
        }
    }

    private Drawable getAppIcon(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String getAppName(Context context, String packageName) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return packageName;
        }
    }

    private String formatTime(long milliseconds) {
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds);
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    private boolean resetMediaIfNeeded() {
        if (!AudioDataProvider.isMediaPlaying()) {
            mPlayPauseButton.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, null));
            mPlayPauseButtonBig.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, null));
            mCurrentTime.setText("0:00");
            mTotalTime.setText("0:00");
            mTrackSeekBar.setProgress(0);
            mTitle.setText(NO_MEDIA_STRING);
            mAuthor.setText("");
            trackTitle.setText(NO_MEDIA_STRING);
            trackTitle.setSelected(true);
            setDefaultIcon();
            setBarBackground(Color.BLACK);
            updateViewsColors(Color.WHITE);
            return true;
        }
        return false;
    }

    private void setDefaultIcon() {
        albumArt.setImageDrawable(ResourcesCompat.getDrawable(modRes, R.drawable.ic_volume_eq, null));
        albumArt.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        mAlbumArtBig.setImageDrawable(ResourcesCompat.getDrawable(modRes, R.drawable.ic_volume_eq, null));
        mAlbumArtBig.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        AudioDataProvider.registerMediaMetadataListener(mMediaMetaDataListener);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AudioDataProvider.unregisterMediaMetadataListener(mMediaMetaDataListener);
    }

    private final AudioDataProvider.MediaMetadataListener mMediaMetaDataListener = new AudioDataProvider.MediaMetadataListener() {
        @Override
        public void onMediaMetadataChanged() {
            updateMediaPlaybackState();
        }

        @Override
        public void onPlaybackStateChanged() {
            updateMediaPlaybackState();
        }

        @Override
        public void onMediaColorsChanged() {
            if (resetMediaIfNeeded()) {
                return;
            }
            Object colorScheme = getColorScheme();
            if (colorScheme == null) {
                mCurrentColorScheme = null;
            } else if (mCurrentColorScheme != colorScheme) {
                mCurrentColorScheme = colorScheme;
            }
            setBarBackground(getColorContainer());
            updateViewsColors(getColorOnContainer());
        }
    };

    private int getColorContainer() {
        if (mCurrentColorScheme == null) return Color.BLACK;
        boolean isDark = SystemUtils.isDarkMode();
        return (int) ((!isDark) ?
                        callMethod(callMethod(mCurrentColorScheme, "getAccent1"), "getS100") :
                        callMethod(callMethod(mCurrentColorScheme, "getAccent1"), "getS700"));
    }

    private int getColorOnContainer() {
        if (mCurrentColorScheme == null) return Color.WHITE;
        boolean isDark = SystemUtils.isDarkMode();
        return (int) ((!isDark) ?
                callMethod(callMethod(mCurrentColorScheme, "getAccent1"), "getS900") :
                callMethod(callMethod(mCurrentColorScheme, "getAccent1"), "getS100"));
    }

    private void updateViewsColors(int color) {
        mPrevButton.setImageTintList(ColorStateList.valueOf(color));
        mNextButton.setImageTintList(ColorStateList.valueOf(color));
        mPlayPauseButton.setBackgroundTintList(ColorStateList.valueOf(color));
        mPrevButtonBig.setImageTintList(ColorStateList.valueOf(color));
        mNextButtonBig.setImageTintList(ColorStateList.valueOf(color));
        mPlayPauseButtonBig.setBackgroundTintList(ColorStateList.valueOf(color));
        callMethod(mTrackSeekBar, "setProgressColor", ColorStateList.valueOf(color));
        callMethod(mTrackSeekBar, "setThumbColor", ColorStateList.valueOf(color));
        trackTitle.setTextColor(color);
    }

}
