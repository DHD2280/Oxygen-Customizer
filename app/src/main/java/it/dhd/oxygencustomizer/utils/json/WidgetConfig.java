package it.dhd.oxygencustomizer.utils.json;


import com.google.gson.annotations.SerializedName;

import java.util.List;

import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class WidgetConfig {

    @SerializedName("widget_type")
    private String widgetType;

    @SerializedName("widget_mode")
    private int widgetMode;

    @SerializedName("settings")
    private List<SettingItem> settings;

    public WidgetConfig(String widgetType, WidgetMode mode, List<SettingItem> settings) {
        this.widgetType = widgetType;
        this.widgetMode = mode.ordinal();
        this.settings = settings;
    }

    public String getWidgetType() { return widgetType; }
    public int getWidgetMode() { return widgetMode; }
    public List<SettingItem> getSettings() { return settings; }

    public WidgetMode getWidgetModeOrdinal(int intMode) {
        for (WidgetMode mode : WidgetMode.values()) {
            if (mode.ordinal() == intMode) {
                return mode;
            }
        }
        return WidgetMode.BIG;
    }
}
