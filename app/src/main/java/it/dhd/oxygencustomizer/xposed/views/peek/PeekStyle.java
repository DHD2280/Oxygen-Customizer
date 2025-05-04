package it.dhd.oxygencustomizer.xposed.views.peek;

import androidx.annotation.NonNull;

import java.util.Arrays;

public class PeekStyle {

    private String mName;
    private int mStyle;
    private int mTitleColor;
    private int mSummaryColor;
    private int mCardBackgroundColor;
    private float[] mCardBackgroundRadius;
    private int mButtonsColor;
    private boolean mUseAppIcons;
    private PeekIconStyle mPeekIconStyle;

    /** Peek Style Constructor
     *
     * @param name The name of the style
     * @param style The int of a style corresponding to {@link resources/values/arrays.xml}
     * @param titleColor The color of the title
     * @param summaryColor The color of the summary
     * @param cardBackgroundColor The color of the card background
     * @param cardBackgroundRadius The radius of the card background
     * @param buttonsColor The color of the buttons
     * @param useAppIcons Whether to use app icons
     * @param peekIconStyle The {@link PeekIconStyle}
     */
    public PeekStyle(
            String name, int style,
            int titleColor, int summaryColor, int cardBackgroundColor, float[] cardBackgroundRadius,
            int buttonsColor,
            boolean useAppIcons,
            PeekIconStyle peekIconStyle) {
        mName = name;
        mStyle = style;
        mTitleColor = titleColor;
        mSummaryColor = summaryColor;
        mCardBackgroundColor = cardBackgroundColor;
        mCardBackgroundRadius = cardBackgroundRadius;
        mButtonsColor = buttonsColor;
        mUseAppIcons = useAppIcons;
        mPeekIconStyle = peekIconStyle;
    }

    public String getName() {
        return mName;
    }

    public int getStyle() {
        return mStyle;
    }

    public int getTitleColor() {
        return mTitleColor;
    }

    public int getSummaryColor() {
        return mSummaryColor;
    }

    public int getCardBackgroundColor() {
        return mCardBackgroundColor;
    }

    public float[] getCardBackgroundRadius() {
        return mCardBackgroundRadius;
    }

    public int getButtonsColor() {
        return mButtonsColor;
    }

    public boolean useAppIcons() {
        return mUseAppIcons;
    }

    public void setUseAppIcons(boolean useAppIcons) {
        mUseAppIcons = useAppIcons;
    }

    public PeekIconStyle getPeekIconStyle() {
        return mPeekIconStyle;
    }

    public void setPeekIconStyle(PeekIconStyle peekIconStyle) {
        mPeekIconStyle = peekIconStyle;
    }

    @NonNull
    @Override
    public String toString() {
        return "PeekStyle{" +
                "mName='" + mName + '\'' +
                ", mStyle=" + mStyle +
                ", mTitleColor=" + mTitleColor +
                ", mSummaryColor=" + mSummaryColor +
                ", mCardBackgroundColor=" + mCardBackgroundColor +
                ", mCardBackgroundRadius=" + Arrays.toString(mCardBackgroundRadius) +
                ", mButtonsColor=" + mButtonsColor +
                ", mUseAppIcons=" + mUseAppIcons +
                ", mPeekIconStyle=" + mPeekIconStyle.toString() +
                '}';
    }

}
