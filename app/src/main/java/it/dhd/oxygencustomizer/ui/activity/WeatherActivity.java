package it.dhd.oxygencustomizer.ui.activity;

import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;

import android.content.ContentValues;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ActivityWeatherBinding;
import it.dhd.oxygencustomizer.ui.adapters.ForecastDayAdapter;
import it.dhd.oxygencustomizer.ui.adapters.ForecastHourAdapter;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;
import it.dhd.oxygencustomizer.weather.WeatherContentProvider;

public class WeatherActivity extends AppCompatActivity implements OmniJawsClient.OmniJawsObserver {

    private static final String TAG = "WeatherActivity";
    private static final boolean DEBUG = false;
    private OmniJawsClient mWeatherClient;
    private ActivityWeatherBinding binding;
    private ForecastDayAdapter mForecastDayAdapter;
    private ForecastHourAdapter mForecastHourAdapter;

    /** The background colors of the app, it changes thru out the day to mimic the sky. **/
    public static final String[] BACKGROUND_SPECTRUM = { "#212121", "#27232e", "#2d253a",
            "#332847", "#382a53", "#3e2c5f", "#442e6c", "#393a7a", "#2e4687", "#235395", "#185fa2",
            "#0d6baf", "#0277bd", "#0d6cb1", "#1861a6", "#23569b", "#2d4a8f", "#383f84", "#433478",
            "#3d3169", "#382e5b", "#322b4d", "#2c273e", "#272430" };
    public static final String[] BACKGROUND_CARD_SPECTRUM = { "#171717", "#1b1820", "#201a29",
            "#241c32", "#271d3a", "#2b1f42", "#30204c", "#282955", "#20315e", "#183a68", "#114271",
            "#094b7a", "#015384", "#094c7c", "#114474", "#183c6c", "#203464", "#272c5c", "#2f2454",
            "#2b224a", "#272040", "#231e36", "#1f1b2b", "#1b1922" };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setShowWhenLocked(true);
        setTurnScreenOn(true);

