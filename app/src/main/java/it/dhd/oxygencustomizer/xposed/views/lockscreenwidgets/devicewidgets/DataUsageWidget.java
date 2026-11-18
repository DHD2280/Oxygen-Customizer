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
public class DataUsageWidget extends BaseDeviceWidget {

    private static final long REFRESH_MS = 5 * 60_000L;
    private static final long MB = 1024L * 1024L;
    private static final long GB = 1024L * MB;
    private static final int PERIOD_TODAY = 0;
    private static final int PERIOD_MONTH = 1;
    private static final int[] LIMITS_GB = {1, 2, 5, 10, 20, 50, 100};
    private static final String KEY_PERIOD = "period";
    private static final String KEY_LIMIT = "limit_gb";

    private final ImageView mProgress;
    private final WidgetTicker mTicker;
    private long mBytes = 0;
    private int mPeriod = PERIOD_TODAY;
    private int mLimitGb = 5;

    public DataUsageWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mProgress = createArcView();
        addView(mProgress);
        mTicker = new WidgetTicker(this, REFRESH_MS, this::query);
        if (mSettingsInterface) mBytes = 820 * MB;
        render();
    }

    private void query() {
        if (mSettingsInterface) return;
        ProviderQuery.queryLong(appContext != null ? appContext : mContext,
                mPeriod == PERIOD_MONTH ? UsageProvider.DATA_MONTH_URI : UsageProvider.DATA_TODAY_URI, mBytes, value -> {
                    mBytes = value;
                    render();
                });
    }

    private void render() {
        String text;
        int progress = 0;
        int max = 1000;
        if (mBytes < 0) {
            text = "--";
        } else {
            text = mBytes >= GB ? String.format(Locale.getDefault(), "%.1fG", mBytes / (double) GB) : (mBytes / MB) + "M";
            progress = (int) Math.min(max, mBytes * max / (mLimitGb * GB));
        }
        mProgress.setImageBitmap(arcBitmap(progress, max, text, tintedIcon(R.drawable.ic_widget_data)));
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
        return appContext.getString(R.string.data_usage_widget);
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
        return new DataUsageWidget(mContext, true);
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<SettingItem> settings = new ArrayList<>();
        List<String> periods = new ArrayList<>();
        periods.add(appContext.getString(R.string.data_usage_today));
        periods.add(appContext.getString(R.string.data_usage_month));
        List<String> periodValues = new ArrayList<>();
        periodValues.add(String.valueOf(PERIOD_TODAY));
        periodValues.add(String.valueOf(PERIOD_MONTH));
        settings.add(new SettingItem(appContext.getString(R.string.data_usage_period), KEY_PERIOD, SettingItem.Type.MENU, mPeriod, periods, periodValues));
        List<String> limits = new ArrayList<>();
        List<String> limitValues = new ArrayList<>();
        for (int limit : LIMITS_GB) {
            limits.add(limit + " GB");
            limitValues.add(String.valueOf(limit));
        }
        settings.add(new SettingItem(appContext.getString(R.string.data_usage_limit), KEY_LIMIT, SettingItem.Type.LIST, String.valueOf(mLimitGb), limits, limitValues));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        mPeriod = intSetting(settings.get(KEY_PERIOD), mPeriod);
        mLimitGb = Math.max(1, intSetting(settings.get(KEY_LIMIT), mLimitGb));
        if (mProgress != null) {
            render();
            query();
        }
    }

    private static int intSetting(Object value, int fallback) {
        if (value == null) return fallback;
        if (value instanceof Number number) return number.intValue();
        try {
            return Math.round(Float.parseFloat(String.valueOf(value)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        if (mProgress != null) render();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }
}
