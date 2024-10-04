package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;

public class OplusJumpPreference extends OplusPreference {

    private TextView mJumpText;
    private String mPendingJumpText;

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

        if (!TextUtils.isEmpty(mPendingJumpText)) {
            mJumpText.setText(mPendingJumpText);
        }
    }

    public void setJumpText(String text) {
        mPendingJumpText = text;
        if (mJumpText != null) {
            mJumpText.setText(text);
        }
    }
}
