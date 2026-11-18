package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.view.View;
import android.widget.ImageView;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;

@SuppressLint("ViewConstructor")
public class StorageWidget extends BaseDeviceWidget {

    private static final long REFRESH_MS = 10 * 60_000L;

    private final ImageView mProgress;
    private final WidgetTicker mTicker;
    private int mUsedPercent = -1;

    public StorageWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mProgress = createArcView();
        addView(mProgress);
        mTicker = new WidgetTicker(this, REFRESH_MS, this::update);
        update();
    }

    private void update() {
        try {
            StatFs stat = new StatFs(Environment.getDataDirectory().getPath());
            long total = stat.getTotalBytes();
            long free = stat.getAvailableBytes();
            mUsedPercent = total <= 0 ? -1 : (int) ((total - free) * 100 / total);
        } catch (Throwable t) {
            mUsedPercent = -1;
        }
        render();
    }

    private void render() {
        mProgress.setImageBitmap(arcBitmap(Math.max(mUsedPercent, 0), 100, mUsedPercent < 0 ? "--" : mUsedPercent + "%", tintedIcon(R.drawable.ic_widget_storage)));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mTicker.start();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTicker.stop();
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.storage_widget);
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
        return new StorageWidget(mContext, true);
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
