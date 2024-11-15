package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.AppUtils.doesClassExist;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_BACKGROUND;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CENTERED;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR_CAT;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_HUMIDITY;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_IMAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_CONDITION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SHOW_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_TEXT_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_UI_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.LockscreenWeather.LOCKSCREEN_WEATHER_WIND;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_DEVICE_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CENTERED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_COLOR_CAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_CUSTOM_MARGIN_TOP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_HUMIDITY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_IMAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SHOW_CONDITION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SHOW_LOCATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_TEXT_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_UI_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodWeather.AOD_WEATHER_WIND;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_CIRCLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DEFAULT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DEFAULT_LANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DEFAULT_RLANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DOTTED_CIRCLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_FILLED_CIRCLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYJ;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYM;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_IOS_16;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOMIZE_BATTERY_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_BLEND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HIDE_PERCENTAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_WIDTH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_DEVICE_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_DATE_FORMAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_BIG_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_EXTRAS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SCALE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_WEATHER_SETTINGS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_FORMAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_HIDE_DATE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_RED_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_ALPHA;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_BOTTOM_FADE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_HEIGHT_PORTRAIT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_LANDSCAPE_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_PADDING_SIDE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_PADDING_TOP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_TINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_URI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderImage.QS_HEADER_IMAGE_ZOOM_TO_FIT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_FILTER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_SHOW_ALBUM_ART;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_DURATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_INTERPOLATOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_TRANSFORMATIONS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_TRANSFORMATIONS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIDE_LABELS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_LABELS_CUSTOM_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.BLUR_RADIUS_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_BLUR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_VAL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CUSTOMIZE_CLEAR_BUTTON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_OWM_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_PROVIDER;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_YANDEX_KEY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import it.dhd.oneplusui.preference.OplusSliderPreference;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedSharedPreferences;

public class PreferenceHelper {
    public static boolean showOverlays, showFonts;

    public final ExtendedSharedPreferences mPreferences;

    public static PreferenceHelper instance;

    private final List<Integer> LsClockDateFormat = new ArrayList<>() {{
        addAll(Arrays.asList(1, 2, 3, 4, 6, 7, 8, 9, 10, 11, 12, 13, 15, 16, 17, 18, 19, 20, 21, 24, 25, 26, 28));
    }};

    private final List<Integer> QsClockDateFormat = new ArrayList<>() {{
        addAll(Arrays.asList(1, 2, 3, 4, 7));
    }};

    private final List<Integer> LsClockUserImageVisible = new ArrayList<>() {{
        addAll(Arrays.asList(7, 29, 30, 32, 35, 36, 37, 42, 56, 57, 58, 59));
    }};

    private final List<Integer> LsClockCustomImageVisible = new ArrayList<>() {{
       addAll(Arrays.asList(25, 39, 40));
    }};

    private final List<Integer> LsClockUserVisible = new ArrayList<>() {{
        addAll(Arrays.asList(7, 32, 35, 42, 48, 50, 53, 58, 59));
    }};

    private final List<Integer> LsClockDeviceVisible = new ArrayList<>() {{
        addAll(Arrays.asList(19, 32, 47));
    }};

    public static void init(ExtendedSharedPreferences prefs) {
        new PreferenceHelper(prefs);
    }

    private PreferenceHelper(ExtendedSharedPreferences prefs) {
        mPreferences = prefs;

        instance = this;
    }

    public static SharedPreferences getModulePrefs() {
        if (instance != null) return instance.mPreferences;
        return null;
    }

