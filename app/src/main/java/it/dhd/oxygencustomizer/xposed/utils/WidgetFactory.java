package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.utils.OCPreferences;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.utils.json.WidgetConfig;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BaseDeviceWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BatteryWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.BluetoothWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.CalendarWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.SoundWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WeatherWidget;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetMode;

public class WidgetFactory {

    private final static String PREF_KEY = LOCKSCREEN_WIDGETS_CUSTOM_DEVICE;

    public static BaseDeviceWidget getNewWidget(Context context, WidgetConfig config, boolean settingsInterface) {
        String ref = config.getWidgetType();
        WidgetMode mode = config.getWidgetModeOrdinal(config.getWidgetMode());
        List<SettingItem> configs = config.getSettings();
        Map<String, Object> maps = new HashMap<>();
        for (SettingItem setting : configs) {
            maps.put(setting.getKey(), setting.getValue());
        }
        return getNewWidget(context, ref, mode, settingsInterface, maps);
    }

    public static BaseDeviceWidget getNewWidget(Context context, String reference) {
        return getNewWidget(context, reference, WidgetMode.BIG, true);
    }

    public static BaseDeviceWidget getNewWidget(Context context, String reference, WidgetMode mode) {
        return getNewWidget(context, reference, mode, true);
    }

    public static BaseDeviceWidget getNewWidget(Context context, String reference, WidgetMode mode, boolean settingsInterface) {
        return getNewWidget(context, reference, mode, settingsInterface, Collections.emptyMap());
    }

    public static BaseDeviceWidget getNewWidget(Context context, String reference, WidgetMode mode, boolean settingsInterface, Map<String, Object> settings) {
        BaseDeviceWidget widget = switch (reference) {
            case "batteryWidget" -> new BatteryWidget(context, settingsInterface);
            case "WeatherWidget" -> new WeatherWidget(context, settingsInterface);
            case "CalendarWidget" -> new CalendarWidget(context, settingsInterface);
            case "BluetoothWidget" -> new BluetoothWidget(context, settingsInterface);
            case "SoundWidget" -> new SoundWidget(context, settingsInterface);
            default -> null;
        };
        if (widget != null) {
            widget.setMode(mode);
            widget.applySettings(settings);
        }
        return widget;
    }

    public static List<BaseDeviceWidget> fromJson(String jsonString, Context context, boolean settingsInterface) {

        Gson gson = new Gson();
        Type listType = new TypeToken<List<WidgetConfig>>() {}.getType();
        List<WidgetConfig> configList = gson.fromJson(jsonString, listType);

        List<BaseDeviceWidget> widgets = new ArrayList<>();
        if (configList != null) {
            for (WidgetConfig config : configList) {
                BaseDeviceWidget widget = WidgetFactory.getNewWidget(context, config, settingsInterface);
                if (widget != null) {
                    widgets.add(widget);
                }
            }
        }

        Log.d("WidgetFactory", "Widget caricati: " + widgets.size());
        return widgets;
    }

    public static void saveWidgets(List<BaseDeviceWidget> widgets) {
        Gson gson = new Gson();
        List<WidgetConfig> configList = new ArrayList<>();

        for (BaseDeviceWidget widget : widgets) {
            configList.add(new WidgetConfig(
                    widget.getWidgetReference(),
                    widget.getMode(),
                    widget.getCustomSettings()
            ));
        }

        String jsonString = gson.toJson(configList);
        OCPreferences.getPrefs().edit()
                .putString(PREF_KEY, jsonString)
                .apply();

        Log.w("WidgetFactory", "#saveWidgets: " + jsonString);
    }

}
