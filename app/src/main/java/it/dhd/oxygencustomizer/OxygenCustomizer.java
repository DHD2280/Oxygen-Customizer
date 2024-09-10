package it.dhd.oxygencustomizer;

import android.app.Application;
import android.content.Context;

import com.google.android.material.color.DynamicColors;

import java.lang.ref.WeakReference;

import it.dhd.oxygencustomizer.utils.LocaleHelper;

public class OxygenCustomizer extends Application {

    private static OxygenCustomizer instance;
    private static WeakReference<Context> contextReference;

    public void onCreate() {
        super.onCreate();
        instance = this;
        contextReference = new WeakReference<>(getApplicationContext());
        DynamicColors.applyToActivitiesIfAvailable(this);
    }

    public static Context getAppContext() {
        if (contextReference == null || contextReference.get() == null) {
            contextReference = new WeakReference<>(OxygenCustomizer.getInstance().getApplicationContext());
        }
        return contextReference.get();
    }

    public static Context getAppContextLocale() {
        return LocaleHelper.setLocale(getAppContext());
    }

    private static OxygenCustomizer getInstance() {
        if (instance == null) {
            instance = new OxygenCustomizer();
        }
        return instance;
    }
}
