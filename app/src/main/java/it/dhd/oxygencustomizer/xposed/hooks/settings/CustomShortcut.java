package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookConstructor;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;

import androidx.core.content.res.ResourcesCompat;

import org.w3c.dom.Attr;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class CustomShortcut extends XposedMods {

    private final String packageName = Constants.Packages.SETTINGS;
    private boolean showInSettings = true;
    private AttributeSet attrs;
    private Context c;
    private int i1, i2;

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

        Class<?> TopHomePreferenceClass = findClass("com.oplus.settings.widget.preference.SettingsSimpleJumpPreference", lpparam.classLoader);
        findAndHookConstructor(TopHomePreferenceClass,
                Context.class,
                AttributeSet.class,
                int.class,
                int.class,
                new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (attrs == null) {
                    c = (Context) param.args[0];
                    attrs = (AttributeSet) param.args[1];
                    i1 = (int) param.args[2];
                    i2 = (int) param.args[3];
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
        Class<?> COUITintUtil = findClass("com.coui.appcompat.tintimageview.COUITintUtil", lpparam.classLoader);
        Class<?> ThemeUtils = findClass("com.oplus.settings.utils.ThemeUtils", lpparam.classLoader);
        hookAllMethods(TopLevelSettingsClass, "onCreateAdapter", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if(!showInSettings) return;

                Object OCPreference = TopHomePreferenceClass.getConstructor(Context.class)
                        .newInstance(c);

                Object mWallpaperCategory = callMethod(param.args[0], "findPreference", "notification_settings_category");

                Drawable OCIcon = ResourcesCompat.getDrawable(ResourceManager.modRes,
                        R.drawable.pref_icon,
                        mContext.getTheme());
                Drawable tinted;
                try {
                    tinted = (Drawable) callStaticMethod(ThemeUtils, "getApplyCOUITintDrawable", c, OCIcon, true);
                } catch (Throwable t) {
                    tinted = OCIcon;
                }
                callMethod(OCPreference, "setIcon",
                        tinted);
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
