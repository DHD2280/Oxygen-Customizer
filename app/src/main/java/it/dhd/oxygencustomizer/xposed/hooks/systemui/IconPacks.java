package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.TYPE_DRAWABLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.IDMapping;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.Mapping;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.OverlayID;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.OverlayIDName;

public class IconPacks extends XposedMods {

    static IDMapping drawableMapping = new IDMapping();
    private final android.content.pm.PackageManager mPackageManager;
    private final List<XC_MethodHook.Unhook> hooks = new ArrayList<>();
    private boolean mPackageLoaded = false;

    public IconPacks(Context context) {
        super(context);
        mPackageManager = mContext.getPackageManager();
    }

    @Override
    public void updatePrefs(String... Key) {
        updateMapping();
    }

    private void hookAll() {
        if (hooks.isEmpty() && mPackageLoaded && !drawableMapping.isEmpty()) {
            hooks.add(findAndHookMethod(Resources.class, "getDrawable", int.class, Resources.Theme.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Drawable drawable = getDrawable((int) param.args[0], (Resources.Theme) param.args[1]);
                    if (drawable != null) {
                        param.setResult(drawable);
                    }
                }
            }));

            hooks.add(findAndHookMethod(Resources.class, "getDrawable", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Drawable drawable = getDrawable((int) param.args[0], mContext.getTheme());
                    if (drawable != null) {
                        param.setResult(drawable);
                    }
                }
            }));

            hooks.add(findAndHookMethod(Resources.class, "getDrawableForDensity", int.class, int.class, Resources.Theme.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Drawable drawable = getDrawable((int) param.args[0], (Resources.Theme) param.args[2]);
                    if (drawable != null) {
                        param.setResult(drawable);
                    }

                }
            }));

            hooks.add(findAndHookMethod(Resources.class, "getDrawableForDensity", int.class, int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Drawable drawable = getDrawable((int) param.args[0], mContext.getTheme());
                    if (drawable != null) {
                        param.setResult(drawable);
                    }
                }
            }));
        }
    }

    private void unhookAll() {
        for (XC_MethodHook.Unhook hook : hooks) {
            hook.unhook();
        }
        hooks.clear();
    }

    private void updateMapping() {
        new Thread(() -> {
            drawableMapping = getIDMapping(TYPE_DRAWABLE, "drawable");
            if (drawableMapping.isEmpty()) {
                unhookAll();
            } else {
                hookAll();
            }
        }).start();
    }

    /**
     * @noinspection SameParameterValue
     */
    private IDMapping getIDMapping(int prefType, String type) {
        Mapping prefMapping = Mapping.loadMapping(Xprefs, prefType);

        IDMapping idMapping = new IDMapping();
        for (String key : prefMapping.keySet()) {
            try {
                OverlayIDName overlayIDName = prefMapping.get(key);

                int mappingID = getMappingID(key, type);

                //noinspection DataFlowIssue
                int replacementID = getReplacementID(overlayIDName, type);

                if (replacementID == 0 || mappingID == 0) //some id cannot be found. ignore
                {
                    continue;
                }

                idMapping.put(mappingID, new OverlayID(overlayIDName.packageName, replacementID));
            } catch (Throwable ignored) {
            }
        }
        return idMapping;
    }

    @SuppressLint("DiscouragedApi")
    private int getReplacementID(OverlayIDName overlayIDName, String type) throws PackageManager.NameNotFoundException {
        return mPackageManager.getResourcesForApplication(overlayIDName.packageName).getIdentifier(overlayIDName.resName, type, overlayIDName.packageName);
    }

    @SuppressLint("DiscouragedApi")
    private int getMappingID(String key, String type) {
        String[] keyParts = key.split(":");
        String resName = keyParts[keyParts.length - 1];

        String sourcePackage = keyParts.length > 1 ? keyParts[0] : mContext.getPackageName();
        if (sourcePackage.equals("module")) {
            sourcePackage = BuildConfig.APPLICATION_ID;
        }

        return mContext.getResources().getIdentifier(resName, type, sourcePackage);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        mPackageLoaded = true;
        hookAll();
    }

    private Drawable getDrawable(int id, Resources.Theme theme) throws Throwable {
        if (drawableMapping.containsKey(id)) {
            OverlayID overlayID = drawableMapping.get(id);

            //noinspection DataFlowIssue
            Resources res = mPackageManager.getResourcesForApplication(overlayID.packageName);

            return Drawable.createFromXml(res, res.getXml(overlayID.resID), theme);
        }
        return null;
    }

    @Override
    public boolean listensTo(String packageName) {
        return true;
    }

}

