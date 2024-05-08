package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.Objects;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;

/** @noinspection RedundantThrows*/
public class DepthWallpaper extends XposedMods {
    private static final String listenPackage = SYSTEM_UI;

    private static final String TAG = "Oxygen Customizer - DepthWallpaper: ";
    private static boolean lockScreenSubjectCacheValid = false;
    private Object mScrimController;
    private static boolean DWallpaperEnabled = false;
    private static int DWOpacity = 192;
    private FrameLayout mLockScreenSubject;
    private Drawable mSubjectDimmingOverlay;
    private FrameLayout mWallpaperBackground;
    private FrameLayout mWallpaperBitmapContainer;
    private FrameLayout mWallpaperDimmingOverlay;
    private boolean mLayersCreated = false;

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
        Class<?> QSImplClass = findClass("com.android.systemui.qs.QSFragment", lpParam.classLoader);
        Class<?> CanvasEngineClass = findClass("com.android.systemui.wallpapers.ImageWallpaper$CanvasEngine", lpParam.classLoader);
        Class<?> CentralSurfacesImplClass = findClass("com.android.systemui.statusbar.phone.CentralSurfacesImpl", lpParam.classLoader);
        Class<?> ScrimControllerClass = findClass("com.android.systemui.statusbar.phone.ScrimControllerEx", lpParam.classLoader);
        Class<?> ScrimViewClass = findClass("com.oplus.systemui.scrim.ScrimViewExImp", lpParam.classLoader);

