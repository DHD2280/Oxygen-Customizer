package it.dhd.oxygencustomizer.ui.views;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;

public class TileContainerView extends ViewGroup {
    public int mCellHeight;
    public int mCellMarginHorizontal;
    public int mCellMarginVertical;
    public int mCellWidth;
    public int mColumns;
    public int mLastCellWidth;
    public final ArrayList mRecords;
    public int mRows;

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    public void onLayout(boolean z, int i2, int i3, int i4, int i5) {
    }

    public void setLayerType(boolean z) {
    }

    public TileContainerView(Context context) {
        this(context, null);
    }

    public TileContainerView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.mColumns = 1;
        this.mLastCellWidth = -1;
        this.mRows = 1;
        this.mRecords = new ArrayList();
        updateCell();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mLastCellWidth = -1;
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public void updateCell() {
        int columnResId = getResources().getIdentifier("qs_cell_colum", "integer", SYSTEM_UI);
        int rowResId = getResources().getIdentifier("qs_fix_cell_row", "integer", SYSTEM_UI);
        try {
            Context systemUIContext = getContext().createPackageContext("com.android.systemui", Context.CONTEXT_IGNORE_SECURITY);
            int column = systemUIContext.getResources().getInteger(
                    columnResId
            );
            int row = systemUIContext.getResources().getInteger(
                    rowResId
            );
            this.mColumns = column;
            this.mRows = row;
        } catch (Throwable ignored) {
            this.mColumns = 4;
            this.mRows = 3;
        }
    }

    public void setColumns(int columns) {
        this.mColumns = columns;
    }

    public void setRows(int rows) {
        this.mRows = rows;
    }

    public void updateResources(int cellSize, int marginHorizontal, int marginVertical) {
        this.mCellWidth = cellSize;
        this.mCellHeight = cellSize;
        this.mCellMarginHorizontal = marginHorizontal;
        this.mCellMarginVertical = marginVertical;
        int i2 = this.mLastCellWidth;
        if (i2 == this.mCellHeight) {
            return;
        }
        if (i2 == -1) {
            this.mLastCellWidth = this.mCellWidth;
            return;
        }
        this.mLastCellWidth = this.mCellHeight;
        requestLayout();
    }

    public int getSubSize() {
        if (this.mRecords.size() % 4 > 0) {
            return (this.mRecords.size() / 4) + 1;
        }
        return this.mRecords.size() / 4;
    }

    public void dump(final PrintWriter printWriter, String[] strArr) {
        printWriter.println(getClass().getSimpleName() + " :");
        StringBuilder sb = new StringBuilder();
        sb.append("mColumns=");
        sb.append(this.mColumns);
        sb.append(", mCellWidth=");
        sb.append(this.mCellWidth);
        sb.append(", mLastCellWidth=");
        sb.append(this.mLastCellWidth);
        sb.append(", mCellHeight=");
        sb.append(this.mCellHeight);
        sb.append(", mCellMarginHorizontal=");
        sb.append(this.mCellMarginHorizontal);
        sb.append(", mCellMarginVertical=");
        sb.append(this.mCellMarginVertical);
        sb.append(", mRows=");
        sb.append(this.mRows);
        printWriter.println(sb);
    }

    public static int exactly(int i2) {
        return View.MeasureSpec.makeMeasureSpec(i2, MeasureSpec.EXACTLY);
    }
}
