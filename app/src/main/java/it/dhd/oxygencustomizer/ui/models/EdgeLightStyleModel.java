package it.dhd.oxygencustomizer.ui.models;

import androidx.annotation.DrawableRes;

public class EdgeLightStyleModel {

    private final String mTitle;
    private final int mPreview;
    private final String mPrefValue;

    public EdgeLightStyleModel(String title, @DrawableRes int preview, String prefValue) {
        this.mTitle = title;
        this.mPreview = preview;
        this.mPrefValue = prefValue;
    }

    public String getTitle() {
        return mTitle;
    }

    public int getPreview() {
        return mPreview;
    }

    public String getPrefValue() {
        return mPrefValue;
    }

}
