package it.dhd.oxygencustomizer.xposed.views.controls.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.LinearLayout;

public class QsMediaTile extends LinearLayout implements QsMediaTileI {

    private final Context mContext;
    private final MediaBinder mMediaBinder;

    public QsMediaTile(Context context) {
        super(context);

        mContext = context;
        mMediaBinder = new MediaBinder(mContext, this.getClass().getSimpleName());
        mMediaBinder.inflateView(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void updatePrefs(boolean showAlbumArt, int mediaQsArtFilter, int mediaQsTintColor, int mediaQsTintAmount, float mediaQsArtBlurAmount) {
        mMediaBinder.updatePrefs(showAlbumArt, mediaQsArtFilter, mediaQsTintColor, mediaQsTintAmount, mediaQsArtBlurAmount);
    }

    public void updateDefaultBackground(Drawable defDrawable) {
        mMediaBinder.updateDefaultBackground(defDrawable);
    }

    public void updateColors(Drawable defDrawable, boolean customColor, int color) {
       mMediaBinder.updateColors(defDrawable, customColor, color);
    }

}
