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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.materialswitch.MaterialSwitch;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class TopIntroWidget extends LinearLayout {

    private LinearLayout container;
    private TextView titleTextView;
    private ImageView iconImageView;
    private String mForcePosition = null;
    private boolean mDrawBackground;

    public TopIntroWidget(Context context) {
        super(context);
        init(context, null);
    }

    public TopIntroWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public TopIntroWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        inflate(context, R.layout.top_intro_preference, this);

        initializeId();

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.TopIntroWidget);
        setTitle(typedArray.getString(R.styleable.TopIntroWidget_titleText));
        if (typedArray.hasValue(R.styleable.TopIntroWidget_forcePosition)) {
            mForcePosition = typedArray.getString(R.styleable.TopIntroWidget_forcePosition);
        }
        mDrawBackground = typedArray.getBoolean(R.styleable.TopIntroWidget_drawBackground, true);
        int icon = typedArray.getResourceId(R.styleable.TopIntroWidget_icon, R.drawable.settingslib_ic_info_outline_24);
        typedArray.recycle();

        if (icon != 0) {
            iconImageView.setVisibility(VISIBLE);
            iconImageView.setImageResource(icon);
        }

        setPosition();
    }

    public void setTitle(int titleResId) {
        titleTextView.setText(titleResId);
    }

    public void setTitle(String title) {
        titleTextView.setText(title);
    }

    public void setIcon(int icon) {
        iconImageView.setImageResource(icon);
        iconImageView.setVisibility(VISIBLE);
    }

    public void setIcon(Drawable drawable) {
        iconImageView.setImageDrawable(drawable);
        iconImageView.setVisibility(VISIBLE);
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
        if (!mDrawBackground) {
            container.setBackgroundResource(0);
            return;
        }

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
    }

    // to avoid listener bug, we need to re-generate unique id for each view
    private void initializeId() {
        container = findViewById(R.id.container);
        iconImageView = findViewById(android.R.id.icon);
        titleTextView = findViewById(android.R.id.title);

        container.setId(View.generateViewId());
        iconImageView.setId(View.generateViewId());
        titleTextView.setId(View.generateViewId());
    }

    public View getTitleView() {
        return this.titleTextView;
    }

    public interface BeforeSwitchChangeListener {
        void beforeSwitchChanged();
    }
}
