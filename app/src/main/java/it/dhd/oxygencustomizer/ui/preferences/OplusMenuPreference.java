package it.dhd.oxygencustomizer.ui.preferences;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceViewHolder;

public class OplusMenuPreference extends OplusListPreference {

    private final Context mContext;
    private final ArrayAdapter mAdapter;

    private PopupMenu mPopupMenu;

    private final AdapterView.OnItemClickListener mItemSelectedListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0) {
                String value = getEntryValues()[position].toString();
                if (!value.equals(getValue()) && callChangeListener(value)) {
                    setValue(value);
                }
            }
            mPopupMenu.dismiss();
        }
    };

    public OplusMenuPreference(@NonNull Context context) {
        this(context, null);
    }

    public OplusMenuPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public OplusMenuPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                              int defStyle) {
        this(context, attrs, defStyle, 0);
    }

    public OplusMenuPreference(@NonNull Context context, @Nullable AttributeSet attrs,
                              int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mAdapter = createAdapter();
        updateEntries();
    }

    @Override
    protected void onClick() {
        mPopupMenu.show();
    }

    @Override
    public void setEntries(@NonNull CharSequence[] entries) {
        super.setEntries(entries);
        updateEntries();
    }

    @NonNull
    private ArrayAdapter<?> createAdapter() {
        return new ArrayAdapter<>(mContext, android.R.layout.simple_spinner_dropdown_item);
    }

    @SuppressWarnings("unchecked")
    private void updateEntries() {
        mAdapter.clear();
        if (getEntries() != null) {
            for (CharSequence c : getEntries()) {
                mAdapter.add(c.toString());
            }
        }
    }

    @Override
    public void setValueIndex(int index) {
        setValue(getEntryValues()[index].toString());
    }

    @Override
    protected void notifyChanged() {
        super.notifyChanged();
        // When setting a SummaryProvider for this Preference, this method may be called before
        // mAdapter has been set in ListPreference's constructor.
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {

        mPopupMenu = new PopupMenu(mContext, holder.itemView, Gravity.END);
        for (int i = 0; i < mAdapter.getCount(); i++) {
            mPopupMenu.getMenu().add(0, i, 0, mAdapter.getItem(i).toString());
        }
        mPopupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() >= 0) {
                String value = getEntryValues()[item.getItemId()].toString();
                if (!value.equals(getValue()) && callChangeListener(value)) {
                    setValue(value);
                }
            }
            return true;
        });

        TextView summary = holder.itemView.findViewById(android.R.id.summary);
        summary.setTextColor(ContextCompat.getColor(mContext, android.R.color.system_accent1_400));

        super.onBindViewHolder(holder);
    }

    private int findSpinnerIndexOfValue(String value) {
        CharSequence[] entryValues = getEntryValues();
        if (value != null && entryValues != null) {
            for (int i = entryValues.length - 1; i >= 0; i--) {
                if (TextUtils.equals(entryValues[i].toString(), value)) {
                    return i;
                }
            }
        }
        return Spinner.INVALID_POSITION;
    }
}