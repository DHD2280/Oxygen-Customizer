package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.callStaticMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_CUSTOM_FINGERPRINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_FINGERPRINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_SOS;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.DrawableConverter.scaleDrawable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class Lockscreen extends XposedMods {

    private final static String listenPackage = Constants.Packages.SYSTEM_UI;
    private final String TAG = "Oxygen Customizer - Lockscreen: ";
    private Class<?> KeyguardHelper = null;
    private Object OSF = null;
    private boolean removeSOS;
    private boolean hideFingerprint = false, customFingerprint = false;
    private int fingerprintStyle = 0;
    private Object mFpIcon;
    private float mFpScale = 1.0f;
    private Drawable mFpDrawable = null;

    public Lockscreen(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {

        removeSOS = Xprefs.getBoolean(LOCKSCREEN_REMOVE_SOS, false);
        hideFingerprint = Xprefs.getBoolean(LOCKSCREEN_HIDE_FINGERPRINT, false);
        customFingerprint = Xprefs.getBoolean(LOCKSCREEN_CUSTOM_FINGERPRINT, false);
        fingerprintStyle = Integer.parseInt(Xprefs.getString(LOCKSCREEN_FINGERPRINT_STYLE, "0"));
        mFpScale = Xprefs.getSliderFloat(LOCKSCREEN_FINGERPRINT_SCALING, 1.0f);

        if (Key.length > 0) {
            if (Key[0].equals(LOCKSCREEN_FINGERPRINT_STYLE)
                || Key[0].equals(LOCKSCREEN_CUSTOM_FINGERPRINT)
                || Key[0].equals(LOCKSCREEN_HIDE_FINGERPRINT)
                || Key[0].equals(LOCKSCREEN_FINGERPRINT_SCALING)) {
                updateDrawable();
            }
        }
    }

    private boolean isMethodSecure() {
        log(TAG + "isMethodSecure" + " != null" + (KeyguardHelper != null));
        if (KeyguardHelper != null) {
            return (boolean) callStaticMethod(KeyguardHelper, "hasSecurityKeyguard");
        }
        return false;
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;


        KeyguardHelper = findClass("com.oplus.systemui.common.helper.KeyguardHelper", lpparam.classLoader);

        Class<?> OplusEmergencyButtonExImpl = findClass("com.oplus.keyguard.OplusEmergencyButtonExImpl", lpparam.classLoader);
        try {
            findAndHookMethod(OplusEmergencyButtonExImpl, "disableShowEmergencyButton", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (removeSOS) param.setResult(true);
                }
            });
        } catch (Throwable t) {
            log(TAG + "disableShowEmergencyButton not found");
        }

        Class<?> OnScreenFingerprint = findClass("com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMach", lpparam.classLoader);
        hookAllMethods(OnScreenFingerprint, "initFpIconWin", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                OSF = param.thisObject;
            }
        });
        try {
            hookAllMethods(OnScreenFingerprint, "loadAnimDrawables", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (hideFingerprint || customFingerprint) updateFingerprintIcon(param, false);
                }
            });
        } catch (Throwable t) {
            log(TAG + "onAttachedToWindow not found");
        }

        try {
            hookAllMethods(OnScreenFingerprint, "startFadeInAnimation", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (hideFingerprint || customFingerprint) updateFingerprintIcon(param, true);
                }
            });
        } catch (Throwable t) {
            log(TAG + "startFadeInAnimation not found");
        }

        try {
            hookAllMethods(OnScreenFingerprint, "updateFpIconColor", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!customFingerprint || hideFingerprint) return;
                    Drawable d = (Drawable) getObjectField(param.thisObject, "mImMobileDrawable");
                    if (d != null) d.clearColorFilter();
                }
            });
        } catch (Throwable t) {
            log(TAG + "updateFpIconColor not found");
        }

        try {
            hookAllMethods(OnScreenFingerprint, "startFadeOutAnimation", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (hideFingerprint || customFingerprint) param.setResult(null);
                }
            });
        } catch (Throwable t) {
            log(TAG + "startFadeInAnimation not found");
        }

    }

    private void updateFingerprintIcon(XC_MethodHook.MethodHookParam param, boolean isStartMethod) {
        if (mFpIcon == null) mFpIcon = getObjectField(param.thisObject, "mFpIcon");

        if (BuildConfig.DEBUG) log(TAG + "updateFingerprintIcon");

        if (mFpDrawable == null) {
            setObjectField(param.thisObject, "mFadeInAnimDrawable", null);
            setObjectField(param.thisObject, "mFadeOutAnimDrawable", null);
        }
        setObjectField(param.thisObject, "mImMobileDrawable", mFpDrawable);
        if (mFpIcon != null) {
            callMethod(mFpIcon, "setImageDrawable", mFpDrawable == null ? null : mFpDrawable);
        }
        if (hideFingerprint && isStartMethod) {
            param.setResult(null);
        }
        //if (!isStartMethod) callMethod(param.thisObject, "updateFpIconColor");
    }

    private void updateDrawable() {
        if (customFingerprint) {
            if (fingerprintStyle != -1) {
                @SuppressLint("DiscouragedApi") int resId = ResourceManager.modRes.getIdentifier("fingerprint_" + fingerprintStyle,"drawable", BuildConfig.APPLICATION_ID);
                mFpDrawable = (ResourcesCompat.getDrawable(ResourceManager.modRes,
                        resId,
                        mContext.getTheme()));
            } else {
                try {
                    ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_fp_icon.png"));
                    mFpDrawable = ImageDecoder.decodeDrawable(source);
                    if (mFpDrawable instanceof AnimatedImageDrawable) {
                        ((AnimatedImageDrawable) mFpDrawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                        ((AnimatedImageDrawable) mFpDrawable).start();
                    }
                } catch (Throwable ignored) {}
            }
        } else {
            mFpDrawable = null;
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
