package it.dhd.oxygencustomizer.xposed.views.base;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

import androidx.appcompat.content.res.AppCompatResources;

import com.android.systemui.DependencyEx;
import com.android.systemui.animation.view.LaunchableLinearLayout;
import com.android.systemui.qs.OplusQuickSettingsEx;
import com.android.systemui.qs.personality.PersonalityManagerEx;
import com.oplus.systemui.blur.GaussBlurUtils;
import com.oplus.systemui.qs.base.tile.PressFeedbackHelper;
import com.oplus.systemui.qs.base.util.QsColorUtil;
import com.oplus.systemui.qs.base.widget.QsStaticViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundProxy;
import com.oplus.systemui.qs.base.widget.QsViewOutlineProvider;
import com.oplus.systemui.qs.widget.QsViewOutlineProviderKt;
import com.oplusos.systemui.common.blurability.MixColor;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.function.Function;

import it.dhd.oxygencustomizer.xposed.hooks.systemui.QsStyleObserver;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;

public abstract class BaseQsStaticView extends LaunchableLinearLayout implements QsStaticViewInfoProvider {

    private Context mContext;

    protected boolean mSettingsInterface;
    protected final QsViewBackgroundProxy backgroundProxy;

    public BaseQsStaticView(@Nullable Context context) {
        super(context);
        mContext = context;
        backgroundProxy = createBackgroundProxy(this);
    }

    private static QsViewBackgroundProxy createBackgroundProxy(QsStaticViewInfoProvider provider) {
        try {
            return QsViewBackgroundProxy.Companion.getQsStaticViewBackgroundProxy(provider);
        } catch (NoSuchMethodError ignored) {
            return QsViewBackgroundProxy.Companion.getMediaPanelBackgroundProxy(provider);
        }
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
        return switch (Build.VERSION.SDK_INT) {
            case 37 -> Color.parseColor("#33FFFFFF");
            case 36 -> QsColorUtil.obtainColorForQsPanelBackground(getContext(), false, false);
            default -> QsColorUtil.obtainColorForQsPanelBackground(getContext());
        };
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
        try {
            return QsViewOutlineProviderKt.getMediaPanelViewOutlineProvider(this, personalityManagerEx);
        } catch (NoClassDefFoundError | NoSuchMethodError ignored) {
            return createRoundRectOutlineProvider(personalityManagerEx);
        }
    }

