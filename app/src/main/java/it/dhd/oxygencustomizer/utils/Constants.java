package it.dhd.oxygencustomizer.utils;

import android.os.Environment;

import java.util.Arrays;
import java.util.List;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector;

public class Constants {

    public static class Packages {
        public static final String FRAMEWORK = "android";
        public static final String SYSTEM_UI = "com.android.systemui";
        public static final String TELECOM_SERVER_PACKAGE = "com.android.server.telecom";
        public static final String LAUNCHER = "com.android.launcher";
        public static final String SETTINGS = "com.android.settings";
    }

    public static class Preferences {
        public static class General {
            public static final List<String> PREF_UPDATE_EXCLUSIONS = Arrays.asList(BootLoopProtector.LOAD_TIME_KEY_KEY, BootLoopProtector.PACKAGE_STRIKE_KEY_KEY);
        }
        public static class Framework {
            public static final String SENSOR_BLOCK = "sensor_block";
            public static final String SENSOR_BLOCK_APP_LIST = "sensor_block_app_list";
            public static final String SENSOR_BLOCKED_APP = "sensor_blocked_app";
            public static final String SENSOR_BLOCK_APP_DUMMY = "sensor_blocked_app_dummy";
        }
        public static class BatteryPrefs {
            public static final String CUSTOMIZE_BATTERY_ICON = "battery_icon_customize";
            public static final String CUSTOM_BATTERY_LAYOUT_REVERSE = "battery_rotate_layout";
            public static final String CUSTOM_BATTERY_MARGINS = "category_battery_margins";
            public static final String CUSTOM_BATTERY_MARGIN_LEFT = "battery_margin_left";
            public static final String CUSTOM_BATTERY_MARGIN_TOP = "battery_margin_top";
            public static final String CUSTOM_BATTERY_MARGIN_RIGHT = "battery_margin_right";
            public static final String CUSTOM_BATTERY_MARGIN_BOTTOM = "battery_margin_bottom";
            public static final String CUSTOM_BATTERY_PERIMETER_ALPHA = "battery_perimeter_alpha";
            public static final String CUSTOM_BATTERY_FILL_ALPHA = "battery_fill_alpha";
            public static final String CUSTOM_BATTERY_RAINBOW_FILL_COLOR = "battery_rainbow_color";
            public static final String CUSTOM_BATTERY_BLEND_COLOR = "battery_blend_color";
            public static final String CUSTOM_BATTERY_CHARGING_COLOR = "battery_charging_fill_color";
            public static final String CUSTOM_BATTERY_FAST_CHARGING_COLOR = "battery_fast_charging_fill_color";
            public static final String CUSTOM_BATTERY_FILL_COLOR = "battery_fill_color";
            public static final String CUSTOM_BATTERY_FILL_GRAD_COLOR = "battery_fill_gradient_color";
            public static final String CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR = "battery_powersave_icon_color";
            public static final String CUSTOM_BATTERY_POWERSAVE_FILL_COLOR = "battery_powersave_fill_color";
            public static final String CUSTOM_BATTERY_SWAP_PERCENTAGE = "battery_reverse_layout";
            public static final String CUSTOM_BATTERY_CHARGING_ICON_SWITCH = "battery_icon_change_charging_icon";
            public static final String CUSTOM_BATTERY_CHARGING_ICON_STYLE = "battery_charging_icon_style";
            public static final String CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT = "battery_charging_icon_margin_left";
            public static final String CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT = "battery_charging_icon_margin_right";
            public static final String CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT = "battery_charging_icon_size";
            public static final String CUSTOM_BATTERY_HIDE_PERCENTAGE = "battery_hide_percentage";
            public static final String CUSTOM_BATTERY_INSIDE_PERCENTAGE = "battery_inside_percentage";
            public static final String CUSTOM_BATTERY_HIDE_BATTERY = "battery_hide_battery";
            public static final String CUSTOM_BATTERY_STYLE = "battery_icon_style";
            public static final String CUSTOM_BATTERY_WIDTH = "battery_width";
            public static final String CUSTOM_BATTERY_HEIGHT = "battery_height";
            public static final String STOCK_CUSTOMIZE_PERCENTAGE_SIZE = "customize_stock_percentage_size";
            public static final String STOCK_PERCENTAGE_SIZE = "stock_percentage_size";

