package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider.isFastCharging;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider.isPowerSaving;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BlendMode;
import android.graphics.BlendModeColorFilter;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.icu.text.MeasureFormat;
import android.icu.util.Measure;
import android.icu.util.MeasureUnit;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.SuperPowerSaveObserver;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.OplusBatteryStatus;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;
import it.dhd.oxygencustomizer.xposed.views.lockscreenwidgets.devicewidgets.WidgetTicker;

public class NowBarBattery extends RelativeLayout {

    private static final String POWER_SUPPLY = "/sys/class/power_supply/";
    private static final String[] CHARGER_SUPPLIES = {"usb", "wireless", "ac", "dc"};
    private static final long INFO_REFRESH_MS = 5000;

    private final Context mContext;
    private Context appContext;

    private ImageView mChargingIcon;
    private TextView mChargingInfo;
    private int mExtraCurrent;
    private int mExtraLevel;
    private int mExtraStatus;
    private boolean mIsCharging;
    private float mTemperature;
    private String mChargingString;
    private BroadcastReceiver mBatteryReceiver;
    private boolean mBroadcastRegistered = false;

    private ValueAnimator mColorAnimator;
    private LayerDrawable mChargingIconDrawable;
    private final ActivityLauncherUtils mActivityLauncherUtils;

    private OplusBatteryStatus mOplusBatteryStatus;

    private String mChargingSpeed;
    private boolean mPowerCharged;
    private boolean mSuperPowerSave = false;

    private boolean mCustomColors = false;
    private int mColor1 = Color.parseColor("#FFFF0000"),
            mColor2 = Color.parseColor("#FFFFA500"),
            mColor3 = Color.parseColor("#FF48D5C4"),
            mColor4 = Color.parseColor("#FF4CAF50");
    private int mFastChargingColor = Color.parseColor("#FF247CFF"), mPowerSaveColor = Color.parseColor("#FFA500");
    private int mChargingIconStyle = -1;
    private int mTextColor = Color.WHITE;
    private int[] mBarColors;
    private boolean mIndicateFastCharging = false, mIndicatePowerSave = false;
    private boolean mShowVoltage = false, mShowTimeRemaining = false;
    private boolean mInfoRefreshing = false;
    private double mAverageCurrent = 0;
    private final WidgetTicker mInfoTicker = new WidgetTicker(this, INFO_REFRESH_MS, this::updateInfoText);

    private SuperPowerSaveObserver.SuperPowerSaveListener mSuperPowerSaveListener = superPowerSave -> {
        mSuperPowerSave = superPowerSave;
        updateBatteryUI();
    };

    public NowBarBattery(Context context) {
        super(context);
        mContext = context;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        init();
        initBatteryReceiver();
        if (!mBroadcastRegistered) {
            mContext.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            mBroadcastRegistered = true;
        }
        BatteryDataProvider.registerOplusBatteryCallback(this::updateOplusBatteryStats);
        SuperPowerSaveObserver.registerSuperPowerSaveCallback(mSuperPowerSaveListener);

        setOnLongClickListener(v -> {
            mActivityLauncherUtils.launchApp(new Intent(Build.VERSION.SDK_INT >= 37 ? Settings.ACTION_BATTERY_SAVER_SETTINGS : Intent.ACTION_POWER_USAGE_SUMMARY));
            return true;
        });
    }

