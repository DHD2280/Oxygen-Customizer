package it.dhd.oxygencustomizer.xposed.utils;

public class Helpers {

    public static boolean isMethodAvailable(
            Object target,
            String methodName,
            Class<?>... parameterTypes
    ) {
        if (target == null) return false;

        try {
            target.getClass().getMethod(methodName, parameterTypes);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

}


