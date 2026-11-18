package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.ProgressImageView;

@SuppressLint("ViewConstructor")
public class MemoryWidget extends BaseDeviceWidget {

    private final ProgressImageView mMemoryProgress;

    public MemoryWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mMemoryProgress = progress();
        addView(mMemoryProgress);
    }

    private ProgressImageView progress() {
        ProgressImageView view = new ProgressImageView(mContext);
        view.setProgressType(ProgressImageView.ProgressType.MEMORY);
        view.setColors(mProgressColor, mTextColor);
        return view;
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.memory_widget);
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
        return progress();
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
        mMemoryProgress.setColors(progressColor, textColor);
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
