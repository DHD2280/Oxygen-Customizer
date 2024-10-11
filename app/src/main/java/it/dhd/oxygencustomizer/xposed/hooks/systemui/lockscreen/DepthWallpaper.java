package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static android.content.Context.RECEIVER_EXPORTED;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_DEPTH_BACKGROUND_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTION_DEPTH_SUBJECT_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.getLockScreenBitmapCachePath;
import static it.dhd.oxygencustomizer.utils.Constants.getLockScreenSubjectCachePath;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.AlbumArtLockscreen.canShowArt;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.AlbumArtLockscreen.showAlbumArt;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;

/**
 * @noinspection RedundantThrows
 */
public class DepthWallpaper extends XposedMods {
    private static final String listenPackage = SYSTEM_UI;

    private static boolean lockScreenSubjectCacheValid = false;
    private static boolean DWallpaperEnabled = false;
    private static int DWOpacity = 192;
    private static int DWMode = 0;
    private Object mScrimController;
    private FrameLayout mLockScreenSubject;
    private Drawable mSubjectDimmingOverlay;
    private FrameLayout mWallpaperBackground;
    private FrameLayout mWallpaperBitmapContainer;
    private FrameLayout mWallpaperDimmingOverlay;
    private boolean mLayersCreated = false;
    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                switch (intent.getAction()) {
                    case ACTION_DEPTH_BACKGROUND_CHANGED -> setDepthBackground();
                    case ACTION_DEPTH_SUBJECT_CHANGED -> lockScreenSubjectCacheValid = false;
                }
            }
        }
    };
    private boolean superPowerSave = false;
    private boolean mBroadcastRegistered = false;

    public DepthWallpaper(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        DWallpaperEnabled = Xprefs.getBoolean("DWallpaperEnabled", false);
        DWOpacity = Xprefs.getSliderInt("DWOpacity", 192);
        DWMode = Integer.parseInt(Xprefs.getString("DWMode", "0"));

        if (Key.length > 0) {
            if (Key[0].equals("DWMode")) {
                cleanFiles();
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        if (Build.VERSION.SDK_INT < 34) return;

        if (!mBroadcastRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_DEPTH_BACKGROUND_CHANGED);
            filter.addAction(ACTION_DEPTH_SUBJECT_CHANGED);
            mContext.registerReceiver(mReceiver, filter, RECEIVER_EXPORTED);
            mBroadcastRegistered = true;
        }

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
                if (!mLayersCreated) return;

                @SuppressLint("DiscouragedApi") String notificationScrim =
                        mContext.getResources().getString(mContext.getResources().getIdentifier("scrim_notification_name", "string", SYSTEM_UI));

                log("ScrimViewExImp setViewAlpha " + notificationScrim + " " + getObjectField(param.thisObject, "name"));

                String scrimName = (String) getObjectField(param.thisObject, "name");

                log("ScrimViewExImp setViewAlpha " + scrimName + " " + param.args[0]);

                if (mLayersCreated) {
                    if (scrimName.equals(notificationScrim)) {
                        log("ScrimViewExImp Notification Scrim Alpha: " + param.args[0]);
                        float notificationAlpha = (float) param.args[0];
                        final float finalAlpha = calculateAlpha(notificationAlpha);// * getFloatField(getScrimController(), "mBehindAlpha");
                        log("ScrimViewExImp finalAlpha: " + finalAlpha);
                        mLockScreenSubject.post(() -> mLockScreenSubject.setAlpha(finalAlpha));
                    }
                }
            }
        });

        hookAllMethods(CentralSurfacesImplClass, "start", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!DWallpaperEnabled) return;

                log("CentralSurfacesImpl started");

                View scrimBehind = (View) getObjectField(getScrimController(), "mScrimBehind");
                ViewGroup rootView = (ViewGroup) scrimBehind.getParent();

                @SuppressLint("DiscouragedApi")
                ViewGroup targetView = rootView.findViewById(mContext.getResources().getIdentifier("notification_container_parent", "id", mContext.getPackageName()));

                if (!mLayersCreated) {
                    createLayers();
                }

                rootView.addView(mWallpaperBackground, 2);

                targetView.addView(mLockScreenSubject, 1);

                if (DWMode == 1) {
                    setDepthBackground();
                }

                log("CentralSurfacesImpl finished");
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
                log("OplusLockScreenWallpaperController created");
                hookAllMethods(mWallpaperChangeListener.getClass(), "onWallpaperChange", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (DWallpaperEnabled && DWMode == 0) {
                            if (superPowerSave) return;
                            log("Wallpaper Changed");

                            Bitmap wallpaperBitmap = Bitmap.createBitmap(DrawableConverter.drawableToBitmap((Drawable) callMethod(mLockscreenWallpaper, "loadBitmap")));

                            boolean cacheIsValid = assertCache(wallpaperBitmap);

                            Rect displayBounds = ((Context) getObjectField(mLockscreenWallpaper, "mContext")).getSystemService(WindowManager.class)
                                    .getCurrentWindowMetrics()
                                    .getBounds();

                            float scale = 1.0f;

                            try {
                                View v = (View) getObjectField(mLockscreenWallpaper, "mBackDropBackView");
                                if (v != null) {
                                    scale = v.getScaleX();
                                }
                            } catch (Throwable t) {
                                log("Error getting scale: " + t.getMessage());
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

                            if (!mLayersCreated) {
                                log("drawFrameOnCanvas Layers not created");
                                createLayers();
                            }

                            log("finalScaledWallpaperBitmap != null " + (finalScaledWallpaperBitmap != null));

                            mWallpaperBackground.post(() -> mWallpaperBitmapContainer.setBackground(new BitmapDrawable(mContext.getResources(), finalScaledWallpaperBitmap)));

                            log("cacheIsValid " + cacheIsValid);

                            if (!cacheIsValid) {
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
                log("ScrimController applyAndDispatchState");
                setDepthWallpaper();

            }
        });

        hookAllMethods(QSImplClass, "setQsExpansion", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if ((boolean) callMethod(param.thisObject, "isKeyguardState")) {
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
        try {
            File wallpaperCacheFile = new File(Constants.getLockScreenBitmapCachePath());
            log("Checking cache: " + wallpaperCacheFile.getAbsolutePath());
            ByteArrayOutputStream compressedBitmap = new ByteArrayOutputStream();
            wallpaperBitmap.compress(Bitmap.CompressFormat.JPEG, 100, compressedBitmap);
            if (wallpaperCacheFile.exists()) {
                log("Cache file exists");
                FileInputStream cacheStream = new FileInputStream(wallpaperCacheFile);

                if (Arrays.equals(cacheStream.readAllBytes(), compressedBitmap.toByteArray())) {
                    cacheIsValid = true;
                    log("Cache file is valid");
                } else {
                    log("Cache file is invalid");
                    FileOutputStream newCacheStream = new FileOutputStream(wallpaperCacheFile);
                    compressedBitmap.writeTo(newCacheStream);
                    newCacheStream.close();
                }
                cacheStream.close();
            } else {
                log("Cache file does not exist");
                FileOutputStream newCacheStream = new FileOutputStream(wallpaperCacheFile);
                compressedBitmap.writeTo(newCacheStream);
                newCacheStream.close();
            }
            compressedBitmap.close();
        } catch (Throwable t) {
            log("Error asserting cache: " + t.getMessage());
        }

        if (!cacheIsValid) invalidateLSWSC();

        return cacheIsValid;
    }

    private void createLayers() {
        log("Creating Layers");

        mWallpaperBackground = new FrameLayout(mContext);
        mWallpaperDimmingOverlay = new FrameLayout(mContext);
        mWallpaperBitmapContainer = new FrameLayout(mContext);
        FrameLayout.LayoutParams lpw = new FrameLayout.LayoutParams(-1, -1);

        mWallpaperDimmingOverlay.setBackgroundColor(Color.TRANSPARENT);
        mWallpaperDimmingOverlay.setLayoutParams(lpw);
        mWallpaperBitmapContainer.setLayoutParams(lpw);

        mWallpaperBackground.addView(mWallpaperBitmapContainer);
        mWallpaperBackground.addView(mWallpaperDimmingOverlay);
        mWallpaperBackground.setLayoutParams(lpw);

        mLockScreenSubject = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mLockScreenSubject.setLayoutParams(lp);

        mLayersCreated = true;
        log("Layers Created");
    }

    private void setDepthWallpaper() {
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
        log("Setting Depth Wallpaper ScrimState: " + state + " showSubject: " + showSubject + " lockScreenSubjectCacheValid: " + lockScreenSubjectCacheValid);

        if (showSubject) {
           log("Show Subject lockScreenSubjectCacheValid " + lockScreenSubjectCacheValid + " cacheFile exists " + new File(Constants.getLockScreenSubjectCachePath()).exists());
            if (!lockScreenSubjectCacheValid && new File(Constants.getLockScreenSubjectCachePath()).exists()) {
                log("lockScreenSubjectCacheValid false");
                try (FileInputStream inputStream = new FileInputStream(Constants.getLockScreenSubjectCachePath())) {
                    log("Loading Lock Screen Subject Cache file exists");
                    Drawable bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "");
                    bitmapDrawable.setAlpha(255);

                    mSubjectDimmingOverlay = bitmapDrawable.getConstantState().newDrawable().mutate();
                    mSubjectDimmingOverlay.setTint(Color.BLACK);

                    mLockScreenSubject.setBackground(new LayerDrawable(new Drawable[]{bitmapDrawable, mSubjectDimmingOverlay}));
                    lockScreenSubjectCacheValid = true;
                    log("Lock Screen Subject Cache Loaded " + lockScreenSubjectCacheValid);
                } catch (Throwable t) {
                    log("Error loading lockscreen subject cache: " + t.getMessage());
                }
            }

            if (lockScreenSubjectCacheValid) {
                log("lockScreenSubjectCacheValid true");
                mLockScreenSubject.getBackground().setAlpha(DWOpacity);

                if (!state.equals("KEYGUARD")) {
                    mSubjectDimmingOverlay.setAlpha(192 /*Math.round(192 * (DWOpacity / 255f))*/);
                } else {
                    log("Setting Alpha");
                    //this is the dimmed wallpaper coverage
                    mSubjectDimmingOverlay.setAlpha(Math.round(getFloatField(getScrimController(), "mScrimBehindAlphaKeyguard") * 240)); //A tad bit lower than max. show it a bit lighter than other stuff
                    mWallpaperDimmingOverlay.setAlpha(getFloatField(getScrimController(), "mScrimBehindAlphaKeyguard"));

                }
                log("Setting Visibility");
                mWallpaperBackground.setVisibility(VISIBLE);
                mLockScreenSubject.setVisibility(VISIBLE);
            }
        } else if (mLayersCreated) {
            mLockScreenSubject.setVisibility(GONE);

            if (state.contains("UNLOCKED")) {
                mWallpaperBackground.setVisibility(GONE);
            }
        }
    }


    private void invalidateLSWSC() //invalidate lock screen wallpaper subject cache
    {
        log("Invalidating Lock Screen Wallpaper Subject Cache");
        lockScreenSubjectCacheValid = false;
        if (mLayersCreated) {
            mLockScreenSubject.post(() -> {
                mLockScreenSubject.setVisibility(GONE);
                mLockScreenSubject.setBackground(null);
                mWallpaperBackground.setVisibility(GONE);
                mWallpaperBitmapContainer.setBackground(null);
            });
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(getLockScreenSubjectCachePath()).delete();
        } catch (Throwable ignored) {
        }
    }

    private void cleanFiles() {
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(getLockScreenBitmapCachePath()).delete();
        } catch (Throwable ignored) {
        }
        try {
            //noinspection ResultOfMethodCallIgnored
            new File(getLockScreenSubjectCachePath()).delete();
        } catch (Throwable ignored) {
        }
        lockScreenSubjectCacheValid = false;
        if (mWallpaperBackground != null)
            mWallpaperBackground.post(() -> mWallpaperBitmapContainer.setBackground(null));
    }

    private Object getScrimController() {
        if (mScrimController == null) {
            log("ScrimController is null!");
        }
        return callMethod(mScrimController, "getScrimController");
    }

    private void setDepthBackground() {
        if (!DWallpaperEnabled || !mLayersCreated) return;
        if (mWallpaperBackground != null) {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(new File(getLockScreenBitmapCachePath()));

                        Drawable drawable = ImageDecoder.decodeDrawable(source);
                        mWallpaperBackground.post(() -> mWallpaperBitmapContainer.setBackground(drawable));

                        if (drawable instanceof AnimatedImageDrawable) {
                            ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                            ((AnimatedImageDrawable) drawable).start();
                        }
                    } catch (Throwable ignored) {
                    }

                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName) && !XPLauncher.isChildProcess;
    }
}
