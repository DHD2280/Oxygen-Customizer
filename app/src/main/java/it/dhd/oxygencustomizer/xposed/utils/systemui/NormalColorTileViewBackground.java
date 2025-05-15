package it.dhd.oxygencustomizer.xposed.utils.systemui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;

import com.oplus.systemui.qs.base.widget.BaseTileViewBackground;
import com.oplus.systemui.qs.base.widget.QsTileViewInfoProvider;

public abstract class NormalColorTileViewBackground extends BaseTileViewBackground {

    public static final Interpolator ICON_COLOR_TRANSITION_INTERPOLATOR = new PathInterpolator(0.33f, 0.0f, 0.67f, 1.0f);

    public GradientDrawable gradientDrawable;
    public ValueAnimator iconBgAnimator;
    public final QsTileViewInfoProvider qsTileView;
    private int mActiveColor, mInactiveColor, unavailableColor, inoperableColor;
    private boolean customRadius = false;
    private float[] radii = {0, 0, 0, 0, 0, 0, 0, 0};

    public NormalColorTileViewBackground(QsTileViewInfoProvider qsTileViewInfoProvider) {
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

    public GradientDrawable ensureGradientDrawable() {
        GradientDrawable createOrUpdateGradientDrawable = createOrUpdateGradientDrawable(this.gradientDrawable);

        this.gradientDrawable = createOrUpdateGradientDrawable;
        return createOrUpdateGradientDrawable;
    }

    @Override // com.oplus.systemui.p127qs.base.widget.BaseTileViewBackground
    public void updateBackground(int i2, boolean z, boolean z2) {
        Log.d("NormalColorTileViewBackground", "updateBackground: i2=" + i2 + ", z=" + z + ", z2=" + z2);
        int obtainColorForInactive;
        ValueAnimator valueAnimator = this.iconBgAnimator;
        if (valueAnimator != null) {
            valueAnimator.cancel();
        }
        if (i2 == 1) {
            obtainColorForInactive = obtainColorForInactive(getViewContext());
        } else if (i2 == 2) {
            obtainColorForInactive = obtainColorForActive(getViewContext(), z);
        } else if (i2 == 101) {
            obtainColorForInactive = obtainColorForInoperable(getViewContext(), z);
        } else {
            obtainColorForInactive = obtainColorForUnavailable(getViewContext());
        }
        int i3 = obtainColorForInactive;
        final GradientDrawable ensureGradientDrawable = ensureGradientDrawable();
        ensureGradientDrawable.setColor(i3);
        if (!z2) {
            ensureGradientDrawable.setColor(i3);
            ensureGradientDrawable.invalidateSelf();
            this.qsTileView.updateBackground(ensureGradientDrawable);
        } else {
            ColorStateList color = ensureGradientDrawable.getColor();
            ValueAnimator initIconBgAnimator = initIconBgAnimator(ensureGradientDrawable, color != null ? color.getDefaultColor() : 0, i3, i2 == 2 ? 200L : 250L);
            initIconBgAnimator.addUpdateListener(valueAnimator2 -> {
                ensureGradientDrawable.invalidateSelf();
                qsTileView.updateBackground(ensureGradientDrawable);
            });
            initIconBgAnimator.start();
        }
    }

    public int obtainColorForActive(Context context, boolean z) {
        Log.d("NormalColorTileViewBackground", "obtainColorForActive: activeColor" + Integer.toHexString(mActiveColor));
        return mActiveColor;
//        return this.qsTileView.getActiveColorWithDarkMode(context);
    }

    public int obtainColorForInactive(Context context) {
//        return QsColorUtil.obtainColorForQsPanelBackground(context);
        return mInactiveColor;
    }

    public int obtainColorForUnavailable(Context context) {
//        return QsColorUtil.obtainColorForTileUnavailable(context);
        return unavailableColor;
    }

    public int obtainColorForInoperable(Context context, boolean z) {
        return inoperableColor;
//        return BaseTileViewBackground.Companion.setColorAlpha(obtainColorForActive(context, z), 0.3f);
    }

    public void setColors(int activeColor, int inactiveColor, int strokeColor) {
        Log.d("NormalColorTileViewBackground", "setColors: activeColor=" + Integer.toHexString(activeColor) + ", inactiveColor=" + Integer.toHexString(inactiveColor) + ", strokeColor=" + Integer.toHexString(strokeColor));
        mActiveColor = activeColor;
        mInactiveColor = inactiveColor;
        unavailableColor = strokeColor;
        inoperableColor = Color.argb(77, Color.red(activeColor), Color.green(activeColor), Color.blue(activeColor));
    }

    public void setRadii(boolean useCustomRadius, float[] radii) {
        customRadius = useCustomRadius;
        this.radii = radii;
    }

}
