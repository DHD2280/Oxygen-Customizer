package it.dhd.oxygencustomizer.xposed.views.controls.widgets.base;

import android.graphics.drawable.Drawable;

public interface BaseLaunchWidget {

    void onWidgetClick();

    Drawable getWidgetImage();

    String getWidgetName();

    void onSizeChanged(int newWidth);

}