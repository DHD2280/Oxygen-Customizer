package it.dhd.oxygencustomizer.xposed.views.controls.tile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.Nullable;

import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.systemui.StockQsTileResources;

public class QsStockHighlightTile extends ExtendedFAB {

    private static final float ICON_SIZE_DP = 24f;
    private static final float TEXT_PADDING_START_DP = 6f;
    private static final float TEXT_PADDING_END_DP = 15f;
    private static final float TEXT_SIZE_DP = 14f;

    private QsStockHighlightContainer mContainer;
    private Integer mColorfulConfig;
    private boolean mUseStockIconColors;
    private Drawable mRawIcon;
    private ColorStateList mInnerIconTint;

    public QsStockHighlightTile(Context context) {
        super(context);
        mUseStockIconColors = true;
        float iconInset = (QsStockHighlightContainer.ICON_BG_SIZE_DP - ICON_SIZE_DP) / 2f;
        setIconSize(dp(ICON_SIZE_DP));
        setIconPadding(dp(iconInset + TEXT_PADDING_START_DP));
        setPaddingRelative(dp(QsStockHighlightContainer.ICON_BG_MARGIN_START_DP + iconInset), 0, dp(TEXT_PADDING_END_DP), 0);
        setTextSize(TypedValue.COMPLEX_UNIT_PX, dp(TEXT_SIZE_DP));
        setAllCaps(false);
        setElevation(0);
        setStateListAnimator(null);
        setInsetTop(0);
        setInsetBottom(0);
        setMinHeight(0);
        setMinimumHeight(0);
        setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        setRippleColor(ColorStateList.valueOf(Color.TRANSPARENT));
    }

    void attachContainer(QsStockHighlightContainer container) {
        mContainer = container;
    }

    public QsStockHighlightContainer getContainer() {
        return mContainer;
    }

    public void applyStockBackground() {
        if (mContainer != null) mContainer.applyStockBackground(mUseStockIconColors);
    }

    public void setTileState(int tileState) {
        if (mContainer != null) mContainer.setTileState(tileState);
    }

    public void setColorfulConfig(Integer colorfulConfig) {
        mColorfulConfig = colorfulConfig;
    }

    public void setUseStockIconColors(boolean useStockIconColors) {
        boolean changed = mUseStockIconColors != useStockIconColors;
        mUseStockIconColors = useStockIconColors;
        View host = mContainer != null ? mContainer : this;
        mInnerIconTint = useStockIconColors ? StockQsTileResources.getIconColors(host, mColorfulConfig) : null;
        ColorStateList labelColors = StockQsTileResources.getIconColors(host, null);
        if (labelColors != null) setTextColor(labelColors.getDefaultColor());
        if (changed) applyStockBackground();
        rebuildIcon();
    }

    @Override
    public void setIcon(@Nullable Drawable icon) {
        mRawIcon = icon;
        rebuildIcon();
    }

    @Override
    public void setIconTint(@Nullable ColorStateList iconTint) {
        if (mUseStockIconColors) return;
        mInnerIconTint = iconTint;
        rebuildIcon();
    }

    void rebuildIcon() {
        if (mRawIcon == null) {
            super.setIcon(null);
            return;
        }
        Drawable icon = mRawIcon.mutate();
        if (mInnerIconTint != null) {
            int[] state = mContainer != null ? mContainer.getDrawableState() : getDrawableState();
            icon.setColorFilter(new PorterDuffColorFilter(mInnerIconTint.getColorForState(state, mInnerIconTint.getDefaultColor()), PorterDuff.Mode.SRC_IN));
        } else {
            icon.setColorFilter(null);
        }
        super.setIconTint(null);
        super.setIcon(icon);
    }

    private int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
