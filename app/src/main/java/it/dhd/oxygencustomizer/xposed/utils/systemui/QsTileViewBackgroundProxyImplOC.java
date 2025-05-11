package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.util.Log;

import com.oplus.systemui.qs.base.widget.QsTileViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackground;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;
import com.oplus.systemui.qs.base.widget.QsViewOutlineProvider;

public final class QsTileViewBackgroundProxyImplOC extends QsViewBackgroundProxy {

    private int mActiveColor, mDisabledColor, mInactiveColor;
    public final QsTileViewInfoProvider qsTileView;
    public MixColorTileViewBackgroundImpl mixColorTileViewBackground;
    public NormalColorTileViewBackground normalColorTileViewBackground;

    public QsTileViewBackgroundProxyImplOC(QsTileViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.qsTileView = qsTileViewInfoProvider;
    }

    @Override
    public QsViewBackground getTargetQsViewBackground() {
        return getNormalTileViewBackground();
    }

    private QsViewBackground getNormalTileViewBackground() {
        return ensureNormalColorTileViewBackground();
    }

    private QsViewBackground getMixTileViewBackground() {
        return ensureMixColorTileViewBackground();
    }

    public final NormalColorTileViewBackground ensureNormalColorTileViewBackground() {
        if (normalColorTileViewBackground == null) {
            normalColorTileViewBackground = new NormalColorTileViewBackgroundImpl();
        }
        this.normalColorTileViewBackground.setColors(mActiveColor, mInactiveColor, mDisabledColor);
        return normalColorTileViewBackground;
    }

    public final MixColorTileViewBackgroundOC ensureMixColorTileViewBackground() {
        if (mixColorTileViewBackground == null) {
            mixColorTileViewBackground = new MixColorTileViewBackgroundImpl();
        }
        this.mixColorTileViewBackground.setColors(mActiveColor, mInactiveColor);
        return mixColorTileViewBackground;
    }


    public class MixColorTileViewBackgroundImpl extends MixColorTileViewBackgroundOC {
        public MixColorTileViewBackgroundImpl() {
            super(QsTileViewBackgroundProxyImplOC.this.qsTileView);
        }

        @Override
        public QsViewOutlineProvider getBgOutlineProvider() {
            return QsTileViewBackgroundProxyImplOC.this.ensureBgOutlineProvider();
        }
    }

    public class NormalColorTileViewBackgroundImpl extends NormalColorTileViewBackground {
        public NormalColorTileViewBackgroundImpl() {
            super(QsTileViewBackgroundProxyImplOC.this.qsTileView);
        }

        @Override
        public QsViewOutlineProvider getBgOutlineProvider() {
            return QsTileViewBackgroundProxyImplOC.this.ensureBgOutlineProvider();
        }
    }

    public void setColors(int activeColor, int inactiveColor, int disabledColor) {
        mActiveColor = activeColor;
        mInactiveColor = inactiveColor;
        mDisabledColor = disabledColor;
        if (this.normalColorTileViewBackground != null) {
            this.normalColorTileViewBackground.setColors(activeColor, inactiveColor, disabledColor);
        }
    }

    public void setRadii(boolean customRadius, float[] radii) {
        if (this.normalColorTileViewBackground != null) {
            this.normalColorTileViewBackground.setRadii(customRadius, radii);
        }
    }

}
