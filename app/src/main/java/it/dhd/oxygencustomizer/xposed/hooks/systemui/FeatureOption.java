package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class FeatureOption extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private int volumePanelPosition = 0;
    private boolean showMyDevice = false;

    public FeatureOption(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        volumePanelPosition = Integer.parseInt(Xprefs.getString("volume_panel_position", "0"));
        showMyDevice = Xprefs.getBoolean("qs_show_my_device", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        Class<?> FeatureOptions = findClass("com.oplusos.systemui.common.feature.FeatureOption", lpparam.classLoader);


        hookAllMethods(FeatureOptions, "isOplusVolumeKeyInRight", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (volumePanelPosition == 0) return;

                if (volumePanelPosition == 1)
                    param.setResult(true);
                else
                    param.setResult(false);
            }
        });

        hookAllMethods(FeatureOptions, "isSupportMyDevice", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (showMyDevice) param.setResult(true);
            }
        });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
