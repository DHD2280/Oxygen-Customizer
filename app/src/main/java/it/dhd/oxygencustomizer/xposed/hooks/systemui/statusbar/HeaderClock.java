package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.content.Context.RECEIVER_EXPORTED;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_QS_CLOCK_UPDATE;
import static it.dhd.oxygencustomizer.utils.Constants.CLOCK_TAG;
import static it.dhd.oxygencustomizer.utils.Constants.DATE_TAG;
import static it.dhd.oxygencustomizer.utils.Constants.HEADER_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CHIP_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_COLOR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_ENABLED;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_CUSTOM_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_LEFT_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_HIDE_CARRIER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_RED_MODE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock.QS_HEADER_PREFS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderSystemIcons.QS_HEADER_SYSTEM_ICON_CHIP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderSystemIcons.QS_SYSTEM_ICON_CHIP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderSystemIcons.QS_SYSTEM_ICON_CHIP_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.getRoundedCorners;
import static it.dhd.oxygencustomizer.utils.Constants.getStrokeWidth;
import static it.dhd.oxygencustomizer.utils.Constants.getStyle;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.getChip;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.Typeface;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.text.TextUtilsCompat;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.utils.Constants.Preferences.QsHeaderClock;
import it.dhd.oxygencustomizer.utils.TextUtil;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class HeaderClock extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    public static final String OC_HEADER_CLOCK_TAG = "oxygencustomizer_header_clock";
    private final String TAG = "HeaderClock: ";

    private Context appContext;
    private final UserManager mUserManager;

    private boolean mBroadcastRegistered = false;

    LinearLayout mQsClockContainer = new LinearLayout(mContext);
    private static TextView mOplusClock = null;
    private static TextView mOplusDate = null;
    private static TextView mOplusCarrier = null;

    // Custom Clock Prefs
    private boolean showHeaderClock = false;
    private int clockStyle = 0;
    private int accent1, accent2, accent3, text1, text2;
    private float mClockTextScale = 1.0f;
    private int mTopMargin, mLeftMargin;
    private boolean centeredClockView = false;
    private boolean mClockCustomColor = false;
    private boolean mClockCustomUserImage = false;

    // Stock Clock Prefs
    private int stockClockRedStyle;
    private int stockClockRedOverrideColor;
    private boolean stockClockTimeColorSwitch;
    private int stockClockTimeColor;
    private boolean stockClockHideDate;
    private boolean stockClockDateColorSwitch;
    private int stockClockDateColor;
    private boolean stockClockTimeBackgroundChip, stockClockDateBackgroundChip;
    private boolean stockClockHideCarrier;

    // Clock Chip Style
    private int clockChipStyle, dateChipStyle;
    private boolean clockGradientAccent, dateGradientAccent;
    private int clockStrokeWitdh, dateStrokeWidth;
    private boolean clockRoundCorners, dateRoundCorners;
    private int clockGradientOrientation, dateGradientOrientation;
    private int clockStrokeColor, dateStrokeColor;
    private boolean clockStrokeAccent, dateStrokeAccent;
    private int clockTopSxRound, clockTopDxRound, clockBottomSxRound, clockBottomDxRound;
    private int dateTopSxRound, dateTopDxRound, dateBottomSxRound, dateBottomDxRound;
    private int clockGradient1, clockGradient2, dateGradient1, dateGradient2;
    private boolean clockUseGradient, dateUseGradient;
    private boolean customFontEnabled;
    private int systemIconBackgroundChipStyle;
    private boolean systemIconsChipEnabled = false;
    private boolean systemIconRoundCorners = false, systemIconUseAccent = true;
    private int systemIconTopSxRound, systemIconTopDxRound, systemIconBottomSxRound, systemIconBottomDxRound;
    private int systemIconChipGradient1, systemIconChipGradient2;
    private boolean systemIconChipGradient = false;
    private int systemIconStrokeWidth;

    private int mAccent;
    private static GradientDrawable mClockChipDrawable = new GradientDrawable();
    private static GradientDrawable mDateChipDrawable = new GradientDrawable();
    private static final GradientDrawable mSystemIconsChipDrawable = new GradientDrawable();
    private Typeface mStockClockTypeface, mStockDateTypeface;
    private Object OQC = null;
    private Object mActivityStarter = null;
    final ClickListener clickListener = new ClickListener();
    private View mStatusIconsView = null;

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(ACTIONS_QS_CLOCK_UPDATE)) {
                    updateClockView();
                }
            }
        }
    };

    public HeaderClock(Context context) {
        super(context);

        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        mUserManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    @Override
    public void updatePrefs(String... Key) {
        // Custom Header Prefs
        showHeaderClock = Xprefs.getBoolean(QS_HEADER_CLOCK_CUSTOM_ENABLED, false);
        clockStyle = Xprefs.getInt(QS_HEADER_CLOCK_CUSTOM_VALUE, 0);
        centeredClockView = Xprefs.getBoolean("center_clock", false);
        boolean nightMode = mContext.getResources().getConfiguration().isNightModeActive();
        int textColor = nightMode ? Color.WHITE : Color.BLACK;
        accent1 = Xprefs.getInt(
                QS_HEADER_CLOCK_COLOR_CODE_ACCENT1,
                getPrimaryColor(mContext)
        );
        accent2 = Xprefs.getInt(
                QS_HEADER_CLOCK_COLOR_CODE_ACCENT2,
                ContextCompat.getColor(mContext, android.R.color.system_accent2_600)
        );
        accent3 = Xprefs.getInt(
                QS_HEADER_CLOCK_COLOR_CODE_ACCENT3,
                ContextCompat.getColor(mContext, android.R.color.system_accent3_600)
        );
        text1 = Xprefs.getInt(
                QS_HEADER_CLOCK_COLOR_CODE_TEXT1,
                textColor
        );
        text2 = Xprefs.getInt(
                QS_HEADER_CLOCK_COLOR_CODE_TEXT2,
                textColor
        );
        mTopMargin = Xprefs.getSliderInt(QS_HEADER_CLOCK_TOP_MARGIN, 0);
        mLeftMargin = Xprefs.getSliderInt(QS_HEADER_CLOCK_LEFT_MARGIN, 8);
        mClockTextScale = Xprefs.getSliderFloat(QS_HEADER_CLOCK_TEXT_SCALING, 1.0f);
        mClockCustomColor = Xprefs.getBoolean(QS_HEADER_CLOCK_CUSTOM_COLOR_SWITCH, false);
        mClockCustomUserImage = Xprefs.getBoolean(QS_HEADER_CLOCK_CUSTOM_USER_IMAGE, false);

        // Stock Header Prefs
        stockClockRedStyle = Integer.parseInt(Xprefs.getString(QS_HEADER_CLOCK_STOCK_RED_MODE, "0"));
        stockClockRedOverrideColor = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR, Color.RED);
        stockClockTimeColorSwitch = Xprefs.getBoolean(QsHeaderClock.QS_HEADER_CLOCK_STOCK_TIME_COLOR_SWITCH, false);
        stockClockTimeColor = Xprefs.getInt(QsHeaderClock.QS_HEADER_CLOCK_STOCK_TIME_COLOR, 0);
        stockClockHideDate = Xprefs.getBoolean(QsHeaderClock.QS_HEADER_CLOCK_STOCK_HIDE_DATE, false);
        stockClockDateColorSwitch = Xprefs.getBoolean(QsHeaderClock.QS_HEADER_CLOCK_STOCK_DATE_COLOR_SWITCH, false);
        stockClockDateColor = Xprefs.getInt(QsHeaderClock.QS_HEADER_CLOCK_STOCK_DATE_COLOR, 0);
        stockClockTimeBackgroundChip = Xprefs.getBoolean(QsHeaderClock.QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP_SWITCH, false);
        clockChipStyle = Xprefs.getInt(getStyle(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP), 0);
        stockClockDateBackgroundChip = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP_SWITCH, false);
        dateChipStyle = Xprefs.getInt(getStyle(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP), 0);
        stockClockHideCarrier = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_HIDE_CARRIER, false);
        systemIconsChipEnabled = Xprefs.getBoolean(QS_SYSTEM_ICON_CHIP_SWITCH, false);
        systemIconBackgroundChipStyle = Xprefs.getInt(getStyle(QS_SYSTEM_ICON_CHIP), 0);

        // Font pref
        customFontEnabled = Xprefs.getBoolean(QS_HEADER_CLOCK_CUSTOM_FONT, false);

        // gradients prefs
        clockGradientAccent = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_USE_ACCENT_COLOR", true);
        clockGradientOrientation = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_GRADIENT_ORIENTATION", 0);
        clockUseGradient = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_USE_GRADIENT", false);
        clockGradient1 = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_GRADIENT_1", mAccent);
        clockGradient2 = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_GRADIENT_2", mAccent);
        clockStrokeAccent = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_USE_ACCENT_COLOR_STROKE", false);
        clockStrokeColor = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_STROKE_COLOR", mAccent);
        clockStrokeWitdh = Xprefs.getInt(getStrokeWidth(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP), 10);
        clockRoundCorners = Xprefs.getBoolean(getRoundedCorners(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP), true);
        clockTopSxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_TOP_LEFT_RADIUS", 28);
        clockTopDxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_TOP_RIGHT_RADIUS", 28);
        clockBottomSxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_BOTTOM_LEFT_RADIUS", 28);
        clockBottomDxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_CLOCK_BACKGROUND_CHIP + "_BOTTOM_RIGHT_RADIUS", 28);

        dateGradientAccent = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_USE_ACCENT_COLOR", true);
        dateGradientOrientation = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_GRADIENT_ORIENTATION", 0);
        dateUseGradient = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_USE_GRADIENT", false);
        dateGradient1 = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_GRADIENT_1", mAccent);
        dateGradient2 = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_GRADIENT_2", mAccent);
        dateStrokeAccent = Xprefs.getBoolean(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_USE_ACCENT_COLOR_STROKE", false);
        dateStrokeColor = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_STROKE_COLOR", mAccent);
        dateStrokeWidth = Xprefs.getInt(getStrokeWidth(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP), 10);
        dateRoundCorners = Xprefs.getBoolean(getRoundedCorners(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP), true);
        dateTopSxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_TOP_LEFT_RADIUS", 28);
        dateTopDxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_TOP_RIGHT_RADIUS", 28);
        dateBottomSxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_BOTTOM_LEFT_RADIUS", 28);
        dateBottomDxRound = Xprefs.getInt(QS_HEADER_CLOCK_STOCK_DATE_BACKGROUND_CHIP + "_BOTTOM_RIGHT_RADIUS", 28);

        // System Icon Chip
        systemIconUseAccent = Xprefs.getBoolean(QS_SYSTEM_ICON_CHIP + "_USE_ACCENT_COLOR", true);
        systemIconChipGradient = Xprefs.getBoolean(QS_SYSTEM_ICON_CHIP + "_USE_GRADIENT", false);
        systemIconChipGradient1 = Xprefs.getInt(QS_SYSTEM_ICON_CHIP + "_GRADIENT_1", mAccent);
        systemIconChipGradient2 = Xprefs.getInt(QS_SYSTEM_ICON_CHIP + "_GRADIENT_2", mAccent);
        systemIconStrokeWidth = Xprefs.getInt(getStrokeWidth(QS_SYSTEM_ICON_CHIP), 10);
        systemIconRoundCorners = Xprefs.getBoolean(getRoundedCorners(QS_SYSTEM_ICON_CHIP), true);
        systemIconTopSxRound = Xprefs.getInt(QS_SYSTEM_ICON_CHIP + "_TOP_LEFT_RADIUS", 28);
        systemIconTopDxRound = Xprefs.getInt(QS_SYSTEM_ICON_CHIP + "_TOP_RIGHT_RADIUS", 28);
        systemIconBottomSxRound = Xprefs.getInt(QS_SYSTEM_ICON_CHIP + "_BOTTOM_LEFT_RADIUS", 28);
        systemIconBottomDxRound = Xprefs.getInt(QS_SYSTEM_ICON_CHIP + "_BOTTOM_RIGHT_RADIUS", 28);


        if (Key.length > 0) {
            if (Key[0].equals(QS_HEADER_CLOCK_STOCK_RED_MODE)
                    || Key[0].equals(QS_HEADER_CLOCK_STOCK_RED_MODE_COLOR)
                    || Key[0].equals(QS_HEADER_CLOCK_CUSTOM_ENABLED)) {
                callMethod(OQC, "updateClock");
            }
            for (String k : QS_HEADER_PREFS) {
                if (Key[0].equals(k)) {
                    updateStockPrefs();
                    updateClockView();
                }
            }
            for (String k : QS_HEADER_CHIP_PREFS) {
                if (Key[0].equals(k)) {
                    setupChips();
                }
            }
            for (String k : QS_HEADER_SYSTEM_ICON_CHIP) {
                if (Key[0].equals(k)) {
                    setupStatusChips();
                }

            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (!mBroadcastRegistered) {
            mBroadcastRegistered = true;

            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ACTIONS_QS_CLOCK_UPDATE);
            mContext.registerReceiver(mReceiver, intentFilter, RECEIVER_EXPORTED); //for Android 14, receiver flag is mandatory
        }

        Class<?> QuickStatusBarHeader;
        try {
            QuickStatusBarHeader = findClass("com.oplus.systemui.qs.OplusQuickStatusBarHeader", lpparam.classLoader);
        } catch (Throwable t) {
            QuickStatusBarHeader = findClass("com.android.systemui.qs.QuickStatusBarHeader", lpparam.classLoader);
        }

        Class<?> Clock = findClass("com.android.systemui.statusbar.policy.Clock", lpparam.classLoader);
        hookAllConstructors(Clock, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                OQC = param.thisObject;
            }
        });

        try {
            hookAllMethods(QuickStatusBarHeader, "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    mStatusIconsView = (View) getObjectField(param.thisObject, "mStatusIconsView");
                    if (systemIconsChipEnabled) updateStatusChips();
                }
            });
        } catch (Throwable t) {
            log(TAG + "QuickStatusBarHeader onFinishInflate error: " + t.getMessage());
        }

        //OplusQSFooterImpl
        Class<?> OplusQSFooterImpl;
        try {
            OplusQSFooterImpl = findClass("com.oplus.systemui.qs.OplusQSFooterImpl", lpparam.classLoader);
        } catch (Throwable t) {
            OplusQSFooterImpl = findClass("com.oplusos.systemui.qs.OplusQSFooterImpl", lpparam.classLoader);
        }
        hookAllMethods(OplusQSFooterImpl, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {

                FrameLayout mQuickStatusBarHeader = (FrameLayout) param.thisObject;//getObjectField(param.thisObject, "mSettingsContainer");

                // qs_footer_side_padding
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                mQsClockContainer.setLayoutParams(layoutParams);
                mQsClockContainer.setPaddingRelative(0, -10, 0, 0);
                mQsClockContainer.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
                mQsClockContainer.setVisibility(View.GONE);

                if (mQsClockContainer.getParent() != null) {
                    ((ViewGroup) mQsClockContainer.getParent()).removeView(mQsClockContainer);
                }

                mQuickStatusBarHeader.addView(mQsClockContainer, mQuickStatusBarHeader.getChildCount());

                // Hide stock clock, date and carrier group
                try {
                    mOplusDate = (TextView) getObjectField(param.thisObject, "mQsDateView");
                    mStockDateTypeface = mOplusDate.getTypeface();
                } catch (Throwable t) {
                    try {
                        mOplusDate = (TextView) mQuickStatusBarHeader.findViewById(mContext.getResources().getIdentifier("oplus_date", "id", listenPackage));
                    } catch (Throwable ignored) {
                    }
                }

                try {
                    mOplusClock = (TextView) getObjectField(param.thisObject, "mClockView");
                    mStockClockTypeface = mOplusClock.getTypeface();
                } catch (Throwable t) {
                    try {
                        mOplusClock = (TextView) mQuickStatusBarHeader.findViewById(mContext.getResources().getIdentifier("qs_footer_clock", "id", listenPackage));
                    } catch (Throwable ignored) {
                    }
                }

                try {
                    mOplusCarrier = (TextView) getObjectField(param.thisObject, "mOplusQSCarrier");
                } catch (Throwable t) {
                    try {
                        mOplusCarrier = (TextView) mQuickStatusBarHeader.findViewById(mContext.getResources().getIdentifier("qs_footer_carrier_text", "id", listenPackage));
                    } catch (Throwable ignored) {
                    }
                }

                updateStockPrefs();
                setupChips();
                updateChips();
                updateClockView();
            }
        });

        hookAllMethods(OplusQSFooterImpl, "updateResources", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                updateStockPrefs();
                updateClockView();
                updateChips();
            }
        });

        Class<?> QsFragmentHelper;
        try {
            QsFragmentHelper = findClass("com.oplus.systemui.qs.helper.QSFragmentHelper", lpparam.classLoader);
        } catch (Throwable t) {
            QsFragmentHelper = findClass("com.oplusos.systemui.qs.helper.QSFragmentHelper", lpparam.classLoader);
        }

        hookAllMethods(QsFragmentHelper, "onFractionUpdated", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showHeaderClock) return;

                if (getObjectField(param.thisObject, "mQsFooterClock") == null) return;

                float f = (float) param.args[0];
                Interpolator mAlphaAnimInterpolator = (Interpolator) getObjectField(param.thisObject, "mAlphaAnimInterpolator");
                Interpolator mQsCoveredInterpolator = (Interpolator) getObjectField(param.thisObject, "mQsCoveredInterpolator");
                float interpolation = mQsCoveredInterpolator.getInterpolation(f) * 833.0f;
                float interpolation5 = mAlphaAnimInterpolator.getInterpolation(Math.min(interpolation / 500.0f, 1.0f));
                float f3 = 1.0f - interpolation5;

                mQsClockContainer.setAlpha(f3);
            }
        });

        Class<?> OplusClockExImpl;
        try {
            OplusClockExImpl = findClass("com.oplus.systemui.common.clock.OplusClockExImpl", lpparam.classLoader);
        } catch (Throwable t) {
            OplusClockExImpl = findClass("com.oplusos.systemui.ext.BaseClockExt", lpparam.classLoader); // OOS 13
        }

        hookAllMethods(OplusClockExImpl, "setTextWithRedOneStyleInternal", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                TextView textView = (TextView) param.args[0];
                if (showHeaderClock || stockClockRedStyle == 1) {
                    param.setResult(null);
                    if (showHeaderClock) {
                        textView.setText("");
                        textView.setTextColor(Color.TRANSPARENT); // Force transparent if custom clock is enabled
                    }
                    return;
                }

                if (stockClockRedStyle == 2 || stockClockRedStyle == 3) {
                    CharSequence charSequence = (CharSequence) param.args[1];
                    StringBuilder sb = new StringBuilder(charSequence);
                    int length = sb.length();
                    for (int i = 0; i < length; i++) {
                        char c = sb.charAt(i);
                        if (c == ':') {
                            sb.replace(i, i + 1, "\u200e∶");
                            break;
                        }
                    }

                    int mColorAccent = getPrimaryColor(mContext);
                    int colorToApply = stockClockRedStyle == 2 ? mColorAccent : stockClockRedOverrideColor;
                    SpannableString spannableString = new SpannableString(sb);
                    for (int i = 0; i < 2 && i < length; i++) {
                        if (sb.charAt(i) == '1') {
                            spannableString.setSpan(new ForegroundColorSpan(colorToApply), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                    }
                    textView.setText(spannableString, TextView.BufferType.SPANNABLE);
                    param.setResult(null);
                }
            }
        });


        try {
            Class<?> ShadeHeaderControllerClass = findClassIfExists("com.android.systemui.shade.LargeScreenShadeHeaderController", lpparam.classLoader);
            if (ShadeHeaderControllerClass == null)
                ShadeHeaderControllerClass = findClass("com.android.systemui.shade.ShadeHeaderController", lpparam.classLoader);

            hookAllMethods(ShadeHeaderControllerClass, "onInit", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!showHeaderClock) return;

                    try {
                        TextView clock = (TextView) getObjectField(param.thisObject, "clock");
                        ((ViewGroup) clock.getParent()).removeView(clock);
                    } catch (Throwable ignored) {
                    }

                    try {
                        TextView date = (TextView) getObjectField(param.thisObject, "date");
                        ((ViewGroup) date.getParent()).removeView(date);
                    } catch (Throwable ignored) {
                    }

                    try {
                        LinearLayout qsCarrierGroup = (LinearLayout) getObjectField(param.thisObject, "qsCarrierGroup");
                        ((ViewGroup) qsCarrierGroup.getParent()).removeView(qsCarrierGroup);
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable t) {
            log(TAG + "ShadeHeaderController error: " + t.getMessage());
        }


        Class<?> QSSecurityFooterUtilsClass;
        try {
            QSSecurityFooterUtilsClass = findClass("com.android.systemui.qs.QSSecurityFooterUtils", lpparam.classLoader);
        } catch (Throwable t) {
            QSSecurityFooterUtilsClass = findClass("com.android.systemui.qs.QSSecurityFooter", lpparam.classLoader);
        }
        hookAllConstructors(QSSecurityFooterUtilsClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mActivityStarter = getObjectField(param.thisObject, "mActivityStarter");
            }
        });


        try {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    updateStockPrefs();
                    updateClockView();
                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Throwable ignored) {
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void setupClockChip() {
        int[] colors;
        int strokeColor = Color.TRANSPARENT;
        int strokeWidth = clockStrokeWitdh;

        if (clockGradientAccent) {
            colors = new int[]{getPrimaryColor(mContext), getPrimaryColor(mContext)};
        } else if (clockUseGradient) {
            colors = new int[]{clockGradient1, clockGradient2};
        } else {
            colors = new int[]{clockGradient1, clockGradient1};
        }
        switch (clockChipStyle) {
            case 0:
                strokeWidth = 0;
                break;
            case 1:
                colors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT};
                strokeWidth = clockStrokeWitdh;
                strokeColor = clockStrokeAccent ? getPrimaryColor(mContext) : clockStrokeColor;
                break;
            case 2:
                strokeWidth = clockStrokeWitdh;
                strokeColor = clockStrokeAccent ? getPrimaryColor(mContext) : clockStrokeColor;
                break;
        }
        float[] radii;
        if (clockRoundCorners) {
            radii = new float[]{
                    dp2px(mContext, clockTopSxRound), dp2px(mContext, clockTopSxRound),
                    dp2px(mContext, clockTopDxRound), dp2px(mContext, clockTopDxRound),
                    dp2px(mContext, clockBottomDxRound), dp2px(mContext, clockBottomDxRound),
                    dp2px(mContext, clockBottomSxRound), dp2px(mContext, clockBottomSxRound)
            };
        } else {
            radii = null;
        }
        mClockChipDrawable = getChip(clockGradientOrientation, colors, strokeWidth, strokeColor, radii);
        mClockChipDrawable.invalidateSelf();
    }

    private void setupDateChip() {
        int[] colors;
        int strokeColor = Color.TRANSPARENT;
        int strokeWidth = dateStrokeWidth;

        if (dateGradientAccent) {
            colors = new int[]{getPrimaryColor(mContext), getPrimaryColor(mContext)};
        } else if (dateUseGradient) {
            colors = new int[]{dateGradient1, dateGradient2};
        } else {
            colors = new int[]{dateGradient1, dateGradient1};
        }
        switch (dateChipStyle) {
            case 0:
                strokeWidth = 0;
                break;
            case 1:
                colors = new int[]{Color.TRANSPARENT, Color.TRANSPARENT};
                strokeWidth = dateStrokeWidth;
                strokeColor = dateStrokeAccent ? getPrimaryColor(mContext) : dateStrokeColor;
                break;
            case 2:
                strokeWidth = dateStrokeWidth;
                strokeColor = dateStrokeAccent ? getPrimaryColor(mContext) : dateStrokeColor;
                break;
        }
        float[] radii;
        if (dateRoundCorners) {
            radii = new float[]{
                    dp2px(mContext, dateTopSxRound), dp2px(mContext, dateTopSxRound),
                    dp2px(mContext, dateTopDxRound), dp2px(mContext, dateTopDxRound),
                    dp2px(mContext, dateBottomDxRound), dp2px(mContext, dateBottomDxRound),
                    dp2px(mContext, dateBottomSxRound), dp2px(mContext, dateBottomSxRound)
            };
        } else {
            radii = null;
        }
        mDateChipDrawable = getChip(clockGradientOrientation, colors, strokeWidth, strokeColor, radii);
        mDateChipDrawable.invalidateSelf();
    }

    private void setupChips() {

        mAccent = getPrimaryColor(mContext);

        setupClockChip();
        setupDateChip();

        updateChips();

    }

    private void setupStatusChips() {
        mSystemIconsChipDrawable.setShape(GradientDrawable.RECTANGLE);
        mSystemIconsChipDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        mSystemIconsChipDrawable.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        if (systemIconRoundCorners) {
            mSystemIconsChipDrawable.setCornerRadii(new float[]{
                    dp2px(mContext, systemIconTopSxRound), dp2px(mContext, systemIconTopSxRound),
                    dp2px(mContext, systemIconTopDxRound), dp2px(mContext, systemIconTopDxRound),
                    dp2px(mContext, systemIconBottomDxRound), dp2px(mContext, systemIconBottomDxRound),
                    dp2px(mContext, systemIconBottomSxRound), dp2px(mContext, systemIconBottomSxRound)
            });
        } else {
            mSystemIconsChipDrawable.setCornerRadius(0);
        }
        mSystemIconsChipDrawable.setPadding(20, 0, 20, 0);
        if (systemIconBackgroundChipStyle == 0) {
            if (systemIconUseAccent)
                mSystemIconsChipDrawable.setColors(new int[]{mAccent, mAccent});
            else if (systemIconChipGradient)
                mSystemIconsChipDrawable.setColors(new int[]{systemIconChipGradient1, systemIconChipGradient2});
            else
                mSystemIconsChipDrawable.setColors(new int[]{systemIconChipGradient1, systemIconChipGradient1});
            mSystemIconsChipDrawable.setStroke(0, Color.TRANSPARENT);
        } else {
            mSystemIconsChipDrawable.setColors(new int[]{Color.TRANSPARENT, Color.TRANSPARENT});
            mSystemIconsChipDrawable.setStroke(clockStrokeWitdh, clockGradientAccent ? mAccent : systemIconChipGradient1);
        }
        mSystemIconsChipDrawable.invalidateSelf();
        updateStatusChips();
    }

    private void updateChips() {
        if (stockClockTimeBackgroundChip) {
            applyChip(mOplusClock);
        } else {
            removeChip(mOplusClock);
        }
        if (!stockClockHideDate && stockClockDateBackgroundChip) {
            if (mOplusDate != null) {
                mOplusDate.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
                mOplusDate.requestLayout();
            }
            applyChip(mOplusDate);
        } else {
            removeChip(mOplusDate);
        }
    }

    private void hideView(TextView textView) {
        if (textView == null) return;
        try {
            if (textView.getVisibility() != View.VISIBLE) return;
            textView.setVisibility(View.INVISIBLE);
            textView.setTextColor(Color.TRANSPARENT);
        } catch (Throwable t) {
            log(TAG + "hideView: " + t.getMessage());
        }
    }

    private void showView(TextView textView) {
        if (textView == null) return;
        try {
            if (textView.getVisibility() == View.VISIBLE) return;
            textView.setVisibility(View.VISIBLE);
            textView.setTextColor(Color.WHITE);
        } catch (Throwable t) {
            log(TAG + "showView: " + t.getMessage());
        }
    }

    private void updateStockPrefs() {
        // Custom clock visible so hide stock clock
        if (showHeaderClock) {
            hideView(mOplusDate);
            hideView(mOplusClock);
            hideView(mOplusCarrier);
            return;
        } else {
            showView(mOplusDate);
            showView(mOplusClock);
            showView(mOplusCarrier);
        }

        if (customFontEnabled) {
            Typeface typeface = null;
            String customFontFile = Environment.getExternalStorageDirectory() + "/.oxygen_files/headerclock_font.ttf";
            if ((new File(customFontFile).exists()))
                typeface = Typeface.createFromFile(new File(customFontFile));
            mOplusClock.setTypeface(typeface);
            mOplusDate.setTypeface(typeface);
        } else {
            if (mStockClockTypeface != null)
                mOplusClock.setTypeface(mStockClockTypeface);
            if (mStockDateTypeface != null)
                mOplusDate.setTypeface(mStockDateTypeface);
        }

        // Stock clock hide date
        if (showHeaderClock || stockClockHideDate) {
            hideView(mOplusDate);
        } else {
            showView(mOplusDate);
        }

        if (showHeaderClock || stockClockHideCarrier) {
            hideView(mOplusCarrier);
        } else {
            showView(mOplusCarrier);
        }

        setupStockColors();

    }

    private void setupStockColors() {
        boolean nightMode = mContext.getResources().getConfiguration().isNightModeActive();
        int textColor = nightMode ? Color.WHITE : Color.BLACK;
        if (mOplusClock != null) {
            if (stockClockTimeColorSwitch) {
                mOplusClock.setTextColor(stockClockTimeColor);
            } else {
                mOplusClock.setTextColor(textColor);
            }
        }

        if (mOplusCarrier != null) {
            if (!stockClockHideDate && stockClockDateColorSwitch) {
                mOplusDate.setTextColor(stockClockDateColor);
            } else if (!stockClockHideDate) {
                mOplusDate.setTextColor(textColor);
            }
        }

    }

    private void removeChip(TextView textView) {
        if (textView == null || textView.getVisibility() != View.VISIBLE) return;
        try {
            textView.setBackground(null);
        } catch (Throwable t) {
            log(TAG + "removeChip: " + t.getMessage());
        }
    }

    private void applyChip(TextView textView) {
        if (textView == null || textView.getVisibility() != View.VISIBLE) return;
        try {
            textView.setBackground(textView == mOplusClock ? mClockChipDrawable : mDateChipDrawable);
        } catch (Throwable t) {
            log(TAG + "applyChip: " + t.getMessage());
        }
    }

    private void updateClockView() {
        if (mQsClockContainer == null) return;

        if (!showHeaderClock) {
            mQsClockContainer.setVisibility(View.GONE);
            return;
        }

        View clockView = getClockView();

        if (mQsClockContainer.findViewWithTag(OC_HEADER_CLOCK_TAG) != null) {
            mQsClockContainer.removeView(mQsClockContainer.findViewWithTag(OC_HEADER_CLOCK_TAG));
        }

        if (clockView != null) {
            if (centeredClockView) {
                mQsClockContainer.setGravity(Gravity.CENTER);
            } else {
                mQsClockContainer.setGravity(Gravity.START);
            }
            clockView.setTag(OC_HEADER_CLOCK_TAG);

            TextUtil.convertTextViewsToTitleCase((ViewGroup) clockView);

            mQsClockContainer.addView(clockView);
            modifyClockView(clockView);
        }

        try {
            setOnClickListener(mQsClockContainer);
        } catch (Throwable ignored) {
        }

        mQsClockContainer.setVisibility(View.VISIBLE);
        mQsClockContainer.setAlpha(1.0f);

        //hideView(mOplusCarrier);
    }

    private void setOnClickListener(View view) {
        if (view == null) return;

        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                String tag = child.getTag() == null ? "" : child.getTag().toString();

                if (tag.toLowerCase(Locale.getDefault()).contains("clock") ||
                        tag.toLowerCase(Locale.getDefault()).contains("date")) {
                    child.setOnClickListener(clickListener);
                }

                if (child instanceof ViewGroup) {
                    setOnClickListener(child);
                }
            }
        } else {
            String tag = view.getTag() == null ? "" : view.getTag().toString();

            if (tag.toLowerCase(Locale.getDefault()).contains("clock") ||
                    tag.toLowerCase(Locale.getDefault()).contains("date")) {
                view.setOnClickListener(clickListener);
            }
        }
    }

    private void modifyClockView(View clockView) {
        String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/header_clock_font.ttf";
        boolean nightMode = mContext.getResources().getConfiguration().isNightModeActive();
        int textColor = nightMode ? Color.WHITE : Color.BLACK;

        Typeface typeface = null;
        if (customFontEnabled && (new File(customFont).exists()))
            typeface = Typeface.createFromFile(new File(customFont));

        if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == View.LAYOUT_DIRECTION_RTL) {
            ViewHelper.setMargins(clockView, mContext, 0, mTopMargin, mLeftMargin, 0);
        } else {
            ViewHelper.setMargins(clockView, mContext, mLeftMargin, mTopMargin, 0, 0);
        }

        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", mClockCustomColor ? accent1 : mAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", mClockCustomColor ? accent2 : mAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", mClockCustomColor ? accent3 : mAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", mClockCustomColor ? text1 : textColor);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", mClockCustomColor ? text2 : textColor);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "backgroundAccent", mClockCustomColor ? accent1 : mAccent);

        if (typeface != null) {
            ViewHelper.applyFontRecursively((ViewGroup) clockView, typeface);
        }

        if (mClockTextScale != 1.0f) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, mClockTextScale);
        }

        switch (clockStyle) {
            case 6 -> {
                ImageView imageView = clockView.findViewById(R.id.user_profile_image);
                imageView.setImageDrawable(mClockCustomUserImage ? getCustomUserImage() : getUserImage());
            }
        }
    }

    @SuppressWarnings("all")
    private Drawable getUserImage() {
        if (mUserManager == null) {
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }

        try {
            Method getUserIconMethod = mUserManager.getClass().getMethod("getUserIcon", int.class);
            int userId = (int) UserHandle.class.getDeclaredMethod("myUserId").invoke(null);
            Bitmap bitmapUserIcon = (Bitmap) getUserIconMethod.invoke(mUserManager, userId);
            return new BitmapDrawable(mContext.getResources(), bitmapUserIcon);
        } catch (Throwable throwable) {
            log(TAG + throwable);
            return appContext.getResources().getDrawable(R.drawable.default_avatar);
        }
    }

    private Drawable getCustomUserImage() {
        try {
            ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/header_clock_user_image.png"));

            Drawable drawable = ImageDecoder.decodeDrawable(source);

            if (drawable instanceof AnimatedImageDrawable) {
                ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                ((AnimatedImageDrawable) drawable).start();
            }

            return drawable;
        } catch (Throwable ignored) {
            return ResourcesCompat.getDrawable(appContext.getResources(), R.drawable.default_avatar, appContext.getTheme());
        }
    }

    class ClickListener implements View.OnClickListener {
        public ClickListener() {
        }

        @Override
        public void onClick(View v) {
            String tag = v.getTag().toString();
            if (tag.contains(CLOCK_TAG)) {
                clockClick();
            } else if (tag.contains(DATE_TAG)) {
                dateClick();
            }
        }
    }

    private void clockClick() {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.SHOW_ALARMS");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP + Intent.FLAG_ACTIVITY_SINGLE_TOP);
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0 /* dismissShade */);
    }

    private void dateClick() {
        Intent intent = new Intent();
        if (!AppUtils.isAppInstalled(mContext, "com.google.android.calendar")) return;
        intent.setClassName("com.google.android.calendar", "com.google.android.calendar.AllInOneCalendarActivity");
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        callMethod(mActivityStarter, "postStartActivityDismissingKeyguard", intent, 0 /* dismissShade */);
    }

    private View getClockView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);

        return inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                HEADER_CLOCK_LAYOUT + clockStyle,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );
    }

    private void updateStatusChips() {
        if (mStatusIconsView == null) return;
        int paddingStartEnd = 0;
        int paddingTopBottom = 0;
        if (systemIconsChipEnabled) {
            mStatusIconsView.setBackground(mSystemIconsChipDrawable);
            paddingStartEnd = dp2px(mContext, 12);
            paddingTopBottom = dp2px(mContext, 4);
            mStatusIconsView.getLayoutParams().width = ViewGroup.LayoutParams.WRAP_CONTENT;
            mStatusIconsView.requestLayout();
        } else {
            mStatusIconsView.setBackground(null);
        }
        mStatusIconsView.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
    }

}