        binding = ActivityWeatherBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mWeatherClient = new OmniJawsClient(this);
        mForecastDayAdapter = new ForecastDayAdapter(mWeatherClient);
        mForecastHourAdapter = new ForecastHourAdapter(mWeatherClient);
        updateHourColor();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finishAffinity();
            }
        });

        binding.settings.setOnClickListener(v -> startActivity(mWeatherClient.getSettingsIntent()));
        binding.refresh.setOnClickListener(v -> forceRefresh());

        binding.hourlyForecastRecycler.setAdapter(mForecastHourAdapter);
        binding.hourlyForecastRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.dailyForecastRecycler.setAdapter(mForecastDayAdapter);
        binding.dailyForecastRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        startProgress();

        queryAndUpdateWeather();
    }

    private void updateViews() {

        // Title
        binding.currentLocation.setText(mWeatherClient.getWeatherInfo().city);

        // Current Condition
        binding.currentTemperature.setText(mWeatherClient.getWeatherInfo().temp);
        binding.currentTemperatureUnit.setText(mWeatherClient.getWeatherInfo().tempUnits);
        binding.currentCondition.setText(getWeatherCondition(mWeatherClient.getWeatherInfo().condition));
        binding.currentConditionIcon.setImageDrawable(mWeatherClient.getWeatherConditionImage(mWeatherClient.getWeatherInfo().conditionCode));

        // Wind and Humidity
        binding.currentWind.setText(mWeatherClient.getWeatherInfo().windSpeed + " " + mWeatherClient.getWeatherInfo().windUnits);
        binding.currentWindDirection.setText(mWeatherClient.getWeatherInfo().pinWheel);
        binding.currentHumidity.setText(mWeatherClient.getWeatherInfo().humidity);

        // Provider Info
        binding.currentProvider.setText(mWeatherClient.getWeatherInfo().provider);
        String format = DateFormat.is24HourFormat(this) ? "HH:mm" : "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        binding.lastUpdate.setText(sdf.format(mWeatherClient.getWeatherInfo().timeStamp));

    }
    
    private String getWeatherCondition(String formattedCondition) {
        if (formattedCondition.toLowerCase().contains("clouds") || formattedCondition.toLowerCase().contains("overcast")) {
            formattedCondition = getString(R.string.weather_condition_clouds);
        } else if (formattedCondition.toLowerCase().contains("rain")) {
            formattedCondition = getString(R.string.weather_condition_rain);
        } else if (formattedCondition.toLowerCase().contains("clear")) {
            formattedCondition = getString(R.string.weather_condition_clear);
        } else if (formattedCondition.toLowerCase().contains("storm")) {
            formattedCondition = getString(R.string.weather_condition_storm);
        } else if (formattedCondition.toLowerCase().contains("snow")) {
            formattedCondition = getString(R.string.weather_condition_snow);
        } else if (formattedCondition.toLowerCase().contains("wind")) {
            formattedCondition = getString(R.string.weather_condition_wind);
        } else if (formattedCondition.toLowerCase().contains("mist")) {
            formattedCondition = getString(R.string.weather_condition_mist);
        }
        return formattedCondition;
    }

    @Override
    public void onResume() {
        super.onResume();
        mWeatherClient.addObserver(this);
        queryAndUpdateWeather();
    }

    @Override
    public void onPause() {
        super.onPause();
        mWeatherClient.removeObserver(this);
    }

    @Override
    public void weatherUpdated() {
        if (DEBUG) Log.d(TAG, "weatherUpdated");
        queryAndUpdateWeather();
    }

    @Override
    public void weatherError(int errorReason) {
        if (DEBUG) Log.d(TAG, "weatherError " + errorReason);
    }

    private void queryAndUpdateWeather() {
        stopProgress();
        mWeatherClient.queryWeather();
        if (mWeatherClient.getWeatherInfo().hourlyForecasts.size() >= 2) {
            mForecastHourAdapter.updateList(mWeatherClient.getWeatherInfo().hourlyForecasts);
            binding.hourlyForecastCard.setVisibility(View.VISIBLE);
            binding.hourlyForecastRecycler.scrollToPosition(0);
        } else {
            binding.hourlyForecastCard.setVisibility(View.GONE);
        }
        if (!mWeatherClient.getWeatherInfo().dayForecasts.isEmpty()) {
            mForecastDayAdapter.updateList(mWeatherClient.getWeatherInfo().dayForecasts);
            binding.dailyForecastCard.setVisibility(View.VISIBLE);
        } else {
            binding.dailyForecastCard.setVisibility(View.GONE);
        }
        updateViews();
    }

    private void forceRefresh() {
        if (mWeatherClient.isOmniJawsEnabled()) {
            startProgress();
            ContentValues values = new ContentValues();
            values.put(WeatherContentProvider.COLUMN_FORCE_REFRESH, true);
            this.getContentResolver().update(OmniJawsClient.CONTROL_URI,
                    values, "", null);

            //WeatherUpdateService.scheduleUpdateNow(getContext());
        }
    }

    private void startProgress() {
        binding.progress.setVisibility(View.VISIBLE);
        binding.weatherLayout.setVisibility(View.GONE);
    }

    private void stopProgress() {
        binding.progress.setVisibility(View.GONE);
        binding.weatherLayout.setVisibility(View.VISIBLE);
    }

    private int getCurrentHourColor() {
        final int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return Color.parseColor(BACKGROUND_SPECTRUM[hourOfDay]);
    }

    private int getCurrentCardColor() {
        final int hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        return Color.parseColor(BACKGROUND_CARD_SPECTRUM[hourOfDay]);
    }

    public void updateHourColor() {
        getWindow().getDecorView().setBackgroundColor(getCurrentHourColor());
        getWindow().setNavigationBarColor(getCurrentHourColor());
        getWindow().setStatusBarColor(getCurrentHourColor());
        binding.hourlyForecastCard.setCardBackgroundColor(getCurrentCardColor());
        binding.hourlyForecastCard.setStrokeColor(getCurrentCardColor());
        binding.dailyForecastCard.setCardBackgroundColor(getCurrentCardColor());
        binding.dailyForecastCard.setStrokeColor(getCurrentCardColor());
    }
}
