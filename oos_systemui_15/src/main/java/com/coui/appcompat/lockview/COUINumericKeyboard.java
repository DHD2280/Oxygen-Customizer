package com.coui.appcompat.lockview;

import android.content.Context;
import android.view.View;

public class COUINumericKeyboard extends View {

    public COUINumericKeyboard(Context context) {
        super(context);
        throw new UnsupportedOperationException("Stub!");
    }

    public interface OnClickItemListener {
        void onClickLeft();

        void onClickNumber(int i2);

        void onClickRight();
    }


    public class Cell {

        public String cellNumberStr;
        public int column;
        public int row;

        public Cell(int i2, int i3) {
            throw new UnsupportedOperationException("Stub!");
        }

        public void setCircleColor(int i2) {
            throw new UnsupportedOperationException("Stub!");
        }

        public int getRow() {
            throw new UnsupportedOperationException("Stub!");
        }

        public int getColumn() {
            throw new UnsupportedOperationException("Stub!");
        }

        public void setCellNumberAlpha(float f2) {
            throw new UnsupportedOperationException("Stub!");
        }

        public void setCellNumberTranslateX(int i2) {
            throw new UnsupportedOperationException("Stub!");
        }

        public void setCellNumberTranslateY(int i2) {
            throw new UnsupportedOperationException("Stub!");
        }

        public String toString() {
            return "row " + this.row + "column " + this.column;
        }

        public boolean equals(Cell cell) {
            throw new UnsupportedOperationException("Stub!");
        }

        public boolean equals(Object obj) {
            throw new UnsupportedOperationException("Stub!");
        }

        public int hashCode() {
            throw new UnsupportedOperationException("Stub!");
        }
    }


}
