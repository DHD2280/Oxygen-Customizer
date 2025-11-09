package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;
import static it.dhd.oxygencustomizer.utils.AppUtils.doesClassExist;
import static it.dhd.oxygencustomizer.utils.AppUtils.getAppName;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_COLOR_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_RETICK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_RETICK_DURATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodEdgeLight.EDGE_LIGHT_WIDTH;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_KIM;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_ONE_UI7;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_ATTACH_TO_BB;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_CHARGING_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_FAST_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_INDICATE_CHARGING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_INDICATE_FAST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_INDICATE_POWERSAVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_TEXT_POWERSAVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOMIZE_BATTERY_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_ANIM_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_BLEND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HIDE_PERCENTAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_WIDTH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.STOCK_CUSTOMIZE_PERCENTAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.STOCK_PERCENTAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_AI_STATUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_AOD;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_AOD_OPACITY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_BACKGROUND;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_CATEGORY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_OPACITY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.DepthWallpaper.DEPTH_WALLPAPER_SUBJECT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_HOLD_BACK_LEFT_APP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_HOLD_BACK_RIGHT_APP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_OVERRIDE_HOLDBACK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_OVERRIDE_HOLDBACK_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_OVERRIDE_HOLDBACK_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_OVERRIDE_HOLDBACK_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_KEEP_SHUFFLING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_SHUFFLE_PIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN_AOD;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_DEVICE_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_DATE_FORMAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CHARGING_ICON_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CHARGING_ICON_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_COLOR_4;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_CUSTOM_COLORS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_FAST_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_INDICATE_FAST;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_INDICATE_POWERSAVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BATTERY_POWERSAVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXDENDED_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXTENDED_BACKGROUND;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXTENDED_CLOCK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_MUSIC_EXTENDED_PLAYER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATION_1LINE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATION_2LINE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATION_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATION_CUSTOM_COLORS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_NOTIFICATION_ICON_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER_BACKGROUND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER_CUSTOM_COLORS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenNowBar.NOW_BAR_WEATHER_TEXT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_BDX;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_BSX;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_BUTTONS_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_SUMMARY_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_TDX;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_TITLE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CARD_TSX;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CLEAR_ALL_COUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_CLEAR_ALL_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_ICON_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_ICON_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_ICON_PADDING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_ICON_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_ICON_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_NOTIFICATIONS_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenPeekNotifications.LOCKSCREEN_PEEK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_1_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_2_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_3_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.EXTRA_WIDGET_4_KEY;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_EXTRAS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SCALE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_ICON_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_SMALL_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.LOCKSCREEN_WIDGETS_WEATHER_SETTINGS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.MAIN_WIDGET_1_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenWidgets.MAIN_WIDGET_2_KEY;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_DARK_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_BRIGHTNESS_SLIDER_CUSTOMIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_FILTER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_ART_TINT_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_SHOW_ALBUM_ART;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_TILE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_TILE_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_TILE_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_MEDIA_TILE_RADIUS_TOTAL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_SLIDERS_BLEND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_SLIDERS_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_SLIDERS_RADIUS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_SLIDERS_REMOVE_BLUR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_HIGHLIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_DURATION;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_INTERPOLATOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_TRANSFORMATIONS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ANIMATION_TRANSFORMATIONS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_CUSTOM_COLORS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_HIGHLIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_HIGHLIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_DISABLED_COLOR_HIGHLIGHT_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIDE_LABELS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHLIGHT_CUSTOM_ICON_BG_CAT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_HIGHTLIGHT_RADIUS_TOTAL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ICON_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ICON_CUSTOM_COLOR_ACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ICON_CUSTOM_COLOR_ACTIVE_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ICON_CUSTOM_COLOR_DISABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_ICON_CUSTOM_COLOR_INACTIVE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_HIGHLIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_LABELS_CUSTOM_COLOR_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_BOTTOM_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOP_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsTilesCustomization.QS_TILE_RADIUS_TOTAL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.BLUR_RADIUS_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_BLUR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_MAX_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_VAL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_CUSTOM_WIDTH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.SeparateQsPrefs.SEPARATE_QS_WIDTH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_APPLY_TINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Statusbar.STATUSBAR_LOGO_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_BG_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CLEAR_BUTTON_ICON_LINK_ACCENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.CUSTOMIZE_CLEAR_BUTTON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIFICATIONS_SHOW_BUTTONS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_OWM_KEY;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_PROVIDER;
import static it.dhd.oxygencustomizer.utils.Constants.Weather.WEATHER_YANDEX_KEY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;

import com.topjohnwu.superuser.Shell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import it.dhd.oneplusui.preference.OplusSliderPreference;
import it.dhd.oneplusui.preference.OplusSwitchPreference;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedSharedPreferences;
import it.dhd.oxygencustomizer.xposed.views.edgelight.EdgeLightView;

public class PreferenceHelper {
    public static boolean showOverlays, showFonts;

