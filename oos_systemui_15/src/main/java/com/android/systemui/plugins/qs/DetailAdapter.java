package com.android.systemui.plugins.qs;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;

public interface DetailAdapter {

    public static final int VERSION = 1;

    static int lambda$static$0() {
        throw new UnsupportedOperationException("Stub!");
    }

    default View createDetailView(Context context, View view, ViewGroup viewGroup) {
        throw new UnsupportedOperationException("Stub!");
    }

    default View createDetailView(Context context, ViewGroup viewGroup, boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }

    default int getDoneText() {
        throw new UnsupportedOperationException("Stub!");
    }

    default int getMetricsCategory() {
        throw new UnsupportedOperationException("Stub!");
    }

    default OplusQsDialogSupply getOplusQsDialogSupply() {
        throw new UnsupportedOperationException("Stub!");
    }

    Intent getSettingsIntent();

    default int getSettingsText() {
        throw new UnsupportedOperationException("Stub!");
    }

    CharSequence getTitle();

    default boolean getToggleEnabled() {
        throw new UnsupportedOperationException("Stub!");
    }

    default Boolean getToggleState() {
        throw new UnsupportedOperationException("Stub!");
    }

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

    default void setToggleState(boolean z) {
        throw new UnsupportedOperationException("Stub!");
    }

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
