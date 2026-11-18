package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;

@SuppressLint("ViewConstructor")
public class DndWidget extends BaseDeviceWidget {

    private static final int ZEN_MODE_OFF = 0;
    private static final int ZEN_MODE_IMPORTANT_INTERRUPTIONS = 1;

    private final ImageView mProgress;
    private boolean mReceiverRegistered = false;

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            render();
        }
    };

    public DndWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mProgress = createArcView();
        addView(mProgress);
        render();
    }

    private NotificationManager manager() {
        return mContext.getSystemService(NotificationManager.class);
    }

    private boolean isOn() {
        if (mSettingsInterface) return true;
        try {
            NotificationManager manager = manager();
            int filter = manager == null ? NotificationManager.INTERRUPTION_FILTER_ALL : manager.getCurrentInterruptionFilter();
            return filter != NotificationManager.INTERRUPTION_FILTER_ALL && filter != NotificationManager.INTERRUPTION_FILTER_UNKNOWN;
        } catch (Throwable t) {
            return false;
        }
    }

    private void render() {
        boolean on = isOn();
        mProgress.setImageBitmap(arcBitmap(on ? 1 : 0, 1, appContext.getString(on ? R.string.dnd_on : R.string.dnd_off), tintedIcon(R.drawable.ic_widget_dnd)));
    }

    @Override
    protected boolean onWidgetTap() {
        if (mSettingsInterface) return false;
        NotificationManager manager = manager();
        if (manager == null) return true;
        boolean on = isOn();
        try {
            Method setZenMode = NotificationManager.class.getMethod("setZenMode", int.class, Uri.class, String.class);
            setZenMode.invoke(manager, on ? ZEN_MODE_OFF : ZEN_MODE_IMPORTANT_INTERRUPTIONS, null, "OxygenCustomizer");
        } catch (Throwable t) {
            try {
                if (manager.isNotificationPolicyAccessGranted()) {
                    manager.setInterruptionFilter(on ? NotificationManager.INTERRUPTION_FILTER_ALL : NotificationManager.INTERRUPTION_FILTER_PRIORITY);
                }
            } catch (Throwable ignored) {
            }
        }
        render();
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mReceiverRegistered && !mSettingsInterface) {
            mContext.registerReceiver(mReceiver, new IntentFilter(NotificationManager.ACTION_INTERRUPTION_FILTER_CHANGED), Context.RECEIVER_EXPORTED);
            mReceiverRegistered = true;
        }
        render();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mReceiverRegistered) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Throwable ignored) {
            }
            mReceiverRegistered = false;
        }
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.dnd_widget);
    }

    @Override
    public boolean hasBigMode() {
        return false;
    }

    @Override
    public boolean hasSmallMode() {
        return true;
    }

    @Override
    public View getBigPreview() {
        return null;
    }

    @Override
    public View getSmallPreview() {
        return new DndWidget(mContext, true);
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        return Collections.emptyList();
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        if (mProgress != null) render();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