    public static boolean isVisible(String key) {
        if (instance == null) return true;

        switch (key) {
            // Status Bar Prefs
            // Battery Bar
            case "batteryFastChargingColor",
                 "batteryChargingColor",
                 "batteryWarningColor",
                 "batteryCriticalColor",
                 "batteryPowerSaveColor" -> {

                boolean critZero = false, warnZero = false;
                List<Float> BBarLevels = instance.mPreferences.getSliderValues("batteryWarningRange", 0);

                if (!BBarLevels.isEmpty()) {
                    critZero = BBarLevels.get(0) == 0;
                    warnZero = BBarLevels.get(1) == 0;
                }
                boolean bBarEnabled = instance.mPreferences.getBoolean("BBarEnabled", false);
                boolean transitColors = instance.mPreferences.getBoolean("BBarTransitColors", false);

                return switch (key) {
                    case "batteryFastChargingColor" ->
                            instance.mPreferences.getBoolean("indicateFastCharging", false) && bBarEnabled;
                    case "batteryChargingColor" ->
                            instance.mPreferences.getBoolean("indicateCharging", false) && bBarEnabled;
                    case "batteryPowerSaveColor" ->
                            instance.mPreferences.getBoolean("indicatePowerSave", false) && bBarEnabled;
                    case "batteryWarningColor" -> !warnZero && bBarEnabled;
                    default ->  //batteryCriticalColor
                            (!critZero || transitColors) && bBarEnabled && !warnZero;
                };
            }
            case "BBarTransitColors",
                 "BBarColorful",
                 "BBOnlyWhileCharging",
                 "BBOnBottom",
                 "BBOpacity",
                 "BBarHeight",
                 "BBSetCentered",
                 "BBAnimateCharging",
                 "indicateCharging",
                 "indicateFastCharging",
                 "indicatePowerSave",
                 "batteryWarningRange" -> {
                return instance.mPreferences.getBoolean("BBarEnabled", false);
            }
            // Launcher Prefs
            case "folder_rows", "folder_columns", "rearrange_preview" -> {
                return instance.mPreferences.getBoolean("rearrange_folder", false);
            }
            case "drawer_columns" -> {
                return instance.mPreferences.getBoolean("rearrange_drawer", false);
            }
            case "launcher_max_columns", "launcher_max_rows" -> {
                return instance.mPreferences.getBoolean("rearrange_home", false);
            }

            // Statusbar Prefs
            case "statusbarPaddings", "statusbar_top_padding" -> {
                return instance.mPreferences.getBoolean("statusbar_padding_enabled", false);
            }
            case "status_bar_clock_color" -> {
                return instance.mPreferences.getBoolean("status_bar_custom_clock_color", false);
            }

            // Clock & date
            case "status_bar_clock_auto_hide_hduration", "status_bar_clock_auto_hide_sduration" -> {
                return instance.mPreferences.getBoolean("status_bar_clock_auto_hide", false);
            }
            case "status_bar_clock_date_position", "status_bar_clock_date_style",
                 "status_bar_clock_date_format" -> {
                return !instance.mPreferences.getString("status_bar_clock_date_display", "0").equals("0");
            }
            case "status_bar_clock_background_chip" -> {
                return instance.mPreferences.getBoolean("status_bar_clock_background_chip_switch", false);
            }
            case "status_bar_java_custom" -> {
                return !instance.mPreferences.getString("status_bar_clock_date_display", "0").equals("0") &&
                        instance.mPreferences.getString("status_bar_clock_date_format", "$GEEE").equals("custom");
            }

            // Notification
            case "statusbar_notification_transparency_value" -> {
                return instance.mPreferences.getBoolean("statusbar_notification_transparency", false);
            }
            case CLEAR_BUTTON_BG_LINK_ACCENT,
                 CLEAR_BUTTON_ICON_LINK_ACCENT -> {
                return instance.mPreferences.getBoolean(CUSTOMIZE_CLEAR_BUTTON, false);
            }
            case CLEAR_BUTTON_BG_COLOR -> {
                return instance.mPreferences.getBoolean(CUSTOMIZE_CLEAR_BUTTON, false) &&
                        !instance.mPreferences.getBoolean(CLEAR_BUTTON_BG_LINK_ACCENT, false);
            }
            case CLEAR_BUTTON_ICON_COLOR -> {
                return instance.mPreferences.getBoolean(CUSTOMIZE_CLEAR_BUTTON, false) &&
                        !instance.mPreferences.getBoolean(CLEAR_BUTTON_ICON_LINK_ACCENT, false);
            }


            // Battery Icon
            case "battery_icon_style",
                 "category_battery_icon_settings",
                 "battery_hide_percentage",
                 "battery_inside_percentage",
                 "battery_hide_battery",
                 "battery_reverse_layout",
                 "battery_rotate_layout",
                 "category_battery_colors",
                 "battery_perimeter_alpha",
                 "battery_fill_alpha",
                 "battery_rainbow_color",
                 "battery_blend_color",
                 "battery_fill_color",
                 "battery_fill_gradient_color",
                 "battery_charging_fill_color",
                 "battery_fast_charging_fill_color",
                 "battery_powersave_fill_color",
                 "battery_powersave_icon_color",
                 "category_battery_margins",
                 "category_battery_charging_icon" -> {
                return isVisibleBattery(key);
            }
            case "category_battery_stock_prefs" -> {
                return !instance.mPreferences.getBoolean(CUSTOMIZE_BATTERY_ICON, false);
            }
            case "stock_percentage_size" -> {
                return !instance.mPreferences.getBoolean(CUSTOMIZE_BATTERY_ICON, false) &&
                        instance.mPreferences.getBoolean("customize_stock_percentage_size", false);
            }

            // QuickSettings Prefs
            case "quick_pulldown_side", "quick_pulldown_length" -> {
                return instance.mPreferences.getBoolean("quick_pulldown", false);
            }
            case QS_TRANSPARENCY_VAL -> {
                return instance.mPreferences.getBoolean(QS_TRANSPARENCY_SWITCH, false);
            }
            case BLUR_RADIUS_VALUE -> {
                return instance.mPreferences.getBoolean(QSPANEL_BLUR_SWITCH, false);
            }

            // Qs Tiles
            case "quick_settings_quick_tiles",
                 "qs_tile_potrait",
                 "qs_tile_landscape" -> {
                return instance.mPreferences.getBoolean("quick_settings_tiles_customize", false);
            }

            // Qs Appearance
            case "qs_tile_active_color" -> {
                return instance.mPreferences.getBoolean("qs_tile_active_color_enabled", false);
            }
            case "qs_tile_inactive_color" -> {
                return instance.mPreferences.getBoolean("qs_tile_inactive_color_enabled", false);
            }
            case "qs_tile_disabled_color" -> {
                return instance.mPreferences.getBoolean("qs_tile_disabled_color_enabled", false);
            }
            case "brightness_slider_progress_color_mode" -> {
                return instance.mPreferences.getBoolean("customize_brightness_slider", false);
            }
            case "brightness_slider_color" -> {
                return instance.mPreferences.getBoolean("customize_brightness_slider", false) &&
                        instance.mPreferences.getString("brightness_slider_progress_color_mode", "0").equals("2");
            }
            case "brightness_slider_background_color" -> {
                return instance.mPreferences.getBoolean("brightness_slider_background_color_enabled", false);
            }
            case QS_TILE_ANIMATION_INTERPOLATOR,
                 QS_TILE_ANIMATION_DURATION -> {
                return !instance.mPreferences.getString(QS_TILE_ANIMATION_STYLE, "0").equals("0");
            }
            case QS_TILE_ANIMATION_TRANSFORMATIONS -> {
                return instance.mPreferences.getBoolean(QS_TILE_ANIMATION_TRANSFORMATIONS_SWITCH, false);
            }
            case "qs_tile_label" -> {
                return instance.mPreferences.getBoolean("qs_tile_label_enabled", false);
            }
            case QS_MEDIA_ART_FILTER -> {
                return instance.mPreferences.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false);
            }
            case QS_MEDIA_ART_BLUR_AMOUNT -> {
                return instance.mPreferences.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false) &&
                        (instance.mPreferences.getString(QS_MEDIA_ART_FILTER, "0").equals("3") ||
                                instance.mPreferences.getString(QS_MEDIA_ART_FILTER, "0").equals("4"));
            }
            case QS_MEDIA_ART_TINT_COLOR,
                 QS_MEDIA_ART_TINT_AMOUNT -> {
                return instance.mPreferences.getBoolean(QS_MEDIA_SHOW_ALBUM_ART, false) &&
                        (instance.mPreferences.getString(QS_MEDIA_ART_FILTER, "0").equals("5"));
            }

            // Tile Radius
            case QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT,
                 QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT,
                 QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT,
                 QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT -> {
                return instance.mPreferences.getBoolean(QS_TILE_HIGHTLIGHT_RADIUS, false);
            }
            case QS_TILE_RADIUS_TOP_LEFT,
                 QS_TILE_RADIUS_TOP_RIGHT,
                 QS_TILE_RADIUS_BOTTOM_LEFT,
                 QS_TILE_RADIUS_BOTTOM_RIGHT -> {
                return instance.mPreferences.getBoolean(QS_TILE_RADIUS, false);
            }

