package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.content.Context;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class ThemeEnabler extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    @SuppressLint("StaticFieldLeak")
    private static ThemeEnabler instance = null;
    private final ArrayList<OnThemeChangedListener> mThemeChangedListeners = new ArrayList<>();
    private int themeNum = -1;

    public ThemeEnabler(Context context) {
        super(context);
        instance = this;
    }

    public static void registerThemeChangedListener(ThemeEnabler.OnThemeChangedListener listener) {
        instance.mThemeChangedListeners.add(listener);
    }

    /**
     * @noinspection unused
     */
    public static void unRegisterThemeChangedListener(OnThemeChangedListener listener) {
        instance.mThemeChangedListeners.remove(listener);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
        for (int i = 0; i < Xprefs.getInt("UiStylesThemes", 0); i++) {
            if (Xprefs.getBoolean("OxygenCustomizerComponentTH" + (i + 1) + ".overlay", false)) {
                themeNum = (i + 1);
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
        try {
            ReflectedClass ScrimController = ReflectedClass.of("com.android.systemui.statusbar.phone.ScrimController");
            ScrimController
                    .after("updateThemeColors")
                    .run(param -> {
                        enableTheme();
                        notifyThemeChanged();
                    });
        } catch (Throwable ignored) {
        }

        try {
            ReflectedClass NotificationPanelViewController = ReflectedClass.of("com.android.systemui.shade.NotificationPanelViewController");
            NotificationPanelViewController
                    .after("onThemeChanged")
                    .run(param -> {
                        enableTheme();
                        notifyThemeChanged();
                    });
        } catch (Throwable ignored) {
        }

    }

    private void enableTheme() {
        if (themeNum == -1) return;
        XPLauncher.enqueueProxyCommand(proxy -> proxy.applyTheme("OxygenCustomizerComponentTH" + themeNum + ".overlay"));
    }

    private void notifyThemeChanged() {
        for (ThemeEnabler.OnThemeChangedListener listener : mThemeChangedListeners) {
            try {
                listener.onThemeChanged();
            } catch (Throwable ignored) {
            }
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    public interface OnThemeChangedListener {
        void onThemeChanged();
    }
}
