package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_CIRCLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_CUSTOM_LANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_CUSTOM_RLANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DEFAULT_LANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DEFAULT_RLANDSCAPE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DOTTED_CIRCLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYA;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYB;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYC;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYD;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYF;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYG;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYJ;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYM;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_BATTERYO;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_COLOROS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_IOS_15;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_IOS_16;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_MIUI_PILL;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_SMILEY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_STYLE_A;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_LANDSCAPE_STYLE_B;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_PORTRAIT_AIROO;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_PORTRAIT_CAPSULE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_PORTRAIT_LORN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_PORTRAIT_MX;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_PORTRAIT_ORIGAMI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_RLANDSCAPE_COLOROS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_RLANDSCAPE_STYLE_A;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_RLANDSCAPE_STYLE_B;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOMIZE_BATTERY_ICON;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_BLEND_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_FILL_ALPHA;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_FILL_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_FILL_GRAD_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HIDE_BATTERY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_HIDE_PERCENTAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_INSIDE_PERCENTAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_LAYOUT_REVERSE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_PERIMETER_ALPHA;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_POWERSAVE_FILL_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_RAINBOW_FILL_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_SWAP_PERCENTAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_WIDTH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.STOCK_CUSTOMIZE_PERCENTAGE_SIZE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.STOCK_PERCENTAGE_SIZE;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider.getCurrentLevel;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider.isCharging;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider.isFastCharging;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider.isPowerSaving;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getChargingColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.setMargins;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.batterystyles.BatteryDrawable;
import it.dhd.oxygencustomizer.xposed.batterystyles.CircleBattery;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBattery;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryA;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryB;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryC;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryColorOS;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryD;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryE;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryF;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryG;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryH;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryI;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryJ;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryK;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryL;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryM;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryMIUIPill;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryN;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryO;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatterySmiley;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryStyleA;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryStyleB;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryiOS15;
import it.dhd.oxygencustomizer.xposed.batterystyles.LandscapeBatteryiOS16;
import it.dhd.oxygencustomizer.xposed.batterystyles.PortraitBatteryAiroo;
import it.dhd.oxygencustomizer.xposed.batterystyles.PortraitBatteryCapsule;
import it.dhd.oxygencustomizer.xposed.batterystyles.PortraitBatteryLorn;
import it.dhd.oxygencustomizer.xposed.batterystyles.PortraitBatteryMx;
import it.dhd.oxygencustomizer.xposed.batterystyles.PortraitBatteryOrigami;
import it.dhd.oxygencustomizer.xposed.batterystyles.RLandscapeBattery;
import it.dhd.oxygencustomizer.xposed.batterystyles.RLandscapeBatteryColorOS;
import it.dhd.oxygencustomizer.xposed.batterystyles.RLandscapeBatteryStyleA;
import it.dhd.oxygencustomizer.xposed.batterystyles.RLandscapeBatteryStyleB;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider;

