package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreferenceCompat;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListHelper;


public class OplusSwitchPreference extends SwitchPreferenceCompat {

    private String mForcePosition = null;

    @SuppressWarnings("unused")
    public OplusSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
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
        // Handle top margin for the first item
        ViewGroup.MarginLayoutParams layoutParams = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
        if (holder.getBindingAdapterPosition() == 0) {
            layoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        } else {
            layoutParams.topMargin = 0; // Reset margin for non-first items
        }

        // Handle bottom margin for the last item
        if (holder.getBindingAdapterPosition() == holder.getBindingAdapter().getItemCount() - 1) {
            layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, getContext().getResources().getDisplayMetrics());
        } else {
            layoutParams.bottomMargin = 0; // Reset margin for non-last items
        }

        // Apply the adjusted layout params back to the item view
        holder.itemView.setLayoutParams(layoutParams);
    }
}