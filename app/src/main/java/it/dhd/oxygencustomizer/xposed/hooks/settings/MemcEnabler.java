package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static it.dhd.oxygencustomizer.utils.Constants.OPLUS_MEMC_FEATURES;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class MemcEnabler extends XposedMods {

    private final static String listenPackage = SETTINGS;
    private final static String TAG = "MemcEnabler--> ";

    private boolean mForceEnableMemc = false;

    public MemcEnabler(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        mForceEnableMemc = Xprefs.getBoolean("force_memc_enabled", false);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        ReflectedClass SysFeatureUtils = ReflectedClass.ofIfPossible("com.oplus.settings.utils.SysFeatureUtils");
        SysFeatureUtils
                .before("hasOplusFeature")
                .run(param -> {
                    String requestedFeature = (String) param.args[0];
                    if (OPLUS_MEMC_FEATURES.contains(requestedFeature) && mForceEnableMemc) {
                        param.setResult(true);
                    }
                });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
