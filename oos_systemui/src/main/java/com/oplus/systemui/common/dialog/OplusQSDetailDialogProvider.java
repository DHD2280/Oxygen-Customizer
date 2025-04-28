package com.oplus.systemui.common.dialog;

import android.content.Context;

import com.android.systemui.animation.DialogTransitionAnimator;
import com.android.systemui.animation.Expandable;
import com.android.systemui.plugins.qs.DetailAdapter;

public final class OplusQSDetailDialogProvider implements OplusThemeSystemUiDialog.ILifeCycleCallback {

    public OplusQSDetailDialogProvider(Context context, DetailAdapter detailAdapter) {
        throw new UnsupportedOperationException("Stub!");
    }

    @Override
    public void onStop() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final boolean isShowing() {
        throw new UnsupportedOperationException("Stub!");
    }

    public final void showDetail(Expandable expandable, boolean z, DialogTransitionAnimator dialogTransitionAnimator, DialogHeight dialogHeight) {
        throw new UnsupportedOperationException("Stub!");
    }

    public static final class DialogHeight {
        public static final DialogHeight CHANGE = new DialogHeight("CHANGE", 2);

        public DialogHeight(String str, int i2) {
        }


    }

}
