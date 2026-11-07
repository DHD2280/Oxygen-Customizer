package it.dhd.oxygencustomizer.xposed.hooks.systemui.statusbar;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.BLUR_RADIUS_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_BLUR_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QSPANEL_MAX_BLUR_AMOUNT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.QuickSettings.QS_TRANSPARENCY_VAL;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findClass;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class QSTransparency extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private final float keyguard_alpha = 0.85f;
    boolean qsTransparencyActive = false;
    private float alpha = 40;
    private boolean blurEnabled = false;
    private int blurRadius = 60;
    private Object mScrimControllerExImp = null;
    private float maxBlurRadius = 1f;

    public QSTransparency(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        if (Xprefs == null) return;

        qsTransparencyActive = Xprefs.getBoolean(QS_TRANSPARENCY_SWITCH, false);
        alpha = (float) ((float) Xprefs.getSliderInt(QS_TRANSPARENCY_VAL, 40) / 100.0);

        blurEnabled = Xprefs.getBoolean(QSPANEL_BLUR_SWITCH, false);
        blurRadius = Xprefs.getSliderInt(BLUR_RADIUS_VALUE, 60);
        maxBlurRadius = Xprefs.getInt(QSPANEL_MAX_BLUR_AMOUNT, 100) / 100f;

    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        setQsTransparency(PRParam);
        setBlurRadius();
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    private void setQsTransparency(XposedModuleInterface.PackageReadyParam PRParam) {
        final Class<?> ScrimControllerClass = findClass(SYSTEM_UI + ".statusbar.phone.ScrimController", PRParam.getClassLoader());

        ReflectedClass.of(ScrimControllerClass)
                .before("updateScrimColor")
                .run(param -> {
                    if (!qsTransparencyActive) return;
                    if (Build.VERSION.SDK_INT >= 35) return;

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
                });

        if (Build.VERSION.SDK_INT >= 35) {
            hookQs();
        }

    }

    private void hookQs() {
        ReflectedClass ScrimControllerExImp = ReflectedClass.of("com.oplus.systemui.statusbar.phone.ScrimControllerExImp");
        ScrimControllerExImp
                .afterConstruction()
                .run(param -> {
                    mScrimControllerExImp = param.thisObject;
                });

        ReflectedClass ScrimViewExImp = ReflectedClass.of("com.oplus.systemui.scrim.ScrimViewExImp");
        ScrimViewExImp
                .before("setBlurAmount")
                .run(param -> {
                    // float f2, String str
                    if (mScrimControllerExImp == null) return;
                    float blurAmount = (float) param.args[0];
                    boolean isBehind = false;
                    try {
                        isBehind = (boolean) callMethod(param.thisObject, "isBehind");
                    } catch (Throwable ignored) {
                        isBehind = getBooleanField(param.thisObject, "isBehind");
                    }
                    boolean isQsVisible = false;
                    try {
                        isQsVisible = (boolean) callMethod(mScrimControllerExImp, "isQsVisible");
                    } catch (Throwable ignored) {
                        isQsVisible = getBooleanField(mScrimControllerExImp, "isQsVisible");
                    }
                    if (isBehind && isQsVisible) {
                        param.args[0] = constrain(blurAmount, 0.0f, maxBlurRadius);
                    }
                });
    }

    private float constrain(float amount, float low, float high) {
        return amount < low ? low : (amount > high ? high : amount);
    }

    private void setBlurRadius() {
        ReflectedClass.of(Resources.class)
                .before("getDimensionPixelSize")
                .run(param -> {
                    if (!blurEnabled) return;
                    if (Build.VERSION.SDK_INT >= 35) return;

                    try {
                        @SuppressLint("DiscouragedApi") int resId = mContext.getResources()
                                .getIdentifier("max_window_blur_radius", "dimen", mContext.getPackageName());
                        if (param.args[0].equals(resId)) {
                            param.setResult(blurRadius);
                        }
                    } catch (Throwable throwable) {
                        log(throwable);
                    }
                });
    }
}
