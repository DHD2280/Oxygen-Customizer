package it.dhd.oxygencustomizer.ui.preferences;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.materialswitch.MaterialSwitch;

import it.dhd.oxygencustomizer.R;

/**
 * A custom preference that provides a switch toggle and a clickable preference.
 */
public class OplusPrimarySwitchPreference extends OplusTwoTargetPreference {

    private MaterialSwitch mSwitch;
    private boolean mChecked;
    private boolean mCheckedSet;
    private boolean mEnableSwitch = true;

    public OplusPrimarySwitchPreference(Context context, AttributeSet attrs,
                                           int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public OplusPrimarySwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public OplusPrimarySwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public OplusPrimarySwitchPreference(Context context) {
        super(context);
    }

    @Override
    protected int getSecondTargetResId() {
        return R.layout.oplus_preference_widget_switch;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        mSwitch = (MaterialSwitch) holder.findViewById(R.id.switchWidget);
        if (mSwitch != null) {
            mSwitch.setOnClickListener(v -> {
                if (mSwitch != null && !mSwitch.isEnabled()) {
                    return;
                }
                final boolean newChecked = !mChecked;
                if (callChangeListener(newChecked)) {
                    setChecked(newChecked);
                }
            });

            // Consumes move events to ignore drag actions.
            mSwitch.setOnTouchListener((v, event) -> {
                return event.getActionMasked() == MotionEvent.ACTION_MOVE;
            });

            mSwitch.setContentDescription(getTitle());
            mSwitch.setChecked(mChecked);
            mSwitch.setEnabled(mEnableSwitch);
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    /**
     * Used to validate the state of mChecked and mCheckedSet when testing, without requiring
     * that a ViewHolder be bound to the object.
     */
    @Keep
    @Nullable
    public Boolean getCheckedState() {
        return mCheckedSet ? mChecked : null;
    }

    /**
     * Set the checked status to be {@code checked}.
     *
     * @param checked The new checked status
     */
    public void setChecked(boolean checked) {
        // Always persist/notify the first time; don't assume the field's default of false.
        final boolean changed = mChecked != checked;
        if (changed || !mCheckedSet) {
            mChecked = checked;
            mCheckedSet = true;
            persistBoolean(checked);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    /**
     * Set the Switch to be the status of {@code enabled}.
     *
     * @param enabled The new enabled status
     */
    public void setSwitchEnabled(boolean enabled) {
        mEnableSwitch = enabled;
        if (mSwitch != null) {
            mSwitch.setEnabled(enabled);
        }
    }

    public MaterialSwitch getSwitch() {
        return mSwitch;
    }

    @Override
    protected boolean shouldHideSecondTarget() {
        return getSecondTargetResId() == 0;
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        if (defaultValue == null) {
            defaultValue = false;
        }
        setChecked(getPersistedBoolean((Boolean) defaultValue));
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.mChecked = isChecked();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(@Nullable Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.mChecked);
    }

    static class SavedState extends Preference.BaseSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<>() {
                    @Override
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };

        boolean mChecked;

        SavedState(Parcel source) {
            super(source);
            mChecked = source.readInt() == 1;
        }

        SavedState(Parcelable superState) {
            super(superState);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(mChecked ? 1 : 0);
        }
    }

}

