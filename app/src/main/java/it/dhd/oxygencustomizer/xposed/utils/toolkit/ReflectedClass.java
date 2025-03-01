package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.hookMethod;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.ArraySet;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import it.dhd.oxygencustomizer.BuildConfig;

/*
    Reflection Toolkit by @Siavash79/PixelXpert
 */

/**
 * @noinspection unused
 */
public class ReflectedClass {

    private static ClassLoader defaultClassloader = null;
    private static final String APP_VERSION_NAME = BuildConfig.VERSION_NAME;
    private static final boolean FLAG_DEBUG_HOOKS = APP_VERSION_NAME.contains("nightly") || APP_VERSION_NAME.contains("beta");
    Class<?> clazz;

    public ReflectedClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static ReflectedClass of(Class<?> clazz) {
        return new ReflectedClass(clazz);
    }

    public static ReflectedClass of(String name, ClassLoader loader) {
        return new ReflectedClass(findClass(name, loader));
    }

    public static ReflectedClass of(String name) {
        return ReflectedClass.of(name, defaultClassloader);
    }

    public static ReflectedClass of(String... classes) {
        for (String clazz : classes) {
            try {
                Class<?> foundClass = findClass(clazz, defaultClassloader);
                if (foundClass != null) {
                    return ReflectedClass.of(foundClass);
                }
            } catch (Throwable ignored) {}
        }
        return new ReflectedClass(null);
    }

    public static ReflectedClass of(ClassLoader loader, String... classes) {
        for (String clazz : classes) {
            try {
                Class<?> foundClass = findClass(clazz, loader);
                if (foundClass != null) {
                    return ReflectedClass.of(foundClass);
                }
            } catch (Throwable ignored) {}
        }
        return new ReflectedClass(null);
    }