            // Gesture Prefs
            case "gesture_left_height_double" -> {
                return instance.mPreferences.getBoolean("gesture_left", false);
            }
            case "gesture_right_height_double" -> {
                return instance.mPreferences.getBoolean("gesture_right", false);
            }
            case "gesture_override_holdback_left", "gesture_override_holdback_mode" -> {
                return instance.mPreferences.getBoolean("gesture_override_holdback", false);
            }
            case "gesture_override_holdback_right" -> {
                String mode = instance.mPreferences.getString("gesture_override_holdback_mode", "0");
                return mode.equals("1") && instance.mPreferences.getBoolean("gesture_override_holdback", false);
            }

            // Header Image
            // Header Image
            case QS_HEADER_IMAGE_TINT,
                 QS_HEADER_IMAGE_ALPHA,
                 QS_HEADER_IMAGE_BOTTOM_FADE,
                 QS_HEADER_IMAGE_HEIGHT_PORTRAIT,
                 QS_HEADER_IMAGE_LANDSCAPE_ENABLED,
                 QS_HEADER_IMAGE_PADDING_SIDE,
                 QS_HEADER_IMAGE_PADDING_TOP,
                 QS_HEADER_IMAGE_URI,
                 QS_HEADER_IMAGE_ZOOM_TO_FIT -> {
                return instance.mPreferences.getBoolean(QS_HEADER_IMAGE_ENABLED, false);
            }
            case "qs_header_image_tint_custom" -> {
                return isVisible(QS_HEADER_IMAGE_TINT) && instance.mPreferences.getString("qs_header_image_tint", "0").equals("4");
            }
            case "qs_header_image_tint_intensity" -> {
                return isVisible(QS_HEADER_IMAGE_TINT) && !instance.mPreferences.getString("qs_header_image_tint", "0").equals("0");
            }

            // Header Clock
            case "qs_header_stock_clock_prefs",
                 "qs_header_stock_clock_red_one_mode",
                 "qs_header_stock_clock_date_hide",
                 "qs_header_stock_clock_background_chip_switch",
                 "qs_header_stock_clock_hide_carrier_label" -> {
                return !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }

