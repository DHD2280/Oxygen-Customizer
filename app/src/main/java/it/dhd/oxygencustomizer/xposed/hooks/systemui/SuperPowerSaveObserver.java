package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;

import android.content.Context;
import android.os.Build;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class SuperPowerSaveObserver extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static SuperPowerSaveObserver instance;
    public static boolean mIsSuperPowerSave = false;
    private final ArrayList<SuperPowerSaveListener> mSuperPowerSaveCallbaks = new ArrayList<>();


    public SuperPowerSaveObserver(Context context) {
        super(context);
        instance = this;
    }

    public static void registerSuperPowerSaveCallback(SuperPowerSaveListener callback) {
        instance.mSuperPowerSaveCallbaks.add(callback);
    }

    public static void unregisterSuperPowerSaveCallback(SuperPowerSaveListener callback) {
        instance.mSuperPowerSaveCallbaks.remove(callback);
    }

    @Override
    public void updatePrefs(String... Key) {

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        ReflectedClass SuperPowerSaveSettingsObserver = ReflectedClass.of("com.oplus.systemui.qs.observer.SuperPowerSaveSettingsObserver" /* OOS15-14 */,
                "com.oplusos.systemui.common.observer.SuperPowerSaveSettingsObserver" /* OOS13 */);

        SuperPowerSaveSettingsObserver
                .after("onChange")
                        .run(param -> {
                            mIsSuperPowerSave = getBooleanField(param.thisObject,
                                    Build.VERSION.SDK_INT >= 35 ?
                                            "isSuperPowerSaveState" :
                                            "mIsSuperPowerSaveState");
                            log("SuperPowerSaveObserver: SuperPowerSave is " + mIsSuperPowerSave);
                            onSuperPowerSaveChanged(mIsSuperPowerSave);
                        });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void onSuperPowerSaveChanged(boolean isSuperPowerSave) {
        for (SuperPowerSaveListener listener : mSuperPowerSaveCallbaks) {
            try {
                listener.onSuperPowerSaveChanged(isSuperPowerSave);
            } catch (Throwable ignored) {}
        }
    }

    public interface SuperPowerSaveListener {
        void onSuperPowerSaveChanged(boolean isSuperPowerSave);
    }

}
