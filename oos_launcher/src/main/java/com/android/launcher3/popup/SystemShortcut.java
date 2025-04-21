package com.android.launcher3.popup;

import android.content.Context;
import android.view.View;

import androidx.annotation.Nullable;

import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.views.ActivityContext;

public abstract class SystemShortcut<T extends Context & ActivityContext> extends ItemInfo implements View.OnClickListener {


    public SystemShortcut(int i8, int i9, T t8, ItemInfo itemInfo, View view) {
        throw new UnsupportedOperationException("STUB");
    }


}
