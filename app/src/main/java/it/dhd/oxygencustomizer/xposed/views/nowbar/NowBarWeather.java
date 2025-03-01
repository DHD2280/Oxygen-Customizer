package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static de.robv.android.xposed.XposedBridge.log;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setTextRecursively;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class NowBarWeather extends RelativeLayout implements OmniJawsClient.OmniJawsObserver {

    private final String TAG = "NowBarWeather: ";

    private final Context mContext;
    private Context appContext;
    private ActivityLauncherUtils mActivityLauncherUtils;

    private final OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    private TextView mCity;
    private TextView mCondition;
    private ImageView mConditionImage;

    private boolean mCustomColors = false;
    private int mTextColor = Color.WHITE;
    private int mBackgroundColor = Color.parseColor("#0D47A1");

    public NowBarWeather(Context context) {
        super(context);
        mContext = context;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        mWeatherClient = new OmniJawsClient(context);

        inflateView();
        enableUpdates();
        setBarBackground();

        setOnLongClickListener(v -> {
            mActivityLauncherUtils.launchWeatherActivity(false);
            return true;
        });
    }

    private void setBarBackground() {
        GradientDrawable background = new GradientDrawable();
        background.setColor(mCustomColors ? mBackgroundColor : Color.parseColor("#0D47A1"));
        background.setCornerRadius(100f);
        setBackground(background);
    }

    private void inflateView() {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(mContext, 72)));
        LayoutInflater inflater = LayoutInflater.from(appContext);
        @SuppressLint("DiscouragedApi") View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                "now_bar_weather",
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
        mCity = (TextView) ViewHelper.findViewWithTag(v, "city");
        mCondition = (TextView) ViewHelper.findViewWithTag(v, "current_condition");
        mConditionImage = (ImageView) ViewHelper.findViewWithTag(v, "condition_image");
        v.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        addView(v);
    }

    public void enableUpdates() {
        log(TAG + "enableUpdates");
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this);
            //WeatherScheduler.scheduleUpdateNow(mContext);
            queryAndUpdateWeather();
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        enableUpdates();
    }

    @SuppressLint("SetTextI18n")
    private void queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null) {
                setErrorView(2);
                return;
            }
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo != null) {
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
                Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
                mConditionImage.setImageDrawable(d);
                mCondition.setText(mWeatherInfo.temp + " " + mWeatherInfo.tempUnits + " " + formattedCondition);
                mCondition.setSelected(true);
                mCity.setText(mWeatherInfo.city);
            }
        } catch (Exception e) {
            log(TAG + "Weather query failed" + e.getMessage());
            Log.e(TAG, "Weather query failed", e);
        }
    }

    @Override
    public void weatherUpdated() {
        queryAndUpdateWeather();
    }

    @Override
    public void weatherError(int errorReason) {
        setErrorView(errorReason);
    }

    private void setErrorView(int errorReason) {
        log(TAG + "setErrorView - " + errorReason);
        boolean reQuery = false;
        String errorText = switch (errorReason) {
            case OmniJawsClient.EXTRA_ERROR_DISABLED ->
                    modRes.getString(R.string.omnijaws_service_disabled);
            case OmniJawsClient.EXTRA_ERROR_NO_PERMISSIONS ->
                    modRes.getString(R.string.omnijaws_service_error_permissions);
            default -> "";
        };
        if (!TextUtils.isEmpty(errorText)) {
            mCity.setText(errorText);
        } else {
            reQuery = true;
        }
        if (reQuery) {
            queryAndUpdateWeather();
        } else {
            setTextRecursively(this, "");
            mConditionImage.setImageDrawable(null);
        }
    }

    public void setOptions(
            boolean customColor, int textColor, int backgroundColor
    ) {
        mCustomColors = customColor;
        mTextColor = textColor;
        mBackgroundColor = backgroundColor;
        setBarBackground();
        mCity.setTextColor(mCustomColors ? mTextColor : Color.WHITE);
        mCondition.setTextColor(mCustomColors ? mTextColor : Color.WHITE);
    }

}
