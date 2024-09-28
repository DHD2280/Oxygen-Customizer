package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListHelper;

public class OplusEditTextPreference extends EditTextPreference {

    private String mForcePosition = null;

    public OplusEditTextPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);
    }

    public OplusEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyle) {
        this(context, attrs, defStyle, android.R.attr.editTextPreferenceStyle);
    }

    public OplusEditTextPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                               int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusEditTextPreference);
        if (a.hasValue(R.styleable.OplusEditTextPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusEditTextPreference_forcePosition);
        }
        a.recycle();
        setLayoutResource(R.layout.custom_preference);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (mForcePosition != null) {
            int pos = switch (mForcePosition) {
                case "top" -> CardListHelper.HEAD;
                case "middle" -> CardListHelper.MIDDLE;
                case "bottom" -> CardListHelper.TAIL;
                case "full" -> CardListHelper.FULL;
                default -> CardListHelper.NONE;
            };
            CardListHelper.setItemCardBackground(holder.itemView, pos);
        } else {
            CardListHelper.setItemCardBackground(holder.itemView, CardListHelper.getPositionInGroup(this));
        }
    }

}
