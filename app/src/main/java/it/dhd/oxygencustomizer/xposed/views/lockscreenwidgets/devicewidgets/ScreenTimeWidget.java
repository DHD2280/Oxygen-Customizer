package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.provider.UsageProvider;
import it.dhd.oxygencustomizer.utils.json.SettingItem;

@SuppressLint("ViewConstructor")
public class ScreenTimeWidget extends BaseDeviceWidget {

    private static final long REFRESH_MS = 60_000L;
    private static final long MINUTE_MS = 60_000L;
    private static final int[] GOALS_HOURS = {1, 2, 3, 4, 6, 8};
    private static final String KEY_GOAL = "goal_hours";

    private final ImageView mProgress;
    private final WidgetTicker mTicker;
    private long mScreenTime = 0;
    private int mGoalHours = 4;

    public ScreenTimeWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mProgress = createArcView();
        addView(mProgress);
        mTicker = new WidgetTicker(this, REFRESH_MS, this::query);
        if (mSettingsInterface) mScreenTime = 135 * MINUTE_MS;
        render();
    }

    private void query() {
        if (mSettingsInterface) return;
        ProviderQuery.queryLong(appContext != null ? appContext : mContext, UsageProvider.SCREEN_TIME_URI, mScreenTime, value -> {
            mScreenTime = value;
            render();
        });
    }

    private void render() {
        String text;
        int minutes = 0;
        if (mScreenTime < 0) {
            text = "--";
        } else {
            minutes = (int) (mScreenTime / MINUTE_MS);
            text = minutes >= 60 ? String.format(Locale.getDefault(), "%dh%02d", minutes / 60, minutes % 60) : minutes + "m";
        }
        mProgress.setImageBitmap(arcBitmap(minutes, mGoalHours * 60, text, tintedIcon(R.drawable.ic_widget_screen_time)));
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mSettingsInterface) mTicker.start();
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
        return appContext.getString(R.string.screen_time_widget);
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
        return new ScreenTimeWidget(mContext, true);
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<String> entries = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (int hours : GOALS_HOURS) {
            entries.add(hours + "h");
            values.add(String.valueOf(hours));
        }
        List<SettingItem> settings = new ArrayList<>();
        settings.add(new SettingItem(appContext.getString(R.string.screen_time_goal), KEY_GOAL, SettingItem.Type.LIST, String.valueOf(mGoalHours), entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        Object goal = settings.get(KEY_GOAL);
        if (goal == null) return;
        try {
            mGoalHours = Math.max(1, goal instanceof Number number ? number.intValue() : Math.round(Float.parseFloat(String.valueOf(goal))));
        } catch (NumberFormatException ignored) {
        }
        if (mProgress != null) render();
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        if (mProgress != null) render();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
