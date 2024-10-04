package it.dhd.oxygencustomizer.xposed.views;

import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_QS_PHOTO_CHANGED;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
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
import android.os.Environment;
import android.provider.MediaStore;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QsWidgets;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;

@SuppressLint({"ViewConstructor", "AppCompatCustomView"})
public class QsPhotoShowcaseView extends ImageView {

    private Context mContext;
    private final ActivityLauncherUtils mActivityLauncherUtils;

    private float radius;
    private final Path path;
    private RectF rect;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateImage();
        }
    };

    public QsPhotoShowcaseView(Context context) {
        super(context);
        mContext = context;

        mContext.registerReceiver(mReceiver, new IntentFilter(ACTIONS_QS_PHOTO_CHANGED), Context.RECEIVER_EXPORTED);

        setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        setScaleType(ScaleType.CENTER_CROP);

        radius = modRes.getDimension(R.dimen.qs_controls_container_radius);
        path = new Path();
        mContext = context;
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, QsWidgets.mActivityStarter);
        setOnClickListener(v -> launchGalleryApp());
        updateImage();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateImage();
    }

    private void launchGalleryApp() {
        Intent galleryIntent = new Intent(Intent.ACTION_VIEW, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        mActivityLauncherUtils.launchAppIfAvailable(galleryIntent, R.string.gallery, true);
    }

    private void updateImage() {
        if (mContext == null) return;

        String imagePath = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/qs_photo.png";
        try {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    try {
                        if (new File(imagePath).exists()) {
                            ImageDecoder.Source source = ImageDecoder.createSource(new File(imagePath));

                            Drawable drawable = ImageDecoder.decodeDrawable(source);
                            post(() -> setImageDrawable(drawable));

                            if (drawable instanceof AnimatedImageDrawable) {
                                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                                ((AnimatedImageDrawable) drawable).start();
                            }
                        } else {
                            post(() ->setImageDrawable(ResourcesCompat.getDrawable(
                                    modRes,
                                    R.mipmap.ic_launcher,
                                    mContext.getTheme()
                            )));
                        }
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
}
