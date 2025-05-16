package it.dhd.oxygencustomizer.xposed.views.base;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.content.res.AppCompatResources;

import com.android.systemui.DependencyEx;
import com.android.systemui.animation.view.LaunchableLinearLayout;
import com.android.systemui.qs.OplusQuickSettingsEx;
import com.android.systemui.qs.personality.PersonalityManagerEx;
import com.oplus.systemui.blur.GaussBlurUtils;
import com.oplus.systemui.qs.base.util.QsColorUtil;
import com.oplus.systemui.qs.base.widget.QsStaticViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;
import com.oplus.systemui.qs.base.widget.QsViewOutlineProvider;
import com.oplus.systemui.qs.widget.QsViewOutlineProviderKt;
import com.oplusos.systemui.common.blurability.MixColor;

import org.jetbrains.annotations.Nullable;

public abstract class BaseQsStaticView extends LaunchableLinearLayout implements QsStaticViewInfoProvider {

    private Context mContext;
    protected final QsViewBackgroundProxy backgroundProxy;

    public BaseQsStaticView(@Nullable Context context) {
        super(context);
        mContext = context;
        backgroundProxy = QsViewBackgroundProxy.Companion.getMediaPanelBackgroundProxy(this);
    }

    @Override
    public View getBackgroundView() {
        return this;
    }

    @Override
    public boolean isGaussBlurDisabled() {
        return GaussBlurUtils.isGaussBlurDisabled();
    }

    @Override
    public Drawable getGlobalThemeBackgroundDrawable() {
        return AppCompatResources.getDrawable(getContext(), getContext().getResources().getIdentifier("status_bar_qs_media_bg_active", "drawable", SYSTEM_UI));
    }

    @Override
    public MixColor getMixColor() {
        return null;
    }

    @Override
    public int getNormalBackGroundColor() {
        return QsColorUtil.obtainColorForQsPanelBackground(getContext());
    }

    @Override
    public boolean isApplyGlobalTheme() {
        return true;
    }

    @Override
    public QsViewOutlineProvider getBgOutlineProvider() {
        PersonalityManagerEx personalityManagerEx;
        try {
            personalityManagerEx = ((OplusQuickSettingsEx) DependencyEx.get(OplusQuickSettingsEx.class)).getPersonalityManagerEx();
        } catch (Throwable ignored) {
            personalityManagerEx = ((OplusQuickSettingsEx) DependencyEx.sDependency.getDependency(OplusQuickSettingsEx.class)).getPersonalityManagerEx();
        }
        return QsViewOutlineProviderKt.getMediaPanelViewOutlineProvider(this, personalityManagerEx);
    }

    public void reloadBackground() {
        this.backgroundProxy.refreshViewBackground();
    }

}
