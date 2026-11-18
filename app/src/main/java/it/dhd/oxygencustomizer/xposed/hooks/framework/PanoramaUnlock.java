package it.dhd.oxygencustomizer.xposed.hooks.framework;

import static it.dhd.oxygencustomizer.utils.Constants.Preferences.PanoramaUnlock.PANORAMA_FLOATING_WINDOWS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.PanoramaUnlock.PANORAMA_MAX_WINDOWS;
import static it.dhd.oxygencustomizer.utils.Constants.Preferences.PanoramaUnlock.PANORAMA_THREE_SPLIT;
import static it.dhd.oxygencustomizer.xposed.XPrefs.Xprefs;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.callMethod;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.getObjectField;
import static it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectionHelpers.setStaticBooleanField;

import android.content.Context;
import android.content.res.Configuration;
import android.view.SurfaceControl;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import io.github.libxposed.api.XposedModuleInterface;
import it.dhd.oxygencustomizer.xposed.FrameworkMods;
import it.dhd.oxygencustomizer.xposed.utils.toolkit.ReflectedClass;

public class PanoramaUnlock extends FrameworkMods {

    private static final String PANORAMA_UTIL = "com.android.server.wm.OplusPanoramaWorkBranchUtil";
    private static final String FLEXIBLE_WINDOW_UTILS = "com.android.server.wm.FlexibleWindowUtils";
    private static final String FLEXIBLE_WMS = "com.android.server.wm.FlexibleWindowManagerService";
    private static final String FLEXIBLE_TASK_CONTROLLER = "com.android.server.wm.FlexibleTaskController";
    private static final String ZOOM_WINDOW_CONFIG = "com.android.server.wm.OplusZoomWindowConfig";
    private static final String ZOOM_WMS = "com.android.server.wm.OplusZoomWindowManagerService";
    private static final String CANVAS_LAYOUT_RECT = "com.oplus.flexiblewindow.CanvasCalculateLayoutRect";
    private static final String MINI_SWITCH_FEATURE = "mini_window_switch_to_fullscreen";
    private static final int HIDE_ZOOM_SWIPE_UP = 12;
    private static final int THREE_PANES = 3;
    private static final int LAYOUT_ONE_TOP_TWO_BOTTOM = 4;

    private final ReflectedClass.ReflectionConsumer RETURN_TRUE = split(param -> param.setResult(true));
    private final ReflectedClass.ReflectionConsumer RETURN_FALSE = split(param -> param.setResult(false));

    private final Map<Integer, Object> mTasks = new ConcurrentHashMap<>();
    private ReflectedClass mTaskController;
    private boolean mThreeSplit = true;
    private boolean mFloatingWindows = true;
    private int mMaxWindows = 3;

    public PanoramaUnlock(Context context) {
        super(context);
    }

    @Override
    protected void loadSettings() {
        mThreeSplit = Xprefs.getBoolean(PANORAMA_THREE_SPLIT, false);
        mFloatingWindows = Xprefs.getBoolean(PANORAMA_FLOATING_WINDOWS, false);
        mMaxWindows = Xprefs.getSliderInt(PANORAMA_MAX_WINDOWS, 3);
        if (mThreeSplit) clearMiniSwitch();
    }

    @Override
    protected void onFrameworkLoaded(XposedModuleInterface.PackageReadyParam PRParam) throws Throwable {
        hookMaxWinNum();

        ReflectedClass util = ReflectedClass.ofIfPossible(PANORAMA_UTIL);
        if (util.getClazz() == null) {
            log("PanoramaUnlock: OplusPanoramaWorkBranchUtil not found");
            return;
        }

        int n = util.after("hasTabletPanoramaWorkFeature").run(RETURN_TRUE).size();
        n += util.after("hasPanoramaWorkFeature").run(RETURN_TRUE).size();
        n += util.after("isPanoramaWorkBranchEnable").run(RETURN_TRUE).size();
        n += util.after("isPanoramaWorkBranchExcludeSmallDevice").run(RETURN_TRUE).size();
        n += util.after("hasFoldPanoramaWorkFeature").run(RETURN_FALSE).size();
        n += util.after("isFoldPanoramaWorkEnable").run(RETURN_FALSE).size();
        n += util.after("isPanoramaWorkBranchForSmallDevice").run(RETURN_FALSE).size();
        n += util.after("isPanoramaForcedDisable").run(RETURN_FALSE).size();
        log("PanoramaUnlock: panorama branch hooks=" + n);

        hookWindowLimit();
        hookLayoutPolicy();
        hookZoomWindow();
        hookTaskLayoutPolicy();
        hookMultiWindowTrace();
        hookTaskCapture();
        hookMultiZoomWhitelist();
        hookMiniWindowFeature();
        hookHideZoomWindow();
        hookZoomSupport();
    }

