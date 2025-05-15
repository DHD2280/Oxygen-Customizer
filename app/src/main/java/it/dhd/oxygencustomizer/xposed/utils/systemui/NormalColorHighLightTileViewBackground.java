package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.oplus.systemui.qs.base.widget.BaseTileViewBackground;
import com.oplus.systemui.qs.base.widget.QsTileViewInfoProvider;

public abstract class NormalColorHighLightTileViewBackground extends BaseTileViewBackground {

    public static final Interpolator ICON_COLOR_TRANSITION_INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);

    public GradientDrawable gradientDrawable;
    public ValueAnimator iconBgAnimator;
    public final QsTileViewInfoProvider qsTileView;
    private int mActiveColor, mInactiveColor, unavailableColor, inoperableColor;

    public NormalColorHighLightTileViewBackground(QsTileViewInfoProvider qsTileViewInfoProvider) {
        super(qsTileViewInfoProvider);
        this.qsTileView = qsTileViewInfoProvider;
    }

    public final Context getViewContext() {
        return this.qsTileView.getContext();
    }

    public final ValueAnimator getIconBgAnimator() {
        return this.iconBgAnimator;
    }

    public final void setIconBgAnimator(ValueAnimator valueAnimator) {
        this.iconBgAnimator = valueAnimator;
    }

    public ValueAnimator initIconBgAnimator(GradientDrawable gradientDrawable, int i2, int i3, long j2) {
        if (iconBgAnimator == null) {
            iconBgAnimator = ObjectAnimator.ofArgb(gradientDrawable, "color", i2, i3);
        }
        iconBgAnimator.setTarget(gradientDrawable);
        iconBgAnimator.setIntValues(i2, i3);
        iconBgAnimator.setInterpolator(ICON_COLOR_TRANSITION_INTERPOLATOR);
        iconBgAnimator.setDuration(j2);
        iconBgAnimator.removeAllUpdateListeners();
        iconBgAnimator.removeAllListeners();
        return iconBgAnimator;
    }

    public GradientDrawable ensureGradientDrawable() {
        GradientDrawable createOrUpdateGradientDrawable = createOrUpdateGradientDrawable(this.gradientDrawable);
        this.gradientDrawable = createOrUpdateGradientDrawable;
        return createOrUpdateGradientDrawable;
    }

    @Override
    public void updateBackground(int state, boolean z, boolean z2) {
        int tileColor;
        if (iconBgAnimator != null) {
            iconBgAnimator.cancel();
        }
        if (state == 1) {
            tileColor = obtainColorForInactive();
        } else if (state == 2) {
            tileColor = obtainColorForActive();
        } else if (state == 101) {
            tileColor = obtainColorForInoperable();
        } else {
            tileColor = obtainColorForUnavailable();
        }
        final GradientDrawable gradient = ensureGradientDrawable();
        gradient.setColor(tileColor);
        if (!z2) {
            gradient.setColor(tileColor);
            gradient.invalidateSelf();
            this.qsTileView.updateBackground(gradient);
        } else {
            ColorStateList color = gradient.getColor();
            ValueAnimator initIconBgAnimator = initIconBgAnimator(gradient, color != null ? color.getDefaultColor() : 0, tileColor, state == 2 ? 200L : 250L);
            initIconBgAnimator.addUpdateListener(valueAnimator2 -> {
                gradient.invalidateSelf();
                qsTileView.updateBackground(gradient);
            });
            initIconBgAnimator.start();
        }
    }

    public int obtainColorForActive() {
        return mActiveColor;
    }

    public int obtainColorForInactive() {
        return mInactiveColor;
    }

    public int obtainColorForUnavailable() {
        return unavailableColor;
    }

    public int obtainColorForInoperable() {
        return inoperableColor;
    }

    public void setColors(int activeColor, int inactiveColor, int strokeColor) {
        mActiveColor = activeColor;
        mInactiveColor = inactiveColor;
        unavailableColor = strokeColor;
        inoperableColor = Color.argb(77, Color.red(activeColor), Color.green(activeColor), Color.blue(activeColor));
    }

}

