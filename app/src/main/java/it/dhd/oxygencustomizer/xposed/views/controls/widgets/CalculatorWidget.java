package it.dhd.oxygencustomizer.xposed.views.controls.widgets;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.CALCULATOR_ICON;
import static it.dhd.oxygencustomizer.xposed.utils.WidgetUtils.CALCULATOR_LABEL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.utils.WidgetUtils;
import it.dhd.oxygencustomizer.xposed.views.controls.widgets.base.BaseQsWidget;

@SuppressLint("ViewConstructor")
public class CalculatorWidget extends BaseQsWidget {

    public CalculatorWidget(@NonNull Context context, boolean settingsInterface) {
        super(context, settingsInterface);
    }

    @Override
    public void onWidgetClick() {}

    @Override
    public Drawable getWidgetImage() {
        try {
            Context sysuiContext = mContext;
            if (!mSettingsInterface) {
                sysuiContext = mContext;
            } else {
                try {
                    sysuiContext = mContext.createPackageContext(
                            SYSTEM_UI,
                            Context.CONTEXT_IGNORE_SECURITY
                    );
                } catch (Throwable ignored) {
                }
            }
            Drawable calcIcon = WidgetUtils.getDrawable(sysuiContext, CALCULATOR_ICON, SYSTEM_UI);
            calcIcon.setTint(Color.WHITE);
            return calcIcon;
        } catch (Throwable ignored) {
            return ContextCompat.getDrawable(
                    getContext(),
                    R.drawable.ic_calculator
            );
        }
    }

    @Override
    public String getWidgetName() {
        try {
            Context sysuiContext = mContext;
            if (mSettingsInterface) {
                sysuiContext = mContext;
            } else {
                try {
                    sysuiContext = mContext.createPackageContext(
                            SYSTEM_UI,
                            Context.CONTEXT_IGNORE_SECURITY
                    );
                } catch (Throwable ignored) {
                }
            }
            return WidgetUtils.getString(sysuiContext, CALCULATOR_LABEL, SYSTEM_UI);
        } catch (Throwable ignored) {
            return getContext().getString(R.string.calculator);
        }
    }

}
