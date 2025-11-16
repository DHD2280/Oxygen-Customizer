package it.dhd.oxygencustomizer.xposed.hooks.systemui.navbar;

import static android.view.MotionEvent.ACTION_DOWN;
import static de.robv.android.xposed.XposedBridge.hookAllConstructors;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.findClass;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getFloatField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_OPEN_QUICK_SETTINGS;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_OVERRIDE_BACK;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_SWITCH_APP;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_TOGGLE_ONE_HANDED;
import static it.dhd.oxygencustomizer.utils.Constants.ACTIONS_TOGGLE_PANEL;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.SYSTEM_UI;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_HOLD_BACK_LEFT_APP;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.GesturesPrefs.GESTURE_HOLD_BACK_RIGHT_APP;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.hooks.systemui.ControllersProvider.getActivityStarterExternal;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

import java.util.Arrays;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.R;
import it.dhd.oxygencustomizer.utils.AppUtils;
import it.dhd.oxygencustomizer.xposed.ResourceManager;
import it.dhd.oxygencustomizer.xposed.XPLauncher;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.ActivityLauncherUtils;
import it.dhd.oxygencustomizer.xposed.utils.DrawableConverter;
import it.dhd.oxygencustomizer.xposed.utils.ScreenshotUtils;
import it.dhd.oxygencustomizer.xposed.utils.SystemUtils;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class GestureNavbarManager extends XposedMods {

    private static final String listenPackage = SYSTEM_UI;
    private static final int mLightColor = 0xEBFFFFFF, mDarkColor = 0x99000000; //original navbar colors
    //region pill size
    private static float widthFactor = 1f;
    private static boolean navPillColorAccent = false;
    private Object SideGestureConfigurationEx;
    private Object mOverviewProxyService = null;
    //region Back gesture
    private List<Float> backGestureHeightFractionLeft = Arrays.asList(0f, 1f); // % of screen height. can be anything between 0 to 1
    private List<Float> backGestureHeightFractionRight = Arrays.asList(0f, 1f); // % of screen height. can be anything between 0 to 1
    private boolean leftEnabled = true;
    private boolean rightEnabled = true;
    private boolean onRotationToo = true;
    private boolean overrideBack = false;
    private int overrideMode = 0;
    private int overrideLeft = 0;
    private int overrideRight = 0;
    private String leftCustomApp, rightCustomApp;
    private ActivityLauncherUtils mActivityLauncherUtils;
    //endregion
    private int mDirection;
    private Object mNavigationBarInflaterView = null;
    //region pill color
    private boolean colorReplaced = false;
    //endregion

    private boolean mBroadcastRegistered = false;

    private final BroadcastReceiver mSwitchAppReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int side = intent.getIntExtra("side", 0);
            switchApp(side);
        }
    };

    private final BroadcastReceiver mOverrideBackReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int side = intent.getIntExtra("side", 0);
            if (overrideMode == 0) {
                if (overrideLeft == 0) return;
                runAction(overrideLeft, leftCustomApp);
            } else {
                if (side == 0) {
                    if (overrideLeft == 0) return;
                    runAction(overrideLeft, leftCustomApp);
                } else {
                    if (overrideRight == 0) return;
                    runAction(overrideRight, rightCustomApp);
                }
            }
        }
    };


    public GestureNavbarManager(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        //region Back gesture
        leftEnabled = Xprefs.getBoolean("gesture_left", true);
        rightEnabled = Xprefs.getBoolean("gesture_right", true);
        backGestureHeightFractionLeft = Xprefs.getSliderValues("gesture_left_height_double", 100f);
        backGestureHeightFractionRight = Xprefs.getSliderValues("gesture_right_height_double", 100f);
        onRotationToo = Xprefs.getBoolean("gesture_on_rotate", true);
        overrideBack = Xprefs.getBoolean("gesture_override_holdback", false);
        overrideMode = Integer.parseInt(Xprefs.getString("gesture_override_holdback_mode", "0"));
        overrideLeft = Integer.parseInt(Xprefs.getString("gesture_override_holdback_left", "0"));
        overrideRight = Integer.parseInt(Xprefs.getString("gesture_override_holdback_right", "0"));
        leftCustomApp = Xprefs.getString(GESTURE_HOLD_BACK_LEFT_APP, "");
        rightCustomApp = Xprefs.getString(GESTURE_HOLD_BACK_RIGHT_APP, "");
        //endregion

        //region pill size
        widthFactor = Xprefs.getSliderInt("GesPillWidthModPos", 50) * .02f;
        //endregion

        //region pill color
        navPillColorAccent = Xprefs.getBoolean("navPillColorAccent", false);
        //endregion

        if (Key.length > 0) {
            if (Key[0].equals("GesPillWidthModPos") ||
                    Key[0].equals("GesPillHeightFactor") ||
                    Key[0].equals("navPillColorAccent") ||
                    Key[0].equals("navPillCustomColor") ||
                    Key[0].equals("navPillColor")) {
                refreshNavbar();
            }
        }
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        if (!mBroadcastRegistered) {
            IntentFilter filter = new IntentFilter(ACTIONS_SWITCH_APP);
            mContext.registerReceiver(mSwitchAppReceiver, filter, Context.RECEIVER_EXPORTED);
            IntentFilter overrideFilter = new IntentFilter(ACTIONS_OVERRIDE_BACK);
            mContext.registerReceiver(mOverrideBackReceiver, overrideFilter, Context.RECEIVER_EXPORTED);
            mBroadcastRegistered = true;
        }

        ReflectedClass SideGestureDetector = ReflectedClass.of(
                "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureDetector",
                "com.oplusos.systemui.navigationbar.gesture.sidegesture.SideGestureDetector" //OOS13
        );
        ReflectedClass SideGestureNavView = ReflectedClass.of(
                "com.oplus.systemui.navigationbar.gesture.sidegesture.SideGestureNavView",
                "com.oplusos.systemui.navigationbar.gesture.sidegesture.SideGestureNavView" //OOS13
        );

        if (Build.VERSION.SDK_INT >= 34) {
            SideGestureDetector
                    .afterConstruction()
                    .run(param -> {
                        try {
                            SideGestureConfigurationEx = getObjectField(param.thisObject, "mSideGestureConfiguration");
                            mOverviewProxyService = getObjectField(param.thisObject, "mOverviewProxyService");
                        } catch (Throwable ignored) {}
                        mActivityLauncherUtils = new ActivityLauncherUtils(mContext, getActivityStarterExternal());
                    });
            SideGestureDetector
                    .before("onMotionEventImpl")
                            .run(param -> {
                                if (SideGestureConfigurationEx == null) return;
                                MotionEvent ev = (MotionEvent) param.args[0];
                                if (getForegroundApp()[0].equals(getDefaultLauncherPackageName())) return;

                                Point mDisplaySize = (Point) getObjectField(param.thisObject, "mDisplaySize");
                                boolean isLeftSide = ev.getX() < (mDisplaySize.x / 3f);
                                mDirection = isLeftSide ? 0 : 1;
                                if (ev.getActionMasked() == ACTION_DOWN) //down action is enough. once gesture is refused it won't accept further actions
                                {
                                    int mBottomGestureHeight = (int) callMethod(SideGestureConfigurationEx, "getBottomGestureAreaHeight");
                                    int rotation = (int) getFloatField(param.thisObject, "mRotation");
                                    if (notWithinInsets(ev.getX(),
                                            ev.getY(),
                                            mDisplaySize,
                                            mBottomGestureHeight, rotation)) {
                                        setObjectField(param.thisObject, "mAllowGesture", false); //act like the gesture was not good enough
                                        param.setResult(null); //and stop the current method too
                                    }
                                }
                            });
        } else {
            SideGestureDetector
                    .before("isWithinInsets")
                            .run(param -> {
                                int x = (int) param.args[0];
                                int y = (int) param.args[1];


                                if (getForegroundApp()[0].equals(getDefaultLauncherPackageName()))
                                    return;

                                Point mDisplaySize = (Point) getObjectField(param.thisObject, "mDisplaySize");
                                boolean isLeftSide = x < (mDisplaySize.x / 3f);
                                mDirection = isLeftSide ? 0 : 1;
                                int mBottomGestureHeight = mContext.getResources().getDimensionPixelSize(mContext.getResources().getIdentifier("bottom_gesture_area_height", "dimen", listenPackage));
                                int rotation = (int) getFloatField(param.thisObject, "mRotation");
                                if (notWithinInsets(x,
                                        y,
                                        mDisplaySize,
                                        mBottomGestureHeight, rotation)) {
                                    setObjectField(param.thisObject, "mAllowGesture", false); //act like the gesture was not good enough
                                    param.setResult(false); //and stop the current method too
                                }
                            });
        }

        SideGestureNavView
                .before("setAppIcon")
                        .run(param -> {
                            int mPosition = getIntField(param.thisObject, "mPosition");
                            if (overrideBack) {
                                Drawable icon;
                                if (overrideMode == 0) {
                                    if (overrideLeft == 0) return;
                                    icon = getActionIcon(overrideLeft, leftCustomApp);
                                } else {
                                    if (mPosition == 0) {
                                        if (overrideLeft == 0) return;
                                        icon = getActionIcon(overrideLeft, leftCustomApp);
                                    } else {
                                        if (overrideRight == 0) return;
                                        icon = getActionIcon(overrideRight, rightCustomApp);
                                    }
                                }
                                param.args[0] = DrawableConverter.drawableToBitmap(icon);
                            }
                        });

        SideGestureDetector
                .before("switchApp")
                        .run(param -> {
                            int side = (int) callMethod(param.thisObject, "getSide");
                            if (overrideBack) {
                                if (overrideMode == 0) {
                                    if (overrideLeft == 0) return;
                                    runAction(overrideLeft, leftCustomApp);
                                } else {
                                    if (side == 0) {
                                        if (overrideLeft == 0) return;
                                        runAction(overrideLeft, leftCustomApp);
                                    } else {
                                        if (overrideRight == 0) return;
                                        runAction(overrideRight, rightCustomApp);
                                    }
                                }
                                param.setResult(null);
                            }
                        });


        Class<?> OplusNavigationBarInflaterView = findClass("com.oplusos.systemui.navigationbar.OplusNavigationBarInflaterView", lpparam.classLoader);

        Class<?> OplusNavigationHandle;
        try {
            OplusNavigationHandle = findClass("com.oplus.systemui.navigationbar.gesture.sidegesture.OplusNavigationHandle", lpparam.classLoader);
        } catch (Throwable t) {
            OplusNavigationHandle = findClass("com.oplusos.systemui.navigationbar.gesture.sidegesture.OplusNavigationHandle", lpparam.classLoader); // OOS 13
        }
        //region pill color
        hookAllMethods(OplusNavigationHandle, "setDarkIntensity", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (navPillColorAccent || colorReplaced) {
                    setObjectField(param.thisObject, "mLightColor", (navPillColorAccent) ? mContext.getResources().getColor(android.R.color.system_accent1_200, mContext.getTheme()) : mLightColor);
                    setObjectField(param.thisObject, "mDarkColor", (navPillColorAccent) ? mContext.getResources().getColor(android.R.color.system_accent1_600, mContext.getTheme()) : mDarkColor);
                    colorReplaced = true;
                }
            }
        });
        //endregion

        //region pill size
        hookAllMethods(OplusNavigationHandle,
                "setVertical", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (widthFactor != 1f) {
                            View result = (View) param.thisObject;
                            ViewGroup.LayoutParams resultLayoutParams = result.getLayoutParams();
                            int originalWidth;
                            try {
                                originalWidth = (int) getAdditionalInstanceField(param.thisObject, "originalWidth");
                            } catch (Throwable ignored) {
                                originalWidth = resultLayoutParams.width;
                                setAdditionalInstanceField(param.thisObject, "originalWidth", originalWidth);
                            }
                            resultLayoutParams.width = Math.round(originalWidth * widthFactor);
                        }
                    }
                });


        hookAllConstructors(OplusNavigationBarInflaterView, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                mNavigationBarInflaterView = param.thisObject;
                refreshNavbar();
            }
        });

        hookAllMethods(OplusNavigationBarInflaterView,
                "createView", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        if (widthFactor != 1f) {
                            String button = (String) callMethod(param.thisObject, "extractButton", param.args[0]);
                            if (!button.equals("home_handle")) return;

                            View result = (View) param.getResult();
                            ViewGroup.LayoutParams resultLayoutParams = result.getLayoutParams();
                            resultLayoutParams.width = Math.round(resultLayoutParams.width * widthFactor);
                            result.setLayoutParams(resultLayoutParams);
                        }
                    }
                });
        //endregion

    }

    private Drawable getActionIcon(int action, String app) {
        if (action == 11) {
            return getAppIcon(app);
        }
        int resId = switch (action) {
            case 0 -> R.drawable.ic_switch_app;
            case 1 -> R.drawable.ic_clear_all;
            case 2 -> R.drawable.ic_kill;
            case 3 -> R.drawable.ic_screenshot;
            case 4 -> R.drawable.ic_quick_settings;
            case 5 -> R.drawable.ic_one_hand;
            case 6 -> R.drawable.ic_notifications;
            case 7 -> R.drawable.ic_screen_off;
            case 8 -> R.drawable.ic_screenshot_scroll;
            case 9 -> R.drawable.ic_screenshot_area;
            case 10 -> R.drawable.ic_circle_search;
            default -> 0;
        };
        return ResourcesCompat.getDrawable(ResourceManager.modRes,
                resId,
                mContext.getTheme());
    }

    private Drawable getAppIcon(String pkg) {
        try {
            Drawable appIcon = SystemUtils.PackageManager().getApplicationIcon(pkg);
            return DrawableConverter.scaleDrawable(mContext,
                    appIcon,
                    0.2f);
        } catch (Throwable ignored) {
            return ResourcesCompat.getDrawable(ResourceManager.modRes,
                    R.drawable.ic_custom_app,
                    mContext.getTheme());
        }
    }

    private void runAction(int action, String app) {
        switch (action) {
            case 2 -> killForegroundApp();
            case 3 -> takeScreenshot(ScreenshotUtils.ScreenshotType.FULL);
            case 8 -> takeScreenshot(ScreenshotUtils.ScreenshotType.SCROLL);
            case 9 -> takeScreenshot(ScreenshotUtils.ScreenshotType.PARTIAL);
            case 4 -> showQs();
            case 5 -> toggleOneHanded();
            case 6 -> toggleNotifications();
            case 7 ->
                    callMethod(SystemUtils.PowerManager(), "goToSleep", SystemClock.uptimeMillis());
            case 10 -> runCircleToSearch();
            case 11 -> mActivityLauncherUtils.launchApp(app);
        }
    }

    //region Back gesture
    private boolean notWithinInsets(float x, float y, Point mDisplaySize, float mBottomGestureHeight, int rotation) {
        if (y >= (mDisplaySize.y - mBottomGestureHeight)) {
            // Touch are in bottom gesture area
            return false;
        }
        boolean isLeftSide = x < (mDisplaySize.x / 3f);
        if (!onRotationToo &&
                (rotation == 1 ||
                        rotation == 3)) return false;
        if ((isLeftSide && !leftEnabled)
                || (!isLeftSide && !rightEnabled)) {
            return true;
        }


        float topLeft = backGestureHeightFractionLeft.size() == 2 ? backGestureHeightFractionLeft.get(1) / 100f : 1f;
        float topRight = backGestureHeightFractionLeft.size() == 2 ? backGestureHeightFractionRight.get(1) / 100f : 1f;
        float bottomLeft = backGestureHeightFractionLeft.size() == 2 ? backGestureHeightFractionLeft.get(0) / 100f : 0 / 100f;
        float bottomRight = backGestureHeightFractionRight.size() == 2 ? backGestureHeightFractionRight.get(0) / 100f : 0 / 100f;

        return isLeftSide ?
                y < (mDisplaySize.y
                        - mBottomGestureHeight
                        - Math.round(mDisplaySize.y * topLeft))
                        || y > (mDisplaySize.y
                        - mBottomGestureHeight
                        - Math.round(mDisplaySize.y * bottomLeft)) :
                y < (mDisplaySize.y
                        - mBottomGestureHeight
                        - Math.round(mDisplaySize.y * topRight))
                        || y > (mDisplaySize.y
                        - mBottomGestureHeight
                        - Math.round(mDisplaySize.y * bottomRight));

        /*int mEdgeHeight = isLeftSide ?
                Math.round(mDisplaySize.y * backGestureHeightFractionLeft) :
                Math.round(mDisplaySize.y * backGestureHeightFractionRight);

        return mEdgeHeight != 0
                && y < (mDisplaySize.y
                - mBottomGestureHeight
                - mEdgeHeight);*/
    }
    //endregion

    private String[] getForegroundApp() {
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

        String foregroundApp = null;
        String uid = null;
        for (ActivityManager.RunningAppProcessInfo processInfo : processes) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                foregroundApp = processInfo.processName;
                uid = String.valueOf(processInfo.uid);
                break;
            }
        }
        return new String[]{foregroundApp, uid};
    }

    private void killForegroundApp() {
        Handler mainHandler = new Handler(Looper.getMainLooper());
        mainHandler.post(() -> {
            try {
                ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();

                String foregroundApp = null;
                String[] appInfo = getForegroundApp();
                foregroundApp = appInfo[0];

                if (foregroundApp != null && !foregroundApp.equals(SYSTEM_UI) && !foregroundApp.equals(getDefaultLauncherPackageName())) {
                    //am.killBackgroundProcesses(foregroundApp);
                    String finalForegroundApp = foregroundApp;
                    XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("killall " + finalForegroundApp));
                    XPLauncher.enqueueProxyCommand(proxy -> proxy.runCommand("am force-stop " + finalForegroundApp));
                    String appLabel = getApplicationLabel(foregroundApp, mContext.getPackageManager());
                    Toast.makeText(mContext, "Killed: " + appLabel, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mContext, "Nothing to kill.", Toast.LENGTH_SHORT).show();
                }
            } catch (Throwable t) {
                Log.e("KillAppNow", "Error in killForegroundApp", t);
            }
        });
    }

    private void takeScreenshot(ScreenshotUtils.ScreenshotType type) {
        ScreenshotUtils.takeScreenshot(type, mContext, 750L);
    }

    private void showQs() {
        Intent intent = new Intent(ACTIONS_OPEN_QUICK_SETTINGS);
        intent.setPackage(SYSTEM_UI);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcast(intent);
    }

    private void toggleOneHanded() {
        Intent intent = new Intent(ACTIONS_TOGGLE_ONE_HANDED);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setPackage(LAUNCHER);
        mContext.sendBroadcast(intent);
    }

    private void switchApp(int side) {
        if (mOverviewProxyService == null) return;
        try {
            Object proxy = callMethod(mOverviewProxyService, "getProxy");
            if (proxy != null) {
                try {
                callMethod(proxy, "switchApp", side);
                } catch (Throwable t) {
                    // Fallback for OOS15.0.1
                    callMethod(proxy, "switchPreApp", side);
                }
            }
        } catch (Throwable t) {
            log(t);
        }
    }

    private void toggleNotifications() {
        Intent intent = new Intent(ACTIONS_TOGGLE_PANEL);
        intent.setPackage(LAUNCHER);
        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
        mContext.sendBroadcast(intent);
    }

    private void runCircleToSearch() {
        new Handler(Looper.getMainLooper()).postDelayed(AppUtils::circleToSearch, 150L);
    }

    private String getDefaultLauncherPackageName() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = mContext.getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        if (resolveInfo != null) {
            return resolveInfo.activityInfo.packageName;
        }
        return null;
    }

    private String getApplicationLabel(String packageName, PackageManager pm) {
        try {
            ApplicationInfo appInfo = pm.getApplicationInfo(packageName, 0);
            return (String) pm.getApplicationLabel(appInfo);
        } catch (PackageManager.NameNotFoundException e) {
            return packageName; // Fallback to package name if the app label is not found
        }
    }

    //region pill size
    private void refreshNavbar() {
        try {
            callMethod(mNavigationBarInflaterView, "updateLayout");
        } catch (Throwable ignored) {
        }
    }
    //endregion

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(listenPackage);
    }
}
