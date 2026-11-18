package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.utils.Constants.ACTION_SETTINGS_CHANGED;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import android.content.Context;
import android.content.Intent;

import it.dhd.oxygencustomizer.xposed.FrameworkMods;
import it.dhd.oxygencustomizer.xposed.hooks.framework.Buttons;
import it.dhd.oxygencustomizer.xposed.hooks.framework.DarkMode;
import it.dhd.oxygencustomizer.xposed.hooks.framework.MemcEnhancer;
import it.dhd.oxygencustomizer.xposed.hooks.framework.OplusStartingWindowManager;
import it.dhd.oxygencustomizer.xposed.hooks.framework.PanoramaUnlock;

public final class FrameworkSettings {

    public static final String EXTRA_CLASS = "class";

    public enum Mod {
        BUTTONS(Buttons.class),
        DARK_MODE(DarkMode.class),
        MEMC(MemcEnhancer.class),
        LAG_FIX(OplusStartingWindowManager.class),
        PANORAMA(PanoramaUnlock.class);

        private final Class<? extends FrameworkMods> mClass;

        Mod(Class<? extends FrameworkMods> clazz) {
            mClass = clazz;
        }
    }

    private FrameworkSettings() {
    }

    public static void notifyChanged(Context context, Mod mod) {
        if (context == null) return;
        Intent broadcast = new Intent(ACTION_SETTINGS_CHANGED);
        broadcast.putExtra(EXTRA_CLASS, mod.mClass.getSimpleName());
        broadcast.setPackage(FRAMEWORK);
        broadcast.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        context.sendBroadcast(broadcast);
    }
}
