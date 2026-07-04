package it.dhd.oxygencustomizer.xposed.utils;

import static it.dhd.oxygencustomizer.xposed.XPLauncher.moduleResources;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.R;

public class ExtendedFAB extends ExtendedFloatingActionButton {

    public ExtendedFAB(Context context) {
        this(context, null);
    }

    public ExtendedFAB(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context.getPackageName().equals(BuildConfig.APPLICATION_ID) ? context : new ContextThemeWrapper(new ContextWrapper(context) {
            @Override
            public Resources getResources() {
                return moduleResources;
            }
        }, R.style.Theme_MaterialComponents_DayNight), attrs, defStyleAttr);
    }

}
