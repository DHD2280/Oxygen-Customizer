package com.android.launcher3.util;

public interface SafeCloseable extends AutoCloseable {
    @Override
    void close();
}
