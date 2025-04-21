package com.android.launcher3.model.data;

import android.content.ComponentName;
import android.os.UserHandle;

import androidx.annotation.Nullable;

import com.oplus.basecommon.sort.AppNameComparator;

public class ItemInfo implements AppNameComparator.IAppOrderInfo {
    @Override
    public int getAlphabeticIndex() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public char getInitialChar() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String getOrderInfo() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public String getOrderInfo2() {
        throw new UnsupportedOperationException("STUB");
    }

    @Override
    public UserHandle getUser() {
        throw new UnsupportedOperationException("STUB");
    }

    @Nullable
    public ComponentName getTargetComponent() {
        throw new UnsupportedOperationException("STUB");
    }


}
