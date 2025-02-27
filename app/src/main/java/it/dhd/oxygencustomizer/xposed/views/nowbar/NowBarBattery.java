package it.dhd.oxygencustomizer.xposed.views.nowbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.modRes;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;

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
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;

import java.text.NumberFormat;
import java.util.Locale;

import de.robv.android.xposed.XposedBridge;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.OplusBatteryStatus;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class NowBarBattery extends RelativeLayout {

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

    private ValueAnimator mColorAnimator;
    private LayerDrawable mChargingIconDrawable;
    private final ActivityLauncherUtils mActivityLauncherUtils;

    private OplusBatteryStatus mOplusBatteryStatus;

    private String mChargingSpeed;
    private boolean mPowerCharged;

    public NowBarBattery(Context context) {
        super(context);
        mContext = context;
        try {
            appContext = mContext.createPackageContext(BuildConfig.APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY);
        } catch (Throwable ignored) {}
        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
        init();
        initBatteryReceiver(context);
        mContext.registerReceiver(mBatteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        BatteryDataProvider.registerOplusBatteryCallback(this::updateOplusBatteryStats);

        setOnLongClickListener(v -> {
            mActivityLauncherUtils.launchApp(new Intent(Intent.ACTION_POWER_USAGE_SUMMARY));
            return true;
        });
    }

    private void init() {
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        inflate(appContext, R.layout.now_bar_charging_info, this);
        mChargingIcon = (ImageView) ViewHelper.findViewWithTag(this, "charging_icon");
        mChargingInfo = (TextView) ViewHelper.findViewWithTag(this, "charging_info");

        int roundSize = (int) modRes.getDimension(R.dimen.nowbar_album_art_size);
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(roundSize);
        background.setColor(Color.WHITE);
        Drawable chargingIcon = ResourcesCompat.getDrawable(modRes, R.drawable.ic_battery, appContext.getTheme());
        mChargingIconDrawable = new LayerDrawable(new Drawable[]{background, chargingIcon});
        mChargingIcon.setImageDrawable(mChargingIconDrawable);
    }

    private void initBatteryReceiver(Context context) {
        mBatteryReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                mExtraStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 1);
                mExtraLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
                mExtraCurrent = calculateMaxChargingMicroWatt(intent);
                mChargingSpeed = getChargingSpeedString(mExtraStatus, mExtraLevel);
                mIsCharging = mExtraStatus == 2;
                if (!mIsCharging) {
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
        if (level < 20) {
            return Color.parseColor("#FFFF0000");
        } else if (level < 30) {
            return Color.parseColor("#FFFFA500");
        } else if (level < 60) {
            return Color.parseColor("#FF48D5C4");
        } else {
            return Color.parseColor("#FF4CAF50");
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mContext.unregisterReceiver(mBatteryReceiver);
        if (mColorAnimator != null && mColorAnimator.isRunning()) {
            mColorAnimator.cancel();
        }
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
    }

    private void setBarBackground(int[] colors) {
        GradientDrawable background = new GradientDrawable();
        background.setCornerRadius(100f);
        background.setColors(colors);
        background.setOrientation(GradientDrawable.Orientation.LEFT_RIGHT);
        setBackground(background);
    }

    @SuppressLint("SetTextI18n")
    private void updateBatteryUI() {
        int color = getChargingColor(mExtraLevel);
        String finalText = "";
        if (mPowerCharged || mIsCharging) {
            if (TextUtils.isEmpty(mChargingString)) {
                finalText = getSystemUiString(mContext, "keyguard_charging_progress", NumberFormat.getPercentInstance().format(mExtraLevel / 100f)) + " " + mTemperature + "°C";
            } else {
                finalText = computePowerChargingStringIndication() + " " + mTemperature + "°C";
            }
            animateChargingBackground(color);
        } else {
            finalText = mExtraLevel + "%";
            if (mColorAnimator != null && mColorAnimator.isRunning()) {
                mColorAnimator.cancel();
                mColorAnimator = null;
            }
            setBarBackground(new int[]{lightenColor(color), color, darkenColor(color)});
        }
        BlendModeColorFilter filter = new BlendModeColorFilter(color, BlendMode.SRC_IN);
        mChargingIconDrawable.getDrawable(1).setColorFilter(filter);
        mChargingInfo.setText(finalText);
        mChargingInfo.setSelected(true);
    }

    protected String computePowerChargingStringIndication() {
        if (mPowerCharged) {
            return getSystemUiString(mContext, "keyguard_charge_full");
        }
        return getChargingStringByTechnology(mContext, String.valueOf(mExtraLevel));
    }

    private void updateOplusBatteryStats(OplusBatteryStatus oplusBatteryStatus) {
        mOplusBatteryStatus = oplusBatteryStatus;
        XposedBridge.log("OplusBatteryStatus changed: " + oplusBatteryStatus.toString());
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

}
