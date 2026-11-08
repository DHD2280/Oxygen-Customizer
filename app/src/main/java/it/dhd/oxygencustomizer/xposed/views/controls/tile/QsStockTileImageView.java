package it.dhd.oxygencustomizer.xposed.views.controls.tile;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import it.dhd.oxygencustomizer.xposed.utils.systemui.StockQsTileResources;

public class QsStockTileImageView extends ImageView {

    private int mTileState = StockQsTileResources.STATE_INACTIVE;
    private boolean mUseStockIconColors = true;
    private Integer mColorfulConfig = null;

    public QsStockTileImageView(Context context) {
        super(context);
        refreshDrawableState();
    }

    public void applyStockBackground() {
        Drawable background = StockQsTileResources.createTileBackground(this);
        if (background != null) {
            setBackground(background);
            setBackgroundTintList(null);
        }
    }

    public void setTileState(int tileState) {
        if (mTileState == tileState) return;
        mTileState = tileState;
        refreshDrawableState();
    }

    public void setColorfulConfig(Integer colorfulConfig) {
        mColorfulConfig = colorfulConfig;
    }

    public void setUseStockIconColors(boolean useStockIconColors) {
        mUseStockIconColors = useStockIconColors;
        if (useStockIconColors) {
            ColorStateList iconColors = StockQsTileResources.getIconColors(this, mColorfulConfig);
            if (iconColors != null) setImageTintList(iconColors);
        }
    }

    public boolean usesStockIconColors() {
        return mUseStockIconColors;
    }

    @Override
    public int[] onCreateDrawableState(int extraSpace) {
        int[] stateFlag = StockQsTileResources.getStateFlag(this, mTileState);
        int[] drawableState = super.onCreateDrawableState(extraSpace + stateFlag.length);
        return mergeDrawableStates(drawableState, stateFlag);
    }
}
