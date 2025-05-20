package it.dhd.oxygencustomizer.xposed.views.controls.weather;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.oplus.systemui.qs.base.tile.PressFeedbackHelper;

import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;

/**
 * OOS 15 class
 */
@SuppressLint("ViewConstructor")
public class QsMiniWeatherView extends BaseQsStaticView {

    private final WeatherBinder mWeatherBinder;
    private final boolean mSettingsInterface;

    public QsMiniWeatherView(@NonNull Context context, boolean settingsInterface) {
        super(context);
        mSettingsInterface = settingsInterface;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        setLayoutParams(layoutParams);
        mWeatherBinder = new WeatherBinder(context, QsMiniWeatherView.class.getSimpleName(), settingsInterface);
        mWeatherBinder.inflateSmall(this);
    }

    public void onSizeChanged(int newWidth) {
        mWeatherBinder.onSizeChanged(newWidth);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWeatherBinder.onConfigurationChanged();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.backgroundProxy.onBackgroundAttach();
        if (!mSettingsInterface) {
            PressFeedbackHelper.attachPressFeedback(this, this);
        }
        mWeatherBinder.enableWeatherUpdates();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.backgroundProxy.onBackgroundDetach();
        mWeatherBinder.disableWeatherUpdates();
    }

}