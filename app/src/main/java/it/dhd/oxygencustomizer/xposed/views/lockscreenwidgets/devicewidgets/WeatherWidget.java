package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.getHumidityProgress;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class WeatherWidget extends BaseDeviceWidget implements OmniJawsClient.OmniJawsObserver {

    private static final int ARROW_BITMAP_SIZE = 96;
    private static final int TEXT_SIZE = 40;
    private static final int WIND_TEXT_SIZE = 30;
    private static final int ICON_SIZE = 36;

    private final OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;
    private boolean mObserving = false;

    private ImageView mConditionImage;
    private TextView mCurrentCondition, mLocation, mHighLow;

    private final ImageView mTempProgress = new ImageView(mContext);

    private final int PROGRESS_TEMPERATURE = 0;
    private final int PROGRESS_HUMIDITY = 1;
    private final int PROGRESS_WIND = 2;
    private final int PROGRESS_FEELS_LIKE = 3;
    private final int PROGRESS_UV = 4;
    private final int PROGRESS_PRESSURE = 5;
    private final int PROGRESS_RAIN = 6;
    private final int PROGRESS_DAYLIGHT = 7;
    private static final int UV_MAX = 11;
    private static final int PRESSURE_MIN = 950;
    private static final int PRESSURE_MAX = 1050;
    private int progressType = PROGRESS_TEMPERATURE;

    public WeatherWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mWeatherClient = new OmniJawsClient(context);
        mTempProgress.setScaleType(ImageView.ScaleType.FIT_CENTER);
        mTempProgress.setLayoutParams(new LayoutParams(dp2px(mContext, 60), dp2px(mContext, 60)));
        onWidgetModeChanged();
    }

    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View view = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "device_widget_weather_widget",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        mConditionImage = (ImageView) ViewHelper.findViewWithTag(view, "condition_image");
        mLocation = (TextView) ViewHelper.findViewWithTag(view, "current_location");
        mCurrentCondition = (TextView) ViewHelper.findViewWithTag(view, "current_condition");
        mHighLow = (TextView) ViewHelper.findViewWithTag(view, "high_low");
        addView(view, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
    }

    public void enableUpdates() {
        if (mWeatherClient == null) return;
        if (!mObserving) {
            mWeatherClient.addObserver(this);
            mObserving = true;
        }
        //WeatherScheduler.scheduleUpdateNow(mContext);
        queryAndUpdateWeather();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        enableUpdates();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mWeatherClient != null && mObserving) {
            mWeatherClient.removeObserver(this);
            mObserving = false;
        }
    }

    private void queryAndUpdateWeather() {
        if (mWeatherClient != null) {
            try {
                mWeatherClient.queryWeather();
                mWeatherInfo = mWeatherClient.getWeatherInfo();
            } catch (Throwable ignored) {
            }
        }
        updateDetails();
        updateProgress();
    }

    private void updateDetails() {
        if (mLocation == null) return;
        mLocation.setTextColor(mTextColor);
        mCurrentCondition.setTextColor(mTextColor);
        mHighLow.setTextColor(mTextColor);
        if (mWeatherInfo == null) return;
        Drawable condition = conditionImage();
        if (condition != null) {
            mConditionImage.setImageTintList(null);
            mConditionImage.setImageDrawable(condition);
        }
        mLocation.setText(mWeatherInfo.temp + mWeatherInfo.tempUnits);
        mCurrentCondition.setText(formattedCondition(mWeatherInfo.condition));
        StringBuilder details = new StringBuilder(mWeatherInfo.city == null ? "" : mWeatherInfo.city.trim());
        OmniJawsClient.DayForecast today = today();
        if (today != null) {
            if (details.length() > 0) details.append(" · ");
            details.append("H ").append(today.high).append("° L ").append(today.low).append("°");
        }
        mHighLow.setText(details);
    }

    private String formattedCondition(String condition) {
        if (TextUtils.isEmpty(condition)) return "";
        String lower = condition.toLowerCase(Locale.ROOT);
        if (lower.contains("clouds") || lower.contains("overcast"))
            return appContext.getString(R.string.weather_condition_clouds);
        if (lower.contains("rain")) return appContext.getString(R.string.weather_condition_rain);
        if (lower.contains("clear")) return appContext.getString(R.string.weather_condition_clear);
        if (lower.contains("storm")) return appContext.getString(R.string.weather_condition_storm);
        if (lower.contains("snow")) return appContext.getString(R.string.weather_condition_snow);
        if (lower.contains("wind")) return appContext.getString(R.string.weather_condition_wind);
        if (lower.contains("mist")) return appContext.getString(R.string.weather_condition_mist);
        return condition;
    }

    private Drawable conditionImage() {
        try {
            Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
            if (d == null || d.getConstantState() == null) return d;
            Drawable copy = d.getConstantState().newDrawable().mutate();
            copy.setTintList(null);
            return copy;
        } catch (Throwable t) {
            return null;
        }
    }

    private OmniJawsClient.DayForecast today() {
        if (mWeatherInfo == null || mWeatherInfo.dayForecasts == null || mWeatherInfo.dayForecasts.isEmpty())
            return null;
        return mWeatherInfo.dayForecasts.get(0);
    }

    private void updateProgress() {
        try {
            mTempProgress.setImageBitmap(progressBitmap());
        } catch (Throwable ignored) {
        }
    }

    private Bitmap progressBitmap() {
        if (mWeatherInfo == null) {
            return ArcProgressWidget.generateBitmap(mContext, 20, 25, "20°C", TEXT_SIZE,
                    ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.google_30, appContext.getTheme()),
                    false, ICON_SIZE, null, mProgressColor, mTextColor);
        }
        if (progressType == PROGRESS_HUMIDITY) {
            int humidity = Math.max(0, Math.min(parseNumber(mWeatherInfo.humidity, 0), 100));
            return getHumidityProgress(mContext, appContext, humidity, humidity + "%", 35, null, mProgressColor, mTextColor);
        }
        if (progressType == PROGRESS_WIND) {
            int speed = Math.max(0, parseNumber(mWeatherInfo.windSpeed, 0));
            int max = maxWindSpeed(mWeatherInfo.windUnits);
            String units = mWeatherInfo.windUnits == null ? "" : mWeatherInfo.windUnits.trim();
            return ArcProgressWidget.generateBitmap(mContext, Math.min(speed, max), max, speed + units, WIND_TEXT_SIZE,
                    windArrow(parseNumber(mWeatherInfo.windDirection, 0)), false, ICON_SIZE, null, mProgressColor, mTextColor);
        }
        if (progressType == PROGRESS_UV) {
            if (Float.isNaN(mWeatherInfo.uvIndex))
                return arc(0, 1, "--", TEXT_SIZE, tinted(R.drawable.ic_weather_uv));
            int uv = Math.max(0, Math.round(mWeatherInfo.uvIndex));
            return arc(Math.min(uv, UV_MAX), UV_MAX, String.valueOf(uv), TEXT_SIZE, tinted(R.drawable.ic_weather_uv));
        }
        if (progressType == PROGRESS_PRESSURE) {
            if (Float.isNaN(mWeatherInfo.pressure))
                return arc(0, 1, "--", TEXT_SIZE, tinted(R.drawable.ic_weather_pressure));
            int pressure = Math.round(mWeatherInfo.pressure);
            int progress = Math.max(0, Math.min(pressure - PRESSURE_MIN, PRESSURE_MAX - PRESSURE_MIN));
            return arc(progress, PRESSURE_MAX - PRESSURE_MIN, String.valueOf(pressure), WIND_TEXT_SIZE, tinted(R.drawable.ic_weather_pressure));
        }
        if (progressType == PROGRESS_RAIN) {
            if (mWeatherInfo.rainChance < 0)
                return arc(0, 1, "--", TEXT_SIZE, tinted(R.drawable.ic_weather_rain));
            int rain = Math.min(mWeatherInfo.rainChance, 100);
            return arc(rain, 100, rain + "%", TEXT_SIZE, tinted(R.drawable.ic_weather_rain));
        }
        if (progressType == PROGRESS_DAYLIGHT) {
            if (mWeatherInfo.sunrise <= 0 || mWeatherInfo.sunset <= mWeatherInfo.sunrise)
                return arc(0, 1, "--", TEXT_SIZE, tinted(R.drawable.ic_weather_sunrise));
            long now = System.currentTimeMillis();
            long length = mWeatherInfo.sunset - mWeatherInfo.sunrise;
            long elapsed = Math.max(0, Math.min(now - mWeatherInfo.sunrise, length));
            long next = now < mWeatherInfo.sunrise ? mWeatherInfo.sunrise : now < mWeatherInfo.sunset ? mWeatherInfo.sunset : mWeatherInfo.sunrise + DateUtils.DAY_IN_MILLIS;
            String text = DateFormat.getTimeFormat(mContext).format(new Date(next));
            return arc((int) (elapsed / DateUtils.MINUTE_IN_MILLIS), (int) Math.max(1, length / DateUtils.MINUTE_IN_MILLIS), text,
                    text.length() > 5 ? 28 : WIND_TEXT_SIZE, tinted(R.drawable.ic_weather_sunrise));
        }
        if (progressType == PROGRESS_FEELS_LIKE && parseNumber(mWeatherInfo.feelsLike, Integer.MIN_VALUE) == Integer.MIN_VALUE) {
            return arc(0, 1, "--", TEXT_SIZE, tinted(R.drawable.ic_temperature));
        }
        int temp = parseNumber(progressType == PROGRESS_FEELS_LIKE ? mWeatherInfo.feelsLike : mWeatherInfo.temp, 0);
        int progress = temp;
        int max = 40;
        OmniJawsClient.DayForecast today = today();
        if (today != null) {
            int low = parseNumber(today.low, temp);
            int high = parseNumber(today.high, temp);
            max = Math.max(1, high - low);
            progress = Math.max(0, Math.min(temp - low, max));
        }
        Drawable icon = progressType == PROGRESS_FEELS_LIKE ? tinted(R.drawable.ic_temperature) : conditionImage();
        return ArcProgressWidget.generateBitmap(mContext, progress, max, temp + mWeatherInfo.tempUnits, TEXT_SIZE,
                icon, false, ICON_SIZE, null, mProgressColor, mTextColor);
    }

    private Bitmap arc(int progress, int max, String text, int textSize, Drawable icon) {
        return ArcProgressWidget.generateBitmap(mContext, progress, max, text, textSize, icon, false, ICON_SIZE, null, mProgressColor, mTextColor);
    }

    private Drawable tinted(int res) {
        Drawable drawable = ResourcesCompat.getDrawable(appContext.getResources(), res, appContext.getTheme());
        if (drawable == null) return null;
        drawable = drawable.mutate();
        drawable.setTint(mTextColor);
        return drawable;
    }

    private Drawable windArrow(int fromDegrees) {
        Drawable arrow = ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.ic_wind_direction, appContext.getTheme());
        if (arrow == null) return null;
        arrow = arrow.mutate();
        arrow.setTint(mTextColor);
        Bitmap bitmap = Bitmap.createBitmap(ARROW_BITMAP_SIZE, ARROW_BITMAP_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.rotate((fromDegrees + 180) % 360, ARROW_BITMAP_SIZE / 2f, ARROW_BITMAP_SIZE / 2f);
        arrow.setBounds(0, 0, ARROW_BITMAP_SIZE, ARROW_BITMAP_SIZE);
        arrow.draw(canvas);
        return new BitmapDrawable(appContext.getResources(), bitmap);
    }

    private static int maxWindSpeed(String units) {
        if (units == null) return 60;
        String lower = units.toLowerCase(Locale.ROOT);
        if (lower.contains("mph")) return 40;
        if (lower.contains("m/s")) return 17;
        if (lower.contains("kn")) return 35;
        return 60;
    }

    private static int parseNumber(String value, int fallback) {
        if (value == null) return fallback;
        String number = value.replaceAll("[^0-9.\\-]", "");
        if (number.isEmpty()) return fallback;
        try {
            return Math.round(Float.parseFloat(number));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    @Override
    public void onWidgetModeChanged() {
        super.onWidgetModeChanged();
        removeAllViews();
        if (mTempProgress.getParent() instanceof ViewGroup parent) parent.removeView(mTempProgress);
        mLocation = null;
        if (mCurrentMode == WidgetMode.SMALL) {
            addView(mTempProgress);
        } else {
            inflateView();
        }
        enableUpdates();
    }

    @Override
    public String getWidgetReference() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String getWidgetName() {
        return appContext.getString(R.string.weather);
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
        return this;
    }

    @Override
    public View getSmallPreview() {
        return mTempProgress;
    }

    @Override
    public List<SettingItem> getCustomSettings() {
        if (mCurrentMode == WidgetMode.BIG) return Collections.emptyList();

        List<SettingItem> settings = new ArrayList<>();
        List<String> entries = new ArrayList<>() {{
            add(appContext.getString(R.string.weather_widget_temperature));
            add(appContext.getString(R.string.weather_widget_humidity));
            add(appContext.getString(R.string.weather_widget_wind));
            add(appContext.getString(R.string.weather_feels_like));
            add(appContext.getString(R.string.weather_uv_index));
            add(appContext.getString(R.string.weather_pressure));
            add(appContext.getString(R.string.weather_rain_chance));
            add(appContext.getString(R.string.weather_daylight));
        }};
        List<String> values = new ArrayList<>() {{
            add(String.valueOf(PROGRESS_TEMPERATURE));
            add(String.valueOf(PROGRESS_HUMIDITY));
            add(String.valueOf(PROGRESS_WIND));
            add(String.valueOf(PROGRESS_FEELS_LIKE));
            add(String.valueOf(PROGRESS_UV));
            add(String.valueOf(PROGRESS_PRESSURE));
            add(String.valueOf(PROGRESS_RAIN));
            add(String.valueOf(PROGRESS_DAYLIGHT));
        }};
        settings.add(new SettingItem(appContext.getString(R.string.progress_type), "progress_type", SettingItem.Type.MENU, progressType, entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        if (settings.isEmpty()) return;
        Object pt = settings.get("progress_type");
        if (pt == null) return;
        if (pt instanceof Number num) {
            progressType = num.intValue();
        } else {
            progressType = parseNumber(String.valueOf(pt), PROGRESS_TEMPERATURE);
        }
        updateProgress();
    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        updateDetails();
        updateProgress();
    }

    @Override
    public void onSetDeviceName(String deviceName) {}

    @Override
    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    @Override
    public void updateSettings() {
        queryAndUpdateWeather();
    }

    @Override
    public void weatherError(int errorReason) {

    }
}
