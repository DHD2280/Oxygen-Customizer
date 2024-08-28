package it.dhd.oxygencustomizer.customprefs;

import android.content.Context;
import android.util.AttributeSet;
import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.adapters.SnapOnScrollListener;
import it.dhd.oxygencustomizer.utils.CarouselLayoutManager;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;

public class RecyclerPreference extends Preference {

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter<?> mAdapter;
    private String mKey = null;
    private int mDefaultValue = 0;

    public RecyclerPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public RecyclerPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        init();
    }

    public RecyclerPreference(Context context) {
        this(context, null, 0);
        init();
    }

    private void init() {
        setSelectable(false);
        setLayoutResource(R.layout.custom_preference_recyclerview);
    }

    public void setPreference(String key, int defaultValue) {
        mKey = key;
        mDefaultValue = defaultValue;
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        mRecyclerView = (RecyclerView) holder.findViewById(R.id.recycler_view);
        // Create a new LayoutManager instance for each RecyclerView
        mRecyclerView.setLayoutManager(new CarouselLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
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
