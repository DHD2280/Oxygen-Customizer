package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.res.ColorStateList;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import androidx.annotation.NonNull;

import com.oplus.systemui.qs.base.widget.MixColorTileViewBackground;
import com.oplus.systemui.qs.base.widget.QsTileViewInfoProvider;
import com.oplus.systemui.qs.base.widget.QsViewBackgroundKt;
import com.oplusos.systemui.common.blurability.drawable.AutoBlurDrawable;


public abstract class MixColorTileViewBackgroundOC extends MixColorTileViewBackground {

    public static final Interpolator ICON_COLOR_TRANSITION_INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);

    public AutoBlurDrawable autoBlurDrawable;
    public ValueAnimator iconBgAnimator;
    public GradientDrawable maskDrawable;
    private int mActiveColor, mInactiveColor;
    public final QsTileViewInfoProvider qsTileView;


    public MixColorTileViewBackgroundOC(QsTileViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.qsTileView = qsTileViewInfoProvider;
    }

    @Override
    public void updateBackground(int i2, boolean z, boolean z2) {
        cancelIconBgAnimator();
        if (i2 == 2) {
            toActiveBackgroundOC(z2);
        } else {
            toInactiveBackgroundOC(z2);
        }
    }

    private final AutoBlurDrawable ensureAutoBlurDrawable() {
        AutoBlurDrawable autoBlurDrawable = this.autoBlurDrawable;
        if (autoBlurDrawable != null) {
            return autoBlurDrawable;
        }
        AutoBlurDrawable createMixColorDrawableForQsView$default = (AutoBlurDrawable) QsViewBackgroundKt.createMixColorDrawableForQsView(this.qsTileView.getBackgroundView(), null);
        this.autoBlurDrawable = createMixColorDrawableForQsView$default;
        return createMixColorDrawableForQsView$default;
    }

    public final void toActiveBackgroundOC(boolean z) {
        int activeColorWithDarkMode = mActiveColor;
        final GradientDrawable ensureMaskDrawable = ensureMaskDrawable();
        if (!z) {
            this.qsTileView.updateBackground(ensureMaskDrawable);
            this.qsTileView.updateBackgroundMask(null);
            ensureMaskDrawable.setColor(activeColorWithDarkMode);
            ensureMaskDrawable.invalidateSelf();
            return;
        }
        this.qsTileView.updateBackground(ensureAutoBlurDrawable());
        this.qsTileView.updateBackgroundMask(ensureMaskDrawable);
        getBgOutlineProvider().refreshSmoothRound();
        ColorStateList color = ensureMaskDrawable.getColor();
        ValueAnimator initIconBgAnimator = initIconBgAnimator(ensureMaskDrawable, color != null ? color.getDefaultColor() : 0, activeColorWithDarkMode, 200L);
        initIconBgAnimator.addUpdateListener(valueAnimator -> {
            ensureMaskDrawable.invalidateSelf();
            qsTileView.updateBackgroundMask(ensureMaskDrawable);
        });
        initIconBgAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationCancel(@NonNull Animator animator) {}

            @Override
            public void onAnimationRepeat(@NonNull Animator animator) {}

            @Override
            public void onAnimationStart(@NonNull Animator animator) {}

            @Override
            public void onAnimationEnd(@NonNull Animator animator) {
                qsTileView.updateBackgroundMask(null);
                qsTileView.updateBackground(ensureMaskDrawable);
            }
        });
        initIconBgAnimator.start();
    }

    private final ValueAnimator initIconBgAnimator(GradientDrawable gradientDrawable, int i2, int i3, long j2) {
        ValueAnimator valueAnimator = this.iconBgAnimator;
        if (valueAnimator == null) {
            valueAnimator = ObjectAnimator.ofArgb(gradientDrawable, "color", i2, i3);
            this.iconBgAnimator = valueAnimator;
        }
        valueAnimator.setTarget(gradientDrawable);
        valueAnimator.setIntValues(i2, i3);
        valueAnimator.setInterpolator(ICON_COLOR_TRANSITION_INTERPOLATOR);
        valueAnimator.setDuration(j2);
        valueAnimator.removeAllUpdateListeners();
        valueAnimator.removeAllListeners();
        return valueAnimator;
    }


    public final void toInactiveBackgroundOC(boolean z) {
        final GradientDrawable ensureMaskDrawable = ensureMaskDrawable();
        this.qsTileView.updateBackground(ensureAutoBlurDrawable());
        this.qsTileView.updateBackgroundMask(ensureMaskDrawable);
        getBgOutlineProvider().refreshSmoothRound();
        if (!z) {
            ensureMaskDrawable.setColor(0);
            ensureMaskDrawable.invalidateSelf();
        } else {
            ColorStateList color = ensureMaskDrawable.getColor();
            ValueAnimator initIconBgAnimator = initIconBgAnimator(ensureMaskDrawable, color != null ? color.getDefaultColor() : 0, 0, 250L);
            initIconBgAnimator.addUpdateListener(valueAnimator -> {
                ensureMaskDrawable.invalidateSelf();
                qsTileView.updateBackgroundMask(ensureMaskDrawable);
            });
            initIconBgAnimator.start();
        }
    }

    public void setColors(int activeColor, int inactiveColor) {
        this.mActiveColor = activeColor;
        this.mInactiveColor = inactiveColor;
    }

}
