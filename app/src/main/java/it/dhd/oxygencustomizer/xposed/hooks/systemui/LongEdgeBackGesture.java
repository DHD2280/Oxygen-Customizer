package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import android.content.Context;
import android.os.Build;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class LongEdgeBackGesture extends XposedMods {

    public LongEdgeBackGesture(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {

    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        if (Build.VERSION.SDK_INT < 36) return; //only OOS16

        // icon com.oplus.systemui.navigationbar.gesture.sidegesture.view.SideGestureViewManager # backIcon


    }

    @Override
    public boolean listensTo(String packageName) {
        return false;
    }
}