            case "qs_header_stock_clock_date_custom_color_switch" -> {
                return !instance.mPreferences.getBoolean("qs_header_stock_clock_date_hide", false) &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case "qs_header_clock_custom",
                 "qs_header_clock_prefs", "qs_header_clock_custom_margins" -> {
                return instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR -> {
                return instance.mPreferences.getString(QS_HEADER_CLOCK_STOCK_RED_MODE, "0").equals("3") &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case "qs_header_stock_clock_custom_color" -> {
                return instance.mPreferences.getBoolean("qs_header_stock_clock_custom_color_switch", false) &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case "qs_header_stock_clock_date_custom_color" -> {
                return instance.mPreferences.getBoolean("qs_header_stock_clock_date_custom_color_switch", false) &&
                        !instance.mPreferences.getBoolean("qs_header_stock_clock_date_hide", false) &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case "qs_header_clock_font_custom" -> {
                return instance.mPreferences.getBoolean("qs_header_clock_custom_font", false);
            }
            case QS_HEADER_CLOCK_COLOR_CODE_ACCENT1,
                 QS_HEADER_CLOCK_COLOR_CODE_ACCENT2,
                 QS_HEADER_CLOCK_COLOR_CODE_ACCENT3,
                 QS_HEADER_CLOCK_COLOR_CODE_TEXT1,
                 QS_HEADER_CLOCK_COLOR_CODE_TEXT2 -> {
                return instance.mPreferences.getBoolean(QS_HEADER_CLOCK_CUSTOM_COLOR_SWITCH, false);
            }
            case QS_HEADER_CLOCK_CUSTOM_FORMAT -> {
                return instance.QsClockDateFormat.contains(instance.mPreferences.getInt(QS_HEADER_CLOCK_CUSTOM_VALUE, 0));
            }
            case "qs_header_clock_custom_user_image" -> {
                return instance.mPreferences.getInt(QS_HEADER_CLOCK_CUSTOM_VALUE, 0) == 6;
            }
            case "qs_header_clock_custom_user_image_picker" -> {
                return instance.mPreferences.getInt(QS_HEADER_CLOCK_CUSTOM_VALUE, 0) == 6 &&
                        instance.mPreferences.getBoolean("qs_header_clock_custom_user_image", false);
            }
            case "qs_header_stock_clock_background_chip" -> {
                return instance.mPreferences.getBoolean("qs_header_stock_clock_background_chip_switch", false) &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case "qs_header_stock_date_background_chip_switch" -> {
                return !instance.mPreferences.getBoolean(QS_HEADER_CLOCK_STOCK_HIDE_DATE, false) &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }
            case "qs_header_stock_date_background_chip" -> {
                return !instance.mPreferences.getBoolean(QS_HEADER_CLOCK_STOCK_HIDE_DATE, false) &&
                        instance.mPreferences.getBoolean("qs_header_stock_date_background_chip_switch", false) &&
                        !instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
            }

            // Pulse Prefs
            case "pulse_color_user" -> {
                return instance.mPreferences.getString("pulse_color_mode", "2").equals("1");
            }

            // Lockscreen Prefs
            case "lockscreen_fp_icon_custom",
                 "lockscreen_fp_icon_picker",
                 LOCKSCREEN_FINGERPRINT_SCALING -> {
                return instance.mPreferences.getBoolean("lockscreen_fp_custom_icon", false);
            }
            case "DWCategory", "DWallpaperEnabled" -> {
                return Build.VERSION.SDK_INT >= 34;
            }
            case "DWOpacity", "DWMode", "DWAIStatus" -> {
                return Build.VERSION.SDK_INT >= 34 && instance.mPreferences.getBoolean("DWallpaperEnabled", false);
            }
            case "DWBackground", "DWSubject" -> {
                return Build.VERSION.SDK_INT >= 34 && instance.mPreferences.getBoolean("DWallpaperEnabled", false) &&
                        instance.mPreferences.getString("DWMode", "0").equals("1");
            }
            case "DWShowOnAod" -> {
                return isVisible("DWallpaperEnabled") &&
                        instance.mPreferences.getBoolean("DWallpaperEnabled", false);
            }
            case "DWAodOpacity" -> {
                return isVisible("DWShowOnAod") &&
                        instance.mPreferences.getBoolean("DWShowOnAod", false);
            }
            case "lockscreen_album_art_category" -> {
                return Build.VERSION.SDK_INT >= 34;
            }
            case "lockscreen_album_art_filter" -> {
                return Build.VERSION.SDK_INT >= 34 && instance.mPreferences.getBoolean("lockscreen_album_art", false);
            }
            case "lockscreen_media_blur" -> {
                return Build.VERSION.SDK_INT >= 34 && instance.mPreferences.getBoolean("lockscreen_album_art", false) &&
                        (instance.mPreferences.getString("lockscreen_album_art_filter", "0").equals("3") ||
                                instance.mPreferences.getString("lockscreen_album_art_filter", "0").equals("4"));
            }

            // Lockscreen Clock
            case "lockscreen_clock_custom", "lockscreen_clock_prefs",
                 "lockscreen_clock_custom_margins", "lockscreen_clock_font_prefs" -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false);
            }
            case "lockscreen_clock_color_code_accent1",
                 "lockscreen_clock_color_code_accent2",
                 "lockscreen_clock_color_code_accent3",
                 "lockscreen_clock_color_code_text1",
                 "lockscreen_clock_color_code_text2" -> {
                return instance.mPreferences.getBoolean("lockscreen_custom_color_switch", false);
            }
            case LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE -> {
                return instance.LsClockUserImageVisible.contains(instance.mPreferences.getInt("lockscreen_custom_clock_style", 0));
            }
            case LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE -> {
                return instance.LsClockUserVisible.contains(instance.mPreferences.getInt("lockscreen_custom_clock_style", 0));
            }
            case LOCKSCREEN_CLOCK_CUSTOM_DEVICE_VALUE -> {
                return instance.LsClockDeviceVisible.contains(instance.mPreferences.getInt("lockscreen_custom_clock_style", 0));
            }
            case "lockscreen_clock_custom_user_image_picker" -> {
                return isVisible("lockscreen_clock_custom_user_image") && instance.mPreferences.getBoolean("lockscreen_clock_custom_user_image", false);
            }
            case "lockscreen_clock_custom_image_switch" -> {
                return instance.LsClockCustomImageVisible.contains(instance.mPreferences.getInt("lockscreen_custom_clock_style", 0));
            }
            case "lockscreen_clock_custom_image_picker" -> {
                return isVisible("lockscreen_clock_custom_image_switch") &&
                        instance.mPreferences.getBoolean("lockscreen_clock_custom_image_switch", false);
            }
            case "lockscreen_clock_custom_image" -> {
                return instance.mPreferences.getBoolean("lockscreen_clock_custom_image_switch", false);
            }
            case "lockscreen_clock_stock_prefs",
                 "lockscreen_stock_clock_red_one_mode" -> {
                return !instance.mPreferences.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false);
            }
            case "lockscreen_stock_clock_red_one_color" -> {
                return !instance.mPreferences.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false) &&
                        Integer.parseInt(instance.mPreferences.getString("lockscreen_stock_clock_red_one_mode", "0")) == 3;
            }
            case "lockscreen_clock_font_custom" -> {
                return instance.mPreferences.getBoolean("lockscreen_custom_font", false);
            }
            case LOCKSCREEN_CLOCK_DATE_FORMAT -> {
                return instance.LsClockDateFormat.contains(instance.mPreferences.getInt("lockscreen_custom_clock_style", 0));
            }

            // Lockscreen Weather
            case LOCKSCREEN_WEATHER_HUMIDITY,
                 LOCKSCREEN_WEATHER_WIND,
                 LOCKSCREEN_WEATHER_TEXT_SIZE,
                 LOCKSCREEN_WEATHER_IMAGE_SIZE,
                 LOCKSCREEN_WEATHER_UI_PREFS,
                 LOCKSCREEN_WEATHER_SHOW_LOCATION,
                 LOCKSCREEN_WEATHER_SHOW_CONDITION,
                 LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH,
                 LOCKSCREEN_WEATHER_CUSTOM_MARGINS,
                 LOCKSCREEN_WEATHER_CENTERED,
                 LOCKSCREEN_WEATHER_BACKGROUND -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WEATHER_SWITCH, false);
            }
            case LOCKSCREEN_WEATHER_CUSTOM_COLOR,
                 LOCKSCREEN_WEATHER_CUSTOM_COLOR_CAT -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WEATHER_SWITCH, false) &&
                        instance.mPreferences.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH, false);
            }
            case LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP,
                 LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WEATHER_SWITCH, false) &&
                        instance.mPreferences.getBoolean(LOCKSCREEN_WEATHER_CUSTOM_MARGINS, false);
            }

            // Lockscreen Widgets
            case LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, false);
            }

            case LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, false) &&
                        instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false);
            }

            case LOCKSCREEN_WIDGETS_BIG_ACTIVE,
                 LOCKSCREEN_WIDGETS_BIG_INACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_ACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_INACTIVE,
                 LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE,
                 LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_CUSTOM_COLOR, false);
            }

            // Aod Clocks
            case "aod_clock_custom",
                 "aod_clock_font_prefs",
                 "aod_clock_prefs" -> {
                return instance.mPreferences.getBoolean(AOD_CLOCK_SWITCH, false);
            }
            case AOD_CLOCK_COLOR_CODE_ACCENT1,
                 AOD_CLOCK_COLOR_CODE_ACCENT2,
                 AOD_CLOCK_COLOR_CODE_ACCENT3,
                 AOD_CLOCK_COLOR_CODE_TEXT1,
                 AOD_CLOCK_COLOR_CODE_TEXT2 -> {
                return instance.mPreferences.getBoolean(AOD_CLOCK_CUSTOM_COLOR_SWITCH, false);
            }
            case "aod_clock_custom_user_image" -> {
                return instance.LsClockUserImageVisible.contains(instance.mPreferences.getInt("aod_custom_clock_style", 0));
            }
            case AOD_CLOCK_CUSTOM_USER_VALUE -> {
                return instance.LsClockUserVisible.contains(instance.mPreferences.getInt("aod_custom_clock_style", 0));
            }
            case AOD_CLOCK_CUSTOM_DEVICE_VALUE -> {
                return instance.LsClockDeviceVisible.contains(instance.mPreferences.getInt("aod_custom_clock_style", 0));
            }
            case "aod_clock_custom_user_image_picker" -> {
                return isVisible("aod_clock_custom_user_image") && instance.mPreferences.getBoolean("aod_clock_custom_user_image", false);
            }
            case "aod_clock_font_custom" -> {
                return instance.mPreferences.getBoolean("aod_custom_font", false);
            }
            case AOD_CLOCK_CUSTOM_IMAGE -> {
                return instance.LsClockCustomImageVisible.contains(instance.mPreferences.getInt("aod_custom_clock_style", 0));
            }
            case "aod_clock_custom_image_picker" -> {
                return isVisible("aod_clock_custom_image_switch") &&
                        instance.mPreferences.getBoolean("aod_clock_custom_image_switch", false);
            }

            // Aod Weather
            case AOD_WEATHER_HUMIDITY,
                 AOD_WEATHER_WIND,
                 AOD_WEATHER_TEXT_SIZE,
                 AOD_WEATHER_IMAGE_SIZE,
                 AOD_WEATHER_UI_PREFS,
                 AOD_WEATHER_SHOW_LOCATION,
                 AOD_WEATHER_SHOW_CONDITION,
                 AOD_WEATHER_COLOR_CAT,
                 AOD_WEATHER_CUSTOM_COLOR_SWITCH,
                 AOD_WEATHER_CUSTOM_MARGINS,
                 AOD_WEATHER_CENTERED -> {
                return instance.mPreferences.getBoolean(AOD_WEATHER_SWITCH, false);
            }
            case AOD_WEATHER_CUSTOM_COLOR -> {
                return instance.mPreferences.getBoolean(AOD_WEATHER_SWITCH, false) &&
                        instance.mPreferences.getBoolean(AOD_WEATHER_CUSTOM_COLOR_SWITCH, false);
            }
            case AOD_WEATHER_CUSTOM_MARGIN_TOP,
                 AOD_WEATHER_CUSTOM_MARGIN_LEFT -> {
                return instance.mPreferences.getBoolean(AOD_WEATHER_SWITCH, false) &&
                        instance.mPreferences.getBoolean(AOD_WEATHER_CUSTOM_MARGINS, false);
            }

            // Volume Panel Customization
            case "volume_panel_seekbar_link_primary" -> {
                return instance.mPreferences.getBoolean("volume_panel_seekbar_color_enabled", false);
            }
            case "volume_panel_seekbar_color" -> {
                return instance.mPreferences.getBoolean("volume_panel_seekbar_color_enabled", false) &&
                        !instance.mPreferences.getBoolean("volume_panel_seekbar_link_primary", false);
            }
            case "volume_panel_seekbar_bg_color" -> {
                return instance.mPreferences.getBoolean("volume_panel_seekbar_bg_color_enabled", false);
            }
            case "volume_panel_icon_accent" -> {
                return instance.mPreferences.getBoolean("volume_panel_icon_color_enabled", false);
            }
            case "volume_panel_icon_color" -> {
                return instance.mPreferences.getBoolean("volume_panel_icon_color_enabled", false) &&
                        !instance.mPreferences.getBoolean("volume_panel_icon_accent", false);
            }

            // Advanced Reboot
            case "advanced_reboot_auth" -> {
                return instance.mPreferences.getBoolean("show_advanced_reboot", false);
            }

            // Pulse

            case "pulse_lavalamp_speed" -> {
                return Integer.parseInt(instance.mPreferences.getString("pulse_color_mode", "2")) == 2;
            }

            // Fluid Music
            case "fluid_settings" -> {
                return doesClassExist(SYSTEM_UI,
                        "com.oplus.systemui.media.seedling.rus.OplusMediaRusUpdateManager");
            }


            case "volbtn_torch_enable_timeout", "volbtn_torch_use_proximity" -> {
                return instance.mPreferences.getBoolean("volbtn_torch", false);
            }
            case "volbtn_torch_timeout" -> {
                return instance.mPreferences.getBoolean("volbtn_torch", false) && instance.mPreferences.getBoolean("volbtn_torch_enable_timeout", false);
            }

            // Weather
            case WEATHER_OWM_KEY -> {
                return instance.mPreferences.getString(WEATHER_PROVIDER, "2").equals("0");
            }
            case WEATHER_YANDEX_KEY -> {
                return instance.mPreferences.getString(WEATHER_PROVIDER, "2").equals("3");
            }

            // Lag Fix
            case "lag_fix_cat",
                 "fix_lag_switch",
                 "fix_lag_force_all_apps",
                 "fix_lag_app_chooser" -> {
                return Build.VERSION.SDK_INT >= 34;
            }

            // Ota Card
            case "ota_card_picker" -> {
                return instance.mPreferences.getBoolean("custom_ota_card", false);
            }

            // Pocket Studio
            case "enable_pocket_studio", "pocket_studio_cat", "pocket_studio_footer" -> {
                return ModuleUtil.moduleExists();
            }

            case "custom_memc_values" -> {
                return instance.mPreferences.getBoolean("custom_memc_config", false);
            }

        }
        return true;
    }

    public static boolean isVisibleBattery(String key) {
        int batteryStyle = Integer.parseInt(instance.mPreferences.getString("battery_icon_style", String.valueOf(Constants.Preferences.BatteryPrefs.BATTERY_STYLE_CUSTOM_RLANDSCAPE)));

        boolean showAdvancedCustomizations = batteryStyle >= Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYA &&
                batteryStyle <= Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYO;
        boolean showColorPickers = instance.mPreferences.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false);
        boolean showRainbowBattery = batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYI ||
                batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYJ;
        boolean showCommonCustomizations = instance.mPreferences.getBoolean(CUSTOMIZE_BATTERY_ICON, false);
        boolean showPercentage = batteryStyle != BATTERY_STYLE_DEFAULT &&
                batteryStyle != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                batteryStyle != BATTERY_STYLE_DEFAULT_RLANDSCAPE &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_IOS_16 &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_BATTERYL &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_BATTERYM;
        boolean showInsidePercentage = showPercentage && !instance.mPreferences.getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false);
        boolean showChargingIconCustomization = instance.mPreferences.getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false);
        boolean circleBattery = batteryStyle == BATTERY_STYLE_CIRCLE ||
                batteryStyle == BATTERY_STYLE_DOTTED_CIRCLE ||
                batteryStyle == BATTERY_STYLE_FILLED_CIRCLE;

        return switch (key) {
            case "category_battery_icon_settings",
                 "battery_icon_style" -> showCommonCustomizations;
            case "category_battery_charging_icon" -> showChargingIconCustomization;
            case "battery_perimeter_alpha",
                 "battery_fill_alpha",
                 "battery_rotate_layout" -> showAdvancedCustomizations;
            case "battery_reverse_layout", "battery_inside_percentage" -> showInsidePercentage;
            case "battery_rainbow_color" ->
                    (showAdvancedCustomizations || circleBattery) && showRainbowBattery;
            case "battery_blend_color" -> (showAdvancedCustomizations || circleBattery);
            case "battery_fill_color",
                 "battery_fill_gradient_color",
                 "battery_charging_fill_color",
                 "battery_fast_charging_fill_color",
                 "battery_powersave_fill_color",
                 "battery_powersave_icon_color" ->
                    (showAdvancedCustomizations || circleBattery) && showColorPickers;
            case "battery_hide_percentage" -> showPercentage;
            case "category_battery_colors" ->
                    showCommonCustomizations && (showAdvancedCustomizations || showRainbowBattery || showColorPickers || circleBattery);
            default -> false;
        };

    }

    public static boolean isEnabled(String key) {
        return switch (key) {
            case "BBarTransitColors" -> !instance.mPreferences.getBoolean("BBarColorful", false);
            case "BBarColorful" -> !instance.mPreferences.getBoolean("BBarTransitColors", false);
            case "BIconColorful" -> !instance.mPreferences.getBoolean("BIconTransitColors", false);
            case "BIconTransitColors" -> !instance.mPreferences.getBoolean("BIconColorful", false);
            case "lockscreen_fp_remove_icon" ->
                    !instance.mPreferences.getBoolean("lockscreen_fp_custom_icon", false);
            case "lockscreen_fp_custom_icon" ->
                    !instance.mPreferences.getBoolean("lockscreen_fp_remove_icon", false);
            case QS_TILE_HIDE_LABELS ->
                    !instance.mPreferences.getBoolean(QS_TILE_LABELS_CUSTOM_COLOR_ENABLED, false);
            case QS_TILE_LABELS_CUSTOM_COLOR_ENABLED ->
                    !instance.mPreferences.getBoolean(QS_TILE_HIDE_LABELS, false);

            // Lockscreen Widgets
            case LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE,
                 LOCKSCREEN_WIDGETS_CUSTOM_COLOR,
                 LOCKSCREEN_WIDGETS_BIG_ACTIVE,
                 LOCKSCREEN_WIDGETS_BIG_INACTIVE,
                 LOCKSCREEN_WIDGETS_BIG_ICON_ACTIVE,
                 LOCKSCREEN_WIDGETS_BIG_ICON_INACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_ACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_INACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE,
                 LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE ->
                    instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false);

            case LOCKSCREEN_WIDGETS_WEATHER_SETTINGS ->
                    instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_ENABLED, false) &&
                            (instance.mPreferences.getString(LOCKSCREEN_WIDGETS, "").contains("weather") ||
                                    instance.mPreferences.getString(LOCKSCREEN_WIDGETS_EXTRAS, "").contains("weather"));

            case "fix_lag_force_all_apps" ->
                    instance.mPreferences.getBoolean("fix_lag_switch", false);
            case "fix_lag_app_chooser" ->
                    instance.mPreferences.getBoolean("fix_lag_switch", false) &&
                            !instance.mPreferences.getBoolean("fix_lag_force_all_apps", false);

            default -> true;
        };
    }

    /**
     *
     */
    @SuppressLint("DefaultLocale")
    @Nullable
    public static String getSummary(Context fragmentCompat, @NonNull String key) {
        if (key.contains("Slider")) {
            return String.format("%.2f", instance.mPreferences.getSliderFloat(key, 0f));
        }
        if (key.contains("Switch")) {
            return fragmentCompat.getString(instance.mPreferences.getBoolean(key, false) ? android.R.string.ok : android.R.string.cancel);
        }
        if (key.contains("List")) {
            return instance.mPreferences.getString(key, "");
        }
        if (key.contains("EditText")) {
            return instance.mPreferences.getString(key, "");
        }
        if (key.contains("MultiSelect")) {
            return instance.mPreferences.getStringSet(key, Collections.emptySet()).toString();
        }

        return switch (key) {
            // Padding
            case "statusbar_padding_start", "statusbar_padding_end" ->
                    fragmentCompat.getString(R.string.statusbar_padding_info) + "\n" +
                    instance.mPreferences.getSliderFloat(key, 0);

            // Statusbar Clock
            case "status_bar_java_custom" ->
                    instance.mPreferences.getString("status_bar_custom_clock_format", "$GEEE");
            case "sbc_before_clock" ->
                    instance.mPreferences.getString("sbc_before_clock_format", "");
            case "sbc_after_clock" -> instance.mPreferences.getString("sbc_after_clock_format", "");

            // Battery Bar
            case "BBOpacity", "BBarHeight" -> instance.mPreferences.getSliderInt(key, 100) + "%";

            // Quick Settings Prefs
            case "quick_pulldown_length" ->
                    instance.mPreferences.getSliderInt("quick_pulldown_length", 25) + "%";
            // Tiles
            case "quick_settings_quick_tiles" ->
                    String.valueOf(instance.mPreferences.getSliderInt("quick_settings_quick_tiles", 5));
            case "quick_settings_tiles_rows" ->
                    String.valueOf(instance.mPreferences.getSliderInt("quick_settings_tiles_rows", 3));
            case "quick_settings_tiles_horizontal_columns" ->
                    String.valueOf(instance.mPreferences.getSliderInt("quick_settings_tiles_horizontal_columns", 4));
            case "quick_settings_tiles_vertical_columns" ->
                    String.valueOf(instance.mPreferences.getSliderInt("quick_settings_tiles_vertical_columns", 4));

            // Tile Radius
            case QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT, 0) + "dp";
            case QS_TILE_RADIUS_TOP_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_TOP_LEFT, 0) + "dp";
            case QS_TILE_RADIUS_TOP_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_TOP_RIGHT, 0) + "dp";
            case QS_TILE_RADIUS_BOTTOM_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_BOTTOM_LEFT, 0) + "dp";
            case QS_TILE_RADIUS_BOTTOM_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_BOTTOM_RIGHT, 0) + "dp";

            case QS_TILE_ANIMATION_DURATION ->
                    instance.mPreferences.getSliderInt(QS_TILE_ANIMATION_DURATION, 1) + "s";

            case QS_TRANSPARENCY_VAL ->
                    instance.mPreferences.getSliderInt(QS_TRANSPARENCY_VAL, 40) + "%";
            case BLUR_RADIUS_VALUE ->
                    instance.mPreferences.getSliderInt(BLUR_RADIUS_VALUE, 60) + "%";
            case QS_MEDIA_ART_BLUR_AMOUNT ->
                    instance.mPreferences.getSliderInt(QS_MEDIA_ART_BLUR_AMOUNT, 35) + "%";
            case QS_MEDIA_ART_TINT_AMOUNT ->
                    instance.mPreferences.getSliderInt(QS_MEDIA_ART_TINT_AMOUNT, 30) + "%";
            // Statusbar
            case "status_bar_clock_size" ->
                    instance.mPreferences.getSliderInt("status_bar_clock_size", 12) + "sp";
            case "status_bar_clock_auto_hide_hduration" ->
                    fragmentCompat.getString(R.string.status_bar_clock_auto_hide_hdur_summary) + "\n" +
                            instance.mPreferences.getSliderInt("status_bar_clock_auto_hide_hduration", 60) + " " +
                            fragmentCompat.getString(R.string.seconds);
            case "status_bar_clock_auto_hide_sduration" ->
                    fragmentCompat.getString(R.string.status_bar_clock_auto_hide_sdur_summary) + "\n" +
                            instance.mPreferences.getSliderInt("status_bar_clock_auto_hide_sduration", 5) + " " +
                            fragmentCompat.getString(R.string.seconds);
            case NOTIF_TRANSPARENCY_VALUE ->
                    String.valueOf(instance.mPreferences.getSliderInt(NOTIF_TRANSPARENCY_VALUE, 25));

            // Header Clock
            case "qs_header_clock_text_scaling" ->
                    instance.mPreferences.getSliderFloat("qs_header_clock_text_scaling", 1.0f) + "%";
            case "qs_header_clock_top_margin" ->
                    instance.mPreferences.getSliderInt("qs_header_clock_top_margin", 0) + "dp";
            case "qs_header_clock_left_margin" ->
                    instance.mPreferences.getSliderInt("qs_header_clock_left_margin", 0) + "dp";

            // Battery
            case CUSTOM_BATTERY_WIDTH ->
                    instance.mPreferences.getSliderInt(CUSTOM_BATTERY_WIDTH, 20) + "dp";
            case CUSTOM_BATTERY_HEIGHT ->
                    instance.mPreferences.getSliderInt(CUSTOM_BATTERY_HEIGHT, 20) + "dp";
            case CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT ->
                    instance.mPreferences.getSliderInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14) + "dp";
            case CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT ->
                    instance.mPreferences.getSliderInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1) + "dp";
            case CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT ->
                    instance.mPreferences.getSliderInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 1) + "dp";

            // Gesture Prefs
            case "gesture_left_height_double" -> getGestureHeight(key);
            case "gesture_right_height_double" -> getGestureHeight(key);
            case "GesPillWidthModPos" ->
                    instance.mPreferences.getSliderInt("GesPillWidthModPos", 0) + "%";

            // Launcher Prefs
            case "folder_columns" ->
                    String.valueOf(instance.mPreferences.getSliderInt("folder_columns", 3));
            case "folder_rows" ->
                    String.valueOf(instance.mPreferences.getSliderInt("folder_rows", 3));
            case "drawer_columns" ->
                    String.valueOf(instance.mPreferences.getSliderInt("drawer_columns", 4));
            case "launcher_max_columns" ->
                    String.valueOf(instance.mPreferences.getSliderInt("launcher_max_columns", 5));
            case "launcher_max_rows" ->
                    String.valueOf(instance.mPreferences.getSliderInt("launcher_max_rows", 6));

            // Header Image
            case "qs_header_image_alpha" ->
                    String.valueOf(instance.mPreferences.getSliderInt("qs_header_image_alpha", 255));
            case "qs_header_image_height_portrait" ->
                    String.valueOf(instance.mPreferences.getSliderInt("qs_header_image_height_portrait", 325));
            case "qs_header_image_height_landscape" ->
                    String.valueOf(instance.mPreferences.getSliderInt("qs_header_image_height_landscape", 200));
            case "qs_header_image_padding_side" ->
                    String.valueOf(instance.mPreferences.getSliderInt("qs_header_image_padding_side", -50));
            case "qs_header_image_padding_top" ->
                    String.valueOf(instance.mPreferences.getSliderInt("qs_header_image_padding_top", 0));
            case "qs_header_image_tint_intensity" ->
                    instance.mPreferences.getSliderInt("qs_header_image_tint_intensity", 50) + "%";
            case QS_HEADER_IMAGE_BOTTOM_FADE ->
                    instance.mPreferences.getSliderInt(QS_HEADER_IMAGE_BOTTOM_FADE, 40) + "dp";

            // Lockscreen
            case LOCKSCREEN_FINGERPRINT_SCALING ->
                    instance.mPreferences.getSliderFloat(LOCKSCREEN_FINGERPRINT_SCALING, 1.0f) + "%";
            case "DWOpacity" -> instance.mPreferences.getSliderInt("DWOpacity", 192) + "dp";
            case "DWAodOpacity" -> instance.mPreferences.getSliderInt("DWAodOpacity", 192) + "dp";
            case "lockscreen_media_blur" ->
                    instance.mPreferences.getSliderInt("lockscreen_media_blur", 35) + "%";

            // Lockscreen Clock
            case LOCKSCREEN_CLOCK_LINE_HEIGHT ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_LINE_HEIGHT, 100) + "dp";
            case LOCKSCREEN_CLOCK_TEXT_SCALING ->
                    instance.mPreferences.getSliderFloat(LOCKSCREEN_CLOCK_TEXT_SCALING, 1.0f) + "%";
            case LOCKSCREEN_CLOCK_TOP_MARGIN ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_TOP_MARGIN, 0) + "dp";
            case LOCKSCREEN_CLOCK_BOTTOM_MARGIN ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_BOTTOM_MARGIN, 0) + "dp";

            // Lockscreen Weather
            case LOCKSCREEN_WEATHER_IMAGE_SIZE ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_WEATHER_IMAGE_SIZE, 18) + "dp";
            case LOCKSCREEN_WEATHER_TEXT_SIZE ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_WEATHER_TEXT_SIZE, 16) + "sp";
            case LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_WEATHER_CUSTOM_MARGIN_TOP, 0) + "dp";
            case LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_WEATHER_CUSTOM_MARGIN_LEFT, 0) + "dp";

            // Lockscreen Widgets
            case LOCKSCREEN_WIDGETS_SCALE ->
                    instance.mPreferences.getSliderFloat(LOCKSCREEN_WIDGETS_SCALE, 1.0f) + "%";

            // Aod Clock
            case AOD_CLOCK_LINE_HEIGHT ->
                    instance.mPreferences.getSliderInt(AOD_CLOCK_LINE_HEIGHT, 100) + "dp";
            case AOD_CLOCK_TEXT_SCALING ->
                    instance.mPreferences.getSliderFloat(AOD_CLOCK_TEXT_SCALING, 1.0f) + "%";

            // Aod Weather
            case AOD_WEATHER_IMAGE_SIZE ->
                    instance.mPreferences.getSliderInt(AOD_WEATHER_IMAGE_SIZE, 18) + "dp";
            case AOD_WEATHER_TEXT_SIZE ->
                    instance.mPreferences.getSliderInt(AOD_WEATHER_TEXT_SIZE, 16) + "sp";
            case AOD_WEATHER_CUSTOM_MARGIN_TOP ->
                    instance.mPreferences.getSliderInt(AOD_WEATHER_CUSTOM_MARGIN_TOP, 0) + "dp";
            case AOD_WEATHER_CUSTOM_MARGIN_LEFT ->
                    instance.mPreferences.getSliderInt(AOD_WEATHER_CUSTOM_MARGIN_LEFT, 0) + "dp";

            // Sound Prefs
            case "volume_dialog_timeout" ->
                    instance.mPreferences.getSliderInt("volume_dialog_timeout", 3) + " s";

            // Pulse Prefs
            case "pulse_lavalamp_speed" ->
                    instance.mPreferences.getSliderInt("pulse_lavalamp_speed", 10000) + " ms";
            case "pulse_custom_dimen" ->
                    instance.mPreferences.getSliderInt("pulse_custom_dimen", 14) + " px";
            case "pulse_custom_div" ->
                    instance.mPreferences.getSliderInt("pulse_custom_div", 16) + " px";
            case "pulse_custom_fudge_factor" ->
                    String.valueOf(instance.mPreferences.getSliderInt("pulse_custom_fudge_factor", 4));
            case "pulse_filled_block_size" ->
                    instance.mPreferences.getSliderInt("pulse_filled_block_size", 4) + " px";
            case "pulse_empty_block_size" ->
                    instance.mPreferences.getSliderInt("pulse_empty_block_size", 4) + " px";
            case "pulse_solid_units_opacity" ->
                    String.valueOf(instance.mPreferences.getSliderInt("pulse_solid_units_opacity", 200));
            case "pulse_solid_units_count" ->
                    String.valueOf(instance.mPreferences.getSliderInt("pulse_solid_units_count", 32));
            case "pulse_solid_fudge_factor" ->
                    String.valueOf(instance.mPreferences.getSliderInt("pulse_solid_fudge_factor", 4));
            case "pulse_line_wave_stroke" ->
                    instance.mPreferences.getSliderInt("pulse_line_wave_stroke", 5) + " px";
            case "pulse_line_wave_opacity" ->
                    String.valueOf(instance.mPreferences.getSliderInt("pulse_line_wave_opacity", 200));

            // Buttons
            case "volbtn_torch_timeout" ->
                    instance.mPreferences.getSliderInt("volbtn_torch_timeout", 5) + " " +
                            fragmentCompat.getString(R.string.seconds);

            default -> null;
        };

    }

    public static String getGestureHeight(String key) {
        List<Float> height = instance.mPreferences.getSliderValues(key, 100f);
        if (height.size() == 2) {
            return height.get(0).intValue() + "% - " + height.get(1).intValue() + "%";
        } else {
            return "100%";
        }
    }

    /**
     *
     */
    public static void setupPreference(Preference preference) {
        try {
            String key = preference.getKey();

            preference.setVisible(isVisible(key));
            preference.setEnabled(isEnabled(key));

            String summary = getSummary(preference.getContext(), key);
            if (summary != null && !preference.getKey().equals("sb_illustration")) {
                preference.setSummary(summary);
            }

            if (preference instanceof OplusSliderPreference) {
                ((OplusSliderPreference) preference).slider.setLabelFormatter(value -> {
                    if (value == ((OplusSliderPreference) preference).defaultValue.get(0))
                        return getAppContext().getString(R.string.default_value);
                    else return String.valueOf(Math.round(value));
                });
            }

            //Other special cases
            switch (key) {
                // Quick Settings
                case "QSLabelScaleFactor", "QSSecondaryLabelScaleFactor" ->
                        ((OplusSliderPreference) preference).slider.setLabelFormatter(value -> (value + 100) + "%");
            }
        } catch (Throwable ignored) {
        }
    }

    public static void setupAllPreferences(PreferenceGroup group) {
        for (int i = 0; ; i++) {
            try {
                Preference thisPreference = group.getPreference(i);

                PreferenceHelper.setupPreference(thisPreference);

                if (thisPreference instanceof PreferenceGroup) {
                    setupAllPreferences((PreferenceGroup) thisPreference);
                }
            } catch (Throwable ignored) {
                break;
            }
        }
    }

}
