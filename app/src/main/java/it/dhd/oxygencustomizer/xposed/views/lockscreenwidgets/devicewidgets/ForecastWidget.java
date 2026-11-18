package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;

@SuppressLint("ViewConstructor")
public class ForecastWidget extends BaseDeviceWidget implements OmniJawsClient.OmniJawsObserver {

    private static final int TYPE_HOURLY = 0;
    private static final int TYPE_DAILY = 1;
    private static final int COLUMNS = 4;
    private static final String KEY_TYPE = "forecast_type";

    private final OmniJawsClient mWeatherClient;
    private final LinearLayout mRow;
    private int mType = TYPE_HOURLY;
    private boolean mObserving = false;

    public ForecastWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mWeatherClient = new OmniJawsClient(context);
        mRow = new LinearLayout(mContext);
        mRow.setOrientation(HORIZONTAL);
        mRow.setGravity(Gravity.CENTER_VERTICAL);
        addView(mRow, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        refresh();
    }

    private void refresh() {
        OmniJawsClient.WeatherInfo info = null;
        try {
            mWeatherClient.queryWeather();
            info = mWeatherClient.getWeatherInfo();
        } catch (Throwable ignored) {
        }
        mRow.removeAllViews();
        List<String[]> columns = info == null ? placeholder() : mType == TYPE_DAILY ? daily(info) : hourly(info);
        for (String[] column : columns) {
            mRow.addView(column(column[0], icon(info, column[1]), column[2]), new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));
        }
    }

    private List<String[]> placeholder() {
        List<String[]> columns = new ArrayList<>();
        for (int i = 0; i < COLUMNS; i++) columns.add(new String[]{"--", "-1", "--"});
        return columns;
    }

    private List<String[]> hourly(OmniJawsClient.WeatherInfo info) {
        List<String[]> columns = new ArrayList<>();
        if (info.hourlyForecasts == null) return columns;
        for (OmniJawsClient.HourForecast hour : info.hourlyForecasts) {
            if (columns.size() == COLUMNS) break;
            if (TextUtils.isEmpty(hour.time) || "NaN".equals(hour.time)) continue;
            String time = hour.time.contains("T") ? hour.time.substring(hour.time.indexOf('T') + 1) : hour.time;
            if (time.length() > 5) time = time.substring(0, 5);
            columns.add(new String[]{time, String.valueOf(hour.conditionCode), hour.temperature + "°"});
        }
        return columns;
    }

    private List<String[]> daily(OmniJawsClient.WeatherInfo info) {
        List<String[]> columns = new ArrayList<>();
        if (info.dayForecasts == null) return columns;
        for (OmniJawsClient.DayForecast day : info.dayForecasts) {
            if (columns.size() == COLUMNS) break;
            if (TextUtils.isEmpty(day.date) || "NaN".equals(day.date)) continue;
            columns.add(new String[]{weekday(day.date), String.valueOf(day.conditionCode), day.high + "°/" + day.low + "°"});
        }
        return columns;
    }

    private String weekday(String date) {
        try {
            Date parsed = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date);
            return parsed == null ? date : new SimpleDateFormat("EEE", Locale.getDefault()).format(parsed);
        } catch (ParseException e) {
            return date;
        }
    }

    private Drawable icon(OmniJawsClient.WeatherInfo info, String code) {
        if (info == null) return null;
        try {
            Drawable drawable = mWeatherClient.getWeatherConditionImage(Integer.parseInt(code));
            if (drawable == null || drawable.getConstantState() == null) return drawable;
            Drawable copy = drawable.getConstantState().newDrawable().mutate();
            copy.setTintList(null);
            return copy;
        } catch (Throwable t) {
            return null;
        }
    }

    private View column(String label, Drawable icon, String value) {
        LinearLayout column = new LinearLayout(mContext);
        column.setOrientation(VERTICAL);
        column.setGravity(Gravity.CENTER_HORIZONTAL);
        column.addView(text(label, 11, false));
        ImageView image = new ImageView(mContext);
        image.setImageDrawable(icon);
        LayoutParams params = new LayoutParams(dp2px(mContext, 22), dp2px(mContext, 22));
        params.topMargin = dp2px(mContext, 2);
        params.bottomMargin = dp2px(mContext, 2);
        column.addView(image, params);
        column.addView(text(value, 12, true));
        return column;
    }

    private TextView text(String value, int size, boolean bold) {
        TextView view = new TextView(mContext);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(mTextColor);
        view.setSingleLine(true);
        view.setEllipsize(TextUtils.TruncateAt.END);
        view.setGravity(Gravity.CENTER);
        if (bold) view.setTypeface(Typeface.DEFAULT_BOLD);
        else view.setAlpha(0.8f);
        return view;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mObserving) {
            mWeatherClient.addObserver(this);
            mObserving = true;
        }
        refresh();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mObserving) {
            mWeatherClient.removeObserver(this);
            mObserving = false;
        }
    }

    @Override
    public String getWidgetReference() {
        return getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.weather_forecast_widget);
    }

    @Override
    public boolean hasBigMode() {
        return true;
    }

    @Override
    public boolean hasSmallMode() {
        return false;
    }

    @Override
    public View getBigPreview() {
        ForecastWidget preview = new ForecastWidget(mContext, true);
        preview.setMode(WidgetMode.BIG);
        return preview;
    }

    @Override
    public View getSmallPreview() {
        return null;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        List<SettingItem> settings = new ArrayList<>();
        List<String> entries = new ArrayList<>();
        entries.add(appContext.getString(R.string.weather_forecast_hourly));
        entries.add(appContext.getString(R.string.weather_forecast_daily));
        List<String> values = new ArrayList<>();
        values.add(String.valueOf(TYPE_HOURLY));
        values.add(String.valueOf(TYPE_DAILY));
        settings.add(new SettingItem(appContext.getString(R.string.weather_forecast_type), KEY_TYPE, SettingItem.Type.MENU, mType, entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        Object type = settings.get(KEY_TYPE);
        if (type == null) return;
        try {
            mType = type instanceof Number number ? number.intValue() : Math.round(Float.parseFloat(String.valueOf(type)));
        } catch (NumberFormatException ignored) {
        }
        if (mRow != null) refresh();
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        refresh();
    }

    @Override
    public void onSetDeviceName(String deviceName) {
    }

    @Override
    public void weatherUpdated() {
        refresh();
    }

    @Override
    public void updateSettings() {
        refresh();
    }

    @Override
    public void weatherError(int errorReason) {
    }
}
