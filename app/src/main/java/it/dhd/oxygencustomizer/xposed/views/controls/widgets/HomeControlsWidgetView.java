package it.dhd.oxygencustomizer.xposed.views.controls.widgets;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.Expandable;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getControlsTile;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOME_CONTROLS;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.HOME_CONTROLS_LABEL;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.WALLET_LABEL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.View;

import androidx.annotation.NonNull;

import it.dhd.oxygencustomizer.xposed.utils.ExtendedFAB;
import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.base.BaseQsWidgetView;

@SuppressLint("ViewConstructor")
public class HomeControlsWidgetView extends BaseQsWidgetView {

    public HomeControlsWidgetView(@NonNull Context context, boolean settingsInterface) {
        super(context, settingsInterface);
    }

    @Override
    public void onWidgetClick() {
        launchHomeControls(this);
    }

    private void launchHomeControls(View view) {
        Object controlsTile = getControlsTile();
        if (controlsTile == null) return;
        View finalView;
        Object expandable;
        if (view instanceof ExtendedFAB) {
            finalView = (View) view.getParent();
        } else {
            finalView = view;
        }
        if (Build.VERSION.SDK_INT >= 35) {
            expandable = callStaticMethod(Expandable, "fromView", view);
        } else {
            expandable = null;
        }
        post(() -> callMethod(controlsTile, "handleClick", (Build.VERSION.SDK_INT >= 35) ? expandable : finalView));
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
