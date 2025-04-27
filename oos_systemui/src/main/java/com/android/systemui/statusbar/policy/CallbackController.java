package com.android.systemui.statusbar.policy;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.Lifecycle.Event;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

/**
 * Implementation of the collection used and thread guarantees are left to the discretion of the
 * client. Consider using ListenerSet to prevent concurrent
 * modification exceptions.
 */
public interface CallbackController<T> {

    /** Add a callback */
    void addCallback(@NonNull T listener);

    /** Remove a callback */
    void removeCallback(@NonNull T listener);

    /**
     * Wrapper to {@link #addCallback(Object)} when a lifecycle is in the resumed state
     * and {@link #removeCallback(Object)} when not resumed automatically.
     */
    default T observe(LifecycleOwner owner, T listener) {
        return observe(owner.getLifecycle(), listener);
    }

    /**
     * Wrapper to {@link #addCallback(Object)} when a lifecycle is in the resumed state
     * and {@link #removeCallback(Object)} when not resumed automatically.
     */
    default T observe(Lifecycle lifecycle, T listener) {
        lifecycle.addObserver((LifecycleEventObserver) (lifecycleOwner, event) -> {
            if (event == Event.ON_RESUME) {
                addCallback(listener);
            } else if (event == Event.ON_PAUSE) {
                removeCallback(listener);
            }
        });
        return listener;
    }
}

