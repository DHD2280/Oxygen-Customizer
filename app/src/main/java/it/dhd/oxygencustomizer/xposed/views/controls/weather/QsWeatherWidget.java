package it.dhd.oxygencustomizer.xposed.views.controls.weather;

import android.content.Context;
import android.widget.LinearLayout;

@SuppressWarnings("viewConstructor")
public class QsWeatherWidget extends LinearLayout {

    private final static String TAG = "QsWeatherWidget";

    private final Context mContext;
    private Context appContext;
    private final WeatherBinder mWeatherBinder;

    public QsWeatherWidget(Context context) {
        this(context, false);
    }

    public QsWeatherWidget(Context context, boolean settingsInterface) {
        super(context);

        mContext = context;
        mWeatherBinder = new WeatherBinder(mContext, TAG, settingsInterface);
        mWeatherBinder.inflateViews(this);

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
