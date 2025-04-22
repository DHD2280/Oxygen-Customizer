package it.dhd.oxygencustomizer.xposed.startup;

import android.content.Context;
import android.util.Log;

import java.net.URL;

import it.dhd.oxygencustomizer.BuildConfig;

/**
 * NOTICE: Do NOT use any androidx annotations here.
 */
public class HybridClassLoader extends ClassLoader {

    private static final String TAG = "HybridClassLoader";

    /**
     * The bootstrap class loader for Android, effectively NonNull.
     */
    private static final ClassLoader BOOT_CLASS_LOADER = Context.class.getClassLoader();
    private static final String SELF_PKG_NAME_PREFIX = BuildConfig.APPLICATION_ID + ".";
    private final ClassLoader clPreload;
    private final ClassLoader clBase;

    public HybridClassLoader(ClassLoader x, ClassLoader ctx) {
        super(BOOT_CLASS_LOADER);
        clPreload = x;
        clBase = ctx;
    }

    /**
     * Check whether the class is a host class.
     *
     * @param name the FQCN of the class
     * @return true if the class is a host class
     */
    public static boolean isHostClass(String name) {
        return name.startsWith("com.coui.")
                || name.startsWith("com.heytap.")
                || name.startsWith("com.hey5media.")
                || name.startsWith("com.nearme.")
                || name.startsWith("com.coloros.")
                || name.startsWith("com.umetrip.")
                || name.startsWith("com.oplus.")
                || name.startsWith("com.oplusos.")
                || name.startsWith("com.pantanal.")
                || name.startsWith("com.android.launcher3.")
                || name.startsWith("com.android.launcher.")
                || name.startsWith("com.android.quickstep.")
                || name.startsWith("com.android.systemui.")
                || name.startsWith("com.android.settingslib.")
                || name.startsWith("com.android.server.audio.AlertSlider")
                || name.startsWith("com.android.server.audio.AlertSliderHw")
                || name.startsWith("com.android.server.health.")
                || name.startsWith("com.android.server.oplus.")
                || name.startsWith("android.hardware.health.")
                || name.startsWith("android.os.BatteryProperty")
                || name.startsWith("com.android.server.cpulimit.OplusCpuInfoStore")
                || name.startsWith("com.android.server.cpulimit.OplusCpuLimitHistory")
                || name.startsWith("com.android.server.wm.FlexibleWindowUtils")
                || name.startsWith("com.android.server.wm.FlexibleWindowManagerService")
                || name.startsWith("com.android.server.wm.Task")
                || name.startsWith("android.util.PathParser")
                || name.startsWith("android.app.OplusUXIconLoader")
                // 任意 Oplus 开头
                || name.startsWith("android.app.Oplus")
                || name.startsWith("com.android.internal.os.")
                || name.startsWith("cooperation.")
                || name.startsWith("androidx.preference.")
                || name.startsWith("androidx.recyclerview.")
                || name.startsWith("androidx.fragment.")
                || name.startsWith("dagger.")
                || name.startsWith("androidx.constraintlayout.")
                || name.startsWith("kotlin.")
                || name.startsWith("com.google.android.")
                || name.startsWith("com.bumptech.glide.")
                || name.startsWith("dov.");
        // add more if needed
    }

    /**
     * 把宿主和模块共有的 package 扔这里.
     *
     * @param name NonNull, class name
     * @return true if conflicting
     */
    public static boolean isConflictingClass(String name) {
        if (name.startsWith("androidx.recyclerview.")
                || name.startsWith("androidx.constraintlayout.")) {
            return false;
        }

        return name.startsWith("androidx.")
                || name.startsWith("android.support.")
                || name.startsWith("kotlin.")
                || name.startsWith("kotlinx.")
                || name.startsWith("com.tencent.mmkv.")
                || name.startsWith("com.android.tools.r8.")
                || name.startsWith("com.google.android.")
                || name.startsWith("com.google.gson.")
                || name.startsWith("com.google.common.")
                || name.startsWith("com.google.protobuf.")
                || name.startsWith("com.microsoft.appcenter.")
                || name.startsWith("org.intellij.lang.annotations.")
                || name.startsWith("org.jetbrains.annotations.")
                || name.startsWith("com.google.errorprone.annotations.")
                || name.startsWith("org.jf.dexlib2.")
                || name.startsWith("org.jf.util.")
                || name.startsWith("javax.annotation.")
                || name.startsWith("_COROUTINE.");
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return BOOT_CLASS_LOADER.loadClass(name);
        } catch (ClassNotFoundException ignored) {
        }
        if (name != null && isConflictingClass(name)) {
            //Nevertheless, this will not interfere with the host application,
            //classes in host application SHOULD find with their own ClassLoader, eg Class.forName()
            //use shipped androidx and kotlin lib.
            throw new ClassNotFoundException(name);
        }
        // The ClassLoader for some apk-modifying frameworks are terrible, XposedBridge.class.getClassLoader()
        // is the sane as Context.getClassLoader(), which mess up with 3rd lib, can cause the ART to crash.
        if (clPreload != null) {
            try {
                return clPreload.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        if (clBase != null) {
            try {
                return clBase.loadClass(name);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(name);
    }

    @Override
    public URL getResource(String name) {
        URL ret = clPreload.getResource(name);
        if (ret != null) {
            return ret;
        }
        return clBase.getResource(name);
    }
}
