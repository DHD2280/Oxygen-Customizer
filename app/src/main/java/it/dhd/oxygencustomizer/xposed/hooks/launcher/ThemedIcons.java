package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.graphics.drawable.AdaptiveIconDrawable;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.GoogleMonochromeIconFactory;

public class ThemedIcons extends XposedMods {

    private final static String listenPackage = LAUNCHER;
    private final static String TAG = "Oxygen Customizer - Launcher - ThemedIcons: ";

    private static boolean ForceThemedLauncherIcons = false;
    private int mIconBitmapSize;

    public ThemedIcons(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        ForceThemedLauncherIcons = Xprefs.getBoolean("force_themed_launcher_icons", false);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            Class<?> BaseIconFactoryClass = findClass("com.android.launcher3.icons.BaseIconFactory", lpparam.classLoader);

            hookAllConstructors(BaseIconFactoryClass, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mIconBitmapSize = getIntField(param.thisObject, "mIconBitmapSize");
                }
            });

//            Class<?> UxCustomAdaptiveIconDrawable = findClass("com.oplus.uxicon.ui.ui.UxCustomAdaptiveIconDrawable", lpparam.classLoader);
//            hookAllConstructors(UxCustomAdaptiveIconDrawable, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    for (int i = 0;i< param.args.length; i++) {
//                        log("UxCustomAdaptiveIconDrawable: " + i + " " + (param.args[i] != null));
//                    }
//                    //log("UxCustomAdaptiveIconDrawable: " + callMethod(icon, "toString"));
//                }
//            });

            hookAllMethods(AdaptiveIconDrawable.class, "getMonochrome", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if (param.getResult() == null && ForceThemedLauncherIcons) {

                            if (new Throwable().getStackTrace()[4].getMethodName().toLowerCase().contains("override")) //It's from com.android.launcher3.icons.IconProvider.getIconWithOverrides. Monochrome is included
                            {
                                return;
                            }

                            GoogleMonochromeIconFactory mono = (GoogleMonochromeIconFactory) getAdditionalInstanceField(param.thisObject, "mMonoFactoryOC");
                            if (mono == null) {
                                mono = new GoogleMonochromeIconFactory((AdaptiveIconDrawable) param.thisObject, mIconBitmapSize);
                                setAdditionalInstanceField(param.thisObject, "mMonoFactoryOC", mono);
                            }
                            param.setResult(mono);
                        }
                    } catch (Throwable ignored) {
                    }
                }
            });
        } catch (Throwable t) {
            log(t.getMessage());
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
