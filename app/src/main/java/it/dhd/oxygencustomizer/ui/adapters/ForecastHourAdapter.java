package it.dhd.oxygencustomizer.ui.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.databinding.ViewListForecastHourItemBinding;
import it.dhd.oxygencustomizer.weather.OmniJawsClient;

public class ForecastHourAdapter extends RecyclerView.Adapter<ForecastHourAdapter.ViewHolder> {

    private List<OmniJawsClient.HourForecast> mList = new ArrayList<>();
    private OmniJawsClient mWeatherClient;

    public ForecastHourAdapter(OmniJawsClient client) {
        mWeatherClient = client;
    }

    @NonNull
    public ForecastHourAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ViewListForecastHourItemBinding binding = ViewListForecastHourItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding, mWeatherClient);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastHourAdapter.ViewHolder holder, int position) {
        OmniJawsClient.HourForecast forecast = mList.get(position);
        holder.bind(forecast);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void updateList(List<OmniJawsClient.HourForecast> list) {
        mList.clear();
        mList.addAll(list);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final ViewListForecastHourItemBinding binding;
        private final OmniJawsClient mWeatherClient;

        ViewHolder(@NonNull ViewListForecastHourItemBinding binding, OmniJawsClient weatherClient) {
            super(binding.getRoot());
            this.binding = binding;
            mWeatherClient = weatherClient;
        }

        @SuppressLint("SetTextI18n")
        public void bind(OmniJawsClient.HourForecast forecast) {
            binding.forecastTime.setText(fotmatHour(forecast.time));
            binding.forecastIcon.setImageDrawable(mWeatherClient.getWeatherConditionImage(forecast.conditionCode));
            binding.forecastTemperature.setText(forecast.temperature + "°");
        }

        private String fotmatHour(String inputDate) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
            SimpleDateFormat dayMonthFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

            try {
                Date date = inputFormat.parse(inputDate);

                return dayMonthFormat.format(date);

            } catch (Exception e) {
                Log.e("ForecastHourAdapter", "Error parsing hours", e);
                return null;
            }
        }
    }

}