            public static final String[] BatteryPrefs = {
                    CUSTOMIZE_BATTERY_ICON,
                    CUSTOM_BATTERY_LAYOUT_REVERSE,
                    CUSTOM_BATTERY_MARGINS,
                    CUSTOM_BATTERY_MARGIN_LEFT,
                    CUSTOM_BATTERY_MARGIN_TOP,
                    CUSTOM_BATTERY_MARGIN_RIGHT,
                    CUSTOM_BATTERY_MARGIN_BOTTOM,
                    CUSTOM_BATTERY_PERIMETER_ALPHA,
                    CUSTOM_BATTERY_FILL_ALPHA,
                    CUSTOM_BATTERY_RAINBOW_FILL_COLOR,
                    CUSTOM_BATTERY_BLEND_COLOR,
                    CUSTOM_BATTERY_CHARGING_COLOR,
                    CUSTOM_BATTERY_FAST_CHARGING_COLOR,
                    CUSTOM_BATTERY_FILL_COLOR,
                    CUSTOM_BATTERY_FILL_GRAD_COLOR,
                    CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR,
                    CUSTOM_BATTERY_POWERSAVE_FILL_COLOR,
                    CUSTOM_BATTERY_SWAP_PERCENTAGE,
                    CUSTOM_BATTERY_CHARGING_ICON_SWITCH,
                    CUSTOM_BATTERY_CHARGING_ICON_STYLE,
                    CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT,
                    CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT,
                    CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT,
                    CUSTOM_BATTERY_HIDE_PERCENTAGE,
                    CUSTOM_BATTERY_INSIDE_PERCENTAGE,
                    CUSTOM_BATTERY_HIDE_BATTERY,
                    CUSTOM_BATTERY_STYLE,
                    CUSTOM_BATTERY_WIDTH,
                    CUSTOM_BATTERY_HEIGHT

            };

            // Battery styles
            public static final int BATTERY_STYLE_DEFAULT = 0;
            public static final int BATTERY_STYLE_DEFAULT_RLANDSCAPE = 1;
            public static final int BATTERY_STYLE_DEFAULT_LANDSCAPE = 2;
            public static final int BATTERY_STYLE_CUSTOM_RLANDSCAPE = 3;
            public static final int BATTERY_STYLE_CUSTOM_LANDSCAPE = 4;
            public static final int BATTERY_STYLE_PORTRAIT_CAPSULE = 5;
            public static final int BATTERY_STYLE_PORTRAIT_LORN = 6;
            public static final int BATTERY_STYLE_PORTRAIT_MX = 7;
            public static final int BATTERY_STYLE_PORTRAIT_AIROO = 8;
            public static final int BATTERY_STYLE_RLANDSCAPE_STYLE_A = 9;
            public static final int BATTERY_STYLE_LANDSCAPE_STYLE_A = 10;
            public static final int BATTERY_STYLE_RLANDSCAPE_STYLE_B = 11;
            public static final int BATTERY_STYLE_LANDSCAPE_STYLE_B = 12;
            public static final int BATTERY_STYLE_LANDSCAPE_IOS_15 = 13;
            public static final int BATTERY_STYLE_LANDSCAPE_IOS_16 = 14;
            public static final int BATTERY_STYLE_PORTRAIT_ORIGAMI = 15;
            public static final int BATTERY_STYLE_LANDSCAPE_SMILEY = 16;
            public static final int BATTERY_STYLE_LANDSCAPE_MIUI_PILL = 17;
            public static final int BATTERY_STYLE_LANDSCAPE_COLOROS = 18;
            public static final int BATTERY_STYLE_RLANDSCAPE_COLOROS = 19;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYA = 20;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYB = 21;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYC = 22;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYD = 23;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYE = 24;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYF = 25;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYG = 26;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYH = 27;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYI = 28;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYJ = 29;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYK = 30;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYL = 31;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYM = 32;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYN = 33;
            public static final int BATTERY_STYLE_LANDSCAPE_BATTERYO = 34;
            public static final int BATTERY_STYLE_CIRCLE = 35;
            public static final int BATTERY_STYLE_DOTTED_CIRCLE = 36;

        }
        public static class QsHeaderImage {
            public static final String QS_HEADER_IMAGE_ENABLED =
                    "qs_header_image_enabled";
            public static final String QS_HEADER_IMAGE_VALUE =
                    "qs_header_image_number";
            public static final String QS_HEADER_IMAGE_TINT =
                    "qs_header_image_tint";
            public static final String QS_HEADER_IMAGE_TINT_CUSTOM =
                    "qs_header_image_tint_custom";
            public static final String QS_HEADER_IMAGE_TINT_INTENSITY =
                    "qs_header_image_tint_intensity";
            public static final String QS_HEADER_IMAGE_ALPHA =
                    "qs_header_image_alpha";
            public static final String QS_HEADER_IMAGE_HEIGHT_PORTRAIT =
                    "qs_header_image_height_portrait";
            public static final String QS_HEADER_IMAGE_LANDSCAPE_ENABLED =
                    "qs_header_image_landscape_enabled";
            public static final String QS_HEADER_IMAGE_PADDING_SIDE =
                    "qs_header_image_padding_side";
            public static final String QS_HEADER_IMAGE_PADDING_TOP =
                    "qs_header_image_padding_top";
            public static final String QS_HEADER_IMAGE_URI =
                    "qs_header_image_uri";
            public static final String QS_HEADER_IMAGE_ZOOM_TO_FIT =
                    "qs_header_image_zoom_to_fit";

