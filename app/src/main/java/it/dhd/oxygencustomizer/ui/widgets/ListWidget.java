package it.dhd.oxygencustomizer.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class ListWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private ImageView iconImageView;
    private AlertDialog mListDialog;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private OnSelectedListener mOnSelectedListener;
    private int selectedPosition = -1;
    private String mForcePosition = null;

    public ListWidget(Context context) {
        super(context);
        init(context, null);
    }

    public ListWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ListWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.view_widget_list, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ListWidget);
        setTitle(typedArray.getString(R.styleable.ListWidget_titleText));
        setSummary(typedArray.getString(R.styleable.ListWidget_summaryText));
        mEntries = typedArray.getTextArray(R.styleable.ListWidget_entries);
        mEntryValues = typedArray.getTextArray(R.styleable.ListWidget_entryValues);
        if (typedArray.hasValue(R.styleable.ListWidget_forcePosition)) {
            mForcePosition = typedArray.getString(R.styleable.ListWidget_forcePosition);
        }
        int icon = typedArray.getResourceId(R.styleable.ListWidget_icon, 0);
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.ListWidget_iconSpaceReserved, false);
        typedArray.recycle();

        if (icon != 0) {
            iconSpaceReserved = true;
            iconImageView.setImageResource(icon);
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE);
        }

        setPosition();

        container.setOnClickListener(v -> showDialog());
    }

    public void setOnSelectedListener(OnSelectedListener listener) {
        mOnSelectedListener = listener;
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setSummary(int summaryResId) {
        summaryTextView.setText(summaryResId);
    }

    public void setSummary(String summary) {
        if (TextUtils.isEmpty(summary)) {
            summaryTextView.setVisibility(GONE);
        } else {
            summaryTextView.setVisibility(VISIBLE);
            summaryTextView.setText(summary);
        }
    }

    public void setIcon(int icon) {
        iconImageView.setImageResource(icon);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setImageDimensions(int width, int height) {
        iconImageView.getLayoutParams().width = width;
        iconImageView.getLayoutParams().height = height;
    }

    public void setImageMargin(int left) {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) iconImageView.getLayoutParams();
        layoutParams.setMarginStart(left);
        iconImageView.setLayoutParams(layoutParams);
    }

    public void setIconVisibility(int visibility) {
        iconImageView.setVisibility(visibility);
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

        if (enabled) {
            TypedValue typedValue = new TypedValue();
            TypedArray a = getContext().obtainStyledAttributes(
                    typedValue.data,
                    new int[]{com.google.android.material.R.attr.colorPrimary}
            );
            int color = a.getColor(0, 0);
            a.recycle();

            iconImageView.setImageTintList(ColorStateList.valueOf(color));
        } else {
            if (SystemUtils.isDarkMode()) {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.DKGRAY));
            } else {
                iconImageView.setImageTintList(ColorStateList.valueOf(Color.LTGRAY));
            }
        }

        container.setEnabled(enabled);
        iconImageView.setEnabled(enabled);
        titleTextView.setEnabled(enabled);
        summaryTextView.setEnabled(enabled);
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        iconImageView = findViewById(R.id.icon);
        titleTextView = findViewById(R.id.title);
        summaryTextView = findViewById(R.id.summary);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());

        RelativeLayout.LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.END_OF, iconImageView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    public void setSelectedValue(CharSequence value) {
        for (int i = 0; i < mEntryValues.length; i++) {
            if (mEntryValues[i].equals(value)) {
                selectedPosition = i;
                setSummary(mEntries[i].toString());
                break;
            }
        }
    }

    private void showDialog() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext());
        builder.setTitle(titleTextView.getText());
        builder.setSingleChoiceItems(mEntries, selectedPosition, (dialog, which) -> {
            if (mOnSelectedListener != null) {
                mOnSelectedListener.onSelected(mEntries[which], mEntryValues[which]);
            }
            dialog.dismiss();
        });
        builder.show();
    }

    public interface OnSelectedListener {
        void onSelected(CharSequence entry, CharSequence entryValue);
    }
}