package it.dhd.oxygencustomizer.ui.models;

import android.graphics.Color;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class DarkShadowItem {

    private String mOverlayName;
    private String mTitle;
    private String mSummary;
    private List<String> mResourceName;
    private List<String> mPackages = new ArrayList<>();
    private int mColor;

    public DarkShadowItem(String overlayName, String title, String summary, List<String> resourceNames, List<String> packages) {
        this.mOverlayName = overlayName;
        this.mTitle = title;
        this.mSummary = summary;
        this.mPackages.addAll(packages);
        this.mResourceName = resourceNames;
        this.mColor = Color.BLACK;
    }

    public DarkShadowItem(String overlayName, String title, String summary, List<String> resourceNames, List<String> packages, int color) {
        this.mOverlayName = overlayName;
        this.mTitle = title;
        this.mSummary = summary;
        this.mPackages.addAll(packages);
        this.mResourceName = resourceNames;
        this.mColor = color;
    }

    public String getOverlayName() {
        return mOverlayName;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSummary() {
        return mSummary;
    }

    public List<String> getPackages() {
        return mPackages;
    }

    public int getNumPackages() {
        return mPackages.size();
    }

    public List<String> getResourceNames() {
        return mResourceName;
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int color) {
        mColor = color;
    }

    @Override
    @NonNull
    public String toString() {
        return "DarkShadowItem{" +
                "mOverlayName='" + mOverlayName + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mSummary='" + mSummary + '\'' +
                ", mResourceName='" + mResourceName + '\'' +
                ", mPackages=" + mPackages +
                ", mColor=" + mColor +
                '}';
    }

}
