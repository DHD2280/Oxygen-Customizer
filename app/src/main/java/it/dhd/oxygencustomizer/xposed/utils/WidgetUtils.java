package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedBridge.log;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import it.dhd.oxygencustomizer.R;

public class WidgetUtils {

    public static final int HOTSPOT_ENABLED = 13;

    public static final String BT_ACTIVE = "status_bar_qs_bluetooth_active";
    public static final String BT_INACTIVE = "status_bar_qs_bluetooth_inactive";
    public static final String DATA_ACTIVE = "status_bar_qs_data_active";
    public static final String DATA_INACTIVE = "status_bar_qs_data_inactive";
    public static final String RINGER_NORMAL = "status_bar_qs_mute_inactive";
    public static final String RINGER_VIBRATE = "status_bar_qs_icon_volume_ringer_vibrate";
    public static final String RINGER_SILENT = "status_bar_qs_mute_active";
    public static final String RINGER_INACTIVE = "status_bar_qs_mute_active";
    public static final String TORCH_RES_ACTIVE = "status_bar_qs_flashlight_active";
    public static final String TORCH_RES_INACTIVE = "status_bar_qs_flashlight_inactive";
    public static final String WIFI_ACTIVE = "status_bar_qs_wifi_active";
    public static final String WIFI_INACTIVE = "status_bar_qs_wifi_inactive";
    public static final String HOME_CONTROLS = "status_bar_qs_device_control_active";
    public static final String CALCULATOR_ICON = "status_bar_qs_calculator_inactive";
    public static final String CAMERA_ICON = "status_bar_qs_camera_allowed"; // Use qs camera access icon for camera
    public static final String WALLET_ICON = "status_bar_qs_wallet_active";
    public static final String HOTSPOT_ACTIVE = "status_bar_qs_hotspot_active";
    public static final String HOTSPOT_INACTIVE = "status_bar_qs_hotspot_inactive";

    public static final String BT_LABEL_INACTIVE = "quick_settings_bluetooth_label";
    public static final String DATA_LABEL_INACTIVE = "mobile_data_settings_title";
    public static final String RINGER_LABEL_INACTIVE = "state_button_silence";
    public static final String TORCH_LABEL_ACTIVE = "notification_flashlight_hasopen";
    public static final String TORCH_LABEL_INACTIVE = "notification_flashlight_hasclose";
    public static final String WIFI_LABEL_INACTIVE = "quick_settings_wifi_label";
    public static final String HOME_CONTROLS_LABEL = "quick_controls_title";
    public static final String MEDIA_PLAY_LABEL = "controls_media_button_play";
    public static final String CALCULATOR_LABEL = "state_button_calculator";
    public static final String CAMERA_LABEL = "affordance_settings_camera";
    public static final String WALLET_LABEL = "wallet_title";
    public static final String HOTSPOT_LABEL = "quick_settings_hotspot_label";

    @SuppressLint("DiscouragedApi")
    public static Drawable getDrawable(Context mContext, String drawableRes, String pkg) {
        try {
            return ContextCompat.getDrawable(
                    mContext,
                    mContext.getResources().getIdentifier(drawableRes, "drawable", pkg));
        } catch (Throwable t) {
            // We have a calculator icon, so if SystemUI doesn't just return ours
            if (drawableRes.equals(CALCULATOR_ICON))
                return ResourcesCompat.getDrawable(modRes, R.drawable.ic_calculator, mContext.getTheme());
            else if (drawableRes.equals(HOME_CONTROLS))
                return getDrawable(mContext, "controls_icon", SYSTEM_UI);

            log("LockscreenWidgetsView getDrawable " + drawableRes + " from " + pkg + " error " + t);
            return null;
        }
    }

    @SuppressLint("DiscouragedApi")
    public static String getString(Context mContext, String stringRes, String pkg) {
        try {
            return mContext.getResources().getString(
                    mContext.getResources().getIdentifier(stringRes, "string", pkg));
        } catch (Throwable t) {
            switch (stringRes) {
                // We have out own strings too, so if getString from SystemUI fails
                // return our own strings,
                case CALCULATOR_LABEL -> {
                    return modRes.getString(R.string.calculator);
                }
                case CAMERA_LABEL -> {
                    return modRes.getString(R.string.camera);
                }
                case WALLET_LABEL -> {
                    return modRes.getString(R.string.wallet);
                }
            }
            log("LockscreenWidgetsView getString " + stringRes + " from " + pkg + " error " + t);
            return "";
        }
    }

}