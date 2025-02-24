package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getColorScheme;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getLastNonNullPackageName;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableLinearLayout;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaMetadata;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.CircleFramedDrawable;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class NowBarMusic extends LinearLayout {

    @SuppressLint("StaticFieldLeak")
    public static NowBarMusic instance = null;

    private final Context mContext;

    private final String NO_MEDIA_ID = "oplus_qs_media_panel_title_default";
    private final String NO_MEDIA_STRING;

    private ImageView albumArt;
    private ImageButton prevButton;
    private ImageButton playPauseButton;
    private ImageButton nextButton;
    private TextView trackTitle;
    private Context appContext;
    private LinearLayout mNowBarLayout;
    private Object mCurrentColorScheme = null;
    private final ActivityLauncherUtils mActivityLauncherUtils;

    public NowBarMusic(Context context) {
        super(context);
        instance = this;
        mContext = context;
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

    public static NowBarMusic getInstance(Context context) {
        if (instance == null) {
            instance = new NowBarMusic(context);
        }
        return instance;
    }

    private void init() {
        XposedBridge.log("NowBarMusic init");
        setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        try {
            mNowBarLayout = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(mContext);
        } catch (Exception e) {
            mNowBarLayout = new LinearLayout(mContext);
        }

        mNowBarLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
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

        albumArt = (ImageView) ViewHelper.findViewWithTag(v, "albumArt");
        trackTitle = (TextView) ViewHelper.findViewWithTag(v, "trackTitle");
        prevButton = (ImageButton) ViewHelper.findViewWithTag(v, "previous_action");
        playPauseButton = (ImageButton) ViewHelper.findViewWithTag(v, "play_pause");
        nextButton = (ImageButton) ViewHelper.findViewWithTag(v, "next_action");
        mNowBarLayout.addView(v);
        setBarBackground(Color.BLACK);

        albumArt.setOnClickListener(v1 -> mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false));
        prevButton.setOnClickListener(v1 -> AudioDataProvider.instance.prevSong());
        playPauseButton.setOnClickListener(v1 -> AudioDataProvider.instance.toggleMediaPlaybackState());
        nextButton.setOnClickListener(v1 -> AudioDataProvider.instance.nextSong());

        setDefaultIcon();
        trackTitle.setText(NO_MEDIA_STRING);

//        setOnClickListener(v -> showMediaDialog(mNowBarLayout));
        setOnLongClickListener(v1 -> {
            mActivityLauncherUtils.launchApp(getLastNonNullPackageName(), false);
            return true;
        });
        addView(mNowBarLayout);

        AudioDataProvider.registerMediaMetadataListener(mMediaMetaDataListener);
    }

    private void setBarBackground(int color) {
        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(100f);
        setBackground(background);
    }

    private void updateMediaPlaybackState() {
        XposedBridge.log("NowBarMusic updateMediaPlaybackState");
        MediaMetadata mediaMetadata = AudioDataProvider.getMediaMetadata();
        boolean isPlaying = AudioDataProvider.isMediaPlaying();

        if (resetMediaIfNeeded()) {
            return;
        }

        playPauseButton.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_pause, null));

        if (mediaMetadata != null) {
            String title = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE);
            trackTitle.setText(title != null && !title.isEmpty() ? title : modRes.getString(R.string.omnijaws_city_unknown));
            if (!trackTitle.isSelected()) trackTitle.setSelected(true);

            Bitmap bitmap = getArt();
            XposedBridge.log("bitmap getArt() null? " + (bitmap == null));
            if (bitmap != null) {
                int roundSize = (int) modRes.getDimension(R.dimen.nowbar_album_art_size);
                CircleFramedDrawable drawable = new CircleFramedDrawable(bitmap, roundSize);
                albumArt.setImageDrawable(drawable);
                albumArt.clearColorFilter();
            } else {
                setDefaultIcon();
            }
        }
    }

    private boolean resetMediaIfNeeded() {
        if (!AudioDataProvider.isMediaPlaying()) {
            playPauseButton.setBackground(ResourcesCompat.getDrawable(modRes, R.drawable.ic_play, null));
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
        prevButton.setImageTintList(ColorStateList.valueOf(color));
        nextButton.setImageTintList(ColorStateList.valueOf(color));
        playPauseButton.setBackgroundTintList(ColorStateList.valueOf(color));
        trackTitle.setTextColor(color);
    }

}
