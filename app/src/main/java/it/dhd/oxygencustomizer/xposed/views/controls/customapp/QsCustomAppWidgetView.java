package it.dhd.oxygencustomizer.xposed.views.controls.customapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.oplus.systemui.qs.base.tile.PressFeedbackHelper;

import it.dhd.oxygencustomizer.xposed.views.base.BaseQsStaticView;

@SuppressLint("ViewConstructor")
public class QsCustomAppWidgetView extends BaseQsStaticView {

    private final CustomAppBinder mCustomAppBinder;
    private final boolean mSettingsInterface;

    public QsCustomAppWidgetView(@NonNull Context context, @NonNull String pkgName, boolean settingsInterface) {
        super(context);
        mSettingsInterface = settingsInterface;
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        setLayoutParams(layoutParams);
        mCustomAppBinder = new CustomAppBinder(context, pkgName, settingsInterface);
        mCustomAppBinder.bindView(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.backgroundProxy.onBackgroundAttach();
        if (!mSettingsInterface) PressFeedbackHelper.attachPressFeedback(this, this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.backgroundProxy.onBackgroundDetach();
        if (!mSettingsInterface) PressFeedbackHelper.attachPressFeedback(this, this);
    }

    public void onSizeChanged(int newWidth) {
        mCustomAppBinder.onSizeChanged(newWidth);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mCustomAppBinder.onConfigurationChanged();
    }

}
