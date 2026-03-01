package com.android.systemui.statusbar.phone;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.view.ViewRootImpl;

public class SystemUIDialog extends AlertDialog implements ViewRootImpl.ConfigChangedCallback {

    protected SystemUIDialog(Context context) {
        super(context);
    }

    @Override
    public void onConfigurationChanged(Configuration globalConfig) {
        throw new UnsupportedOperationException("Stub!");
    }
}
