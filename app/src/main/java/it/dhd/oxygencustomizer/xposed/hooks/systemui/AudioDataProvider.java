package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaMetadata;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class AudioDataProvider extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    @SuppressLint("StaticFieldLeak")
    private static AudioDataProvider instance = null;
    private final ArrayList<AudioInfoCallbacks> mInfoCallbacks = new ArrayList<>();
    public MediaMetadata mMediaMetadata;
    public int mPlaybackState;
    public Bitmap mArt;

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

    public static int getPlaybackState() {
        return instance.mPlaybackState;
    }

    public static MediaMetadata getMediaMetadata() {
        return instance.mMediaMetadata;
    }

    public static Bitmap getArt() {
        return instance.mArt;
    }

    @Override
    public void updatePrefs(String... Key) {
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // Stole method from KeyguardSliceProvider
        Class<?> KeyguardSliceProvider = findClass("com.android.systemui.keyguard.KeyguardSliceProvider", lpparam.classLoader);
        hookAllMethods(KeyguardSliceProvider, "onPrimaryMetadataOrStateChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //log("onPrimaryMetadataOrStateChanged: PlaybackState: " + param.args[1] + " Metadata: " + param.args[0]);
                MediaMetadata metaData = (MediaMetadata) param.args[0];
                if (mMediaMetadata != metaData) {
                    mMediaMetadata = metaData;
                    mArt = getArtWork();
                }
                mPlaybackState = (int) param.args[1];
                onPrimaryMetadataOrStateChanged((int) param.args[1]);
            }
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

    private Bitmap getArtWork() {
        MediaMetadata mediaMetadata = instance.mMediaMetadata;
        if (mediaMetadata == null) {
            return null;
        }
        Bitmap art = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (art == null) {
            return mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
        }
        return art;
    }

    public interface AudioInfoCallbacks {
        void onPrimaryMetadataOrStateChanged(int state);
    }
}
