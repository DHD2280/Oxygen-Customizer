package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.findClassIfExists;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.HookHelper.hookAllMethods;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.HookHelper.hookMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.Logger.log;

import android.annotation.SuppressLint;
import android.util.ArraySet;

import androidx.annotation.NonNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

import de.robv.android.xposed.XposedHelpers;
import io.github.libxposed.api.XposedInterface;

/**
 * @noinspection unused
 */
public class ReflectedClass {

    private static ClassLoader defaultClassloader = null;
    private static ClassLoader frameworkClassloader = null;
    private static XposedInterface defaultXposedInterface;
    private static final boolean FLAG_DEBUG_HOOKS = false;
    Class<?> clazz;

    public ReflectedClass(Class<?> clazz) {
        this.clazz = clazz;
    }

    public static ReflectedClass of(Class<?> clazz) {
        return new ReflectedClass(clazz);
    }

    public static ReflectedClass of(String name, ClassLoader loader) {
        try {
            return new ReflectedClass(findClass(name, loader));
        } catch (Throwable ignored) {
            return new ReflectedClass(findClass(name, frameworkClassloader));
        }
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
            } catch (Throwable ignored) {
            }
        }
        return new ReflectedClass(null);
    }

    public static void setFrameworkClassloader(ClassLoader frameworkClassloader) {
        ReflectedClass.frameworkClassloader = frameworkClassloader;
    }

    public static void setDefaultClassloader(ClassLoader classloader) {
        defaultClassloader = classloader;
    }

    public static void setDefaultXposedInterface(XposedInterface xposedInterface) {
        defaultXposedInterface = xposedInterface;
    }


    public void dumpStructure() {
        Method[] ms = clazz.getDeclaredMethods();
        log("Class: " + clazz.getName());
        //noinspection DataFlowIssue
        log("extends: " + clazz.getSuperclass().getName());
        log("Subclasses:");
        Class<?>[] scs = clazz.getClasses();
        for (Class<?> c : scs) {
            log(c.getName());
        }
        log("Methods:");

        Constructor<?>[] cons = clazz.getDeclaredConstructors();
        for (Constructor<?> m : cons) {
            log(m.getName() + " - " + " - " + m.getParameterCount());
            Class<?>[] cs = m.getParameterTypes();
            for (Class<?> c : cs) {
                log("\t\t" + c.getTypeName());
            }
        }

        for (Method m : ms) {
            log(m.getName() + " - " + m.getReturnType() + " - " + m.getParameterCount());
            Class<?>[] cs = m.getParameterTypes();
            for (Class<?> c : cs) {
                log("\t\t" + c.getTypeName());
            }
        }
        log("Fields:");

        Field[] fs = clazz.getDeclaredFields();
        for (Field f : fs) {
            log("\t\t" + f.getName() + "-" + f.getType().getName());
        }
        log("End dump");
    }


    public Class<?> getClazz() {
        return clazz;
    }

    public static ReflectedClass ofIfPossible(String name, ClassLoader loader) {
        Class<?> result = findClassIfExists(name, loader);
        if (result == null && frameworkClassloader != null) {
            result = findClassIfExists(name, frameworkClassloader);
        }
        return new ReflectedClass(result);
    }

    public static ReflectedClass ofIfPossible(String name) {
        return ReflectedClass.ofIfPossible(name, defaultClassloader);
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

    public Set<Method> findMethods(Pattern namePattern) {
        return findMethods(clazz, namePattern);
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

        protected Set<XposedInterface.HookHandle> runBefore(XposedInterface xposedInterface, ReflectionConsumer consumer) {
            return runBefore(xposedInterface, consumer, false);
        }

        /**
         * @noinspection SameParameterValue
         */
        @SuppressLint("DefaultLocale")
        protected Set<XposedInterface.HookHandle> runBefore(XposedInterface xposedInterface, ReflectionConsumer consumer, boolean log) {
            if (clazz == null) return new ArraySet<>();

            Set<XposedInterface.HookHandle> unhooks;
            if (isConstructor) {
                unhooks = HookHelper.hookAllConstructors(clazz, param -> {
                    if (log) {
                        log(param.method.getName() + " called");
                    }
                    consumer.run(param);
                }, true, xposedInterface);

                if (log || FLAG_DEBUG_HOOKS) {
                    StackTraceElement element = Thread.currentThread().getStackTrace()[2];
                    String callingClassName = element.getClassName();
                    int lineNumber = element.getLineNumber();
                    log(String.format("%s line %d: Hook to before constructor of %s size = %d", callingClassName, lineNumber, clazz.getName(), unhooks.size()));
                }
            } else if (method != null) {
                unhooks = Collections.singleton(hookMethod(method, param -> {
                    if (log) {
                        log(param.method.getName() + " called");
                    }
                    consumer.run(param);
                }, true, xposedInterface));

                if (log || FLAG_DEBUG_HOOKS) {
                    StackTraceElement element = Thread.currentThread().getStackTrace()[2];
                    String callingClassName = element.getClassName();
                    int lineNumber = element.getLineNumber();
                    log(String.format("%s line %d: Hook to %s before method %s size = %d", callingClassName, lineNumber, clazz.getName(), method.getName(), unhooks.size()));
                }
            } else {
                unhooks = hookAllMethods(clazz, methodName, param -> {
                    if (log) {
                        log(param.method.getName() + " called");
                    }
                    consumer.run(param);
                }, true, xposedInterface);

                if (log || FLAG_DEBUG_HOOKS) {
                    StackTraceElement element = Thread.currentThread().getStackTrace()[2];
                    String callingClassName = element.getClassName();
                    int lineNumber = element.getLineNumber();
                    log(String.format("%s line %d: Hook to %s before method %s size = %d", callingClassName, lineNumber, clazz.getName(), methodName, unhooks.size()));
                }
            }
            return unhooks;
        }

        protected Set<XposedInterface.HookHandle> runAfter(XposedInterface xposedInterface, ReflectionConsumer consumer) {
            return runAfter(xposedInterface, consumer, false);
        }

        @SuppressLint("DefaultLocale")
        protected Set<XposedInterface.HookHandle> runAfter(XposedInterface xposedInterface, ReflectionConsumer consumer, boolean log) {
            if (clazz == null) return new ArraySet<>();

            Set<XposedInterface.HookHandle> unhooks;
            if (isConstructor) {
                unhooks = HookHelper.hookAllConstructors(clazz, param -> {
                    if (log) {
                        log(param.method.getName() + " called");
                    }
                    consumer.run(param);
                }, false, xposedInterface);

                if (log || FLAG_DEBUG_HOOKS) {
                    StackTraceElement element = Thread.currentThread().getStackTrace()[2];
                    String callingClassName = element.getClassName();
                    int lineNumber = element.getLineNumber();
                    log(String.format("%s line %d: Hook to after constructor of %s size = %d", callingClassName, lineNumber, clazz.getName(), unhooks.size()));
                }
            } else if (method != null) {
                unhooks = Collections.singleton(hookMethod(method, param -> {
                    if (log) {
                        log(param.method.getName() + " called");
                    }
                    consumer.run(param);
                }, false, xposedInterface));

                if (log || FLAG_DEBUG_HOOKS) {
                    StackTraceElement element = Thread.currentThread().getStackTrace()[2];
                    String callingClassName = element.getClassName();
                    int lineNumber = element.getLineNumber();
                    log(String.format("%s line %d: Hook to %s after method %s size = %d", callingClassName, lineNumber, clazz.getName(), method.getName(), unhooks.size()));
                }
            } else {
                unhooks = hookAllMethods(clazz, methodName, param -> {
                    if (log) {
                        log(param.method.getName() + " called");
                    }
                    consumer.run(param);
                }, false, xposedInterface);

                if (log || FLAG_DEBUG_HOOKS) {
                    StackTraceElement element = Thread.currentThread().getStackTrace()[2];
                    String callingClassName = element.getClassName();
                    int lineNumber = element.getLineNumber();
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

        public Set<XposedInterface.HookHandle> run(ReflectionConsumer consumer) {
            return runBefore(defaultXposedInterface, consumer, false);
        }

        public Set<XposedInterface.HookHandle> run(ReflectionConsumer consumer, boolean log) {
            return runBefore(defaultXposedInterface, consumer, log);
        }

        public Set<XposedInterface.HookHandle> run(XposedInterface xposedInterface, ReflectionConsumer consumer) {
            return runBefore(xposedInterface, consumer, false);
        }

        public Set<XposedInterface.HookHandle> run(XposedInterface xposedInterface, ReflectionConsumer consumer, boolean log) {
            return runBefore(xposedInterface, consumer, log);
        }
    }

    public class BeforeMethodDatas {
        Set<BeforeMethodData> datas = new ArraySet<>();

        public BeforeMethodDatas(Class<?> clazz, Pattern namePattern) {
            findMethods(clazz, namePattern).forEach(method -> datas.add(new BeforeMethodData(clazz, method.getName(), null, false)));
        }

        public Set<XposedInterface.HookHandle> run(ReflectionConsumer consumer) {
            return run(defaultXposedInterface, consumer);
        }

        public Set<XposedInterface.HookHandle> run(XposedInterface xposedInterface, ReflectionConsumer consumer) {
            Set<XposedInterface.HookHandle> unhooks = new ArraySet<>();
            datas.forEach(data -> unhooks.addAll(data.run(xposedInterface, consumer)));
            return unhooks;
        }
    }

    public class AfterMethodDatas {
        Set<AfterMethodData> datas = new ArraySet<>();

        public AfterMethodDatas(Class<?> clazz, Pattern namePattern) {
            findMethods(clazz, namePattern).forEach(method -> datas.add(new AfterMethodData(clazz, method.getName(), null, false)));
        }

        public Set<XposedInterface.HookHandle> run(ReflectionConsumer consumer) {
            return run(defaultXposedInterface, consumer);
        }

        public Set<XposedInterface.HookHandle> run(XposedInterface xposedInterface, ReflectionConsumer consumer) {
            Set<XposedInterface.HookHandle> unhooks = new ArraySet<>();
            datas.forEach(data -> unhooks.addAll(data.run(xposedInterface, consumer)));
            return unhooks;
        }
    }

    private static Set<Method> findMethods(Class<?> clazz, Pattern namePattern) {
        Set<Method> result = new ArraySet<>();

        Method[] methods = clazz.getDeclaredMethods();

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

        public Set<XposedInterface.HookHandle> run(ReflectionConsumer consumer) {
            return runAfter(defaultXposedInterface, consumer, false);
        }

        public Set<XposedInterface.HookHandle> run(XposedInterface xposedInterface, ReflectionConsumer consumer) {
            return runAfter(xposedInterface, consumer, false);
        }

        public Set<XposedInterface.HookHandle> run(ReflectionConsumer consumer, boolean log) {
            return runAfter(defaultXposedInterface, consumer, log);
        }

        public Set<XposedInterface.HookHandle> run(XposedInterface xposedInterface, ReflectionConsumer consumer, boolean log) {
            return runAfter(xposedInterface, consumer, log);
        }
    }

    /**
     * Finds FIRST instance of a class by hooking to every METHOD defined in it.
     * Obviously, won't be able to find any instance if there's no method defined or called in that class.
     * <br>
     * It's useful if the constructor is removed due to build optimizations
     *
     * @param foundCallback callback that will be called once instance was found, and delivers the instance captured
     */
    public void findFirstInstance(InstanceFoundCallback foundCallback) {
        findFirstInstance(defaultXposedInterface, foundCallback);
    }

    public void findFirstInstance(XposedInterface xposedInterface, InstanceFoundCallback foundCallback) {
        Set<XposedInterface.HookHandle> unhooks = new ArraySet<>();
        findMethods(Pattern.compile(".+"))
                .forEach(method -> unhooks.addAll(before(method).run(xposedInterface, param -> {
                    unhooks.forEach(XposedInterface.HookHandle::unhook);
                    unhooks.clear();
                    foundCallback.onInstanceCaptured(param.thisObject);
                })));
    }

    public interface InstanceFoundCallback {
        void onInstanceCaptured(@NonNull Object instance);
    }


    public interface ReflectionConsumer {
        void run(HookHelper.RunParam param) throws Throwable;
    }
}