package it.dhd.oxygencustomizer.xposed.views.controls.devicewidget;

import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public interface DeviceWidgetInterface {

    BaseDeviceWidget getWidget();

    void setWidget(BaseDeviceWidget widget);

    void setMode(WidgetMode mode);

}
