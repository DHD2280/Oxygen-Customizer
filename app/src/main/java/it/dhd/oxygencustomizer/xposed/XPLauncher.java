package it.dhd.oxygencustomizer.xposed;

import static android.content.Context.CONTEXT_IGNORE_SECURITY;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static it.dhd.oxygencustomizer.BuildConfig.APPLICATION_ID;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.BootLoopProtector.isBootLooped;
import static it.dhd.oxygencustomizer.xposed.utils.SystemUtils.sleep;

import android.annotation.SuppressLint;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.NonNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
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
        isSystemServer = param.isSystemServer();
        Log.e("OXYGEN_CUSTOMIZER", "onModuleLoaded:" + processName + ", isSystemServer: " + isSystemServer);
        Logger.log("[ Oxygen Customizer - XPLauncher ] onModuleLoaded: processName: " + processName + ", isSystemServer: " + isSystemServer);
    }

    @Override
    public void onSystemServerStarting(@NonNull XposedModuleInterface.SystemServerStartingParam SSSP) {
        ReflectedClass.setFrameworkClassloader(injectClassLoader(SSSP.getClassLoader()));
    }

    @Override
    public void onPackageReady(@NonNull PackageReadyParam PRParam) {
        ReflectedClass.setDefaultXposedInterface(this);
        try {
            isChildProcess = PRParam.getPackageName().contains(":");
            processName = PRParam.getPackageName();
        } catch (Throwable ignored) {
            isChildProcess = false;
        }


        if (isSystemServer && (PRParam.getPackageName().equals(Constants.Packages.TELECOM_SERVER_PACKAGE) || PRParam.getPackageName().equals(Constants.Packages.FRAMEWORK))) {
            Logger.log("[ Oxygen Customizer - XPLauncher ] packageName Framework: " + PRParam.getPackageName());
            Class<?> PhoneWindowManager = findClass("com.android.server.policy.PhoneWindowManager", PRParam.getClassLoader());
            hookAllMethods(PhoneWindowManager, "init", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                    Logger.log("[ Oxygen Customizer - XPLauncher ] packageName Framework: PhoneWindowManager init ");
                    try {
                        Logger.log("[ Oxygen Customizer - XPLauncher ] mContext null? " + (mContext == null));
                        if (param.args[0] instanceof Context && mContext == null) {
                            Logger.log("[ Oxygen Customizer - XPLauncher ] PhoneWindowManager param.args[0] instanceof Context");
                            mContext = (Context) param.args[0];
                            Logger.log("[ Oxygen Customizer - XPLauncher ] PhoneWindowManager Context null? " + (mContext == null));

                            moduleResources = mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY)
                                    .getResources();

                            XPrefs.init(mContext);
                        }
                        CompletableFuture.runAsync(() -> waitForXprefsLoad(PRParam));
                    } catch (Throwable t) {
                        Logger.log("[ Oxygen Customizer - XPLauncher ] fault in PhoneWindowManager: " + t);
                    }
                }
            });
//            ReflectedClass PhoneWindowManager = ReflectedClass.of("com.android.server.policy.PhoneWindowManager");
//            PhoneWindowManager
//                    .before("init")
//                    .run(instance, param -> {
//                        Logger.log("[ Oxygen Customizer - XPLauncher ] packageName Framework: PhoneWindowManager init ");
//                        try {
//                            Logger.log("[ Oxygen Customizer - XPLauncher ] mContext null? " + (mContext == null));
//                            if (param.args[0] instanceof Context && mContext == null) {
//                                Logger.log("[ Oxygen Customizer - XPLauncher ] PhoneWindowManager param.args[0] instanceof Context");
//                                mContext = (Context) param.args[0];
//                                Logger.log("[ Oxygen Customizer - XPLauncher ] PhoneWindowManager Context null? " + (mContext == null));
//
//                                moduleResources = mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY)
//                                        .getResources();
//
//                                XPrefs.init(mContext);
//                            }
//                            CompletableFuture.runAsync(() -> waitForXprefsLoad(PRParam));
//                        } catch (Throwable t) {
//                            Logger.log("[ Oxygen Customizer - XPLauncher ] fault in PhoneWindowManager: " + t);
//                        }
//                    });
        }
        if (!isSystemServer || PRParam.getPackageName().equals(Constants.Packages.TELECOM_SERVER_PACKAGE)) {
            ReflectedClass.of(Instrumentation.class)
                    .after("newApplication")
                    .run(this, param -> {
                        try {
                            if (mContext == null || PRParam.getPackageName().equals(Constants.Packages.TELECOM_SERVER_PACKAGE)) { //telecom service launches as a secondary process in framework, but has its own package name. context is not null when it loads
//                                if (Build.VERSION.SDK_INT >= 35 && PRParam.getPackageName().equals(Constants.Packages.TELECOM_SERVER_PACKAGE))
//                                    return;
                                if (param.args[2] == null) return;
                                if (!(param.args[2] instanceof Context)) return;
                                mContext = (Context) param.args[2];

                                moduleResources = mContext.createPackageContext(APPLICATION_ID, CONTEXT_IGNORE_SECURITY)
                                        .getResources();

                                XPrefs.init(mContext);

                                waitForXprefsLoad(PRParam);
                            }
                        } catch (Throwable t) {
                            // Context is null
                            Logger.log("[ Oxygen Customizer - XPLauncher ] Instrumentation error in newApplication: " + t);
                        }
                    });
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
