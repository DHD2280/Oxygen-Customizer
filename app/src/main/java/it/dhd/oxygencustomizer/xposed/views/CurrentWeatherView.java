package it.dhd.oxygencustomizer.xposed.views;

/*
 * Copyright (C) 2023-2024 crDroid Android Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import static de.robv.android.xposed.XposedBridge.log;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.applyTextSizeRecursively;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setTextRecursively;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.weather.WeatherUpdateService;
import it.dhd.oxygencustomizer.xposed.utils.OmniJawsClient;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;


public class CurrentWeatherView extends LinearLayout implements OmniJawsClient.OmniJawsObserver {

    static final String TAG = "CurrentWeatherView: ";

    private ImageView mCurrentImage, mHumImage, mWindImage;
    private OmniJawsClient mWeatherClient;
    private OmniJawsClient.WeatherInfo mWeatherInfo;
    private TextView mLeftText, mRightText, mWeatherText; // Weather Layout
    private TextView mHumText, mWindText;
    private LinearLayout mWeatherLayout, mHumLayout, mWindLayout;
    private Drawable mHumDrawable, mWindDrawable;
    private int mWeatherBgSelection = 0;

    private boolean mShowWeatherLocation;
    private boolean mShowWeatherText;
    private boolean mShowWeatherHumidity, mShowWeatherWind;
    @SuppressLint("StaticFieldLeak")
    public static CurrentWeatherView instance = null;
    private int mWeatherHorPadding = 0, mWeatherVerPadding = 0;
    private final Context mContext;

    public CurrentWeatherView(Context context) {
        super(context);
        instance = this;
        mContext = context;
        mWeatherClient = new OmniJawsClient(context, true);

        setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(VERTICAL);

        mWeatherLayout = new LinearLayout(context);
        mWeatherLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mWeatherLayout.setOrientation(HORIZONTAL);

        mLeftText = new TextView(context);
        mLeftText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mLeftText.setTextColor(Color.WHITE); // Aggiungi il colore desiderato
        mLeftText.setSingleLine(true);
        mLeftText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mLeftText.setEllipsize(TextUtils.TruncateAt.END);
        mLeftText.setTag("text");

        mCurrentImage = new ImageView(context);
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dp2px(mContext, 18), dp2px(mContext, 18));
        mCurrentImage.setLayoutParams(imageParams);

        mRightText = new TextView(context);
        mRightText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mRightText.setTextColor(Color.WHITE);
        mRightText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mRightText.setSingleLine(true);
        mRightText.setTag("text");

        mWeatherText = new TextView(context);
        mWeatherText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mWeatherText.setTextColor(Color.WHITE);
        mWeatherText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        mWeatherText.setSingleLine(true);
        mWeatherText.setTag("text");

        mWeatherLayout.addView(mLeftText);
        mWeatherLayout.addView(mCurrentImage);
        mWeatherLayout.addView(mRightText);
        mWeatherLayout.addView(mWeatherText);

        mHumLayout = new LinearLayout(context);
        mHumLayout.setOrientation(HORIZONTAL);
        mHumLayout.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setMargins(mHumLayout, context, 0, dp2px(context, 1), 0, 0);

        mHumImage = new ImageView(context);
        mHumImage.setLayoutParams(new ViewGroup.LayoutParams(dp2px(context, 18), dp2px(context, 18)));
        mHumImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mHumDrawable = ResourcesCompat.getDrawable(
                modRes,
                modRes.getIdentifier("ic_humidity_symbol", "drawable", BuildConfig.APPLICATION_ID),
                context.getTheme()
        );

        mHumText = new TextView(context);
        mHumText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mHumText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mHumText.setTextColor(Color.WHITE);
        mHumText.setTag("text");
        setMargins(mHumText, context, dp2px(context, 2), 0, 0, 0);

        mHumLayout.addView(mHumImage);
        mHumLayout.addView(mHumText);

        mWindLayout = new LinearLayout(context);
        mWindLayout.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        setMargins(mWindLayout, context, 0, dp2px(context, 1), 0, 0);

        mWindImage = new ImageView(context);
        mWindImage.setLayoutParams(new LayoutParams(dp2px(context, 18), dp2px(context, 18)));
        mWindImage.setScaleType(ImageView.ScaleType.FIT_XY);
        mWindDrawable = ResourcesCompat.getDrawable(
                modRes,
                modRes.getIdentifier("ic_wind_symbol", "drawable", BuildConfig.APPLICATION_ID),
                context.getTheme()
        );

        mWindText = new TextView(context);
        mWindText.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        mWindText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        mWindText.setTextColor(Color.WHITE);
        mWindText.setTag("text");
        setMargins(mWindText, context, dp2px(context, 2), 0, 0, 0);

        mWindLayout.addView(mWindImage);
        mWindLayout.addView(mWindText);

        addView(mWeatherLayout);
        addView(mHumLayout);
        addView(mWindLayout);

        enableUpdates();
    }

    public void updateSizes(int weatherTextSize, int weatherImageSize) {
        if (instance == null) return;
        instance.mCurrentImage.setLayoutParams(new LinearLayout.LayoutParams(dp2px(instance.mContext, weatherImageSize), dp2px(instance.mContext, weatherImageSize)));
        instance.mHumImage.setLayoutParams(new LinearLayout.LayoutParams(dp2px(instance.mContext, weatherImageSize), dp2px(instance.mContext, weatherImageSize)));
        instance.mWindImage.setLayoutParams(new LinearLayout.LayoutParams(dp2px(instance.mContext, weatherImageSize), dp2px(instance.mContext, weatherImageSize)));
        applyTextSizeRecursively(instance, weatherTextSize);
    }

    public void updateColors(int color) {
        if (instance == null) return;
        ViewHelper.findViewWithTagAndChangeColor(instance, "text", color);
    }

    public void enableUpdates() {
        log(TAG + "enableUpdates");
        if (mWeatherClient != null) {
            mWeatherClient.addObserver(this);
            WeatherUpdateService.scheduleUpdateNow(mContext);
            queryAndUpdateWeather();
        }
    }

    public void disableUpdates() {
        if (mWeatherClient != null) {
            mWeatherClient.removeObserver(this);
        }
    }

    private void setErrorView() {
        setTextRecursively(instance, "");
        mCurrentImage.setImageDrawable(null);
        mHumImage.setImageDrawable(null);
        mWindImage.setImageDrawable(null);
    }

    @Override
    public void weatherError(int errorReason) {
        // since this is shown in ambient and lock screen
        // it would look bad to show every error since the
        // screen-on revovery of the service had no chance
        // to run fast enough
        // so only show the disabled state
        log(TAG + "weatherError " + errorReason);
        if (errorReason == OmniJawsClient.EXTRA_ERROR_DISABLED) {
            mWeatherInfo = null;
            setErrorView();
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

    @SuppressLint("SetTextI18n")
    private void queryAndUpdateWeather() {
        try {
            if (mWeatherClient == null || !mWeatherClient.isOmniJawsEnabled()) {
                setErrorView();
                return;
            }
            mWeatherClient.queryWeather();
            mWeatherInfo = mWeatherClient.getWeatherInfo();
            if (mWeatherInfo != null) {
                String formattedCondition = mWeatherInfo.condition;
                if (formattedCondition.toLowerCase().contains("clouds")) {
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
                mCurrentImage.setImageDrawable(d);
                mRightText.setText(mWeatherInfo.temp + " " + mWeatherInfo.tempUnits);
                mLeftText.setText(mWeatherInfo.city);
                mLeftText.setVisibility(mShowWeatherLocation ? View.VISIBLE : View.GONE);
                mWeatherText.setText(" · "  + formattedCondition);
                mWeatherText.setVisibility(mShowWeatherText ? View.VISIBLE : View.GONE);

                mHumImage.setImageDrawable(mHumDrawable);
                mHumText.setText(mWeatherInfo.humidity);
                mHumLayout.setVisibility(mShowWeatherHumidity ? View.VISIBLE : View.GONE);

                mWindImage.setImageDrawable(mWindDrawable);
                mWindText.setText(mWeatherInfo.windDirection + " " + mWeatherInfo.pinWheel + " · " + mWeatherInfo.windSpeed + " " + mWeatherInfo.windUnits);
                mWindLayout.setVisibility(mShowWeatherWind ? View.VISIBLE : View.GONE);

            }
        } catch(Exception e) {
            log(TAG + "Weather query failed");
            Log.e(TAG, "Weather query failed", e);
        }
    }

    public void updateWeatherBg(int selection) {
        if (instance == null) return;
        instance.mWeatherBgSelection = selection;
        instance.updateWeatherBg();
    }

    private void updateWeatherBg() {
        Drawable bg = null;
            switch (mWeatherBgSelection) {
                case 0: // default
                    bg = null;
                    mWeatherHorPadding = 0;
                    mWeatherVerPadding = 0;
                    break;
                case 1: // semi-transparent box
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.date_box_str_border, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_ver), mContext.getResources().getDisplayMetrics()));
                    break;
                case 2: // semi-transparent box (round)
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.date_str_border, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_box_padding_ver), mContext.getResources().getDisplayMetrics()));
                    break;
                case 3: // Q-Now Playing background
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.ambient_indication_pill_background, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.q_nowplay_pill_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.q_nowplay_pill_padding_ver), mContext.getResources().getDisplayMetrics()));
                    break;
                case 4, 5: // accent box
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.date_str_accent, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver), mContext.getResources().getDisplayMetrics()));
                    break;
                case 6: // gradient box
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.date_str_gradient, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver), mContext.getResources().getDisplayMetrics()));
                    break;
                case 7: // Dark Accent border
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.date_str_borderacc, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver), mContext.getResources().getDisplayMetrics()));
                    break;
                case 8: // Dark Gradient border
                    bg = ResourcesCompat.getDrawable(modRes, R.drawable.date_str_bordergrad, mContext.getTheme());
                    mWeatherHorPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_hor), mContext.getResources().getDisplayMetrics()));
                    mWeatherVerPadding = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, modRes.getDimensionPixelSize(R.dimen.widget_date_accent_box_padding_ver), mContext.getResources().getDisplayMetrics()));

                    break;
                default:
                    break;
            }
        setViewBackground(bg, (bg  != null && mWeatherBgSelection == 5) ? 160 : 255);
            setPadding(mWeatherHorPadding, mWeatherVerPadding, mWeatherHorPadding, mWeatherVerPadding);

    }

    public void setViewBackground(Drawable drawRes, int bgAlpha) {
        setBackground(drawRes);
        if (drawRes != null) getBackground().setAlpha(bgAlpha);
    }

    public void updateWeatherSettings(boolean showLocation, boolean showText,
                                             boolean showHumidity, boolean showWind) {
        if (BuildConfig.DEBUG) log(TAG + "updateWeatherSettings " + (instance!=null));
        instance.mShowWeatherLocation = showLocation;
        instance.mShowWeatherText = showText;
        instance.mShowWeatherHumidity = showHumidity;
        instance.mShowWeatherWind = showWind;
        instance.mLeftText.setVisibility(showLocation ? View.VISIBLE : View.GONE);
        instance.mWeatherText.setVisibility(showText ? View.VISIBLE : View.GONE);
        instance.mHumLayout.setVisibility(showHumidity ? View.VISIBLE : View.GONE);
        instance.mWindLayout.setVisibility(showWind ? View.VISIBLE : View.GONE);
        instance.updateSettings();
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static CurrentWeatherView getInstance(Context c) {
        if (instance!=null) return instance;
        return new CurrentWeatherView(c);
    }

    public static CurrentWeatherView getInstance() {
        return instance;
    }

}
