package it.dhd.oxygencustomizer.xposed;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;

import java.util.ArrayList;

import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.hooks.HookTester;
import it.dhd.oxygencustomizer.xposed.hooks.framework.Buttons;
import it.dhd.oxygencustomizer.xposed.hooks.framework.DarkMode;
import it.dhd.oxygencustomizer.xposed.hooks.framework.MemcEnhancer;
import it.dhd.oxygencustomizer.xposed.hooks.framework.OplusStartingWindowManager;
import it.dhd.oxygencustomizer.xposed.hooks.launcher.Launcher;
import it.dhd.oxygencustomizer.xposed.hooks.launcher.ThemedIcons;
import it.dhd.oxygencustomizer.xposed.hooks.settings.CustomShortcut;
import it.dhd.oxygencustomizer.xposed.hooks.settings.DarkModeSettings;
import it.dhd.oxygencustomizer.xposed.hooks.settings.MemcEnabler;
import it.dhd.oxygencustomizer.xposed.hooks.settings.OtaCard;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AdaptivePlayback;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AdvancedReboot;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.AudioDataProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.BatteryDataProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.CaffeineTile;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.FeatureOption;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.FluidMusic;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.MediaPlayerObserver;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.MiscMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.PulseViewHook;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.SettingsLibUtilsProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThemeEnabler;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.ThermalProvider;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.VolumePanel;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.aod.AodClock;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.aod.AodWeather;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.AlbumArtLockscreen;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.DepthWallpaper;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.Lockscreen;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenClock;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenWeather;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen.LockscreenWidgets;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.navbar.GestureNavbarManager;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.BatteryBar;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.BatteryStyleManager;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.HeaderClock;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.HeaderImage;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.NotificationTransparency;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QSTiles;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QSTransparency;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QsTileCustomization;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.QsWidgets;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.StatusbarClock;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.StatusbarIcons;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.StatusbarMods;
import it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar.StatusbarNotification;

public class ModPacks {

    public static ArrayList<Class<? extends XposedMods>> getMods(String packageName) {
        ArrayList<Class<? extends XposedMods>> modPacks = new ArrayList<>();

        //Should be loaded before others
        modPacks.add(HookTester.class);
        modPacks.add(SettingsLibUtilsProvider.class);

        switch (packageName) {
            case Constants.Packages.FRAMEWORK -> {
                modPacks.add(Buttons.class);
                modPacks.add(DarkMode.class);
                modPacks.add(OplusStartingWindowManager.class);
                modPacks.add(MemcEnhancer.class);
            }
            case SYSTEM_UI -> {
                if (!XPLauncher.isChildProcess) {
                    // Caffeine Tile
                    modPacks.add(CaffeineTile.class);

                    // Theme Enabler
                    modPacks.add(ThemeEnabler.class);

                    // Thermal Provider
                    modPacks.add(ThermalProvider.class);

                    // Controllers Provider
                    modPacks.add(ControllersProvider.class);

                    // Battery Data Provider
                    modPacks.add(BatteryDataProvider.class);

                    // Audio Data Provider
                    modPacks.add(AudioDataProvider.class);
                    modPacks.add(MediaPlayerObserver.class);

                    // System Classes We need
                    modPacks.add(OpUtils.class);

                    // Oplus Feature Enabler
                    modPacks.add(FeatureOption.class);

                    // Advanced Reboot
                    modPacks.add(AdvancedReboot.class);

                    // AOD
                    modPacks.add(AodClock.class);
                    modPacks.add(AodWeather.class);

                    // Statusbar
                    modPacks.add(StatusbarMods.class);
                    modPacks.add(StatusbarNotification.class);
                    modPacks.add(StatusbarClock.class);
                    modPacks.add(StatusbarIcons.class);
                    modPacks.add(BatteryStyleManager.class);
                    modPacks.add(NotificationTransparency.class);
                    // QS
                    modPacks.add(HeaderImage.class); // Load first QS Header Image since we have to check ScrimView Alpha before set by QS Transparency
                    modPacks.add(DepthWallpaper.class); // Load first Depth Wallpaper since we have to check ScrimView Alpha before set by QS Transparency
                    modPacks.add(HeaderClock.class);
                    modPacks.add(QSTransparency.class);
                    modPacks.add(QSTiles.class);
                    modPacks.add(QsTileCustomization.class);
                    modPacks.add(QsWidgets.class);

                    // Pulse View
                    modPacks.add(PulseViewHook.class);

                    // Lockscreen
                    modPacks.add(Lockscreen.class);
                    modPacks.add(LockscreenClock.class);
                    modPacks.add(LockscreenWidgets.class);
                    modPacks.add(LockscreenWeather.class);
                    modPacks.add(AlbumArtLockscreen.class);

                    // Volume Panel
                    modPacks.add(VolumePanel.class);

                    modPacks.add(GestureNavbarManager.class);
                    modPacks.add(BatteryBar.class);
                    modPacks.add(AdaptivePlayback.class);

                    // Fluid Music Settings
                    modPacks.add(FluidMusic.class);

                    modPacks.add(MiscMods.class);
                }
            }
            case Constants.Packages.SETTINGS -> {
                modPacks.add(CustomShortcut.class);
                modPacks.add(DarkModeSettings.class);
                modPacks.add(OtaCard.class);
                modPacks.add(MemcEnabler.class);
            }
            case Constants.Packages.LAUNCHER -> {
                modPacks.add(Launcher.class);
                modPacks.add(ThemedIcons.class);
            }
        }

        return modPacks;
    }
}