            public static final String QS_HEADER_IMAGE_BOTTOM_FADE =
                    "qs_header_image_bottom_fade";
            public static final String[] QS_PREFS = {
                    QS_HEADER_IMAGE_ENABLED,
                    QS_HEADER_IMAGE_VALUE,
                    QS_HEADER_IMAGE_TINT,
                    QS_HEADER_IMAGE_TINT_CUSTOM,
                    QS_HEADER_IMAGE_TINT_INTENSITY,
                    QS_HEADER_IMAGE_ALPHA,
                    QS_HEADER_IMAGE_BOTTOM_FADE,
                    QS_HEADER_IMAGE_HEIGHT_PORTRAIT,
                    QS_HEADER_IMAGE_LANDSCAPE_ENABLED,
                    QS_HEADER_IMAGE_PADDING_SIDE,
                    QS_HEADER_IMAGE_PADDING_TOP,
                    QS_HEADER_IMAGE_URI,
                    QS_HEADER_IMAGE_ZOOM_TO_FIT
            };
        }
        public static class QsTiles {
            public static final String QS_CUSTOMIZE_TILES = "quick_settings_tiles_customize";
            public static final String QS_QUICK_TILES = "quick_settings_quick_tiles";
            public static final String QS_ROWS = "quick_settings_tiles_rows";
            public static final String QS_COLUMNS = "quick_settings_tiles_horizontal_columns";

            public static final String[] QS_TILES_PREFS = {
                    QS_CUSTOMIZE_TILES,
                    QS_QUICK_TILES,
                    QS_ROWS,
                    QS_COLUMNS
            };
        }
        public static class QsTilesCustomization {
            public static final String QS_TILE_ACTIVE_COLOR_ENABLED = "qs_tile_active_color_enabled";
            public static final String QS_TILE_ACTIVE_COLOR = "qs_tile_active_color";
            public static final String QS_TILE_INACTIVE_COLOR_ENABLED = "qs_tile_inactive_color_enabled";
            public static final String QS_TILE_INACTIVE_COLOR = "qs_tile_inactive_color";
            public static final String QS_TILE_DISABLED_COLOR_ENABLED = "qs_tile_disabled_color_enabled";
            public static final String QS_TILE_DISABLED_COLOR = "qs_tile_disabled_color";
            public static final String QS_TILE_HIDE_LABELS = "qs_hide_labels";
            public static final String QS_TILE_LABELS_CUSTOM_COLOR_ENABLED = "qs_tile_label_enabled";
            public static final String QS_TILE_LABELS_CUSTOM_COLOR = "qs_tile_label";
            public static final String QS_BRIGHTNESS_SLIDER_CUSTOMIZE = "customize_brightness_slider";
            public static final String QS_BRIGHTNESS_SLIDER_COLOR_MODE = "brightness_slider_progress_color_mode";
            public static final String QS_BRIGHTNESS_SLIDER_COLOR = "brightness_slider_color";
            public static final String QS_BRIGHTNESS_SLIDER_BACKGROUND_ENABLED = "brightness_slider_background_color_enabled";
            public static final String QS_BRIGHTNESS_SLIDER_BACKGROUND_COLOR = "brightness_slider_background_color";

