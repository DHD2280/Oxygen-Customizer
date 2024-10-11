package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.views.BatteryBarView;

public class BatteryBar extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static boolean BBarColorful;
    private static boolean BBOnlyWhileCharging;
    private static boolean BBOnBottom;
    private static boolean BBSetCentered;
    private static boolean BBAnimateCharging;
    private static int BBOpacity = 100;
    private static int BBarHeight = 10;
    private static List<Float> batteryLevels = Arrays.asList(20f, 40f);
    private static int[] batteryColors = new int[]{Color.RED, Color.YELLOW};
    private static int chargingColor = Color.WHITE;
    private static int fastChargingColor = Color.WHITE;
    private static boolean indicateCharging = false;
    private static boolean indicateFastCharging = false;
    private static boolean BBarTransitColors = false;
    private static boolean indicatePowerSave = false;
    private static int powerSaveColor = Color.GREEN;
    private FrameLayout fullStatusbar;
    private Object mStatusBarIconController;
    private ViewGroup mStatusBar;
    private Object mCollapsedStatusBarFragment = null;
    private boolean BBarEnabled;

    public BatteryBar(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        BBarEnabled = Xprefs.getBoolean("BBarEnabled", false);
        BBarColorful = Xprefs.getBoolean("BBarColorful", false);
        BBOnlyWhileCharging = Xprefs.getBoolean("BBOnlyWhileCharging", false);
        BBOnBottom = Xprefs.getBoolean("BBOnBottom", false);
        BBSetCentered = Xprefs.getBoolean("BBSetCentered", false);
        BBAnimateCharging = Xprefs.getBoolean("BBAnimateCharging", true);
        BBOpacity = Xprefs.getSliderInt("BBOpacity", 100);
        BBarHeight = Xprefs.getSliderInt("BBarHeight", 50);
        BBarTransitColors = Xprefs.getBoolean("BBarTransitColors", false);

        batteryLevels = Xprefs.getSliderValues("batteryWarningRange", 0);

        batteryColors = new int[]{
                Xprefs.getInt("batteryCriticalColor", Color.RED),
                Xprefs.getInt("batteryWarningColor", Color.YELLOW)};

        indicateFastCharging = Xprefs.getBoolean("indicateFastCharging", false);
        indicateCharging = Xprefs.getBoolean("indicateCharging", true);
        indicatePowerSave = Xprefs.getBoolean("indicatePowerSave", false);
        powerSaveColor = Xprefs.getInt("batteryPowerSaveColor", Color.GREEN);

        chargingColor = Xprefs.getInt("batteryChargingColor", Color.GREEN);
        fastChargingColor = Xprefs.getInt("batteryFastChargingColor", Color.GREEN);

        if (Key.length > 0) {
            if (Key[0].equals("BBarEnabled")) {
                if (BBarEnabled) {
                    placeBatteryBar();
                }
            }
            if (BatteryBarView.hasInstance()) {
                refreshBatteryBar(BatteryBarView.getInstance());
            }
        }


    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> CollapsedStatusBarFragmentClass = findClassIfExists("com.android.systemui.statusbar.phone.fragment.CollapsedStatusBarFragment", lpparam.classLoader);
        Class<?> PhoneStatusBarViewClass = findClass("com.android.systemui.statusbar.phone.PhoneStatusBarView", lpparam.classLoader);

        //getting statusbar class for further use
        hookAllConstructors(CollapsedStatusBarFragmentClass, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mCollapsedStatusBarFragment = param.thisObject;
            }
        });

        hookAllMethods(PhoneStatusBarViewClass, "onConfigurationChanged", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (BatteryBarView.hasInstance()) {
                            BatteryBarView.getInstance().post(() -> refreshBatteryBar(BatteryBarView.getInstance()));
                        }
                    }
                }, 2000);
            }
        });


        findAndHookMethod(CollapsedStatusBarFragmentClass,
                "onViewCreated", View.class, Bundle.class, new XC_MethodHook() {
                    @SuppressLint("DiscouragedApi")
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                        mStatusBarIconController = getObjectField(param.thisObject, "mStatusBarIconController");

                        mStatusBar = (ViewGroup) getObjectField(mCollapsedStatusBarFragment, "mStatusBar");

                        fullStatusbar = (FrameLayout) mStatusBar.getParent();


                        if (BBarEnabled) //in case we got the config but view wasn't ready yet
                        {
                            placeBatteryBar();
                        }
                    }
                });


    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }


    private void refreshBatteryBar(BatteryBarView instance) {
        BatteryBarView.setStaticColor(batteryLevels, batteryColors, indicateCharging, chargingColor, indicateFastCharging, fastChargingColor, indicatePowerSave, powerSaveColor, BBarTransitColors, BBAnimateCharging);
        instance.setVisibility((BBarEnabled) ? VISIBLE : GONE);
        instance.setColorful(BBarColorful);
        instance.setOnlyWhileCharging(BBOnlyWhileCharging);
        instance.setOnTop(!BBOnBottom);
        instance.setSingleColorTone(Color.WHITE);
        instance.setAlphaPct(BBOpacity);
        instance.setBarHeight(Math.round(BBarHeight / 10f) + 5);
        instance.setCenterBased(BBSetCentered);
        instance.refreshLayout();
    }

    private void placeBatteryBar() {
        try {
            BatteryBarView batteryBarView = BatteryBarView.getInstance(mContext);
            try {
                ((ViewGroup) batteryBarView.getParent()).removeView(batteryBarView);
            } catch (Throwable ignored) {
            }
            fullStatusbar.addView(batteryBarView);
            refreshBatteryBar(BatteryBarView.getInstance());
        } catch (Throwable ignored) {
        }
    }

}
