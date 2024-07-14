package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.text.style.CharacterStyle;
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

import java.util.Date;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.StringFormatter;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import kotlin.Suppress;

public class StatusbarClock extends XposedMods {

    private final String TAG = "Oxygen Customizer - Statusbar Clock: ";
    private static final String listenPackage = SYSTEM_UI;

    private static final int AM_PM_STYLE_NORMAL = 0;
    private static final int AM_PM_STYLE_SMALL = 1;
    private static final int AM_PM_STYLE_GONE = 2;

    private static final int CLOCK_DATE_DISPLAY_GONE = 0;
    private static final int CLOCK_DATE_DISPLAY_SMALL = 1;
    private static final int CLOCK_DATE_DISPLAY_NORMAL = 2;
    private static final int CLOCK_DATE_STYLE_REGULAR = 0;
    private static final int CLOCK_DATE_STYLE_LOWERCASE = 1;
    private static final int CLOCK_DATE_STYLE_UPPERCASE = 2;
    private static final int HIDE_DURATION = 60; // 1 minute
    private static final int SHOW_DURATION = 5; // 5 seconds
    private static final int STYLE_DATE_LEFT = 0;
    private static final int STYLE_DATE_RIGHT = 1;

    private boolean mClockAutoHideLauncher = false;
    private boolean mScreenOn = true;
    private Handler autoHideHandler = new Handler();

    private boolean mClockAutoHide;
    private boolean mClockAutoHideLauncherSwitch;
    private int mHideDuration = HIDE_DURATION, mShowDuration = SHOW_DURATION;
    private int mAmPmStyle;
    private boolean mShowSeconds;
    private int mClockDateDisplay = CLOCK_DATE_DISPLAY_GONE;
    private int mClockDateStyle = CLOCK_DATE_STYLE_REGULAR;
    private int mClockDatePosition = STYLE_DATE_LEFT;
    private String mClockDateFormat = null;
    private String mCustomClockDateFormat = "$GEEE";
    private Object Clock = null;
    private TextView mClockView;
    private Object mCollapsedStatusBarFragment = null;
    private ViewGroup mStatusbarStartSide = null;
    private View mCenteredIconArea = null;
    private LinearLayout mSystemIconArea = null;
    public static final int POSITION_LEFT = 2;
    public static final int POSITION_CENTER = 1;
    public static final int POSITION_RIGHT = 0;
    public static final int POSITION_LEFT_EXTRA_LEVEL = 3;
    private int mClockPosition = POSITION_LEFT;
    private int leftClockPadding = 0, rightClockPadding = 0;

    private boolean mClockCustomColor;
    private int mClockColor = Color.WHITE;

    // Clock Chip
    private boolean clockChip;
    private int chipStyle;
    private boolean chipUseAccent, chipUseGradient;
    private int chipGradient1, chipGradient2;
    private int chipStrokeWidth;
    private boolean chipRoundCorners;
    private int chipTopSxRound, chipTopDxRound, chipBottomSxRound,chipBottomDxRound;
    private int mAccent;
    private final GradientDrawable mClockChipDrawale = new GradientDrawable();
    private int mClockSize = 12;


    public StatusbarClock(Context context) {
        super(context);

        if (!listensTo(context.getPackageName())) return;

        rightClockPadding = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_clock_starting_padding", "dimen", mContext.getPackageName()));
        leftClockPadding = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("status_bar_left_clock_end_padding", "dimen", mContext.getPackageName()));
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
        mClockDateFormat = Xprefs.getString("status_bar_clock_date_format", null);
        mCustomClockDateFormat = Xprefs.getString("status_bar_custom_clock_format", "$GEEE");
        mClockPosition = Integer.parseInt(Xprefs.getString("status_bar_clock", String.valueOf(POSITION_LEFT)));
        mClockCustomColor = Xprefs.getBoolean("status_bar_custom_clock_color", false);
        mClockColor = Xprefs.getInt("status_bar_clock_color", Color.WHITE);
        mClockSize = Xprefs.getSliderInt("status_bar_clock_size", 12);

        // gradients prefs
        clockChip = Xprefs.getBoolean("status_bar_clock_background_chip_switch", false);
        chipStyle = Xprefs.getInt("status_bar_clock_background_chip" + "_STYLE", 0);
        chipUseAccent = Xprefs.getBoolean("status_bar_clock_background_chip" + "_USE_ACCENT_COLOR", true);
        chipUseGradient = Xprefs.getBoolean("status_bar_clock_background_chip" + "_USE_GRADIENT", false);
        chipGradient1 = Xprefs.getInt("status_bar_clock_background_chip" + "_GRADIENT_1", mAccent);
        chipGradient2 = Xprefs.getInt("status_bar_clock_background_chip" + "_GRADIENT_2", mAccent);
        chipStrokeWidth = Xprefs.getInt("status_bar_clock_background_chip" + "_STROKE_WIDTH", 10);
        chipRoundCorners = Xprefs.getBoolean("status_bar_clock_background_chip" + "_ROUNDED_CORNERS", false);
        chipTopSxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_TOP_LEFT_RADIUS", 28);
        chipTopDxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_TOP_RIGHT_RADIUS", 28);
        chipBottomSxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_BOTTOM_LEFT_RADIUS", 28);
        chipBottomDxRound = Xprefs.getInt("status_bar_clock_background_chip" + "_BOTTOM_RIGHT_RADIUS", 28);


