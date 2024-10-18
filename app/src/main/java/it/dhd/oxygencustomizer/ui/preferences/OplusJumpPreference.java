package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;

public class OplusJumpPreference extends OplusPreference {

    private TextView mJumpText;
    private String mPendingJumpText;
    private int mJumpVisibility = View.VISIBLE;
    private View mJumpView;

    public OplusJumpPreference(@NonNull Context context) {
        this(context, null);
        initResources();
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        initResources();
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        initResources();
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initResources();
    }

    private void initResources() {
        setWidgetLayoutResource(R.layout.custom_preference_widget_jump);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mJumpText = (TextView) holder.findViewById(R.id.jump_text);
        mJumpView = holder.findViewById(android.R.id.widget_frame);

        if (!TextUtils.isEmpty(mPendingJumpText)) {
            mJumpText.setText(mPendingJumpText);
        }
        mJumpView.setVisibility(mJumpVisibility);
    }

    public void setJumpText(String text) {
        mPendingJumpText = text;
        if (mJumpText != null) {
            mJumpText.setText(text);
        }
    }

    public void setJumpEnabled(boolean enabled) {
        mJumpVisibility = enabled ? View.VISIBLE : View.GONE;
        if (mJumpView != null) {
            mJumpView.setVisibility(enabled ? View.VISIBLE : View.GONE);
        }
    }
}
