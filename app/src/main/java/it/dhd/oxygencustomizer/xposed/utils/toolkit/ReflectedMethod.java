package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import static de.robv.android.xposed.XposedBridge.invokeOriginalMethod;
import static de.robv.android.xposed.XposedHelpers.findMethodExact;


import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import io.github.libxposed.api.XposedInterface;

/**
 * @noinspection unused
 */
public class ReflectedMethod {
    Method method;

    private ReflectedMethod(Method method) {
        this.method = method;
    }

    public static ReflectedMethod of(Method method) {
        return new ReflectedMethod(method);
    }

    public static ReflectedMethod ofExactData(ReflectedClass reflectedClass, String name, Class<?>... parameterTypes) {
        return new ReflectedMethod(findMethodExact(reflectedClass.getClazz(), name, parameterTypes));
    }

    public static ReflectedMethod ofName(ReflectedClass reflectedClass, String exactName) {
        return new ReflectedMethod(findMethod(reflectedClass.getClazz(), exactName));
    }

    public Collection<XposedInterface.HookHandle> beforeThat(ReflectedClass.ReflectionConsumer consumer) {
        return ReflectedClass.of(method.getClass())
                .before(method)
                .run(consumer);
    }

    public Set<XposedInterface.HookHandle> afterThat(ReflectedClass.ReflectionConsumer consumer) {
        return ReflectedClass.of(method.getClass())
                .after(method)
                .run(consumer);
    }

    public static Method findMethod(Class<?> clazz, String namePattern) {
        Method[] methods = clazz.getMethods();

        for (Method method : methods) {
            if (Pattern.matches(namePattern, method.getName())) {
                return method;
            }
        }
        return null;
    }

    public Object invokeOriginal(Object object, Object... args) throws Throwable {
        return invokeOriginalMethod(method, object, args);
    }

    public Object invoke(Object object, Object... args) throws Throwable {
        return method.invoke(object, args);
    }
}