        if (Key.length > 0) {
            switch (Key[0]) {
                case "status_bar_clock_auto_hide" -> updateClockVisibility();
                case "status_bar_am_pm",
                        "status_bar_clock_date_display",
                        "status_bar_clock_seconds",
                        "status_bar_clock_date_position",
                        "status_bar_clock_date_style",
                        "status_bar_clock_date_format",
                     "status_bar_custom_clock_format" -> updateClock();
                case "status_bar_clock_size" -> setClockSize();
                case "status_bar_custom_clock_color", "status_bar_clock_color" -> updateClockColor();
                case "status_bar_clock" -> placeClock();
                case "status_bar_clock_background_chip" + "_STYLE",
                        "status_bar_clock_background_chip" + "_USE_ACCENT_COLOR",
                        "status_bar_clock_background_chip" + "_USE_GRADIENT",
                        "status_bar_clock_background_chip" + "_GRADIENT_1",
                        "status_bar_clock_background_chip" + "_GRADIENT_2",
                        "status_bar_clock_background_chip" + "_STROKE_WIDTH",
                        "status_bar_clock_background_chip" + "_ROUNDED_CORNERS",
                        "status_bar_clock_background_chip" + "_TOP_LEFT_RADIUS",
                        "status_bar_clock_background_chip" + "_TOP_RIGHT_RADIUS",
                        "status_bar_clock_background_chip" + "_BOTTOM_LEFT_RADIUS",
                        "status_bar_clock_background_chip" + "_BOTTOM_RIGHT_RADIUS" -> updateChip();
                case "status_bar_clock_background_chip_switch" -> setupChip();
            }
        }

    }

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

    @Suppress(names = "UNREACHABLE_CODE")
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
            log(TAG + "StatClock not found");
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
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        FrameLayout mStatusBar = (FrameLayout) param.thisObject;
                        mStatusbarStartSide = mStatusBar.findViewById(mContext.getResources().getIdentifier("status_bar_left_side", "id", mContext.getPackageName()));
                    }
                });
            } catch (Throwable ignored) {}
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
                            log(TAG + "mClockView not found");
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

                        placeClock();
                        setClockSize();
                        updateClockColor();
                        updateChip();
                        setupChip();
                    }
                });

        if (StatClock != null) {
            try {
                hookAllMethods(StatClock, "measureText", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (param.args.length >= 2 && param.args[1] instanceof Float) {
                            param.args[1] = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mClockSize, mContext.getResources().getDisplayMetrics());
                            ;
                        }
                    }
                });
            } catch (Throwable ignored) {
                log(TAG + "measureText in StatClock not found");
            }
        }

        findAndHookMethod(CollapsedStatusBarFragmentClass, "animateShow",
                View.class, boolean.class,
                new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.args[0] == mClockView) {
                    updateClockColor();
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

                if (!mClockAutoHide) return;

                Clock = param.thisObject;

                boolean visible = (boolean) callMethod(param.thisObject, "shouldBeVisible");
                int visibility = visible ? View.VISIBLE : View.GONE;
                try {
                    autoHideHandler.removeCallbacksAndMessages(null);
                } catch (Throwable ignored) {

                }
                callMethod(param.thisObject, "setVisibility", visibility);
                if (mClockAutoHide && visible && mScreenOn) {
                    autoHideHandler.postDelayed(() -> autoHideClock(param.thisObject), mShowDuration * 1000);
                }
                param.setResult(null);
            }
        });

        hookAllMethods(ClockClass, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (param.thisObject != mClockView)
                    return;
                setClockSize();
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


                        /*CharSequence dateString = null;
                        CharSequence amPmString = null;
                        String dateResult = "";
                        String amPmResult = "";
                        CharSequence timeResult = (CharSequence) param.getResult();
                        String result = timeResult.toString();
                        Date now = new Date();

                        if (mAmPmStyle != AM_PM_STYLE_GONE) {
                            amPmString = DateFormat.format("a", now).toString();
                            amPmResult = amPmString.toString().toUpperCase();
                            result = result + " " + amPmResult;
                        }

                        if (mClockDateDisplay != CLOCK_DATE_DISPLAY_GONE) {
                            if (mClockDateFormat == null || mClockDateFormat.isEmpty()) {
                                // Set dateString to short uppercase Weekday if empty
                                dateString = DateFormat.format("EEE", now);
                            } else {
                                if (!mClockDateFormat.equals("custom")) {
                                    dateString = DateFormat.format(mClockDateFormat, now);
                                } else {

                                }
                            }
                            if (mClockDateStyle == CLOCK_DATE_STYLE_LOWERCASE) {
                                // When Date style is small, convert date to uppercase
                                dateResult = dateString.toString().toLowerCase();
                            } else if (mClockDateStyle == CLOCK_DATE_STYLE_UPPERCASE) {
                                dateResult = dateString.toString().toUpperCase();
                            } else {
                                dateResult = dateString.toString();
                            }
                            result = (mClockDatePosition == STYLE_DATE_LEFT) ? dateResult + " " + result
                                    : result + " " + dateResult;
                        }

                        SpannableStringBuilder formatted = new SpannableStringBuilder(result);

                        if (mAmPmStyle != AM_PM_STYLE_GONE) {
                            if (amPmString != null) {
                                int amPmStringLen = amPmString.length();
                                int timeStringOffset = timeResult.length() + 1;
                                if (mAmPmStyle == AM_PM_STYLE_SMALL) {
                                    CharacterStyle style = new RelativeSizeSpan(0.7f);
                                    formatted.setSpan(style, timeStringOffset,
                                            timeStringOffset + amPmStringLen,
                                            Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                }
                            }
                        }

                        if (mClockDateDisplay != CLOCK_DATE_DISPLAY_NORMAL) {
                            if (dateString != null) {
                                int dateStringLen = dateString.length();
                                int timeStringOffset = (mClockDatePosition == STYLE_DATE_RIGHT)
                                        ? timeResult.length() + 1 : 0;
                                if (mClockDateDisplay == CLOCK_DATE_DISPLAY_GONE) {
                                    formatted.delete(0, dateStringLen);
                                } else {
                                    if (mClockDateDisplay == CLOCK_DATE_DISPLAY_SMALL) {
                                        CharacterStyle style = new RelativeSizeSpan(0.7f);
                                        formatted.setSpan(style, timeStringOffset,
                                                timeStringOffset + dateStringLen,
                                                Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
                                    }
                                }
                            }
                        }

                        if(getAdditionalInstanceField(param.thisObject, "stringFormatCallBack") == null) {
                            StringFormatter.FormattedStringCallback callback = () -> {
                                if(!mShowSeconds) //don't update again if it's going to do it every second anyway
                                    updateClock();
                            };

                            stringFormatter.registerCallback(callback);
                            setAdditionalInstanceField(param.thisObject, "stringFormatCallBack", callback);
                        }

                        param.setResult(formatted);*/

                        SpannableStringBuilder result = new SpannableStringBuilder();

                        SpannableStringBuilder clockText = SpannableStringBuilder.valueOf((CharSequence) param.getResult()); //THE clock
                        if (mClockCustomColor) {
                            clockText.setSpan(mClockColor, 0, (clockText).length(),
                                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        String dateFormat = "";
                        if (!TextUtils.isEmpty(mClockDateFormat) && !mClockDateFormat.equals("custom")) {
                            dateFormat = mClockDateFormat;
                        } else if (!TextUtils.isEmpty(mCustomClockDateFormat)) {
                            dateFormat = mCustomClockDateFormat;
                        }
                        if (mClockDateDisplay != CLOCK_DATE_DISPLAY_GONE && mClockDatePosition == STYLE_DATE_LEFT) {
                            result.append(getFormattedString(dateFormat + " ", mClockDateDisplay == CLOCK_DATE_DISPLAY_SMALL, mClockDateStyle)); //before clock
                        }
                        result.append(clockText);
                        if (mAmPmStyle != AM_PM_STYLE_GONE) {
                            result.append(getFormattedString(" $Ga", mAmPmStyle == AM_PM_STYLE_SMALL, 0));
                        }
                        if (mClockDateDisplay != CLOCK_DATE_DISPLAY_GONE && mClockDatePosition == STYLE_DATE_RIGHT) {
                            result.append(getFormattedString(" " + dateFormat, mClockDateDisplay == CLOCK_DATE_DISPLAY_SMALL, mClockDateStyle)); //before clock
                        }

                        if(getAdditionalInstanceField(param.thisObject, "stringFormatCallBack") == null) {
                            StringFormatter.FormattedStringCallback callback = () -> {
                                if(!mShowSeconds) //don't update again if it's going to do it every second anyway
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


    private void autoHideClock(Object clock) {
        callMethod(clock, "setVisibility", View.GONE);
        autoHideHandler.postDelayed(() -> updateClockVisibility(clock), mHideDuration * 1000);
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
        }
        catch (Throwable ignored){}
    }

    private void updateClockColor() {
        if (mClockCustomColor)
            mClockView.post(() -> mClockView.setTextColor(mClockColor));
    }

    private void placeClock() {
        ViewGroup parent = (ViewGroup) mClockView.getParent();
        ViewGroup targetArea = null;
        Integer index = null;

        switch (mClockPosition) {
            case POSITION_LEFT -> {
                targetArea = mStatusbarStartSide;
                index = 1;
                mClockView.setPadding(0, 0, leftClockPadding, 0);
            }
            case POSITION_CENTER -> {
                targetArea = (ViewGroup) mCenteredIconArea;
                mClockView.setPadding(rightClockPadding, 0, rightClockPadding, 0);
            }
            case POSITION_RIGHT -> {
                mClockView.setPadding(rightClockPadding, 0, 0, 0);
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

    }

    private final StringFormatter stringFormatter = new StringFormatter();

    private CharSequence getFormattedString(String dateFormat, boolean small, int caseStyle) {
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

        return formatted;
    }

    private void setClockSize() {
        if (mClockView == null) return;
        if (mClockSize > 12) {
            mClockView.getLayoutParams().height = WRAP_CONTENT;
            ViewHelper.setMargins(mClockView, mContext, 0, 0, 0, 0);
            mClockView.setPadding(0, 0, 0, 0);
            switch (mClockDatePosition) {
                case POSITION_LEFT -> mClockView.setGravity(Gravity.LEFT | Gravity.CENTER);
                case POSITION_CENTER -> mClockView.setGravity(Gravity.CENTER);
                case POSITION_RIGHT -> mClockView.setGravity(Gravity.RIGHT | Gravity.CENTER);
            }
            mClockView.setIncludeFontPadding(false);
            mClockView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            mClockView.requestLayout();
            if (mCenteredIconArea != null) mCenteredIconArea.requestLayout();
        }
        mClockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mClockSize);
    }

    private void updateChip() {
        mAccent = getPrimaryColor(mContext);

        mClockChipDrawale.setShape(GradientDrawable.RECTANGLE);
        mClockChipDrawale.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mClockChipDrawale.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        if (chipRoundCorners) {
            mClockChipDrawale.setCornerRadii(new float[]{
                    dp2px(mContext, chipTopSxRound), dp2px(mContext, chipTopSxRound),
                    dp2px(mContext, chipTopDxRound), dp2px(mContext, chipTopDxRound),
                    dp2px(mContext, chipBottomDxRound), dp2px(mContext, chipBottomDxRound),
                    dp2px(mContext, chipBottomSxRound), dp2px(mContext, chipBottomSxRound)
            });
        } else {
            mClockChipDrawale.setCornerRadius(0);
        }

        if (chipStyle == 0) {
            if (chipUseAccent)
                mClockChipDrawale.setColors(new int[]{mAccent, mAccent});
            else if (chipUseGradient)
                mClockChipDrawale.setColors(new int[]{chipGradient1, chipGradient2});
            else
                mClockChipDrawale.setColors(new int[]{chipGradient1, chipGradient1});
            mClockChipDrawale.setStroke(0, Color.TRANSPARENT);
        } else {
            mClockChipDrawale.setColors(new int[]{Color.TRANSPARENT, Color.TRANSPARENT});
            mClockChipDrawale.setStroke(chipStrokeWidth, chipUseAccent ? mAccent : chipGradient1);
        }
        mClockChipDrawale.setPadding(2, 0, 2, 0);
        mClockChipDrawale.invalidateSelf();
        setupChip();
    }

    @SuppressLint("RtlHardcoded")
    private void setupChip() {
        if (clockChip) {
            mClockView.setPadding(dp2px(mContext, 4), dp2px(mContext, 2), dp2px(mContext, 4), dp2px(mContext, 2));
            mClockView.getLayoutParams().height = ViewGroup.LayoutParams.WRAP_CONTENT;
            mClockView.setBackground(mClockChipDrawale);
        } else {
            mClockView.setPadding(0, 0, 0, 0);
            mClockView.getLayoutParams().height = ViewGroup.LayoutParams.MATCH_PARENT;
            mClockView.setBackground(null);
        }
        switch (mClockDatePosition) {
            case POSITION_LEFT -> mClockView.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            case POSITION_CENTER -> mClockView.setGravity(Gravity.CENTER_VERTICAL);
            case POSITION_RIGHT -> mClockView.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        }
        mClockView.setIncludeFontPadding(false);
        mClockView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        mClockView.requestLayout();
        if (mCenteredIconArea != null) mCenteredIconArea.requestLayout();
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
            log(TAG + "Updating Clock");
            callMethod(Clock, "updateClock");
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
