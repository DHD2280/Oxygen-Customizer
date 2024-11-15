package it.dhd.oxygencustomizer.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.dhd.oxygencustomizer.R;

public class CategoryWidget extends RelativeLayout {

    private LinearLayout container;
    private TextView titleTextView;

    public CategoryWidget(Context context) {
        super(context);
        init(context, null);
    }

    public CategoryWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public CategoryWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.oplus_preference_category, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.CategoryWidget);
        setTitle(typedArray.getString(R.styleable.CategoryWidget_titleText));
        typedArray.recycle();

        setClickable(false);
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        titleTextView = findViewById(android.R.id.title);
    }

}