    private void init() {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, dp2px(mContext, 72)));
        inflate(appContext, R.layout.now_bar_charging_info, this);
        mChargingIcon = (ImageView) ViewHelper.findViewWithTag(this, "charging_icon");
        mChargingInfo = (TextView) ViewHelper.findViewWithTag(this, "charging_info");

        updateChargingIcon();
    }

    private Drawable getChargingIcon(int style) {
        return switch (style) {
            case 0 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_bold, mContext.getTheme());
            case 1 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_asus, mContext.getTheme());
            case 2 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_buddy, appContext.getTheme());
            case 3 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_evplug, appContext.getTheme());
            case 4 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_idc, appContext.getTheme());
            case 5 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_ios, appContext.getTheme());
            case 6 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_koplak, appContext.getTheme());
            case 7 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_miui, appContext.getTheme());
            case 8 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_mmk, appContext.getTheme());
            case 9 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_moto, appContext.getTheme());
            case 10 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_nokia, appContext.getTheme());
            case 11 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_plug, appContext.getTheme());
            case 12 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powercable, appContext.getTheme());
            case 13 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powercord, appContext.getTheme());
            case 14 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_powerstation, appContext.getTheme());
            case 15 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_realme, appContext.getTheme());
            case 16 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_soak, appContext.getTheme());
            case 17 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_stres, appContext.getTheme());
            case 18 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_strip, appContext.getTheme());
            case 19 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_usbcable, appContext.getTheme());
            case 20 ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_charging_xiaomi, appContext.getTheme());
            default ->
                    ResourcesCompat.getDrawable(modRes, R.drawable.ic_battery, appContext.getTheme());
        };
    }

    private void initBatteryReceiver() {
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mExtraStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 1);
                mExtraLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mExtraCurrent = calculateMaxChargingMicroWatt(intent);
                mChargingSpeed = getChargingSpeedString(mExtraStatus, mExtraLevel);
                mIsCharging = mExtraStatus == 2;
                if (!mIsCharging) {
                    mAverageCurrent = 0;
                    mChargingString = "";
                }
                mPowerCharged = mExtraStatus == 5;
                mTemperature = (float) intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10;
                updateBatteryUI();
            }
        };
    }

    public int calculateMaxChargingMicroWatt(Intent intent) {
        return calculateMaxChargingMicroWatt(intent.getIntExtra("max_charging_current", -1), intent.getIntExtra("max_charging_voltage", -1));
    }

    public int calculateMaxChargingMicroWatt(int i2, int i3) {
        if (i3 <= 0) {
            i3 = 5000000;
        }
        if (i2 > 0) {
            return (int) Math.round(i2 * 0.001d * i3 * 0.001d);
        }
        return -1;
    }


    private String getChargingSpeedString(int status, int level) {
        if (status == 2 && level < 100) {
            return "- " + mExtraCurrent + " mA";
        } else {
            return "";
        }
    }

    private int lightenColor(int color) {
        float factor = 0.3f;
        int alpha = (color >> 24) & 0xff;
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;
        red = (int) (red + (255 - red) * factor);
        green = (int) (green + (255 - green) * factor);
        blue = (int) (blue + (255 - blue) * factor);
        red = Math.max(0, Math.min(red, 255));
        green = Math.max(0, Math.min(green, 255));
        blue = Math.max(0, Math.min(blue, 255));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private int darkenColor(int color) {
        float factor = 0.8f;
        int alpha = (color >> 24) & 0xff;
        int red = (color >> 16) & 0xff;
        int green = (color >> 8) & 0xff;
        int blue = color & 0xff;
        red = (int) (red * factor);
        green = (int) (green * factor);
        blue = (int) (blue * factor);
        red = Math.max(0, Math.min(red, 255));
        green = Math.max(0, Math.min(green, 255));
        blue = Math.max(0, Math.min(blue, 255));
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }


    private int getChargingColor(int level) {
        if (mIndicateFastCharging && (isFastCharging() || (mOplusBatteryStatus != null && mOplusBatteryStatus.isFastCharge()))) {
            return mFastChargingColor;
        } else if (mIndicatePowerSave && (isPowerSaving() || mSuperPowerSave)) {
            return mPowerSaveColor;
        } else {
            if (level <= 20) {
                return mCustomColors ? mColor1 : Color.parseColor("#FFFF0000");
            } else if (level <= 30) {
                return mCustomColors ? mColor2 : Color.parseColor("#FFFFA500");
            } else if (level <= 60) {
                return mCustomColors ? mColor3 : Color.parseColor("#FF48D5C4");
            } else {
                return mCustomColors ? mColor4 : Color.parseColor("#FF4CAF50");
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (!mBroadcastRegistered) {
            mContext.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
            mBroadcastRegistered = true;
        }
        updateBatteryUI();
    }

    @Override
    public void onVisibilityAggregated(boolean isVisible) {
        super.onVisibilityAggregated(isVisible);
        if (mColorAnimator == null) return;
        if (isVisible) mColorAnimator.resume();
        else mColorAnimator.pause();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mBroadcastRegistered) {
            mContext.unregisterReceiver(mBatteryReceiver);
            mBroadcastRegistered = false;
        }
        if (mColorAnimator != null && mColorAnimator.isRunning()) {
            mColorAnimator.cancel();
        }
        mInfoTicker.stop();
        mInfoRefreshing = false;
    }

    private void animateChargingBackground(int startColor) {
        int endColor = darkenColor(startColor);
        if (mColorAnimator != null && mColorAnimator.isRunning()) {
            mColorAnimator.cancel();
        }
        mColorAnimator = ValueAnimator.ofArgb(startColor, endColor);
        mColorAnimator.setDuration(1000);
        mColorAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mColorAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mColorAnimator.addUpdateListener(animation -> {
            int animatedColor = (int) animation.getAnimatedValue();
            setBarBackground(new int[]{lightenColor(animatedColor), animatedColor, darkenColor(animatedColor)});
        });
        mColorAnimator.start();
        if (!isShown()) mColorAnimator.pause();
    }

    private void setBarBackground(int[] colors) {
        mBarColors = colors;
        NowBarBackground.apply(this, colors);
    }

    void refreshBarBackground() {
        if (mBarColors != null) setBarBackground(mBarColors);
    }

    private void updateBatteryUI() {
        int color = getChargingColor(mExtraLevel);
        boolean charging = mPowerCharged || mIsCharging;
        if (charging) {
            animateChargingBackground(color);
        } else {
            if (mColorAnimator != null && mColorAnimator.isRunning()) {
                mColorAnimator.cancel();
                mColorAnimator = null;
            }
            setBarBackground(new int[]{lightenColor(color), color, darkenColor(color)});
        }
        BlendModeColorFilter filter = new BlendModeColorFilter(color, BlendMode.SRC_IN);
        mChargingIconDrawable.getDrawable(1).setColorFilter(filter);
        updateInfoText();
        boolean refresh = charging && (mShowVoltage || mShowTimeRemaining) && isAttachedToWindow();
        if (refresh == mInfoRefreshing) return;
        mInfoRefreshing = refresh;
        if (refresh) mInfoTicker.start();
        else mInfoTicker.stop();
    }

    @SuppressLint("SetTextI18n")
    private void updateInfoText() {
        String finalText;
        if (mPowerCharged || mIsCharging) {
            if (TextUtils.isEmpty(mChargingString)) {
                finalText = getSystemUiString(mContext, "keyguard_charging_progress", NumberFormat.getPercentInstance().format(mExtraLevel / 100f)) + " " + mTemperature + "°C";
            } else {
                finalText = computePowerChargingStringIndication() + " " + mTemperature + "°C";
            }
            List<String> extras = new ArrayList<>();
            if (mShowVoltage) {
                int millivolts = readChargerMillivolts();
                if (millivolts > 0)
                    extras.add(String.format(Locale.getDefault(), "%.1f V", millivolts / 1000f));
            }
            if (mShowTimeRemaining && mIsCharging) {
                String time = formatTimeToFull();
                if (time != null) extras.add(time);
            }
            if (!extras.isEmpty()) finalText += " • " + TextUtils.join(" • ", extras);
        } else {
            finalText = mExtraLevel + "%";
        }
        if (finalText.contentEquals(mChargingInfo.getText())) return;
        mChargingInfo.setText(finalText);
        mChargingInfo.setSelected(true);
    }

    private int readChargerMillivolts() {
        for (String supply : CHARGER_SUPPLIES) {
            if (!"1".equals(readNode(POWER_SUPPLY + supply + "/online"))) continue;
            String value = readNode(POWER_SUPPLY + supply + "/voltage_now");
            if (value == null) continue;
            try {
                long voltage = Long.parseLong(value);
                if (voltage > 0) return (int) (voltage > 100000 ? voltage / 1000 : voltage);
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    private static String readNode(String path) {
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            return line == null ? null : line.trim();
        } catch (Throwable t) {
            return null;
        }
    }

    private long estimateTimeToFull(BatteryManager batteryManager) {
        if (mExtraLevel <= 0 || mExtraLevel >= 100) return -1;
        int counter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        int current = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        if (counter <= 0 || current == 0 || current == Integer.MIN_VALUE) return -1;
        double microAmps = Math.abs((double) current);
        if (microAmps < 100000) microAmps *= 1000;
        mAverageCurrent = mAverageCurrent <= 0 ? microAmps : mAverageCurrent * 0.8 + microAmps * 0.2;
        double remainingCharge = counter * (100d - mExtraLevel) / mExtraLevel;
        return Math.round(remainingCharge / mAverageCurrent * 3600000d);
    }

    private String formatTimeToFull() {
        BatteryManager batteryManager = mContext.getSystemService(BatteryManager.class);
        if (batteryManager == null) return null;
        long remaining = batteryManager.computeChargeTimeRemaining();
        if (remaining <= 0) remaining = estimateTimeToFull(batteryManager);
        if (remaining <= 0) return null;
        long total = Math.max(1, Math.round(remaining / 60000d));
        long hours = total / 60;
        long minutes = total % 60;
        MeasureFormat format = MeasureFormat.getInstance(Locale.getDefault(), MeasureFormat.FormatWidth.SHORT);
        String time;
        if (hours == 0) time = format.format(new Measure(minutes, MeasureUnit.MINUTE));
        else if (minutes == 0) time = format.format(new Measure(hours, MeasureUnit.HOUR));
        else
            time = format.formatMeasures(new Measure(hours, MeasureUnit.HOUR), new Measure(minutes, MeasureUnit.MINUTE));
        return modRes.getString(R.string.lockscreen_now_bar_battery_time_to_full, time);
    }

    protected String computePowerChargingStringIndication() {
        if (mPowerCharged) {
            return getSystemUiString(mContext, "keyguard_charge_full");
        }
        return getChargingStringByTechnology(mContext, NumberFormat.getPercentInstance().format(mExtraLevel / 100f));
    }

    private void updateOplusBatteryStats(OplusBatteryStatus oplusBatteryStatus) {
        mOplusBatteryStatus = oplusBatteryStatus;
        mChargingString = getChargingStringByTechnology(mContext, NumberFormat.getPercentInstance().format(mExtraLevel / 100f));
        updateBatteryUI();
    }

    public final String getChargingStringByTechnology(Context context, String str) {
        String string;
        if (mOplusBatteryStatus == null) return getSystemUiString(context, "keyguard_charging_progress", str);
        if (mOplusBatteryStatus.isHeatStateActive()) {
            string = getSystemUiString(context, "arctic_mode_started");
        } else if (mOplusBatteryStatus.isWirelessVoocCharge()) {
            string = getSystemUiString(context, "keyguard_charging_progress_airvooc", str);
        } else if (mOplusBatteryStatus.isWirelessNormalCharge()) {
            string = getSystemUiString(context, "keyguard_wireless_charging_progress", str);
        } else if (mOplusBatteryStatus.isSuperVoocCharge150W()) {
            string = getSystemUiString(context, "keyguard_charging_progress_supervooc", str);
        } else if (mOplusBatteryStatus.isSuperVoocCharge()) {
            string = getSystemUiString(context, "keyguard_charging_progress_supervooc", str);
        } else if (mOplusBatteryStatus.isVoocCharge()) {
            string = getSystemUiString(context, "keyguard_charging_progress_vooc", str);
        } else {
            string = mOplusBatteryStatus.isFastCharge() ? getSystemUiString(context, "keyguard_charging_progress_fastcharge", str) : getSystemUiString(context, "keyguard_charging_progress", str);
        }
        return string;
    }

    private String getSystemUiString(Context context, String identifier) {
        return context.getString(
                context.getResources().getIdentifier(
                        identifier,
                        "string",
                        SYSTEM_UI)
        );
    }

    private String getSystemUiString(Context context, String identifier, String arg) {
        return context.getString(
                context.getResources().getIdentifier(
                        identifier,
                        "string",
                        SYSTEM_UI),
                arg
        );
    }

    public void setBatteryBarOptions(
            int chargingIconStyle,
            boolean customColors, int color1, int color2, int color3, int color4,
            boolean indicateFast, int fastColor, boolean indicatePowerSave, int powerSaveColor,
            int textColor, boolean showVoltage, boolean showTimeRemaining
    ) {
        this.mShowVoltage = showVoltage;
        this.mShowTimeRemaining = showTimeRemaining;
        if (mChargingIconStyle != chargingIconStyle) {
            this.mChargingIconStyle = chargingIconStyle;
            updateChargingIcon();
        }
        this.mCustomColors = customColors;
        this.mColor1 = color1;
        this.mColor2 = color2;
        this.mColor3 = color3;
        this.mColor4 = color4;
        this.mIndicateFastCharging = indicateFast;
        this.mFastChargingColor = fastColor;
        this.mIndicatePowerSave = indicatePowerSave;
        this.mPowerSaveColor = powerSaveColor;
        if (mTextColor != textColor) {
            this.mTextColor = textColor;
            mChargingInfo.setTextColor(mTextColor);
            updateChargingIcon();
        }
        updateBatteryUI();
    }

    private void updateChargingIcon() {
        int roundSize = (int) modRes.getDimension(R.dimen.nowbar_album_art_size);
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(roundSize);
        background.setColor(mTextColor);
        Drawable chargingIcon = getChargingIcon(mChargingIconStyle);
        mChargingIconDrawable = new LayerDrawable(new Drawable[]{background, chargingIcon});
        mChargingIcon.setImageDrawable(mChargingIconDrawable);
    }

}
