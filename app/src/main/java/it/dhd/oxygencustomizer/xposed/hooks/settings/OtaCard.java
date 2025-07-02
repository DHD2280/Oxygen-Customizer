package it.dhd.oxygencustomizer.xposed.hooks.settings;

import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SETTINGS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.dp2px;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class OtaCard extends XposedMods {

    private static final String listenPackage = SETTINGS;

    private boolean mCustomStyle = false;
    private View mOtaCard;

    public OtaCard(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        mCustomStyle = Xprefs.getBoolean("custom_ota_card", true);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {


        ReflectedClass AboutDeviceOtaUpdatePreference = ReflectedClass.of("com.oplus.settings.widget.preference.AboutDeviceOtaUpdatePreference");
        AboutDeviceOtaUpdatePreference
                .after("onBindViewHolder")
                .run(param -> {
                    if (!mCustomStyle) return;
                    Object preferenceHolder = param.args[0];
                    View iView = (View) getObjectField(preferenceHolder, "itemView");

                    mOtaCard = iView;
                    try {
                        ImageView iv = iView.findViewById(mContext.getResources().getIdentifier("about_device_top_bg", "id", SETTINGS));
                        iv.setVisibility(View.GONE);
                    } catch (Throwable t) {
                        log(t);
                    }
                    setCustomImage();
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

                        RoundedBitmapDrawable otaImage = RoundedBitmapDrawableFactory.create(
                                mContext.getResources(),
                                ImageDecoder.decodeBitmap(source));
                        otaImage.setCornerRadius(dp2px(mContext, 12));
                        mOtaCard.setBackground(otaImage);
                    } catch (Throwable ignored) {
                    }

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
