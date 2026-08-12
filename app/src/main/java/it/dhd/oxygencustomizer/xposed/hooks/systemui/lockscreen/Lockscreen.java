package it.dhd.oxygencustomizer.xposed.hooks.systemui.lockscreen;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_NOW_BAR_EXPANDED_CHANGED;
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
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.isLeftAffordanceHidden;
import static it.dhd.oxygencustomizer.xposed.utils.DrawableConverter.scaleDrawable;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import androidx.interpolator.view.animation.FastOutLinearInInterpolator;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.StringFormatter;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class Lockscreen extends XposedMods {

    private final static String listenPackage = SYSTEM_UI;
    final StringFormatter carrierStringFormatter = new StringFormatter();
    private final String TAG = "Oxygen Customizer - Lockscreen: ";
    private boolean removeSOS = false;
    private boolean hideFingerprint = false, customFingerprint = false;
    private int fingerprintStyle = 0;
    private float mFpScale = 1.0f;
    private Drawable mFpDrawable = null;
    private boolean removeLeftAffordance = false, removeRightAffordance = false;
    private boolean removeLockIcon = false;
    private View mStartAnimatable = null, mEndAnimatable = null;
    private View mStartButton = null, mEndButton = null;
    private View mLockIcon = null;
    private View mLockIconContaier = null, mLockIconView = null;
    private boolean hideLockscreenCarrier = false, hideLockscreenStatusbar = false, hideLockscreenCapsule = false;
    private TextView mCarrierText = null;
    private String lockscreenCarrierReplacement = "";

    private String mFpIconField;
    private String mImMobileDrawableField;
    private String mFadeInAnimDrawableField;
    private String mFadeOutAnimDrawableField;

    private boolean mReceiverRegistered = false;
    private final BroadcastReceiver mNowBarReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            boolean isExpanded = intent.getBooleanExtra("isExpanded", false);
            animateButtons(isExpanded);
            if (isExpanded) {
                hideLockIcon();
            }
        }
    };

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
                hideLockIcon();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        if (!mReceiverRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction(ACTIONS_NOW_BAR_EXPANDED_CHANGED);
            mContext.registerReceiver(mNowBarReceiver, filter, Context.RECEIVER_EXPORTED);
            mReceiverRegistered = true;
        }

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

        if (Build.VERSION.SDK_INT >= 36) {
            ReflectedClass EmergencyButton = ReflectedClass.of("com.android.keyguard.EmergencyButton");
            EmergencyButton
                    .before("updateEmergencyCallButton")
                    .run(param -> {
                        if (!removeSOS) return;
                        callMethod(param.thisObject, "setVisibility", View.GONE);
                        param.setResult(null);
                    });

        } else if (Build.VERSION.SDK_INT >= 34) {
            try {
                ReflectedClass OplusEmergencyButtonExImpl = ReflectedClass.of("com.oplus.keyguard.OplusEmergencyButtonExImpl");
                OplusEmergencyButtonExImpl.before("disableShowEmergencyButton").run(param -> {
                    if (removeSOS) param.setResult(true);
                });
            } catch (Throwable t) {
                log(t);
            }
        } else {
            ReflectedClass EmergencyButton = ReflectedClass.of("com.oplus.keyguard.EmergencyButton", "com.android.keyguard.EmergencyButton");
            if (EmergencyButton.getClazz() != null) {
                EmergencyButton.before("updateEmergencyCallButton").run(param -> {
                    if (!removeSOS) return;
                    View button = (View) param.thisObject;
                    button.setVisibility(View.GONE);
                });
            }
        }

        ReflectedClass OnScreenFingerprint = ReflectedClass.of("com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMech", /* OOS15 */
                "com.oplus.systemui.biometrics.finger.udfps.OnScreenFingerprintUiMach", /* OOS14 */
                "com.oplus.systemui.keyguard.finger.onscreenfingerprint.OnScreenFingerprintUiMech" /* OOS13 */);

        OnScreenFingerprint
                .afterConstruction()
                .run(param -> resolveFieldNames(param.thisObject));

        try {
            OnScreenFingerprint
                    .after("loadAnimDrawables")
                    .run(param -> {
                        if (hideFingerprint || customFingerprint)
                            updateFingerprintIcon(param, false);
                    });
        } catch (Throwable t) {
            log(t);
        }

        try {
            OnScreenFingerprint
                    .before("startFadeInAnimation")
                    .run(param -> {
                        if (hideFingerprint || customFingerprint)
                            updateFingerprintIcon(param, true);
                    });
        } catch (Throwable t) {
            log(t);
        }

        if (Build.VERSION.SDK_INT == 33) {
            try {
                OnScreenFingerprint
                        .after("updateFpIconColor")
                        .run(param -> {
                            if (!customFingerprint || hideFingerprint) return;
                            Drawable d = (Drawable) getObjectField(param.thisObject, "mImMobileDrawable");
                            if (d != null) d.clearColorFilter();
                        });
            } catch (Throwable t) {
                log(t);
            }
        }

        if (Build.VERSION.SDK_INT >= 34) {
            try {
                OnScreenFingerprint
                        .before("updateFpColor")
                        .run(param -> {
                            if (!customFingerprint || hideFingerprint) return;
                            param.args[0] = Color.TRANSPARENT;
                        });
            } catch (Throwable t) {
                log(t);
            }
        }

        // Affordance Section
        hookAffordance();

        // Lock Icon
        hookLockIcon();

        // Custom Carrier
        try {
            hookCarrier();
        } catch (Throwable t) {
            log(t);
        }

    }

    private void updateFingerprintIcon(XC_MethodHook.MethodHookParam param, boolean isStartMethod) {
        Object mFpIcon;

        mFpIcon = getObjectField(param.thisObject, mFpIconField);

        log("updateFingerprintIcon");

        if (mFpDrawable == null) {
            setObjectField(param.thisObject, mFadeInAnimDrawableField, null);
            setObjectField(param.thisObject, mFadeOutAnimDrawableField, null);
        }
        setObjectField(param.thisObject, mImMobileDrawableField, mFpDrawable);
        if (mFpIcon != null) {
            callMethod(mFpIcon, "setImageDrawable", mFpDrawable == null ? null : mFpDrawable);
        }
        if (isStartMethod) {
            param.setResult(null);
        }
        if (!isStartMethod) callMethod(param.thisObject, "updateFpIconColor");
    }

    private void resolveFieldNames(Object target) {
        mFpIconField = resolveField(target, "fpIcon", "mFpIcon");
        mImMobileDrawableField = resolveField(target, "imMobileDrawable", "mImMobileDrawable");
        mFadeInAnimDrawableField = resolveField(target, "fadeInAnimDrawable", "mFadeInAnimDrawable");
        mFadeOutAnimDrawableField = resolveField(target, "fadeOutAnimDrawable", "mFadeOutAnimDrawable");
    }

    private String resolveField(Object target, String... candidates) {
        for (String name : candidates) {
            try {
                Object val = getObjectField(target, name);
                return name;
            } catch (Throwable ignored) { }
        }
        return null;
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

    @SuppressLint("DiscouragedApi")
    private void hookAffordance() {

        ReflectedClass OplusKeyguardBottomAreaController = ReflectedClass.ofIfPossible("com.oplus.systemui.keyguard.OplusKeyguardBottomAreaController");
        if (OplusKeyguardBottomAreaController.getClazz() != null) {
            OplusKeyguardBottomAreaController
                    .after("bindKeyguardBottomAreaView")
                    .run(param -> {
                        try {
                            mStartAnimatable = (View) getObjectField(param.thisObject, "startButton");
                            mEndAnimatable = (View) getObjectField(param.thisObject, "endButton");
                        } catch (Throwable ignored) {
                        }
                    });
        }

        if (Build.VERSION.SDK_INT >= 34) {
            ReflectedClass KeyguardBottomAreaView = ReflectedClass.of(
                    "com.oplus.systemui.keyguard.ui.binder.OplusKeyguardBottomAreaViewBinder",
                    "com.android.systemui.keyguard.ui.binder.KeyguardBottomAreaViewBinder");
            KeyguardBottomAreaView
                    .after("updateButton")
                    .run(param -> {
                        if (!(removeLeftAffordance || removeRightAffordance)) return;
                        ImageView view = (ImageView) param.args[0];
                        if (view != null && view.getId() == mContext.getResources().getIdentifier("start_button", "id", listenPackage)) {
                            mStartButton = view;
                            if (removeLeftAffordance) {
                                view.setVisibility(View.GONE);
                            }
                        } else if (view != null && view.getId() == mContext.getResources().getIdentifier("end_button", "id", listenPackage)) {
                            mEndButton = view;
                            if (removeRightAffordance) {
                                view.setVisibility(View.GONE);
                            }
                        }
                    });
            KeyguardBottomAreaView
                    .after("access$updateButton")
                    .run(param -> { // OOS 15.0.1 && 16
                        if (!(removeLeftAffordance || removeRightAffordance)) return;
                        ImageView view = (ImageView) ((Build.VERSION.SDK_INT >= 36) ? param.args[2] : param.args[1]);
                        Object viewModel = (Build.VERSION.SDK_INT >= 36) ? param.args[3] : null;
                        if (view != null) {
                            if (Build.VERSION.SDK_INT >= 36) {
                                String slotId = (String) getObjectField(viewModel, "slotId");
                                if ("bottom_start".equals(slotId)) {
                                    mStartButton = view;
                                    if (removeLeftAffordance) {
                                        view.setVisibility(View.GONE);
                                    }
                                } else if ("bottom_end".equals(slotId)) {
                                    mEndButton = view;
                                    if (removeRightAffordance) {
                                        view.setVisibility(View.GONE);
                                    }
                                }
                            } else {
                                if (view.getId() == mContext.getResources().getIdentifier("start_button", "id", listenPackage)) {
                                    mStartButton = view;
                                    if (removeLeftAffordance) {
                                        view.setVisibility(View.GONE);
                                    }
                                } else if (view.getId() == mContext.getResources().getIdentifier("end_button", "id", listenPackage)) {
                                    mEndButton = view;
                                    if (removeRightAffordance) {
                                        view.setVisibility(View.GONE);
                                    }
                                }
                            }
                        }
                    });
        } else {
            ReflectedClass KeyguardBottomAreaView = ReflectedClass.of("com.android.systemui.statusbar.phone.KeyguardBottomAreaView");
            KeyguardBottomAreaView
                    .after("updateCameraVisibility")
                    .run(param -> {
                        mEndButton = (View) getObjectField(param.thisObject, "mRightAffordanceView");
                        if (removeRightAffordance) {
                            mEndButton.setVisibility(View.GONE);
                        }
                    });
            KeyguardBottomAreaView
                    .after("updateLeftAffordanceVisibility")
                    .run(param -> {
                        mStartButton = (View) getObjectField(param.thisObject, "mLeftAffordanceView");
                        if (removeLeftAffordance) {
                            mStartButton.setVisibility(View.GONE);
                        }
                    });
        }
    }

    private void hookLockIcon() {
        try {
            ReflectedClass LockIconView = ReflectedClass.of("com.android.keyguard.LockIconView");
            LockIconView
                    .after("onFinishInflate")
                    .run(param -> {
                        mLockIcon = (View) param.thisObject;
                        if (removeLockIcon) mLockIcon.setVisibility(View.GONE);
                    });
        } catch (Throwable t) {
            log("LockIconView (keyguard) not found");
        }

        if (Build.VERSION.SDK_INT >= 36) {
            ReflectedClass LockIconView = ReflectedClass.of(
                    "com.android.systemui.keyguard.ui.view.LockIconView",
                    "com.oplus.systemui.keyguard.lock.LockIconView",
                    "com.oplus.systemui.keyguard.ui.view.LockIconView");
            LockIconView
                    .after("onFinishInflate").run(param -> {
                        mLockIconView = (View) param.thisObject;
                        if (removeLockIcon) mLockIconView.setVisibility(View.GONE);
                    });
            LockIconView.after("onAttachedToWindow").run(param -> {
                mLockIconView = (View) param.thisObject;
                if (removeLockIcon) mLockIconView.setVisibility(View.GONE);
            });
        }

        // OOS-specific controller (OOS 13-15)
        try {
            ReflectedClass OplusLockIconViewExImpl = ReflectedClass.ofIfPossible("com.oplus.keyguard.OplusLockIconViewExImpl");
            OplusLockIconViewExImpl
                    .after("addOplusIconView")
                    .run(param -> {
                        try {
                            mLockIconContaier = (View) getObjectField(param.thisObject, "mLockIconContainer");
                            mLockIconView = (View) getObjectField(param.thisObject, "mLockIcon");
                            if (removeLockIcon) {
                                mLockIconContaier.setVisibility(View.GONE);
                                mLockIconView.setVisibility(View.GONE);
                            }
                        } catch (Throwable ignored) {
                            try {
                                mLockIconContaier = (View) getObjectField(param.thisObject, "lockIcon");
                            } catch (Throwable ignored2) {
                            }
                        }
                    });
        } catch (Throwable t) {
            log(t);
        }
    }

    private void hookCarrier() {

        carrierStringFormatter.registerCallback(this::setCarrierText);

        ReflectedClass OplusStatCarrierTextController = ReflectedClass.ofIfPossible("com.oplus.systemui.statusbar.widget.OplusStatCarrierTextController");
        if (OplusStatCarrierTextController.getClazz() != null) {
            OplusStatCarrierTextController
                    .before("updateCarrierInfo")
                    .run(param -> {
                        TextView mView = (TextView) getObjectField(param.thisObject, "mView");
                        if (mView.getId() == mContext.getResources().getIdentifier("keyguard_carrier_text", "id", listenPackage)) {
                            mCarrierText = mView;
                            setCarrierText();
                            if (!TextUtils.isEmpty(lockscreenCarrierReplacement))
                                param.setResult(null);
                        }
                    });
        }
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

    private void animateButtons(boolean isExpanded) {
        XposedBridge.log("animateButtons: " + isExpanded);
        if (removeLeftAffordance && removeRightAffordance) return;
        if (isExpanded) {
            if (mStartAnimatable != null) {
                mStartAnimatable
                        .animate()
                        .scaleX(0.4f)
                        .scaleY(0.4f)
                        .alpha(0f)
                        .setDuration(175L)
                        .setInterpolator(new FastOutLinearInInterpolator())
                        .withEndAction(() -> mStartAnimatable.setVisibility(View.GONE))
                        .start();
            }
            if (mEndAnimatable != null) {
                mEndAnimatable
                        .animate()
                        .scaleX(0.4f)
                        .scaleY(0.4f)
                        .alpha(0f)
                        .setDuration(175L)
                        .setInterpolator(new FastOutLinearInInterpolator())
                        .withEndAction(() -> mEndAnimatable.setVisibility(View.GONE))
                        .start();
            }
        } else {
            if (!isLeftAffordanceHidden(mContext, removeLeftAffordance) && mStartAnimatable != null) {
                mStartAnimatable.setVisibility(View.VISIBLE);
                mStartAnimatable.setScaleX(0.4f);
                mStartAnimatable.setScaleY(0.4f);
                mStartAnimatable.setAlpha(0f);
                mStartAnimatable.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300L)
                        .setInterpolator(new FastOutLinearInInterpolator())
                        .start();
            }
            if (!removeRightAffordance && mEndAnimatable != null) {
                mEndAnimatable.setVisibility(View.VISIBLE);
                mEndAnimatable.setScaleX(0.4f);
                mEndAnimatable.setScaleY(0.4f);
                mEndAnimatable.setAlpha(0f);
                mEndAnimatable.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(300L)
                        .setInterpolator(new FastOutLinearInInterpolator())
                        .start();
            }
        }
    }

    private void hideLockIcon() {
        if (mLockIcon != null)
            mLockIcon.setVisibility(removeLockIcon ? View.GONE : View.VISIBLE);
        if (mLockIconContaier != null)
            mLockIconContaier.setVisibility(removeLockIcon ? View.GONE : View.VISIBLE);
        if (mLockIconView != null)
            mLockIconView.setVisibility(removeLockIcon ? View.GONE : View.VISIBLE);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
