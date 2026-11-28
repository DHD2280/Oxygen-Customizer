package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets;

import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.WidgetFactory;

@SuppressLint("ViewConstructor")
public class LockscreenWidgetsView extends LinearLayout {

    private static final int MAX_MAIN_WIDGETS = 2;
    private static final int MAX_SECONDARY_WIDGETS = 4;

    @SuppressLint("StaticFieldLeak")
    public static LockscreenWidgetsView instance = null;

    private final Context mContext;
    private final WidgetsController mController;
    private final LinearLayout mDeviceWidgetContainer;
    private final DeviceWidgetView mDeviceWidgetView;
    private final WidgetRowView mMainRow;
    private final WidgetRowView mSecondaryRow;

    private boolean lockscreenWidgetsEnabled = false;
    private boolean deviceWidgetsEnabled = false;
    private float mWidgetsScale = 1f;

    public LockscreenWidgetsView(Context context, Object activityStarter) {
        super(context);
        instance = this;
        mContext = context;
        mController = WidgetsController.getInstance(context, activityStarter);
        setOrientation(VERTICAL);

        mDeviceWidgetContainer = new LinearLayout(context);
        mDeviceWidgetContainer.setOrientation(HORIZONTAL);
        mDeviceWidgetContainer.setGravity(Gravity.CENTER);
        mDeviceWidgetContainer.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        mDeviceWidgetView = new DeviceWidgetView(context);
        mDeviceWidgetContainer.addView(mDeviceWidgetView);
        addView(mDeviceWidgetContainer);

        mMainRow = new WidgetRowView(context, mController, true, MAX_MAIN_WIDGETS);
        mMainRow.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addView(mMainRow);

        mSecondaryRow = new WidgetRowView(context, mController, false, MAX_SECONDARY_WIDGETS);
        LayoutParams secondaryParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        secondaryParams.topMargin = modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_vertical);
        secondaryParams.bottomMargin = modRes.getDimensionPixelSize(R.dimen.kg_widget_margin_bottom);
        mSecondaryRow.setLayoutParams(secondaryParams);
        addView(mSecondaryRow);

        updateContainerVisibility();
    }

    public static LockscreenWidgetsView getInstance(Context context, Object activityStarter) {
        if (instance != null) return instance;
        return new LockscreenWidgetsView(context, activityStarter);
    }

    public static LockscreenWidgetsView getInstance() {
        return instance;
    }

    public void setOptions(boolean lsWidgets, boolean deviceWidget, String mainWidgets, String secondaryWidgets) {
        lockscreenWidgetsEnabled = lsWidgets;
        deviceWidgetsEnabled = deviceWidget;
        mMainRow.setWidgets(split(mainWidgets));
        mSecondaryRow.setWidgets(split(secondaryWidgets));
        updateContainerVisibility();
    }

    public void setDeviceWidgetOptions(int deviceWidgetStyle, boolean liquidGlass, boolean customColor, int linearColor, int circularColor, int textColor, String devName, String customWidgets) {
        mDeviceWidgetView.setDeviceWidgetStyle(deviceWidgetStyle);
        mDeviceWidgetView.setLiquidGlass(liquidGlass);
        mDeviceWidgetView.setCustomColor(customColor, linearColor, circularColor);
        mDeviceWidgetView.setTextCustomColor(textColor);
        mDeviceWidgetView.setDeviceName(devName);
        mDeviceWidgetView.setDeviceWidgetScaling(mWidgetsScale);
        mDeviceWidgetView.setWidgets(WidgetFactory.fromJson(customWidgets, mContext, false));
        updateContainerVisibility();
    }

    public void setCustomColors(int widgetsStyle, boolean customColorsEnabled,
                                int bigInactive, int bigActive, int smallInactive, int smallActive,
                                int bigIconInactive, int bigIconActive, int smallIconInactive, int smallIconActive) {
        mController.setStyle(widgetsStyle, customColorsEnabled, bigInactive, bigActive, smallInactive, smallActive,
                bigIconInactive, bigIconActive, smallIconInactive, smallIconActive);
    }

    public void setScale(float scale) {
        mWidgetsScale = scale;
        mDeviceWidgetView.setDeviceWidgetScaling(scale);
        mMainRow.setScale(scale);
        mSecondaryRow.setScale(scale);
    }

    public void setActivityStarter(Object activityStarter) {
        mController.setActivityStarter(activityStarter);
    }

    private static List<String> split(String widgets) {
        if (TextUtils.isEmpty(widgets)) return Collections.emptyList();
        return Arrays.asList(widgets.split(","));
    }

    private void updateContainerVisibility() {
        boolean showDevice = deviceWidgetsEnabled && !mDeviceWidgetView.getWidgetsList().isEmpty();
        mDeviceWidgetContainer.setVisibility(showDevice ? View.VISIBLE : View.GONE);
        boolean isEmpty = !showDevice && mMainRow.isEmpty() && mSecondaryRow.isEmpty();
        setVisibility(isEmpty || !lockscreenWidgetsEnabled ? View.GONE : View.VISIBLE);
    }

    @Override
    protected void onVisibilityChanged(@NonNull View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE && isAttachedToWindow()) {
            mController.onVisible();
        }
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (lockscreenWidgetsEnabled && visibility == View.VISIBLE) {
            mController.onVisible();
        }
    }
}
