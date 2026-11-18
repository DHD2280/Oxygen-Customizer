package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets;

import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Objects;

import it.dhd.oxygencustomizer.xposed.utils.LiquidGlass;
import it.dhd.oxygencustomizer.xposed.utils.WidgetFactory;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

@SuppressLint("ViewConstructor")
public class DeviceWidgetCell extends FrameLayout {

    private static final int GRID_COLUMNS = 4;
    private static final int GAP_DP = 12;
    private static final int GLASS_PADDING_DP = 8;
    private static final int GLASS_RADIUS_DP = 18;
    private static final int BIG_PADDING_DP = 14;
    private static final float ARC_FILL = 0.78f;

    private BaseDeviceWidget mWidget;
    private String mConfig;
    private boolean mLiquidGlass;
    private boolean mCustomColor;
    private int mProgressColor;
    private int mTextColor;
    private String mDeviceName = "";
    private int mReferenceWidth = 0;
    private float mWidthScale = 1f;
    private float mHeightScale = 1f;

    public DeviceWidgetCell(Context context) {
        super(context);
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void setConfig(String config) {
        if (Objects.equals(mConfig, config) && mWidget != null) return;
        mConfig = config;
        removeAllViews();
        mWidget = WidgetFactory.fromConfigJson(config, getContext(), false);
        if (mWidget == null) return;
        addView(mWidget, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        applyStyle();
    }

    public void setWidthScale(float scale) {
        float clamped = scale > 0f ? scale : 1f;
        if (clamped == mWidthScale) return;
        mWidthScale = clamped;
        requestLayout();
    }

    public void setHeightScale(float scale) {
        float clamped = scale > 0f ? scale : 1f;
        if (clamped == mHeightScale) return;
        mHeightScale = clamped;
        requestLayout();
    }

    public void setReferenceWidth(int width) {
        mReferenceWidth = width;
        requestLayout();
    }

    public void setStyle(boolean liquidGlass, boolean customColor, int progressColor, int textColor, String deviceName) {
        mLiquidGlass = liquidGlass && Build.VERSION.SDK_INT >= 37;
        mCustomColor = customColor;
        mProgressColor = progressColor;
        mTextColor = textColor;
        mDeviceName = TextUtils.isEmpty(deviceName) ? Build.MODEL : deviceName;
        applyStyle();
    }

    private void applyStyle() {
        if (mWidget == null) return;
        mWidget.setCustomColors(mCustomColor ? mProgressColor : getPrimaryColor(getContext()), mTextColor);
        mWidget.setCustomDeviceName(mDeviceName);
        if (mLiquidGlass) {
            LiquidGlass.apply(mWidget, dp2px(getContext(), GLASS_RADIUS_DP), LiquidGlass.DEFAULT_COLOR);
        } else {
            mWidget.setBackground(null);
        }
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mWidget == null) {
            setMeasuredDimension(0, 0);
            return;
        }
        int available = mReferenceWidth > 0 ? mReferenceWidth : MeasureSpec.getSize(widthMeasureSpec);
        int gap = dp2px(getContext(), GAP_DP);
        int column = (available - gap) / GRID_COLUMNS;
        int cell = Math.max(0, column - gap);
        boolean small = mWidget.getMode() == WidgetMode.SMALL;
        int span = small ? 1 : 2;
        int width = Math.min(Math.round((column * span - gap) * mWidthScale), Math.max(available, 0));
        int height = Math.round(cell * mHeightScale);
        fitContent(small, Math.min(Math.max(width, 0), height));
        mWidget.measure(MeasureSpec.makeMeasureSpec(Math.max(width, 0), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
        setMeasuredDimension(Math.max(width, 0), height);
    }

    private void fitContent(boolean small, int cell) {
        int gravity = small ? Gravity.CENTER : Gravity.CENTER_VERTICAL | Gravity.START;
        if (mWidget.getGravity() != gravity) mWidget.setGravity(gravity);
        int vertical;
        int horizontal;
        if (small) {
            vertical = Math.max(0, Math.round(cell * (1f - ARC_FILL) / 2f));
            horizontal = vertical;
            for (int i = 0; i < mWidget.getChildCount(); i++) {
                View child = mWidget.getChildAt(i);
                if (!(child instanceof ImageView image)) continue;
                image.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ViewGroup.LayoutParams params = child.getLayoutParams();
                if (params != null && params.width == ViewGroup.LayoutParams.MATCH_PARENT && params.height == ViewGroup.LayoutParams.MATCH_PARENT)
                    continue;
                child.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            }
        } else {
            vertical = mLiquidGlass ? dp2px(getContext(), GLASS_PADDING_DP) : 0;
            horizontal = mLiquidGlass ? dp2px(getContext(), BIG_PADDING_DP) : 0;
        }
        if (mWidget.getPaddingLeft() != horizontal || mWidget.getPaddingTop() != vertical) {
            mWidget.setPadding(horizontal, vertical, horizontal, vertical);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mWidget != null)
            mWidget.layout(0, 0, mWidget.getMeasuredWidth(), mWidget.getMeasuredHeight());
    }
}
