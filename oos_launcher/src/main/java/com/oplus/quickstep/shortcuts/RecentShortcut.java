package com.oplus.quickstep.shortcuts;

import android.view.View;

import com.android.launcher3.BaseDraggingActivity;
import com.android.launcher3.model.data.ItemInfo;
import com.android.launcher3.popup.SystemShortcut;

public abstract class RecentShortcut<T extends BaseDraggingActivity> extends SystemShortcut<T> {

    public RecentShortcut(int i8, int i9, T t8, ItemInfo itemInfo, View view) {
        super(i8, i9, t8, itemInfo, view);
    }
}
