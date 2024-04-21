package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class MiscMods extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;

    private boolean mHideRotationButton;
    private View mRotationButton;

    public MiscMods(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mHideRotationButton = Xprefs.getBoolean("misc_remove_rotate_floating", false);

        if (Key.length > 0 && Key[0].equals("misc_remove_rotate_floating")) {
            if (mRotationButton != null) mRotationButton.setVisibility(mHideRotationButton ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        Class<?> FloatingRotationButton = findClass("com.android.systemui.shared.rotation.FloatingRotationButton", lpparam.classLoader);
        hookAllConstructors(FloatingRotationButton, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mRotationButton = (View) getObjectField(param.thisObject, "mKeyButtonView");
                if (mHideRotationButton) mRotationButton.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
