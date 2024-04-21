package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.Intent;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class CustomShortcut extends XposedMods {

    private final String packageName = Constants.Packages.SETTINGS;
    private Object TopLevelSettings;
    private boolean showInSettings = true;

    public CustomShortcut(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        showInSettings = Xprefs.getBoolean("show_entry_settings", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(packageName)) return;

        Class<?> SettingsPreferenceFragmentClass = findClass("com.android.settings.SettingsPreferenceFragment", lpparam.classLoader);
        Class<?> HomeTopCategory = findClass("com.oplus.settings.feature.homepage.HomepageTopCategory", lpparam.classLoader);
        Class<?> TopHomePreferenceClass = findClass("com.oplus.settings.widget.preference.SettingsSimpleJumpPreference", lpparam.classLoader);

        Class<?> TopLevelSettingsClass = findClass("com.oplus.settings.feature.homepage.OplusTopLevelSettings", lpparam.classLoader);
        hookAllMethods(TopLevelSettingsClass, "onPreferenceTreeClick", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if("Oxygen Customizer".equals(getObjectField(param.args[0], "mTitle"))) {
                    param.setResult(true);

                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                    mContext.startActivity(intent);
                }
            }
        });

        hookAllConstructors(TopLevelSettingsClass, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                TopLevelSettings = param.thisObject;
            }
        });

        hookAllMethods(SettingsPreferenceFragmentClass, "setPreferenceScreen", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if(!showInSettings) return;

                Object OCPreference = TopHomePreferenceClass.getConstructor(Context.class).newInstance(mContext);

                //personalized_customization_entrance
                Object mWallpaperCategory = callMethod(param.args[0], "findPreference", "notification_settings_category");
                Object mPersonalizePref = callMethod(param.args[0], "findPreference", "personalized_customization_entrance");
                Object resId = callMethod(mPersonalizePref, "getIcon");

                callMethod(OCPreference, "setIcon",
                        resId);
                callMethod(OCPreference, "setTitle", "Oxygen Customizer");
                callMethod(OCPreference, "setOrder", 1);
                callMethod(OCPreference, "setKey", "oxygen_customizer");
                callMethod(mWallpaperCategory, "addPreference", OCPreference);
            }
        });
    }


    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(this.packageName);
    }



}
