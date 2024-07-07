package it.dhd.oxygencustomizer.weather;

import static androidx.preference.PreferenceManager.getDefaultSharedPreferences;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;

import androidx.annotation.NonNull;
import androidx.concurrent.futures.CallbackToFutureAdapter;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;

public class WeatherWork extends ListenableWorker {
    final Context mContext;
    public WeatherWork(@NonNull Context appContext, @NonNull WorkerParameters workerParams) {
        super(appContext, workerParams);
        mContext = appContext;
    }
    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {

        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

        boolean isGoodNetwork =
                capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        || capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
                        && Config.isEnabled(mContext);

        if(isGoodNetwork)
            updateWeather();

        return CallbackToFutureAdapter.getFuture(completer -> {
            completer.set(isGoodNetwork ? Result.success() : Result.retry());
            return completer;
        });
    }

    private void updateWeather() {
        WeatherUpdateService.scheduleUpdateNow(mContext);
    }

}

