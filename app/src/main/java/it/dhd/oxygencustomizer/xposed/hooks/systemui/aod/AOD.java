package it.dhd.oxygencustomizer.xposed.hooks.systemui.aod;

import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.LOCKSCREEN_CLOCK_LAYOUT;
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
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.BuildConfig;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ViewHelper;

public class AOD extends XposedMods {
    private static final String listenPackage = Constants.Packages.SYSTEM_UI;
    private static final String TAG = "Oxygen Customizer: AOD ";
    private RelativeLayout mRootLayout = null;
    private Context appContext;
    Class<?> LottieAn = null;
    public static final String OC_AOD_CLOCK_TAG = "oxygencustomizer_aod_clock";

    public AOD(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;
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

        Class<?> AodClockLayout = findClass("com.oplus.systemui.aod.aodclock.off.AodClockLayout", lpparam.classLoader);
        hookAllConstructors(AodClockLayout, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mRootLayout = (RelativeLayout) param.thisObject;

                Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                callMethod(mAodViewFromApk, "setVisibility", View.GONE);

                Object mClockLayout  = getObjectField(param.thisObject, "mClockLayout");
                callMethod(mClockLayout, "setVisibility", View.GONE);


                Object mDateLayout  = getObjectField(param.thisObject, "mDateLayout");
                callMethod(mDateLayout, "setVisibility", View.GONE);

                Object mAodGlobalThemeRootLayout  = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);

                updateClockView();
            }
        });

        findAndHookMethod(AodClockLayout, "updateLayout", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                log(TAG + " updateLayout after");
                Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                callMethod(mAodViewFromApk, "setVisibility", View.GONE);

                Object mClockLayout  = getObjectField(param.thisObject, "mClockLayout");
                callMethod(mClockLayout, "setVisibility", View.GONE);


                Object mDateLayout  = getObjectField(param.thisObject, "mDateLayout");
                callMethod(mDateLayout, "setVisibility", View.GONE);

                Object mAodGlobalThemeRootLayout  = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);
            }
        });

        hookAllMethods(AodClockLayout, "updateRamlessArea", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                callMethod(mAodViewFromApk, "setVisibility", View.GONE);

                Object mClockLayout  = getObjectField(param.thisObject, "mClockLayout");
                callMethod(mClockLayout, "setVisibility", View.GONE);


                Object mDateLayout  = getObjectField(param.thisObject, "mDateLayout");
                callMethod(mDateLayout, "setVisibility", View.GONE);

                Object mAodGlobalThemeRootLayout  = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);
            }
        });

        hookAllMethods(AodClockLayout, "setVisibleWithSetupAnimate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object mAodViewFromApk = getObjectField(param.thisObject, "mAodViewFromApk");
                callMethod(mAodViewFromApk, "setVisibility", View.GONE);

                Object mClockLayout  = getObjectField(param.thisObject, "mClockLayout");
                callMethod(mClockLayout, "setVisibility", View.GONE);


                Object mDateLayout  = getObjectField(param.thisObject, "mDateLayout");
                callMethod(mDateLayout, "setVisibility", View.GONE);

                Object mAodGlobalThemeRootLayout  = getObjectField(param.thisObject, "mAodGlobalThemeRootLayout");
                callMethod(mAodGlobalThemeRootLayout, "setVisibility", View.GONE);

            }
        });

    }

    private void updateClockView() {

        if (mRootLayout == null) return;

        View clockView = getClockView();

        boolean isClockAdded = mRootLayout.findViewWithTag(OC_AOD_CLOCK_TAG) != null;

        // Remove existing clock view
        if (isClockAdded) {
            mRootLayout.removeView(mRootLayout.findViewWithTag(OC_AOD_CLOCK_TAG));
        }

        if (clockView != null) {
            clockView.setTag(OC_AOD_CLOCK_TAG);

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

    private void modifyClockView(View clockView) {
        int topMargin = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_TOP_MARGIN, 100);
        int bottomMargin = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_BOTTOM_MARGIN, 40);
        float clockScale = Xprefs.getSliderFloat(LOCKSCREEN_CLOCK_TEXT_SCALING, 1.0f);
        String customFont = Environment.getExternalStorageDirectory() + "/.oxygen_customizer/aod_clock_font.ttf";
        int lineHeight = Xprefs.getSliderInt(LOCKSCREEN_CLOCK_LINE_HEIGHT, 0);
        boolean customFontEnabled = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_FONT, false);
        boolean useCustomName = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_USER, false);
        String customName = Xprefs.getString(LOCKSCREEN_CLOCK_CUSTOM_USER_VALUE, "TEST");
        boolean useCustomImage = Xprefs.getBoolean(LOCKSCREEN_CLOCK_CUSTOM_USER_IMAGE, false);

        Typeface typeface = null;
        if (customFontEnabled && (new File(customFont).exists())) {
            typeface = Typeface.createFromFile(new File(customFont));
        }

        ViewHelper.setMargins(clockView, mContext, 0, topMargin, 0, bottomMargin);

        if (true) {
            ViewHelper.findViewWithTagAndChangeColor(clockView, "accent1", getPrimaryColor(mContext));
            ViewHelper.findViewWithTagAndChangeColor(clockView, "accent2", Color.WHITE);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "accent3", Color.WHITE);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text1", Color.WHITE);
            ViewHelper.findViewWithTagAndChangeColor(clockView, "text2", Color.WHITE);
        }

        if (typeface != null) {
            ViewHelper.applyFontRecursively((ViewGroup) clockView, typeface);
        }

        ViewHelper.applyTextMarginRecursively((ViewGroup) clockView, lineHeight);

        if (clockScale != 1.0f) {
            ViewHelper.applyTextScalingRecursively((ViewGroup) clockView, clockScale);
        }
    }

    @SuppressLint("DiscouragedApi")
    private View getClockView() {
        LayoutInflater inflater = LayoutInflater.from(appContext);

        View v = inflater.inflate(
                appContext
                        .getResources()
                        .getIdentifier(
                                LOCKSCREEN_CLOCK_LAYOUT + 4,
                                "layout",
                                BuildConfig.APPLICATION_ID
                        ),
                null
        );

        loadLottieAnimationView(
                appContext,
                LottieAn,
                v,
                4
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
