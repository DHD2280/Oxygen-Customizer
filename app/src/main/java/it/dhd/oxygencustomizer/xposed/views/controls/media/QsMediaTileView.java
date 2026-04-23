package it.dhd.oxygencustomizer.xposed.views.controls.media;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;

import org.jetbrains.annotations.Nullable;

import it.dhd.oxygencustomizer.xposed.utils.systemui.StaticViewBackgroundProxyImplOC;
import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;

/**
 * OOS 15 custom Media Tile
 * This implements {@link BaseQsStaticView} to gain the background proxy
 */
public class QsMediaTileView extends BaseQsStaticView implements QsMediaTileI {

    private static final String TAG = "QsMediaTileView";
    private final Context mContext;
    private final QsMediaTile mMediaTile;

    public QsMediaTileView(@Nullable Context context) {
        super(context);
        mContext = context;
        mMediaTile = new QsMediaTile(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        addView(mMediaTile);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.backgroundProxy.onBackgroundAttach();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.backgroundProxy.onBackgroundDetach();
    }

    public void refreshViewBackground() {
        this.backgroundProxy.refreshViewBackground();
    }

    @Override
    public void updatePrefs(boolean showAlbumArt, int mediaQsArtFilter, int mediaQsTintColor, int mediaQsTintAmount, float mediaQsArtBlurAmount) {
        mMediaTile.updatePrefs(showAlbumArt, mediaQsArtFilter, mediaQsTintColor, mediaQsTintAmount, mediaQsArtBlurAmount);
    }

    @Override
    public void updateDefaultBackground(Drawable defDrawable) {}

    @Override
    public void updateColors(Drawable defDrawable, boolean customColor, int color) {
        if (backgroundProxy instanceof StaticViewBackgroundProxyImplOC staticViewBackgroundProxyImplOC) {
            staticViewBackgroundProxyImplOC.setColors(customColor ? color : super.getNormalBackGroundColor());
            refreshViewBackground();
        }
    }

}
