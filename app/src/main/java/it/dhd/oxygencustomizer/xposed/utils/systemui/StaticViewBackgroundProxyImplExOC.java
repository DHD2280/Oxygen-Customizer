package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;

import com.oplus.systemui.qs.base.widget.BaseQsViewBackground;
import com.oplus.systemui.qs.base.widget.QsStaticViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackground;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;
import com.oplus.systemui.qs.base.widget.QsViewOutlineProvider;

import de.robv.android.xposed.XposedBridge;

/**
 * A transparent background proxy for any {@link QsStaticViewInfoProvider}
 * Only when {@link BaseQsViewBackground not available}
 */
public class StaticViewBackgroundProxyImplExOC extends QsViewBackgroundProxy {

    private int mBackgroundColor = Color.TRANSPARENT;
    public final QsStaticViewInfoProvider panelInfo;
    public NormalColorNonFunctionalBackgroundEx normalColorTileViewBackgroundEx;


    public StaticViewBackgroundProxyImplExOC(QsStaticViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.panelInfo = qsTileViewInfoProvider;
    }

    @Override
    public QsViewBackground getTargetQsViewBackground() {
        return getNormalTileViewBackgroundEx();
    }

    private QsViewBackground getNormalTileViewBackgroundEx() {
        return ensureNormalColorTileViewBackgroundEx();
    }

    public final NormalColorNonFunctionalBackgroundEx ensureNormalColorTileViewBackgroundEx() {
        if (normalColorTileViewBackgroundEx == null) {
            normalColorTileViewBackgroundEx = new NormalColorNonFunctionalBackgroundEx();
        }
        return normalColorTileViewBackgroundEx;
    }

    public void setColors(int backgroundColor) {
        mBackgroundColor = backgroundColor;
        if (this.normalColorTileViewBackgroundEx != null) {
            this.normalColorTileViewBackgroundEx.refreshViewBackground();
        }
    }

    public final class NormalColorNonFunctionalBackgroundEx implements QsViewBackground {
        public NormalColorNonFunctionalBackgroundEx() {
        }

        @Override
        public void onBackgroundAttach() {
            StaticViewBackgroundProxyImplExOC.this.panelInfo.updateBgOutlineProvider(StaticViewBackgroundProxyImplExOC.this.ensureBgOutlineProvider());
            QsStaticViewInfoProvider qsStaticViewInfoProvider = StaticViewBackgroundProxyImplExOC.this.panelInfo;
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(mBackgroundColor);
            qsStaticViewInfoProvider.updateBackground(gradientDrawable);
        }

        @Override
        public void refreshViewBackground() {
            Drawable backgroundDrawable = StaticViewBackgroundProxyImplExOC.this.panelInfo.getBackgroundDrawable();
            GradientDrawable gradientDrawable = backgroundDrawable instanceof GradientDrawable ? (GradientDrawable) backgroundDrawable : null;
            if (gradientDrawable != null) {
                gradientDrawable.setColor(mBackgroundColor);
                gradientDrawable.invalidateSelf();
            }
        }
    }



}
