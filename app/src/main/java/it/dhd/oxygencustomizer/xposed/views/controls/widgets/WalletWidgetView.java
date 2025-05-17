package it.dhd.oxygencustomizer.xposed.views.controls.widgets;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_LABEL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.base.BaseQsWidgetView;

@SuppressLint("ViewConstructor")
public class WalletWidgetView extends BaseQsWidgetView {

    public WalletWidgetView(@NonNull Context context, boolean settingsInterface) {
        super(context, settingsInterface);
    }

    @Override
    public void onWidgetClick() {
        mActivityLauncherUtils.launchWallet();
    }

    @Override
    public Drawable getWidgetImage() {
        Context sysuiContext = mContext;
        if (mSettingsInterface) {
            sysuiContext = mContext;
        } else {
            try {
                sysuiContext = mContext.createPackageContext(
                        SYSTEM_UI,
                        Context.CONTEXT_IGNORE_SECURITY
                );
            } catch (Throwable ignored) {}
        }
        return WidgetUtils.getDrawable(sysuiContext, WALLET_ICON, SYSTEM_UI);
    }

    @Override
    public String getWidgetName() {
        Context sysuiContext = mContext;
        if (mSettingsInterface) {
            sysuiContext = mContext;
        } else {
            try {
                sysuiContext = mContext.createPackageContext(
                        SYSTEM_UI,
                        Context.CONTEXT_IGNORE_SECURITY
                );
            } catch (Throwable ignored) {}
        }
        return WidgetUtils.getString(sysuiContext, WALLET_LABEL, SYSTEM_UI);
    }

}
