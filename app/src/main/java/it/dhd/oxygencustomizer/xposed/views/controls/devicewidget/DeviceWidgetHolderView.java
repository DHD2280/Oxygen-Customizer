package it.dhd.oxygencustomizer.xposed.views.controls.devicewidget;

import android.content.Context;
import android.view.ViewGroup;

import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class DeviceWidgetHolderView extends BaseQsStaticView implements DeviceWidgetInterface {

    private final static String TAG = "DeviceWidgetHolderView: ";

    private final Context mContext;
    private Context appContext;
    private final boolean mSettingsInterface;
    private BaseDeviceWidget mWidget;

    public DeviceWidgetHolderView(Context context) {
        this(context, false);
    }

    public DeviceWidgetHolderView(Context context, boolean settingsInterface) {
        super(context);
        mSettingsInterface = settingsInterface;
        mContext = context;
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    @Override
    public BaseDeviceWidget getWidget() {
        return mWidget;
    }

    public void setWidget(BaseDeviceWidget widget) {
        mWidget = widget;
        addView(mWidget);
    }

    @Override
    public void setMode(WidgetMode mode) {
        if (mWidget == null) return;
        mWidget.setMode(mode);
    }

    public void setDeviceName(String name) {
        mWidget.setCustomDeviceName(name);
    }

    public void setColors(int progressColor, int textColor) {
        mWidget.setCustomColors(progressColor, textColor);
    }

}