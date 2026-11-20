package it.dhd.oxygencustomizer.xmorph.designer

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import it.dhd.oneplusui.preference.OplusSliderPreference
import it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_WEATHER_CUSTOM_FONT
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_BACKGROUND
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CENTERED
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_HUMIDITY
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_IMAGE_SIZE
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_CONDITION
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_LOCATION
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_TEXT_SIZE
import it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_WIND
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_DEVICE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_INACTIVE
import it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_STYLE
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayer
import it.dhd.oxygencustomizer.xmorph.domain.model.FaceLayerType
import it.dhd.oxygencustomizer.xmorph.view.FaceClockView
import it.dhd.oxygencustomizer.xposed.utils.WidgetFactory
import it.dhd.oxygencustomizer.xposed.views.CurrentWeatherView
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.DeviceWidgetCell
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.DeviceWidgetView
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.WidgetRowView
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.WidgetsController

class LockscreenHosts(
    private val context: Context,
    private val prefs: SharedPreferences,
    private val preview: Boolean
) : FaceClockView.HostProvider {

    private val controller: WidgetsController by lazy {
        if (preview) {
            WidgetsController.createPreview(context).also(::applyWidgetsStyle)
        } else {
            WidgetsController.getInstance() ?: WidgetsController.getInstance(context, null)
                .also(::applyWidgetsStyle)
        }
    }

    private val weatherNames = HashMap<View, String>()
    private val devices = ArrayList<DeviceWidgetView>()
    private val cells = ArrayList<DeviceWidgetCell>()
    private var live = 0

    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = Runnable { refresh() }

    private val prefsListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == null || key.contains(WEATHER_KEY) || key.startsWith(WIDGETS_KEY) || key.startsWith(
                DEVICE_WIDGET_KEY
            )
        ) {
            handler.removeCallbacks(refreshRunnable)
            handler.post(refreshRunnable)
        }
    }

    override fun create(layer: FaceLayer): View? = createView(layer)?.also {
        if (live++ == 0) prefs.registerOnSharedPreferenceChangeListener(prefsListener)
    }

    private fun createView(layer: FaceLayer): View? = when (layer.type) {
        FaceLayerType.WEATHER_CARD -> {
            val name = WEATHER_NAME + layer.id
            CurrentWeatherView(context, name).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                weatherNames[it] = name
                configureWeather(it, name)
            }
        }

        FaceLayerType.DEVICE_WIDGET -> DeviceWidgetView(context).also {
            devices += it
            configureDevice(it)
        }

        FaceLayerType.DEVICE_ITEM -> DeviceWidgetCell(context).also {
            cells += it
            it.setConfig(layer.format)
            it.setWidthScale(layer.aspect)
            it.setHeightScale(layer.hostHeight)
            styleCell(it)
        }

        FaceLayerType.WIDGET_BIG, FaceLayerType.WIDGET_SMALL ->
            WidgetRowView(context, controller, layer.type == FaceLayerType.WIDGET_BIG, 1).also {
                it.layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                it.setWidgets(widgetsOf(layer))
            }

        else -> null
    }

    override fun update(layer: FaceLayer, view: View) {
        when (view) {
            is WidgetRowView -> view.setWidgets(widgetsOf(layer))
            is CurrentWeatherView -> weatherNames[view]?.let { configureWeather(view, it) }
            is DeviceWidgetView -> configureDevice(view)
            is DeviceWidgetCell -> {
                view.setConfig(layer.format)
                view.setWidthScale(layer.aspect)
                view.setHeightScale(layer.hostHeight)
            }
        }
    }

    override fun aod(view: View, progress: Float) {
        if (view is CurrentWeatherView) view.setBackgroundVisibility(1f - progress)
    }

    override fun release(view: View) {
        when (view) {
            is WidgetRowView -> view.release()
            is CurrentWeatherView -> {
                weatherNames.remove(view)
                view.release()
            }

            is DeviceWidgetView -> devices -= view
            is DeviceWidgetCell -> cells -= view
        }
        if (--live == 0) {
            prefs.unregisterOnSharedPreferenceChangeListener(prefsListener)
            handler.removeCallbacks(refreshRunnable)
        }
    }

    private fun refresh() {
        if (preview) applyWidgetsStyle(controller)
        weatherNames.forEach { (view, name) -> configureWeather(view as CurrentWeatherView, name) }
        devices.forEach(::configureDevice)
        cells.forEach(::styleCell)
    }

    private fun styleCell(cell: DeviceWidgetCell) {
        cell.setStyle(
            prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS, false),
            prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false),
            prefs.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, Color.WHITE),
            prefs.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR, Color.WHITE),
            prefs.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, "") ?: ""
        )
    }

    private fun widgetsOf(layer: FaceLayer): List<String> = listOf(layer.format)

    private fun applyWidgetsStyle(controller: WidgetsController) {
        controller.setStyle(
            prefs.getString(LOCKSCREEN_WIDGETS_STYLE, "0")?.toIntOrNull() ?: 0,
            prefs.getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, false),
            prefs.getInt(LOCKSCREEN_WIDGETS_BIG_INACTIVE, Color.BLACK),
            prefs.getInt(LOCKSCREEN_WIDGETS_BIG_ACTIVE, Color.WHITE),
            prefs.getInt(LOCKSCREEN_WIDGETS_SMALL_INACTIVE, Color.BLACK),
            prefs.getInt(LOCKSCREEN_WIDGETS_SMALL_ACTIVE, Color.WHITE),
            prefs.getInt(LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE, Color.WHITE),
            prefs.getInt(LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE, Color.BLACK),
            prefs.getInt(LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE, Color.WHITE),
            prefs.getInt(LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE, Color.BLACK)
        )
    }

    private fun configureWeather(view: CurrentWeatherView, name: String) {
        val customColor = prefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH, false)
        view.updateSizes(
            OplusSliderPreference.getSingleIntValue(prefs, LOCKSCREEN_WEATHER_TEXT_SIZE, 16),
            OplusSliderPreference.getSingleIntValue(prefs, LOCKSCREEN_WEATHER_IMAGE_SIZE, 18),
            name
        )
        view.updateColors(
            if (customColor) prefs.getInt(
                LOCKSCREEN_WEATHER_CUSTOM_COLOR,
                Color.WHITE
            ) else Color.WHITE, name
        )
        view.updateWeatherSettings(
            prefs.getBoolean(LOCKSCREEN_WEATHER_SHOW_LOCATION, true),
            prefs.getBoolean(LOCKSCREEN_WEATHER_SHOW_CONDITION, true),
            prefs.getBoolean(LOCKSCREEN_WEATHER_HUMIDITY, true),
            prefs.getBoolean(LOCKSCREEN_WEATHER_WIND, true),
            name
        )
        view.updateWeatherBg(
            prefs.getString(LOCKSCREEN_WEATHER_BACKGROUND, "0")?.toIntOrNull() ?: 0, name
        )
        view.setCustomFont(
            prefs.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_FONT_SWITCH, false),
            LOCKSCREEN_WEATHER_CUSTOM_FONT,
            name
        )
        view.setWeatherCentered(prefs.getBoolean(LOCKSCREEN_WEATHER_CENTERED, false))
    }

    private fun configureDevice(view: DeviceWidgetView) {
        view.setDeviceWidgetStyle(
            prefs.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE, "0")?.toIntOrNull() ?: 0
        )
        view.setLiquidGlass(
            Build.VERSION.SDK_INT >= 37 && prefs.getBoolean(
                LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LIQUID_GLASS,
                false
            )
        )
        view.setCustomColor(
            prefs.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false),
            prefs.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR, Color.WHITE),
            prefs.getInt(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR, Color.WHITE)
        )
        view.setTextCustomColor(
            prefs.getInt(
                LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
                Color.WHITE
            )
        )
        view.setDeviceName(prefs.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE, "") ?: "")
        view.setDeviceWidgetScaling(1f)
        view.setWidgets(
            WidgetFactory.fromJson(
                prefs.getString(
                    LOCKSCREEN_WIDGETS_CUSTOM_DEVICE,
                    "[]"
                ) ?: "[]", context, false
            )
        )
    }

    companion object {
        private const val WEATHER_NAME = "designer_weather_"
        private const val WEATHER_KEY = "weather"
        private const val WIDGETS_KEY = "lockscreen_widget"
        private const val DEVICE_WIDGET_KEY = "lockscreen_device_widget"
    }
}
