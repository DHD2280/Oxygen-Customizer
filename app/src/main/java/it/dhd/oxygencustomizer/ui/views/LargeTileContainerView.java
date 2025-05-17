package it.dhd.oxygencustomizer.ui.views;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;

public class LargeTileContainerView extends TileContainerView {

    public LinearLayout mBrightnessTileContainer;
    public LinearLayout mFirstTileContainer;
    public ViewGroup mMediaPanelContainer;
    public int mPaddingTop;
    public LinearLayout mSecondTileContainer;
    public List<ViewCellInfo> mViewCells = new ArrayList();
    public LinearLayout mVolumeTileContainer;
    private Map<View, int[]> mCustomWidgetsMap = new HashMap();

    private int cellSize = 183, cellMargin = 42, cellM2 = 42;

    public LargeTileContainerView(Context context) {
        super(context);
        mPaddingTop = dp2px(getContext(), 12);
    }

    public LargeTileContainerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        mPaddingTop = dp2px(getContext(), 12);
    }

    @Override
    public void onLayout(boolean z, int i2, int i3, int i4, int i5) {
        layoutView(getLayoutDirection() == View.LAYOUT_DIRECTION_RTL);
    }

    @Override
    public void updateCell() {
        super.updateCell();
    }

    public void setInts(int cellSize, int cellMargin, int cellM2) {
        this.cellSize = cellSize;
        this.cellMargin = cellMargin;
        this.cellM2 = cellM2;
        requestLayout();
        invalidate();
    }

    @Override
    public void onMeasure(int i2, int i3) {
        updateResources(cellSize, cellMargin, cellM2);
        super.setMeasuredDimension(View.MeasureSpec.getSize(i2), ((mCellHeight + mCellMarginVertical) * mRows) + mPaddingTop);
        measureView();
    }

    public final void measureView() {
        for (ViewCellInfo viewCellInfo : mViewCells) {
            if (viewCellInfo.mView.getVisibility() != View.GONE) {
                int i2 = viewCellInfo.spanX;
                int i3 = (mCellWidth * i2) + ((i2 - 1) * mCellMarginHorizontal);
                int i4 = viewCellInfo.spanY;
                int i5 = (mCellHeight * i4) + ((i4 - 1) * mCellMarginVertical);
                viewCellInfo.mView.measure(exactly(i3), exactly(i5));
            }
        }
    }

    public final void layoutView(boolean z) {
        int totalContentWidth = mColumns * (mCellWidth + mCellMarginHorizontal) - mCellMarginHorizontal;

        int availableWidth = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
        int extraSpace = Math.max(0, availableWidth - totalContentWidth);
        int horizontalOffset = extraSpace / 2; // Centra lo spazio extra

        for (ViewCellInfo viewCellInfo : mViewCells) {
            int top = (viewCellInfo.mY * (mCellHeight + mCellMarginVertical)) + mPaddingTop;
            int bottom = top + viewCellInfo.mView.getMeasuredHeight();

            int left = ((z ? (mColumns - viewCellInfo.spanX) - viewCellInfo.mX : viewCellInfo.mX)
                    * (mCellWidth + mCellMarginHorizontal) + getPaddingLeft() + horizontalOffset);

            viewCellInfo.mView.layout(
                    left,
                    top,
                    left + viewCellInfo.mView.getMeasuredWidth(),
                    bottom
            );
        }
    }

    @Override
    public void onFinishInflate() {
        super.onFinishInflate();
        mMediaPanelContainer = (ViewGroup) findViewById(R.id.qs_media_panel_layout);
        mBrightnessTileContainer = (LinearLayout) findViewById(R.id.qs_brightness_slider_layout);
        mVolumeTileContainer = (LinearLayout) findViewById(R.id.qs_volume_slider_layout);
        mFirstTileContainer = (LinearLayout) findViewById(R.id.separate_hl_tile_one_holder);
        mSecondTileContainer = (LinearLayout) findViewById(R.id.separate_hl_tile_two_holder);
        mViewCells.add(new ViewCellInfo(mMediaPanelContainer, 0, 1, 2, 2));
        mViewCells.add(new ViewCellInfo(mBrightnessTileContainer, 2, 1, 1, 2));
        mViewCells.add(new ViewCellInfo(mVolumeTileContainer, 3, 1, 1, 2));
        mViewCells.add(new ViewCellInfo(mFirstTileContainer, 0, 0, 2, 1));
        mViewCells.add(new ViewCellInfo(mSecondTileContainer, 2, 0, 2, 1));
    }

    public List<ViewCellInfo> getEmptySpaces(int minSpanX, int minSpanY) {
        List<ViewCellInfo> emptySpaces = new ArrayList<>();
        boolean[][] grid = buildOccupancyGrid();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[0].length; x++) {
                if (!grid[y][x] && canFit(x, y, minSpanX, minSpanY, grid)) {
                    emptySpaces.add(new ViewCellInfo(null, x, y, minSpanX, minSpanY));
                }
            }
        }
        return emptySpaces;
    }

    private boolean[][] buildOccupancyGrid() {

        boolean[][] grid = new boolean[mRows][mColumns];

        for (ViewCellInfo cell : mViewCells) {
            for (int y = cell.mY; y < cell.mY + cell.spanY; y++) {
                for (int x = cell.mX; x < cell.mX + cell.spanX; x++) {
                    if (y < grid.length && x < grid[y].length) {
                        grid[y][x] = true;
                    }
                }
            }
        }
        return grid;
    }

    private boolean canFit(int startX, int startY, int spanX, int spanY, boolean[][] grid) {
        if (startX + spanX > grid[0].length || startY + spanY > grid.length) {
            return false;
        }

        for (int y = startY; y < startY + spanY; y++) {
            for (int x = startX; x < startX + spanX; x++) {
                if (grid[y][x]) return false;
            }
        }
        return true;
    }

    public boolean addWidgetSafe(View view, int x, int y, int spanX, int spanY) {
        if (spanX < 1 || spanY < 1) {
            throw new IllegalArgumentException("Minimum span is 1x1");
        }

        if (!canFit(x, y, spanX, spanY, buildOccupancyGrid())) {
            return false;
        }

        // Aggiungi alla griglia
        mViewCells.add(new ViewCellInfo(view, x, y, spanX, spanY));
        addView(view);
        updateLayout();
        return true;
    }

    private void updateLayout() {
        requestLayout();
        invalidate();
    }

    /**
     * Aggiunge un widget trovando automaticamente la prima posizione disponibile
     * @param view Widget da aggiungere
     * @param spanX Larghezza richiesta in celle
     * @param spanY Altezza richiesta in celle
     * @return true se l'aggiunta è riuscita
     */
    public int[] addWidget(String vId, View view, int spanX, int spanY) {
        boolean[][] grid = buildOccupancyGrid();

        for (int y = 0; y < grid.length; y++) {
            for (int x = 0; x < grid[y].length; x++) {
                if (isSpaceFree(grid, x, y, spanX, spanY)) {
                    view.setTag(vId);
                    mCustomWidgetsMap.put(view, new int[]{x, y, spanX, spanY});
                    placeWidget(view, x, y, spanX, spanY);
                    return new int[]{x, y, spanX, spanY};
                }
            }
        }
        return new int[]{-1, -1, -1, -1}; // No space
    }

    public void addWidget(View view, int x, int y, int spanX, int spanY) {
        for (int i = 0; i < mViewCells.size(); i++) {
            if (mViewCells.get(i).mView == view) {
                mViewCells.remove(i);
                break;
            }
        }
        for (Map.Entry<View, int[]> entry : mCustomWidgetsMap.entrySet()) {
            View v = entry.getKey();
            if (v.getTag() != null && v.getTag().equals(view.getTag())) {
                mCustomWidgetsMap.remove(entry.getKey());
                break;
            }
        }
        addView(view);
        mViewCells.add(new ViewCellInfo(view, x, y, spanX, spanY));
        mCustomWidgetsMap.put(view, new int[]{x, y, spanX, spanY});
    }

    public void removeWidget(String viewTag) {
        Log.d("LargeTileContainerView", "removeWidget: " + viewTag);
        for (int i = 0; i < mViewCells.size(); i++) {
            if (mViewCells.get(i).mView.getTag() != null && mViewCells.get(i).mView.getTag().equals(viewTag)) {
                removeView(mViewCells.get(i).mView);
                break;
            }
        }
        for (Map.Entry<View, int[]> entry : mCustomWidgetsMap.entrySet()) {
            if (entry.getKey().getTag() != null && entry.getKey().getTag().equals(viewTag)) {
                mCustomWidgetsMap.remove(entry.getKey());
                break;
            }
        }
        Log.d("LargeTileContainerView", "mCustomWidgetsMap.size: " + mCustomWidgetsMap.size());
        requestLayout();
    }

    private void placeWidget(View view, int x, int y, int spanX, int spanY) {
        mViewCells.add(new ViewCellInfo(view, x, y, spanX, spanY));
        addView(view);
        updateLayout();
    }

    private boolean isSpaceFree(boolean[][] grid, int startX, int startY, int spanX, int spanY) {
        if (startX + spanX > grid[0].length || startY + spanY > grid.length) {
            return false;
        }

        for (int y = startY; y < startY + spanY; y++) {
            for (int x = startX; x < startX + spanX; x++) {
                if (grid[y][x]) return false;
            }
        }
        return true;
    }

    @Override
    public void removeView(View view) {
        super.removeView(view);
        for (ViewCellInfo cell : mViewCells) {
            if (cell.mView == view) {
                mViewCells.remove(cell);
                break;
            }
        }
        requestLayout();
    }

    public void setMediaCell(boolean enabled, int x, int y, int spanX, int spanY) {
        ViewCellInfo mediaCell = mViewCells.stream()
                .filter(info -> info.mView == mMediaPanelContainer)
                .findFirst()
                .orElse(null);
        mViewCells.remove(mediaCell);
        mViewCells.add(new ViewCellInfo(mMediaPanelContainer, x, y, spanX, spanY));
        mMediaPanelContainer.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setBrightnessCell(boolean enabled, int x, int y, int spanX, int spanY) {
        ViewCellInfo brightnessCell = mViewCells.stream()
                .filter(info -> info.mView == mBrightnessTileContainer)
                .findFirst()
                .orElse(null);
        mViewCells.remove(brightnessCell);
        mViewCells.add(new ViewCellInfo(mBrightnessTileContainer, x, y, spanX, spanY));
        mBrightnessTileContainer.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setVolumeCell(boolean enabled, int x, int y, int spanX, int spanY) {
        ViewCellInfo volumeCell = mViewCells.stream()
                .filter(info -> info.mView == mVolumeTileContainer)
                .findFirst()
                .orElse(null);
        mViewCells.remove(volumeCell);
        mViewCells.add(new ViewCellInfo(mVolumeTileContainer, x, y, spanX, spanY));
        mVolumeTileContainer.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setFirstTileCell(boolean enabled, int x, int y, int spanX, int spanY) {
        ViewCellInfo firstTileCell = mViewCells.stream()
                .filter(info -> info.mView == mFirstTileContainer)
                .findFirst()
                .orElse(null);
        mViewCells.remove(firstTileCell);
        mViewCells.add(new ViewCellInfo(mFirstTileContainer, x, y, spanX, spanY));
        mFirstTileContainer.setVisibility(enabled ? VISIBLE : GONE);
    }

    public void setSecondTileCell(boolean enabled, int x, int y, int spanX, int spanY) {
        ViewCellInfo secondTileCell = mViewCells.stream()
                .filter(info -> info.mView == mSecondTileContainer)
                .findFirst()
                .orElse(null);
        mViewCells.remove(secondTileCell);
        mViewCells.add(new ViewCellInfo(mSecondTileContainer, x, y, spanX, spanY));
        mSecondTileContainer.setVisibility(enabled ? VISIBLE : GONE);
    }

    public Map<View, int[]> getCustomWidgetsMap() {
        Log.d("LargeTileContainerView", "getCustomWidgetsMap: " + mCustomWidgetsMap.size());
        return mCustomWidgetsMap;
    }

    public class ViewCellInfo {
        public int spanX;
        public int spanY;

        public View mView;

        public int mX;

        public int mY;

        public ViewCellInfo(View view, int x, int y, int spanx, int spany) {
            mView = view;
            mX = x;
            mY = y;
            spanX = spanx;
            spanY = spany;
        }
    }
}
