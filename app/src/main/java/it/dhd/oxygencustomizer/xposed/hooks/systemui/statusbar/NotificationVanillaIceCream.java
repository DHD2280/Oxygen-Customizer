package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.isOOS1501;

import android.content.Context;
import android.os.Build;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class NotificationVanillaIceCream extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private boolean hasOverlays = false;

    public NotificationVanillaIceCream(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        hasOverlays = Xprefs.getBoolean("hasNotificationOverlays", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {

        if (Build.VERSION.SDK_INT < 35) return;

        ReflectedClass ViewBlurManager = ReflectedClass.of("com.oplus.systemui.notification.blur.ViewBlurManager");

        ReflectedClass.ReflectionConsumer nullReturner = param -> {
            if (hasOverlays) param.setResult(null);
        };

        ViewBlurManager
                .before("updateCardPlatformMixConfig")
                .run(nullReturner);
        ViewBlurManager
                .before("updateRowsBlur")
                .run(nullReturner);
        ViewBlurManager
                .before("headsupCardMotionMixConfig")
                .run(nullReturner);

        ViewBlurManager
                .before("requireBlurProxyForView")
                .run(param -> {
                    if (!isOOS1501()) {
                        param.setResult(null);
                    }
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
