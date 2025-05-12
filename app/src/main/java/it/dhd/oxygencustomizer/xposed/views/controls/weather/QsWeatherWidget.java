package it.dhd.oxygencustomizer.xposed.views.controls.weather;

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
