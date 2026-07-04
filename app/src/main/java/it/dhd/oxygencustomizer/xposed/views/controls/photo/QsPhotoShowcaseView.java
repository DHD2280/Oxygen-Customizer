package it.dhd.oxygencustomizer.xposed.views.controls.photo;

import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_QS_PHOTO_CHANGED;
import static it.dhd.oxygencustomizer.xposed.XPLauncher.moduleResources;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Environment;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

@SuppressLint({"ViewConstructor", "AppCompatCustomView"})
public class QsPhotoShowcaseView extends ImageView {

    private final boolean mSettingsInterface;
    private final Context mContext;
    private final ActivityLauncherUtils mActivityLauncherUtils;
    private Drawable mLoadedDrawable = null;
    private boolean mIsShowcase = false;
    private final List<Drawable> mDrawables = new ArrayList<>();
    private int mCurrentDrawableIndex = 0;
    private long mLastRunTime = 0L;
    private final long mUpdateInterval = 1000L * 30; // 30 seconds
    private boolean mScreenOn = true;
    private boolean mPowerSaveModeEnabled = false;
    private PowerManager mPowerManager;

    private final Handler mImageHandler = new Handler();
    private final Runnable mImageUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mLastRunTime = System.currentTimeMillis();
            switchDrawable();
            mImageHandler.postDelayed(this, mUpdateInterval);
        }
    };

    private float radius;
    private final Path path;
    private RectF rect;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateImage();
        }
    };

    private final BroadcastReceiver mScreenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOn = false;
                removeCallback();
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOn = true;
            } else if (PowerManager.ACTION_POWER_SAVE_MODE_CHANGED.equals(intent.getAction())) {
                mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
                removeCallback();
            }
        }
    };

    private void removeCallback() {
        if (mImageHandler.hasCallbacks(mImageUpdateRunnable)) {
            mImageHandler.removeCallbacks(mImageUpdateRunnable);
        }
    }

    public QsPhotoShowcaseView(Context context) {
        this(context, false);
    }

    public QsPhotoShowcaseView(Context context, boolean settingsInterface) {
        super(context);
        mContext = context;
        mSettingsInterface = settingsInterface;

        mContext.registerReceiver(mReceiver, new IntentFilter(ACTIONS_QS_PHOTO_CHANGED), Context.RECEIVER_EXPORTED);
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED);
        mContext.registerReceiver(mScreenReceiver, filter, Context.RECEIVER_EXPORTED);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setScaleType(ScaleType.CENTER_CROP);

        if (mSettingsInterface) {
            radius = getContext().getResources().getDimension(R.dimen.qs_controls_container_radius);
            mPowerManager = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        } else {
            radius = moduleResources.getDimension(R.dimen.qs_controls_container_radius);
            mPowerManager = SystemUtils.PowerManager();
        }
        if (mPowerManager != null) {
            mPowerSaveModeEnabled = mPowerManager.isPowerSaveMode();
        }
        path = new Path();
        mActivityLauncherUtils = mSettingsInterface ? null : new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal());
        if (!mSettingsInterface) {
            setOnClickListener(v -> launchGalleryApp());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateImage();
    }

    private void launchGalleryApp() {
        if (mIsShowcase) {
            switchDrawable();
            return;
        }
        if (mActivityLauncherUtils == null) return;
        Intent galleryIntent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mActivityLauncherUtils.launchAppIfAvailable(galleryIntent, R.string.gallery, true);
    }

    public void switchDrawable() {
        if (mDrawables.isEmpty()) return;
        if (!mIsShowcase) return;

        mCurrentDrawableIndex = (mCurrentDrawableIndex + 1) % mDrawables.size();
        Drawable currentDrawable = getDrawable();
        Drawable nextDrawable = mDrawables.get(mCurrentDrawableIndex);

        if (currentDrawable == null || nextDrawable == null || mDrawables.size() == 1) {
            setImageDrawable(nextDrawable);
            return;
        }

        Drawable[] layers = new Drawable[]{currentDrawable, nextDrawable};
        TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        post(() -> {
            setImageDrawable(transitionDrawable);
            transitionDrawable.startTransition(275);
        });
    }

    private void updateImage() {
        if (mContext == null) return;
        mDrawables.clear();
        removeCallback();
        String imagePath = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/qs_photo.png";
        try {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    try {
                        // Load showcase images
                        File dir = new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer");
                        File[] filteredFiles = dir.listFiles((dir1, name) ->
                                name.startsWith("qs_photo_") && name.endsWith(".png"));
                        if (filteredFiles != null) {
                            mDrawables.clear();
                            for (File file : filteredFiles) {
                                ImageDecoder.Source source = ImageDecoder.createSource(file);

                                Drawable drawable = ImageDecoder.decodeDrawable(source);
                                mDrawables.add(drawable);
                            }
                        }
                        // load custom image
                        if (new File(imagePath).exists()) {
                            ImageDecoder.Source source = ImageDecoder.createSource(new File(imagePath));

                            Drawable drawable = ImageDecoder.decodeDrawable(source);

                            if (drawable instanceof AnimatedImageDrawable) {
                                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                                ((AnimatedImageDrawable) drawable).start();
                            }
                            mLoadedDrawable = drawable;
                        }
                        post(() -> {
                            if (mIsShowcase && !mDrawables.isEmpty()) {
                                // Set the first drawable from the showcase images
                                setImageDrawable(mDrawables.get(0));
                                mCurrentDrawableIndex = 0;
                            } else if (mLoadedDrawable != null) {
                                // Set the custom image if available
                                setImageDrawable(mLoadedDrawable);
                            } else {
                                // Fallback to default icon if no images are found
                                setImageDrawable(ResourcesCompat.getDrawable(
                                        mSettingsInterface ?
                                                mContext.getResources() :
                                                moduleResources,
                                        R.mipmap.ic_launcher,
                                        mContext.getTheme()));
                            }
                        });
                    } catch (Throwable ignored) {}

                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);

        } catch (Throwable ignored) {
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        rect = new RectF(0, 0, this.getWidth(), this.getHeight());
        path.reset();
        path.addRoundRect(rect, radius, radius, Path.Direction.CW);
        canvas.clipPath(path);
        super.onDraw(canvas);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void setRadius(int mRadius) {
        radius = dp2px(mContext, mRadius);
        post(this::invalidate);
    }

    public interface OnShowcaseClick {
        void onClick();
    }

    public void setPhotoMode(boolean isShowcase) {
        mIsShowcase = isShowcase;
        updateImage();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility == View.VISIBLE && isShown() && mScreenOn && !mPowerSaveModeEnabled) {
            if (!mImageHandler.hasCallbacks(mImageUpdateRunnable)) {
                long now = System.currentTimeMillis();
                long timeSinceLastRun = now - mLastRunTime;
                long delay = Math.max(0, mUpdateInterval - timeSinceLastRun);
                mImageHandler.postDelayed(mImageUpdateRunnable, delay);
            }
        } else {
            removeCallback();
        }
    }

}
