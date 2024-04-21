package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class StatusbarIcons extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private boolean hideBluetooth;

    public StatusbarIcons(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        hideBluetooth = Xprefs.getBoolean("hide_bluetooth_when_disconnected", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> OplusPhoneStatusBarPolicyExImpl = findClass("com.oplus.systemui.statusbar.phone.OplusPhoneStatusBarPolicyExImpl", lpparam.classLoader);

        // private final void updateBluetoothIcon(int i, int i2, CharSequence charSequence, boolean z) {
        findAndHookMethod(OplusPhoneStatusBarPolicyExImpl, "updateBluetoothIcon",
                int.class,
                int.class,
                CharSequence.class,
                boolean.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                boolean enabled = (boolean) param.args[3];

                if (!enabled || !hideBluetooth) return;

                Object bluetoothController = getObjectField(param.thisObject, "bluetoothController");
                boolean connected = (boolean) callMethod(bluetoothController, "isBluetoothConnected");

                if (!connected)
                    param.setResult(connected);
            }
        });

    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
