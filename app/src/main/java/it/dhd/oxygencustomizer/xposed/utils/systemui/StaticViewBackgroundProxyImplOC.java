package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;

import com.oplus.systemui.qs.base.widget.BaseQsViewBackground;
import com.oplus.systemui.qs.base.widget.QsStaticViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackground;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;
import com.oplus.systemui.qs.base.widget.QsViewOutlineProvider;

/**
 * A transparent background proxy for any {@link QsStaticViewInfoProvider}
 */
public class StaticViewBackgroundProxyImplOC extends QsViewBackgroundProxy {

    private int mBackgroundColor = Color.TRANSPARENT;
    public final QsStaticViewInfoProvider panelInfo;
    public NormalColorNonFunctionalBackground normalColorTileViewBackground;


    public StaticViewBackgroundProxyImplOC(QsStaticViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.panelInfo = qsTileViewInfoProvider;
    }

    @Override
    public QsViewBackground getTargetQsViewBackground() {
        return getNormalTileViewBackground();
    }

    private QsViewBackground getNormalTileViewBackground() {
        return ensureNormalColorTileViewBackground();
    }

    public final NormalColorNonFunctionalBackground ensureNormalColorTileViewBackground() {
        if (normalColorTileViewBackground == null) {
            normalColorTileViewBackground = new StaticViewBackgroundProxyImplOC.NormalColorNonFunctionalBackground();
        }
        return normalColorTileViewBackground;
    }

    public void setColors(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        if (this.normalColorTileViewBackground != null) {
            this.normalColorTileViewBackground.refreshViewBackground();
        }
    }

    public final class NormalColorNonFunctionalBackground extends BaseQsViewBackground {
        public GradientDrawable maskDrawableNormal;

        public NormalColorNonFunctionalBackground() {
            super(StaticViewBackgroundProxyImplOC.this.panelInfo);
        }

        @Override
        public QsViewOutlineProvider getBgOutlineProvider() {
            return StaticViewBackgroundProxyImplOC.this.ensureBgOutlineProvider();
        }

        @Override
        public void onBackgroundAttach() {
            StaticViewBackgroundProxyImplOC.this.panelInfo.updateBgOutlineProvider(StaticViewBackgroundProxyImplOC.this.ensureBgOutlineProvider());
            GradientDrawable ensureMaskDrawable = ensureMaskDrawable();
            ensureMaskDrawable.setColor(mBackgroundColor);
            StaticViewBackgroundProxyImplOC.this.panelInfo.updateBackground(ensureMaskDrawable);
        }

        @Override
        public void refreshViewBackground() {
            GradientDrawable ensureMaskDrawable = ensureMaskDrawable();
            ensureMaskDrawable.setColor(mBackgroundColor);
            ensureMaskDrawable.invalidateSelf();
            StaticViewBackgroundProxyImplOC.this.panelInfo.updateBackground(ensureMaskDrawable);
        }

        public final GradientDrawable getMaskDrawableNormal() {
            return this.maskDrawableNormal;
        }

        public final void setMaskDrawableNormal(GradientDrawable gradientDrawable) {
            this.maskDrawableNormal = gradientDrawable;
        }

        public final GradientDrawable ensureMaskDrawable() {
            GradientDrawable createOrUpdateGradientDrawable = createOrUpdateGradientDrawable(this.maskDrawableNormal);
            this.maskDrawableNormal = createOrUpdateGradientDrawable;
            return createOrUpdateGradientDrawable;
        }
    }


}
