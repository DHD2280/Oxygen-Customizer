package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.getChip;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.StringFormatter;
import it.dhd.oxygencustomizer.xposed.XposedMods;

/**
 * @noinspection RedundantThrows
 */
public class StatusbarClock extends XposedMods {

    public static final int POSITION_LEFT = 2;
    public static final int POSITION_CENTER = 1;
    public static final int POSITION_RIGHT = 0;
    private static final String listenPackage = SYSTEM_UI;
    private static final int AM_PM_STYLE_SMALL = 1;
    private static final int AM_PM_STYLE_GONE = 2;
    private static final int CLOCK_DATE_DISPLAY_GONE = 0;
    private static final int CLOCK_DATE_DISPLAY_SMALL = 1;
    private static final int CLOCK_DATE_STYLE_REGULAR = 0;
    private static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    private static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int HIDE_DURATION = 60; // 1 minute
    private static final int SHOW_DURATION = 5; // 5 seconds
    private static final int STYLE_DATE_LEFT = 0;
    private final String TAG = "Oxygen Customizer - Statusbar Clock: ";
    private final Handler autoHideHandler = new Handler(Looper.getMainLooper());
    private final StringFormatter stringFormatter = new StringFormatter();
    private boolean mClockAutoHideLauncher = false;
    private boolean mScreenOn = true;
    private boolean mClockAutoHide;
    private boolean mClockAutoHideLauncherSwitch;
    private int mHideDuration = HIDE_DURATION, mShowDuration = SHOW_DURATION;
    private int mAmPmStyle;
    private boolean mShowSeconds;
    private int mClockDateDisplay = CLOCK_DATE_DISPLAY_GONE;
    private int mClockDateStyle = CLOCK_DATE_STYLE_REGULAR;
    private int mClockDatePosition = STYLE_DATE_LEFT;
    private String mClockDateFormat = "$GEEE";
    private String mCustomClockDateFormat = "$GEEE";
    private String mCustomBeforeClock = "", mCustomAfterClock = "";
    private boolean mCustomBeforeSmall = false, mCustomAfterSmall = false;
    private Object Clock = null;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Clock == null) return;
            Handler handler = (Handler) callMethod(Clock, "getHandler");
            if (handler == null) return;
            String action = intent.getAction();
            if (Intent.ACTION_SCREEN_OFF.equals(action)) {
                mScreenOn = false;
            } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
                mScreenOn = true;
            }
            if (mScreenOn) {
                if (mClockAutoHide) autoHideHandler.post(() -> updateClockVisibility(Clock));
            }
        }
    };
    private TextView mClockView;
    private Object mCollapsedStatusBarFragment = null;
    private ViewGroup mStatusbarStartSide = null;
    private View mCenteredIconArea = null;
    private LinearLayout mSystemIconArea = null;
    private int mClockPosition = POSITION_LEFT;
    private int leftClockPadding = 0, rightClockPadding = 0;
    private boolean mClockCustomColor;
    private int mClockColor = Color.WHITE;
    // Clock Chip
    private boolean clockChip;
    private int chipStyle;
    private boolean chipUseAccent, chipUseGradient;
    private int chipGradientOrientation, chipGradient1, chipGradient2;
    private boolean chipAccentStroke;
    private int chipStrokeWidth, chipStrokeColor;
    private boolean chipRoundCorners;
    private int chipTopSxRound, chipTopDxRound, chipBottomSxRound, chipBottomDxRound;
    private int chipMarginSx, chipMarginDx, chipMarginTop, chipMarginBottom;
    private int chipPaddingSx, chipPaddingDx, chipPaddingTop, chipPaddingBottom;
    private LayerDrawable mClockChipDrawable;
    private int mClockSize = 12;

    @SuppressLint("DiscouragedApi")
    public StatusbarClock(Context context) {
        super(context);

        if (!listensTo(context.getPackageName())) return;

        rightClockPadding = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_clock_starting_padding", "dimen", mContext.getPackageName()));
        leftClockPadding = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_left_clock_starting_padding", "dimen", mContext.getPackageName()));
    }

    @Override
    public void updatePrefs(String... Key) {

        mClockAutoHide = Xprefs.getBoolean("status_bar_clock_auto_hide", false);
        mClockAutoHideLauncherSwitch = Xprefs.getBoolean("status_bar_clock_auto_hide_launcher", false);
        mHideDuration = Xprefs.getSliderInt("status_bar_clock_auto_hide_hduration", HIDE_DURATION);
        mShowDuration = Xprefs.getSliderInt("status_bar_clock_auto_hide_sduration", SHOW_DURATION);
        mAmPmStyle = Integer.parseInt(Xprefs.getString("status_bar_am_pm", String.valueOf(AM_PM_STYLE_GONE)));
        mShowSeconds = Xprefs.getBoolean("status_bar_clock_seconds", false);
        mClockDateDisplay = Integer.parseInt(Xprefs.getString("status_bar_clock_date_display", String.valueOf(CLOCK_DATE_DISPLAY_GONE)));
        mClockDatePosition = Integer.parseInt(Xprefs.getString("status_bar_clock_date_position", String.valueOf(STYLE_DATE_LEFT)));
        mClockDateStyle = Integer.parseInt(Xprefs.getString("status_bar_clock_date_style", String.valueOf(CLOCK_DATE_STYLE_REGULAR)));
        mClockDateFormat = Xprefs.getString("status_bar_clock_date_format", "$GEEE");
        mCustomClockDateFormat = Xprefs.getString("status_bar_custom_clock_format", "$GEEE");
        mCustomBeforeClock = Xprefs.getString("sbc_before_clock_format", "");
        mCustomAfterClock = Xprefs.getString("sbc_after_clock_format", "");
        mCustomBeforeSmall = Xprefs.getBoolean("sbc_before_small", false);
        mCustomAfterSmall = Xprefs.getBoolean("sbc_after_small", false);
        mClockPosition = Integer.parseInt(Xprefs.getString("status_bar_clock", String.valueOf(POSITION_LEFT)));
        mClockCustomColor = Xprefs.getBoolean("status_bar_custom_clock_color", false);
        mClockColor = Xprefs.getInt("status_bar_clock_color", Color.WHITE);
        mClockSize = Xprefs.getSliderInt("status_bar_clock_size", 12);

        // gradients prefs
        clockChip = Xprefs.getBoolean("status_bar_clock_background_chip_switch", false);
        chipStyle = Xprefs.getInt("status_bar_clock_background_chip" + "_STYLE", 0);
        chipGradientOrientation = Xprefs.getInt("status_bar_clock_background_chip" + "_GRADIENT_ORIENTATION", 0);
        chipUseAccent = Xprefs.getBoolean("status_bar_clock_background_chip" + "_USE_ACCENT_COLOR", true);
        chipUseGradient = Xprefs.getBoolean("status_bar_clock_background_chip" + "_USE_GRADIENT", false);
        chipGradient1 = Xprefs.getInt("status_bar_clock_background_chip" + "_GRADIENT_1", getPrimaryColor(mContext));
        chipGradient2 = Xprefs.getInt("status_bar_clock_background_chip" + "_GRADIENT_2", getPrimaryColor(mContext));
        chipAccentStroke = Xprefs.getBoolean("status_bar_clock_background_chip" + "_USE_ACCENT_COLOR_STROKE", false);
        chipStrokeColor = Xprefs.getInt("status_bar_clock_background_chip" + "_STROKE_COLOR", getPrimaryColor(mContext));
        chipStrokeWidth = Xprefs.getInt("status_bar_clock_background_chip" + "_STROKE_WIDTH", 10);
        chipRoundCorners = Xprefs.getBoolean("status_bar_clock_background_chip" + "_ROUNDED_CORNERS", false);
        chipTopSxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_TOP_LEFT_RADIUS", 28);
        chipTopDxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_TOP_RIGHT_RADIUS", 28);
        chipBottomSxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_BOTTOM_LEFT_RADIUS", 28);
        chipBottomDxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_BOTTOM_RIGHT_RADIUS", 28);
        chipMarginSx = Xprefs.getInt("status_bar_clock_background_chip" + "_MARGIN_LEFT", 0);
        chipMarginDx = Xprefs.getInt("status_bar_clock_background_chip" + "_MARGIN_RIGHT", 0);
        chipMarginTop = Xprefs.getInt("status_bar_clock_background_chip" + "_MARGIN_TOP", 0);
        chipMarginBottom = Xprefs.getInt("status_bar_clock_background_chip" + "_MARGIN_BOTTOM", 0);
        chipPaddingSx = Xprefs.getInt("status_bar_clock_background_chip" + "_PADDING_LEFT", 0);
        chipPaddingDx = Xprefs.getInt("status_bar_clock_background_chip" + "_PADDING_RIGHT", 0);
        chipPaddingTop = Xprefs.getInt("status_bar_clock_background_chip" + "_PADDING_TOP", 0);
        chipPaddingBottom = Xprefs.getInt("status_bar_clock_background_chip" + "_PADDING_BOTTOM", 0);

        String dateFormat;
        if (mClockDateFormat.equals("custom")) {
            dateFormat = mCustomClockDateFormat;
        } else {
            dateFormat = mClockDateFormat;
        }
        if ((mCustomBeforeClock + mCustomAfterClock).trim().isEmpty()) {
            if (mClockDateDisplay != CLOCK_DATE_DISPLAY_GONE) {
                if (mClockDatePosition == STYLE_DATE_LEFT) {
                    mCustomBeforeClock = dateFormat + " ";
                    mCustomBeforeSmall = mClockDateDisplay == CLOCK_DATE_DISPLAY_SMALL;
                    mCustomAfterClock = "";
                    mCustomAfterSmall = false;
                } else {
                    mCustomAfterClock = " " + dateFormat;
                    mCustomAfterSmall = mClockDateDisplay == CLOCK_DATE_DISPLAY_SMALL;
                    mCustomBeforeClock = "";
                    mCustomBeforeSmall = false;
                }
            }
        } else {
            if (!TextUtils.isEmpty(mCustomBeforeClock)) {
                mCustomBeforeClock = mCustomBeforeClock + " ";
            }
            if (!TextUtils.isEmpty(mCustomAfterClock)) {
                mCustomAfterClock = " " + mCustomAfterClock;
            }
            mClockDateStyle = CLOCK_DATE_STYLE_REGULAR;
        }

        if (Key.length > 0) {
            switch (Key[0]) {
                case "status_bar_clock_auto_hide" -> updateClockVisibility();
                case "status_bar_am_pm",
                     "status_bar_clock_date_display",
                     "status_bar_clock_date_position",
                     "status_bar_clock_date_style",
                     "status_bar_clock_date_format",
                     "status_bar_custom_clock_format",
                     "sbc_before_clock_format", "sbc_before_small", "sbc_after_clock_format",
                     "sbc_after_small" -> updateClock();
                case "status_bar_clock_size" -> setClockSize();
                case "status_bar_clock",
                     "status_bar_clock_seconds",
                     "status_bar_custom_clock_color",
                     "status_bar_clock_color" -> {
                    placeClock();
                    updateClock();
                }
                case "status_bar_clock_background_chip_switch",
                     "status_bar_clock_background_chip" + "_STYLE",
                     "status_bar_clock_background_chip" + "_GRADIENT_ORIENTATION",
                     "status_bar_clock_background_chip" + "_USE_ACCENT_COLOR",
                     "status_bar_clock_background_chip" + "_USE_GRADIENT",
                     "status_bar_clock_background_chip" + "_GRADIENT_1",
                     "status_bar_clock_background_chip" + "_GRADIENT_2",
                     "status_bar_clock_background_chip" + "_USE_ACCENT_COLOR_STROKE",
                     "status_bar_clock_background_chip" + "_STROKE_COLOR",
                     "status_bar_clock_background_chip" + "_STROKE_WIDTH",
                     "status_bar_clock_background_chip" + "_ROUNDED_CORNERS",
                     "status_bar_clock_background_chip" + "_TOP_LEFT_RADIUS",
                     "status_bar_clock_background_chip" + "_TOP_RIGHT_RADIUS",
                     "status_bar_clock_background_chip" + "_BOTTOM_LEFT_RADIUS",
                     "status_bar_clock_background_chip" + "_BOTTOM_RIGHT_RADIUS",
                     "status_bar_clock_background_chip" + "_MARGIN_LEFT",
                     "status_bar_clock_background_chip" + "_MARGIN_RIGHT",
                     "status_bar_clock_background_chip" + "_MARGIN_TOP",
                     "status_bar_clock_background_chip" + "_MARGIN_BOTTOM",
                     "status_bar_clock_background_chip" + "_PADDING_LEFT",
                     "status_bar_clock_background_chip" + "_PADDING_RIGHT",
                     "status_bar_clock_background_chip" + "_PADDING_TOP",
                     "status_bar_clock_background_chip" + "_PADDING_BOTTOM" -> updateChip();
            }
        }

    }

    //noinspection throwableresult of the method is ignored
    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        Class<?> ClockClass = findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);
        Class<?> CollapsedStatusBarFragmentClass = findClass("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.classLoader);
        Class<?> TaskStackListenerImpl = findClass("com.android.wm.shell.common.TaskStackListenerImpl", lpparam.classLoader);
        Class<?> StatClock = null;
        try {
            StatClock = findClass("com.oplus.systemui.statusbar.widget.StatClock", lpparam.classLoader);
        } catch (Throwable ignored) {
            log("StatClock not found");
        }

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        mContext.registerReceiver(mBroadcastReceiver, filter, Context.RECEIVER_EXPORTED);

        hookAllConstructors(CollapsedStatusBarFragmentClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mCollapsedStatusBarFragment = param.thisObject;
            }
        });

        if (Build.VERSION.SDK_INT == 33) {
            try {
                Class<?> PhoneStatusBarView = findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader);
                hookAllMethods(PhoneStatusBarView, "onFinishInflate", new XC_MethodHook() {
                    @SuppressLint("DiscouragedApi")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        FrameLayout mStatusBar = (FrameLayout) param.thisObject;
                        mStatusbarStartSide = mStatusBar.findViewById(mContext.getResources().getIdentifier("status_bar_left_side", "id", mContext.getPackageName()));
                    }
                });
            } catch (Throwable ignored) {
            }
        } else {
            mStatusbarStartSide = null;
        }

        findAndHookMethod(CollapsedStatusBarFragmentClass,
                "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                    @SuppressLint("DiscouragedApi")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        try {
                            mClockView = (TextView) getObjectField(param.thisObject, "mClockView");
                        } catch (Throwable ignored) {
                            log("mClockView not found");
                        }

                        ViewGroup mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");
                        try {
                            mStatusbarStartSide = mStatusBar.findViewById(mContext.getResources().getIdentifier("status_bar_start_side_except_heads_up", "id", mContext.getPackageName()));
                        } catch (Throwable t) {
                            mStatusbarStartSide = null;
                        }

                        try {
                            mSystemIconArea = mStatusBar.findViewById(mContext.getResources().getIdentifier("statusIcons", "id", mContext.getPackageName()));
                        } catch (Throwable t) {
                            mSystemIconArea = mStatusBar.findViewById(mContext.getResources().getIdentifier("system_icon_area", "id", mContext.getPackageName())); // OOS 13
                        }


                        try {
                            mCenteredIconArea = (View) ((View) getObjectField(param.thisObject, "mCenteredIconArea")).getParent();
                        } catch (Throwable ignored) {
                            mCenteredIconArea = new LinearLayout(mContext);
                            FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT);
                            lp.gravity = Gravity.CENTER;
                            mCenteredIconArea.setLayoutParams(lp);
                            mStatusBar.addView(mCenteredIconArea);
                        }

                        updateClock();
                        updateChip();
                        setupChip();
                        placeClock();
                        setClockSize();
                    }
                });

        if (StatClock != null) {
            try {
                hookAllMethods(StatClock, "updateMinWidth", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        // StatClock has a method to update the minimum width of the clock
                        // we can use it to update the clock width
                        // Based on our custom formats
                        TextView tv = (TextView) param.thisObject;
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, mClockSize);
                        if (!mShowSeconds) {
                            float totalWidth = measureTextWithSpans();
                            totalWidth += tv.getPaddingStart();
                            totalWidth += tv.getPaddingEnd();
                            int calculatedMinWidth = (int) totalWidth;
                            if (tv.getMinimumWidth() != calculatedMinWidth) {
                                tv.setMinimumWidth(calculatedMinWidth);
                            }
                        }
                    }
                });
            } catch (Throwable ignored) {
                log("updateMinWidth in StatClock not found");
            }
        }

        findAndHookMethod(CollapsedStatusBarFragmentClass, "animateShow",
                View.class, boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args[0] == mClockView) {
                            updateClock();
                        }
                    }
                });

        hookAllMethods(ClockClass, "updateClockVisibility", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                if (param.thisObject != mClockView)
                    return;

                if (mClockCustomColor)
                    mClockView.post(() -> mClockView.setTextColor(mClockColor));

                Clock = param.thisObject;

                if (!mClockAutoHide) return;

                boolean visible = (boolean) callMethod(param.thisObject, "shouldBeVisible");
                int visibility = visible ? View.VISIBLE : View.GONE;
                try {
                    autoHideHandler.removeCallbacksAndMessages(null);
                } catch (Throwable ignored) {

                }
                callMethod(param.thisObject, "setVisibility", visibility);
                if (mClockAutoHide && visible && mScreenOn) {
                    autoHideHandler.postDelayed(() -> autoHideClock(param.thisObject), mShowDuration * 1000L);
                }
                param.setResult(null);
            }
        });

        hookAllMethods(ClockClass, "shouldBeVisible", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                if (!mClockAutoHideLauncherSwitch) return;

                if (param.thisObject != mClockView)
                    return;

                Clock = param.thisObject;

                boolean mClockVisibleByPolicy = getBooleanField(param.thisObject, "mClockVisibleByPolicy");
                boolean mClockVisibleByUser = getBooleanField(param.thisObject, "mClockVisibleByUser");
                param.setResult(!mClockAutoHideLauncher && mClockVisibleByPolicy && mClockVisibleByUser);
            }
        });


        findAndHookMethod(ClockClass,
                "getSmallTime", new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        setObjectField(param.thisObject, "mShowSeconds", mShowSeconds);
                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.thisObject != mClockView)
                            return; //We don't want custom format in QS header. do we?


                        SpannableStringBuilder result = new SpannableStringBuilder();

                        SpannableStringBuilder clockText = SpannableStringBuilder.valueOf((CharSequence) param.getResult()); //THE clock

                        result.append(getFormattedString(mCustomBeforeClock, mCustomBeforeSmall, mClockDateStyle, mClockCustomColor ? mClockColor : null)); //before clock

                        if (mClockCustomColor) {
                            clockText.setSpan(new ForegroundColorSpan(mClockColor), 0, (clockText).length(),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        result.append(clockText);

                        if (mAmPmStyle != AM_PM_STYLE_GONE) {
                            result.append(getFormattedString("$Ga", mAmPmStyle == AM_PM_STYLE_SMALL, 0, mClockCustomColor ? mClockColor : null));
                        }

                        result.append(getFormattedString(mCustomAfterClock, mCustomAfterSmall, mClockDateStyle, mClockCustomColor ? mClockColor : null)); //after clock

                        if (getAdditionalInstanceField(param.thisObject, "stringFormatCallBack") == null) {
                            StringFormatter.FormattedStringCallback callback = () -> {
                                if (!mShowSeconds) //don't update again if it's going to do it every second anyway
                                    updateClock();
                            };

                            stringFormatter.registerCallback(callback);
                            setAdditionalInstanceField(param.thisObject, "stringFormatCallBack", callback);
                        }
                        param.setResult(result);
                    }
                });


        findAndHookMethod(TaskStackListenerImpl, "onTaskStackChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (mClockAutoHideLauncherSwitch) updateShowClock();
            }
        });

        findAndHookMethod(TaskStackListenerImpl, "onTaskRemoved", int.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (mClockAutoHideLauncherSwitch) updateShowClock();
            }
        });

        hookAllMethods(TaskStackListenerImpl, "onTaskMovedToFront", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (mClockAutoHideLauncherSwitch) updateShowClock();
            }
        });


    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private float measureTextWithSpans() {
        TextPaint textPaint = new TextPaint();
        textPaint.setTextSize(mClockSize);
        textPaint.setTypeface(mClockView.getTypeface());

        float totalWidth = 0f;

        CharSequence beforeClock = getFormattedString(mCustomBeforeClock, mCustomBeforeSmall, mClockDateStyle, mClockCustomColor ? mClockColor : null);
        float beforeClockWidth = textPaint.measureText(beforeClock.toString());

        if (mCustomBeforeSmall) {
            beforeClockWidth *= 0.7f;
        }
        totalWidth += beforeClockWidth;

        String timeText = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
        totalWidth += textPaint.measureText(timeText);

        if (mAmPmStyle != AM_PM_STYLE_GONE) {
            CharSequence amPmText = getFormattedString("$Ga", mAmPmStyle == AM_PM_STYLE_SMALL, 0, mClockCustomColor ? mClockColor : null);
            float amPmWidth = textPaint.measureText(amPmText.toString());
            if (mAmPmStyle == AM_PM_STYLE_SMALL) {
                amPmWidth *= 0.7f;
            }
            totalWidth += amPmWidth;
        }

        CharSequence afterClock = getFormattedString(mCustomAfterClock, mCustomAfterSmall, mClockDateStyle, mClockCustomColor ? mClockColor : null);
        float afterClockWidth = textPaint.measureText(afterClock.toString());

        if (mCustomAfterSmall) {
            afterClockWidth *= 0.7f;
        }
        totalWidth += afterClockWidth;


        return dp2px(mContext, totalWidth);
    }

    private void autoHideClock(Object clock) {
        callMethod(clock, "setVisibility", View.GONE);
        autoHideHandler.postDelayed(() -> updateClockVisibility(clock), mHideDuration * 1000L);
    }

    private void updateClockVisibility(Object clock) {
        if (clock != null)
            callMethod(clock, "updateClockVisibility");
    }

    private void updateClockVisibility() {
        if (Clock != null) {
            callMethod(Clock, "updateClockVisibility");
        }
    }

    private void updateClock() {
        try {
            mClockView.post(() -> { //the builtin update method doesn't care about the format. Just the text sadly
                callMethod(getObjectField(mClockView, "mCalendar"), "setTimeInMillis", System.currentTimeMillis());
                callMethod(mClockView, "updateClock");
            });
        } catch (Throwable ignored) {
        }
    }

    private void placeClock() {
        ViewGroup parent = (ViewGroup) mClockView.getParent();
        ViewGroup targetArea = null;
        Integer index = null;

        switch (mClockPosition) {
            case POSITION_LEFT -> {
                targetArea = mStatusbarStartSide;
                index = 1;
                mClockView.setPadding(leftClockPadding, 0, 0, 0);
            }
            case POSITION_CENTER -> {
                targetArea = (ViewGroup) mCenteredIconArea;
                mClockView.setPadding(rightClockPadding, 0, rightClockPadding, 0);
                setMargins(mCenteredIconArea, mContext, 0, 4, 0, 4);
            }
            case POSITION_RIGHT -> {
                mClockView.setPadding(0, 0, 0, 0);
                targetArea = ((ViewGroup) mSystemIconArea.getParent());
            }
        }
        if (targetArea != null) {
            parent.removeView(mClockView);
            if (index != null) {
                targetArea.addView(mClockView, index);
            } else {
                targetArea.addView(mClockView);
            }
        }
        updateChip();
    }

    private CharSequence getFormattedString(String dateFormat, boolean small, int caseStyle, @Nullable @ColorInt Integer textColor) {
        if (dateFormat.isEmpty()) return "";

        //There's some format to work on
        CharSequence format = stringFormatter.formatString(dateFormat);
        String form = format.toString();
        if (caseStyle == CLOCK_DATE_STYLE_UPPERCASE) {
            form = form.toUpperCase();
        } else if (caseStyle == CLOCK_DATE_STYLE_LOWERCASE) {
            form = form.toLowerCase();
        }
        SpannableStringBuilder formatted = new SpannableStringBuilder(form);

        if (small) {
            //small size requested
            CharacterStyle style = new RelativeSizeSpan(0.7f);
            formatted.setSpan(style, 0, formatted.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        if (textColor != null) {
            //color requested
            CharacterStyle textColorSpan = new ForegroundColorSpan(textColor);
            formatted.setSpan(textColorSpan, 0, (formatted).length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return formatted;
    }

    @SuppressLint("RtlHardcoded")
    private void setClockSize() {
        if (mClockView == null) return;
        if (mClockSize > 12) {
            ViewGroup.LayoutParams params = mClockView.getLayoutParams();
            params.height = MATCH_PARENT;
            mClockView.setLayoutParams(params);
            setMargins(mClockView, mContext, 0, 0, 0, 0);
            mClockView.setPadding(0, 0, 0, 0);
            switch (mClockPosition) {
                case POSITION_LEFT -> mClockView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
                case POSITION_CENTER -> mClockView.setGravity(Gravity.CENTER);
                case POSITION_RIGHT ->
                        mClockView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            }
            mClockView.setIncludeFontPadding(false);
            mClockView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            mClockView.requestLayout();
        }
        mClockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mClockSize);
        if (clockChip) updateChip();
    }

    private void updateChip() {
        int[] colors;
        int strokeColor = Color.TRANSPARENT;
        int strokeWidth = chipStrokeWidth;

        if (chipUseAccent) {
            colors = new int[]{getPrimaryColor(mContext), getPrimaryColor(mContext)};
        } else if (chipUseGradient) {
            colors = new int[]{chipGradient1, chipGradient2};
        } else {
            colors = new int[]{chipGradient1, chipGradient1};
        }
        switch (chipStyle) {
            case 0:
                strokeWidth = 0;
                break;
            case 1:
                colors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT};
                strokeWidth = chipStrokeWidth;
                strokeColor = chipAccentStroke ? getPrimaryColor(mContext) : chipStrokeColor;
                break;
            case 2:
                strokeWidth = chipStrokeWidth;
                strokeColor = chipAccentStroke ? getPrimaryColor(mContext) : chipStrokeColor;
                break;
        }
        float[] radii;
        if (chipRoundCorners) {
            radii = new float[]{
                    dp2px(mContext, chipTopSxRound), dp2px(mContext, chipTopSxRound),
                    dp2px(mContext, chipTopDxRound), dp2px(mContext, chipTopDxRound),
                    dp2px(mContext, chipBottomDxRound), dp2px(mContext, chipBottomDxRound),
                    dp2px(mContext, chipBottomSxRound), dp2px(mContext, chipBottomSxRound)
            };
        } else {
            radii = null;
        }
        mClockChipDrawable = getChip(chipGradientOrientation, colors, strokeWidth, strokeColor, radii);
        mClockChipDrawable.invalidateSelf();
        setupChip();
    }

    @SuppressLint("RtlHardcoded")
    private void setupChip() {
        if (clockChip) {
            mClockView.setPadding(dp2px(mContext, chipPaddingSx), dp2px(mContext, chipPaddingTop), dp2px(mContext, chipPaddingDx), dp2px(mContext, chipPaddingBottom));
            setMargins(mClockView, mContext, chipMarginSx, chipMarginTop, chipMarginDx, chipMarginBottom);
        } else {
            mClockView.setPadding(0, 0, 0, 0);
            setMargins(mClockView, mContext, 0, 0, 0, 0);
            mClockView.post(() -> mClockView.setBackground(null));
        }
        if (!clockChip) return;
        mClockView.post(() -> mClockView.setBackground(mClockChipDrawable));
        switch (mClockPosition) {
            case POSITION_LEFT -> mClockView.setGravity(Gravity.LEFT | Gravity.CENTER);
            case POSITION_CENTER -> mClockView.setGravity(Gravity.CENTER);
            case POSITION_RIGHT -> mClockView.setGravity(Gravity.RIGHT | Gravity.CENTER);
        }
        mClockView.setIncludeFontPadding(false);
        mClockView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mClockView.post(() -> {
            mClockView.invalidate();
            mClockView.requestLayout();
        });
    }

    private void updateShowClock() {

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

        String foregroundApp = null;
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                foregroundApp = processInfo.processName;
                break;
            }
        }

        if (TextUtils.isEmpty(foregroundApp)) return;
        final boolean clockAutoHide = foregroundApp.equals(getDefaultLauncherPackageName());
        if (mClockAutoHideLauncher != clockAutoHide) {
            mClockAutoHideLauncher = clockAutoHide;
            updateClock();
            autoHideHandler.post(() -> updateClockVisibility(Clock));
        }
    }

    private String getDefaultLauncherPackageName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        return null;
    }

}