    private void hookWindowLimit() {
        ReflectedClass windowUtils = ReflectedClass.ofIfPossible(FLEXIBLE_WINDOW_UTILS);
        if (windowUtils.getClazz() == null) {
            log("PanoramaUnlock: FlexibleWindowUtils not found");
            return;
        }
        int n = after(windowUtils, "getMaxFlexibleTaskNum", m -> m.getReturnType() == int.class,
                param -> log("PanoramaUnlock: getMaxFlexibleTaskNum -> " + param.getResult()));
        n += windowUtils.after("isSmallDeviceMultiFlexibleEnabled").run(RETURN_FALSE).size();
        log("PanoramaUnlock: window limit hooks=" + n);
    }

    private void hookTaskLayoutPolicy() {
        ReflectedClass wms = ReflectedClass.ofIfPossible(FLEXIBLE_WMS);
        int n = after(wms, "getTaskLayoutPolicy",
                m -> m.getParameterCount() == 1 && m.getReturnType() != void.class,
                param -> {
                    try {
                        Object tablet = getObjectField(param.thisObject, "mTabletCanvasLayoutPolicy");
                        if (tablet != null) param.setResult(tablet);
                    } catch (Throwable ignored) {
                    }
                });
        log("PanoramaUnlock: task layout policy hooks=" + n);
    }

    private void hookMultiWindowTrace() {
        ReflectedClass controller = ReflectedClass.ofIfPossible(FLEXIBLE_TASK_CONTROLLER);
        int n = before(controller, "toggleMultiFlexibleWindowFromCanvas", m -> m.getParameterCount() >= 2, param -> {
            int[] ids = param.args[0] instanceof int[] array ? array : null;
            int bounds = param.args[1] instanceof List<?> list ? list.size() : -1;
            log("PanoramaUnlock: toggleMulti ids=" + (ids != null ? ids.length : -1) + " bounds=" + bounds + " " + Arrays.toString(ids));
        });
        n += hook(controller, "finishDragAnimationAndStartFlexibleTask", m -> m.getParameterCount() >= 1,
                param -> log("PanoramaUnlock: finishDrag taskId=" + param.args[0] + " rect=" + param.args[1] + " scale=" + param.args[3]),
                param -> {
                    if (!(param.args[0] instanceof Integer taskId)) return;
                    Object task = mTasks.get(taskId);
                    if (task == null) return;
                    try {
                        SurfaceControl surface = (SurfaceControl) callMethod(task, "getSurfaceControl");
                        if (surface == null) return;
                        new SurfaceControl.Transaction().setAlpha(surface, 1f).apply();
                        log("PanoramaUnlock: restored alpha taskId=" + taskId);
                    } catch (Throwable t) {
                        log("PanoramaUnlock: restore alpha failed " + t.getMessage());
                    }
                });
        n += controller.before("resetFlexibleTaskToFullScreen").run(split(param -> {
            StackTraceElement[] stack = new Throwable().getStackTrace();
            for (StackTraceElement element : stack) {
                String className = element.getClassName();
                if (className.contains("XC_MethodHook") || className.contains("XposedBridge")) {
                    param.setResult(null);
                    log("PanoramaUnlock: blocked external reset via " + className);
                    return;
                }
            }
            StringBuilder trace = new StringBuilder("PanoramaUnlock: resetToFullScreen stack:");
            for (int i = 1; i < Math.min(stack.length, 7); i++) {
                trace.append(" | ").append(stack[i].getClassName()).append('#').append(stack[i].getMethodName());
            }
            log(trace.toString());
        })).size();
        n += controller.before("onRecentClicked").run(split(param -> log("PanoramaUnlock: onRecentClicked called"))).size();
        log("PanoramaUnlock: multi window trace hooks=" + n);
    }

