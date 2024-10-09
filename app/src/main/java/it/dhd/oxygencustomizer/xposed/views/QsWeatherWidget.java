package it.dhd.oxygencustomizer.xposed.views;

import static de.robv.android.xposed.XposedBridge.log;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

@SuppressWarnings("viewConstructor")
public class QsWeatherWidget extends LinearLayout implements OmniJawsClient.OmniJawsObserver {

    private final static String TAG = "QsWeatherWidget: ";

    private final Context mContext;
    private Context appContext;

    private TextView mCity, mTemp, mCondition;
    private ImageView mWeatherIcon;

    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    public QsWeatherWidget(Context context) {
        super(context);

        mContext = context;
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (mWeatherClient == null) {
            mWeatherClient = new OmniJawsClient(context);
        }
        inflateView();

    }

    private void inflateView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);
        View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "view_qs_weather_widget",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        mCity = (TextView) ViewHelper.findViewWithTag(v, "weather_location");
        mCity.setSelected(true);
        mTemp = (TextView) ViewHelper.findViewWithTag(v, "weather_current_temp");
        mCondition = (TextView) ViewHelper.findViewWithTag(v, "weather_condition");
        mWeatherIcon = (ImageView) ViewHelper.findViewWithTag(v, "weather_icon");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        layoutParams.setMargins(
                dp2px(mContext, 8),
                dp2px(mContext, 8),
                dp2px(mContext, 8),
                dp2px(mContext, 8)
        );
        v.setLayoutParams(layoutParams);
        addView(v);
        setupViews();
    }

    private void setupViews() {

        queryAndUpdateWeather();

        // Use White Color
        // as we have blue background
        mCity.setTextColor(Color.WHITE);
        mTemp.setTextColor(Color.WHITE);
        mCondition.setTextColor(Color.WHITE);
    }

    public void enableWeatherUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this);
            queryAndUpdateWeather();
        }
    }

    public void disableWeatherUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient.removeObserver(this);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        enableWeatherUpdates();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        disableWeatherUpdates();
    }

    @SuppressLint("SetTextI18n")
    private void queryAndUpdateWeather() {
        log(TAG + "Querying weather");
        try {
            if (mWeatherClient == null) {
                return;
            }
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo != null) {
                // OpenWeatherMap
                String formattedCondition = mWeatherInfo.condition;
                if (formattedCondition.toLowerCase().contains("clouds") || formattedCondition.toLowerCase().contains("overcast")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clouds);
                } else if (formattedCondition.toLowerCase().contains("rain")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_rain);
                } else if (formattedCondition.toLowerCase().contains("clear")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_clear);
                } else if (formattedCondition.toLowerCase().contains("storm")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_storm);
                } else if (formattedCondition.toLowerCase().contains("snow")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_snow);
                } else if (formattedCondition.toLowerCase().contains("wind")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_wind);
                } else if (formattedCondition.toLowerCase().contains("mist")) {
                    formattedCondition = modRes.getString(R.string.weather_condition_mist);
                }

                final Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
                mCity.setText(mWeatherInfo.city);
                mTemp.setText(mWeatherInfo.temp + mWeatherInfo.tempUnits);
                mCondition.setText(formattedCondition);
                mWeatherIcon.setImageDrawable(d);
            }
        } catch (Exception e) {
            log(TAG + "Error updating weather: " + e.getMessage());
        }
    }

    @Override
    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    @Override
    public void weatherError(int errorReason) {
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            mWeatherInfo = null;
        }
    }
}
