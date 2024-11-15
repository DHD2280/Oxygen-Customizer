package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_CARRIER_REPLACEMENT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_CUSTOM_FINGERPRINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_FINGERPRINT_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_CAPSULE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_CARRIER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_FINGERPRINT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_HIDE_STATUSBAR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_LEFT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_LOCK;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.Lockscreen.LOCKSCREEN_REMOVE_SOS;
import static it.dhd.oxygencustomizer.xposed.ResourceManager.resparams;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.DrawableConverter.scaleDrawable;
import static it.dhd.oxygencustomizer.xposed.utils.ReflectionTools.findClassInArray;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.drawable.AnimatedImageDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.StringFormatter;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;

public class Lockscreen extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    final StringFormatter carrierStringFormatter = new StringFormatter();
    private final String TAG = "Oxygen Customizer - Lockscreen: ";
    private final Class<?> KeyguardHelper = null;
    private boolean removeSOS;
    private boolean hideFingerprint = false, customFingerprint = false;
    private int fingerprintStyle = 0;
    private float mFpScale = 1.0f;
    private Drawable mFpDrawable = null;
    private boolean removeLeftAffordance = false, removeRightAffordance = false;
    private boolean removeLockIcon = false;
    private View mStartButton = null, mEndButton = null;
    private ImageView mLockIcon = null;
    private boolean hideLockscreenCarrier = false, hideLockscreenStatusbar = false, hideLockscreenCapsule = false;
    private TextView mCarrierText = null;
    private String lockscreenCarrierReplacement = "";

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
        removeLockIcon = Xprefs.getBoolean(LOCKSCREEN_REMOVE_LOCK, false);
        removeLeftAffordance = Xprefs.getBoolean(LOCKSCREEN_REMOVE_LEFT_AFFORDANCE, false);
        removeRightAffordance = Xprefs.getBoolean(LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE, false);
        hideLockscreenCarrier = Xprefs.getBoolean(LOCKSCREEN_HIDE_CARRIER, false);
        hideLockscreenStatusbar = Xprefs.getBoolean(LOCKSCREEN_HIDE_STATUSBAR, false);
        hideLockscreenCapsule = Xprefs.getBoolean(LOCKSCREEN_HIDE_CAPSULE, false);
        lockscreenCarrierReplacement = Xprefs.getString(LOCKSCREEN_CARRIER_REPLACEMENT, "");

        updateDrawable();

        if (Key.length > 0) {
            if (Key[0].equals(LOCKSCREEN_REMOVE_LEFT_AFFORDANCE)
                    || Key[0].equals(LOCKSCREEN_REMOVE_RIGHT_AFFORDANCE)) {
                updateAffordance();
            }
            if (Key[0].equals(LOCKSCREEN_HIDE_CARRIER) ||
                    Key[0].equals(LOCKSCREEN_HIDE_STATUSBAR) ||
                    Key[0].equals(LOCKSCREEN_HIDE_CAPSULE)
            ) {
                hideLockscreenStuff();
            }
            if (Key[0].equals(LOCKSCREEN_REMOVE_LOCK)) {
                if (mLockIcon != null)
                    mLockIcon.setVisibility(removeLockIcon ? View.GONE : View.VISIBLE);
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        try {
            ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleWithFixedDelay(() -> {
                File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                if (Android.isDirectory()) {
                    updateDrawable();
                    executor.shutdown();
                    executor.shutdownNow();
                }
            }, 0, 5, TimeUnit.SECONDS);
        } catch (Throwable ignored) {
        }

        try {
            hideLockscreenStuff();
        } catch (Throwable t) {
            log(t);
        }

        try {
            Class<?> OplusEmergencyButtonExImpl = findClass("com.oplus.keyguard.OplusEmergencyButtonExImpl", lpparam.classLoader);
            findAndHookMethod(OplusEmergencyButtonExImpl, "disableShowEmergencyButton", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (removeSOS) param.setResult(true);
                }
            });
        } catch (Throwable t) {
            log(t);
        }

        Class<?> OnScreenFingerprint = findClassInArray(lpparam,
                "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMech", /* OOS15 */
                "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMach", /* OOS14 */
                "com.oplus.systemui.keyguard.finger.onscreenfingerprint.OnScreenFingerprintUiMech" /* OOS13 */);

        try {
            hookAllMethods(OnScreenFingerprint, "loadAnimDrawables", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (hideFingerprint || customFingerprint) updateFingerprintIcon(param, false);
                }
            });
        } catch (Throwable t) {
            log(t);
        }

        try {
            hookAllMethods(OnScreenFingerprint, "startFadeInAnimation", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (hideFingerprint || customFingerprint) updateFingerprintIcon(param, true);
                }
            });
        } catch (Throwable t) {
            log(t);
        }

        if (Build.VERSION.SDK_INT == 33) {
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
                log(t);
            }
        }

        if (Build.VERSION.SDK_INT >= 34) {
            try {
                findAndHookMethod(OnScreenFingerprint, "updateFpColor", int.class, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (!customFingerprint || hideFingerprint) return;
                        param.args[0] = Color.TRANSPARENT;
                    }
                });
            } catch (Throwable t) {
                log(t);
            }
        }

        // Affordance Section
        hookAffordance(lpparam);

        // Lock Icon
        hookLockIcon(lpparam);

        // Custom Carrier
        try {
            hookCarrier(lpparam);
        } catch (Throwable t) {
            log(t);
        }

    }

    private void updateFingerprintIcon(XC_MethodHook.MethodHookParam param, boolean isStartMethod) {
        Object mFpIcon;
        mFpIcon = getObjectField(param.thisObject, Build.VERSION.SDK_INT == 35 ? "fpIcon" : "mFpIcon");

        log("updateFingerprintIcon");

        if (mFpDrawable == null) {
            setObjectField(param.thisObject, "mFadeInAnimDrawable", null);
            setObjectField(param.thisObject, "mFadeOutAnimDrawable", null);
        }
        setObjectField(param.thisObject, Build.VERSION.SDK_INT == 35 ? "imMobileDrawable" : "mImMobileDrawable", mFpDrawable);
        if (mFpIcon != null) {
            callMethod(mFpIcon, "setImageDrawable", mFpDrawable == null ? null : mFpDrawable);
        }
        if (isStartMethod) {
            param.setResult(null);
        }
        if (!isStartMethod) callMethod(param.thisObject, "updateFpIconColor");
    }

    private void updateDrawable() {
        if (customFingerprint) {
            if (fingerprintStyle != -1) {
                @SuppressLint("DiscouragedApi") int resId = ResourceManager.modRes.getIdentifier("fingerprint_" + fingerprintStyle, "drawable", BuildConfig.APPLICATION_ID);
                mFpDrawable = (ResourcesCompat.getDrawable(ResourceManager.modRes,
                        resId,
                        mContext.getTheme()));
            } else {
                try {
                    ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
                    executor.scheduleWithFixedDelay(() -> {
                        File Android = new File(Environment.getExternalStorageDirectory() + "/Android");

                        if (Android.isDirectory()) {
                            try {
                                ImageDecoder.Source source = ImageDecoder.createSource(new File(Environment.getExternalStorageDirectory() + "/.oxygen_customizer/lockscreen_fp_icon.png"));
                                mFpDrawable = ImageDecoder.decodeDrawable(source);
                                if (mFpDrawable instanceof AnimatedImageDrawable) {
                                    ((AnimatedImageDrawable) mFpDrawable).setRepeatCount(AnimatedImageDrawable.REPEAT_INFINITE);
                                    ((AnimatedImageDrawable) mFpDrawable).start();
                                }
                            } catch (Throwable t) {
                                log("Failed to load custom fingerprint icon: " + t.getMessage());
                            }
                        }
                    }, 0, 5, TimeUnit.SECONDS);
                } catch (Throwable ignored) {
                }
            }
        } else {
            mFpDrawable = null;
        }
        if (mFpScale != 1.0f && mFpDrawable != null)
            mFpDrawable = scaleDrawable(mContext, mFpDrawable, mFpScale);
    }

    private void updateAffordance() {
        if (removeLeftAffordance || removeRightAffordance) {
            if (mStartButton != null)
                mStartButton.setVisibility(removeLeftAffordance ? View.GONE : View.VISIBLE);
            if (mEndButton != null)
                mEndButton.setVisibility(removeRightAffordance ? View.GONE : View.VISIBLE);
        }
    }

    private void hookAffordance(XC_LoadPackage.LoadPackageParam lpparam) {
        if (Build.VERSION.SDK_INT >= 34) {
            Class<?> KeyguardBottomAreaView = findClass("com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder", lpparam.classLoader);
            hookAllMethods(KeyguardBottomAreaView, "updateButton", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (!(removeLeftAffordance || removeRightAffordance)) return;
                    ImageView view = (ImageView) param.args[0];
                    if (view != null && view.getId() == mContext.getResources().getIdentifier("start_button", "id", listenPackage)) {
                        mStartButton = view;
                        if (removeLeftAffordance) {
                            view.setVisibility(View.GONE);
                        }
                    } else if (view != null && view.getId() == mContext.getResources().getIdentifier("end_button", "id", listenPackage))
                        mEndButton = view;
                    if (removeRightAffordance) {
                        view.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            Class<?> KeyguardBottomAreaView = findClass("com.android.systemui.statusbar.phone.KeyguardBottomAreaView", lpparam.classLoader);
            hookAllMethods(KeyguardBottomAreaView, "updateCameraVisibility", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mEndButton = (View) getObjectField(param.thisObject, "mRightAffordanceView");
                    if (removeRightAffordance) {
                        mEndButton.setVisibility(View.GONE);
                    }
                }
            });
            hookAllMethods(KeyguardBottomAreaView, "updateLeftAffordanceVisibility", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mStartButton = (View) getObjectField(param.thisObject, "mLeftAffordanceView");
                    if (removeLeftAffordance) {
                        mStartButton.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void hookLockIcon(XC_LoadPackage.LoadPackageParam lpparam) {
        try {
            Class<?> LockIconView = findClass("com.android.keyguard.LockIconView", lpparam.classLoader);
            hookAllMethods(LockIconView, "onFinishInflate", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    mLockIcon = (ImageView) getObjectField(param.thisObject, "mLockIcon");
                    if (removeLockIcon) {
                        mLockIcon.setVisibility(View.GONE);
                    }
                }
            });
        } catch (Throwable t) {
            log("LockIconViewController not found");
        }
    }

    private void hookCarrier(XC_LoadPackage.LoadPackageParam lpparam) {

        carrierStringFormatter.registerCallback(this::setCarrierText);

        Class<?> OplusStatCarrierTextController = findClass("com.oplus.systemui.statusbar.widget.OplusStatCarrierTextController", lpparam.classLoader);
        hookAllMethods(OplusStatCarrierTextController, "updateCarrierInfo", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                TextView mView = (TextView) getObjectField(param.thisObject, "mView");
                if (mView.getId() == mContext.getResources().getIdentifier("keyguard_carrier_text", "id", listenPackage)) {
                    mCarrierText = mView;
                    setCarrierText();
                    if (!TextUtils.isEmpty(lockscreenCarrierReplacement)) param.setResult(null);
                }
            }
        });
    }

    private void setCarrierText() {
        if (mCarrierText != null && !TextUtils.isEmpty(lockscreenCarrierReplacement)) {
            mCarrierText.post(() -> mCarrierText.setText(carrierStringFormatter.formatString(lockscreenCarrierReplacement)));
        }
    }

    private void hideLockscreenStuff() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI);
        if (ourResparam == null) return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI, "layout", "keyguard_status_bar", new XC_LayoutInflated() {
                @SuppressLint("DiscouragedApi")
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    if (hideLockscreenCarrier) {
                        try {
                            @SuppressLint("DiscouragedApi") TextView keyguard_carrier_text = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_carrier_text", "id", mContext.getPackageName()));
                            keyguard_carrier_text.getLayoutParams().height = 0;
                            keyguard_carrier_text.setVisibility(View.INVISIBLE);
                            keyguard_carrier_text.requestLayout();
                        } catch (Throwable ignored) {
                        }
                    }
                    if (hideLockscreenCapsule) {
                        try {
                            @SuppressLint("DiscouragedApi") LinearLayout keyguard_seeding_card_container = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_seeding_card_container", "id", mContext.getPackageName()));
                            keyguard_seeding_card_container.getLayoutParams().height = 0;
                            keyguard_seeding_card_container.setVisibility(View.INVISIBLE);
                            keyguard_seeding_card_container.requestLayout();
                        } catch (Throwable ignored) {
                        }
                    }
                    if (hideLockscreenStatusbar) {
                        try {
                            @SuppressLint("DiscouragedApi") LinearLayout status_icon_area = liparam.view.findViewById(liparam.res.getIdentifier("status_icon_area", "id", mContext.getPackageName()));
                            status_icon_area.getLayoutParams().height = 0;
                            status_icon_area.setVisibility(View.INVISIBLE);
                            status_icon_area.requestLayout();
                        } catch (Throwable ignored) {
                        }

                        try {
                            @SuppressLint("DiscouragedApi") TextView keyguard_carrier_text = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_carrier_text", "id", mContext.getPackageName()));
                            keyguard_carrier_text.getLayoutParams().height = 0;
                            keyguard_carrier_text.setVisibility(View.INVISIBLE);
                            keyguard_carrier_text.requestLayout();
                        } catch (Throwable ignored) {
                        }
                        try {
                            @SuppressLint("DiscouragedApi") LinearLayout keyguard_seeding_card_container = liparam.view.findViewById(liparam.res.getIdentifier("keyguard_seeding_card_container", "id", mContext.getPackageName()));
                            keyguard_seeding_card_container.getLayoutParams().height = 0;
                            keyguard_seeding_card_container.setVisibility(View.INVISIBLE);
                            keyguard_seeding_card_container.requestLayout();
                        } catch (Throwable ignored) {
                        }
                    }
                }
            });
        } catch (Throwable t) {
            log(t);
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
