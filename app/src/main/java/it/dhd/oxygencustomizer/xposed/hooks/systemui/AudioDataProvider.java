package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;

import android.annotation.SuppressLint;
import android.app.WallpaperColors;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Icon;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import androidx.palette.graphics.Palette;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class AudioDataProvider extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;

    @SuppressLint("StaticFieldLeak")
    public static AudioDataProvider instance = null;

    private final ArrayList<AudioInfoCallbacks> mInfoCallbacks = new ArrayList<>();
    private final ArrayList<MediaMetadataListener> mMediaMetaDataListeners = new ArrayList<>();

    public MediaMetadata mMediaMetadata;
    public int mPlaybackState;
    public Bitmap mArt;
    private String lastSavedPackageName = "";
    private MediaSessionManager mMediaSessionManager;
    private MediaController mActiveController;
    private int mCurrentMediaArtColor = 0;
    private WallpaperColors mWallpaperColors = null;
    private Class<?> mColorSchemeClass = null;
    private Object mCurrentColorScheme = null;
    private Class<?> mOplusMediaControllerImpl = null;
    public Object mMediaData = null;

    private final Handler handler = new Handler();
    private final Runnable periodicTask = new Runnable() {
        @Override
        public void run() {
            updateMediaController();
            handler.postDelayed(this, 1000);
        }
    };

    private final MediaController.Callback mediaControllerCallback = new MediaController.Callback() {
        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            try {
                mMediaData = callStaticMethod(mOplusMediaControllerImpl, "selectPlayingOnes");
            } catch (Throwable ignored) {}
            setArtWork();
            if (mMediaMetadata != metadata) {
                mMediaMetadata = metadata;
                onMediaMetadataChanged();
            }
        }

        @Override
        public void onPlaybackStateChanged(PlaybackState state) {
            try {
                mMediaData = callStaticMethod(mOplusMediaControllerImpl, "selectPlayingOnes");
            } catch (Throwable ignored) {}
            instance.onPlaybackStateChanged();
            if (mPlaybackState == PlaybackState.STATE_NONE) {
                instance.mArt = null;
            }
        }
    };

    public AudioDataProvider(Context context) {
        super(context);
        instance = this;
    }

    public static void registerInfoCallback(AudioInfoCallbacks callback) {
        instance.mInfoCallbacks.add(callback);
    }

    public static void unregisterInfoCallback(AudioInfoCallbacks callback) {
        instance.mInfoCallbacks.remove(callback);
    }

    public void onPlaybackStateChanged() {
        for (MediaMetadataListener callback : mMediaMetaDataListeners) {
            try {
                callback.onPlaybackStateChanged();
            } catch (Throwable ignored) {
            }
        }
    }

    public void onMediaMetadataChanged() {
        for (MediaMetadataListener callback : mMediaMetaDataListeners) {
            try {
                callback.onMediaMetadataChanged();
            } catch (Throwable ignored) {
            }
        }
    }

    public void onMediaColorsChanged(int mediaColor, WallpaperColors wallpaperColors) {
        if (mCurrentMediaArtColor != mediaColor) {
            mCurrentMediaArtColor = mediaColor;
            mWallpaperColors = wallpaperColors;
        }
        for (MediaMetadataListener callback : mMediaMetaDataListeners) {
            try {
                callback.onMediaColorsChanged(mCurrentMediaArtColor, mWallpaperColors, mCurrentColorScheme);
            } catch (Throwable ignored) {
            }
        }
    }

    private void startPeriodicUpdate() {
        handler.post(periodicTask);
    }

    public static void registerMediaMetadataListener(MediaMetadataListener listener) {
        boolean wasEmpty = instance.mMediaMetaDataListeners.isEmpty();
        instance.mMediaMetaDataListeners.add(listener);
        if (wasEmpty) {
            instance.startPeriodicUpdate();
        }
    }

    public static void unregisterMediaMetadataListener(MediaMetadataListener listener) {
        instance.mMediaMetaDataListeners.remove(listener);
        if (instance.mMediaMetaDataListeners.isEmpty()) {
            instance.mActiveController.unregisterCallback(instance.mediaControllerCallback);
            instance.mActiveController = null;
        }
    }

    public static int getPlaybackState() {
        return instance.mPlaybackState;
    }

    public static MediaMetadata getStoredMediaMetadata() {
        return instance.mMediaMetadata;
    }

    public static Bitmap getArt() {
        return instance.mArt;
    }

    @Override
    public void updatePrefs(String... Key) {}

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        mMediaSessionManager = mContext.getSystemService(MediaSessionManager.class);
        ReflectedClass colorScheme = ReflectedClass.of("com.android.systemui.monet.ColorScheme");
        mColorSchemeClass = colorScheme.getClazz();

        // Stole method from KeyguardSliceProvider
        ReflectedClass KeyguardSliceProvider = ReflectedClass.of("com.android.systemui.keyguard.KeyguardSliceProvider");
        KeyguardSliceProvider
                .before("onPrimaryMetadataOrStateChanged")
                        .run(param -> {
                            MediaMetadata metaData = (MediaMetadata) param.args[0];
                            if (mMediaMetadata != metaData) {
                                mMediaMetadata = metaData;
                                setArtWork();
                            }
                            mPlaybackState = (int) param.args[1];
                            onPrimaryMetadataOrStateChanged((int) param.args[1]);
                        });

        // OplusQsMediaUtil
        try {
            ReflectedClass OplusQsMediaUtil = ReflectedClass.of("com.oplus.systemui.qs.media.OplusQsMediaUtil", lpparam.classLoader);
            mOplusMediaControllerImpl = OplusQsMediaUtil.getClazz();
        } catch (Throwable ignored) {}

        ReflectedClass OplusMediaControllerImpl = ReflectedClass.of("com.oplus.systemui.media.OplusMediaControllerImpl");
        OplusMediaControllerImpl
                .after("onMediaDataLoaded")
                .run(param -> {
                    mMediaData = param.args[2];
                    setArtWork();
                });
        OplusMediaControllerImpl
                .after("dispatchMediaDataOnRemove")
                .run(param -> {
                    try {
                        mMediaData = callStaticMethod(mOplusMediaControllerImpl, "selectPlayingOnes");
                    } catch (Throwable ignored) {}
                    mArt = null;
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void onPrimaryMetadataOrStateChanged(int state) {
        for (AudioInfoCallbacks callback : mInfoCallbacks) {
            try {
                callback.onPrimaryMetadataOrStateChanged(state);
            } catch (Throwable ignored) {
            }
        }
    }

    private void setArtWork() {
        MediaMetadata mediaMetadata = instance.mMediaMetadata;
        Object MediaData = null;
        try {
            MediaData = callStaticMethod(mOplusMediaControllerImpl, "selectPlayingOnes");
        } catch (Throwable ignored) {}
        Bitmap art = null;
        if (mediaMetadata != null) {
            art = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) != null ?
                    mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) :
                    mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART) != null ?
                            mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART) :
                            mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        }
        Icon artIcon;
        if (MediaData != null) {
            artIcon = (Icon) callMethod(MediaData, "getArtwork");
            if (artIcon != null && art == null) {
                art = artIcon != null ? DrawableConverter.drawableToBitmap(artIcon.loadDrawable(mContext)) : null;
            }
        }
        instance.mArt = art;
        updateMediaColors();
    }

    public void updateMediaColors() {

        WallpaperColors wallpaperColors = (instance.mArt != null) ? WallpaperColors.fromBitmap(instance.mArt) : null;
        if (wallpaperColors == null || wallpaperColors.equals(mWallpaperColors)) return;

        Configuration config = mContext.getResources().getConfiguration();
        int currentNightMode = config.uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean isDarkThemeOn = (currentNightMode == Configuration.UI_MODE_NIGHT_YES);

        Object colorScheme = null;
        try {
            if (Build.VERSION.SDK_INT >= 36) colorScheme = XposedHelpers.newInstance(mColorSchemeClass, wallpaperColors, isDarkThemeOn, 1);
            else colorScheme = XposedHelpers.newInstance(mColorSchemeClass, wallpaperColors, isDarkThemeOn);
        } catch (Throwable t) {
            XposedBridge.log("AudioDataProvider Error: " + Log.getStackTraceString(t));
        }
        if (colorScheme == null) return;
        mCurrentColorScheme = colorScheme;
        if (Build.VERSION.SDK_INT == 33) {
            Palette.Builder builder = new Palette.Builder(mArt);
            builder.generate(palette -> {
                int color = palette.getDominantColor(Color.WHITE);
                onMediaColorsChanged(color, wallpaperColors);
            });
        } else {
            int newMediaArtColor;
            newMediaArtColor = (int) (isDarkThemeOn ? callMethod(callMethod(colorScheme, "getAccent1"), "getS100") : callMethod(callMethod(colorScheme, "getAccent1"), "getS800"));
            onMediaColorsChanged(newMediaArtColor, wallpaperColors);
        }
    }

    public static Object getColorScheme() {
        return instance.mCurrentColorScheme;
    }

    public static Object getMediaData() {
        return instance.mMediaData;
    }

    public static void refreshMediaData() {
        try {
            instance.mMediaData = callStaticMethod(instance.mOplusMediaControllerImpl, "selectPlayingOnes");
        } catch (Throwable ignored) {}
    }

    public static String getLastNonNullPackageName() {
        return instance.lastSavedPackageName;
    }

    private void notifyListeners() {
        // Store the last used media package name
        saveLastNonNullPackageName();
        for (MediaMetadataListener listener : mMediaMetaDataListeners) {
            listener.onMediaMetadataChanged();
            listener.onPlaybackStateChanged();
            listener.onMediaColorsChanged(mCurrentMediaArtColor, mWallpaperColors, mCurrentColorScheme);
        }
    }

    public void seekTo(long time) {
        MediaController controller = getActiveLocalMediaController();
        if (controller != null) {
            controller.getTransportControls().seekTo(time);
        }
    }

    public long getCurrentTime() {
        MediaController controller = getActiveLocalMediaController();
        return controller != null ?
            controller.getPlaybackState() != null ?
                controller.getPlaybackState().getPosition() :
                    1L :
                1L;
    }

    public static long getTotalDuration() {
        MediaMetadata metadata = getMediaMetadata();
        return (metadata != null) ? metadata.getLong(MediaMetadata.METADATA_KEY_DURATION) : 0L;
    }

    private void saveLastNonNullPackageName() {
        try {
            MediaController controller = getActiveLocalMediaController();
            String packageName = controller != null ? controller.getPackageName() : null;
            if (!TextUtils.isEmpty(packageName) && !packageName.equals(lastSavedPackageName)) {
                lastSavedPackageName = packageName;
            }
        } catch (Throwable ignored) {} // rare case when the controller is null while saving the package name
    }

    public void updateMediaController() {
        MediaController localController = getActiveLocalMediaController();
        if (localController != null && !sameSessions(mActiveController, localController)) {
            if (mActiveController != null) {
                mActiveController.unregisterCallback(mediaControllerCallback);
            }
            mActiveController = localController;
            mActiveController.registerCallback(mediaControllerCallback);
            notifyListeners();
        } else if (localController == null && mActiveController != null) {
            mActiveController.unregisterCallback(mediaControllerCallback);
            mActiveController = null;
            notifyListeners();
        }
    }

    private boolean sameSessions(MediaController a, MediaController b) {
        if (a == b) {
            return true;
        }
        if (a == null) {
            return false;
        }
        if (b == null) {
            return false;
        }
        return a.equals(b);
    }

    private MediaController getActiveLocalMediaController() {
        MediaController localController = null;
        List<String> remoteMediaSessionLists = new ArrayList<>();
        if (mMediaSessionManager != null) {
            for (MediaController controller : mMediaSessionManager.getActiveSessions(null)) {
                MediaController.PlaybackInfo playbackInfo = controller.getPlaybackInfo();
                if (playbackInfo == null) {
                    continue;
                }
                PlaybackState playbackState = controller.getPlaybackState();
                if (playbackState == null) {
                    continue;
                }
                if (playbackState.getState() != PlaybackState.STATE_PLAYING) {
                    continue;
                }
                if (playbackInfo.getPlaybackType() == MediaController.PlaybackInfo.PLAYBACK_TYPE_REMOTE) {
                    if (localController != null && localController.getPackageName().equals(controller.getPackageName())) {
                        localController = null;
                    }
                    if (!remoteMediaSessionLists.contains(controller.getPackageName())) {
                        remoteMediaSessionLists.add(controller.getPackageName());
                    }
                    continue;
                }
                if (playbackInfo.getPlaybackType() == MediaController.PlaybackInfo.PLAYBACK_TYPE_LOCAL) {
                    if (localController == null && !remoteMediaSessionLists.contains(controller.getPackageName())) {
                        localController = controller;
                    }
                }
            }
        }
        return localController;
    }

    public static Bitmap getMediaBitmap() {
        MediaMetadata metadata = getMediaMetadata();
        if (metadata != null) {
            Bitmap bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
            if (bitmap != null) {
                return bitmap;
            }
            bitmap = metadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
            if (bitmap != null) {
                return bitmap;
            }
            return metadata.getBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON);
        }
        return null;
    }

    public static MediaMetadata getMediaMetadata() {
        MediaController controller = instance.getActiveLocalMediaController();
        return controller != null ? controller.getMetadata() : null;
    }

    public int getMediaColor() {
        return mCurrentMediaArtColor;
    }

    public boolean isMediaControllerAvailable() {
        MediaController controller = getActiveLocalMediaController();
        return controller != null && !TextUtils.isEmpty(controller.getPackageName());
    }

    public static boolean isMediaPlaying() {
        try {
            return instance.isMediaControllerAvailable() && instance.getMediaControllerPlaybackState(instance.getActiveLocalMediaController()) == PlaybackState.STATE_PLAYING;
        } catch (Throwable t) {
            XposedBridge.log("AudioDataProvider Error: " + Log.getStackTraceString(t));
            return false;
        }
    }

    public int getMediaControllerPlaybackState(MediaController controller) {
        if (controller == null) return PlaybackState.STATE_NONE;
        try {
            PlaybackState state = controller.getPlaybackState();
            return state != null ? state.getState() : PlaybackState.STATE_NONE;
        } catch (Throwable ignored) {
            // PlaybackState changed state while checking
            return PlaybackState.STATE_NONE;
        }
    }

    public PlaybackState getMediaControllerPlaybackState() {
        MediaController controller = getActiveLocalMediaController();
        return controller != null ? controller.getPlaybackState() : null;
    }

    private void dispatchMediaKeyWithWakeLockToMediaSession(final int keycode) {
        Intent keyIntent = new Intent(Intent.ACTION_MEDIA_BUTTON, null);
        KeyEvent keyEvent = new KeyEvent(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), KeyEvent.ACTION_DOWN, keycode, 0);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        KeyEvent mediaEvent = new KeyEvent(KeyEvent.ACTION_DOWN, keycode);
        SystemUtils.AudioManager().dispatchMediaKeyEvent(mediaEvent);

        mediaEvent = KeyEvent.changeAction(mediaEvent, KeyEvent.ACTION_UP);
        keyIntent.putExtra(Intent.EXTRA_KEY_EVENT, keyEvent);
        SystemUtils.AudioManager().dispatchMediaKeyEvent(mediaEvent);

    }

    public void prevSong() {
        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    public void nextSong() {
        dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    public void toggleMediaPlaybackState() {
        if (isMediaPlaying()) {
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PAUSE);
        } else {
            dispatchMediaKeyWithWakeLockToMediaSession(KeyEvent.KEYCODE_MEDIA_PLAY);
        }
        onPlaybackStateChanged();
    }


    public interface AudioInfoCallbacks {
        void onPrimaryMetadataOrStateChanged(int state);
    }

    public interface MediaMetadataListener {
        void onMediaMetadataChanged();
        void onPlaybackStateChanged();
        void onMediaColorsChanged(int mediaColor, WallpaperColors wallpaperColors, Object colorScheme);
    }
}
