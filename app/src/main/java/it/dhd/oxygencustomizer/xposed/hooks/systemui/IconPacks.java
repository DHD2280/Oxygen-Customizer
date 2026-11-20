package it.dhd.oxygencustomizer.xposed.hooks.systemui;

import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.TYPE_DRAWABLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.IDMapping;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.Mapping;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.OverlayID;
import it.dhd.oxygencustomizer.xposed.utils.ThemePackMapping.OverlayIDName;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers;

public class IconPacks extends XposedMods {

    static IDMapping drawableMapping = new IDMapping();
    private static Map<Integer, String> sourcePackages = new ConcurrentHashMap<>();
    private final PackageManager mPackageManager;
    private final List<XposedInterface.HookHandle> hooks = new ArrayList<>();
    private final Map<String, Resources> mPackResources = new ConcurrentHashMap<>();
    private final ThreadLocal<Boolean> mLoading = ThreadLocal.withInitial(() -> false);
    private boolean mPackageLoaded = false;

    public IconPacks(Context context) {
        super(context);
        mPackageManager = mContext.getPackageManager();
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        updateMapping();
    }

    private void hookAll() {
        if (hooks.isEmpty() && mPackageLoaded && !drawableMapping.isEmpty()) {
            Class<?> resourcesImpl = ReflectionHelpers.findClass("android.content.res.ResourcesImpl", Resources.class.getClassLoader());
            hooks.addAll(ReflectedClass.of(resourcesImpl)
                    .before(ReflectionHelpers.findMethodExact(resourcesImpl, "loadDrawable", Resources.class, TypedValue.class, int.class, int.class, Resources.Theme.class))
                    .run(param -> {
                        Resources.Theme theme = (Resources.Theme) param.args[4];
                        Drawable drawable = getDrawable((Resources) param.args[0], (int) param.args[2], (int) param.args[3], theme != null ? theme : mContext.getTheme());
                        if (drawable != null) {
                            param.setResult(drawable);
                        }
                    }));
        }
    }

    private void unhookAll() {
        for (XposedInterface.HookHandle hook : hooks) {
            hook.unhook();
        }
        hooks.clear();
    }

    private void updateMapping() {
        new Thread(() -> {
            Map<Integer, String> sources = new ConcurrentHashMap<>();
            IDMapping mapping = getIDMapping(TYPE_DRAWABLE, "drawable", sources);
            mPackResources.clear();
            sourcePackages = sources;
            drawableMapping = mapping;
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
    private IDMapping getIDMapping(int prefType, String type, Map<Integer, String> sources) {
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
                sources.put(mappingID, getSourcePackage(key));
            } catch (Throwable ignored) {
            }
        }
        return idMapping;
    }

    @SuppressLint("DiscouragedApi")
    private int getReplacementID(OverlayIDName overlayIDName, String type) throws PackageManager.NameNotFoundException {
        return mPackageManager.getResourcesForApplication(overlayIDName.packageName).getIdentifier(overlayIDName.resName, type, overlayIDName.packageName);
    }

    private String getSourcePackage(String key) {
        String[] keyParts = key.split(":");
        String sourcePackage = keyParts.length > 1 ? keyParts[0] : mContext.getPackageName();
        return sourcePackage.equals("module") ? BuildConfig.APPLICATION_ID : sourcePackage;
    }

    @SuppressLint("DiscouragedApi")
    private int getMappingID(String key, String type) {
        String[] keyParts = key.split(":");
        String resName = keyParts[keyParts.length - 1];
        return mContext.getResources().getIdentifier(resName, type, getSourcePackage(key));
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        mPackageLoaded = true;
        hookAll();
    }

    private Drawable getDrawable(Resources caller, int id, int density, Resources.Theme theme) {
        OverlayID overlayID = drawableMapping.get(id);
        if (overlayID == null || mLoading.get()) return null;
        mLoading.set(true);
        try {
            String source = sourcePackages.get(id);
            if (source != null && !source.equals(caller.getResourcePackageName(id))) return null;
            Resources res = getPackResources(overlayID.packageName, caller.getConfiguration());
            return density == 0 ? res.getDrawable(overlayID.resID, theme) : res.getDrawableForDensity(overlayID.resID, density, theme);
        } catch (Throwable ignored) {
            return null;
        } finally {
            mLoading.set(false);
        }
    }

    private Resources getPackResources(String packageName, Configuration config) throws PackageManager.NameNotFoundException {
        String key = packageName + "|" + (config.uiMode & Configuration.UI_MODE_NIGHT_MASK) + "|" + config.densityDpi;
        Resources res = mPackResources.get(key);
        if (res == null) {
            res = mContext.createPackageContext(packageName, 0).createConfigurationContext(config).getResources();
            mPackResources.put(key, res);
        }
        return res;
    }

    @Override
    public boolean listensTo(String packageName) {
        return true;
    }

}

