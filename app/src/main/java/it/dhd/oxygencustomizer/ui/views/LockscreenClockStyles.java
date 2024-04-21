package it.dhd.oxygencustomizer.ui.views;


import static it.dhd.oxygencustomizer.utils.Constants.BATTERY_PROGRESSBAR;
import static it.dhd.oxygencustomizer.utils.Constants.BATTERY_PROGRESSBAR_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.MEDIA_PROGRESSBAR;
import static it.dhd.oxygencustomizer.utils.Constants.MEDIA_PROGRESSBAR_VALUE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AnalogClock;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import androidx.annotation.RawRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.widget.TextViewCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.ThemeUtils;

public class LockscreenClockStyles {

    static Typeface typeface = null;
    static int topMargin = 0;
    static int bottomMargin = 0;
    static int lineHeight = 4;
    static float textScaling = 0.6f;
    static boolean forceWhiteText = false;
    static boolean customColorEnabled = false;
    static int customColorCode = Color.WHITE;

    @SuppressLint("SetTextI18n")
    @SuppressWarnings("deprecation")
    public static ViewGroup initLockscreenClockStyle(Context mContext, int style) {
        LinearLayout container = new LinearLayout(mContext);
        container.setGravity(Gravity.START | Gravity.CENTER);
        customColorCode = ResourcesCompat.getColor(mContext.getResources(), R.color.white, mContext.getTheme());

        switch (style) {
            case 0 -> {
                final TextView textView0 = new TextView(mContext);
                textView0.setText("NONE");
                textView0.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.white, mContext.getTheme()));
                textView0.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 80 * textScaling);
                textView0.setLetterSpacing(0.2f);
                textView0.setTypeface(typeface != null ? typeface : textView0.getTypeface(), Typeface.BOLD);
                textView0.setGravity(Gravity.CENTER);
                final LinearLayout blank0 = new LinearLayout(mContext);
                LinearLayout.LayoutParams blankParams0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
                blankParams0.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                blank0.setLayoutParams(blankParams0);
                blank0.setOrientation(LinearLayout.VERTICAL);
                blank0.setGravity(Gravity.CENTER);
                blank0.addView(textView0);
                container = blank0;
            }
            case 1 -> {
                final TextClock date1 = new TextClock(mContext);
                date1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date1.setFormat12Hour("EEEE d MMMM");
                date1.setFormat24Hour("EEEE d MMMM");
                date1.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.material_dynamic_primary60, mContext.getTheme()));
                date1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22 * textScaling);
                date1.setTypeface(typeface != null ? typeface : date1.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams dateParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateParams1.setMargins(0, 0, 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -16, mContext.getResources().getDisplayMetrics()));
                date1.setLayoutParams(dateParams1);
                final TextClock clock1 = new TextClock(mContext);
                clock1.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clock1.setFormat12Hour("hh:mm");
                clock1.setFormat24Hour("HH:mm");
                clock1.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.material_dynamic_primary60, mContext.getTheme()));
                clock1.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 100 * textScaling);
                clock1.setTypeface(typeface != null ? typeface : clock1.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams clockParams1 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                clockParams1.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -4 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                clock1.setLayoutParams(clockParams1);
                final LinearLayout clockContainer1 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams1.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                clockContainer1.setLayoutParams(layoutParams1);
                clockContainer1.setGravity(Gravity.CENTER);
                clockContainer1.setOrientation(LinearLayout.VERTICAL);
                clockContainer1.addView(date1);
                clockContainer1.addView(clock1);
                container = clockContainer1;
            }
            case 2 -> {
                Typeface futuristicTypeface = ResourcesCompat.getFont(mContext, R.font.futurist_fixed_width_bold);
                //LayoutInflater.from(mContext).inflate(R.layout.digital_clock_10, container, true);
                int titleSize = 18;
                int timeSize = 35;
                final TextView timeis101 = new TextView(mContext);
                timeis101.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                timeis101.setText("Time is");
                timeis101.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleSize * textScaling);
                timeis101.setTypeface(futuristicTypeface);
                final TextClock time101 = new TextClock(mContext);
                ViewGroup.MarginLayoutParams timeParams101 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                timeParams101.setMargins(
                        0,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                        0,
                        0);
                time101.setLayoutParams(timeParams101);
                time101.setFormat12Hour("hh a:mm");
                time101.setFormat24Hour("HH:mm");
                time101.setTypeface(futuristicTypeface, Typeface.BOLD);
                time101.setTextSize(TypedValue.COMPLEX_UNIT_DIP, timeSize * textScaling);
                final LinearLayout divider1 = new LinearLayout(mContext);
                ViewGroup.MarginLayoutParams dividerParams1 = new ViewGroup.MarginLayoutParams(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 90 * textScaling, mContext.getResources().getDisplayMetrics()));
                dividerParams1.setMargins(
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                        0,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()),
                        0);
                divider1.setLayoutParams(dividerParams1);
                GradientDrawable mDrawable1 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{
                                Color.parseColor("#FFA2ECBB"),
                                Color.parseColor("#FFA2ECBB")});
                divider1.setBackground(mDrawable1);
                final LinearLayout firstLineSep101 = new LinearLayout(mContext);
                firstLineSep101.setLayoutParams(new LinearLayout.LayoutParams(dpToPx(20, mContext), ViewGroup.LayoutParams.WRAP_CONTENT));

                final LinearLayout firstLineVertical = new LinearLayout(mContext);
                firstLineVertical.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                firstLineVertical.setOrientation(LinearLayout.VERTICAL);
                firstLineVertical.addView(timeis101);
                firstLineVertical.addView(time101);
                final LinearLayout firstLine = new LinearLayout(mContext);
                firstLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                firstLine.setOrientation(LinearLayout.HORIZONTAL);
                firstLine.addView(divider1);
                firstLine.addView(firstLineVertical);

                final TextView date101 = new TextView(mContext);
                date101.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date101.setText("Calendar");
                date101.setTypeface(futuristicTypeface);
                date101.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleSize * textScaling);
                final TextClock date102 = new TextClock(mContext);
                ViewGroup.MarginLayoutParams dateParams102 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateParams102.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, mContext.getResources().getDisplayMetrics()), 0, 0);
                date102.setLayoutParams(timeParams101);
                date102.setFormat12Hour("MMMM dd");
                date102.setFormat24Hour("MMMM dd");
                date102.setAllCaps(true);
                date102.setTextSize(TypedValue.COMPLEX_UNIT_DIP, timeSize * textScaling);
                date102.setTypeface(futuristicTypeface, Typeface.BOLD);
                final LinearLayout divider2 = new LinearLayout(mContext);
                ViewGroup.MarginLayoutParams dividerParams2 = new ViewGroup.MarginLayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80 * textScaling, mContext.getResources().getDisplayMetrics()));
                dividerParams2.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                divider2.setLayoutParams(dividerParams1);
                GradientDrawable mDrawable2 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{
                                Color.parseColor("#FF89D8F6"),
                                Color.parseColor("#FF89D8F6")});
                divider2.setBackground(mDrawable2);

                final LinearLayout secondLineVertical = new LinearLayout(mContext);
                secondLineVertical.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                secondLineVertical.setOrientation(LinearLayout.VERTICAL);
                secondLineVertical.addView(date101);
                secondLineVertical.addView(date102);
                final LinearLayout secondLine = new LinearLayout(mContext);
                secondLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                secondLine.setOrientation(LinearLayout.HORIZONTAL);
                secondLine.addView(divider2);
                secondLine.addView(secondLineVertical);

                final TextView date103 = new TextView(mContext);
                date103.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date103.setText("Today is");
                date103.setTypeface(futuristicTypeface);
                date103.setTextSize(TypedValue.COMPLEX_UNIT_DIP, titleSize * textScaling);
                final TextClock date104 = new TextClock(mContext);
                ViewGroup.MarginLayoutParams dateParams104 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateParams104.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, mContext.getResources().getDisplayMetrics()), 0, 0);
                date104.setLayoutParams(timeParams101);
                date104.setFormat12Hour("EEEE");
                date104.setFormat24Hour("EEEE");
                date104.setAllCaps(true);
                date104.setTextSize(TypedValue.COMPLEX_UNIT_DIP, timeSize * textScaling);
                date104.setTypeface(futuristicTypeface, Typeface.BOLD);
                final LinearLayout divider3 = new LinearLayout(mContext);
                ViewGroup.MarginLayoutParams dividerParams3 = new ViewGroup.MarginLayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80 * textScaling, mContext.getResources().getDisplayMetrics()));
                dividerParams3.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                divider3.setLayoutParams(dividerParams1);
                GradientDrawable mDrawable3 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                        new int[]{
                                Color.parseColor("#FFF68989"),
                                Color.parseColor("#FFF68989")});
                divider3.setBackground(mDrawable3);

                final LinearLayout thirdLineVertical = new LinearLayout(mContext);
                thirdLineVertical.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                thirdLineVertical.setOrientation(LinearLayout.VERTICAL);
                thirdLineVertical.addView(date103);
                thirdLineVertical.addView(date104);
                final LinearLayout thirdLine = new LinearLayout(mContext);
                thirdLine.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                thirdLine.setOrientation(LinearLayout.HORIZONTAL);
                thirdLine.addView(divider3);
                thirdLine.addView(thirdLineVertical);

                final LinearLayout clockContainer1 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams1.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams1.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                clockContainer1.setLayoutParams(layoutParams1);
                clockContainer1.setGravity(Gravity.START);
                clockContainer1.setOrientation(LinearLayout.VERTICAL);
                clockContainer1.addView(firstLine);
                clockContainer1.addView(secondLine);
                clockContainer1.addView(thirdLine);
                container = clockContainer1;
            }
            case 3 -> {
                final TextClock date3 = new TextClock(mContext);
                date3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date3.setFormat12Hour("EEE, MMM dd");
                date3.setFormat24Hour("EEE, MMM dd");
                date3.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.material_dynamic_primary60, mContext.getTheme()));
                date3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 24 * textScaling);
                date3.setTypeface(typeface != null ? typeface : date3.getTypeface(), Typeface.BOLD);
                final TextClock clockHour3 = new TextClock(mContext);
                clockHour3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockHour3.setFormat12Hour("hh");
                clockHour3.setFormat24Hour("HH");
                clockHour3.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.material_dynamic_primary60, mContext.getTheme()));
                clockHour3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 160 * textScaling);
                clockHour3.setTypeface(typeface != null ? typeface : clockHour3.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams clockHourParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                clockHourParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -30 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                clockHour3.setLayoutParams(clockHourParams3);
                final TextClock clockMinute3 = new TextClock(mContext);
                clockMinute3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                clockMinute3.setFormat12Hour("mm");
                clockMinute3.setFormat24Hour("mm");
                clockMinute3.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.material_dynamic_primary60, mContext.getTheme()));
                clockMinute3.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 160 * textScaling);
                clockMinute3.setTypeface(typeface != null ? typeface : clockMinute3.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams clockMinuteParams3 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                clockMinuteParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -54 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                clockMinute3.setLayoutParams(clockMinuteParams3);
                final LinearLayout clockContainer3 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams3.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams3.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                clockContainer3.setLayoutParams(layoutParams3);
                clockContainer3.setGravity(Gravity.CENTER_HORIZONTAL);
                clockContainer3.setOrientation(LinearLayout.VERTICAL);
                clockContainer3.addView(date3);
                clockContainer3.addView(clockHour3);
                clockContainer3.addView(clockMinute3);
                container = clockContainer3;
            }
            case 4 -> {
                final AnalogClock analogClock4 = new AnalogClock(mContext);
                analogClock4.setLayoutParams(new LinearLayout.LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 180 * textScaling, mContext.getResources().getDisplayMetrics())));
                ((LinearLayout.LayoutParams) analogClock4.getLayoutParams()).gravity = Gravity.CENTER_HORIZONTAL;
                final TextClock day4 = new TextClock(mContext);
                day4.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                day4.setFormat12Hour("EEE dd MMM");
                day4.setFormat24Hour("EEE dd MMM");
                day4.setTextColor(mContext.getResources().getColor(android.R.color.system_neutral1_200, mContext.getTheme()));
                day4.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28 * textScaling);
                day4.setTypeface(typeface != null ? typeface : day4.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams dayParams4 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dayParams4.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                day4.setLayoutParams(dayParams4);
                final LinearLayout clockContainer4 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams4.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams4.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                clockContainer4.setLayoutParams(layoutParams4);
                clockContainer4.setGravity(Gravity.CENTER_HORIZONTAL);
                clockContainer4.setOrientation(LinearLayout.VERTICAL);
                clockContainer4.addView(analogClock4);
                clockContainer4.addView(day4);
                container = clockContainer4;
            }
            case 5 -> {
                final TextClock hour5 = new TextClock(mContext);
                hour5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                hour5.setFormat12Hour("hh");
                hour5.setFormat24Hour("HH");
                hour5.setTextColor(mContext.getResources().getColor(R.color.white, mContext.getTheme()));
                hour5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling);
                hour5.setTypeface(typeface != null ? typeface : hour5.getTypeface(), Typeface.BOLD);
                final TextClock minute5 = new TextClock(mContext);
                minute5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                minute5.setFormat12Hour("mm");
                minute5.setFormat24Hour("mm");
                minute5.setTextColor(mContext.getResources().getColor(R.color.white, mContext.getTheme()));
                minute5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling);
                minute5.setTypeface(typeface != null ? typeface : minute5.getTypeface(), Typeface.BOLD);
                ViewGroup.MarginLayoutParams minuteParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                minuteParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                minute5.setLayoutParams(minuteParams5);
                final LinearLayout time5 = new LinearLayout(mContext);
                LinearLayout.LayoutParams timeLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                timeLayoutParams5.gravity = Gravity.CENTER;
                time5.setLayoutParams(timeLayoutParams5);
                time5.setOrientation(LinearLayout.VERTICAL);
                time5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                GradientDrawable timeDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(R.color.black, mContext.getTheme()), mContext.getResources().getColor(R.color.black, mContext.getTheme())});
                timeDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                time5.setBackground(timeDrawable5);
                time5.setGravity(Gravity.CENTER);
                time5.addView(hour5);
                time5.addView(minute5);
                final TextClock day5 = new TextClock(mContext);
                day5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                day5.setFormat12Hour("EEE");
                day5.setFormat24Hour("EEE");
                day5.setAllCaps(true);
                day5.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.white, mContext.getTheme()));
                day5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28 * textScaling);
                day5.setTypeface(typeface != null ? typeface : day5.getTypeface(), Typeface.BOLD);
                day5.setLetterSpacing(0.2f);
                final TextClock date5 = new TextClock(mContext);
                date5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date5.setFormat12Hour("dd");
                date5.setFormat24Hour("dd");
                date5.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.white, mContext.getTheme()));
                date5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28 * textScaling);
                date5.setTypeface(typeface != null ? typeface : date5.getTypeface(), Typeface.BOLD);
                date5.setLetterSpacing(0.2f);
                ViewGroup.MarginLayoutParams dateParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                date5.setLayoutParams(dateParams5);
                final TextClock month5 = new TextClock(mContext);
                month5.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                month5.setFormat12Hour("MMM");
                month5.setFormat24Hour("MMM");
                month5.setAllCaps(true);
                month5.setTextColor(mContext.getResources().getColor(forceWhiteText ? R.color.white : R.color.white, mContext.getTheme()));
                month5.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 28 * textScaling);
                month5.setTypeface(typeface != null ? typeface : month5.getTypeface(), Typeface.BOLD);
                month5.setLetterSpacing(0.2f);
                ViewGroup.MarginLayoutParams monthParams5 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                monthParams5.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -2 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                month5.setLayoutParams(monthParams5);
                final LinearLayout right5 = new LinearLayout(mContext);
                LinearLayout.LayoutParams rightLayoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
                rightLayoutParams5.gravity = Gravity.CENTER;
                right5.setLayoutParams(rightLayoutParams5);
                right5.setOrientation(LinearLayout.VERTICAL);
                right5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, mContext.getResources().getDisplayMetrics()));
                right5.setGravity(Gravity.CENTER);
                right5.addView(day5);
                right5.addView(date5);
                right5.addView(month5);
                final LinearLayout container5 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams5.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams5.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                container5.setLayoutParams(layoutParams5);
                container5.setOrientation(LinearLayout.HORIZONTAL);
                container5.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12, mContext.getResources().getDisplayMetrics()));
                GradientDrawable mDrawable5 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{mContext.getResources().getColor(R.color.material_dynamic_primary60, mContext.getTheme()), mContext.getResources().getColor(R.color.material_dynamic_primary60, mContext.getTheme())});
                mDrawable5.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, mContext.getResources().getDisplayMetrics()));
                container5.setBackground(mDrawable5);
                container5.addView(time5);
                container5.addView(right5);
                container = container5;
            }
            case 6 -> {
                int margin6 = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14 * textScaling, mContext.getResources().getDisplayMetrics());
                final TextClock day6 = new TextClock(mContext);
                day6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                day6.setFormat12Hour("EEE");
                day6.setFormat24Hour("EEE");
                day6.setAllCaps(true);
                day6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                day6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42 * textScaling);
                day6.setTypeface(typeface != null ? typeface : day6.getTypeface(), Typeface.NORMAL);
                day6.setIncludeFontPadding(false);
                int maxLength6 = 2;
                InputFilter[] fArray6 = new InputFilter[1];
                fArray6[0] = new InputFilter.LengthFilter(maxLength6);
                day6.setFilters(fArray6);
                final TextView dayText6 = new TextView(mContext);
                dayText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                dayText6.setText("DAY");
                dayText6.setAllCaps(true);
                dayText6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                dayText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textScaling);
                dayText6.setTypeface(typeface != null ? typeface : dayText6.getTypeface(), Typeface.NORMAL);
                dayText6.setIncludeFontPadding(false);
                dayText6.setAlpha(0.4f);
                ViewGroup.MarginLayoutParams dayTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dayTextParams6.setMargins(0, 12 + topMargin, 0, 0);
                dayText6.setLayoutParams(dayTextParams6);
                final LinearLayout dayContainer6 = new LinearLayout(mContext);
                LinearLayout.LayoutParams dayLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dayLayoutParams6.gravity = Gravity.CENTER;
                dayLayoutParams6.setMargins(margin6, margin6, margin6, margin6);
                dayContainer6.setLayoutParams(dayLayoutParams6);
                dayContainer6.setGravity(Gravity.CENTER);
                dayContainer6.setOrientation(LinearLayout.VERTICAL);
                dayContainer6.addView(day6);
                dayContainer6.addView(dayText6);
                final TextClock hour6 = new TextClock(mContext);
                hour6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                hour6.setFormat12Hour("hh");
                hour6.setFormat24Hour("HH");
                hour6.setAllCaps(true);
                hour6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                hour6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42 * textScaling);
                hour6.setTypeface(typeface != null ? typeface : hour6.getTypeface(), Typeface.NORMAL);
                hour6.setIncludeFontPadding(false);
                final TextView hourText6 = new TextView(mContext);
                hourText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                hourText6.setText("HOURS");
                hourText6.setAllCaps(true);
                hourText6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                hourText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textScaling);
                hourText6.setTypeface(typeface != null ? typeface : hourText6.getTypeface(), Typeface.NORMAL);
                hourText6.setIncludeFontPadding(false);
                hourText6.setAlpha(0.4f);
                ViewGroup.MarginLayoutParams hourTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                hourTextParams6.setMargins(0, 12 + topMargin, 0, 0);
                hourText6.setLayoutParams(hourTextParams6);
                final LinearLayout hourContainer6 = new LinearLayout(mContext);
                LinearLayout.LayoutParams hourLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                hourLayoutParams6.gravity = Gravity.CENTER;
                hourLayoutParams6.setMargins(margin6, margin6, margin6, margin6);
                hourContainer6.setLayoutParams(hourLayoutParams6);
                hourContainer6.setGravity(Gravity.CENTER);
                hourContainer6.setOrientation(LinearLayout.VERTICAL);
                hourContainer6.addView(hour6);
                hourContainer6.addView(hourText6);
                final TextClock minute6 = new TextClock(mContext);
                minute6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                minute6.setFormat12Hour("mm");
                minute6.setFormat24Hour("mm");
                minute6.setAllCaps(true);
                minute6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                minute6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42 * textScaling);
                minute6.setTypeface(typeface != null ? typeface : minute6.getTypeface(), Typeface.NORMAL);
                minute6.setIncludeFontPadding(false);
                final TextView minuteText6 = new TextView(mContext);
                minuteText6.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                minuteText6.setText("MINUTES");
                minuteText6.setAllCaps(true);
                minuteText6.setTextColor(mContext.getResources().getColor(android.R.color.white, mContext.getTheme()));
                minuteText6.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16 * textScaling);
                minuteText6.setTypeface(typeface != null ? typeface : minuteText6.getTypeface(), Typeface.NORMAL);
                minuteText6.setIncludeFontPadding(false);
                minuteText6.setAlpha(0.4f);
                ViewGroup.MarginLayoutParams minuteTextParams6 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                minuteTextParams6.setMargins(0, 12 + topMargin, 0, 0);
                minuteText6.setLayoutParams(minuteTextParams6);
                final LinearLayout minuteContainer6 = new LinearLayout(mContext);
                LinearLayout.LayoutParams minuteLayoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                minuteLayoutParams6.gravity = Gravity.CENTER;
                minuteLayoutParams6.setMargins(margin6, margin6, margin6, margin6);
                minuteContainer6.setLayoutParams(minuteLayoutParams6);
                minuteContainer6.setGravity(Gravity.CENTER);
                minuteContainer6.setOrientation(LinearLayout.VERTICAL);
                minuteContainer6.addView(minute6);
                minuteContainer6.addView(minuteText6);
                final LinearLayout container6 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams6.gravity = Gravity.CENTER_HORIZONTAL;
                container6.setLayoutParams(layoutParams6);
                container6.setGravity(Gravity.CENTER);
                container6.setOrientation(LinearLayout.HORIZONTAL);
                container6.setPadding(margin6, margin6, margin6, margin6 + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                GradientDrawable mDrawable6 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{Color.parseColor("#090909"), Color.parseColor("#090909")});
                mDrawable6.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                container6.setBackground(mDrawable6);
                container6.addView(dayContainer6);
                container6.addView(hourContainer6);
                container6.addView(minuteContainer6);
                container = container6;
            }
            case 7 -> {
                final TextView time71 = new TextView(mContext);
                String timeFormat71 = "It's";
                time71.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                time71.setText(timeFormat71);
                time71.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : mContext.getResources().getColor(R.color.white, mContext.getTheme()));
                time71.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42 * textScaling);
                time71.setTypeface(typeface != null ? typeface : time71.getTypeface(), Typeface.NORMAL);
                time71.setIncludeFontPadding(false);
                ViewGroup.MarginLayoutParams timeLayoutParams71 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                timeLayoutParams71.setMargins(0, 12 + lineHeight, 0, 0);
                time71.setLayoutParams(timeLayoutParams71);
                final TextView time72 = new TextView(mContext);
                String timeFormat72 = "One";
                time72.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                time72.setText(timeFormat72);
                time72.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : mContext.getResources().getColor(R.color.white, mContext.getTheme()));
                time72.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42 * textScaling);
                time72.setTypeface(typeface != null ? typeface : time72.getTypeface(), Typeface.NORMAL);
                time72.setIncludeFontPadding(false);
                ViewGroup.MarginLayoutParams timeLayoutParams72 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                timeLayoutParams72.setMargins(0, 12 + lineHeight, 0, 0);
                time72.setLayoutParams(timeLayoutParams72);
                final TextView time73 = new TextView(mContext);
                String timeFormat73 = "Sixteen";
                time73.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                time73.setText(timeFormat73);
                time73.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : mContext.getResources().getColor(R.color.white, mContext.getTheme()));
                time73.setTextSize(TypedValue.COMPLEX_UNIT_SP, 42 * textScaling);
                time73.setTypeface(typeface != null ? typeface : time73.getTypeface(), Typeface.NORMAL);
                time73.setIncludeFontPadding(false);
                ViewGroup.MarginLayoutParams timeLayoutParams73 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                timeLayoutParams73.setMargins(0, 12 + lineHeight, 0, 0);
                time73.setLayoutParams(timeLayoutParams73);
                final TextClock date7 = new TextClock(mContext);
                date7.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                date7.setFormat12Hour("EEEE, MMM dd");
                date7.setFormat24Hour("EEEE, MMM dd");
                date7.setTextColor(forceWhiteText ? mContext.getResources().getColor(android.R.color.white, mContext.getTheme()) : mContext.getResources().getColor(R.color.white, mContext.getTheme()));
                date7.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22 * textScaling);
                date7.setTypeface(typeface != null ? typeface : date7.getTypeface(), Typeface.NORMAL);
                date7.setIncludeFontPadding(false);
                ViewGroup.MarginLayoutParams dateLayoutParams7 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateLayoutParams7.setMargins(0, 16 + lineHeight, 0, 0);
                date7.setLayoutParams(dateLayoutParams7);
                final LinearLayout container7 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams7 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams7.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams7.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                container7.setLayoutParams(layoutParams7);
                container7.setGravity(Gravity.START | Gravity.CENTER_HORIZONTAL);
                container7.setOrientation(LinearLayout.VERTICAL);
                container7.addView(time71);
                container7.addView(time72);
                container7.addView(time73);
                container7.addView(date7);
                container = container7;
            }
            case 8 -> {
                final TextClock day8 = new TextClock(mContext);
                ViewGroup.MarginLayoutParams dayParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dayParams8.setMargins(0, 0, 0, 0);
                day8.setLayoutParams(dayParams8);
                day8.setFormat12Hour("EEEE");
                day8.setFormat24Hour("EEEE");
                day8.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : R.color.white, mContext.getTheme()));
                day8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                day8.setTypeface(typeface != null ? typeface : day8.getTypeface(), Typeface.BOLD);
                day8.setIncludeFontPadding(false);
                final TextClock clock8 = new TextClock(mContext);
                ViewGroup.MarginLayoutParams clockParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                clockParams8.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, -4 + lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                clock8.setLayoutParams(clockParams8);
                clock8.setFormat12Hour("hh:mm");
                clock8.setFormat24Hour("HH:mm");
                clock8.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : R.color.white, mContext.getTheme()));
                clock8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 100 * textScaling);
                clock8.setTypeface(typeface != null ? typeface : clock8.getTypeface(), Typeface.BOLD);
                clock8.setIncludeFontPadding(false);
                final TextClock date8 = new TextClock(mContext);
                ViewGroup.MarginLayoutParams dateParams8 = new ViewGroup.MarginLayoutParams(ViewGroup.MarginLayoutParams.WRAP_CONTENT, ViewGroup.MarginLayoutParams.WRAP_CONTENT);
                dateParams8.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, lineHeight, mContext.getResources().getDisplayMetrics()), 0, 0);
                date8.setLayoutParams(dateParams8);
                date8.setFormat12Hour("MMMM dd");
                date8.setFormat24Hour("MMMM dd");
                date8.setTextColor(mContext.getResources().getColor(forceWhiteText ? android.R.color.white : R.color.white, mContext.getTheme()));
                date8.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28 * textScaling);
                date8.setTypeface(typeface != null ? typeface : date8.getTypeface(), Typeface.NORMAL);
                date8.setIncludeFontPadding(false);
                final LinearLayout container8 = new LinearLayout(mContext);
                LinearLayout.LayoutParams layoutParams8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams8.gravity = Gravity.CENTER_HORIZONTAL;
                layoutParams8.setMargins(0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, topMargin, mContext.getResources().getDisplayMetrics()), 0, (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, bottomMargin, mContext.getResources().getDisplayMetrics()));
                container8.setLayoutParams(layoutParams8);
                container8.setGravity(Gravity.CENTER);
                container8.setOrientation(LinearLayout.VERTICAL);
                container8.setPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 40 * textScaling, mContext.getResources().getDisplayMetrics()), (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24 * textScaling, mContext.getResources().getDisplayMetrics()));
                GradientDrawable mDrawable8 = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{forceWhiteText ? android.R.color.white : (customColorEnabled ? customColorCode : mContext.getResources().getColor(R.color.white, mContext.getTheme())), forceWhiteText ? android.R.color.white : (customColorEnabled ? customColorCode : mContext.getResources().getColor(R.color.white, mContext.getTheme()))});
                mDrawable8.setCornerRadius((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24 * textScaling, mContext.getResources().getDisplayMetrics()));
                mDrawable8.setAlpha(50);
                container8.setBackground(mDrawable8);
                container8.addView(day8);
                container8.addView(clock8);
                container8.addView(date8);
                container = container8;
            }
            case 9 -> {
                LinearLayout mainLayout = new LinearLayout(mContext);
                LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                mainParams.gravity = Gravity.CENTER_HORIZONTAL;
                mainParams.setMargins(
                        dpToPx(16, mContext),
                        0,
                        dpToPx(16, mContext),
                        dpToPx(5, mContext)
                );
                mainLayout.setLayoutParams(mainParams
                );
                mainLayout.setPadding(dpToPx(10, mContext), 0, dpToPx(10, mContext), dpToPx(10, mContext));
                mainLayout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout topClockLayout = new LinearLayout(mContext);
                topClockLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                topClockLayout.setOrientation(LinearLayout.HORIZONTAL);
                topClockLayout.setGravity(Gravity.CENTER);

                LinearLayout clock = new LinearLayout(mContext);
                LinearLayout.LayoutParams clockParams = new LinearLayout.LayoutParams(
                        0,
                        dpToPx(90, mContext)
                );
                clockParams.weight = 1;
                clockParams.setMarginEnd(dpToPx(5, mContext));
                clock.setLayoutParams(clockParams);

                LinearLayout clockContainer = new LinearLayout(mContext);
                LinearLayout.LayoutParams clockContainerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                clockContainerParams.leftMargin = dpToPx(65, mContext);
                clockContainer.setLayoutParams(clockContainerParams);
                clockContainer.setGravity(Gravity.CENTER | Gravity.END | Gravity.FILL_HORIZONTAL | Gravity.START);
                clockContainer.setOrientation(LinearLayout.VERTICAL);
                clockContainer.setPadding(
                        0,
                        dpToPx(10, mContext),
                        dpToPx(5, mContext),
                        dpToPx(10, mContext));

                TextClock clockAmPm = new TextClock(mContext);
                clockAmPm.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                clockAmPm.setFormat12Hour("a");
                clockAmPm.setFormat24Hour("");
                clockAmPm.setTextColor(Color.WHITE);
                clockAmPm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                clockAmPm.setTypeface(Typeface.DEFAULT_BOLD);
                clockAmPm.setSingleLine();
                clockAmPm.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                clockAmPm.setAlpha(0.7f);

                TextClock clockHourMin = new TextClock(mContext);
                clockHourMin.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                clockHourMin.setFormat12Hour("hh:mm");
                clockHourMin.setFormat24Hour("HH:mm");
                clockHourMin.setTextColor(Color.WHITE);
                clockHourMin.setTextSize(TypedValue.COMPLEX_UNIT_SP, 25);
                clockHourMin.setTypeface(Typeface.DEFAULT_BOLD);
                clockHourMin.setSingleLine();
                clockHourMin.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);


                clockContainer.addView(clockAmPm);
                clockContainer.addView(clockHourMin);
                clock.addView(clockContainer);


                LinearLayout calendar = new LinearLayout(mContext);
                LinearLayout.LayoutParams calendarParams = new LinearLayout.LayoutParams(
                        dpToPx(0, mContext),
                        dpToPx(90, mContext)
                );
                calendarParams.weight = 1;
                calendarParams.setMarginStart(dpToPx(5, mContext));
                calendar.setLayoutParams(calendarParams);

                LinearLayout calendarContainer = new LinearLayout(mContext);
                LinearLayout.LayoutParams calendarContainerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                calendarContainerParams.leftMargin = dpToPx(80, mContext);
                calendarContainer.setLayoutParams(calendarContainerParams);
                calendarContainer.setGravity(Gravity.CENTER | Gravity.END | Gravity.FILL_HORIZONTAL | Gravity.START);
                calendarContainer.setOrientation(LinearLayout.VERTICAL);
                calendarContainer.setPadding(
                        0,
                        dpToPx(10, mContext),
                        dpToPx(5, mContext),
                        dpToPx(10, mContext));

                TextClock calendarDay = new TextClock(mContext);
                calendarDay.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                calendarDay.setFormat12Hour("EEEE");
                calendarDay.setFormat24Hour("EEEE");
                calendarDay.setTextColor(Color.WHITE);
                calendarDay.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                calendarDay.setTypeface(Typeface.DEFAULT_BOLD);
                calendarDay.setSingleLine();
                calendarDay.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
                calendarDay.setAlpha(0.7f);

                TextClock calendarDate = new TextClock(mContext);
                calendarDate.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                calendarDate.setFormat12Hour("dd");
                calendarDate.setFormat24Hour("dd");
                calendarDate.setTextColor(Color.WHITE);
                calendarDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);
                calendarDate.setTypeface(Typeface.DEFAULT_BOLD);
                calendarDate.setSingleLine();
                calendarDate.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

                TextClock calendarMonth = new TextClock(mContext);
                calendarMonth.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                calendarMonth.setFormat12Hour("MMM");
                calendarMonth.setFormat24Hour("MMM");
                calendarMonth.setTextColor(Color.WHITE);
                calendarMonth.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                calendarMonth.setTypeface(Typeface.DEFAULT_BOLD);
                calendarMonth.setSingleLine();
                calendarMonth.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);

                calendarContainer.addView(calendarDay);
                calendarContainer.addView(calendarDate);
                calendarContainer.addView(calendarMonth);

                calendar.addView(calendarContainer);

                topClockLayout.addView(clock);
                topClockLayout.addView(calendar);

                LinearLayout bottomClockLayout = new LinearLayout(mContext);
                bottomClockLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                bottomClockLayout.setOrientation(LinearLayout.HORIZONTAL);

                LinearLayout progressBars = new LinearLayout(mContext);
                LinearLayout.LayoutParams progressBarsParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                progressBarsParams.setMargins(
                        0,
                        dpToPx(10, mContext),
                        0,
                        0
                );
                progressBars.setLayoutParams(progressBarsParams);
                progressBars.setOrientation(LinearLayout.VERTICAL);
                progressBars.setGravity(Gravity.CENTER);

                LinearLayout progress1Container = new LinearLayout(mContext);
                LinearLayout.LayoutParams progress1ContainerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                progress1ContainerParams.bottomMargin = dpToPx(5, mContext);
                progress1Container.setLayoutParams(progress1ContainerParams);
                progress1Container.setOrientation(LinearLayout.HORIZONTAL);
                progress1Container.setGravity(Gravity.CENTER_VERTICAL);

                FrameLayout progress1Frame = new FrameLayout(mContext);
                LinearLayout.LayoutParams progress1FrameParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(40, mContext)
                );
                progress1Frame.setLayoutParams(progress1FrameParams);
                ProgressBar progress1 = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
                FrameLayout.LayoutParams progress1Params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(40, mContext)
                );
                progress1.setTag(MEDIA_PROGRESSBAR);
                progress1.setLayoutParams(progress1Params);
                progress1.setIndeterminate(false);
                progress1.setIndeterminateDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.widget_progress_track, mContext.getTheme()));
                progress1.setProgressDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.widget_progress_track, mContext.getTheme()));
                progress1.setProgressTintList(ColorStateList.valueOf(ThemeUtils.getPrimaryColor(mContext)));
                progress1.setProgress(50);

                RelativeLayout progress1TextContainer = new RelativeLayout(mContext);
                RelativeLayout.LayoutParams progress1TextContainerParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                progress1TextContainer.setLayoutParams(progress1TextContainerParams);
                progress1TextContainer.setGravity(Gravity.CENTER_VERTICAL);
                progress1TextContainer.setPadding(
                        dpToPx(10, mContext),
                        0,
                        dpToPx(10, mContext),
                        0
                );

                TextView progress1Title = new TextView(mContext);
                RelativeLayout.LayoutParams progress1TitleParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                progress1TitleParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                progress1Title.setLayoutParams(progress1TitleParams);
                progress1Title.setGravity(Gravity.START);
                progress1Title.setText("Media volume");
                progress1Title.setEllipsize(TextUtils.TruncateAt.END);
                progress1Title.setTextColor(Color.WHITE);
                progress1Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                progress1Title.setTypeface(Typeface.DEFAULT_BOLD);
                progress1Title.setSingleLine();
                progress1Title.setCompoundDrawablePadding(dpToPx(3, mContext));
                progress1Title.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_widget_media,
                        0,
                        0,
                        0
                );
                TextViewCompat.setCompoundDrawableTintList(progress1Title, ColorStateList.valueOf(Color.WHITE));

                TextView progress1Value = new TextView(mContext);
                RelativeLayout.LayoutParams progress1ValueParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                progress1ValueParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                progress1Value.setLayoutParams(progress1ValueParams);
                progress1Value.setTag(MEDIA_PROGRESSBAR_VALUE);
                progress1Value.setGravity(Gravity.END);
                progress1Value.setText("50%");
                progress1Value.setTextColor(Color.WHITE);
                progress1Value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                progress1Value.setTypeface(Typeface.DEFAULT_BOLD);
                progress1Value.setSingleLine();
                progress1Value.setEllipsize(TextUtils.TruncateAt.END);

                progress1TextContainer.addView(progress1Title);
                progress1TextContainer.addView(progress1Value);

                progress1Frame.addView(progress1);
                progress1Frame.addView(progress1TextContainer);

                progress1Container.addView(progress1Frame);

                LinearLayout progress2Container = new LinearLayout(mContext);
                LinearLayout.LayoutParams progress2ContainerParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                progress2ContainerParams.bottomMargin = dpToPx(5, mContext);
                progress2Container.setLayoutParams(progress2ContainerParams);
                progress2Container.setOrientation(LinearLayout.HORIZONTAL);
                progress2Container.setGravity(Gravity.CENTER_VERTICAL);

                FrameLayout progress2Frame = new FrameLayout(mContext);
                LinearLayout.LayoutParams progress2FrameParams = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(40, mContext)
                );
                progress2Frame.setLayoutParams(progress2FrameParams);
                ProgressBar progress2 = new ProgressBar(mContext, null, android.R.attr.progressBarStyleHorizontal);
                FrameLayout.LayoutParams progress2Params = new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        dpToPx(40, mContext)
                );
                progress2.setTag(BATTERY_PROGRESSBAR);
                progress2.setLayoutParams(progress2Params);
                progress2.setIndeterminate(false);
                progress2.setIndeterminateDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.widget_progress_track, mContext.getTheme()));
                progress2.setProgressDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.widget_progress_track, mContext.getTheme()));
                progress2.setProgressTintList(ColorStateList.valueOf(ThemeUtils.getPrimaryColor(mContext)));
                progress2.setProgress(50);

                RelativeLayout progress2TextContainer = new RelativeLayout(mContext);
                RelativeLayout.LayoutParams progress2TextContainerParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                );
                progress2TextContainer.setLayoutParams(progress2TextContainerParams);
                progress2TextContainer.setGravity(Gravity.CENTER_VERTICAL);
                progress2TextContainer.setPadding(
                        dpToPx(10, mContext),
                        0,
                        dpToPx(10, mContext),
                        0
                );

                TextView progress2Title = new TextView(mContext);
                RelativeLayout.LayoutParams progress2TitleParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                progress2TitleParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                progress2Title.setLayoutParams(progress2TitleParams);
                progress2Title.setGravity(Gravity.START);
                progress2Title.setText("Battery Status");
                progress2Title.setEllipsize(TextUtils.TruncateAt.END);
                progress2Title.setTextColor(Color.WHITE);
                progress2Title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                progress2Title.setTypeface(Typeface.DEFAULT_BOLD);
                progress2Title.setSingleLine();
                progress2Title.setCompoundDrawablePadding(dpToPx(3, mContext));
                progress2Title.setCompoundDrawablesWithIntrinsicBounds(
                        R.drawable.ic_widget_power,
                        0,
                        0,
                        0
                );
                TextViewCompat.setCompoundDrawableTintList(progress2Title, ColorStateList.valueOf(Color.WHITE));

                TextView progress2Value = new TextView(mContext);
                RelativeLayout.LayoutParams progress2ValueParams = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                progress2ValueParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                progress2Value.setLayoutParams(progress2ValueParams);
                progress2Value.setTag(BATTERY_PROGRESSBAR_VALUE);
                progress2Value.setGravity(Gravity.END);
                progress2Value.setText("50%");
                progress2Value.setTextColor(Color.WHITE);
                progress2Value.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                progress2Value.setTypeface(Typeface.DEFAULT_BOLD);
                progress2Value.setSingleLine();
                progress2Value.setEllipsize(TextUtils.TruncateAt.END);

                progress2TextContainer.addView(progress2Title);
                progress2TextContainer.addView(progress2Value);

                progress2Frame.addView(progress2);
                progress2Frame.addView(progress2TextContainer);

                progress2Container.addView(progress2Frame);

                progressBars.addView(progress1Container);
                progressBars.addView(progress2Container);

                bottomClockLayout.addView(progressBars);

                mainLayout.addView(topClockLayout);
                mainLayout.addView(bottomClockLayout);

                // Aggiunge il layout interno al layout principale
                container = mainLayout;
            }
            case 10, 11 -> {
                LinearLayout mainLayout = new LinearLayout(mContext);
                LinearLayout.LayoutParams mainParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                mainParams.gravity = Gravity.CENTER_HORIZONTAL;


                RelativeLayout mainLayout2 = new RelativeLayout(mContext);
                RelativeLayout.LayoutParams mainParams2 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                mainLayout2.setLayoutParams(mainParams2);

                LottieAnimationView animationView = new LottieAnimationView(mContext);
                RelativeLayout.LayoutParams animationParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        dpToPx(300, mContext)
                );
                animationParams.addRule(RelativeLayout.CENTER_IN_PARENT);
                animationView.setLayoutParams(animationParams);
                @RawRes int rawRes = style == 10 ? R.raw.loop : R.raw.dual_layer;
                animationView.setAnimation(rawRes);
                animationView.setRepeatCount(LottieDrawable.INFINITE);
                animationView.playAnimation();

                LinearLayout clockLayout = new LinearLayout(mContext);
                RelativeLayout.LayoutParams clockParams = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                int topMargin = style == 10 ? 65 : 75;
                clockParams.setMargins(
                        0,
                        dpToPx(topMargin, mContext),
                        0,
                        0
                );
                clockLayout.setLayoutParams(clockParams);
                clockLayout.setOrientation(LinearLayout.HORIZONTAL);
                clockLayout.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP);

                TextClock hour = new TextClock(mContext);
                LinearLayout.LayoutParams hourParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                hourParams.weight = 1;
                hour.setLayoutParams(hourParams);
                hour.setFormat12Hour("hh");
                hour.setFormat24Hour("kk");
                hour.setTextColor(Color.WHITE);
                hour.setTextSize(TypedValue.COMPLEX_UNIT_SP, 108);
                hour.setTypeface(Typeface.DEFAULT_BOLD);
                hour.setPadding(
                        0,
                        0,
                        dpToPx(4, mContext),
                        0
                );

                TextClock minute = new TextClock(mContext);
                LinearLayout.LayoutParams minuteParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                minuteParams.weight = 1;
                minute.setLayoutParams(minuteParams);
                minute.setFormat12Hour("mm");
                minute.setFormat24Hour("mm");
                minute.setTextColor(Color.WHITE);
                minute.setTextSize(TypedValue.COMPLEX_UNIT_SP, 56);
                minute.setTypeface(Typeface.DEFAULT_BOLD);
                minute.setPadding(
                        dpToPx(4, mContext),
                        0,
                        0,
                        0
                );

                clockLayout.addView(hour);
                clockLayout.addView(minute);

                mainLayout2.addView(animationView);
                mainLayout2.addView(clockLayout);

                mainLayout.addView(mainLayout2);

                container = mainLayout;


            }
            /*case 10 -> {
                RelativeLayout relativeLayout_817 = new RelativeLayout(this);
                relativeLayout_817.setGravity(start|center);
                LayoutParams layout_712 = new LayoutParams();
                layout_712.width = LayoutParams.MATCH_PARENT;
                layout_712.height = LayoutParams.WRAP_CONTENT;
                layout_712.gravity = start|center;
                relativeLayout_817.setLayoutParams(layout_712);

                LinearLayout timeSection = new LinearLayout(this);
                timeSection.setId(R.id.timeSection);
                timeSection.setOrientation(VERTICAL);
                LayoutParams layout_855 = new LayoutParams();
                layout_855.width = LayoutParams.WRAP_CONTENT;
                layout_855.height = LayoutParams.WRAP_CONTENT;
                layout_855.setMarginStart20dp;
                layout_855.topMargin = 40dp;
                timeSection.setLayoutParams(layout_855);






                relativeLayout_817.addView(timeSection);

                LinearLayout timeTextSection = new LinearLayout(this);
                timeTextSection.setId(R.id.timeTextSection);
                timeTextSection.setOrientation(VERTICAL);
                LayoutParams layout_538 = new LayoutParams();
                layout_538.width = LayoutParams.WRAP_CONTENT;
                layout_538.height = LayoutParams.WRAP_CONTENT;
                layout_538.setMarginStart30dp;
                layout_538.topMargin = 40dp;
                timeTextSection.setLayoutParams(layout_538);

                TextView timeLabel = new TextView(this);
                timeLabel.setId(R.id.timeLabel);
                timeLabel.setText("Time is");
                timeLabel.setTextSize((12/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
                timeLabel.setTextColor(Color.parseColor("#FFFFFF"));
                timeLabel.setTypeface(@font/Future);
                LayoutParams layout_362 = new LayoutParams();
                layout_362.width = LayoutParams.WRAP_CONTENT;
                layout_362.height = LayoutParams.WRAP_CONTENT;
                layout_362.setMarginStart5dp;
                layout_362.bottomMargin = -20dp;
                timeLabel.setLayoutParams(layout_362);
                timeTextSection.addView(timeLabel);

                TextClock timeValue = new TextClock(this);
                timeValue.setId(R.id.timeValue);
                timeValue.setText("$df(h:mm a)$");
                timeValue.setTextSize((25/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
                timeValue.setTextColor(Color.parseColor("#FFFFFF"));
                timeValue.setTypeface(@font/Futurist-Fixed-width-Bold);
                LayoutParams layout_744 = new LayoutParams();
                layout_744.width = LayoutParams.WRAP_CONTENT;
                layout_744.height = LayoutParams.WRAP_CONTENT;
                layout_744.bottomMargin = 20dp;
                layout_744.topMargin = 30dp;
                timeValue.setLayoutParams(layout_744);
                timeTextSection.addView(timeValue);
                relativeLayout_817.addView(timeTextSection);

                LinearLayout calendarSection = new LinearLayout(this);
                calendarSection.setId(R.id.calendarSection);
                calendarSection.setOrientation(VERTICAL);
                LayoutParams layout_600 = new LayoutParams();
                layout_600.width = LayoutParams.WRAP_CONTENT;
                layout_600.height = LayoutParams.WRAP_CONTENT;
                layout_600.setMarginStart30dp;
                layout_600.topMargin = 110dp;
                calendarSection.setLayoutParams(layout_600);

                TextView calendarLabel = new TextView(this);
                calendarLabel.setId(R.id.calendarLabel);
                calendarLabel.setText("Calendar");
                calendarLabel.setTextSize((12/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
                calendarLabel.setTextColor(Color.parseColor("#FFFFFF"));
                calendarLabel.setTypeface(@font/Future);
                LayoutParams layout_74 = new LayoutParams();
                layout_74.width = LayoutParams.WRAP_CONTENT;
                layout_74.height = LayoutParams.WRAP_CONTENT;
                layout_74.setMarginStart5dp;
                layout_74.bottomMargin = -20dp;
                calendarLabel.setLayoutParams(layout_74);
                calendarSection.addView(calendarLabel);

                TextClock calendarValue = new TextClock(this);
                calendarValue.setId(R.id.calendarValue);
                calendarValue.setFormat12Hour("MMMM dd");
                calendarValue.setFormat24Hour("MMMM dd");
                calendarValue.setTextSize((25/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
                calendarValue.setAllCaps(TRUE);
                calendarValue.setTextColor(Color.parseColor("#FFFFFF"));
                calendarValue.setTypeface(@font/Futurist-Fixed-width-Bold);
                LayoutParams layout_933 = new LayoutParams();
                layout_933.width = LayoutParams.WRAP_CONTENT;
                layout_933.height = LayoutParams.WRAP_CONTENT;
                layout_933.bottomMargin = 20dp;
                layout_933.topMargin = 30dp;
                calendarValue.setLayoutParams(layout_933);
                calendarSection.addView(calendarValue);
                relativeLayout_817.addView(calendarSection);

                LinearLayout todaySection = new LinearLayout(this);
                todaySection.setId(R.id.todaySection);
                todaySection.setOrientation(VERTICAL);
                LayoutParams layout_841 = new LayoutParams();
                layout_841.width = LayoutParams.WRAP_CONTENT;
                layout_841.height = LayoutParams.WRAP_CONTENT;
                layout_841.setMarginStart30dp;
                layout_841.topMargin = 180dp;
                todaySection.setLayoutParams(layout_841);

                TextView todayLabel = new TextView(this);
                todayLabel.setId(R.id.todayLabel);
                todayLabel.setText("Today is");
                todayLabel.setTextSize((12/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
                todayLabel.setTextColor(Color.parseColor("#FFFFFF"));
                todayLabel.setTypeface(@font/Future);
                LayoutParams layout_791 = new LayoutParams();
                layout_791.width = LayoutParams.WRAP_CONTENT;
                layout_791.height = LayoutParams.WRAP_CONTENT;
                layout_791.setMarginStart5dp;
                layout_791.bottomMargin = -20dp;
                todayLabel.setLayoutParams(layout_791);
                todaySection.addView(todayLabel);

                TextClock todayValue = new TextClock(this);
                todayValue.setId(R.id.todayValue);
                todayValue.setFormat12Hour("EEEE");
                todayValue.setFormat24Hour("EEEE");
                todayValue.setTextSize((25/getApplicationContext().getResources().getDisplayMetrics().scaledDensity));
                todayValue.setTextColor(Color.parseColor("#FFFFFF"));
                todayValue.setAllCaps(TRUE);
                todayValue.setTypeface(@font/Futurist-Fixed-width-Bold);
                LayoutParams layout_65 = new LayoutParams();
                layout_65.width = LayoutParams.WRAP_CONTENT;
                layout_65.height = LayoutParams.WRAP_CONTENT;
                layout_65.bottomMargin = 20dp;
                layout_65.topMargin = 30dp;
                todayValue.setLayoutParams(layout_65);
                todaySection.addView(todayValue);
                relativeLayout_817.addView(todaySection);
            }*/
        }

        return container;
    }

    private static TextClock createTextClock(Context context, int textSize, int textColor) {
        TextClock textClock = new TextClock(context);
        textClock.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        textClock.setTextSize(textSize);
        textClock.setTextColor(textColor);
        return textClock;
    }
    private static LinearLayout createLinearLayout(Context context, int marginStart, int marginTop) {
        LinearLayout linearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(marginStart, context), dpToPx(marginTop, context), 0, 0);
        linearLayout.setLayoutParams(layoutParams);
        return linearLayout;
    }

    private static View createColorView(Context context, int widthDp, int heightDp, int color) {
        View view = new View(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                dpToPx(widthDp, context),
                dpToPx(heightDp, context)
        );
        layoutParams.setMargins(0, 0, 0, dpToPx(10, context));
        view.setLayoutParams(layoutParams);
        view.setBackgroundColor(color);
        return view;
    }

    private static TextView createTextView(Context context, String text, int textSizeSp, int marginBottomDp) {
        TextView textView = new TextView(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(5, context), 0, 0, dpToPx(marginBottomDp, context));
        textView.setLayoutParams(layoutParams);
        textView.setText(text);
        textView.setTextSize(textSizeSp);
        textView.setTextColor(Color.WHITE);
        //textView.setTypeface(context.getResources().getFont(R.font.future));
        return textView;
    }

    private static TextClock createTextClock(Context context, String format, int textSizeSp, int marginTopDp, int marginBottomDp) {
        TextClock textClock = new TextClock(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(dpToPx(5, context), dpToPx(marginTopDp, context), 0, dpToPx(marginBottomDp, context));
        textClock.setLayoutParams(layoutParams);
        textClock.setFormat12Hour(format);
        textClock.setFormat24Hour(format);
        textClock.setTextSize(textSizeSp);
        textClock.setTextColor(Color.WHITE);
        //textClock.setTypeface(context.getResources().getFont(R.font.futurist_fixed_width_bold));
        return textClock;
    }

    private static int dpToPx(int dp, Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
