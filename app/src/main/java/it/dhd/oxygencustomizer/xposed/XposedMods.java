package it.dhd.oxygencustomizer.xposed;

import android.content.Context;
import android.util.Log;

import java.lang.reflect.Field;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.startup.HybridClassLoader;
import it.dhd.oxygencustomizer.xposed.startup.StartupHook;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public abstract class XposedMods {

    protected Context mContext;
    protected boolean mDebug = false;

    public XposedMods(Context context) {
        mContext = context;
    }

    public abstract void updatePrefs(String... Key);

    public abstract void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable;

    protected void initResources() {}

    public final void handleLoadPackageInternal(XC_LoadPackage.LoadPackageParam lpParam) throws Throwable {
        ReflectedClass.setDefaultClassloader(injectClassLoader(lpParam.classLoader));
        handleLoadPackage(lpParam);
    }

    public abstract boolean listensTo(String packageName);

    public void log(String message) {
        if (!mDebug) return;
        XposedBridge.log("[ Oxygen Customizer - " + getClass().getSimpleName() + " ] " + message);
    }

    public void log(Throwable throwable) {
        XposedBridge.log("[ Oxygen Customizer - " + getClass().getSimpleName() + " ] ERROR:" + " \n " + Log.getStackTraceString(throwable));
        XposedBridge.log(throwable);
    }

    private static void injectClassLoader(ClassLoader self, ClassLoader newParent) {
        Log.d("OXYGEN_CUSTOMIZER", "injectClassLoader self: " + self + ", newParent: " + newParent);
        try {
            Field fParent = ClassLoader.class.getDeclaredField("parent");
            fParent.setAccessible(true);
            fParent.set(self, newParent);
        } catch (Exception e) {
            android.util.Log.e("OXYGEN_CUSTOMIZER", "injectClassLoader: failed", e);
        }
    }

    private ClassLoader injectClassLoader(ClassLoader classLoader) {
        if (classLoader == null) {
            throw new NullPointerException("classLoader == null");
        }
        try {
            Field fParent = ClassLoader.class.getDeclaredField("parent");
            fParent.setAccessible(true);
            ClassLoader mine = XposedMods.class.getClassLoader();
            ClassLoader curr = (ClassLoader) fParent.get(mine);
            if (curr == null) {
                curr = XposedBridge.class.getClassLoader();
            }
            if (!curr.getClass().getName().equals(HybridClassLoader.class.getName())) {
                HybridClassLoader hybrid = new HybridClassLoader(curr, classLoader);
                fParent.set(mine, hybrid);
                return hybrid;
            } else {
                return curr;
            }
        } catch (Exception e) {
            XposedBridge.log(e);
            return classLoader;
        }
    }

}