            public static final String[] QS_UPDATE_PREFS = {
                    QS_TILE_ACTIVE_COLOR_ENABLED,
                    QS_TILE_ACTIVE_COLOR,
                    QS_TILE_INACTIVE_COLOR_ENABLED,
                    QS_TILE_INACTIVE_COLOR,
                    QS_TILE_DISABLED_COLOR_ENABLED,
                    QS_TILE_DISABLED_COLOR,
                    QS_BRIGHTNESS_SLIDER_CUSTOMIZE,
                    QS_BRIGHTNESS_SLIDER_COLOR_MODE,
                    QS_BRIGHTNESS_SLIDER_COLOR,
                    QS_BRIGHTNESS_SLIDER_BACKGROUND_ENABLED,
                    QS_BRIGHTNESS_SLIDER_BACKGROUND_COLOR
            };

        }

        public static class QsHeaderClock {
            // Custom Switch
            public static final String QS_HEADER_CLOCK_CUSTOM_ENABLED = "qs_header_clock_custom_enabled";
            public static final String QS_HEADER_CLOCK_CUSTOM_VALUE = "qs_header_clock_custom_value";

            // Stock Clock Prefs
            public static final String QS_HEADER_CLOCK_STOCK_RED_MODE = "qs_header_stock_clock_red_one_mode";
            public static final String QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR = "qs_header_stock_clock_red_one_color";
            public static final String QS_HEADER_CLOCK_STOCK_TIME_COLOR_SWITCH = "qs_header_stock_clock_custom_color_switch";
            public static final String QS_HEADER_CLOCK_STOCK_TIME_COLOR = "qs_header_stock_clock_custom_color";
            public static final String QS_HEADER_CLOCK_STOCK_HIDE_DATE = "qs_header_stock_clock_date_hide";
            public static final String QS_HEADER_CLOCK_STOCK_DATE_COLOR_SWITCH = "qs_header_stock_clock_date_custom_color_switch";
            public static final String QS_HEADER_CLOCK_STOCK_DATE_COLOR = "qs_header_stock_clock_date_custom_color";

            public static final String QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_SWITCH = "qs_header_stock_clock_background_chip_switch";
            public static final String QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP = "qs_header_stock_clock_background_chip";
            public static final String QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_COLOR_MODE = "qs_header_stock_clock_background_chip_color_mode";
            public static final String QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_COLOR = "qs_header_stock_clock_background_chip_color";
            public static final String QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP_SWITCH = "qs_header_stock_date_background_chip_switch";
            public static final String QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP = "qs_header_stock_date_background_chip";
            public static final String QS_HEADER_CLOCK_STOCK_HIDE_CARRIER = "qs_header_stock_clock_hide_carrier_label";

            // Font Prefs
            public static final String QS_HEADER_CLOCK_CUSTOM_FONT = "qs_header_clock_custom_font";

            // Custom Clock Prefs
            public static final String QS_HEADER_CLOCK_TEXT_SCALING = "qs_header_clock_text_scaling";
            public static final String QS_HEADER_CLOCK_CUSTOM_COLOR_SWITCH = "qs_header_clock_custom_color_switch";
            public static final String QS_HEADER_CLOCK_CUSTOM_COLOR = "qs_header_clock_custom_color";
            public static final String QS_HEADER_CLOCK_TOP_MARGIN = "qs_header_clock_top_margin";
            public static final String QS_HEADER_CLOCK_LEFT_MARGIN = "qs_header_clock_left_margin";
            public static final String QS_HEADER_CLOCK_COLOR_CODE_ACCENT1 = "qs_header_clock_color_code_accent1";
            public static final String QS_HEADER_CLOCK_COLOR_CODE_ACCENT2 = "qs_header_clock_color_code_accent2";
            public static final String QS_HEADER_CLOCK_COLOR_CODE_ACCENT3 = "qs_header_clock_color_code_accent3";
            public static final String QS_HEADER_CLOCK_COLOR_CODE_TEXT1 = "qs_header_clock_color_code_text1";
            public static final String QS_HEADER_CLOCK_COLOR_CODE_TEXT2 = "qs_header_clock_color_code_text2";

