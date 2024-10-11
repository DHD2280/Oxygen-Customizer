package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY_VALUE;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class NotificationTransparency extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    private final static String TAG = "Oxygen Customizer - Notification Transparency: ";
    private boolean notificationTransparency = false;
    private int notificationTransparencyValue = 25;
    private boolean hasOverlays = false;

    public NotificationTransparency(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        notificationTransparency = Xprefs.getBoolean(NOTIF_TRANSPARENCY, false);
        notificationTransparencyValue = Xprefs.getSliderInt(NOTIF_TRANSPARENCY_VALUE, 25);
        hasOverlays = Xprefs.getBoolean("hasNotificationOverlays", false);
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
                        if (d != null && notificationTransparency && !hasOverlays) {
                            d.setAlpha(notificationTransparencyValue);
                        }
                    }
                });
        findAndHookMethod(ExpandableNotificationRow, "updateBackgroundForGroupState", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                // blurColor
                if (notificationTransparency && !hasOverlays) {
                    setBooleanField(param.thisObject, "mShowGroupBackgroundWhenExpanded", true);
                }
            }
        });

        try {
            Class<?> OpNotificationBackgroundView = findClass("com.oplus.systemui.statusbar.notification.row.NotificationBackgroundViewExtImp", lpparam.classLoader);
            findAndHookMethod(OpNotificationBackgroundView,
                    "drawBlur",
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            if (notificationTransparency || hasOverlays) param.setResult(false);
                        }
                    });

        } catch (Throwable t) {
            log("ERROR IN OpNotificationBackgroundView " + t.getMessage());
        }

        fixNotificationColorA14(lpparam);
    }

    private void fixNotificationColorA14(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        if (Build.VERSION.SDK_INT < 34) return;

        try {
            Class<?> ActivatableNotificationViewClass = findClass(SYSTEM_UI + ".statusbar.notification.row.ActivatableNotificationView", loadPackageParam.classLoader);
            Class<?> NotificationBackgroundViewClass = findClass(SYSTEM_UI + ".statusbar.notification.row.NotificationBackgroundView", loadPackageParam.classLoader);
            Class<?> FooterViewClass = findClassIfExists(SYSTEM_UI + ".statusbar.notification.footer.ui.view.FooterView", loadPackageParam.classLoader);
            if (FooterViewClass == null) {
                FooterViewClass = findClass(SYSTEM_UI + ".statusbar.notification.row.FooterView", loadPackageParam.classLoader);
            }

            XC_MethodHook removeNotificationTint = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!hasOverlays) return;

                    View notificationBackgroundView = (View) getObjectField(param.thisObject, "mBackgroundNormal");

                    try {
                        setObjectField(param.thisObject, "mCurrentBackgroundTint", param.args[0]);
                    } catch (Throwable ignored) {
                    }

                    try {
                        callMethod(getObjectField(notificationBackgroundView, "mBackground"), "clearColorFilter");
                    } catch (Throwable ignored) {
                    }
                    try {
                        callMethod(notificationBackgroundView, "setColorFilter", 0);
                    } catch (Throwable ignored) {
                    }

                    setObjectField(notificationBackgroundView, "mTintColor", 0);
                    notificationBackgroundView.invalidate();
                }
            };

            hookAllMethods(ActivatableNotificationViewClass, "setBackgroundTintColor", removeNotificationTint);
            hookAllMethods(ActivatableNotificationViewClass, "updateBackgroundTint", removeNotificationTint);

            XC_MethodHook replaceTintColor = new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    if (!hasOverlays) return;

                    setObjectField(param.thisObject, "mTintColor", 0);
                }
            };

            try {
                hookAllMethods(NotificationBackgroundViewClass, "setCustomBackground$1", replaceTintColor);
            } catch (Throwable t) {
                log("setCustomBackground$1" + t);
            }

            try {
                hookAllMethods(NotificationBackgroundViewClass, "setCustomBackground", replaceTintColor);
            } catch (Throwable t) {
                log("setCustomBackground" + t);
            }

            XC_MethodHook removeButtonTint = new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) {
                    if (!hasOverlays) return;

                    Button mClearAllButton = (Button) getObjectField(param.thisObject, "mClearAllButton");
                    Button mManageButton = (Button) getObjectField(param.thisObject, "mManageButton");

                    mClearAllButton.getBackground().clearColorFilter();
                    mManageButton.getBackground().clearColorFilter();

                    mClearAllButton.invalidate();
                    mManageButton.invalidate();
                }
            };

            try {
                hookAllMethods(FooterViewClass, "updateColors", removeButtonTint);
            } catch (Throwable t) {
                log("updateColors" + t);
            }
            try {
                hookAllMethods(FooterViewClass, "updateColors$3", removeButtonTint);
            } catch (Throwable t) {
                log("updateColors$3" + t);
            }
        } catch (Throwable throwable) {
            log(throwable);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}

