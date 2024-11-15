package it.dhd.oxygencustomizer.ui.preferences;

import static it.dhd.oxygencustomizer.utils.Constants.getBottomDxR;
import static it.dhd.oxygencustomizer.utils.Constants.getBottomSxR;
import static it.dhd.oxygencustomizer.utils.Constants.getGradientNum;
import static it.dhd.oxygencustomizer.utils.Constants.getGradientOrientation;
import static it.dhd.oxygencustomizer.utils.Constants.getMarginBottom;
import static it.dhd.oxygencustomizer.utils.Constants.getMarginDx;
import static it.dhd.oxygencustomizer.utils.Constants.getMarginSx;
import static it.dhd.oxygencustomizer.utils.Constants.getMarginTop;
import static it.dhd.oxygencustomizer.utils.Constants.getPaddingBottom;
import static it.dhd.oxygencustomizer.utils.Constants.getPaddingDx;
import static it.dhd.oxygencustomizer.utils.Constants.getPaddingSx;
import static it.dhd.oxygencustomizer.utils.Constants.getPaddingTop;
import static it.dhd.oxygencustomizer.utils.Constants.getRoundedCorners;
import static it.dhd.oxygencustomizer.utils.Constants.getStrokeColor;
import static it.dhd.oxygencustomizer.utils.Constants.getStrokeWidth;
import static it.dhd.oxygencustomizer.utils.Constants.getStyle;
import static it.dhd.oxygencustomizer.utils.Constants.getTopDxR;
import static it.dhd.oxygencustomizer.utils.Constants.getTopSxR;
import static it.dhd.oxygencustomizer.utils.Constants.getUseAccentColor;
import static it.dhd.oxygencustomizer.utils.Constants.getUseAccentColorStroke;
import static it.dhd.oxygencustomizer.utils.Constants.getUseGradient;
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
import androidx.preference.PreferenceViewHolder;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import it.dhd.oneplusui.appcompat.cardlist.CardListHelper;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.QsChipLayoutBinding;
import it.dhd.oxygencustomizer.utils.ThemeUtils;

public class BackgroundChipPreference extends DialogPreference {

    private BottomSheetDialog bottomSheetDialog;
    private int mAccentColor;
    boolean useGradient;
    boolean useAccentColor;
    boolean useAccentStroke;
    boolean roundCorners;
    int gradientColor1;
    int gradientColor2;
    int gradientOrientation;
    GradientDrawable.Orientation orientation;
    int gradientType = GradientDrawable.LINEAR_GRADIENT;
    int strokeWidth;
    int strokeColor;
    int topSxR, topDxR, bottomSxR, bottomDxR;
    int marginSx, marginDx, marginTop, marginBottom;
    int paddingSx, paddingDx, paddingTop, paddingBottom;
    int backgroundChipStyle = 0; // 0 = filled, 1 = outlined, 2 mixed
    GradientDrawable gradientDrawable = new GradientDrawable();
    private QsChipLayoutBinding binding;

    private int chipStyle = 0;

