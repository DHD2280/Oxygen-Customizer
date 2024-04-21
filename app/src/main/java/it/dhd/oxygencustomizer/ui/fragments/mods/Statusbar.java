package it.dhd.oxygencustomizer.ui.fragments.mods;

import static android.content.Context.BATTERY_SERVICE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.BatteryPrefs.BATTERY_STYLE_DOTTED_CIRCLE;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.BatteryManager;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.customprefs.ListWithPopUpPreference;
import it.dhd.oxygencustomizer.customprefs.dialogadapter.ListPreferenceAdapter;
import it.dhd.oxygencustomizer.ui.base.ControlledPreferenceFragmentCompat;
import it.dhd.oxygencustomizer.utils.Constants;
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

public class Statusbar extends ControlledPreferenceFragmentCompat {
    @Override
    public String getTitle() {
        return getString(R.string.statusbar_title);
    }

    @Override
    public boolean backButtonEnabled() {
        return true;
    }

    @Override
    public int getLayoutResource() {
        return R.xml.statusbar;
    }

    @Override
    public boolean hasMenu() {
        return true;
    }

    @Override
    public String[] getScopes() {
        return new String[]{Constants.Packages.SYSTEM_UI};
    }


    public static class BatteryBar extends ControlledPreferenceFragmentCompat {
        @Override
        public String getTitle() {
            return getString(R.string.statusbar_batterybar_title);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.battery_bar_settings;
        }

        @Override
        public boolean hasMenu() {
            return false;
        }

        @Override
        public String[] getScopes() {
            return null;
        }
    }

    public static class Notifications extends ControlledPreferenceFragmentCompat {
        @Override
        public String getTitle() {
            return getString(R.string.statusbar_notifications);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.statusbar_notifications;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }
    }

    public static class Clock extends ControlledPreferenceFragmentCompat {

        @Override
        public String getTitle() {
            return getString(R.string.status_bar_clock_title);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public int getLayoutResource() {
            return R.xml.statusbar_clock;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }
    }

    public static class BatteryIcon extends ControlledPreferenceFragmentCompat {

        ListWithPopUpPreference mBatteryStyle, mChargingIconStock, mChargingIcon;

        @Override
        public String getTitle() {
            return getString(R.string.statusbar_battery_icon_options);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);

