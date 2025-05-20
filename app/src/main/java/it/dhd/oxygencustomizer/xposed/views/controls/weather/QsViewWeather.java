package it.dhd.oxygencustomizer.xposed.views.controls.weather;

import android.content.Context;
import android.content.res.Configuration;
import android.view.ViewGroup;

import com.oplus.systemui.qs.base.tile.PressFeedbackHelper;

import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;

/**
 * A class representing the weather view in the Quick Settings panel.
 * Note, this is only for OOS15
 */
public class QsViewWeather extends BaseQsStaticView {

    private final static String TAG = "QsWeatherWidget: ";

    private final Context mContext;
    private Context appContext;
    private final WeatherBinder mWeatherBinder;
    private final boolean mSettingsInterface;

    public QsViewWeather(Context context) {
        this(context, false);
    }

    public QsViewWeather(Context context, boolean settingsInterface) {
        super(context);
        mSettingsInterface = settingsInterface;
        mContext = context;
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mWeatherBinder = new WeatherBinder(mContext, TAG, settingsInterface);
        mWeatherBinder.inflateViews(this);
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

    public void refreshViewBackground() {
        this.backgroundProxy.refreshViewBackground();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mWeatherBinder.onConfigurationChanged();
    }

}
