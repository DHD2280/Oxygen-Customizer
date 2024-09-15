package it.dhd.oxygencustomizer.xposed.utils;

import android.content.Context;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.text.style.ForegroundColorSpan;
import android.widget.TextClock;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.robv.android.xposed.XposedBridge;

public class TimeUtils {

    private static final String TAG = "Oxygen Customizer - " + TimeUtils.class.getSimpleName() + ": ";
    private static final String[] numbers = {
            "Zero",
            "One",
            "Two",
            "Three",
            "Four",
            "Five",
            "Six",
            "Seven",
            "Eight",
            "Nine",
            "Ten",
            "Eleven",
            "Twelve",
            "Thirteen",
            "Fourteen",
            "Fifteen",
            "Sixteen",
            "Seventeen",
            "Eighteen",
            "Nineteen",
            "Twenty",
            "Twenty One",
            "Twenty Two",
            "Twenty Three",
            "Twenty Four",
            "Twenty Five",
            "Twenty Six",
            "Twenty Seven",
            "Twenty Eight",
            "Twenty Nine",
            "Thirty",
            "Thirty One",
            "Thirty Two",
            "Thirty Three",
            "Thirty Four",
            "Thirty Five",
            "Thirty Six",
            "Thirty Seven",
            "Thirty Eight",
            "Thirty Nine",
            "Forty",
            "Forty One",
            "Forty Two",
            "Forty Three",
            "Forty Four",
            "Forty Five",
            "Forty Six",
            "Forty Seven",
            "Forty Eight",
            "Forty Nine",
            "Fifty",
            "Fifty One",
            "Fifty Two",
            "Fifty Three",
            "Fifty Four",
            "Fifty Five",
            "Fifty Six",
            "Fifty Seven",
            "Fifty Eight",
            "Fifty Nine",
            "Sixty"
    };

    private static String convertNumberToText(String number) {
        try {
            return numbers[Integer.parseInt(number)];
        } catch (Throwable throwable) {
            return number;
        }
    }

    public String regionFormattedDate(String usFormat, String euFormat) {
        try {
            Date currentDate = new Date();
            Locale currentLocale = Locale.getDefault();
            if (currentLocale.equals(Locale.US)) {
                SimpleDateFormat usDateFormat = new SimpleDateFormat(usFormat, Locale.US);
                return usDateFormat.format(currentDate);
            } else {
                SimpleDateFormat euDateFormat = new SimpleDateFormat(euFormat, currentLocale);
                return euDateFormat.format(currentDate);
            }
        } catch (Throwable throwable) {
            XposedBridge.log(TAG + throwable);
        }
        return new SimpleDateFormat(usFormat, Locale.getDefault()).format(new Date());
    }

    public String formatTime(Context context, String format24H, String format12H) {
        return formatTime(DateFormat.is24HourFormat(context) ? format24H : format12H);
    }

    private String formatTime(String format) {
        return new SimpleDateFormat(format, Locale.getDefault()).format(new Date());
    }

    public static void setCurrentTimeTextClock(Context context, TextClock tickIndicator, TextView hourView, TextView minuteView) {
        if (tickIndicator == null || hourView == null || minuteView == null) return;
        setCurrentTimeHour(context, hourView);
        setCurrentTimeMinute(minuteView);

        tickIndicator.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    setCurrentTimeHour(context, hourView);
                    setCurrentTimeMinute(minuteView);
                }
            }
        });

    }

    public static void setCurrentTimeTextClockRed(TextClock tickIndicator, TextView hourView, int color) {
        if (tickIndicator == null || hourView == null) return;

        setCurrentTimeHourRed(tickIndicator, hourView, color);

        tickIndicator.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s)) {
                    setCurrentTimeHourRed(tickIndicator, hourView, color);
                }
            }
        });
    }

    private static void setCurrentTimeHour(Context context, TextView hourView) {
        String hourFormat = DateFormat.is24HourFormat(context) ? "HH" : "hh";
        String hour = new SimpleDateFormat(hourFormat, Locale.getDefault()).format(Calendar.getInstance().getTime());
        hourView.setText(convertNumberToText(hour));
    }

    private static void setCurrentTimeHourRed(TextClock tickIndicator, TextView hourView, int color) {
        String hourFormat = tickIndicator.getText().toString();
        StringBuilder sb = new StringBuilder(hourFormat);
        SpannableString spannableString = new SpannableString(sb);
        for (int i = 0; i < 2 && i < sb.length(); i++) {
            if (sb.charAt(i) == '1') {
                spannableString.setSpan(new ForegroundColorSpan(color), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        hourView.setText(spannableString, TextView.BufferType.SPANNABLE);
    }

    private static void setCurrentTimeMinute(TextView minuteView) {
        String minuteFormat = "mm";
        String minute = new SimpleDateFormat(minuteFormat, Locale.getDefault()).format(Calendar.getInstance().getTime());
        minuteView.setText(convertNumberToText(minute));
    }

}