        hookAllMethods(ScrimViewClass, "setViewAlpha", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if(!mLayersCreated) return;

                String notificationScrim =
                        mContext.getResources().getString(mContext.getResources().getIdentifier("scrim_notification_name", "string", SYSTEM_UI));

                log(TAG + "ScrimViewExImp setViewAlpha " + notificationScrim + " " + getObjectField(param.thisObject, "name"));

                if(mLayersCreated && getObjectField(param.thisObject, "name").equals("scrim_in_front"))
                {
                    log(TAG + "ScrimViewExImp Notification Scrim Alpha: " + param.args[0]);
                    float notificationAlpha = (float)param.args[0];

                    if(notificationAlpha < .25f)
                        notificationAlpha = 0;

                    float subjectAlpha = 1f - notificationAlpha;
                    if(subjectAlpha < .75f)
                    {
                        subjectAlpha /= .75f;
                    }
                    final float finalSubjectAlpha = subjectAlpha;
                    Object scrimController = callMethod(mScrimController, "getScrimController");
                    final float finalAlpha = subjectAlpha * getFloatField(mScrimController, "scrimBehindAlphaForCustomBlur");
                    log(TAG + "ScrimViewExImp Notification Scrim Alpha: " + notificationAlpha + " Subject Alpha: " + subjectAlpha + " Final Alpha: " + finalAlpha + " scrimBehindAlphaForCustomBlur: " + getFloatField(mScrimController, "scrimBehindAlphaForCustomBlur"));

                    mLockScreenSubject.post(() -> mLockScreenSubject.setAlpha(finalAlpha));
                }
            }
        });

        hookAllMethods(CentralSurfacesImplClass, "start", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(!DWallpaperEnabled) return;

                log(TAG + "CentralSurfacesImpl started");

                Object scrimController = callMethod(mScrimController, "getScrimController");
                View scrimBehind = (View) getObjectField(scrimController, "mScrimBehind");
                ViewGroup rootView = (ViewGroup) scrimBehind.getParent();

                @SuppressLint("DiscouragedApi")
                ViewGroup targetView = rootView.findViewById(mContext.getResources().getIdentifier("notification_container_parent", "id", mContext.getPackageName()));

                if(!mLayersCreated) {
                    createLayers();
                }

                rootView.addView(mWallpaperBackground, 0);

                targetView.addView(mLockScreenSubject,1);
                log(TAG + "CentralSurfacesImpl finished");
            }
        });

        hookAllMethods(CanvasEngineClass, "onSurfaceDestroyed", new XC_MethodHook() { //lockscreen wallpaper changed
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log(TAG + "CanvasEngineClass onSurfaceDestroyed");
                log(TAG + "isLockScreenWallpaper(param.thisObject): " + isLockScreenWallpaper(param.thisObject));
                if(DWallpaperEnabled && isLockScreenWallpaper(param.thisObject))
                {
                    log(TAG + "call invalidateLSWSC");
                    invalidateLSWSC();
                }
            }
        });

        hookAllMethods(CanvasEngineClass, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(
                        callMethod(
                                getObjectField(param.thisObject, "mWallpaperManager"),
                                "getWallpaperInfo", WallpaperManager.FLAG_LOCK)
                                != null) //it's live wallpaper. we can't use that
                {
                    log(TAG + "Live Wallpaper Detected. Disabling Depth Wallpaper");
                    invalidateLSWSC();
                }
            }
        });

        hookAllMethods(CanvasEngineClass, "drawFrameOnCanvas", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if(DWallpaperEnabled && isLockScreenWallpaper(param.thisObject))
                {
                    log(TAG + "isLockScreenWallpaper " + isLockScreenWallpaper(param.thisObject));
                    log(TAG + "CanvasEngineClass drawFrameOnCanvas");
                    Bitmap wallpaperBitmap = Bitmap.createBitmap((Bitmap) param.args[0]);

                    boolean cacheIsValid = assertCache(wallpaperBitmap);

                    Rect displayBounds =  ((Context) callMethod(param.thisObject, "getDisplayContext")).getSystemService(WindowManager.class)
                            .getCurrentWindowMetrics()
                            .getBounds();

                    float ratioW = 1f * displayBounds.width() / wallpaperBitmap.getWidth();
                    float ratioH = 1f * displayBounds.height() / wallpaperBitmap.getHeight();

                    int desiredHeight = Math.round(Math.max(ratioH, ratioW) * wallpaperBitmap.getHeight());
                    int desiredWidth = Math.round(Math.max(ratioH, ratioW) * wallpaperBitmap.getWidth());

                    int xPixelShift = (desiredWidth - displayBounds.width()) / 2;
                    int yPixelShift = (desiredHeight - displayBounds.height()) / 2;

                    Bitmap scaledWallpaperBitmap = Bitmap.createScaledBitmap(wallpaperBitmap, desiredWidth, desiredHeight, true);

                    //crop to display bounds
                    scaledWallpaperBitmap = Bitmap.createBitmap(scaledWallpaperBitmap, xPixelShift, yPixelShift, displayBounds.width(), displayBounds.height());
                    Bitmap finalScaledWallpaperBitmap = scaledWallpaperBitmap;

                    if(!mLayersCreated) {
                        log(TAG + "drawFrameOnCanvas Layers not created");
                        createLayers();
                    }

                    log(TAG + "finalScaledWallpaperBitmap != null " + (finalScaledWallpaperBitmap != null));

                    mWallpaperBackground.post(() -> mWallpaperBitmapContainer.setBackground(new BitmapDrawable(mContext.getResources(), finalScaledWallpaperBitmap)));

                    if(!cacheIsValid)
                    {
                        XPLauncher.enqueueProxyCommand(proxy -> proxy.extractSubject(finalScaledWallpaperBitmap, Constants.getLockScreenSubjectCachePath(mContext)));
                    }
                }
            }
        });

        hookAllConstructors(ScrimControllerClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mScrimController = param.thisObject;
            }
        });

        Class<?> ScrimControllerClassEx = findClass("com.android.systemui.statusbar.phone.ScrimController", lpParam.classLoader);
        hookAllMethods(ScrimControllerClassEx, "applyAndDispatchState", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
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

    private boolean assertCache(Bitmap wallpaperBitmap) {

        boolean cacheIsValid = false;
        try
        {
            File wallpaperCacheFile = new File(Constants.getLockScreenBitmapCachePath(mContext));
            log(TAG + "Checking cache: " + wallpaperCacheFile.getAbsolutePath());
            ByteArrayOutputStream compressedBitmap = new ByteArrayOutputStream();
            wallpaperBitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressedBitmap);
            if(wallpaperCacheFile.exists())
            {
                log(TAG + "Cache file exists");
                FileInputStream cacheStream = new FileInputStream(wallpaperCacheFile);

                if(Arrays.equals(cacheStream.readAllBytes(), compressedBitmap.toByteArray()))
                {
                    cacheIsValid = true;
                    log(TAG + "Cache file is valid");
                }
                else
                {
                    log(TAG + "Cache file is invalid");
                    FileOutputStream newCacheStream = new FileOutputStream(wallpaperCacheFile);
                    compressedBitmap.writeTo(newCacheStream);
                    newCacheStream.close();
                }
                cacheStream.close();
            }
            log(TAG + "Cache file does not exist");
            compressedBitmap.close();
        }
        catch (Throwable t)
        {
            log(TAG + "Error asserting cache: " + t.getMessage());
        }

        if (!cacheIsValid) invalidateLSWSC();

        return cacheIsValid;
    }

    private void createLayers() {
        log(TAG + "Creating Layers");

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
        log(TAG + "Layers Created");
    }

    private boolean isLockScreenWallpaper(Object canvasEngine)
    {
        log(TAG + "Checking if lockscreen wallpaper " + "getWallpaperFlag(canvasEngine): " + getWallpaperFlag(canvasEngine));
        return (getWallpaperFlag(canvasEngine)
                & WallpaperManager.FLAG_LOCK)
                == WallpaperManager.FLAG_LOCK;
    }
    private void setDepthWallpaper()
    {
        log(TAG + "Setting Depth Wallpaper");
        Object scrimController = callMethod(mScrimController, "getScrimController");
        String state = getObjectField(scrimController, "mState").toString();
        boolean showSubject = DWallpaperEnabled
                &&
                (
                        state.equals("KEYGUARD")
                );

        log(TAG + "State: " + state + " Show Subject: " + showSubject);

        if(showSubject) {
            log(TAG + "Show Subject lockScreenSubjectCacheValid " + lockScreenSubjectCacheValid + " cacheFile exists " + new File(Constants.getLockScreenSubjectCachePath(mContext)).exists());
            if(!lockScreenSubjectCacheValid && new File(Constants.getLockScreenSubjectCachePath(mContext)).exists())
            {
                log(TAG + "lockScreenSubjectCacheValid false");
                try (FileInputStream inputStream = new FileInputStream(Constants.getLockScreenSubjectCachePath(mContext)))
                {
                    log(TAG + "Loading Lock Screen Subject Cache file exists");
                    Drawable bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "");
                    bitmapDrawable.setAlpha(255);

                    mSubjectDimmingOverlay = bitmapDrawable.getConstantState().newDrawable().mutate();
                    mSubjectDimmingOverlay.setTint(Color.BLACK);

                    mLockScreenSubject.setBackground(new LayerDrawable(new Drawable[]{bitmapDrawable, mSubjectDimmingOverlay}));
                    lockScreenSubjectCacheValid = true;
                    log(TAG + "Lock Screen Subject Cache Loaded " + lockScreenSubjectCacheValid);
                }
                catch (Throwable t) {
                    log(TAG + "Error loading lockscreen subject cache: " + t.getMessage());
                }
            }

            if(lockScreenSubjectCacheValid) {
                log(TAG + "lockScreenSubjectCacheValid true");
                mLockScreenSubject.getBackground().setAlpha(DWOpacity);

                if(!state.equals("KEYGUARD")) {
                    mSubjectDimmingOverlay.setAlpha(192 /*Math.round(192 * (DWOpacity / 255f))*/);
                }
                else {
                    log(TAG + "Setting Alpha");
                    //this is the dimmed wallpaper coverage
                    mSubjectDimmingOverlay.setAlpha(Math.round(getFloatField(scrimController, "mScrimBehindAlphaKeyguard") * 240)); //A tad bit lower than max. show it a bit lighter than other stuff
                    mWallpaperDimmingOverlay.setAlpha(getFloatField(scrimController, "mScrimBehindAlphaKeyguard"));
                }
                log(TAG + "Setting Visibility");
                mWallpaperBackground.setVisibility(VISIBLE);
                mLockScreenSubject.setVisibility(VISIBLE);
            }
        }
        else if(mLayersCreated)
        {
            mLockScreenSubject.setVisibility(GONE);

            if (state.equals("UNLOCKED")) {
                mWallpaperBackground.setVisibility(GONE);
            }
        }
    }

    private int getWallpaperFlag(Object canvasEngine) {
        log(TAG + "Getting Wallpaper Flag");
        return (int) callMethod(canvasEngine, "getWallpaperFlags");
    }

    private void invalidateLSWSC() //invalidate lock screen wallpaper subject cache
    {
        log(TAG + "Invalidating Lock Screen Wallpaper Subject Cache");
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
            new File(Constants.getLockScreenSubjectCachePath(mContext)).delete();
        }
        catch (Throwable ignored){}
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }
}
