package it.dhd.oxygencustomizer.customprefs;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getBottomDxR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getBottomSxR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getGradientNum;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getGradientOrientation;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getRoundedCorners;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getStrokeColor;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getStrokeWidth;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getStyle;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getTopDxR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getTopSxR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getUseAccentColor;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.getUseGradient;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.DialogPreference;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.QsChipLayoutBinding;
import it.dhd.oxygencustomizer.utils.PreferenceHelper;
import it.dhd.oxygencustomizer.utils.ThemeUtils;

public class BackgroundChipPreference extends DialogPreference {

    private BottomSheetDialog bottomSheetDialog;
    private int mAccentColor;
    boolean useGradient;
    boolean useAccentColor;
    boolean roundCorners;
    int gradientColor1;
    int gradientColor2;
    int gradientOrientation;
    GradientDrawable.Orientation orientation;
    int gradientType = GradientDrawable.LINEAR_GRADIENT;
    int strokeWidth;
    int strokeColor;
    int topSxR, topDxR, bottomSxR, bottomDxR;
    int backgroundChipStyle = 0;
    GradientDrawable gradientDrawable = new GradientDrawable();
    private QsChipLayoutBinding binding;

    private int chipStyle = 0;


    public BackgroundChipPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackgroundChipPreference);
        chipStyle = a.getInteger(R.styleable.BackgroundChipPreference_backgroundChipStyle, 0);
        a.recycle();

    }

    public BackgroundChipPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackgroundChipPreference);
        chipStyle = a.getInteger(R.styleable.BackgroundChipPreference_backgroundChipStyle, 0);
        a.recycle();
    }

    public BackgroundChipPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackgroundChipPreference);
        chipStyle = a.getInteger(R.styleable.BackgroundChipPreference_backgroundChipStyle, 0);
        a.recycle();
    }

    public BackgroundChipPreference(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        mAccentColor = ThemeUtils.getPrimaryColor(getContext());
        SharedPreferences prefs = getSharedPreferences();

        // def props
        backgroundChipStyle = prefs.getInt(getStyle(getKey()), 0);
        useAccentColor = prefs.getBoolean(getUseAccentColor(getKey()), true);
        useGradient = prefs.getBoolean(getUseGradient(getKey()), false);
        gradientColor1 = prefs.getInt(getGradientNum(getKey(), 1), mAccentColor);
        gradientColor2 = prefs.getInt(getGradientNum(getKey(), 2), mAccentColor);
        gradientOrientation = prefs.getInt(getGradientOrientation(getKey()), 0);
        orientation = switch (gradientOrientation) {
            case 1 -> GradientDrawable.Orientation.RIGHT_LEFT;
            case 2 -> GradientDrawable.Orientation.TOP_BOTTOM;
            case 3 -> GradientDrawable.Orientation.BOTTOM_TOP;
            default -> GradientDrawable.Orientation.LEFT_RIGHT;
        };
        gradientType = GradientDrawable.LINEAR_GRADIENT;
        strokeWidth = prefs.getInt(getStrokeWidth(getKey()), 10);
        strokeColor = prefs.getInt(getStrokeColor(getKey()), mAccentColor);
        topSxR = prefs.getInt(getTopSxR(getKey()), 28);
        topDxR = prefs.getInt(getTopDxR(getKey()), 28);
        bottomSxR = prefs.getInt(getBottomSxR(getKey()), 28);
        bottomDxR = prefs.getInt(getBottomDxR(getKey()), 28);

        binding = QsChipLayoutBinding.inflate(LayoutInflater.from(getContext()));

        // Setup textclock
        switch (chipStyle) {
            case 0 -> {
                binding.textClock.setFormat12Hour("hh:mm");
                binding.textClock.setFormat24Hour("HH:mm");
            }
            case 1 -> {
                binding.textClock.setFormat12Hour("EEE dd MMM");
                binding.textClock.setFormat24Hour("EEE dd MMM");
            }
            case 2 -> {
                binding.textClock.setVisibility(View.GONE);
            }
        }

        // Setup toolbar
        binding.toolbarPreference.setTitle(getTitle());
        binding.toolbarPreference.setTitleCentered(true);

        setupGradient();

        // set textclock
        binding.textClock.setBackground(gradientDrawable);
        binding.textClock.setTextSize(getAdapterTextSizeSp());

        // Buttons
        binding.filledChip.setOnClickListener(v -> {
            prefs.edit().putInt(getKey() + "_STYLE", 0).apply();
            backgroundChipStyle = 0;

            setupWidgets();
            setupGradient();
        });
        binding.outlinedChip.setOnClickListener(v -> {
            prefs.edit().putInt(getKey() + "_STYLE", 1).apply();
            backgroundChipStyle = 1;

            setupWidgets();
            setupGradient();
        });

        // Color Category
        binding.accentSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(getUseAccentColor(getKey()), isChecked).apply();
            useAccentColor = isChecked;

            setupWidgets();
            setupGradient();
        });

        binding.gradientSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(getUseGradient(getKey()), isChecked).apply();
            useGradient = isChecked;

            setupWidgets();
            setupGradient();
        });

        binding.colorPickerGradient1.setColorPickerListener(
                (FragmentActivity) getContext(),
                prefs.getInt(getGradientNum(getKey(), 1), mAccentColor),
                true,
                true,
                true
        );

        binding.colorPickerGradient1.setOnColorSelectedListener(color -> {
            prefs.edit().putInt(getGradientNum(getKey(), 1), color).apply();
            gradientColor1 = color;

            setupWidgets();
            setupGradient();
        });

        binding.colorPickerGradient2.setColorPickerListener(
                (FragmentActivity) getContext(),
                prefs.getInt(getGradientNum(getKey(), 2), mAccentColor),
                true,
                true,
                true
        );
        binding.colorPickerGradient2.setOnColorSelectedListener(color -> {
            prefs.edit().putInt(getGradientNum(getKey(), 2), color).apply();
            gradientColor2 = color;

            setupWidgets();
            setupGradient();
        });

        // Stroke Category
        binding.strokeWidth.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getStrokeWidth(getKey()), (int) value).apply();
            strokeWidth = (int) value;

            setupGradient();
        });
        binding.roundCornersSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(getRoundedCorners(getKey()), isChecked).apply();
            roundCorners = isChecked;

            setupWidgets();
            setupGradient();
        });
        binding.topSxCorner.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getTopSxR(getKey()), (int) value).apply();
            topSxR = (int) value;

            setupGradient();
        });
        binding.topDxCorner.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getTopDxR(getKey()), (int) value).apply();
            topDxR = (int) value;

            setupGradient();
        });
        binding.bottomSxCorner.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getBottomSxR(getKey()), (int) value).apply();
            bottomSxR = (int) value;

            setupGradient();
        });
        binding.bottomDxCorner.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getBottomDxR(getKey()), (int) value).apply();
            bottomDxR = (int) value;

            setupGradient();
        });

        // Set Widgets
        setupWidgets();

    }

    public float getAdapterTextSizeSp() {
        float f = 12.0f;
        int fontScaleToArrayIndex = fontScaleToArrayIndex(new float[]{0.9f, 1.0f, 1.15f, 1.35f, 1.60f});
        int[] desiredFontSize = new int[]{18, 18, 18, 16, 12};
        if (desiredFontSize.length > fontScaleToArrayIndex) {
            f = desiredFontSize[fontScaleToArrayIndex];
        }
        return f;
    }


    private int fontScaleToArrayIndex(float[] fArr) {
        float fontScale = Resources.getSystem().getConfiguration().fontScale;
        int index = 0;
        float minDifference = Math.abs(fArr[0] - fontScale);

        for (int i = 1; i < fArr.length; i++) {
            float difference = Math.abs(fArr[i] - fontScale);

            if (difference <= minDifference) {
                index = i;
                minDifference = difference;
            }
        }

        return index;
    }

    private void setupGradient() {
        // setup gradient
        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        if (backgroundChipStyle == 0) {
            if (useAccentColor)
                gradientDrawable.setColors(new int[]{mAccentColor, mAccentColor});
            else if (useGradient)
                gradientDrawable.setColors(new int[]{gradientColor1, gradientColor2});
            else
                gradientDrawable.setColors(new int[]{gradientColor1, gradientColor1});

            gradientDrawable.setStroke(0, Color.TRANSPARENT);
        } else {
            gradientDrawable.setColors(new int[]{Color.TRANSPARENT, Color.TRANSPARENT});
            gradientDrawable.setStroke(strokeWidth, useAccentColor ? mAccentColor : gradientColor1);
        }
        if (roundCorners) {
            gradientDrawable.setCornerRadii(new float[]{
                    dp2px(getContext(), topSxR), dp2px(getContext(), topSxR),
                    dp2px(getContext(), topDxR), dp2px(getContext(), topDxR),
                    dp2px(getContext(), bottomSxR), dp2px(getContext(), bottomSxR),
                    dp2px(getContext(), bottomDxR), dp2px(getContext(), bottomDxR)
            });
        } else {
            gradientDrawable.setCornerRadius(0);
        }

        gradientDrawable.setPadding(20, 8, 20, 8);
        gradientDrawable.invalidateSelf();
    }

    private void setupWidgets() {
        if (binding == null) return;
        binding.filledChip.setChecked(backgroundChipStyle == 0);
        binding.outlinedChip.setChecked(!binding.filledChip.isChecked());
        binding.strokePrefs.setVisibility(binding.outlinedChip.isChecked() ? View.VISIBLE : View.GONE);
        binding.strokeWidth.setVisibility(binding.outlinedChip.isChecked() ? View.VISIBLE : View.GONE);
        binding.accentSwitch.setSwitchChecked(useAccentColor);
        binding.gradientSwitch.setVisibility(useAccentColor || backgroundChipStyle == 1 ? View.GONE : View.VISIBLE);
        binding.gradientSwitch.setSwitchChecked(useGradient);
        binding.colorPickerGradient1.setVisibility(useAccentColor ? View.GONE : View.VISIBLE);
        binding.colorPickerGradient1.setTitle(useGradient ? getContext().getString(R.string.chip_gradient_color_1) : getContext().getString(R.string.chip_color_color));
        binding.colorPickerGradient1.setPreviewColor(gradientColor1);
        if (!useAccentColor && useGradient && backgroundChipStyle == 0) {
            binding.colorPickerGradient2.setVisibility(View.VISIBLE);
        } else {
            binding.colorPickerGradient2.setVisibility(View.GONE);
        }

        binding.colorPickerGradient2.setPreviewColor(gradientColor2);
        binding.strokeWidth.setSliderValue(strokeWidth);
        binding.roundCornersSwitch.setSwitchChecked(roundCorners);
        binding.topSxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.topSxCorner.setSliderValue(topSxR);
        binding.topDxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.topDxCorner.setSliderValue(topDxR);
        binding.bottomSxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.bottomSxCorner.setSliderValue(bottomSxR);
        binding.bottomDxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.bottomDxCorner.setSliderValue(bottomDxR);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.show();
    }
}
