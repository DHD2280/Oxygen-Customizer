package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.AlbumArtLockscreen.canShowArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.AlbumArtLockscreen.showAlbumArt;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;

/** @noinspection RedundantThrows*/
public class DepthWallpaper extends XposedMods {
    private static final String listenPackage = SYSTEM_UI;

    private static final String TAG = "Oxygen Customizer - DepthWallpaper: ";
    private static boolean lockScreenSubjectCacheValid = false;
    private Object mScrimController;
    private static boolean DWallpaperEnabled = false;
    private static int DWOpacity = 192;
    private final boolean DEBUG = BuildConfig.DEBUG;
    private FrameLayout mLockScreenSubject;
    private Drawable mSubjectDimmingOverlay;
    private FrameLayout mWallpaperBackground;
    private FrameLayout mWallpaperBitmapContainer;
    private FrameLayout mWallpaperDimmingOverlay;
    private boolean mLayersCreated = false;
    private boolean superPowerSave = false;

    public DepthWallpaper(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        DWallpaperEnabled = Xprefs.getBoolean("DWallpaperEnabled", false);
        DWOpacity = Xprefs.getSliderInt("DWOpacity", 192);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        if (Build.VERSION.SDK_INT < 34) return;

        Class<?> QSImplClass = findClass("com.android.systemui.qs.QSFragment", lpParam.classLoader);

        Class<?> SuperPowerSaveSettingsObserver;
        try {
            SuperPowerSaveSettingsObserver = findClass("com.oplus.systemui.qs.observer.SuperPowerSaveSettingsObserver", lpParam.classLoader);
        } catch (Throwable t) {
            SuperPowerSaveSettingsObserver = findClass("com.oplusos.systemui.common.observer.SuperPowerSaveSettingsObserver", lpParam.classLoader);
        }
        findAndHookMethod(SuperPowerSaveSettingsObserver,
                "onChange",
                boolean.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                superPowerSave = getBooleanField(param.thisObject, "mIsSuperPowerSaveState");
            }
        });

        Class<?> CentralSurfacesImplClass = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpParam.classLoader);
        Class<?> ScrimControllerClassEx = findClass("com.android.systemui.statusbar.phone.ScrimControllerEx", lpParam.classLoader);

        Class<?> ScrimViewClass = findClass("com.oplus.systemui.scrim.ScrimViewExImp", lpParam.classLoader);

        hookAllMethods(ScrimViewClass, "setViewAlpha", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if(!mLayersCreated) return;

                @SuppressLint("DiscouragedApi") String notificationScrim =
                        mContext.getResources().getString(mContext.getResources().getIdentifier("scrim_notification_name", "string", SYSTEM_UI));

                if (DEBUG) log(TAG + "ScrimViewExImp setViewAlpha " + notificationScrim + " " + getObjectField(param.thisObject, "name"));

                String scrimName = (String) getObjectField(param.thisObject, "name");

                if (DEBUG) log(TAG + "ScrimViewExImp setViewAlpha " + scrimName + " " + param.args[0]);

                if(mLayersCreated) {
                    if (scrimName.equals(notificationScrim)) {
                        if (DEBUG)log(TAG + "ScrimViewExImp Notification Scrim Alpha: " + param.args[0]);
                        float notificationAlpha = (float) param.args[0];
                        final float finalAlpha = calculateAlpha(notificationAlpha);// * getFloatField(getScrimController(), "mBehindAlpha");
                        if (DEBUG)log(TAG + "ScrimViewExImp finalAlpha: " + finalAlpha);
                        mLockScreenSubject.post(() -> mLockScreenSubject.setAlpha(finalAlpha));
                    }
                }
            }
        });

        hookAllMethods(CentralSurfacesImplClass, "start", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(!DWallpaperEnabled) return;

                if (DEBUG) log(TAG + "CentralSurfacesImpl started");

                View scrimBehind = (View) getObjectField(getScrimController(), "mScrimBehind");
                ViewGroup rootView = (ViewGroup) scrimBehind.getParent();

                @SuppressLint("DiscouragedApi")
                ViewGroup targetView = rootView.findViewById(mContext.getResources().getIdentifier("notification_container_parent", "id", mContext.getPackageName()));

                if(!mLayersCreated) {
                    createLayers();
                }

                rootView.addView(mWallpaperBackground, 0);

                targetView.addView(mLockScreenSubject,1);
                if (DEBUG) log(TAG + "CentralSurfacesImpl finished");
            }
        });

        Class<?> OplusLockScreenWallpaperController;
        try {
            OplusLockScreenWallpaperController = findClass("com.oplus.systemui.keyguard.wallpaper.OplusLockScreenWallpaperController", lpParam.classLoader);
        } catch (Throwable t) {
            OplusLockScreenWallpaperController = findClass("com.oplusos.systemui.keyguard.wallpaper.OplusLockScreenWallpaper", lpParam.classLoader); //OOS 13
        }

        hookAllConstructors(OplusLockScreenWallpaperController, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mLockscreenWallpaper = param.thisObject;
                Object mWallpaperChangeListener = getObjectField(param.thisObject, "mWallpaperChangeListener");
                if (DEBUG) log(TAG + "OplusLockScreenWallpaperController created");
                hookAllMethods(mWallpaperChangeListener.getClass(), "onWallpaperChange", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if(DWallpaperEnabled)
                        {
                            if (superPowerSave) return;
                            if (DEBUG) log(TAG + "Wallpaper Changed");

                            Bitmap wallpaperBitmap = Bitmap.createBitmap(DrawableConverter.drawableToBitmap((Drawable) callMethod(mLockscreenWallpaper, "loadBitmap")));

                            boolean cacheIsValid = assertCache(wallpaperBitmap);

                            Rect displayBounds =  ((Context) getObjectField(mLockscreenWallpaper, "mContext")).getSystemService(WindowManager.class)
                                    .getCurrentWindowMetrics()
                                    .getBounds();

                            float scale = 1.0f;

                            try {
                                View v = (View) getObjectField(mLockscreenWallpaper, "mBackDropBackView");
                                if (v != null) {
                                    scale = v.getScaleX();
                                }
                            } catch (Throwable t) {
                                if (DEBUG) log(TAG + "Error getting scale: " + t.getMessage());
                            }

                            float ratioW = scale * displayBounds.width() / wallpaperBitmap.getWidth();
                            float ratioH = scale * displayBounds.height() / wallpaperBitmap.getHeight();

                            int desiredHeight = Math.round(Math.max(ratioH, ratioW) * wallpaperBitmap.getHeight());
                            int desiredWidth = Math.round(Math.max(ratioH, ratioW) * wallpaperBitmap.getWidth());

                            int xPixelShift = (desiredWidth - displayBounds.width()) / 2;
                            int yPixelShift = (desiredHeight - displayBounds.height()) / 2;

                            Bitmap scaledWallpaperBitmap = Bitmap.createScaledBitmap(wallpaperBitmap, desiredWidth, desiredHeight, true);

                            //crop to display bounds
                            scaledWallpaperBitmap = Bitmap.createBitmap(scaledWallpaperBitmap, xPixelShift, yPixelShift, displayBounds.width(), displayBounds.height());
                            Bitmap finalScaledWallpaperBitmap = scaledWallpaperBitmap;

                            if(!mLayersCreated) {
                                if (DEBUG) log(TAG + "drawFrameOnCanvas Layers not created");
                                createLayers();
                            }

                            if (DEBUG) log(TAG + "finalScaledWallpaperBitmap != null " + (finalScaledWallpaperBitmap != null));

                            mWallpaperBackground.post(() -> mWallpaperBitmapContainer.setBackground(new BitmapDrawable(mContext.getResources(), finalScaledWallpaperBitmap)));

                            if (DEBUG) log(TAG + "cacheIsValid " + cacheIsValid);

                            if(!cacheIsValid)
                            {
                                XPLauncher.enqueueProxyCommand(proxy -> proxy.extractSubject(finalScaledWallpaperBitmap, Constants.getLockScreenSubjectCachePath()));
                            }
                        }
                    }
                });
            }
        });

        hookAllConstructors(ScrimControllerClassEx, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mScrimController = param.thisObject;
            }
        });

        Class<?> ScrimControllerClass = findClass("com.android.systemui.statusbar.phone.ScrimController", lpParam.classLoader);
        hookAllMethods(ScrimControllerClass, "applyAndDispatchState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (DEBUG) log(TAG + "ScrimController applyAndDispatchState");
                setDepthWallpaper();

            }
        });

        hookAllMethods(QSImplClass, "setQsExpansion", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if((boolean) callMethod(param.thisObject, "isKeyguardState"))
                {
                    setDepthWallpaper();
                }
            }
        });
    }

    private float calculateAlpha(float alpha) {
        if (alpha < .25f)
            alpha = 0;

        float subjectAlpha = 1f - alpha;
        if (subjectAlpha < .75f) {
            subjectAlpha /= .75f;
        }

        return subjectAlpha;

    }

    private boolean assertCache(Bitmap wallpaperBitmap) {

        boolean cacheIsValid = false;
        try
        {
            File wallpaperCacheFile = new File(Constants.getLockScreenBitmapCachePath());
            if (DEBUG) log(TAG + "Checking cache: " + wallpaperCacheFile.getAbsolutePath());
            ByteArrayOutputStream compressedBitmap = new ByteArrayOutputStream();
            wallpaperBitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressedBitmap);
            if(wallpaperCacheFile.exists())
            {
                if (DEBUG) log(TAG + "Cache file exists");
                FileInputStream cacheStream = new FileInputStream(wallpaperCacheFile);

                if(Arrays.equals(cacheStream.readAllBytes(), compressedBitmap.toByteArray()))
                {
                    cacheIsValid = true;
                    if (DEBUG) log(TAG + "Cache file is valid");
                }
                else
                {
                    if (DEBUG) log(TAG + "Cache file is invalid");
                    FileOutputStream newCacheStream = new FileOutputStream(wallpaperCacheFile);
                    compressedBitmap.writeTo(newCacheStream);
                    newCacheStream.close();
                }
                cacheStream.close();
            } else {
                if (DEBUG) log(TAG + "Cache file does not exist");
                FileOutputStream newCacheStream = new FileOutputStream(wallpaperCacheFile);
                compressedBitmap.writeTo(newCacheStream);
                newCacheStream.close();
            }
            compressedBitmap.close();
        }
        catch (Throwable t)
        {
            if (DEBUG) log(TAG + "Error asserting cache: " + t.getMessage());
        }

        if (!cacheIsValid) invalidateLSWSC();

        return cacheIsValid;
    }

    private void createLayers() {
        if (DEBUG) log(TAG + "Creating Layers");

        mWallpaperBackground = new FrameLayout(mContext);
        mWallpaperDimmingOverlay = new FrameLayout(mContext);
        mWallpaperBitmapContainer = new FrameLayout(mContext);
        FrameLayout.LayoutParams lpw = new FrameLayout.LayoutParams(-1, -1);

        mWallpaperDimmingOverlay.setBackgroundColor(Color.BLACK);
        mWallpaperDimmingOverlay.setLayoutParams(lpw);
        mWallpaperBitmapContainer.setLayoutParams(lpw);

        mWallpaperBackground.addView(mWallpaperBitmapContainer);
        mWallpaperBackground.addView(mWallpaperDimmingOverlay);
        mWallpaperBackground.setLayoutParams(lpw);

        mLockScreenSubject = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mLockScreenSubject.setLayoutParams(lp);

        mLayersCreated = true;
        if (DEBUG) log(TAG + "Layers Created");
    }

    private void setDepthWallpaper()
    {
        String state = getObjectField(getScrimController(), "mState").toString();
        boolean canShow = true;
        if (showAlbumArt && !canShowArt) {
            canShow = true;
        } else if (showAlbumArt && canShowArt) {
            canShow = false;
        }
        boolean showSubject = DWallpaperEnabled
                &&
                (
                        state.contains("KEYGUARD") // OOS 13
                )
                && canShow;
        if (DEBUG) log(TAG + "Setting Depth Wallpaper ScrimState: " + state + " showSubject: " + showSubject + " lockScreenSubjectCacheValid: " + lockScreenSubjectCacheValid);

        if(showSubject) {
            if (DEBUG) log(TAG + "Show Subject lockScreenSubjectCacheValid " + lockScreenSubjectCacheValid + " cacheFile exists " + new File(Constants.getLockScreenSubjectCachePath()).exists());
            if(!lockScreenSubjectCacheValid && new File(Constants.getLockScreenSubjectCachePath()).exists())
            {
                if (DEBUG) log(TAG + "lockScreenSubjectCacheValid false");
                try (FileInputStream inputStream = new FileInputStream(Constants.getLockScreenSubjectCachePath()))
                {
                    if (DEBUG) log(TAG + "Loading Lock Screen Subject Cache file exists");
                    Drawable bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "");
                    bitmapDrawable.setAlpha(255);

                    mSubjectDimmingOverlay = bitmapDrawable.getConstantState().newDrawable().mutate();
                    mSubjectDimmingOverlay.setTint(Color.BLACK);

                    mLockScreenSubject.setBackground(new LayerDrawable(new Drawable[]{bitmapDrawable, mSubjectDimmingOverlay}));
                    lockScreenSubjectCacheValid = true;
                    if (DEBUG) log(TAG + "Lock Screen Subject Cache Loaded " + lockScreenSubjectCacheValid);
                }
                catch (Throwable t) {
                    if (DEBUG) log(TAG + "Error loading lockscreen subject cache: " + t.getMessage());
                }
            }

            if(lockScreenSubjectCacheValid) {
                if (DEBUG) log(TAG + "lockScreenSubjectCacheValid true");
                mLockScreenSubject.getBackground().setAlpha(DWOpacity);

                if(!state.equals("KEYGUARD")) {
                    mSubjectDimmingOverlay.setAlpha(192 /*Math.round(192 * (DWOpacity / 255f))*/);
                }
                else {
                    if (DEBUG) log(TAG + "Setting Alpha");
                    //this is the dimmed wallpaper coverage
                    mSubjectDimmingOverlay.setAlpha(Math.round(getFloatField(getScrimController(), "mScrimBehindAlphaKeyguard") * 240)); //A tad bit lower than max. show it a bit lighter than other stuff
                    mWallpaperDimmingOverlay.setAlpha(getFloatField(getScrimController(), "mScrimBehindAlphaKeyguard"));

                }
                if (DEBUG) log(TAG + "Setting Visibility");
                mWallpaperBackground.setVisibility(VISIBLE);
                mLockScreenSubject.setVisibility(VISIBLE);
            }
        }
        else if(mLayersCreated)
        {
            mLockScreenSubject.setVisibility(GONE);

            if (state.contains("UNLOCKED")) {
                mWallpaperBackground.setVisibility(GONE);
            }
        }
    }


    private void invalidateLSWSC() //invalidate lock screen wallpaper subject cache
    {
        if (DEBUG) log(TAG + "Invalidating Lock Screen Wallpaper Subject Cache");
        lockScreenSubjectCacheValid = false;
        if(mLayersCreated) {
            mLockScreenSubject.post(() -> {
                mLockScreenSubject.setVisibility(GONE);
                mLockScreenSubject.setBackground(null);
                mWallpaperBackground.setVisibility(GONE);
                mWallpaperBitmapContainer.setBackground(null);
            });
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(Constants.getLockScreenSubjectCachePath()).delete();
        }
        catch (Throwable ignored){}
    }

    private Object getScrimController() {
        return callMethod(mScrimController, "getScrimController");
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }
}
