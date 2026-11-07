package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class FluidMusic extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;

    private boolean mFluidCustomEnabled = false;
    private List<String> mFluidAppsList = new ArrayList<>();

    public FluidMusic(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        mFluidCustomEnabled = Xprefs.getBoolean("fluid_music_custom_switch", false);
        Set<String> mFluidApps = Xprefs.getStringSet("fluid_music_apps", new HashSet<>());
        mFluidAppsList.clear();
        mFluidAppsList = new ArrayList<>(mFluidApps);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        ReflectedClass OplusMediaRusUpdateManager = ReflectedClass.ofIfPossible("com.oplus.systemui.media.seedling.rus.OplusMediaRusUpdateManager");

        OplusMediaRusUpdateManager
                .before("getRusWhiteList")
                .run(param -> {
                    if (!mFluidCustomEnabled) return;

                    List<String> whiteList = (List<String>) getObjectField(param.thisObject, "whiteList");
                    whiteList.clear();
                    whiteList.addAll(mFluidAppsList);
                    param.setResult(mFluidAppsList);
                });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
