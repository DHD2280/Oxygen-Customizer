package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static de.robv.android.xposed.XposedHelpers.callMethod;
import static de.robv.android.xposed.XposedHelpers.getAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.getIntField;
import static de.robv.android.xposed.XposedHelpers.getObjectField;
import static de.robv.android.xposed.XposedHelpers.getStaticIntField;
import static de.robv.android.xposed.XposedHelpers.setAdditionalInstanceField;
import static de.robv.android.xposed.XposedHelpers.setBooleanField;
import static de.robv.android.xposed.XposedHelpers.setIntField;
import static de.robv.android.xposed.XposedHelpers.setObjectField;
import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Process;
import android.provider.Settings;
import android.util.Pair;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import de.robv.android.xposed.callbacks.XC_LoadPackage;
import it.dhd.oxygencustomizer.utils.Constants;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class Launcher extends XposedMods {

    private static final String listenPackage = Constants.Packages.LAUNCHER;
    private int mFolderRows, mFolderColumns, mDrawerColumns;
    private boolean mRearrangeHome = false, mFolderRearrange = false, mFolderPreview = false, mDrawerRearrange = false, mOpenAppDetails;
    private boolean mRemoveFolderPagination = false, mRemoveHomePagination = false;
    private int mMaxRows = 6, mMaxColumns = 4;
    private boolean mForceDockAsColumns = false;
    private boolean mHideScroller = false;
    private boolean mHideDesktopLabels = false, mHideDrawerLabels = false;
    private boolean mDisablePreviousRecents = false;
    private boolean mReplaceLock = false;
    private boolean mCustomShelfBehavior = false;
    private int mShelfBehavior = 2;
    // 0 disable discover
    // 1 replace discover with shelf
    // 2 enable discover

    private View OplusFastScroll;

    private ContentObserver assistScreenSwitchObserverExp, shelfSupportAssistScreenObserver;

    public Launcher(Context context) {
        super(context);
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        mFolderRows = Xprefs.getSliderInt("folder_max_rows", 3);
        mFolderColumns = Xprefs.getSliderInt("folder_max_columns", 3);
        mDrawerColumns = Xprefs.getSliderInt("drawer_columns", 4);
        mMaxRows = Xprefs.getSliderInt("launcher_rows", 6);
        mMaxColumns = Xprefs.getSliderInt("launcher_columns", 5);
        mForceDockAsColumns = Xprefs.getBoolean("force_dock_as_columns", false);
        mRearrangeHome = Xprefs.getBoolean("rearrange_home", false);
        mFolderRearrange = Xprefs.getBoolean("rearrange_folder", true);
        mFolderPreview = Xprefs.getBoolean("rearrange_preview", true);
        mDrawerRearrange = Xprefs.getBoolean("rearrange_drawer", true);
        mOpenAppDetails = Xprefs.getBoolean("launcher_open_app_details", false);
        mRemoveFolderPagination = Xprefs.getBoolean("remove_folder_pagination", false);
        mRemoveHomePagination = Xprefs.getBoolean("remove_home_pagination", false);
        mHideScroller = Xprefs.getBoolean("hide_scroller", false);
        mHideDesktopLabels = Xprefs.getBoolean("desktop_hide_app_labels", false);
        mHideDrawerLabels = Xprefs.getBoolean("drawer_hide_app_labels", false);
        mDisablePreviousRecents = Xprefs.getBoolean("disable_previous_recents", false);
        mReplaceLock = Xprefs.getBoolean("replace_lock", false);

        // shelf behavior
        mCustomShelfBehavior = Xprefs.getBoolean("launcher_custom_shelf_switch", false);
        mShelfBehavior = Xprefs.getInt("laucher_shelf_custom", 2);

        if (Key.length > 0) {
            if (Key[0].equals("hide_scroller")) {
                updateFastScroll();
            } else if (Key[0].equals("launcher_custom_shelf_switch") || Key[0].equals("laucher_shelf_custom")) {
                callChanges();
            }
        }

    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!lpparam.packageName.equals(listenPackage)) return;

        ReflectedClass GridOption = ReflectedClass.of("com.android.launcher3.InvariantDeviceProfile$GridOption");
        GridOption
                .afterConstruction()
                .run(param -> {
                    if (mFolderRearrange) {
                        setIntField(param.thisObject, "numFolderColumns", mFolderColumns);
                        setIntField(param.thisObject, "numFolderRows", mFolderRows);
                    }
                    if (mFolderPreview && mFolderColumns > 3)
                        setIntField(param.thisObject, "numFolderPreview", mFolderColumns);
                    if (mDrawerRearrange)
                        setIntField(param.thisObject, "numAllAppsColumns", mDrawerColumns);
                });

        ReflectedClass FolderInfo = ReflectedClass.ofIfPossible("com.android.launcher3.model.data.FolderInfo");
        if (FolderInfo.getClazz() != null) {
            FolderInfo
                    .before("getPreviewRow")
                    .run(param -> {
                        try {
                            int spanX = getIntField(param.thisObject, "spanX");
                            int spanY = getIntField(param.thisObject, "spanY");
                            if (spanX == 1 && spanY == 1 && mFolderPreview) {
                                param.setResult(mFolderRows);
                            }
                        } catch (Throwable t) {
                            log(t);
                        }
                    });

            FolderInfo
                    .before("getPreviewColumn")
                    .run(param -> {
                        try {
                            int spanX = getIntField(param.thisObject, "spanX");
                            int spanY = getIntField(param.thisObject, "spanY");
                            if (spanX == 1 && spanY == 1 && mFolderPreview) {
                                param.setResult(mFolderColumns);
                            }
                        } catch (Throwable t) {
                            log(t);
                        }
                    });
        }

        try {
            ReflectedClass AllAppsParam = ReflectedClass.of("com.android.launcher.layoutparam.AllAppsParam");
            if (AllAppsParam.getClazz() != null) {
                AllAppsParam
                        .after("getNumAllAppsColumns")
                        .run(param -> {
                            if (mDrawerRearrange)
                                param.setResult(mDrawerColumns);
                        });
            }
        } catch (Throwable ignored) {
        }

        ReflectedClass OplusTaskViewImpl = ReflectedClass.of("com.android.quickstep.views.OplusTaskViewImpl");

        OplusTaskViewImpl
                .after("setIcon")
                .run(param -> {
                    View headerView = (View) callMethod(param.thisObject, "getHeaderView");
                    View iconView = (View) callMethod(headerView, "getTaskIcon");
                    View titleView = (View) callMethod(headerView, "getTitleTv");
                    Object task = callMethod(param.thisObject, "getTask");
                    if (task == null) return;
                    Object key = getObjectField(task, "key");
                    if (key == null) return;
                    String pkgName = (String) callMethod(key, "getPackageName");
                    int userId = getIntField(key, "userId");
                    final ClickListener clickListener = new ClickListener(pkgName, userId);

                    iconView.setOnLongClickListener(clickListener);
                    titleView.setOnLongClickListener(clickListener);
                });

        ReflectedClass DockIconView = ReflectedClass.of("com.oplus.quickstep.dock.DockIconView");
        DockIconView
                .after("setIcon")
                .run(param -> {
                    Object task = callMethod(param.thisObject, "getTask");
                    if (task == null) return;
                    Object key = getObjectField(task, "key");
                    if (key == null) return;
                    String pkgName = (String) callMethod(key, "getPackageName");
                    int userId = getIntField(key, "userId");
                    View iconView = (View) param.thisObject;
                    final ClickListener clickListener = new ClickListener(pkgName, userId);
                    iconView.setOnLongClickListener(clickListener);
                });

        try {
            ReflectedClass.ReflectionConsumer pageIndicator = param -> {
                if (!mRemoveHomePagination && !mRemoveFolderPagination) return;
                View v = (View) param.thisObject;
                if (v.getParent() == null) return;
                switch (v.getParent().getClass().getCanonicalName()) {
                    case "com.android.launcher3.OplusDragLayer":
                        if (mRemoveHomePagination) {
                            v.setVisibility(View.GONE);
                            param.setResult(null);
                        }
                        break;
                    case "android.widget.FrameLayout":
                        if (mRemoveFolderPagination) {
                            v.setVisibility(View.GONE);
                            param.setResult(null);
                        }
                        break;
                    default:
                        break;
                }
            };
            ReflectedClass OplusPageIndicator = ReflectedClass.of("com.android.launcher.pageindicators.OplusPageIndicator");
            OplusPageIndicator
                    .before("dispatchDraw")
                    .run(pageIndicator);
            OplusPageIndicator
                    .before("onDraw")
                    .run(pageIndicator);
        } catch (Throwable t) {
            log(t);
        }

        try {
            ReflectedClass PageIndicatorTouchHelper = ReflectedClass.of("com.android.launcher.pageindicators.PageIndicatorTouchHelper");
            PageIndicatorTouchHelper
                    .before("dispatchTouchEvent")
                    .run(param -> {
                        if (mRemoveHomePagination) param.setResult(false);
                    });
        } catch (Throwable ignored) {
        }

        try {
            ReflectedClass UiConfig = ReflectedClass.of("com.android.launcher.UiConfig");
            UiConfig
                    .before("isSupportLayout")
                    .run(param -> {
                        if (mRearrangeHome) param.setResult(true);
                    });

            if (Build.VERSION.SDK_INT >= 36) { //OOS16
                UiConfig
                        .before("getSupportLayout")
                        .run(param -> {
                            /*
                                List<Pair<Integer, Pair<Integer, Integer>>> getLayoutNormal()
                             */
                            if (!mRearrangeHome) return;
                            ArrayList<Pair<Integer, Pair<Integer, Integer>>> arrayList = new ArrayList<>();

                            int MIN_COLS = 3;
                            int MIN_ROWS = 4;

                            for (int col = MIN_COLS; col <= mMaxColumns; col++) {
                                for (int row = MIN_ROWS; row <= mMaxRows; row++) {
                                    arrayList.add(Pair.create(col, Pair.create(row, row)));

                                }
                            }
                            param.setResult(arrayList);
                        });
            }

            ReflectedClass ToggleBarLayoutAdapter = ReflectedClass.of("com.android.launcher.togglebar.adapter.ToggleBarLayoutAdapter");
            ToggleBarLayoutAdapter
                    .before("initToggleBarLayoutConfigs")
                    .run(param -> {
                        if (!mRearrangeHome) return;
                        int[] mMinMaxRows = (int[]) getObjectField(param.thisObject, "MIN_MAX_ROW");
                        int[] mMinMaxColumns = (int[]) getObjectField(param.thisObject, "MIN_MAX_COLUMN");
                        mMinMaxRows[1] = mMaxRows;
                        mMinMaxColumns[1] = mMaxColumns;
                        setObjectField(param.thisObject, "MIN_MAX_ROW", mMinMaxRows);
                        setObjectField(param.thisObject, "MIN_MAX_COLUMN", mMinMaxColumns);
                    });
        } catch (Throwable t) {
            log("Error in Launcher Layout " + t);
        }

        ReflectedClass OplusFastScrollLayoutClass = ReflectedClass.of(
                "com.android.launcher3.allapps.OplusFastScrollLayout", /* OOS 14-15 */
                "com.android.launcher3.allapps.LetterIndexFastScrollHelper" // OOS 13
        );
        if (OplusFastScrollLayoutClass.getClazz() != null) {
            OplusFastScrollLayoutClass
                    .afterConstruction()
                    .run(param -> {
                        if (param.getClass().getSimpleName().contains("LetterIndexFastScrollHelper")) { // OOS13
                            OplusFastScroll = (View) getObjectField(param.thisObject, "mFastScroll");
                        } else {
                            OplusFastScroll = (View) param.thisObject;
                        }
                        updateFastScroll();
                    });
        }

        // App Labels
        ReflectedClass BubbleTextView = ReflectedClass.of("com.android.launcher3.BubbleTextView");
        BubbleTextView
                .before("applyLabel")
                .run(param -> {
                    int mDisplay = getIntField(param.thisObject, "mDisplay");
                    int DRAWER_DISPLAY = 1;
                    try {
                        DRAWER_DISPLAY = getStaticIntField(BubbleTextView.getClazz(), "DISPLAY_ALL_APPS");
                    } catch (Throwable ignored) {
                    }
                    if (mDisplay == DRAWER_DISPLAY) {
                        if (mHideDrawerLabels) {
                            param.setResult(null);
                        }
                    } else {
                        if (mHideDesktopLabels) {
                            param.setResult(null);
                        }
                    }
                });

        ReflectedClass FeatureOption = ReflectedClass.of("com.android.common.config.FeatureOption");
        FeatureOption
                .before("isDockerMax5")
                .run(param -> {
                    if (!mForceDockAsColumns) return;
                    param.setResult(false);
                });

        ReflectedClass AppFeatureUtils = ReflectedClass.of("com.android.common.util.AppFeatureUtils");
        AppFeatureUtils
                .before("isSupportAutoFocusToNextPageInOverviewState")
                .run(param -> {
                    if (mDisablePreviousRecents) {
                        param.setResult(false);
                    }
                });

        ReflectedClass OplusStackTaskViewTouchCtrl = ReflectedClass.ofIfPossible("com.android.quickstep.uioverrides.touchcontrollers.OplusStackTaskViewTouchCtrl");
        OplusStackTaskViewTouchCtrl
                .before("onDragEnd")
                .run(param -> {
                    if (!mReplaceLock) return;
                    // check orientation first
                    Object recentsView = getObjectField(param.thisObject, "mRecentsView");
                    Object pagedOrientationHandler = callMethod(recentsView, "getPagedOrientationHandler");
                    int rotation = (Integer) callMethod(pagedOrientationHandler, "getRotation");
                    float mDisplacement = (float) getObjectField(param.thisObject, "mDisplacement");
                    boolean mIsRtl = (boolean) getObjectField(param.thisObject, "mIsRtl");

                    boolean shouldLock = false;
                    if (rotation == 1) {
                        shouldLock = mIsRtl ? (mDisplacement > 100.0f) : (mDisplacement < -100.0f);
                    } else if (rotation == 3) {
                        shouldLock = mIsRtl ? (mDisplacement < -100.0f) : (mDisplacement > 100.0f);
                    } else {
                        // Rotation 0 o 2 (Portrait)
                        shouldLock = (mDisplacement > 200.0f);
                    }

                    if (!shouldLock) {
                        return;
                    }
                    param.setResult(null);
                    Object task = null;
                    Object taskView = getObjectField(param.thisObject, "mTaskBeingDragged");
                    if (taskView != null) task = callMethod(taskView, "getTask");
                    if (taskView == null && task == null) return;
                    String packageName = (String) callMethod(task, "getPackageName");
                    callMethod(mContext.getSystemService(Context.ACTIVITY_SERVICE),
                            "forceStopPackageAsUser",
                            packageName,
                            callMethod(Process.myUserHandle(), "getIdentifier"));
                    callMethod(taskView, "performAccessibilityAction", mContext.getResources().getIdentifier("accessibility_close", "string", LAUNCHER), null);
                    Toast.makeText(mContext, "App Killed", Toast.LENGTH_SHORT).show();

                });

        ReflectedClass OplusTaskViewTouchControllerImpl = ReflectedClass.ofIfPossible("com.android.quickstep.uioverrides.touchcontrollers.OplusTaskViewTouchControllerImpl");
        OplusTaskViewTouchControllerImpl
                .before("onDragEnd")
                .run(param -> {

                    if (!mReplaceLock) return;

                    setAdditionalInstanceField(getObjectField(param.thisObject, "mTaskBeingDragged"), "mShouldKill", true);

                });

        ReflectedClass.ReflectionConsumer killHook = param -> {
            if (!mReplaceLock) return;

            Object task = null;
            Object TaskView = param.args[0];
            boolean shouldLock = false;
            try {
                shouldLock = (boolean) getAdditionalInstanceField(TaskView, "mShouldKill");
            } catch (Throwable ignored) {
            }

            if (!shouldLock) return;
            param.setResult(null);
            if (TaskView != null) task = callMethod(TaskView, "getTask");
            if (TaskView == null && task == null) return;
            String packageName = (String) callMethod(task, "getPackageName");
            callMethod(mContext.getSystemService(Context.ACTIVITY_SERVICE),
                    "forceStopPackageAsUser",
                    packageName,
                    callMethod(Process.myUserHandle(), "getIdentifier"));
            callMethod(TaskView, "performAccessibilityAction", mContext.getResources().getIdentifier("accessibility_close", "string", LAUNCHER), null);
            Toast.makeText(mContext, "App Killed", Toast.LENGTH_SHORT).show();
        };


        ReflectedClass OplusLockManager = ReflectedClass.ofIfPossible("com.oplus.quickstep.applock.OplusLockManager");
        OplusLockManager
                .before("unLockForPullDown")
                .run(killHook);
        OplusLockManager
                .before("lockForPullDown")
                .run(killHook);


        ReflectedClass LauncherClient = ReflectedClass.ofIfPossible("com.google.android.libraries.gsa.launcherclient.LauncherClient");
        LauncherClient
                .afterConstruction()
                .run(param -> {
                    try {
                        assistScreenSwitchObserverExp = (ContentObserver) getObjectField(param.thisObject, "mAssistScreenSwitchObserverExp");
                    } catch (Throwable ignored) {
                    }
                    try {
                        shelfSupportAssistScreenObserver = (ContentObserver) getObjectField(param.thisObject, "mShelfSupportAssistScreenObserver");
                    } catch (Throwable ignored) {
                    }

                });


        ReflectedClass OverlayProxy = ReflectedClass.ofIfPossible("com.android.overlay.OverlayProxy");
        OverlayProxy
                .before("getAssistScreenType")
                .run(param -> {
                    if (!mCustomShelfBehavior) return;
                    param.setResult(mShelfBehavior);
                });
        OverlayProxy
                .before("setAssistScreenType")
                .run(param -> {
                    if (!mCustomShelfBehavior) return;
                    param.args[0] = mShelfBehavior;
                });
        FeatureOption
                .before("getSShelfAssistantEnable")
                .run(param -> {
                    if (!mCustomShelfBehavior) return;
                    param.setResult(mShelfBehavior == 0);
                });
        FeatureOption
                .before("updateSupportShelfAssistant")
                .run(param -> {
                    if (!mCustomShelfBehavior) return;
                    setBooleanField(param.thisObject, "sShelfAssistantEnable", mShelfBehavior == 0);
                });

    }

    private void updateFastScroll() {
        if (OplusFastScroll == null) return;
        OplusFastScroll.setVisibility(mHideScroller ? View.GONE : View.VISIBLE);
    }

    private void callChanges() {
        if (assistScreenSwitchObserverExp != null)
            assistScreenSwitchObserverExp.onChange(true);
        if (shelfSupportAssistScreenObserver != null)
            shelfSupportAssistScreenObserver.onChange(true);
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }

    class ClickListener implements View.OnLongClickListener {

        final String pkgName;
        final int userId;

        public ClickListener(String pkgName, int userId) {
            this.pkgName = pkgName;
            this.userId = userId;
        }

        @Override
        public boolean onLongClick(View v) {
            if (!mOpenAppDetails) return false;
            Intent appDetails = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", pkgName, null));
            appDetails.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            appDetails.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            appDetails.putExtra("userId", userId);
            mContext.startActivity(appDetails);
            return true;
        }
    }
}
