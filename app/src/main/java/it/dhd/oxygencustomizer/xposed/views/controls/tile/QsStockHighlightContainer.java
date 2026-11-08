package it.dhd.oxygencustomizer.xposed.views.controls.tile;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import it.dhd.oxygencustomizer.xposed.utils.systemui.StockQsTileResources;

public class QsStockHighlightContainer extends FrameLayout {

    public static final float ICON_BG_SIZE_DP = 42f;
    public static final float ICON_BG_MARGIN_START_DP = 10f;

    private final ImageView mIconBackgroundView;
    private final QsStockHighlightTile mTile;
    private int mTileState;

    public QsStockHighlightContainer(Context context, QsStockHighlightTile tile) {
        super(context);
        mTileState = StockQsTileResources.STATE_INACTIVE;
        mTile = tile;
        mIconBackgroundView = new ImageView(context);
        mIconBackgroundView.setDuplicateParentStateEnabled(true);
        LayoutParams iconParams = new LayoutParams(dp(ICON_BG_SIZE_DP), dp(ICON_BG_SIZE_DP), Gravity.START | Gravity.CENTER_VERTICAL);
        iconParams.setMarginStart(dp(ICON_BG_MARGIN_START_DP));
        addView(mIconBackgroundView, iconParams);
        addView(tile, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        tile.attachContainer(this);
        refreshDrawableState();
    }

    public QsStockHighlightTile getTile() {
        return mTile;
    }

    public void applyStockBackground(boolean showIconBackground) {
        Drawable background = StockQsTileResources.createHighlightBackground(this);
        if (background != null) setBackground(background);
        Drawable iconBackground = showIconBackground ? StockQsTileResources.createHighlightIconBackground(mIconBackgroundView) : null;
        mIconBackgroundView.setBackground(iconBackground);
        mIconBackgroundView.setVisibility(iconBackground != null ? VISIBLE : GONE);
    }

    public void setTileState(int tileState) {
        if (mTileState == tileState) return;
        mTileState = tileState;
        refreshDrawableState();
        mTile.rebuildIcon();
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] stateFlag = StockQsTileResources.getToggleableStateFlag(this, mTileState);
        int[] drawableState = super.onCreateDrawableState(extraSpace + stateFlag.length);
        return mergeDrawableStates(drawableState, stateFlag);
    }

    private int dp(float value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getResources().getDisplayMetrics()));
    }
}