    private void hookMaxWinNum() {
        ReflectedClass wms = ReflectedClass.ofIfPossible(FLEXIBLE_WMS);
        int n = 0;
        for (Method method : methods(wms, "getMaxWinNum", m -> m.getReturnType() == int.class && m.getParameterCount() == 1)) {
            n += wms.after(method).run(param -> {
                if (!mFloatingWindows) return;
                Object original = param.getResult();
                param.setResult(mMaxWindows);
                log("PanoramaUnlock: getMaxWinNum scenario=" + param.args[0] + " " + original + " -> " + mMaxWindows);
            }).size();
        }
        log("PanoramaUnlock: max win num hooks=" + n);
    }

    private void hookZoomSupport() {
        int n = ReflectedClass.ofIfPossible(ZOOM_WINDOW_CONFIG).before("isSupportZoomMode").run(RETURN_TRUE).size();
        n += before(ReflectedClass.ofIfPossible(FLEXIBLE_WINDOW_UTILS), "isSupportFlexibleWindow", m -> m.getReturnType() == boolean.class
                && m.getParameterCount() == 2
                && m.getParameterTypes()[0] == String.class
                && m.getParameterTypes()[1] == String.class, RETURN_TRUE);
        log("PanoramaUnlock: zoom support hooks=" + n);
    }

    private void hookHideZoomWindow() {
        ReflectedClass controller = ReflectedClass.ofIfPossible(FLEXIBLE_TASK_CONTROLLER);
        int n = before(controller, "hideZoomWindow", m -> m.getParameterCount() == 1 && m.getParameterTypes()[0] == int.class, param -> {
            if (param.args[0] instanceof Integer reason && reason == HIDE_ZOOM_SWIPE_UP) {
                param.setResult(null);
                log("PanoramaUnlock: blocked hideZoomWindow " + HIDE_ZOOM_SWIPE_UP);
            }
        });
        log("PanoramaUnlock: hide zoom window hooks=" + n);
    }

    private void hookMiniWindowFeature() {
        ReflectedClass featureConfig = ReflectedClass.of("com.oplus.content.OplusFeatureConfigManager", "com.oplus.content.OplusFeatureConfigManagerInternal");
        int n = after(featureConfig, "hasFeature", m -> m.getParameterCount() == 1
                && m.getParameterTypes()[0] == String.class
                && m.getReturnType() == boolean.class, param -> {
            if (param.args[0] instanceof String feature && feature.contains(MINI_SWITCH_FEATURE)) {
                param.setResult(false);
            }
        });
        mTaskController = ReflectedClass.ofIfPossible(FLEXIBLE_TASK_CONTROLLER);
        if (mThreeSplit) clearMiniSwitch();
        log("PanoramaUnlock: mini window feature hooks=" + n);
    }

    private void clearMiniSwitch() {
        if (mTaskController == null || mTaskController.getClazz() == null) return;
        try {
            setStaticBooleanField(mTaskController.getClazz(), "sIsMiniSwitchToFullscreen", false);
            log("PanoramaUnlock: sIsMiniSwitchToFullscreen cleared");
        } catch (Throwable t) {
            log("PanoramaUnlock: clear sIsMiniSwitchToFullscreen failed " + t.getMessage());
        }
    }

    private ReflectedClass.ReflectionConsumer split(ReflectedClass.ReflectionConsumer body) {
        return param -> {
            if (mThreeSplit) body.run(param);
        };
    }

    private void hookMultiZoomWhitelist() {
        ReflectedClass service = ReflectedClass.ofIfPossible(ZOOM_WMS);
        if (service.getClazz() == null) {
            log("PanoramaUnlock: OplusZoomWindowManagerService not found");
            return;
        }
        log("PanoramaUnlock: multi zoom whitelist hooks=" + service.after("isZoomSupportMultiWindow").run(RETURN_TRUE).size());
    }