    private QsViewOutlineProvider createRoundRectOutlineProvider(PersonalityManagerEx personalityManagerEx) {
        try {
            Class<?> roundRectProvider = ReflectionHelpers.findClass(
                    "com.oplus.systemui.qs.base.widget.QsViewOutlineProvider$QsViewRoundRectOutlineProvider",
                    getClass().getClassLoader());
            Function<Context, Float> radius = context -> personalityManagerEx.getMediaPanelRadius(context);
            return (QsViewOutlineProvider) roundRectProvider
                    .getConstructor(View.class, Function.class)
                    .newInstance(this, radius);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void updateBackground(Drawable drawable) {
        if (applyStockMediaPanelBackground()) return;
        QsStaticViewInfoProvider.super.updateBackground(drawable);
    }

    private Drawable mStockBgWrapper;
    private Drawable mStockThemeBgWrapper;
    private Drawable mStockTransitionDrawable;

    private boolean applyStockMediaPanelBackground() {
        if (Build.VERSION.SDK_INT < 37) return false;
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            if (mStockTransitionDrawable == null) {
                Constructor<?> wrapperConstructor = ReflectionHelpers.findClass("com.oplus.systemui.qs.base.res.drawable.TileDrawableWrapper", classLoader)
                        .getConstructor(Drawable.class);
                mStockBgWrapper = (Drawable) wrapperConstructor.newInstance((Object) null);
                mStockThemeBgWrapper = (Drawable) wrapperConstructor.newInstance((Object) null);
                mStockTransitionDrawable = (Drawable) ReflectionHelpers.findClass("com.oplus.systemui.qs.media.multilight.OplusQsMediaBackgroundDrawable", classLoader)
                        .getConstructor(Context.class, Drawable.class, Drawable.class)
                        .newInstance(getContext(), mStockBgWrapper, mStockThemeBgWrapper);
            }

            Object panelBuilder;
            Object outline;
            if (QsStyleObserver.isSeparateStyle()) {
                Object sepResPool = ReflectionHelpers.getStaticObjectField(
                        ReflectionHelpers.findClass("com.oplus.systemui.qs.base.res.SepQSResPool", classLoader), "INSTANCE");
                panelBuilder = stateFlowValue(ReflectionHelpers.callMethod(sepResPool, "getMediaPanelDrawable"));
                outline = stateFlowValue(ReflectionHelpers.callMethod(sepResPool, "getMediaPanelOutline"));
                setWrappedDrawable(mStockThemeBgWrapper,
                        buildQsDrawable(stateFlowValue(ReflectionHelpers.callMethod(sepResPool, "getMediaPanelThemeDrawable")), false));
                boolean globalTheme = (boolean) ReflectionHelpers.callStaticMethod(
                        ReflectionHelpers.findClass("com.oplus.systemui.qs.base.res.BaseQSResPool", classLoader), "globalThemeApplied");
                ReflectionHelpers.callMethod(mStockTransitionDrawable, "setTransitionAlpha", globalTheme ? 0 : 255);
            } else {
                Class<?> stdResPool = ReflectionHelpers.findClass("com.oplus.systemui.qs.base.res.StdQSResPool", classLoader);
                panelBuilder = stateFlowValue(ReflectionHelpers.getStaticObjectField(stdResPool, "mediaPanelDrawable"));
                outline = stateFlowValue(ReflectionHelpers.callMethod(ReflectionHelpers.getStaticObjectField(stdResPool, "INSTANCE"), "getMediaPanelOutline"));
            }
            setWrappedDrawable(mStockBgWrapper, buildQsDrawable(panelBuilder, true));

            ReflectionHelpers.callMethod(mStockTransitionDrawable, "setPathProvider", outline);
            Object cornerRadius = ReflectionHelpers.callMethod(outline, "getCornerRadius", new Class<?>[]{Drawable.class}, mStockTransitionDrawable);
            Object cornerWeight = ReflectionHelpers.callMethod(outline, "getCornerWeight", new Class<?>[]{Drawable.class}, mStockTransitionDrawable);
            ReflectionHelpers.callMethod(mStockTransitionDrawable, "setCornerParams",
                    new Class<?>[]{float.class, Float.class}, cornerRadius, cornerWeight);

            setOutlineProvider((ViewOutlineProvider) outline);
            setClipToOutline(true);
            setBackground(mStockTransitionDrawable);
            return true;
        } catch (Throwable t) {
            Logger.log(t);
            return false;
        }
    }

    private static Object stateFlowValue(Object stateFlow) {
        return ReflectionHelpers.callMethod(stateFlow, "getValue");
    }

    private Drawable buildQsDrawable(Object builder, boolean useDefaultFlag) {
        return (Drawable) ReflectionHelpers.callMethod(builder, "build",
                new Class<?>[]{View.class, boolean.class, Integer.class}, this, useDefaultFlag, null);
    }

    private static void setWrappedDrawable(Drawable wrapper, Drawable drawable) {
        ReflectionHelpers.callMethod(wrapper, "setDrawable", new Class<?>[]{Drawable.class}, drawable);
    }

    public void reloadBackground() {
        this.backgroundProxy.refreshViewBackground();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        reloadBackground();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        applyStockMediaPanelBackground();
        if (!mSettingsInterface) {
            attachPressFeedback(this, this);
        }
    }

    private void attachPressFeedback(View view, View parent) {
        try {
            PressFeedbackHelper.attachPressFeedback(view, parent);
        } catch (Throwable ignored) {
        } //method not found
        try {
            PressFeedbackHelper.QsTilePressFeedback qsTilePressFeedback = new PressFeedbackHelper.QsTilePressFeedback(this);
            PressFeedbackHelper.attachPressFeedback(this, qsTilePressFeedback);
        } catch (Throwable ignored) {
        }
    }

}