    public final ExtendedSharedPreferences mPreferences;
    public final String mOsVersion;
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
        addAll(Arrays.asList(25, 30, 39, 40, 53, 58, 59));
    }};

    private final List<Integer> LsClockUserVisible = new ArrayList<>() {{
        addAll(Arrays.asList(7, 32, 35, 36, 42, 48, 50, 53, 58, 59));
    }};

    private final List<Integer> LsClockDeviceVisible = new ArrayList<>() {{
        addAll(Arrays.asList(19, 32, 47));
    }};

    public static void init(ExtendedSharedPreferences prefs) {
        new PreferenceHelper(prefs);
    }

    private PreferenceHelper(ExtendedSharedPreferences prefs) {
        mPreferences = prefs;
        mOsVersion = Shell.cmd("getprop ro.build.display.id").exec().getOut().get(0);
        instance = this;
    }

    public static SharedPreferences getModulePrefs() {
        if (instance != null) return instance.mPreferences;
        return null;
    }

    public static String getOsVersion() {
        if (instance != null) return instance.mOsVersion;
        return "";
    }

    public static int getOOSVersion() {
        if (instance == null) return -1;
        String[] split = instance.mOsVersion.split("\\.");
        String version = split[split.length - 1].substring(0, split[split.length - 1].indexOf("("));
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            Log.getStackTraceString(e);
            return -1;
        }
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
            case "disable_previous_recents",
                 "dock_background_jump" -> {
                return Build.VERSION.SDK_INT >= 35;
            }

            // Statusbar Prefs
            case "statusbarPaddings", "statusbar_top_padding" -> {
                return instance.mPreferences.getBoolean("statusbar_padding_enabled", false);
            }
            case "status_bar_clock_color" -> {
                return instance.mPreferences.getBoolean("status_bar_custom_clock_color", false);
            }
            // Notification
            case "statusbar_notification_app_icon_scale" -> {
                return instance.mPreferences.getBoolean("statusbar_notification_app_icon", false);
            }
            case NOTIFICATIONS_SHOW_BUTTONS -> {
                return Build.VERSION.SDK_INT >= 35;
            }
            // Statusbar Logo
            case STATUSBAR_LOGO_APPLY_TINT -> {
                return instance.mPreferences.getString(STATUSBAR_LOGO_STYLE, "0").equals("-1");
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
            case "notif_transparency_cat" -> {
                return Build.VERSION.SDK_INT < 35;
            }
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
                 "category_battery_charging_icon",
                 "battery_text",
                 CUSTOM_BATTERY_ANIM_ENABLED -> {
                return isVisibleBattery(key);
            }
            case BATTERY_TEXT_CHARGING_COLOR -> {
                return instance.mPreferences.getBoolean(BATTERY_TEXT_INDICATE_CHARGING, false);
            }
            case BATTERY_TEXT_FAST_COLOR -> {
                return instance.mPreferences.getBoolean(BATTERY_TEXT_INDICATE_FAST, false);
            }
            case BATTERY_TEXT_POWERSAVE_COLOR -> {
                return instance.mPreferences.getBoolean(BATTERY_TEXT_INDICATE_POWERSAVE, false);
            }
            case STOCK_PERCENTAGE_SIZE -> {
                return instance.mPreferences.getBoolean(STOCK_CUSTOMIZE_PERCENTAGE_SIZE, false);
            }

            // QuickSettings Prefs
            case "quick_pulldown_side", "quick_pulldown_length" -> {
                return instance.mPreferences.getBoolean("quick_pulldown", false);
            }
            case QS_TRANSPARENCY_SWITCH,
                 QSPANEL_BLUR_SWITCH -> {
                return Build.VERSION.SDK_INT < 35;
            }
            case QS_TRANSPARENCY_VAL -> {
                return instance.mPreferences.getBoolean(QS_TRANSPARENCY_SWITCH, false) && Build.VERSION.SDK_INT < 35;
            }
            case BLUR_RADIUS_VALUE -> {
                return instance.mPreferences.getBoolean(QSPANEL_BLUR_SWITCH, false) && Build.VERSION.SDK_INT < 35;
            }
            case QSPANEL_MAX_BLUR_AMOUNT -> {
                return Build.VERSION.SDK_INT >= 35;
            }

            // Separate Qs
            case SEPARATE_QS_WIDTH -> {
                return instance.mPreferences.getBoolean(SEPARATE_QS_CUSTOM_WIDTH, false);
            }

            // Qs Tiles
            case "quick_settings_quick_tiles",
                 "qs_tile_potrait",
                 "qs_tile_landscape" -> {
                return instance.mPreferences.getBoolean("quick_settings_tiles_customize", false);
            }

            // Qs Appearance
            // Tile colors
            // Common
            case QS_TILE_ACTIVE_COLOR_ENABLED,
                 QS_TILE_INACTIVE_COLOR_ENABLED,
                 QS_TILE_DISABLED_COLOR_ENABLED,
                 QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ENABLED,
                 QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ENABLED,
                 QS_TILE_DISABLED_COLOR_HIGHLIGHT_ENABLED -> {
                return Build.VERSION.SDK_INT < 35;
            }
            // Highlight
            case QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH -> {
                return Build.VERSION.SDK_INT >= 35;
            }
            case QS_TILE_ACTIVE_COLOR_HIGHLIGHT -> {
                return instance.mPreferences.getBoolean(QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ENABLED, false) ||
                        instance.mPreferences.getBoolean(QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH, false);
            }
            case QS_TILE_INACTIVE_COLOR_HIGHLIGHT -> {
                return instance.mPreferences.getBoolean(QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ENABLED, false) ||
                        instance.mPreferences.getBoolean(QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH, false);
            }
            case QS_TILE_DISABLED_COLOR_HIGHLIGHT -> {
                return instance.mPreferences.getBoolean(QS_TILE_DISABLED_COLOR_HIGHLIGHT_ENABLED, false) ||
                        instance.mPreferences.getBoolean(QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH, false);
            }
            // Icon BG
            case QS_TILE_HIGHLIGHT_CUSTOM_ICON_BG_CAT -> {
                return Build.VERSION.SDK_INT >= 35;
            }
            case QS_TILE_ACTIVE_COLOR_HIGHLIGHT_ICON,
                 QS_TILE_INACTIVE_COLOR_HIGHLIGHT_ICON,
                 QS_TILE_DISABLED_COLOR_HIGHLIGHT_ICON -> {
                return Build.VERSION.SDK_INT >= 35 &&
                        instance.mPreferences.getBoolean(QS_TILE_HIGHLIGHT_CUSTOM_COLORS_SWITCH_ICON, false);
            }
            // Icon color
            case QS_TILE_ICON_CUSTOM_COLOR -> {
                return Build.VERSION.SDK_INT >= 35;
            }
            case QS_TILE_ICON_CUSTOM_COLOR_ACTIVE_ACCENT,
                 QS_TILE_ICON_CUSTOM_COLOR_ACTIVE,
                 QS_TILE_ICON_CUSTOM_COLOR_INACTIVE,
                 QS_TILE_ICON_CUSTOM_COLOR_DISABLED -> {
                return isVisible(QS_TILE_ICON_CUSTOM_COLOR) &&
                        instance.mPreferences.getBoolean(QS_TILE_ICON_CUSTOM_COLOR, false);
            }
            // Base
            case QS_TILE_CUSTOM_COLORS_SWITCH -> {
                return Build.VERSION.SDK_INT >= 35;
            }
            case QS_TILE_ACTIVE_COLOR -> {
                return Build.VERSION.SDK_INT >= 35 ?
                        instance.mPreferences.getBoolean(QS_TILE_CUSTOM_COLORS_SWITCH, false) :
                        instance.mPreferences.getBoolean(QS_TILE_ACTIVE_COLOR_ENABLED, false);
            }
            case QS_TILE_INACTIVE_COLOR -> {
                return Build.VERSION.SDK_INT >= 35 ?
                        instance.mPreferences.getBoolean(QS_TILE_CUSTOM_COLORS_SWITCH, false) :
                        instance.mPreferences.getBoolean(QS_TILE_INACTIVE_COLOR_ENABLED, false);
            }
            case QS_TILE_DISABLED_COLOR -> {
                return Build.VERSION.SDK_INT >= 35 ?
                        instance.mPreferences.getBoolean(QS_TILE_CUSTOM_COLORS_SWITCH, false) :
                        instance.mPreferences.getBoolean(QS_TILE_DISABLED_COLOR_ENABLED, false);
            }
            // Media
            case QS_MEDIA_TILE_COLOR -> {
                return instance.mPreferences.getBoolean(QS_MEDIA_TILE_CUSTOM_COLOR, false);
            }
            // Sliders
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
            case QS_SLIDERS_REMOVE_BLUR,
                 QS_SLIDERS_BLEND_COLOR,
                 QS_BRIGHTNESS_DARK_ICON,
                 QS_SLIDERS_RADIUS_SWITCH -> {
                return Build.VERSION.SDK_INT >= 35;
            }
            case QS_SLIDERS_RADIUS -> {
                return Build.VERSION.SDK_INT >= 35 &&
                        instance.mPreferences.getBoolean(QS_SLIDERS_RADIUS_SWITCH, false);
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
                return instance.mPreferences.getBoolean(QS_TILE_HIGHTLIGHT_RADIUS, false) && Build.VERSION.SDK_INT < 35;
            }
            case QS_TILE_HIGHTLIGHT_RADIUS_TOTAL -> {
                return instance.mPreferences.getBoolean(QS_TILE_HIGHTLIGHT_RADIUS, false) && Build.VERSION.SDK_INT >= 35;
            }
            case QS_TILE_RADIUS_TOP_LEFT,
                 QS_TILE_RADIUS_TOP_RIGHT,
                 QS_TILE_RADIUS_BOTTOM_LEFT,
                 QS_TILE_RADIUS_BOTTOM_RIGHT -> {
                return instance.mPreferences.getBoolean(QS_TILE_RADIUS, false) && Build.VERSION.SDK_INT < 35;
            }
            case QS_TILE_RADIUS_TOTAL -> {
                return instance.mPreferences.getBoolean(QS_TILE_RADIUS, false) && Build.VERSION.SDK_INT >= 35;
            }
            case QS_MEDIA_TILE_RADIUS_TOTAL -> {
                return instance.mPreferences.getBoolean(QS_MEDIA_TILE_RADIUS, false);
            }

            // Separate QS
            case "qs_separate_jump" -> {
                return Build.VERSION.SDK_INT >= 35;
            }

            // Gesture Prefs
            case "gesture_left_height_double" -> {
                return instance.mPreferences.getBoolean("gesture_left", false);
            }
            case "gesture_right_height_double" -> {
                return instance.mPreferences.getBoolean("gesture_right", false);
            }
            case GESTURE_OVERRIDE_HOLDBACK_LEFT, GESTURE_OVERRIDE_HOLDBACK_MODE -> {
                return instance.mPreferences.getBoolean(GESTURE_OVERRIDE_HOLDBACK, false);
            }
            case GESTURE_OVERRIDE_HOLDBACK_RIGHT -> {
                String mode = instance.mPreferences.getString(GESTURE_OVERRIDE_HOLDBACK_MODE, "0");
                return mode.equals("1") && instance.mPreferences.getBoolean(GESTURE_OVERRIDE_HOLDBACK, false);
            }
            case GESTURE_HOLD_BACK_LEFT_APP -> {
                return instance.mPreferences.getString(GESTURE_OVERRIDE_HOLDBACK_LEFT, "0").equals("11");
            }

            case GESTURE_HOLD_BACK_RIGHT_APP -> {
                String action = instance.mPreferences.getString(GESTURE_OVERRIDE_HOLDBACK_RIGHT, "0");
                return action.equals("11") && isVisible(GESTURE_OVERRIDE_HOLDBACK_RIGHT);
            }

            case "leftSwipeUpPercentage" -> {
                return !instance.mPreferences.getString("leftSwipeUpAction", "-1").equals("-1");
            }
            case "rightSwipeUpPercentage" -> {
                return !instance.mPreferences.getString("rightSwipeUpAction", "-1").equals("-1");
            }

            case "swipeUpPercentage" -> {
                return !instance.mPreferences.getString("leftSwipeUpAction", "-1").equals("-1") ||
                        !instance.mPreferences.getString("rightSwipeUpAction", "-1").equals("-1") ||
                        !instance.mPreferences.getString("twoFingerSwipeUpAction", "-1").equals("-1");
            }

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
            case "qs_header_clock_notice_oos15" -> {
                return Build.VERSION.SDK_INT >= 35 &
                        instance.mPreferences.getBoolean("qs_header_clock_custom_enabled", false);
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

            case DEPTH_WALLPAPER_CATEGORY,
                 DEPTH_WALLPAPER_ENABLED -> {
                return Build.VERSION.SDK_INT >= 34;
            }
            case DEPTH_WALLPAPER_BACKGROUND,
                 DEPTH_WALLPAPER_SUBJECT -> {
                return instance.mPreferences.getString("DWMode", "0").equals("1");
            }
            case DEPTH_WALLPAPER_AOD_OPACITY -> {
                return instance.mPreferences.getBoolean(DEPTH_WALLPAPER_AOD_OPACITY, false);
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
            case LOCKSCREEN_KEEP_SHUFFLING -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_SHUFFLE_PIN, false);
            }

            // Lockscreen Clock
            case "lockscreen_clock_custom", "lockscreen_clock_prefs",
                 "lockscreen_clock_custom_margins", "lockscreen_clock_font_prefs" -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false);
            }
            case LOCKSCREEN_CLOCK_BOTTOM_MARGIN_AOD -> {
                return Build.VERSION.SDK_INT >= 35;
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
                return !instance.mPreferences.getBoolean(LOCKSCREEN_CLOCK_SWITCH, false) && Build.VERSION.SDK_INT < 35;
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

            case LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR -> {
                return instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET, false) &&
                        instance.mPreferences.getBoolean(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH, false);
            }

            case LOCKSCREEN_WIDGETS_DEVICE_WIDGET_LINEAR_COLOR -> {
                return isVisible(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CIRCULAR_COLOR) &&
                        instance.mPreferences.getString(LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE, "0").equals("0");
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

            // Lockscreen Now Bar
            case NOW_BAR_MUSIC_EXDENDED_MODE,
                 NOW_BAR_MUSIC_EXTENDED_BACKGROUND,
                 NOW_BAR_MUSIC_EXTENDED_CLOCK -> {
                return instance.mPreferences.getBoolean(NOW_BAR_MUSIC_EXTENDED_PLAYER, false);
            }
            case NOW_BAR_BATTERY_CHARGING_ICON_STYLE -> {
                return instance.mPreferences.getBoolean(NOW_BAR_BATTERY_CHARGING_ICON_SWITCH, false);
            }
            case NOW_BAR_BATTERY_COLOR_1,
                 NOW_BAR_BATTERY_COLOR_2,
                 NOW_BAR_BATTERY_COLOR_3,
                 NOW_BAR_BATTERY_COLOR_4 -> {
                return instance.mPreferences.getBoolean(NOW_BAR_BATTERY_CUSTOM_COLORS, false);
            }
            case NOW_BAR_BATTERY_FAST_COLOR -> {
                return instance.mPreferences.getBoolean(NOW_BAR_BATTERY_INDICATE_FAST, false);
            }
            case NOW_BAR_BATTERY_POWERSAVE_COLOR -> {
                return instance.mPreferences.getBoolean(NOW_BAR_BATTERY_INDICATE_POWERSAVE, false);
            }
            case NOW_BAR_WEATHER_BACKGROUND_COLOR,
                 NOW_BAR_WEATHER_TEXT_COLOR -> {
                return instance.mPreferences.getBoolean(NOW_BAR_WEATHER_CUSTOM_COLORS, false);
            }
            case NOW_BAR_NOTIFICATION_BG_COLOR,
                 NOW_BAR_NOTIFICATION_1LINE_COLOR,
                 NOW_BAR_NOTIFICATION_2LINE_COLOR,
                 NOW_BAR_NOTIFICATION_ICON_COLOR -> {
                return instance.mPreferences.getBoolean(NOW_BAR_NOTIFICATION_CUSTOM_COLORS, false);
            }

            // Peek notifications
            case "peek_card_radius_cat",
                 LOCKSCREEN_PEEK_CARD_BG_COLOR,
                 LOCKSCREEN_PEEK_CARD_BUTTONS_COLOR,
                 LOCKSCREEN_PEEK_CARD_TITLE_COLOR,
                 LOCKSCREEN_PEEK_CARD_SUMMARY_COLOR,
                 "peek_icon_style_cat" -> {
                return instance.mPreferences.getString(LOCKSCREEN_PEEK_NOTIFICATIONS_STYLE, "0").equals("2");
            }
            // Peek Icon Style
            case LOCKSCREEN_PEEK_ICON_BG_COLOR,
                 LOCKSCREEN_PEEK_ICON_SIZE,
                 LOCKSCREEN_PEEK_ICON_MARGIN,
                 LOCKSCREEN_PEEK_ICON_PADDING -> {
                return instance.mPreferences.getString(LOCKSCREEN_PEEK_ICON_STYLE, "0").equals("2");
            }
            case LOCKSCREEN_PEEK_CLEAR_ALL_COUNT -> {
                return instance.mPreferences.getString(LOCKSCREEN_PEEK_CLEAR_ALL_MODE, "1").equals("1");
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

            // Edge Light
            case Constants.Preferences.AodEdgeLight.EDGE_LIGHT_CUSTOM_COLOR -> {
                return Integer.parseInt(instance.mPreferences.getString(EDGE_LIGHT_COLOR_MODE, "0")) == EdgeLightView.ColorMode.CUSTOM.ordinal();
            }

            // Volume Panel Customization
            case "volume_panel_seekbar_link_primary" -> {
                return instance.mPreferences.getBoolean("volume_panel_seekbar_color_enabled", false);
            }
            case "volume_panel_seekbar_color" -> {
                return instance.mPreferences.getBoolean("volume_panel_seekbar_color_enabled", false) &&
                        !instance.mPreferences.getBoolean("volume_panel_seekbar_link_primary", false);
            }
            case "volume_panel_seekbar_bg_color_enabled" -> {
                return switch (Build.VERSION.SDK_INT) {
                    case 35 -> getOOSVersion() <= 840;
                    case 34 -> true;
                    default -> Build.VERSION.SDK_INT >= 36 ? false : true;
                };
            }
            case "volume_panel_seekbar_bg_color" -> {
                return isVisible("volume_panel_seekbar_bg_color_enabled") && instance.mPreferences.getBoolean("volume_panel_seekbar_bg_color_enabled", false);
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
        boolean isKim = batteryStyle == BATTERY_STYLE_LANDSCAPE_KIM;

        boolean showAdvancedCustomizations = batteryStyle >= Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYA &&
                batteryStyle <= Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYO;
        boolean showRainbowBattery = batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYI ||
                batteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYJ;
        boolean showCommonCustomizations = instance.mPreferences.getBoolean(CUSTOMIZE_BATTERY_ICON, false);
        boolean showPercentage = batteryStyle != BATTERY_STYLE_DEFAULT &&
                batteryStyle != BATTERY_STYLE_DEFAULT_LANDSCAPE &&
                batteryStyle != BATTERY_STYLE_DEFAULT_RLANDSCAPE &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_IOS_16 &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_BATTERYL &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_BATTERYM &&
                batteryStyle != BATTERY_STYLE_LANDSCAPE_ONE_UI7;
        boolean showInsidePercentage = showPercentage && !isKim && !instance.mPreferences.getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false);
        boolean showChargingIconCustomization = instance.mPreferences.getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false);
        boolean circleBattery = batteryStyle == BATTERY_STYLE_CIRCLE ||
                batteryStyle == BATTERY_STYLE_DOTTED_CIRCLE ||
                batteryStyle == BATTERY_STYLE_FILLED_CIRCLE;
        boolean showColorPickers = (showAdvancedCustomizations || circleBattery) && instance.mPreferences.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false);

        return switch (key) {
            case "category_battery_icon_settings",
                 "battery_icon_style" -> showCommonCustomizations;
            case "category_battery_charging_icon" -> showChargingIconCustomization;
            case "battery_perimeter_alpha",
                 "battery_fill_alpha",
                 "battery_rotate_layout" -> showAdvancedCustomizations;
            case "battery_reverse_layout" -> showAdvancedCustomizations || isKim;
            case "battery_inside_percentage" -> showInsidePercentage;
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
            case "battery_text" -> showCommonCustomizations ? showPercentage : true;
            case "category_battery_colors" ->
                    showCommonCustomizations && (showAdvancedCustomizations || showRainbowBattery || showColorPickers || circleBattery);
            case CUSTOM_BATTERY_ANIM_ENABLED -> showCommonCustomizations && circleBattery;
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
            case QS_SLIDERS_BLEND_COLOR ->
                    !instance.mPreferences.getBoolean(QS_SLIDERS_REMOVE_BLUR, false);
            // Lockscreen Widgets
            case LOCKSCREEN_WIDGETS_DEVICE_WIDGET_CUSTOM_COLOR_SWITCH,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_TEXT_COLOR,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_DEVICE,
                 LOCKSCREEN_WIDGETS_DEVICE_WIDGET_STYLE,
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

            // Depth Wallpaper
            case DEPTH_WALLPAPER_MODE,
                 DEPTH_WALLPAPER_AI_STATUS,
                 DEPTH_WALLPAPER_OPACITY,
                 DEPTH_WALLPAPER_AOD,
                 DEPTH_WALLPAPER_BACKGROUND,
                 DEPTH_WALLPAPER_SUBJECT ->
                    instance.mPreferences.getBoolean(DEPTH_WALLPAPER_ENABLED, false);

            case EDGE_LIGHT_RETICK_DURATION ->
                    instance.mPreferences.getBoolean(EDGE_LIGHT_ENABLED, false) &&
                            instance.mPreferences.getBoolean(EDGE_LIGHT_RETICK, false);

            case "fix_lag_force_all_apps" ->
                    instance.mPreferences.getBoolean("fix_lag_switch", false);
            case "fix_lag_app_chooser" ->
                    instance.mPreferences.getBoolean("fix_lag_switch", false) &&
                            !instance.mPreferences.getBoolean("fix_lag_force_all_apps", false);

            case "moreLogging" -> !BuildConfig.VERSION_NAME.contains("nightly");

            // Battery Text
            case BATTERY_TEXT_ATTACH_TO_BB ->
                    instance.mPreferences.getBoolean("BBarEnabled", false);

            case BATTERY_TEXT_INDICATE_POWERSAVE,
                 BATTERY_TEXT_INDICATE_CHARGING,
                 BATTERY_TEXT_INDICATE_FAST,
                 BATTERY_TEXT_CHARGING_COLOR,
                 BATTERY_TEXT_FAST_COLOR,
                 BATTERY_TEXT_POWERSAVE_COLOR ->
                    instance.mPreferences.getBoolean(BATTERY_TEXT_ATTACH_TO_BB, false);

            case QS_TILE_ICON_CUSTOM_COLOR_ACTIVE ->
                    !instance.mPreferences.getBoolean(QS_TILE_ICON_CUSTOM_COLOR_ACTIVE_ACCENT, false);
            case "dockBackground" ->
                    !instance.mPreferences.getBoolean("dockBackgroundMaterial", false);
            case "dockBackgroundMaterial" ->
                    !instance.mPreferences.getBoolean("dockBackground", false);

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
            case "statusbar_top_padding" ->
                instance.mPreferences.getSliderInt("statusbar_top_padding", 0) + "dp";
            case "statusbarPaddings" -> {
                List<Float> statusbarPaddings = instance.mPreferences.getSliderValues("statusbarPaddings", 0);
                float start, end;
                if (statusbarPaddings.isEmpty()) {
                    start = -1;
                    end = 101f;
                } else {
                    start = statusbarPaddings.get(0);
                    end = statusbarPaddings.size() > 1 ? statusbarPaddings.get(1) : 101f;
                }
                yield fragmentCompat.getString(R.string.sb_padding_summary) + "\n" +
                        String.format("%s - %s", start, end);
            }


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

            // Separate Qs
            case SEPARATE_QS_WIDTH ->
                    (instance.mPreferences.getSliderInt(SEPARATE_QS_WIDTH, 50)) + "%";

            // Tile Radius
            case QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_RIGHT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOP_LEFT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_RIGHT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_BOTTOM_LEFT, 0) + "dp";
            case QS_TILE_HIGHTLIGHT_RADIUS_TOTAL ->
                    instance.mPreferences.getSliderInt(QS_TILE_HIGHTLIGHT_RADIUS_TOTAL, 0) + "dp";
            case QS_TILE_RADIUS_TOP_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_TOP_LEFT, 0) + "dp";
            case QS_TILE_RADIUS_TOP_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_TOP_RIGHT, 0) + "dp";
            case QS_TILE_RADIUS_BOTTOM_LEFT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_BOTTOM_LEFT, 0) + "dp";
            case QS_TILE_RADIUS_BOTTOM_RIGHT ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_BOTTOM_RIGHT, 0) + "dp";
            case QS_TILE_RADIUS_TOTAL ->
                    instance.mPreferences.getSliderInt(QS_TILE_RADIUS_TOTAL, 0) + "dp";
            case QS_MEDIA_TILE_RADIUS_TOTAL ->
                    instance.mPreferences.getSliderInt(QS_MEDIA_TILE_RADIUS_TOTAL, 0) + "dp";


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

            case QS_SLIDERS_RADIUS ->
                    instance.mPreferences.getSliderInt(QS_SLIDERS_RADIUS, 20) + "dp";

            // Statusbar
            case "status_bar_clock_size" ->
                    instance.mPreferences.getSliderInt("status_bar_clock_size", 12) + "sp";
            case "status_bar_clock_auto_hide_hduration" ->
                    fragmentCompat.getString(R.string.status_bar_clock_auto_hide_hdur_summary) + "\n" +
                            String.format(fragmentCompat.getString(R.string.duration_seconds),
                                    instance.mPreferences.getSliderInt("status_bar_clock_auto_hide_hduration", 60));
            case "status_bar_clock_auto_hide_sduration" ->
                    fragmentCompat.getString(R.string.status_bar_clock_auto_hide_sdur_summary) + "\n" +
                            String.format(fragmentCompat.getString(R.string.duration_seconds),
                                    instance.mPreferences.getSliderInt("status_bar_clock_auto_hide_sduration", 5));

            case NOTIF_TRANSPARENCY_VALUE ->
                    String.valueOf(instance.mPreferences.getSliderInt(NOTIF_TRANSPARENCY_VALUE, 25));
            case "statusbar_notification_app_icon_scale" ->
                    String.valueOf(instance.mPreferences.getSliderFloat("statusbar_notification_app_icon_scale", 1.0f)) + "%";
            // Statusbar Logo
            case STATUSBAR_LOGO_SIZE ->
                    instance.mPreferences.getSliderInt(STATUSBAR_LOGO_SIZE, 18) + "dp";


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
            case STOCK_PERCENTAGE_SIZE ->
                    instance.mPreferences.getSliderInt(STOCK_PERCENTAGE_SIZE, 12) + "sp";

            // Gesture Prefs
            case "gesture_left_height_double" -> getGestureHeight(key);
            case "gesture_right_height_double" -> getGestureHeight(key);
            case "GesPillWidthModPos" ->
                    instance.mPreferences.getSliderInt("GesPillWidthModPos", 0) + "%";

            case "leftSwipeUpPercentage" ->
                    instance.mPreferences.getSliderInt("leftSwipeUpPercentage", 25) + "%";

            case "rightSwipeUpPercentage" ->
                    instance.mPreferences.getSliderInt("rightSwipeUpPercentage", 25) + "%";

            case "swipeUpPercentage" ->
                    instance.mPreferences.getSliderInt("swipeUpPercentage", 5) + "%";
            case GESTURE_HOLD_BACK_LEFT_APP,
                 GESTURE_HOLD_BACK_RIGHT_APP ->
                    TextUtils.isEmpty(instance.mPreferences.getString(key, "")) ?
                            fragmentCompat.getString(R.string.select_app) :
                            getAppName(fragmentCompat, instance.mPreferences.getString(key, ""));

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
            case DEPTH_WALLPAPER_OPACITY ->
                    instance.mPreferences.getSliderInt(DEPTH_WALLPAPER_OPACITY, 192) + "dp";
            case DEPTH_WALLPAPER_AOD_OPACITY ->
                    instance.mPreferences.getSliderInt(DEPTH_WALLPAPER_AOD_OPACITY, 192) + "dp";
            case "lockscreen_media_blur" ->
                    instance.mPreferences.getSliderInt("lockscreen_media_blur", 35) + "%";
            case LOCKSCREEN_FINGERPRINT_STYLE ->
                    String.format(fragmentCompat.getString(R.string.lockscreen_fp_style), Integer.parseInt(instance.mPreferences.getString(LOCKSCREEN_FINGERPRINT_STYLE, "0")));

            // Lockscreen Clock
            case LOCKSCREEN_CLOCK_LINE_HEIGHT ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_LINE_HEIGHT, 100) + "dp";
            case LOCKSCREEN_CLOCK_TEXT_SCALING ->
                    instance.mPreferences.getSliderFloat(LOCKSCREEN_CLOCK_TEXT_SCALING, 1.0f) + "%";
            case LOCKSCREEN_CLOCK_TOP_MARGIN ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_TOP_MARGIN, 0) + "dp";
            case LOCKSCREEN_CLOCK_BOTTOM_MARGIN ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_BOTTOM_MARGIN, 0) + "dp";
            case LOCKSCREEN_CLOCK_BOTTOM_MARGIN_AOD ->
                    fragmentCompat.getString(R.string.lockscreen_clock_bottom_margin_aod_summary) + "\n" +
                            instance.mPreferences.getSliderInt(LOCKSCREEN_CLOCK_BOTTOM_MARGIN_AOD, 0) + "dp";

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
            case LOCKSCREEN_WIDGETS_TOP_MARGIN ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_WIDGETS_TOP_MARGIN, 0) + "dp";

            // Now Bar
            case NOW_BAR_BOTTOM_MARGIN ->
                    instance.mPreferences.getSliderInt(NOW_BAR_BOTTOM_MARGIN, 0) + "dp";
            case NOW_BAR_MUSIC_CLOCK_TEXT_SCALING ->
                    instance.mPreferences.getSliderFloat(NOW_BAR_MUSIC_CLOCK_TEXT_SCALING, 1.0f) + "%";
            case NOW_BAR_CLOCK_TOP_MARGIN ->
                    instance.mPreferences.getSliderInt(NOW_BAR_CLOCK_TOP_MARGIN, 38) + "dp";

            // Peek Notifications
            case LOCKSCREEN_PEEK_CARD_TSX ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_PEEK_CARD_TSX, 26) + "dp";
            case LOCKSCREEN_PEEK_CARD_TDX ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_PEEK_CARD_TDX, 26) + "dp";
            case LOCKSCREEN_PEEK_CARD_BSX ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_PEEK_CARD_BSX, 26) + "dp";
            case LOCKSCREEN_PEEK_CARD_BDX ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_PEEK_CARD_BDX, 26) + "dp";
            case LOCKSCREEN_PEEK_TOP_MARGIN ->
                    instance.mPreferences.getSliderInt(LOCKSCREEN_PEEK_TOP_MARGIN, 0) + "dp";

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

            // Edge Light
            case EDGE_LIGHT_WIDTH ->
                    fragmentCompat.getString(R.string.edge_light_stroke_width_summary) + "\n" +
                            instance.mPreferences.getSliderFloat(EDGE_LIGHT_WIDTH, 20f) + " dp";

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
                    String.format(fragmentCompat.getString(R.string.duration_seconds), instance.mPreferences.getSliderInt("volbtn_torch_timeout", 5));

            // Screen Off On Flat
            case "FlatStandbyTime" ->
                    String.format(fragmentCompat.getString(R.string.duration_seconds), instance.mPreferences.getSliderInt("FlatStandbyTime", 5));

            default -> null;
        };

    }

    public static String getGestureHeight(String key) {
        List<Float> height = instance.mPreferences.getSliderValues(key, 100f);
        if (height.size() == 2) {
            return height.get(0).intValue() + "% - " + height.get(1).intValue() + "%";
        } else {
            return "0% - 100%";
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
            if (summary != null && !key.equals("sb_illustration")) {
                preference.setSummary(summary);
            }

            if (key.equals(MAIN_WIDGET_1_KEY) ||
                    key.equals(MAIN_WIDGET_2_KEY) ||
                    key.equals(EXTRA_WIDGET_1_KEY) ||
                    key.equals(EXTRA_WIDGET_2_KEY) ||
                    key.equals(EXTRA_WIDGET_3_KEY) ||
                    key.equals(EXTRA_WIDGET_4_KEY)) {
                String prefValue = instance.mPreferences.getString(key, "none");
                if (prefValue.contains("customapp:")) {
                    preference.setSummaryProvider(preference1 -> preference1.getContext().getString(R.string.qs_widget_custom_app) + "\n" +
                            AppUtils.getAppName(preference1.getContext(), prefValue.replace("customapp:", "")));
                } else {
                    preference.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
                }
            }

            if (preference instanceof OplusSliderPreference sliderPreference) {
                if (Objects.equals(sliderPreference.getKey(), "batteryWarningRange")) {
                    sliderPreference.mOplusSlider.setLabelFormatter(value -> (int) value + "%");
                } else {
                    if (sliderPreference.mOplusSlider.getValues().size() == 1) {
                        sliderPreference.mOplusSlider.setLabelFormatter(value -> {
                            if (value == ((OplusSliderPreference) preference).defaultValue.get(0))
                                return getAppContext().getString(R.string.default_value);
                            else return String.valueOf(Math.round(value));
                        });
                    }
                }
            }

            //Other special cases
            switch (key) {
                // Quick Settings
                case "QSLabelScaleFactor", "QSSecondaryLabelScaleFactor" ->
                        ((OplusSliderPreference) preference).mOplusSlider.setLabelFormatter(value -> (value + 100) + "%");
                case "moreLogging" -> {
                    if (BuildConfig.VERSION_NAME.contains("nightly")) {
                        ((OplusSwitchPreference) preference).setChecked(true);
                    }
                }
                case "qs_sliders_cat" -> {
                    if (Build.VERSION.SDK_INT >= 35) {
                        preference.setTitle(preference.getContext().getString(R.string.qs_sliders));
                    }
                }
                case QS_BRIGHTNESS_SLIDER_CUSTOMIZE -> {
                    if (Build.VERSION.SDK_INT >= 35) {
                        preference.setTitle(preference.getContext().getString(R.string.customize_qs_sliders_title));
                    }
                }
                case BATTERY_TEXT_ATTACH_TO_BB -> {
                    if (!instance.mPreferences.getBoolean("BBarEnabled", false)) {
                        preference.setEnabled(false);
                        preference.setSummary(preference.getContext().getString(R.string.battery_text_enable_bb_first));
                    }
                }

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
