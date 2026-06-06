package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.core.content.res.ResourcesCompat;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class CustomShortcut extends XposedMods {

    private final String listenPackage = SETTINGS;
    private boolean showInSettings = true;
    private Context c;
    private Class<?> ThemeUtils = null;
    private ReflectedClass OOSUtils, COUITintUtil;

    public CustomShortcut(Context context) {
        super(context);

    }

    @Override
    public void updatePrefs(String... Key) {
        showInSettings = Xprefs.getBoolean("show_entry_settings", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        Class<?> TopHomePreferenceClass = findClass("com.oplus.settings.widget.preference.SettingsSimpleJumpPreference", lpparam.classLoader);
        findAndHookConstructor(TopHomePreferenceClass,
                Context.class,
                AttributeSet.class,
                int.class,
                int.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (c == null) {
                            c = (Context) param.args[0];
                        }
                    }
                });
        Class<?> TopLevelSettingsClass = findClass("com.android.settings.homepage.TopLevelSettings", lpparam.classLoader);
        hookAllMethods(TopLevelSettingsClass, "onPreferenceTreeClick", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if ("Oxygen Customizer".equals(getObjectField(param.args[0], "mTitle"))) {
                    param.setResult(true);

                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                    mContext.startActivity(intent);
                }
            }
        });
        try {
            ThemeUtils = findClass("com.oplus.settings.utils.ThemeUtils", lpparam.classLoader);
        } catch (Throwable ignored) {
        }
        OOSUtils = ReflectedClass.ofIfPossible("com.oplus.settings.utils.OOSUtils");
        COUITintUtil = ReflectedClass.ofIfPossible("com.coui.appcompat.tintimageview.COUITintUtil");
        hookAllMethods(TopLevelSettingsClass, "onCreateAdapter", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (!showInSettings) return;

                Object OCPreference = TopHomePreferenceClass.getConstructor(Context.class)
                        .newInstance(c);

                Object mCategory = null;
                String[] possibleCategories = new String[]{
                        "personality_settings_category", // OOS14-15
                        "notification_settings_category",
                        "system_settings_category" // OOS16+
                };
                for (String category : possibleCategories) {
                    try {
                        mCategory = callMethod(param.args[0], "findPreference", category);
                        if (mCategory != null) break;
                    } catch (Throwable ignored) {
                    }
                }

                Drawable OCIcon = ResourcesCompat.getDrawable(ResourceManager.modRes,
                        Build.VERSION.SDK_INT >= 35 ?
                                R.drawable.ic_navbar_mods_unchecked :
                                R.drawable.pref_icon,
                        mContext.getTheme());
                Drawable tinted;
                if (ThemeUtils == null) {
                    tinted = OCIcon;
                } else {
                    try {
                        if (Build.VERSION.SDK_INT >= 35 && OOSUtils.getClazz() != null) {
                            // Check two tone
                            boolean isTwoTone = (boolean) callStaticMethod(OOSUtils.getClazz(), "isTwoToneTheme", mContext);
                            if (isTwoTone) {
                                int resId = mContext.getResources().getIdentifier("oos_vector_stroke_color", "color", SETTINGS);
                                int color = ResourcesCompat.getColor(c.getResources(), resId, c.getTheme());
                                tinted = (Drawable) callStaticMethod(COUITintUtil.getClazz(), "tintDrawable", OCIcon, color);
                            } else {
                                tinted = (Drawable) callStaticMethod(ThemeUtils, "getApplyCOUITintDrawable", c, OCIcon, true);
                            }
                        } else {
                            tinted = (Drawable) callStaticMethod(ThemeUtils, "getApplyCOUITintDrawable", c, OCIcon, true);
                        }
                    } catch (Throwable t) {
                        tinted = OCIcon;
                    }
                }
                callMethod(OCPreference, "setIcon",
                        tinted);
                callMethod(OCPreference, "setTitle", "Oxygen Customizer");
                callMethod(OCPreference, "setOrder", Integer.MIN_VALUE);
                callMethod(OCPreference, "setKey", "oxygen_customizer");
                callMethod(mCategory, "addPreference", OCPreference);
            }
        });
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(this.listenPackage);
    }


}