            boolean nightMode = (requireContext().getResources().getConfiguration().uiMode
                    & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
            int batteryColor = nightMode ? Color.WHITE : Color.BLACK;
            Drawable[] batteryDrawables = new Drawable[] {
                    new RLandscapeBattery(requireContext(), batteryColor, false),
                    new LandscapeBattery(requireContext(), batteryColor, false),
                    new PortraitBatteryCapsule(requireContext(), batteryColor, false),
                    new PortraitBatteryLorn(requireContext(), batteryColor, false),
                    new PortraitBatteryMx(requireContext(), batteryColor, false),
                    new PortraitBatteryAiroo(requireContext(), batteryColor, false),
                    new RLandscapeBatteryStyleA(requireContext(), batteryColor, false),
                    new LandscapeBatteryStyleA(requireContext(), batteryColor, false),
                    new RLandscapeBatteryStyleB(requireContext(), batteryColor, false),
                    new LandscapeBatteryStyleB(requireContext(), batteryColor, false),
                    new LandscapeBatteryiOS15(requireContext(), batteryColor, false),
                    new LandscapeBatteryiOS16(requireContext(), batteryColor, false),
                    new PortraitBatteryOrigami(requireContext(), batteryColor, false),
                    new LandscapeBatterySmiley(requireContext(), batteryColor, false),
                    new LandscapeBatteryMIUIPill(requireContext(), batteryColor, false),
                    new LandscapeBatteryColorOS(requireContext(), batteryColor, false),
                    new RLandscapeBatteryColorOS(requireContext(), batteryColor, false),
                    new LandscapeBatteryA(requireContext(), batteryColor, false),
                    new LandscapeBatteryB(requireContext(), batteryColor, false),
                    new LandscapeBatteryC(requireContext(), batteryColor, false),
                    new LandscapeBatteryD(requireContext(), batteryColor, false),
                    new LandscapeBatteryE(requireContext(), batteryColor, false),
                    new LandscapeBatteryF(requireContext(), batteryColor, false),
                    new LandscapeBatteryG(requireContext(), batteryColor, false),
                    new LandscapeBatteryH(requireContext(), batteryColor, false),
                    new LandscapeBatteryI(requireContext(), batteryColor, false),
                    new LandscapeBatteryJ(requireContext(), batteryColor, false),
                    new LandscapeBatteryK(requireContext(), batteryColor, false),
                    new LandscapeBatteryL(requireContext(), batteryColor, false),
                    new LandscapeBatteryM(requireContext(), batteryColor, false),
                    new LandscapeBatteryN(requireContext(), batteryColor, false),
                    new LandscapeBatteryO(requireContext(), batteryColor, false),
                    new CircleBattery(requireContext(), batteryColor, false),
                    new CircleBattery(requireContext(), batteryColor, false)
            };


            int[] chargingIcons = new int[]{
                    R.drawable.ic_charging_bold, // Bold
                    R.drawable.ic_charging_asus, // Asus
                    R.drawable.ic_charging_buddy, // Buddy
                    R.drawable.ic_charging_evplug, // EV Plug
                    R.drawable.ic_charging_idc, // IDC
                    R.drawable.ic_charging_ios, // IOS
                    R.drawable.ic_charging_koplak, // Koplak
                    R.drawable.ic_charging_miui, // MIUI
                    R.drawable.ic_charging_mmk, // MMK
                    R.drawable.ic_charging_moto, // Moto
                    R.drawable.ic_charging_nokia, // Nokia
                    R.drawable.ic_charging_plug, // Plug
                    R.drawable.ic_charging_powercable, // Power Cable
                    R.drawable.ic_charging_powercord, // Power Cord
                    R.drawable.ic_charging_powerstation, // Power Station
                    R.drawable.ic_charging_realme, // Realme
                    R.drawable.ic_charging_soak, // Soak
                    R.drawable.ic_charging_stres, // Stres
                    R.drawable.ic_charging_strip, // Strip
                    R.drawable.ic_charging_usbcable, // USB Cable
                    R.drawable.ic_charging_xiaomi, // Xiaomi
            };

            BatteryManager bm = (BatteryManager) requireContext().getSystemService(BATTERY_SERVICE);
            int batLevel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);

            for (Drawable batteryIcon : batteryDrawables) {
                ((BatteryDrawable)batteryIcon).setBatteryLevel(batLevel);
                if (batteryIcon instanceof CircleBattery && batteryIcon == batteryDrawables[batteryDrawables.length-1]) {
                    ((CircleBattery)batteryIcon).setMeterStyle(BATTERY_STYLE_DOTTED_CIRCLE);
                }
                ((BatteryDrawable)batteryIcon).setColors(batteryColor, batteryColor, batteryColor);
            }

            mBatteryStyle = findPreference("battery_icon_style");
            if (mBatteryStyle != null) {
                mBatteryStyle.createDefaultAdapter(batteryDrawables);
                mBatteryStyle.setAdapterType(ListPreferenceAdapter.TYPE_BATTERY_ICONS);
            }

            mChargingIcon = findPreference("battery_charging_icon_style");
            if (mChargingIcon != null) {
                List<String> mValues = new ArrayList<>();
                for(int i = 0; i<mChargingIcon.getEntries().length; i++) {
                    mValues.add(String.valueOf(i));
                }
                mChargingIcon.setEntryValues(mValues.toArray(new CharSequence[0]));
                mChargingIcon.setDrawables(chargingIcons);
                mChargingIcon.createDefaultAdapter();
                mChargingIcon.setAdapterType(ListPreferenceAdapter.TYPE_BATTERY_ICONS);
            }
        }

        @Override
        public int getLayoutResource() {
            return R.xml.statusbar_battery_icon;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }
    }

    public static class Icons extends ControlledPreferenceFragmentCompat {


        @Override
        public String getTitle() {
            return getString(R.string.statusbar_icons);
        }

        @Override
        public boolean backButtonEnabled() {
            return true;
        }


        @Override
        public int getLayoutResource() {
            return R.xml.statusbar_icons;
        }

        @Override
        public boolean hasMenu() {
            return true;
        }

        @Override
        public String[] getScopes() {
            return new String[]{Constants.Packages.SYSTEM_UI};
        }
    }

}
