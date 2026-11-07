package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.findMethodExactIfExists;

import android.content.res.Resources;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

/**
 * Exposes module resources to the hooked app through fake resource IDs.
 * <p>
 * Replacement for legacy {@code XResources.addResource}, which is not available with libxposed API:
 * a fake ID is handed to host code (e.g. a launcher shortcut taking a resource ID), and
 * {@link Resources} lookups for that ID are served from the module's own resources.
 */
public final class ModuleResourceInjector {

    /**
     * Package id 0x7e is not used by apps (0x7f) nor by the framework (0x01).
     */
    private static int sNextId = 0x7e7f0000;

    private static final Map<Integer, Integer> sDrawables = new ConcurrentHashMap<>();
    private static final Map<Integer, Integer> sStrings = new ConcurrentHashMap<>();
    private static boolean sHooked = false;

    private ModuleResourceInjector() {
    }

    /**
     * @param moduleResId a drawable ID of the module (e.g. {@code R.drawable.x})
     * @return a fake ID that resolves to the module drawable in the hooked process
     */
    public static synchronized int addDrawable(int moduleResId) {
        ensureHooked();
        int fakeId = sNextId++;
        sDrawables.put(fakeId, moduleResId);
        return fakeId;
    }

    /**
     * @param moduleResId a string ID of the module (e.g. {@code R.string.x})
     * @return a fake ID that resolves to the module string in the hooked process
     */
    public static synchronized int addString(int moduleResId) {
        ensureHooked();
        int fakeId = sNextId++;
        sStrings.put(fakeId, moduleResId);
        return fakeId;
    }

    private static void ensureHooked() {
        if (sHooked) return;
        sHooked = true;

        ReflectedClass resources = ReflectedClass.of(Resources.class);

        hookDrawable(resources, findMethodExactIfExists(Resources.class, "getDrawable", int.class, Resources.Theme.class));
        hookDrawable(resources, findMethodExactIfExists(Resources.class, "getDrawableForDensity", int.class, int.class, Resources.Theme.class));

        Method getText = findMethodExactIfExists(Resources.class, "getText", int.class);
        if (getText != null) {
            resources.before(getText).run(param -> {
                Integer moduleId = sStrings.get((int) param.args[0]);
                if (moduleId != null && ResourceManager.modRes != null)
                    param.setResult(ResourceManager.modRes.getText(moduleId));
            });
        }
        Method getString = findMethodExactIfExists(Resources.class, "getString", int.class);
        if (getString != null) {
            resources.before(getString).run(param -> {
                Integer moduleId = sStrings.get((int) param.args[0]);
                if (moduleId != null && ResourceManager.modRes != null)
                    param.setResult(ResourceManager.modRes.getString(moduleId));
            });
        }
    }

    private static void hookDrawable(ReflectedClass resources, Method method) {
        if (method == null) return;
        resources.before(method).run(param -> {
            Integer moduleId = sDrawables.get((int) param.args[0]);
            if (moduleId != null && ResourceManager.modRes != null)
                param.setResult(ResourceManager.modRes.getDrawable(moduleId, null));
        });
    }
}