            public static String getStyle(String key) {
                return key + "_STYLE";
            }

            public static String getUseAccentColor(String key) {
                return key + "_USE_ACCENT_COLOR";
            }
            public static String getUseGradient(String key) {
                return key + "_USE_GRADIENT";
            }
            public static String getGradientNum(String key, int num) {
                return key + "_GRADIENT_" + num;
            }
            public static String getGradientOrientation(String key) {
                return key + "_GRADIENT_ORIENTATION";
            }
            public static String getStrokeWidth(String key) {
                return key + "_STROKE_WIDTH";
            }
            public static String getStrokeColor(String key) {
                return key + "_STROKE_COLOR";
            }
            public static String getRoundedCorners(String key) {
                return key + "_ROUNDED_CORNERS";
            }
            public static String getTopSxR(String key) {
                return key + "_TOP_LEFT_RADIUS";
            }
            public static String getTopDxR(String key) {
                return key + "_TOP_RIGHT_RADIUS";
            }
            public static String getBottomSxR(String key) {
                return key + "_BOTTOM_LEFT_RADIUS";
            }
            public static String getBottomDxR(String key) {
                return key + "_BOTTOM_RIGHT_RADIUS";
            }

            public static final String[] QS_HEADER_PREFS = {
                    QS_HEADER_CLOCK_STOCK_RED_MODE,
                    QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR,
                    QS_HEADER_CLOCK_STOCK_TIME_COLOR_SWITCH,
                    QS_HEADER_CLOCK_STOCK_TIME_COLOR,
                    QS_HEADER_CLOCK_STOCK_HIDE_DATE,
                    QS_HEADER_CLOCK_STOCK_DATE_COLOR_SWITCH,
                    QS_HEADER_CLOCK_STOCK_DATE_COLOR,
                    QS_HEADER_CLOCK_STOCK_HIDE_CARRIER,
                    QS_HEADER_CLOCK_CUSTOM_ENABLED,
                    QS_HEADER_CLOCK_CUSTOM_VALUE,
                    QS_HEADER_CLOCK_TEXT_SCALING,
                    QS_HEADER_CLOCK_CUSTOM_COLOR_SWITCH,
                    QS_HEADER_CLOCK_COLOR_CODE_ACCENT1,
                    QS_HEADER_CLOCK_COLOR_CODE_ACCENT2,
                    QS_HEADER_CLOCK_COLOR_CODE_ACCENT3,
                    QS_HEADER_CLOCK_COLOR_CODE_TEXT1,
                    QS_HEADER_CLOCK_COLOR_CODE_TEXT2,
                    QS_HEADER_CLOCK_TOP_MARGIN,
                    QS_HEADER_CLOCK_LEFT_MARGIN,
                    QS_HEADER_CLOCK_CUSTOM_FONT
            };

