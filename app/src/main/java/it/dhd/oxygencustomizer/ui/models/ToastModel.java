package it.dhd.oxygencustomizer.ui.models;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

public class ToastModel {

    private @DrawableRes int mStyle;
    private @StringRes int mTitle;
    private boolean mSelected = false;

    public ToastModel(@DrawableRes int style, @StringRes int title) {
        mStyle = style;
        mTitle = title;
    }

    public ToastModel(@DrawableRes int style, @StringRes int title, boolean selected) {
        mStyle = style;
        mTitle = title;
        mSelected = selected;
    }


    public @DrawableRes int getStyle() {
        return mStyle;
    }

    public @StringRes int getTitle() {
        return mTitle;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

}