    public static void setDefaultClassloader(ClassLoader classloader) {
        if (defaultClassloader == null)
            defaultClassloader = classloader;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public static ReflectedClass ofIfPossible(String name, ClassLoader loader) {
        return new ReflectedClass(findClassIfExists(name, loader));
    }

    public static ReflectedClass ofIfPossible(String name) {
        return ReflectedClass.ofIfPossible(name, defaultClassloader);
    }

    public HookedMethodData hook(String methodName) {
        return new HookedMethodData(clazz, methodName, null, false);
    }

    public HookedMethodData hook(Method method) {
        return new HookedMethodData(method.getClass(), null, method, false);
    }

    public HookedMethodData hookConstructor(String methodName) {
        return new HookedMethodData(clazz, methodName, null, true);
    }

    public HookedMethodData hookConstructor(Method method) {
        return new HookedMethodData(method.getClass(), null, method, true);
    }

    public BeforeMethodData before(Method method) {
        return new BeforeMethodData(method.getClass(), null, method, false);
    }

    public BeforeMethodData before(String methodName) {
        return new BeforeMethodData(clazz, methodName, null, false);
    }

    public BeforeMethodDatas before(Pattern pattern) {
        return new BeforeMethodDatas(clazz, pattern);
    }

    public BeforeMethodData beforeConstruction() {
        return new BeforeMethodData(clazz, null, null, true);
    }

    public AfterMethodData after(String methodName) {
        return new AfterMethodData(clazz, methodName, null, false);
    }

    public AfterMethodData after(Method method) {
        return new AfterMethodData(method.getClass(), null, method, false);
    }

    public AfterMethodDatas after(Pattern pattern) {
        return new AfterMethodDatas(clazz, pattern);
    }

    public AfterMethodData afterConstruction() {
        return new AfterMethodData(clazz, null, null, true);
    }

    public Object callStaticMethod(String methodName, Object... args) {
        return XposedHelpers.callStaticMethod(clazz, methodName, args);
    }

    private static class MethodData {
        String methodName;
        Class<?> clazz;
        boolean isConstructor;
        Method method;

        private MethodData(Class<?> clazz, String name, Method method, boolean isConstructor) {
            this.clazz = clazz;
            this.methodName = name;
            this.isConstructor = isConstructor;
            this.method = method;
        }

        @SuppressLint("DefaultLocale")
        protected Set<XC_MethodHook.Unhook> runBefore(ReflectionConsumer consumer) {
            if (clazz == null) return new ArraySet<>();

            Set<XC_MethodHook.Unhook> unhooks;
            if (isConstructor) {
                unhooks = hookAllConstructors(clazz, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        consumer.run(param);
                    }
                });

                if (FLAG_DEBUG_HOOKS) {
                    Throwable throwable = new Throwable();
                    String callingClassName = throwable.getStackTrace()[2].getClassName();
                    int lineNumber = throwable.getStackTrace()[2].getLineNumber();
                    log(String.format("%s line %d: Hook to before constructor of %s size = %d", callingClassName, lineNumber, clazz.getName(), unhooks.size()));
                }
            } else if (method != null) {
                unhooks = Collections.singleton(hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        consumer.run(param);
                    }
                }));

                if (FLAG_DEBUG_HOOKS) {
                    Throwable throwable = new Throwable();
                    String callingClassName = throwable.getStackTrace()[2].getClassName();
                    int lineNumber = throwable.getStackTrace()[2].getLineNumber();
                    log(String.format("%s line %d: Hook to %s before method %s size = %d", callingClassName, lineNumber, clazz.getName(), method.getName(), unhooks.size()));
                }
            } else {
                unhooks = hookAllMethods(clazz, methodName, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        consumer.run(param);
                    }
                });

                if (FLAG_DEBUG_HOOKS) {
                    Throwable throwable = new Throwable();
                    String callingClassName = throwable.getStackTrace()[2].getClassName();
                    int lineNumber = throwable.getStackTrace()[2].getLineNumber();
                    log(String.format("%s line %d: Hook to %s before method %s size = %d", callingClassName, lineNumber, clazz.getName(), methodName, unhooks.size()));
                }
            }
            return unhooks;
        }

        @SuppressLint("DefaultLocale")
        protected Set<XC_MethodHook.Unhook> runAfter(ReflectionConsumer consumer) {
            if (clazz == null) return new ArraySet<>();

            Set<XC_MethodHook.Unhook> unhooks;
            if (isConstructor) {
                unhooks = hookAllConstructors(clazz, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        consumer.run(param);
                    }
                });

                if (FLAG_DEBUG_HOOKS) {
                    Throwable throwable = new Throwable();
                    String callingClassName = throwable.getStackTrace()[2].getClassName();
                    int lineNumber = throwable.getStackTrace()[2].getLineNumber();
                    log(String.format("%s line %d: Hook to after constructor of %s size = %d", callingClassName, lineNumber, clazz.getName(), unhooks.size()));
                }

            } else if (method != null) {
                unhooks = Collections.singleton(hookMethod(method, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        consumer.run(param);
                    }
                }));

                if (FLAG_DEBUG_HOOKS) {
                    Throwable throwable = new Throwable();
                    String callingClassName = throwable.getStackTrace()[2].getClassName();
                    int lineNumber = throwable.getStackTrace()[2].getLineNumber();
                    log(String.format("%s line %d: Hook to %s after method %s size = %d", callingClassName, lineNumber, clazz.getName(), method.getName(), unhooks.size()));
                }
            } else {
                unhooks = hookAllMethods(clazz, methodName, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        consumer.run(param);
                    }
                });

                if (FLAG_DEBUG_HOOKS) {
                    Throwable throwable = new Throwable();
                    String callingClassName = throwable.getStackTrace()[2].getClassName();
                    int lineNumber = throwable.getStackTrace()[2].getLineNumber();
                    log(String.format("%s line %d: Hook to %s after method %s size = %d", callingClassName, lineNumber, clazz.getName(), methodName, unhooks.size()));
                }

            }
            return unhooks;
        }
    }

    public class BeforeMethodData extends MethodData {
        private BeforeMethodData(Class<?> clazz, String name, Method method, boolean isConstructor) {
            super(clazz, name, method, isConstructor);
        }

        public Set<XC_MethodHook.Unhook> run(ReflectionConsumer consumer) {
            return runBefore(consumer);
        }
    }

    public class BeforeMethodDatas {
        Set<BeforeMethodData> datas = new ArraySet<>();

        public BeforeMethodDatas(Class<?> clazz, Pattern namePattern) {
            findMethods(clazz, namePattern).forEach(method -> datas.add(new BeforeMethodData(clazz, method.getName(), null, false)));
        }

        public Set<XC_MethodHook.Unhook> run(ReflectionConsumer consumer) {
            Set<XC_MethodHook.Unhook> unhooks = new ArraySet<>();
            datas.forEach(data -> unhooks.addAll(data.run(consumer)));
            return unhooks;
        }
    }

    public class AfterMethodDatas {
        Set<AfterMethodData> datas = new ArraySet<>();

        public AfterMethodDatas(Class<?> clazz, Pattern namePattern) {
            findMethods(clazz, namePattern).forEach(method -> datas.add(new AfterMethodData(clazz, method.getName(), null, false)));
        }

        public Set<XC_MethodHook.Unhook> run(ReflectionConsumer consumer) {
            Set<XC_MethodHook.Unhook> unhooks = new ArraySet<>();
            datas.forEach(data -> unhooks.addAll(data.run(consumer)));
            return unhooks;
        }
    }

    public class HookedMethodData extends MethodData {

        public HookedMethodData(Class<?> clazz, String name, Method method, boolean isConstructor) {
            super(clazz, name, method, isConstructor);
        }

        public HookedMethodData after(ReflectionConsumer consumer) {
            runAfter(consumer);
            return this;
        }

        public HookedMethodData before(ReflectionConsumer consumer) {
            runBefore(consumer);
            return this;
        }
    }

    private static Set<Method> findMethods(Class<?> clazz, Pattern namePattern) {
        Set<Method> result = new ArraySet<>();

        Method[] methods = clazz.getMethods();
        Method[] declaredMethods = clazz.getDeclaredMethods();

        for (Method method : declaredMethods) {
            if (namePattern.matcher(method.getName()).matches()) {
                result.add(method);
            }
        }
        for (Method method : methods) {
            if (namePattern.matcher(method.getName()).matches()) {
                result.add(method);
            }
        }
        return result;
    }

    public class AfterMethodData extends MethodData {
        private AfterMethodData(Class<?> clazz, String name, Method method, boolean isConstructor) {
            super(clazz, name, method, isConstructor);
        }

        public Set<XC_MethodHook.Unhook> run(ReflectionConsumer consumer) {
            return runAfter(consumer);
        }
    }

    public interface ReflectionConsumer {
        void run(XC_MethodHook.MethodHookParam param) throws Throwable;
    }
}