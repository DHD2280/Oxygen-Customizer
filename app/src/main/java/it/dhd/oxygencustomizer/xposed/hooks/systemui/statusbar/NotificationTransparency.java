package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.StatusbarNotificationPrefs.NOTIF_TRANSPARENCY_VALUE;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setBooleanField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setObjectField;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;
import android.widget.Button;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;

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
    public void onPreferenceUpdated(String... Key) {
        notificationTransparency = Xprefs.getBoolean(NOTIF_TRANSPARENCY, false);
        notificationTransparencyValue = Xprefs.getSliderInt(NOTIF_TRANSPARENCY_VALUE, 25);
        hasOverlays = Xprefs.getBoolean("hasNotificationOverlays", false);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        Class<?> NotificationBackgroundView = findClass("com.android.systemui.statusbar.notification.row.NotificationBackgroundView", PRParam.getClassLoader());
        Class<?> ExpandableNotificationRow = findClass("com.android.systemui.statusbar.notification.row.ExpandableNotificationRow", PRParam.getClassLoader());

        ReflectedClass.of(NotificationBackgroundView)
                .before(ReflectionHelpers.findMethodExact(NotificationBackgroundView, "draw", Canvas.class, Drawable.class))
                .run(param -> {
                    if (Build.VERSION.SDK_INT >= 35) return;
                    Drawable d = (Drawable) param.args[1];
                    if (d != null && notificationTransparency && !hasOverlays) {
                        d.setAlpha(notificationTransparencyValue);
                    }
                });
        ReflectedClass.of(ExpandableNotificationRow)
                .before(ReflectionHelpers.findMethodExact(ExpandableNotificationRow, "updateBackgroundForGroupState"))
                .run(param -> {
                    // blurColor
                    if (Build.VERSION.SDK_INT >= 35) return;
                    if (notificationTransparency && !hasOverlays) {
                        setBooleanField(param.thisObject, "mShowGroupBackgroundWhenExpanded", true);
                    }
                });

        try {
            Class<?> OpNotificationBackgroundView = findClass("com.oplus.systemui.statusbar.notification.row.NotificationBackgroundViewExtImp", PRParam.getClassLoader());
            ReflectedClass.of(OpNotificationBackgroundView)
                    .before(ReflectionHelpers.findMethodExact(OpNotificationBackgroundView, "drawBlur"))
                    .run(param -> {
                        if (notificationTransparency || hasOverlays) param.setResult(false);
                    });

        } catch (Throwable t) {
            log("ERROR IN OpNotificationBackgroundView " + t.getMessage());
        }

        fixNotificationColorA14(PRParam);
    }

    private void fixNotificationColorA14(XposedModuleInterface.PackageReadyParam PRParam) {
        if (Build.VERSION.SDK_INT < 34) return;

        try {
            ReflectedClass ActivatableNotificationViewClass = ReflectedClass.of(SYSTEM_UI + ".statusbar.notification.row.ActivatableNotificationView", PRParam.getClassLoader());
            ReflectedClass NotificationBackgroundViewClass = ReflectedClass.of(SYSTEM_UI + ".statusbar.notification.row.NotificationBackgroundView", PRParam.getClassLoader());
            ReflectedClass FooterViewClass = ReflectedClass.of(SYSTEM_UI + ".statusbar.notification.footer.ui.view.FooterView",
                    SYSTEM_UI + ".statusbar.notification.row.FooterView");

            ReflectedClass.ReflectionConsumer removeNotificationTint = param -> {
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

                try {
                    setObjectField(notificationBackgroundView, "mTintColor", 0);
                } catch (Throwable ignored) {
                }

                if (notificationBackgroundView != null) notificationBackgroundView.invalidate();
            };

            ActivatableNotificationViewClass
                    .before("setBackgroundTintColor")
                    .run(removeNotificationTint);
            ActivatableNotificationViewClass
                    .before("updateBackgroundTint")
                    .run(removeNotificationTint);

            ReflectedClass.ReflectionConsumer replaceTintColor = param -> {
                if (!hasOverlays) return;

                try {
                    setObjectField(param.thisObject, "mTintColor", 0);
                } catch (Throwable ignored) {
                }
            };

            NotificationBackgroundViewClass
                    .before("setCustomBackground$1")
                    .run(replaceTintColor);
            NotificationBackgroundViewClass
                    .before("setCustomBackground")
                    .run(replaceTintColor);

            ReflectedClass.ReflectionConsumer removeButtonTint = param -> {
                if (!hasOverlays) return;

                Button mClearAllButton = (Button) getObjectField(param.thisObject, "mClearAllButton");
                Button mManageButton = (Button) getObjectField(param.thisObject, "mManageButton");

                mClearAllButton.getBackground().clearColorFilter();
                mManageButton.getBackground().clearColorFilter();

                mClearAllButton.invalidate();
                mManageButton.invalidate();
            };

            FooterViewClass
                    .after("updateColors")
                    .run(removeButtonTint);
            FooterViewClass
                    .after("updateColors$3")
                    .run(removeButtonTint);

        } catch (Throwable throwable) {
            log(throwable);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}