public class BatteryStyleManager extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final String TAG = "Oxygen Customizer - " + BatteryStyleManager.class.getSimpleName() + ": ";
    private static final boolean DEBUG = false;

    private TextView batteryPercentInView, batteryPercentOutView;
    private static final ArrayList<View> batteryViews = new ArrayList<>();
    private static final int BatteryIconOpacity = 100;
    private static int BatteryStyle = 0;
    public static int mBatteryStyle = 0;
    private static boolean mShowPercentInside = true;
    private static boolean mHidePercentage = false;
    private static boolean mHideBattery = false;
    private static int mBatteryRotation = 0;
    private static boolean CustomBatteryEnabled = false;
    private static int mBatteryScaleWidth = 20;
    private static int mBatteryScaleHeight = 20;
    private static int mBatteryStockMarginLeft = 0, mBatteryStockMarginRight = 0;
    private boolean DefaultLandscapeBatteryEnabled = false;
    private static int frameColor = Color.WHITE;
    private static int backgroundColor = Color.WHITE;
    private static int singleToneColor = Color.WHITE;
    private boolean mBatteryLayoutReverse = false;
    private boolean mScaledPerimeterAlpha = false;
    private boolean mScaledFillAlpha = false;
    private boolean mRainbowFillColor = false;
    private boolean mCustomBlendColor = false;
    private int mCustomChargingColor = Color.BLACK;
    private int mCustomFillColor = Color.BLACK;
    private int mCustomFillGradColor = Color.BLACK;
    private int mCustomPowerSaveColor = Color.BLACK;
    private int mCustomPowerSaveFillColor = Color.BLACK;
    private int mCustomFastChargingColor = Color.BLACK;
    private boolean mSwapPercentage = false;
    private static boolean mChargingIconSwitch = false;
    private int mChargingIconStyle = 0;
    private int mChargingIconML = 1;
    private int mChargingIconMR = 0;
    private int mChargingIconWH = 14;
    private static boolean mIsPowerSaving = false;
    private static boolean mIsCharging = false;
    private ImageView mStockChargingIcon = null, mBatteryIcon = null;
    private boolean updating = false;
    private boolean customizePercSize = false;
    private int mBatteryPercSize = 12;
    private static boolean oos13 = false;
    private Class<?> DarkIconDispatcher = null;
    private Class<?> DualToneHandler = null;
    private View mQsBattery = null;

    public BatteryStyleManager(Context context) {
        super(context);

        mBatteryStockMarginLeft = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("op_status_bar_battery_icon_margin_left", "dimen", mContext.getPackageName()));
        mBatteryStockMarginRight = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("op_status_bar_battery_icon_margin_right", "dimen", mContext.getPackageName()));
    }

    @Override
    public void updatePrefs(String... Key) {

        BatteryStyle = Integer.parseInt(Xprefs.getString(CUSTOM_BATTERY_STYLE, String.valueOf(BATTERY_STYLE_CUSTOM_RLANDSCAPE)));
        boolean hidePercentage = Xprefs.getBoolean(CUSTOM_BATTERY_HIDE_PERCENTAGE, false);
        boolean defaultInsidePercentage = BatteryStyle == BATTERY_STYLE_LANDSCAPE_IOS_16 ||
                BatteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYL ||
                BatteryStyle == BATTERY_STYLE_LANDSCAPE_BATTERYM;
        boolean insidePercentage = (defaultInsidePercentage ||
                Xprefs.getBoolean(CUSTOM_BATTERY_INSIDE_PERCENTAGE, false));

        DefaultLandscapeBatteryEnabled = BatteryStyle == BATTERY_STYLE_DEFAULT_LANDSCAPE ||
                BatteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE;
        CustomBatteryEnabled = Xprefs.getBoolean(CUSTOMIZE_BATTERY_ICON, false);

        if (DefaultLandscapeBatteryEnabled) {
            if (BatteryStyle == BATTERY_STYLE_DEFAULT_RLANDSCAPE) {
                mBatteryRotation = 90;
            } else {
                mBatteryRotation = 270;
            }
        } else {
            mBatteryRotation = 0;
        }

        mHidePercentage = hidePercentage || insidePercentage;
        mShowPercentInside = insidePercentage && (defaultInsidePercentage || !hidePercentage);
        mHideBattery = Xprefs.getBoolean(CUSTOM_BATTERY_HIDE_BATTERY, false);
        mBatteryLayoutReverse = Xprefs.getBoolean(CUSTOM_BATTERY_LAYOUT_REVERSE, false);
        mBatteryScaleWidth = Xprefs.getSliderInt(CUSTOM_BATTERY_WIDTH, 20);
        mBatteryScaleHeight = Xprefs.getSliderInt(CUSTOM_BATTERY_HEIGHT, 20);
        mScaledPerimeterAlpha = Xprefs.getBoolean(CUSTOM_BATTERY_PERIMETER_ALPHA, false);
        mScaledFillAlpha = Xprefs.getBoolean(CUSTOM_BATTERY_FILL_ALPHA, false);
        mRainbowFillColor = Xprefs.getBoolean(CUSTOM_BATTERY_RAINBOW_FILL_COLOR, false);
        mCustomBlendColor = Xprefs.getBoolean(CUSTOM_BATTERY_BLEND_COLOR, false);
        mCustomChargingColor = Xprefs.getInt(CUSTOM_BATTERY_CHARGING_COLOR, Color.BLACK);
        mCustomFillColor = Xprefs.getInt(CUSTOM_BATTERY_FILL_COLOR, Color.BLACK);
        mCustomFillGradColor = Xprefs.getInt(CUSTOM_BATTERY_FILL_GRAD_COLOR, Color.BLACK);
        mCustomPowerSaveColor = Xprefs.getInt(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR, Color.BLACK);
        mCustomPowerSaveFillColor = Xprefs.getInt(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR, Color.BLACK);
        mSwapPercentage = Xprefs.getBoolean(CUSTOM_BATTERY_SWAP_PERCENTAGE, false);
        mChargingIconSwitch = Xprefs.getBoolean(CUSTOM_BATTERY_CHARGING_ICON_SWITCH, false);
        mChargingIconStyle = Integer.parseInt(Xprefs.getString(CUSTOM_BATTERY_CHARGING_ICON_STYLE, "0"));
        mChargingIconML = Xprefs.getSliderInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT, 1);
        mChargingIconMR = Xprefs.getSliderInt(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT, 0);
        mChargingIconWH = Xprefs.getSliderInt(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT, 14);

        // Stock Style
        customizePercSize = Xprefs.getBoolean(STOCK_CUSTOMIZE_PERCENTAGE_SIZE, false);
        mBatteryPercSize = Xprefs.getSliderInt(STOCK_PERCENTAGE_SIZE, 12);

        if (mBatteryStyle != BatteryStyle && !oos13) {
            mBatteryStyle = BatteryStyle;
            for (View view : batteryViews) //destroy old drawables and make new ones :D
            {

                ImageView batteryIcon = null, chargingIcon = null;
                TextView batteryOutPercentage = null, batteryInPercentage = null;
                if (view instanceof LinearLayout) {
                    try {
                        batteryIcon = view.findViewById(mContext.getResources().getIdentifier("battery_icon_view", "id", mContext.getPackageName()));
                    } catch (Throwable ignored) {
                    }
                }

                if (CustomBatteryEnabled && batteryIcon != null) {
                    BatteryDrawable newDrawable = getNewBatteryDrawable(mContext);
                    batteryIcon.setImageDrawable(newDrawable);
                    setAdditionalInstanceField(view, "mBatteryDrawable", newDrawable);
                }
            }
        }

        if (!oos13) refreshAllBatteryIcons();
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listensTo(lpparam.packageName)) return;

        Class<?> QuickStatusBarHeader;
        try { QuickStatusBarHeader = findClass("com.oplus.systemui.qs.OplusQuickStatusBarHeader", lpparam.classLoader);
        } catch (Throwable t) {
            QuickStatusBarHeader = findClass("com.android.systemui.qs.QuickStatusBarHeader", lpparam.classLoader);
        }

        hookAllMethods(QuickStatusBarHeader, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    mQsBattery = (View) getObjectField(param.thisObject, "mBatteryView");
                } catch (Throwable t) {
                    mQsBattery = null;
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 34) {
            hookBattery(lpparam); // OOS 14
        } else {
            hookBattery13(lpparam); // OOS 13
        }

    }

    private void hookBattery(XC_LoadPackage.LoadPackageParam lpparam) {

        BatteryDataProvider.registerInfoCallback(this::refreshAllBatteryIcons);

        DarkIconDispatcher = findClass("com.android.systemui.plugins.DarkIconDispatcher", lpparam.classLoader);
        DualToneHandler = findClass("com.android.systemui.DualToneHandler", lpparam.classLoader);

        final View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                batteryViews.add(v);
                updateBatteryViewValues(v);
            }

            @Override
            public void onViewDetachedFromWindow(@NonNull View v) {
                if (DEBUG) log(TAG + "BatteryMeter detached from window");
                batteryViews.remove(v);
            }
        };

        Class<?> StatBatteryMeterView = findClass("com.oplus.systemui.statusbar.pipeline.battery.ui.view.StatBatteryMeterView", lpparam.classLoader);
        hookAllConstructors(StatBatteryMeterView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                if (DEBUG) log(TAG + "StatBatteryMeterView constructor called");

                ((View) param.thisObject).addOnAttachStateChangeListener(listener);

                if (!CustomBatteryEnabled) return;

                BatteryDrawable mBatteryDrawable = getNewBatteryDrawable(mContext);
                setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", mBatteryDrawable);

            }
        });
        findAndHookMethod(StatBatteryMeterView, "onFinishInflate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //((View) param.thisObject).addOnAttachStateChangeListener(listener);

                if (DEBUG) log(TAG + "StatBatteryMeterView onFinishInflate called");

                if (!CustomBatteryEnabled) return;

                LinearLayout batteryMeterView = (LinearLayout) param.thisObject;

                ImageView mBatteryIconView = batteryMeterView.findViewById(mContext.getResources().getIdentifier("battery_icon_view", "id", mContext.getPackageName()));

                BatteryDrawable mBatteryDrawable = getNewBatteryDrawable(mContext);
                setAdditionalInstanceField(param.thisObject, "mBatteryDrawable", mBatteryDrawable);
                mBatteryIconView.setImageDrawable(mBatteryDrawable);
                mBatteryIconView.requestLayout();
                mBatteryIconView.invalidate();
                refreshAllBatteryIcons();
            }
        });

        hookAllMethods(StatBatteryMeterView, "onDarkChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //((View) param.thisObject).addOnAttachStateChangeListener(listener);

                /*
                public void onDarkChanged(@NotNull ArrayList<Rect> areas, float f, int i) {
                    Intrinsics.checkNotNullParameter(areas, "areas");
                    this.darkIntensity = f;
                    this.tmpRectList.clear();
                    this.tmpRectList.addAll(areas);
                    if (!DarkIconDispatcher.isInAreas(areas, this)) {
                        f = 0.0f;
                    }
                    BatteryViewBinder.Binding binding = this.binding;
                    if (binding != null) {
                        binding.darkIntensityChanged(f);
                    }
                }
                 */
                if (!CustomBatteryEnabled) return;
                ArrayList<Rect> areas = (ArrayList<Rect>) param.args[0];
                float darkIntensity = (float) param.args[1];
                boolean isInAreas = (boolean) callStaticMethod(DarkIconDispatcher, "isInAreas", areas, param.thisObject);
                if (!isInAreas) {
                    darkIntensity = 0.0f;
                }
                View v = (View) param.thisObject;
                Context c = v.getContext();
                Object dualToneHandler = DualToneHandler.getConstructor(Context.class).newInstance(c);
                // final DualToneHandler dualToneHandler = new DualToneHandler(context);
                int singleToneColor = (int) callMethod(dualToneHandler, "getSingleColor", darkIntensity);
                int foregroundColor = (int) callMethod(dualToneHandler, "getFillColor", darkIntensity);
                int backgroundColor = (int) callMethod(dualToneHandler, "getBackgroundColor", darkIntensity);

                boolean isOS = (BatteryStyle == BATTERY_STYLE_LANDSCAPE_IOS_15 || BatteryStyle == BATTERY_STYLE_LANDSCAPE_IOS_16);
                boolean nightMode = (c.getResources().getConfiguration().uiMode
                        & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
                int iosColorForeground = nightMode ? Color.WHITE : Color.BLACK;

                if (mQsBattery != null && v == mQsBattery) {
                    updateIconColor(v, singleToneColor, isOS ? iosColorForeground : foregroundColor, isOS ? Color.parseColor("#ff4c4c4c") : backgroundColor);
                } else {
                    updateIconColor(v, singleToneColor, foregroundColor, backgroundColor);
                }
                updateBatteryViewValues(v);

            }
        });


        Class<?> BatteryViewBinder = findClass("com.oplus.systemui.statusbar.pipeline.battery.ui.binder.BatteryViewBinder", lpparam.classLoader);
        hookAllMethods(BatteryViewBinder, "bind", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (DEBUG) log(TAG + "BatteryViewBinder bind called");
                if (param.args[0] instanceof View v) updateBatteryViewValues(v);
            }
        });
        hookAllMethods(BatteryViewBinder, "bind$initView", new XC_MethodHook() {

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                /*
                stock method
                public static final void bind$initView(
                0   TextView textView,
                1   TextView textView2,
                2   StatBatteryMeterView statBatteryMeterView,
                3   ImageView imageView,
                4   ImageView imageView2,
                5   ProgressBar progressBar,
                6   ImageView imageView3,
                7   TwoBatteryDashChargeView twoBatteryDashChargeView,
                8   StatBatteryIcon statBatteryIcon) {
                 */
                if (DEBUG) log(TAG + "BatteryViewBinder bind$initView called");
                if (param.args[2] instanceof View v) {
                    Object statBattery = param.args[2];
                    if (statBattery.getClass().getCanonicalName().equals(StatBatteryMeterView.getCanonicalName())) {
                        updateBatteryViewValues(v);
                    }
                }
            }
        });
    }

    private void refreshAllBatteryIcons() {
        for (View view : batteryViews) {
            view.post(() -> updateBatteryViewValues(view));
        }
    }

    private void updateBatteryViewValues(View view) {
        if (DEBUG) log(TAG + "updateBatteryViewValues called");
        TextView batteryOutPercentage = null;
        try {
            batteryOutPercentage = view.findViewById(mContext.getResources().getIdentifier("battery_percentage_view", "id", mContext.getPackageName()));
        } catch (Throwable ignored) {
            if (DEBUG) log(TAG + "battery_percentage_view not found");
        }
        if (batteryOutPercentage != null && batteryOutPercentage.getVisibility() == View.VISIBLE && !CustomBatteryEnabled) {
            batteryOutPercentage.setTextSize(TypedValue.COMPLEX_UNIT_SP, customizePercSize ? mBatteryPercSize : 12);
        }
        if (!CustomBatteryEnabled) return;
        //setPercentViewColor(view, forcePercentageColor);
        ImageView batteryIcon = null, chargingIcon = null;
        TextView batteryInPercentage = null;
        if (view instanceof LinearLayout) {
            try {
                batteryIcon = view.findViewById(mContext.getResources().getIdentifier("battery_icon_view", "id", mContext.getPackageName()));
            } catch (Throwable ignored) {
                if (DEBUG) log(TAG + "battery_icon_view not found");
            }
            try {
                chargingIcon = view.findViewById(mContext.getResources().getIdentifier("battery_dash_charge_view", "id", mContext.getPackageName()));
            } catch (Throwable ignored) {
                if (DEBUG) log(TAG + "battery_dash_charge_view not found");
            }
            try {
                batteryInPercentage = view.findViewById(mContext.getResources().getIdentifier("battery_text", "id", mContext.getPackageName()));
                batteryInPercentage.setVisibility(View.GONE);
            } catch (Throwable ignored) {
                if (DEBUG) log(TAG + "battery_text not found");
            }
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View child = ((ViewGroup) view).getChildAt(i);
                if (child instanceof FrameLayout) {
                    child.setVisibility(View.GONE);
                } else if (child instanceof TextView tv) {
                    if (tv.getText().toString().contains("%")) {
                        batteryOutPercentage = tv;
                    }
                } else {
                    if (child != batteryIcon && child != batteryOutPercentage && child != batteryInPercentage) {
                        child.setVisibility(View.GONE);
                    }
                }
            }
        }
        if (CustomBatteryEnabled && batteryIcon != null) {
            if (DEBUG) log(TAG + "CustomBatteryEnabled && batteryIcon != null");
            scaleBatteryMeterViews(batteryIcon);
            updateBatteryRotation(batteryIcon);
            updateFlipper(batteryIcon.getParent());
            mIsCharging = isCharging();
            mIsPowerSaving = isPowerSaving();
            batteryIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (batteryOutPercentage != null) {
                TextView finalBatteryOutPercentage = batteryOutPercentage;
                if (mHidePercentage) {
                    batteryOutPercentage.post(() -> finalBatteryOutPercentage.setVisibility(View.GONE));
                } else {
                    batteryOutPercentage.post(() -> finalBatteryOutPercentage.setVisibility(View.VISIBLE));
                }
            }
            try {
                BatteryDrawable drawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
                drawable.setChargingEnabled(mIsCharging, isFastCharging());
                drawable.setPowerSavingEnabled(isPowerSaving());
                drawable.setShowPercentEnabled(mShowPercentInside);
                drawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                //drawable.setColors(frameColor, backgroundColor, singleToneColor);
                drawable.customizeBatteryDrawable(
                        mBatteryLayoutReverse,
                        mScaledPerimeterAlpha,
                        mScaledFillAlpha,
                        mCustomBlendColor,
                        mRainbowFillColor,
                        mCustomFillColor,
                        mCustomFillGradColor,
                        mCustomBlendColor ? mCustomChargingColor : getChargingColor(mCustomChargingColor),
                        mCustomBlendColor ? mCustomFastChargingColor : getChargingColor(mCustomFastChargingColor),
                        mCustomPowerSaveColor,
                        mCustomPowerSaveFillColor,
                        mChargingIconSwitch
                );
                drawable.setBatteryLevel(getCurrentLevel());
                drawable.invalidateSelf();
                batteryIcon.setImageDrawable(drawable);
            } catch (Throwable t) {
                if (DEBUG) log(TAG + "BatteryDrawable not found " + t.getMessage());
            } //it's probably the default battery. no action needed
        }


        if (mChargingIconSwitch && chargingIcon != null) {
            if (mIsCharging) {
                chargingIcon.setImageDrawable(getNewChargingIcon());
                chargingIcon.setVisibility(View.VISIBLE);
                int size = dp2px(mContext, mChargingIconWH);
                chargingIcon.setLayoutParams(new LinearLayout.LayoutParams(size, size));
                setMargins(chargingIcon, mContext, mChargingIconML, 0, mChargingIconMR, 0);
            }
            chargingIcon.setVisibility(mIsCharging ? View.VISIBLE : View.GONE);
        }
    }

    private void updateIconsColor() {
        if (DEBUG) log(TAG + "updateIconsColor " + batteryViews.size());
        if (batteryViews.isEmpty()) return;
        for (View v : batteryViews) {
            if (v instanceof ImageView) {
                Drawable drawable = ((ImageView) v).getDrawable();
                if (drawable instanceof BatteryDrawable) {
                    ((BatteryDrawable) drawable).setColors(frameColor, backgroundColor, singleToneColor);
                }
                drawable.invalidateSelf();
            } else if (v instanceof LinearLayout) {
                if (oos13) return;
                BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(v, "mBatteryDrawable");
                mBatteryDrawable.setColors(frameColor, backgroundColor, singleToneColor);
                mBatteryDrawable.invalidateSelf();
            }
        }
    }

    private void updateIconColor(View view, int singleColor, int fillColor, int bgColor) {
        if (DEBUG) log(TAG + "updateIconsColor " + batteryViews.size());
        if (batteryViews.isEmpty()) return;
        BatteryDrawable mBatteryDrawable = (BatteryDrawable) getAdditionalInstanceField(view, "mBatteryDrawable");
        mBatteryDrawable.setColors(fillColor, bgColor, singleColor);
        mBatteryDrawable.invalidateSelf();
    }

    private void hookBattery13(XC_LoadPackage.LoadPackageParam lpparam) {
        oos13 = true;
        Class<?> TwoBatteryMeterDrawable = findClass("com.oplusos.systemui.statusbar.widget.TwoBatteryMeterDrawable", lpparam.classLoader);
        Class<?> StatBatteryMeterView = findClass("com.oplusos.systemui.statusbar.widget.StatBatteryMeterView", lpparam.classLoader);
        findAndHookMethod(StatBatteryMeterView, "updateColors",
                int.class,
                int.class,
                int.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                /*
                    this.mOutlineColor = i3;
                    setColors(i, i2);
                    public void setColors(int i, int i2) {
                        int argb = Color.argb((int) (Color.alpha(i) * 0.85f), Color.red(i), Color.green(i), Color.blue(i));
                        int argb2 = Color.argb((int) (Color.alpha(i2) * 0.85f), Color.red(i2), Color.green(i2), Color.blue(i2));
                        this.mCircleBackPaint.setColor(argb2);
                        this.mCircleFrontPaint.setColor(argb);
                        this.mCircleChargingPaint.setColor(argb);
                        super.setColors(argb, argb2);
                    }
                    public void setColors(int i, int i2) {
                        this.mIconTint = i;
                        this.mFramePaint.setColor(i2);
                        this.mBoltPaint.setColor(i);
                        this.mChargeColor = i;
                        invalidateSelf();
                    }

                 */
                        backgroundColor = (int) param.args[1];
                        frameColor = (int) param.args[0];
                        singleToneColor = (int) param.args[2];
                        updateIconsColor();
                    }
                });
        final View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(@NonNull View v) {
                if (v instanceof ImageView batteryIconView) {
                    batteryViews.add(batteryIconView);
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                batteryViews.remove(v);
            }
        };
        findAndHookMethod(StatBatteryMeterView, "initViews", Context.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                View v = (View) getObjectField(param.thisObject, "mBatteryIconView");
                v.addOnAttachStateChangeListener(listener);
            }
        });
        findAndHookMethod(StatBatteryMeterView, "onBatteryLevelChanged",
                int.class,
                boolean.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                /*
                public void onBatteryLevelChanged(int i, boolean z, boolean z2) {

                TwoBatteryMeterDrawable twoBatteryMeterDrawable = this.mTwoDrawable;
                if (twoBatteryMeterDrawable != null) {
                    twoBatteryMeterDrawable.setCharging(z2);
                    twoBatteryMeterDrawable.setBatteryLevel(i);
                }
                 */
                        //batteryLevel = (int) param.args[0];
                        if (DEBUG) log(TAG + "onBatteryLevelChanged");
                        mIsCharging = (boolean) param.args[2];
                        mBatteryIcon = (ImageView) getObjectField(param.thisObject, "mBatteryIconView");
                        batteryPercentOutView = (TextView) getObjectField(param.thisObject, "batteryPercentText");
                        ImageView batteryCharge = (ImageView) getObjectField(param.thisObject, "batteryCharge");
                        int batteryLevel = (int) param.args[0];//getCurrentLevel();
                        if (CustomBatteryEnabled && mBatteryIcon != null) {

                            if (mHidePercentage)
                                batteryPercentOutView.setVisibility(View.GONE);
                            else {
                                batteryPercentOutView.setVisibility(View.VISIBLE);
                            }
                            BatteryDrawable mBatteryDrawable = getNewBatteryDrawable(mContext);
                            if (mBatteryDrawable == null) return;
                            if (mBatteryDrawable != null) {
                                mBatteryDrawable.setBatteryLevel(batteryLevel);
                                mBatteryDrawable.setChargingEnabled(mIsCharging, isFastCharging());
                                mBatteryDrawable.setPowerSavingEnabled(isPowerSaving());
                                mBatteryDrawable.setShowPercentEnabled(mShowPercentInside);
                                mBatteryDrawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
                                mBatteryDrawable.setColors(frameColor, backgroundColor, singleToneColor);
                                mBatteryDrawable.customizeBatteryDrawable(
                                        mBatteryLayoutReverse,
                                        mScaledPerimeterAlpha,
                                        mScaledFillAlpha,
                                        mCustomBlendColor,
                                        mRainbowFillColor,
                                        mCustomFillColor,
                                        mCustomFillGradColor,
                                        mCustomBlendColor ? mCustomChargingColor : getChargingColor(mCustomChargingColor),
                                        mCustomBlendColor ? mCustomFastChargingColor : getChargingColor(mCustomFastChargingColor),
                                        mCustomPowerSaveColor,
                                        mCustomPowerSaveFillColor,
                                        mChargingIconSwitch
                                );
                                mBatteryIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                                scaleBatteryMeterViews(mBatteryIcon);
                                updateBatteryRotation(mBatteryIcon);
                                updateFlipper(mBatteryIcon.getParent());
                                mBatteryIcon.setImageDrawable(mBatteryDrawable);
                            }

                        } else {
                            if (customizePercSize) {
                                if (batteryPercentOutView != null && batteryPercentOutView.getVisibility() == View.VISIBLE)
                                    batteryPercentOutView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mBatteryPercSize);
                            }
                        }

                        if (mChargingIconSwitch && batteryCharge != null) {
                            if (mIsCharging) {
                                batteryCharge.setVisibility(View.VISIBLE);
                                batteryCharge.setImageDrawable(getNewChargingIcon());
                                int left = dp2px(mContext, mChargingIconML);
                                int right = dp2px(mContext, mChargingIconMR);
                                int size = dp2px(mContext, mChargingIconWH);

                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                                lp.setMargins(left, 0, right, mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("battery_margin_bottom", "dimen", mContext.getPackageName())));
                                batteryCharge.setLayoutParams(lp);
                            } else {
                                batteryCharge.setVisibility(View.GONE);
                            }
                        }

                    }
                });
    }

    private BatteryDrawable getNewBatteryDrawable(Context context) {
        BatteryDrawable mBatteryDrawable = switch (mBatteryStyle) {
            case BATTERY_STYLE_CUSTOM_RLANDSCAPE ->
                    new RLandscapeBattery(context, frameColor);
            case BATTERY_STYLE_CUSTOM_LANDSCAPE -> new LandscapeBattery(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_CAPSULE ->
                    new PortraitBatteryCapsule(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_LORN -> new PortraitBatteryLorn(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_MX -> new PortraitBatteryMx(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_AIROO ->
                    new PortraitBatteryAiroo(context, frameColor);
            case BATTERY_STYLE_RLANDSCAPE_STYLE_A ->
                    new RLandscapeBatteryStyleA(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_STYLE_A ->
                    new LandscapeBatteryStyleA(context, frameColor);
            case BATTERY_STYLE_RLANDSCAPE_STYLE_B ->
                    new RLandscapeBatteryStyleB(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_STYLE_B ->
                    new LandscapeBatteryStyleB(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_IOS_15 ->
                    new LandscapeBatteryiOS15(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_IOS_16 ->
                    new LandscapeBatteryiOS16(context, frameColor);
            case BATTERY_STYLE_PORTRAIT_ORIGAMI ->
                    new PortraitBatteryOrigami(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_SMILEY ->
                    new LandscapeBatterySmiley(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_MIUI_PILL ->
                    new LandscapeBatteryMIUIPill(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_COLOROS ->
                    new LandscapeBatteryColorOS(context, frameColor);
            case BATTERY_STYLE_RLANDSCAPE_COLOROS ->
                    new RLandscapeBatteryColorOS(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYA ->
                    new LandscapeBatteryA(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYB ->
                    new LandscapeBatteryB(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYC ->
                    new LandscapeBatteryC(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYD ->
                    new LandscapeBatteryD(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYE ->
                    new LandscapeBatteryE(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYF ->
                    new LandscapeBatteryF(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYG ->
                    new LandscapeBatteryG(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYH ->
                    new LandscapeBatteryH(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYI ->
                    new LandscapeBatteryI(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYJ ->
                    new LandscapeBatteryJ(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYK ->
                    new LandscapeBatteryK(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYL ->
                    new LandscapeBatteryL(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYM ->
                    new LandscapeBatteryM(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYN ->
                    new LandscapeBatteryN(context, frameColor);
            case BATTERY_STYLE_LANDSCAPE_BATTERYO ->
                    new LandscapeBatteryO(context, frameColor);
            case BATTERY_STYLE_CIRCLE, BATTERY_STYLE_DOTTED_CIRCLE ->
                    new CircleBattery(context, frameColor);
            default -> null;
        };

        if (mBatteryDrawable != null) {
            if (mBatteryStyle == BATTERY_STYLE_DOTTED_CIRCLE && mBatteryDrawable instanceof CircleBattery) {
                ((CircleBattery)mBatteryDrawable).setMeterStyle(BATTERY_STYLE_DOTTED_CIRCLE);
            }
            mBatteryDrawable.setShowPercentEnabled(mShowPercentInside);
            mBatteryDrawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
            mBatteryDrawable.setColors(frameColor, backgroundColor, singleToneColor);
            mBatteryDrawable.customizeBatteryDrawable(
                    mBatteryLayoutReverse,
                    mScaledPerimeterAlpha,
                    mScaledFillAlpha,
                    mCustomBlendColor,
                    mRainbowFillColor,
                    mCustomFillColor,
                    mCustomFillGradColor,
                    mCustomBlendColor ? mCustomChargingColor : getChargingColor(mCustomChargingColor),
                    mCustomBlendColor ? mCustomFastChargingColor : getChargingColor(mCustomFastChargingColor),
                    mCustomPowerSaveColor,
                    mCustomPowerSaveFillColor,
                    mChargingIconSwitch
            );
        }

        return mBatteryDrawable;
    }

    private Drawable getNewChargingIcon() {
        return switch (mChargingIconStyle) {
            case 0 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_bold, mContext.getTheme());
            case 1 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_asus, mContext.getTheme());
            case 2 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_buddy, mContext.getTheme());
            case 3 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_evplug, mContext.getTheme());
            case 4 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_idc, mContext.getTheme());
            case 5 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_ios, mContext.getTheme());
            case 6 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_koplak, mContext.getTheme());
            case 7 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_miui, mContext.getTheme());
            case 8 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_mmk, mContext.getTheme());
            case 9 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_moto, mContext.getTheme());
            case 10 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_nokia, mContext.getTheme());
            case 11 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_plug, mContext.getTheme());
            case 12 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powercable, mContext.getTheme());
            case 13 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powercord, mContext.getTheme());
            case 14 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powerstation, mContext.getTheme());
            case 15 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_realme, mContext.getTheme());
            case 16 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_soak, mContext.getTheme());
            case 17 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_stres, mContext.getTheme());
            case 18 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_strip, mContext.getTheme());
            case 19 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_usbcable, mContext.getTheme());
            case 20 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_xiaomi, mContext.getTheme());
            default -> null;
        };
    }

    public static void scaleBatteryMeterViews(@Nullable ImageView mBatteryIconView) {
        if (mBatteryIconView == null) {
            return;
        }

        try {
            Context context = mBatteryIconView.getContext();
            Resources res = context.getResources();

            TypedValue typedValue = new TypedValue();

            res.getValue(res.getIdentifier("status_bar_icon_scale_factor", "dimen", context.getPackageName()), typedValue, true);
            float iconScaleFactor = typedValue.getFloat();

            int batteryWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBatteryScaleWidth, mBatteryIconView.getContext().getResources().getDisplayMetrics());
            int batteryHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mBatteryScaleHeight, mBatteryIconView.getContext().getResources().getDisplayMetrics());

            LinearLayout.LayoutParams scaledLayoutParams = (LinearLayout.LayoutParams) mBatteryIconView.getLayoutParams();
            if (!oos13) {
                scaledLayoutParams.width = (int) (batteryWidth * iconScaleFactor);
                scaledLayoutParams.height = (int) (batteryHeight * iconScaleFactor);
            } else {
                scaledLayoutParams.width = batteryWidth;
                scaledLayoutParams.height = batteryHeight;
            }
            mBatteryIconView.setLayoutParams(scaledLayoutParams);
            mBatteryIconView.setVisibility(mHideBattery ? View.GONE : View.VISIBLE);
            mBatteryIconView.requestLayout();
        } catch (Throwable throwable) {
            log(TAG + throwable);
        }
    }

    private void updateFlipper(Object thisObject) {
        LinearLayout batteryView = (LinearLayout) thisObject;
        batteryView.setOrientation(LinearLayout.HORIZONTAL);
        batteryView.setGravity(Gravity.CENTER_VERTICAL | Gravity.START);
        batteryView.setLayoutDirection(mSwapPercentage ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }

    private void updateBatteryRotation(View mBatteryIconView) {
        mBatteryIconView.setRotation(!DefaultLandscapeBatteryEnabled && mBatteryLayoutReverse ? 180 : mBatteryRotation);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}

