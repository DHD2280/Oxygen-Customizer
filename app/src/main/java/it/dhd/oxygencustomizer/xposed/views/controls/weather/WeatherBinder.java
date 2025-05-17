package it.dhd.oxygencustomizer.xposed.views.controls.weather;

import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.SeparateQsWidgetsFactory;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class WeatherBinder implements OmniJawsClient.OmniJawsObserver {

    private final String TAG = "WeatherBinder (%s): ";
    private final String VIEW_TAG;

    private final Context mContext;
    private Context appContext;

    private TextView mCity, mTemp, mCondition;
    private ImageView mWeatherIcon;

    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;

    private ActivityLauncherUtils mActivityLauncherUtils;
    private boolean mSettingsInterface;
    private ImageView mImageView;
    private ExtendedFAB mFab;

    public WeatherBinder(Context context, String vTag) {
        this(context, vTag, false);
    }

    public WeatherBinder(Context context, String vTag, boolean settingsInterface) {
        mContext = context;
        VIEW_TAG = vTag;
        mSettingsInterface = settingsInterface;
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
        mImageView = SeparateQsWidgetsFactory.createImageView(mContext, settingsInterface);
        mFab = SeparateQsWidgetsFactory.createFAB(mContext, settingsInterface);
    }

    public void onSizeChanged(int newWidth) {
        mImageView.setVisibility(newWidth >= 2 ? View.GONE : View.VISIBLE);
        mFab.setVisibility(newWidth >= 2 ? View.VISIBLE : View.GONE);
    }

    public void inflateViews(ViewGroup parentView) {
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
        parentView.addView(v);
        setupViews(parentView);
    }

    public void inflateSmall(ViewGroup parentView) {
        parentView.addView(mImageView);
        parentView.addView(mFab);
        mFab.setVisibility(View.GONE);
        if (!mSettingsInterface) {
            mActivityLauncherUtils = new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal());
            parentView.setOnClickListener(v -> {
                mActivityLauncherUtils.launchWeatherActivity(true);
            });
        }
    }

    private void setupViews(ViewGroup parentView) {

        queryAndUpdateWeather();

        // Use White Color
        // as we have blue background
        mCity.setTextColor(Color.WHITE);
        mTemp.setTextColor(Color.WHITE);
        mCondition.setTextColor(Color.WHITE);

        if (!mSettingsInterface) {
            mActivityLauncherUtils = new ActivityLauncherUtils(mContext, ControllersProvider.getActivityStarterExternal());
            parentView.setOnClickListener(v -> {
                mActivityLauncherUtils.launchWeatherActivity(true);
            });
        }
    }

    @SuppressLint("SetTextI18n")
    private void queryAndUpdateWeather() {
        Resources res = mSettingsInterface ? mContext.getResources() : modRes;
        Log.v(String.format(TAG, VIEW_TAG), "Querying weather");
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
                    formattedCondition = res.getString(R.string.weather_condition_clouds);
                } else if (formattedCondition.toLowerCase().contains("rain")) {
                    formattedCondition = res.getString(R.string.weather_condition_rain);
                } else if (formattedCondition.toLowerCase().contains("clear")) {
                    formattedCondition = res.getString(R.string.weather_condition_clear);
                } else if (formattedCondition.toLowerCase().contains("storm")) {
                    formattedCondition = res.getString(R.string.weather_condition_storm);
                } else if (formattedCondition.toLowerCase().contains("snow")) {
                    formattedCondition = res.getString(R.string.weather_condition_snow);
                } else if (formattedCondition.toLowerCase().contains("wind")) {
                    formattedCondition = res.getString(R.string.weather_condition_wind);
                } else if (formattedCondition.toLowerCase().contains("mist")) {
                    formattedCondition = res.getString(R.string.weather_condition_mist);
                }

                final Drawable d = mWeatherClient.getWeatherConditionImage(mWeatherInfo.conditionCode);
                if (mCity != null) mCity.setText(mWeatherInfo.city);
                if (mTemp != null) mTemp.setText(mWeatherInfo.temp + mWeatherInfo.tempUnits);
                if (mCondition != null) mCondition.setText(formattedCondition);
                if (mWeatherIcon != null) mWeatherIcon.setImageDrawable(d);
                mImageView.setImageTintList(null);
                mImageView.clearColorFilter();
                mImageView.setImageDrawable(d);
                mFab.setIconTint(null);
                mFab.setIcon(d);
                mFab.setText(mWeatherInfo.temp + mWeatherInfo.tempUnits + " • " + formattedCondition);
            }
        } catch (Exception e) {
            Log.e(String.format(TAG, VIEW_TAG), "Error updating weather: " + e.getMessage());
        }
    }

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
        mCity.setText("");
        mCondition.setText("");
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            mWeatherInfo = null;
        } else if (errorReason == OmniJawsClient.EXTRA_ERROR_NETWORK) {
            mTemp.setText("Location disabled");
        }
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

}