    public BackgroundChipPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackgroundChipPreference);
        chipStyle = a.getInteger(R.styleable.BackgroundChipPreference_backgroundChipStyle, 0);
        a.recycle();
        initResources();
    }

    public BackgroundChipPreference(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackgroundChipPreference);
        chipStyle = a.getInteger(R.styleable.BackgroundChipPreference_backgroundChipStyle, 0);
        a.recycle();
        initResources();
    }

    public BackgroundChipPreference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BackgroundChipPreference);
        chipStyle = a.getInteger(R.styleable.BackgroundChipPreference_backgroundChipStyle, 0);
        a.recycle();
        initResources();
    }

    public BackgroundChipPreference(@NonNull Context context) {
        super(context);
        initResources();
    }

    private void initResources() {
        setLayoutResource(R.layout.oplus_preference);
        setWidgetLayoutResource(R.layout.oplus_preference_widget_jump);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        CardListHelper.setItemCardBackground(holder.itemView, CardListHelper.getPositionInGroup(this));
    }

    @Override
    protected void onClick() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        mAccentColor = ThemeUtils.getPrimaryColor(getContext());
        SharedPreferences prefs = getSharedPreferences();

        // def props
        loadPrefs(prefs);

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
        binding.mixedChip.setOnClickListener(v -> {
            prefs.edit().putInt(getKey() + "_STYLE", 2).apply();
            backgroundChipStyle = 2;

            setupWidgets();
            setupGradient();
        });

        // Gradient Props
        binding.gradientOrientation.setOnSelectedListener((entry, entryValue) -> {
            prefs.edit().putInt(getGradientOrientation(getKey()), Integer.parseInt(entryValue.toString())).apply();
            gradientOrientation = Integer.parseInt(entryValue.toString());

            setupWidgets();
            setupGradient();
        });
        binding.accentSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(getUseAccentColor(getKey()), isChecked).apply();
            binding.accentSwitch.forcePosition(isChecked ? "full" : "top");
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

        // Stroke Props
        binding.accentStrokeSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(getUseAccentColorStroke(getKey()), isChecked).apply();
            useAccentStroke = isChecked;

            setupWidgets();
            setupGradient();
        });
        binding.strokeColor.setColorPickerListener(
                (FragmentActivity) getContext(),
                prefs.getInt(getStrokeColor(getKey()), mAccentColor),
                true,
                true,
                true
        );
        binding.strokeColor.setOnColorSelectedListener(color -> {
            prefs.edit().putInt(getStrokeColor(getKey()), color).apply();
            strokeColor = color;

            setupWidgets();
            setupGradient();
        });
        binding.strokeWidth.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getStrokeWidth(getKey()), (int) value).apply();
            strokeWidth = (int) value;

            setupGradient();
        });
        binding.roundCornersSwitch.setSwitchChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean(getRoundedCorners(getKey()), isChecked).apply();
            binding.roundCornersSwitch.forcePosition(isChecked ? "top" : "full");
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

        // Margin
        binding.chipMarginLeft.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getMarginSx(getKey()), (int) value).apply();
            marginSx = (int) value;
        });
        binding.chipMarginRight.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getMarginDx(getKey()), (int) value).apply();
            marginDx = (int) value;
        });
        binding.chipMarginTop.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getMarginTop(getKey()), (int) value).apply();
            marginTop = (int) value;
        });
        binding.chipMarginBottom.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getMarginBottom(getKey()), (int) value).apply();
            marginBottom = (int) value;
        });

        // Padding
        binding.chipPaddingLeft.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getPaddingSx(getKey()), (int) value).apply();
            paddingSx = (int) value;
        });
        binding.chipPaddingRight.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getPaddingDx(getKey()), (int) value).apply();
            paddingDx = (int) value;
        });
        binding.chipPaddingTop.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getPaddingTop(getKey()), (int) value).apply();
            paddingTop = (int) value;
        });
        binding.chipPaddingBottom.setOnSliderChangeListener((slider, value, fromUser) -> {
            prefs.edit().putInt(getPaddingBottom(getKey()), (int) value).apply();
            paddingBottom = (int) value;
        });

        // Set Widgets
        setupWidgets();

    }

    private void loadPrefs(SharedPreferences prefs) {
        backgroundChipStyle = prefs.getInt(getStyle(getKey()), 0);
        useAccentColor = prefs.getBoolean(getUseAccentColor(getKey()), true);
        useGradient = prefs.getBoolean(getUseGradient(getKey()), false);
        gradientColor1 = prefs.getInt(getGradientNum(getKey(), 1), mAccentColor);
        gradientColor2 = prefs.getInt(getGradientNum(getKey(), 2), mAccentColor);
        gradientOrientation = prefs.getInt(getGradientOrientation(getKey()), 0);
        orientation = switch (gradientOrientation) {
            case 1 -> GradientDrawable.Orientation.TOP_BOTTOM;
            case 2 -> GradientDrawable.Orientation.TL_BR;
            case 3 -> GradientDrawable.Orientation.TR_BL;
            default -> GradientDrawable.Orientation.LEFT_RIGHT;
        };
        gradientType = GradientDrawable.LINEAR_GRADIENT;
        strokeWidth = prefs.getInt(getStrokeWidth(getKey()), 10);
        useAccentStroke = prefs.getBoolean(getUseAccentColorStroke(getKey()), true);
        strokeColor = prefs.getInt(getStrokeColor(getKey()), mAccentColor);
        roundCorners = prefs.getBoolean(getRoundedCorners(getKey()), false);
        topSxR = prefs.getInt(getTopSxR(getKey()), 28);
        topDxR = prefs.getInt(getTopDxR(getKey()), 28);
        bottomSxR = prefs.getInt(getBottomSxR(getKey()), 28);
        bottomDxR = prefs.getInt(getBottomDxR(getKey()), 28);
        marginSx = prefs.getInt(getMarginSx(getKey()), 0);
        marginDx = prefs.getInt(getMarginDx(getKey()), 0);
        marginTop = prefs.getInt(getMarginTop(getKey()), 0);
        marginBottom = prefs.getInt(getMarginBottom(getKey()), 0);
        paddingSx = prefs.getInt(getMarginSx(getKey()), 0);
        paddingDx = prefs.getInt(getMarginDx(getKey()), 0);
        paddingTop = prefs.getInt(getMarginTop(getKey()), 0);
        paddingBottom = prefs.getInt(getMarginBottom(getKey()), 0);
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
        GradientDrawable.Orientation orientation = switch (gradientOrientation) {
            case 1 -> GradientDrawable.Orientation.TOP_BOTTOM;
            case 2 -> GradientDrawable.Orientation.TL_BR;
            case 3 -> GradientDrawable.Orientation.TR_BL;
            default -> GradientDrawable.Orientation.LEFT_RIGHT;
        };
        gradientDrawable.setOrientation(orientation);
        int[] colors;
        int strokeC = Color.TRANSPARENT;
        int strokeW = strokeWidth;

        if (useAccentColor) {
            colors = new int[]{mAccentColor, mAccentColor};
        } else if (useGradient) {
            colors = new int[]{gradientColor1, gradientColor2};
        } else {
            colors = new int[]{gradientColor1, gradientColor1};
        }
        switch (backgroundChipStyle) {
            case 0 -> strokeW = 0;
            case 1 -> {
                colors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT};
                strokeC = useAccentStroke ? mAccentColor : strokeColor;
            }
            case 2 -> strokeC = useAccentStroke ? mAccentColor : strokeColor;
        };
        gradientDrawable.setColors(colors);
        gradientDrawable.setStroke(strokeW, strokeC);
        if (roundCorners) {
            gradientDrawable.setCornerRadii(new float[]{
                    dp2px(getContext(), topSxR), dp2px(getContext(), topSxR),
                    dp2px(getContext(), topDxR), dp2px(getContext(), topDxR),
                    dp2px(getContext(), bottomDxR), dp2px(getContext(), bottomDxR),
                    dp2px(getContext(), bottomSxR), dp2px(getContext(), bottomSxR)
            });
        } else {
            gradientDrawable.setCornerRadius(0);
        }

        gradientDrawable.setPadding(20, 8, 20, 8);
        gradientDrawable.invalidateSelf();
    }

    private void setupWidgets() {
        if (binding == null) return;
        boolean filled = backgroundChipStyle == 0;
        boolean outlined = backgroundChipStyle == 1;
        boolean mixed = backgroundChipStyle == 2;

        binding.filledChip.setChecked(filled);
        binding.outlinedChip.setChecked(outlined);
        binding.mixedChip.setChecked(mixed);
        // Gradient Prefs
        binding.gradientPrefs.setVisibility(filled || mixed ? View.VISIBLE : View.GONE);
        binding.gradientOrientation.setVisibility((filled || mixed) && useGradient && !useAccentColor ? View.VISIBLE : View.GONE);
        binding.gradientOrientation.setSelectedValue(String.valueOf(gradientOrientation));
        binding.accentSwitch.setVisibility((filled || mixed) ? View.VISIBLE : View.GONE);
        if ((filled || mixed) && useGradient && !useAccentColor) {
            binding.accentSwitch.forcePosition("middle");
        }
        binding.gradientSwitch.setVisibility((filled || mixed) && !useAccentColor ? View.VISIBLE : View.GONE);
        binding.gradientSwitch.setSwitchChecked(useGradient);
        binding.colorPickerGradient1.setTitle(useGradient ? getContext().getString(R.string.chip_gradient_color_1) : getContext().getString(R.string.chip_color_color));
        binding.colorPickerGradient1.setVisibility((filled || mixed) && !useAccentColor ? View.VISIBLE : View.GONE);
        binding.colorPickerGradient1.setPreviewColor(gradientColor1);
        if (!useAccentColor && useGradient && (filled || mixed)) {
            binding.colorPickerGradient2.setVisibility(View.VISIBLE);
        } else {
            binding.colorPickerGradient2.setVisibility(View.GONE);
        }
        binding.colorPickerGradient2.setPreviewColor(gradientColor2);

        if (binding.gradientOrientation.getVisibility() == View.VISIBLE && binding.gradientSwitch.getVisibility() == View.VISIBLE) {
            binding.accentSwitch.forcePosition("middle");
        } else if (binding.gradientOrientation.getVisibility() == View.GONE && binding.gradientSwitch.getVisibility() == View.VISIBLE) {
            binding.accentSwitch.forcePosition("top");
        } else {
            binding.accentSwitch.forcePosition("full");
        }

        if (binding.colorPickerGradient2.getVisibility() == View.VISIBLE) {
            binding.colorPickerGradient1.forcePosition("middle");
        } else {
            binding.colorPickerGradient1.forcePosition("bottom");
        }

        // Stroke Prefs
        binding.strokePrefs.setVisibility(outlined || mixed ? View.VISIBLE : View.GONE);
        binding.accentStrokeSwitch.setVisibility(outlined || mixed ? View.VISIBLE : View.GONE);
        binding.strokeColor.setVisibility((outlined || mixed) && !useAccentStroke ? View.VISIBLE : View.GONE);
        binding.strokeWidth.setVisibility(outlined || mixed || binding.mixedChip.isChecked() ? View.VISIBLE : View.GONE);
        binding.strokeColor.setPreviewColor(strokeColor);
        binding.strokeWidth.setSliderValue(strokeWidth);

        // Round Corner Prefs
        binding.roundCornersSwitch.setSwitchChecked(roundCorners);
        binding.topSxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.topSxCorner.setSliderValue(topSxR);
        binding.topDxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.topDxCorner.setSliderValue(topDxR);
        binding.bottomSxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.bottomSxCorner.setSliderValue(bottomSxR);
        binding.bottomDxCorner.setVisibility(roundCorners ? View.VISIBLE : View.GONE);
        binding.bottomDxCorner.setSliderValue(bottomDxR);
        binding.chipMarginLeft.setSliderValue(marginSx);
        binding.chipMarginRight.setSliderValue(marginDx);
        binding.chipMarginTop.setSliderValue(marginTop);
        binding.chipMarginBottom.setSliderValue(marginBottom);
        binding.chipPaddingLeft.setSliderValue(paddingSx);
        binding.chipPaddingRight.setSliderValue(paddingDx);
        binding.chipPaddingTop.setSliderValue(paddingTop);
        binding.chipPaddingBottom.setSliderValue(paddingBottom);
        bottomSheetDialog.setContentView(binding.getRoot());
        bottomSheetDialog.show();
    }
}
