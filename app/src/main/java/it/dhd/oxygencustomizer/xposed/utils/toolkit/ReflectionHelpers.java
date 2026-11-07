package it.dhd.oxygencustomizer.xposed.utils.toolkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plain Java reflection helpers (no Xposed dependency).
 * <p>
 * Lookups are cached. Method / constructor resolution picks the most specific overload
 * compatible with the runtime argument types (autoboxing and primitive widening supported,
 * {@code null} matches any reference type).
 * <p>
 * Checked reflection exceptions are rethrown unchecked; exceptions thrown by the invoked
 * target are rethrown as-is (not wrapped in {@link InvocationTargetException}).
 *
 * @noinspection unused
 */
public final class ReflectionHelpers {

    private static final Map<String, Optional<Field>> fieldCache = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Method>> methodCache = new ConcurrentHashMap<>();
    private static final Map<String, Optional<Constructor<?>>> constructorCache = new ConcurrentHashMap<>();
    private static final WeakHashMap<Object, HashMap<String, Object>> additionalFields = new WeakHashMap<>();

    private static final ClassLoader BOOT_CLASS_LOADER = android.content.Context.class.getClassLoader();

    private ReflectionHelpers() {
    }

    // region Classes

    @NonNull
    public static Class<?> findClass(@NonNull String className, @Nullable ClassLoader classLoader) {
        try {
            return Class.forName(className, false, classLoader == null ? BOOT_CLASS_LOADER : classLoader);
        } catch (ClassNotFoundException e) {
            throw sneakyThrow(e);
        }
    }

    @Nullable
    public static Class<?> findClassIfExists(@NonNull String className, @Nullable ClassLoader classLoader) {
        try {
            return findClass(className, classLoader);
        } catch (Throwable ignored) {
            return null;
        }
    }

    // endregion

    // region Fields

    @NonNull
    public static Field findField(@NonNull Class<?> clazz, @NonNull String fieldName) {
        String key = clazz.getName() + '#' + fieldName;
        Optional<Field> cached = fieldCache.computeIfAbsent(key, k -> Optional.ofNullable(lookupField(clazz, fieldName)));
        return cached.orElseThrow(() -> new NoSuchFieldError(key));
    }

    @Nullable
    public static Field findFieldIfExists(@NonNull Class<?> clazz, @NonNull String fieldName) {
        try {
            return findField(clazz, fieldName);
        } catch (NoSuchFieldError ignored) {
            return null;
        }
    }

    @Nullable
    private static Field lookupField(Class<?> clazz, String fieldName) {
        for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
            try {
                Field field = c.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }
        return null;
    }

    public static Object getObjectField(@NonNull Object obj, @NonNull String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).get(obj);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * Returns the outer instance of an inner class instance.
     */
    public static Object getSurroundingThis(@NonNull Object obj) {
        return getObjectField(obj, "this$0");
    }

