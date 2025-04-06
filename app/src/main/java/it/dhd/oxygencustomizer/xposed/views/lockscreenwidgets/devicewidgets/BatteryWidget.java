package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.ProgressImageView;

@SuppressLint("ViewConstructor")
public class BatteryWidget extends BaseDeviceWidget {

    private final int PROGRESS_BATTERY_LEVEL = 0;
    private final int PROGRESS_BATTERY_TEMPERATURE = 1;

    private ProgressImageView mCurrentProgress;
    private int mProgressType = PROGRESS_BATTERY_LEVEL;

    public BatteryWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mCurrentProgress = new ProgressImageView(context);
        mCurrentProgress.setProgressType(ProgressImageView.ProgressType.BATTERY);
        addView(mCurrentProgress);
    }

    @Override
    public String getWidgetReference() {
        return "batteryWidget";
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.battery_widget);
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
        ProgressImageView preview = new ProgressImageView(mContext);
        preview.setProgressType(ProgressImageView.ProgressType.BATTERY);
        return preview;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<SettingItem> settings = new ArrayList<>();
        List<String> entries = new ArrayList<>() {{
           add(appContext.getString(R.string.battery_widget_percentage));
            add(appContext.getString(R.string.battery_widget_temperature));
        }};
        List<String> values = new ArrayList<>() {{
            add(String.valueOf(PROGRESS_BATTERY_LEVEL));
            add(String.valueOf(PROGRESS_BATTERY_TEMPERATURE));
        }};
        settings.add(new SettingItem(appContext.getString(R.string.progress_type), "progressType", SettingItem.Type.MENU, mProgressType, entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        if (settings.isEmpty()) return;
        Object pt = settings.get("progressType");
        if (pt instanceof Number num) {
            mProgressType = num.intValue();
        } else {
            mProgressType = Integer.parseInt((String.valueOf(settings.get("progressType"))));
        }
        mCurrentProgress.setProgressType(mProgressType == PROGRESS_BATTERY_LEVEL ? ProgressImageView.ProgressType.BATTERY : ProgressImageView.ProgressType.TEMPERATURE);
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        mCurrentProgress.setColors(progressColor, textColor);

    }

}
