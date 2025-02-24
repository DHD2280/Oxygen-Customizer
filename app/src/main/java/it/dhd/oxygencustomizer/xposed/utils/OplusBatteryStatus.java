package it.dhd.oxygencustomizer.xposed.utils;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import android.content.Context;
import android.provider.Settings;

import androidx.annotation.NonNull;

import java.text.NumberFormat;
import java.util.Objects;

import de.robv.android.xposed.XposedBridge;

public class OplusBatteryStatus {

    private Object mOplusAddBatteryStatus = null;

    public OplusBatteryStatus(Object oplusAddBatteryStatus) {
        if (oplusAddBatteryStatus == null) return;
        XposedBridge.log("OplusAddBatteryStatus class: " + oplusAddBatteryStatus.getClass().getCanonicalName());

        mOplusAddBatteryStatus = oplusAddBatteryStatus;

    }

    public final boolean isPluggin() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isPluggin");
    }

    public final boolean isChargingForKeyguard() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isChargingForKeyguard");
    }

    public final boolean isChargingForKeyguardNotLowBattery() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isChargingForKeyguardNotLowBattery");
    }

    public final boolean isRealCharging() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isRealCharging");
    }

    public final boolean isClockFullCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isClockFullCharge");
    }

    public final boolean isHighOrLowTmpFull() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isHighOrLowTmpFull");
    }

    public final boolean isChargingTimeout() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isChargingTimeout");
    }

    public final boolean isFastCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isFastCharge");
    }

    public final boolean isVoocCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isVoocCharge");
    }

    public final boolean isSuperVoocCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isSuperVoocCharge");
    }

    public final boolean isSuperVoocCharge150W() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isSuperVoocCharge150W");
    }

    public final boolean isWirelessVoocCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isWirelessVoocCharge");
    }

    public final boolean isWirelessNormalCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isWirelessNormalCharge");
    }

    public final int getChargerTechnology() {
        return (int) callMethod(mOplusAddBatteryStatus, "getChargerTechnology");
    }

    public final int getChargerWattage() {
        return (int) callMethod(mOplusAddBatteryStatus, "getChargerWattage");
    }

    public final int getChargerWattageOrigin() {
        return (int) callMethod(mOplusAddBatteryStatus, "getChargerWattageOrigin");
    }

    public final boolean isWirelessCharging() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isWirelessCharging");
    }

    public final boolean isWirelessChargingReverse() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isWirelessChargingReverse");
    }

    public final boolean isHeatStateActive() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isHeatStateActive");
    }

    public final int getPlugged() {
        return (int) callMethod(mOplusAddBatteryStatus, "getPlugged");
    }

    public final boolean isFull() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "isFull");
    }

    public final boolean getBatteryCharging() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "getBatteryCharging");
    }

    public final int getLevel() {
        return (int) callMethod(mOplusAddBatteryStatus, "getLevel");
    }

    public final boolean getSupportSingleSmartCharge() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "getSupportSingleSmartCharge");
    }

    public final boolean getHighLowLevelChange() {
        return (boolean) callMethod(mOplusAddBatteryStatus, "getHighLowLevelChange");
    }

    public String getChargeInfo(Context context) {
        if (!Objects.equals(context.getPackageName(), SYSTEM_UI)) return "NON SYSTEM UI";

        int level = getLevel();
        if (!isChargingForKeyguardNotLowBattery()) {
            boolean  mBatteryIsLow = level < 20;
            return (!mBatteryIsLow || isHighOrLowTmpFull() || isChargingTimeout() || isPluggin()) ? "" :
                            getSystemUiString(
                                    context,
                                    "keyguard_low_battery");
        }
        if (isClockFullCharge()) {
            return getSystemUiString(context, "keyguard_charge_full");
        }
        if (isWirelessChargingReverse()) {
            return getSystemUiString(context, "keyguard_wireless_reverse_charging");
        }
        if (getCurrentChargeProtectionState(context)) {
            return getSystemUiString(context, "night_charging_optimization_info");
        }
        String format = NumberFormat.getPercentInstance().format(level / 100.0f);
        return getChargingStringByTechnology(context, format);
    }

    public final String getChargingStringByTechnology(Context context, String str) {
        String string;
        if (isHeatStateActive()) {
            string = getSystemUiString(context, "arctic_mode_started");
        } else if (isWirelessVoocCharge()) {
            string = getSystemUiString(context, "keyguard_charging_progress_airvooc", str);
        } else if (isWirelessNormalCharge()) {
            string = getSystemUiString(context, "keyguard_wireless_charging_progress", str);
        } else if (isSuperVoocCharge150W()) {
            string = getSystemUiString(context, "keyguard_charging_progress_supervooc", str);
        } else if (isSuperVoocCharge()) {
            string = getSystemUiString(context, "keyguard_charging_progress_supervooc", str);
        } else if (isVoocCharge()) {
            string = getSystemUiString(context, "keyguard_charging_progress_vooc", str);
        } else {
            string = isFastCharge() ? getSystemUiString(context, "keyguard_charging_progress_fastcharge", str) : getSystemUiString(context, "keyguard_charging_progress", str);
        }
        return string;
    }

    public final String getChargingStringByTechnology(Context context) {
        int level = getLevel();
        String format = NumberFormat.getPercentInstance().format(level / 100.0f);
        return getChargingStringByTechnology(context, format);
    }

    public final boolean getCurrentChargeProtectionState(Context context) {
        return Settings.System.getInt(context.getContentResolver(), "charge_protection_current_state", 0) == 1;
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

    @NonNull
    @Override
    public String toString() {
        return "OplusBatteryStatus{" +
                "isPluggin=" + isPluggin() +
                ", isChargingForKeyguard=" + isChargingForKeyguard() +
                ", isChargingForKeyguardNotLowBattery=" + isChargingForKeyguardNotLowBattery() +
                ", isRealCharging=" + isRealCharging() +
                ", isClockFullCharge=" + isClockFullCharge() +
                ", isHighOrLowTmpFull=" + isHighOrLowTmpFull() +
                ", isChargingTimeout=" + isChargingTimeout() +
                ", isFastCharge=" + isFastCharge() +
                ", isVoocCharge=" + isVoocCharge() +
                ", isSuperVoocCharge=" + isSuperVoocCharge() +
                ", isSuperVoocCharge150W=" + isSuperVoocCharge150W() +
                ", isWirelessVoocCharge=" + isWirelessVoocCharge() +
                ", isWirelessNormalCharge=" + isWirelessNormalCharge() +
                ", getChargerTechnology=" + getChargerTechnology() +
                ", getChargerWattage=" + getChargerWattage() +
                ", getChargerWattageOrigin=" + getChargerWattageOrigin() +
                ", isWirelessCharging=" + isWirelessCharging() +
                ", isWirelessChargingReverse=" + isWirelessChargingReverse() +
                ", isHeatStateActive=" + isHeatStateActive() +
                ", getPlugged=" + getPlugged() +
                ", isFull=" + isFull() +
                ", getBatteryCharging=" + getBatteryCharging() +
                ", getLevel=" + getLevel() +
                ", getSupportSingleSmartCharge=" + getSupportSingleSmartCharge() +
                ", getHighLowLevelChange=" + getHighLowLevelChange() +
                '}';
    }

}
