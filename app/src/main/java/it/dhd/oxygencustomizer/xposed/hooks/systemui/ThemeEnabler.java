package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.views.CurrentWeatherView.reloadWeatherBg;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class ThemeEnabler extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private int themeNum;

    public ThemeEnabler(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        for (int i = 0; i<Xprefs.getInt("UiStylesThemes", 0); i++) {
            if (Xprefs.getBoolean("OxygenCustomizerComponentTH" + (i+1) + ".overlay", false)) {
                themeNum = (i+1);
            } else {
                themeNum = -1;
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        // System UI Got Restarted, Re-Apply Theme
        enableTheme();

        // Get monet change so we can apply theme
        Class<?> ScrimController = findClass("com.android.systemui.statusbar.phone.ScrimController", lpparam.classLoader);
        hookAllMethods(ScrimController, "updateThemeColors", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                enableTheme();
                reloadWeatherBg();
            }
        });

    }

    private void enableTheme() {
        if (themeNum == -1) return;
        XPLauncher.enqueueProxyCommand(proxy -> proxy.applyTheme("OxygenCustomizerComponentTH" + themeNum + ".overlay"));
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
