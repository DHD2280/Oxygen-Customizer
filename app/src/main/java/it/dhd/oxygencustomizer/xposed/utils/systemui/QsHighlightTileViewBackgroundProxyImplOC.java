package it.dhd.oxygencustomizer.xposed.utils.systemui;

import com.oplus.systemui.qs.base.widget.QsTileViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackground;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;
import com.oplus.systemui.qs.base.widget.QsViewOutlineProvider;

public class QsHighlightTileViewBackgroundProxyImplOC extends QsViewBackgroundProxy {

    private int mActiveColor, mDisabledColor, mInactiveColor;
    public NormalColorHighLightTileViewBackground normalColorHighLightTileViewBackground;
    public final QsTileViewInfoProvider qsTileView;

    public QsHighlightTileViewBackgroundProxyImplOC(QsTileViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.qsTileView = qsTileViewInfoProvider;
    }


    @Override
    public QsViewBackground getTargetQsViewBackground() {
        return getNormalTileViewBackground();
    }

    private final QsViewBackground getNormalTileViewBackground() {
//        if (QsViewBackgroundKt.isMixColorSupport(this.qsTileView)) {
//            return ensureMixColorHighLightTileViewBackground();
//        }
        return ensureNormalColorHighLightTileViewBackground();
    }

    public final NormalColorHighLightTileViewBackground ensureNormalColorHighLightTileViewBackground() {
        if (normalColorHighLightTileViewBackground == null) {
            normalColorHighLightTileViewBackground = new NormalColorTileViewBackgroundImpl();
        }
        normalColorHighLightTileViewBackground.setColors(mActiveColor, mInactiveColor, mDisabledColor);
        return normalColorHighLightTileViewBackground;
    }

    public class NormalColorTileViewBackgroundImpl extends NormalColorHighLightTileViewBackground {
        public NormalColorTileViewBackgroundImpl() {
            super(QsHighlightTileViewBackgroundProxyImplOC.this.qsTileView);
        }

        @Override
        public QsViewOutlineProvider getBgOutlineProvider() {
            return QsHighlightTileViewBackgroundProxyImplOC.this.ensureBgOutlineProvider();
        }
    }

    public void setColors(int activeColor, int inactiveColor, int disabledColor) {
        mActiveColor = activeColor;
        mInactiveColor = inactiveColor;
        mDisabledColor = disabledColor;
        if (normalColorHighLightTileViewBackground != null) {
            normalColorHighLightTileViewBackground.setColors(activeColor, inactiveColor, disabledColor);
        }
    }

}
