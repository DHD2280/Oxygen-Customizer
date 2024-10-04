package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.DrawableRes;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.ui.preferences.dialogadapter.ListPreferenceAdapter;

public class ListWithPopUpPreference extends OplusListPreference {

    private int[] mEntryIcons;
    private Drawable[] mEntryDrawables;
    private boolean mHasImages = false;
    private ListPreferenceAdapter mAdapter;
    private BottomSheetDialog bottomSheetDialog;
    private RecyclerView recyclerView;

    public ListWithPopUpPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public ListWithPopUpPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ListWithPopUpPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ListWithPopUpPreference(Context context) {
        super(context);
    }

    public void setDrawables(@DrawableRes int[] drawables) {
        mHasImages = true;
        mEntryIcons = drawables;
    }

    public void setDrawables(Drawable[] drawables) {
        mHasImages = true;
        mEntryDrawables = drawables;
    }

    public void setHasImages(boolean hasImages) {
        mHasImages = hasImages;
    }

    public void setAdapter(ListPreferenceAdapter adapter) {
        mAdapter = adapter;
    }

    public void setAdapterType(int type) {
        if (mAdapter != null)
            mAdapter.setType(type);
    }

    public void setImages(List<String> images) {
        if (mAdapter != null) {
            mAdapter.setImages(images);
        }
    }

    public void setDefaultAdapterListener() {
        mAdapter.setListener((view, position) -> {
            if (callChangeListener(getEntryValues()[position].toString())) {
                setValueIndex(position);
            }
            if (bottomSheetDialog != null) bottomSheetDialog.dismiss();
        });
    }

    @Override
    protected void onClick() {
        bottomSheetDialog = new BottomSheetDialog(getContext());

        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_bottom_sheet_dialog_layout, (ViewGroup) null);
        recyclerView = view.findViewById(R.id.select_dialog_listview);
        MaterialToolbar toolbarPref = view.findViewById(R.id.toolbar_preference);
        toolbarPref.setTitle(getTitle());
        toolbarPref.setTitleCentered(true);
        if (mAdapter != null && mAdapter.getType() == ListPreferenceAdapter.TYPE_BATTERY_ICONS) {
            recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        } else {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        if (mAdapter == null) {
            mAdapter = new ListPreferenceAdapter(getEntries(),
                    getEntryValues(),
                    mEntryIcons,
                    getKey(),
                    mHasImages,
                    (view1, position) -> {
                        if (callChangeListener(getEntryValues()[position].toString())) {
                            setValueIndex(position);
                        }
                    });
        }
        recyclerView.setAdapter(mAdapter);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

    }

    private int getValueIndex() {
        return findIndexOfValue(getValue());
    }

    public void setValueIndex(int index) {
        setValue(getEntryValues()[index].toString());
    }

    public void createDefaultAdapter() {
        mAdapter = new ListPreferenceAdapter(getEntries(),
                getEntryValues(),
                mEntryIcons,
                getKey(),
                mHasImages,
                (view1, position) -> {
                    if (callChangeListener(getEntryValues()[position].toString())) {
                        setValueIndex(position);
                    }
                });
    }

    public void createDefaultAdapter(Drawable[] drawables) {
        mHasImages = true;
        mEntryDrawables = drawables;
        mAdapter = new ListPreferenceAdapter(getEntries(),
                getEntryValues(),
                drawables,
                getKey(),
                mHasImages,
                (view1, position) -> {
                    if (callChangeListener(getEntryValues()[position].toString())) {
                        setValueIndex(position);
                    }
                });
    }

    public void createDefaultAdapter(Drawable[] drawables, onItemClick listener) {
        mHasImages = true;
        mEntryDrawables = drawables;
        mAdapter = new ListPreferenceAdapter(getEntries(),
                getEntryValues(),
                drawables,
                getKey(),
                mHasImages,
                (view1, position) -> {
                    if (callChangeListener(getEntryValues()[position].toString())) {
                        setValueIndex(position);
                    }
                    if (listener != null) {
                        listener.onItemClick(position);
                    }
                });
    }

    public interface onItemClick {
        void onItemClick(int position);
    }
}
