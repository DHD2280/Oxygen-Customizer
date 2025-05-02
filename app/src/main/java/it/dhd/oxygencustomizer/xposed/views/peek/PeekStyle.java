package it.dhd.oxygencustomizer.xposed.views.peek;

public class PeekStyle {

    private String mName;
    private int mStyle;
    private int mBackgroundColor;
    private int mImageViewSize;
    private float mBackgroundRadius;
    private int mBackgroundPadding;
    private int mTitleColor;
    private int mSummaryColor;
    private int mCardBackgroundColor;
    private float[] mCardBackgroundRadius;
    private int mButtonsColor;
    private boolean mUseAppIcons;

    /** Peek Style Constructor
     *
     * @param name The name of the style
     * @param style The int of a style corresponding to {@link resources/values/arrays.xml}
     * @param backgroundColor The color of the background
     * @param imageViewSize The size of the background
     * @param backgroundRadius The radius of the background
     * @param backgroundPadding The padding of the background
     * @param titleColor The color of the title
     * @param summaryColor The color of the summary
     * @param cardBackgroundColor The color of the card background
     * @param cardBackgroundRadius The radius of the card background
     * @param buttonsColor The color of the buttons
     * @param useAppIcons Whether to use app icons
     */
    public PeekStyle(
            String name, int style,
            int backgroundColor, int imageViewSize, float backgroundRadius, int backgroundPadding,
            int titleColor, int summaryColor, int cardBackgroundColor, float[] cardBackgroundRadius,
            int buttonsColor,
            boolean useAppIcons) {
        mName = name;
        mStyle = style;
        mBackgroundColor = backgroundColor;
        mImageViewSize = imageViewSize;
        mBackgroundRadius = backgroundRadius;
        mBackgroundPadding = backgroundPadding;
        mTitleColor = titleColor;
        mSummaryColor = summaryColor;
        mCardBackgroundColor = cardBackgroundColor;
        mCardBackgroundRadius = cardBackgroundRadius;
        mButtonsColor = buttonsColor;
        mUseAppIcons = useAppIcons;
    }

    public String getName() {
        return mName;
    }

    public int getStyle() {
        return mStyle;
    }

    public int getBackgroundColor() {
        return mBackgroundColor;
    }

    public int getImageViewSize() {
        return mImageViewSize;
    }

    public float getBackgroundRadius() {
        return mBackgroundRadius;
    }

    public int getBackgroundPadding() {
        return mBackgroundPadding;
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


}
