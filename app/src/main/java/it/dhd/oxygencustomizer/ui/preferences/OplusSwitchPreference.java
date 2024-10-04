package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListHelper;


public class OplusSwitchPreference extends SwitchPreferenceCompat {

    private String mForcePosition = null;

    public OplusSwitchPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.switchPreferenceCompatStyle);
    }

    public OplusSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        initResources();

    }

    public OplusSwitchPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusSwitchPreference);
        if (a.hasValue(R.styleable.OplusSwitchPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusSwitchPreference_forcePosition);
        }
        a.recycle();
    }

    private void initResources() {
        setLayoutResource(R.layout.custom_preference);
        setWidgetLayoutResource(R.layout.custom_preference_widget_switch);
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