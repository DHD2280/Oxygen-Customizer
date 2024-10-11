package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.view.View;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class MiscMods extends XposedMods {

    private static final String listenPackage = Constants.Packages.SYSTEM_UI;

    private boolean mHideRotationButton = false;
    private View mRotationButton;
    private boolean mRemoveUsb = false;

    public MiscMods(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mHideRotationButton = Xprefs.getBoolean("misc_remove_rotate_floating", false);
        mRemoveUsb = Xprefs.getBoolean("remove_usb_dialog", false);

        if (Key.length > 0 && Key[0].equals("misc_remove_rotate_floating")) {
            if (mRotationButton != null)
                mRotationButton.setVisibility(mHideRotationButton ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!listenPackage.equals(lpparam.packageName)) return;

        try {
            Class<?> FloatingRotationButton = findClass("com.android.systemui.shared.rotation.FloatingRotationButton", lpparam.classLoader);
            hookAllConstructors(FloatingRotationButton, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mRotationButton = (View) getObjectField(param.thisObject, "mKeyButtonView");
                    if (mHideRotationButton) mRotationButton.setVisibility(View.GONE);
                }
            });
        } catch (Throwable ignored) {
        }

        try {
            Class<?> UsbService = findClass("com.oplus.systemui.usb.UsbService", lpparam.classLoader);
            hookAllMethods(UsbService, "onUsbConnected", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!mRemoveUsb) return;
                    Context c = (Context) param.args[0];
                    param.setResult(null);
                    callMethod(param.thisObject, "onUsbSelect", 1);
                    callMethod(param.thisObject, "updateAdbNotification", c);
                    callMethod(param.thisObject, "updateUsbNotification", c, 1);
                    callMethod(param.thisObject, "changeUsbConfig", c, 1);
                }
            });

            findAndHookMethod(UsbService, "helpUpdateUsbNotification", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (!mRemoveUsb) return;
                    setBooleanField(param.thisObject, "mNeedShowUsbDialog", false);
                }
            });
        } catch (Throwable ignored) {
        }

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
