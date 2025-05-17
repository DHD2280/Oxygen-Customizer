package it.dhd.oxygencustomizer.xposed.views.controls.widgets;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOME_CONTROLS;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOME_CONTROLS_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_LABEL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.base.BaseQsWidget;

@SuppressLint("ViewConstructor")
public class HomeControlsWidget extends BaseQsWidget {

    public HomeControlsWidget(@NonNull Context context, boolean settingsInterface) {
        super(context, settingsInterface);
    }

    @Override
    public void onWidgetClick() {}

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
        return WidgetUtils.getDrawable(sysuiContext, HOME_CONTROLS, SYSTEM_UI);
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
        return WidgetUtils.getString(sysuiContext, HOME_CONTROLS_LABEL, SYSTEM_UI);
    }

}
