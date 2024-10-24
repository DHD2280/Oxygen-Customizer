package it.dhd.oxygencustomizer.utils;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WallpaperLoaderTask {

    private final Context mContext;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final WallpaperLoaderListener mListener;

    public WallpaperLoaderTask(Context context, WallpaperLoaderListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void loadWallpaper() {
        executorService.execute(() -> {
            Bitmap bitmap = WallpaperUtil.getCompressedWallpaper(mContext, 100, WallpaperManager.FLAG_LOCK);

            mainHandler.post(() -> {
                if (bitmap != null) {
                    mListener.onWallpaperLoaded(bitmap);
                }
            });
        });
    }

    public void shutdown() {
        executorService.shutdown();
    }

    public interface WallpaperLoaderListener {
        void onWallpaperLoaded(Bitmap bitmap);
    }

}