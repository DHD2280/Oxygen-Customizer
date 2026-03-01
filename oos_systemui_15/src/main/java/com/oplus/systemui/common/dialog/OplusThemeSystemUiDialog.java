package com.oplus.systemui.common.dialog;

import android.content.Context;

import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.statusbar.policy.ConfigurationController;

public class OplusThemeSystemUiDialog extends SystemUIDialog implements ConfigurationController.ConfigurationListener {

    protected OplusThemeSystemUiDialog(Context context) {
        super(context);
    }

    public interface ILifeCycleCallback {
        default void onStart() {
        }

        void onStop();
    }

}
