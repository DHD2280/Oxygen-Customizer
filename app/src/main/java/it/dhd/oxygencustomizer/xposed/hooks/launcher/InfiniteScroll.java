package it.dhd.oxygencustomizer.xposed.hooks.launcher;

import static it.dhd.oxygencustomizer.utils.Constants.Packages.LAUNCHER;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getBooleanField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getIntField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getStaticObjectField;

import android.content.Context;
import android.view.ViewGroup;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.XposedMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class InfiniteScroll extends XposedMods {

    private static final String listenPackage = LAUNCHER;

    private boolean mInfiniteScroll = false;

    private ReflectedClass mWorkspaceClass = null;
    private Object mNormalState = null;

    public InfiniteScroll(Context context) {
        super(context);
    }

    @Override
    public void onPreferenceUpdated(String... Key) {
        mInfiniteScroll = Xprefs.getBoolean("launcher_infinite_scroll", false);
    }

    @Override
    public void onPackageLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        mWorkspaceClass = ReflectedClass.ofIfPossible("com.android.launcher3.Workspace");
        ReflectedClass mLauncherState = ReflectedClass.ofIfPossible("com.android.launcher3.LauncherState");

        if (mWorkspaceClass.getClazz() == null || mLauncherState.getClazz() == null) {
            log("Workspace or LauncherState not found");
            return;
        }
        mNormalState = getStaticObjectField(mLauncherState.getClazz(), "NORMAL");

        ReflectedClass OplusPagedViewImpl = ReflectedClass.ofIfPossible("com.android.launcher.OplusPagedViewImpl");

        OplusPagedViewImpl
                .before("interceptUpEventWhenDragged")
                .run(param -> {
                    if (!mInfiniteScroll || param.args.length != 7) return;
                    try {
                        Integer target = getWrapTarget(param.thisObject,
                                (int) param.args[0], (float) param.args[2],
                                (boolean) param.args[4], (boolean) param.args[5], (boolean) param.args[6]);
                        if (target == null) return;
                        callMethod(param.thisObject, "snapToPage", target);
                        param.setResult(true);
                    } catch (Throwable t) {
                        log(t);
                    }
                });
    }

    private Integer getWrapTarget(Object pagedView, int velocity, float delta,
                                  boolean isSignificantMove, boolean passedSlop, boolean isFling) {
        if (!mWorkspaceClass.getClazz().isInstance(pagedView)) return null;
        if (getBooleanField(pagedView, "mFreeScroll")) return null;

        Object launcher = getObjectField(pagedView, "mLauncher");
        if (launcher == null || !(boolean) callMethod(launcher, "isInState", mNormalState))
            return null;

        boolean flinging = isFling && passedSlop;
        if (!isSignificantMove && !flinging) return null;

        ViewGroup workspace = (ViewGroup) pagedView;
        int panelCount = (int) callMethod(pagedView, "getPanelCount");
        int childCount = workspace.getChildCount();
        if (childCount <= panelCount) return null;

        boolean rtl = getBooleanField(pagedView, "mIsRtl");
        boolean forward = flinging
                ? (rtl ? velocity > 0 : velocity < 0)
                : (rtl ? delta > 0 : delta < 0);

        int currentPage = getIntField(pagedView, "mCurrentPage");
        int lastPage = childCount - panelCount;

        if (forward && currentPage >= lastPage) {
            return 0;
        }
        if (!forward && currentPage < panelCount && !(boolean) callMethod(pagedView, "hasOverlay")) {
            return lastPage;
        }
        return null;
    }

    @Override
    public boolean listensTo(String packageName) {
        return listenPackage.equals(packageName);
    }
}
