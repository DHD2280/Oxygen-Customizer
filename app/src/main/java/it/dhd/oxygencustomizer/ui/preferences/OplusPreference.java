package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListHelper;

public class OplusPreference extends Preference {

    private int mClickStyle = 0;
    private String mForcePosition = null;
    private boolean mTintTitle = false;
    private boolean mTitleCentered = false;
    private boolean mShowDivider;
    private View mItemView;


    public OplusPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
        initResources();
    }

    public OplusPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                                 int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusPreference);
        mClickStyle = a.getInt(R.styleable.OplusPreference_clickStyle, 0);
        if (a.hasValue(R.styleable.OplusPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusPreference_forcePosition);
        }
        mTintTitle = a.getBoolean(R.styleable.OplusPreference_tintTitle, false);
        mTitleCentered = a.getBoolean(R.styleable.OplusPreference_centerTitle, false);
        a.recycle();
    }

    private void initResources() {
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
        this.mItemView = holder.itemView;
        View findViewById = holder.findViewById(R.id.coui_preference);
        if (findViewById != null) {
            if (mClickStyle != 1) {
                if (mClickStyle == 2) {
                    findViewById.setClickable(true);
                }
            } else {
                findViewById.setClickable(false);
            }
        }
        TextView title = (TextView) holder.findViewById(android.R.id.title);
        if (mTintTitle) {
            title.setTextColor(ContextCompat.getColor(getContext(), android.R.color.system_accent1_400));
        }
        if (mTitleCentered) {
            title.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        }
    }


}
