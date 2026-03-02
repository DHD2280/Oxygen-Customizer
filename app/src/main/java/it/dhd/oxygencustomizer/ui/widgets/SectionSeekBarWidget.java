package it.dhd.oxygencustomizer.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;

import java.text.DecimalFormat;
import java.util.Objects;

import it.dhd.oneplusui.appcompat.cardlist.CardListSelectedItemLayout;
import it.dhd.oneplusui.appcompat.seekbar.OplusSectionSeekBar;
import it.dhd.oneplusui.appcompat.seekbar.OplusSeekBar;
import it.dhd.oxygencustomizer.R;

public class SectionSeekBarWidget extends CardListSelectedItemLayout {

    private CardListSelectedItemLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private OplusSectionSeekBar sectionSeekBar;
    private MaterialButton resetButton;
    private String valueFormat;
    private int defaultValue;
    private boolean hasResetButton;
    private float outputScale = 1f;
    private boolean isDecimalFormat = false;
    private String decimalFormat = "#.#";
    private OnLongClickListener resetClickListener;
    private OplusSeekBar.OnSeekBarChangeListener onSliderTouchListener;
    private String mForcePosition = null;

    public SectionSeekBarWidget(Context context) {
        super(context);
        init(context, null);
    }

    public SectionSeekBarWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SectionSeekBarWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_section_seekbar, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.SliderWidget);
        valueFormat = typedArray.getString(R.styleable.SliderWidget_valueFormat);
        defaultValue = typedArray.getInt(R.styleable.SliderWidget_sliderDefaultValue, Integer.MAX_VALUE);
        hasResetButton = typedArray.getBoolean(R.styleable.SliderWidget_hasResetButton, false);
        if (typedArray.hasValue(R.styleable.SliderWidget_forcePosition)) {
            mForcePosition = typedArray.getString(R.styleable.SliderWidget_forcePosition);
        }
        setTitle(typedArray.getString(R.styleable.SliderWidget_titleText));
        setSliderValueFrom(typedArray.getInt(R.styleable.SliderWidget_sliderValueFrom, 0));
        setSliderValueTo(typedArray.getInt(R.styleable.SliderWidget_sliderValueTo, 100));
        setSliderStepSize(typedArray.getInt(R.styleable.SliderWidget_sliderStepSize, 1));
        setSliderValue(typedArray.getInt(
                R.styleable.SliderWidget_sliderValue,
                typedArray.getInt(R.styleable.SliderWidget_sliderDefaultValue, 50)
        ));
        isDecimalFormat = typedArray.getBoolean(R.styleable.SliderWidget_isDecimalFormat, false);
        decimalFormat = typedArray.getString(R.styleable.SliderWidget_decimalFormat);
        outputScale = typedArray.getFloat(R.styleable.SliderWidget_outputScale, 1f);
        typedArray.recycle();

        if (valueFormat == null) {
            valueFormat = "";
        }

        if (decimalFormat == null) {
            decimalFormat = "#.#";
        }

        setPosition();
        setSelectedText();
        setOnSliderTouchListener(null);
        setResetClickListener(null);
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSelectedText() {
        if (TextUtils.isEmpty(valueFormat)) valueFormat = "";
        summaryTextView.setText(
                (valueFormat.isBlank() || valueFormat.isEmpty() ?
                        String.valueOf(
                                !isDecimalFormat ?
                                        (int) (sectionSeekBar.getProgress() / outputScale) :
                                        new DecimalFormat(decimalFormat)
                                                .format(sectionSeekBar.getProgress() / outputScale)
                        ) :
                        getContext().getString(
                                R.string.opt_selected2,
                                !isDecimalFormat ?
                                        String.valueOf((int) sectionSeekBar.getProgress()) :
                                        new DecimalFormat(decimalFormat)
                                                .format(sectionSeekBar.getProgress() / outputScale),
                                valueFormat
                        )
                )
        );
    }

    public void setSliderStepSize(int value) {
        sectionSeekBar.setIncrement(value);
    }

    public int getSliderValue() {
        return (int) sectionSeekBar.getProgress();
    }

    public void setSliderValue(int value) {
        sectionSeekBar.setProgress(value);
        setSelectedText();
    }

    public void setSliderValueFrom(int value) {
        sectionSeekBar.setMin(value);
    }

    public void setSliderValueTo(int value) {
        sectionSeekBar.setMax(value);
    }

    public void setIsDecimalFormat(boolean isDecimalFormat) {
        this.isDecimalFormat = isDecimalFormat;
        setSelectedText();
    }

    public void setDecimalFormat(String decimalFormat) {
        this.decimalFormat = Objects.requireNonNullElse(decimalFormat, "#.#");
        setSelectedText();
    }

    public void setOutputScale(float scale) {
        this.outputScale = scale;
        setSelectedText();
    }

    public void setOnSliderTouchListener(OplusSeekBar.OnSeekBarChangeListener listener) {
        onSliderTouchListener = listener;

        sectionSeekBar.addOnSeekBarChangeListener(new OplusSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(OplusSeekBar oplusSeekbar, int progress, boolean fromUser) {
                setSelectedText();
                notifyChange(oplusSeekbar);
            }

            @Override
            public void onStartTrackingTouch(OplusSeekBar oplusSeekbar) {
                notifyOnSliderTouchStarted(oplusSeekbar);
            }

            @Override
            public void onStopTrackingTouch(OplusSeekBar oplusSeekbar) {
                setSelectedText();
                notifyOnSliderTouchStopped(oplusSeekbar);
            }
        });
    }

    public void setResetClickListener(OnLongClickListener listener) {
        if (!hasResetButton) {
            resetButton.setVisibility(View.GONE);
            return;
        }
        resetClickListener = listener;

        resetButton.setOnLongClickListener(v -> {
            if (defaultValue == Integer.MAX_VALUE) {
                return false;
            }

            setSliderValue(defaultValue);
            notifyOnResetClicked(v);

            return true;
        });
    }

    public void resetSlider() {
        resetButton.performLongClick();
    }

    private void notifyChange(@NonNull OplusSeekBar slider) {
        if (onSliderTouchListener != null) {
            onSliderTouchListener.onProgressChanged(slider, slider.getProgress(), true);
        }
    }

    private void notifyOnSliderTouchStarted(@NonNull OplusSeekBar slider) {
        if (onSliderTouchListener != null) {
            onSliderTouchListener.onStartTrackingTouch(slider);
        }
    }

    private void notifyOnSliderTouchStopped(@NonNull OplusSeekBar slider) {
        if (onSliderTouchListener != null) {
            onSliderTouchListener.onStopTrackingTouch(slider);
        }
    }

    private void notifyOnResetClicked(View v) {
        if (resetClickListener != null) {
            resetClickListener.onLongClick(v);
        }
    }

    private void setPosition() {
        if (TextUtils.isEmpty(mForcePosition) || container == null) return;

        int bgRes = switch(mForcePosition) {
            case "top" -> R.drawable.preference_background_top;
            case "middle" -> R.drawable.preference_background_middle;
            case "bottom" -> R.drawable.preference_background_bottom;
            default -> R.drawable.preference_background_center;
        };

        container.setBackgroundResource(bgRes);
    }

    public void forcePosition(String position) {
        mForcePosition = position;
        setPosition();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        container.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
        resetButton.setEnabled(enabled);
        sectionSeekBar.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);
        sectionSeekBar = findViewById(R.id.slider_widget);
        resetButton = findViewById(R.id.reset_button);

        container.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        sectionSeekBar.setId(View.generateViewId());
        resetButton.setId(View.generateViewId());
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();

        SavedState ss = new SavedState(superState);
        ss.sliderValue = sectionSeekBar.getProgress();

        return ss;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof SavedState ss)) {
            super.onRestoreInstanceState(state);
            return;
        }

        super.onRestoreInstanceState(ss.getSuperState());

        sectionSeekBar.setProgress(ss.sliderValue);
        setSelectedText();
    }

    private static class SavedState extends BaseSavedState {
        int sliderValue;

        SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            sliderValue = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeFloat(sliderValue);
        }

        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    public View getSliderView() {
        return this.sectionSeekBar;
    }

}

