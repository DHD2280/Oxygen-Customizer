package it.dhd.oxygencustomizer.ui.adapters;

import static it.dhd.oxygencustomizer.OxygenCustomizer.getAppContext;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.databinding.ViewListForecastDayItemBinding;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;

public class ForecastDayAdapter extends RecyclerView.Adapter<ForecastDayAdapter.ViewHolder> {

    private List<OmniJawsClient.DayForecast> mList = new ArrayList<>();
    private final OmniJawsClient mWeatherClient;

    public ForecastDayAdapter(OmniJawsClient weatherClient) {
        mWeatherClient = weatherClient;
        mWeatherClient.queryWeather();
    }

    @NonNull
    public ForecastDayAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewListForecastDayItemBinding binding = ViewListForecastDayItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, mWeatherClient);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastDayAdapter.ViewHolder holder, int position) {
        OmniJawsClient.DayForecast forecast = mList.get(position);
        holder.bind(forecast);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateList(List<OmniJawsClient.DayForecast> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewListForecastDayItemBinding binding;
        private final OmniJawsClient mWeatherClient;

        ViewHolder(@NonNull ViewListForecastDayItemBinding binding, OmniJawsClient weatherClient) {
            super(binding.getRoot());
            this.binding = binding;
            mWeatherClient = weatherClient;
        }

        @SuppressLint("SetTextI18n")
        public void bind(OmniJawsClient.DayForecast forecast) {
            binding.forecastTime.setText(formatDate(forecast.date));
            binding.forecastIcon.setImageDrawable(mWeatherClient.getWeatherConditionImage(forecast.conditionCode));
            binding.forecastTemperature.setText(forecast.low + "° / " + forecast.high + "°");
        }

        private String formatDate(String inputDate) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat dayMonthFormat = new SimpleDateFormat("dd MMM", Locale.getDefault());

            try {
                Date date = inputFormat.parse(inputDate);
                Calendar inputCalendar = Calendar.getInstance();
                inputCalendar.setTime(date);

                Calendar today = Calendar.getInstance();
                Calendar tomorrow = Calendar.getInstance();
                tomorrow.add(Calendar.DAY_OF_YEAR, 1);

                String formattedDate = dayMonthFormat.format(date);

                if (isSameDay(inputCalendar, today)) {
                    return formattedDate + " " + getAppContext().getString(R.string.omnijaws_today);
                } else if (isSameDay(inputCalendar, tomorrow)) {
                    return formattedDate + " " + getAppContext().getString(R.string.omnijaws_tomorrow);
                } else {
                    SimpleDateFormat dayOfWeekFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                    String dayOfWeek = dayOfWeekFormat.format(date);
                    return formattedDate + " " + dayOfWeek;
                }

            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        private boolean isSameDay(Calendar cal1, Calendar cal2) {
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        }

    }

}
