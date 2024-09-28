package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;

public class OplusPreferenceCategory extends PreferenceCategory {

    public OplusPreferenceCategory(@NonNull Context context) {
        this(context, null);
        initResource();
    }

    public OplusPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
        initResource();
    }

    public OplusPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyle) {
        this(context, attrs, defStyle, 0);
        initResource();
    }

    public OplusPreferenceCategory(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initResource();
    }

    private void initResource() {
        setLayoutResource(R.layout.custom_preference_category);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        holder.setDividerAllowedAbove(false);
        holder.setDividerAllowedBelow(false);
    }

}
