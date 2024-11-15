package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_AOD_INVALIDATE_DEPTH;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;

import java.io.File;
import java.io.FileInputStream;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

/**
 * @noinspection RedundantThrows
 */
public class AodDepthSubject extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;

    private boolean mDepthWallpaper = false;
    private boolean mDepthOnAod = false;
    private int mDepthSubjectAlpha = 0;
    private FrameLayout mAodRootLayout = null;
    private FrameLayout mLockScreenSubject;
    private boolean mLayersCreated = false;
    private boolean mSubjectCacheValid = false;
    private boolean mReceiverRegistered = false;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int direction = 0; // 0: right, 1: up, 2: left, 3: down
    private static final int MOVE_DISTANCE = 2;
    private static final long DELAY_MILLIS = 60000; // 1 minute

    private final BroadcastReceiver mInvalidateCacheReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            mSubjectCacheValid = false;
            loadDepth();
        }
    };

    private final Runnable mBurnInProtection = new Runnable() {
        @Override
        public void run() {
            if (!mDepthWallpaper || !mLayersCreated) return;
            moveFrameLayout();
            mHandler.postDelayed(this, DELAY_MILLIS);
        }
    };

    public void startMoving() {
        mHandler.postDelayed(mBurnInProtection, DELAY_MILLIS);
    }

    public void stopMoving() {
        mHandler.removeCallbacks(mBurnInProtection);
    }

    private void moveFrameLayout() {
        int x = (int) mLockScreenSubject.getX();
        int y = (int) mLockScreenSubject.getY();

        switch (direction) {
            case 0:
                mLockScreenSubject.setX(x + MOVE_DISTANCE);
                break;
            case 1:
                mLockScreenSubject.setY(y - MOVE_DISTANCE);
                break;
            case 2:
                mLockScreenSubject.setX(x - MOVE_DISTANCE);
                break;
            case 3:
                mLockScreenSubject.setY(y + MOVE_DISTANCE);
                break;
        }

        direction = (direction + 1) % 4;
    }

    public AodDepthSubject(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mDepthWallpaper = Xprefs.getBoolean("DWallpaperEnabled", false);
        mDepthOnAod = Xprefs.getBoolean("DWShowOnAod", false);
        mDepthSubjectAlpha = Xprefs.getSliderInt("DWAodOpacity", 192);

        if (mLayersCreated) {
            mLockScreenSubject.setVisibility(mDepthWallpaper && mDepthOnAod ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!mReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTIONS_AOD_INVALIDATE_DEPTH);
            mContext.registerReceiver(mInvalidateCacheReceiver, filter, Context.RECEIVER_EXPORTED);
            mReceiverRegistered = true;
        }

        Class<?> AodRecord = findClassInArray(lpparam,
                "com.oplus.systemui.aod.AodRecord",
                "com.oplusos.systemui.aod.AodRecord");
        hookAllMethods(AodRecord, "createAndInitRootView", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mAodRootLayout = (FrameLayout) param.getResult();
                placeSubject();
            }
        });

        hookAllMethods(AodRecord, "onDreamingStarted", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((!mLayersCreated) ||
                        (mDepthWallpaper && !mDepthOnAod) ||
                        (!mDepthWallpaper && mDepthOnAod)) {
                    return;
                }
                log("onDreamingStarted");
                animateView(true);
                startMoving();
            }
        });

        hookAllMethods(AodRecord, "onDreamingStopped", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if ((!mLayersCreated) ||
                        (mDepthWallpaper && !mDepthOnAod) ||
                        (!mDepthWallpaper && mDepthOnAod)) {
                    return;
                }
                log("onDreamingStopped");
                animateView(false);
                stopMoving();
            }
        });

    }

    public void animateView(boolean show) {
        ObjectAnimator scaleX;
        ObjectAnimator scaleY;
        if (show) {
             scaleX = ObjectAnimator.ofFloat(mLockScreenSubject, "scaleX", 0.8f, 1f);
             scaleY = ObjectAnimator.ofFloat(mLockScreenSubject, "scaleY", 0.8f, 1f);
        } else {
            scaleX = ObjectAnimator.ofFloat(mLockScreenSubject, "scaleX", 1f, 0.8f);
            scaleY = ObjectAnimator.ofFloat(mLockScreenSubject, "scaleY", 1f, 0.8f);
        }
        scaleX.setDuration(1000);
        scaleY.setDuration(1000);

        AlphaAnimation fade;
        if (show) {
            fade = new AlphaAnimation(0f, 1f);
        } else {
            fade = new AlphaAnimation(1f, 0f);
        }
        fade.setInterpolator(new DecelerateInterpolator());
        fade.setDuration(1500);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleX).with(scaleY);
        animatorSet.start();

        mLockScreenSubject.startAnimation(fade);
    }

    private void placeSubject() {
        if (mAodRootLayout == null) {
            return;
        }
        createLayers();
        if (!mSubjectCacheValid) loadDepth();
        try {
            ViewGroup v = (ViewGroup) mLockScreenSubject.getParent();
            v.removeView(mLockScreenSubject);
        } catch (Throwable t) {
            log(t);
        }
        mAodRootLayout.addView(mLockScreenSubject, 0);
    }

    private void createLayers() {
        if (mLayersCreated) return;
        mLockScreenSubject = new FrameLayout(mContext);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(-1, -1);
        mLockScreenSubject.setLayoutParams(lp);
        mLockScreenSubject.setVisibility(View.GONE);
        mLayersCreated = true;
    }

    private void loadDepth() {
        if (!(new File(Constants.getLockScreenSubjectCachePath()).exists())) {
            return;
        }
        try (FileInputStream inputStream = new FileInputStream(Constants.getLockScreenSubjectCachePath())) {
            log("Loading Depth Cache");
            Drawable bitmapDrawable = BitmapDrawable.createFromStream(inputStream, "");
            bitmapDrawable.setAlpha(255);

            mLockScreenSubject.setBackground(bitmapDrawable);
            mLockScreenSubject.getBackground().setAlpha(mDepthSubjectAlpha);
            mSubjectCacheValid = true;
        } catch (Throwable ignored) {}
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
