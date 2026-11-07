package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import androidx.annotation.NonNull;
import androidx.collection.ArraySet;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;

public class HookHelper {

    public static Set<XposedInterface.HookHandle> hookAllConstructors(Class<?> hookClass, ReflectedClass.ReflectionConsumer callback, boolean runBefore, XposedInterface xposedInterface) {
        ArraySet<XposedInterface.HookHandle> result = new ArraySet<>();
        for (Executable constructor : hookClass.getDeclaredConstructors()) {
            result.add(hookMethod(constructor, callback, runBefore, xposedInterface));
        }
        return result;
    }

    public static Set<XposedInterface.HookHandle> hookAllMethods(Class<?> hookClass, String methodName, ReflectedClass.ReflectionConsumer callback, boolean runBefore, XposedInterface xposedInterface) {
        ArraySet<XposedInterface.HookHandle> result = new ArraySet<>();
        for (Executable method : findMethods(hookClass, methodName)) {
            result.add(hookMethod(method, callback, runBefore, xposedInterface));
        }
        return result;
    }

    public static Set<XposedInterface.HookHandle> interceptAllConstructors(Class<?> hookClass, XposedInterface.Hooker hooker, XposedInterface xposedInterface) {
        ArraySet<XposedInterface.HookHandle> result = new ArraySet<>();
        for (Executable constructor : hookClass.getDeclaredConstructors()) {
            result.add(intercept(constructor, hooker, xposedInterface));
        }
        return result;
    }

    public static Set<XposedInterface.HookHandle> interceptAllMethods(Class<?> hookClass, String methodName, XposedInterface.Hooker hooker, XposedInterface xposedInterface) {
        ArraySet<XposedInterface.HookHandle> result = new ArraySet<>();
        for (Executable method : findMethods(hookClass, methodName)) {
            result.add(intercept(method, hooker, xposedInterface));
        }
        return result;
    }

    @NonNull
    public static XposedInterface.HookHandle intercept(Executable hookMethod, XposedInterface.Hooker hooker, XposedInterface xposedInterface) {
        return xposedInterface.hook(hookMethod).intercept(hooker);
    }

    @SuppressWarnings("unchecked")
    @CanIgnoreReturnValue
    public static <T> T callMethod(Object obj, String methodName, Object... args) {
        return (T) ReflectionHelpers.callMethod(obj, methodName, args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getObjectField(Object obj, String fieldName) {
        return (T) ReflectionHelpers.getObjectField(obj, fieldName);
    }

    public static XposedInterface.HookHandle hookMethod(Executable hookMethod, ReflectedClass.ReflectionConsumer callback, boolean runBefore, XposedInterface xposedInterface) {
        return xposedInterface.hook(hookMethod)
                .intercept(chain -> {
                    RunParam param = new RunParam(chain.getThisObject(), chain.getExecutable(), chain.getArgs().toArray());

                    if (runBefore) {
                        callback.run(param);
                        if (param.returnEarly)
                            return param.getResultOrThrowable(); // skip the original call

                        return chain.proceed(param.args);
                    } else {
                        try {
                            param.result = chain.proceed(param.args);
                        } catch (Throwable t) {
                            param.throwable = t;
                        }
                        callback.run(param);
                        return param.getResultOrThrowable();
                    }
                });
    }

    /**
     * Builds a hooker running {@code before} ahead of the original call and {@code after} once it returns.
     * If {@code before} sets a result or throwable, the original call is skipped but {@code after} still runs.
     */
    public static XposedInterface.Hooker beforeAfter(ReflectedClass.ReflectionConsumer before, ReflectedClass.ReflectionConsumer after) {
        return chain -> {
            RunParam param = new RunParam(chain.getThisObject(), chain.getExecutable(), chain.getArgs().toArray());
            if (before != null) before.run(param);
            if (!param.returnEarly) {
                try {
                    param.result = chain.proceed(param.args);
                } catch (Throwable t) {
                    param.throwable = t;
                }
            }
            if (after != null) after.run(param);
            return param.getResultOrThrowable();
        };
    }

    private static Set<Method> findMethods(Class<?> clazz, String name) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(m -> m.getName().equals(name) && !Modifier.isAbstract(m.getModifiers()))
                .collect(ArraySet::new, ArraySet::add, ArraySet::addAll);
    }

    @SuppressWarnings({"unused"})
    public static class RunParam {
        public Object thisObject;
        public Executable method;
        public Object[] args;
        private Object result;
        private Throwable throwable;
        private boolean returnEarly = false;

        public RunParam(Object thisObject, Executable method, Object[] args) {
            this.thisObject = thisObject;
            this.method = method;
            this.args = args;
        }

        /**
         * Sets the call result. In a "before" callback, the original call is skipped.
         */
        public void setResult(Object result) {
            this.result = result;
            this.throwable = null;
            this.returnEarly = true;
        }

        /**
         * Makes the call throw. In a "before" callback, the original call is skipped.
         */
        public void setThrowable(Throwable throwable) {
            this.throwable = throwable;
            this.result = null;
            this.returnEarly = true;
        }

        @SuppressWarnings("unchecked")
        public <T> T getResult() {
            return (T) result;
        }

        public Throwable getThrowable() {
            return throwable;
        }

        public boolean hasThrowable() {
            return throwable != null;
        }

        public Object getResultOrThrowable() throws Throwable {
            if (throwable != null) throw throwable;
            return result;
        }

        @SuppressWarnings("unchecked")
        public <T> T getThisObject() {
            return (T) thisObject;
        }

        @SuppressWarnings({"unchecked"})
        public <T> T getArg(int index) {
            return (T) args[index];
        }
    }
}
