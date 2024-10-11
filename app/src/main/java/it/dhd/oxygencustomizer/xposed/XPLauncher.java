package it.dhd.oxygencustomizer.xposed;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
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
import android.os.IBinder;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.IRootProviderProxy;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;

public class XPLauncher implements ServiceConnection {

    public static boolean isChildProcess = false;
    public static String processName = "";

    public static ArrayList<XposedMods> runningMods = new ArrayList<>();
    public Context mContext = null;

    private static IRootProviderProxy rootProxyIPC;
    private static final Queue<ProxyRunnable> proxyQueue = new LinkedList<>();
    @SuppressLint("StaticFieldLeak")
    static XPLauncher instance;

    /**
     * @noinspection FieldCanBeLocal
     */
    public XPLauncher() {
        instance = this;
    }

    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        try {
            isChildProcess = lpparam.processName.contains(":");
            processName = lpparam.processName;
        } catch (Throwable ignored) {
            isChildProcess = false;
        }


        if (lpparam.packageName.equals(Constants.Packages.FRAMEWORK)) {
            Class<?> PhoneWindowManagerExtImpl = findClass("com.android.server.policy.PhoneWindowManagerExtImpl", lpparam.classLoader);
            hookAllMethods(PhoneWindowManagerExtImpl, "overrideInit", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    log("packageName Framework: PhoneWindowManagerExtImpl overrideInit ");
                    try {
                            log("PhoneWindowManagerExtImpl " + (mContext != null));
                            mContext = (Context) param.args[1];

                            ResourceManager.modRes = mContext.createPackageContext(APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)
                                    .getResources();

                            XPrefs.init(mContext);

                            CompletableFuture.runAsync(() -> waitForXprefsLoad(lpparam));
                    } catch (Throwable t) {
                        log("fault in PhoneWindowManagerExtImpl: " + t);
                    }
                }
            });
        } else {
            findAndHookMethod(Instrumentation.class, "newApplication", ClassLoader.class, String.class, Context.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    try {
                        if ((mContext == null || lpparam.packageName.equals(Constants.Packages.TELECOM_SERVER_PACKAGE)) && param.args[2] != null) { //telecom service launches as a secondary process in framework, but has its own package name. context is not null when it loads
                            mContext = (Context) param.args[2];

                            ResourceManager.modRes = mContext.createPackageContext(APPLICATION_ID, Context.CONTEXT_IGNORE_SECURITY)
                                    .getResources();

                            XPrefs.init(mContext);

                            waitForXprefsLoad(lpparam);
                        }
                    } catch (Throwable t) {
                        // Context is null
                        log("Instrumentation newApplication: " + t);
                    }
                }
            });
        }
    }

    private void onXPrefsReady(XC_LoadPackage.LoadPackageParam lpparam) {
        if (isBootLooped(lpparam.packageName)) {
            log(String.format("Oxygen Customizer: Possible bootloop in %s. Will not load for now", lpparam.packageName));
            return;
        }

        new SystemUtils(mContext);

        loadModpacks(lpparam);
    }

    private void loadModpacks(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Arrays.asList(ResourceManager.modRes.getStringArray(R.array.root_requirement)).contains(lpparam.packageName)) {
            log("Root required package: " + lpparam.packageName);
            forceConnectRootService();
        }
        for (Class<? extends XposedMods> mod : ModPacks.getMods(lpparam.packageName)) {
            try {
                XposedMods instance = mod.getConstructor(Context.class).newInstance(mContext);
                if (!instance.listensTo(lpparam.packageName)) continue;
                try {
                    instance.updatePrefs();
                } catch (Throwable ignored) {
                }
                instance.handleLoadPackage(lpparam);
                runningMods.add(instance);
            } catch (Throwable T) {
                log("Start Error Dump - Occurred in " + mod.getName());
                log(T);
            }
        }
    }

    private void waitForXprefsLoad(XC_LoadPackage.LoadPackageParam lpparam) {
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

        log("Oxygen Customizer Version: " + BuildConfig.VERSION_NAME + " package: " + lpparam.packageName + " loaded");

        onXPrefsReady(lpparam);
    }

    private void forceConnectRootService()
    {
        new Thread(() -> {
            while(SystemUtils.UserManager() == null
                    || !SystemUtils.UserManager().isUserUnlocked()) //device is still CE encrypted
            {
                sleep(2000);
            }
            sleep(5000); //wait for the unlocked account to settle down a bit

            while(rootProxyIPC == null)
            {
                connectRootService();
                sleep(5000);
            }
        }).start();
    }

    private void connectRootService()
    {
        try {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(APPLICATION_ID, APPLICATION_ID + ".services.RootProviderProxy"));
            mContext.bindService(intent, instance, Context.BIND_AUTO_CREATE | Context.BIND_ADJUST_WITH_ACTIVITY);
        }
        catch (Throwable t)
        {
            log(t);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        rootProxyIPC = IRootProviderProxy.Stub.asInterface(service);
        synchronized (proxyQueue)
        {
            while(!proxyQueue.isEmpty())
            {
                try
                {
                    Objects.requireNonNull(proxyQueue.poll()).run(rootProxyIPC);
                }
                catch (Throwable ignored){}
            }
        }
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        rootProxyIPC = null;

        forceConnectRootService();
    }

    public static void enqueueProxyCommand(ProxyRunnable runnable)
    {
        if(rootProxyIPC != null)
        {
            try {
                runnable.run(rootProxyIPC);
            } catch (RemoteException ignored) {}
        }
        else
        {
            synchronized (proxyQueue) {
                proxyQueue.add(runnable);
            }
            instance.forceConnectRootService();
        }
    }

    public interface ProxyRunnable
    {
        void run(IRootProviderProxy proxy) throws RemoteException;
    }

}
