package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY_VALUE;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class NotificationTransparency extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private final static String TAG = "Oxygen Customizer - Notification Transparency: ";
    private boolean notificationTransparency = false;
    private int notificationTransparencyValue = 25;

    public NotificationTransparency(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        notificationTransparency = Xprefs.getBoolean(NOTIF_TRANSPARENCY, false);
        notificationTransparencyValue = Xprefs.getSliderInt(NOTIF_TRANSPARENCY_VALUE, 25);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        Class<?> NotificationBackgroundView = findClass("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", lpparam.classLoader);
        Class<?> ExpandableNotificationRow = findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", lpparam.classLoader);

        findAndHookMethod(NotificationBackgroundView,
                "draw",
                Canvas.class,
                Drawable.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Drawable d = (Drawable) param.args[1];
                if (d!=null && notificationTransparency) {
                    d.setAlpha(notificationTransparencyValue);
                }
            }
        });
        findAndHookMethod(ExpandableNotificationRow, "updateBackgroundForGroupState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // blurColor
                if (notificationTransparency) {
                    setBooleanField(param.thisObject, "mShowGroupBackgroundWhenExpanded", true);
                }
            }
        });
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}

