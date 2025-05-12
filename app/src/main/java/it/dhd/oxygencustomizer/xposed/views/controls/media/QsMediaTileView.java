package it.dhd.oxygencustomizer.xposed.views.controls.media;

import android.content.Context;

import com.oplus.systemui.qs.base.tile.PressFeedbackHelper;

import org.jetbrains.annotations.Nullable;

import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;
import it.dhd.oxygencustomizer.xposed.utils.systemui.StaticViewBackgroundProxyImplOC;

public class QsMediaTileView extends BaseQsStaticView {

    private static final String TAG = "QsMediaTileView";
    private final Context mContext;
    private final MediaBinder mMediaBinder;

    public QsMediaTileView(@Nullable Context context) {
        super(context);
        mContext = context;
        mMediaBinder = new MediaBinder(mContext, TAG);
        mMediaBinder.inflateView(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.backgroundProxy.onBackgroundAttach();
        PressFeedbackHelper.attachPressFeedback(this, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.backgroundProxy.onBackgroundDetach();
    }

    public void refreshViewBackground() {
        this.backgroundProxy.refreshViewBackground();
    }

    public void updatePrefs(boolean showAlbumArt, int mediaQsArtFilter, int mediaQsTintColor, int mediaQsTintAmount, float mediaQsArtBlurAmount) {
        mMediaBinder.updatePrefs(showAlbumArt, mediaQsArtFilter, mediaQsTintColor, mediaQsTintAmount, mediaQsArtBlurAmount);
    }

    public void updateColors(boolean custom, int color) {
        if (backgroundProxy instanceof StaticViewBackgroundProxyImplOC staticViewBackgroundProxyImplOC) {
            staticViewBackgroundProxyImplOC.setColors(custom ? color : super.getNormalBackGroundColor());
            refreshViewBackground();
        }
    }

}
