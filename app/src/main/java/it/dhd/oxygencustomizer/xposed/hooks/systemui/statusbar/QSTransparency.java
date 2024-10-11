package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.BLUR_RADIUS_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_BLUR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_VAL;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class QSTransparency extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private final float keyguard_alpha = 0.85f;
    boolean qsTransparencyActive = false;
    private float alpha = 40;
    private boolean blurEnabled = false;
    private int blurRadius = 60;

    public QSTransparency(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        qsTransparencyActive = Xprefs.getBoolean(QS_TRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getSliderInt(QS_TRANSPARENCY_VAL, 40) / 100.0);

        blurEnabled = Xprefs.getBoolean(QSPANEL_BLUR_SWITCH, false);
        blurRadius = Xprefs.getSliderInt(BLUR_RADIUS_VALUE, 60);

    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        setQsTransparency(loadPackageParam);
        setBlurRadius();
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void setQsTransparency(XC_LoadPackage.LoadPackageParam loadPackageParam) {
        final Class<?> ScrimControllerClass = findClass(SYSTEM_UI + ".statusbar.phone.ScrimController", loadPackageParam.classLoader);

        hookAllMethods(ScrimControllerClass, "updateScrimColor", new XC_MethodHook() {
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!qsTransparencyActive) return;

                int alphaIndex = param.args[2] instanceof Float ? 2 : 1;
                String scrimState = getObjectField(param.thisObject, "mState").toString();

                if (scrimState.contains("BOUNCER")) {
                    param.args[alphaIndex] = (Float) param.args[alphaIndex] * keyguard_alpha;
                } else {
                    String scrimName = "unknown_scrim";

                    if (findField(ScrimControllerClass, "mScrimInFront").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "scrim_in_front";
                    } else if (findField(ScrimControllerClass, "mScrimBehind").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "scrim_behind";
                    } else if (findField(ScrimControllerClass, "mNotificationsScrim").get(param.thisObject).equals(param.args[0])) {
                        scrimName = "scrim_notifications";
                    }

                    if (scrimName.equals("scrim_notifications") || scrimName.equals("scrim_behind")) {
                        param.args[alphaIndex] = (Float) param.args[alphaIndex] * alpha;
                    }
                }
            }
        });

    }

    private void setBlurRadius() {
        hookAllMethods(Resources.class, "getDimensionPixelSize", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) {
                if (!blurEnabled) return;

                try {
                    @SuppressLint("DiscouragedApi") int resId = mContext.getResources()
                            .getIdentifier("max_window_blur_radius", "dimen", mContext.getPackageName());
                    if (param.args[0].equals(resId)) {
                        param.setResult(blurRadius);
                    }
                } catch (Throwable throwable) {
                    log(throwable);
                }
            }
        });
    }
}
