package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListHelper;
import it.dhd.oxygencustomizer.appcompat.cardlist.CardListSelectedItemLayout;
import it.dhd.oxygencustomizer.ui.recyclerview.OplusRecyclerView;

public class OplusPreference extends Preference {

    private int mClickStyle = 0;
    private String mForcePosition = null;
    private boolean mShowDivider;
    private View mItemView;

    @SuppressWarnings("unused")
    public OplusPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OplusPreference);
        mClickStyle = a.getInt(R.styleable.OplusPreference_clickStyle, 0);
        if (a.hasValue(R.styleable.OplusPreference_forcePosition)) {
            mForcePosition = a.getString(R.styleable.OplusPreference_forcePosition);
        }
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
    }


}