    public static boolean getBooleanField(@NonNull Object obj, @NonNull String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getBoolean(obj);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static int getIntField(@NonNull Object obj, @NonNull String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getInt(obj);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static long getLongField(@NonNull Object obj, @NonNull String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getLong(obj);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static float getFloatField(@NonNull Object obj, @NonNull String fieldName) {
        try {
            return findField(obj.getClass(), fieldName).getFloat(obj);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setObjectField(@NonNull Object obj, @NonNull String fieldName, Object value) {
        try {
            findField(obj.getClass(), fieldName).set(obj, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setBooleanField(@NonNull Object obj, @NonNull String fieldName, boolean value) {
        try {
            findField(obj.getClass(), fieldName).setBoolean(obj, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setIntField(@NonNull Object obj, @NonNull String fieldName, int value) {
        try {
            findField(obj.getClass(), fieldName).setInt(obj, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setLongField(@NonNull Object obj, @NonNull String fieldName, long value) {
        try {
            findField(obj.getClass(), fieldName).setLong(obj, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setFloatField(@NonNull Object obj, @NonNull String fieldName, float value) {
        try {
            findField(obj.getClass(), fieldName).setFloat(obj, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static Object getStaticObjectField(@NonNull Class<?> clazz, @NonNull String fieldName) {
        try {
            return findField(clazz, fieldName).get(null);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static int getStaticIntField(@NonNull Class<?> clazz, @NonNull String fieldName) {
        try {
            return findField(clazz, fieldName).getInt(null);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static boolean getStaticBooleanField(@NonNull Class<?> clazz, @NonNull String fieldName) {
        try {
            return findField(clazz, fieldName).getBoolean(null);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setStaticObjectField(@NonNull Class<?> clazz, @NonNull String fieldName, Object value) {
        try {
            findField(clazz, fieldName).set(null, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    /**
     * Sets a static final object field. ART rejects {@link Field#set} on static final fields
     * (checked against the native field flags), so the value is written with {@code Unsafe}
     * at the field's ART offset inside the declaring class, where static fields are stored.
     */
    public static void setStaticFinalObjectField(@NonNull Field field, Object value) {
        if (!Modifier.isStatic(field.getModifiers()) || field.getType().isPrimitive())
            throw new IllegalArgumentException("Not a static object field: " + field);
        if (value != null && !field.getType().isInstance(value))
            throw new IllegalArgumentException("Value type mismatch for " + field);
        try {
            if (!Modifier.isFinal(field.getModifiers())) {
                field.setAccessible(true);
                field.set(null, value);
                return;
            }
            UnsafeAccess unsafe = UnsafeAccess.get();
            long offset = unsafe.fieldOffset.getInt(field);
            unsafe.putObject.invoke(unsafe.instance, field.getDeclaringClass(), offset, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getCause());
        }
    }

    private static final class UnsafeAccess {
        private static volatile UnsafeAccess sInstance;

        final Object instance;
        final Method putObject;
        /**
         * ART {@code java.lang.reflect.Field#offset}
         */
        final Field fieldOffset;

        private UnsafeAccess(Object instance, Method putObject, Field fieldOffset) {
            this.instance = instance;
            this.putObject = putObject;
            this.fieldOffset = fieldOffset;
        }

        static UnsafeAccess get() throws IllegalAccessException {
            UnsafeAccess result = sInstance;
            if (result != null) return result;
            Class<?> unsafeClass = findClass("sun.misc.Unsafe", null);
            Object instance = null;
            for (Field f : org.lsposed.hiddenapibypass.HiddenApiBypass.getStaticFields(unsafeClass)) {
                if (f.getType() == unsafeClass) {
                    f.setAccessible(true);
                    instance = f.get(null);
                    break;
                }
            }
            Field offset = null;
            for (Field f : org.lsposed.hiddenapibypass.HiddenApiBypass.getInstanceFields(Field.class)) {
                if (f.getName().equals("offset")) {
                    f.setAccessible(true);
                    offset = f;
                    break;
                }
            }
            if (instance == null || offset == null)
                throw new UnsupportedOperationException("Unsafe static field write not available");
            Method putObject = findMethodExact(unsafeClass, "putObject", Object.class, long.class, Object.class);
            result = new UnsafeAccess(instance, putObject, offset);
            sInstance = result;
            return result;
        }
    }

    public static void setStaticIntField(@NonNull Class<?> clazz, @NonNull String fieldName, int value) {
        try {
            findField(clazz, fieldName).setInt(null, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    public static void setStaticBooleanField(@NonNull Class<?> clazz, @NonNull String fieldName, boolean value) {
        try {
            findField(clazz, fieldName).setBoolean(null, value);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    // endregion

    // region Methods

    /**
     * Finds a method with the exact parameter types, searching superclasses too.
     * Parameter types can be {@link Class} instances or fully qualified class names.
     */
    @NonNull
    public static Method findMethodExact(@NonNull Class<?> clazz, @NonNull String methodName, Object... parameterTypes) {
        Class<?>[] types = resolveTypes(clazz.getClassLoader(), parameterTypes);
        String key = clazz.getName() + '#' + methodName + Arrays.toString(types) + "#exact";
        Optional<Method> cached = methodCache.computeIfAbsent(key, k -> {
            for (Class<?> c = clazz; c != null; c = c.getSuperclass()) {
                try {
                    Method m = c.getDeclaredMethod(methodName, types);
                    m.setAccessible(true);
                    return Optional.of(m);
                } catch (NoSuchMethodException ignored) {
                }
            }
            return Optional.empty();
        });
        return cached.orElseThrow(() -> new NoSuchMethodError(key));
    }

    @Nullable
    public static Method findMethodExactIfExists(@NonNull Class<?> clazz, @NonNull String methodName, Object... parameterTypes) {
        try {
            return findMethodExact(clazz, methodName, parameterTypes);
        } catch (Throwable ignored) {
            return null;
        }
    }

    /**
     * Finds the most specific method compatible with the given parameter types.
     */
    @NonNull
    public static Method findMethodBestMatch(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>... parameterTypes) {
        String key = clazz.getName() + '#' + methodName + Arrays.toString(parameterTypes) + "#best";
        Optional<Method> cached = methodCache.computeIfAbsent(key, k -> Optional.ofNullable(lookupBestMethod(clazz, methodName, parameterTypes)));
        return cached.orElseThrow(() -> new NoSuchMethodError(key));
    }

    /**
     * Finds the most specific method compatible with the runtime types of the given arguments.
     */
    @NonNull
    public static Method findMethodBestMatch(@NonNull Class<?> clazz, @NonNull String methodName, Object... args) {
        return findMethodBestMatch(clazz, methodName, typesOf(args));
    }

    @Nullable
    private static Method lookupBestMethod(Class<?> clazz, String methodName, Class<?>[] argTypes) {
        Method best = null;
        float bestCost = Float.MAX_VALUE;
        boolean declaringClass = true;
        for (Class<?> c = clazz; c != null; c = c.getSuperclass(), declaringClass = false) {
            for (Method m : c.getDeclaredMethods()) {
                // private methods of superclasses are not reachable from a subclass instance
                if (!declaringClass && Modifier.isPrivate(m.getModifiers())) continue;
                if (!m.getName().equals(methodName)) continue;
                float cost = matchCost(m.getParameterTypes(), argTypes);
                if (cost < bestCost) {
                    best = m;
                    bestCost = cost;
                }
            }
        }
        if (best == null) {
            // interface default methods
            for (Method m : clazz.getMethods()) {
                if (!m.getName().equals(methodName)) continue;
                float cost = matchCost(m.getParameterTypes(), argTypes);
                if (cost < bestCost) {
                    best = m;
                    bestCost = cost;
                }
            }
        }
        if (best != null) best.setAccessible(true);
        return best;
    }

    public static Object callMethod(@NonNull Object obj, @NonNull String methodName, Object... args) {
        return invoke(findMethodBestMatch(obj.getClass(), methodName, args), obj, args);
    }

    public static Object callMethod(@NonNull Object obj, @NonNull String methodName, Class<?>[] parameterTypes, Object... args) {
        return invoke(findMethodBestMatch(obj.getClass(), methodName, parameterTypes), obj, args);
    }

    public static Object callStaticMethod(@NonNull Class<?> clazz, @NonNull String methodName, Object... args) {
        return invoke(findMethodBestMatch(clazz, methodName, args), null, args);
    }

    public static Object callStaticMethod(@NonNull Class<?> clazz, @NonNull String methodName, Class<?>[] parameterTypes, Object... args) {
        return invoke(findMethodBestMatch(clazz, methodName, parameterTypes), null, args);
    }

    private static Object invoke(Method method, Object obj, Object[] args) {
        try {
            return method.invoke(obj, args);
        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getCause() != null ? e.getCause() : e);
        } catch (IllegalAccessException e) {
            throw sneakyThrow(e);
        }
    }

    // endregion

    // region Constructors

    @NonNull
    public static Constructor<?> findConstructorExact(@NonNull Class<?> clazz, Object... parameterTypes) {
        Class<?>[] types = resolveTypes(clazz.getClassLoader(), parameterTypes);
        String key = clazz.getName() + Arrays.toString(types) + "#exact";
        Optional<Constructor<?>> cached = constructorCache.computeIfAbsent(key, k -> {
            try {
                Constructor<?> c = clazz.getDeclaredConstructor(types);
                c.setAccessible(true);
                return Optional.of(c);
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        });
        return cached.orElseThrow(() -> new NoSuchMethodError(key));
    }

    @Nullable
    public static Constructor<?> findConstructorExactIfExists(@NonNull Class<?> clazz, Object... parameterTypes) {
        try {
            return findConstructorExact(clazz, parameterTypes);
        } catch (Throwable ignored) {
            return null;
        }
    }

    @NonNull
    public static Constructor<?> findConstructorBestMatch(@NonNull Class<?> clazz, Class<?>... parameterTypes) {
        String key = clazz.getName() + Arrays.toString(parameterTypes) + "#best";
        Optional<Constructor<?>> cached = constructorCache.computeIfAbsent(key, k -> {
            Constructor<?> best = null;
            float bestCost = Float.MAX_VALUE;
            for (Constructor<?> c : clazz.getDeclaredConstructors()) {
                float cost = matchCost(c.getParameterTypes(), parameterTypes);
                if (cost < bestCost) {
                    best = c;
                    bestCost = cost;
                }
            }
            if (best != null) best.setAccessible(true);
            return Optional.ofNullable(best);
        });
        return cached.orElseThrow(() -> new NoSuchMethodError(key));
    }

    public static Object newInstance(@NonNull Class<?> clazz, Object... args) {
        Constructor<?> constructor = findConstructorBestMatch(clazz, typesOf(args));
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getCause() != null ? e.getCause() : e);
        } catch (IllegalAccessException | InstantiationException e) {
            throw sneakyThrow(e);
        }
    }

    public static Object newInstance(@NonNull Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        Constructor<?> constructor = findConstructorBestMatch(clazz, parameterTypes);
        try {
            return constructor.newInstance(args);
        } catch (InvocationTargetException e) {
            throw sneakyThrow(e.getCause() != null ? e.getCause() : e);
        } catch (IllegalAccessException | InstantiationException e) {
            throw sneakyThrow(e);
        }
    }

    // endregion

    // region Additional instance fields

    /**
     * Attaches a value to an object instance without modifying its class. Entries are weakly
     * referenced by the instance.
     */
    public static Object setAdditionalInstanceField(@NonNull Object obj, @NonNull String key, Object value) {
        synchronized (additionalFields) {
            return additionalFields.computeIfAbsent(obj, k -> new HashMap<>()).put(key, value);
        }
    }

    public static Object getAdditionalInstanceField(@NonNull Object obj, @NonNull String key) {
        synchronized (additionalFields) {
            HashMap<String, Object> fields = additionalFields.get(obj);
            return fields == null ? null : fields.get(key);
        }
    }

    public static Object removeAdditionalInstanceField(@NonNull Object obj, @NonNull String key) {
        synchronized (additionalFields) {
            HashMap<String, Object> fields = additionalFields.get(obj);
            return fields == null ? null : fields.remove(key);
        }
    }

    // endregion

    // region Matching

    private static Class<?>[] typesOf(Object[] args) {
        if (args == null) return new Class<?>[0];
        Class<?>[] types = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            types[i] = args[i] == null ? null : args[i].getClass();
        }
        return types;
    }

    private static Class<?>[] resolveTypes(ClassLoader loader, Object[] parameterTypes) {
        if (parameterTypes == null) return new Class<?>[0];
        Class<?>[] types = new Class<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
            Object type = parameterTypes[i];
            if (type instanceof Class<?> c) {
                types[i] = c;
            } else if (type instanceof String name) {
                types[i] = findClass(name, loader);
            } else {
                throw new IllegalArgumentException("Parameter type must be Class or String: " + type);
            }
        }
        return types;
    }

    /**
     * @return cost of passing {@code argTypes} to {@code paramTypes}, or {@link Float#MAX_VALUE}
     * if not compatible. Lower is more specific.
     */
    private static float matchCost(Class<?>[] paramTypes, Class<?>[] argTypes) {
        if (paramTypes.length != argTypes.length) return Float.MAX_VALUE;
        float total = 0f;
        for (int i = 0; i < paramTypes.length; i++) {
            float cost = argCost(argTypes[i], paramTypes[i]);
            if (cost == Float.MAX_VALUE) return Float.MAX_VALUE;
            total += cost;
        }
        return total;
    }

    private static final Class<?>[] PRIMITIVE_ORDER = {byte.class, short.class, char.class, int.class, long.class, float.class, double.class};

    private static float argCost(@Nullable Class<?> arg, @NonNull Class<?> param) {
        if (arg == null) return param.isPrimitive() ? Float.MAX_VALUE : 1.5f;
        if (param.isPrimitive()) {
            float cost = 0f;
            Class<?> prim = arg;
            if (!arg.isPrimitive()) {
                prim = unbox(arg);
                if (prim == null) return Float.MAX_VALUE;
                cost += 0.1f;
            }
            if (prim == param) return cost;
            int from = indexOf(prim), to = indexOf(param);
            // widening only; char does not widen to short and byte/short do not widen to char
            if (from < 0 || to < 0 || from >= to) return Float.MAX_VALUE;
            if (prim == char.class && param == short.class) return Float.MAX_VALUE;
            if ((prim == byte.class || prim == short.class) && param == char.class)
                return Float.MAX_VALUE;
            return cost + 0.1f * (to - from);
        }
        Class<?> ref = arg.isPrimitive() ? box(arg) : arg;
        if (!param.isAssignableFrom(ref)) return Float.MAX_VALUE;
        if (param == ref) return arg.isPrimitive() ? 0.1f : 0f;
        if (param.isInterface()) return 0.25f;
        float cost = 0f;
        for (Class<?> c = ref; c != null && c != param; c = c.getSuperclass()) cost++;
        return cost;
    }

    private static int indexOf(Class<?> prim) {
        for (int i = 0; i < PRIMITIVE_ORDER.length; i++) if (PRIMITIVE_ORDER[i] == prim) return i;
        return -1;
    }

    @Nullable
    private static Class<?> unbox(Class<?> c) {
        if (c == Integer.class) return int.class;
        if (c == Boolean.class) return boolean.class;
        if (c == Long.class) return long.class;
        if (c == Float.class) return float.class;
        if (c == Double.class) return double.class;
        if (c == Byte.class) return byte.class;
        if (c == Short.class) return short.class;
        if (c == Character.class) return char.class;
        return null;
    }

    private static Class<?> box(Class<?> c) {
        if (c == int.class) return Integer.class;
        if (c == boolean.class) return Boolean.class;
        if (c == long.class) return Long.class;
        if (c == float.class) return Float.class;
        if (c == double.class) return Double.class;
        if (c == byte.class) return Byte.class;
        if (c == short.class) return Short.class;
        if (c == char.class) return Character.class;
        return c;
    }

    // endregion

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

    /**
     * Name of an executable, for logging.
     */
    public static String describe(Executable executable) {
        return executable.getDeclaringClass().getName() + '#' + executable.getName();
    }
}