            public static final String[] QS_HEADER_CHIP_PREFS = {
                    QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_SWITCH,
                    QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP,
                    getStyle(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getUseGradient(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getGradientNum(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP, 1),
                    getGradientNum(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP, 2),
                    getGradientOrientation(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getStrokeWidth(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getRoundedCorners(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getTopSxR(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getTopDxR(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getBottomSxR(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    getBottomDxR(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP),
                    QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_COLOR_MODE,
                    QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_COLOR,
                    QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP_SWITCH,
                    QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP,
                    getStyle(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getUseGradient(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getGradientNum(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP, 1),
                    getGradientNum(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP, 2),
                    getGradientOrientation(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getStrokeWidth(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getRoundedCorners(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getTopSxR(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getTopDxR(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getBottomSxR(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP),
                    getBottomDxR(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP)
            };
        }

        public static class Buttons {
            public static final String BUTTONS_POWER_LONGPRESS_TORCH = "torch_long_press_power_gesture";
            public static final String BUTTONS_POWER_LONGPRESS_TORCH_TIMEOUT_SWITCH = "torch_long_press_power_gesture_enable_timeout";
            public static final String BUTTONS_POWER_LONGPRESS_TORCH_TIMEOUT = "torch_long_press_power_timeout";
            public static final String BUTTONS_VOLUME_MUSIC = "volbtn_music_controls";
        }

        public static class Lockscreen {
            public static final String DISABLE_POWER = "disable_power_on_lockscreen";
            public static final String LOCKSCREEN_REMOVE_SOS = "hide_sos_lockscreen";
            public static final String LOCKSCREEN_HIDE_FINGERPRINT = "lockscreen_fp_remove_icon";
            public static final String LOCKSCREEN_CUSTOM_FINGERPRINT = "lockscreen_fp_custom_icon";
            public static final String LOCKSCREEN_FINGERPRINT_STYLE = "lockscreen_fp_icon_custom";
            public static final String LOCKSCREEN_FINGERPRINT_SCALING = "lockscreen_fp_icon_scaling";
        }

        public static class LockscreenClock {
            public static final String LOCKSCREEN_CLOCK_SWITCH = "lockscreen_custom_clock_switch";
            public static final String LOCKSCREEN_CLOCK_STYLE = "lockscreen_custom_clock_style";
            public static final String LOCKSCREEN_CLOCK_CUSTOM_COLOR_SWITCH = "lockscreen_custom_color_switch";
            public static final String LOCKSCREEN_CLOCK_CUSTOM_COLOR = "lockscreen_custom_color";
            public static final String LOCKSCREEN_CLOCK_LINE_HEIGHT = "lockscreen_clock_line_height";
            public static final String LOCKSCREEN_CLOCK_TOP_MARGIN = "lockscreen_top_margin";
            public static final String LOCKSCREEN_CLOCK_BOTTOM_MARGIN = "lockscreen_bottom_margin";
            public static final String LOCKSCREEN_CLOCK_CUSTOM_FONT = "lockscreen_custom_font";
            public static final String LOCKSCREEN_CLOCK_TEXT_SCALING = "lockscreen_text_scaling";
            public static final String LOCKSCREEN_STOCK_CLOCK_RED_ONE = "lockscreen_stock_clock_red_one_mode";
            public static final String LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR = "lockscreen_stock_clock_red_one_color";
            public static final String LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT1 = "lockscreen_clock_color_code_accent1";
            public static final String LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT2 = "lockscreen_clock_color_code_accent2";
            public static final String LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT3 = "lockscreen_clock_color_code_accent3";
            public static final String LOCKSCREEN_CLOCK_COLOR_CODE_TEXT1 = "lockscreen_clock_color_code_text1";
            public static final String LOCKSCREEN_CLOCK_COLOR_CODE_TEXT2 = "lockscreen_clock_color_code_text2";
            public static final String LOCKSCREEN_CLOCK_CUSTOM_USER = "lockscreen_clock_custom_user_switch";
            public static final String LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE = "lockscreen_clock_custom_user";
            public static final String LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE = "lockscreen_clock_custom_user_image";

            public static final String[] LOCKSCREEN_CLOCK_PREFS = {
                    LOCKSCREEN_CLOCK_SWITCH,
                    LOCKSCREEN_CLOCK_STYLE,
                    LOCKSCREEN_CLOCK_CUSTOM_COLOR_SWITCH,
                    LOCKSCREEN_CLOCK_CUSTOM_COLOR,
                    LOCKSCREEN_CLOCK_TOP_MARGIN,
                    LOCKSCREEN_CLOCK_BOTTOM_MARGIN,
                    LOCKSCREEN_CLOCK_CUSTOM_FONT,
                    LOCKSCREEN_CLOCK_TEXT_SCALING,
                    LOCKSCREEN_CLOCK_LINE_HEIGHT,
                    LOCKSCREEN_STOCK_CLOCK_RED_ONE,
                    LOCKSCREEN_STOCK_CLOCK_RED_ONE_COLOR,
                    LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT1,
                    LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT2,
                    LOCKSCREEN_CLOCK_COLOR_CODE_ACCENT3,
                    LOCKSCREEN_CLOCK_COLOR_CODE_TEXT1,
                    LOCKSCREEN_CLOCK_COLOR_CODE_TEXT2
            };
        }

        public static final String ADAPTIVE_PLAYBACK_ENABLED = "sound_adaptive_playback_main_switch";
        public static final String ADAPTIVE_PLAYBACK_TIMEOUT = "adaptive_playback_timeout";
    }

    public static class LockscreenWeather {
        public static final String LOCKSCREEN_WEATHER_SWITCH = "lockscreen_weather_enabled";
        public static final String LOCKSCREEN_WEATHER_UPDATE_INTERVAL = "weather_update_interval";
        public static final String LOCKSCREEN_WEATHER_PROVIDER = "weather_provider";
        public static final String LOCKSCREEN_WEATHER_OWM_KEY = "owm_key";
        public static final String LOCKSCREEN_WEATHER_UNITS = "weather_units";
        public static final String LOCKSCREEN_WEATHER_CUSTOM_LOCATION = "weather_custom_location_switch";
        public static final String LOCKSCREEN_WEATHER_ICON_PACK = "weather_icon_pack";
        public static final String LOCKSCREEN_WEATHER_TEXT_SIZE = "weather_text_size";
        public static final String LOCKSCREEN_WEATHER_IMAGE_SIZE = "weather_image_size";
        public static final String LOCKSCREEN_WEATHER_SHOW_LOCATION = "weather_show_location";
        public static final String LOCKSCREEN_WEATHER_SHOW_CONDITION = "weather_show_condition";
        public static final String LOCKSCREEN_WEATHER_HUMIDITY = "weather_show_humidity";
        public static final String LOCKSCREEN_WEATHER_WIND = "weather_show_wind";
        public static final String LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH = "weather_custom_color_switch";
        public static final String LOCKSCREEN_WEATHER_CUSTOM_COLOR = "weather_custom_color";

        public static final String[] LOCKSCREEN_WEATHER_PREFS = {
                LOCKSCREEN_WEATHER_SWITCH,
                LOCKSCREEN_WEATHER_UPDATE_INTERVAL,
                LOCKSCREEN_WEATHER_PROVIDER,
                LOCKSCREEN_WEATHER_OWM_KEY,
                LOCKSCREEN_WEATHER_UNITS,
                LOCKSCREEN_WEATHER_CUSTOM_LOCATION,
                LOCKSCREEN_WEATHER_ICON_PACK,
                LOCKSCREEN_WEATHER_TEXT_SIZE,
                LOCKSCREEN_WEATHER_IMAGE_SIZE,
                LOCKSCREEN_WEATHER_HUMIDITY,
                LOCKSCREEN_WEATHER_WIND,
                LOCKSCREEN_WEATHER_SHOW_LOCATION,
                LOCKSCREEN_WEATHER_SHOW_CONDITION,
                LOCKSCREEN_WEATHER_CUSTOM_COLOR_SWITCH,
                LOCKSCREEN_WEATHER_CUSTOM_COLOR
        };

    }

    public static final class SoundPrefs {
        public static final String PULSE_NAVBAR = "navbar_pulse_enabled";
        public static final String PULSE_LOCKSCREEN = "lockscreen_pulse_enabled";
        public static final String PULSE_AMBIENT = "ambient_pulse_enabled";
        public static final String PULSE_RENDER_STYLE = "pulse_render_style";
        public static final String PULSE_SMOOTHING = "pulse_smoothing_enabled";
        public static final String PULSE_COLOR_MODE = "pulse_color_mode";
        public static final String PULSE_COLOR_USER = "pulse_color_user";
        public static final String PULSE_LAVA_SPEED = "pulse_lavalamp_speed";

        // Fading Blocks
        public static final String PULSE_EMPTY_BLOCK_SIZE = "pulse_empty_block_size";
        public static final String PULSE_CUSTOM_DIMEN = "pulse_custom_dimen";
        public static final String PULSE_CUSTOM_DIV = "pulse_custom_div";
        public static final String PULSE_FILLED_BLOCK_SIZE = "pulse_filled_block_size";
        public static final String PULSE_FUDGE_FACTOR = "pulse_custom_fudge_factor";

        // Solid Blocks
        public static final String PULSE_SOLID_UNITS_ROUNDED = "pulse_solid_units_rounded";
        public static final String PULSE_SOLID_UNITS_OPACITY = "pulse_solid_units_opacity";
        public static final String PULSE_SOLID_UNITS_COUNT = "pulse_solid_units_count";
        public static final String PULSE_SOLID_FUDGE_FACTOR = "pulse_solid_fudge_factor";

        public static final String[] PULSE_PREFS = {
                PULSE_NAVBAR,
                PULSE_LOCKSCREEN,
                PULSE_AMBIENT,
                PULSE_RENDER_STYLE,
                PULSE_SMOOTHING,
                PULSE_COLOR_MODE,
                PULSE_COLOR_USER,
                PULSE_LAVA_SPEED,
                PULSE_EMPTY_BLOCK_SIZE,
                PULSE_CUSTOM_DIMEN,
                PULSE_CUSTOM_DIV,
                PULSE_FILLED_BLOCK_SIZE,
                PULSE_FUDGE_FACTOR,
                PULSE_SOLID_UNITS_ROUNDED,
                PULSE_SOLID_UNITS_OPACITY,
                PULSE_SOLID_UNITS_COUNT,
                PULSE_SOLID_FUDGE_FACTOR
        };
    }

    public static final String ACTION_XPOSED_CONFIRMED = BuildConfig.APPLICATION_ID + ".ACTION_XPOSED_CONFIRMED_OC";
    public static final String ACTION_XPOSED_NOT_FOUND = BuildConfig.APPLICATION_ID + ".ACTION_XPOSED_NOT_FOUND_OC";
    public static final String ACTION_CHECK_XPOSED_ENABLED = BuildConfig.APPLICATION_ID + ".ACTION_CHECK_XPOSED_ENABLED_OC";
    public static final String ACTION_CLEAR_ALL_TASKS = BuildConfig.APPLICATION_ID + ".ACTION_CLEAR_ALL_TASKS_OC";
    public static final String ACTION_POWER_MENU = BuildConfig.APPLICATION_ID + ".ACTION_POWER_MENU_OC";
    public static final String ACTION_AUTH_SUCCESS_SHOW_ADVANCED_REBOOT = BuildConfig.APPLICATION_ID + ".ACTION_AUTH_SUCCESS_SHOW_ADVANCED_REBOOT_OC";

    public static final String ACTION_MAX_CHANGED = BuildConfig.APPLICATION_ID + ".ACTION_MAX_CHANGED_OC";

    public static final String XPOSED_RESOURCE_TEMP_DIR = Environment.getExternalStorageDirectory() + "/.oxygen_customizer";
    public static final String HEADER_IMAGE_DIR = XPOSED_RESOURCE_TEMP_DIR + "/header_image.png";
    public static final String HEADER_CLOCK_FONT_DIR = XPOSED_RESOURCE_TEMP_DIR + "/header_clock_font.ttf";
    public static final String LOCKSCREEN_CLOCK_FONT_DIR = XPOSED_RESOURCE_TEMP_DIR + "/lockscreen_clock_font.ttf";
    public static final String LOCKSCREEN_USER_IMAGE = XPOSED_RESOURCE_TEMP_DIR + "/lockscreen_user_image.png";
    public static final String LOCKSCREEN_FINGERPRINT_FILE = XPOSED_RESOURCE_TEMP_DIR + "/lockscreen_fp_icon.png";

    // View Tags
    public static final String MEDIA_PROGRESSBAR = "media_progressbar";
    public static final String MEDIA_PROGRESSBAR_VALUE = "media_progress_value";
    public static final String BATTERY_PROGRESSBAR = "battery_progressbar";
    public static final String BATTERY_PROGRESSBAR_VALUE = "battery_progress_value";

    // Resource names
    public static final String LOCKSCREEN_CLOCK_LAYOUT = "preview_lockscreen_clock_";
    public static final String HEADER_CLOCK_LAYOUT = "preview_header_clock_";

    public static final String CLOCK_TAG = "clock";
    public static final String DATE_TAG = "date";

}
