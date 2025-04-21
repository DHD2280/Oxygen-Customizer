package com.android.launcher3;

import android.app.Activity;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;

public class OplusBaseActivity extends Activity implements LifecycleOwner, ViewModelStoreOwner {

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        throw new UnsupportedOperationException("STUB");
    }

    @NonNull
    @Override
    public ViewModelStore getViewModelStore() {
        throw new UnsupportedOperationException("STUB");
    }
}
