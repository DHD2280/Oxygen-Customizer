package it.dhd.oxygencustomizer.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.VisibleForTesting;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.preferences.FooterPreference;
import it.dhd.oxygencustomizer.ui.preferences.LinkTextView;

public class FooterWidget extends LinearLayout {

    public static final String KEY_FOOTER = "footer_preference";
    static final int ORDER_FOOTER = Integer.MAX_VALUE - 1;
    @VisibleForTesting
    View.OnClickListener mLearnMoreListener;
    @VisibleForTesting
    private LinearLayout container;
    private TextView titleTextView;
    private LinkTextView learnMoreView;
    private ImageView iconView;
    private CharSequence mContentDescription;
    private CharSequence mLearnMoreText;
    private FooterPreference.FooterLearnMoreSpan mLearnMoreSpan;

    public FooterWidget(Context context) {
        super(context);
        init(context, null);
    }

    public FooterWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public FooterWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {

        inflate(context, R.layout.preference_footer, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.FooterWidget);
        setTitle(typedArray.getString(R.styleable.FooterWidget_titleText));
        typedArray.recycle();

        setClickable(false);

    }

    public void setLearnMoreAction(View.OnClickListener listener) {
        if (mLearnMoreListener != listener) {
            mLearnMoreListener = listener;
            refreshLearnMore();
        }
    }

    public void refreshLearnMore() {
        if (mLearnMoreListener != null) {
            learnMoreView.setVisibility(View.VISIBLE);
            if (TextUtils.isEmpty(mLearnMoreText)) {
                mLearnMoreText = learnMoreView.getText();
            } else {
                learnMoreView.setText(mLearnMoreText);
            }
            SpannableString learnMoreText = new SpannableString(mLearnMoreText);
            if (mLearnMoreSpan != null) {
                learnMoreText.removeSpan(mLearnMoreSpan);
            }
            mLearnMoreSpan = new FooterPreference.FooterLearnMoreSpan(mLearnMoreListener);
            learnMoreText.setSpan(mLearnMoreSpan, 0,
                    learnMoreText.length(), 0);
            learnMoreView.setText(learnMoreText);
        } else {
            learnMoreView.setVisibility(View.GONE);
        }
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    private void initializeId() {

        titleTextView = findViewById(android.R.id.title);
        learnMoreView = findViewById(R.id.settingslib_learn_more);
        iconView = findViewById(android.R.id.icon);
        iconView.setImageResource(R.drawable.settingslib_ic_info_outline_24);

    }

}
