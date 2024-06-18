package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getMediaMetadata;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getPlaybackState;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadata;
import android.media.session.PlaybackState;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.views.VisualizerView;
import it.dhd.oxygencustomizer.xposed.views.pulse.PulseControllerImpl;

public class AlbumArtLockscreen extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private boolean showAlbumArt = true;
    private int albumArtFilter = 0; // 0 = None, 1 = Blur, 2 = Darken, 3 = Lighten
    private float albumArtBlurAmount = 7.5f;
    private FrameLayout albumArtContainer;
    private ImageView albumArtView;
    private Object mScrimController;
    private MediaMetadata mMediaMetaData;
    private Bitmap mAlbumArt;
    private boolean shouldShowArt = false;

    public AlbumArtLockscreen(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showAlbumArt = Xprefs.getBoolean("lockscreen_album_art", false);
        albumArtFilter = Integer.parseInt(Xprefs.getString("lockscreen_album_art_filter", "0"));
        albumArtBlurAmount = (Xprefs.getSliderInt("lockscreen_media_blur", 30)/100f) * 25f;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        // Get mediadata change
        AudioDataProvider.registerInfoCallback(this::onPrimaryMetadataOrStateChanged);

        // Get Scrim Controller for checking Scrim Change
        Class<?> ScrimControllerClassEx = findClass("com.android.systemui.statusbar.phone.ScrimControllerEx", lpparam.classLoader);
        hookAllConstructors(ScrimControllerClassEx, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mScrimController = param.thisObject;
            }
        });

        // Hook Central Surfaces so we can put the new view
        Class<?> CentralSurfacesImplClass = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpparam.classLoader);
        hookAllMethods(CentralSurfacesImplClass, "start", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {


                View scrimBehind = (View) getObjectField(callMethod(mScrimController, "getScrimController"), "mScrimBehind");
                ViewGroup rootView = (ViewGroup) scrimBehind.getParent();

                albumArtContainer = new FrameLayout(mContext);
                albumArtContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                albumArtView = new ImageView(mContext);
                albumArtView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                albumArtView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumArtContainer.addView(albumArtView);

                rootView.addView(albumArtContainer, 2);

            }
        });

        // Stole Keyguard is showing
        Class<?> KayguardUpdateMonitor = findClass("com.android.keyguard.KeyguardUpdateMonitor", lpparam.classLoader);
        hookAllMethods(KayguardUpdateMonitor, "setKeyguardShowing", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                shouldShowArt = (boolean) param.args[0];
            }
        });

        // Get Scrim Change
        Class<?> ScrimControllerClass = findClass("com.android.systemui.statusbar.phone.ScrimController", lpparam.classLoader);
        hookAllMethods(ScrimControllerClass, "applyAndDispatchState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                updateAlbumArt();
            }
        });

        Class<?> QSImplClass = findClass("com.android.systemui.qs.QSFragment", lpparam.classLoader);
        hookAllMethods(QSImplClass, "setQsExpansion", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if((boolean) callMethod(param.thisObject, "isKeyguardState"))
                {
                    updateAlbumArt();
                }
            }
        });

    }

    private void updateAlbumArt() {
        if (shouldShowArt) {
            // Keyguard so we can show album art
            albumArtView.post(() -> albumArtView.setVisibility(View.VISIBLE));
        } else {
            // Not Keyguard so we must hide album art
            albumArtView.post(() -> albumArtView.setVisibility(View.GONE));
        }
    }

    private void onPrimaryMetadataOrStateChanged(int state) {
        log("AlbumArtLockscreen: PlaybackState: " + getPlaybackState() + " Metadata: " + (getMediaMetadata() != null));
        boolean isMusicActive = false;
        if (SystemUtils.AudioManager() != null) {
            isMusicActive = SystemUtils.AudioManager().isMusicActive();
        }
        boolean isPlaying = (isMusicActive || state == PlaybackState.STATE_PLAYING);
        if (showAlbumArt && isPlaying) {
            if (mMediaMetaData != getMediaMetadata()) {
                mMediaMetaData = getMediaMetadata();
                mAlbumArt = getArt(mMediaMetaData);
            }
            Bitmap filteredArt = getArtFilter(mAlbumArt);
            albumArtView.setImageBitmap(filteredArt);
        } else {
            albumArtView.setImageBitmap(null);
        }
        updateAlbumArt();
    }

    private Bitmap getArt(MediaMetadata mediaMetadata) {
        if (mediaMetadata == null) {
            return null;
        }
        Bitmap art = mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART);
        if (art == null) {
            return mediaMetadata.getBitmap(MediaMetadata.METADATA_KEY_ART);
        }
        return art;
    }
    
    private Bitmap getArtFilter(Bitmap art) {
        Bitmap finalArt;
        switch (albumArtFilter) {
            default -> finalArt = art;
            case 1 -> finalArt = DrawableConverter.toGrayscale(art);
            case 2 -> {
                finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                        getPrimaryColor(mContext));
            }
            case 3 -> finalArt = DrawableConverter.getBlurredImage(mContext, art, albumArtBlurAmount);
            case 4 -> finalArt = DrawableConverter.getGrayscaleBlurredImage(mContext, art, albumArtBlurAmount);
        };
        return finalArt;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
