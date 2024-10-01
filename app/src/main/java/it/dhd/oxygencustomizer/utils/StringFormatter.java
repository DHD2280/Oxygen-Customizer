package it.dhd.oxygencustomizer.utils;

import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.text.SpannableStringBuilder;
import android.util.Log;

import androidx.annotation.NonNull;

import com.github.mfathi91.time.PersianDate;

import java.awt.font.NumericShaper;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThermalProvider;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class StringFormatter {

    private Timer scheduler = new Timer();
    private static final ArrayList<StringFormatter> instances = new ArrayList<>();
    private final ArrayList<FormattedStringCallback> callbacks = new ArrayList<>();
    private boolean hasDate = false;

    public StringFormatter() {
        instances.add(this);
        scheduleNextDateUpdate();
    }

    public static void refreshAll() {
        instances.forEach(StringFormatter::informCallbacks);
    }

    private void informCallbacks() {
        for (FormattedStringCallback callback : callbacks) {
            callback.onRefreshNeeded();
        }
    }

    private void scheduleNextDateUpdate() {
        try {
            AlarmManager alarmManager = SystemUtils.AlarmManager();

            Calendar alarmTime = Calendar.getInstance();
            alarmTime.set(Calendar.HOUR_OF_DAY, 0);
            alarmTime.set(Calendar.MINUTE, 0);
            alarmTime.add(Calendar.DATE, 1);

            //noinspection ConstantConditions
            alarmManager.set(AlarmManager.RTC,
                    alarmTime.getTimeInMillis(),
                    "",
                    () -> {
                        scheduleNextDateUpdate();
                        if (hasDate) {
                            informCallbacks();
                        }
                    },
                    null);

        } catch (Throwable t) {
            if (BuildConfig.DEBUG) {
                try {
                    log("Error setting formatted string update schedule");
                    log(t);
                } catch (Throwable ignored) {}
            }
        }
    }

    private CharSequence temperatureOf(String format)
    {
        try
        {
            Matcher match = Pattern.compile("^([A-Za-z])([0-9]*)$").matcher(format);

            if(!match.find())
            {
                throw new Exception();
            }
            String typeStr = match.group(1);

            long nextUpdate;

            try
            {
                //noinspection ConstantConditions
                nextUpdate = Integer.parseInt(match.group(2));
            }
            catch (Throwable ignored)
            {
                nextUpdate = 60;
            }

            nextUpdate *= 1000L;

            int type;

            //noinspection ConstantConditions
            switch (typeStr.toLowerCase())
            {
                case "b":
                    type = ThermalProvider.BATTERY;
                    break;
                case "c":
                    type = ThermalProvider.CPU;
                    break;
                case "g":
                    type = ThermalProvider.GPU;
                    break;
                case "s":
                    type = ThermalProvider.SKIN;
                    break;
                default:
                    throw new Exception();
            }

            int temperature = ThermalProvider.getTemperatureMaxInt(type);

            if(temperature < -990)
            {
                scheduleUpdate(1000L);
                return "Err";
            }

            scheduleUpdate(nextUpdate);

            return String.valueOf(temperature);

        } catch (Exception ignored)
        {
            return "$T" + format;
        }
    }

    private CharSequence georgianDateOf(String format) {
        try {
            @SuppressLint("SimpleDateFormat")
            String result = new SimpleDateFormat(format).
                    format(
                            Calendar.getInstance().getTime()
                    );
            hasDate = true;
            return result;
        } catch (Exception ignored) {
            return "$G" + format;
        }
    }

    private CharSequence persianDateOf(String format) {
        try {
            String result = PersianDate.now().format(
                    DateTimeFormatter.ofPattern(
                            format,
                            Locale.forLanguageTag("fa")
                    )
            );
            hasDate = true;
            char[] bytes = result.toCharArray();
            NumericShaper.getShaper(NumericShaper.EASTERN_ARABIC).shape(bytes, 0, bytes.length); //Numbers to be shown in correct font
            return String.copyValueOf(bytes);
        } catch (Exception ignored) {
            return "$P" + format;
        }
    }

    public CharSequence formatString(String input) {
        Log.d("StringFormatter", "Formatting string: " + input);
        SpannableStringBuilder result = new SpannableStringBuilder(input);
        hasDate = false;
        Pattern pattern = Pattern.compile("\\$((T[a-zA-Z][0-9]*)|([A-Z][A-Za-z]+))"); //variables start with $ and continue with characters, until they don't!

        //We'll locate each variable and replace it with a value, if possible
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String match = matcher.group(1);

            int start = result.toString().indexOf("$" + match);

            Log.d("StringFormatter", "Match: " + match + " at " + start + " value: " + valueOf(match));

            //noinspection ConstantConditions
            result.replace(start, start + match.length() + 1, valueOf(match));
        }
        return result;
    }

    private CharSequence valueOf(String match) {
        return switch (match.substring(0, 1)) {
            case "G" -> georgianDateOf(match.substring(1));
            case "P" -> persianDateOf(match.substring(1));
            case "T" -> temperatureOf(match.substring(1));
            default -> "$" + match;
        };
    }

    public void registerCallback(@NonNull FormattedStringCallback callback) {
        callbacks.add(callback);
    }

    @SuppressWarnings("unused")
    public void unRegisterCallback(@NonNull FormattedStringCallback callback) {
        callbacks.remove(callback);
    }

    @SuppressWarnings("unused")
    public void resetCallbacks() {
        callbacks.clear();
    }

    private void scheduleUpdate(long nextUpdate) {
        scheduler.cancel();

        scheduler = new Timer();
        scheduler.schedule(new TimerTask() {
            @Override
            public void run() {
                informCallbacks();
            }
        }, nextUpdate);
    }

    public interface FormattedStringCallback {
        void onRefreshNeeded();
    }

}
