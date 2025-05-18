package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider.getMediaMetadata;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.session.PlaybackState;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class AlbumArtLockscreen extends XposedMods {

    private final static String TAG = "AlbumArtLockscreen: ";

    private static final String listenPackage = SYSTEM_UI;
    public static boolean showAlbumArt = true;
    public static boolean canShowArt = false;
    private Bitmap mArt;
    private int albumArtFilter = 0;
    private float albumArtBlurAmount = 7.5f;
    private FrameLayout albumArtContainer;
    private ImageView albumArtView;
    private Object mScrimController;
    private boolean shouldShowArt = false;

    public AlbumArtLockscreen(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showAlbumArt = Xprefs.getBoolean("lockscreen_album_art", false);
        albumArtFilter = Integer.parseInt(Xprefs.getString("lockscreen_album_art_filter", "0"));
        albumArtBlurAmount = (Xprefs.getSliderInt("lockscreen_media_blur", 30) / 100f) * 25f;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (Build.VERSION.SDK_INT <= 33) return;

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

                if (mScrimController == null) {
                    log("ScrimController is null!");
                    return;
                }
                View scrimBehind = (View) getObjectField(callMethod(mScrimController, "getScrimController"), "mScrimBehind");
                if (scrimBehind == null) {
                    log("ScrimBehind is null");
                    return;
                }
                ViewGroup rootView = (ViewGroup) scrimBehind.getParent();

                albumArtContainer = new FrameLayout(mContext);
                albumArtContainer.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                albumArtContainer.setVisibility(View.GONE);
                albumArtView = new ImageView(mContext);
                albumArtView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                albumArtView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                albumArtView.setVisibility(View.VISIBLE);
                albumArtContainer.addView(albumArtView);

                rootView.addView(albumArtContainer, 3);

            }
        });

        ControllersProvider.registerKeyguardShowingCallback(showing -> {
            shouldShowArt = showing;
            updateAlbumArt();
        });

        hookAllMethods(CentralSurfacesImplClass, "onKeyguardGoingAway", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                updateAlbumArt();
            }
        });

    }

    private void updateAlbumArt() {
        if (albumArtContainer == null) {
            log("AlbumArtContainer is null");
            return;
        }
        if (showAlbumArt && shouldShowArt && canShowArt) {
            // Keyguard so we can show album art
            albumArtContainer.post(() -> albumArtContainer.setVisibility(View.VISIBLE));
        } else {
            // Not Keyguard so we must hide album art
            albumArtContainer.post(() -> albumArtContainer.setVisibility(View.GONE));
        }
    }

    private void onPrimaryMetadataOrStateChanged(int state) {
        canShowArt = (getMediaMetadata() != null &&
                (state == PlaybackState.STATE_PLAYING ||
                        state == PlaybackState.STATE_BUFFERING) && getArt() != null);
        if (showAlbumArt && canShowArt) {
            Bitmap oldArt = mArt;
            mArt = getArtFilter(getArt());
            Drawable[] layers = new Drawable[]{
                    new BitmapDrawable(mContext.getResources(), oldArt),
                    new BitmapDrawable(mContext.getResources(), mArt)};
            TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
            albumArtView.setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(250);
        } else {
            albumArtView.setImageBitmap(null);
        }
        updateAlbumArt();
    }

    private Bitmap getArtFilter(Bitmap art) {
        Bitmap finalArt;
        switch (albumArtFilter) {
            case 1 -> finalArt = DrawableConverter.toGrayscale(art);
            case 2 ->
                    finalArt = DrawableConverter.getColoredBitmap(new BitmapDrawable(mContext.getResources(), art),
                            getPrimaryColor(mContext));
            case 3 ->
                    finalArt = DrawableConverter.getBlurredImage(mContext, art, albumArtBlurAmount);
            case 4 ->
                    finalArt = DrawableConverter.getGrayscaleBlurredImage(mContext, art, albumArtBlurAmount);
            default -> finalArt = art;
        }
        return finalArt;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
