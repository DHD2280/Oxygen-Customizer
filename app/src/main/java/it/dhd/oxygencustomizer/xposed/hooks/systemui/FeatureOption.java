package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

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

        ReflectedClass FeatureOptions = ReflectedClass.of("com.oplusos.systemui.common.feature.FeatureOption");

        FeatureOptions
                .before("isOplusVolumeKeyInRight")
                .run(param -> {
                    if (volumePanelPosition == 0) return;

                    if (volumePanelPosition == 1)
                        param.setResult(true);
                    else
                        param.setResult(false);
                });

        FeatureOptions
                .before("isSupportMyDevice")
                .run(param -> {
                    if (showMyDevice) param.setResult(true);
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
