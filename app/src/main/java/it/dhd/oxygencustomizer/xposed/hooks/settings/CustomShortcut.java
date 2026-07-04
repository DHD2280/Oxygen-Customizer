package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPLauncher.moduleResources;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.core.content.res.ResourcesCompat;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class CustomShortcut extends XposedMods {

    private final String listenPackage = SETTINGS;
    private boolean showInSettings = true;
    private Context c;
    private ReflectedClass ThemeUtils = null;
    private ReflectedClass OOSUtils, COUITintUtil;

    public CustomShortcut(Context context) {
        super(context);

    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        showInSettings = Xprefs.getBoolean("show_entry_settings", true);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {

        ReflectedClass TopHomePreferenceClass = ReflectedClass.of("com.oplus.settings.widget.preference.SettingsSimpleJumpPreference");
        TopHomePreferenceClass
                .afterConstruction()
                .run(param -> {
                    if (c == null) {
                        c = (Context) param.args[0];
                    }
                });

        ReflectedClass TopLevelSettingsClass = ReflectedClass.of("com.android.settings.homepage.TopLevelSettings");
        TopLevelSettingsClass
                .before("onPreferenceTreeClick")
                .run(param -> {
                    if ("Oxygen Customizer".equals(getObjectField(param.args[0], "mTitle"))) {
                        param.setResult(true);

                        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(BuildConfig.APPLICATION_ID);
                        mContext.startActivity(intent);
                    }
                });
        ThemeUtils = ReflectedClass.ofIfPossible("com.oplus.settings.utils.ThemeUtils");
        OOSUtils = ReflectedClass.ofIfPossible("com.oplus.settings.utils.OOSUtils");
        COUITintUtil = ReflectedClass.ofIfPossible("com.coui.appcompat.tintimageview.COUITintUtil");

        TopLevelSettingsClass
                .before("onCreateAdapter")
                .run(param -> {
                    if (!showInSettings) return;

                    Object OCPreference = TopHomePreferenceClass.getClazz().getConstructor(Context.class)
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

                    Drawable OCIcon = ResourcesCompat.getDrawable(moduleResources,
                            Build.VERSION.SDK_INT >= 35 ?
                                    R.drawable.ic_navbar_mods_unchecked :
                                    R.drawable.pref_icon,
                            mContext.getTheme());
                    Drawable tinted;
                    if (ThemeUtils.getClazz() == null) {
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
                                    tinted = (Drawable) callStaticMethod(ThemeUtils.getClazz(), "getApplyCOUITintDrawable", c, OCIcon, true);
                                }
                            } else {
                                tinted = (Drawable) callStaticMethod(ThemeUtils.getClazz(), "getApplyCOUITintDrawable", c, OCIcon, true);
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
                });

    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(this.listenPackage);
    }


}
