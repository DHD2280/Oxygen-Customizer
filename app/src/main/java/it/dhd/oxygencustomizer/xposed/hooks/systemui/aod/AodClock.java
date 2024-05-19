package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_ACCENT3;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_TEXT1;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_COLOR_CODE_TEXT2;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_COLOR;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_USER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_STYLE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_SWITCH;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.AodClock.AOD_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_BOTTOM_MARGIN;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_FONT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_LINE_HEIGHT;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TEXT_SCALING;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.LockscreenClock.LOCKSCREEN_CLOCK_TOP_MARGIN;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.OpUtils.getPrimaryColor;
import static it.dhd.oxygencustomizer.xposed.utils.ViewHelper.loadLottieAnimationView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class AodClock extends XposedMods {
    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final String TAG = "Oxygen Customizer: AOD ";
    private FrameLayout mRootLayout = null;
    private ViewGroup testView = null;
    private Context appContext;
    Class<?> LottieAn = null;
    public static final String OC_AOD_CLOCK_TAG = "oxygencustomizer_aod_clock";
    private boolean mAodClockEnabled = false;
    private int mAodClockStyle = 0;

    public AodClock(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mAodClockEnabled = Xprefs.getBoolean(AOD_CLOCK_SWITCH, false);
        mAodClockStyle = Xprefs.getInt(AOD_CLOCK_STYLE, 0);

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        initResources(mContext);

        LottieAn = findClass("com.airbnb.lottie.LottieAnimationView", lpparam.classLoader);

        /*Class<?> OplusAodCurvedDisplayView = findClass("com.oplus.systemui.aod.surface.OplusAodCurvedDisplayView", lpparam.classLoader);
        hookAllMethods(OplusAodCurvedDisplayView, "recordColor", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                log(TAG + "OpusAodCurvedDisplayView recordColor");
                log(TAG + "str " + param.args[0].toString());
            }
        });
        hookAllMethods(OplusAodCurvedDisplayView, "onDraw", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                log("OpusAodCurvedDisplayView onDraw");
                Paint[] mPaintList = (Paint[]) getObjectField(param.thisObject, "mPaintList");
                for (Paint paint : mPaintList) {
                    log(TAG + "Paint: " + paint.toString() + " color: " + paint.getColor());
                }
            }
        });
                //mPaintList*/

        //initResource
        /*Class<?> OplusAodCurvedDisplayView = findClass("com.oplus.systemui.aod.surface.OplusAodCurvedDisplayView", lpparam.classLoader);
        hookAllMethods(OplusAodCurvedDisplayView, "initResource", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Bitmap mViewLeft = (Bitmap) getObjectField(param.thisObject, "mViewLeft");
                Bitmap mViewRight = (Bitmap) getObjectField(param.thisObject, "mViewRight");
                mViewLeft.colo
            }
        });*/

        Class<?> AodClockLayout;
        try {
            AodClockLayout = findClass("com.oplus.systemui.aod.aodclock.off.AodClockLayout", lpparam.classLoader);
        } catch (Throwable t) {
            AodClockLayout = findClass("com.oplusos.systemui.aod.aodclock.off.AodClockLayout", lpparam.classLoader);
        }

        hookAllConstructors(AodClockLayout, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mAodData = getObjectField(param.thisObject, "mAodData");
                log(TAG + " AodClockLayout constructor " + callMethod(mAodData, "shouldUseNewRenderMethod"));

                log(TAG + " AodClockLayout constructor");
            }
        });
        hookAllMethods(AodClockLayout, "initForAodApk", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //mRootLayout = (FrameLayout) getObjectField(param.thisObject, "mClockLayout");
                if (!mAodClockEnabled) return;
                FrameLayout mAodViewFromApk = (FrameLayout) getObjectField(param.thisObject, "mAodViewFromApk");
                for (int i = 0; i < mAodViewFromApk.getChildCount(); i++) {
                    log(TAG + " mAodViewFromApk " + mAodViewFromApk.getChildAt(i).getClass().getCanonicalName());
                    if (mAodViewFromApk.getChildAt(i) instanceof ViewGroup v) {
                        for (int j = 0; j < v.getChildCount(); j++) {
                            testView = v;
                            if (v.getChildAt(j) instanceof ViewGroup v2) {
                                for (int k = 0; k < v2.getChildCount(); k++) {
                                    v.getChildAt(k).setVisibility(View.GONE);
                                }
                                break;
                            }
                        }

                        /*
                        for (int j = 0; j < v.getChildCount(); j++) {
                            log(TAG + " mAodViewFromApk group " + v.getChildAt(i).getClass().getCanonicalName());
                            if (mAodViewFromApk.getChildAt(j) instanceof ViewGroup v2) {
                                for (int k = 0; k < v2.getChildCount(); k++) {
                                    log(TAG + " mAodViewFromApk group2 " + v2.getChildAt(i).getClass().getCanonicalName());
                                }
                            }
                        }*/
                    }
                }
                log(TAG + " initForAodApk");
                updateClockView2();
                //updateClockView();
            }
        });
        hookAllMethods(AodClockLayout, "performTimeUpdate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                updateClockView2();
            }
        });
        hookAllMethods(AodClockLayout, "initGlobalThemeLayout", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                //mRootLayout = (FrameLayout) getObjectField(param.thisObject, "mClockLayout");
                //updateClockView2();
            }
        });
        hookAllMethods(AodClockLayout, "updateLayout", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log(TAG + " updateLayout after");

                log(TAG + " updateLayout after " + (mRootLayout != null));
                /*try {
                    View v = (View) getObjectField(param.thisObject, "mDateLayout");
                    if (v != null) v.setVisibility(View.INVISIBLE);
                } catch (Throwable ignored) {}
                updateClockView();*/
            }
        });

        /*hookAllMethods(AodClockLayout, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {

                log(TAG + " updateClock after");

                updateClockView();
            }
        });

       /* findAndHookMethod(AodClockLayout, "updateLayout", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log(TAG + " updateLayout after");
                try {
                    Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                    callMethod(mAodViewFromApk, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mClockLayout = getObjectField(param.thisObject, "mClockLayout");
                    callMethod(mClockLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mDateLayout = getObjectField(param.thisObject, "mDateLayout");
                    callMethod(mDateLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mAodGlobalThemeRootLayout = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                    callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mClockRootView = getObjectField(param.thisObject, "mClockRootView");
                    callMethod(mClockRootView, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }
                updateClockView();
            }
        });

        hookAllMethods(AodClockLayout, "updateRamlessArea", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                    callMethod(mAodViewFromApk, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mClockLayout = getObjectField(param.thisObject, "mClockLayout");
                    callMethod(mClockLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mDateLayout = getObjectField(param.thisObject, "mDateLayout");
                    callMethod(mDateLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mAodGlobalThemeRootLayout = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                    callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mClockRootView = getObjectField(param.thisObject, "mClockRootView");
                    callMethod(mClockRootView, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }
                updateClockView();
            }
        });

        hookAllMethods(AodClockLayout, "setVisibleWithSetupAnimate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                    callMethod(mAodViewFromApk, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mClockRootView = getObjectField(param.thisObject, "mClockRootView");
                    callMethod(mClockRootView, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mDateLayout = getObjectField(param.thisObject, "mDateLayout");
                    callMethod(mDateLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                try {
                    Object mAodGlobalThemeRootLayout = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                    callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);
                } catch (Throwable ignored) {
                }

                updateClockView();

            }
        });*/

    }

    private void updateClockView() {

        if (mRootLayout == null) return;

        log(TAG + " updateClockView " + mRootLayout.getChildCount());

        for (int i = 0; i < mRootLayout.getChildCount(); i++) {
            mRootLayout.getChildAt(i).setVisibility(View.INVISIBLE);
        }

        View clockView = getClockView();

        boolean isClockAdded = mRootLayout.findViewWithTag(OC_AOD_CLOCK_TAG) != null;

        // Remove existing clock view
        if (isClockAdded) {
            mRootLayout.removeView(mRootLayout.findViewWithTag(OC_AOD_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(OC_AOD_CLOCK_TAG);
            ViewGroup.LayoutParams params = clockView.getLayoutParams();

            int idx = 0;
            LinearLayout dummyLayout = null;


            if (clockView.getParent() != null) {
                ((ViewGroup) clockView.getParent()).removeView(clockView);
            }

//            TextUtil.convertTextViewsToTitleCase((ViewGroup) clockView);

            mRootLayout.addView(clockView, idx);
            modifyClockView(clockView);
        }
    }

    private void updateClockView2() {

        if (testView == null) return;

        log(TAG + " updateClockView " + testView.getChildCount());

        /*for (int i = 0; i < testView.getChildCount(); i++) {
            testView.getChildAt(i).setVisibility(View.INVISIBLE);
        }*/

        View clockView = getClockView();

        boolean isClockAdded = testView.findViewWithTag(OC_AOD_CLOCK_TAG) != null;

        // Remove existing clock view
        if (isClockAdded) {
            testView.removeView(testView.findViewWithTag(OC_AOD_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(OC_AOD_CLOCK_TAG);

            int idx = 0;
            LinearLayout dummyLayout = null;


            if (clockView.getParent() != null) {
                ((ViewGroup) clockView.getParent()).removeView(clockView);
            }

//            TextUtil.convertTextViewsToTitleCase((ViewGroup) clockView);

            testView.addView(clockView, 0);
            modifyClockView(clockView);
        }
    }

    private void modifyClockView(View clockView) {
        boolean customColor = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_COLOR, false);
        float clockScale = Xprefs.getSliderFloat(AOD_CLOCK_TEXT_SCALING, 1.0f);
        String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/aod_clock_font.ttf";
        int lineHeight = Xprefs.getSliderInt(AOD_CLOCK_LINE_HEIGHT, 0);
        boolean customFontEnabled = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_FONT, false);
        boolean useCustomName = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_USER, false);
        String customName = Xprefs.getString(AOD_CLOCK_CUSTOM_USER_VALUE, "TEST");
        boolean useCustomImage = Xprefs.getBoolean(AOD_CLOCK_CUSTOM_USER_IMAGE, false);
        int systemAccent = getPrimaryColor(mContext);

        int accent1 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_ACCENT1, getPrimaryColor(mContext));
        int accent2 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_ACCENT2, getPrimaryColor(mContext));
        int accent3 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_ACCENT3, getPrimaryColor(mContext));
        int text1 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_TEXT1, Color.WHITE);
        int text2 = Xprefs.getInt(AOD_CLOCK_COLOR_CODE_TEXT2, Color.WHITE);

        Typeface typeface = null;
        if (customFontEnabled && (new File(customFont).exists())) {
            typeface = Typeface.createFromFile(new File(customFont));
        }

        ViewHelper.setMargins(clockView, mContext, 0, 0, 0, 0);

        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", customColor ? accent1 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", customColor ? accent2 : systemAccent);
        ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", customColor ? accent3 : systemAccent);
        if (customColor) {
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", text1);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", text2);
        }

        if (typeface != null) {
            ViewHelper.applyFontRecursively((ViewGroup) clockView, typeface);
        }

        ViewHelper.applyTextMarginRecursively((ViewGroup) clockView, lineHeight);

        if (clockScale != 1.0f) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, clockScale);
        }
        clockView.setVisibility(View.VISIBLE);
    }

    @SuppressLint("DiscouragedApi")
    private View getClockView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);

        View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                LOCKSCREEN_CLOCK_LAYOUT + mAodClockStyle,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );

        loadLottieAnimationView(
                appContext,
                LottieAn,
                v,
                mAodClockStyle
        );

        return v;

    }

    private void initResources(Context context) {
        try {
            appContext = context.createPackageContext(
                    BuildConfig.APPLICATION_ID,
                    Context.CONTEXT_IGNORE_SECURITY
            );
        } catch (PackageManager.NameNotFoundException ignored) {
        }
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
