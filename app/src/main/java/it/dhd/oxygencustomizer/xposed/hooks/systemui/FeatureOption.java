package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class FeatureOption extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private int volumePanelPosition = 0;
    private boolean showMyDevice = false;
    private boolean allowAodMusic = false;

    public FeatureOption(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        volumePanelPosition = Integer.parseInt(Xprefs.getString("volume_panel_position", "0"));
        showMyDevice = Xprefs.getBoolean("qs_show_my_device", false);
        allowAodMusic = Xprefs.getBoolean("allow_aod_music", false);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        ReflectedClass FeatureOptions = ReflectedClass.of("com.oplusos.systemui.common.feature.FeatureOption", PRParam.getClassLoader());

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
        ReflectedClass QSFeatureOption = ReflectedClass.ofIfPossible("com.oplusos.systemui.common.feature.QSFeatureOption");
        if (QSFeatureOption.getClazz() != null) {
            QSFeatureOption
                    .before("isSupportMyDevice")
                    .run(param -> {
                        if (showMyDevice) param.setResult(true);
                    });
        }

        ReflectedClass AodMediaDataListener = ReflectedClass.of("com.oplusos.systemui.aod.mediapanel.AodMediaDataListener$Companion");
        ReflectedClass.ReflectionConsumer musicHooker = param -> {
            if (allowAodMusic) param.setResult(true);
        };
        AodMediaDataListener
                .before("isAodMediaSupportWithoutFeature")
                .run(musicHooker);
        AodMediaDataListener
                .before("isAodMediaSupport")
                .run(musicHooker);

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
