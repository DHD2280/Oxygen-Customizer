package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.widget.TextView;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListHelper;

public class OplusListPreference extends ListPreference {

    private final Context mContext;
    private String mForcePosition = null;

    public OplusListPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusListPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusListPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public OplusListPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                   int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusListPreference);
        if (a.hasValue(R.styleable.OplusListPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusListPreference_forcePosition);
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

        TextView summary = (TextView) holder.findViewById(android.R.id.summary);
        summary.setTextColor(ContextCompat.getColor(mContext, android.R.color.system_accent1_400));

    }

}
