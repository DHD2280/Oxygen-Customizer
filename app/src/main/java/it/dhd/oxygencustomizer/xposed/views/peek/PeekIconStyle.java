package it.dhd.oxygencustomizer.xposed.views.peek;

import androidx.annotation.NonNull;

public class PeekIconStyle {

    private String mName;
    private int mStyle;
    private int mIconSize;
    private int mIconBackgroundColor;
    private int mIconSpacing;
    private int mIconPadding;

    /**
     * Peek Icon Style Constructor
     * @param name The name of the style
     * @param style The int of a style corresponding to {@link resources/values/arrays.xml}
     * @param iconSize The size of the icon
     * @param iconBackgroundColor The color of the icon background
     * @param iconSpacing The spacing between icons, the margin end of the icon in recycler view
     * @param iconPadding The padding of the icon
     */
    public PeekIconStyle(String name, int style,
                         int iconSize, int iconBackgroundColor,
                         int iconSpacing, int iconPadding) {
        this.mName = name;
        this.mStyle = style;
        this.mIconSize = iconSize;
        this.mIconBackgroundColor = iconBackgroundColor;
        this.mIconSpacing = iconSpacing;
        this.mIconPadding = iconPadding;
    }

    public String getName() {
        return mName;
    }

    public int getStyle() {
        return mStyle;
    }

    public int getIconSize() {
        return mIconSize;
    }

    public int getIconBackgroundColor() {
        return mIconBackgroundColor;
    }

    public int getIconSpacing() {
        return mIconSpacing;
    }

    public int getIconPadding() {
        return mIconPadding;
    }

    public void setIconSize(int iconSize) {
        this.mIconSize = iconSize;
    }

    @NonNull
    @Override
    public String toString() {
        return "PeekIconStyle{" +
                "mName='" + mName + '\'' +
                ", mStyle=" + mStyle +
                ", mIconSize=" + mIconSize +
                ", mIconBackgroundColor=" + mIconBackgroundColor +
                ", mIconSpacing=" + mIconSpacing +
                ", mIconPadding=" + mIconPadding +
                '}';
    }

}
