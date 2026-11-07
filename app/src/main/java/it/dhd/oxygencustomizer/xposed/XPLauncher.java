package it.dhd.oxygencustomizer.xposed;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static it.dhd.oxygencustomizer.BuildConfig.APPLICATION_ID;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.isBootLooped;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.sleep;

import android.annotation.SuppressLint;
import android.app.AppComponentFactory;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.res.Resources;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.IRootProviderProxy;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.startup.HybridClassLoader;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class XPLauncher extends XposedModule implements ServiceConnection {

    public static boolean isChildProcess = false;
    public static String processName = "";
    public static boolean isSystemServer = false;
    private final java.util.concurrent.atomic.AtomicBoolean frameworkLoadStarted = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static ArrayList<XposedMods> runningMods = new ArrayList<>();
    public Context mContext = null;
    public static Resources moduleResources;

    private static IRootProviderProxy rootProxyIPC;
    private static final Queue<ProxyRunnable> proxyQueue = new LinkedList<>();
    @SuppressLint("StaticFieldLeak")
    static XPLauncher instance;

    /**
     * @noinspection FieldCanBeLocal
     */
    public XPLauncher() {
        instance = this;
        Logger.setXposedInterface(this);
    }

    @Override
    public void onModuleLoaded(@NonNull ModuleLoadedParam param) {
        super.onModuleLoaded(param);

        processName = param.getProcessName();
        isChildProcess = processName.contains(":");
        isSystemServer = param.isSystemServer();
        ReflectedClass.setDefaultXposedInterface(this);
        ResourceManager.setModulePath(getModuleApplicationInfo().sourceDir);
        Logger.log("[ Oxygen Customizer - XPLauncher ] onModuleLoaded: processName: " + processName + ", isSystemServer: " + isSystemServer);
    }

    @Override
    public void onSystemServerStarting(@NonNull XposedModuleInterface.SystemServerStartingParam SSSP) {
        // In system server, onPackageLoaded/onPackageReady are NOT called for "android":
        // this callback replaces them, so framework hooks must be installed here.
        ClassLoader systemServerClassLoader = SSSP.getClassLoader();
        ReflectedClass.setFrameworkClassloader(injectClassLoader(systemServerClassLoader));
        ReflectedClass.setDefaultClassloader(systemServerClassLoader);

        PackageReadyParam frameworkParam = new SystemServerPackageParam(systemServerClassLoader);

        Logger.log("[ Oxygen Customizer - XPLauncher ] onSystemServerStarting");
        ReflectedClass.of("com.android.server.policy.PhoneWindowManager", systemServerClassLoader)
                .before("init")
                .run(this, param -> {
                    Logger.log("[ Oxygen Customizer - XPLauncher ] packageName Framework: PhoneWindowManager init ");
                    try {
                        if (param.args[0] instanceof Context && mContext == null) {
                            mContext = (Context) param.args[0];

                            setModuleResources(mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY)
                                    .getResources());

                            XPrefs.init(mContext);
                        }
                        // PhoneWindowManager has more than one init overload: load framework mods only once
                        if (frameworkLoadStarted.compareAndSet(false, true))
                            CompletableFuture.runAsync(() -> waitForXprefsLoad(frameworkParam));
                    } catch (Throwable t) {
                        Logger.log("[ Oxygen Customizer - XPLauncher ] fault in PhoneWindowManager: " + t);
                    }
                });
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam PRParam) {
        String pkgName = PRParam.getPackageName();

        if (isSystemServer) {
            // Framework is handled in onSystemServerStarting.
            // Telecom runs inside system server; old code skipped it on SDK >= 35 as well.
            if (Build.VERSION.SDK_INT >= 35 || !pkgName.equals(Constants.Packages.TELECOM_SERVER_PACKAGE))
                return;
        }

        ReflectedClass.of(Instrumentation.class)
                .after("newApplication")
                .run(this, param -> {
                    try {
                        if (mContext == null || pkgName.equals(Constants.Packages.TELECOM_SERVER_PACKAGE)) { //telecom service launches as a secondary process in framework, but has its own package name. context is not null when it loads
                            if (param.args[2] == null) return;
                            if (!(param.args[2] instanceof Context)) return;
                            mContext = (Context) param.args[2];

                            setModuleResources(mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY)
                                    .getResources());

                            XPrefs.init(mContext);

                            waitForXprefsLoad(PRParam);
                        }
                    } catch (Throwable t) {
                        // Context is null
                        Logger.log("[ Oxygen Customizer - XPLauncher ] Instrumentation error in newApplication: " + t);
                    }
                });
    }

    private static void setModuleResources(Resources resources) {
        moduleResources = resources;
        // Legacy field still used by many hooks
        ResourceManager.modRes = resources;
    }

    /**
     * Minimal {@link PackageReadyParam} for system server, where the API gives no package callback for "android".
     */
    private static final class SystemServerPackageParam implements PackageReadyParam {
        private final ClassLoader classLoader;

        SystemServerPackageParam(ClassLoader classLoader) {
            this.classLoader = classLoader;
        }

        @NonNull
        @Override
        public String getPackageName() {
            return Constants.Packages.FRAMEWORK;
        }

        @NonNull
        @Override
        public ApplicationInfo getApplicationInfo() {
            throw new UnsupportedOperationException("Not available in system server");
        }

        @Override
        public boolean isFirstPackage() {
            return true;
        }

        @NonNull
        @Override
        public ClassLoader getDefaultClassLoader() {
            return classLoader;
        }

        @NonNull
        @Override
        public ClassLoader getClassLoader() {
            return classLoader;
        }

        @NonNull
        @Override
        public AppComponentFactory getAppComponentFactory() {
            throw new UnsupportedOperationException("Not available in system server");
        }
    }

    private void onXPrefsReady(PackageReadyParam PRParam) {
        if (isBootLooped(PRParam.getPackageName())) {
            Logger.log(String.format("Oxygen Customizer: Possible bootloop in %s. Will not load for now", PRParam.getPackageName()));
            return;
        }

        new SystemUtils(mContext);

        loadModpacks(PRParam);
    }

    private void loadModpacks(PackageReadyParam PRParam) {
        String pkgName = PRParam.getPackageName();
        ReflectedClass.setDefaultClassloader(injectClassLoader(PRParam.getClassLoader()));
        if (Arrays.asList(moduleResources.getStringArray(R.array.root_requirement)).contains(pkgName)) {
            Logger.log("Root required package: " + pkgName);
            forceConnectRootService();
        }
        for (Class<? extends XposedMods> mod : ModPacks.getMods(pkgName)) {
            try {
                XposedMods instance = mod.getConstructor(Context.class).newInstance(mContext);
                if (!instance.listensTo(pkgName)) continue;
                try {
                    instance.onPreferenceUpdated();
                } catch (Throwable ignored) {
                }
                instance.initResources();
                instance.onPackageLoaded(PRParam);
                runningMods.add(instance);
            } catch (Throwable T) {
                Logger.log("Start Error Dump - Occurred in " + mod.getName());
                Logger.log(T);
            }
        }
    }

    private void waitForXprefsLoad(PackageReadyParam PRParam) {
        while (true) {
            try {
                Xprefs.getBoolean("LoadTestBooleanValue", false);
                break;
            } catch (Throwable ignored) {
                try {
                    //noinspection BusyWait
                    Thread.sleep(1000);
                } catch (Throwable ignored1) {
                }
            }
        }

        Logger.log("Oxygen Customizer Version: " + BuildConfig.VERSION_NAME + " package: " + PRParam.getPackageName() + " loaded");

        onXPrefsReady(PRParam);
    }

    private void forceConnectRootService() {
        new Thread(() -> {
            while (SystemUtils.UserManager() == null
                    || !SystemUtils.UserManager().isUserUnlocked()) //device is still CE encrypted
            {
                sleep(2000);
            }
            sleep(5000); //wait for the unlocked account to settle down a bit

            while (rootProxyIPC == null) {
                connectRootService();
                sleep(5000);
            }
        }).start();
    }

    private void connectRootService() {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(APPLICATION_ID, APPLICATION_ID + ".services.RootProviderProxy"));
            mContext.bindService(intent, instance, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
        } catch (Throwable t) {
            Logger.log(t);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        rootProxyIPC = IRootProviderProxy.Stub.asInterface(service);
        synchronized (proxyQueue) {
            while (!proxyQueue.isEmpty()) {
                try {
                    Objects.requireNonNull(proxyQueue.poll()).run(rootProxyIPC);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        rootProxyIPC = null;

        forceConnectRootService();
    }

    public static void enqueueProxyCommand(ProxyRunnable runnable) {
        if (rootProxyIPC != null) {
            try {
                runnable.run(rootProxyIPC);
            } catch (RemoteException ignored) {
            }
        } else {
            synchronized (proxyQueue) {
                proxyQueue.add(runnable);
            }
            instance.forceConnectRootService();
        }
    }

    public interface ProxyRunnable {
        void run(IRootProviderProxy proxy) throws RemoteException;
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
                curr = XposedModule.class.getClassLoader();
            }
            if (!curr.getClass().getName().equals(HybridClassLoader.class.getName())) {
                HybridClassLoader hybrid = new HybridClassLoader(curr, classLoader);
                fParent.set(mine, hybrid);
                return hybrid;
            } else {
                return curr;
            }
        } catch (Exception e) {
            Logger.log(e);
            return classLoader;
        }
    }

}