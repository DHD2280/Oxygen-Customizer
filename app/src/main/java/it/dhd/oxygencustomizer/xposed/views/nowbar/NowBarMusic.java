package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.NOW_BAR_CLOCK_FONT_FILE;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPLauncher.moduleResources;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getColorScheme;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getLastNonNullPackageName;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getMediaData;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableLinearLayout;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getMediaActionBinder;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.WallpaperColors;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Space;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

import com.coui.appcompat.seekbar.COUISeekBar;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.CircleFramedDrawable;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.TimeUtils;
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
    private FrameLayout mClockContainer;
    private LinearLayout mBigMediaPlayer, mLittleMediaPlayer;
    private LinearLayout mSeekBarContainer;
    private LinearLayout mAppInfos, mCompactInfos;

    // Clock
    private float mDateSize, mClockSize;
    private TextClock mTopDate;
    private TextClock mBottomDate;
    private TextView mHours;
    private TextClock mTicker;

    // Texts
    private TextView trackTitle;
    private TextView mTitle, mAuthor;
    private TextView mCurrentTime, mTotalTime;
    private TextView mCompactTitle, mCompactAuthor;

    // App Info
    private TextView mAppName;
    private ImageView mAppIcon;

    // SeekBar
    private COUISeekBar mTrackSeekBar;

    // Vars
    private boolean mExtendedPlayer = true;
    private int mBackgroundColor = Color.BLACK;
    private boolean mIsSeeking = false;
    private int mSeekingProgress = 0;
    private Object mCurrentColorScheme = null;
    private boolean mExpanded = false;
    private String mLastTitle = "";
    private String mLastAuthor = "";
    private int mExtendedPlayerMode = 0;
    private final int MODE_LARGE = 0;
    private final int MODE_COMPACT = 1;
    private boolean mShowClock = false;
    private int mDatePosition = 1;
    private String mDateFormat = "";
    private float mClockTextScaling = 1.0f;
    private boolean mCustomFont = false;
    private int mClockTopMargin = 38;

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

        mClockContainer = (FrameLayout) ViewHelper.findViewWithTag(v, "clock_container");
        mTopDate = (TextClock) ViewHelper.findViewWithTag(v, "top_date");
        mHours = (TextView) ViewHelper.findViewWithTag(v, "clock_hours_text");
        mBottomDate = (TextClock) ViewHelper.findViewWithTag(v, "bottom_date");
        mDateSize = mTopDate.getTextSize();
        mClockSize = mHours.getTextSize();
        mTicker = (TextClock) ViewHelper.findViewWithTag(v, "clock_hours_tick");
        TimeUtils.setCurrentTimeTextClockRed(mTicker, mHours, Color.parseColor("#FFF50514"));
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
        mCompactTitle = (TextView) ViewHelper.findViewWithTag(v, "title_medium");
        mCompactAuthor = (TextView) ViewHelper.findViewWithTag(v, "album_medium");
        // Big Media Player
        mCompactInfos = (LinearLayout) ViewHelper.findViewWithTag(v, "compact_infos");
        mAppInfos = (LinearLayout) ViewHelper.findViewWithTag(v, "app_infos");
        mAppIcon = (ImageView) ViewHelper.findViewWithTag(v, "app_icon");
        mAppName = (TextView) ViewHelper.findViewWithTag(v, "app_name");
        mCurrentTime = (TextView) ViewHelper.findViewWithTag(v, "current_position");
        mTotalTime = (TextView) ViewHelper.findViewWithTag(v, "total_duration");
        mSeekBarContainer = (LinearLayout) ViewHelper.findViewWithTag(v, "seek_bar_container");
        mTrackSeekBar = new COUISeekBar(mContext);
        mTrackSeekBar.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        mTrackSeekBar.setMin(0);
        mTrackSeekBar.setMax(100);
        mTrackSeekBar.setOnSeekBarChangeListener(mSeekbarListener);
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

        mLittleMediaPlayer.setOnLongClickListener(v1 -> {
            mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false);
            return true;
        });
        mBigMediaPlayer.setOnLongClickListener(v1 -> {
            mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false);
            return true;
        });
        mAlbumArtBig.setOnClickListener(v1 -> triggerMediaPlayer());
        albumArt.setOnClickListener(v1 -> triggerMediaPlayer());
        mPrevButton.setOnClickListener(v1 -> AudioDataProvider.instance.prevSong());
        mPlayPauseButton.setOnClickListener(v1 -> AudioDataProvider.instance.toggleMediaPlaybackState());
        mNextButton.setOnClickListener(v1 -> AudioDataProvider.instance.nextSong());
        mPrevButtonBig.setOnClickListener(v1 -> AudioDataProvider.instance.prevSong());
        mPlayPauseButtonBig.setOnClickListener(v1 -> AudioDataProvider.instance.toggleMediaPlaybackState());
        mNextButtonBig.setOnClickListener(v1 -> AudioDataProvider.instance.nextSong());

        setDefaultIcon();
        trackTitle.setText(NO_MEDIA_STRING);
        mTitle.setText(NO_MEDIA_STRING);
        mCompactTitle.setText(NO_MEDIA_STRING);

        addView(mNowBarLayout);

        AudioDataProvider.registerMediaMetadataListener(mMediaMetaDataListener);
    }

    static COUISeekBar.OnSeekBarChangeListener mSeekbarListener = new COUISeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(COUISeekBar cOUISeekBar, int progress, boolean fromUser) {
            if (fromUser) {
                instance.mIsSeeking = true;
                instance.mSeekingProgress = progress;
                instance.mCurrentTime.setText(instance.formatTime((long) ((instance.mSeekingProgress / 100f) * AudioDataProvider.getTotalDuration())));
            }
        }

        @Override
        public void onStartTrackingTouch(COUISeekBar cOUISeekBar) {
            instance.mIsSeeking = true;
        }

        @Override
        public void onStopTrackingTouch(COUISeekBar cOUISeekBar) {
            if (instance.mIsSeeking) {
                instance.mIsSeeking = false;
                long time = (long) ((instance.mSeekingProgress / 100f) * AudioDataProvider.getTotalDuration());
                AudioDataProvider.instance.seekTo(time);
            }
        }
    };

    private void triggerMediaPlayer() {
        if (!mExtendedPlayer) return;
        mExpanded = !mExpanded;
        List<View> views = new ArrayList<>(List.of(mAlbumArtBig, mBigMediaPlayer));
        if (mExtendedPlayerMode == MODE_LARGE) {
            views.addAll(List.of(mTitle, mAuthor));
        }
        if (mShowClock) views.add(mClockContainer);
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
                        animateOthers(views);
                        setBarBackground(mBackgroundColor);
                    })
                    .start();
        } else {
            for (View v : views) {
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

    private void animateOthers(List<View> views) {
        for (View v : views) {
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
            if (v instanceof TextView text) {
                text.setSelected(true);
            }
        }
    }

    private void setBarBackground(int color) {
        if (mBackgroundColor != color) mBackgroundColor = color;
        GradientDrawable background = new GradientDrawable();
        background.setColor(mBackgroundColor);
        background.setCornerRadius(100f);
        mBigMediaPlayer.setBackground(background);
        mLittleMediaPlayer.setBackground(background);

    }

    private void updateMediaPlaybackState() {
        MediaMetadata mediaMetadata = AudioDataProvider.getMediaMetadata();
        if (resetMediaIfNeeded()) {
            return;
        }

        mPlayPauseButton.setBackground(ResourcesCompat.getDrawable(moduleResources, R.drawable.ic_pause, null));
        mPlayPauseButtonBig.setBackground(ResourcesCompat.getDrawable(moduleResources, R.drawable.ic_pause, null));

        Object mediaData = getMediaData();
        try {
            bindActions(mediaData);
        } catch (Throwable t) {
            XposedBridge.log("NowBarMusic bindActions: " + Log.getStackTraceString(t));
        }

        if (mediaMetadata != null) {
            String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            String artist = mediaMetadata.getString(MediaMetadata.METADATA_KEY_ARTIST);
            if (!TextUtils.isEmpty(title) && !title.equals(mLastTitle) && !TextUtils.equals(title, trackTitle.getText())) {
                mLastTitle = title;
                trackTitle.setText(title);
                mTitle.setText(title);
                mCompactTitle.setText(title);
            } else if (TextUtils.isEmpty(title)) {
                trackTitle.setText(moduleResources.getString(R.string.omnijaws_city_unknown));
                mTitle.setText(moduleResources.getString(R.string.omnijaws_city_unknown));
                mCompactTitle.setText(moduleResources.getString(R.string.omnijaws_city_unknown));
            }
            trackTitle.setSelected(true);
            mCompactTitle.setSelected(true);
            if (!TextUtils.isEmpty(artist) && !artist.equals(mLastAuthor) && !TextUtils.equals(artist, mAuthor.getText())) {
                mLastAuthor = artist;
                mAuthor.setText(artist);
                mCompactAuthor.setText(artist);
                mCompactAuthor.setSelected(true);
            } else if (TextUtils.isEmpty(artist)) {
                mAuthor.setText(moduleResources.getString(R.string.omnijaws_city_unknown));
                mCompactAuthor.setText(moduleResources.getString(R.string.omnijaws_city_unknown));
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

            Bitmap bitmap = getArt();
            if (bitmap != null) {
                mAlbumArtBig.clearColorFilter();
                RoundedBitmapDrawable roundedDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), bitmap);
                roundedDrawable.setCornerRadius(32f);
                roundedDrawable.setAntiAlias(true);
                mAlbumArtBig.setImageDrawable(roundedDrawable);
                mAlbumArtListener.onAlbumArtChanged(roundedDrawable);
                int roundSize = (int) moduleResources.getDimension(R.dimen.nowbar_album_art_size);
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
            mPlayPauseButton.setBackground(ResourcesCompat.getDrawable(moduleResources, R.drawable.ic_play, null));
            mPlayPauseButtonBig.setBackground(ResourcesCompat.getDrawable(moduleResources, R.drawable.ic_play, null));
            mCurrentTime.setText("0:00");
            mTotalTime.setText("0:00");
            mTrackSeekBar.setProgress(0);
            mTitle.setText(NO_MEDIA_STRING);
            mCompactTitle.setText(NO_MEDIA_STRING);
            mLastTitle = "";
            mAuthor.setText("");
            mCompactAuthor.setText("");
            mLastAuthor = "";
            trackTitle.setText(NO_MEDIA_STRING);
            trackTitle.setSelected(true);
            mAppIcon.setImageDrawable(null);
            mAppName.setText("");
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

    private void bindActions(Object mediaData) throws Throwable {
        if (mediaData == null) return;
        List<Object> actions = (List<Object>) callMethod(mediaData, "getActions");
        if (Build.VERSION.SDK_INT <= 35) {
            if (actions == null || actions.isEmpty()) return;
        }
        if (Build.VERSION.SDK_INT >= 36) {
            Object activityStarter = getActivityStarterExternal();
            Object OplusMediaActions = callStaticMethod(getMediaActionBinder(), "createQSMediaActions", mediaData, activityStarter);
            if (OplusMediaActions == null) return;
            actions = (List<Object>) callMethod(OplusMediaActions, "getNotNullActions");
            if (actions == null || actions.isEmpty()) return;
        }
        int size = actions.size();
        if (size < 4) {
            return;
        }

        boolean isRtl = mContext.getResources().getConfiguration().getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        if (size == 4) {
            bindMediaNotificationButton(mediaData, actions.get(Build.VERSION.SDK_INT <= 35 ? 0 : 3), null);
        } else {
            bindMediaNotificationButton(mediaData, actions.get(isRtl ? 4 : 0), actions.get(isRtl ? 0 : 4));
        }
    }

    public final void bindMediaNotificationButton(Object mediaData, Object mediaAction, Object mediaAction1) throws Throwable {

        mLocalFavoriteButton.setVisibility(mediaAction == null ? View.GONE : View.VISIBLE);
        mLocalFavoriteSpace.setVisibility(mediaAction == null ? View.GONE : View.VISIBLE);
        mLocalLyricBtn.setVisibility(mediaAction1 == null ? View.GONE : View.VISIBLE);
        mLocalLyricSpace.setVisibility(mediaAction1 == null ? View.GONE : View.VISIBLE);

        if (mediaAction != null) {
            setExtraBtnState(mLocalFavoriteButton, mediaAction, mediaData);
        }
        if (mediaAction1 != null) {
            setExtraBtnState(mLocalLyricBtn, mediaAction1, mediaData);
        }
    }

    private void setExtraBtnState(final ImageButton imageButton, final Object mediaAction, final Object mediaData) throws Throwable {
        if (imageButton == null) return;
        Icon finalIcon = null;
        Object mediaActionEx = null;
        try {
            mediaActionEx = callMethod(mediaAction, "getMediaActionEx");
        } catch (Throwable t) {
            mediaActionEx = getObjectField(mediaAction, "mediaActionEx");
        }
        Icon mediaIcon = (Icon) callMethod(mediaActionEx, "getIcon");
        Drawable drawable = (Drawable) callMethod(mediaIcon, "loadDrawableAsUser", mContext, callStaticMethod(ActivityManager.class, "getCurrentUser"));
        if (drawable == null) {
            Icon mediaIcon2 = (Icon) callMethod(mediaAction, "getIcon");
            drawable = mediaIcon2.loadDrawable(mContext);
        }
        if (imageButton != null) {
            imageButton.setImageDrawable(drawable);
            imageButton.setOnClickListener(v -> handleSemanticButtonClick(mediaAction));
        }
    }

    private void handleSemanticButtonClick(Object mediaAction) {
        // Execute media action
        Runnable action = (Runnable) callMethod(mediaAction, "getAction");
        if (action != null) {
            action.run();
        }
        new Handler(Looper.getMainLooper()).postDelayed(this::refreshMediaData, 500);
    }

    private void refreshMediaData() {
        AudioDataProvider.refreshMediaData();
        updateMediaPlaybackState();
    }

    private void setDefaultIcon() {
        Drawable defaultIcon = ResourcesCompat.getDrawable(moduleResources, R.drawable.ic_volume_eq, appContext.getTheme());
        albumArt.setImageDrawable(defaultIcon);
        albumArt.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        mAlbumArtBig.setImageDrawable(defaultIcon);
        mAlbumArtBig.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
        GradientDrawable background = new GradientDrawable();
        background.setColor(Color.parseColor("#6F161616"));
        mAlbumArtListener.onAlbumArtChanged(background);
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
        public void onMediaColorsChanged(int mediaColor, WallpaperColors wallpaperColors, Object colorScheme) {
            if (resetMediaIfNeeded()) {
                return;
            }
            mCurrentColorScheme = getColorScheme();
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
        mTrackSeekBar.setProgressColor(ColorStateList.valueOf(color));
        mTrackSeekBar.setThumbColor(ColorStateList.valueOf(color));
        mAppName.setTextColor(color);
        trackTitle.setTextColor(color);
        mCurrentTime.setTextColor(color);
        mTotalTime.setTextColor(color);
        mCompactTitle.setTextColor(color);
        mCompactAuthor.setTextColor(color);
    }

    public void setExtendedPlayerEnabled(boolean enabled) {
        if (mExpanded) {
            triggerMediaPlayer();
        }
        mExtendedPlayer = enabled;
    }

    public void setExtendedPlayerOptions(
            int playerMode, boolean showClock,
            int datePosition, String dateFormat, float textScaling, boolean customFont, int clockTopMargin) {
        mExtendedPlayerMode = playerMode;
        mAppInfos.setVisibility(playerMode == MODE_LARGE ? View.VISIBLE : View.GONE);
        mCompactInfos.setVisibility(playerMode == MODE_COMPACT ? View.VISIBLE : View.GONE);
        mTitle.setVisibility(playerMode == MODE_LARGE ? View.VISIBLE : View.INVISIBLE);
        mAuthor.setVisibility(playerMode == MODE_LARGE ? View.VISIBLE : View.INVISIBLE);
        mClockContainer.setVisibility(showClock ? View.VISIBLE : View.GONE);
        mShowClock = showClock;
        mDatePosition = datePosition;
        mDateFormat = dateFormat;
        mCustomFont = customFont;
        mClockTopMargin = clockTopMargin;
        mClockTextScaling = textScaling;
        setupClock();
        if (mExpanded) {
            triggerMediaPlayer();
        }
    }

    private void setupClock() {
        mTopDate.setFormat12Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        mTopDate.setFormat24Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        mBottomDate.setFormat12Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        mBottomDate.setFormat24Hour(TextUtils.isEmpty(mDateFormat) ? "EEEE" : mDateFormat);
        setupFont(mTopDate);
        setupFont(mHours);
        setupFont(mBottomDate);
        mTopDate.setVisibility(mDatePosition == 0 ? View.VISIBLE : View.GONE);
        mBottomDate.setVisibility(mDatePosition == 1 ? View.VISIBLE : View.GONE);
        setMargins(mHours.getParent(), mContext, 0, mClockTopMargin, 0, 0);
        ViewHelper.applyTextScalingRecursively(mClockContainer, mClockTextScaling);
    }

    private void setupFont(TextView tv) {
        Typeface typeface = null;
        if (mCustomFont && new File(NOW_BAR_CLOCK_FONT_FILE).exists()) {
            typeface = Typeface.createFromFile(new File(NOW_BAR_CLOCK_FONT_FILE));
        } else {
            typeface = ResourcesCompat.getFont(appContext, R.font.slateforoneplus);
        }
        tv.setTypeface(typeface);
        if (tv == mHours) {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mClockSize * mClockTextScaling);
        } else {
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDateSize * mClockTextScaling);
        }
    }

}
