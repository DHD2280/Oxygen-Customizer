package com.android.systemui;

public class Dependency {

    public static Dependency sDependency;

    public static Object get(Class cls) {
        throw new UnsupportedOperationException("Stub!");
    }

    public final synchronized Object getDependencyInner(Object obj) {
        throw new UnsupportedOperationException("Stub!");
    }


}
