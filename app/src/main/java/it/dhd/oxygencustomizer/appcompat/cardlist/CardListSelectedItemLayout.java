package it.dhd.oxygencustomizer.appcompat.cardlist;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;

import it.dhd.oxygencustomizer.R;

public class CardListSelectedItemLayout extends LinearLayout {

    private static final int DEFAULT_PADDING = 0;
    private final int ANIMATOR_TYPE_APPEAR;
    private final int ANIMATOR_TYPE_DISAPPER;
    private final int HEAD_OR_TAIL_PADDING;
    private boolean mApplyOutline;
    private boolean mBottomRounded;
    private ConfigurationChangedListener mConfigurationChangeListener;
    private int mInitPaddingBottom;
    private int mInitPaddingTop;
    private int mMinimumHeight;
    private boolean mTopRounded;

    public interface ConfigurationChangedListener {
        void configurationChanged(Configuration configuration);
    }

    public CardListSelectedItemLayout(Context context) {
        this(context, null);
    }

    private void init(Context context) {
        this.mMinimumHeight = getMinimumHeight();
        this.mInitPaddingTop = getPaddingTop();
        this.mInitPaddingBottom = getPaddingBottom();
    }


    private void setCardRadiusStyle(int i) {
        if (i == 4) {
            this.mTopRounded = true;
            this.mBottomRounded = true;
        } else if (i == 1) {
            this.mTopRounded = true;
            this.mBottomRounded = false;
        } else if (i == 3) {
            this.mTopRounded = false;
            this.mBottomRounded = true;
        } else {
            this.mTopRounded = false;
            this.mBottomRounded = false;
        }
    }

    private void setPadding(int i) {
        int i2;
        int i3 = 0;
        if (i == 1) {
            i3 = this.HEAD_OR_TAIL_PADDING;
            i2 = 0;
        } else if (i == 3) {
            i2 = this.HEAD_OR_TAIL_PADDING;
        } else {
            if (i == 4) {
                i3 = this.HEAD_OR_TAIL_PADDING;
            }
            i2 = i3;
        }
        setMinimumHeight(this.mMinimumHeight + i3 + i2);
        setPaddingRelative(getPaddingStart(), this.mInitPaddingTop + i3, getPaddingEnd(), this.mInitPaddingBottom + i2);
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        ConfigurationChangedListener configurationChangedListener = this.mConfigurationChangeListener;
        if (configurationChangedListener != null) {
            configurationChangedListener.configurationChanged(configuration);
        }
    }

    public void setConfigurationChangeListener(ConfigurationChangedListener configurationChangedListener) {
        this.mConfigurationChangeListener = configurationChangedListener;
    }

    public void setPositionInGroup(int i) {
        if (i > 0) {
            setPadding(i);
            setCardRadiusStyle(i);
            setCardBackground();
        }
    }

    private void setCardBackground() {
        if (mTopRounded && mBottomRounded) {
            setBackgroundResource(R.drawable.preference_background_center);
        } else if (mTopRounded) {
            setBackgroundResource(R.drawable.preference_background_top);
        } else if (mBottomRounded) {
            setBackgroundResource(R.drawable.preference_background_bottom);
        } else {
            setBackgroundResource(R.drawable.preference_background_middle);
        }
    }

    public CardListSelectedItemLayout(Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public CardListSelectedItemLayout(Context context, @Nullable AttributeSet attributeSet, int i) {
        this(context, attributeSet, i, 0);
    }

    public CardListSelectedItemLayout(Context context, AttributeSet attributeSet, int i, int i2) {
        super(context, attributeSet, i, i2);
        this.ANIMATOR_TYPE_APPEAR = 0;
        this.ANIMATOR_TYPE_DISAPPER = 1;
        this.mTopRounded = true;
        this.mBottomRounded = true;
        this.mApplyOutline = false;
        this.HEAD_OR_TAIL_PADDING = 2;
        this.setForceDarkAllowed(false);
        init(getContext());
    }

}
