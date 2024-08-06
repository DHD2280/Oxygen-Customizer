package it.dhd.oxygencustomizer.xposed.utils;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.xposed.ResourceManager;

public class ExtendedFAB extends ExtendedFloatingActionButton {

    public ExtendedFAB(Context context) {
        this(context, null);
    }

    public ExtendedFAB(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExtendedFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(new ContextThemeWrapper(new ContextWrapper(context) {
            @Override
            public Resources getResources() {
                return ResourceManager.modRes;
            }
        }, R.style.Theme_MaterialComponents_DayNight), attrs, defStyleAttr);
    }

}
