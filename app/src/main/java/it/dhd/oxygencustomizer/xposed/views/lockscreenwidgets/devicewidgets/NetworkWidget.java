package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint({"ViewConstructor", "MissingPermission"})
public class NetworkWidget extends BaseDeviceWidget {

    private static final long REFRESH_MS = 30_000L;
    private static final int SOURCE_WIFI = 0;
    private static final int SOURCE_MOBILE = 1;
    private static final int MAX_LEVEL = 4;
    private static final String KEY_SOURCE = "source";

    private final ImageView mProgress;
    private final WidgetTicker mTicker;
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private int mSource = SOURCE_WIFI;
    private boolean mCallbackRegistered = false;

    private final ConnectivityManager.NetworkCallback mNetworkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(Network network) {
            mHandler.post(NetworkWidget.this::update);
        }

        @Override
        public void onLost(Network network) {
            mHandler.post(NetworkWidget.this::update);
        }
    };

    private record Signal(boolean connected, int level, String name, String label) {
    }

    public NetworkWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        setOrientation(VERTICAL);
        mProgress = createArcView();
        mTicker = new WidgetTicker(this, REFRESH_MS, this::update);
        onWidgetModeChanged();
    }

    private Signal wifi() {
        if (mSettingsInterface)
            return new Signal(true, 3, "Home Wi-Fi", appContext.getString(R.string.network_wifi));
        try {
            WifiManager manager = mContext.getSystemService(WifiManager.class);
            WifiInfo info = manager == null ? null : manager.getConnectionInfo();
            if (info == null || info.getNetworkId() == -1) {
                return new Signal(false, 0, appContext.getString(R.string.network_disconnected), appContext.getString(R.string.network_wifi));
            }
            int max = Math.max(1, manager.getMaxSignalLevel());
            int level = Math.round(manager.calculateSignalLevel(info.getRssi()) * MAX_LEVEL / (float) max);
            String ssid = info.getSSID();
            if (ssid == null || ssid.contains("unknown ssid"))
                ssid = appContext.getString(R.string.network_wifi);
            return new Signal(true, level, ssid.replace("\"", ""), appContext.getString(R.string.network_wifi));
        } catch (Throwable t) {
            return new Signal(false, 0, appContext.getString(R.string.network_disconnected), appContext.getString(R.string.network_wifi));
        }
    }

    private Signal mobile() {
        if (mSettingsInterface) return new Signal(true, 4, "Carrier", "5G");
        try {
            TelephonyManager manager = mContext.getSystemService(TelephonyManager.class);
            if (manager == null || manager.getSimState() != TelephonyManager.SIM_STATE_READY) {
                return new Signal(false, 0, appContext.getString(R.string.network_disconnected), appContext.getString(R.string.network_mobile));
            }
            SignalStrength strength = manager.getSignalStrength();
            int level = strength == null ? 0 : strength.getLevel();
            String type = networkType(manager);
            String operator = manager.getNetworkOperatorName();
            if (TextUtils.isEmpty(operator))
                operator = appContext.getString(R.string.network_mobile);
            return new Signal(true, level, operator, type);
        } catch (Throwable t) {
            return new Signal(false, 0, appContext.getString(R.string.network_disconnected), appContext.getString(R.string.network_mobile));
        }
    }

    private String networkType(TelephonyManager manager) {
        int type;
        try {
            type = manager.getDataNetworkType();
        } catch (Throwable t) {
            type = TelephonyManager.NETWORK_TYPE_UNKNOWN;
        }
        return switch (type) {
            case TelephonyManager.NETWORK_TYPE_NR -> "5G";
            case TelephonyManager.NETWORK_TYPE_LTE, TelephonyManager.NETWORK_TYPE_IWLAN -> "4G";
            case TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_HSPA,
                 TelephonyManager.NETWORK_TYPE_HSDPA,
                 TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_UMTS,
                 TelephonyManager.NETWORK_TYPE_EVDO_0,
                 TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_EVDO_B,
                 TelephonyManager.NETWORK_TYPE_TD_SCDMA -> "3G";
            case TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_GPRS,
                 TelephonyManager.NETWORK_TYPE_CDMA,
                 TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_GSM -> "2G";
            default -> appContext.getString(R.string.network_mobile);
        };
    }

    private void update() {
        removeAllViews();
        if (mProgress.getParent() instanceof ViewGroup parent) parent.removeView(mProgress);
        if (mCurrentMode == WidgetMode.SMALL) {
            boolean wifi = mSource == SOURCE_WIFI;
            Signal signal = wifi ? wifi() : mobile();
            String text = signal.connected() ? signal.label() : appContext.getString(R.string.dnd_off);
            mProgress.setImageBitmap(arcBitmap(signal.connected() ? signal.level() : 0, MAX_LEVEL, text,
                    tintedIcon(wifi ? R.drawable.ic_widget_wifi : R.drawable.ic_widget_cell)));
            addView(mProgress);
            return;
        }
        Signal wifi = wifi();
        Signal mobile = mobile();
        addView(row(tintedIcon(R.drawable.ic_widget_wifi), wifi.name() + (wifi.connected() ? " · " + bars(wifi.level()) : "")));
        addView(row(tintedIcon(R.drawable.ic_widget_cell), mobile.name() + (mobile.connected() ? " · " + mobile.label() + " " + bars(mobile.level()) : "")));
    }

    private static String bars(int level) {
        StringBuilder bars = new StringBuilder();
        for (int i = 0; i < MAX_LEVEL; i++) bars.append(i < level ? '▮' : '▯');
        return bars.toString();
    }

    private View row(Drawable icon, String label) {
        View view = LayoutInflater.from(appContext).inflate(
                appContext.getResources().getIdentifier("view_icon_label_row", "layout", BuildConfig.APPLICATION_ID), null);
        ((ImageView) ViewHelper.findViewWithTag(view, "icon")).setImageDrawable(icon);
        TextView text = (TextView) ViewHelper.findViewWithTag(view, "label");
        text.setText(label);
        text.setTextColor(mTextColor);
        return view;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mSettingsInterface) return;
        mTicker.start();
        if (!mCallbackRegistered) {
            try {
                ConnectivityManager manager = mContext.getSystemService(ConnectivityManager.class);
                if (manager != null) {
                    manager.registerDefaultNetworkCallback(mNetworkCallback);
                    mCallbackRegistered = true;
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTicker.stop();
        if (mCallbackRegistered) {
            try {
                ConnectivityManager manager = mContext.getSystemService(ConnectivityManager.class);
                if (manager != null) manager.unregisterNetworkCallback(mNetworkCallback);
            } catch (Throwable ignored) {
            }
            mCallbackRegistered = false;
        }
    }

    @Override
    public void onWidgetModeChanged() {
        super.onWidgetModeChanged();
        if (mProgress == null) return;
        update();
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.network_widget);
    }

    @Override
    public boolean hasBigMode() {
        return true;
    }

    @Override
    public boolean hasSmallMode() {
        return true;
    }

    @Override
    public View getBigPreview() {
        NetworkWidget preview = new NetworkWidget(mContext, true);
        preview.setMode(WidgetMode.BIG);
        return preview;
    }

    @Override
    public View getSmallPreview() {
        NetworkWidget preview = new NetworkWidget(mContext, true);
        preview.setMode(WidgetMode.SMALL);
        return preview;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<SettingItem> settings = new ArrayList<>();
        if (mCurrentMode != WidgetMode.SMALL) return settings;
        List<String> entries = new ArrayList<>();
        entries.add(appContext.getString(R.string.network_wifi));
        entries.add(appContext.getString(R.string.network_mobile));
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(SOURCE_WIFI));
        values.add(String.valueOf(SOURCE_MOBILE));
        settings.add(new SettingItem(appContext.getString(R.string.network_source), KEY_SOURCE, SettingItem.Type.MENU, mSource, entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        Object source = settings.get(KEY_SOURCE);
        if (source == null) return;
        try {
            mSource = source instanceof Number number ? number.intValue() : Math.round(Float.parseFloat(String.valueOf(source)));
        } catch (NumberFormatException ignored) {
        }
        if (mProgress != null) update();
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        if (mProgress != null) update();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
