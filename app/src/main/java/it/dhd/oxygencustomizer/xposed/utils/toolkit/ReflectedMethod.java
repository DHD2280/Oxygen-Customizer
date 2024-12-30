package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import static de.robv.android.xposed.XposedHelpers.findMethodExact;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

import de.robv.android.xposed.XC_MethodHook;

/*
    Reflection Toolkit by @Siavash79/PixelXpert
 */
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

    public Collection<? extends XC_MethodHook.Unhook> beforeThat(ReflectedClass.ReflectionConsumer consumer) {
        return ReflectedClass.of(method.getClass())
                .before(method)
                .run(consumer);
    }

    public Set<XC_MethodHook.Unhook> afterThat(ReflectedClass.ReflectionConsumer consumer) {
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

    public Object invoke(Object object, Object... args) throws Throwable {
        return method.invoke(object, args);
    }
}
