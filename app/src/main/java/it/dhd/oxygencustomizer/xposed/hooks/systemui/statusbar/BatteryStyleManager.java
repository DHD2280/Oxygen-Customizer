package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findClass;
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
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_MARGINS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_MARGIN_BOTTOM;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_MARGIN_LEFT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_MARGIN_RIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.CUSTOM_BATTERY_MARGIN_TOP;
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

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
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
import it.dhd.oxygencustomizer.xposed.utils.ShellUtils;

public class BatteryStyleManager extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final String TAG = "Oxygen Customizer - " + BatteryStyleManager.class.getSimpleName() + ": ";

    private TextView batteryPercentInView, batteryPercentOutView;
    private static final ArrayList<View> batteryViews = new ArrayList<>();
    private static final int BatteryIconOpacity = 100;
    private static int BatteryStyle = 0;
    private static boolean mShowPercentInside = true;
    private static boolean mHidePercentage = false;
    private static boolean mHideBattery = false;
    private static int mBatteryRotation = 0;
    private static boolean CustomBatteryEnabled = false;
    private static int mBatteryScaleWidth = 20;
    private static int mBatteryScaleHeight = 20;
    private static boolean mBatteryCustomDimension = false;
    private static int mBatteryMarginLeft = 0;
    private static int mBatteryMarginTop = 0;
    private static int mBatteryMarginRight = 0;
    private static int mBatteryMarginBottom = 0;
    private static int mBatteryStockMarginLeft = 0, mBatteryStockMarginRight = 0;
    private boolean DefaultLandscapeBatteryEnabled = false;
    private int frameColor = Color.WHITE;
    private int backgroundColor = Color.WHITE;
    private int singleToneColor = Color.WHITE;
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
    private boolean mChargingIconSwitch = false;
    private int mChargingIconStyle = 0;
    private int mChargingIconML = 1;
    private int mChargingIconMR = 0;
    private int mChargingIconWH = 14;
    private boolean mIsChargingImpl = false;
    private boolean mIsCharging = false;
    private ImageView mStockChargingIcon = null, mBatteryIcon = null;
    private Object BatteryControllerImpl = null;
    private boolean updating = false;
    private boolean customizePercSize = false;
    private int mBatteryPercSize = 14;

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
        mBatteryCustomDimension = Xprefs.getBoolean(CUSTOM_BATTERY_MARGINS, false);
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
        mBatteryMarginLeft = dp2px(mContext, Xprefs.getSliderInt(CUSTOM_BATTERY_MARGIN_LEFT, mBatteryStockMarginLeft));
        mBatteryMarginTop = dp2px(mContext, Xprefs.getSliderInt(CUSTOM_BATTERY_MARGIN_TOP, 0));
        mBatteryMarginRight = dp2px(mContext, Xprefs.getSliderInt(CUSTOM_BATTERY_MARGIN_RIGHT, mBatteryStockMarginRight));
        mBatteryMarginBottom = dp2px(mContext, Xprefs.getSliderInt(CUSTOM_BATTERY_MARGIN_BOTTOM, 0));

        // Stock Style
        customizePercSize = Xprefs.getBoolean(STOCK_CUSTOMIZE_PERCENTAGE_SIZE, false);
        mBatteryPercSize = Xprefs.getSliderInt(STOCK_PERCENTAGE_SIZE, 14);

        if (Key.length > 0 && (Key[0].equals(CUSTOMIZE_BATTERY_ICON) ||
                Key[0].equals(CUSTOM_BATTERY_STYLE) ||
                Key[0].equals(CUSTOM_BATTERY_HIDE_PERCENTAGE) ||
                Key[0].equals(CUSTOM_BATTERY_LAYOUT_REVERSE) ||
                Key[0].equals(CUSTOM_BATTERY_MARGINS) ||
                Key[0].equals(CUSTOM_BATTERY_PERIMETER_ALPHA) ||
                Key[0].equals(CUSTOM_BATTERY_FILL_ALPHA) ||
                Key[0].equals(CUSTOM_BATTERY_RAINBOW_FILL_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_BLEND_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_CHARGING_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_FILL_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_FILL_GRAD_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_POWERSAVE_INDICATOR_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_POWERSAVE_FILL_COLOR) ||
                Key[0].equals(CUSTOM_BATTERY_SWAP_PERCENTAGE) ||
                Key[0].equals(CUSTOM_BATTERY_CHARGING_ICON_SWITCH) ||
                Key[0].equals(CUSTOM_BATTERY_CHARGING_ICON_STYLE) ||
                Key[0].equals(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_LEFT) ||
                Key[0].equals(CUSTOM_BATTERY_CHARGING_ICON_MARGIN_RIGHT) ||
                Key[0].equals(CUSTOM_BATTERY_CHARGING_ICON_WIDTH_HEIGHT) ||
                Key[0].equals(CUSTOM_BATTERY_MARGIN_LEFT) ||
                Key[0].equals(CUSTOM_BATTERY_MARGIN_TOP) ||
                Key[0].equals(CUSTOM_BATTERY_MARGIN_RIGHT) ||
                Key[0].equals(CUSTOM_BATTERY_MARGIN_BOTTOM) ||
                Key[0].equals(CUSTOM_BATTERY_INSIDE_PERCENTAGE) ||
                Key[0].equals(CUSTOM_BATTERY_WIDTH) ||
                Key[0].equals(CUSTOM_BATTERY_HEIGHT) ||
                Key[0].equals(STOCK_CUSTOMIZE_PERCENTAGE_SIZE) ||
                Key[0].equals(STOCK_PERCENTAGE_SIZE)
        )) {
            notifyUpdate();
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listensTo(lpparam.packageName)) return;

        Class<?> StatBatteryMeterView = findClass("com.oplus.systemui.statusbar.pipeline.battery.ui.view.StatBatteryMeterView", lpparam.classLoader);
        Class<?> BatteryIconColor = findClass("com.oplus.systemui.statusbar.pipeline.battery.ui.model.BatteryIconColor", lpparam.classLoader);

        findAndHookConstructor(BatteryIconColor,
                int.class,
                int.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        /*
                        this.singleToneColor = i;
                        this.foregroundColor = i2;
                        this.backgroundColor = i3;
                         */
                        singleToneColor = (int) param.args[0];
                        frameColor = (int) param.args[1];
                        backgroundColor = (int) param.args[2];
                    }
                });

        Class<?> BatteryControllerImplClass = findClass("com.android.systemui.statusbar.policy.BatteryControllerImpl", lpparam.classLoader);
        hookAllConstructors(BatteryControllerImplClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                BatteryControllerImpl = param.thisObject;
            }
        });

        Class<?> BatteryViewBinder = findClass("com.oplus.systemui.statusbar.pipeline.battery.ui.binder.BatteryViewBinder", lpparam.classLoader);

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

                // No need to call BatteryDataProvider or BroadcastReceiver
                // since battery view is updated automatically


                mIsCharging = isCharging();
                boolean isCharging = false;

                batteryPercentInView = (TextView) param.args[0];
                batteryPercentOutView = (TextView) param.args[1];
                Object statBatteryIcon = param.args[8];

                // Fix Charging if Battery Data Provider is not ready
                if (statBatteryIcon != null) {
                    try {
                        isCharging = (boolean) callMethod(statBatteryIcon, "getCharging");
                    } catch (Throwable t) {
                        isCharging = mIsCharging;
                    }
                }


                if (CustomBatteryEnabled) {
                    batteryPercentInView.setVisibility(View.GONE);
                    if (mHidePercentage)
                        batteryPercentOutView.setVisibility(View.GONE);
                    else {
                        batteryPercentOutView.setVisibility(View.VISIBLE);
                    }
                    BatteryDrawable mBatteryDrawable = getNewBatteryDrawable(mContext);
                    if (mBatteryDrawable == null) return;
                    LinearLayout statBatteryMeterView = (LinearLayout) param.args[2];
                    ImageView batteryIcon = (ImageView) param.args[3];
                    mBatteryIcon = (ImageView) param.args[6];
                    ImageView stdBatt = (ImageView) param.args[4];
                    mBatteryIcon.setScaleType(stdBatt.getScaleType());
                    if (mBatteryDrawable != null) {
                        if (statBatteryIcon != null) {
                            int batteryLevel = ((int) callMethod(statBatteryIcon, "getPowerLevel"));
                            mBatteryDrawable.setBatteryLevel(batteryLevel);
                        } else {
                            mBatteryDrawable.setBatteryLevel(getCurrentLevel());
                        }
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
                        mBatteryIcon.setImageDrawable(mBatteryDrawable);

                    }

                    batteryIcon.setVisibility(View.GONE);

                    scaleBatteryMeterViews(mBatteryIcon);
                    updateBatteryRotation(mBatteryIcon);
                    updateFlipper(mBatteryIcon.getParent());
                    ProgressBar progress = (ProgressBar) param.args[5];
                    progress.setProgress(0);
                    progress.setVisibility(View.GONE);
                } else {
                    batteryPercentOutView.setVisibility(View.VISIBLE);
                    if (customizePercSize) {
                        batteryPercentOutView.setTextSize(TypedValue.COMPLEX_UNIT_SP, mBatteryPercSize);
                    }
                }


                if (mChargingIconSwitch) {
                    mStockChargingIcon = (ImageView) param.args[7];
                    if (isCharging) {
                        mStockChargingIcon.setVisibility(View.VISIBLE);
                        if (mChargingIconSwitch) {
                            mStockChargingIcon.setImageDrawable(getNewChargingIcon());
                            int left = dp2px(mContext, mChargingIconML);
                            int right = dp2px(mContext, mChargingIconMR);
                            int size = dp2px(mContext, mChargingIconWH);

                            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(size, size);
                            lp.setMargins(left, 0, right, mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("battery_margin_bottom", "dimen", mContext.getPackageName())));
                            mStockChargingIcon.setLayoutParams(lp);
                        }
                    }
                }
            }
        });
    }

    private BatteryDrawable getNewBatteryDrawable(Context context) {
        BatteryDrawable mBatteryDrawable = switch (BatteryStyle) {
            case BATTERY_STYLE_CUSTOM_RLANDSCAPE -> new RLandscapeBattery(context, frameColor, true);
            case BATTERY_STYLE_CUSTOM_LANDSCAPE -> new LandscapeBattery(context, frameColor, true);
            case BATTERY_STYLE_PORTRAIT_CAPSULE -> new PortraitBatteryCapsule(context, frameColor, true);
            case BATTERY_STYLE_PORTRAIT_LORN -> new PortraitBatteryLorn(context, frameColor, true);
            case BATTERY_STYLE_PORTRAIT_MX -> new PortraitBatteryMx(context, frameColor, true);
            case BATTERY_STYLE_PORTRAIT_AIROO -> new PortraitBatteryAiroo(context, frameColor, true);
            case BATTERY_STYLE_RLANDSCAPE_STYLE_A ->
                    new RLandscapeBatteryStyleA(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_STYLE_A -> new LandscapeBatteryStyleA(context, frameColor, true);
            case BATTERY_STYLE_RLANDSCAPE_STYLE_B ->
                    new RLandscapeBatteryStyleB(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_STYLE_B -> new LandscapeBatteryStyleB(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_IOS_15 -> new LandscapeBatteryiOS15(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_IOS_16 -> new LandscapeBatteryiOS16(context, frameColor, true);
            case BATTERY_STYLE_PORTRAIT_ORIGAMI -> new PortraitBatteryOrigami(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_SMILEY -> new LandscapeBatterySmiley(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_MIUI_PILL ->
                    new LandscapeBatteryMIUIPill(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_COLOROS ->
                    new LandscapeBatteryColorOS(context, frameColor, true);
            case BATTERY_STYLE_RLANDSCAPE_COLOROS ->
                    new RLandscapeBatteryColorOS(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYA -> new LandscapeBatteryA(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYB -> new LandscapeBatteryB(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYC -> new LandscapeBatteryC(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYD -> new LandscapeBatteryD(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYE -> new LandscapeBatteryE(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYF -> new LandscapeBatteryF(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYG -> new LandscapeBatteryG(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYH -> new LandscapeBatteryH(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYI -> new LandscapeBatteryI(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYJ -> new LandscapeBatteryJ(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYK -> new LandscapeBatteryK(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYL -> new LandscapeBatteryL(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYM -> new LandscapeBatteryM(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYN -> new LandscapeBatteryN(context, frameColor, true);
            case BATTERY_STYLE_LANDSCAPE_BATTERYO -> new LandscapeBatteryO(context, frameColor, true);
            case BATTERY_STYLE_CIRCLE, BATTERY_STYLE_DOTTED_CIRCLE ->
                    new CircleBattery(context, frameColor, true);
            default -> null;
        };

        if (mBatteryDrawable != null) {
            mBatteryDrawable.setShowPercentEnabled(mShowPercentInside);
            mBatteryDrawable.setAlpha(Math.round(BatteryIconOpacity * 2.55f));
            mBatteryDrawable.setColors(frameColor, backgroundColor, singleToneColor);
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
            scaledLayoutParams.width = (int) (batteryWidth * iconScaleFactor);
            scaledLayoutParams.height = (int) (batteryHeight * iconScaleFactor);
            if (mBatteryCustomDimension) {
                scaledLayoutParams.setMargins(mBatteryMarginLeft, mBatteryMarginTop, mBatteryMarginRight, mBatteryMarginBottom);
            } else {
                scaledLayoutParams.setMargins(mBatteryStockMarginLeft, 0, mBatteryStockMarginRight, context.getResources().getDimensionPixelOffset(context.getResources().getIdentifier("battery_margin_bottom", "dimen", context.getPackageName())));
            }

            mBatteryIconView.setLayoutParams(scaledLayoutParams);
            mBatteryIconView.setVisibility(mHideBattery ? View.GONE : View.VISIBLE);
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

    private void notifyUpdate() {
        new Thread(() -> {
            try {
                while (updating) {
                    Thread.currentThread().wait(500);
                }
                updating = true;
                int currentStyle = 3;
                try {
                    currentStyle = Integer.parseInt(ShellUtils.execCommand("settings get system display_battery_style", true).successMsg);
                } catch (Throwable ignored) {}
                ShellUtils.execCommand("settings put system display_battery_style 2", true);
                Thread.sleep(750);
                ShellUtils.execCommand("settings put system display_battery_style " + currentStyle, true);

                Thread.sleep(500);
                updating = false;
            } catch (Exception ignored) {
            }
        }).start();
    }

    private void updateBatteryRotation(View mBatteryIconView) {
        mBatteryIconView.setRotation(!DefaultLandscapeBatteryEnabled && mBatteryLayoutReverse ? 180 : mBatteryRotation);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}

