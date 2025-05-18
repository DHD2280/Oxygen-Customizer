package it.dhd.oxygencustomizer.xposed.views.controls.media;

import android.graphics.drawable.Drawable;

public interface QsMediaTileI {

    void updatePrefs(boolean showAlbumArt, int mediaQsArtFilter, int mediaQsTintColor, int mediaQsTintAmount, float mediaQsArtBlurAmount);

    void updateDefaultBackground(Drawable defDrawable);

    void updateColors(Drawable defDrawable, boolean customColor, int color);

}
