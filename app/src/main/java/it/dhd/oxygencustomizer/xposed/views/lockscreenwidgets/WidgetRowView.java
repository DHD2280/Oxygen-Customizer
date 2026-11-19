package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets;

import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableImageView;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.LaunchableLinearLayout;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.QsTileTouchAnim;

@SuppressLint("ViewConstructor")
public class WidgetRowView extends FrameLayout {

    private final WidgetsController mController;
    private final boolean mBig;
    private final int mMaxItems;
    private final List<String> mWidgets = new ArrayList<>();
    private float mScale = 1f;
    private final QsTileTouchAnim mTouchAnim = new QsTileTouchAnim();
    private View mPressed;

    public WidgetRowView(Context context, WidgetsController controller, boolean big, int maxItems) {
        super(context);
        mController = controller;
        mBig = big;
        mMaxItems = maxItems;
        mController.addRow(this);
        rebuild();
    }

    public void setWidgets(List<String> widgets) {
        mWidgets.clear();
        for (String widget : widgets) {
            if (TextUtils.isEmpty(widget) || "none".equals(widget)) continue;
            if (mMaxItems > 0 && mWidgets.size() >= mMaxItems) break;
            mWidgets.add(widget);
        }
        rebuild();
    }

    public void setScale(float scale) {
        mScale = scale;
        rebuild();
    }

    public float getScale() {
        return mScale;
    }

    public boolean isBig() {
        return mBig;
    }

    public boolean isEmpty() {
        return mWidgets.isEmpty();
    }

    public void release() {
        mController.removeRow(this);
        removeAllViews();
    }

    void rebuild() {
        mController.unbind(this);
        removeAllViews();
        LinearLayout container = createContainer();
        addView(container);
        for (String type : mWidgets) {
            if (mBig) {
                ExtendedFAB fab = createFAB();
                container.addView(fab);
                applyFabLayout(fab);
                mController.bind(this, type, null, fab);
            } else {
                ImageView iv = createImageView();
                container.addView(iv);
                applyIconLayout(iv);
                mController.bind(this, type, iv, null);
            }
        }
        setVisibility(mWidgets.isEmpty() ? GONE : VISIBLE);
        if (isAttachedToWindow()) mController.onVisible();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mController.onVisible();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN) mPressed = buttonAt(event.getX(), event.getY());
        if (mPressed != null) mTouchAnim.doAction(action, mPressed);
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) mPressed = null;
        return super.dispatchTouchEvent(event);
    }

    private View buttonAt(float x, float y) {
        if (getChildCount() == 0 || !(getChildAt(0) instanceof ViewGroup container)) return null;
        float localX = x - container.getLeft();
        float localY = y - container.getTop();
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child.getVisibility() == VISIBLE && child.isClickable()
                    && localX >= child.getLeft() && localX < child.getRight()
                    && localY >= child.getTop() && localY < child.getBottom()) return child;
        }
        return null;
    }

    private LinearLayout createContainer() {
        LinearLayout container;
        try {
            container = (LinearLayout) LaunchableLinearLayout.getConstructor(Context.class).newInstance(getContext());
        } catch (Throwable t) {
            container = new LinearLayout(getContext());
        }
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setGravity(mBig ? Gravity.CENTER : Gravity.CENTER_HORIZONTAL);
        container.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        return container;
    }

    private ExtendedFAB createFAB() {
        ExtendedFAB fab = new ExtendedFAB(getContext());
        fab.setId(View.generateViewId());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) (mController.mFabHeight * mScale), 1f);
        params.gravity = Gravity.CENTER;
        params.setMargins((int) (mController.mFabMarginStart * mScale), 0, (int) (mController.mFabMarginEnd * mScale), 0);
        fab.setLayoutParams(params);
        int padding = (int) (mController.mFabPadding * mScale);
        fab.setPadding(padding, padding, padding, padding);
        fab.setGravity(Gravity.CENTER);
        fab.setMaxLines(2);
        return fab;
    }

    private void applyFabLayout(ExtendedFAB fab) {
        fab.setElevation(0);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) fab.getLayoutParams();
        if (mWidgets.size() == 1) {
            params.width = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_width);
            params.height = modRes.getDimensionPixelSize(R.dimen.kg_widget_main_height);
        } else {
            params.width = 0;
            params.weight = 1;
        }
        if (mController.isGlass()) {
            params.height = (int) (dp2px(getContext(), 58) * mScale);
            int padding = (int) (dp2px(getContext(), 16) * mScale);
            fab.setPadding(padding, 0, padding, 0);
            fab.setMaxLines(1);
            fab.setEllipsize(TextUtils.TruncateAt.END);
        }
        fab.setLayoutParams(params);
        fab.setMaxWidth((int) (mController.mFabWidth * mScale));
    }

    private ImageView createImageView() {
        ImageView imageView;
        try {
            imageView = (ImageView) LaunchableImageView.getConstructor(Context.class).newInstance(getContext());
        } catch (Throwable t) {
            imageView = new ImageView(getContext());
        }
        imageView.setId(View.generateViewId());
        int size = (int) (mController.mWidgetCircleSize * mScale);
        int margin = (int) (mController.mWidgetMarginHorizontal * mScale);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, 0, margin, 0);
        imageView.setLayoutParams(params);
        int padding = (int) (mController.mWidgetIconPadding * mScale);
        imageView.setPadding(padding, padding, padding, padding);
        imageView.setFocusable(true);
        imageView.setClickable(true);
        return imageView;
    }

    private void applyIconLayout(ImageView iv) {
        if (mController.isGlass()) {
            int size = (int) (dp2px(getContext(), 58) * mScale);
            int margin = (int) (dp2px(getContext(), 10) * mScale);
            int padding = (int) (dp2px(getContext(), 15) * mScale);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) iv.getLayoutParams();
            params.width = size;
            params.height = size;
            params.setMargins(margin, 0, margin, 0);
            iv.setLayoutParams(params);
            iv.setPadding(padding, padding, padding, padding);
            iv.setBackground(null);
            return;
        }
        int bgRes = mController.getStyle() == 1 ? R.drawable.lockscreen_widget_background_square : R.drawable.lockscreen_widget_background_circle;
        iv.setBackground(ResourcesCompat.getDrawable(modRes, bgRes, getContext().getTheme()));
    }
}
