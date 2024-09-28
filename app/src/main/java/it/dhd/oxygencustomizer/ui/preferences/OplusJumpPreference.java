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

    @SuppressWarnings("unused")
    public OplusJumpPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusJumpPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
