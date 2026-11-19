package it.dhd.oxygencustomizer.utils;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.FRAMEWORK;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import io.github.libxposed.service.XposedService;
import io.github.libxposed.service.XposedServiceHelper;

public final class XposedScope {

    private static final String SYSTEM_SCOPE = "system";
    private static final List<Runnable> sListeners = new CopyOnWriteArrayList<>();
    private static volatile XposedService sService;
    private static boolean sRegistered = false;

    private XposedScope() {
    }

    public static synchronized void init() {
        if (sRegistered) return;
        sRegistered = true;
        XposedServiceHelper.registerListener(new XposedServiceHelper.OnServiceListener() {
            @Override
            public void onServiceBind(XposedService service) {
                sService = service;
                notifyListeners();
            }

            @Override
            public void onServiceDied(XposedService service) {
                if (sService == service) sService = null;
                notifyListeners();
            }
        });
    }

    public static void addListener(Runnable listener) {
        sListeners.add(listener);
    }

    public static void removeListener(Runnable listener) {
        sListeners.remove(listener);
    }

    private static void notifyListeners() {
        for (Runnable listener : sListeners) listener.run();
    }

    public static boolean isAvailable() {
        return sService != null;
    }

    public static Boolean isInScope(String packageName) {
        List<String> scope = getScope();
        if (scope == null) return null;
        if (FRAMEWORK.equals(packageName))
            return scope.contains(SYSTEM_SCOPE) || scope.contains(FRAMEWORK);
        return scope.contains(packageName);
    }

    private static List<String> getScope() {
        XposedService service = sService;
        if (service == null) return null;
        try {
            return service.getScope();
        } catch (Throwable t) {
            return null;
        }
    }

    public static boolean requestScope(String packageName, XposedService.OnScopeEventListener listener) {
        XposedService service = sService;
        if (service == null) return false;
        try {
            service.requestScope(Collections.singletonList(FRAMEWORK.equals(packageName) ? SYSTEM_SCOPE : packageName), listener);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }
}