    private void hookTaskCapture() {
        ReflectedClass owner = ReflectedClass.of(FLEXIBLE_WMS, FLEXIBLE_TASK_CONTROLLER);
        int n = before(owner, "finishSplitDragAnimationAndStartFlexible",
                m -> m.getParameterCount() >= 1 && m.getParameterTypes()[0].getName().endsWith("Task"), param -> {
                    Object task = param.args[0];
                    if (task == null) return;
                    try {
                        if (getObjectField(task, "mTaskId") instanceof Integer id)
                            mTasks.put(id, task);
                    } catch (Throwable ignored) {
                    }
                });
        log("PanoramaUnlock: task capture hooks=" + n);
    }

    private void hookZoomWindow() {
        ReflectedClass config = ReflectedClass.ofIfPossible(ZOOM_WINDOW_CONFIG);
        if (config.getClazz() == null) {
            log("PanoramaUnlock: OplusZoomWindowConfig not found");
            return;
        }
        log("PanoramaUnlock: zoom window hooks=" + config.after("isZoomWindowEnabled").run(RETURN_TRUE).size());
    }

    private void hookLayoutPolicy() {
        ReflectedClass layoutRect = ReflectedClass.ofIfPossible(CANVAS_LAYOUT_RECT);
        if (layoutRect.getClazz() == null) {
            log("PanoramaUnlock: CanvasCalculateLayoutRect not found");
            return;
        }
        int n = layoutRect.after("isTabletDevice").run(RETURN_TRUE).size();
        n += layoutRect.after("isPhoneDisplay").run(RETURN_FALSE).size();
        n += forcePortraitThreeSplit();
        log("PanoramaUnlock: layout policy hooks=" + n);
    }

    private int forcePortraitThreeSplit() {
        ReflectedClass policy = ReflectedClass.of("com.oplus.flexiblewindow.TabletCanvasCalculateLayoutRectPolicy",
                "com.oplus.flexiblewindow.DefaultCanvasCalculateLayoutRectPolicy");
        return after(policy, "computeLayoutOrientation", m -> m.getReturnType() == int.class && m.getParameterCount() == 4, param -> {
            if (!(param.args[0] instanceof List<?> panes) || !(param.args[3] instanceof Context context))
                return;
            if (panes.size() != THREE_PANES) return;
            Configuration configuration = context.getResources().getConfiguration();
            if (configuration.screenHeightDp > configuration.screenWidthDp) {
                param.setResult(LAYOUT_ONE_TOP_TWO_BOTTOM);
            }
        });
    }

    private List<Method> methods(ReflectedClass reflected, String name, Predicate<Method> filter) {
        List<Method> out = new ArrayList<>();
        Class<?> clazz = reflected.getClazz();
        if (clazz == null) return out;
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(name) && !Modifier.isAbstract(method.getModifiers()) && filter.test(method)) {
                out.add(method);
            }
        }
        return out;
    }

    private int before(ReflectedClass reflected, String name, Predicate<Method> filter, ReflectedClass.ReflectionConsumer body) {
        int count = 0;
        for (Method method : methods(reflected, name, filter)) {
            try {
                count += reflected.before(method).run(split(body)).size();
            } catch (Throwable t) {
                log("PanoramaUnlock: hook " + name + " failed " + t.getMessage());
            }
        }
        return count;
    }

    private int after(ReflectedClass reflected, String name, Predicate<Method> filter, ReflectedClass.ReflectionConsumer body) {
        int count = 0;
        for (Method method : methods(reflected, name, filter)) {
            try {
                count += reflected.after(method).run(split(body)).size();
            } catch (Throwable t) {
                log("PanoramaUnlock: hook " + name + " failed " + t.getMessage());
            }
        }
        return count;
    }

    private int hook(ReflectedClass reflected, String name, Predicate<Method> filter, ReflectedClass.ReflectionConsumer before, ReflectedClass.ReflectionConsumer after) {
        int count = 0;
        for (Method method : methods(reflected, name, filter)) {
            try {
                count += reflected.hook(method).run(split(before), split(after)).size();
            } catch (Throwable t) {
                log("PanoramaUnlock: hook " + name + " failed " + t.getMessage());
            }
        }
        return count;
    }
}
