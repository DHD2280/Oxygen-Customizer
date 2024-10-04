package it.dhd.oxygencustomizer.ui.models;

import androidx.annotation.DrawableRes;

public class ToastModel {

    private int mStyle;
    private String mTitle;
    private boolean mSelected = false;

    public ToastModel( int style, String title) {
        mStyle = style;
        mTitle = title;
    }

    public ToastModel(int style, String title, boolean selected) {
        mStyle = style;
        mTitle = title;
        mSelected = selected;
    }


    public @DrawableRes int getStyle() {
        return mStyle;
    }

    public String getTitle() {
        return mTitle;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

}
