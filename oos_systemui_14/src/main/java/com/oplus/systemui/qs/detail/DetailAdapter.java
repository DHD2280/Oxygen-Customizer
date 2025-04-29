package com.oplus.systemui.qs.detail;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

public interface DetailAdapter {

    public static final int VERSION = 1;

    static  int lambda$static$0() {
        throw new UnsupportedOperationException("Stub!");
    }

    View createDetailView(Context context, View view, ViewGroup viewGroup);

    default int getDoneText() {
        throw new UnsupportedOperationException("Stub!");
    }

    int getMetricsCategory();

    Intent getSettingsIntent();

    default int getSettingsText() {
        throw new UnsupportedOperationException("Stub!");
    }

    CharSequence getTitle();

    default boolean getToggleEnabled() {
        throw new UnsupportedOperationException("Stub!");
    }

    Boolean getToggleState();

    default boolean hasHeader() {
        throw new UnsupportedOperationException("Stub!");
    }

    default boolean hasHint() {
        throw new UnsupportedOperationException("Stub!");
    }

    default void onDialogEnterAnimation(boolean z) {
    }

    default boolean onDoneButtonClicked() {
        throw new UnsupportedOperationException("Stub!");
    }

    void setToggleState(boolean z);

    default boolean shouldAnimate() {
        throw new UnsupportedOperationException("Stub!");
    }

    default boolean shouldCheckAnimate() {
        throw new UnsupportedOperationException("Stub!");
    }

    default Boolean isNeedOpenWhenEnable() {
        throw new UnsupportedOperationException("Stub!");
    }

    default Boolean isNeedCloseWhenUnlock() {
        throw new UnsupportedOperationException("Stub!");
    }
}