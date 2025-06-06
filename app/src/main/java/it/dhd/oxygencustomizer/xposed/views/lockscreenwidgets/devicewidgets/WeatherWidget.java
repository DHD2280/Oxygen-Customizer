package it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets;

import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.getHumidityProgress;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.json.SettingItem;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.utils.ArcProgressWidget;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressLint("ViewConstructor")
public class WeatherWidget extends BaseDeviceWidget implements OmniJawsClient.OmniJawsObserver {

    private final OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    private ImageView mConditionImage;
    private TextView mCurrentCondition, mLocation, mHighLow;

    private ImageView mTempProgress = new ImageView(mContext);

    private final int PROGRESS_TEMPERATURE = 0;
    private final int PROGRESS_HUMIDITY = 1;
    private final int PROGRESS_WIND = 2;
    private int progressType = PROGRESS_TEMPERATURE;

    public WeatherWidget(Context context, boolean settingsInterface) {
        super(context, settingsInterface);
        mWeatherClient = new OmniJawsClient(context);
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

        mTempProgress.setLayoutParams(new ViewGroup.LayoutParams(
                dp2px(mContext, 60),
                dp2px(mContext, 60)
        ));
        addView(view);
    }

    public void enableUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this);
            //WeatherScheduler.scheduleUpdateNow(mContext);
            queryAndUpdateWeather();
        }
    }

    private void queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null) {
                mLocation.setText("Somewhere 20°C");
                mLocation.setTextColor(mTextColor);
                mCurrentCondition.setTextColor(mTextColor);
                mHighLow.setTextColor(mTextColor);
                return;
            }
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo != null) {
                String formattedCondition = mWeatherInfo.condition;
                if (formattedCondition.toLowerCase().contains("clouds") || formattedCondition.toLowerCase().contains("overcast")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_clouds);
                } else if (formattedCondition.toLowerCase().contains("rain")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_rain);
                } else if (formattedCondition.toLowerCase().contains("clear")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_clear);
                } else if (formattedCondition.toLowerCase().contains("storm")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_storm);
                } else if (formattedCondition.toLowerCase().contains("snow")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_snow);
                } else if (formattedCondition.toLowerCase().contains("wind")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_wind);
                } else if (formattedCondition.toLowerCase().contains("mist")) {
                    formattedCondition = appContext.getResources().getString(R.string.weather_condition_mist);
                }
                Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
                d.setTintList(null);
                mConditionImage.setImageTintList(null);
                mConditionImage.setImageDrawable(d.getConstantState().newDrawable().mutate());
                mLocation.setText(mWeatherInfo.city.trim() + " " + mWeatherInfo.temp + " " + mWeatherInfo.tempUnits);
                mLocation.setTextColor(mTextColor);
                mCurrentCondition.setText(formattedCondition);
                mCurrentCondition.setTextColor(mTextColor);
                mHighLow.setText("H: " + mWeatherInfo.dayForecasts.get(0).high + " " + mWeatherInfo.tempUnits + " L: " + mWeatherInfo.dayForecasts.get(0).low + " " + mWeatherInfo.tempUnits);
                mHighLow.setTextColor(mTextColor);
                updateProgress();
            }
        } catch (Exception e) {
        }
    }

    private void updateProgress() {
        if (mTempProgress == null) return;
        if (mWeatherInfo == null) {
            mTempProgress
                    .setImageBitmap(
                            ArcProgressWidget
                                    .generateBitmap(
                                            mContext,
                                            20,
                                            25,
                                            "20°C",
                                            40,
                                            ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.google_30, appContext.getTheme()),
                                            false,
                                            36,
                                            null,
                                            mProgressColor,
                                            mTextColor
                                    )
                    );
            return;
        }
        if (progressType == PROGRESS_TEMPERATURE) {
            int progress, maxProgress = 100;
            Drawable d;
            String textInside;
            progress = Integer.parseInt(mWeatherInfo.temp);
            d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode).getConstantState().newDrawable().mutate();
            if (!mWeatherInfo.dayForecasts.isEmpty()) {
                maxProgress = Integer.parseInt(mWeatherInfo.dayForecasts.get(0).high);
            }
            textInside = mWeatherInfo.temp + mWeatherInfo.tempUnits;
            mTempProgress
                    .setImageBitmap(
                            ArcProgressWidget
                                    .generateBitmap(
                                            mContext,
                                            progress,
                                            maxProgress,
                                            textInside,
                                            40,
                                            d,
                                            false,
                                            36,
                                            null,
                                            mProgressColor,
                                            mTextColor
                                    )
                    );
        } else if (progressType == PROGRESS_HUMIDITY) {
            String hum = mWeatherInfo.humidity.replaceAll("[^0-9]", "");
            mTempProgress.setImageBitmap(
                    getHumidityProgress(
                            mContext,
                            appContext,
                            Integer.parseInt(hum),
                            hum + "%",
                            35,
                            null,
                            mProgressColor,
                            mTextColor)
            );
        }

    }

    @Override
    public void onWidgetModeChanged() {
        super.onWidgetModeChanged();
        removeAllViews();
        if (mCurrentMode == WidgetMode.SMALL) {
            try {
                ((ViewGroup) mTempProgress.getParent()).removeView(mTempProgress);
            } catch (Throwable ignored) {
            }
            addView(mTempProgress);
            updateProgress();
            return;
        }
        inflateView();
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
        }};
        List<String> values = new ArrayList<>() {{
            add(String.valueOf(PROGRESS_TEMPERATURE));
            add(String.valueOf(PROGRESS_HUMIDITY));
        }};
        settings.add(new SettingItem(appContext.getString(R.string.progress_type), "progress_type", SettingItem.Type.MENU, progressType, entries, values));
        return settings;
    }

    @Override
    public void applySettings(Map<String, Object> settings) {
        if (settings.isEmpty()) return;
        Object pt = settings.get("progress_type");
        if (pt instanceof Number num) {
            progressType = num.intValue();
        } else {
            progressType = Integer.parseInt(String.valueOf(settings.get("progress_type")));
        }
        updateProgress();

    }

    @Override
    public void onSetCustomColors(int progressColor, int textColor) {
        queryAndUpdateWeather();
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
