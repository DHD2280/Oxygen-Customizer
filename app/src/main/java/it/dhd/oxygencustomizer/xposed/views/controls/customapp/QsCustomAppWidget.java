package it.dhd.oxygencustomizer.xposed.views.controls.customapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

@SuppressLint("ViewConstructor")
public class QsCustomAppWidget extends LinearLayout {

    private final CustomAppBinder mCustomAppBinder;

    public QsCustomAppWidget(@NonNull Context context, @NonNull String pkgName, boolean settingsInterface) {
        super(context);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        setLayoutParams(layoutParams);
        mCustomAppBinder = new CustomAppBinder(context, pkgName, settingsInterface);
        mCustomAppBinder.bindView(this);
    }

    public void onSizeChanged(int newWidth) {
        mCustomAppBinder.onSizeChanged(newWidth);
    }

}
