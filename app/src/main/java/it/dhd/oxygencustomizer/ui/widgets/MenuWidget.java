package it.dhd.oxygencustomizer.ui.widgets;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import it.dhd.oneplusui.appcompat.poplist.SimpleMenuPopupWindow;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class MenuWidget extends RelativeLayout {

    private RelativeLayout container;
    private TextView titleTextView;
    private TextView summaryTextView;
    private TextView selectedTextView;
    private ImageView iconImageView;
    private CharSequence[] mEntries;
    private CharSequence[] mEntryValues;
    private OnSelectedListener mOnSelectedListener;
    private int selectedPosition = -1;
    private int iconWidth, iconHeight;
    private String mForcePosition = null;
    private SimpleMenuPopupWindow mPopupWindow;

    public MenuWidget(Context context) {
        super(context);
        init(context, null, R.style.Preferences_OplusMenuPreference);
    }

    public MenuWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.style.Preferences_OplusMenuPreference);
    }

    public MenuWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        inflate(context, R.layout.view_widget_menu, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MenuWidget);
        setTitle(typedArray.getString(R.styleable.MenuWidget_titleText));
        setSummary(typedArray.getString(R.styleable.MenuWidget_summaryText));
        iconWidth = typedArray.getDimensionPixelSize(R.styleable.MenuWidget_iconWidth, 20);
        iconHeight = typedArray.getDimensionPixelSize(R.styleable.MenuWidget_iconHeight, 20);
        mEntries = typedArray.getTextArray(R.styleable.MenuWidget_entries);
        mEntryValues = typedArray.getTextArray(R.styleable.MenuWidget_entryValues);
        if (typedArray.hasValue(R.styleable.MenuWidget_forcePosition)) {
            mForcePosition = typedArray.getString(R.styleable.MenuWidget_forcePosition);
        }
        int icon = typedArray.getResourceId(R.styleable.MenuWidget_icon, 0);
        boolean iconSpaceReserved = typedArray.getBoolean(R.styleable.MenuWidget_iconSpaceReserved, false);
        typedArray.recycle();

        if (icon != 0) {
            iconSpaceReserved = true;
            iconImageView.setImageResource(icon);
        }

        if (!iconSpaceReserved) {
            iconImageView.setVisibility(GONE);
        }

        int popupStyle = R.style.Widget_Preference_SimpleMenuPreference_PopupMenu;
        int popupTheme = R.style.Widget_App_PopupMenu;
        Context popupContext;
        popupContext = new ContextThemeWrapper(context, popupTheme);

        mPopupWindow = new SimpleMenuPopupWindow(popupContext, attrs, R.styleable.OplusMenuPreference_android_popupMenuStyle, popupStyle);
        mPopupWindow.setOnItemClickListener(i -> {
            Log.d("MenuWidget", "setOnItemClickListener: " + i + " " + mEntries[i] + " " + mEntryValues[i]);
            setSelectedValue(mEntryValues[i]);
            if (mOnSelectedListener != null) {
                mOnSelectedListener.onSelected(mEntries[i], mEntryValues[i]);
            }
        });

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
        setImageDimensions(iconWidth, iconHeight);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
        setImageDimensions(iconWidth, iconHeight);
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

        int bgRes = switch (mForcePosition) {
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
                    new int[]{R.attr.colorPrimaryVariant}
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
        selectedTextView = findViewById(R.id.menu_value);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
        summaryTextView.setId(View.generateViewId());
        selectedTextView.setId(View.generateViewId());


        RelativeLayout.LayoutParams layoutParams = (LayoutParams) findViewById(R.id.text_container).getLayoutParams();
        layoutParams.addRule(RelativeLayout.END_OF, iconImageView.getId());
        findViewById(R.id.text_container).setLayoutParams(layoutParams);
    }

    public void setSelectedValue(CharSequence value) {
        if (mEntryValues == null || mEntryValues.length == 0) return;
        for (int i = 0; i < mEntryValues.length; i++) {
            if (mEntryValues[i].equals(value)) {
                selectedPosition = i;
                setValue(mEntries[i]);
                break;
            }
        }
    }

    private void setValue(CharSequence val) {
        selectedTextView.setText(val);
    }

    private View findRecyclerViewParent() {
        View parent = (View) getParent();

        while (parent != null) {
            if (parent instanceof RecyclerView) {
                return parent;
            }
            if (parent.getParent() instanceof View) {
                parent = (View) parent.getParent();
            } else {
                break;
            }
        }

        return null;
    }

    private void showDialog() {

        if (mEntries == null || mEntries.length == 0) {
            return;
        }

        if (mPopupWindow == null) {
            return;
        }

        boolean[] checkedValue = new boolean[mEntries.length];
        for (int i = 0; i < mEntries.length; i++) {
            checkedValue[i] = i == selectedPosition;
        }

        mPopupWindow.setEntries(mEntries);
        mPopupWindow.setSelectedIndex(selectedPosition);

        View container = findRecyclerViewParent();

        if (container == null) {
            container = (View) getParent(); // fallback
        }

        mPopupWindow.show(
                this,               // anchor
                container,          // container
                (int) titleTextView.getX()
        );
    }

    public interface OnSelectedListener {
        void onSelected(CharSequence entry, CharSequence entryValue);
    }

    public void setEntries(CharSequence[] entries) {
        mEntries = entries;
    }

    public void setEntryValues(CharSequence[] entryValues) {
        mEntryValues = entryValues;
    }

    public CharSequence[] getEntries() {
        return mEntries;
    }

    public CharSequence[] getEntryValues() {
        return mEntryValues;
    }

    public View getTitleView() {
        return this.titleTextView;
    }

}
