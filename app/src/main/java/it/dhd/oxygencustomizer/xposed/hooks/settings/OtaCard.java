package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.hookAllMethods;

import android.content.Context;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class OtaCard extends XposedMods {

    private static final String listenPackage = SETTINGS;

    private boolean mCustomStyle = false;
    private RelativeLayout mOtaCard;

    public OtaCard(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mCustomStyle = Xprefs.getBoolean("custom_ota_card", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {



        Class<?> AboutDeviceOtaUpdatePreference = findClass("com.oplus.settings.widget.preference.AboutDeviceOtaUpdatePreference", lpparam.classLoader);
        hookAllMethods(AboutDeviceOtaUpdatePreference, "onBindViewHolder", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                if (!mCustomStyle) return;
                View iView = (View) getObjectField(param.thisObject, "mLogoView");

                mOtaCard = (RelativeLayout) iView.getParent();
                setCustomImage();
            }
        });
    }

    private void setCustomImage() {
        if (mOtaCard == null) return;

        try {
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
            executor.scheduleWithFixedDelay(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    try {
                        ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/settings_ota_card.png"));

                        Drawable drawable = ImageDecoder.decodeDrawable(source);
                        mOtaCard.setBackground(drawable);

                        if (drawable instanceof AnimatedImageDrawable) {
                            ((AnimatedImageDrawable) drawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                            ((AnimatedImageDrawable) drawable).start();
                        }
                    } catch (Throwable ignored) {}

                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);

        } catch (Throwable ignored) {
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
