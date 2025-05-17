package it.dhd.oxygencustomizer.xposed.views.controls.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class QsMiniWeather extends LinearLayout {

    private final WeatherBinder mWeatherBinder;

    public QsMiniWeather(@NonNull Context context, boolean settingsInterface) {
        super(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        setLayoutParams(layoutParams);
        mWeatherBinder = new WeatherBinder(context, QsMiniWeather.class.getSimpleName(), settingsInterface);
        mWeatherBinder.inflateSmall(this);
    }

    public void onSizeChanged(int newWidth) {
        mWeatherBinder.onSizeChanged(newWidth);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWeatherBinder.enableWeatherUpdates();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWeatherBinder.disableWeatherUpdates();
    }

}
