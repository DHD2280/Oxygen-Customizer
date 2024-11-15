package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;
import it.dhd.oneplusui.preference.OplusPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.SnapOnScrollListener;
import it.dhd.oxygencustomizer.utils.CarouselLayoutManager;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class OplusRecyclerPreference extends OplusPreference {

    private View mItemView;
    private String mForcePosition = null;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private String mKey = null;
    private int mDefaultValue = 0;

    public OplusRecyclerPreference(@NonNull Context context) {
        super(context, null);
        init();
    }

    public OplusRecyclerPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, 0);
        init();
    }

    public OplusRecyclerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr, 0);
        init();
    }

    public OplusRecyclerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        setLayoutResource(R.layout.custom_preference_recyclerview);
    }

    public void setPreference(String key, int defaultValue) {
        mKey = key;
        mDefaultValue = defaultValue;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mItemView = holder.itemView;
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
        mRecyclerView = (RecyclerView) holder.findViewById(R.id.pref_recycler_view);

        // Create a new LayoutManager instance for each RecyclerView
        CarouselLayoutManager layoutManager = new CarouselLayoutManager(getContext(), RecyclerView.HORIZONTAL, false);
//        layoutManager.setMinifyDistance(0.8f);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setAdapter(mAdapter);
        //mRecyclerView.setHasFixedSize(true);
        mRecyclerView.scrollToPosition(PreferenceHelper.instance.mPreferences.getInt(mKey, mDefaultValue));
        SnapHelper snapHelper = new PagerSnapHelper();
        if (mRecyclerView.getOnFlingListener() == null) {
            snapHelper.attachToRecyclerView(mRecyclerView);
            SnapOnScrollListener snapOnScrollListener = new SnapOnScrollListener(snapHelper, SnapOnScrollListener.Behavior.NOTIFY_ON_SCROLL, position -> mRecyclerView.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK));
            mRecyclerView.addOnScrollListener(snapOnScrollListener);
        }
    }

    public void setAdapter(RecyclerView.Adapter<?> adapter) {
        mAdapter = adapter;
    }

}
