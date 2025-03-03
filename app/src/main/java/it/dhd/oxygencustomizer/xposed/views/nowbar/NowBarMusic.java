package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getColorScheme;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getLastNonNullPackageName;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getMediaData;
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
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.AbsSeekBar;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Space;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
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
    private ImageButton mLocalFavoriteButton, mLocalLyricBtn;
    private Space mLocalFavoriteSpace, mLocalLyricSpace;

    // Containers
    private LinearLayout mNowBarLayout;
    private LinearLayout mBigMediaPlayer, mLittleMediaPlayer;
    private LinearLayout mSeekBarContainer;

    // Texts
    private TextView trackTitle;
    private TextView mTitle, mAuthor;
    private TextView mCurrentTime, mTotalTime;

    // App Info
    private TextView mAppName;
    private ImageView mAppIcon;

    // SeekBar
    private AbsSeekBar mTrackSeekBar;

    // Vars
    private boolean mExtendedPlayer = true;
    private int mBackgroundColor = Color.BLACK;
    private boolean mIsSeeking = false;
    private int mSeekingProgress = 0;
    private Object mCurrentColorScheme = null;
    private boolean mExpanded = false;
    private String mLastTitle = "";
    private String mLastAuthor = "";

    // Activity Launcher
    private final ActivityLauncherUtils mActivityLauncherUtils;

    // Music Expansion listener
    private final MusicExpansionListener mExpansionListener;

    // Album art listener
    private final OnAlbumArtChanged mAlbumArtListener;

    public boolean isExpanded() {
        return mExpanded;
    }

    public interface MusicExpansionListener {
        void onExpandedStateChanged(boolean expanded);
    }

    public interface OnAlbumArtChanged {
        void onAlbumArtChanged(Drawable albumArt);
    }

    public NowBarMusic(Context context, MusicExpansionListener listener, OnAlbumArtChanged albumArtChanged) {
        super(context);
        instance = this;
        mContext = context;
        mExpansionListener = listener;
        mAlbumArtListener = albumArtChanged;
        int noMediaString = mContext.getResources().getIdentifier(NO_MEDIA_ID, "string", SYSTEM_UI);
        if (noMediaString != 0x0) {
            NO_MEDIA_STRING = mContext.getResources().getString(noMediaString);
        } else {
            NO_MEDIA_STRING = "No Media"; //no really needed
        }
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        init();
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static NowBarMusic getInstance() {
        return instance;
    }

    public static NowBarMusic getInstance(Context context, MusicExpansionListener listener, OnAlbumArtChanged albumArtChanged) {
        if (instance == null) {
            instance = new NowBarMusic(context, listener, albumArtChanged);
        }
        return instance;
    }

    private void init() {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        try {
            mNowBarLayout = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(mContext);
        } catch (Exception e) {
            mNowBarLayout = new LinearLayout(mContext);
        }

        mNowBarLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
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
        LayoutParams imageParams = new LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                (int) (bounds.right * 0.9f)
        );
        imageParams.gravity = Gravity.CENTER_HORIZONTAL;
        mAlbumArtBig.setLayoutParams(imageParams);
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
        mTrackSeekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mTrackSeekBar.setMin(0);
        mTrackSeekBar.setMax(100);
        // create a new proxy for the seekbar listener
        callMethod(mTrackSeekBar, "setOnSeekBarChangeListener", Proxy.newProxyInstance(
                COUISeekBarListener.getClassLoader(),
                new Class[]{COUISeekBarListener},
                new SeekbarListener()
        ));
        mSeekBarContainer.addView(mTrackSeekBar);
        mLocalFavoriteSpace = (Space) ViewHelper.findViewWithTag(v, "space_action_1");
        mLocalFavoriteButton = (ImageButton) ViewHelper.findViewWithTag(v, "action1");
        mBigMediaPlayer = (LinearLayout) ViewHelper.findViewWithTag(v, "big_media_player");
        mPrevButtonBig = (ImageButton) ViewHelper.findViewWithTag(v, "previous_big");
        mPlayPauseButtonBig = (ImageButton) ViewHelper.findViewWithTag(v, "big_playpause");
        mNextButtonBig = (ImageButton) ViewHelper.findViewWithTag(v, "next_big");
        mLocalLyricSpace = (Space) ViewHelper.findViewWithTag(v, "space_action_2");
        mLocalLyricBtn = (ImageButton) ViewHelper.findViewWithTag(v, "action2");
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
        if (!mExtendedPlayer) return;
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
                        setBarBackground(mBackgroundColor);
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
                            setBarBackground(mBackgroundColor);
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
        if (mBackgroundColor != color) mBackgroundColor = color;
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(100f);
        mBigMediaPlayer.setBackground(background);
        mLittleMediaPlayer.setBackground(background);

    }

    private void updateMediaPlaybackState() {
        MediaMetadata mediaMetadata = AudioDataProvider.getMediaMetadata();
        boolean isPlaying = AudioDataProvider.isMediaPlaying();
        if (resetMediaIfNeeded()) {
            return;
        }

        mPlayPauseButton.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_pause, null));
        mPlayPauseButtonBig.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_pause, null));

        Object mediaData = getMediaData();
        bindActions(mediaData);

        if (mediaMetadata != null) {
            String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            if (!TextUtils.isEmpty(title) && !title.equals(mLastTitle) && !TextUtils.equals(title, trackTitle.getText())) {
                mLastTitle = title;
                trackTitle.setText(title);
                mTitle.setText(title);
                trackTitle.setSelected(true);
            } else if (TextUtils.isEmpty(title)) {
                trackTitle.setText(modRes.getString(R.string.omnijaws_city_unknown));
                mTitle.setText(modRes.getString(R.string.omnijaws_city_unknown));
            }
            if (!TextUtils.isEmpty(artist) && !artist.equals(mLastAuthor) && !TextUtils.equals(artist, mAuthor.getText())) {
                mLastAuthor = artist;
                mAuthor.setText(artist);
            } else if (TextUtils.isEmpty(artist)) {
                mAuthor.setText(modRes.getString(R.string.omnijaws_city_unknown));
            }

            long currentMillis = AudioDataProvider.instance.getCurrentTime();
            long durationMillis = AudioDataProvider.getTotalDuration();
            if (!mIsSeeking) {
                mCurrentTime.setText(formatTime(currentMillis));
            }
            mTotalTime.setText(formatTime(durationMillis));
            if (durationMillis == 0L) {
                mTrackSeekBar.setProgress(0);
            } else {
                int progress = (int) ((currentMillis * 100) / durationMillis);
                mTrackSeekBar.setProgress(progress);
            }
            mAppIcon.setImageDrawable(getAppIcon(getLastNonNullPackageName()));
            mAppName.setText(getAppName(getLastNonNullPackageName()));

            Drawable artWork = null;
            try {
                if (mediaData != null) {
                    artWork = (Drawable) callMethod(mediaData, "getArtwork");
                }
            } catch (Throwable ignored) {}
            Bitmap bitmap = getArt();
            if (bitmap != null || artWork != null) {
                mAlbumArtBig.clearColorFilter();
                RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(),
                        bitmap != null ? bitmap :
                                DrawableConverter.drawableToBitmap(artWork));
                roundedDrawable.setCornerRadius(32f);
                roundedDrawable.setAntiAlias(true);
                mAlbumArtBig.setImageDrawable(roundedDrawable);
                mAlbumArtListener.onAlbumArtChanged(roundedDrawable);
                int roundSize = (int) modRes.getDimension(R.dimen.nowbar_album_art_size);
                CircleFramedDrawable drawable = new CircleFramedDrawable(bitmap, roundSize);
                albumArt.setImageDrawable(drawable);
                albumArt.clearColorFilter();
            } else {
                setDefaultIcon();
            }
        }
    }

    private Drawable getAppIcon(String packageName) {
        PackageManager pm = SystemUtils.PackageManager();
        try {
            return pm.getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    private String getAppName(String packageName) {
        PackageManager pm = SystemUtils.PackageManager();
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
            mLastTitle = "";
            mAuthor.setText("");
            mLastAuthor = "";
            trackTitle.setText(NO_MEDIA_STRING);
            trackTitle.setSelected(true);
            restoreActions();
            setDefaultIcon();
            setBarBackground(Color.BLACK);
            updateViewsColors(Color.WHITE);
            return true;
        }
        return false;
    }

    private void restoreActions() {
        mLocalFavoriteButton.setVisibility(View.GONE);
        mLocalLyricBtn.setVisibility(View.GONE);
        mLocalFavoriteSpace.setVisibility(View.GONE);
        mLocalLyricSpace.setVisibility(View.GONE);
    }

    private void bindActions(Object mediaData) {
        XposedBridge.log("NowBarMusic bindActions");
        if (mediaData == null) return;
        List<Object> actions = (List<Object>) callMethod(mediaData, "getActions");
        XposedBridge.log("NowBarMusic bindActions actions: " + actions);
        if (actions == null || actions.isEmpty()) return;
        if (actions.size() < 4) return;
        int size = actions.size();
        boolean isRtl = mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (size == 4) {
            bindMediaNotificationButton(mediaData, actions.get(0),null);
        } else {
            bindMediaNotificationButton(mediaData, actions.get(isRtl ? 4 : 0), actions.get(isRtl ? 0 : 4));
        }
    }

    public final void bindMediaNotificationButton(Object mediaData, Object mediaAction, Object mediaAction1) {

        mLocalFavoriteButton.setVisibility(mediaAction == null ? View.GONE : View.VISIBLE);
        mLocalFavoriteSpace.setVisibility(mediaAction == null ? View.GONE : View.VISIBLE);
        mLocalLyricBtn.setVisibility(mediaAction1 == null ? View.GONE : View.VISIBLE);
        mLocalLyricSpace.setVisibility(mediaAction1 == null ? View.GONE : View.VISIBLE);

        if (mediaAction != null) {
            setExtraBtnState(this.mLocalFavoriteButton, mediaAction, mediaData);
        }
        if (mediaAction1 != null) {
            setExtraBtnState(this.mLocalLyricBtn, mediaAction1, mediaData);
        }
    }

    private void setExtraBtnState(final ImageButton imageButton, final Object mediaAction, final Object mediaData) {
        if (imageButton == null) return;
        Icon finalIcon = null;
        Icon mediaIcon = (Icon) callMethod(callMethod(mediaAction, "getMediaActionEx"), "getIcon");
        if (mediaIcon != null) {
            finalIcon = mediaIcon;
        } else {
            finalIcon = (Icon) callMethod(mediaAction, "getIcon");
        }
        XposedBridge.log("NowBarMusic setExtraBtnState icon: finalIcon =" + (finalIcon != null));
        if (imageButton != null) {
            imageButton.setImageIcon(finalIcon);
            imageButton.setOnClickListener(v -> handleSemanticButtonClick(mediaAction));
        }
    }

    private void handleSemanticButtonClick(Object mediaAction) {
        // Execute media action
        Runnable action = (Runnable) callMethod(mediaAction, "getAction");
        if (action != null) {
            action.run();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::refreshMediaData, 250);
    }

    private void refreshMediaData() {
        AudioDataProvider.refreshMediaData();
        updateMediaPlaybackState();
    }

    private void setDefaultIcon() {
        Drawable defaultIcon = ResourcesCompat.getDrawable(modRes, R.drawable.ic_volume_eq, appContext.getTheme());
        albumArt.setImageDrawable(defaultIcon);
        albumArt.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        mAlbumArtBig.setImageDrawable(defaultIcon);
        mAlbumArtBig.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#6F161616"));
        mAlbumArtListener.onAlbumArtChanged(background);
        mLocalFavoriteSpace.setVisibility(View.GONE);
        mLocalFavoriteButton.setVisibility(View.GONE);
        mLocalLyricSpace.setVisibility(View.GONE);
        mLocalLyricBtn.setVisibility(View.GONE);
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
        mLocalLyricBtn.setImageTintList(ColorStateList.valueOf(color));
        mLocalFavoriteButton.setImageTintList(ColorStateList.valueOf(color));
        callMethod(mTrackSeekBar, "setProgressColor", ColorStateList.valueOf(color));
        callMethod(mTrackSeekBar, "setThumbColor", ColorStateList.valueOf(color));
        mAppName.setTextColor(color);
        trackTitle.setTextColor(color);
        mCurrentTime.setTextColor(color);
        mTotalTime.setTextColor(color);
    }

    public void setExtendedPlayerEnabled(boolean enabled) {
        if (mExpanded) {
            triggerMediaPlayer();
        }
        mExtendedPlayer = enabled;
    }

}
